package com.sunflow.math3d.models;

import com.sunflow.game.Game3D;

public abstract class DrawableObject {

	protected boolean draw = true, visible = true, seeThrough, highlight;

	protected boolean useLighting = true;
	public float lighting = 1;

	protected Game3D screen;

	public DrawableObject(Game3D screen) {
		this.screen = screen;
	}

	public abstract void render();

	public abstract boolean contains(float x, float y);

	public void lighting(boolean useLighting) {
		this.useLighting = useLighting;
	}
}
