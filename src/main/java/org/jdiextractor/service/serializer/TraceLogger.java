package org.jdiextractor.service.serializer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.jdiextractor.config.LoggingConfig;
import org.jdiextractor.tracemodel.entities.Trace;

public class TraceLogger {

	private LoggingConfig loggingConfig;

	/**
	 * Constructor of TraceLogger
	 * 
	 * @param loggingConfig information to instantiate the logger
	 */
	public TraceLogger(LoggingConfig loggingConfig) {
		this.loggingConfig = loggingConfig;
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
