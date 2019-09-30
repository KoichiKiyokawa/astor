package fr.inria.astor.approaches.levenshtein;

import spoon.processing.AbstractProcessor;
import spoon.refactoring.Refactoring;
import spoon.refactoring.RefactoringException;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import fr.inria.astor.core.manipulation.filters.TargetElementProcessor;

public class NormalizeProcessor extends TargetElementProcessor {
  private static int localVarIndex = 0;

  public void process(CtElement elem) {
    if (elem instanceof CtLocalVariable) {
      CtLocalVariable localVar = (CtLocalVariable) elem;
      try {
        Refactoring.changeLocalVariableName(localVar, "$" + this.localVarIndex++);
      } catch (RefactoringException e) {
        e.printStackTrace();
      }
    }
  }
}
