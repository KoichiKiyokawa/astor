package fr.inria.astor.approaches.levenshtein;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.approaches.jgenprog.JGenProg;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ProjectRepairFacade;

public class LevenshteinApproach extends JGenProg {

  // とりまコンストラクタはそのまま
  public LevenshteinApproach(MutationSupporter mutatorExecutor, ProjectRepairFacade projFacade) throws JSAPException {
    super(mutatorExecutor, projFacade);
  }
}