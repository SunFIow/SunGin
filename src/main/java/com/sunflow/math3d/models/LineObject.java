package com.sunflow.math3d.models;

import java.awt.Color;

import com.sunflow.game.Game3D;
import com.sunflow.util.GeometryUtils;

public class LineObject extends DrawableObject implements GeometryUtils {

	private float x1, y1, x2, y2;

	protected boolean renderStroke = true;

	public Color stroke = new Color(10, 10, 10);

	public float strokeWeight = 2;

	public LineObject(Game3D screen, float x1, float y1, float x2, float y2) {
		super(screen);

		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	public void update(float x1, float y1, float x2, float y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	@Override
	public void render() {
		if (!draw || !visible) return;
		float r = stroke.getRed();
		float g = stroke.getGreen();
		float b = stroke.getBlue();
		float a = seeThrough ? 100 : 255;

		if (useLighting) {
			r *= lighting;
			g *= lighting;
			b *= lighting;
		}

		if (renderStroke) {
			screen.stroke(r, g, b, a);
			screen.strokeWeight(strokeWeight);
			screen.line(x1, y1, x2, y2);
		}

		if (highlight) {
			screen.stroke(255, 255, 255, 100);
			screen.strokeWeight(strokeWeight);
			screen.line(x1, y1, x2, y2);
		}
	}

	@Override
	public boolean contains(float x, float y) {
//		return this.x == x && this.y == y;
		return distLinePoint(x1, y1, x2, y2, x, y) < strokeWeight &&
				hitBoxPoint(x1, y1, x2, y2, x, y);
	}

	public void stroke(Color stroke) {
		this.stroke = stroke;
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
}
