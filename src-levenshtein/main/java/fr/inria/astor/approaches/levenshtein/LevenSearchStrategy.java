package fr.inria.astor.approaches.levenshtein;

import java.util.Collections;
import java.util.List;
import java.util.Comparator;
import org.apache.log4j.Logger;

import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientPool;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientSearchStrategy;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import fr.inria.astor.core.stats.Stats;
import fr.inria.astor.approaches.jgenprog.operators.ReplaceOp;
import fr.inria.astor.util.MapList;
import fr.inria.astor.util.StringUtil;

import org.apache.lucene.search.spell.LevensteinDistance;

public class LevenSearchStrategy extends IngredientSearchStrategy {
  private static final Boolean DESACTIVATE_CACHE = ConfigurationProperties
      .getPropertyBool("desactivateingredientcache");
  protected Logger log = Logger.getLogger(this.getClass().getName());

  public MapList<String, String> cache = new MapList<>();

  public LevenSearchStrategy(IngredientPool space) {
    super(space);
  }

  @Override
  public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {
    int attemptsBaseIngredients = 0;

    List<Ingredient> baseElements = getIngredientsFromSpace(modificationPoint, operationType);

    /**
     * baseElementsをレーベンシュタイン距離に基づいて並び替える
     */
    // start sort
    LevensteinDistance lDis = new LevensteinDistance();
    log.debug("modification point" + modificationPoint.getCodeElement());
    String modifCode = modificationPoint.getCodeElement().toString(); // 修正対象のソースコード
    Collections.sort(baseElements, new Comparator<Ingredient>() {
      @Override
      public int compare(Ingredient ingredientA, Ingredient ingredientB) {
        return Float.compare(lDis.getDistance(ingredientA.getCode().toString(), modifCode),
            lDis.getDistance(ingredientB.getCode().toString(), modifCode));
      }
    });
    // end sort

    if (baseElements == null || baseElements.isEmpty()) {
      log.debug("Any element available for mp " + modificationPoint);
      return null;
    }

    int elementsFromFixSpaceCount = baseElements.size();
    log.debug("Templates availables" + elementsFromFixSpaceCount);

    Stats.currentStat.getIngredientsStats().addSize(Stats.currentStat.getIngredientsStats().ingredientSpaceSize,
        baseElements.size());

    while (attemptsBaseIngredients < elementsFromFixSpaceCount) {

      log.debug(
          String.format("Attempts Base Ingredients  %d total %d", attemptsBaseIngredients, elementsFromFixSpaceCount));

      Ingredient baseIngredient = baseElements.get(attemptsBaseIngredients);
      attemptsBaseIngredients++;

      String newingredientkey = getKey(modificationPoint, operationType);

      if (baseIngredient != null && baseIngredient.getCode() != null) {

        // check if the element was already used
        if (DESACTIVATE_CACHE || !this.cache.containsKey(newingredientkey)
            || !this.cache.get(newingredientkey).contains(baseIngredient.getChacheCodeString())) {
          this.cache.add(newingredientkey, baseIngredient.getChacheCodeString());
          return baseIngredient;
        }

      }

    } // End while

    log.debug("--- no mutation left to apply in element "
        + StringUtil.trunc(modificationPoint.getCodeElement().getShortRepresentation()) + ", search space size: "
        + elementsFromFixSpaceCount);
    return null;
  }

  public String getKey(ModificationPoint modPoint, AstorOperator operator) {
    String lockey = modPoint.getCodeElement().getPosition().toString() + "-" + modPoint.getCodeElement() + "-"
        + operator.toString();
    return lockey;
  }

  /**
   * ランダムではなく、レーベンシュタイン距離に基づいて返す fixSpaceを並び替える
   *
   * @param fixSpace
   * @return
   */
  // protected Ingredient getStatementFromSpace(List<Ingredient> fixSpace,
  // ModificationPoint modificationPoint) {
  // LevensteinDistance lDis = new LevensteinDistance();
  // log.debug(modificationPoint.getCodeElement());
  // String modifCode = modificationPoint.getCodeElement().toString();
  // fixSpace.stream().sorted((ingredientA, ingredientB) ->
  // lDis.getDistance(ingredientA.getCode().toString, modifCode)
  // - lDis.getDistance(ingredientB.getCode().toString, modifCode));
  // return null;
  // }

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