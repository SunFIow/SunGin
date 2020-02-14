package com.sunflow.math3d.models;

import java.awt.Color;

import com.sunflow.game.Game3D;
import com.sunflow.math3d.Calculator;
import com.sunflow.math3d.Plane;
import com.sunflow.math3d.Vertex3F;

public class DPolygon extends BaseModel {

	public Vertex3F[] vertices = new Vertex3F[0];

	public float dist;

	private Game3D screen;

	public PolygonObject drawablePolygon;
//	private Polygon p;
//	public boolean draw, visible, seeThrough, highlight;
//	private float lighting = 1;

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

//	@Override
//	public void project() {
//		int[] xP = new int[vertices.length];
//		int[] yP = new int[vertices.length];
//
//		for (int i = 0; i < vertices.length; i++) {
//			Vertex3F v = vertices[i];
//			float scale = fl / (fl + parent.pos.z + pos.z + v.z);
//
//			v.x2D = (parent.pos.x + pos.x + v.x) * scale;
//			v.y2D = (parent.pos.y + pos.y + v.y) * scale;
//			xP[i] = v.x2D();
//			yP[i] = v.y2D();
//		}
//
//		p.reset();
//		for (int i = 0; i < xP.length; i++) {
//			p.xpoints[i] = xP[i];
//			p.ypoints[i] = yP[i];
//		}
//		p.npoints = xP.length;
//
//		dist = getDistToP();
//
//		calcLighting();
//
//		needsUpdate = false;
//	}

	@Override
	public void updateModel() {
		float[] newX = new float[vertices.length];
		float[] newY = new float[vertices.length];
		boolean draw = true;
		for (int i = 0; i < vertices.length; i++) {
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

		drawablePolygon.draw = draw;
		drawablePolygon.updatePolygon(newX, newY);
		dist = getDistToP(screen.vCameraPos.x, screen.vCameraPos.y, screen.vCameraPos.z);
//		needsUpdate = false;
	}

	private void calcLighting() {
		Plane lightingPlane = new Plane(this);
		float angle = (float) Math.acos(((lightingPlane.NV.x * screen.vLightDir.x) + (lightingPlane.NV.y * screen.vLightDir.y) + (lightingPlane.NV.z * screen.vLightDir.z)) / (screen.vLightDir.mag()));
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

	public boolean contains(float x, float y) { return drawablePolygon.contains(x, y); }

	public void render(boolean renderFill, Color fill, boolean renderOutline, Color outline, boolean highlight, boolean seeThrough) {
		drawablePolygon.renderFill(renderFill);
		drawablePolygon.fill(fill);
		drawablePolygon.renderOutline(renderOutline);
		drawablePolygon.outline(outline);
		drawablePolygon.highlight(highlight);
		drawablePolygon.seeThrough(seeThrough);
		drawablePolygon.render();
	}

	@Override
	public void render() { drawablePolygon.render(); }

	public void fill(Color fill) { drawablePolygon.fill(fill); }

	public void outline(Color outline) { drawablePolygon.outline(outline); }

	public void renderFill(boolean renderFill) { drawablePolygon.renderFill(renderFill); }

	public void renderOutline(boolean renderOutline) { drawablePolygon.renderOutline(renderOutline); }

	public void highlight(boolean highlight) { drawablePolygon.highlight(highlight); }

	public void seeThrough(boolean seeThrough) { drawablePolygon.seeThrough(seeThrough); }
}
