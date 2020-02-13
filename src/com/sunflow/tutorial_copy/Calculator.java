package com.sunflow.tutorial_copy;

import com.sunflow.math3d.Vertex3F;

public class Calculator {
	public static float t = 0;
	private static Vertex3F w1, w2, viewVector, rotVector, dirVector, planeVector1, planeVector2;
	private static Plane plane;
	public static float[] calcFocusPos = new float[2];

	public static float[] CalculatePositionP(Vertex3F viewFrom, Vertex3F viewTo, float x, float y, float z) {
		float[] projP = getProj(viewFrom, viewTo, x, y, z, plane);
		float[] drawP = getDrawP(projP[0], projP[1], projP[2]);
		return drawP;
	}

	private static float[] getProj(Vertex3F viewFrom, Vertex3F viewTo, float x, float y, float z, Plane p) {
		Vertex3F ViewToPoint = new Vertex3F(x - viewFrom.x, y - viewFrom.y, z - viewFrom.z).normalized();

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

	private static Vertex3F getRotationVector(Vertex3F viewFrom, Vertex3F viewTo) {
		float dx = Math.abs(viewFrom.x - viewTo.x);
		float dy = Math.abs(viewFrom.y - viewTo.y);
		float xRot, yRot;
		xRot = dy / (dx + dy);
		yRot = dx / (dx + dy);

		if (viewFrom.y > viewTo.y)
			xRot = -xRot;
		if (viewFrom.x < viewTo.x)
			yRot = -yRot;

		Vertex3F V = new Vertex3F(xRot, yRot, 0).normalized();
		return V;
	}

	public static void SetPrederterminedInfo(TutorialGame3D screen) {
		viewVector = new Vertex3F(screen.vCameraDir.x - screen.vCameraPos.x, screen.vCameraDir.y - screen.vCameraPos.y, screen.vCameraDir.z - screen.vCameraPos.z).normalized();
		dirVector = new Vertex3F(1, 1, 1).normalized();
		planeVector1 = Vertex3F.cross(viewVector, dirVector).normalized();
		planeVector2 = Vertex3F.cross(viewVector, planeVector1).normalized();
		plane = new Plane(planeVector1, planeVector2, screen.vCameraDir);

		rotVector = Calculator.getRotationVector(screen.vCameraPos, screen.vCameraDir);
		w1 = Vertex3F.cross(viewVector, rotVector).normalized();
		w2 = Vertex3F.cross(viewVector, w1).normalized();

		calcFocusPos = Calculator.CalculatePositionP(screen.vCameraPos, screen.vCameraDir, screen.vCameraDir.x, screen.vCameraDir.y, screen.vCameraDir.z);
		calcFocusPos[0] = TutorialGame3D.zoom * calcFocusPos[0];
		calcFocusPos[1] = TutorialGame3D.zoom * calcFocusPos[1];

//		Log.info(rotVector);
	}
}
