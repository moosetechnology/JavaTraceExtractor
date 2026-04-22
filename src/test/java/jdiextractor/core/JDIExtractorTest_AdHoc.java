package jdiextractor.core;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.jdi.VirtualMachine;

import jdiextractor.config.AbstractExtractorConfig;
import jdiextractor.config.CallStackHistoryExtractorConfig;
import jdiextractor.config.CallStackSnapshotExtractorConfig;
import jdiextractor.config.components.BreakpointConfig;
import jdiextractor.service.connector.JDIAttach;
import jdiextractor.tracemodel.entities.Trace;
import jdiextractor.tracemodel.entities.TraceMethod;
import jdiextractor.tracemodel.entities.traceValues.TraceClassReference;
import jdiextractor.tracemodel.entities.traceValues.TraceField;
import jdiextractor.tracemodel.entities.traceValues.TracePrimitiveValue;
import jdiextractor.tracemodel.entities.traceValues.TraceStringReference;
import jdiextractor.tracemodel.entities.traceValues.TraceValueAlreadyFound;

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

	/**
	 * Utility method that collect the field of a given name in a class reference
	 */
	public TraceField getFieldNamed(TraceClassReference classRef, String fieldName) {
		Optional<TraceField> optionalField = classRef.getFields().stream().filter((field) -> field.getName().equals(fieldName)).findAny();
		if(optionalField.isEmpty()) {
			throw new RuntimeException("Given field name does not exist in the given class");
		}
		return optionalField.get();
	}

	@Test
	public void testMethodArgumentCanReferencesAndPrimitiveTypes() {
		CallStackSnapshotExtractorConfig config = CallStackSnapshotExtractorConfig.builder()
				.entrypoint(new BreakpointConfig("dummies.ObjectArgsTypes", "main", List.of("java.lang.String[]"), 0))
				.endpoint(new BreakpointConfig("dummies.ObjectArgsTypes", "endpoint", List.of("java.util.List", "int"),
						0))
				.build();
		this.startTargetJVM("dummies.ObjectArgsTypes", config);

		CallStackSnapshotExtractor extractor = new CallStackSnapshotExtractor(false);
		extractor.launch(vm, config);

		Trace trace = extractor.getTrace();

		TraceMethod endpoint = (TraceMethod) trace.getElements().get(1);
		assertNotNull(endpoint);

		assertEquals(2, endpoint.getParameters().size());

		assertTrue(endpoint.getArguments().get(0).getValue() instanceof TraceClassReference);
		assertTrue(endpoint.getArguments().get(1).getValue() instanceof TracePrimitiveValue);
	}

	/**
	 * This test is ignored because we deactivated the extraction of String values.
	 * It causes a parsing error when handling specific attack payloads (e.g.,
	 * CommonsCollection1 from ysoserial). * @see
	 * jdiextractor.core.JDIToTraceConverter#newStringReferenceFrom(com.sun.jdi.StringReference)
	 */
	@Test
	@Ignore("Deactivated: String parsing error on ysoserial payloads. Check JDIToTraceConverter.")
	public void testStringValue() {
		CallStackSnapshotExtractorConfig config = CallStackSnapshotExtractorConfig.builder()
				.entrypoint(new BreakpointConfig("dummies.StringValues", "main", List.of("java.lang.String[]"), 0))
				.endpoint(new BreakpointConfig("dummies.StringValues", "endpoint", List.of("java.lang.String"), 0))
				.build();
		this.startTargetJVM("dummies.StringValues", config);

		CallStackSnapshotExtractor extractor = new CallStackSnapshotExtractor(false);
		extractor.launch(vm, config);

		Trace trace = extractor.getTrace();

		TraceMethod endpoint = (TraceMethod) trace.getElements().get(1);
		assertNotNull(endpoint);

		assertEquals("toto", ((TraceStringReference) endpoint.getArguments().get(0).getValue()).getValue());
	}

	@Test
	public void testCallStackSnapshotOnlyGetLastVersionOfObject() {
		CallStackSnapshotExtractorConfig config = CallStackSnapshotExtractorConfig.builder()
				.entrypoint(new BreakpointConfig("dummies.ObjectEvolution", "main", List.of("java.lang.String[]"), 0))
				.endpoint(new BreakpointConfig("dummies.ObjectEvolution", "endpoint", List.of("dummies.ObjectEvolution$Dog"), 0))
				.build();
		this.startTargetJVM("dummies.ObjectEvolution", config);

		CallStackSnapshotExtractor extractor = new CallStackSnapshotExtractor(false);
		extractor.launch(vm, config);

		Trace trace = extractor.getTrace();

		// At first, the dog has the attribute age set at 1, but the snapshot took the last version and see the age of 2
		TraceMethod changeAge = (TraceMethod) trace.getElements().get(1);
		assertNotNull(changeAge);
		assertEquals("changeAge", changeAge.getName());
		
		TraceClassReference dogReference = (TraceClassReference) changeAge.getArguments().get(0).getValue();
		Object rawAge1 = ((TracePrimitiveValue) getFieldNamed(dogReference, "age").getValue()).getValue();
		int actualAge1 = ((com.sun.jdi.IntegerValue) rawAge1).value();

		assertEquals(2, actualAge1);
		
		// At the last method, we have the dog again, but we got a TraceValueAlreadyFound
		TraceMethod endpoint = (TraceMethod) trace.getElements().get(2);
		assertNotNull(endpoint);
		assertEquals("endpoint", endpoint.getName());
		
		TraceValueAlreadyFound alreadyFoundDog = (TraceValueAlreadyFound) endpoint.getArguments().get(0).getValue();

		// Checking that the already found variable is really the dog
		assertEquals(dogReference.getUniqueID(),alreadyFoundDog.getUniqueID());
	}
	
	@Test
	public void testCallStackHistoryGetEvolutionOfObject() {
		CallStackHistoryExtractorConfig config = CallStackHistoryExtractorConfig.builder()
				.entrypoint(new BreakpointConfig("dummies.ObjectEvolution", "main", List.of("java.lang.String[]"), 0))
				.endpoint(new BreakpointConfig("dummies.ObjectEvolution", "endpoint", List.of("dummies.ObjectEvolution$Dog"), 0))
				.build();
		this.startTargetJVM("dummies.ObjectEvolution", config);

		CallStackHistoryExtractor extractor = new CallStackHistoryExtractor(false);
		extractor.launch(vm, config);

		Trace trace = extractor.getTrace();

		// At first, the dog has the attribute age set at 1
		TraceMethod changeAge = (TraceMethod) trace.getElements().get(1);
		assertNotNull(changeAge);
		assertEquals("changeAge", changeAge.getName());
		
		Object rawAge1 = ((TracePrimitiveValue) getFieldNamed((TraceClassReference) changeAge.getArguments().get(0).getValue(), "age").getValue()).getValue();
		int actualAge1 = ((com.sun.jdi.IntegerValue) rawAge1).value();

		assertEquals(1, actualAge1);
		
		// At the end, the dog has the attribute age set at 2  
		TraceMethod endpoint = (TraceMethod) trace.getElements().get(2);
		assertNotNull(endpoint);
		assertEquals("endpoint", endpoint.getName());
		
		Object rawAge2 = ((TracePrimitiveValue) getFieldNamed((TraceClassReference) endpoint.getArguments().get(0).getValue(), "age").getValue()).getValue();
		int actualAge2 = ((com.sun.jdi.IntegerValue) rawAge2).value();

		assertEquals(2, actualAge2);
	}

}