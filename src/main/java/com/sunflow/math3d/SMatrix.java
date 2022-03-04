package com.sunflow.math3d;

import java.io.Serializable;
import java.util.function.Consumer;

import com.sunflow.logging.LogManager;
import com.sunflow.math.SVector;
import com.sunflow.util.Mapper;
import com.sunflow.util.MathUtils;
import com.sunflow.util.SimpleMapper;

public class SMatrix implements Cloneable, Serializable
//		, MathUtils, LogUtils
{
	private static final long serialVersionUID = 8448225411066843402L;

	public int rows;
	public int cols;

	public float[][] data;

	public SMatrix(int r, int c) {
		rows = r;
		cols = c;
		data = new float[r][c];
	}

	public SMatrix(SMatrix m) {
		rows = m.rows;
		cols = m.cols;
		data = new float[rows][cols];
		map((x, i, j) -> m.data[i][j]);
	}

	public SMatrix(float[][] data) {
		if (this.rows != data.length || this.cols != data[0].length) {
			LogManager.error("MatrixF#add: rows and cols didnt match");
			LogManager.error("MatrixF#add this: \n" + this);
			LogManager.error("matrixF#add data: \n" + data);
		}
		this.data = data;
	}

	public SMatrix transpose() {
		set(new SMatrix(cols, rows).map((x, i, j) -> data[j][i]));
		return this;
	}

	public SMatrix set(SMatrix m) {
		rows = m.rows;
		cols = m.cols;
		data = new float[rows][cols];
		map((x, i, j) -> m.data[i][j]);
		return this;
	}

	public SMatrix set(float[][] data) {
		if (this.rows != data.length || this.cols != data[0].length) {
			LogManager.error("MatrixF#add: rows and cols didnt match");
			LogManager.error("MatrixF#add this: \n" + this);
			LogManager.error("matrixF#add data: \n" + data);
		}

		this.data = data;
		return this;
	}

	@Override
	public SMatrix clone() {
		return new SMatrix(rows, cols).set(this);
	}

	static public SMatrix transpose(SMatrix m) {
		return m.clone().transpose();
	}

	public SMatrix add(float n) {
		map((x, i, j) -> x + n);
		return this;
	}

	public SMatrix add(SMatrix b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			LogManager.error("MatrixF#add: rows and cols didnt match");
			LogManager.error("MatrixF#add this: \n" + this);
			LogManager.error("matrixF#add b: \n" + b);
		}
		map((x, i, j) -> x + b.data[i][j]);
		return this;
	}

	static public SMatrix add(SMatrix a, SMatrix b) {
		return a.clone().add(b);
	}

	public SMatrix substract(float n) {
		map((x, i, j) -> x - n);
		return this;
	}

	public SMatrix substract(SMatrix b) {
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

	static public SMatrix substract(SMatrix a, SMatrix b) {
		return a.clone().substract(b);
	}

	// Scalar product
	public SMatrix multiply(float n) {
		map((x, i, j) -> x * n);
		return this;
	}

	// Hadamar product
	static public SMatrix multiply(SMatrix a, float n) {
		return a.clone().multiply(n);
	}

	// Hadamar product
	public SMatrix multiply(SMatrix b) {
		if (this.rows != b.rows || this.cols != b.cols) {
			LogManager.error("MatrixF#multiply: rows and cols didnt match");
			LogManager.error("MatrixF#multiply this: \n" + this);
			LogManager.error("matrixF#multiply b: \n" + b);
		}
		map((x, i, j) -> x * b.data[i][j]);
		return this;
	}

	// Hadamar product
	static public SMatrix multiply(SMatrix a, SMatrix b) {
		return a.clone().multiply(b);
	}

	// Matrix dot product
	public SMatrix dot(SMatrix b) {
		if (this.cols != b.rows) {
			LogManager.error("MatrixF#dot: cols and rows didnt match");
			LogManager.error("MatrixF#dot this:\n" + this);
			LogManager.error("matrixF#dot b:\n" + b);
//			new Throwable().printStackTrace();
		}
		SMatrix result = new SMatrix(this.rows, b.cols);
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
	static public SMatrix dot(SMatrix a, SMatrix b) {
		return a.clone().dot(b);
	}

	public SMatrix randomize() {
		return randomize(-1, 1);
	}

	public SMatrix randomize(float high) {
		map((x, i, j) -> MathUtils.instance.random(high));
		return this;
	}

	public SMatrix randomize(float low, float high) {
		map((x, i, j) -> MathUtils.instance.random(low, high));
		return this;
	}

	public SMatrix map(Mapper mapper) {
		for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++)
			data[i][j] = mapper.func(data[i][j], i, j);

		return this;
	}

	public SMatrix map(SimpleMapper mapper) {
		for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++)
			data[i][j] = mapper.func(data[i][j]);
		return this;
	}

	static public SMatrix map(SMatrix matrix, Mapper mapper) {
		return matrix.clone().map(mapper);
	}

	static public SMatrix map(SMatrix matrix, SimpleMapper mapper) {
		return matrix.clone().map(mapper);
	}

	public void forEach(Consumer<Float> consumer) {
		for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++)
			consumer.accept(data[i][j]);
	}

	static public void forEach(SMatrix matrix, Consumer<Float> consumer) {
		matrix.forEach(consumer);
	}

	static public SMatrix fromArray(float[] arr) {
		return new SMatrix(arr.length, 1).map((x, i, j) -> arr[i]);
	}

	public float[] toArray() {
		float[] arr = new float[rows * cols];
		for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++)
			arr[j + i * cols] = data[i][j];
		return arr;
	}

	public float[][] data() { return data; }

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

		StringBuilder builder = new StringBuilder("MatrixF[" + rows + "][" + cols + "]" + System.lineSeparator());
		for (int i = 0; i < rows; i++, builder.append(System.lineSeparator())) for (int j = 0; j < cols; j++) {
//			System.out.println(i + ", " + j);
			builder.append("|" + data[i][j] + "|");
		}
		return builder.toString();
	}

	// 3D Math
	static public SMatrix Matrix_MakeIdentity() {
		SMatrix matrix = new SMatrix(4, 4);
		matrix.data[0][0] = 1.0f;
		matrix.data[1][1] = 1.0f;
		matrix.data[2][2] = 1.0f;
		matrix.data[3][3] = 1.0f;
		return matrix;
	}

	static public SVector Matrix_MultiplyVector(SMatrix m, SVector i) {
		SVector v = new SVector();
		v.x = i.x * m.data[0][0] + i.y * m.data[1][0] + i.z * m.data[2][0] + i.w * m.data[3][0];
		v.y = i.x * m.data[0][1] + i.y * m.data[1][1] + i.z * m.data[2][1] + i.w * m.data[3][1];
		v.z = i.x * m.data[0][2] + i.y * m.data[1][2] + i.z * m.data[2][2] + i.w * m.data[3][2];
		v.w = i.x * m.data[0][3] + i.y * m.data[1][3] + i.z * m.data[2][3] + i.w * m.data[3][3];
		return v;
	}

	static public SMatrix Matrix_MultiplyMatrix(SMatrix m1, SMatrix m2) {
		SMatrix matrix = new SMatrix(4, 4);
		for (int c = 0; c < 4; c++) for (int r = 0; r < 4; r++)
			matrix.data[r][c] = 0 +
					m1.data[r][0] * m2.data[0][c] +
					m1.data[r][1] * m2.data[1][c] +
					m1.data[r][2] * m2.data[2][c] +
					m1.data[r][3] * m2.data[3][c];
		return matrix;
	}

	static public SMatrix Matrix_MakeTranslation(float x, float y, float z) {
		SMatrix matrix = new SMatrix(4, 4);
		matrix.data[0][0] = 1.0f;
		matrix.data[1][1] = 1.0f;
		matrix.data[2][2] = 1.0f;
		matrix.data[3][3] = 1.0f;
		matrix.data[3][0] = x;
		matrix.data[3][1] = y;
		matrix.data[3][2] = z;
		return matrix;
	}

	static public SMatrix Matrix_MakeScale(float s) { return Matrix_MakeScale(s, s, s); }

	static public SMatrix Matrix_MakeScale(float x, float y, float z) {
		SMatrix matrix = new SMatrix(4, 4);
		matrix.data[0][0] = x;
		matrix.data[1][1] = y;
		matrix.data[2][2] = z;
		matrix.data[3][3] = 1.0f;
		matrix.data[3][0] = 1.0f;
		matrix.data[3][1] = 1.0f;
		matrix.data[3][2] = 1.0f;
		return matrix;
	}

	static public SMatrix Matrix_MakeRotationX(float angle) {
		SMatrix matrix = new SMatrix(4, 4);
		matrix.data[0][0] = 1.0f;
		matrix.data[1][1] = MathUtils.instance.cos(angle);
		matrix.data[1][2] = -MathUtils.instance.sin(angle);
		matrix.data[2][1] = MathUtils.instance.sin(angle);
		matrix.data[2][2] = MathUtils.instance.cos(angle);
		matrix.data[3][3] = 1.0f;
		return matrix;
	}

	static public SMatrix Matrix_MakeRotationY(float angle) {
		SMatrix matrix = new SMatrix(4, 4);
		matrix.data[0][0] = MathUtils.instance.cos(angle);
		matrix.data[0][2] = MathUtils.instance.sin(angle);
		matrix.data[2][0] = -MathUtils.instance.sin(angle);
		matrix.data[1][1] = 1.0f;
		matrix.data[2][2] = MathUtils.instance.cos(angle);
		matrix.data[3][3] = 1.0f;
		return matrix;
	}

	static public SMatrix Matrix_MakeRotationZ(float angle) {
		SMatrix matrix = new SMatrix(4, 4);
		matrix.data[0][0] = MathUtils.instance.cos(angle);
		matrix.data[0][1] = -MathUtils.instance.sin(angle);
		matrix.data[1][0] = MathUtils.instance.sin(angle);
		matrix.data[1][1] = MathUtils.instance.cos(angle);
		matrix.data[2][2] = 1.0f;
		matrix.data[3][3] = 1.0f;
		return matrix;
	}

	static public SMatrix Matrix_MakeProjection(float fFov, float fAspectRatio, float fNear, float fFar) {
		float fFovRad = 1.0f / MathUtils.instance.tan(fFov * 0.5f);
//		float fFovRad = 1.0f / MathUtils.instance.tan(MathUtils.instance.degrees(fFovDegrees * 0.5f));
		SMatrix matrix = new SMatrix(4, 4);

		matrix.data[0][0] = fAspectRatio * fFovRad;
		matrix.data[1][1] = fFovRad;
		matrix.data[2][2] = fFar / (fFar - fNear);

		matrix.data[3][2] = (-fFar * fNear) / (fFar - fNear);
		matrix.data[2][3] = 1.0f;
		matrix.data[3][3] = 0.0f;
		return matrix;
	}

	static public SMatrix Matrix_PointAt(SVector pos, SVector target, SVector up) {
		// Calculate new forward direction
		SVector newForward = SVector.sub(target, pos);
		newForward.normalize();

		// Calculate new up direction
		SVector a = SVector.mult(newForward, SVector.dot(up, newForward));
		SVector newUp = SVector.sub(up, a);
		newUp.normalize();

		// New Right direction is easy, its just cross product
		SVector newRight = SVector.cross(newUp, newForward);

		// Construct Dimensioning and Translation Matrix
		SMatrix matrix = new SMatrix(4, 4);
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
	static public SMatrix Matrix_QuickInverse(SMatrix m) {
		SMatrix matrix = new SMatrix(4, 4);
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

	static public SMatrix mat4(SVector translation, SVector scale, SVector rotation) {
		final float c3 = MathUtils.instance.cos(rotation.z);
		final float s3 = MathUtils.instance.sin(rotation.z);
		final float c2 = MathUtils.instance.cos(rotation.x);
		final float s2 = MathUtils.instance.sin(rotation.x);
		final float c1 = MathUtils.instance.cos(rotation.y);
		final float s1 = MathUtils.instance.sin(rotation.y);
		return new SMatrix(new float[][] {
				{
						scale.x * (c1 * c3 + s1 * s2 * s3),
						scale.x * (c2 * s3),
						scale.x * (c1 * s2 * s3 - c3 * s1),
						0.0f,
				},
				{
						scale.y * (c3 * s1 * s2 - c1 * s3),
						scale.y * (c2 * c3),
						scale.y * (c1 * c3 * s2 + s1 * s3),
						0.0f,
				},
				{
						scale.z * (c2 * s1),
						scale.z * (-s2),
						scale.z * (c1 * c2),
						0.0f,
				},
				{ translation.x, translation.y, translation.z, 1.0f }
		});
	}

	static public SMatrix mat4(float rz, float rx, float ry) {
		final float c3 = MathUtils.instance.cos(rz);
		final float s3 = MathUtils.instance.sin(rz);
		final float c2 = MathUtils.instance.cos(rx);
		final float s2 = MathUtils.instance.sin(rx);
		final float c1 = MathUtils.instance.cos(ry);
		final float s1 = MathUtils.instance.sin(ry);
		return new SMatrix(new float[][] {
				{
						c1 * c3 + s1 * s2 * s3,
						c2 * s3,
						c1 * s2 * s3 - c3 * s1,
						0.0f,
				},
				{
						c3 * s1 * s2 - c1 * s3,
						c2 * c3,
						c1 * c3 * s2 + s1 * s3,
						0.0f,
				},
				{
						c2 * s1,
						-s2,
						c1 * c2,
						0.0f,
				},
				{ 0.0f, 0.0f, 0.0f, 1.0f }
		});
	}

	public static SMatrix initRotation(SVector forward, SVector up) {
		SVector f = forward.normalized();
		SVector r = up.normalized();
		r = r.cross(f);

		SVector u = f.cross(r);

		return initRotation(f, u, r);
	}

//	public static SMatrix initRotation(SVector forward, SVector up) {
//		SMatrix result = new SMatrix(4, 4);
//		
//		forward = forward.normalized();
//		up = up.normalized();
//		SVector right = up.cross(forward);
//
//		result.data[0][0] = right.x;
//		result.data[0][1] = right.y;
//		result.data[0][2] = right.z;
//
//		result.data[1][0] = up.x;
//		result.data[1][1] = up.y;
//		result.data[1][2] = up.z;
//
//		result.data[2][0] = forward.x;
//		result.data[2][1] = forward.y;
//		result.data[2][2] = forward.z;
//
//		result.data[3][3] = 1;
//
//		return result;
//	}

	public static SMatrix initRotation(SVector forward, SVector up, SVector right) {
		SMatrix result = new SMatrix(4, 4);

		forward = forward.normalized();
		up = up.normalized();
		right = right.normalized();

		result.data[0][0] = right.x;
		result.data[0][1] = right.y;
		result.data[0][2] = right.z;

		result.data[1][0] = up.x;
		result.data[1][1] = up.y;
		result.data[1][2] = up.z;

		result.data[2][0] = forward.x;
		result.data[2][1] = forward.y;
		result.data[2][2] = forward.z;

		result.data[3][3] = 1;

		return result;
	}
}
