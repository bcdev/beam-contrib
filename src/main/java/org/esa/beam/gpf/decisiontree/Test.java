package org.esa.beam.gpf.decisiontree;

public class Test {
	static final int LOOPS = 100000000;

	public static void main(String[] args) {
		for (;;) {
			timeit1();
			timeit2();
			timeit3();
		}
	}

	static void timeit1() {
		long startTime;
		int test;
		IntTest intTest = new IntTest();

		startTime = System.currentTimeMillis();
		for (int i = 0; i < LOOPS; i++) {
			intTest.i = i;
			test = intTest.i;
		}
		println("intTest.i : " + (System.currentTimeMillis() - startTime)
				+ "ms.");
	}

	static void timeit2() {
		long startTime;
		int test;
		IntTest intTest = new IntTest();

		startTime = System.currentTimeMillis();
		for (int i = 0; i < LOOPS; i++) {
			IntTest.j = i;
			test = IntTest.j;
		}
		println("IntTest.j : " + (System.currentTimeMillis() - startTime)
				+ "ms.");
	}

	static void timeit3() {
		long startTime;
		int test;
		IntTest intTest = new IntTest();

		startTime = System.currentTimeMillis();
		for (int i = 0; i < LOOPS; i++) {
			intTest.set(i);
			test = intTest.get();
		}
		println("IntTest.set/get : " + (System.currentTimeMillis() - startTime)
				+ " ms.");
	}

	static void println(String s) {
		System.out.println(s);
	}
}