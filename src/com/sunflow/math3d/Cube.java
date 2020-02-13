package com.sunflow.math3d;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sunflow.game.Game3D;
import com.sunflow.util.MathUtils;

public class Cube extends IModel implements MathUtils {
	private float x, y, z, width, length, height, rotation = (float) (Math.PI * 0.75);
	private float[] rotAdd = new float[4];
	@SuppressWarnings("unused")
	private Color c;
	private float x1, x2, x3, x4, y1, y2, y3, y4;
	private float[] angle;

//	private Game3D screen;

	public Cube(Game3D screen, float x, float y, float z, float width, float length, float height, Color c) {
//		this.screen = screen;
		polys = new DPolygon[6];

		polys[0] = new DPolygon(screen, new float[] { x, x + width, x + width, x }, new float[] { y, y, y + length, y + length }, new float[] { z, z, z, z }, c, false);
		polys[1] = new DPolygon(screen, new float[] { x, x + width, x + width, x }, new float[] { y, y, y + length, y + length }, new float[] { z + height, z + height, z + height, z + height }, c, false);
		polys[2] = new DPolygon(screen, new float[] { x, x, x + width, x + width }, new float[] { y, y, y, y }, new float[] { z, z + height, z + height, z }, c, false);
		polys[3] = new DPolygon(screen, new float[] { x + width, x + width, x + width, x + width }, new float[] { y, y, y + length, y + length }, new float[] { z, z + height, z + height, z }, c, false);
		polys[4] = new DPolygon(screen, new float[] { x, x, x + width, x + width }, new float[] { y + length, y + length, y + length, y + length }, new float[] { z, z + height, z + height, z }, c, false);
		polys[5] = new DPolygon(screen, new float[] { x, x, x, x }, new float[] { y, y, y + length, y + length }, new float[] { z, z + height, z + height, z }, c, false);

		this.c = c;
		this.x = x;
		this.y = y;
		this.z = z;
		this.width = width;
		this.length = length;
		this.height = height;

		setRotAdd();
		updatePoly();

//		screen.Models.add(this);
	}

	@Override
	public void updatePolygon() {
		for (DPolygon poly : polys) poly.updatePolygon();
		updatePoly();
	}

	private void updatePoly() {
		float radius = (float) Math.sqrt(dot(width, length));

		x1 = x + width * 0.5f + radius * 0.5f * (float) Math.cos(rotation + rotAdd[0]);
		x2 = x + width * 0.5f + radius * 0.5f * (float) Math.cos(rotation + rotAdd[1]);
		x3 = x + width * 0.5f + radius * 0.5f * (float) Math.cos(rotation + rotAdd[2]);
		x4 = x + width * 0.5f + radius * 0.5f * (float) Math.cos(rotation + rotAdd[3]);

		y1 = y + length * 0.5f + radius * 0.5f * (float) Math.sin(rotation + rotAdd[0]);
		y2 = y + length * 0.5f + radius * 0.5f * (float) Math.sin(rotation + rotAdd[1]);
		y3 = y + length * 0.5f + radius * 0.5f * (float) Math.sin(rotation + rotAdd[2]);
		y4 = y + length * 0.5f + radius * 0.5f * (float) Math.sin(rotation + rotAdd[3]);

		polys[0].x = new float[] { x1, x2, x3, x4 };
		polys[0].y = new float[] { y1, y2, y3, y4 };
		polys[0].z = new float[] { z, z, z, z };

		polys[1].x = new float[] { x4, x3, x2, x1 };
		polys[1].y = new float[] { y4, y3, y2, y1 };
		polys[1].z = new float[] { z + height, z + height, z + height, z + height };

		polys[2].x = new float[] { x1, x1, x2, x2 };
		polys[2].y = new float[] { y1, y1, y2, y2 };
		polys[2].z = new float[] { z, z + height, z + height, z };

		polys[3].x = new float[] { x2, x2, x3, x3 };
		polys[3].y = new float[] { y2, y2, y3, y3 };
		polys[3].z = new float[] { z, z + height, z + height, z };

		polys[4].x = new float[] { x3, x3, x4, x4 };
		polys[4].y = new float[] { y3, y3, y4, y4 };
		polys[4].z = new float[] { z, z + height, z + height, z };

		polys[5].x = new float[] { x4, x4, x1, x1 };
		polys[5].y = new float[] { y4, y4, y1, y1 };
		polys[5].z = new float[] { z, z + height, z + height, z };
	}

	@Override
	public void rotate(float angle) { rotation += angle; }

	private void setRotAdd() {
		angle = new float[4];

		float xdif = -width / 2 + 0.00001f;
		float ydif = -length / 2 + 0.00001f;

		angle[0] = (float) Math.atan(ydif / xdif);

		if (xdif < 0) angle[0] += (float) Math.PI;

		xdif = width / 2 + 0.00001f;
		ydif = -length / 2 + 0.00001f;

		angle[1] = (float) Math.atan(ydif / xdif);

		if (xdif < 0) angle[1] += (float) Math.PI;

		xdif = width / 2 + 0.00001f;
		ydif = length / 2 + 0.00001f;

		angle[2] = (float) Math.atan(ydif / xdif);

		if (xdif < 0) angle[2] += (float) Math.PI;

		xdif = -width / 2 + 0.00001f;
		ydif = length / 2 + 0.00001f;

		angle[3] = (float) Math.atan(ydif / xdif);

		if (xdif < 0) angle[3] += (float) Math.PI;

		rotAdd[0] = angle[0] + 0.25f * (float) Math.PI;
		rotAdd[1] = angle[1] + 0.25f * (float) Math.PI;
		rotAdd[2] = angle[2] + 0.25f * (float) Math.PI;
		rotAdd[3] = angle[3] + 0.25f * (float) Math.PI;

	}

	@SuppressWarnings("unused")
	private void updateDirection(float toX, float toY) {
		float xdif = toX - (x + width / 2) + 0.00001f;
		float ydif = toY - (y + length / 2) + 0.00001f;

		float angle = (float) Math.atan(ydif / xdif) + 0.75f * (float) Math.PI;

		if (xdif < 0) angle += (float) Math.PI;

		rotation = angle;
		updatePoly();
	}

	public void draw(Graphics2D g) {
		for (DPolygon poly : polys) poly.drawablePolygon.drawPolygon(g);
	}
}
