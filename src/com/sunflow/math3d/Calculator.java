package com.sunflow.math3d;

import com.sunflow.game.Game3D;

public class Calculator {
	public static double t = 0;
	private static Vertex3D w1, w2, viewVector, rotVector, dirVector, planeVector1, planeVector2;
	private static Plane plane;
	public static double[] calcFocusPos = new double[2];

	public static double[] CalculatePositionP(Vertex3D viewFrom, Vertex3D viewTo, double x, double y, double z) {
		double[] projP = getProj(viewFrom, viewTo, x, y, z, plane);
		double[] drawP = getDrawP(projP[0], projP[1], projP[2]);
		return drawP;
	}

	private static double[] getProj(Vertex3D viewFrom, Vertex3D viewTo, double x, double y, double z, Plane p) {
		Vertex3D ViewToPoint = new Vertex3D(x - viewFrom.x, y - viewFrom.y, z - viewFrom.z).normalized();

		t = (p.NV.x * p.P.x + p.NV.y * p.P.y + p.NV.z * p.P.z
				- (p.NV.x * viewFrom.x + p.NV.y * viewFrom.y + p.NV.z * viewFrom.z))
				/ (p.NV.x * ViewToPoint.x + p.NV.y * ViewToPoint.y + p.NV.z * ViewToPoint.z);

		x = viewFrom.x + ViewToPoint.x * t;
		y = viewFrom.y + ViewToPoint.y * t;
		z = viewFrom.z + ViewToPoint.z * t;

		return new double[] { x, y, z };
	}

	private static double[] getDrawP(double x, double y, double z) {
		double DrawX = w2.x * x + w2.y * y + w2.z * z;
		double DrawY = w1.x * x + w1.y * y + w1.z * z;

//		Log.info(new Vertex3D(x, y, z));
//		Log.info(DrawX + " | " + DrawY);

		return new double[] { DrawX, DrawY };
	}

	private static Vertex3D getRotationVector(Vertex3D viewFrom, Vertex3D viewTo) {
		double dx = Math.abs(viewFrom.x - viewTo.x);
		double dy = Math.abs(viewFrom.y - viewTo.y);
		double xRot, yRot;
		if (dx != 0 || dy != 0) {
			xRot = dy / (dx + dy);
			yRot = dx / (dx + dy);
		} else {
			xRot = yRot = 0;
		}

		if (viewFrom.y >= viewTo.y)
			xRot = -xRot;
		if (viewFrom.x <= viewTo.x)
			yRot = -yRot;

		Vertex3D v = new Vertex3D(xRot, yRot, 0).normalized();
		return v;
	}

	public static void SetPrederterminedInfo(Game3D game) {
		viewVector = new Vertex3D(game.vCameraDir.x - game.vCameraPos.x, game.vCameraDir.y - game.vCameraPos.y, game.vCameraDir.z - game.vCameraPos.z).normalized();
		dirVector = new Vertex3D(1, 1, 1).normalized();
		planeVector1 = Vertex3D.cross(viewVector, dirVector).normalized();
		planeVector2 = Vertex3D.cross(viewVector, planeVector1).normalized();

		plane = new Plane(planeVector1, planeVector2, game.vCameraDir);

		rotVector = Calculator.getRotationVector(game.vCameraPos, game.vCameraDir);
		w1 = Vertex3D.cross(viewVector, rotVector).normalized();
		w2 = Vertex3D.cross(viewVector, w1).normalized();

		calcFocusPos = Calculator.CalculatePositionP(game.vCameraPos, game.vCameraDir, game.vCameraDir.x, game.vCameraDir.y, game.vCameraDir.z);
		calcFocusPos[0] = game.zoom * calcFocusPos[0];
		calcFocusPos[1] = game.zoom * calcFocusPos[1];
	}
}
