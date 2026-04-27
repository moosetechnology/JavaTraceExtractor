package jdiextractor.service.serializer;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Type;
import com.sun.jdi.VirtualMachine;

import jdiextractor.tracemodel.entities.Trace;
import jdiextractor.tracemodel.entities.TraceElement;
import jdiextractor.tracemodel.entities.TraceParameter;
import jdiextractor.tracemodel.entities.javaType.TraceJavaClass;
import jdiextractor.tracemodel.entities.javaType.TraceJavaType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JDIToTraceConverterTest {
	
	
	@Test
    public void testCreateAClassFromStringWhenTypeIsNotLoaded() throws ClassNotLoadedException {
		MockJDIToTraceConverter converter = new MockJDIToTraceConverter(true,0,null);

        LocalVariable mockParam = new MockLocalVariable();
        
        TraceParameter result = converter.newParameterFrom(mockParam);
        
        assertEquals(1, converter.newJavaTypeFromStringCount);
        assertEquals("org.project.NotLoadedClass",result.getType().getName());
        // Always a class no matter what it really is
        assertTrue(result.getType() instanceof TraceJavaClass);
        
    }
	
	
	public static class MockJDIToTraceConverter extends JDIToTraceConverter {
		
		public int newJavaTypeFromStringCount = 0;

		public MockJDIToTraceConverter(boolean valuesIndependents, int maxObjectDepth, TraceSerializer serializer) {
			super(valuesIndependents, maxObjectDepth, serializer);
		}

		@Override
		protected void addElement(TraceElement element) {}

		@Override
		public void serialize() {}

		@Override
		public void removeLastElement() {}

		@Override
		public Trace getTrace() {
			return null;
		}
	
		@Override
		protected TraceJavaType newJavaTypeFrom(String typeName) {
			newJavaTypeFromStringCount++;
			return super.newJavaTypeFrom(typeName);
		}
		
	}
	public static class MockLocalVariable implements LocalVariable {

		@Override
		public VirtualMachine virtualMachine() {
			return null;
		}

		@Override
		public int compareTo(LocalVariable o) {
			return 0;
		}

		@Override
		public String name() {
			return "phantom";
		}

		@Override
		public String typeName() {
			return null;
		}

		@Override
		public Type type() throws ClassNotLoadedException {
			throw new ClassNotLoadedException("Not loaded");
		}

		@Override
		public String signature() {
			return "Lorg/project/NotLoadedClass;";
		}

		@Override
		public String genericSignature() {
			return null;
		}

		@Override
		public boolean isVisible(StackFrame frame) {
			return false;
		}

		@Override
		public boolean isArgument() {
			return false;
		}
		
	}

}
