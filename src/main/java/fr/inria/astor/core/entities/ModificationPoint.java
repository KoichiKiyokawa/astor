package fr.inria.astor.core.entities;

import java.util.List;

import fr.inria.astor.util.CommandExecuter;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;

import org.apache.log4j.Logger;

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
		setCommitMessageByGitLogL(originalProjectRootDir);
	}

	// by `git log -L`
	private void setCommitMessageByGitLogL(String originalProjectRootDir) {
		int lineNumber = this.getCodeElement().getPosition().getSourceStart();
		String[] args = String.format("git@log@-L@%d,%d:%s", lineNumber, lineNumber, getFilePath()).split("@");
		String res = CommandExecuter.run(args, originalProjectRootDir);
		log.info("git result: " + res);
	}

	private void setCommitMessageByGitLogS(String originalProjectRootDir){
		String codeStr = this.getCodeElement().toString();
		String[] args = String.format("git@log@-S@'%s'", codeStr).split("@");
		String res = CommandExecuter.run(args, originalProjectRootDir);
		log.info("git result: " + res);
		// TODO: 結果をパースする
	}

	private String getFilePath() {
		log.info("meta data: " + this.getCodeElement().getAllMetadata());
		log.info("File Path: " + this.getCodeElement().getPath().toString());
		return this.getCodeElement().getPath().toString();
	}
}
