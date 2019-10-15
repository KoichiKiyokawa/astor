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
	
	private List<CtElement> locationsAnalyzed = new ArrayList<>();

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

	@SuppressWarnings("unchecked")
	@Override
	public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {

		modificationPoint.setCommitMessage(this.originalProjectRootDir, this.javaFilePath);

		List<Ingredient> baseElements = getIngredientsFromSpace(modificationPoint, operationType);

		if (baseElements == null || baseElements.isEmpty()) {
			return null;
		}

		// We store the location to avoid sorting the ingredient twice.
		if (!locationsAnalyzed.contains(modificationPoint.getCodeElement())) {
			locationsAnalyzed.add(modificationPoint.getCodeElement());

			// We have never analyze this location, let's sort the ingredients.
			Collections.sort(baseElements, new Comparator<Ingredient>() {
			@Override
			public int compare(Ingredient ingredientA, Ingredient ingredientB) {

				// 同じコミットメッセージのものは優先して使わない
				if (modificationPoint.commitMessage.equals(ingredientA.commitMessage)) {
					if (modificationPoint.commitMessage.equals(ingredientB.commitMessage)) {
						// ingredientAもingredientBもmodificationPointと同じコミットメッセージだったら、
						// とりあえず並び替えずにそのまま
						return 1;
					} else {
						// modificationPointとingredientAのコミットメッセージが同じ かつ
						// modificationPointとingredientBのコミットメッセージが違う とき、
						// ingredientAをうしろに
						return -1;
					}
				} else {
					if (modificationPoint.commitMessage.equals(ingredientB.commitMessage)) {
						// modificationPointとingredientAのコミットメッセージが違う かつ
						// modificationPointとingredientBのコミットメッセージが同じ とき、
						// そのまま
						return 1;
					}
				}

				// modificationPointとingredientAのコミットメッセージが違う かつ
				// modificationPointとingredientBのコミットメッセージが違う とき、
				// コサイン類似度で並び替える
				try {
					ParagraphVectors vec = WordVectorSerializer.readParagraphVectors(getSavedModelFile());
					TokenizerFactory t = new DefaultTokenizerFactory();
					t.setTokenPreProcessor(new CommonPreprocessor());
					vec.setTokenizerFactory(t);
					// コサイン類似度を測定
					// caution! モデルの中に存在していない文章は無理
					INDArray vecingredientA_CommitMessage = vec.inferVector(ingredientA.commitMessage);
					INDArray vecingredientB_CommitMessage = vec.inferVector(ingredientB.commitMessage);
					INDArray vecModificationPointCommitMessage = vec.inferVector(modificationPoint.commitMessage);
					double simA2modif = Transforms.cosineSim(vecingredientA_CommitMessage, vecModificationPointCommitMessage);
					log.info(String.format("modif: {code: %s, commit: %s}, ingA: {code: %s,commit: %s}, sim: %f",
							modificationPoint.getCodeElement(), modificationPoint.commitMessage, ingredientA.getCodeElement(),
							ingredientA.commitMessage, simA2modif));

					double simB2modif = Transforms.cosineSim(vecingredientB_CommitMessage, vecModificationPointCommitMessage);
					log.info(String.format("modif: {code: %s, commit: %s}, ingA: {code: %s,commit: %s}, sim: %f",
							modificationPoint.getCodeElement(), modificationPoint.commitMessage, ingredientB.getCodeElement(),
							ingredientB.commitMessage, simB2modif));

					return Double.compare(simA2modif, simB2modif);
				} catch (IOException e) {
					e.printStackTrace();
				}

				return 1; // default: no sort
			}
		});
			// end sort

			// We reintroduce the sorted list ingredient into the space
			this.ingredientSpace.setIngredients(modificationPoint.getCodeElement(), baseElements);
		}
		int size = baseElements.size();
		if (size > 0) {
			// We get the smaller element
			CtElement element = baseElements.get(0).getCode();
			// we remove it from space
			baseElements.remove(0);
			return new Ingredient(element, this.ingredientSpace.spaceScope());
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