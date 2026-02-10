package org.jdiextractor.config.copy;

import com.fasterxml.jackson.databind.JsonNode;

public class JDIExtractorConfig {

	private static final int DEFAULT_MAX_DEPTH = 14;

	private VmConfig vm;
	private BreakpointConfig endpoint;
	private LoggingConfig logging;
	private BreakpointConfig entrypoint;
	private int maxDepth;

	// --- Private Constructor (use the static factory method) ---
	private JDIExtractorConfig() {
	}

	/**
	 * Factory method to create the configuration architecture from a JsonNode.
	 * * @param rootNode the root node of the JSON configuration
	 * 
	 * @return the configuration object
	 * @throws IllegalArgumentException if required fields are missing or invalid
	 */
	public static JDIExtractorConfig fromJson(JsonNode rootNode) {
		if (rootNode == null) {
			throw new IllegalArgumentException("Root configuration node is null.");
		}

		JDIExtractorConfig config = new JDIExtractorConfig();

		config.maxDepth = rootNode.has("maxDepth") ? rootNode.get("maxDepth").asInt() : DEFAULT_MAX_DEPTH;

		config.vm = VmConfig.fromJson(rootNode.get("vm"));
		config.endpoint = BreakpointConfig.fromJson(rootNode.get("endpoint"));
		config.entrypoint = BreakpointConfig.fromJson(rootNode.get("entrypoint"));
		config.logging = LoggingConfig.fromJson(rootNode.get("logging"));

		return config;
	}

	public VmConfig getVm() {
		return vm;
	}

	public BreakpointConfig getEndpoint() {
		return endpoint;
	}

	public LoggingConfig getLogging() {
		return logging;
	}

	public BreakpointConfig getEntrypoint() {
		return entrypoint;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

}