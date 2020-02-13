package com.sunflow.tutorial;

import java.awt.Color;

import com.sunflow.util.MathUtils;

public class Cube extends IModel implements MathUtils {
	private double x, y, z, width, length, height, rotation = Math.PI * 0.75;
	private double[] rotAdd = new double[4];
	@SuppressWarnings("unused")
	private Color c;
	private double x1, x2, x3, x4, y1, y2, y3, y4;
	private double[] angle;

//	private Game3D screen;

	public Cube(TutorialGame3D screen, double x, double y, double z, double width, double length, double height, Color c) {
//		this.screen = screen;
		polys = new DPolygon[6];

		polys[0] = new DPolygon(screen, new double[] { x, x + width, x + width, x }, new double[] { y, y, y + length, y + length }, new double[] { z, z, z, z }, c, false);
		polys[1] = new DPolygon(screen, new double[] { x, x + width, x + width, x }, new double[] { y, y, y + length, y + length }, new double[] { z + height, z + height, z + height, z + height }, c, false);
		polys[2] = new DPolygon(screen, new double[] { x, x, x + width, x + width }, new double[] { y, y, y, y }, new double[] { z, z + height, z + height, z }, c, false);
		polys[3] = new DPolygon(screen, new double[] { x + width, x + width, x + width, x + width }, new double[] { y, y, y + length, y + length }, new double[] { z, z + height, z + height, z }, c, false);
		polys[4] = new DPolygon(screen, new double[] { x, x, x + width, x + width }, new double[] { y + length, y + length, y + length, y + length }, new double[] { z, z + height, z + height, z }, c, false);
		polys[5] = new DPolygon(screen, new double[] { x, x, x, x }, new double[] { y, y, y + length, y + length }, new double[] { z, z + height, z + height, z }, c, false);

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
		double radius = Math.sqrt(dot(width, length));

		x1 = x + width * 0.5 + radius * 0.5 * Math.cos(rotation + rotAdd[0]);
		x2 = x + width * 0.5 + radius * 0.5 * Math.cos(rotation + rotAdd[1]);
		x3 = x + width * 0.5 + radius * 0.5 * Math.cos(rotation + rotAdd[2]);
		x4 = x + width * 0.5 + radius * 0.5 * Math.cos(rotation + rotAdd[3]);

		y1 = y + length * 0.5 + radius * 0.5 * Math.sin(rotation + rotAdd[0]);
		y2 = y + length * 0.5 + radius * 0.5 * Math.sin(rotation + rotAdd[1]);
		y3 = y + length * 0.5 + radius * 0.5 * Math.sin(rotation + rotAdd[2]);
		y4 = y + length * 0.5 + radius * 0.5 * Math.sin(rotation + rotAdd[3]);

		polys[0].x = new double[] { x1, x2, x3, x4 };
		polys[0].y = new double[] { y1, y2, y3, y4 };
		polys[0].z = new double[] { z, z, z, z };

		polys[1].x = new double[] { x4, x3, x2, x1 };
		polys[1].y = new double[] { y4, y3, y2, y1 };
		polys[1].z = new double[] { z + height, z + height, z + height, z + height };

		polys[2].x = new double[] { x1, x1, x2, x2 };
		polys[2].y = new double[] { y1, y1, y2, y2 };
		polys[2].z = new double[] { z, z + height, z + height, z };

		polys[3].x = new double[] { x2, x2, x3, x3 };
		polys[3].y = new double[] { y2, y2, y3, y3 };
		polys[3].z = new double[] { z, z + height, z + height, z };

		polys[4].x = new double[] { x3, x3, x4, x4 };
		polys[4].y = new double[] { y3, y3, y4, y4 };
		polys[4].z = new double[] { z, z + height, z + height, z };

		polys[5].x = new double[] { x4, x4, x1, x1 };
		polys[5].y = new double[] { y4, y4, y1, y1 };
		polys[5].z = new double[] { z, z + height, z + height, z };
	}

	@Override
	public void rotate(double angle) { rotation += angle; }

	private void setRotAdd() {
		angle = new double[4];

		double xdif = -width / 2 + 0.00001;
		double ydif = -length / 2 + 0.00001;

		angle[0] = Math.atan(ydif / xdif);

		if (xdif < 0)
			angle[0] += Math.PI;

		xdif = width / 2 + 0.00001;
		ydif = -length / 2 + 0.00001;

		angle[1] = Math.atan(ydif / xdif);

		if (xdif < 0)
			angle[1] += Math.PI;

		xdif = width / 2 + 0.00001;
		ydif = length / 2 + 0.00001;

		angle[2] = Math.atan(ydif / xdif);

		if (xdif < 0)
			angle[2] += Math.PI;

		xdif = -width / 2 + 0.00001;
		ydif = length / 2 + 0.00001;

		angle[3] = Math.atan(ydif / xdif);

		if (xdif < 0)
			angle[3] += Math.PI;

		rotAdd[0] = angle[0] + 0.25 * Math.PI;
		rotAdd[1] = angle[1] + 0.25 * Math.PI;
		rotAdd[2] = angle[2] + 0.25 * Math.PI;
		rotAdd[3] = angle[3] + 0.25 * Math.PI;

	}

	@SuppressWarnings("unused")
	private void updateDirection(double toX, double toY) {
		double xdif = toX - (x + width / 2) + 0.00001;
		double ydif = toY - (y + length / 2) + 0.00001;

		double angle = Math.atan(ydif / xdif) + 0.75 * Math.PI;

		if (xdif < 0)
			angle += Math.PI;

		rotation = angle;
		updatePoly();
	}
}
