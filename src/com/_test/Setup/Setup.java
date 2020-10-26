package com._test.Setup;

public class Setup extends BaseSetup {
	public static void main(String[] args) { BaseSetup.main(Setup.class); }
//	public static void main(String[] args) { new Setup().setup(); }

	private int a = 1;
	private int b = 2;

	@Override
	protected void setup() {
		super.setup();
		b = 3;
		System.out.println(a);
		System.out.println(b);
		System.out.println();
	}

}
