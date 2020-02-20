package com.sunflow.math3d;

import com.sunflow.math.Vertex2D;

public class Vertex3D extends Vertex2D implements Cloneable {
	public double z;

	public Vertex3D() { this(0, 0, 0); }

	public Vertex3D(Vertex3D v) { this(v.x, v.y, v.z); }

	public Vertex3D(double x, double y, double z) {
		super(x, y);
		this.z = z;
	}

	public static Vertex3D of() {
		return of(0, 0, 0);
	}

	public static Vertex3D of(Vertex3D v) {
		return of(v.x, v.y, v.z);
	}

	public static Vertex3D of(double x, double y, double z) {
		return new Vertex3D(x, y, z);
	}

	@Override
	public Vertex3D clone() {
		return (Vertex3D) super.clone();
	}

	public int z() { return (int) z; }

	public Vertex3D set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public Vertex3D set(Vertex3D b) {
		return set(b.x, b.y, b.z);
	}

	public Vertex3D add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vertex3D add(Vertex3D b) {
		return add(b.x, b.y, b.z);
	}

	public static Vertex3D add(Vertex3D a, Vertex3D b) {
		return add(a.x, a.y, a.z, b.x, b.y, b.z);
	}

	public static Vertex3D add(double x1, double y1, double z1, double x2, double y2, double z2) {
		return new Vertex3D(
				x1 + x2,
				y1 + y2,
				z1 + z2);
	}

	public Vertex3D sub(double x, double y, double z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	public Vertex3D sub(Vertex3D b) {
		return sub(b.x, b.y, b.z);
	}

	public static Vertex3D sub(Vertex3D a, Vertex3D b) {
		return sub(a.x, a.y, a.z, b.x, b.y, b.z);
	}

	public static Vertex3D sub(double x1, double y1, double z1, double x2, double y2, double z2) {
		return new Vertex3D(
				x1 - x2,
				y1 - y2,
				z1 - z2);
	}

	@Override
	public double mag() {
		return Math.sqrt(dot(this, this));
	}

	@Override
	public double magSq() {
		return dot(this, this);
	}

	@Override
	public Vertex3D neg() {
		return clone().mult(-1);
	}

	public double dot(Vertex3D b) {
		return dot(this, b);
	}

	public static double dot(Vertex3D a, Vertex3D b) {
		return (a.x * b.x + a.y * b.y + a.z * b.z);
	}

	public Vertex3D cross(Vertex3D b) {
		return cross(this, b);
	}

	public static Vertex3D cross(Vertex3D a, Vertex3D b) {
		return new Vertex3D(
				(a.y * b.z - a.z * b.y),
				(a.z * b.x - a.x * b.z),
				(a.x * b.y - a.y * b.x));
	}

	public static Vertex3D normal(Vertex3D a, Vertex3D b, Vertex3D c) {
		return cross(Vertex3D.sub(b, a), Vertex3D.sub(c, a)).normalized();
	}

	@Override
	public Vertex3D normalize() {
		if (mag() > 0)
			div(mag());
		return this;
	}

	public Vertex3D normalized() {
		return clone().normalize();
	}

	public double dist(Vertex3D other) {
		return Vertex3D.sub(other, this).mag();
	}

	@Override
	public Vertex3D mult(double m) {
		return mult(m, m, m);
	}

	public Vertex3D mult(Vertex3D v) {
		return mult(v.x, v.y, v.z);
	}

	public Vertex3D mult(double w, double h, double d) {
		super.mult(w, h);
		z *= d;
		return this;
	}

	@Override
	public Vertex3D div(double m) {
		return div(m, m, m);
	}

	public Vertex3D div(Vertex3D v) {
		return div(v.x, v.y, v.z);
	}

	public Vertex3D div(double w, double h, double d) {
		super.div(w, h);
		z /= d;
		return this;
	}

	public static Vertex3D avg(Vertex3D... ins) {
		Vertex3D avg = new Vertex3D();
		for (int i = 0; i < ins.length; i++) {
			avg.add(ins[i]);
		}
		avg.div(ins.length);
		return avg;
	}

	public static double dist(Vertex3D v1, Vertex3D v2) {
		return Vertex3D.sub(v2, v1).mag();
	}

	public MatrixD toMatrix() {
		double[][] m = new double[3][1];
		m[0][0] = x;
		m[1][0] = y;
		m[2][0] = z;
		return new MatrixD(3, 1).set(m);
	}

	static public MatrixD toMatrix(Vertex3D v) {
		return v.toMatrix();
	}

	static public Vertex3D fromMatrix(MatrixD m) {
		return new Vertex3D(
				m.data.length > 0 ? m.data[0][0] : 0,
				m.data.length > 1 ? m.data[1][0] : 0,
				m.data.length > 2 ? m.data[2][0] : 0);
	}

	@Override
	public String toString() {
		return "Vertex3D[x=" + x + ",y=" + y + ",z=" + z + "]";
	}
}
