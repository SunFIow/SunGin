package com.sunflow.math;

import com.sunflow.math3d.SMatrix;
import com.sunflow.util.MathUtils;

public class SMatrix2D {

	public SVector pos;
	public SMatrix rot;
	public float angle;

	public SMatrix2D() {
		pos = new SVector();
		angle = 0;
//		rotate(0);
	}

	public final void translate(float x, float y) { pos.add(rotated(new SVector(x, y))); }

	public final void rotateTo(float angle) { rotate(angle - this.angle); }

	public final void rotate(float angle) {
		this.angle += angle;
		float[][] rotArr = {
				{ MathUtils.instance.cos(this.angle), -MathUtils.instance.sin(this.angle) },
				{ MathUtils.instance.sin(this.angle), MathUtils.instance.cos(this.angle) }
		};
		rot = new SMatrix(2, 2);
		rot.set(rotArr);
		pos = rotated(pos);
	}

	public final SMatrix getRotationMatrix() { return rot; }

	public final SVector rotated(SVector pos) {
		SVector rotated = matmul(getRotationMatrix(), pos);
		return rotated;
	}

	private final SVector matmul(SMatrix a, SVector b) {
		SMatrix m = b.get((SMatrix) null);
		SMatrix matmul = a.dot(m);
		return SVector.fromMatrix(matmul);
	}
}
