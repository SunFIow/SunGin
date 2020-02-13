package com.sunflow.math3d.models;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sunflow.math3d.Vertex3F;

public class Base3DModel extends BaseModel implements Cloneable {

	public DPolygon[] polys = new DPolygon[0];

	public Base3DModel(DPolygon... vs) {
		this(0, 0, 0, vs);
	}

	public Base3DModel(float x, float y, float z, DPolygon... vs) {
		pos = new Vertex3F(x, y, z);
		addPolygone(vs);
	}

	protected void addPolygone(DPolygon... vs) {
		DPolygon[] newPolygone = new DPolygon[polys.length + vs.length];
		for (int i = 0; i < polys.length; i++) {
			newPolygone[i] = polys[i];
		}
		for (int i = 0; i < vs.length; i++) {
			newPolygone[polys.length + i] = vs[i];
			newPolygone[polys.length + i].setParent(this);
		}
		polys = newPolygone;
	}

	@Override
	public void render(Graphics2D g, boolean drawFill, Color fill, boolean drawOutline, Color outline) {
//		g.setColor(outline);
		for (DPolygon pol : polys) pol.render(g, drawFill, fill, drawOutline, outline);
	}

	@Override
	public void project() {
		for (DPolygon pol : polys) {
			pol.updatePolygon();
//			pol.project();
		}
		needsUpdate = false;
	}

	@Override
	public void translateModel(float x, float y, float z) {
		pos.add(x, y, z);
		needsUpdate = true;
	}

	@Override
	public void rotateX(float angle) {
		for (DPolygon pol : polys) pol.rotateX(angle);
		needsUpdate = true;
	}

	@Override
	public void rotateY(float angle) {
		for (DPolygon pol : polys) pol.rotateY(angle);
		needsUpdate = true;
	}

	@Override
	public void rotateZ(float angle) {
		for (DPolygon pol : polys) pol.rotateZ(angle);
		needsUpdate = true;
	}

	@Override
	public boolean needsUpdate() { return needsUpdate; }

	@Override
	protected Base3DModel clone() {
		try {
			return (Base3DModel) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
