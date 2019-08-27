package fr.inria.astor.approaches.purpose_simularity;

import java.util.List;

import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientPool;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientSearchStrategy;
import fr.inria.astor.approaches.jgenprog.operators.ReplaceOp;

public class PurposeBasedSearchStrategy extends IngredientSearchStrategy {
	public String originalProjectRootDir;

	public PurposeBasedSearchStrategy(IngredientPool space, String originalProjectRootDir) {
		super(space);
		this.originalProjectRootDir = originalProjectRootDir;
	}

	@Override
	public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {
		modificationPoint.setCommitMessage(this.originalProjectRootDir);

		List<Ingredient> baseElements = getIngredientsFromSpace(modificationPoint, operationType);
		for (Ingredient baseElem : baseElements) {
			baseElem.setCommitMessage(this.originalProjectRootDir);
		}
		// TODO: baseElementsをコミットメッセージに応じて並び替える

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