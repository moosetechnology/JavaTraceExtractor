package dummies;

/**
 * This class offer a simple Anonymous class of an Interface 
 * It is basically the same as AnonymousClass, but we use an Interface here to check how it is handled
 */
public class AnonymousInterfaceClass {

	private static interface Dog {
		public void foo();
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
