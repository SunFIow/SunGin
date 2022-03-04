package com.sunflow.math3d.models;

import java.awt.Color;

import com.sunflow.game.Game3D;
import com.sunflow.math.SVector;
import com.sunflow.math3d.Plane;

public class DLine extends BaseModel {

	public SVector[] vertices = new SVector[2];

	protected float dist;

	protected Game3D screen;

	protected LineObject drawableLine;

	public DLine(Game3D screen, float x1, float y1, float z1, float x2, float y2, float z2) {
		this(screen, new SVector(x1, y1, z1), new SVector(x2, y2, z2));
	}

	public DLine(Game3D screen, SVector start, SVector end) {
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
			SVector v = vertices[i];

//			float x = parent.pos.x + pos.x + v.x;
//			float y = parent.pos.y + pos.y + v.y;
//			float z = parent.pos.z + pos.z + v.z;

			SVector aPos = absolutePosition();
			float x = aPos.x + v.x;
			float y = aPos.y + v.y;
			float z = aPos.z + v.z;

//			float[] calcPos = Calculator.CalculatePositionP(screen.vCameraPos, screen.vCameraDir, x, y, z);
//			newX[i] = (screen.width / 2 - Calculator.calcFocusPos[0]) + calcPos[0] * screen.zoom();
//			newY[i] = (screen.height / 2 - Calculator.calcFocusPos[1]) + calcPos[1] * screen.zoom();
//			if (Calculator.t < 0) draw = false;
			float[] pos = screen.convert3Dto2D(screen.apply(x, y, z));
			newX[i] = pos[0];
			newY[i] = pos[1];
			if (pos[2] < 0) draw = false;
		}

		calcLighting();

		drawableLine.draw = draw;
		drawableLine.update(newX[0], newY[0], newX[1], newY[1]);
		dist = getDistToP(screen.vCameraPos.x, screen.vCameraPos.y, screen.vCameraPos.z);
		needsUpdate = false;
	}

	private void calcLighting() {
		Plane lightingPlane = new Plane(vertices[0], vertices[1]);
		SVector NV = lightingPlane.NV;
		float angle = (float) Math.acos(((NV.x * screen.vLightDir.x) + (NV.y * screen.vLightDir.y) + (NV.z * screen.vLightDir.z)) / (screen.vLightDir.mag()));
//		float angle = Math.acos(SVector.dot(lightingPlane.NV, screen.vLightDir)/ (screen.vLightDir.mag()));

		float lighting = (float) (0.2 + 1 - Math.sqrt(Math.toDegrees(angle) / 180));

		if (lighting > 1) lighting = 1;
		if (lighting < 0) lighting = 0;

		drawableLine.lighting = lighting;
	}

	@Override
	public void rotateX(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (SVector v : vertices) {
			float newY = v.y * cos - v.z * sin;
			float newZ = v.z * cos + v.y * sin;
			v.y = newY;
			v.z = newZ;
		}
		markDirty();
	}

	@Override
	public void rotateY(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (SVector v : vertices) {
			float newX = v.x * cos - v.z * sin;
			float newZ = v.z * cos + v.x * sin;
			v.x = newX;
			v.z = newZ;
		}
		markDirty();
	}

	@Override
	public void rotateZ(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (SVector v : vertices) {
			float newX = v.x * cos - v.y * sin;
			float newY = v.y * cos + v.x * sin;
			v.x = newX;
			v.y = newY;
		}
		markDirty();
	}

	@Override
	public void rotateX(float angle, SVector origin) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (SVector v : vertices) {
			float y = v.y - origin.y;
			float z = v.z - origin.z;

			float newY = y * cos - z * sin;
			float newZ = z * cos + y * sin;

			float y_ = origin.y - newY;
			float z_ = origin.z - newZ;

			v.y = y_;
			v.z = z_;
		}
		markDirty();
	}

	@Override
	public void rotateY(float angle, SVector origin) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (SVector v : vertices) {
			float x = v.x - origin.x;
			float z = v.z - origin.z;

			float newX = x * cos - z * sin;
			float newZ = z * cos + x * sin;

			float x_ = origin.x - newX;
			float z_ = origin.z - newZ;

			v.x = x_;
			v.z = z_;
		}
		markDirty();
	}

	@Override
	public void rotateZ(float angle, SVector origin) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (SVector v : vertices) {
			float x = v.x - origin.x;
			float y = v.y - origin.y;

			float newX = x * cos - y * sin;
			float newY = y * cos + x * sin;

			float x_ = origin.x - newX;
			float y_ = origin.y - newY;

			v.x = x_;
			v.y = y_;
		}
		markDirty();
	}

	public float getDistToP() { return getDistToP(screen.vCameraPos); }

	public float getDistToP(SVector p) { return getDistToP(p.x, p.y, p.z); }

	public float getDistToP(float x, float y, float z) {
		float total = 0;
		for (int i = 0; i < vertices.length; i++) {
			SVector v = vertices[i];

//			float _x = parent.pos.x + pos.x + v.x;
//			float _y = parent.pos.y + pos.y + v.y;
//			float _z = parent.pos.z + pos.z + v.z;

			SVector aPos = absolutePosition();
			aPos.add(v);
			SVector rPos = screen.apply(aPos);

			total += Math.sqrt((x - rPos.x) * (x - rPos.x)
					+ (y - rPos.y) * (y - rPos.y)
					+ (z - rPos.z) * (z - rPos.z));
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

	@Override
	public void lighting(boolean lighting) {
		drawableLine.lighting(lighting);
	}

	@Override
	public boolean draw() { return drawableLine.draw; }

	@Override
	public boolean visible() { return drawableLine.visible; }
}
