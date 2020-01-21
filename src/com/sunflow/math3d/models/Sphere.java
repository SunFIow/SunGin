package com.sunflow.math3d.models;

import com.sunflow.game.Game3D;
import com.sunflow.math3d.Vertex3D;

public class Sphere extends Base3DModel {

	public Sphere(Game3D game, double x, double y, double z, double radius) {
		this(game, x, y, z, radius, 25);
	}

	public Sphere(Game3D game, double x, double y, double z, double radius, int detail) {
		super(x, y, z);
		int add = 1;
		Vertex3D[][] globe = new Vertex3D[detail + add][detail + add];

		for (int i = 0; i < detail + add; i++) {
			double lat = map(i, 0, detail, 0, Math.PI);
			for (int j = 0; j < detail + add; j++) {
				double lon = map(j, 0, detail, 0, Math.PI * 2);
				double vx = radius * Math.sin(lat) * Math.cos(lon);
				double vy = radius * Math.sin(lat) * Math.sin(lon);
				double vz = radius * Math.cos(lat);
				globe[i][j] = new Vertex3D(vx, vy, vz);
			}
		}

		for (int i = 0; i < detail; i++) {
			for (int j = 0; j < detail; j++) {

				DPolygon pol1 = new DPolygon(game);
				DPolygon pol2 = new DPolygon(game);

				Vertex3D v1 = globe[i][j];
				Vertex3D v2 = globe[i + 1][j];
				Vertex3D v3 = globe[i][j + 1];
				Vertex3D v4 = globe[i + 1][j + 1];

				pol1.addVertices(v1.clone(), v2.clone(), v3.clone());
				pol2.addVertices(v2.clone(), v3.clone(), v4.clone());

				addPolygone(pol1);
				addPolygone(pol2);
			}
		}
	}
}
