package com.sunflow.math3d.models;

import java.awt.Color;

import com.sunflow.game.Game3D;
import com.sunflow.util.MathUtils;

public class PointObject extends DrawableObject implements MathUtils {

	private float x, y;

	protected boolean renderStroke = true;

	public Color stroke = new Color(10, 10, 10);

	public float strokeWeight = 5;

	public PointObject(Game3D screen, float x, float y) {
		super(screen);

		this.x = x;
		this.y = y;
	}

	public void update(float x, float y) {
		this.x = x;
		this.y = y;
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
			screen.point(x, y);
		}

		if (highlight) {
			screen.stroke(255, 255, 255, 100);
			screen.strokeWeight(strokeWeight);
			screen.point(x, y);
		}
	}

	@Override
	public boolean contains(float x, float y) {
//		return this.x == x && this.y == y;
		return dist(this.x, this.y, x, y) < strokeWeight;
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
