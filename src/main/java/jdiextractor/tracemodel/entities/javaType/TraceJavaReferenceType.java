package jdiextractor.tracemodel.entities.javaType;

public abstract class TraceJavaReferenceType extends TraceJavaType {

	private boolean isParametric;

	public TraceJavaReferenceType(String name) {
		super(name);
	}

	public boolean isParametric() {
		return isParametric;
	}

	public void setIsParametric(boolean isParametric) {
		this.isParametric = isParametric;
	}

}
