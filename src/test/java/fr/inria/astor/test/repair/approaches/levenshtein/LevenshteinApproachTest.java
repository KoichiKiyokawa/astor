package fr.inria.astor.test.repair.approaches.levenshtein;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;
import java.io.File;
import java.io.IOException;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.approaches.levenshtein.LevenshteinApproach;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.main.CommandSummary;
import fr.inria.main.evolution.AstorMain;
import fr.inria.astor.core.setup.ProjectConfiguration;

import fr.inria.astor.approaches.levenshtein.LevenFacade;

public class LevenshteinApproachTest {
  @Test
  public void testGetIngredientPool() throws Exception {
    try {
      MutationSupporter mutSupporter = new MutationSupporter();
      ProjectRepairFacade projFacade = new ProjectRepairFacade(new ProjectConfiguration());
      LevenshteinApproach leven = new LevenshteinApproach(mutSupporter, projFacade);
      assertNull(leven.getIngredientPool());
      assertEquals("fr.inria.astor.approaches.levenshtein.LevenshteinApproach",
          LevenshteinApproach.class.getCanonicalName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testEngine() throws Exception {
    AstorMain main = new AstorMain();

    CommandSummary cs = new CommandSummary();
    cs.command.put("-mode", "leven");
    cs.command.put("-srcjavafolder", "/src/java/");
    cs.command.put("-srctestfolder", "/src/test/");
    cs.command.put("-binjavafolder", "/target/classes");
    cs.command.put("-bintestfolder", "/target/test-classes/");
    cs.command.put("-loglevel", "INFO");
    cs.command.put("-location", new File("./examples/Math-issue-280/").getAbsolutePath());
    cs.command.put("-dependencies", new File("./examples/Math-issue-280/lib").getAbsolutePath());
    cs.command.put("-maxgen", "0");

    main.execute(cs.flat());
    assertTrue(main.getEngine() instanceof LevenshteinApproach);
  }

  @Test
  public void testNormalize() {
    ProjectConfiguration properties = new ProjectConfiguration();
    // TODO: 対象のディレクトリとかをセットしなきゃだめ

    try {
      LevenFacade lFacade = new LevenFacade(properties);
      String pathOriginalCode = "examples/math_70/src/main/java/org/apache/commons/math/DimensionMismatchException.java";
      String currentMutatorIdentifier = "default";
      lFacade.copyOriginalSourceCode(pathOriginalCode, currentMutatorIdentifier);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}