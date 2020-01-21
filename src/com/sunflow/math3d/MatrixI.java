package com.sunflow.math3d;

import java.io.Serializable;

import com.sunflow.util.Log;
import com.sunflow.util.MathUtils;

public class MatrixI implements MathUtils, Cloneable, Serializable {
	private static final long serialVersionUID = 987250889582600596L;

	private int rows;
	private int cols;

	private int[][] data;

	public MatrixI(int r, int c) {
		rows = r;
		cols = c;
		data = new int[r][c];
	}

	public MatrixI transpose() {
		set(new MatrixI(cols, rows).map((x, i, j) -> data[j][i]));
		return this;
	}

	public MatrixI set(MatrixI m) {
		rows = m.rows;
		cols = m.cols;
		data = new int[rows][cols];
		map((x, i, j) -> m.data[i][j]);
		return this;
	}

	@Override
	public MatrixI clone() {
		return new MatrixI(rows, cols).set(this);
	}

	public static MatrixI transpose(MatrixI m) {
		return m.clone().transpose();
	}

	public MatrixI add(int n) {
		map((x, i, j) -> x + n);
		return this;
	}

	public MatrixI add(MatrixI b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			Log.err("MatrixI#add: rows and cols didnt match");
			Log.err("MatrixI#add this: \n" + this);
			Log.err("MatrixI#add b: \n" + b);
		}
		map((x, i, j) -> x + b.data[i][j]);
		return this;
	}

	public static MatrixI add(MatrixI a, MatrixI b) {
		return a.clone().add(b);
	}

	public MatrixI substract(int n) {
		map((x, i, j) -> x - n);
		return this;
	}

	public MatrixI substract(MatrixI b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			Log.err("MatrixI#substract: rows and cols didnt match");
			Log.err("MatrixI#substract this: \n" + this);
			Log.err("MatrixI#substract b: \n" + b);
		}
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this.data[i][j] -= b.data[i][j];
			}
		}
		return this;
	}

	public static MatrixI substract(MatrixI a, MatrixI b) {
		return a.clone().substract(b);
	}

	// Scalar product
	public MatrixI multiply(int n) {
		map((x, i, j) -> x * n);
		return this;
	}

	// Hadamar product
	public static MatrixI multiply(MatrixI a, int n) {
		return a.clone().multiply(n);
	}

	// Hadamar product
	public MatrixI multiply(MatrixI b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			Log.err("MatrixI#multiply: rows and cols didnt match");
			Log.err("MatrixI#multiply this: \n" + this);
			Log.err("MatrixI#multiply b: \n" + b);
		}
		map((x, i, j) -> x * b.data[i][j]);
		return this;
	}

	// Hadamar product
	public static MatrixI multiply(MatrixI a, MatrixI b) {
		return a.clone().multiply(b);
	}

	// Matrix dot product
	public MatrixI dot(MatrixI b) {
		if (this.cols != b.rows) {
			Log.err("MatrixI#dot: cols and rows didnt match");
			Log.err("MatrixI#dot this: \n" + this);
			Log.err("MatrixI#dot b: \n" + b);
		}
		MatrixI result = new MatrixI(this.rows, b.cols);
		result.map((x, i, j) -> {
			// Dot product of values in col
			int sum = 0;
			for (int k = 0; k < this.cols; k++) {
				sum += this.data[i][k] * b.data[k][j];
			}
			return sum;
		});
		return result;
	}

	// Matrix dot product
	public static MatrixI dot(MatrixI a, MatrixI b) {
		return a.clone().dot(b);
	}

	public MatrixI randomize() {
		return randomize(-1, 1);
	}

	public MatrixI randomize(int high) {
		map((x, i, j) -> random(high));
		return this;
	}

	public MatrixI randomize(int low, int high) {
		map((x, i, j) -> random(low, high));
		return this;
	}

	public MatrixI map(Mapper mapper) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				data[i][j] = mapper.func(data[i][j], i, j);
			}
		}
		return this;
	}

	public static MatrixI map(MatrixI matrix, Mapper mapper) {
		return matrix.clone().map(mapper);
	}

	public static MatrixI fromArray(int[] arr) {
		return new MatrixI(arr.length, 1).map((x, i, j) -> arr[i]);
	}

	public int[] toArray() {
		int[] arr = new int[rows * cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				arr[j + i * cols] = data[i][j];
			}
		}
		return arr;
	}

	@Override
	public String toString() {
		String s = "MatrixI[" + rows + "][" + cols + "]" + System.lineSeparator();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				s += "|" + data[i][j] + "|";
			}
			s += System.lineSeparator();
		}
		return s;
	}

	public interface Mapper {
		int func(int x, int i, int j);
	}
}
