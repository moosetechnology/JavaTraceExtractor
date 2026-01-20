package app.experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Create simple call stack for the JDIAttach when putting a breakpoint on java.lang.Runtime.exec(java.lang.String). 
 * This simulation demonstrate the fact that if an object is modified in a later stack frame, previous frames will also have theses changes.
 */
public class ObjectRefSimulation {

	public static void main(String[] args) {
		List<String> l = new ArrayList<>();
		addAnElement(l);
	}

	public static void addAnElement(List<String> l) {
		l.add("AnElement");
		addAnotherElement(l);
	}

	public static void addAnotherElement(List<String> l) {
		l.add("AnotherElement");
		wowAnElementWasAdded(l);
	}

	public static void wowAnElementWasAdded(List<String> l) {
		try {
			Runtime.getRuntime().exec("open -a calculator");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
