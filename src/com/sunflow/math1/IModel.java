package com.sunflow.math1;

public abstract class IModel {
	public DPolygon[] polys = new DPolygon[0];

	public abstract void rotate(double angle);

	public abstract void updatePolygon();
}
