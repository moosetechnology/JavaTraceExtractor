package app.config;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Configuration related to the Target Virtual Machine connection.
 */
public class VmConfig {
	private final String host;
	private final String port;

	public VmConfig(String host, String port) {
		this.host = host;
		this.port = port;
	}

	public static VmConfig fromJson(JsonNode node) {
		if (node == null)
			throw new IllegalArgumentException("Missing 'vm' section in configuration.");
		if (!node.has("host") || !node.has("port")) {
			throw new IllegalArgumentException("Missing 'host' or 'port' in 'vm' section.");
		}

		return new VmConfig(node.get("host").textValue(), node.get("port").textValue());
	}

	public String getHost() {
		return host;
	}

	public String getPort() {
		return port;
	}
}
