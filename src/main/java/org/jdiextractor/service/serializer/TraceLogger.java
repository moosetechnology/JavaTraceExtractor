package org.jdiextractor.service.serializer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.jdiextractor.config.LoggingConfig;
import org.jdiextractor.tracemodel.entities.Trace;

public class TraceLogger {

	private LoggingConfig loggingConfig;

	private int maxDepth;

	/**
	 * Constructor of TraceLogger
	 * 
	 * @param loggingConfig information to instantiate the logger
	 * @param depth         the max depth of the object graphs
	 */
	public TraceLogger(LoggingConfig loggingConfig, int depth) {
		this.loggingConfig = loggingConfig;
		this.maxDepth = depth;
	}

	public void serialize(Trace trace) {
		BufferedWriter output;
		try {
			output = new BufferedWriter(
					new FileWriter(this.loggingConfig.getOutputName() + "." + this.loggingConfig.getExtension()));

			TraceSerializerJson serializer = new TraceSerializerJson(output);
			serializer.serialize(trace);

			output.flush();
			output.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
