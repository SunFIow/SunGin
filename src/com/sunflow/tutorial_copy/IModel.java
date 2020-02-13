package com.sunflow.tutorial_copy;

import com.sunflow.util.MathUtils;

public abstract class IModel implements MathUtils {
	protected float x, y, z;
	protected DPolygon[] polys = new DPolygon[0];

	public abstract void updatePolygon();

//	@Override
	public void rotateX(float angle) {
		for (DPolygon pol : polys) pol.rotateX(angle);
//		needsUpdate = true;
	}

//	@Override
	public void rotateY(float angle) {
		for (DPolygon pol : polys) pol.rotateY(angle);
//		needsUpdate = true;
	}

//	@Override
	public void rotateZ(float angle) {
		for (DPolygon pol : polys) pol.rotateZ(angle);
//		needsUpdate = true;
	}

	public void rotateX1(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (DPolygon pol : polys) {
			for (int i = 0; i < pol.x.length; i++) {
				float newY = (pol.y[i] - y) * cos - (pol.z[i] - z) * sin;
				float newZ = (pol.z[i] - z) * cos + (pol.y[i] - y) * sin;
				pol.y[i] = newY + y;
				pol.z[i] = newZ + z;
			}
		}
	}

	public void rotateY1(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (DPolygon pol : polys) {
			for (int i = 0; i < pol.x.length; i++) {

				float newX = (pol.x[i] - x) * cos - (pol.z[i] - z) * sin;
				float newZ = (pol.z[i] - z) * cos + (pol.x[i] - x) * sin;
				pol.x[i] = newX + x;
				pol.z[i] = newZ + z;
			}
		}
	}

	public void rotateZ1(float angle) {
		float cos = cos(angle);
		float sin = sin(angle);

		for (DPolygon pol : polys) {
			for (int i = 0; i < pol.x.length; i++) {
				float newX = (pol.x[i] - x) * cos - (pol.y[i] - y) * sin;
				float newY = (pol.y[i] - y) * cos + (pol.x[i] - x) * sin;
				pol.x[i] = newX + x;
				pol.y[i] = newY + y;
			}
		}
	}
}
