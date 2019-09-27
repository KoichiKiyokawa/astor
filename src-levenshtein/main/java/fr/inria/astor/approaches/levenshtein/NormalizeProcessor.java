package fr.inria.astor.approaches.levenshtein;

import spoon.processing.AbstractProcessor;
import spoon.refactoring.Refactoring;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import fr.inria.astor.core.manipulation.filters.TargetElementProcessor;

public class NormalizeProcessor extends TargetElementProcessor {
  private int localVarIndex = 0;

  public void process(CtElement elem) {
    if (elem instanceof CtLocalVariable) {
      CtLocalVariable localVar = (CtLocalVariable) elem;
      Refactoring.changeLocalVariableName(localVar, "$" + this.localVarIndex++);
    }
  }
}
