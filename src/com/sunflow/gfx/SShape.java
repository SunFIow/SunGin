package com.sunflow.gfx;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import com.sunflow.game.Game2D;
import com.sunflow.game.Game3D;
import com.sunflow.math.SVector;
import com.sunflow.util.Style;
import com.sunflow.util.Transform;

public abstract class SShape {

	public static boolean tempShape = false;

	public GeneralPath path;
	public Style style;
	public AffineTransform transform;

	public static void drawAll(SGraphics screen) {
		if (screen instanceof Game3D) Shape3D.drawAll((Game3D) screen);
		else if (screen instanceof Game2D) Shape2D.drawAll(screen);
	}

	public static void addShape(SGraphics screen) {
		if (screen instanceof Game3D) Shape3D.addShape((Game3D) screen);
		else if (screen instanceof Game2D) Shape2D.addShape(screen);
	}

	public static void endShape(SGraphics screen) {
		if (tempShape) return;
		if (screen instanceof Game3D) Shape3D.endShape((Game3D) screen);
		else if (screen instanceof Game2D) Shape2D.endShape(screen);
	}

	public static void beginShape(SGraphics screen) {
		if (tempShape) return;
		if (screen instanceof Game3D) Shape3D.beginShape((Game3D) screen);
		else if (screen instanceof SGraphics) Shape2D.beginShape(screen);
	}

	public static class Shape3D extends SShape {
		public static ArrayList<Shape3D> shapes = new ArrayList<>();
		public static ArrayList<Shape3D> temp_shapes = new ArrayList<>();

		public ArrayList<SVector> vertices;
		public Transform m_transform;

		public static void drawAll(Game3D screen) {
			if (shapes.size() < 1) return;
			int[] order = shapes.get(0).getOrder(screen);
			for (int i = 0; i < order.length; i++) {
				Shape3D shape = shapes.get(order[i]);

//				if (shape.path != null) {
				screen.push();
				screen.style(shape.style);
				screen.graphics.setTransform(shape.transform);
				screen.gMatrix.transform(shape.m_transform);
				screen.drawShape(shape.path);
				screen.pop();
//				}
			}
			shapes.clear();
		}

		private final int[] getOrder(Game3D screen) {
			int size = shapes.size();
			float[] dists = new float[size];
			int[] order = new int[size];

			for (int i = 0; i < size; i++) {
				dists[i] = shapes.get(i).dist(screen.vCameraPos);
				order[i] = i;
			}

			float ftemp;
			int itemp;
			for (int a = 0; a < size - 1; a++) {
				for (int b = 0; b < size - 1; b++) {
					if (dists[b] < dists[b + 1]) {
						ftemp = dists[b];
						itemp = order[b];
						order[b] = order[b + 1];
						dists[b] = dists[b + 1];

						order[b + 1] = itemp;
						dists[b + 1] = ftemp;
					}
				}
			}

			return order;
		}

		public float dist(SVector cam) { return dist(cam.x, cam.y, cam.z); }

		public float dist(float x, float y, float z) {
			float total = 0;
			for (int i = 0; i < vertices.size(); i++) {
				SVector v = vertices.get(i);

				total += Math.sqrt((x - v.x) * (x - v.x)
						+ (y - v.y) * (y - v.y)
						+ (z - v.z) * (z - v.z));
			}
			return total / vertices.size();
		}

		public static void beginShape(Game3D screen) {
			temp_shapes.clear();
		}

		public static void addShape(Game3D screen) {
			temp_shapes.add(getShape(screen));
		}

		public static void endShape(Game3D screen) {
			shapes.addAll(temp_shapes);
			temp_shapes.clear();
		}

		public static Shape3D getShape(Game3D screen) { return getShape(screen, null); }

		public static Shape3D getShape(Game3D screen, Shape3D s) {
			if (s == null) s = new Shape3D();
			s.path = screen.gpath;
			s.vertices = screen.vertices;
			s.style = screen.getStyle();
			s.transform = screen.graphics.getTransform();
			s.m_transform = screen.gMatrix.getTransform();
			return s;
		}
	}

	public static class Shape2D extends SShape {
		public static ArrayList<Shape2D> shapes = new ArrayList<>();
		public static ArrayList<Shape2D> temp_shapes = new ArrayList<>();

		public static void drawAll(SGraphics screen) {
			for (Shape2D shape : shapes) {
				screen.push();
				screen.style(shape.style);
				screen.graphics.setTransform(shape.transform);
				screen.drawShape(shape.path);
				screen.pop();
			}
			shapes.clear();
		}

		public static void beginShape(SGraphics screen) {
			temp_shapes.clear();
		}

		public static void addShape(SGraphics screen) {
			temp_shapes.add(getShape(screen));
		}

		public static void endShape(SGraphics screen) {
			shapes.addAll(temp_shapes);
			temp_shapes.clear();
		}

		public static Shape2D getShape(SGraphics screen) { return getShape(screen, null); }

		public static Shape2D getShape(SGraphics screen, Shape2D s) {
			if (s == null) s = new Shape2D();
			s.path = screen.gpath;
			s.style = screen.getStyle();
			s.transform = screen.graphics.getTransform();
			return s;
		}
	}
}