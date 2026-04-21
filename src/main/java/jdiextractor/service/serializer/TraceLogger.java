package jdiextractor.service.serializer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import jdiextractor.config.components.LoggingConfig;
import jdiextractor.tracemodel.entities.Trace;
import jdiextractor.tracemodel.entities.TraceElement;

public class TraceLogger {

	private LoggingConfig loggingConfig;

	private TraceSerializer serializer;

	/**
	 * Whether the values are independents between all element of the trace or not
	 */
	private boolean valueIdependents;

	/**
	 * Constructor of TraceLogger
	 * 
	 * @param loggingConfig     information to instantiate the logger
	 * @param valueIdependents, Whether the values are independents between all
	 *                          element of the trace or not
	 */
	public TraceLogger(LoggingConfig loggingConfig, boolean valueIdependents) {
		this.loggingConfig = loggingConfig;
		this.valueIdependents = valueIdependents;
	}

	public void serialize(Trace trace) {
		this.startSerialize();
		for (TraceElement elem : trace.getElements()) {
			this.serialize(elem);
		}
		this.endSerialize();
	}

	public void startSerialize() {
		BufferedWriter output;
		try {
			output = new BufferedWriter(
					new FileWriter(this.loggingConfig.getOutputName() + "." + this.loggingConfig.getExtension()));
			this.serializer = new TraceSerializerJson(output, valueIdependents);
			this.serializer.startSerialize();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void serialize(TraceElement element) {
		this.serializer.serialize(element);
	}

	public void endSerialize() {
		this.serializer.endSerialize();
	}

}
