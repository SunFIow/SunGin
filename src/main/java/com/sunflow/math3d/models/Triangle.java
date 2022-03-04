package com.sunflow.math3d.models;

import com.sunflow.game.Game3D;
import com.sunflow.math.SVector;

public class Triangle extends DPolygon {

	public Triangle(Game3D game, SVector v0, SVector v1, SVector v2) {
		super(game, v0, v1, v2);
	}

	public Triangle(Game3D game, float v1x, float v1y, float v1z, float v2x, float v2y, float v2z, float v3x, float v3y, float v3z) {
		super(game, new SVector(v1x, v1y, v1z), new SVector(v2x, v2y, v2z), new SVector(v3x, v3y, v3z));
	}
}
