package com.sunflow.math3d.models;

import java.awt.Color;

import com.sunflow.game.Game3D;
import com.sunflow.math.SVector;

public class Cube extends Base3DModel {

//	private float width, height, depth;

	public Cube(Game3D screen, float x, float y, float z, float width, float height, float depth) {
		this(screen, x, y, z, width, height, depth, null);
	}

	public Cube(Game3D screen, float x, float y, float z, float width, float height, float depth, Color c) {
		super(x, y, z);
//		this.width = width;
//		this.height = height;
//		this.depth = depth;

		SVector[] vs = new SVector[8];
		vs[0] = new SVector(-width / 2, -height / 2, -depth / 2);
		vs[1] = new SVector(-width / 2, height / 2, -depth / 2);
		vs[2] = new SVector(width / 2, height / 2, -depth / 2);
		vs[3] = new SVector(width / 2, -height / 2, -depth / 2);
		vs[4] = new SVector(-width / 2, -height / 2, depth / 2);
		vs[5] = new SVector(-width / 2, height / 2, depth / 2);
		vs[6] = new SVector(width / 2, height / 2, depth / 2);
		vs[7] = new SVector(width / 2, -height / 2, depth / 2);

		DPolygon[] polygone = new DPolygon[6];

		polygone[0] = new DPolygon(screen, vs[0], vs[1], vs[2], vs[3]);
		polygone[1] = new DPolygon(screen, vs[4], vs[5], vs[1], vs[0]);
		polygone[2] = new DPolygon(screen, vs[7], vs[4], vs[5], vs[6]);
		polygone[3] = new DPolygon(screen, vs[3], vs[2], vs[6], vs[7]);
		polygone[4] = new DPolygon(screen, vs[1], vs[5], vs[6], vs[2]);
		polygone[5] = new DPolygon(screen, vs[0], vs[4], vs[7], vs[3]);

		Color[] cs = new Color[] { Color.white, Color.green, Color.yellow, Color.blue, new Color(255, 110, 10), Color.red };

		for (int i = 0; i < polygone.length; i++) {
			Color fill = c != null ? c : cs[i];
			polygone[i].fill(fill);
		}

		addPolygone(polygone);
	}

}
