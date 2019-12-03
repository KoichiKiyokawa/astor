package fr.inria.astor.approaches.purpose_simularity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.File;
import java.io.IOException;

import spoon.reflect.declaration.CtElement;
import fr.inria.astor.core.entities.HasCommitMessage;
import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientPool;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientSearchStrategy;
import fr.inria.astor.approaches.jgenprog.operators.ReplaceOp;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

public class PurposeBasedSearchStrategy extends IngredientSearchStrategy {

  private String javaFilePath;
  public String originalProjectRootDir;

  private List<CtElement> locationsAnalyzed = new ArrayList<>();

  protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass().getName());

  public PurposeBasedSearchStrategy(IngredientPool space, String originalProjectRootDir, String javaFilePath) {
    super(space);
    this.javaFilePath = javaFilePath;
    this.originalProjectRootDir = originalProjectRootDir;
  }

  private File getSavedModelFile() {
    // ex) Math-2 -> math
    String id = ConfigurationProperties.getProperty("projectIdentifier").split("-")[0].toLowerCase();
    return new File(String.format("/astor/models/%s.txt", id));
  }

  @SuppressWarnings("unchecked")
  @Override
  public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {

    modificationPoint.setCommitMessage(this.originalProjectRootDir, this.javaFilePath);

    List<Ingredient> baseElements = getIngredientsFromSpace(modificationPoint, operationType);

    if (baseElements == null || baseElements.isEmpty()) {
      return null;
    }

    for (HasCommitMessage elem : baseElements) {
      elem.setCommitMessage(this.originalProjectRootDir, this.javaFilePath);
    }

    // We store the location to avoid sorting the ingredient twice.
    if (!locationsAnalyzed.contains(modificationPoint.getCodeElement())) {
      locationsAnalyzed.add(modificationPoint.getCodeElement());

      // We have never analyze this location, let's sort the ingredients.
      try {
        ParagraphVectors vec = WordVectorSerializer.readParagraphVectors(getSavedModelFile());
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());
        vec.setTokenizerFactory(t);
        Collections.sort(baseElements, new Comparator<Ingredient>() {
          @Override
          public int compare(Ingredient ingredientA, Ingredient ingredientB) {

            if (ingredientA.commitMessage.length() == 0) {
              if (ingredientB.commitMessage.length() == 0) {
                return 0;
              }
              // ingredientAのコミットメッセージが空かつ、
              // ingredientBのコミットメッセージが空だったら、Aをうしろに
              return 1;
            }

            // 同じコミットメッセージのものは優先して使わない
            if (modificationPoint.commitMessage.equals(ingredientA.commitMessage)) {
              if (modificationPoint.commitMessage.equals(ingredientB.commitMessage)) {
                // ingredientAもingredientBもmodificationPointと同じコミットメッセージだったら、
                // 一致していると定義
                return 0;
              } else {
                // modificationPointとingredientAのコミットメッセージが同じ かつ
                // modificationPointとingredientBのコミットメッセージが違う とき、
                // ingredientAをうしろに
                return 1;
              }
            } else {
              if (modificationPoint.commitMessage.equals(ingredientB.commitMessage)) {
                // modificationPointとingredientAのコミットメッセージが違う かつ
                // modificationPointとingredientBのコミットメッセージが同じ とき、
                // そのまま
                return -1;
              }
            }

            // modificationPointとingredientAのコミットメッセージが違う かつ
            // modificationPointとingredientBのコミットメッセージが違う とき、
            // コサイン類似度で並び替える

            // コサイン類似度を測定
            // caution! モデルの中に存在していない文章は無理
            try {
              INDArray vecingredientA_CommitMessage = vec.inferVector(ingredientA.commitMessage);
              INDArray vecingredientB_CommitMessage = vec.inferVector(ingredientB.commitMessage);
              INDArray vecModifCommitMessage = vec.inferVector(modificationPoint.commitMessage);
              double simA2modif = Transforms.cosineSim(vecingredientA_CommitMessage, vecModifCommitMessage);

              double simB2modif = Transforms.cosineSim(vecingredientB_CommitMessage, vecModifCommitMessage);

              return -1 * Double.compare(simA2modif, simB2modif);
            } catch (org.nd4j.linalg.exception.ND4JIllegalStateException e) {
              // モデル内にコミットメッセージが存在しなかったとき
              log.info(String.format("model does not have the commit message. IngredientA: %s, IngredientB: %s",
                  ingredientA.commitMessage, ingredientB.commitMessage));
            }
            return 0; // default
          }
        });
        // end sort
      } catch (IOException e) {
        e.printStackTrace();
      }

      // We reintroduce the sorted list ingredient into the space
      this.ingredientSpace.setIngredients(modificationPoint.getCodeElement(), baseElements);
    }

    int size = baseElements.size();
    if (size > 0) {
      // We get the smaller element
      Ingredient ingredient = baseElements.get(0);
      // we remove it from space
      baseElements.remove(0);
      return ingredient;
    } // any ingredient
    return null;
  }

  /**
   * ingredientのリストを探索範囲からとってくる
   */
  public List<Ingredient> getIngredientsFromSpace(ModificationPoint modificationPoint, AstorOperator operationType) {

    String type = null;
    if (operationType instanceof ReplaceOp) {
      type = modificationPoint.getCodeElement().getClass().getSimpleName();
    }

    List<Ingredient> elements = null;
    if (type == null) {
      elements = this.ingredientSpace.getIngredients(modificationPoint.getCodeElement());

    } else {
      elements = this.ingredientSpace.getIngredients(modificationPoint.getCodeElement(), type);
    }

    return elements;
  }
}