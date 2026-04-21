package jdiextractor.tracemodel;

import jdiextractor.service.serializer.TraceSerializer;

/**
 * I represent an trace entity 
 */
public abstract class TraceEntity {

	public abstract void acceptSerializer(TraceSerializer serializer);
	
}
