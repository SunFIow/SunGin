package com.sunflow.math3d;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sunflow.util.LogUtils;
import com.sunflow.util.Mapper;
import com.sunflow.util.MathUtils;
import com.sunflow.util.SimpleMapper;

public class Matrix<T extends Number> implements MathUtils, LogUtils, Cloneable, Serializable {

	public int rows, cols;

//	private float[][] data;
	public T[][] data;

	private Class<T> type;
	private Method valueOf;
//	private T typeVal;

	@SuppressWarnings("unchecked")
	public Matrix(Class<? extends Number> clazz, int r, int c) {
		rows = r;
		cols = c;
//		data = new float[r][c];
		data = (T[][]) new Number[r][c];

		type = (Class<T>) clazz;
		try {
			valueOf = type.getDeclaredMethod("valueOf", String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

	public Matrix<T> transpose() {
		set(new Matrix<T>(type, cols, rows).map((x, i, j) -> data[j][i]));
		return this;
	}

	@SuppressWarnings("unchecked")
	public Matrix<T> set(Matrix<T> m) {
		rows = m.rows;
		cols = m.cols;
		data = (T[][]) new Number[rows][cols];
		map((x, i, j) -> m.data[i][j]);

		type = m.type;
		valueOf = m.valueOf;

		return this;
	}

	public Matrix<T> set(T[][] data) {
		if (this.rows != data.length || this.cols != data[0].length) {
			error("MatrixF#add: rows and cols didnt match");
			error("MatrixF#add this: \n" + this);
			error("matrixF#add data: \n" + data);
		}

		this.data = data;
		return this;
	}

	@Override
	public Matrix<T> clone() {
		return new Matrix<T>(type, rows, cols).set(this);
	}

	public static <T extends Number> Matrix<T> transpose(Matrix<T> m) {
		return m.clone().transpose();
	}

	public Matrix<T> add(T n) {
//		map((x, i, j) -> x + n);
		map((x, i, j) -> add(x, n));
		return this;
	}

	public Matrix<T> add(Matrix<T> b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			error("MatrixF#add: rows and cols didnt match");
			error("MatrixF#add this: \n" + this);
			error("matrixF#add b: \n" + b);
		}
//		map((x, i, j) -> x + b.data[i][j]);
		map((x, i, j) -> add(x, b.data[i][j]));
		return this;
	}

	public static <T extends Number> Matrix<T> add(Matrix<T> a, Matrix<T> b) {
		return a.clone().add(b);
	}

	public Matrix<T> substract(T n) {
//		map((x, i, j) -> x - n);
		map((x, i, j) -> sub(x, n));
		return this;
	}

	public Matrix<T> substract(Matrix<T> b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			error("MatrixF#substract: rows and cols didnt match");
			error("MatrixF#substract this: \n" + this);
			error("matrixF#substract b: \n" + b);
		}
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
//				this.data[i][j] -= b.data[i][j];
				sub(this.data[i][j], b.data[i][j]);
			}
		}
		return this;
	}

	public static <T extends Number> Matrix<T> substract(Matrix<T> a, Matrix<T> b) {
		return a.clone().substract(b);
	}

	// Scalar product
	public Matrix<T> multiply(T n) {
//		map((x, i, j) -> x * n);
		map((T x) -> multiply(x, n));
		return this;
	}

	// Hadamar product
	public static <T extends Number> Matrix<T> multiply(Matrix<T> a, T n) {
		return a.clone().multiply(n);
	}

	// Hadamar product
	public Matrix<T> multiply(Matrix<T> b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			error("MatrixF#multiply: rows and cols didnt match");
			error("MatrixF#multiply this: \n" + this);
			error("matrixF#multiply b: \n" + b);
		}
//		map((x, i, j) -> x * b.data[i][j]);
		map((x, i, j) -> multiply(x, b.data[i][j]));
		return this;
	}

	// Hadamar product
	public static <T extends Number> Matrix<T> multiply(Matrix<T> a, Matrix<T> b) {
		return a.clone().multiply(b);
	}

	// Matrix dot product
	public Matrix<T> dot(Matrix<T> b) {
		if (this.cols != b.rows) {
			error("MatrixF#dot: cols and rows didnt match");
			error("MatrixF#dot this: \n" + this);
			error("matrixF#dot b: \n" + b);
		}
		Matrix<T> result = new Matrix<>(type, this.rows, b.cols);
		result.map((x, i, j) -> {
			// Dot product of values in col
//			double sum = 0;
			T sum = castToT(0);
			for (int k = 0; k < this.cols; k++) {
//				sum += this.data[i][k] * b.data[k][j];
				add(sum, multiply(this.data[i][k], b.data[k][j]));
			}
//			return castToT(sum);
			return sum;
		});
		return result;
	}

	// Matrix dot product
	public static <T extends Number> Matrix<T> dot(Matrix<T> a, Matrix<T> b) {
		return a.clone().dot(b);
	}

	public Matrix<T> randomize() {
		return randomize(castToT(-1), castToT(1));
	}

	public Matrix<T> randomize(T high) {
		map((T x) -> castToT(random(high)));
		return this;
	}

	public Matrix<T> randomize(T low, T high) {
		map((x, i, j) -> castToT(random(low, high)));
		return this;
	}

	public Matrix<T> map(SimpleMapper.Generic<T> mapper) {
		for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++) {
			data[i][j] = mapper.func(data[i][j]);
		}
		return this;
	}

	public Matrix<T> map(Mapper.Generic<T> mapper) {
		for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++) {
			data[i][j] = mapper.func(data[i][j], i, j);
		}
		return this;
	}

	public static <T extends Number> Matrix<T> map(Matrix<T> matrix, SimpleMapper.Generic<T> mapper) {
		return matrix.clone().map(mapper);
	}

	public static <T extends Number> Matrix<T> map(Matrix<T> matrix, Mapper.Generic<T> mapper) {
		return matrix.clone().map(mapper);
	}

	public static <N extends Number> Matrix<N> fromArray(N[] arr) {
//		return new Matrix<T>(type, arr.length, 1).map((x, i, j) -> arr[i]);
		return new Matrix<N>(arr[0].getClass(), arr.length, 1).map((x, i, j) -> arr[i]);
	}

	@SuppressWarnings("unchecked")
	public T[] toArray() {
		try {
			T[] arr = (T[]) new Number[rows * cols];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					arr[j + i * cols] = data[i][j];
				}
			}
//			Log.infoArray(arr);
			return arr;
		} catch (Exception e) {
			error("FUCK", e);
		}
		return null;
	}

	@Override
	public String toString() {
		String s = "MatrixF[" + rows + "][" + cols + "]" + System.lineSeparator();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				s += "|" + data[i][j] + "|";
			}
			s += System.lineSeparator();
		}
		return s;
	}

	private T add(T a, T b) {
		a = castToT(valueOf(a) + valueOf(b));
		return a;
	}

	private T sub(T a, T b) {
		a = castToT(valueOf(a) - valueOf(b));
		return a;
	}

	private T multiply(T a, T b) {
		a = castToT(valueOf(a) * valueOf(b));
		return a;
	}

	@SuppressWarnings("unused")
	private T divide(T a, T b) {
		a = castToT(valueOf(a) / valueOf(b));
		return a;
	}

	@SuppressWarnings("unchecked")
	public T castToT(Number n) {
		T val = null;
		try {
			val = (T) valueOf.invoke(n, String.valueOf(n));
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return val;
	}

	public Double valueOf(T num) {
		return num.doubleValue();
	}
}
