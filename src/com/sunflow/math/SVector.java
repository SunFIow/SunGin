package com.sunflow.math;

import com.sunflow.game.Game2D;
import com.sunflow.math3d.MatrixF;
import com.sunflow.util.Constants;
import com.sunflow.util.MathUtils;

public class SVector implements Cloneable {

	public float x;
	public float y;
	public float z;

	public SVector() {}

	public SVector(SVector v) {
		this(v.x, v.y, v.z);
	}

	public SVector(float x, float y) {
		this(x, y, 0);
	}

	public SVector(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public SVector set(float x, float y) {
		return set(x, y, 0);
	}

	public SVector set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	/**
	 * Set the x, y (and maybe z) coordinates using a SVector as the source.
	 * 
	 */
	public SVector set(SVector source) {
		return set(source.x, source.y, source.z);
	}

	/**
	 * Set the x, y (and maybe z) coordinates using a float[] array as the source.
	 * 
	 */
	public SVector set(float[] source) {
		x = source.length > 0 ? source[0] : 0;
		y = source.length > 1 ? source[1] : 0;
		z = source.length > 2 ? source[2] : 0;
		return this;
	}

	/**
	 * Set the x, y (and maybe z) coordinates using a MatrixF as the source.
	 * 
	 */
	public SVector set(MatrixF source) {
		x = source.data.length > 0 ? source.data[0][0] : 0;
		y = source.data.length > 1 ? source.data[1][0] : 0;
		z = source.data.length > 2 ? source.data[2][0] : 0;
		return this;
	}

	static public SVector random2D() { return random2D(null, null); }

	static public SVector random2D(Game2D parent) {
		return random2D(null, parent);
	}

	static public SVector random2D(SVector target) {
		return random2D(target, null);
	}

	static public SVector random2D(SVector target, Game2D parent) {
		return (parent == null)
				? fromAngle((float) (Math.random() * Math.PI * 2), target)
				: fromAngle(parent.random(Constants.TAU), target);
	}

	static public SVector random3D() {
		return random3D(null, null);
	}

	static public SVector random3D(Game2D parent) {
		return random3D(null, parent);
	}

	static public SVector random3D(SVector target) {
		return random3D(target, null);
	}

	static public SVector random3D(SVector target, Game2D parent) {
		float angle;
		float vz;
		if (parent == null) {
			angle = (float) (Math.random() * Math.PI * 2);
			vz = (float) (Math.random() * 2 - 1);
		} else {
			angle = parent.random(Constants.TWO_PI);
			vz = parent.random(-1, 1);
		}
		float vx = (float) (Math.sqrt(1 - vz * vz) * Math.cos(angle));
		float vy = (float) (Math.sqrt(1 - vz * vz) * Math.sin(angle));
		if (target == null) {
			target = new SVector(vx, vy, vz);
			// target.normalize(); // Should be unnecessary
		} else {
			target.set(vx, vy, vz);
		}
		return target;
	}

	static public SVector fromAngle(float angle) { return fromAngle(angle, null); }

	static public SVector fromAngle(float angle, SVector target) {
		if (target == null) {
			target = new SVector((float) Math.cos(angle), (float) Math.sin(angle));
		} else {
			target.set((float) Math.cos(angle), (float) Math.sin(angle));
		}
		return target;
	}

	public SVector copy() { return clone(); }

	@Override
	public SVector clone() { return new SVector(this); }

	static public SVector of() {
		return of(0, 0, 0);
	}

	static public SVector of(SVector v) {
		return of(v.x, v.y, v.z);
	}

	static public SVector of(float x, float y, float z) {
		return new SVector(x, y, z);
	}

	public int x() { return (int) x; }

	public int y() { return (int) y; }

	public int z() { return (int) z; }

	public float[] get(float[] target) {
		if (target == null) {
			return new float[] { x, y, z };
		}
		if (target.length > 0) target[0] = x;
		if (target.length > 1) target[1] = y;
		if (target.length > 2) target[2] = z;
		return target;
	}

	public float[] array() { return get((float[]) null); };

	public MatrixF get(MatrixF target) {
		if (target == null) {
			target = new MatrixF(3, 1);
			target.data[0][0] = x;
			target.data[1][0] = y;
			target.data[2][0] = z;
			return target;
		}
		if (target.data.length > 0) target.data[0][0] = x;
		if (target.data.length > 1) target.data[1][0] = y;
		if (target.data.length > 2) target.data[2][0] = z;
		return target;
	}

	public MatrixF matrix() { return get((MatrixF) null); };

	static public MatrixF matrix(SVector v) { return v.matrix(); }

	public float mag() { return (float) Math.sqrt(dot(this, this)); }

	public float magSq() { return dot(this, this); }

	public SVector add(SVector v) { return add(v.x, v.y, v.z); }

	public SVector add(float x, float y) { return add(x, y, 0); }

	public SVector add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	static public SVector add(SVector a, SVector b) {
		return add(a.x, a.y, a.z, b.x, b.y, b.z);
	}

	static public SVector add(float x1, float y1, float z1, float x2, float y2, float z2) {
		return new SVector(
				x1 + x2,
				y1 + y2,
				z1 + z2);
	}

	public SVector sub(SVector b) { return sub(b.x, b.y, b.z); }

	public SVector sub(float x, float y) { return sub(x, y, 0); }

	public SVector sub(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	static public SVector sub(SVector a, SVector b) {
		return sub(a.x, a.y, a.z, b.x, b.y, b.z);
	}

	static public SVector sub(float x1, float y1, float z1, float x2, float y2, float z2) {
		return new SVector(
				x1 - x2,
				y1 - y2,
				z1 - z2);
	}

	public SVector mult(float n) { return mult(n, n, n); }

	public SVector mult(SVector v) { return mult(v.x, v.y, v.z); }

	public SVector mult(float x, float y, float z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}

	public SVector div(float n) { return div(n, n, n); }

	public SVector div(SVector v) { return div(v.x, v.y, v.z); }

	public SVector div(float x, float y, float z) {
		this.x /= x;
		this.y /= y;
		this.z /= z;
		return this;
	}

	public float dist(SVector v) { return dist(this, v); }

	static public float dist(SVector v1, SVector v2) {
		return SVector.sub(v2, v1).mag();
	}

	public float dot(SVector b) { return dot(this, b); }

	static public float dot(SVector a, SVector b) {
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}

	public float dot(float x, float y, float z) {
		return this.x * x + this.y * y + this.z * z;
	}

	public SVector cross(SVector b) {
		return cross(this, b);
	}

	static public SVector cross(SVector a, SVector b) {
		return new SVector(
				(a.y * b.z - a.z * b.y),
				(a.z * b.x - a.x * b.z),
				(a.x * b.y - a.y * b.x));
	}

	public SVector normalize() {
		float m = mag();
		if (m != 0 && m != 1) div(m);
		return this;
	}

	public SVector normalized() { return clone().normalize(); }

	public SVector limit(float max) {
		if (magSq() > max * max) {
			normalize();
			mult(max);
		}
		return this;
	}

	public SVector setMag(float len) {
		normalize();
		mult(len);
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

	/**
	 * Rotate the vector by an angle (2D only)
	 * 
	 * @param theta
	 *            the angle of rotation
	 */
	public SVector rotate(float theta) {
		float temp = x;
		// Might need to check for rounding errors like with angleBetween function?
		x = x * MathUtils.instance.cos(theta) - y * MathUtils.instance.sin(theta);
		y = temp * MathUtils.instance.sin(theta) + y * MathUtils.instance.cos(theta);
		return this;
	}

	/**
	 * Linear interpolate the vector to another vector
	 * 
	 * @param v
	 *            the vector to lerp to
	 * @param amt
	 *            The amount of interpolation; some value between 0.0 (old vector) and 1.0 (new vector). 0.1 is very near the old vector; 0.5 is halfway in between.
	 */
	public SVector lerp(SVector v, float amt) {
		x = MathUtils.instance.lerp(x, v.x, amt);
		y = MathUtils.instance.lerp(y, v.y, amt);
		z = MathUtils.instance.lerp(z, v.z, amt);
		return this;
	}

	/**
	 * Linear interpolate between two vectors (returns a new PVector object)
	 * 
	 * @param v1
	 *            the vector to start from
	 * @param v2
	 *            the vector to lerp to
	 */
	public static SVector lerp(SVector v1, SVector v2, float amt) {
		SVector v = v1.copy();
		v.lerp(v2, amt);
		return v;
	}

	/**
	 * Linear interpolate the vector to x,y,z values
	 * 
	 * @param x
	 *            the x component to lerp to
	 * @param y
	 *            the y component to lerp to
	 * @param z
	 *            the z component to lerp to
	 */
	public SVector lerp(float x, float y, float z, float amt) {
		this.x = MathUtils.instance.lerp(this.x, x, amt);
		this.y = MathUtils.instance.lerp(this.y, y, amt);
		this.z = MathUtils.instance.lerp(this.z, z, amt);
		return this;
	}

	/**
	 * Calculate and return the angle between two vectors
	 * 
	 * @param v1
	 *            the x, y, and z components of a PVector
	 * @param v2
	 *            the x, y, and z components of a PVector
	 */
	static public float angleBetween(SVector v1, SVector v2) {
		// We get NaN if we pass in a zero vector which can cause problems
		// Zero seems like a reasonable angle between a (0,0,0) vector and something else
		if (v1.x == 0 && v1.y == 0 && v1.z == 0) return 0.0f;
		if (v2.x == 0 && v2.y == 0 && v2.z == 0) return 0.0f;

		double dot = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
		double v1mag = Math.sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z);
		double v2mag = Math.sqrt(v2.x * v2.x + v2.y * v2.y + v2.z * v2.z);
		// This should be a number between -1 and 1, since it's "normalized"
		double amt = dot / (v1mag * v2mag);
		// But if it's not due to rounding error, then we need to fix it
		// http://code.google.com/p/processing/issues/detail?id=340
		// Otherwise if outside the range, acos() will return NaN
		// http://www.cppreference.com/wiki/c/math/acos
		if (amt <= -1) return Constants.PI;
		else if (amt >= 1) return 0;
		return (float) Math.acos(amt);
	}

	@Override
	public String toString() {
		return "[ " + x + ", " + y + ", " + z + " ]";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SVector)) return false;
		final SVector v = (SVector) obj;
		return x == v.x && y == v.y && z == v.z;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + Float.floatToIntBits(x);
		result = 31 * result + Float.floatToIntBits(y);
		result = 31 * result + Float.floatToIntBits(z);
		return result;
	}

	static public SVector normal(SVector a, SVector b, SVector c) {
		return cross(SVector.sub(b, a), SVector.sub(c, a)).normalized();
	}

	public SVector neg() {
		return clone().mult(-1);
	}

	static public SVector avg(SVector... ins) {
		SVector avg = new SVector();
		for (int i = 0; i < ins.length; i++) {
			avg.add(ins[i]);
		}
		avg.div(ins.length);
		return avg;
	}

	static public SVector fromMatrix(MatrixF m) {
		return new SVector(
				m.data.length > 0 ? m.data[0][0] : 0,
				m.data.length > 1 ? m.data[1][0] : 0,
				m.data.length > 2 ? m.data[2][0] : 0);
	}

	static public SVector fromArray(float[] array) {
		return new SVector(
				array.length > 0 ? array[0] : 0,
				array.length > 1 ? array[1] : 0,
				array.length > 2 ? array[2] : 0);
	}
}
