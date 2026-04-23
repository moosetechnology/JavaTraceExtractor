package dummies;

/**
 * This class offer a simple Parametric interface declaration to check how JavaTraceExtractor handle it
 */
public class ParametricInterfaceContainer {
	
	public static interface ParametricInterface<K extends Object>{}

	public static void main(String[] args) {
		endpoint(null);
	}

	public static void endpoint(ParametricInterface<Object> p ) {
	}

}
