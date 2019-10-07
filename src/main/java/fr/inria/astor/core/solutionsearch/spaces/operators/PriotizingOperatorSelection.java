package fr.inria.astor.core.solutionsearch.spaces.operators;

import org.apache.log4j.Logger;

public class PriotizingOperatorSelection extends OperatorSelectionStrategy{

  protected static Logger log = Logger.getLogger(PriotizingOperatorSelection.class.getName());

  List<WeightElement<?>> weightedOperators = new ArrayList<>();

  public PriotizingOperatorSelection(OperatorSpace space) {
    super(space);
    // for
    for (AstorOperator op : this.operatorSpace.getOperators()){
    //  if (op instanceof )
    //   weightedOperators.add(new WeightElement<>(op, uniformprobability));
    }
   }

}