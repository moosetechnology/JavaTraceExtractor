package app.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Configuration defining where to stop the execution to start analysis.
 */
public class BreakpointConfig {
	private final String className;
	private final String methodName;
	private final List<String> methodArguments;
	private final int repBefore;

	public BreakpointConfig(String className, String methodName, List<String> methodArguments, int repBefore) {
		this.className = className;
		this.methodName = methodName;
		this.methodArguments = methodArguments;
		this.repBefore = repBefore;
	}

	public static BreakpointConfig fromJson(JsonNode node) {
		if (node == null)
			throw new IllegalArgumentException("Missing 'breakpoint' section in configuration.");

		String clazz = node.path("className").textValue();
		String method = node.path("methodName").textValue();
		int rep = node.path("repBefore").intValue();

		List<String> args = new ArrayList<>();
		if (node.has("methodArguments") && node.get("methodArguments").isArray()) {
			Iterator<JsonNode> elements = node.get("methodArguments").elements();
			while (elements.hasNext()) {
				args.add(elements.next().textValue());
			}
		}

		return new BreakpointConfig(clazz, method, args, rep);
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public List<String> getMethodArguments() {
		return methodArguments;
	}

	public int getRepBefore() {
		return repBefore;
	}
}

