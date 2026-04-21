package jdiextractor.config.builder;

import java.util.List;

import jdiextractor.config.AbstractExtractorConfig;
import jdiextractor.config.components.BreakpointConfig;
import jdiextractor.config.components.LoggingConfig;
import jdiextractor.config.components.VmConfig;

public abstract class AbstractExtractorConfigBuilder<T extends AbstractExtractorConfigBuilder<T, C>, C extends AbstractExtractorConfig> {

	protected BreakpointConfig entrypoint = null;
	protected BreakpointConfig endpoint = null;
	protected VmConfig vmConfig = null;
	protected LoggingConfig logging = null;
	protected int maxObjectDepth = AbstractExtractorConfig.DEFAULT_MAX_DEPTH;

	public abstract T self();

	public abstract C build();

	protected void ensureConfig() {
		if (entrypoint == null) {
			entrypoint = new BreakpointConfig("dummies.ObjectArgsSimulation", "main",
					List.of("java.lang.String[]", "int"), 0);
		}
		if (endpoint == null) {
			entrypoint = new BreakpointConfig("dummies.ObjectArgsSimulation", "endpoint", List.of("java.lang.String[]"),
					0);
		}
		if (vmConfig == null) {
			vmConfig = new VmConfig("localhost", "5006");
		}
		if (logging == null) {
			logging = new LoggingConfig("JDIOutput", "tr");
		}
	}

	public T entrypoint(BreakpointConfig entrypoint) {
		this.entrypoint = entrypoint;
		return self();
	}

	public T endpoint(BreakpointConfig endpoint) {
		this.endpoint = endpoint;
		return self();
	}

	public T vmConfig(VmConfig vmConfig) {
		this.vmConfig = vmConfig;
		return self();
	}

	public T logging(LoggingConfig logging) {
		this.logging = logging;
		return self();
	}

	public T maxObjectDepth(int maxObjectDepth) {
		this.maxObjectDepth = maxObjectDepth;
		return self();
	}

}
