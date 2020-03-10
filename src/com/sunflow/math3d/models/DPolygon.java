package com.sunflow.math3d.models;

import java.awt.Color;

import com.sunflow.game.Game3D;
import com.sunflow.math3d.Plane;
import com.sunflow.math3d.Vertex3F;

public class DPolygon extends BaseModel {

	public Vertex3F[] vertices = new Vertex3F[0];

	protected float dist;

	protected Game3D screen;

	protected PolygonObject drawablePolygon;

	public DPolygon(Game3D screen, Vertex3F... vs) {
		this(screen, 0, 0, 0, vs);
	}

	public DPolygon(Game3D screen, float x, float y, float z, Vertex3F... vs) {
		this(new Vertex3F(x, y, z), screen, vs);
	}

	public DPolygon(Vertex3F pos, Game3D screen, Vertex3F... vs) {
		this.screen = screen;
		this.pos = pos;
		this.drawablePolygon = new PolygonObject(screen, new float[vs.length], new float[vs.length]);

		addVertices(vs);
	}

	protected void addVertices(Vertex3F... vs) {
		Vertex3F[] newVertices = new Vertex3F[vertices.length + vs.length];
		for (int i = 0; i < vertices.length; i++) {
			newVertices[i] = vertices[i];
		}
		for (int i = 0; i < vs.length; i++) {
			newVertices[vertices.length + i] = vs[i].clone();
		}
		vertices = newVertices;
	}

	@Override
	public void updateModel() {
		float[] newX = new float[vertices.length];
		float[] newY = new float[vertices.length];
		boolean draw = true;
		for (int i = 0; i < vertices.length; i++) {
			Vertex3F v = vertices[i];

//			float x = parent.pos.x + pos.x + v.x;
//			float y = parent.pos.y + pos.y + v.y;
//			float z = parent.pos.z + pos.z + v.z;

			Vertex3F aPos = absolutePosition();
			float x = aPos.x + v.x;
			float y = aPos.y + v.y;
			float z = aPos.z + v.z;

//			float x = parent.pos.x + pos.x + v.x;
//			float y = parent.pos.y + pos.y + v.y;
//			float z = parent.pos.z + pos.z + v.z;
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

		drawablePolygon.draw = draw;
		drawablePolygon.update(newX, newY);
		dist = getDistToP(screen.vCameraPos.x, screen.vCameraPos.y, screen.vCameraPos.z);
		needsUpdate = false;
	}

	private void calcLighting() {
		Plane lightingPlane = new Plane(this);
		Vertex3F NV = lightingPlane.NV;
		float angle = (float) Math.acos(((NV.x * screen.vLightDir.x) + (NV.y * screen.vLightDir.y) + (NV.z * screen.vLightDir.z)) / (screen.vLightDir.mag()));
//		float angle = Math.acos(Vertex3F.dot(lightingPlane.NV, screen.vLightDir)/ (screen.vLightDir.mag()));

		float lighting = (float) (0.2 + 1 - Math.sqrt(Math.toDegrees(angle) / 180));

		if (lighting > 1) lighting = 1;
		if (lighting < 0) lighting = 0;

		drawablePolygon.lighting = lighting;
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
		markDirty();
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
		markDirty();
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
		markDirty();
	}

	@Override
	public void rotateX(float angle, Vertex3F origin) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (Vertex3F v : vertices) {
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
	public void rotateY(float angle, Vertex3F origin) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (Vertex3F v : vertices) {
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
	public void rotateZ(float angle, Vertex3F origin) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (Vertex3F v : vertices) {
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

	public float getDistToP(Vertex3F p) { return getDistToP(p.x, p.y, p.z); }

	public float getDistToP(float x, float y, float z) {
		float total = 0;
		for (int i = 0; i < vertices.length; i++) {
			Vertex3F v = vertices[i];

//			float _x = parent.pos.x + pos.x + v.x;
//			float _y = parent.pos.y + pos.y + v.y;
//			float _z = parent.pos.z + pos.z + v.z;

			Vertex3F aPos = absolutePosition();
			aPos.add(v);
			Vertex3F rPos = screen.apply(aPos);

			total += Math.sqrt((x - rPos.x) * (x - rPos.x)
					+ (y - rPos.y) * (y - rPos.y)
					+ (z - rPos.z) * (z - rPos.z));
		}
		return total / vertices.length;
	}

	@Override
	public float dist() { return dist; }

	@Override
	public boolean contains(float x, float y) { return drawablePolygon.contains(x, y); }

	public void render(boolean renderFill, Color fill, boolean renderStroke, Color stroke, boolean highlight, boolean seeThrough) {
		drawablePolygon.renderFill(renderFill);
		drawablePolygon.fill(fill);
		drawablePolygon.renderStroke(renderStroke);
		drawablePolygon.stroke(stroke);
		drawablePolygon.highlight(highlight);
		drawablePolygon.seeThrough(seeThrough);
		render();
	}

	@Override
	public void render() { drawablePolygon.render(); }

	public void fill(Color fill) { drawablePolygon.fill(fill); }

	public void stroke(Color stroke) { drawablePolygon.stroke(stroke); }

	public void strokeWeight(float strokeWeight) { drawablePolygon.strokeWeight(strokeWeight); }

	public void renderFill(boolean renderFill) { drawablePolygon.renderFill(renderFill); }

	public void renderStroke(boolean renderStroke) { drawablePolygon.renderStroke(renderStroke); }

	@Override
	public void highlight(boolean highlight) { drawablePolygon.highlight(highlight); }

	@Override
	public void seeThrough(boolean seeThrough) { drawablePolygon.seeThrough(seeThrough); }

	@Override
	public void lighting(boolean lighting) {
		drawablePolygon.lighting(lighting);
	}

	@Override
	public boolean draw() { return drawablePolygon.draw; }

	@Override
	public boolean visible() { return drawablePolygon.visible; }

}
