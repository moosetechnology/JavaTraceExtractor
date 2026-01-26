package app.extractor;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;

import app.config.JDIExtractorConfig;

public abstract class AbstractCallStackExtractor extends JDIExtractor {

	protected final StackFrameLogger stackFrameLogger;

	public AbstractCallStackExtractor(VirtualMachine vm, JDIExtractorConfig config, boolean frameIndependents) {
		super(vm, config);
		this.stackFrameLogger = new StackFrameLogger(config.getLogging(), config.getMaxDepth(),frameIndependents);
	}

	/**
	 * Iterates over the stack frames and delegates extraction to the
	 * StackExtractor.
	 * 
	 * @throws IOException
	 */
	protected void processFrames(ThreadReference thread) throws IncompatibleThreadStateException, IOException {
		processFrames(thread.frames());
	}

	/**
	 * Iterates over the stack frames and delegates extraction to the
	 * StackExtractor.
	 * 
	 * @throws IOException
	 */
	protected void processFrames(List<StackFrame> frames) throws IncompatibleThreadStateException, IOException {

		// iterating from the end of the list to start the logging from the first method
		// called
		ListIterator<StackFrame> it = frames.listIterator(frames.size());
		while (it.hasPrevious()) {
			stackFrameLogger.push(it.previous());
		}

		stackFrameLogger.writeAll();

	}

}
