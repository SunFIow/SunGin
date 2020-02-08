package com.sunflow.util;

public interface SimpleMapper extends Mapper {

	float func(float x);

	@Override
	default float func(float x, int i, int j) { return func(x); }

	interface Int extends Mapper.Int {
		int func(int x);

		@Override
		default int func(int x, int i, int j) { return func(x); }
	}

	interface Double extends Mapper.Double {
		double func(double x);

		@Override
		default double func(double x, int i, int j) { return func(x); }
	}

	interface Generic<T> extends Mapper.Generic<T> {
		T func(T x);

		@Override
		default T func(T x, int i, int j) { return func(x); }
	}

}