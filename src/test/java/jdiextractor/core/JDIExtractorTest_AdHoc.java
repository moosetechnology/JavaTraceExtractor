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
import jdiextractor.tracemodel.entities.TraceReceiver;
import jdiextractor.tracemodel.entities.javaType.TraceJavaClass;
import jdiextractor.tracemodel.entities.javaType.TraceJavaInterface;
import jdiextractor.tracemodel.entities.javaType.TraceJavaReferenceType;
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
	
	/**
	 * This test explicitly represent that enum and enum values are represented as any class would
	 * Meaning that JavaTraceExtractor does not understand what an enum is, but directly represent how an enum really work
	 * To understand what really is an enum you can read this https://stackoverflow.com/questions/29139633/java-class-equivalent-for-an-enum 
	 * or this https://dev.to/satyam_gupta_0d1ff2152dcc/java-enums-explained-beyond-basic-constants-a84
	 */
	@Test
	public void testEnumRepresentation() {
		CallStackSnapshotExtractorConfig config = CallStackSnapshotExtractorConfig.builder()
				.entrypoint(new BreakpointConfig("dummies.EnumValues", "main", List.of("java.lang.String[]"), 0))
				.endpoint(new BreakpointConfig("dummies.EnumValues", "endpoint", List.of("dummies.EnumValues$Animal"), 0))
				.build();
		this.startTargetJVM("dummies.EnumValues", config);

		CallStackSnapshotExtractor extractor = new CallStackSnapshotExtractor(false);
		extractor.launch(vm, config);

		Trace trace = extractor.getTrace();

		TraceMethod endpoint = (TraceMethod) trace.getElements().get(1);
		
		assertEquals(1,endpoint.getArguments().size());
		assertTrue(endpoint.getArguments().get(0).getValue() instanceof TraceClassReference);
	}
	
	@Test
	public void testAnonymousClassRepresentation() {
		CallStackSnapshotExtractorConfig config = CallStackSnapshotExtractorConfig.builder()
				.entrypoint(new BreakpointConfig("dummies.AnonymousClass", "main", List.of("java.lang.String[]"), 0))
				.endpoint(new BreakpointConfig("dummies.AnonymousClass", "endpoint", List.of("dummies.AnonymousClass$Dog"), 0))
				.build();
		this.startTargetJVM("dummies.AnonymousClass", config);

		CallStackSnapshotExtractor extractor = new CallStackSnapshotExtractor(false);
		extractor.launch(vm, config);

		Trace trace = extractor.getTrace();
		TraceMethod foo = (TraceMethod) trace.getElements().get(1);
		
		assertNotNull(foo);
		
		TraceReceiver anonymousReceiver = foo.getReceiver();
		
		TraceClassReference anonymousClassRef = (TraceClassReference) anonymousReceiver.getValue();
		// IsAnonymous
		assertTrue(((TraceJavaClass) anonymousClassRef.getType()).isAnonymous());
		// Anonymous name
		assertEquals("dummies.AnonymousClass$1",anonymousClassRef.getType().getName());
		// Parent name
		assertEquals("dummies.AnonymousClass$Dog", ((TraceJavaClass) anonymousClassRef.getType()).getAnonymousParent().getName());
	}
	
	@Test
	public void testAnonymousObjectClassRepresentation() {
		CallStackSnapshotExtractorConfig config = CallStackSnapshotExtractorConfig.builder()
				.entrypoint(new BreakpointConfig("dummies.AnonymousObjectClass", "main", List.of("java.lang.String[]"), 0))
				.endpoint(new BreakpointConfig("dummies.AnonymousObjectClass", "endpoint", List.of("java.lang.Object"), 0))
				.build();
		this.startTargetJVM("dummies.AnonymousObjectClass", config);

		CallStackSnapshotExtractor extractor = new CallStackSnapshotExtractor(false);
		extractor.launch(vm, config);

		Trace trace = extractor.getTrace();
		TraceMethod toString = (TraceMethod) trace.getElements().get(1);
		assertNotNull(toString);
		
		TraceReceiver anonymousReceiver = toString.getReceiver();
		
		TraceClassReference anonymousClassRef = (TraceClassReference) anonymousReceiver.getValue();
		
		// IsAnonymous
		assertTrue(((TraceJavaClass) anonymousClassRef.getType()).isAnonymous());
		// Anonymous name
		assertEquals("dummies.AnonymousObjectClass$1",anonymousClassRef.getType().getName());
		// Parent name
		assertEquals("java.lang.Object", ((TraceJavaClass) anonymousClassRef.getType()).getAnonymousParent().getName());
	}
	
	@Test
	public void testAnonymousInterfaceClassRepresentation() {
		CallStackSnapshotExtractorConfig config = CallStackSnapshotExtractorConfig.builder()
				.entrypoint(new BreakpointConfig("dummies.AnonymousInterfaceClass", "main", List.of("java.lang.String[]"), 0))
				.endpoint(new BreakpointConfig("dummies.AnonymousInterfaceClass", "endpoint", List.of("dummies.AnonymousInterfaceClass$Dog"), 0))
				.build();
		this.startTargetJVM("dummies.AnonymousInterfaceClass", config);

		CallStackSnapshotExtractor extractor = new CallStackSnapshotExtractor(false);
		extractor.launch(vm, config);

		Trace trace = extractor.getTrace();
		TraceMethod foo = (TraceMethod) trace.getElements().get(1);
		assertNotNull(foo);
		
		TraceReceiver anonymousReceiver = foo.getReceiver();
		TraceClassReference anonymousClassRef = (TraceClassReference) anonymousReceiver.getValue();
		
		// IsAnonymous
		assertTrue(((TraceJavaClass) anonymousClassRef.getType()).isAnonymous());
		// Anonymous name
		assertEquals("dummies.AnonymousInterfaceClass$1",anonymousClassRef.getType().getName());
		// Parent name
		assertEquals("dummies.AnonymousInterfaceClass$Dog", ((TraceJavaClass) anonymousClassRef.getType()).getAnonymousParent().getName());
		assertTrue(((TraceJavaClass) anonymousClassRef.getType()).getAnonymousParent() instanceof TraceJavaInterface);
	}
	
	@Test
	public void testNotReallyAnonymousClass() {
		CallStackSnapshotExtractorConfig config = CallStackSnapshotExtractorConfig.builder()
				.entrypoint(new BreakpointConfig("dummies.AnonymousClass$10", "main", List.of("java.lang.String[]"), 0))
				.endpoint(new BreakpointConfig("dummies.AnonymousClass$10", "endpoint", List.of(), 0))
				.build();
		this.startTargetJVM("dummies.AnonymousClass$10", config);

		CallStackSnapshotExtractor extractor = new CallStackSnapshotExtractor(false);
		extractor.launch(vm, config);

		Trace trace = extractor.getTrace();
		TraceMethod main = (TraceMethod) trace.getElements().get(0);
		
		assertFalse(((TraceJavaClass) main.getParentType()).isAnonymous());
	}
	
	@Test
	public void testParametricInterface() {
		CallStackSnapshotExtractorConfig config = CallStackSnapshotExtractorConfig.builder()
				.entrypoint(new BreakpointConfig("dummies.ParametricInterfaceContainer", "main", List.of("java.lang.String[]"), 0))
				.endpoint(new BreakpointConfig("dummies.ParametricInterfaceContainer", "endpoint", List.of("dummies.ParametricInterfaceContainer$ParametricInterface"), 0))
				.build();
		this.startTargetJVM("dummies.ParametricInterfaceContainer", config);

		CallStackSnapshotExtractor extractor = new CallStackSnapshotExtractor(false);
		extractor.launch(vm, config);
		
		Trace trace = extractor.getTrace();
		TraceMethod endpoint = (TraceMethod) trace.getElements().get(1);
		
		assertEquals("dummies.ParametricInterfaceContainer$ParametricInterface",endpoint.getParameters().get(0).getType().getName());
		assertTrue(((TraceJavaInterface) endpoint.getParameters().get(0).getType()).isParametric());
		
	}
	
	@Test
	public void testParametricClass() {
		CallStackSnapshotExtractorConfig config = CallStackSnapshotExtractorConfig.builder()
				.entrypoint(new BreakpointConfig("dummies.ParametricClass", "main", List.of("java.lang.String[]"), 0))
				.endpoint(new BreakpointConfig("dummies.ParametricClass", "endpoint", List.of(), 0))
				.build();
		this.startTargetJVM("dummies.ParametricClass", config);

		CallStackSnapshotExtractor extractor = new CallStackSnapshotExtractor(false);
		extractor.launch(vm, config);
		
		Trace trace = extractor.getTrace();
		TraceMethod main = (TraceMethod) trace.getElements().get(0);
		
		assertTrue(main.getParentType() instanceof TraceJavaClass);
		
		TraceJavaClass clazz = (TraceJavaClass) main.getParentType();
		assertTrue(clazz.isParametric());
	}
	
	@Test
	public void testNonParametricClassAreNotParametric() {
		CallStackSnapshotExtractorConfig config = CallStackSnapshotExtractorConfig.builder()
				.entrypoint(new BreakpointConfig("dummies.ObjectEvolution", "main", List.of("java.lang.String[]"), 0))
				.endpoint(new BreakpointConfig("dummies.ObjectEvolution", "endpoint", List.of("dummies.ObjectEvolution$Dog"), 0))
				.build();
		this.startTargetJVM("dummies.ObjectEvolution", config);

		CallStackSnapshotExtractor extractor = new CallStackSnapshotExtractor(false);
		extractor.launch(vm, config);
		
		Trace trace = extractor.getTrace();
		TraceMethod main = (TraceMethod) trace.getElements().get(0);
		
		TraceJavaClass clazz = (TraceJavaClass) main.getParentType();
		assertFalse(clazz.isParametric());
	}
	
	/**
	 * The argument of main(String[]) was previously resolved as a parametric class due to the use of only genericSignature on types
	 * But a genericSignature exist whenever it use a generic parameter OR it extends a class/interface that does
	 */
	@Test
	public void testArrayAreNotParametricClasses() {
		CallStackSnapshotExtractorConfig config = CallStackSnapshotExtractorConfig.builder()
				.entrypoint(new BreakpointConfig("dummies.ObjectEvolution", "main", List.of("java.lang.String[]"), 0))
				.endpoint(new BreakpointConfig("dummies.ObjectEvolution", "endpoint", List.of("dummies.ObjectEvolution$Dog"), 0))
				.build();
		this.startTargetJVM("dummies.ObjectEvolution", config);

		CallStackSnapshotExtractor extractor = new CallStackSnapshotExtractor(false);
		extractor.launch(vm, config);
		
		Trace trace = extractor.getTrace();
		TraceMethod main = (TraceMethod) trace.getElements().get(0);
		
		assertFalse(((TraceJavaReferenceType) main.getParameters().get(0).getType()).isParametric());
	}
	
	/**
	 * Check if ClassNotFoundException is handled as any other parameter would
	 */
	@Test
	public void testClassNotFoundExceptionTypeOnMethodParameter() {
		CallStackSnapshotExtractorConfig config = CallStackSnapshotExtractorConfig.builder()
				.entrypoint(new BreakpointConfig("dummies.ClassNotFoundParameter", "main", List.of("java.lang.String[]"), 0))
				.endpoint(new BreakpointConfig("dummies.ClassNotFoundParameter", "endpoint", List.of("java.lang.ClassNotFoundException"), 0))
				.build();
		this.startTargetJVM("dummies.ClassNotFoundParameter", config);

		CallStackSnapshotExtractor extractor = new CallStackSnapshotExtractor(false);
		extractor.launch(vm, config);
		
		Trace trace = extractor.getTrace();
		TraceMethod endpoint = (TraceMethod) trace.getElements().get(1);
		
		assertEquals("java.lang.ClassNotFoundException", endpoint.getParameters().get(0).getType().getName());
	}
	

}