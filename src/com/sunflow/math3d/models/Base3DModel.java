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

	public void render(Graphics2D g, boolean renderFill, Color fill, boolean renderStroke, Color stroke, boolean highlight, boolean seeThrough) {
		for (DPolygon pol : polys) pol.render(renderFill, fill, renderStroke, stroke, highlight, seeThrough);
	}

	@Override
	public void render() { for (DPolygon pol : polys) pol.render(); }

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

	public void stroke(Color stroke) { for (DPolygon pol : polys) pol.stroke(stroke); }

	public void renderFill(boolean renderFill) { for (DPolygon pol : polys) pol.renderFill(renderFill); }

	public void renderStroke(boolean renderStroke) { for (DPolygon pol : polys) pol.renderStroke(renderStroke); }

	@Override
	public void highlight(boolean highlight) { for (DPolygon pol : polys) pol.highlight(highlight); }

	@Override
	public void seeThrough(boolean seeThrough) { for (DPolygon pol : polys) pol.seeThrough(seeThrough); }

	@Override
	public boolean draw() {
		for (DPolygon pol : polys) if (pol.draw()) return true;
		return false;
	}

	@Override
	public boolean visible() {
		for (DPolygon pol : polys) if (pol.visible()) return true;
		return false;
	}

	@Override
	public float dist() {
		float total = 0;
		for (DPolygon pol : polys) total += pol.dist();
		return total / polys.length;
	}

	@Override
	public boolean contains(float x, float y) {
		for (DPolygon pol : polys) if (pol.contains(x, y)) return true;
		return false;
	}
}
