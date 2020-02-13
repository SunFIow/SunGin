package com.sunflow.math3d.models;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sunflow.math3d.Vertex3F;
import com.sunflow.util.MathUtils;

public abstract class BaseModel implements MathUtils {
	protected BaseModel parent;

	protected Vertex3F pos = new Vertex3F();

	protected float fl = 500;

	protected boolean needsUpdate = true;

	protected boolean drawFill = true;
	protected boolean drawOutline = true;

	protected Color fill = new Color(200, 200, 200);
	protected Color outline = new Color(25, 25, 25);

	public void translateModel(float x, float y, float z) {
		this.pos.add(x, y, z);
		needsUpdate = true;
	}

	public void translateModel(Vertex3F pos) {
		this.pos.add(pos);
		needsUpdate = true;
	}

	public boolean needsUpdate() {
		return needsUpdate;
	}

	public void setParent(Base3DModel parent) {
		this.parent = parent;
	}

	public abstract void project();

	public abstract void rotateX(float angle);

	public abstract void rotateY(float angle);

	public abstract void rotateZ(float angle);

	public void render(Graphics2D g) {
		this.render(g, drawFill, fill, drawOutline, outline);
	}

	public void render(Graphics2D g, boolean drawFill, boolean drawOutline) {
		this.render(g, drawFill, fill, drawOutline, outline);
	}

	public abstract void render(Graphics2D g, boolean drawFill, Color fill, boolean drawOutline, Color outline);

}
