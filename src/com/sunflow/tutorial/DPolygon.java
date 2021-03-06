package com.sunflow.tutorial;

import com.sunflow.math.SVector;

public class DPolygon extends IModel {

//	public float[] x, y, z;

//	private Color c;

//	public boolean draw = true;
//	private float[] calcPos, newX, newY;

	public PolygonObject drawablePolygon;
	public float avgDist;

	public SVector[] vertices = new SVector[0];

	private TutorialGame3D screen;

	public DPolygon(TutorialGame3D screen, SVector... vs) {
		this(screen, 0, 0, 0, vs);
	}

	public DPolygon(TutorialGame3D screen, float x, float y, float z, SVector... vs) {
		this(new SVector(x, y, z), screen, vs);
	}

	public DPolygon(SVector pos, TutorialGame3D screen, SVector... vs) {
		addVertices(vs);

		this.screen = screen;
		this.pos = pos;
		this.drawablePolygon = new PolygonObject(screen, new float[vertices.length], new float[vertices.length]);
	}

	protected void addVertices(SVector... vs) {
		SVector[] newVertices = new SVector[vertices.length + vs.length];
		for (int i = 0; i < vertices.length; i++) newVertices[i] = vertices[i];
		for (int i = vertices.length; i < vertices.length + vs.length; i++) newVertices[i] = vs[i].clone();
		vertices = newVertices;
	}

	@Override
	public void updatePolygon() {
		float[] newX = new float[vertices.length];
		float[] newY = new float[vertices.length];
		boolean draw = true;
		for (int i = 0; i < vertices.length; i++) {
			SVector v = vertices[i];
			float x = parent.pos.x + pos.x + v.x;
			float y = parent.pos.y + pos.y + v.y;
			float z = parent.pos.z + pos.z + v.z;
			float[] calcPos = Calculator.CalculatePositionP(screen.vCameraPos, screen.vCameraDir, x, y, z);
			newX[i] = (screen.width / 2 - Calculator.calcFocusPos[0]) + calcPos[0] * screen.zoom();
			newY[i] = (screen.height / 2 - Calculator.calcFocusPos[1]) + calcPos[1] * screen.zoom();
			if (Calculator.t < 0) draw = false;
		}

		calcLighting();

		drawablePolygon.draw = draw;
		drawablePolygon.updatePolygon(newX, newY);
		avgDist = GetDist(screen.vCameraPos.x, screen.vCameraPos.y, screen.vCameraPos.z);
	}

	private void calcLighting() {
		Plane lightingPlane = new Plane(this);
		float angle = (float) Math.acos(((lightingPlane.NV.x * screen.vLightDir.x) + (lightingPlane.NV.y * screen.vLightDir.y) + (lightingPlane.NV.z * screen.vLightDir.z)) / (screen.vLightDir.mag()));
//		float angle = Math.acos(Vertex3D.dot(lightingPlane.NV, screen.vLightDir)/ (screen.vLightDir.mag()));

		drawablePolygon.lighting = 0.2f + 1 - (float) Math.sqrt(Math.toDegrees(angle) / 180);

		if (drawablePolygon.lighting > 1) drawablePolygon.lighting = 1;
		if (drawablePolygon.lighting < 0) drawablePolygon.lighting = 0;
	}

	private float GetDist(float x, float y, float z) {
		float total = 0;
		for (int i = 0; i < vertices.length; i++) {
			SVector v = vertices[i];
			float _x = parent.pos.x + pos.x + v.x;
			float _y = parent.pos.y + pos.y + v.y;
			float _z = parent.pos.z + pos.z + v.z;
			total += Math.sqrt((x - _x) * (x - _x)
					+ (y - _y) * (y - _y)
					+ (z - _z) * (z - _z));
		}
		return total / vertices.length;
	}

	@Override
	public void rotateX(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (SVector v : vertices) {
			float newY = v.y * cos - v.z * sin;
			float newZ = v.z * cos + v.y * sin;
			v.y = newY;
			v.z = newZ;
		}
//		needsUpdate = true;
	}

	@Override
	public void rotateY(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (SVector v : vertices) {
			float newX = v.x * cos - v.z * sin;
			float newZ = v.z * cos + v.x * sin;
			v.x = newX;
			v.z = newZ;
		}
//		needsUpdate = true;
	}

	@Override
	public void rotateZ(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (SVector v : vertices) {
			float newX = v.x * cos - v.y * sin;
			float newY = v.y * cos + v.x * sin;
			v.x = newX;
			v.y = newY;
		}
//		needsUpdate = true;
	}
}
