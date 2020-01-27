package com.sunflow.math3d;

import java.io.Serializable;

import com.sunflow.util.LogUtils;
import com.sunflow.util.MathUtils;

public class MatrixF implements MathUtils, LogUtils, Cloneable, Serializable {
	private static final long serialVersionUID = 6051103583793756019L;

	private int rows;
	private int cols;

	private float[][] data;

	public MatrixF(int r, int c) {
		rows = r;
		cols = c;
		data = new float[r][c];
	}

	public MatrixF transpose() {
		set(new MatrixF(cols, rows).map((x, i, j) -> data[j][i]));
		return this;
	}

	public MatrixF set(MatrixF m) {
		rows = m.rows;
		cols = m.cols;
		data = new float[rows][cols];
		map((x, i, j) -> m.data[i][j]);
		return this;
	}

	@Override
	public MatrixF clone() {
		return new MatrixF(rows, cols).set(this);
	}

	public static MatrixF transpose(MatrixF m) {
		return m.clone().transpose();
	}

	public MatrixF add(float n) {
		map((x, i, j) -> x + n);
		return this;
	}

	public MatrixF add(MatrixF b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			error("MatrixF#add: rows and cols didnt match");
			error("MatrixF#add this: \n" + this);
			error("matrixF#add b: \n" + b);
		}
		map((x, i, j) -> x + b.data[i][j]);
		return this;
	}

	public static MatrixF add(MatrixF a, MatrixF b) {
		return a.clone().add(b);
	}

	public MatrixF substract(float n) {
		map((x, i, j) -> x - n);
		return this;
	}

	public MatrixF substract(MatrixF b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			error("MatrixF#substract: rows and cols didnt match");
			error("MatrixF#substract this: \n" + this);
			error("matrixF#substract b: \n" + b);
		}
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this.data[i][j] -= b.data[i][j];
			}
		}
		return this;
	}

	public static MatrixF substract(MatrixF a, MatrixF b) {
		return a.clone().substract(b);
	}

	// Scalar product
	public MatrixF multiply(float n) {
		map((x, i, j) -> x * n);
		return this;
	}

	// Hadamar product
	public static MatrixF multiply(MatrixF a, float n) {
		return a.clone().multiply(n);
	}

	// Hadamar product
	public MatrixF multiply(MatrixF b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			error("MatrixF#multiply: rows and cols didnt match");
			error("MatrixF#multiply this: \n" + this);
			error("matrixF#multiply b: \n" + b);
		}
		map((x, i, j) -> x * b.data[i][j]);
		return this;
	}

	// Hadamar product
	public static MatrixF multiply(MatrixF a, MatrixF b) {
		return a.clone().multiply(b);
	}

	// Matrix dot product
	public MatrixF dot(MatrixF b) {
		if (this.cols != b.rows) {
			error("MatrixF#dot: cols and rows didnt match");
			error("MatrixF#dot this: \n" + this);
			error("matrixF#dot b: \n" + b);
		}
		MatrixF result = new MatrixF(this.rows, b.cols);
		result.map((x, i, j) -> {
			// Dot product of values in col
			float sum = 0;
			for (int k = 0; k < this.cols; k++) {
				sum += this.data[i][k] * b.data[k][j];
			}
			return sum;
		});
		return result;
	}

	// Matrix dot product
	public static MatrixF dot(MatrixF a, MatrixF b) {
		return a.clone().dot(b);
	}

	public MatrixF randomize() {
		return randomize(-1, 1);
	}

	public MatrixF randomize(float high) {
		map((x, i, j) -> random(high));
		return this;
	}

	public MatrixF randomize(float low, float high) {
		map((x, i, j) -> random(low, high));
		return this;
	}

	public MatrixF map(Mapper mapper) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				data[i][j] = mapper.func(data[i][j], i, j);
			}
		}
		return this;
	}

	public static MatrixF map(MatrixF matrix, Mapper mapper) {
		return matrix.clone().map(mapper);
	}

	public static MatrixF fromArray(float[] arr) {
		return new MatrixF(arr.length, 1).map((x, i, j) -> arr[i]);
	}

	public float[] toArray() {
		float[] arr = new float[rows * cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				arr[j + i * cols] = data[i][j];
			}
		}
		return arr;
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

	public interface Mapper {
		float func(float x, int i, int j);
	}
}
