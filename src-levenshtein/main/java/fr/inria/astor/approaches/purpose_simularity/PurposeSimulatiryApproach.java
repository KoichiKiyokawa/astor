package fr.inria.astor.approaches.purpose_simularity;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.approaches.jgenprog.JGenProg;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientSearchStrategy;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientPool;

import fr.inria.astor.approaches.purpose_simularity.PurposeBasedSearchStrategy;

public class PurposeSimulatiryApproach extends JGenProg {
  // とりまコンストラクタはそのまま
  public PurposeSimulatiryApproach(MutationSupporter mutatorExecutor, ProjectRepairFacade projFacade)
      throws JSAPException {
    super(mutatorExecutor, projFacade);
  }

  /**
   * 無理やりLevenSearchStrategyをセット
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected void loadIngredientSearchStrategy() throws Exception {
    IngredientPool ingredientspace = this.getIngredientPool();
    // TODO: ここで変数の正規化しても良い？
    IngredientSearchStrategy ingStrategy = new PurposeBasedSearchStrategy(ingredientspace);
    this.setIngredientSearchStrategy(ingStrategy);
  }
}