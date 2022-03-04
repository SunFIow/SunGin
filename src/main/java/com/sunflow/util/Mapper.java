package com.sunflow.util;

public interface Mapper {

	float func(float x, int i, int j);

	interface Int {
		int func(int x, int i, int j);
	}

	interface Double {
		double func(double x, int i, int j);
	}

	interface Generic<T> {
		T func(T x, int i, int j);
	}

}