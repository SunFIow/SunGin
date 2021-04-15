package com.sunflow.tutorial;

import com.sunflow.math.SVector;
import com.sunflow.util.SConstants;

public class Sphere extends IModel {

	public Sphere(TutorialGame3D game, float x, float y, float z, float radius) {
		this(game, x, y, z, radius, 25);
	}

	public Sphere(TutorialGame3D game, float x, float y, float z, float radius, int detail) {
		super(x, y, z);
		int add = 1;
		SVector[][] globe = new SVector[detail + add][detail + add];

		for (int i = 0; i < detail + add; i++) {
			float lat = map(i, 0, detail, 0, SConstants.PI);
			for (int j = 0; j < detail + add; j++) {
				float lon = map(j, 0, detail, 0, SConstants.PI * 2);
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

				pol1.addVertices(v1.clone(), v2.clone(), v3.clone());
				pol2.addVertices(v2.clone(), v3.clone(), v4.clone());

				addPolygone(pol1);
				addPolygone(pol2);
			}
		}
	}

}
