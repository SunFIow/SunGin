package com.sunflow.math3d.models;

import java.awt.Color;

import com.sunflow.game.Game3D;
import com.sunflow.math3d.Calculator;
import com.sunflow.math3d.Plane;
import com.sunflow.math3d.Vertex3F;

public class DPoint extends BaseModel {

	public float dist;

	protected Game3D screen;

	protected PointObject drawablePoint;

	public DPoint(Game3D screen, float x, float y, float z) {
		this(screen, new Vertex3F(x, y, z));
	}

	public DPoint(Game3D screen, Vertex3F pos) {
		this.screen = screen;
		this.pos = pos;
		this.drawablePoint = new PointObject(screen, 0, 0);
	}

	@Override
	public void updateModel() {
		float newX;
		float newY;
		boolean draw = true;
		float x = parent.pos.x + pos.x;
		float y = parent.pos.y + pos.y;
		float z = parent.pos.z + pos.z;
		float[] calcPos = Calculator.CalculatePositionP(screen.vCameraPos, screen.vCameraDir, x, y, z);
		newX = (screen.width / 2 - Calculator.calcFocusPos[0]) + calcPos[0] * screen.zoom();
		newY = (screen.height / 2 - Calculator.calcFocusPos[1]) + calcPos[1] * screen.zoom();
		if (Calculator.t < 0) draw = false;

		calcLighting();

		drawablePoint.draw = draw;
		drawablePoint.update(newX, newY);
		dist = getDistToP(screen.vCameraPos.x, screen.vCameraPos.y, screen.vCameraPos.z);
//		needsUpdate = false;
	}

	private void calcLighting() {
		Plane lightingPlane = new Plane(pos, pos);
//		Vertex3F NV = pos;
		Vertex3F NV = lightingPlane.NV;
		float angle = (float) Math.acos(((NV.x * screen.vLightDir.x) + (NV.y * screen.vLightDir.y) + (NV.z * screen.vLightDir.z)) / (screen.vLightDir.mag()));
//		float angle = Math.acos(Vertex3F.dot(lightingPlane.NV, screen.vLightDir)/ (screen.vLightDir.mag()));

		float lighting = (float) (0.2 + 1 - Math.sqrt(Math.toDegrees(angle) / 180));

		if (lighting > 1) lighting = 1;
		if (lighting < 0) lighting = 0;

		drawablePoint.lighting = lighting;
	}

	@Override
	public void rotateX(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		Vertex3F v = pos;
		float newY = v.y * cos - v.z * sin;
		float newZ = v.z * cos + v.y * sin;
		v.y = newY;
		v.z = newZ;

//		markDirty();
	}

	@Override
	public void rotateY(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		Vertex3F v = pos;
		float newX = v.x * cos - v.z * sin;
		float newZ = v.z * cos + v.x * sin;
		v.x = newX;
		v.z = newZ;

//		markDirty();
	}

	@Override
	public void rotateZ(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		Vertex3F v = pos;
		float newX = v.x * cos - v.y * sin;
		float newY = v.y * cos + v.x * sin;
		v.x = newX;
		v.y = newY;

//		markDirty();
	}

	public float getDistToP() { return getDistToP(screen.vCameraPos); }

	public float getDistToP(Vertex3F p) { return getDistToP(p.x, p.y, p.z); }

	public float getDistToP(float x, float y, float z) {
		float total = 0;
		Vertex3F v = pos;
		float _x = parent.pos.x + pos.x + v.x;
		float _y = parent.pos.y + pos.y + v.y;
		float _z = parent.pos.z + pos.z + v.z;
		total += Math.sqrt((x - _x) * (x - _x)
				+ (y - _y) * (y - _y)
				+ (z - _z) * (z - _z));

		return total;
	}

	@Override
	public float dist() { return dist; }

	@Override
	public boolean contains(float x, float y) { return drawablePoint.contains(x, y); }

	public void render(boolean renderStroke, Color stroke, float strokeWeight, boolean highlight, boolean seeThrough) {
		drawablePoint.renderStroke(renderStroke);
		drawablePoint.stroke(stroke);
		drawablePoint.strokeWeight(strokeWeight);
		drawablePoint.highlight(highlight);
		drawablePoint.seeThrough(seeThrough);
		render();
	}

	@Override
	public void render() { drawablePoint.render(); }

	public void stroke(Color stroke) { drawablePoint.stroke(stroke); }

	public void renderStroke(boolean renderStroke) { drawablePoint.renderStroke(renderStroke); }

	public void strokeWeight(float strokeWeight) { drawablePoint.strokeWeight(strokeWeight); }

	@Override
	public void highlight(boolean highlight) { drawablePoint.highlight(highlight); }

	@Override
	public void seeThrough(boolean seeThrough) { drawablePoint.seeThrough(seeThrough); }

	public void lighting(boolean lighting) {
		drawablePoint.lighting(lighting);
	}

	@Override
	public boolean draw() { return drawablePoint.draw; }

	@Override
	public boolean visible() { return drawablePoint.visible; }
}
