package com.sunflow.math3d.models;

import com.sunflow.game.Game3D;
import com.sunflow.math3d.Vertex3F;

public class Triangle extends DPolygon {

	public Triangle(Game3D game, Vertex3F v0, Vertex3F v1, Vertex3F v2) {
		super(game, v0, v1, v2);
	}

	public Triangle(Game3D game, float v1x, float v1y, float v1z, float v2x, float v2y, float v2z, float v3x, float v3y, float v3z) {
		super(game, new Vertex3F(v1x, v1y, v1z), new Vertex3F(v2x, v2y, v2z), new Vertex3F(v3x, v3y, v3z));
	}
}
