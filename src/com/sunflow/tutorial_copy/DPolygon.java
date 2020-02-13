package com.sunflow.tutorial_copy;

import java.awt.Color;

public class DPolygon extends IModel {
	private Color c;
	public float[] x, y, z;
	public boolean draw = true, seeThrough = false;
	private float[] calcPos, newX, newY;
	public PolygonObject drawablePolygon;
	public float avgDist;

	private IModel parent;

	public void setParent(IModel parent) { this.parent = parent; }

	private TutorialGame3D screen;

	public DPolygon(TutorialGame3D screen, float[] x, float[] y, float[] z, Color c, boolean seeThrough) {
		this.screen = screen;

		this.x = x;
		this.y = y;
		this.z = z;
		this.c = c;
		this.seeThrough = seeThrough;
		createPolygon();
	}

	private void createPolygon() {
		drawablePolygon = new PolygonObject(screen, new float[x.length], new float[x.length], c, seeThrough);
	}

	@Override
	public void updatePolygon() {
		newX = new float[x.length];
		newY = new float[x.length];
		draw = true;
		for (int i = 0; i < x.length; i++) {
			calcPos = Calculator.CalculatePositionP(screen.vCameraPos, screen.vCameraDir, x[i], y[i], z[i]);
			newX[i] = (screen.width / 2 - Calculator.calcFocusPos[0]) + calcPos[0] * TutorialGame3D.zoom;
			newY[i] = (screen.height / 2 - Calculator.calcFocusPos[1]) + calcPos[1] * TutorialGame3D.zoom;
			if (Calculator.t < 0) draw = false;
		}

		calcLighting();

		drawablePolygon.draw = draw;
		drawablePolygon.updatePolygon(newX, newY);
		avgDist = GetDist(screen.vCameraPos.x, screen.vCameraPos.y, screen.vCameraPos.z);
	}

	private void calcLighting() {
		Plane lightingPlane = new Plane(this);
		float angle = (float) Math.acos(((lightingPlane.NV.x * screen.vLightDir.x) + (lightingPlane.NV.y * screen.vLightDir.y) + (lightingPlane.NV.z * screen.vLightDir.z)) / (screen.vLightDir.mag()));
//		float angle = Math.acos(Vertex3D.dot(lightingPlane.NV, screen.vLightDir)/ (screen.vLightDir.mag()));

		drawablePolygon.lighting = 0.2f + 1 - (float) Math.sqrt(Math.toDegrees(angle) / 180);

		if (drawablePolygon.lighting > 1) drawablePolygon.lighting = 1;
		if (drawablePolygon.lighting < 0) drawablePolygon.lighting = 0;
	}

	private float GetDist(float x, float y, float z) {
		float total = 0;
		for (int i = 0; i < this.x.length; i++)
			total += Math.sqrt((x - this.x[i]) * (x - this.x[i]) +
					(y - this.y[i]) * (y - this.y[i]) +
					(z - this.z[i]) * (z - this.z[i]));
		return total / this.x.length;
	}

	@Override
	public void rotateX(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (int i = 0; i < x.length; i++) {
			float currentY = y[i] - parent.y;
			float currentZ = z[i] - parent.z;
			float newY = currentY * cos - currentZ * sin;
			float newZ = currentZ * cos + currentY * sin;
			y[i] = newY + parent.y;
			z[i] = newZ + parent.z;
		}

	}

	@Override
	public void rotateY(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (int i = 0; i < x.length; i++) {
			float currentX = x[i] - parent.x;
			float currentZ = z[i] - parent.z;
			float newX = currentX * cos - currentZ * sin;
			float newZ = currentZ * cos + currentX * sin;
			x[i] = newX + parent.x;
			z[i] = newZ + parent.z;
		}

	}

	@Override
	public void rotateZ(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (int i = 0; i < x.length; i++) {
			float currentX = x[i] - parent.x;
			float currentY = y[i] - parent.y;
			float newX = currentX * cos - currentY * sin;
			float newY = currentY * cos + currentX * sin;
			x[i] = newX + parent.x;
			y[i] = newY + parent.y;
		}
	}
}
