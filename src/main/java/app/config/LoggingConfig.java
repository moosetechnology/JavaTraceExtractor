package app.config;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Configuration for the output format.
 */
public class LoggingConfig {
	private final String format;
	private final String outputName;
	private final String extension;

	public LoggingConfig(String format, String outputName, String extension) {
		this.format = format;
		this.outputName = outputName;
		this.extension = extension;
	}

	public static LoggingConfig fromJson(JsonNode node) {
		if (node == null)
			throw new IllegalArgumentException("Missing 'logging' section in configuration.");

		return new LoggingConfig(node.path("format").textValue(), node.path("outputName").textValue(),
				node.path("extension").textValue());
	}

	public String getFormat() {
		return format;
	}

	public String getOutputName() {
		return outputName;
	}

	public String getExtension() {
		return extension;
	}
}