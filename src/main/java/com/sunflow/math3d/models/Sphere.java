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
		this(game, x, y, z, radius, detail, detail);
	}

	public Sphere(Game3D game, float x, float y, float z, float radius, int detailX, int detailY) {
		super(x, y, z);

		SVector[][] globe = createGlobe(radius, detailX, detailY);

		SVector[][] tris = createTris(globe);

		for (int i = 0; i < tris.length; i++) {
			DPolygon pol = new DPolygon(game);
			SVector[] tri = tris[i];
			pol.addVertices(tri[0], tri[1], tri[2]);
			addPolygone(pol);
		}
	}

	public static SVector[][] createGlobe(float radius, int detail) { return createGlobe(radius, detail, detail); }

	public static SVector[][] createGlobe(float radius, int detailX, int detailY) {
		SVector[][] globe = new SVector[detailX + 1][detailY + 1];

		for (int i = 0; i < detailX + 1; i++) {
			float lat = MathUtils.instance.map(i, 0, detailX, 0, SConstants.PI);
			for (int j = 0; j < detailY + 1; j++) {
				float lon = MathUtils.instance.map(j, 0, detailY, 0, SConstants.PI * 2);
				float vx = radius * sin(lat) * cos(lon);
				float vy = radius * sin(lat) * sin(lon);
				float vz = radius * cos(lat);
				globe[i][j] = new SVector(vx, vy, vz);
			}
		}
		return globe;
	}

	public static SVector[][] createTris(SVector[][] globe) {
		int detailX = globe.length - 1;
		int detailY = globe[0].length - 1;
		SVector[][] tris = new SVector[detailX * detailY * 2][3];
		int k = 0;
		for (int i = 0; i < detailX; i++) {
			for (int j = 0; j < detailY; j++) {

				SVector v1 = globe[i][j];
				SVector v2 = globe[i + 1][j];
				SVector v3 = globe[i][j + 1];
				SVector v4 = globe[i + 1][j + 1];

				tris[k++] = new SVector[] { v1.clone(), v2.clone(), v3.clone() };
				tris[k++] = new SVector[] { v4.clone(), v3.clone(), v2.clone() };
			}
		}
		return tris;
	}
}
