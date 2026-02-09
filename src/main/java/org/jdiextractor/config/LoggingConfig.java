package org.jdiextractor.config;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Configuration for the output format.
 */
public class LoggingConfig {
	private final String outputName;
	private final String extension;

	public LoggingConfig(String outputName, String extension) {
		this.outputName = outputName;
		this.extension = extension;
	}

	public static LoggingConfig fromJson(JsonNode node) {
		if (node == null)
			throw new IllegalArgumentException("Missing 'logging' section in configuration.");

		return new LoggingConfig( node.path("outputName").textValue(),
				node.path("extension").textValue());
	}

	public String getOutputName() {
		return outputName;
	}

	public String getExtension() {
		return extension;
	}
}