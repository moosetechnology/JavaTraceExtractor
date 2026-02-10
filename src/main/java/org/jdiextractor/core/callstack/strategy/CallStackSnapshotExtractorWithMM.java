package org.jdiextractor.core.callstack.strategy;

import java.util.List;

import org.jdiextractor.config.JDIExtractorConfig;
import org.jdiextractor.core.callstack.AbstractCallStackExtractor;
import org.jdiextractor.service.serializer.TraceLogger;
import org.jdiextractor.service.serializer.TracePopulator;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InternalException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.StepEvent;

/**
 * Extracts the call stack only once, when the breakpoint is reached. *
 * <p>
 * <b>Note:</b> Fast, but object states are captured at the very end. If an
 * object was modified during execution, older frames will show the
 * <i>current</i> modified value, not the value at the time of the call.
 */
public class CallStackSnapshotExtractorWithMM extends AbstractCallStackExtractor {

	/**
	 * The trace model built during execution
	 */
	private TracePopulator tracePopulator;

	public CallStackSnapshotExtractorWithMM(VirtualMachine vm, JDIExtractorConfig config) {
		super(vm, config, false);
		this.tracePopulator = new TracePopulator(false,config.getMaxDepth());
	}

	@Override
	protected void executeExtraction() {
		try {
			// wait for the breakpoint
			this.waitForBreakpoint();

			// Extract all frames
			List<StackFrame> frames = this.getThread().frames();
			for (int i = frames.size() - 1; i >= 0; i--) {
			    StackFrame next = frames.get(i);
			    try {
			        tracePopulator.newMethodFrom(next.location().method(), next.getArgumentValues(), next.thisObject());
			    } catch (InternalException e) {
			    	// Happens for native calls, and can't be obtained
			        tracePopulator.newMethodFrom(next.location().method(), null, next.thisObject());
			    }
			}

			// Serialize the trace
			TraceLogger serializer = new TraceLogger(config.getLogging(), config.getMaxDepth());
			serializer.serialize(this.tracePopulator.getTrace());

		} catch (IncompatibleThreadStateException e) {
			// Should not happen because we are supposed to be at a breakpoint
			throw new IllegalStateException("Thread should be at a breakpoint but isn't");
		}
	}

	private void waitForBreakpoint() throws IncompatibleThreadStateException {
		this.processEventsUntil(config.getEndpoint());
	}

	@Override
	protected void reactToStepEvent(StepEvent event, ThreadReference targetThread) {
		// Nothing, should not happen in this scenario
	}

	@Override
	protected void reactToMethodEntryEvent(MethodEntryEvent event, ThreadReference targetThread) {
		// Nothing, should not happen in this scenario
	}

}
