package jdiextractor.tracemodel.entities;

import jdiextractor.service.serializer.TraceSerializer;

public class TraceAssignation extends TraceElement {

	private TraceAssignationLeft left;

	private TraceAssignationRight right;

	public TraceAssignation() {

	}

	public TraceAssignationLeft getLeft() {
		return left;
	}

	public void setLeft(TraceAssignationLeft left) {
		this.left = left;
	}

	public TraceAssignationRight getRight() {
		return right;
	}

	public void setRight(TraceAssignationRight right) {
		this.right = right;
	}

	@Override
	public void acceptSerializer(TraceSerializer serializer) {
		serializer.serialize(this);
	}

}
