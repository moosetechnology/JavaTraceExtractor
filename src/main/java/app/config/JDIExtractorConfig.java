package app.config;

import com.fasterxml.jackson.databind.JsonNode;

public class JDIExtractorConfig {

	private static final int DEFAULT_MAX_DEPTH = 20;

	private VmConfig vm;
	private BreakpointConfig breakpoint;
	private LoggingConfig logging;
	private String entryMethod;
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

		// Simple Field Extraction
		if (rootNode.has("entryMethod")) {
			config.entryMethod = rootNode.get("entryMethod").textValue();
		} else {
			throw new IllegalArgumentException("Missing required field: 'entryMethod'");
		}

		config.maxDepth = rootNode.has("maxDepth") ? rootNode.get("maxDepth").asInt() : DEFAULT_MAX_DEPTH;

		config.vm = VmConfig.fromJson(rootNode.get("vm"));
		config.breakpoint = BreakpointConfig.fromJson(rootNode.get("breakpoint"));
		config.logging = LoggingConfig.fromJson(rootNode.get("logging"));

		return config;
	}

	public VmConfig getVm() {
		return vm;
	}

	public BreakpointConfig getBreakpoint() {
		return breakpoint;
	}

	public LoggingConfig getLogging() {
		return logging;
	}

	public String getEntryMethod() {
		return entryMethod;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

}