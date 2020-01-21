package com.sunflow.math1;

import com.sunflow.math3d.Vertex3D;

public class Plane {
//	public VectorMy V1, V2, NV;
	public Vertex3D V1, V2, NV;
	public Vertex3D P = new Vertex3D();

	public Plane(DPolygon DP) {
		P.x = DP.x[0];
		P.y = DP.y[0];
		P.z = DP.z[0];

		V1 = new Vertex3D(DP.x[1] - DP.x[0],
				DP.y[1] - DP.y[0],
				DP.z[1] - DP.z[0]).normalized();

		V2 = new Vertex3D(DP.x[2] - DP.x[0],
				DP.y[2] - DP.y[0],
				DP.z[2] - DP.z[0]).normalized();

		NV = Vertex3D.cross(V1, V2).normalized();
	}

	public Plane(Vertex3D V1, Vertex3D V2, Vertex3D P) {
		this.P = P;
		this.V1 = V1;
		this.V2 = V2;
		NV = Vertex3D.cross(V1, V2).normalized();
	}
}
