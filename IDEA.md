# アイデア

- いろいろな類似度に基づいた実装

## 文字列の類似度

### コードの意図の類似度

- コミットメッセージが類似していれば、意図も近い → そこを挿入元にする
- 同一ソースコードの各行について、コミットメッセージの"近さ"で比較する(コードから追いかける)、もしくは、怪しい箇所が追加されたコミットのメッセージと類似した過去のコミットを探し、そこで追加された行を挿入元に選ぶ(コミットから追いかける)

## コマンドメモ

コミットハッシュから、コミットメッセージの表示(上述の意図の類似度を測定する際に使える)

```
git log --oneline -n 1 --format=%B eb5b11a25c
```

## コミットの分類

- 機能追加
  - Add, Enhance
- 削除
  - Delete, remove
- リファクタリング
  - Refactor…, rename…, replace, unused …,
- バグ修正
  - 軽微なもの
    - タイポ fix typo...
  - 環境依存
    - テストケースが通るようにする(prevent build failure)→pom.xml とかだから拡張子で弾ける
  - アルゴリズム ← ここをいかに細分化して、類似度を図れるようにするか
    - if 文の中身、ループの終了条件の修正 fix...

想定：事前にコミットメッセージのルールを決めておいて、それに基づいて

`src/main/java/fr/inria/astor/core/ingredientbased/IngredientBasedEvolutionaryRepairApproachImpl.java`の`getIngredientPool`を拡張すれば良い気がする

- ingredient を並び替える処理を拡張して、変数名の正規化&レーベンシュタイン距離の測定で並び替える

## ここのソースコード見ると参考になりそう

`src/main/java/fr/inria/astor/core/solutionsearch/spaces/ingredients/scopes/ctscopes`
`ProjectFacade` 修正対象のソースコードをコピーしてくる処理が書かれている。コピー後に変数名の正規化を行えばよい？

## 発見

ingredient を何も返さない(`return null;`)実装にしても、削除のみで自動修正が行えてしまうことがある。
