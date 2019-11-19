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
import spoon.reflect.declaration.CtVariable;
import spoon.refactoring.CtRenameGenericVariableRefactoring;
import spoon.refactoring.RefactoringException;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import fr.inria.astor.core.entities.HasCommitMessage;
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
  private Map<String, CtElement> raw2normalized = new HashMap<>();

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

    CtElement normalizedModif = getNormalizedElement(modificationPoint.getCodeElement());
    for (Ingredient baseElem : baseElements) {
      getNormalizedElement(baseElem.getCode());
    }

    // We store the location to avoid sorting the ingredient twice.
    if (!locationsAnalyzed.contains(modificationPoint.getCodeElement())) {
      locationsAnalyzed.add(modificationPoint.getCodeElement());

      // We have never analyze this location, let's sort the ingredients.
      LevensteinDistance lDis = new LevensteinDistance();
      Collections.sort(baseElements, new Comparator<Ingredient>() {
        @Override
        public int compare(Ingredient ingredientA, Ingredient ingredientB) {

          String normalizedIngredientAstr, normalizedIngredientBstr;
          try {
            normalizedIngredientAstr = raw2normalized.get(ingredientA.getCode().toString()).toString();
          } catch (NullPointerException e) {
            normalizedIngredientAstr = ingredientA.getCode().toString();
          }
          try {
            normalizedIngredientBstr = raw2normalized.get(ingredientB.getCode().toString()).toString();
          } catch (NullPointerException e) {
            normalizedIngredientBstr = ingredientB.getCode().toString();
          }

          return -1 * Float.compare(lDis.getDistance(normalizedIngredientAstr, normalizedModif.toString()),
              lDis.getDistance(normalizedIngredientBstr, normalizedModif.toString()));
        }
      });
      // end sort

      // We reintroduce the sorted list ingredient into the space
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

  private CtElement getNormalizedElement(CtElement elem) {
    String rawElem = elem.toString();
    // フィールド、ローカル変数を正規化。CtRenameGenericVariableRefactoringは重複チェックを行わないので注意
    for (CtVariable variable : elem.getElements(new TypeFilter<CtVariable>(CtVariable.class))) {
      try {
        new CtRenameGenericVariableRefactoring().setTarget(variable).setNewName("$").refactor();
      } catch (RefactoringException e) {
        e.printStackTrace();
      }
    }

    // 正規化済みのコードを更新
    raw2normalized.put(rawElem, elem);

    return elem;
  }
}
