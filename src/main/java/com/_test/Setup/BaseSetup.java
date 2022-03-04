package com._test.Setup;

public class BaseSetup {

	public BaseSetup() {}

	protected void setup() {}

	public static void main(Class<Setup> mainClass) {
		main(mainClass.getName());
	}

	public static void main(String mainClass) {
		System.out.println(mainClass);
		try {
			Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(mainClass);
			BaseSetup sketch = (BaseSetup) c.getDeclaredConstructor().newInstance();
			sketch.setup();
		} catch (RuntimeException re) {
			// Don't re-package runtime exceptions
			throw re;
		} catch (Exception e) {
			// Package non-runtime exceptions so we can throw them freely
			throw new RuntimeException(e);
		}
	}
}
