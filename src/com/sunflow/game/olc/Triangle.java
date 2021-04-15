package com.sunflow.game.olc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.sunflow.math.SVector;

public class Triangle {

	public SVector[] p = new SVector[3];
	public SVector[] t = new SVector[3];

	public int color = 0xffffffff;

	public Function<Integer, Integer> shaderFun;

	public Triangle() {
		this.p[0] = new SVector();
		this.p[1] = new SVector();
		this.p[2] = new SVector();

		this.t[0] = new SVector();
		this.t[1] = new SVector();
		this.t[2] = new SVector();
	}

	public Triangle(Triangle t) {
		this.p[0] = new SVector(t.p[0]);
		this.p[1] = new SVector(t.p[1]);
		this.p[2] = new SVector(t.p[2]);

		this.t[0] = new SVector(t.t[0]);
		this.t[1] = new SVector(t.t[1]);
		this.t[2] = new SVector(t.t[2]);

		this.color = t.color;
		this.shaderFun = t.shaderFun;
	}

	public Triangle(SVector v1, SVector v2, SVector v3) {
		this.p[0] = new SVector(v1);
		this.p[1] = new SVector(v2);
		this.p[2] = new SVector(v3);
		this.t[0] = new SVector();
		this.t[1] = new SVector();
		this.t[2] = new SVector();
	}

	public Triangle(SVector v1, SVector v2, SVector v3, SVector t1, SVector t2, SVector t3) {
		this.p[0] = new SVector(v1);
		this.p[1] = new SVector(v2);
		this.p[2] = new SVector(v3);

		this.t[0] = new SVector(t1);
		this.t[1] = new SVector(t2);
		this.t[2] = new SVector(t3);
	}

	public Triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
		this.p[0] = new SVector(x1, y1);
		this.p[1] = new SVector(x2, y2);
		this.p[2] = new SVector(x3, y3);

		this.t[0] = new SVector();
		this.t[1] = new SVector();
		this.t[2] = new SVector();
	}

	public Triangle(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
		this.p[0] = new SVector(x1, y1, z1);
		this.p[1] = new SVector(x2, y2, z2);
		this.p[2] = new SVector(x3, y3, z3);

		this.t[0] = new SVector();
		this.t[1] = new SVector();
		this.t[2] = new SVector();
	}

	public Triangle(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float u1, float v1, float u2, float v2, float u3, float v3) {
		this.p[0] = new SVector(x1, y1, z1);
		this.p[1] = new SVector(x2, y2, z2);
		this.p[2] = new SVector(x3, y3, z3);

		this.t[0] = new SVector(u1, v1);
		this.t[1] = new SVector(u2, v2);
		this.t[2] = new SVector(u3, v3);
	}

	public Triangle(float x1, float y1, float z1, float w1, float x2, float y2, float z2, float w2, float x3, float y3, float z3, float w3, float u1, float v1, float g1, float u2, float v2, float g2, float u3, float v3, float g3) {
		this.p[0] = new SVector(x1, y1, z1, w1);
		this.p[1] = new SVector(x2, y2, z2, w2);
		this.p[2] = new SVector(x3, y3, z3, w3);

		this.t[0] = new SVector(u1, v1, g1);
		this.t[1] = new SVector(u2, v2, g2);
		this.t[2] = new SVector(u3, v3, g3);
	}

	public Triangle copyAttributes() {
		Triangle copy = new Triangle();
		copy.color = this.color;
		copy.shaderFun = this.shaderFun;
		return copy;
	}

	static public Triangle[] Triangle_ClipAgainstPlane(Triangle tri, SVector plane_p, SVector plane_n) {
		return tri.Triangle_ClipAgainstPlane(plane_p, plane_n);
	}

	public Triangle[] Triangle_ClipAgainstPlane(SVector plane_p, SVector plane_n) {
		Triangle in_tri = this;
		Triangle out_tri1 = new Triangle(), out_tri2 = new Triangle();

		// Make sure plane normal is indeed normal
		plane_n.normalize();

		// Return signed shortest distance from point to plane, plane normal must be normalised
		Function<SVector, Float> dist = (p) -> {
//			SVector n = p.normalized();
			return (plane_n.x * p.x + plane_n.y * p.y + plane_n.z * p.z - SVector.dot(plane_n, plane_p));
		};

		// Create two temporary storage arrays to classify points either side of plane
		// If distance sign is positive, point lies on "inside" of plane
		SVector[] inside_points = new SVector[3];
		int nInsidePointCount = 0;
		SVector[] outside_points = new SVector[3];
		int nOutsidePointCount = 0;

		SVector[] inside_tex = new SVector[3];
		int nInsideTexCount = 0;
		SVector[] outside_tex = new SVector[3];
		int nOutsideTexCount = 0;

		// Get signed distance of each point in triangle to plane
		float d0 = dist.apply(in_tri.p[0]);
		float d1 = dist.apply(in_tri.p[1]);
		float d2 = dist.apply(in_tri.p[2]);

		if (d0 >= 0) {
			inside_points[nInsidePointCount++] = in_tri.p[0];
			inside_tex[nInsideTexCount++] = in_tri.t[0];
		} else {
			outside_points[nOutsidePointCount++] = in_tri.p[0];
			outside_tex[nOutsideTexCount++] = in_tri.t[0];
		}
		if (d1 >= 0) {
			inside_points[nInsidePointCount++] = in_tri.p[1];
			inside_tex[nInsideTexCount++] = in_tri.t[1];
		} else {
			outside_points[nOutsidePointCount++] = in_tri.p[1];
			outside_tex[nOutsideTexCount++] = in_tri.t[1];
		}
		if (d2 >= 0) {
			inside_points[nInsidePointCount++] = in_tri.p[2];
			inside_tex[nInsideTexCount++] = in_tri.t[2];
		} else {
			outside_points[nOutsidePointCount++] = in_tri.p[2];
			outside_tex[nOutsideTexCount++] = in_tri.t[2];
		}

		// Now classify triangle points, and break the input triangle into
		// smaller output triangles if required. There are four possible
		// outcomes...

		if (nInsidePointCount == 0) {
			// All points lie on the outside of plane, so clip whole triangle
			// It ceases to exist

			return new Triangle[0]; // No returned triangles are valid
		}

		if (nInsidePointCount == 3) {
			// All points lie on the inside of plane, so do nothing
			// and allow the triangle to simply pass through

			out_tri1 = in_tri;

			return new Triangle[] { out_tri1 }; // Just the one returned original triangle is valid
		}

		if (nInsidePointCount == 1 && nOutsidePointCount == 2) {
			// Triangle should be clipped. As two points lie outside
			// the plane, the triangle simply becomes a smaller triangle

			// Copy appearance info to new triangle
			out_tri1 = in_tri.copyAttributes();

			// The inside point is valid, so keep that...
			out_tri1.p[0] = inside_points[0];
			out_tri1.t[0] = inside_tex[0];

			// but the two new points are at the locations where the
			// original sides of the triangle (lines) intersect with the plane

			Reference<Float> t = new Reference<>();
			out_tri1.p[1] = Vector_IntersectPlane(plane_p, plane_n, inside_points[0], outside_points[0], t);
			out_tri1.t[1].x = t.value * (outside_tex[0].x - inside_tex[0].x) + inside_tex[0].x;
			out_tri1.t[1].y = t.value * (outside_tex[0].y - inside_tex[0].y) + inside_tex[0].y;
			out_tri1.t[1].w = t.value * (outside_tex[0].w - inside_tex[0].w) + inside_tex[0].w;

			out_tri1.p[2] = Vector_IntersectPlane(plane_p, plane_n, inside_points[0], outside_points[1], t);
			out_tri1.t[2].x = t.value * (outside_tex[1].x - inside_tex[0].x) + inside_tex[0].x;
			out_tri1.t[2].y = t.value * (outside_tex[1].y - inside_tex[0].y) + inside_tex[0].y;
			out_tri1.t[2].w = t.value * (outside_tex[1].w - inside_tex[0].w) + inside_tex[0].w;

			return new Triangle[] { out_tri1 }; // Return the newly formed single triangle
		}

		if (nInsidePointCount == 2 && nOutsidePointCount == 1) {
			// Triangle should be clipped. As two points lie inside the plane,
			// the clipped triangle becomes a "quad". Fortunately, we can
			// represent a quad with two new triangles

			// Copy appearance info to new triangles
			out_tri1 = in_tri.copyAttributes();
			out_tri2 = in_tri.copyAttributes();

			// The first triangle consists of the two inside points and a new
			// point determined by the location where one side of the triangle
			// intersects with the plane

			out_tri1.p[0] = inside_points[0];
			out_tri1.p[1] = inside_points[1];
			out_tri1.t[0] = inside_tex[0];
			out_tri1.t[1] = inside_tex[1];

			Reference<Float> t = new Reference<>();

			out_tri1.p[2] = Vector_IntersectPlane(plane_p, plane_n, inside_points[0], outside_points[0], t);
			out_tri1.t[2].x = t.value * (outside_tex[0].x - inside_tex[0].x) + inside_tex[0].x;
			out_tri1.t[2].y = t.value * (outside_tex[0].y - inside_tex[0].y) + inside_tex[0].y;
			out_tri1.t[2].w = t.value * (outside_tex[0].w - inside_tex[0].w) + inside_tex[0].w;

			// The second triangle is composed of one of he inside points, a
			// new point determined by the intersection of the other side of the
			// triangle and the plane, and the newly created point above
			out_tri2.p[0] = inside_points[1];
			out_tri2.p[1] = out_tri1.p[2];
			out_tri2.t[0] = inside_tex[1];
			out_tri2.t[1] = out_tri1.t[2];

			out_tri2.p[2] = Vector_IntersectPlane(plane_p, plane_n, inside_points[1], outside_points[0], t);
			out_tri2.t[2].x = t.value * (outside_tex[0].x - inside_tex[1].x) + inside_tex[1].x;
			out_tri2.t[2].y = t.value * (outside_tex[0].y - inside_tex[1].y) + inside_tex[1].y;
			out_tri2.t[2].w = t.value * (outside_tex[0].w - inside_tex[1].w) + inside_tex[1].w;

			return new Triangle[] { out_tri1, out_tri2 }; // Return two newly formed triangles which form a quad
		}

		return null;
	}

	static public SVector Vector_IntersectPlane(SVector plane_p, SVector plane_n, SVector lineStart, SVector lineEnd) {
		plane_n.normalize();
		float plane_d = -SVector.dot(plane_n, plane_p);
		float ad = SVector.dot(lineStart, plane_n);
		float bd = SVector.dot(lineEnd, plane_n);
		float t = (-plane_d - ad) / (bd - ad);
		SVector lineStartToEnd = SVector.sub(lineEnd, lineStart);
		SVector lineToIntersect = SVector.mult(lineStartToEnd, t);
		return SVector.add(lineStart, lineToIntersect);
	}

	static public SVector Vector_IntersectPlane(SVector plane_p, SVector plane_n, SVector lineStart, SVector lineEnd, Reference<Float> rT) {
		plane_n.normalize();
		float plane_d = -SVector.dot(plane_n, plane_p);
		float ad = SVector.dot(lineStart, plane_n);
		float bd = SVector.dot(lineEnd, plane_n);
		float t = (-plane_d - ad) / (bd - ad);
		SVector lineStartToEnd = SVector.sub(lineEnd, lineStart);
		SVector lineToIntersect = SVector.mult(lineStartToEnd, t);
		rT.value = t;
		return SVector.add(lineStart, lineToIntersect);
	}

	static public List<Triangle> toTriangs(List<SVector> polygIn, List<SVector> texIn) {
		List<int[]> mapping = getMapping(polygIn);

		List<Triangle> triangs = new ArrayList<>();
		for (int[] map : mapping) triangs.add(new Triangle(
				polygIn.get(map[0]), polygIn.get(map[1]), polygIn.get(map[2]),
				texIn.get(map[0]), texIn.get(map[1]), texIn.get(map[2])));

		return triangs;
	}

	static public List<Triangle> toTriangs(List<SVector> polygIn) {
		List<int[]> mapping = getMapping(polygIn);

		List<Triangle> triangs = new ArrayList<>();
		for (int[] map : mapping) triangs.add(new Triangle(
				polygIn.get(map[0]),
				polygIn.get(map[1]),
				polygIn.get(map[2])));

		return triangs;
	}

	static public List<int[]> getMapping(List<SVector> polygIn) {
//		Log.info();
		List<SVector> polyg = new ArrayList<>(polygIn);

//		boolean isCw = orien(polyg.toArray(new SVector[0])) > 0.0f;
		boolean isCw = orien(polyg) > 0.0f;

		if (!isCw) Collections.reverse(polyg);

		List<int[]> mapping = new ArrayList<>();
		while (polyg.size() >= 3) {
//			System.out.println("a");
			boolean isTriagRemoved = false;
			int sz = polyg.size();
			for (int i = 0; i < sz; i++) {
				int a = i;
				int b = (i + 1) % sz;
				int c = (i + 2) % sz;
				SVector p1 = polyg.get(a);
				SVector p2 = polyg.get(b);
				SVector p3 = polyg.get(c);

				if (orien(p1, p2, p3) <= 0.0f) continue;

				boolean hasSVector = false;
				for (int index = 0; index < polyg.size(); index++) {
					if (pointInTriang(polyg.get(index), new SVector[] { p1, p2, p3 })) {
						hasSVector = true;
						break;
					}
				}
				if (hasSVector) continue;
//				System.out.println("b");

				isTriagRemoved = true;

				int pi1 = polygIn.indexOf(p1);
				int pi2 = polygIn.indexOf(p2);
				int pi3 = polygIn.indexOf(p3);
//				Log.info(pi1, pi2, pi3);
				mapping.add(new int[] { pi1, pi2, pi3 });

				polyg.remove(b);
				sz = polyg.size();
			}
			if (!isTriagRemoved) break;
		}
		return mapping;
	}

	static private float orien(final SVector v1, final SVector v2, final SVector v3) {
		return SVector.crossZ(v1, v2) + SVector.crossZ(v2, v3) + SVector.crossZ(v3, v1);
	}

	static private float orien(final List<SVector> polyg) {
		float sum = 0;
		for (int i = 0; i < polyg.size(); i++) {
			sum += SVector.crossZ(polyg.get(i), polyg.get((i + 1) % polyg.size()));
		}
		return sum;
	}

	static private boolean pointInTriang(final SVector p, final SVector[] triang) {
		return orien(p, triang[0], triang[1]) > 0 &&
				orien(p, triang[1], triang[2]) > 0 &&
				orien(p, triang[2], triang[0]) > 0;
	}

	@Override
	public String toString() {
		return p[0] + " | " + p[1] + " | " + p[2];
	}

}