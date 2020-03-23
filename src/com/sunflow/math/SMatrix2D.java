package com.sunflow.math;

import com.sunflow.math3d.MatrixF;
import com.sunflow.util.MathUtils;

public class SMatrix2D {

	public Vertex2F pos;
	public MatrixF rot;
	public float angle;

	public SMatrix2D() {
		pos = new Vertex2F();
		angle = 0;
//		rotate(0);
	}

	public final void translate(float x, float y) { pos.add(rotated(new Vertex2F(x, y))); }

	public final void rotateTo(float angle) { rotate(angle - this.angle); }

	public final void rotate(float angle) {
		this.angle += angle;
		float[][] rotArr = {
				{ MathUtils.instance.cos(this.angle), -MathUtils.instance.sin(this.angle) },
				{ MathUtils.instance.sin(this.angle), MathUtils.instance.cos(this.angle) }
		};
		rot = new MatrixF(2, 2);
		rot.set(rotArr);
		pos = rotated(pos);
	}

	public final MatrixF getRotationMatrix() { return rot; }

	public final Vertex2F rotated(Vertex2F pos) {
		Vertex2F rotated = matmul(getRotationMatrix(), pos);
		return rotated;
	}

	private final Vertex2F matmul(MatrixF a, Vertex2F b) {
		MatrixF m = b.toMatrix();
		MatrixF matmul = a.dot(m);
		return Vertex2F.fromMatrix(matmul);
	}
}
