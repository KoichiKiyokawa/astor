package fr.inria.astor.approaches.levenshtein;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.approaches.jgenprog.JGenProg;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientSearchStrategy;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientPool;

import fr.inria.astor.approaches.levenshtein.LevenSearchStrategy;

public class LevenshteinApproach extends JGenProg {
  // とりまコンストラクタはそのまま
  public LevenshteinApproach(MutationSupporter mutatorExecutor, ProjectRepairFacade projFacade) throws JSAPException {
    super(mutatorExecutor, projFacade);
  }

  /**
   * 無理やりLevenSearchStrategyをセット TODO: getIngredientPoolメソッドを改良して、正規化したい
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected void loadIngredientSearchStrategy() throws Exception {
    IngredientPool ingredientspace = this.getIngredientPool();
    // TODO: ここで変数の正規化しても良い？
    IngredientSearchStrategy ingStrategy = new LevenSearchStrategy(ingredientspace);
    this.setIngredientSearchStrategy(ingStrategy);
  }
}