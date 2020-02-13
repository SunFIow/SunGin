package com.sunflow.tutorial_copy;

import java.awt.Color;

import com.sunflow.math3d.Vertex3F;

public class Cube extends IModel {
//	private float width, length, height, rotation = (float) (Math.PI * 0.75);

	public Cube(TutorialGame3D screen, float x, float y, float z, float width, float depth, float height, Color c) {
		super(x, y, z);

		Vertex3F[] vs = new Vertex3F[8];
		vs[0] = new Vertex3F(-width / 2, -height / 2, -depth / 2);
		vs[1] = new Vertex3F(-width / 2, height / 2, -depth / 2);
		vs[2] = new Vertex3F(width / 2, height / 2, -depth / 2);
		vs[3] = new Vertex3F(width / 2, -height / 2, -depth / 2);
		vs[4] = new Vertex3F(-width / 2, -height / 2, depth / 2);
		vs[5] = new Vertex3F(-width / 2, height / 2, depth / 2);
		vs[6] = new Vertex3F(width / 2, height / 2, depth / 2);
		vs[7] = new Vertex3F(width / 2, -height / 2, depth / 2);

		DPolygon[] polygone = new DPolygon[6];

		polygone[0] = new DPolygon(screen, vs[0], vs[1], vs[2], vs[3]);
		polygone[1] = new DPolygon(screen, vs[4], vs[5], vs[1], vs[0]);
		polygone[2] = new DPolygon(screen, vs[7], vs[4], vs[5], vs[6]);
		polygone[3] = new DPolygon(screen, vs[3], vs[2], vs[6], vs[7]);
		polygone[4] = new DPolygon(screen, vs[1], vs[5], vs[6], vs[2]);
		polygone[5] = new DPolygon(screen, vs[0], vs[4], vs[7], vs[3]);

		Color[] cs = new Color[] { Color.red, Color.green, Color.blue, Color.orange, Color.magenta, new Color(150, 50, 0) };

		for (int i = 0; i < polygone.length; i++) {
//			int grayScale = (int) map(i, 0, polygone.length - 1, 100, 200);
//			Color fill = new Color(grayScale, grayScale, grayScale, 160);
			Color fill = cs[i];
			Color outline = new Color(25, 25, 25);
			polygone[i].drawablePolygon.fill = fill;
			polygone[i].drawablePolygon.outline = outline;
			polygone[i].drawablePolygon.drawFill = true;
			polygone[i].drawablePolygon.drawOutline = false;
			polygone[i].drawablePolygon.seeThrough = false;
		}

		addPolygone(polygone);
	}

	@Override
	public void updatePolygon() { for (DPolygon poly : polys) poly.updatePolygon(); }

//	@SuppressWarnings("unused")
//	private void updateDirection(float toX, float toY) {
//		float xdif = toX - (x + width / 2) + 0.00001f;
//		float ydif = toY - (y + length / 2) + 0.00001f;
//
//		float angle = (float) Math.atan(ydif / xdif) + 0.75f * (float) Math.PI;
//
//		if (xdif < 0) angle += (float) Math.PI;
//
//		rotation = angle;
//	}
}
