package com.sunflow.math3d.models;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import com.sunflow.game.Game3D;
import com.sunflow.math3d.Calculator;
import com.sunflow.math3d.Plane;
import com.sunflow.math3d.Vertex3D;

public class DPolygon extends BaseModel {

	public Vertex3D[] vertices = new Vertex3D[0];

	private Polygon p;

	public double dist;

	public boolean visible;
	public boolean draw;
	public boolean highlight;

	private double lighting = 1;

	private Game3D game;

	public DPolygon(Game3D game, Vertex3D... vs) {
		this(game, 0, 0, 0, vs);
	}

	public DPolygon(Game3D game, double x, double y, double z, Vertex3D... vs) {
		this(new Vertex3D(x, y, z), game, vs);
	}

	public DPolygon(Vertex3D pos, Game3D game, Vertex3D... vs) {
		this.game = game;
		this.pos = pos;
		p = new Polygon();

		visible = true;
		draw = true;
		highlight = false;
		addVertices(vs);
	}

	protected void addVertices(Vertex3D... vs) {
		Vertex3D[] newVertices = new Vertex3D[vertices.length + vs.length];
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
			Vertex3D v = vertices[i];
			double scale = fl / (fl + parent.pos.z + pos.z + v.z);

			v.x2D = (parent.pos.x + pos.x + v.x) * scale;
			v.y2D = (parent.pos.y + pos.y + v.y) * scale;
			xP[i] = v.x2D();
			yP[i] = v.y2D();
		}

		p.reset();
		p.xpoints = xP;
		p.ypoints = yP;
		p.npoints = xP.length;

		dist = getDistToP();

		needsUpdate = false;
	}

	public void updatePolygon() {
//		Log.err("DPolygon#updatePolygon");
		draw = true;
		for (int i = 0; i < vertices.length; i++) {
			Vertex3D v = vertices[i];
			double[] calcPos;
			double newX = 0, newY = 0;
			calcPos = Calculator.CalculatePositionP(game.vCameraPos, game.vCameraDir, v.x, v.y, v.z);
			newX = (game.width() / 2 - Calculator.calcFocusPos[0]) + calcPos[0] * game.zoom;
			newY = (game.height() / 2 - Calculator.calcFocusPos[1]) + calcPos[1] * game.zoom;
			if (Calculator.t < 0)
				draw = false;
			v.x2D = newX;
			v.y2D = newY;
		}
		dist = getDistToP();
//		calcLighting();
		needsUpdate = false;
	}

	@SuppressWarnings("unused")
	private void calcLighting() {
		Plane lightingPlane = new Plane(this);
		double angle = Math.acos(((lightingPlane.NV.x * game.vLightDir.x) + (lightingPlane.NV.y * game.vLightDir.y) + (lightingPlane.NV.z * game.vLightDir.z)) / (game.vLightDir.mag()));
//		double angle = Math.acos(Vertex3D.dot(lightingPlane.NV, screen.vLightDir)/ (screen.vLightDir.mag()));

		lighting = 0.2 + 1 - Math.sqrt(Math.toDegrees(angle) / 180);

		if (lighting > 1)
			lighting = 1;
		if (lighting < 0)
			lighting = 0;
	}

	@Override
	public void rotateX(double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);

		for (Vertex3D v : vertices) {
			double newY = v.y * cos - v.z * sin;
			double newZ = v.z * cos + v.y * sin;
			v.y = newY;
			v.z = newZ;
		}
		needsUpdate = true;
	}

	@Override
	public void rotateY(double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);

		for (Vertex3D v : vertices) {
			double newX = v.x * cos - v.z * sin;
			double newZ = v.z * cos + v.x * sin;
			v.x = newX;
			v.z = newZ;
		}
		needsUpdate = true;
	}

	@Override
	public void rotateZ(double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);

		for (Vertex3D v : vertices) {
			double newX = v.x * cos - v.y * sin;
			double newY = v.y * cos + v.x * sin;
			v.x = newX;
			v.y = newY;
		}
		needsUpdate = true;
	}

	@Override
	public void render(Graphics2D g, boolean drawFill, Color fill, boolean drawOutline, Color outline) {
		if (drawFill || drawOutline) {
			if (drawFill) {
//				g.setColor(fill);
				g.setColor(new Color((int) (fill.getRed() * lighting), (int) (fill.getGreen() * lighting), (int) (fill.getBlue() * lighting)));

				g.fillPolygon(p);
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
	}

	public double getDistToP() {
		double total = 0;

		for (Vertex3D v : vertices) {
			total += Vertex3D.add(v, parent.pos).dist(game.vCameraPos);
		}

		return total / vertices.length;
	}

	public boolean isOver(int x, int y) {
		return p.contains(x, y);
	}
}
