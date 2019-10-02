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
    String[] args = { "git", "log", SHA, "--oneline", "-1", "--pretty=format:%s" };
    String message = CommandExecuter.run(args, originalProjectRootDir);
    log.info("commit message: " + message);

    return message;
  }

  public String getAuthor() {
    String[] args = { "git", "log", SHA, "--oneline", "-1", "--pretty=format:%an" };
    String author = CommandExecuter.run(args, originalProjectRootDir);
    log.info("commit author: " + author);

    return author;
  }

  public boolean isCommitedByDefects4j() {
    return getAuthor().equals("defects4j <defects4j@localhost>");
  }
}