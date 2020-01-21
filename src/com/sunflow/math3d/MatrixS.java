package com.sunflow.math3d;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public final class MatrixS<T extends Number> implements Comparable<MatrixS<T>> {

	public static void main(String[] args) {
		MatrixS<Double> m = new MatrixS<>(Double.class, 3, 4);
		System.out.println(m.blub(4.3D));
	}

	private int rows, cols;
	private T[][] data;

	private Class<T> clazz;

	public MatrixS(Class<T> clazz, int rows, int cols) {
		this.clazz = clazz;
		this.rows = rows;
		this.cols = cols;
		this.data = (T[][]) new Number[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				data[i][j] = (T) Integer.valueOf(10);
			}
		}
	}

	public final T blub(T input) {
		try {
			System.out.println(clazz);
			Method method = clazz.getDeclaredMethod("valueOf", String.class);
			T val = (T) method.invoke(input, String.valueOf(input));
			return val;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("    ");
		for (int i = 0; i < cols; i++) {
			builder.append("[" + i + "]  ");
		}
		builder.append("\n");
		for (int i = 0; i < rows; i++) {
			builder.append("[" + i + "] ");
			for (int j = 0; j < cols; j++) {
//					builder.append("[" + i + "][" + j + "]" + data[i][j]);
				builder.append(data[i][j] + (j < cols - 1 ? " | " : "\n"));
			}
		}

		return builder.toString();
	}

	@Override
	public int compareTo(MatrixS<T> other) {
		return this.sum() < other.sum() ? -1 : ((this.sum() == other.sum()) ? 0 : 1);
	}

	private double sum() {
		double sum = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				sum += data[i][j].doubleValue();
			}
		}
		return sum;
	}

	public static final class Bla<T extends Number> {
		public final T blub(T input) {
			try {
				Class<? extends Number> clazz = input.getClass();
//				Method[] ms = clazz.getDeclaredMethods();
//				for (Method m : ms) {
//					System.out.println(m);
//				}
				Method method = clazz.getDeclaredMethod("valueOf", String.class);
				T val = (T) method.invoke(input, String.valueOf(input));
				return val;
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}