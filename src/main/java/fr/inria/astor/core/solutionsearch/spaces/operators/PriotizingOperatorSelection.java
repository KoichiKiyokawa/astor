package fr.inria.astor.core.solutionsearch.spaces.operators;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.entities.WeightElement;

public class PriotizingOperatorSelection extends OperatorSelectionStrategy {

  protected static Logger log = Logger.getLogger(PriotizingOperatorSelection.class.getName());

  List<WeightElement<?>> weightedOperators = new ArrayList<>();

  public PriotizingOperatorSelection(OperatorSpace space) {
    super(space);
    // for
    for (AstorOperator op : this.operatorSpace.getOperators()) {
      // if (op instanceof )
      // weightedOperators.add(new WeightElement<>(op, uniformprobability));
    }
  }

  // 一旦コピペ
  @Override
  public AstorOperator getNextOperator() {

    WeightElement<?> selected = WeightElement.selectElementWeightBalanced(this.weightedOperators);
    AstorOperator selectedOp = (AstorOperator) selected.element;
    return selectedOp;
  }

  @Override
  public AstorOperator getNextOperator(SuspiciousModificationPoint modificationPoint) {
    return getNextOperator();
  }
  // 一旦コピペ ここまで

}