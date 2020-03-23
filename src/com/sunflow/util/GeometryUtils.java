package com.sunflow.util;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sunflow.math.Vertex2D;

public interface GeometryUtils {
	public static final GeometryUtils instance = new GeometryUtils() {};

	default boolean hitLineLine(double l1x1, double l1y1, double l1x2, double l1y2, double l2x1, double l2y1, double l2x2, double l2y2) {
		return Line2D.linesIntersect(l1x1, l1y1, l1x2, l1y2, l2x1, l2y1, l2x2, l2y2);
	}

	default boolean hitLineBox(double lx1, double ly1, double lx2, double ly2, double bx, double by, double bw, double bh) {
		return new Rectangle2D.Double(bx, by, bw, bh).intersectsLine(lx1, ly1, lx2, ly2);
	}

	default boolean hitLineCircle(double lx1, double ly1, double lx2, double ly2, double cx, double cy, double cr) {
//		List<Vertex2D> ps = getLineCircleIntersectionPoints(lx1, ly1, lx2, ly2, cx, cy, cr);
//		return !ps.isEmpty();
		return Line2D.ptLineDist(lx1, ly1, lx2, ly2, cx, cy) <= cr;
	}

	default List<Vertex2D> getLineCircleIntersectionPoints(double lx1, double ly1, double lx2, double ly2, double cx, double cy, double cr) {
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

		Vertex2D p1 = new Vertex2D(lx1 - baX * abScalingFactor1, ly1
				- baY * abScalingFactor1);
		if (disc == 0) { // abScalingFactor1 == abScalingFactor2
			return Collections.singletonList(p1);
		}
		Vertex2D p2 = new Vertex2D(lx1 - baX * abScalingFactor2, ly1
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
		return new Rectangle2D.Double(bx, by, w, h).contains(px, py);
	}

	default double distLinePoint(float x1, float y1, float x2, float y2, float px, float py) {
		return Line2D.ptLineDist(x1, y1, x2, y2, px, py);
	}

}
