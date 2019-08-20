package fr.inria.astor.approaches.purpose_simularity;

import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientPool;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientSearchStrategy;

public class PurposeBasedSearchStrategy extends IngredientSearchStrategy {
	public String originalProjectRootDir = "./";

	public PurposeBasedSearchStrategy(IngredientPool space, String originalProjectRootDir) {
		super(space);
		this.originalProjectRootDir = originalProjectRootDir;
	}

	@Override
	public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {
		modificationPoint.setCommitMessage(this.originalProjectRootDir);

		List<Ingredient> baseElements = getIngredientsFromSpace(modificationPoint, operationType);
		for (Ingredient baseElem : baseElements) {
			baseElem.setCommitMessageByGitLogS(this.originalProjectRootDir);
		}
		// TODO: baseElementsをコミットメッセージに応じて並び替える

		return null;
	}
}