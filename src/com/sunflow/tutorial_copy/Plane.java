package com.sunflow.tutorial_copy;

import com.sunflow.math3d.Vertex3F;

public class Plane {
	public Vertex3F V1, V2, NV;
	public Vertex3F P = new Vertex3F();

	public Plane(DPolygon DP) {
		P.x = DP.x[0];
		P.y = DP.y[0];
		P.z = DP.z[0];

		V1 = new Vertex3F(DP.x[1] - DP.x[0],
				DP.y[1] - DP.y[0],
				DP.z[1] - DP.z[0]).normalized();

		V2 = new Vertex3F(DP.x[2] - DP.x[0],
				DP.y[2] - DP.y[0],
				DP.z[2] - DP.z[0]).normalized();

		NV = Vertex3F.cross(V1, V2).normalized();
	}

	public Plane(Vertex3F V1, Vertex3F V2, Vertex3F P) {
		this.P = P;
		this.V1 = V1;
		this.V2 = V2;
		NV = Vertex3F.cross(V1, V2).normalized();
	}
}
