package jdiextractor.tracemodel.entities.javaType;

public abstract class TraceJavaReferenceType extends TraceJavaType {

	private final String DEFAULT_PACKAGE = "<Default Package>";

	private boolean isParametric;
	
	/**
	 * The type container, in case the type is an inner type and need more informations
	 */
	private TraceJavaReferenceType typeContainer;

	public TraceJavaReferenceType(String name) {
		super(name);
	}

	public boolean isParametric() {
		return isParametric;
	}

	public void setIsParametric(boolean isParametric) {
		this.isParametric = isParametric;
	}
	
	public boolean isInnerClass() {
		return typeContainer != null;
	}
	
	public void setTypeContainer(TraceJavaReferenceType typeContainer) {
		this.typeContainer = typeContainer;
	}
	
	public TraceJavaReferenceType getTypeContainer() {
		return typeContainer;
	}

	public String getFullyQualifiedName() {
		if (this.getName().contains(".")) {
			return this.getName();
		} else {
			return this.DEFAULT_PACKAGE + "." + this.getName();
		}
	}

}
