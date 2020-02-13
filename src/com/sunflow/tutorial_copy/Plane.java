package com.sunflow.tutorial_copy;

import com.sunflow.math3d.Vertex3F;

public class Plane {
	public Vertex3F V1, V2, NV;
	public Vertex3F P = new Vertex3F();

	public Plane(DPolygon DP) {
		P.x = DP.vertices[0].x;
		P.y = DP.vertices[0].y;
		P.z = DP.vertices[0].z;

		V1 = new Vertex3F(DP.vertices[1].x - DP.vertices[0].x,
				DP.vertices[1].y - DP.vertices[0].y,
				DP.vertices[1].z - DP.vertices[0].z).normalized();

		V2 = new Vertex3F(DP.vertices[2].x - DP.vertices[0].x,
				DP.vertices[2].y - DP.vertices[0].y,
				DP.vertices[2].z - DP.vertices[0].z).normalized();

		NV = Vertex3F.cross(V1, V2).normalized();
	}

	public Plane(Vertex3F V1, Vertex3F V2, Vertex3F P) {
		this.P = P;
		this.V1 = V1;
		this.V2 = V2;
		NV = Vertex3F.cross(V1, V2).normalized();
	}
}
