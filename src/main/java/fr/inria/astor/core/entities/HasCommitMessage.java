package fr.inria.astor.core.entities;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import spoon.reflect.declaration.CtElement;

import fr.inria.astor.approaches.purpose_simularity.entities.Commit;
import fr.inria.astor.util.CommandExecuter;

public abstract class HasCommitMessage {

	private String originalProjectRootDir;
	private String javaFilePath;

	public String commitMessage = "";
	protected Logger log = Logger.getLogger(this.getClass().getName());

	public abstract CtElement getCodeElement();

	// by `git log -L`
	public void setCommitMessage(String originalProjectRootDir, String javaFilePath) {
		this.originalProjectRootDir = originalProjectRootDir;
		this.javaFilePath = javaFilePath;

		int lineNumber = this.getCodeElement().getPosition().getLine();
		String[] args = { "git", "log", "-L",
				String.format("%d,%d:%s", lineNumber, lineNumber, javaFilePath + "/" + getRelativeFilePath()) };
		String res = CommandExecuter.run(args, originalProjectRootDir);
		log.info("line number: " + lineNumber);
		log.info("git result: " + res);
		parseGitLogLAndSetCommitMessage(res);
	}

	private String getRelativeFilePath() {
		String rawPath = this.getCodeElement().getPath().toString();

		List<String> paths = new ArrayList<>();
		String fileName = "";

		for (String str : rawPath.split("#")) {
			if (str.startsWith("subPackage[name=")) {
				paths.add(str.substring("subPackage[name=".length(), str.length() - 1));
			}

			if (str.startsWith("containedType[name=")) {
				fileName = str.substring("containedType[name=".length(), str.length() - 1);
			}
		}

		String res = String.format("%s/%s.java", String.join("/", paths), fileName);
		log.info("get relative file path: " + res);

		return res;
	}

	private void parseGitLogLAndSetCommitMessage(String gitLog) {
		// Defects4jが余計なコミットを3つ追加することがあるので、直近4つ分のコミットを記録
		List<Commit> commits = new ArrayList<>();
		for (String line : gitLog.split("\n")) {
			if (line.startsWith("commit ")) {
				String commitSHA = line.substring("commit ".length());
				commits.add(new Commit(commitSHA, this.originalProjectRootDir));
			}
		}

		if (commits.get(0).isCommitedByDefects4j()) {
			this.commitMessage = commits.get(3).getMessage();
		} else {
			this.commitMessage = commits.get(0).getMessage();
		}
		log.info("commitMessage: " + this.commitMessage);
	}
}