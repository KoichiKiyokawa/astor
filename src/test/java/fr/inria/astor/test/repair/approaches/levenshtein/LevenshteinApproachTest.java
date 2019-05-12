package fr.inria.astor.test.repair.approaches.levenshtein;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.approaches.levenshtein.LevenshteinApproach;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.setup.ProjectConfiguration;

public class LevenshteinApproachTest {
  @Test
  public void testGetIngredientPool() {
    try {
      MutationSupporter mutSupporter = new MutationSupporter();
      ProjectRepairFacade projFacade = new ProjectRepairFacade(new ProjectConfiguration());
      LevenshteinApproach leven = new LevenshteinApproach(mutSupporter, projFacade);
      assertNull(leven.getIngredientPool());
      assertEquals("fr.inria.astor.approaches.levenshtein.LevenshteinApproach", LevenshteinApproach.class.getCanonicalName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}