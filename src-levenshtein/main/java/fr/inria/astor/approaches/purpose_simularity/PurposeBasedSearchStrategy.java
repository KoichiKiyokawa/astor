package fr.inria.astor.approaches.purpose_simularity;

import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientPool;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientSearchStrategy;

public class PurposeBasedSearchStrategy extends IngredientSearchStrategy {
  public PurposeBasedSearchStrategy(IngredientPool space) {
    super(space);
  }

  @Override
  public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {
    // TODO: baseElementsをコミットメッセージに応じて並び替える
    return null;
  }
}