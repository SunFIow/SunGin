package com.sunflow.math3d.models;

import java.awt.Color;
import java.awt.Polygon;

import com.sunflow.game.Game3D;

public class PolygonObject {

	private Polygon P;

	public boolean draw = true, visible = true, seeThrough, highlight;
	public float lighting = 1;

	public Color fill = Color.magenta;
	public Color outline = new Color(10, 10, 10);
	protected boolean renderFill = true, renderOutline = true;

	private Game3D screen;

	public PolygonObject(Game3D screen, float[] x, float[] y) {
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

	public void render() {
		if (!draw || !visible) return;

		if (seeThrough) {
			screen.stroke((fill.getRed() * lighting), (fill.getGreen() * lighting), (fill.getBlue() * lighting));
			screen.strokeShape(P);
		} else {
			screen.fill((fill.getRed() * lighting), (fill.getGreen() * lighting), (fill.getBlue() * lighting));
			screen.fillShape(P);
		}

		if (renderOutline) {
			screen.stroke(0, 0, 0);
			screen.strokeShape(P);
		}

		if (highlight) {
			screen.fill(255, 255, 255, 100);
			screen.fillShape(P);
		}
	}

	// If seeThrough and renderOutline only renders a black stroke instead of colored and black
	// no improvment observed

//	public void render() {
//		if (!draw || !visible) return;
//		
//		if (seeThrough) {
//			if (renderOutline) {
//				screen.stroke(0, 0, 0);
//				screen.strokeShape(P);
//			} else {
//				screen.stroke((fill.getRed() * lighting), (fill.getGreen() * lighting), (fill.getBlue() * lighting));
//				screen.strokeShape(P);
//			}
//
//			if (highlight) {
//				screen.fill(255, 255, 255, 100);
//				screen.fillShape(P);
//			}
//		} else {
//			if (renderOutline) {
//				screen.fill((fill.getRed() * lighting), (fill.getGreen() * lighting), (fill.getBlue() * lighting));
//				screen.fillShape(P);
//				screen.stroke(0, 0, 0);
//				screen.strokeShape(P);
//			} else {
//				screen.fill((fill.getRed() * lighting), (fill.getGreen() * lighting), (fill.getBlue() * lighting));
//				screen.fillShape(P);
//			}
//
//			if (highlight) {
//				screen.fill(255, 255, 255, 100);
//				screen.fillShape(P);
//			}
//		}
//	}

	public boolean contains(float x, float y) { return P.contains(x, y); }

	public void fill(Color fill) {
		this.fill = fill;
	}

	public void outline(Color outline) {
		this.outline = outline;
	}

	public void renderFill(boolean renderFill) {
		this.renderFill = renderFill;
	}

	public void renderOutline(boolean renderOutline) {
		this.renderOutline = renderOutline;
	}

	public void highlight(boolean highlight) {
		this.highlight = highlight;
	}

	public void seeThrough(boolean seeThrough) {
		this.seeThrough = seeThrough;
	}
}
