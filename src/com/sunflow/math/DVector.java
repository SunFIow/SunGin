package com.sunflow.math;

import java.awt.Point;

import com.sunflow.game.GameBase;
import com.sunflow.math3d.DMatrix;
import com.sunflow.util.SConstants;
import com.sunflow.util.MathUtils;

public class DVector implements Cloneable {

	public double x = 0.0f;
	public double y = 0.0f;
	public double z = 0.0f;

	// for 4x4 matrix multiplication
	public double w = 1.0f;

	public DVector() {}

	public DVector(DVector v) { this(v.x, v.y, v.z, v.w); }

	public DVector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public DVector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public DVector(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public DVector set(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public DVector set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public DVector set(double x, double y, double z, double w) {
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
	public DVector set(DVector source) {
		return set(source.x, source.y, source.z, source.w);
	}

	/**
	 * Set the x, y (and maybe z) coordinates using a double[] array as the source.
	 * 
	 */
	public DVector set(double[] source) {
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
	public DVector set(DMatrix source) {
		if (source.data.length > 0) x = source.data[0][0];
		if (source.data.length > 1) y = source.data[1][0];
		if (source.data.length > 2) z = source.data[2][0];
		if (source.data.length > 3) w = source.data[3][0];
		return this;
	}

	public DVector set(Point source) {
		return set(source.x, source.y);
	}

	public static DVector random2D() { return random2D(null, null); }

	public static DVector random2D(GameBase parent) {
		return random2D(null, parent);
	}

	public static DVector random2D(DVector target) {
		return random2D(target, null);
	}

	public static DVector random2D(DVector target, GameBase parent) {
		return (parent == null)
				? fromAngle(MathUtils.instance.random() * SConstants.TWO_PI, target)
				: fromAngle(parent.random(SConstants.TAU), target);
	}

	public static DVector random3D() {
		return random3D(null, null);
	}

	public static DVector random3D(GameBase parent) {
		return random3D(null, parent);
	}

	public static DVector random3D(DVector target) {
		return random3D(target, null);
	}

	public static DVector random3D(DVector target, GameBase parent) {
		double angle;
		double vz;
		if (parent == null) {
			angle = MathUtils.instance.random() * SConstants.TWO_PI;
			vz = MathUtils.instance.random() * 2 - 1;
		} else {
			angle = parent.random(SConstants.TWO_PI);
			vz = parent.random(-1, 1);
		}
		double vx = MathUtils.instance.sqrt(1 - vz * vz) * MathUtils.instance.cos(angle);
		double vy = MathUtils.instance.sqrt(1 - vz * vz) * MathUtils.instance.sin(angle);
		if (target == null) {
			target = new DVector(vx, vy, vz);
			// target.normalize(); // Should be unnecessary
		} else {
			target.set(vx, vy, vz);
		}
		return target;
	}

	public static DVector fromAngle(double angle) { return fromAngle(angle, null); }

	public static DVector fromAngle(double angle, DVector target) {
		if (target == null) {
			target = new DVector(MathUtils.instance.cos(angle), MathUtils.instance.sin(angle));
		} else {
			target.set(MathUtils.instance.cos(angle), MathUtils.instance.sin(angle));
		}
		return target;
	}

	public DVector copy() { return clone(); }

	@Override
	public DVector clone() { return new DVector(this); }

	public int x() { return (int) x; }

	public int y() { return (int) y; }

	public int z() { return (int) z; }

	public int w() { return (int) w; }

	public double[] array() { return get((double[]) null); };

	public double[] get(double[] target) {
		if (target == null) {
			return new double[] { x, y, z };
		}
		if (target.length > 0) target[0] = x;
		if (target.length > 1) target[1] = y;
		if (target.length > 2) target[2] = z;
		if (target.length > 3) target[3] = w;
		return target;
	}

	public static DMatrix matrix(DVector v) { return v.matrix(); }

	public DMatrix matrix() { return get((DMatrix) null); };

	public DMatrix get(DMatrix target) {
		if (target == null) {
			target = new DMatrix(3, 1);
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

	public double length() { return mag(); }

	public double mag() { return MathUtils.instance.sqrt(dot(this, this)); }

	public double magSq() { return dot(this, this); }

	public static DVector add(DVector a, DVector b) { return a.clone().add(b); }

	public DVector added(DVector v) { return clone().add(v); }

	public DVector added(double x, double y) { return clone().add(x, y); }

	public DVector added(double x, double y, double z) { return clone().add(x, y, z); }

	public DVector added(double x, double y, double z, double w) { return clone().add(x, y, z, w); }

	public DVector add(DVector v) { return add(v.x, v.y, v.z, v.w); }

	public DVector add(double x, double y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public DVector add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public DVector add(double x, double y, double z, double w) {
		this.x += x;
		this.y += y;
		this.z += z;
		this.w += w;
		return this;
	}

	public static DVector sub(DVector a, DVector b) { return a.clone().sub(b); }

	public DVector subtracted(DVector v) { return clone().sub(v); }

	public DVector subtracted(double x, double y) { return clone().sub(x, y); }

	public DVector subtracted(double x, double y, double z) { return clone().sub(x, y, z); }

	public DVector subtracted(double x, double y, double z, double w) { return clone().sub(x, y, z, w); }

	public DVector sub(DVector v) { return sub(v.x, v.y, v.z, v.w); }

	public DVector sub(double x, double y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	public DVector sub(double x, double y, double z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	public DVector sub(double x, double y, double z, double w) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		this.w -= w;
		return this;
	}

	public DVector multipied(DVector v) { return clone().mult(v); }

	public DVector multipied(double n) { return clone().mult(n, n, n, n); }

	public DVector multipied(double x, double y) { return clone().mult(x, y); }

	public DVector multipied(double x, double y, double z) { return clone().mult(x, y, z); }

	public DVector multipied(double x, double y, double z, double w) { return clone().mult(x, y, z, w); }

	public static DVector mult(DVector v, double n) { return v.clone().mult(n); }

	public static DVector mult(DVector a, DVector b) { return a.clone().mult(b); }

	public DVector mult(DVector v) { return mult(v.x, v.y, v.z, v.w); }

	public DVector mult(double n) { return mult(n, n, n, n); }

	public DVector mult(double x, double y) {
		this.x *= x;
		this.y *= y;
		return this;
	}

	public DVector mult(double x, double y, double z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}

	public DVector mult(double x, double y, double z, double w) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		this.w *= w;
		return this;
	}

	public DVector divied(DVector v) { return clone().div(v); }

	public DVector divied(double x, double y) { return clone().div(x, y); }

	public DVector divied(double x, double y, double z) { return clone().div(x, y, z); }

	public DVector divied(double x, double y, double z, double w) { return clone().div(x, y, z, w); }

	public static DVector div(DVector v, double n) { return v.clone().mult(1.0f / n); }

	public DVector div(double n) { return mult(1.0f / n); }

	public static DVector div(DVector a, DVector b) { return a.clone().div(b); }

	public DVector div(DVector v) { return div(v.x, v.y, v.z, v.w); }

	public DVector div(double x, double y) {
		if (x != 0) this.x /= x;
		if (y != 0) this.y /= y;
		return this;
	}

	public DVector div(double x, double y, double z) {
		if (x != 0) this.x /= x;
		if (y != 0) this.y /= y;
		if (z != 0) this.z /= z;
		return this;
	}

	public DVector div(double x, double y, double z, double w) {
		if (x != 0) this.x /= x;
		if (y != 0) this.y /= y;
		if (z != 0) this.z /= z;
		if (w != 0) this.w /= w;
		return this;
	}

	public double dist(DVector v) { return dist(this, v); }

	public static double dist(DVector v1, DVector v2) {
		return DVector.sub(v2, v1).mag();
	}

	public double dot(DVector b) { return dot(this, b); }

	public static double dot(DVector a, DVector b) {
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}

	public double dot(double x, double y, double z) {
		return this.x * x + this.y * y + this.z * z;
	}

	public DVector cross(DVector b) {
		return cross(this, b);
	}

	public double crossX(DVector b) {
		return crossX(this, b);
	}

	public double crossY(DVector b) {
		return crossY(this, b);
	}

	public double crossZ(DVector b) {
		return crossZ(this, b);
	}

	public static DVector cross(DVector a, DVector b) {
		return new DVector(
				(a.y * b.z - a.z * b.y),
				(a.z * b.x - a.x * b.z),
				(a.x * b.y - a.y * b.x));
	}

	public static double crossX(DVector a, DVector b) {
		return a.y * b.z - a.z * b.y;
	}

	public static double crossY(DVector a, DVector b) {
		return a.z * b.x - a.x * b.z;
	}

	public static double crossZ(DVector a, DVector b) {
		return a.x * b.y - a.y * b.x;
	}

	public DVector normalize() {
		double m = mag();
		if (m != 0.0f && m != 1.0f) div(m);
		return this;
	}

	public DVector normalized() { return clone().normalize(); }

	public DVector limit(double max) {
		if (magSq() > max * max) {
			normalize();
			mult(max);
		}
		return this;
	}

	public DVector setMag(double len) {
		normalize();
		mult(len);
		return this;
	}

	/**
	 * Calculate the angle of rotation for this vector *
	 * 
	 * @return the angle of rotation
	 */
	public double heading() {
		double angle = Math.atan2(y, x);
		return angle;
	}

	/**
	 * Rotate the vector by an angle (2D only)
	 * 
	 * @param theta
	 *            the angle of rotation
	 */
	public DVector rotate(double theta) {
		double temp = x;
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
	public DVector lerp(DVector v, double amt) {
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
	public static DVector lerp(DVector v1, DVector v2, double amt) {
		DVector v = v1.copy();
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
	public DVector lerp(double x, double y, double z, double amt) {
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
	public static double angleBetween(DVector v1, DVector v2) {
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
		return Math.acos(amt);
	}

	@Override
	public String toString() {
		return "[ " + x + ", " + y + ", " + z + " ]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(w);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof DVector)) return false;
		DVector other = (DVector) obj;
		if (Double.doubleToLongBits(w) != Double.doubleToLongBits(other.w)) return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) return false;
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z)) return false;
		return true;
	}

	public static DVector normal(DVector a, DVector b, DVector c) {
		return DVector.cross(DVector.sub(b, a), DVector.sub(c, a)).normalize();
	}

	public DVector neg() { return mult(-1); }

	public DVector negated() { return clone().neg(); }

	public static DVector neg(DVector v) { return v.negated(); }

	public static DVector avg(DVector... ins) {
		DVector avg = new DVector();
		for (int i = 0; i < ins.length; i++) {
			avg.add(ins[i]);
		}
		avg.div(ins.length);
		return avg;
	}

	public static DVector fromMatrix(DMatrix m) {
		return new DVector(
				m.data.length > 0 ? m.data[0][0] : 0,
				m.data.length > 1 ? m.data[1][0] : 0,
				m.data.length > 2 ? m.data[2][0] : 0);
	}

	public static DVector fromArray(double[] array) {
		return new DVector(
				array.length > 0 ? array[0] : 0,
				array.length > 1 ? array[1] : 0,
				array.length > 2 ? array[2] : 0);
	}

	public static DVector floor(DVector v) { return new DVector(v.x(), v.y()); }

	public DVector floor() { x = x(); y = y(); z = z(); w = w(); return this; }

	public DVector floored() { return clone().floor(); }

	public static DVector min(DVector a, DVector b) { return min(a.x, a.y, b.x, b.y); }

	public static DVector min(DVector a, double bx, double by) { return min(a.x, a.y, bx, by); }

	public static DVector min(double ax, double ay, double bx, double by) {
		return new DVector(
				MathUtils.instance.min(ax, bx),
				MathUtils.instance.min(ay, by));
	}

	public DVector min(DVector b) { return min(b.x, b.y); }

	public DVector min(double bx, double by) { x = MathUtils.instance.min(x, bx); y = MathUtils.instance.min(y, by); return this; }

	public DVector mined(DVector b) { return clone().min(b); }

	public DVector mined(double bx, double by) { return clone().min(bx, by); }

	public static DVector max(DVector a, DVector b) { return max(a.x, a.y, b.x, b.y); }

	public static DVector max(DVector a, double bx, double by) { return max(a.x, a.y, bx, by); }

	public static DVector max(double ax, double ay, double bx, double by) {
		return new DVector(
				MathUtils.instance.max(ax, bx),
				MathUtils.instance.max(ay, by));
	}

	public DVector max(DVector b) { return max(b.x, b.y); }

	public DVector max(double bx, double by) { x = MathUtils.instance.max(x, bx); y = MathUtils.instance.max(y, by); return this; }

	public DVector maxed(DVector b) { return clone().max(b); }

	public DVector maxed(double bx, double by) { return clone().max(bx, by); }

}
