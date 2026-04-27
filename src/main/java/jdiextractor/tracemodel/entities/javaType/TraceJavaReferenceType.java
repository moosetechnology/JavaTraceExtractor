package jdiextractor.tracemodel.entities.javaType;

public abstract class TraceJavaReferenceType extends TraceJavaType {

	private final String DEFAULT_PACKAGE = "<Default Package>";

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

	public String getFullyQualifiedName() {
		if (this.getName().contains(".")) {
			return this.getName();
		} else {
			return this.DEFAULT_PACKAGE + "." + this.getName();
		}
	}

}
