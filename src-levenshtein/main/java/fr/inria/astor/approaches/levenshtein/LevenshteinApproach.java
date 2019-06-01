package fr.inria.astor.approaches.levenshtein;

import java.util.List;
import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.approaches.jgenprog.JGenProg;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.manipulation.filters.TargetElementProcessor;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientSearchStrategy;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientPool;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.scopes.GlobalBasicIngredientSpace;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.scopes.PackageBasicFixSpace;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.scopes.LocalIngredientSpace;
import fr.inria.main.evolution.ExtensionPoints;
import fr.inria.main.evolution.PlugInLoader;

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

  // TODO: ここいじっても、変数名の正規化できそう？
  public static IngredientPool getIngredientPool(List<TargetElementProcessor<?>> ingredientProcessors)
      throws JSAPException, Exception {
    String scope = ConfigurationProperties.properties.getProperty("scope");
    IngredientPool ingredientspace = null;
    // 探索するスコープの設定（グローバル | 同じパッケージ内 | 同じファイル内）に応じてingredintSpaceを返す
    if ("global".equals(scope)) {
      ingredientspace = (new GlobalBasicIngredientSpace(ingredientProcessors));
    } else if ("package".equals(scope)) {
      ingredientspace = (new PackageBasicFixSpace(ingredientProcessors));
    } else if ("local".equals(scope) || "file".equals(scope)) {
      ingredientspace = (new LocalIngredientSpace(ingredientProcessors));
    } else {
      ingredientspace = (IngredientPool) PlugInLoader.loadPlugin(ExtensionPoints.INGREDIENT_STRATEGY_SCOPE,
          new Class[] { List.class }, new Object[] { ingredientProcessors });
    }
    return ingredientspace;
  }

  @Override
  protected void loadIngredientPool() throws JSAPException, Exception {
    // TODO: ここを拡張して、変数名の正規化を実装
    List<TargetElementProcessor<?>> ingredientProcessors = this.getTargetElementProcessors();
    // 探索するスコープの設定（グローバル | 同じパッケージ内 | 同じファイル内）に応じてingredintSpaceを返す
    IngredientPool ingredientspace = LevenshteinApproach.getIngredientPool(ingredientProcessors);

    this.setIngredientPool(ingredientspace);
  }
}