package com.sunflow.gfx;

import com.sunflow.math.SVector;
import com.sunflow.math3d.SMatrix;
import com.sunflow.util.MathUtils;
import com.sunflow.util.Transform;

public class GraphicsMatrix {

	private float tx, ty, tz;

	private float sx, sy, sz;

	private int transformCount;
	private static final int MATRIX_STACK_DEPTH = 32;
	private Transform[] transformStack;

	private SMatrix rotX, rotY, rotZ;
	private float angleX, angleY, angleZ;

	public GraphicsMatrix() {
		tx = ty = tz = 0;

		sx = sy = sz = 1;

		transformStack = new Transform[MATRIX_STACK_DEPTH];

		angleX = angleY = angleZ = 0;

		rotateX(0);
		rotateY(0);
		rotateZ(0);
	}

	public final void pushMatrix() {
		if (transformCount == transformStack.length) {
			throw new RuntimeException("pushMatrix() cannot use push more than " +
					transformStack.length + " times");
		}
		transformStack[transformCount] = getTransform();
		transformCount++;
	}

	public final void popMatrix() {
		if (transformCount == 0) {
			throw new RuntimeException("missing a pushMatrix() " +
					"to go with that popMatrix()");
		}
		transformCount--;
		transform(transformStack[transformCount]);
	}

	public final void resetMatrix() {
		translateTo(0, 0, 0);
		scale(1, 1, 1);
		rotateXTo(0);
		rotateYTo(0);
		rotateZTo(0);
	}

	public final Transform getTransform() { return getTransform(null); }

	public final Transform getTransform(Transform t) { // ignore
		if (t == null) t = new Transform();
		t.tx = tx;
		t.ty = ty;
		t.tz = tz;
		t.sx = sx;
		t.sy = sy;
		t.sz = sz;
		t.rotX = rotX;
		t.rotY = rotY;
		t.rotZ = rotZ;
		t.angleX = angleX;
		t.angleY = angleY;
		t.angleZ = angleZ;
		return t;
	}

	public final void transform(Transform t) {
		tx = t.tx;
		ty = t.ty;
		tz = t.tz;
		sx = t.sx;
		sy = t.sy;
		sz = t.sz;
		rotX = t.rotX;
		rotY = t.rotY;
		rotZ = t.rotZ;
		angleX = t.angleX;
		angleY = t.angleY;
		angleZ = t.angleZ;
	}

	public final void translate(float x, float y, float z) { tx += x; ty += y; tz += z; }

	public final void translateTo(float x, float y, float z) { tx = x; ty = y; tz = z; }

	public final void scale(float x, float y, float z) { sx = x; sy = y; sz = z; };

	public final void rotation(float[] rotation) {
		rotateXTo(rotation[0]);
		rotateYTo(rotation[1]);
		rotateZTo(rotation[2]);
	}

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
		rotX = new SMatrix(3, 3);
		rotX.set(rotArrX);
	}

	public final void rotateY(float angle) {
		angleY += angle;
		float[][] rotArrY = {
				{ MathUtils.instance.cos(angleY), 0, MathUtils.instance.sin(angleY) },
				{ 0, 1, 0 },
				{ -MathUtils.instance.sin(angleY), 0, MathUtils.instance.cos(angleY) }
		};
		rotY = new SMatrix(3, 3);
		rotY.set(rotArrY);
	}

	public final void rotateZ(float angle) {
		angleZ += angle;
		float[][] rotArrZ = {
				{ MathUtils.instance.cos(angleZ), -MathUtils.instance.sin(angleZ), 0 },
				{ MathUtils.instance.sin(angleZ), MathUtils.instance.cos(angleZ), 0 },
				{ 0, 0, 1 }
		};
		rotZ = new SMatrix(3, 3);
		rotZ.set(rotArrZ);
	}

	public final SVector apply(SVector pos) { return apply(pos.x, pos.y, pos.z); }

	public final SVector apply(float x, float y, float z) { return rotated(scaled(translated(x, y, z))); }
//	public final SVector apply(float x, float y, float z) { return scaled(translated(rotated(x, y, z))); }

	public final SVector translated(SVector pos) { return translated(pos.x, pos.y, pos.z); }

	public final SVector translated(float x, float y, float z) { return new SVector(x + tx, y + ty, z + tz); }

	public final SVector scaled(SVector pos) { return scaled(pos.x, pos.y, pos.z); }

	public final SVector scaled(float x, float y, float z) { return new SVector(x * sx, y * sy, z * sz); }

	public final SVector rotated(float x, float y, float z) { return rotated(new SVector(x, y, z)); }

	public final SVector rotated(SVector pos) {
		SVector rotated = matmul(getRotationMatrixX(), pos);
		rotated = matmul(getRotationMatrixY(), rotated);
		rotated = matmul(getRotationMatrixZ(), rotated);
		return rotated;
	}

	private final SVector matmul(SMatrix a, SVector b) {
		SMatrix m = b.get((SMatrix) null);
		SMatrix matmul = a.dot(m);
		return SVector.fromMatrix(matmul);
	}

	public final float getRotationX() { return angleX; }

	public final float getRotationY() { return angleY; }

	public final float getRotationZ() { return angleZ; }

	public final float[] getRotation() { return new float[] { angleX, angleY, angleZ }; }

	public final SMatrix getRotationMatrixX() { return rotX; }

	public final SMatrix getRotationMatrixY() { return rotY; }

	public final SMatrix getRotationMatrixZ() { return rotZ; }

	public final SMatrix[] getRotationMatrix() { return new SMatrix[] { rotX, rotY, rotZ }; }
}
