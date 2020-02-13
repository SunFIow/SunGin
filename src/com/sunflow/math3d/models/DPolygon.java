package com.sunflow.math3d.models;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import com.sunflow.game.Game3D;
import com.sunflow.math3d.Calculator;
import com.sunflow.math3d.Plane;
import com.sunflow.math3d.Vertex3F;

public class DPolygon extends BaseModel {

	public Vertex3F[] vertices = new Vertex3F[0];

	private Polygon p;

	public float dist;

	public boolean visible;
	public boolean draw;
	public boolean highlight;
	public boolean seeThrough;

	private float lighting = 1;

	private Game3D game;

	public DPolygon(Game3D game, Vertex3F... vs) {
		this(game, 0, 0, 0, vs);
	}

	public DPolygon(Game3D game, float x, float y, float z, Vertex3F... vs) {
		this(new Vertex3F(x, y, z), game, vs);
	}

	public DPolygon(Vertex3F pos, Game3D game, Vertex3F... vs) {
		this.game = game;
		this.pos = pos;
		p = new Polygon();

		visible = true;
		draw = true;
		highlight = false;
		addVertices(vs);
	}

	protected void addVertices(Vertex3F... vs) {
		Vertex3F[] newVertices = new Vertex3F[vertices.length + vs.length];
		for (int i = 0; i < vertices.length; i++) {
			newVertices[i] = vertices[i];
		}
		for (int i = 0; i < vs.length; i++) {
			newVertices[vertices.length + i] = vs[i];
		}
		vertices = newVertices;
	}

	@Override
	public void project() {
		int[] xP = new int[vertices.length];
		int[] yP = new int[vertices.length];

		for (int i = 0; i < vertices.length; i++) {
			Vertex3F v = vertices[i];
			float scale = fl / (fl + parent.pos.z + pos.z + v.z);

			v.x2D = (parent.pos.x + pos.x + v.x) * scale;
			v.y2D = (parent.pos.y + pos.y + v.y) * scale;
			xP[i] = v.x2D();
			yP[i] = v.y2D();
		}

		p.reset();
		for (int i = 0; i < xP.length; i++) {
			p.xpoints[i] = xP[i];
			p.ypoints[i] = yP[i];
		}
		p.npoints = xP.length;

		dist = getDistToP();

		calcLighting();

		needsUpdate = false;
	}

	public void updatePolygon() {
		draw = true;
		for (int i = 0; i < vertices.length; i++) {
			Vertex3F v = vertices[i];
			float[] calcPos;
			float newX = 0, newY = 0;
			calcPos = Calculator.CalculatePositionP(game.vCameraPos, game.vCameraDir, v.x, v.y, v.z);
			newX = (game.width / 2 - Calculator.calcFocusPos[0]) + calcPos[0] * game.zoom;
			newY = (game.height / 2 - Calculator.calcFocusPos[1]) + calcPos[1] * game.zoom;
			if (Calculator.t < 0) draw = false;
			v.x2D = newX;
			v.y2D = newY;
		}
		dist = getDistToP();

		calcLighting();

		p.reset();
		for (int i = 0; i < vertices.length; i++) {
			Vertex3F v = vertices[i];
			p.xpoints[i] = v.x2D();
			p.ypoints[i] = v.y2D();
		}
		p.npoints = vertices.length;
		needsUpdate = false;
	}

	private void calcLighting() {
		Plane lightingPlane = new Plane(this);
		float angle = (float) Math.acos(((lightingPlane.NV.x * game.vLightDir.x) + (lightingPlane.NV.y * game.vLightDir.y) + (lightingPlane.NV.z * game.vLightDir.z)) / (game.vLightDir.mag()));
//		float angle = Math.acos(Vertex3F.dot(lightingPlane.NV, screen.vLightDir)/ (screen.vLightDir.mag()));

		lighting = (float) (0.2 + 1 - Math.sqrt(Math.toDegrees(angle) / 180));

		if (lighting > 1)
			lighting = 1;
		if (lighting < 0)
			lighting = 0;
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
		needsUpdate = true;
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
		needsUpdate = true;
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
		needsUpdate = true;
	}

	@Override
	public void render(Graphics2D g, boolean drawFill, Color fill, boolean drawOutline, Color outline) {
		if (!drawFill && !drawOutline) return;
		if (drawFill) {
//				g.setColor(fill);
			g.setColor(new Color((int) (fill.getRed() * lighting), (int) (fill.getGreen() * lighting), (int) (fill.getBlue() * lighting)));

			if (!seeThrough) g.fillPolygon(p);
		}
		if (drawOutline) {
//				g.setColor(outline);
			g.setColor(new Color((int) (outline.getRed() * lighting), (int) (outline.getGreen() * lighting), (int) (outline.getBlue() * lighting)));
			g.drawPolygon(p);
		}
//			if (screen.polygonOver == this) {
		if (highlight) {
			g.setColor(new Color(255, 255, 0, 35));
			g.fillPolygon(p);
		}
	}

	public float getDistToP() {
		float total = 0;
		for (Vertex3F v : vertices) total += Vertex3F.add(v, parent.pos).dist(game.vCameraPos);
		return total / vertices.length;
	}

	public boolean contains(float x, float y) { return p.contains(x, y); }
}
