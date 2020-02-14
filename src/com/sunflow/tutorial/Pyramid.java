package com.sunflow.tutorial;

import java.awt.Color;

import com.sunflow.math3d.Vertex3F;

public class Pyramid extends IModel {
//	private float width, length, height, rotation = (float) (Math.PI * 0.75);

	public Pyramid(TutorialGame3D screen, float x, float y, float z, float width, float depth, float height, Color c) {
		super(x, y, z);
		DPolygon[] polygone = new DPolygon[5];

		Vertex3F[] vs = new Vertex3F[8];
		vs[0] = new Vertex3F(-width / 2, -height / 2, -depth / 2);
		vs[1] = new Vertex3F(-width / 2, -height / 2, depth / 2);
		vs[2] = new Vertex3F(width / 2, -height / 2, depth / 2);
		vs[3] = new Vertex3F(width / 2, -height / 2, -depth / 2);
		vs[4] = new Vertex3F(0, height / 2, 0);

		polygone[0] = new DPolygon(screen, vs[0], vs[1], vs[2], vs[3]);
		polygone[1] = new DPolygon(screen, vs[0], vs[4], vs[3]);
		polygone[2] = new DPolygon(screen, vs[1], vs[4], vs[0]);
		polygone[3] = new DPolygon(screen, vs[2], vs[4], vs[1]);
		polygone[4] = new DPolygon(screen, vs[3], vs[4], vs[2]);

		for (int i = 0; i < polygone.length; i++) {
			Color fill = c;
			Color outline = new Color(25, 25, 25);
			polygone[i].drawablePolygon.fill = fill;
			polygone[i].drawablePolygon.outline = outline;
			polygone[i].drawablePolygon.drawFill = true;
			polygone[i].drawablePolygon.drawOutline = false;
			polygone[i].drawablePolygon.seeThrough = false;
		}
		addPolygone(polygone);
	}
}
