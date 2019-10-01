package fr.inria.astor.approaches.purpose_simularity;

import com.martiansoftware.jsap.JSAPException;
import org.apache.log4j.Logger;

import fr.inria.astor.approaches.jgenprog.JGenProg;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientSearchStrategy;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientPool;

import fr.inria.astor.approaches.purpose_simularity.PurposeBasedSearchStrategy;

public class PurposeSimularityApproach extends JGenProg {
  private String javaFilePath;
  private String codeLocation;

  protected Logger log = Logger.getLogger(this.getClass().getName());
  // とりまコンストラクタはそのまま
  public PurposeSimularityApproach(MutationSupporter mutatorExecutor, ProjectRepairFacade projFacade)
      throws JSAPException {
    super(mutatorExecutor, projFacade);
    this.javaFilePath = projFacade.getProperties().getOriginalDirSrc().get(0);
    // ex) [/script/jGenProg_Defects4J_Chart_1/./source] -> /script/jGenProg_Defects4J_Chart_1/
    this.codeLocation = javaFilePath.split("\\.")[0];
  }

  /**
   * 無理やりPurposeBasedSearchStrategyをセット
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected void loadIngredientSearchStrategy() throws Exception {
    IngredientPool ingredientspace = this.getIngredientPool();
    // TODO: ここで変数の正規化しても良い？
    IngredientSearchStrategy ingStrategy = new PurposeBasedSearchStrategy(ingredientspace, this.codeLocation, this.javaFilePath);
    this.setIngredientSearchStrategy(ingStrategy);
  }
}