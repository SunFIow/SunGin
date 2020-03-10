package com.sunflow.gfx;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import com.sunflow.math3d.Vertex3F;
import com.sunflow.util.Style;

public class Shape {
	public ArrayList<Vertex3F> vertices;
	public GeneralPath path;
	public Style style;
	public AffineTransform transform;
	public float[] rotation;

	public float dist(Vertex3F cam) { return dist(cam.x, cam.y, cam.z); }

	public float dist(float x, float y, float z) {
		float total = 0;
		for (int i = 0; i < vertices.size(); i++) {
			Vertex3F v = vertices.get(i);

			total += Math.sqrt((x - v.x) * (x - v.x)
					+ (y - v.y) * (y - v.y)
					+ (z - v.z) * (z - v.z));
		}
		return total / vertices.size();
	}
}