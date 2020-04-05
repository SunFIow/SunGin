package com.sunflow.math3d;

import com.sunflow.math.SVector;
import com.sunflow.util.MathUtils;

public class SMatrix3D {

	public SVector pos;
	private MatrixF rotX, rotY, rotZ;
	private float angleX, angleY, angleZ;

	public SMatrix3D() {
		pos = new SVector();
		angleX = angleY = angleZ = 0;
		rotateX(0);
		rotateY(0);
		rotateZ(0);
	}

	public final void translate(float x, float y, float z) { pos.add(rotated(new SVector(x, y, z))); }

	public final void translateTo(float x, float y, float z) { pos.set(rotated(new SVector(x, y, z))); }

	public final void rotateXTo(float angle) { rotateX(angle - angleX); }

	public final void rotateYTo(float angle) { rotateY(angle - angleY); }

	public final void rotateZTo(float angle) { rotateZ(angle - angleZ); }

	public final void rotateX(float angle) {
		angleX += angle;
		float[][] rotArrX = {
				{ 1, 0, 0 },
				{ 0, MathUtils.instance.cos(angleX), -MathUtils.instance.sin(angleX) },
				{ 0, MathUtils.instance.sin(angleX), MathUtils.instance.cos(angleX) }
		};
		rotX = new MatrixF(3, 3);
		rotX.set(rotArrX);
	}

	public final void rotateY(float angle) {
		angleY += angle;
		float[][] rotArrY = {
				{ MathUtils.instance.cos(angleY), 0, MathUtils.instance.sin(angleY) },
				{ 0, 1, 0 },
				{ -MathUtils.instance.sin(angleY), 0, MathUtils.instance.cos(angleY) }
		};
		rotY = new MatrixF(3, 3);
		rotY.set(rotArrY);
	}

	public final void rotateZ(float angle) {
		angleZ += angle;
		float[][] rotArrZ = {
				{ MathUtils.instance.cos(angleZ), -MathUtils.instance.sin(angleZ), 0 },
				{ MathUtils.instance.sin(angleZ), MathUtils.instance.cos(angleZ), 0 },
				{ 0, 0, 1 }
		};
		rotZ = new MatrixF(3, 3);
		rotZ.set(rotArrZ);
	}

	public final SVector rotated(SVector pos) {
		SVector rotated = matmul(getRotationMatrixX(), pos);
		rotated = matmul(getRotationMatrixY(), rotated);
		rotated = matmul(getRotationMatrixZ(), rotated);
		return rotated;
	}

	private final SVector matmul(MatrixF a, SVector b) {
		MatrixF m = b.get((MatrixF) null);
		MatrixF matmul = a.dot(m);
		return SVector.fromMatrix(matmul);
	}

	public final float getRotationX() { return angleX; }

	public final float getRotationY() { return angleY; }

	public final float getRotationZ() { return angleZ; }

	public final float[] getRotation() { return new float[] { angleX, angleY, angleZ }; }

	public final MatrixF getRotationMatrixX() { return rotX; }

	public final MatrixF getRotationMatrixY() { return rotY; }

	public final MatrixF getRotationMatrixZ() { return rotZ; }

	public final MatrixF[] getRotationMatrix() { return new MatrixF[] { rotX, rotY, rotZ }; }
}
