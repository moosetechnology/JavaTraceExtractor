package jdiextractor.config;

import jdiextractor.config.components.BreakpointConfig;
import jdiextractor.config.components.LoggingConfig;
import jdiextractor.config.components.VmConfig;

import com.fasterxml.jackson.databind.JsonNode;

public class AbstractExtractorConfig {

	public final static int DEFAULT_MAX_DEPTH = 20;

	protected BreakpointConfig entrypoint;
	protected BreakpointConfig endpoint;
	protected VmConfig vm;
	protected LoggingConfig logging;
	protected int maxObjectDepth;

	protected AbstractExtractorConfig() {
	}

	protected AbstractExtractorConfig(BreakpointConfig entrypoint, BreakpointConfig endpoint, VmConfig vm,
			LoggingConfig logging, int maxObjectDepth) {
		this.entrypoint = entrypoint;
		this.endpoint = endpoint;
		this.vm = vm;
		this.logging = logging;
		this.maxObjectDepth = maxObjectDepth;
	}

	protected void fillFromJson(JsonNode rootNode) {
		this.vm = VmConfig.fromJson(rootNode.get("vm"));
		this.entrypoint = BreakpointConfig.fromJson(rootNode.get("entrypoint"));
		this.endpoint = BreakpointConfig.fromJson(rootNode.get("endpoint"));
		this.logging = LoggingConfig.fromJson(rootNode.get("logging"));
		this.maxObjectDepth = rootNode.has("maxObjectDepth") ? rootNode.get("maxObjectDepth").asInt()
				: DEFAULT_MAX_DEPTH;
	}

	public BreakpointConfig getEntrypoint() {
		return entrypoint;
	}

	public BreakpointConfig getEndpoint() {
		return endpoint;
	}

	public VmConfig getVm() {
		return vm;
	}

	public LoggingConfig getLogging() {
		return logging;
	}

	public int getObjectMaxDepth() {
		return maxObjectDepth;
	}

}