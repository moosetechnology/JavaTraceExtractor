package dummies;

/**
 * This class offer a simple Anonymous class declaration to check how JavaTraceExtractor handle it
 */
public class AnonymousClass {

	private static class Dog {
		public void foo() {
		}
	}

	public static void main(String[] args) {
		Dog o = new Dog() {
			public void foo() {
				endpoint(this);
			}
		};

		o.foo();
	}

	public static void endpoint(Dog o) {
	}

}
