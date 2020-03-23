package com.sunflow.math;

import com.sunflow.math3d.MatrixF;
import com.sunflow.math3d.Vertex3F;
import com.sunflow.util.MathUtils;

public class Vertex2F implements Cloneable {

	public float x;
	public float y;

	public Vertex2F() {
		this(0, 0);
	}

	public Vertex2F(Vertex2F v) {
		this(v.x, v.y);
	}

	public Vertex2F(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public static Vertex2F of() {
		return of(0, 0);
	}

	public static Vertex2F of(Vertex2F v) {
		return of(v.x, v.y);
	}

	public static Vertex2F of(float x, float y) {
		return new Vertex2F(x, y);
	}

	@Override
	public Vertex2F clone() {
		try {
			return (Vertex2F) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int x() {
		return (int) x;
	}

	public int y() {
		return (int) y;
	}

	public Vertex2F set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Vertex2F set(Vertex2F v) {
		return set(v.x, v.y);
	}

	public Vertex2F add(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public Vertex2F add(Vertex2F b) {
		return add(b.x, b.y);
	}

	public static Vertex2F add(Vertex2F a, Vertex2F b) {
		return add(a.x, a.y, b.x, b.y);
	}

	public static Vertex2F add(float x1, float y1, float x2, float y2) {
		return new Vertex2F(x1 + x2, y1 + y2);
	}

	public Vertex2F sub(float x, float y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	public Vertex2F sub(Vertex2F b) {
		return sub(b.x, b.y);
	}

	public static Vertex2F sub(Vertex2F a, Vertex2F b) {
		return sub(a.x, a.y, b.x, b.y);
	}

	public static Vertex2F sub(float x1, float y1, float x2, float y2) {
		return new Vertex2F(x1 - x2, y1 - y2);
	}

	public Vertex2F mult(float m) {
		return mult(m, m);
	}

	public Vertex2F mult(float w, float h) {
		x *= w;
		y *= h;
		return this;
	}

	public Vertex2F mult(Vertex2F v) {
		return mult(v.x, v.y);
	}

	public Vertex2F div(float d) {
		return div(d, d);
	}

	public Vertex2F div(float w, float h) {
		x /= w;
		y /= h;
		return this;
	}

	public Vertex2F div(Vertex2F v) {
		return div(v.x, v.y);
	}

	public float mag() {
		return (float) Math.sqrt(dot(this, this));
	}

	public float magSq() {
		return dot(this, this);
	}

	public Vertex2F neg() {
		Vertex2F ret = this.clone();
		ret.mult(-1);
		return ret;
	}

	public static float dot(Vertex2F a, Vertex2F b) {
		return (a.x * b.x + a.y * b.y);
	}

	public static float dist(Vertex2F v1, Vertex2F v2) {
		return Vertex2F.sub(v2, v1).mag();
	}

	public Vertex2F normalize() {
		float m = mag();
		if (m != 0 && m != 1) {
			div(m);
		}
		return this;
	}

	public Vertex2F limit(float max) {
		if (magSq() > max * max) {
			normalize();
			mult(max);
		}
		return this;
	}

	public static Vertex2F random2D() { return random2D(null); }

	public static Vertex2F random2D(Vertex2F target) {
		return fromAngle((float) (Math.random() * Math.PI * 2), target);
	}

	public static Vertex2F fromAngle(float angle) { return fromAngle(angle, null); }

	public static Vertex2F fromAngle(float angle, Vertex2F target) {
		if (target == null) {
			target = new Vertex2F((float) Math.cos(angle), (float) Math.sin(angle));
		} else {
			target.set((float) Math.cos(angle), (float) Math.sin(angle));
		}
		return target;
	}

	/**
	 * Rotate the vector by an angle (2D only)
	 * 
	 * @param theta
	 *            the angle of rotation
	 */
	public Vertex2F rotate(float theta) {
		float temp = x;
		// Might need to check for rounding errors like with angleBetween function?
		x = x * MathUtils.instance.cos(theta) - y * MathUtils.instance.sin(theta);
		y = temp * MathUtils.instance.sin(theta) + y * MathUtils.instance.cos(theta);
		return this;
	}

	/**
	 * Calculate the angle of rotation for this vector *
	 * 
	 * @return the angle of rotation
	 */

	public float heading() {
		float angle = (float) Math.atan2(y, x);
		return angle;
	}

	public MatrixF toMatrix() {
		float[][] m = new float[2][1];
		m[0][0] = x;
		m[1][0] = y;
		return new MatrixF(2, 1).set(m);
	}

	static public MatrixF toMatrix(Vertex3F v) {
		return v.toMatrix();
	}

	static public Vertex2F fromMatrix(MatrixF m) {
		return new Vertex2F(
				m.data.length > 0 ? m.data[0][0] : 0,
				m.data.length > 1 ? m.data[1][0] : 0);
	}

	public float[] array() {
		float[] array = new float[3];
		array[0] = x;
		array[1] = y;
//		array[2] = z;
		return array;
	}

	static public Vertex2F fromArray(float[] array) {
		return new Vertex2F(
				array.length > 0 ? array[0] : 0,
				array.length > 1 ? array[1] : 0);
//				array.length > 2 ? array[2] : 0);
	}

	@Override
	public String toString() {
		return "Vertex2F[x=" + x + ",y=" + y + "]";
	}
}
