package com.sunflow.math3d.models;

import com.sunflow.game.Game3D;
import com.sunflow.math3d.Vertex3D;

public class Triangle extends DPolygon {

	public Triangle(Game3D game, Vertex3D v0, Vertex3D v1, Vertex3D v2) {
		super(game, v0, v1, v2);
	}

	public Triangle(Game3D game, double v1x, double v1y, double v1z, double v2x, double v2y, double v2z, double v3x, double v3y, double v3z) {
		super(game, new Vertex3D(v1x, v1y, v1z), new Vertex3D(v2x, v2y, v2z), new Vertex3D(v3x, v3y, v3z));
	}
}
