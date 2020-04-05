package com.sunflow.math3d;

import com.sunflow.game.Game3D;
import com.sunflow.math.SVector;

public class Calculator {
	public static float t = 0;
	private static SVector w1, w2, viewVector, rotVector, dirVector, planeVector1, planeVector2;
	private static Plane plane;
	public static float[] calcFocusPos = new float[2];

	public static float[] CalculatePositionP(SVector viewFrom, SVector viewTo, SVector pos) {
		return CalculatePositionP(viewFrom, viewTo, pos.x, pos.y, pos.z);
	}

	public static float[] CalculatePositionP(SVector viewFrom, SVector viewTo, float x, float y, float z) {
		float[] projP = getProj(viewFrom, viewTo, x, y, z, plane);
		float[] drawP = getDrawP(projP[0], projP[1], projP[2]);
		return drawP;
	}

	private static float[] getProj(SVector viewFrom, SVector viewTo, float x, float y, float z, Plane p) {
		SVector ViewToPoint = new SVector(x - viewFrom.x, y - viewFrom.y, z - viewFrom.z).normalized();

		t = (p.NV.x * p.P.x + p.NV.y * p.P.y + p.NV.z * p.P.z
				- (p.NV.x * viewFrom.x + p.NV.y * viewFrom.y + p.NV.z * viewFrom.z))
				/ (p.NV.x * ViewToPoint.x + p.NV.y * ViewToPoint.y + p.NV.z * ViewToPoint.z);

		x = viewFrom.x + ViewToPoint.x * t;
		y = viewFrom.y + ViewToPoint.y * t;
		z = viewFrom.z + ViewToPoint.z * t;

		return new float[] { x, y, z };
	}

	private static float[] getDrawP(float x, float y, float z) {
		float DrawX = w2.x * x + w2.y * y + w2.z * z;
		float DrawY = w1.x * x + w1.y * y + w1.z * z;
		return new float[] { DrawX, DrawY };
	}

	private static SVector getRotationVector(SVector viewFrom, SVector viewTo) {
		float dx = Math.abs(viewFrom.x - viewTo.x);
		float dy = Math.abs(viewFrom.y - viewTo.y);
		float xRot, yRot;
		xRot = dy / (dx + dy);
		yRot = dx / (dx + dy);

		if (viewFrom.x < viewTo.x) yRot = -yRot;
		if (viewFrom.y > viewTo.y) xRot = -xRot;

		SVector V = new SVector(xRot, yRot, 0).normalized();
		return V;
	}

	public static void SetPrederterminedInfo(Game3D screen) {
		viewVector = new SVector(screen.vCameraDir.x - screen.vCameraPos.x, screen.vCameraDir.y - screen.vCameraPos.y, screen.vCameraDir.z - screen.vCameraPos.z).normalized();
		dirVector = new SVector(1, 1, 1).normalized();
		planeVector1 = SVector.cross(viewVector, dirVector).normalized();
		planeVector2 = SVector.cross(viewVector, planeVector1).normalized();
		plane = new Plane(planeVector1, planeVector2, screen.vCameraDir);

		rotVector = Calculator.getRotationVector(screen.vCameraPos, screen.vCameraDir);
		w1 = SVector.cross(viewVector, rotVector).normalized();
		w2 = SVector.cross(viewVector, w1).normalized();

		calcFocusPos = Calculator.CalculatePositionP(screen.vCameraPos, screen.vCameraDir, screen.vCameraDir.x, screen.vCameraDir.y, screen.vCameraDir.z);
		calcFocusPos[0] = screen.zoom() * calcFocusPos[0];
		calcFocusPos[1] = screen.zoom() * calcFocusPos[1];

//		Log.info(rotVector);
	}
}
