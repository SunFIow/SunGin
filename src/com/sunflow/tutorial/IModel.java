package com.sunflow.tutorial;

public abstract class IModel {
	public DPolygon[] polys = new DPolygon[0];

	public abstract void rotate(double angle);

	public abstract void updatePolygon();
}
