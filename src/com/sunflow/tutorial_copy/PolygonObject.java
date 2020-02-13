package com.sunflow.tutorial_copy;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

public class PolygonObject {
	private Polygon P;
//	private Color c;
	public boolean draw = true, visible = true, seeThrough, highlight;
	public float lighting = 1;

	private TutorialGame3D screen;

	protected Color fill = Color.magenta, outline = Color.black;
	protected boolean drawFill, drawOutline;

	public PolygonObject(TutorialGame3D screen, float[] x, float[] y) {
		this.screen = screen;

		P = new Polygon();
		for (int i = 0; i < x.length; i++) P.addPoint((int) x[i], (int) y[i]);
	}

	public void updatePolygon(float[] x, float[] y) {
		P.reset();
		for (int i = 0; i < x.length; i++) {
			P.xpoints[i] = (int) x[i];
			P.ypoints[i] = (int) y[i];
		}
		P.npoints = x.length;
	}

	public void drawPolygon(Graphics g) {
		if (!draw || !visible) return;
		g.setColor(new Color((int) (fill.getRed() * lighting), (int) (fill.getGreen() * lighting), (int) (fill.getBlue() * lighting)));

		if (seeThrough) g.drawPolygon(P);
		else g.fillPolygon(P);

		if (screen.outlines) {
			g.setColor(new Color(0, 0, 0));
			g.drawPolygon(P);
		}

		if (highlight) {
//		if (screen.PolygonOver == this) {
			g.setColor(new Color(255, 255, 255, 100));
			g.fillPolygon(P);
		}
	}

	public boolean MouseOver() { return P.contains(screen.width / 2f, screen.height / 2f); }
}
