package com.sunflow.math3d;

import com.sunflow.math.Vertex2I;

public class Vertex3I extends Vertex2I implements Cloneable {
	public int z;

	public Vertex3I() {
		this(0, 0, 0);
	}

	public Vertex3I(Vertex3I v) {
		this(v.x, v.y, v.z);
	}

	public Vertex3I(int x, int y, int z) {
		super(x, y);
		this.z = z;
	}

	public static Vertex3I of() {
		return of(0, 0, 0);
	}

	public static Vertex3I of(Vertex3I v) {
		return of(v.x, v.y, v.z);
	}

	public static Vertex3I of(int x, int y, int z) {
		return new Vertex3I(x, y, z);
	}

	@Override
	public Vertex3I clone() {
		return (Vertex3I) super.clone();
	}

	public Vertex3I set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public Vertex3I set(Vertex3I b) {
		return set(b.x, b.y, b.z);
	}

	public Vertex3I add(int x, int y, int z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vertex3I add(Vertex3I b) {
		return add(b.x, b.y, b.z);
	}

	public static Vertex3I add(Vertex3I a, Vertex3I b) {
		return add(a.x, a.y, a.z, b.x, b.y, b.z);
	}

	public static Vertex3I add(int x1, int y1, int z1, int x2, int y2, int z2) {
		return new Vertex3I(
				x1 + x2,
				y1 + y2,
				z1 + z2);
	}

	public Vertex3I sub(int x, int y, int z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	public Vertex3I sub(Vertex3I b) {
		return sub(b.x, b.y, b.z);
	}

	public static Vertex3I sub(Vertex3I a, Vertex3I b) {
		return sub(a.x, a.y, a.z, b.x, b.y, b.z);
	}

	public static Vertex3I sub(int x1, int y1, int z1, int x2, int y2, int z2) {
		return new Vertex3I(
				x1 - x2,
				y1 - y2,
				z1 - z2);
	}

	@Override
	public double mag() {
		return (int) Math.sqrt(dot(this, this));
	}

	@Override
	public double magSq() {
		return dot(this, this);
	}

	@Override
	public Vertex3I neg() {
		return clone().mult(-1);
	}

	public int dot(Vertex3I b) {
		return dot(this, b);
	}

	public static int dot(Vertex3I a, Vertex3I b) {
		return (a.x * b.x + a.y * b.y + a.z * b.z);
	}

	public Vertex3I cross(Vertex3I b) {
		return cross(this, b);
	}

	public static Vertex3I cross(Vertex3I a, Vertex3I b) {
		return new Vertex3I(
				(a.y * b.z - a.z * b.y),
				(a.z * b.x - a.x * b.z),
				(a.x * b.y - a.y * b.x));
	}

	public static Vertex3I normal(Vertex3I a, Vertex3I b, Vertex3I c) {
		return cross(Vertex3I.sub(b, a), Vertex3I.sub(c, a)).normalized();
	}

	@Override
	public Vertex3I normalize() {
		if (mag() > 0)
			div((int) mag());
		return this;
	}

	public Vertex3I normalized() {
		return clone().normalize();
	}

	public double dist(Vertex3I other) {
		return Vertex3I.sub(other, this).mag();
	}

	@Override
	public Vertex3I mult(int m) {
		return mult(m, m, m);
	}

	public Vertex3I mult(Vertex3I v) {
		return mult(v.x, v.y, v.z);
	}

	public Vertex3I mult(int w, int h, int d) {
		super.mult(w, h);
		z *= d;
		return this;
	}

	@Override
	public Vertex3I div(int m) {
		return div(m, m, m);
	}

	public Vertex3I div(Vertex3I v) {
		return div(v.x, v.y, v.z);
	}

	public Vertex3I div(int w, int h, int d) {
		super.div(w, h);
		z /= d;
		return this;
	}

	public static Vertex3I avg(Vertex3I... ins) {
		Vertex3I avg = new Vertex3I();
		for (int i = 0; i < ins.length; i++) {
			avg.add(ins[i]);
		}
		avg.div(ins.length);
		return avg;
	}

	public static double dist(Vertex3I v1, Vertex3I v2) {
		return Vertex3I.sub(v2, v1).mag();
	}

	public MatrixI toMatrix() {
		int[][] m = new int[3][1];
		m[0][0] = x;
		m[1][0] = y;
		m[2][0] = z;
		return new MatrixI(3, 1).set(m);
	}

	static public MatrixI toMatrix(Vertex3I v) {
		return v.toMatrix();
	}

	static public Vertex3I fromMatrix(MatrixI m) {
		return new Vertex3I(
				m.data.length > 0 ? m.data[0][0] : 0,
				m.data.length > 1 ? m.data[1][0] : 0,
				m.data.length > 2 ? m.data[2][0] : 0);
	}

	@Override
	public String toString() {
		return "Vertex3I[x=" + x + ",y=" + y + ",z=" + z + "]";
	}
}
