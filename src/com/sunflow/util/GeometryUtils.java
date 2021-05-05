package com.sunflow.util;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.sunflow.math.DVector;
import com.sunflow.math.SVector;

public interface GeometryUtils {
	public static final GeometryUtils instance = new GeometryUtils() {};

	default Optional<SVector> cast(SVector ray, SVector A, SVector B, SVector C) {
		// Get the Plane-Repesentation of the Triangle ABC
		SVector plane = SVector.normalBig(A, B, C);
		plane.w = SVector.dot(plane, A);

		// Get the Distance of the Plane to the Camera[0,0,0] relative to the ray
		float t = plane.w / SVector.dot(plane, ray); // TODO Combine with Vector3.dot of "constructPlane"

		// Test if the Plane is in front of the Camera
		if (t <= 0) {
			// It's not so there is no intersection
			return Optional.empty();
		}

		// Get the Intersection-Point P of the Ray with the Plane of ABC
		SVector P = new SVector(t * ray.x, t * ray.y, t * ray.z);

		// Lastly check if the Intersection-Point P is inside the Triangle ABC
		float w1 = (A.x * (C.y - A.y) + (P.y - A.y) * (C.x - A.x) - P.x * (C.y - A.y)) / ((B.y - A.y) * (C.x - A.x) - (B.x - A.x) * (C.y - A.y));
		float w2 = (P.y - A.y - w1 * (B.y - A.y)) / (C.y - A.y);
		boolean intersects = w1 >= 0 && w2 >= 0 && w1 + w2 <= 1;

		if (intersects) return Optional.of(P);
		return Optional.empty();
	}

	/**
	 * Returns an Optional SVector containing the intersection point, provided there is one.
	 * 
	 * @param x1
	 *            x-coordination from the first point of the line
	 * @param y1
	 *            y-coordination from the first point of the line
	 * 
	 * @param x2
	 *            x-coordination from the second point of the line
	 * @param y2
	 *            y-coordination from the second point of the line
	 * 
	 * @param x3
	 *            x-coordination from the position of the ray
	 * @param y3
	 *            y-coordination from the position of the ray
	 * 
	 * @param x4
	 *            x-coordination from the direction of the ray
	 * @param y4
	 *            y-coordination from the direction of the ray
	 */
	default Optional<SVector> cast(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		x4 += x3;
		y4 += y3;
		float den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		if (den == 0) return Optional.empty();

		float t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / den;
		float u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / den;
		if (t <= 0 || t >= 1 || u <= 0) return Optional.empty();

		float x = x1 + t * (x2 - x1);
		float y = y1 + t * (y2 - y1);
		return Optional.of(new SVector(x, y));
	}

	/**
	 * Returns an Optional DVector containing the intersection point, provided there is one.
	 * 
	 * @param x1
	 *            x-coordination from the first point of the line
	 * @param y1
	 *            y-coordination from the first point of the line
	 * 
	 * @param x2
	 *            x-coordination from the second point of the line
	 * @param y2
	 *            y-coordination from the second point of the line
	 * 
	 * @param x3
	 *            x-coordination from the position of the ray
	 * @param y3
	 *            y-coordination from the position of the ray
	 * 
	 * @param x4
	 *            x-coordination from the direction of the ray
	 * @param y4
	 *            y-coordination from the direction of the ray
	 */
	default Optional<DVector> cast(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
		x4 += x3;
		y4 += y3;
		double den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		if (den == 0) return Optional.empty();

		double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / den;
		double u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / den;
		if (t <= 0 || t >= 1 || u <= 0) return Optional.empty();

		double x = x1 + t * (x2 - x1);
		double y = y1 + t * (y2 - y1);
		return Optional.of(new DVector(x, y));
	}

	default boolean hitLineLine(double l1x1, double l1y1, double l1x2, double l1y2, double l2x1, double l2y1, double l2x2, double l2y2) {
		return Line2D.linesIntersect(l1x1, l1y1, l1x2, l1y2, l2x1, l2y1, l2x2, l2y2);
	}

	default boolean hitLineBox(double lx1, double ly1, double lx2, double ly2, double bx, double by, double bw, double bh) {
		return new Rectangle2D.Double(bx, by, bw, bh).intersectsLine(lx1, ly1, lx2, ly2);
	}

	default boolean hitLineCircle(double lx1, double ly1, double lx2, double ly2, double cx, double cy, double cr) {
//		List<SVector> ps = getLineCircleIntersectionPoints(lx1, ly1, lx2, ly2, cx, cy, cr);
//		return !ps.isEmpty();
		return Line2D.ptLineDist(lx1, ly1, lx2, ly2, cx, cy) <= cr;
	}

	default List<SVector> getLineCircleIntersectionPoints(double lx1, double ly1, double lx2, double ly2, double cx, double cy, double cr) {
		double baX = lx2 - lx1;
		double baY = ly2 - ly1;
		double caX = cx - lx1;
		double caY = cy - ly1;

		double a = baX * baX + baY * baY;
		double bBy2 = baX * caX + baY * caY;
		double c = caX * caX + caY * caY - cr * cr;

		double pBy2 = bBy2 / a;
		double q = c / a;

		double disc = pBy2 * pBy2 - q;
		if (disc < 0) {
			return Collections.emptyList();
		}
		// if disc == 0 ... dealt with later
		double tmpSqrt = Math.sqrt(disc);
		double abScalingFactor1 = -pBy2 + tmpSqrt;
		double abScalingFactor2 = -pBy2 - tmpSqrt;

		SVector p1 = new SVector(lx1 - baX * abScalingFactor1, ly1
				- baY * abScalingFactor1);
		if (disc == 0) { // abScalingFactor1 == abScalingFactor2
			return Collections.singletonList(p1);
		}
		SVector p2 = new SVector(lx1 - baX * abScalingFactor2, ly1
				- baY * abScalingFactor2);
		return Arrays.asList(p1, p2);
	}

	default boolean hitBoxBox(double b1x, double b1y, double b1w, double b1h, double b2x, double b2y, double b2w, double b2h) {
		return (b2x + b2w > b1x &&
				b2y + b2h > b1y &&
				b2x < b1x + b1w &&
				b2y < b1y + b1h);
	}

	default boolean hitBoxCircle(double bx, double by, double bw, double bh, double cx, double cy, double cr) {
		Point2D.Double circleDistance = new Point2D.Double();
		circleDistance.x = Math.abs(cx - bx);
		circleDistance.y = Math.abs(cy - by);

		if (circleDistance.x > (bw / 2 + cr)) return false;

		if (circleDistance.y > (bh / 2 + cr)) return false;

		if (circleDistance.x <= (bw / 2)) return true;

		if (circleDistance.y <= (bh / 2)) return true;

		double cornerDistance_sq = Math.pow((circleDistance.x - bw / 2), 2) +
				Math.pow((circleDistance.y - bh / 2), 2);

		return (cornerDistance_sq <= Math.pow(cr, 2));
	}

	default boolean hitCircleCircle(double x1, double y1, double r1, double x2, double y2, double r2) {
		return (MathUtils.instance.dist(x1, y1, x2, y2) <= r1 + r2);
	}

	default boolean hitBoxPoint(float bx, float by, float w, float h, float px, float py) {
		return new Rectangle2D.Float(bx, by, w, h).contains(px, py);
	}

	default boolean hitBoxPoint(double bx, double by, double w, double h, double px, double py) {
		return new Rectangle2D.Double(bx, by, w, h).contains(px, py);
	}

	default double distLinePoint(double x1, double y1, double x2, double y2, double px, double py) {
		return Line2D.Double.ptLineDist(x1, y1, x2, y2, px, py);
	}

}
