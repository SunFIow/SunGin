package com.sunflow.math3d.models;

import java.awt.Color;

import com.sunflow.game.Game3D;
import com.sunflow.math.SVector;
import com.sunflow.math3d.Plane;

public class DPoint extends BaseModel {

	public float dist;

	protected Game3D screen;

	protected PointObject drawablePoint;

	public DPoint(Game3D screen, float x, float y, float z) {
		this(screen, new SVector(x, y, z));
	}

	public DPoint(Game3D screen, SVector pos) {
		this.screen = screen;
		this.pos = pos;
		this.drawablePoint = new PointObject(screen, 0, 0);
	}

	@Override
	public void updateModel() {
		float newX;
		float newY;
		boolean draw = true;

//		float x = parent.pos.x + pos.x;
//		float y = parent.pos.y + pos.y;
//		float z = parent.pos.z + pos.z;

		SVector aPos = absolutePosition();
		float x = aPos.x;
		float y = aPos.y;
		float z = aPos.z;

//		float[] calcPos = Calculator.CalculatePositionP(screen.vCameraPos, screen.vCameraDir, x, y, z);
//		newX = (screen.width / 2 - Calculator.calcFocusPos[0]) + calcPos[0] * screen.zoom();
//		newY = (screen.height / 2 - Calculator.calcFocusPos[1]) + calcPos[1] * screen.zoom();
//		if (Calculator.t < 0) draw = false;
		float[] pos = screen.convert3Dto2D(screen.apply(x, y, z));
		newX = pos[0];
		newY = pos[1];
		if (pos[2] < 0) draw = false;

		calcLighting();

		drawablePoint.draw = draw;
		drawablePoint.update(newX, newY);
		dist = getDistToP(screen.vCameraPos.x, screen.vCameraPos.y, screen.vCameraPos.z);
		needsUpdate = false;
	}

	private void calcLighting() {
		Plane lightingPlane = new Plane(pos, pos);
//		SVector NV = pos;
		SVector NV = lightingPlane.NV;
		float angle = (float) Math.acos(((NV.x * screen.vLightDir.x) + (NV.y * screen.vLightDir.y) + (NV.z * screen.vLightDir.z)) / (screen.vLightDir.mag()));
//		float angle = Math.acos(SVector.dot(lightingPlane.NV, screen.vLightDir)/ (screen.vLightDir.mag()));

		float lighting = (float) (0.2 + 1 - Math.sqrt(Math.toDegrees(angle) / 180));

		if (lighting > 1) lighting = 1;
		if (lighting < 0) lighting = 0;

		drawablePoint.lighting = lighting;
	}

	@Override
	public void rotateX(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		SVector v = pos;
		float newY = v.y * cos - v.z * sin;
		float newZ = v.z * cos + v.y * sin;
		v.y = newY;
		v.z = newZ;

		markDirty();
	}

	@Override
	public void rotateY(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		SVector v = pos;
		float newX = v.x * cos - v.z * sin;
		float newZ = v.z * cos + v.x * sin;
		v.x = newX;
		v.z = newZ;

		markDirty();
	}

	@Override
	public void rotateZ(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		SVector v = pos;
		float newX = v.x * cos - v.y * sin;
		float newY = v.y * cos + v.x * sin;
		v.x = newX;
		v.y = newY;

		markDirty();
	}

	@Override
	public void rotateX(float angle, SVector origin) {
		float cos = cos(angle);
		float sin = sin(angle);

		SVector v = pos;
		float y = v.y - origin.y;
		float z = v.z - origin.z;

		float newY = y * cos - z * sin;
		float newZ = z * cos + y * sin;

		float y_ = origin.y - newY;
		float z_ = origin.z - newZ;

		v.y = y_;
		v.z = z_;

		markDirty();
	}

	@Override
	public void rotateY(float angle, SVector origin) {
		float cos = cos(angle);
		float sin = sin(angle);

		SVector v = pos;

		float x = v.x - origin.x;
		float z = v.z - origin.z;

		float newX = x * cos - z * sin;
		float newZ = z * cos + x * sin;

		float x_ = origin.y - newX;
		float z_ = origin.z - newZ;

		v.x = x_;
		v.z = z_;

		markDirty();
	}

	@Override
	public void rotateZ(float angle, SVector origin) {
		float cos = cos(angle);
		float sin = sin(angle);

		SVector v = pos;

		float x = v.x - origin.x;
		float y = v.y - origin.y;

		float newX = x * cos - y * sin;
		float newY = y * cos + x * sin;

		float x_ = origin.x - newX;
		float y_ = origin.y - newY;

		v.x = x_;
		v.y = y_;

		markDirty();
	}

	public float getDistToP() { return getDistToP(screen.vCameraPos); }

	public float getDistToP(SVector p) { return getDistToP(p.x, p.y, p.z); }

	public float getDistToP(float x, float y, float z) {
		float total = 0;

//		float _x = parent.pos.x + pos.x;
//		float _y = parent.pos.y + pos.y;
//		float _z = parent.pos.z + pos.z;
		SVector aPos = absolutePosition();
		SVector rPos = screen.apply(aPos);

		total += Math.sqrt((x - rPos.x) * (x - rPos.x)
				+ (y - rPos.y) * (y - rPos.y)
				+ (z - rPos.z) * (z - rPos.z));

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

	@Override
	public void lighting(boolean lighting) {
		drawablePoint.lighting(lighting);
	}

	@Override
	public boolean draw() { return drawablePoint.draw; }

	@Override
	public boolean visible() { return drawablePoint.visible; }
}
