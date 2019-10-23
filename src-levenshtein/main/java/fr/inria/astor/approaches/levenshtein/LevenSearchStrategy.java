package fr.inria.astor.approaches.levenshtein;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.search.spell.LevensteinDistance;

import spoon.reflect.visitor.filter.TypeFilter;
import spoon.reflect.code.CtLocalVariable;
import spoon.refactoring.Refactoring;
import spoon.reflect.declaration.CtElement;

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

public class LevenSearchStrategy extends IngredientSearchStrategy {

  private List<CtElement> locationsAnalyzed = new ArrayList<>();

  // <正規化前の要素, 正規化後の要素>
  // 同じ要素を複数回正規化しないように
  private Map<CtElement, CtElement> raw2normalized = new HashMap<>();

  protected Logger log = Logger.getLogger(this.getClass().getName());

  public LevenSearchStrategy(IngredientPool space) {
    super(space);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {

    List<Ingredient> baseElements = getIngredientsFromSpace(modificationPoint, operationType);
    CtElement normalizedModif = getNormalizedElement(modificationPoint.getCodeElement());
    baseElements.forEach(baseElem -> getNormalizedElement((CtElement) baseElem));

    if (baseElements == null || baseElements.isEmpty()) {
      return null;
    }

    // We store the location to avoid sorting the ingredient twice.
    if (!locationsAnalyzed.contains(modificationPoint.getCodeElement())) {
      locationsAnalyzed.add(modificationPoint.getCodeElement());

      // We have never analyze this location, let's sort the ingredients.
      LevensteinDistance lDis = new LevensteinDistance();
      Collections.sort(baseElements, new Comparator<Ingredient>() {
        @Override
        public int compare(Ingredient ingredientA, Ingredient ingredientB) {
          return -1 * Float.compare(
            lDis.getDistance(raw2normalized.get(ingredientA).toString(), normalizedModif.toString()),
            lDis.getDistance(raw2normalized.get(ingredientB).toString(), normalizedModif.toString())
          );
        }
      });
      // end sort

      // We reintroduce the sorted list ingredient into the space
      log.info("modif code: " + modificationPoint.getCodeElement().toString());
      baseElements.forEach(elem -> {
        log.info("baseElement: " + elem.getCodeElement().toString());
      });
      this.ingredientSpace.setIngredients(modificationPoint.getCodeElement(), baseElements);
    }

    int size = baseElements.size();
    if (size > 0) {
      // We get the smaller element
      Ingredient ingredient = baseElements.get(0);
      // we remove it from space
      baseElements.remove(0);
      return ingredient;
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

  private CtElement getNormalizedElement(CtElement rawElem) {
    List<CtElement> normalizedElements = new ArrayList<>(raw2normalized.keySet());
    if (normalizedElements.contains(rawElem)) {
      // 既に正規化済みであればその値を返す
      return raw2normalized.get(rawElem);
    } else {
      int localVarIndex = 0;
      CtElement normalized = rawElem.clone();
      for (CtLocalVariable localVar : normalized.getElements(new TypeFilter<CtLocalVariable>(CtLocalVariable.class))) {
        log.info("localVar: " + localVar);
        Refactoring.changeLocalVariableName(localVar, "$" + localVarIndex++);
      }

      log.info("normalized: " + normalized);
      raw2normalized.put(rawElem, normalized);
      return normalized;
    }
  }
}