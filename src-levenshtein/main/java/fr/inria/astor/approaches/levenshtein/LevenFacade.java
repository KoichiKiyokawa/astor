package fr.inria.astor.approaches.levenshtein;

import java.io.File;
import java.io.IOException;

import fr.inria.astor.core.setup.ProjectConfiguration;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import spoon.Launcher;

import fr.inria.astor.approaches.levenshtein.NormalizeProcessor;

public class LevenFacade extends ProjectRepairFacade {
  public LevenFacade(ProjectConfiguration properties) throws Exception {
    super(properties);
  }

  @Override
  public void copyOriginalSourceCode(String pathOriginalCode, String currentMutatorIdentifier) throws IOException {
    File destination = new File(getProperties().getWorkingDirForSource() + File.separator + currentMutatorIdentifier);
    // File destination = new File("./spooned/");
    destination.mkdirs();
    // FileUtils.copyDirectory(new File(pathOriginalCode), destination);
    // コピーする代わりに、変数名を正規化してから出力
    Launcher launcher = new Launcher();
    launcher.addInputResource(pathOriginalCode);
    launcher.addProcessor(new NormalizeProcessor());
    launcher.setSourceOutputDirectory(destination);
    launcher.run();
  }
}
