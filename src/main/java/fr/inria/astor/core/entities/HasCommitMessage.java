package fr.inria.astor.core.entities;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtPathException;

import fr.inria.astor.approaches.purpose_simularity.entities.Commit;
import fr.inria.astor.util.CommandExecuter;

public abstract class HasCommitMessage {

	private String originalProjectRootDir;

	public String commitMessage = "";

	public abstract CtElement getCodeElement();

	protected Logger log = Logger.getLogger(this.getClass().getName());

	// by `git log -L`
	public void setCommitMessage(String originalProjectRootDir, String javaFilePath) {
		this.originalProjectRootDir = originalProjectRootDir;

		log.info("code element: " + this.getCodeElement().toString());
		int lineNumber = this.getCodeElement().getPosition().getLine();
		String[] args = { "git", "log", "-L",
				String.format("%d,%d:%s", lineNumber, lineNumber, javaFilePath + "/" + getRelativeFilePath()) };
		String res = CommandExecuter.run(args, originalProjectRootDir);
		parseGitLogLAndSetCommitMessage(res);
	}

	private String getRelativeFilePath() {
		String res = "";
		try {
			String rawPath = this.getCodeElement().getPath().toString();
			log.info("rawPath: " + rawPath);

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

			res = String.format("%s/%s.java", String.join("/", paths), fileName);
			log.info("formated path: " + res);

		} catch (CtPathException e) {
			e.printStackTrace();
			log.error("cannot get path");
		}

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
			Commit developerCommit = pickFirstDeveloperCommit(commits);
			if (developerCommit == null) {
				log.error("No developer message");
				this.commitMessage = "";
			} else {
				this.commitMessage = developerCommit.getMessage();
			}
		} else {
			this.commitMessage = commits.get(0).getMessage();
		}
	}

	private Commit pickFirstDeveloperCommit(List<Commit> commits) {
		for (Commit commit : commits) {
			if (!commit.isCommitedByDefects4j()) {
				return commit;
			}
		}

		return null;
	}
}