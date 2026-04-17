package org.jdiextractor.core;

import java.util.List;

import org.jdiextractor.config.TraceExtractorStepConfig;
import org.jdiextractor.config.components.BreakpointConfig;
import org.jdiextractor.service.serializer.TraceLogger;
import org.jdiextractor.service.serializer.BufferedTraceConverter;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Method;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.StepRequest;

/**
 * Extracts the trace of an execution, meaning all method calls and their values
 * at each instant
 */
public class TraceExtractorStep extends AbstractExtractor<TraceExtractorStepConfig> {

	private int frameCountBefore;
	private int steps = 0;

	private StepRequest stepInto;
	private StepRequest stepOver;

	public TraceExtractorStep() {
		super(true);
	}

	@Override
	protected void executeExtraction() {

		try {
			// Wait until the vm is at the start of the main before reacting to each
			// MethodEntry
			this.processEventsUntil(config.getEntrypoint());

			// Fix for the first method being the main
			StackFrame initialFrame = this.getThread().frame(0);
			this.createMethodWith(initialFrame, config.collectValues());

			frameCountBefore = 1;

			// Step configuration
			stepOver = vm.eventRequestManager().createStepRequest(this.getThread(), StepRequest.STEP_MIN,
					StepRequest.STEP_OVER);
			stepInto = vm.eventRequestManager().createStepRequest(this.getThread(), StepRequest.STEP_MIN,
					StepRequest.STEP_INTO);
			this.ensureStepInto();

			// Different end point defined by the configuration
			if (!config.activateEndpoint()) {
				this.processEventsUntilEnd();
			} else {
				this.processEventsUntil(config.getEndpoint());
			}

			this.serializeTrace();

		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void reactToStepEvent(StepEvent event) {
		steps++;
		try {
			ThreadReference targetThread = event.thread();
			StackFrame frame = targetThread.frame(0);
			int frameCountNow = targetThread.frameCount();

			// PHASE 1 : Detections
			// 1. Detect invocations
			if (frameCountNow > frameCountBefore) {
				this.createMethodWith(frame, config.collectValues());
			} else if(frameCountNow < frameCountBefore) {
				this.popParentId();
			}

			// PHASE 2 : Update step policy
			if (frameCountNow >= config.getMaxMethodDepth()) {
				this.ensureStepOver();
			} else {
				this.ensureStepInto();
			}
			
			if (this.maxStepsAttained()) {
				this.desactivateSteps();
			}
			// Update frameCount
			frameCountBefore = frameCountNow;

		} catch (IncompatibleThreadStateException e) {
			throw new IllegalStateException("Exception occured during a step event : " + e);
		}
	}

	private boolean maxStepsAttained() {
		return steps < 0 | steps > config.getMaxSteps();
	}

	@Override
	protected void reactToMethodEntryEvent(MethodEntryEvent event) {
		// Nothing, should not happen in this scenario
	}

	@Override
	protected void reactToMethodExitEvent(MethodExitEvent event) {
		// The only thing we catch is the end of the entrypoint
		// We stop here, everything after is sound from the VM
		Method method = event.method();

		if (isExactMethodMatch(method, config.getEntrypoint())) {
			this.desactivateSteps();
		}
	}

	private boolean isExactMethodMatch(Method method, BreakpointConfig config) {
		if (!method.name().equals(config.getMethodName())) {
			return false;
		}

		List<String> expectedArgs = config.getMethodArguments();
		List<String> actualArgs = method.argumentTypeNames();

		if (expectedArgs == null)
			expectedArgs = java.util.Collections.emptyList();

		if (expectedArgs.size() != actualArgs.size()) {
			return false;
		}

		for (int i = 0; i < expectedArgs.size(); i++) {
			if (!expectedArgs.get(i).equals(actualArgs.get(i))) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected void createTracePopulator() {
		TraceLogger logger = new TraceLogger(config.getLogging(), this.valuesIndependents);
		this.jdiToTraceConverter = new BufferedTraceConverter(valuesIndependents, config.getObjectMaxDepth(), logger);
	}

	private void ensureStepOver() {
		if (!stepOver.isEnabled()) {
			stepInto.disable();
			stepOver.enable();
		}
	}

	private void ensureStepInto() {
		if (!stepInto.isEnabled()) {
			stepOver.disable();
			stepInto.enable();
		}
	}

	private void desactivateSteps() {
		stepInto.disable();
		stepOver.disable();
	}

}
