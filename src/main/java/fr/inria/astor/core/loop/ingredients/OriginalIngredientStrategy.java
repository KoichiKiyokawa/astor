package fr.inria.astor.core.loop.ingredients;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.inria.astor.approaches.jgenprog.operators.ReplaceOp;
import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientSearchStrategy;
import fr.inria.astor.core.solutionsearch.spaces.ingredients.IngredientPool;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import spoon.reflect.declaration.CtElement;

// @author Koichi

public class OriginalIngredientStrategy extends IngredientSearchStrategy {

    private List<CtElement> locationsAnalyzed = new ArrayList<>();

    public OriginalIngredientStrategy(IngredientPool space) {
        super(space);
    }

    @SuppressWarnings("unchecked") // ?
	@Override
    public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {
        // Ingredient: 原料 = 挿入元
        // protected CtElement ingredientCode;
    	// protected IngredientPoolScope scope;
    	// protected CtElement derivedFrom;
        // // Store the value of code var
    	// protected String cacheString = null;
        List<Ingredient> ingredientsLocation = null; // 修正に使えそうな原料のリスト
		if (operationType instanceof ReplaceOp) // operationTypeとReplaceOpが同一のオブジェクトであったら
            ingredientsLocation = this.ingredientSpace.getIngredients(modificationPoint.getCodeElement(),
                modificationPoint.getCodeElement().getClass().getSimpleName());
                // getIngredients:原料(成分)の場所を返す
                // modificationPoint.getCodeElement()の中にあるmodificationPoint.getCodeElement().getClass().getSimpleName()と同じ型を全て返す
        else // operationTypeの型が特定できなかったら
            ingredientsLocation = this.ingredientSpace.getIngredients(modificationPoint.getCodeElement());
            // modificationPoint.getCodeElement() の中にある全ての成分を返す

        // 念の為
        if ( ingredientsLocation == null || ingredientsLocation.isEmpty() )
            return null;

        // 修正ポイントの要素(modificationPoint.getCodeElement)が探索済み(locationsAnalyzed)に含まれていたら
        if (!locationsAnalyzed.contains(modificationPoint.getCodeElement())) {
            // 探索済みに追加する
            locationsAnalyzed.add(modificationPoint.getCodeElement());

            // ソート前に一時的に変数に保存しておく TODO: ingredientsLocationをソートしても大丈夫？
            // ingredientsLocationをまずはコピー
            List<Ingredient> ingredientsLocationSort = new ArrayList<>(ingredientsLocation);

            // ソート TODO:ここの並び替えで優先順位を決めることができる
            Collections.sort(ingredientsLocationSort, new Comparator<Ingredient>() {
                public int compare(Ingredient i1, Ingredient i2) {
                    return i1.toString().length() - i2.toString().length();
                }
                // memo: IngredientクラスのtoSting()はオーバーライドされていて、コードを文字列として返すようになっている。
                // 参照受け渡しなのでingredientsLocationSortに変更を加えると、ingredientsLocationにも変更が反映される
            });
        }

        if (ingredientsLocation.size() > 0) {
            CtElement element = ingredientsLocation.get(0).getCode(); // ０番目の原料のコード(ingredientCode)を代入
            ingredientsLocation.remove(0);

            return new Ingredient(element, this.ingredientSpace.spaceScope());
        }

        return null;
    }
}
