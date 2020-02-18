package com.sunflow.math3d.models;

import java.awt.Color;

import com.sunflow.game.Game3D;
import com.sunflow.math3d.Calculator;
import com.sunflow.math3d.Plane;
import com.sunflow.math3d.Vertex3F;

public class DLine extends BaseModel {

	public Vertex3F[] vertices = new Vertex3F[2];

	protected float dist;

	protected Game3D screen;

	protected LineObject drawableLine;

	public DLine(Game3D screen, float x1, float y1, float z1, float x2, float y2, float z2) {
		this(screen, new Vertex3F(x1, y1, z1), new Vertex3F(x2, y2, z2));
	}

	public DLine(Game3D screen, Vertex3F start, Vertex3F end) {
		this.screen = screen;
//		this.pos;
		vertices[0] = start;
		vertices[1] = end;
		this.drawableLine = new LineObject(screen, 0, 0, 0, 0);
	}

	@Override
	public void updateModel() {
		float[] newX = new float[2];
		float[] newY = new float[2];
		boolean draw = true;
		for (int i = 0; i < 2; i++) {
			Vertex3F v = vertices[i];
			float x = parent.pos.x + pos.x + v.x;
			float y = parent.pos.y + pos.y + v.y;
			float z = parent.pos.z + pos.z + v.z;
			float[] calcPos = Calculator.CalculatePositionP(screen.vCameraPos, screen.vCameraDir, x, y, z);
			newX[i] = (screen.width / 2 - Calculator.calcFocusPos[0]) + calcPos[0] * screen.zoom();
			newY[i] = (screen.height / 2 - Calculator.calcFocusPos[1]) + calcPos[1] * screen.zoom();
			if (Calculator.t < 0) draw = false;
		}

		calcLighting();

		drawableLine.draw = draw;
		drawableLine.update(newX[0], newY[0], newX[1], newY[1]);
		dist = getDistToP(screen.vCameraPos.x, screen.vCameraPos.y, screen.vCameraPos.z);
//		needsUpdate = false;
	}

	private void calcLighting() {
		Plane lightingPlane = new Plane(vertices[0], vertices[1]);
		Vertex3F NV = lightingPlane.NV;
		float angle = (float) Math.acos(((NV.x * screen.vLightDir.x) + (NV.y * screen.vLightDir.y) + (NV.z * screen.vLightDir.z)) / (screen.vLightDir.mag()));
//		float angle = Math.acos(Vertex3F.dot(lightingPlane.NV, screen.vLightDir)/ (screen.vLightDir.mag()));

		float lighting = (float) (0.2 + 1 - Math.sqrt(Math.toDegrees(angle) / 180));

		if (lighting > 1) lighting = 1;
		if (lighting < 0) lighting = 0;

		drawableLine.lighting = lighting;
	}

	@Override
	public void rotateX(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (Vertex3F v : vertices) {
			float newY = v.y * cos - v.z * sin;
			float newZ = v.z * cos + v.y * sin;
			v.y = newY;
			v.z = newZ;
		}
//		markDirty();
	}

	@Override
	public void rotateY(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (Vertex3F v : vertices) {
			float newX = v.x * cos - v.z * sin;
			float newZ = v.z * cos + v.x * sin;
			v.x = newX;
			v.z = newZ;
		}
//		markDirty();
	}

	@Override
	public void rotateZ(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (Vertex3F v : vertices) {
			float newX = v.x * cos - v.y * sin;
			float newY = v.y * cos + v.x * sin;
			v.x = newX;
			v.y = newY;
		}
//		markDirty();
	}

	public float getDistToP() { return getDistToP(screen.vCameraPos); }

	public float getDistToP(Vertex3F p) { return getDistToP(p.x, p.y, p.z); }

	public float getDistToP(float x, float y, float z) {
		float total = 0;
		for (int i = 0; i < vertices.length; i++) {
			Vertex3F v = vertices[i];
			float _x = parent.pos.x + pos.x + v.x;
			float _y = parent.pos.y + pos.y + v.y;
			float _z = parent.pos.z + pos.z + v.z;
			total += Math.sqrt((x - _x) * (x - _x)
					+ (y - _y) * (y - _y)
					+ (z - _z) * (z - _z));
		}
		return total / vertices.length;
	}

	@Override
	public float dist() { return dist; }

	@Override
	public boolean contains(float x, float y) { return drawableLine.contains(x, y); }

	public void render(boolean renderStroke, Color stroke, float strokeWeight, boolean highlight, boolean seeThrough) {
		drawableLine.renderStroke(renderStroke);
		drawableLine.stroke(stroke);
		drawableLine.strokeWeight(strokeWeight);
		drawableLine.highlight(highlight);
		drawableLine.seeThrough(seeThrough);
		render();
	}

	@Override
	public void render() { drawableLine.render(); }

	public void stroke(Color stroke) {
		drawableLine.stroke(stroke);
	}

	public void renderStroke(boolean renderStroke) {
		drawableLine.renderStroke(renderStroke);
	}

	public void strokeWeight(float strokeWeight) {
		drawableLine.strokeWeight(strokeWeight);
	}

	@Override
	public void highlight(boolean highlight) {
		drawableLine.highlight(highlight);
	}

	@Override
	public void seeThrough(boolean seeThrough) {
		drawableLine.seeThrough(seeThrough);
	}

	public void lighting(boolean lighting) {
		drawableLine.lighting(lighting);
	}

	@Override
	public boolean draw() { return drawableLine.draw; }

	@Override
	public boolean visible() { return drawableLine.visible; }
}
