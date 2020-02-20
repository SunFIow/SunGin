package com.sunflow.math3d.models;

import com.sunflow.math3d.Vertex3F;
import com.sunflow.util.MathUtils;

public abstract class BaseModel implements MathUtils {

	public static BaseModel ZERO = new BaseModel() {
		@Override
		public void updateModel() {}

		@Override
		public void rotateX(float angle) {}

		@Override
		public void rotateY(float angle) {}

		@Override
		public void rotateZ(float angle) {}

		@Override
		public void render() {}

		@Override
		public void highlight(boolean highlight) {}

		@Override
		public void seeThrough(boolean seeThrough) {}

		@Override
		public boolean draw() { return false; }

		@Override
		public boolean visible() { return false; }

		@Override
		public float dist() { return 0; }

		@Override
		public boolean contains(float x, float y) { return false; }
	};

	protected BaseModel parent = ZERO;
	public Vertex3F pos = new Vertex3F();

	protected boolean needsUpdate = true;

	public void translateModel(Vertex3F pos) {
		this.pos.add(pos);
		markDirty();
	}

	public void translateModel(float x, float y, float z) {
		this.pos.add(x, y, z);
		markDirty();
	}

	public boolean needsUpdate() { return needsUpdate; }

	public void markDirty() { needsUpdate = true; }

	public void setParent(Base3DModel parent) { this.parent = parent; }

	public abstract void updateModel();

	public abstract void rotateX(float angle);

	public abstract void rotateY(float angle);

	public abstract void rotateZ(float angle);

	public abstract void render();

	public abstract void highlight(boolean highlight);

	public abstract void seeThrough(boolean seeThrough);

	public abstract boolean draw();

	public abstract boolean visible();

	public abstract float dist();

	public abstract boolean contains(float x, float y);
}
