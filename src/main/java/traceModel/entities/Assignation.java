package traceModel.entities;

public class Assignation extends TraceElement {

	private AssignationLeft left;

	private AssignationRight right;

	public Assignation() {

	}

	public AssignationLeft getLeft() {
		return left;
	}

	public void setLeft(AssignationLeft left) {
		this.left = left;
	}

	public AssignationRight getRight() {
		return right;
	}

	public void setRight(AssignationRight right) {
		this.right = right;
	}

}
