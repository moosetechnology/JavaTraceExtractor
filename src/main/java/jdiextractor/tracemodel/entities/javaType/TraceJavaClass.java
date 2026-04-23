package jdiextractor.tracemodel.entities.javaType;

import jdiextractor.service.serializer.TraceSerializer;

public class TraceJavaClass extends TraceJavaReferenceType {

	/**
	 * If the class is anonymous then we also explicit which type is its parent 
	 * It can be either a Interface or a class
	 */
	private TraceJavaType anonymousParent = null;

	public TraceJavaClass(String name) {
		super(name);
	}

	public TraceJavaType getAnonymousParent() {
		return anonymousParent;
	}

	public void setAnonymousParent(TraceJavaType anonymousParent) {
		this.anonymousParent = anonymousParent;
	}
	
	public boolean isAnonymous() {
		return anonymousParent != null;
	}


	@Override
	public void acceptSerializer(TraceSerializer serializer) {
		serializer.serialize(this);
	}

}
