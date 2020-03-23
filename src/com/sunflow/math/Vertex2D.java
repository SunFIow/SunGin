package com.sunflow.math;

public class Vertex2D implements Cloneable {

	public double x;
	public double y;

	public Vertex2D() {
		this(0, 0);
	}

	public Vertex2D(Vertex2D v) {
		this(v.x, v.y);
	}

	public Vertex2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public static Vertex2D of() { return of(0, 0); }

	public static Vertex2D of(Vertex2D v) { return of(v.x, v.y); }

	public static Vertex2D of(double x, double y) { return new Vertex2D(x, y); }

	@Override
	public Vertex2D clone() {
		try {
			return (Vertex2D) super.clone();
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

	public Vertex2D set(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Vertex2D set(Vertex2D v) {
		return set(v.x, v.y);
	}

	public Vertex2D add(double x, double y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public Vertex2D add(Vertex2D b) {
		return add(b.x, b.y);
	}

	public static Vertex2D add(Vertex2D a, Vertex2D b) {
		return add(a.x, a.y, b.x, b.y);
	}

	public static Vertex2D add(double x1, double y1, double x2, double y2) {
		return new Vertex2D(x1 + x2, y1 + y2);
	}

	public Vertex2D sub(double x, double y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	public Vertex2D sub(Vertex2D b) {
		return sub(b.x, b.y);
	}

	public static Vertex2D sub(Vertex2D a, Vertex2D b) {
		return sub(a.x, a.y, b.x, b.y);
	}

	public static Vertex2D sub(double x1, double y1, double x2, double y2) {
		return new Vertex2D(x1 - x2, y1 - y2);
	}

	public Vertex2D mult(double m) {
		return mult(m, m);
	}

	public Vertex2D mult(double w, double h) {
		x *= w;
		y *= h;
		return this;
	}

	public Vertex2D mult(Vertex2D v) {
		return mult(v.x, v.y);
	}

	public Vertex2D div(double d) {
		return div(d, d);
	}

	public Vertex2D div(double w, double h) {
		x /= w;
		y /= h;
		return this;
	}

	public Vertex2D div(Vertex2D v) {
		return div(v.x, v.y);
	}

	public double mag() {
		return Math.sqrt(dot(this, this));
	}

	public double magSq() {
		return dot(this, this);
	}

	public Vertex2D neg() {
		Vertex2D ret = this.clone();
		ret.mult(-1);
		return ret;
	}

	public static double dot(Vertex2D a, Vertex2D b) {
		return (a.x * b.x + a.y * b.y);
	}

	public static double dist(Vertex2D v1, Vertex2D v2) {
		return Vertex2D.sub(v2, v1).mag();
	}

	public Vertex2D normalize() {
		double m = mag();
		if (m != 0 && m != 1) {
			div(m);
		}
		return this;
	}

	public Vertex2D limit(double max) {
		if (magSq() > max * max) {
			normalize();
			mult(max);
		}
		return this;
	}

	public static Vertex2D random2D() { return random2D(null); }

	public static Vertex2D random2D(Vertex2D target) {
		return fromAngle(Math.random() * Math.PI * 2.0D, target);
	}

	public static Vertex2D fromAngle(double angle) { return fromAngle(angle, null); }

	public static Vertex2D fromAngle(double angle, Vertex2D target) {
		if (target == null) {
			target = new Vertex2D(Math.cos(angle), Math.sin(angle));
		} else {
			target.set(Math.cos(angle), Math.sin(angle));
		}
		return target;
	}

	@Override
	public String toString() {
		return "Vertex2D[x=" + x + ",y=" + y + "]";
	}

}
