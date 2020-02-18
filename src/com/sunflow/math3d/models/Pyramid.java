package com.sunflow.math3d.models;

import java.awt.Color;

import com.sunflow.game.Game3D;
import com.sunflow.math3d.Vertex3F;

public class Pyramid extends Base3DModel {
//	private float width, length, height, rotation = (float) (Math.PI * 0.75);

	public Pyramid(Game3D screen, float x, float y, float z, float width, float depth, float height, Color c) {
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

		for (DPolygon pol : polygone) {
			Color fill = c;
			Color stroke = new Color(25, 25, 25);
			pol.fill(fill);
			pol.stroke(stroke);
			pol.renderFill(true);
			pol.renderStroke(false);
			pol.seeThrough(false);
		}

		addPolygone(polygone);
	}

}
