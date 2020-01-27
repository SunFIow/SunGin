package com.sunflow.math3d;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sunflow.util.LogUtils;
import com.sunflow.util.MathUtils;

public class Matrix<N extends Number> implements MathUtils, LogUtils, Cloneable, Serializable {
	private static final long serialVersionUID = -6146337714963647821L;

	private int rows, cols;

//	private float[][] data;
	private N[][] data;

	private Class<N> type;
	private Method valueOf;
//	private T typeVal;

	@SuppressWarnings("unchecked")
	public Matrix(Class<? extends Number> clazz, int r, int c) {
		rows = r;
		cols = c;
//		data = new float[r][c];
		data = (N[][]) new Number[r][c];

		type = (Class<N>) clazz;
		try {
			valueOf = type.getDeclaredMethod("valueOf", String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

	public Matrix<N> transpose() {
		set(new Matrix<N>(type, cols, rows).map((x, i, j) -> data[j][i]));
		return this;
	}

	@SuppressWarnings("unchecked")
	public Matrix<N> set(Matrix<N> m) {
		rows = m.rows;
		cols = m.cols;
		data = (N[][]) new Number[rows][cols];
		map((x, i, j) -> m.data[i][j]);

		type = m.type;
		valueOf = m.valueOf;

		return this;
	}

	@Override
	public Matrix<N> clone() {
		return new Matrix<N>(type, rows, cols).set(this);
	}

	public static <T extends Number> Matrix<T> transpose(Matrix<T> m) {
		return m.clone().transpose();
	}

	public Matrix<N> add(N n) {
//		map((x, i, j) -> x + n);
		map((x, i, j) -> add(x, n));
		return this;
	}

	public Matrix<N> add(Matrix<N> b) {
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

	public Matrix<N> substract(N n) {
//		map((x, i, j) -> x - n);
		map((x, i, j) -> sub(x, n));
		return this;
	}

	public Matrix<N> substract(Matrix<N> b) {
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
	public Matrix<N> multiply(N n) {
//		map((x, i, j) -> x * n);
		map((N x) -> multiply(x, n));
		return this;
	}

	// Hadamar product
	public static <T extends Number> Matrix<T> multiply(Matrix<T> a, T n) {
		return a.clone().multiply(n);
	}

	// Hadamar product
	public Matrix<N> multiply(Matrix<N> b) {
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
	public Matrix<N> dot(Matrix<N> b) {
		if (this.cols != b.rows) {
			error("MatrixF#dot: cols and rows didnt match");
			error("MatrixF#dot this: \n" + this);
			error("matrixF#dot b: \n" + b);
		}
		Matrix<N> result = new Matrix<>(type, this.rows, b.cols);
		result.map((x, i, j) -> {
			// Dot product of values in col
//			double sum = 0;
			N sum = castToN(0);
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

	public Matrix<N> randomize() {
		return randomize(castToN(-1), castToN(1));
	}

	public Matrix<N> randomize(N high) {
		map((N x) -> castToN(random(high)));
		return this;
	}

	public Matrix<N> randomize(N low, N high) {
		map((x, i, j) -> castToN(random(low, high)));
		return this;
	}

	public Matrix<N> map(MapperSimple mapper) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				data[i][j] = castToN(mapper.func(valueOf(data[i][j])));
			}
		}
		return this;
	}

	public Matrix<N> map(MapperS<N> mapper) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				data[i][j] = mapper.func(data[i][j]);
			}
		}
		return this;
	}

	public Matrix<N> map(Mapper<N> mapper) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				data[i][j] = mapper.func(data[i][j], i, j);
			}
		}
		return this;
	}

	public static <T extends Number> Matrix<T> map(Matrix<T> matrix, MapperSimple mapper) {
		return matrix.clone().map(mapper);
	}

	public static <T extends Number> Matrix<T> map(Matrix<T> matrix, Mapper<T> mapper) {
		return matrix.clone().map(mapper);
	}

	public static <N extends Number> Matrix<N> fromArray(N[] arr) {
//		return new Matrix<T>(type, arr.length, 1).map((x, i, j) -> arr[i]);
		return new Matrix<N>(arr[0].getClass(), arr.length, 1).map((x, i, j) -> arr[i]);
	}

	@SuppressWarnings("unchecked")
	public N[] toArray() {
		try {
			N[] arr = (N[]) new Number[rows * cols];
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

	private N add(N a, N b) {
		a = castToN(valueOf(a) + valueOf(b));
		return a;
	}

	private N sub(N a, N b) {
		a = castToN(valueOf(a) - valueOf(b));
		return a;
	}

	private N multiply(N a, N b) {
		a = castToN(valueOf(a) * valueOf(b));
		return a;
	}

	@SuppressWarnings("unused")
	private N divide(N a, N b) {
		a = castToN(valueOf(a) / valueOf(b));
		return a;
	}

	public N castNToN(N n) {
		return castToN(n.doubleValue());
	}

	@SuppressWarnings("unchecked")
	public N castToN(Number n) {
		N val = null;
		try {
			val = (N) valueOf.invoke(n, String.valueOf(n));
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return val;
	}

	public Double valueOf(N num) {
		return num.doubleValue();
	}

	public interface Mapper<T> {
		T func(T x, int i, int j);
	}

	public interface MapperS<T> {
		T func(T x);
	}

	public interface MapperSimple {
		double func(double x);
	}
}
