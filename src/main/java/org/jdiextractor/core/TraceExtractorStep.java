package org.jdiextractor.core;

import org.jdiextractor.config.TraceExtractorStepConfig;
import org.jdiextractor.service.serializer.TraceLogger;
import org.jdiextractor.service.serializer.BufferedTraceConverter;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.StepRequest;

/**
 * Extracts the trace of an execution, meaning all method calls and their values
 * at each instant
 */
public class TraceExtractorStep extends AbstractExtractor<TraceExtractorStepConfig> {

	private int frameCountBefore;

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
			this.createMethodWith(this.getThread().frame(0));
			frameCountBefore = 1;

			vm.eventRequestManager().createStepRequest(this.getThread(), StepRequest.STEP_MIN, StepRequest.STEP_INTO)
					.enable();
			if (config.activateEndpoint()) {
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
		try {
			ThreadReference targetThread = event.thread();
			int frameCountNow = targetThread.frameCount();

			if (frameCountNow < config.getMaxMethodDepth() & frameCountBefore != frameCountNow) {
				if (frameCountBefore + 1 == frameCountNow) {
					this.createMethodWith(targetThread.frame(0));
				}
				frameCountBefore = frameCountNow;
			}

		} catch (IncompatibleThreadStateException e) {
			throw new IllegalStateException("Exception occured during a step event : " + e);
		}
	}

	@Override
	protected void reactToMethodEntryEvent(MethodEntryEvent event) {
		throw new IllegalStateException("Exception occured during a step event : ");
	}

	@Override
	protected void createMethodWith(StackFrame frame) {
		if (config.collectValues()) {
			super.createMethodWith(frame);
		} else {
			this.jdiToTraceConverter.newMethodFrom(frame.location().method());
		}
	}
	
	@Override
	protected void createTracePopulator() {
		TraceLogger logger = new TraceLogger(config.getLogging(), this.valuesIndependents);
		this.jdiToTraceConverter = new BufferedTraceConverter(valuesIndependents, config.getObjectMaxDepth(), logger);
	}

}
