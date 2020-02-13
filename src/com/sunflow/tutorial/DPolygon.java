package com.sunflow.tutorial;

import java.awt.Color;

public class DPolygon extends IModel {
	private Color c;
	public double[] x, y, z;
	public boolean draw = true, seeThrough = false;
	private double[] calcPos, newX, newY;
	public PolygonObject drawablePolygon;
	public double avgDist;

	private TutorialGame3D screen;

	public DPolygon(TutorialGame3D screen, double[] x, double[] y, double[] z, Color c, boolean seeThrough) {
		this.screen = screen;

		this.x = x;
		this.y = y;
		this.z = z;
		this.c = c;
		this.seeThrough = seeThrough;
		createPolygon();
	}

	private void createPolygon() {
		drawablePolygon = new PolygonObject(screen, new double[x.length], new double[x.length], c, seeThrough);
	}

	@Override
	public void updatePolygon() {
		newX = new double[x.length];
		newY = new double[x.length];
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
		avgDist = GetDist();
	}

	private void calcLighting() {
		Plane lightingPlane = new Plane(this);
		double angle = Math.acos(((lightingPlane.NV.x * screen.vLightDir.x) + (lightingPlane.NV.y * screen.vLightDir.y) + (lightingPlane.NV.z * screen.vLightDir.z)) / (screen.vLightDir.mag()));
//		double angle = Math.acos(Vertex3D.dot(lightingPlane.NV, screen.vLightDir)/ (screen.vLightDir.mag()));

		drawablePolygon.lighting = 0.2 + 1 - Math.sqrt(Math.toDegrees(angle) / 180);

		if (drawablePolygon.lighting > 1) drawablePolygon.lighting = 1;
		if (drawablePolygon.lighting < 0) drawablePolygon.lighting = 0;
	}

	private double GetDist() {
		double total = 0;
		for (int i = 0; i < x.length; i++)
			total += Math.sqrt((screen.vCameraPos.x - x[i]) * (screen.vCameraPos.x - x[i]) +
					(screen.vCameraPos.y - y[i]) * (screen.vCameraPos.y - y[i]) +
					(screen.vCameraPos.z - z[i]) * (screen.vCameraPos.z - z[i]));
		return total / x.length;
	}

	@Override
	public void rotate(double angle) {}
}
