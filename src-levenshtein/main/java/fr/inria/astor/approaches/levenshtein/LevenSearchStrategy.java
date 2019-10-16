package fr.inria.astor.approaches.levenshtein;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

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

  private static int localVarIndex = 0;

  private List<CtElement> locationsAnalyzed = new ArrayList<>();

  protected Logger log = Logger.getLogger(this.getClass().getName());

  public LevenSearchStrategy(IngredientPool space) {
    super(space);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {

    List<Ingredient> baseElements = getIngredientsFromSpace(modificationPoint, operationType);

    if (baseElements == null || baseElements.isEmpty()) {
      return null;
    }

    // We store the location to avoid sorting the ingredient twice.
    if (!locationsAnalyzed.contains(modificationPoint.getCodeElement())) {
      locationsAnalyzed.add(modificationPoint.getCodeElement());

      // We have never analyze this location, let's sort the ingredients.
      LevensteinDistance lDis = new LevensteinDistance();
      String modifCode = modificationPoint.getCodeElement().toString();
      Collections.sort(baseElements, new Comparator<Ingredient>() {
        @Override
        public int compare(Ingredient ingredientA, Ingredient ingredientB) {
          return Float.compare(lDis.getDistance(ingredientA.getCode().toString(), modifCode),
              lDis.getDistance(ingredientB.getCode().toString(), modifCode));
        }
      });
      // end sort

      // We reintroduce the sorted list ingredient into the space
      this.ingredientSpace.setIngredients(modificationPoint.getCodeElement(), baseElements);
    }

    int size = baseElements.size();
    if (size > 0) {
      // We get the smaller element
      Ingredient ingredient = baseElements.get(0).getCode();
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
}