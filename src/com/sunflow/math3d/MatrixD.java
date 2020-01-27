package com.sunflow.math3d;

import java.io.Serializable;

import com.sunflow.util.LogUtils;
import com.sunflow.util.MathUtils;

public class MatrixD implements MathUtils, LogUtils, Cloneable, Serializable {
	private static final long serialVersionUID = -6231402347735150578L;

	private int rows;
	private int cols;

	private double[][] data;

	public MatrixD(int r, int c) {
		rows = r;
		cols = c;
		data = new double[r][c];
	}

	public MatrixD transpose() {
		set(new MatrixD(cols, rows).map((x, i, j) -> data[j][i]));
		return this;
	}

	public MatrixD set(MatrixD m) {
		rows = m.rows;
		cols = m.cols;
		data = new double[rows][cols];
		map((x, i, j) -> m.data[i][j]);
		return this;
	}

	@Override
	public MatrixD clone() {
		return new MatrixD(rows, cols).set(this);
	}

	public static MatrixD transpose(MatrixD m) {
		return m.clone().transpose();
	}

	public MatrixD add(double n) {
		map((x, i, j) -> x + n);
		return this;
	}

	public MatrixD add(MatrixD b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			error("MatrixD#add: rows and cols didnt match");
			error("MatrixD#add this: \n" + this);
			error("MatrixD#add b: \n" + b);
		}
		map((x, i, j) -> x + b.data[i][j]);
		return this;
	}

	public static MatrixD add(MatrixD a, MatrixD b) {
		return a.clone().add(b);
	}

	public MatrixD substract(double n) {
		map((x, i, j) -> x - n);
		return this;
	}

	public MatrixD substract(MatrixD b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			error("MatrixD#substract: rows and cols didnt match");
			error("MatrixD#substract this: \n" + this);
			error("MatrixD#substract b: \n" + b);
		}
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this.data[i][j] -= b.data[i][j];
			}
		}
		return this;
	}

	public static MatrixD substract(MatrixD a, MatrixD b) {
		return a.clone().substract(b);
	}

	// Scalar product
	public MatrixD multiply(double n) {
		map((x, i, j) -> x * n);
		return this;
	}

	// Hadamar product
	public static MatrixD multiply(MatrixD a, double n) {
		return a.clone().multiply(n);
	}

	// Hadamar product
	public MatrixD multiply(MatrixD b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			error("MatrixD#multiply: rows and cols didnt match");
			error("MatrixD#multiply this: \n" + this);
			error("MatrixD#multiply b: \n" + b);
		}
		map((x, i, j) -> x * b.data[i][j]);
		return this;
	}

	// Hadamar product
	public static MatrixD multiply(MatrixD a, MatrixD b) {
		return a.clone().multiply(b);
	}

	// Matrix dot product
	public MatrixD dot(MatrixD b) {
		if (this.cols != b.rows) {
			error("MatrixD#dot: cols and rows didnt match");
			error("MatrixD#dot this: \n" + this);
			error("MatrixD#dot b: \n" + b);
		}
		MatrixD result = new MatrixD(this.rows, b.cols);
		result.map((x, i, j) -> {
			// Dot product of values in col
			double sum = 0;
			for (int k = 0; k < this.cols; k++) {
				sum += this.data[i][k] * b.data[k][j];
			}
			return sum;
		});
		return result;
	}

	// Matrix dot product
	public static MatrixD dot(MatrixD a, MatrixD b) {
		return a.clone().dot(b);
	}

	public MatrixD randomize() {
		return randomize(-1, 1);
	}

	public MatrixD randomize(double high) {
		map((x, i, j) -> random(high));
		return this;
	}

	public MatrixD randomize(double low, double high) {
		map((x, i, j) -> random(low, high));
		return this;
	}

	public MatrixD map(Mapper mapper) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				data[i][j] = mapper.func(data[i][j], i, j);
			}
		}
		return this;
	}

	public static MatrixD map(MatrixD matrix, Mapper mapper) {
		return matrix.clone().map(mapper);
	}

	public static MatrixD fromArray(double[] arr) {
		return new MatrixD(arr.length, 1).map((x, i, j) -> arr[i]);
	}

	public double[] toArray() {
		double[] arr = new double[rows * cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				arr[j + i * cols] = data[i][j];
			}
		}
		return arr;
	}

	@Override
	public String toString() {
		String s = "MatrixD[" + rows + "][" + cols + "]" + System.lineSeparator();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				s += "|" + data[i][j] + "|";
			}
			s += System.lineSeparator();
		}
		return s;
	}

	public interface Mapper extends Serializable {
		double func(double x, int i, int j);
	}
}
