package jdiextractor.core;

import jdiextractor.config.CallStackHistoryExtractorConfig;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.StepRequest;

/**
 * Extracts the call stack frame by frame during execution.
 * <p>
 * <b>Note:</b> Extremely slow, but historically accurate. Since data is
 * serialized at every step, it preserves the exact state of objects as they
 * were at the moment of execution.
 */
public class CallStackHistoryExtractor extends AbstractExtractor<CallStackHistoryExtractorConfig> {
	
	private int frameCountBefore = 0;

	public CallStackHistoryExtractor() {
		super(true);
	}

	public CallStackHistoryExtractor(boolean activateLogging) {
		super(true,activateLogging);
	}

	@Override
	protected void executeExtraction() {
		try {
			this.collectFrames();
			this.serializeTrace();
		} catch (IncompatibleThreadStateException e) {
			// Should not happen because we are supposed to be at a breakpoint
			throw new IllegalStateException("Thread should be at a breakpoint but isn't");
		}
	}

	/**
	 * Blocks execution until the configured breakpoint is hit. Handles
	 * ClassPrepareEvent if the class is not yet loaded.
	 * 
	 * @throws IncompatibleThreadStateException
	 */
	private void collectFrames() throws IncompatibleThreadStateException {
		// Wait until the vm is at the start of the main before reacting to every steps
		this.processEventsUntil(config.getEntrypoint());

		vm.eventRequestManager().createStepRequest(this.getThread(), StepRequest.STEP_MIN, StepRequest.STEP_INTO)
				.enable();

		this.processEventsUntil(config.getEndpoint());
	}

	@Override
	protected void reactToStepEvent(StepEvent event) {
		try {
			ThreadReference targetThread = event.thread();
			int frameCountNow = targetThread.frameCount();

			if (frameCountBefore + 1 == frameCountNow) {
				this.createMethodWith(targetThread.frame(0));
				frameCountBefore++;
			} else if (frameCountBefore - 1 == frameCountNow) {
				this.jdiToTraceConverter.removeLastElement();
				frameCountBefore--;
			}
		} catch (IncompatibleThreadStateException e) {
			throw new IllegalStateException("Exception occured during a step event : " + e);
		}
	}

	@Override
	protected void reactToMethodEntryEvent(MethodEntryEvent event) {
		// Nothing, should not happen in this scenario
	}

	@Override
	protected void reactToMethodExitEvent(MethodExitEvent event) {
		// Nothing, should not happen in this scenario
	}

}
