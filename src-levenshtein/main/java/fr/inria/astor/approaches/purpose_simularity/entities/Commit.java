package fr.inria.astor.approaches.purpose_simularity.entities;

public class Commit {

  private String SHA;
  private String originalProjectRootDir;

  public Commit(String SHA, String originalProjectRootDir) {
    this.SHA = SHA;
    this.originalProjectRootDir = originalProjectRootDir;
  }

  public String getMessage() {
    return CommandExecuter.run(String.format("git log %s --oneline -1 --pretty=format:'%s'", SHA),
        originalProjectRootDir);
  }

  public String getAuthor() {
    return CommandExecuter.run(String.format("git log %s --oneline -1 --pretty=format:'%an'", SHA),
        originalProjectRootDir);
  }

  public boolean isCommitedByDefects4j() {
    return getAuthor().equals("defects4j <defects4j@localhost>");
  }
}