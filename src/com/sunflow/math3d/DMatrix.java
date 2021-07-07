package com.sunflow.math3d;

import java.io.Serializable;
import java.util.function.Consumer;

import com.sunflow.logging.LogManager;
import com.sunflow.math.DVector;
import com.sunflow.util.Mapper;
import com.sunflow.util.MathUtils;
import com.sunflow.util.SimpleMapper;

public class DMatrix implements Cloneable, Serializable
//		, MathUtils, LogUtils 
{
	private static final long serialVersionUID = 8448225411066843402L;

	public int rows;
	public int cols;

	public double[][] data;

	public DMatrix(int r, int c) {
		rows = r;
		cols = c;
		data = new double[r][c];
	}

	public DMatrix(DMatrix m) {
		rows = m.rows;
		cols = m.cols;
		data = new double[rows][cols];
		map((x, i, j) -> m.data[i][j]);
	}

	public DMatrix(double[][] data) {
		if (this.rows != data.length || this.cols != data[0].length) {
			LogManager.error("MatrixF#add: rows and cols didnt match");
			LogManager.error("MatrixF#add this: \n" + this);
			LogManager.error("matrixF#add data: \n" + data);
		}
		this.data = data;
	}

	public DMatrix transpose() {
		set(new DMatrix(cols, rows).map((x, i, j) -> data[j][i]));
		return this;
	}

	public DMatrix set(DMatrix m) {
		rows = m.rows;
		cols = m.cols;
		data = new double[rows][cols];
		map((x, i, j) -> m.data[i][j]);
		return this;
	}

	public DMatrix set(double[][] data) {
		if (this.rows != data.length || this.cols != data[0].length) {
			LogManager.error("MatrixF#add: rows and cols didnt match");
			LogManager.error("MatrixF#add this: \n" + this);
			LogManager.error("matrixF#add data: \n" + data);
		}

		this.data = data;
		return this;
	}

	@Override
	public DMatrix clone() {
		return new DMatrix(rows, cols).set(this);
	}

	static public DMatrix transpose(DMatrix m) {
		return m.clone().transpose();
	}

	public DMatrix add(double n) {
		map((x, i, j) -> x + n);
		return this;
	}

	public DMatrix add(DMatrix b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			LogManager.error("MatrixF#add: rows and cols didnt match");
			LogManager.error("MatrixF#add this: \n" + this);
			LogManager.error("matrixF#add b: \n" + b);
		}
		map((x, i, j) -> x + b.data[i][j]);
		return this;
	}

	static public DMatrix add(DMatrix a, DMatrix b) {
		return a.clone().add(b);
	}

	public DMatrix substract(double n) {
		map((x, i, j) -> x - n);
		return this;
	}

	public DMatrix substract(DMatrix b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			LogManager.error("MatrixF#substract: rows and cols didnt match");
			LogManager.error("MatrixF#substract this: \n" + this);
			LogManager.error("matrixF#substract b: \n" + b);
		}
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this.data[i][j] -= b.data[i][j];
			}
		}
		return this;
	}

	static public DMatrix substract(DMatrix a, DMatrix b) {
		return a.clone().substract(b);
	}

	// Scalar product
	public DMatrix multiply(double n) {
		map((x, i, j) -> x * n);
		return this;
	}

	// Hadamar product
	static public DMatrix multiply(DMatrix a, double n) {
		return a.clone().multiply(n);
	}

	// Hadamar product
	public DMatrix multiply(DMatrix b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			LogManager.error("MatrixF#multiply: rows and cols didnt match");
			LogManager.error("MatrixF#multiply this: \n" + this);
			LogManager.error("matrixF#multiply b: \n" + b);
		}
		map((x, i, j) -> x * b.data[i][j]);
		return this;
	}

	// Hadamar product
	static public DMatrix multiply(DMatrix a, DMatrix b) {
		return a.clone().multiply(b);
	}

	// Matrix dot product
	public DMatrix dot(DMatrix b) {
		if (this.cols != b.rows) {
			LogManager.error("MatrixF#dot: cols and rows didnt match");
			LogManager.error("MatrixF#dot this:\n" + this);
			LogManager.error("matrixF#dot b:\n" + b);
		}
		DMatrix result = new DMatrix(this.rows, b.cols);
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
	static public DMatrix dot(DMatrix a, DMatrix b) {
		return a.clone().dot(b);
	}

	public DMatrix randomize() {
		return randomize(-1, 1);
	}

	public DMatrix randomize(double high) {
		map((x, i, j) -> MathUtils.instance.random(high));
		return this;
	}

	public DMatrix randomize(double low, double high) {
		map((x, i, j) -> MathUtils.instance.random(low, high));
		return this;
	}

	public DMatrix map(Mapper.Double mapper) {
		for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++)
			data[i][j] = mapper.func(data[i][j], i, j);

		return this;
	}

	public DMatrix map(SimpleMapper.Double mapper) {
		for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++)
			data[i][j] = mapper.func(data[i][j]);
		return this;
	}

	static public DMatrix map(DMatrix matrix, Mapper.Double mapper) {
		return matrix.clone().map(mapper);
	}

	static public DMatrix map(DMatrix matrix, SimpleMapper.Double mapper) {
		return matrix.clone().map(mapper);
	}

	public void forEach(Consumer<Double> consumer) {
		for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++)
			consumer.accept(data[i][j]);
	}

	static public void forEach(DMatrix matrix, Consumer<Double> consumer) {
		matrix.forEach(consumer);
	}

	static public DMatrix fromArray(double[] arr) {
		return new DMatrix(arr.length, 1).map((x, i, j) -> arr[i]);
	}

	public double[] toArray() {
		double[] arr = new double[rows * cols];
		for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++)
			arr[j + i * cols] = data[i][j];
		return arr;
	}

	@Override
	public String toString() {
//		System.out.println("tostring");
//		String s = "MatrixF[" + rows + "][" + cols + "]" + System.lineSeparator();
//
//		System.out.println(rows + "; " + cols);
//		for (int i = 0; i < rows; i++, s += System.lineSeparator()) for (int j = 0; j < cols; j++) {
////			System.out.println(i + ", " + j);
//			s += "|" + data[i][j] + "|";
//		}
//		System.out.println("tostring: " + s);
//		return s;

		System.out.println("tostring");
		StringBuilder builder = new StringBuilder("MatrixF[" + rows + "][" + cols + "]" + System.lineSeparator());

		System.out.println(rows + "; " + cols);
		for (int i = 0; i < rows; i++, builder.append(System.lineSeparator())) for (int j = 0; j < cols; j++) {
//			System.out.println(i + ", " + j);
			builder.append("|" + data[i][j] + "|");
		}
		return builder.toString();
	}

	// 3D Math
	static public DMatrix Matrix_MakeIdentity() {
		DMatrix matrix = new DMatrix(4, 4);
		matrix.data[0][0] = 1.0f;
		matrix.data[1][1] = 1.0f;
		matrix.data[2][2] = 1.0f;
		matrix.data[3][3] = 1.0f;
		return matrix;
	}

	static public DVector Matrix_MultiplyVector(DMatrix m, DVector i) {
		DVector v = new DVector();
		v.x = i.x * m.data[0][0] + i.y * m.data[1][0] + i.z * m.data[2][0] + i.w * m.data[3][0];
		v.y = i.x * m.data[0][1] + i.y * m.data[1][1] + i.z * m.data[2][1] + i.w * m.data[3][1];
		v.z = i.x * m.data[0][2] + i.y * m.data[1][2] + i.z * m.data[2][2] + i.w * m.data[3][2];
		v.w = i.x * m.data[0][3] + i.y * m.data[1][3] + i.z * m.data[2][3] + i.w * m.data[3][3];
		return v;
	}

	static public DMatrix Matrix_MultiplyMatrix(DMatrix m1, DMatrix m2) {
		DMatrix matrix = new DMatrix(4, 4);
		for (int c = 0; c < 4; c++) for (int r = 0; r < 4; r++)
			matrix.data[r][c] = 0 +
					m1.data[r][0] * m2.data[0][c] +
					m1.data[r][1] * m2.data[1][c] +
					m1.data[r][2] * m2.data[2][c] +
					m1.data[r][3] * m2.data[3][c];
		return matrix;
	}

	static public DMatrix Matrix_MakeTranslation(double x, double y, double z) {
		DMatrix matrix = new DMatrix(4, 4);
		matrix.data[0][0] = 1.0f;
		matrix.data[1][1] = 1.0f;
		matrix.data[2][2] = 1.0f;
		matrix.data[3][3] = 1.0f;
		matrix.data[3][0] = x;
		matrix.data[3][1] = y;
		matrix.data[3][2] = z;
		return matrix;
	}

	static public DMatrix Matrix_MakeScale(double s) { return Matrix_MakeScale(s, s, s); }

	static public DMatrix Matrix_MakeScale(double x, double y, double z) {
		DMatrix matrix = new DMatrix(4, 4);
		matrix.data[0][0] = x;
		matrix.data[1][1] = y;
		matrix.data[2][2] = z;
		matrix.data[3][3] = 1.0f;
		matrix.data[3][0] = 1.0f;
		matrix.data[3][1] = 1.0f;
		matrix.data[3][2] = 1.0f;
		return matrix;
	}

	static public DMatrix Matrix_MakeRotationX(double angle) {
		DMatrix matrix = new DMatrix(4, 4);
		matrix.data[0][0] = 1.0f;
		matrix.data[1][1] = MathUtils.instance.cos(angle);
		matrix.data[1][2] = -MathUtils.instance.sin(angle);
		matrix.data[2][1] = MathUtils.instance.sin(angle);
		matrix.data[2][2] = MathUtils.instance.cos(angle);
		matrix.data[3][3] = 1.0f;
		return matrix;
	}

	static public DMatrix Matrix_MakeRotationY(double angle) {
		DMatrix matrix = new DMatrix(4, 4);
		matrix.data[0][0] = MathUtils.instance.cos(angle);
		matrix.data[0][2] = MathUtils.instance.sin(angle);
		matrix.data[2][0] = -MathUtils.instance.sin(angle);
		matrix.data[1][1] = 1.0f;
		matrix.data[2][2] = MathUtils.instance.cos(angle);
		matrix.data[3][3] = 1.0f;
		return matrix;
	}

	static public DMatrix Matrix_MakeRotationZ(double angle) {
		DMatrix matrix = new DMatrix(4, 4);
		matrix.data[0][0] = MathUtils.instance.cos(angle);
		matrix.data[0][1] = -MathUtils.instance.sin(angle);
		matrix.data[1][0] = MathUtils.instance.sin(angle);
		matrix.data[1][1] = MathUtils.instance.cos(angle);
		matrix.data[2][2] = 1.0f;
		matrix.data[3][3] = 1.0f;
		return matrix;
	}

	static public DMatrix Matrix_MakeProjection(double fFovDegrees, double fAspectRatio, double fNear, double fFar) {
		double fFovRad = 1.0f / MathUtils.instance.tan(MathUtils.instance.radians(fFovDegrees * 0.5f));
//		double fFovRad = 1.0f / MathUtils.instance.tan(MathUtils.instance.degrees(fFovDegrees * 0.5f));
		DMatrix matrix = new DMatrix(4, 4);

		matrix.data[0][0] = fAspectRatio * fFovRad;
		matrix.data[1][1] = fFovRad;
		matrix.data[2][2] = fFar / (fFar - fNear);

		matrix.data[3][2] = (-fFar * fNear) / (fFar - fNear);
		matrix.data[2][3] = 1.0f;
		matrix.data[3][3] = 0.0f;
		return matrix;
	}

	static public DMatrix Matrix_PointAt(DVector pos, DVector target, DVector up) {
		// Calculate new forward direction
		DVector newForward = DVector.sub(target, pos);
		newForward.normalize();

		// Calculate new up direction
		DVector a = DVector.mult(newForward, DVector.dot(up, newForward));
		DVector newUp = DVector.sub(up, a);
		newUp.normalize();

		// New Right direction is easy, its just cross product
		DVector newRight = DVector.cross(newUp, newForward);

		// Construct Dimensioning and Translation Matrix
		DMatrix matrix = new DMatrix(4, 4);
		matrix.data[0][0] = newRight.x;
		matrix.data[0][1] = newRight.y;
		matrix.data[0][2] = newRight.z;
		matrix.data[0][3] = 0.0f;
		matrix.data[1][0] = newUp.x;
		matrix.data[1][1] = newUp.y;
		matrix.data[1][2] = newUp.z;
		matrix.data[1][3] = 0.0f;
		matrix.data[2][0] = newForward.x;
		matrix.data[2][1] = newForward.y;
		matrix.data[2][2] = newForward.z;
		matrix.data[2][3] = 0.0f;
		matrix.data[3][0] = pos.x;
		matrix.data[3][1] = pos.y;
		matrix.data[3][2] = pos.z;
		matrix.data[3][3] = 1.0f;
		return matrix;
	}

	/** Only for Rotation/Translation Matrices */
	static public DMatrix Matrix_QuickInverse(DMatrix m) {
		DMatrix matrix = new DMatrix(4, 4);
		matrix.data[0][0] = m.data[0][0];
		matrix.data[0][1] = m.data[1][0];
		matrix.data[0][2] = m.data[2][0];
		matrix.data[0][3] = 0.0f;
		matrix.data[1][0] = m.data[0][1];
		matrix.data[1][1] = m.data[1][1];
		matrix.data[1][2] = m.data[2][1];
		matrix.data[1][3] = 0.0f;
		matrix.data[2][0] = m.data[0][2];
		matrix.data[2][1] = m.data[1][2];
		matrix.data[2][2] = m.data[2][2];
		matrix.data[2][3] = 0.0f;
		matrix.data[3][0] = -(m.data[3][0] * matrix.data[0][0] + m.data[3][1] * matrix.data[1][0] + m.data[3][2] * matrix.data[2][0]);
		matrix.data[3][1] = -(m.data[3][0] * matrix.data[0][1] + m.data[3][1] * matrix.data[1][1] + m.data[3][2] * matrix.data[2][1]);
		matrix.data[3][2] = -(m.data[3][0] * matrix.data[0][2] + m.data[3][1] * matrix.data[1][2] + m.data[3][2] * matrix.data[2][2]);
		matrix.data[3][3] = 1.0f;
		return matrix;
	}

}
