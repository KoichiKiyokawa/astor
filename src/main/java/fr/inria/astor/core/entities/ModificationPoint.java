package fr.inria.astor.core.entities;

import java.util.List;
import java.io.File;
import java.io.IOException;

import fr.inria.astor.util.CommandExecuter;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;

import org.apache.log4j.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * ModificationPoint of the program variant. It represents an element (i.e.
 * spoon element, CtElement) of the program under analysis.
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class ModificationPoint implements Comparable {

	protected ProgramVariant programVariant;

	protected CtElement codeElement;

	protected CtClass ctClass;

	List<CtVariable> contextOfModificationPoint;

	public int identified = 0;

	protected int generation = -1;

	/** Added */
	public String commitMessage = "";
	protected Logger log = Logger.getLogger(this.getClass().getName());

	public ModificationPoint() {
	}

	public ModificationPoint(int id, CtElement rootElement, CtClass ctClass, List<CtVariable> contextOfGen,
			int generation) {
		super();
		this.identified = id;
		this.codeElement = rootElement;
		this.ctClass = ctClass;
		this.contextOfModificationPoint = contextOfGen;
		this.generation = generation;
	}

	public ModificationPoint(CtElement rootElement, CtClass ctClass, List<CtVariable> contextOfGen) {
		super();
		this.codeElement = rootElement;
		this.ctClass = ctClass;
		this.contextOfModificationPoint = contextOfGen;
	}

	public CtElement getCodeElement() {
		return codeElement;
	}

	public void setCodeElement(CtElement rootElement) {
		this.codeElement = rootElement;
	}

	public CtClass getCtClass() {
		return ctClass;
	}

	public void setCtClass(CtClass clonedClass) {
		this.ctClass = clonedClass;
	}

	public String toString() {
		return "[" + codeElement.getClass().getSimpleName() + ", in " + ctClass.getQualifiedName() + "]";
	}

	public List<CtVariable> getContextOfModificationPoint() {
		return contextOfModificationPoint;
	}

	public void setContextOfModificationPoint(List<CtVariable> contextOfModificationPoint) {
		this.contextOfModificationPoint = contextOfModificationPoint;
	}

	public ProgramVariant getProgramVariant() {
		return programVariant;
	}

	public void setProgramVariant(ProgramVariant programVariant) {
		this.programVariant = programVariant;
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof ModificationPoint) {
			return Integer.compare(this.identified, ((ModificationPoint) o).identified);
		}

		return 0;

	}

	public ModificationPoint clone() {
		return new ModificationPoint(identified, codeElement, ctClass, contextOfModificationPoint, this.generation);
	}

	public void setCommitMessage(String originalProjectRootDir) {
		log.info("originalProjectRootDir: " + originalProjectRootDir);
		setCommitMessageByGitBlame(originalProjectRootDir);
	}

	private void setCommitMessageByGitBlame(String originalProjectRootDir) {
		int lineNumber = this.getCodeElement().getPosition().getLine();
		log.info("lineNumber: " + lineNumber);
		String[] args = String.format("git@blame@-L@%d,%d@%s", lineNumber, lineNumber, getFilePath(originalProjectRootDir)).split("@");
		String res = CommandExecuter.run(args, originalProjectRootDir);
		log.info("blame info made by cmd: " + res);

		// try {
		// 	Repository repo = new FileRepositoryBuilder().setGitDir(new File(originalProjectRootDir + ".git")).readEnvironment()
		// 			.findGitDir().build();
		// 	BlameResult blameResult = new Git(repo).blame().setFilePath(getFilePath(originalProjectRootDir)).call();
		// 	if (blameResult == null) {
		// 		log.info("blameResult is null");
		// 	}
		// 	RevCommit commit = blameResult.getSourceCommit(lineNumber - 1);
		// 	if (commit == null) {
		// 		log.info("commit is null");
		// 	}
		// 	log.info(commit.getFullMessage());
		// } catch (IOException | GitAPIException e) {
		// 	e.printStackTrace();
		// }
	}

	// by `git log -L`
	private void setCommitMessageByGitLogL(String originalProjectRootDir) {
		int lineNumber = this.getCodeElement().getPosition().getLine();
		String[] args = String.format("git@log@-L@%d,%d:%s", lineNumber, lineNumber, getFilePath(originalProjectRootDir)).split("@");
		String res = CommandExecuter.run(args, originalProjectRootDir);
		log.info("line number: " + lineNumber);
		log.info("git result: " + res);
	}

	private void setCommitMessageByGitLogS(String originalProjectRootDir) {
		String codeStr = this.getCodeElement().toString();
		String[] args = String.format("git@log@-S@'%s'", codeStr).split("@");
		String res = CommandExecuter.run(args, originalProjectRootDir);
		log.info("git result: " + res);
		// TODO: 結果をパースする
	}

	private String getFilePath(String originalProjectRootDir) {
		log.info("code element: " + this.getCodeElement().toString()); // 83行目
		log.info("Path: " + this.getCodeElement().getPath().toString()); // => File Path: #subPackage[name=org]#subPackage[name=apache]#subPackage[name=commons]#subPackage[name=math]#subPackage[name=distribution]#containedType[name=AbstractContinuousDistribution]#method[signature=inverseCumulativeProbability(double)]#body#statement[name=bracket]
		String filename = this.getCodeElement().getPath().toString().split("containedType\\[name=")[1].split("]")[0];
		log.info("filename: " + filename);
		String[] args = String.format("git ls-files ./**/%s.java", filename).split(" ");
		String res = CommandExecuter.run(args, originalProjectRootDir);
		log.info("file path: " + res);

		return res;
	}
}
