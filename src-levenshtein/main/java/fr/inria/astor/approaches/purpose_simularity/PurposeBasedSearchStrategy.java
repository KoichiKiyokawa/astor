package fr.inria.astor.approaches.purpose_simularity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

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

	protected Logger log = Logger.getLogger(this.getClass().getName());

	final private String modelFileName = "commit_message_model.txt";

	public PurposeBasedSearchStrategy(IngredientPool space, String originalProjectRootDir, String javaFilePath) {
		super(space);
		this.javaFilePath = javaFilePath;
		this.originalProjectRootDir = originalProjectRootDir;
	}

	private File getSavedModelFile() {
		// ex) Math-2 -> math
		String id = ConfigurationProperties.getProperty("projectIdentifier").split("-")[0].toLowerCase();
		return new File(String.format("/astor/models/%s/%s", id, modelFileName));
	}

	@Override
	public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {
		modificationPoint.setCommitMessage(this.originalProjectRootDir, this.javaFilePath);

		List<Ingredient> baseElements = getIngredientsFromSpace(modificationPoint, operationType);
		for (Ingredient baseElem : baseElements) {
			baseElem.setCommitMessage(this.originalProjectRootDir, this.javaFilePath);
		}
		// baseElementsをコミットメッセージに応じて並び替える
		try {
      ParagraphVectors vec = WordVectorSerializer.readParagraphVectors(getSavedModelFile());
      TokenizerFactory t = new DefaultTokenizerFactory();
      t.setTokenPreProcessor(new CommonPreprocessor());
      vec.setTokenizerFactory(t);
      // コサイン類似度を測定
			// caution! モデルの中に存在していない文章は無理
			Collections.sort(baseElements, new Comparator<Ingredient>() {
				@Override
				public int compare(Ingredient ingredientA, Ingredient ingredientB) {
					INDArray vecIngredientA_CommitMessage = vec.inferVector(ingredientA.commitMessage);
					INDArray vecIngredientB_CommitMessage = vec.inferVector(ingredientB.commitMessage);
					INDArray vecModificationPointCommitMessage = vec.inferVector(modificationPoint.commitMessage);
					double simA2modif = Transforms.cosineSim(vecIngredientA_CommitMessage, vecModificationPointCommitMessage);
					log.info(String.format("modif: %s, ingA: %s, sim: %f", modificationPoint.commitMessage, ingredientA.commitMessage, simA2modif));

					double simB2modif = Transforms.cosineSim(vecIngredientB_CommitMessage, vecModificationPointCommitMessage);
					log.info(String.format("modif: %s, ingA: %s, sim: %f", modificationPoint.commitMessage, ingredientB.commitMessage, simB2modif));

					return Double.compare(simA2modif, simB2modif);
				}
			});
			// end sort

    } catch (IOException e) {
      e.printStackTrace();
    }


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