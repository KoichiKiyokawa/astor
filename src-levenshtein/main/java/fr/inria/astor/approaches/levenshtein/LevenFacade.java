package fr.inria.astor.approaches.levenshtein;

import java.io.File;
import java.io.IOException;

import fr.inria.astor.core.setup.ProjectConfiguration;
import fr.inria.astor.core.setup.ProjectRepairFacade;

import org.apache.log4j.Logger;

import spoon.Launcher;
import fr.inria.astor.approaches.levenshtein.NormalizeProcessor;

public class LevenFacade extends ProjectRepairFacade {
  protected Logger log = Logger.getLogger(this.getClass().getName());

  public LevenFacade(ProjectConfiguration properties) throws Exception {
    super(properties);
  }


  // もとはソースファイル1つに付き1回copyOriginalSourceCodeが走るようになっているが、
  // 1つのプロジェクトにつき1回で十分なのでOverride
  @Override
  public void copyOriginalCodeToAstorWorkspace(String mutIdentifier) {
    // ex) [/script/jGenProg_Defects4J_Chart_1/./source] -> /script/jGenProg_Defects4J_Chart_1/
    String codeLocation = getProperties().getOriginalDirSrc().get(0).split("\\.")[0];
    log.info("codeLocation: " + codeLocation);
    try{
      copyOriginalSourceCode(codeLocation, mutIdentifier);
    } catch (IOException e) {
      e.printStackTrace();
    }
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
    launcher.getEnvironment().setAutoImports(true);
    long startTime = System.currentTimeMillis();
    log.info("--- Start Normalization ---");
    launcher.run();
    long endTime = System.currentTimeMillis();
    log.info("--- End Normalization. Time of normalize: " + (endTime - startTime) / 1000 + "(s) ---");
  }
}
