package com.sunflow.math3d.models;

import java.awt.Color;
import java.awt.Polygon;

import com.sunflow.game.Game3D;
import com.sunflow.util.MathUtils;

public class PolygonObject extends DrawableObject {

	private Polygon P;

	public Color fill = Color.magenta;
	public Color stroke = new Color(10, 10, 10);
	protected boolean renderFill = true, renderStroke = true;

	public float strokeWeight = 1;

	public PolygonObject(Game3D screen, float[] x, float[] y) {
		super(screen);

		P = new Polygon();
		for (int i = 0; i < x.length; i++) P.addPoint((int) x[i], (int) y[i]);
	}

	public PolygonObject(Game3D screen, Polygon p) {
		super(screen);
		P = p;
	}

	public void update(int[] x, int[] y) {
		P.invalidate();
		P.xpoints = x;
		P.ypoints = y;
		P.npoints = MathUtils.instance.min(x.length, y.length);
	}

	public void update(Polygon p) {
		if (P.npoints < p.npoints) {
			P.reset();
			for (int i = 0; i < p.npoints; i++) {
				P.addPoint(p.xpoints[i], p.ypoints[i]);
			}
		} else {
			P.reset();
			for (int i = 0; i < p.npoints; i++) {
				P.xpoints[i] = p.xpoints[i];
				P.ypoints[i] = p.ypoints[i];
			}
			P.npoints = p.npoints;
		}
	}

	@Override
	public void render() {
		if (!draw || !visible) return;
		float r = fill.getRed();
		float g = fill.getGreen();
		float b = fill.getBlue();
		float a = seeThrough ? 100 : 255;

		if (useLighting) {
			r *= lighting;
			g *= lighting;
			b *= lighting;
		}

		if (renderFill) {
			screen.fill(r, g, b, a);
			screen.fillShape(P);
		} else {
			screen.stroke(r, g, b, a);
			screen.strokeWeight(strokeWeight);
			screen.strokeShape(P);
		}

		if (renderStroke) {
			screen.stroke(stroke.getRed(), stroke.getGreen(), stroke.getBlue());
			screen.strokeWeight(strokeWeight);
			screen.strokeShape(P);
		}

		if (highlight) {
			screen.fill(255, 255, 255, 100);
			screen.fillShape(P);
		}
	}

	@Override
	public boolean contains(float x, float y) { return P.contains(x, y); }

	public void fill(Color fill) {
		this.fill = fill;
	}

	public void stroke(Color stroke) {
		this.stroke = stroke;
	}

	public void renderFill(boolean renderFill) {
		this.renderFill = renderFill;
	}

	public void renderStroke(boolean renderStroke) {
		this.renderStroke = renderStroke;
	}

	public void strokeWeight(float strokeWeight) {
		this.strokeWeight = strokeWeight;
	}

	public void highlight(boolean highlight) {
		this.highlight = highlight;
	}

	public void seeThrough(boolean seeThrough) {
		this.seeThrough = seeThrough;
	}

	// If seeThrough and renderStroke only renders a black stroke instead of colored and black
	// no improvment observed

//	public void render() {
//		if (!draw || !visible) return;
//		
//		if (seeThrough) {
//			if (renderStroke) {
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
//			if (renderStroke) {
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
}
