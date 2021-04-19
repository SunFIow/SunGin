package com.sunflow.gfx;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import com.sunflow.game.Game3D;
import com.sunflow.game.GameBase;
import com.sunflow.math.SVector;
import com.sunflow.util.SStyle;
import com.sunflow.util.Transform;

public abstract class S_Shape {

//	public static boolean tempShape = false;

	public Shape path;
	public SStyle style;
	public AffineTransform transform;

	public static void drawAll(SGFX screen) {
		if (screen instanceof Game3D) Shape3D.drawAll((Game3D) screen);
		else if (screen instanceof GameBase) Shape2D.drawAll((GameBase) screen);
	}

	public static void addShape(SGFX screen) {
		if (screen instanceof Game3D) Shape3D.addShape((Game3D) screen);
		else if (screen instanceof GameBase) Shape2D.addShape(screen);
	}

	public static void endShape(SGFX screen) {
//		if (tempShape) return;
		if (screen instanceof Game3D) Shape3D.endShape((Game3D) screen);
		else if (screen instanceof GameBase) Shape2D.endShape(screen);
	}

	public static void beginShape(SGFX screen) {
//		if (tempShape) return;
		if (screen instanceof Game3D) Shape3D.beginShape((Game3D) screen);
		else if (screen instanceof GameBase) Shape2D.beginShape(screen);
	}

	public static class Shape3D extends S_Shape {
		public static ArrayList<Shape3D> shapes = new ArrayList<>();
		public static ArrayList<Shape3D> temp_shapes = new ArrayList<>();

		public ArrayList<SVector> vertices;
		public Transform m_transform;

		public static void drawAll(Game3D screen) {
//			boolean b = true;
//			if (b) return;
//			System.out.println(shapes.size());
			if (shapes.isEmpty()) return;
			int[] order = Shape3D.getOrder(screen);
			for (int i = 0; i < order.length; i++) {
				Shape3D shape = shapes.get(order[i]);
//				System.out.println(shape.dist(screen.vCameraPos));
//				System.out.println(screen.vCameraPos);

//				if (shape.path != null) {
				screen.push();
				screen.style(shape.style);
				screen.setTransform(shape.transform);
				screen.gMatrix.setTransform(shape.m_transform);
				screen.drawShape(shape.path);
				screen.pop();
//				}
//				System.out.print(order[i] + ", ");
			}
			System.out.println();
			shapes.clear();
		}

		private final static int[] getOrder(Game3D screen) {
			int size = shapes.size();
			float[] dists = new float[size];
			int[] order = new int[size];

			for (int i = 0; i < size; i++) {
				dists[i] = shapes.get(i).dist(screen);
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

		static GraphicsMatrix gm = new GraphicsMatrix();

//		public float dist(SVector cam) { return dist(cam.x, cam.y, cam.z); }

//		public float dist(float x, float y, float z) {

		public float dist(Game3D screen) {
			gm.setTransform(m_transform);
			SVector cam_Raw = screen.vCameraPos;
			SVector cam = gm.apply(cam_Raw);
			float total = 0;
			for (int i = 0; i < vertices.size(); i++) {
				SVector v_raw = vertices.get(i);

				SVector v = gm.apply(v_raw);

//				total += Math.sqrt((x - v.x) * (x - v.x)
//						+ (y - v.y) * (y - v.y)
//						+ (z - v.z) * (z - v.z));
				total += cam.dist(v);
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

		public static Shape3D getShape(Game3D screen) { return getShape(screen, null); } // TODO reuse old Shapes

		public static Shape3D getShape(Game3D screen, Shape3D s) {
			if (s == null) s = new Shape3D();
			s.path = (GeneralPath) screen.getGraphics().gpath.clone();
			s.vertices = screen.vertices;
			s.style = screen.getStyle();
			s.transform = screen.getGraphics().graphics.getTransform();
			s.m_transform = screen.gMatrix.getTransform();
			return s;
		}
	}

	public static class Shape2D extends S_Shape {
		public static ArrayList<Shape2D> shapes = new ArrayList<>();
		public static ArrayList<Shape2D> temp_shapes = new ArrayList<>();

		public static void drawAll(GameBase screen) {
			for (Shape2D shape : shapes) {
				screen.push();
				screen.style(shape.style);
				screen.getGraphics().setTransform(shape.transform);
				screen.drawShape(shape.path);
				screen.pop();
			}
			shapes.clear();
		}

		public static void beginShape(GameBase screen) {
			temp_shapes.clear();
		}

		public static void addShape(GameBase screen) {
			temp_shapes.add(getShape(screen));
		}

		public static void endShape(GameBase screen) {
			shapes.addAll(temp_shapes);
			temp_shapes.clear();
		}

		public static Shape2D getShape(GameBase screen) { return getShape(screen, null); }

		public static Shape2D getShape(GameBase screen, Shape2D s) {
			if (s == null) s = new Shape2D();
			s.path = (GeneralPath) screen.getGraphics().gpath.clone();
			s.style = screen.getStyle();
			s.transform = screen.getGraphics().graphics.getTransform();
			return s;
		}
	}
}