package fr.inria.astor.approaches.purpose_simularity.entities;

import fr.inria.astor.util.CommandExecuter;

import org.apache.log4j.Logger;

public class Commit {

  private String SHA;
  private String originalProjectRootDir;

  protected Logger log = Logger.getLogger(this.getClass().getName());

  public Commit(String SHA, String originalProjectRootDir) {
    this.SHA = SHA;
    this.originalProjectRootDir = originalProjectRootDir;
  }

  public String getMessage() {
    String message = CommandExecuter.run(String.format("git log %s --oneline -1 --pretty=format:'%s'", SHA).split(" "),
        originalProjectRootDir);
    log.info("commit message: " + message);

    return message;
  }

  public String getAuthor() {
    String author = CommandExecuter.run(String.format("git log %s --oneline -1 --pretty=format:'%an'", SHA).split(" "),
        originalProjectRootDir);
    log.info("commit author: " + author);

    return author;
  }

  public boolean isCommitedByDefects4j() {
    return getAuthor().equals("defects4j <defects4j@localhost>");
  }
}