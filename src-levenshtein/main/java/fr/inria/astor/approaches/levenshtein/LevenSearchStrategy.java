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

  // 変数名を正規化する際に、同じスコープ内で同じ変数名にならないように
  // <親クラス名#親メソッド名, 最後に割り振ったindex>
  private Map<String, Integer> scope2lastIndex = new HashMap<>();

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

    // log TODO: delete
    for (Map.Entry<String, CtElement> entry : raw2normalized.entrySet()) {
      log.info(entry.getKey() + ":" + entry.getValue().toString());
      log.info("----------------------");
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
              lDis.getDistance(raw2normalized.get(ingredientA.getCode().toString()).toString(),
                  normalizedModif.toString()),
              lDis.getDistance(raw2normalized.get(ingredientB.getCode().toString()).toString(),
                  normalizedModif.toString()));
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

  private String getClassAndMethodName(CtElement elem) {
    String parentClassName = elem.getParent(CtClass.class).getSimpleName();
    String parentMethodName = elem.getParent(CtMethod.class).getSimpleName();
    String classAndMethodName = parentClassName + "#" + parentMethodName;
    log.info("classAndMethodName: " + classAndMethodName);

    return classAndMethodName;
  }

  private int getLastIndex(CtElement elem) {
    String classAndMethodName = getClassAndMethodName(elem);

    if (scope2lastIndex.containsKey(classAndMethodName)) {
      return scope2lastIndex.get(classAndMethodName);
    } else {
      scope2lastIndex.put(classAndMethodName, 0);
      return 0;
    }
  }

  private CtElement getNormalizedElement(CtElement rawElem) {
    List<String> normalizedElements = new ArrayList<>(raw2normalized.keySet());
    if (normalizedElements.contains(rawElem.toString())) {
      // 既に正規化済みであればその値を返す
      return raw2normalized.get(rawElem.toString());
    } else {
      int localVarIndex = getLastIndex(rawElem);
      String rawStr = rawElem.toString();
      for (CtLocalVariable localVar : rawElem.getElements(new TypeFilter<CtLocalVariable>(CtLocalVariable.class))) {
        try {
          Refactoring.changeLocalVariableName(localVar, "$" + localVarIndex++);
        } catch (RefactoringException e) {
          e.printStackTrace();
        }
      }

      // 最後に割り振ったindexを更新
      scope2lastIndex.put(getClassAndMethodName(rawElem), localVarIndex);

      // 正規化済みのコードを更新
      raw2normalized.put(rawStr, rawElem);

      return rawElem;
    }
  }
}