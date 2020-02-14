package com.sunflow.tutorial;

import com.sunflow.math3d.Vertex3F;
import com.sunflow.util.MathUtils;

public abstract class IModel implements MathUtils {

	public static IModel ZERO = new IModel() {
		@Override
		public void updatePolygon() {}
	};

	protected IModel parent = ZERO;

	protected Vertex3F pos;

	protected DPolygon[] polys = new DPolygon[0];

	public IModel(DPolygon... vs) { this(0, 0, 0, vs); }

	public IModel(float x, float y, float z, DPolygon... vs) {
		pos = new Vertex3F(x, y, z);
		addPolygone(vs);
	}

	public void updatePolygon() { for (DPolygon poly : polys) poly.updatePolygon(); }

	public void translateModel(float x, float y, float z) {
		this.pos.add(x, y, z);
//		needsUpdate = true;
	}

	public void translateModel(Vertex3F pos) {
		this.pos.add(pos);
//		needsUpdate = true;
	}

//	@Override
	public void rotateX(float angle) {
		for (DPolygon pol : polys) pol.rotateX(angle);
//		needsUpdate = true;
	}

//	@Override
	public void rotateY(float angle) {
		for (DPolygon pol : polys) pol.rotateY(angle);
//		needsUpdate = true;
	}

//	@Override
	public void rotateZ(float angle) {
		for (DPolygon pol : polys) pol.rotateZ(angle);
//		needsUpdate = true;
	}

//	public boolean needsUpdate() { return needsUpdate; }

	public void setParent(IModel parent) { this.parent = parent; }

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
}
