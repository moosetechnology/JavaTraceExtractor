package jdiextractor.core;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.sun.jdi.VirtualMachine;

import jdiextractor.config.AbstractExtractorConfig;
import jdiextractor.config.CallStackSnapshotExtractorConfig;
import jdiextractor.config.components.BreakpointConfig;
import jdiextractor.service.connector.JDIAttach;
import jdiextractor.tracemodel.entities.Trace;
import jdiextractor.tracemodel.entities.TraceMethod;
import jdiextractor.tracemodel.entities.traceValues.TraceClassReference;
import jdiextractor.tracemodel.entities.traceValues.TracePrimitiveValue;

public class JDIExtractorTest_AdHoc {

	/** Target Virtual Machine connected with JDI */
	private VirtualMachine vm;

	/** Underlying OS process running the target JVM */
	private Process process;

	/**
	 * Launches the target class in a new JVM process and attaches the JDI.
	 */
	public void startTargetJVM(String className, AbstractExtractorConfig config) {
		String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		String currentClasspath = System.getProperty("java.class.path");

		// The hardcoded port 5006 must strictly match the configuration in
		// config.getVm()
		ProcessBuilder builder = new ProcessBuilder(javaBin,
				"-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5006", "-cp", currentClasspath,
				className);

		// Redirects child process I/O to the test console for easier debugging
		builder.inheritIO();

		try {
			process = builder.start();

			// Tactical delay to allow the OS to bind port 5006 before attaching.
			// Ideally, this should be replaced by a polling/retry loop inside JDIAttach.
			Thread.sleep(500);

			vm = (new JDIAttach()).attachToJDI(config.getVm());
		} catch (Exception e) {
			fail("Failed to start or attach to target JVM: " + e.getMessage());
		}
	}

	@After
	/**
	 * Ensures VM and process resources are strictly terminated after each test.
	 */
	public void after() {
		try {
			if (vm != null) {
				vm.dispose();
			}
		} finally {
			// A finally block guarantees the OS process is killed even if JDI fails to
			// disconnect.
			if (process != null) {
				process.destroyForcibly();
			}
		}
	}

	@Test
	public void testMethodArgumentCanReferencesAndPrimitiveTypes() {
		CallStackSnapshotExtractorConfig config = CallStackSnapshotExtractorConfig.builder()
				.entrypoint(
						new BreakpointConfig("dummies.ObjectArgsSimulation", "main", List.of("java.lang.String[]"), 0))
				.endpoint(new BreakpointConfig("dummies.ObjectArgsSimulation", "endpoint",
						List.of("java.util.List", "int"), 0))
				.build();
		this.startTargetJVM("dummies.ObjectArgsSimulation", config);
		
		CallStackSnapshotExtractor extractor = new CallStackSnapshotExtractor(false);
		extractor.launch(vm, config);
		
		Trace trace = extractor.getTrace();
		
		TraceMethod endpoint = (TraceMethod) trace.getElements().get(1);
		assertNotNull(endpoint);
		
		assertEquals(2,endpoint.getParameters().size());
		
		assertTrue(endpoint.getArguments().get(0).getValue() instanceof TraceClassReference);
		assertTrue(endpoint.getArguments().get(1).getValue() instanceof TracePrimitiveValue);
	}
	
}