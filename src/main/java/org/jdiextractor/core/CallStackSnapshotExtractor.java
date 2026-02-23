package org.jdiextractor.core;

import java.util.List;

import org.jdiextractor.config.CallStackSnapshotExtractorConfig;
import org.jdiextractor.service.serializer.TraceLogger;
import org.jdiextractor.service.serializer.TracePopulator;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.StepEvent;

/**
 * Extracts the call stack only once, when the breakpoint is reached. *
 * <p>
 * <b>Note:</b> Fast, but object states are captured at the very end. If an
 * object was modified during execution, older frames will show the
 * <i>current</i> modified value, not the value at the time of the call.
 */
public class CallStackSnapshotExtractor extends AbstractExtractor<CallStackSnapshotExtractorConfig> {


	public CallStackSnapshotExtractor() {
		super(false);
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
				this.createMethodWith(next);
			}

			this.serializeTrace();

		} catch (IncompatibleThreadStateException e) {
			// Should not happen because we are supposed to be at a breakpoint
			throw new IllegalStateException("Thread should be at a breakpoint but isn't");
		}
	}



	private void waitForBreakpoint() throws IncompatibleThreadStateException {
		this.processEventsUntil(config.getEndpoint());
	}

	@Override
	protected void reactToStepEvent(StepEvent event) {
		// Nothing, should not happen in this scenario
	}

	@Override
	protected void reactToMethodEntryEvent(MethodEntryEvent event) {
		// Nothing, should not happen in this scenario
	}

	@Override
	protected void createTracePopulator() {
		TraceLogger logger = new TraceLogger(config.getLogging(), this.valuesIndependents);
		this.jdiToTraceConverter = new TracePopulator(valuesIndependents, config.getObjectMaxDepth(), logger);
	}


}
