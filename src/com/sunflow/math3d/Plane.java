package com.sunflow.math3d;

import com.sunflow.math3d.models.DPolygon;

public class Plane {
//	public VectorMy V1, V2, NV;
	public Vertex3D V1, V2, NV;
	public Vertex3D P = new Vertex3D();

	public Plane(DPolygon DP) {
		P.x = DP.vertices[0].x;
		P.y = DP.vertices[0].y;
		P.z = DP.vertices[0].z;

		V1 = new Vertex3D(DP.vertices[1].x - DP.vertices[0].x,
				DP.vertices[1].y - DP.vertices[0].y,
				DP.vertices[1].z - DP.vertices[0].z).normalized();

		V2 = new Vertex3D(DP.vertices[2].x - DP.vertices[0].x,
				DP.vertices[2].y - DP.vertices[0].y,
				DP.vertices[2].z - DP.vertices[0].z).normalized();

		NV = Vertex3D.cross(V1, V2).normalized();
	}

	public Plane(Vertex3D V1, Vertex3D V2, Vertex3D P) {
		this.P = P;
		this.V1 = V1;
		this.V2 = V2;
		NV = Vertex3D.cross(V1, V2).normalized();
	}
}
