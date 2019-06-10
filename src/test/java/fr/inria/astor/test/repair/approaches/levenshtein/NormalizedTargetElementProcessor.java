package fr.inria.astor.approaches.levenshtein;

import fr.inria.astor.core.manipulation.filters.TargetElementProcessor;

import spoon.reflect.declaration.CtElement;

public class NormalizedTargetElementProcessor {
  // public class NormalizedTargetElementProcessor extends TargetElementProcessor<CtElement> {
  public NormalizedTargetElementProcessor() {
    super();
  }

  // @Override
  public void add(CtElement elem) {
    if (elem == null) {
      return;
    }

    // if (allowsDuplicateIngredients || !contains(elem)) {
    // CtElement code;

    // if (mustClone()) {
    // code = MutationSupporter.clone(elem);
    // }
    // spaceElements.add(code);
    // }
  }
}