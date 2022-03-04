package com.sunflow.math;

import java.awt.Point;

import com.sunflow.game.GameBase;
import com.sunflow.math3d.SMatrix;
import com.sunflow.util.MathUtils;
import com.sunflow.util.SConstants;

public class SVector implements Cloneable {

	public float x = 0.0f;
	public float y = 0.0f;
	public float z = 0.0f;

	// for 4x4 matrix multiplication
	public float w = 1.0f;

	public SVector() {}

	public SVector(SVector v) { this(v.x, v.y, v.z, v.w); }

	public SVector(double x, double y) { this((float) x, (float) y); }

	public SVector(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public SVector(double x, double y, double z) { this((float) x, (float) y, (float) z); }

	public SVector(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public SVector(double x, double y, double z, double w) { this((float) x, (float) y, (float) z, (float) w); }

	public SVector(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public SVector set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public SVector set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public SVector set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}

	/**
	 * Set the x, y (and maybe z) coordinates using a SVector as the source.
	 * 
	 */
	public SVector set(SVector source) {
		return set(source.x, source.y, source.z, source.w);
	}

	/**
	 * Set the x, y (and maybe z) coordinates using a float[] array as the source.
	 * 
	 */
	public SVector set(float[] source) {
		if (source.length > 0) x = source[0];
		if (source.length > 1) y = source[1];
		if (source.length > 2) z = source[2];
		if (source.length > 3) w = source[3];
		return this;
	}

	/**
	 * Set the x, y (and maybe z) coordinates using a MatrixF as the source.
	 * 
	 */
	public SVector set(SMatrix source) {
		if (source.data.length > 0) x = source.data[0][0];
		if (source.data.length > 1) y = source.data[1][0];
		if (source.data.length > 2) z = source.data[2][0];
		if (source.data.length > 3) w = source.data[3][0];
		return this;
	}

	public SVector set(Point source) {
		return set(source.x, source.y);
	}

	public static SVector random2D() { return random2D(null, null); }

	public static SVector random2D(GameBase parent) {
		return random2D(null, parent);
	}

	public static SVector random2D(SVector target) {
		return random2D(target, null);
	}

	public static SVector random2D(SVector target, GameBase parent) {
		return (parent == null)
				? fromAngle(MathUtils.instance.random() * SConstants.TWO_PI, target)
				: fromAngle(parent.random(SConstants.TAU), target);
	}

	public static SVector random3D() {
		return random3D(null, null);
	}

	public static SVector random3D(GameBase parent) {
		return random3D(null, parent);
	}

	public static SVector random3D(SVector target) {
		return random3D(target, null);
	}

	public static SVector random3D(SVector target, GameBase parent) {
		float angle;
		float vz;
		if (parent == null) {
			angle = MathUtils.instance.random() * SConstants.TWO_PI;
			vz = MathUtils.instance.random() * 2 - 1;
		} else {
			angle = parent.random(SConstants.TWO_PI);
			vz = parent.random(-1, 1);
		}
		float vx = MathUtils.instance.sqrt(1 - vz * vz) * MathUtils.instance.cos(angle);
		float vy = MathUtils.instance.sqrt(1 - vz * vz) * MathUtils.instance.sin(angle);
		if (target == null) {
			target = new SVector(vx, vy, vz);
			// target.normalize(); // Should be unnecessary
		} else {
			target.set(vx, vy, vz);
		}
		return target;
	}

	public static SVector fromAngle(float angle) { return fromAngle(angle, null); }

	public static SVector fromAngle(float angle, SVector target) {
		if (target == null) {
			target = new SVector(MathUtils.instance.cos(angle), MathUtils.instance.sin(angle));
		} else {
			target.set(MathUtils.instance.cos(angle), MathUtils.instance.sin(angle));
		}
		return target;
	}

	public static SVector fromAngle(float alpha, float beta) { return fromAngle(alpha, beta, null); }

	public static SVector fromAngle(float alpha, float beta, SVector target) {
		if (target == null) {
			target = new SVector(
//					MathUtils.instance.cos(beta),
//					MathUtils.instance.sin(beta),
//					alpha);

					MathUtils.instance.cos(alpha),
					MathUtils.instance.sin(alpha),
					beta);

//					MathUtils.instance.cos(alpha) * MathUtils.instance.cos(beta),
//					MathUtils.instance.sin(alpha) * MathUtils.instance.cos(beta),
//					MathUtils.instance.sin(beta));
		} else {
			target.set(
					MathUtils.instance.cos(alpha) * MathUtils.instance.cos(beta),
					MathUtils.instance.sin(alpha) * MathUtils.instance.cos(beta),
					MathUtils.instance.sin(beta));
		}
		return target;
	}

	public static SVector fromAngle2(float alpha, float beta, SVector target) {
		if (target == null) {
			target = new SVector(
					MathUtils.instance.cos(alpha) * MathUtils.instance.cos(beta),
					MathUtils.instance.sin(alpha) * MathUtils.instance.cos(beta),
					MathUtils.instance.sin(beta));
//			target = new SVector(
//					MathUtils.instance.cos(alpha) * MathUtils.instance.cos(beta),
//					MathUtils.instance.sin(beta),
//					MathUtils.instance.sin(alpha) * MathUtils.instance.cos(beta));
		} else {
			target.set(
					MathUtils.instance.cos(alpha) * MathUtils.instance.cos(beta),
					MathUtils.instance.sin(alpha) * MathUtils.instance.cos(beta),
					MathUtils.instance.sin(beta));
		}
		return target;
	}

	public SVector rotateX(float angle) {
		y = y * MathUtils.instance.cos(angle) - z * MathUtils.instance.sin(angle);
		z = y * MathUtils.instance.sin(angle) + z * MathUtils.instance.cos(angle);
		return this;
	}

	public SVector rotateY(float angle) {
		x = x * MathUtils.instance.cos(angle) + z * MathUtils.instance.sin(angle);
		z = -x * MathUtils.instance.sin(angle) + z * MathUtils.instance.cos(angle);
		return this;
	}

	public SVector rotateZ(float angle) {
		x = x * MathUtils.instance.cos(angle) - y * MathUtils.instance.sin(angle);
		y = x * MathUtils.instance.sin(angle) + y * MathUtils.instance.cos(angle);
		return this;
	}

	public SVector copy() { return clone(); }

	@Override
	public SVector clone() { return new SVector(this); }

	public int x() { return (int) x; }

	public int y() { return (int) y; }

	public int z() { return (int) z; }

	public int w() { return (int) w; }

	public int getX() { return x(); }

	public int getY() { return y(); }

	public int getZ() { return z(); }

	public int getW() { return w(); }

	public float[] array() { return get((float[]) null); };

	public float[] get(float[] target) {
		if (target == null) {
			return new float[] { x, y, z };
		}
		if (target.length > 0) target[0] = x;
		if (target.length > 1) target[1] = y;
		if (target.length > 2) target[2] = z;
		if (target.length > 3) target[3] = w;
		return target;
	}

	public static SMatrix matrix(SVector v) { return v.matrix(); }

	public SMatrix matrix() { return get((SMatrix) null); };

	public SMatrix get(SMatrix target) {
		if (target == null) {
			target = new SMatrix(3, 1);
			target.data[0][0] = x;
			target.data[1][0] = y;
			target.data[2][0] = z;
			return target;
		}
		if (target.data.length > 0) target.data[0][0] = x;
		if (target.data.length > 1) target.data[1][0] = y;
		if (target.data.length > 2) target.data[2][0] = z;
		if (target.data.length > 3) target.data[3][0] = w;
		return target;
	}

	public Point get(Point target) {
		if (target == null) return new Point(x(), y());
		target.x = x();
		target.y = y();
		return target;
	}

	public float length() { return mag(); }

	public float mag() { return MathUtils.instance.sqrt(magSq()); }

	public float magSq() { return dot(this, this); }

	public static SVector add(SVector a, SVector b) { return a.clone().add(b); }

	public SVector added(SVector v) { return clone().add(v); }

	public SVector added(float x, float y) { return clone().add(x, y); }

	public SVector added(float x, float y, float z) { return clone().add(x, y, z); }

	public SVector added(float x, float y, float z, float w) { return clone().add(x, y, z, w); }

	public SVector add(SVector v) { return add(v.x, v.y, v.z, v.w); }

	public SVector add(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public SVector add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public SVector add(float x, float y, float z, float w) {
		this.x += x;
		this.y += y;
		this.z += z;
		this.w += w;
		return this;
	}

	public static SVector sub(SVector a, SVector b) { return a.clone().sub(b); }

	public SVector subtracted(SVector v) { return clone().sub(v); }

	public SVector subtracted(float x, float y) { return clone().sub(x, y); }

	public SVector subtracted(float x, float y, float z) { return clone().sub(x, y, z); }

	public SVector subtracted(float x, float y, float z, float w) { return clone().sub(x, y, z, w); }

	public SVector sub(SVector v) { return sub(v.x, v.y, v.z, v.w); }

	public SVector sub(float x, float y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	public SVector sub(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	public SVector sub(float x, float y, float z, float w) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		this.w -= w;
		return this;
	}

	public SVector multiplied(SVector v) { return clone().mult(v); }

	public SVector multiplied(float n) { return clone().mult(n, n, n, n); }

	public SVector multiplied(float x, float y) { return clone().mult(x, y); }

	public SVector multiplied(float x, float y, float z) { return clone().mult(x, y, z); }

	public SVector multiplied(float x, float y, float z, float w) { return clone().mult(x, y, z, w); }

	public static SVector mult(SVector v, float n) { return v.clone().mult(n); }

	public static SVector mult(SVector a, SVector b) { return a.clone().mult(b); }

	public SVector mult(SVector v) { return mult(v.x, v.y, v.z, v.w); }

	public SVector mult(float n) { return mult(n, n, n, n); }

	public SVector mult(float x, float y) {
		this.x *= x;
		this.y *= y;
		return this;
	}

	public SVector mult(float x, float y, float z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}

	public SVector mult(float x, float y, float z, float w) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		this.w *= w;
		return this;
	}

	public SVector divided(float n) { return clone().div(n); }

	public SVector divided(SVector v) { return clone().div(v); }

	public SVector divided(float x, float y) { return clone().div(x, y); }

	public SVector divided(float x, float y, float z) { return clone().div(x, y, z); }

	public SVector divided(float x, float y, float z, float w) { return clone().div(x, y, z, w); }

	public static SVector div(SVector v, float n) { return v.clone().div(n, n, n, n); }

	public SVector div(float n) { return div(n, n, n, n); }

	public static SVector div(SVector a, SVector b) { return a.clone().div(b); }

	public SVector div(SVector v) { return div(v.x, v.y, v.z, v.w); }

	public SVector div(float x, float y) {
		if (x != 0) this.x /= x;
		if (y != 0) this.y /= y;
		return this;
	}

	public SVector div(float x, float y, float z) {
		if (x != 0) this.x /= x;
		if (y != 0) this.y /= y;
		if (z != 0) this.z /= z;
		return this;
	}

	public SVector div(float x, float y, float z, float w) {
		if (x != 0) this.x /= x;
		if (y != 0) this.y /= y;
		if (z != 0) this.z /= z;
		if (w != 0) this.w /= w;
		return this;
	}

	public float dist(SVector v) { return dist(this, v); }

	public static float dist(SVector v1, SVector v2) {
		return SVector.sub(v2, v1).mag();
	}

	public float dot(SVector b) { return dot(this, b); }

	public static float dot(SVector a, SVector b) {
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}

	public float dot(float x, float y, float z) {
		return this.x * x + this.y * y + this.z * z;
	}

	public SVector cross(SVector b) {
		return cross(this, b);
	}

	public float crossX(SVector b) {
		return crossX(this, b);
	}

	public float crossY(SVector b) {
		return crossY(this, b);
	}

	public float crossZ(SVector b) {
		return crossZ(this, b);
	}

	public static SVector cross(SVector a, SVector b) {
		return new SVector(
				(a.y * b.z - a.z * b.y),
				(a.z * b.x - a.x * b.z),
				(a.x * b.y - a.y * b.x));
	}

	public static float crossX(SVector a, SVector b) {
		return a.y * b.z - a.z * b.y;
	}

	public static float crossY(SVector a, SVector b) {
		return a.z * b.x - a.x * b.z;
	}

	public static float crossZ(SVector a, SVector b) {
		return a.x * b.y - a.y * b.x;
	}

	public static SVector proj(SVector a, SVector b) {
		float scalar = dot(a, b) / dot(b, b); // TODO We dont need '/ dot(a, a)' if the a is of length 1
		SVector result = new SVector(
				b.x * scalar,
				b.y * scalar,
				b.z * scalar);
		return result;
	}

	public static SVector reflect(SVector v, SVector normal) {
		float scalar = 2 * dot(v, normal) / dot(normal, normal); // TODO We dont need '/ dot(normal, normal)' if the normal is of length 1
		SVector result = new SVector(
				v.x - normal.x * scalar,
				v.y - normal.y * scalar,
				v.z - normal.z * scalar);
		return result;
	}

	public static SVector proj_unsafe(SVector a, SVector b) {
		float scalar = dot(a, b);
		SVector result = new SVector(
				b.x * scalar,
				b.y * scalar,
				b.z * scalar);
		return result;
	}

	public static SVector reflect_unsafe(SVector v, SVector normal) {
		float scalar = 2 * dot(v, normal);
		SVector result = new SVector(
				v.x - normal.x * scalar,
				v.y - normal.y * scalar,
				v.z - normal.z * scalar);
		return result;
	}

	public SVector normalize() {
		float m_sq = magSq();
		if (m_sq != 0.0f && m_sq != 1.0f) {
			div(MathUtils.instance.sqrt(m_sq));
		}
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
	public static float angleBetween(SVector v1, SVector v2) {
		// We get NaN if we pass in a zero vector which can cause problems
		// Zero seems like a reasonable angle between a (0,0,0) vector and something else
		if (v1.x == 0 && v1.y == 0 && v1.z == 0) return 0.0f;
		if (v2.x == 0 && v2.y == 0 && v2.z == 0) return 0.0f;

		double dot = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
		double v1mag = MathUtils.instance.sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z);
		double v2mag = MathUtils.instance.sqrt(v2.x * v2.x + v2.y * v2.y + v2.z * v2.z);
		// This should be a number between -1 and 1, since it's "normalized"
		double amt = dot / (v1mag * v2mag);
		// But if it's not due to rounding error, then we need to fix it
		// http://code.google.com/p/processing/issues/detail?id=340
		// Otherwise if outside the range, acos() will return NaN
		// http://www.cppreference.com/wiki/c/math/acos
		if (amt <= -1) return SConstants.PI;
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

	public static SVector normalBig(SVector a, SVector b, SVector c) {
		return SVector.cross(SVector.sub(b, a), SVector.sub(c, a));
	}

	public static SVector normal(SVector a, SVector b, SVector c) {
		return SVector.cross(SVector.sub(b, a), SVector.sub(c, a)).normalize();
	}

	public SVector neg() { return set(-x, -y, -z, -w); }

	public SVector negated() { return clone().neg(); }

	public static SVector neg(SVector v) { return v.negated(); }

	public static SVector avg(SVector... ins) {
		SVector avg = new SVector();
		for (int i = 0; i < ins.length; i++) {
			avg.add(ins[i]);
		}
		avg.div(ins.length);
		return avg;
	}

	public static SVector fromMatrix(SMatrix m) {
		return new SVector(
				m.data.length > 0 ? m.data[0][0] : 0,
				m.data.length > 1 ? m.data[1][0] : 0,
				m.data.length > 2 ? m.data[2][0] : 0);
	}

	public static SVector fromArray(float[] array) {
		return new SVector(
				array.length > 0 ? array[0] : 0,
				array.length > 1 ? array[1] : 0,
				array.length > 2 ? array[2] : 0);
	}

	public static SVector floor(SVector v) { return new SVector(v.x(), v.y()); }

	public SVector floor() { x = x(); y = y(); z = z(); w = w(); return this; }

	public SVector floored() { return clone().floor(); }

	public static SVector min(SVector a, SVector b) { return min(a.x, a.y, b.x, b.y); }

	public static SVector min(SVector a, float bx, float by) { return min(a.x, a.y, bx, by); }

	public static SVector min(float ax, float ay, float bx, float by) {
		return new SVector(
				MathUtils.instance.min(ax, bx),
				MathUtils.instance.min(ay, by));
	}

	public SVector min(SVector b) { return min(b.x, b.y); }

	public SVector min(float bx, float by) { x = MathUtils.instance.min(x, bx); y = MathUtils.instance.min(y, by); return this; }

	public SVector minimized(SVector b) { return clone().min(b); }

	public SVector minimized(float bx, float by) { return clone().min(bx, by); }

	public static SVector max(SVector a, SVector b) { return max(a.x, a.y, b.x, b.y); }

	public static SVector max(SVector a, float bx, float by) { return max(a.x, a.y, bx, by); }

	public static SVector max(float ax, float ay, float bx, float by) {
		return new SVector(
				MathUtils.instance.max(ax, bx),
				MathUtils.instance.max(ay, by));
	}

	public SVector max(SVector b) { return max(b.x, b.y); }

	public SVector max(float bx, float by) { x = MathUtils.instance.max(x, bx); y = MathUtils.instance.max(y, by); return this; }

	public SVector maximized(SVector b) { return clone().max(b); }

	public SVector maximized(float bx, float by) { return clone().max(bx, by); }

	public SVector abs() {
		x = MathUtils.instance.abs(x);
		y = MathUtils.instance.abs(y);
		z = MathUtils.instance.abs(z);
		return this;
	}

	public float max() {
		return MathUtils.instance.max(MathUtils.instance.max(x, y), z);
	}

}
