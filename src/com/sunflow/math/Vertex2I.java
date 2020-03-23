package com.sunflow.math;

public class Vertex2I implements Cloneable {

	public int x;
	public int y;

	public Vertex2I() {
		this(0, 0);
	}

	public Vertex2I(Vertex2I v) {
		this(v.x, v.y);
	}

	public Vertex2I(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public static Vertex2I of() {
		return of(0, 0);
	}

	public static Vertex2I of(Vertex2I v) {
		return of(v.x, v.y);
	}

	public static Vertex2I of(int x, int y) {
		return new Vertex2I(x, y);
	}

	@Override
	public Vertex2I clone() {
		try {
			return (Vertex2I) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Vertex2I set(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Vertex2I set(Vertex2I v) {
		return set(v.x, v.y);
	}

	public Vertex2I add(int x, int y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public Vertex2I add(Vertex2I b) {
		return add(b.x, b.y);
	}

	public static Vertex2I add(Vertex2I a, Vertex2I b) {
		return add(a.x, a.y, b.x, b.y);
	}

	public static Vertex2I add(int x1, int y1, int x2, int y2) {
		return new Vertex2I(x1 + x2, y1 + y2);
	}

	public Vertex2I sub(int x, int y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	public Vertex2I sub(Vertex2I b) {
		return sub(b.x, b.y);
	}

	public static Vertex2I sub(Vertex2I a, Vertex2I b) {
		return sub(a.x, a.y, b.x, b.y);
	}

	public static Vertex2I sub(int x1, int y1, int x2, int y2) {
		return new Vertex2I(x1 - x2, y1 - y2);
	}

	public Vertex2I mult(int m) {
		return mult(m, m);
	}

	public Vertex2I mult(int w, int h) {
		x *= w;
		y *= h;
		return this;
	}

	public Vertex2I mult(Vertex2I v) {
		return mult(v.x, v.y);
	}

	public Vertex2I div(int d) {
		return div(d, d);
	}

	public Vertex2I div(int w, int h) {
		x /= w;
		y /= h;
		return this;
	}

	public Vertex2I div(Vertex2I v) {
		return div(v.x, v.y);
	}

	public double mag() {
		return Math.sqrt(dot(this, this));
	}

	public double magSq() {
		return dot(this, this);
	}

	public Vertex2I neg() {
		Vertex2I ret = this.clone();
		ret.mult(-1);
		return ret;
	}

	public static double dot(Vertex2I a, Vertex2I b) {
		return (a.x * b.x + a.y * b.y);
	}

	public static double dist(Vertex2I v1, Vertex2I v2) {
		return Vertex2I.sub(v2, v1).mag();
	}

	public Vertex2I normalize() {
		double m = mag();
		if (m != 0 && m != 1) {
			div((int) m);
		}
		return this;
	}

	public Vertex2I limit(int max) {
		if (magSq() > max * max) {
			normalize();
			mult(max);
		}
		return this;
	}

	public static Vertex2I random2D() { return random2D(null); }

	public static Vertex2I random2D(Vertex2I target) {
		return fromAngle(Math.random() * Math.PI * 2.0D, target);
	}

	public static Vertex2I fromAngle(double angle) { return fromAngle(angle, null); }

	public static Vertex2I fromAngle(double angle, Vertex2I target) {
		if (target == null) {
			target = new Vertex2I((int) Math.cos(angle), (int) Math.sin(angle));
		} else {
			target.set((int) Math.cos(angle), (int) Math.sin(angle));
		}
		return target;
	}

	@Override
	public String toString() {
		return "Vertex2I[x=" + x + ",y=" + y + "]";
	}
}
