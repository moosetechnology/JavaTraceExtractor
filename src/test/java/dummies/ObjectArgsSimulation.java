package dummies;

import java.util.ArrayList;
import java.util.List;

/**
 * Create simple call stack for the JDIAttach when putting a breakpoint on the method dummies.ObjectArgsSimulation.endpoint(java.lang.String,int). 
 * This simulation demonstrate that not every arguments given to a method are references, in this case, the second parameter is an int
 */
public class ObjectArgsSimulation {

	public static void main(String[] args) {
		List<String> l = new ArrayList<>();
		endpoint(l, 7);
	}

	public static void endpoint(List<String> l, int i) {}
	

}
