package com.sunflow.math3d.models;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sunflow.math3d.Vertex3F;

public abstract class Base3DModel extends BaseModel implements Cloneable {

	public DPolygon[] polys = new DPolygon[0];

	public Base3DModel(DPolygon... vs) { this(0, 0, 0, vs); }

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
//		for (DPolygon pol : polys) pol.markDirty();
	}

	public void render(Graphics2D g, boolean renderFill, Color fill, boolean renderOutline, Color outline, boolean highlight, boolean seeThrough) {
		for (DPolygon pol : polys) pol.render(g, renderFill, fill, renderOutline, outline, highlight, seeThrough);
	}

	@Override
	public void render(Graphics2D g) { for (DPolygon pol : polys) pol.render(g); }

	@Override
	public void updateModel() {
		for (DPolygon pol : polys) pol.updateModel();
//		needsUpdate = false;
	}

	@Override
	public void rotateX(float angle) {
		for (DPolygon pol : polys) pol.rotateX(angle);
//		markDirty();
	}

	@Override
	public void rotateY(float angle) {
		for (DPolygon pol : polys) pol.rotateY(angle);
//		markDirty();
	}

	@Override
	public void rotateZ(float angle) {
		for (DPolygon pol : polys) pol.rotateZ(angle);
//		markDirty();
	}

//	@Override
//	public boolean needsUpdate() {
//		if (needsUpdate) return true;
//		for (DPolygon pol : polys) if (pol.needsUpdate()) return true;
//		return false;
//	}

	@Override
	protected Base3DModel clone() {
		try {
			return (Base3DModel) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void fill(Color fill) { for (DPolygon pol : polys) pol.fill(fill); }

	public void outline(Color outline) { for (DPolygon pol : polys) pol.outline(outline); }

	public void renderFill(boolean renderFill) { for (DPolygon pol : polys) pol.renderFill(renderFill); }

	public void renderOutline(boolean renderOutline) { for (DPolygon pol : polys) pol.renderOutline(renderOutline); }

	public void highlight(boolean highlight) { for (DPolygon pol : polys) pol.highlight(highlight); }

	public void seeThrough(boolean seeThrough) { for (DPolygon pol : polys) pol.seeThrough(seeThrough); }

}
