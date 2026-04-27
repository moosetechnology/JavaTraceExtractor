package dummies;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Simple class to test interaction with a proxy
 */
public class ProxyClass {
	
	public static interface Dog {
		public void foo();
	}
	
	public static class DogHandler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			ProxyClass.endpoint((Dog) proxy);
			return null;
		}
		
	}
	
	public static void main(String[] args) {
		InvocationHandler handler =  new DogHandler();

		// Create a Dog Proxy
		Dog proxy = (Dog) Proxy.newProxyInstance(Dog.class.getClassLoader(), new Class<?>[]{Dog.class}, handler);

		proxy.foo();
	}

	public static void endpoint(Dog dog) {
	}

}
