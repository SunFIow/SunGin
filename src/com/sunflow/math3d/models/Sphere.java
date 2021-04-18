package com.sunflow.math3d.models;

import com.sunflow.game.Game3D;
import com.sunflow.math.SVector;
import com.sunflow.util.MathUtils;
import com.sunflow.util.SConstants;

public class Sphere extends Base3DModel {

	public Sphere(Game3D game, float x, float y, float z, float radius) {
		this(game, x, y, z, radius, 25);
	}

	public Sphere(Game3D game, float x, float y, float z, float radius, int detail) {
		super(x, y, z);
		int add = 1;
		SVector[][] globe = new SVector[detail + add][detail + add];

		for (int i = 0; i < detail + add; i++) {
			float lat = MathUtils.instance.map(i, 0, detail, 0, SConstants.PI);
			for (int j = 0; j < detail + add; j++) {
				float lon = MathUtils.instance.map(j, 0, detail, 0, SConstants.PI * 2);
				float vx = radius * sin(lat) * cos(lon);
				float vy = radius * sin(lat) * sin(lon);
				float vz = radius * cos(lat);
				globe[i][j] = new SVector(vx, vy, vz);
			}
		}

		for (int i = 0; i < detail; i++) {
			for (int j = 0; j < detail; j++) {

				DPolygon pol1 = new DPolygon(game);
				DPolygon pol2 = new DPolygon(game);

				SVector v1 = globe[i][j];
				SVector v2 = globe[i + 1][j];
				SVector v3 = globe[i][j + 1];
				SVector v4 = globe[i + 1][j + 1];

				pol1.addVertices(v1, v2, v3);
				pol2.addVertices(v4, v3, v2);

				addPolygone(pol1);
				addPolygone(pol2);
			}
		}
	}
}
