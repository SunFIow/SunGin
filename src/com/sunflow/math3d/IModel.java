package com.sunflow.math3d;

public abstract class IModel {
	public DPolygon[] polys = new DPolygon[0];

	public abstract void rotate(float angle);

	public abstract void updatePolygon();
}
