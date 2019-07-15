package fr.inria.astor.approaches.levenshtein;

import spoon.processing.AbstractProcessor;
import spoon.refactoring.Refactoring;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

public class NormalizeProcessor extends AbstractProcessor<CtElement> {
  private int localVarIndex = 0;
  private int methodIndex = 0;

  public void process(CtElement elem) {
    if (elem instanceof CtLocalVariable) {
      CtLocalVariable localVar = (CtLocalVariable) elem;
      Refactoring.changeLocalVariableName(localVar, "$" + this.localVarIndex++);
    } else if (elem instanceof CtMethod) {
      CtMethod method = (CtMethod) elem;
      Refactoring.changeMethodName(method, "_" + this.methodIndex++);
    }
  }
}
