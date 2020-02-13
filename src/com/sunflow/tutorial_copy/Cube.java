package com.sunflow.tutorial_copy;

import java.awt.Color;

public class Cube extends IModel {
	private float width, length, height, rotation = (float) (Math.PI * 0.75);
	@SuppressWarnings("unused")
	private Color c;

//	private Game3D screen;

	public Cube(TutorialGame3D screen, float x, float y, float z, float width, float length, float height, Color c) {
//		this.screen = screen;
		this.c = c;
		this.x = x;
		this.y = y;
		this.z = z;
		this.width = width;
		this.length = length;
		this.height = height;

		this.polys = new DPolygon[6];

		float x1 = x - width / 2, x2 = x + width / 2;
		float y1 = y - length / 2, y2 = y + length / 2;
		float z1 = z - height / 2, z2 = z + height / 2;

		polys[0] = new DPolygon(screen, new float[] { x1, x2, x2, x1 }, new float[] { y1, y1, y2, y2 }, new float[] { z1, z1, z1, z1 }, c, false);
		polys[1] = new DPolygon(screen, new float[] { x1, x2, x2, x1 }, new float[] { y1, y1, y2, y2 }, new float[] { z2, z2, z2, z2 }, c, false);
		polys[2] = new DPolygon(screen, new float[] { x1, x1, x2, x2 }, new float[] { y1, y1, y1, y1 }, new float[] { z1, z2, z2, z1 }, c, false);
		polys[3] = new DPolygon(screen, new float[] { x2, x2, x2, x2 }, new float[] { y1, y1, y2, y2 }, new float[] { z1, z2, z2, z1 }, c, false);
		polys[4] = new DPolygon(screen, new float[] { x1, x1, x2, x2 }, new float[] { y2, y2, y2, y2 }, new float[] { z1, z2, z2, z1 }, c, false);
		polys[5] = new DPolygon(screen, new float[] { x1, x1, x1, x1 }, new float[] { y1, y1, y2, y2 }, new float[] { z1, z2, z2, z1 }, c, false);

		for (DPolygon pol : polys) pol.setParent(this);
	}

	@Override
	public void updatePolygon() { for (DPolygon poly : polys) poly.updatePolygon(); }

	@SuppressWarnings("unused")
	private void updateDirection(float toX, float toY) {
		float xdif = toX - (x + width / 2) + 0.00001f;
		float ydif = toY - (y + length / 2) + 0.00001f;

		float angle = (float) Math.atan(ydif / xdif) + 0.75f * (float) Math.PI;

		if (xdif < 0) angle += (float) Math.PI;

		rotation = angle;
	}
}
