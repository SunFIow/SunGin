package com.sunflow.math3d;

import com.sunflow.math.SVector;
import com.sunflow.math3d.models.DPolygon;

public class Plane {
	public SVector V1, V2, NV;
	public SVector P;

	public Plane(SVector V1, SVector V2, SVector P) {
		this.P = P;
		this.V1 = V1;
		this.V2 = V2;
		this.NV = SVector.cross(V1, V2).normalized();
	}

	public Plane(SVector V1, SVector V2) {
		this.V1 = V1.normalized();
		this.V2 = V2.normalized();
		this.NV = SVector.cross(V1, V2).normalized();
	}

	public Plane(DPolygon DP) {
		P = new SVector(DP.vertices[0].x, DP.vertices[0].y, DP.vertices[0].z);

		V1 = new SVector(DP.vertices[1].x - DP.vertices[0].x,
				DP.vertices[1].y - DP.vertices[0].y,
				DP.vertices[1].z - DP.vertices[0].z).normalized();

		V2 = new SVector(DP.vertices[2].x - DP.vertices[0].x,
				DP.vertices[2].y - DP.vertices[0].y,
				DP.vertices[2].z - DP.vertices[0].z).normalized();

		NV = SVector.cross(V1, V2).normalized();
	}
}
