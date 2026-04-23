package jdiextractor.service.serializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import helper.JsonValidator;
import jdiextractor.tracemodel.entities.javaType.TraceJavaClass;
import jdiextractor.tracemodel.entities.javaType.TraceJavaInterface;

public class TraceSerializerJsonTest {

	private TraceSerializerJson serializer;
	private StringWriter writer;

	@Before
	public void before() {
		writer = new StringWriter();
		serializer = new TraceSerializerJson(false, writer);
	}
	
	@After
	public void after() {
		// Automatically validate the obtained String
		assertTrue(JsonValidator.validate(writer.toString()));
	}

	@Test
	public void testInterfaceSerialization() {
		TraceJavaInterface inter = new TraceJavaInterface("my.Interface");
		serializer.serialize(inter);

		assertEquals("{\"name\":\"my.Interface\",\"isInterface\":true}", writer.toString());
	}

	@Test
	public void testClassSerialization() {
		TraceJavaClass clazz = new TraceJavaClass("my.Class");
		serializer.serialize(clazz);

		assertEquals("{\"name\":\"my.Class\"}", writer.toString());
		assertTrue(JsonValidator.validate(writer.toString()));
	}

	@Test
	public void testNoPackageClassSerialization() {
		TraceJavaClass clazz = new TraceJavaClass("Class");
		serializer.serialize(clazz);

		assertEquals("{\"name\":\"<Default Package>.Class\"}", writer.toString());
	}

	@Test
	public void testAnonymousClassSerialization() {
		TraceJavaClass clazz = new TraceJavaClass("Class");
		clazz.setAnonymousParent(new TraceJavaInterface("my.Interface"));
		serializer.serialize(clazz);

		assertEquals(
				"{\"name\":\"<Default Package>.Class\",\"anonymousParentType\":{\"name\":\"my.Interface\",\"isInterface\":true}}",
				writer.toString());
	}

	@Test
	public void testParametricClassSerialization() {
		TraceJavaClass clazz = new TraceJavaClass("my.Class<K>");
		clazz.setIsParametric(true);
		serializer.serialize(clazz);
		assertEquals("{\"name\":\"my.Class<K>\",\"isParametric\":true}", writer.toString());
	}
	
	@Test
	public void testParametricInterfaceSerialization() {
		TraceJavaInterface inter = new TraceJavaInterface("my.Interface<K>");
		inter.setIsParametric(true);
		serializer.serialize(inter);
		assertEquals("{\"name\":\"my.Interface<K>\",\"isParametric\":true,\"isInterface\":true}", writer.toString());
	}

}
