package com.sunflow.math3d.models;

import java.awt.Color;

import com.sunflow.game.Game3D;
import com.sunflow.math3d.Vertex3D;

public class Cube extends Base3DModel {

//	private float width, height, depth;

	public Cube(Game3D game, double x, double y, double z, double width, double height, double depth) {
		this(game, x, y, z, width, height, depth, new Color(25, 25, 25));
	}

	public Cube(Game3D game, double x, double y, double z, double width, double height, double depth, Color outline) {
		super(x, y, z);
//		this.width = width;
//		this.height = height;
//		this.depth = depth;

		Vertex3D[] vs = new Vertex3D[8];
		vs[0] = new Vertex3D(-width / 2, -height / 2, -depth / 2);
		vs[1] = new Vertex3D(-width / 2, height / 2, -depth / 2);
		vs[2] = new Vertex3D(width / 2, height / 2, -depth / 2);
		vs[3] = new Vertex3D(width / 2, -height / 2, -depth / 2);
		vs[4] = new Vertex3D(-width / 2, -height / 2, depth / 2);
		vs[5] = new Vertex3D(-width / 2, height / 2, depth / 2);
		vs[6] = new Vertex3D(width / 2, height / 2, depth / 2);
		vs[7] = new Vertex3D(width / 2, -height / 2, depth / 2);

		DPolygon[] polygone = new DPolygon[6];

		polygone[0] = new DPolygon(game, vs[0], vs[1], vs[2], vs[3]);
		polygone[1] = new DPolygon(game, vs[4], vs[5], vs[1], vs[0]);
		polygone[2] = new DPolygon(game, vs[7], vs[4], vs[5], vs[6]);
		polygone[3] = new DPolygon(game, vs[3], vs[2], vs[6], vs[7]);
		polygone[4] = new DPolygon(game, vs[1], vs[5], vs[6], vs[2]);
		polygone[5] = new DPolygon(game, vs[0], vs[4], vs[7], vs[3]);

		Color[] cs = new Color[] { Color.red, Color.green, Color.blue, Color.orange, Color.magenta, new Color(150, 50, 0) };

		for (int i = 0; i < polygone.length; i++) {
//			int grayScale = (int) map(i, 0, polygone.length - 1, 100, 200);
//			Color fill = new Color(grayScale, grayScale, grayScale, 160);
			Color fill = cs[i];
			polygone[i].fill = fill;
			polygone[i].outline = outline;
			polygone[i].drawFill = true;
			polygone[i].drawOutline = false;
		}

		addPolygone(polygone);
	}

}
