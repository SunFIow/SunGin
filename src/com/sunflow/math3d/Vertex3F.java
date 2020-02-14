package com.sunflow.math3d;

import com.sunflow.math.Vertex2F;

public class Vertex3F extends Vertex2F implements Cloneable {
	public float z;

	public Vertex3F() { this(0, 0, 0); }

	public Vertex3F(Vertex3F v) { this(v.x, v.y, v.z); }

	public Vertex3F(float x, float y, float z) {
		super(x, y);
		this.z = z;
	}

	public static Vertex3F of() {
		return of(0, 0, 0);
	}

	public static Vertex3F of(Vertex3F v) {
		return of(v.x, v.y, v.z);
	}

	public static Vertex3F of(float x, float y, float z) {
		return new Vertex3F(x, y, z);
	}

	@Override
	public Vertex3F clone() {
		return (Vertex3F) super.clone();
	}

	public int z() { return (int) z; }

	public Vertex3F set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public Vertex3F set(Vertex3F b) {
		return set(b.x, b.y, b.z);
	}

	public Vertex3F add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vertex3F add(Vertex3F b) {
		return add(b.x, b.y, b.z);
	}

	public static Vertex3F add(Vertex3F a, Vertex3F b) {
		return add(a.x, a.y, a.z, b.x, b.y, b.z);
	}

	public static Vertex3F add(float x1, float y1, float z1, float x2, float y2, float z2) {
		return new Vertex3F(
				x1 + x2,
				y1 + y2,
				z1 + z2);
	}

	public Vertex3F sub(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	public Vertex3F sub(Vertex3F b) {
		return sub(b.x, b.y, b.z);
	}

	public static Vertex3F sub(Vertex3F a, Vertex3F b) {
		return sub(a.x, a.y, a.z, b.x, b.y, b.z);
	}

	public static Vertex3F sub(float x1, float y1, float z1, float x2, float y2, float z2) {
		return new Vertex3F(
				x1 - x2,
				y1 - y2,
				z1 - z2);
	}

	@Override
	public float mag() {
		return (float) Math.sqrt(dot(this, this));
	}

	@Override
	public float magSq() {
		return dot(this, this);
	}

	@Override
	public Vertex3F neg() {
		return clone().mult(-1);
	}

	public float dot(Vertex3F b) {
		return dot(this, b);
	}

	public static float dot(Vertex3F a, Vertex3F b) {
		return (a.x * b.x + a.y * b.y + a.z * b.z);
	}

	public Vertex3F cross(Vertex3F b) {
		return cross(this, b);
	}

	public static Vertex3F cross(Vertex3F a, Vertex3F b) {
		return new Vertex3F(
				(a.y * b.z - a.z * b.y),
				(a.z * b.x - a.x * b.z),
				(a.x * b.y - a.y * b.x));
	}

	public static Vertex3F normal(Vertex3F a, Vertex3F b, Vertex3F c) {
		return cross(Vertex3F.sub(b, a), Vertex3F.sub(c, a)).normalized();
	}

	@Override
	public Vertex3F normalize() {
		if (mag() > 0)
			div(mag());
		return this;
	}

	public Vertex3F normalized() {
		return clone().normalize();
	}

	public float dist(Vertex3F other) {
		return Vertex3F.sub(other, this).mag();
	}

	@Override
	public Vertex3F mult(float m) {
		return mult(m, m, m);
	}

	public Vertex3F mult(Vertex3F v) {
		return mult(v.x, v.y, v.z);
	}

	public Vertex3F mult(float w, float h, float d) {
		super.mult(w, h);
		z *= d;
		return this;
	}

	@Override
	public Vertex3F div(float m) {
		return div(m, m, m);
	}

	public Vertex3F div(Vertex3F v) {
		return div(v.x, v.y, v.z);
	}

	public Vertex3F div(float w, float h, float d) {
		super.div(w, h);
		z /= d;
		return this;
	}

	public static Vertex3F avg(Vertex3F... ins) {
		Vertex3F avg = new Vertex3F();
		for (int i = 0; i < ins.length; i++) {
			avg.add(ins[i]);
		}
		avg.div(ins.length);
		return avg;
	}

	public static float dist(Vertex3F v1, Vertex3F v2) {
		return Vertex3F.sub(v2, v1).mag();
	}

	@Override
	public String toString() {
		return "Vertex3F[x=" + x + ",y=" + y + ",z=" + z + "]";
	}
}
