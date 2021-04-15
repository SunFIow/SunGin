package com.sunflow.game.olc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import com.sunflow.game.GameBase;
import com.sunflow.math.SVector;
import com.sunflow.math3d.SMatrix;
import com.sunflow.util.ColorUtils;
import com.sunflow.util.SConstants;
import com.sunflow.util.MathUtils;

public class PipeLineRenderer2 implements MathUtils, SConstants {

	private float[] m_DepthBuffer, m_DepthBuffer_Local;

	private SMatrix matProj;
	private SMatrix matView;
	private SMatrix matWorld;

	private Sprite sprTexture;

	private float fViewX;
	private float fViewY;
	private float fViewW;
	private float fViewH;

	private float fNear;

	private GameBase game;

	private SVector vCamera;

	@SuppressWarnings("unused")
	private SVector vLightPosition;
	private SVector vLightDirection;
	@SuppressWarnings("unused")
	private int light_color;

	private int flags;

	public PipeLineRenderer2(GameBase game) { this.game = game; }

	public void ConfigureDisplay() {
		m_DepthBuffer = new float[game.width() * game.height()];
		m_DepthBuffer_Local = new float[game.width() * game.height()];
	}

	public void ClearDepth() {
		int length = m_DepthBuffer.length;
		for (int i = 0; i < length; i++) {
			m_DepthBuffer[i] = 0;
		}
	}

	private void ClearLocalDepth() {
		int length = m_DepthBuffer_Local.length;
		for (int i = 0; i < length; i++) {
			m_DepthBuffer_Local[i] = 0;
		}
	}

	public void SetProjection(
			float fFovDegrees, float fAspectRatio,
			float fNear, float fFar,
			float fLeft, float fTop,
			float fWidth, float fHeight) {
		matProj = SMatrix.Matrix_MakeProjection(fFovDegrees, fAspectRatio, fNear, fFar);
		fViewX = fLeft;
		fViewY = fTop;
		fViewW = fWidth;
		fViewH = fHeight;

		this.fNear = fNear;
	}

	public void SetCamera(SVector pos, SVector lookat, SVector up) {
		SMatrix matCamera = SMatrix.Matrix_PointAt(pos, lookat, up);
		matView = SMatrix.Matrix_QuickInverse(matCamera);
		vCamera = pos;
	}

	public void SetTransform(SMatrix transform) { matWorld = transform; }

	public void SetTexture(Sprite texture) { sprTexture = texture; }

	public void SetFlags(int flags) { this.flags = flags; }

	public void SetLightSource(SVector pos, SVector dir, int col) {
		vLightPosition = pos;
		vLightDirection = dir;
		light_color = col;
	}

	public SVector convert3Dto2D(SVector v) {
		SVector vTransformed = SMatrix.Matrix_MultiplyVector(matWorld, v);

		SVector vViewed = SMatrix.Matrix_MultiplyVector(matView, vTransformed);

		if (Vector_ClipAgainstPlane(vViewed, new SVector(0.0f, 0.0f, fNear), new SVector(0.0f, 0.0f, 1.0f)) == null) return null;

		SVector vProjected = SMatrix.Matrix_MultiplyVector(matProj, vViewed);
		vProjected.div(vProjected.w);
		vProjected.w = 1.0f / vProjected.w;
//		vProjected.x *= -1.0f;
//		vProjected.y *= -1.0f;
		SVector vOffsetView = new SVector(1.0f, 1.0f, 0.0f, 0.0f);
		vProjected.add(vOffsetView);
		vProjected.mult(fViewW, fViewH).mult(0.5f, 0.5f);

		if (Vector_ClipAgainstPlane(vProjected, new SVector(0.0f, 0.0f, 0.0f), new SVector(0.0f, 1.0f, 0.0f)) == null) return null;
		if (Vector_ClipAgainstPlane(vProjected, new SVector(0.0f, fViewH - 1, 0.0f), new SVector(0.0f, -1.0f, 0.0f)) == null) return null;
		if (Vector_ClipAgainstPlane(vProjected, new SVector(0.0f, 0.0f, 0.0f), new SVector(1.0f, 0.0f, 0.0f)) == null) return null;
		if (Vector_ClipAgainstPlane(vProjected, new SVector(fViewW - 1, 0.0f, 0.0f), new SVector(-1.0f, 0.0f, 0.0f)) == null) return null;

		SVector v2d = vProjected;
		return v2d;
	}

	private SVector Vector_ClipAgainstPlane(SVector vec, SVector plane_p, SVector plane_n) {
		float d0 = (plane_n.x * vec.x + plane_n.y * vec.y + plane_n.z * vec.z - SVector.dot(plane_n, plane_p));
		if (d0 < 0) return null;
		return vec;
	}

	// int flags = RENDER_CULL_CW | RENDER_TEXTURED | RENDER_DEPTH
	public int Render_New_With_error(List<Triangle> triangles) {
		return Render_New_With_error(triangles, RENDER_CULL_CW | RENDER_TEXTURED | RENDER_DEPTH);
	}

	public int Render_New_With_error(List<Triangle> triangles, int flags) {
		this.flags = flags;
		if ((flags & RENDER_DEPTH_LOCAL) == RENDER_DEPTH_LOCAL) ClearLocalDepth();

		SMatrix matProj = new SMatrix(this.matProj);
		SMatrix matWorld = new SMatrix(this.matWorld);
		SMatrix matView = new SMatrix(this.matView);
		SVector vCamera = new SVector(this.vCamera);

		float fViewW = this.fViewW;
		float fViewH = this.fViewH;
		float fViewX = this.fViewX;
		float fViewY = this.fViewY;
		float fNear = this.fNear;

		// Calculate Transformation Matrix
		SMatrix matWorldView = SMatrix.Matrix_MultiplyMatrix(matWorld, matView);
		// matWorldViewProj = MatrixF.Matrix_MultiplyMatrix(matWorldView, matProj);

		// Store triangles for rastering later
//		List<Triangle> vecTrianglesToRaster;

		int nTriangleDrawnCount = 0;

		// Process Triangles
		for (Triangle tri : triangles) {
			Triangle triTransformed;

			triTransformed = new Triangle(tri);

			// Transform Triangle from object into projected space
			triTransformed.p[0] = SMatrix.Matrix_MultiplyVector(matWorldView, tri.p[0]);
			triTransformed.p[1] = SMatrix.Matrix_MultiplyVector(matWorldView, tri.p[1]);
			triTransformed.p[2] = SMatrix.Matrix_MultiplyVector(matWorldView, tri.p[2]);

			// Calculate Triangle Normal in WorldView Space
			SVector vNormal, vLine1, vLine2;
			vLine1 = SVector.sub(triTransformed.p[1], triTransformed.p[0]);
			vLine2 = SVector.sub(triTransformed.p[2], triTransformed.p[0]);
			vNormal = SVector.cross(vLine1, vLine2);
			vNormal = vNormal.normalized();

			// Get Ray from triangle to camera
			// If Ray is not aligned with normal, then triangle is not visible
//			SVector triViewNeg = MatrixF.Matrix_MultiplyVector(MatrixF.Matrix_QuickInverse(matView), triTransformed.p[0]);
			SVector vRayToCamera = SVector.sub(vCamera, SMatrix.Matrix_MultiplyVector(matView, tri.p[0]));
//			SVector vRayToCamera = triTransformed.p[0];
//			if ((flags & RENDER_CULL_CW) == RENDER_CULL_CW && SVector.dot(vNormal, vRayToCamera) < 0.0f) continue;
//			if ((flags & RENDER_CULL_CCW) == RENDER_CULL_CW && SVector.dot(vNormal, vRayToCamera) > 0.0f) continue;
			if ((flags & RENDER_CULL_CW) == RENDER_CULL_CW && SVector.dot(vNormal, triTransformed.p[0]) > 0.0f) continue;
			if ((flags & RENDER_CULL_CCW) == RENDER_CULL_CW && SVector.dot(vNormal, triTransformed.p[0]) < 0.0f) continue;

			boolean bSunLighting = vLightDirection != null && (flags & RENDER_LIGHTING_SUNLIGHT) == RENDER_LIGHTING_SUNLIGHT;
			boolean bRimLighting = (flags & RENDER_LIGHTING_RIM) == RENDER_LIGHTING_RIM;
			boolean bFog = (flags & RENDER_FOG) == RENDER_FOG;

			boolean bLighting = bSunLighting || bRimLighting || bFog;

			if (bLighting) {
				SVector vOutputColor;
				SVector vRayToCameraNormalized = vRayToCamera.normalized();
				// Illuminate
//							int color = triTransformed.color;
//							SVector vColor = new SVector(ColorUtils.instance.red(color), ColorUtils.instance.green(color), ColorUtils.instance.blue(color));
				SVector vColor = new SVector(1.0f, 1.0f, 1.0f, 1.0f);
				float fLight = 1;

				if (bSunLighting) {
					SVector vLightDirection_neg = SVector.neg(vLightDirection);
					fLight = 0;
					// How "aligned" are light direction and triangle surface normal?
					float dp = SVector.dot(vNormal, vLightDirection_neg);
					if (dp < 0.0f) dp = 0.0f;
					float fDiffuseTerm = map(dp, 1.0f, 0.0f, 1.0f, 0.2f);
					fLight += fDiffuseTerm;

//					SVector vHalf = SVector.add(vRayToCameraNormalized, vLightDirection_neg).normalize();
//					float fSpecularTerm = SVector.dot(vNormal, vHalf);
//					if (fSpecularTerm < 0.0f) fSpecularTerm = 0.0f;
//					float a = 15.0f;
//					float c = 0.8f;
//					fSpecularTerm = (float) Math.pow(fSpecularTerm, a);
//					fLight += c * fSpecularTerm;
				}

				SVector vDiffuse = vColor.mult(fLight);

				if (bRimLighting) {
					float fRimLight = 1 - SVector.dot(vRayToCameraNormalized, vNormal);
					fRimLight = clamp(0.0f, fRimLight - 0.5f, 1.0f);
					vDiffuse.add(new SVector(0.4f, 0.4f, 1.0f, 1.0f).mult(fRimLight));
				}

				if (bFog) {
					// Add in some fog. http://youtu.be/YpKVXNPOXg8
					float fDistance = vRayToCamera.length();

					float fFogMin = 5.0f;
					float fFogMax = 20.0f;
					fDistance = clamp(fFogMin, fDistance, fFogMax);
					float fFog = map(fDistance, fFogMin, fFogMax, 0.0f, 1.0f);

					// Use that as our output color
//								vOutputColor = vFoggedDiffuse;
					vOutputColor = vDiffuse.mult((1 - fFog));
					SVector vFogColor = new SVector(194, 204, 217, 255).mult(fFog);
					triTransformed.shaderFun = (c) -> {
						SVector vInputColor = ColorUtils.instance.vColor(c);
						SVector vShaded = vInputColor.mult(vOutputColor).add(vFogColor);
						return ColorUtils.instance.color(vShaded.x, vShaded.y, vShaded.z, vShaded.w);
					};
				} else {
					vOutputColor = vDiffuse;
					triTransformed.shaderFun = (c) -> {
						SVector vInputColor = ColorUtils.instance.vColor(c);
						SVector vShaded = vInputColor.mult(vOutputColor);
						return ColorUtils.instance.color(vShaded.x, vShaded.y, vShaded.z, vShaded.w);
					};
				}
			} else triTransformed.shaderFun = (c) -> c;

			triTransformed.color = tri.color;

			// Clip triangle against near plane
//					int nClippedTriangles = 0;
			Triangle[] clipped = triTransformed.Triangle_ClipAgainstPlane(
					new SVector(0.0f, 0.0f, fNear),
					new SVector(0.0f, 0.0f, 1.0f));

			// This may yield two new triangles
			for (int n = 0; n < clipped.length; n++) {
				// Project triangle from 3D --> 2D
				Triangle triProjected = new Triangle(clipped[n]);

				// Project new triangle
				triProjected.p[0] = SMatrix.Matrix_MultiplyVector(matProj, clipped[n].p[0]);
				triProjected.p[1] = SMatrix.Matrix_MultiplyVector(matProj, clipped[n].p[1]);
				triProjected.p[2] = SMatrix.Matrix_MultiplyVector(matProj, clipped[n].p[2]);

				// Apply Projection to Tex coords
				triProjected.t[0].div(triProjected.p[0].w);
				triProjected.t[1].div(triProjected.p[1].w);
				triProjected.t[2].div(triProjected.p[2].w);

				// Apply Projection to Verts
				triProjected.p[0].div(triProjected.p[0].w);
				triProjected.p[1].div(triProjected.p[1].w);
				triProjected.p[2].div(triProjected.p[2].w);

				// Clip against viewport in screen space
				// Clip triangles against all four screen edges, this could yield
				// a bunch of triangles, so create a queue that we traverse to
				// ensure we only test new triangles generated against planes
				Triangle[] sclipped = null;
				List<Triangle> listTriangles = new ArrayList<>();

				// Add initial triangle
				listTriangles.add(triProjected);
				int nNewTriangles = 1;

				for (int p = 0; p < 4; p++) {
					while (nNewTriangles > 0) {
						// Take triangle from front of queue
						Triangle test = listTriangles.get(0);
						listTriangles.remove(0);
						nNewTriangles--;

						// Clip it against a plane. We only need to test each
						// subsequent plane, against subsequent new triangles
						// as all triangles after a plane clip are guaranteed
						// to lie on the inside of the plane. I like how this
						// comment is almost completely and utterly justified
						switch (p) {
							case 0:
								sclipped = test.Triangle_ClipAgainstPlane(new SVector(0.0f, -1.0f, 0.0f), new SVector(0.0f, 1.0f, 0.0f));
								break;
							case 1:
								sclipped = test.Triangle_ClipAgainstPlane(new SVector(0.0f, +1.0f, 0.0f), new SVector(0.0f, -1.0f, 0.0f));
								break;
							case 2:
								sclipped = test.Triangle_ClipAgainstPlane(new SVector(-1.0f, 0.0f, 0.0f), new SVector(1.0f, 0.0f, 0.0f));
								break;
							case 3:
								sclipped = test.Triangle_ClipAgainstPlane(new SVector(+1.0f, 0.0f, 0.0f), new SVector(-1.0f, 0.0f, 0.0f));
								break;
						}

						// Clipping may yield a variable number of triangles, so
						// add these new ones to the back of the queue for subsequent
						// clipping against next planes
						for (int w = 0; w < sclipped.length; w++)
							listTriangles.add(sclipped[w]);
					}
					nNewTriangles = listTriangles.size();
				}

				for (Triangle triRaster : listTriangles) {
					// Scale to viewport

//					triRaster.p[0].x *= -1.0f;
//					triRaster.p[1].x *= -1.0f;
//					triRaster.p[2].x *= -1.0f;
//					triRaster.p[0].y *= -1.0f;
//					triRaster.p[1].y *= -1.0f;
//					triRaster.p[2].y *= -1.0f;

					SVector vOffsetView = new SVector(1.0f, 1.0f, 0.0f, 0.0f);
					triRaster.p[0] = SVector.add(triRaster.p[0], vOffsetView);
					triRaster.p[1] = SVector.add(triRaster.p[1], vOffsetView);
					triRaster.p[2] = SVector.add(triRaster.p[2], vOffsetView);
					triRaster.p[0].x *= 0.5f * fViewW;
					triRaster.p[0].y *= 0.5f * fViewH;
					triRaster.p[1].x *= 0.5f * fViewW;
					triRaster.p[1].y *= 0.5f * fViewH;
					triRaster.p[2].x *= 0.5f * fViewW;
					triRaster.p[2].y *= 0.5f * fViewH;
					vOffsetView = new SVector(fViewX, fViewY, 0.0f);
					triRaster.p[0] = SVector.add(triRaster.p[0], vOffsetView);
					triRaster.p[1] = SVector.add(triRaster.p[1], vOffsetView);
					triRaster.p[2] = SVector.add(triRaster.p[2], vOffsetView);

					// For now, just draw triangle

//					if ((flags & RENDER_FLAT) == RENDER_FLAT) {
//						DrawTriangleFlat(triRaster);
//						DrawTriangleWire(triRaster);
//					}
//
//					if ((flags & RENDER_TEXTURED) == RENDER_TEXTURED) {
//						DrawTriangleTextured(
//								triRaster.p[0].x, triRaster.p[0].y, triRaster.t[0].x, triRaster.t[0].y, triRaster.t[0].w,
//								triRaster.p[1].x, triRaster.p[1].y, triRaster.t[1].x, triRaster.t[1].y, triRaster.t[1].w,
//								triRaster.p[2].x, triRaster.p[2].y, triRaster.t[2].x, triRaster.t[2].y, triRaster.t[2].w);
//					}
//
//					if ((flags & RENDER_WIRE) == RENDER_WIRE) {
//						DrawTriangleWire(triRaster, RED);
//					}

					@SuppressWarnings("unchecked")
					BiFunction<Float, Float, Integer>[] colorFun = new BiFunction[1];
					boolean drawTextured = false;

					if ((flags & RENDER_FLAT) == RENDER_FLAT) {
						colorFun[0] = (x, y) -> triRaster.color;
						drawTextured = true;
					}

					if ((flags & RENDER_TEXTURED) == RENDER_TEXTURED) {
						colorFun[0] = (x, y) -> sprTexture.SampleColor(x, y);
						drawTextured = true;
					}
					if (drawTextured) DrawTriangleTextured(
							triRaster.p[0].x, triRaster.p[0].y, triRaster.t[0].x, triRaster.t[0].y, triRaster.t[0].w,
							triRaster.p[1].x, triRaster.p[1].y, triRaster.t[1].x, triRaster.t[1].y, triRaster.t[1].w,
							triRaster.p[2].x, triRaster.p[2].y, triRaster.t[2].x, triRaster.t[2].y, triRaster.t[2].w,
							(x, y) -> triRaster.shaderFun.apply(colorFun[0].apply(x, y)));

					if ((flags & RENDER_WIRE) == RENDER_WIRE) {
						DrawTriangleWire(triRaster, RED);
					}

					nTriangleDrawnCount++;
				}
			}
		}

		return nTriangleDrawnCount;
	}

	public int Render(List<SVector> verts, List<SVector> texs, int flags) {
		List<Triangle> triangs = Triangle.toTriangs(verts, texs);
		return Render(triangs, flags);
	}

	public int Render(Triangle... triangles) {
		return Render(Arrays.asList(triangles));
	}

	public int Render(List<Triangle> triangles) {
		return Render(triangles, RENDER_CULL_CW | RENDER_TEXTURED | RENDER_DEPTH);
	}

	public int Render(int flags, Triangle... triangles) {
		return Render(Arrays.asList(triangles), flags);
	}

	public int Render(List<Triangle> triangles, int flags) {
		this.flags = flags;
		if ((flags & RENDER_DEPTH_LOCAL) == RENDER_DEPTH_LOCAL) ClearLocalDepth();

		SMatrix matProj = new SMatrix(this.matProj);
		SMatrix matWorld = new SMatrix(this.matWorld);
		SMatrix matView = new SMatrix(this.matView);
		SVector vCamera = new SVector(this.vCamera);

		float fViewW = this.fViewW;
		float fViewH = this.fViewH;
		float fNear = this.fNear;

		// Calculate Transformation Matrix
//		MatrixF matWorldView = MatrixF.Matrix_MultiplyMatrix(matWorld, matView);
//		MatrixF matWorldViewProj = MatrixF.Matrix_MultiplyMatrix(matWorldView, matProj);

		int nTriangleDrawnCount = 0;

		// Store triangles for rastering
		ArrayList<Triangle> trianglesToClip = new ArrayList<>();

		for (Triangle tri : triangles) {

			// World Matrix Transform
			Triangle triTransformed = new Triangle(tri);
			triTransformed.p[0] = SMatrix.Matrix_MultiplyVector(matWorld, tri.p[0]);
			triTransformed.p[1] = SMatrix.Matrix_MultiplyVector(matWorld, tri.p[1]);
			triTransformed.p[2] = SMatrix.Matrix_MultiplyVector(matWorld, tri.p[2]);

			// Calculate triangle Normal
			SVector vNormal, vLine1, vLine2;
			vLine1 = SVector.sub(triTransformed.p[1], triTransformed.p[0]);
			vLine2 = SVector.sub(triTransformed.p[2], triTransformed.p[0]);
			vNormal = SVector.cross(vLine1, vLine2);
			vNormal.normalize();

			// Get Ray from triangle to camera
			// If Ray is not aligned with normal, then triangle is not visible
			SVector vRayToCamera = SVector.sub(vCamera, triTransformed.p[0]);
			if ((flags & RENDER_CULL_CW) == RENDER_CULL_CW && SVector.dot(vNormal, vRayToCamera) < 0.0f) continue;
			if ((flags & RENDER_CULL_CCW) == RENDER_CULL_CW && SVector.dot(vNormal, vRayToCamera) > 0.0f) continue;

			boolean bSunLighting = vLightDirection != null && (flags & RENDER_LIGHTING_SUNLIGHT) == RENDER_LIGHTING_SUNLIGHT;
			boolean bRimLighting = (flags & RENDER_LIGHTING_RIM) == RENDER_LIGHTING_RIM;
			boolean bFog = (flags & RENDER_FOG) == RENDER_FOG;

			boolean bLighting = bSunLighting || bRimLighting || bFog;

			if (bLighting) {
				SVector vOutputColor;
				SVector vRayToCameraNormalized = vRayToCamera.normalized();
				// Illuminate
//				int color = triTransformed.color;
//				SVector vColor = new SVector(ColorUtils.instance.red(color), ColorUtils.instance.green(color), ColorUtils.instance.blue(color));
				SVector vColor = new SVector(1.0f, 1.0f, 1.0f, 1.0f);
				float fLight = 1;

				if (bSunLighting) {
					SVector vLightDirection_neg = SVector.neg(vLightDirection);
					fLight = 0;
					// How "aligned" are light direction and triangle surface normal?
					float dp = SVector.dot(vNormal, vLightDirection_neg);
					if (dp < 0.0f) dp = 0.0f;
					float fDiffuseTerm = map(dp, 1.0f, 0.0f, 1.0f, 0.2f);
					fLight += fDiffuseTerm;

//					SVector vHalf = SVector.add(vRayToCameraNormalized, vLightDirection_neg).normalize();
//					float fSpecularTerm = SVector.dot(vNormal, vHalf);
//					if (fSpecularTerm < 0.0f) fSpecularTerm = 0.0f;
//					float a = 15.0f;
//					float c = 0.8f;
//					fSpecularTerm = (float) Math.pow(fSpecularTerm, a);
//					fLight += c * fSpecularTerm;
				}

				SVector vDiffuse = vColor.mult(fLight);

				if (bRimLighting) {
					float fRimLight = 1 - SVector.dot(vRayToCameraNormalized, vNormal);
					fRimLight = clamp(0.0f, fRimLight - 0.5f, 1.0f);
					vDiffuse.add(new SVector(0.4f, 0.4f, 1.0f, 1.0f).mult(fRimLight));
				}

				if (bFog) {
					// Add in some fog. http://youtu.be/YpKVXNPOXg8
					float fDistance = vRayToCamera.length();

					float fFogMin = 5.0f;
					float fFogMax = 20.0f;
					fDistance = clamp(fFogMin, fDistance, fFogMax);
					float fFog = map(fDistance, fFogMin, fFogMax, 0.0f, 1.0f);

					// Use that as our output color
//					vOutputColor = vFoggedDiffuse;
					vOutputColor = vDiffuse.mult((1 - fFog));
					SVector vFogColor = new SVector(194, 204, 217, 255).mult(fFog);
					triTransformed.shaderFun = (c) -> {
						SVector vInputColor = ColorUtils.instance.vColor(c);
						SVector vShaded = vInputColor.mult(vOutputColor).add(vFogColor);
						return ColorUtils.instance.color(vShaded.x, vShaded.y, vShaded.z, vShaded.w);
					};
				} else {
					vOutputColor = vDiffuse;
					triTransformed.shaderFun = (c) -> {
						SVector vInputColor = ColorUtils.instance.vColor(c);
						SVector vShaded = vInputColor.mult(vOutputColor);
						return ColorUtils.instance.color(vShaded.x, vShaded.y, vShaded.z, vShaded.w);
					};
				}
			} else triTransformed.shaderFun = (c) -> c;

			triTransformed.color = tri.color;

			// Convert World Space --> View Space
			Triangle triViewed = new Triangle(triTransformed);
			triViewed.p[0] = SMatrix.Matrix_MultiplyVector(matView, triTransformed.p[0]);
			triViewed.p[1] = SMatrix.Matrix_MultiplyVector(matView, triTransformed.p[1]);
			triViewed.p[2] = SMatrix.Matrix_MultiplyVector(matView, triTransformed.p[2]);

			// Clip Viewed Triangle against near plane, this cold form two additional triangles.
//			int nClippedTriangles = 0;
			Triangle[] clipped = triViewed.Triangle_ClipAgainstPlane(
					new SVector(0.0f, 0.0f, fNear),
					new SVector(0.0f, 0.0f, 1.0f));

			for (int n = 0; n < clipped.length; n++) {
				// Project triangle from 3D --> 2D
				Triangle triProjected = new Triangle(clipped[n]);

				triProjected.p[0] = SMatrix.Matrix_MultiplyVector(matProj, clipped[n].p[0]);
				triProjected.p[1] = SMatrix.Matrix_MultiplyVector(matProj, clipped[n].p[1]);
				triProjected.p[2] = SMatrix.Matrix_MultiplyVector(matProj, clipped[n].p[2]);

				triProjected.t[0].div(triProjected.p[0].w);
				triProjected.t[1].div(triProjected.p[1].w);
				triProjected.t[2].div(triProjected.p[2].w);

				// Scale into view, we moved the normalising into cartesian space
				// out of the matrix.vector function from the previous videos, so
				// do this manually

				triProjected.p[0].div(triProjected.p[0].w);
				triProjected.p[1].div(triProjected.p[1].w);
				triProjected.p[2].div(triProjected.p[2].w);

				// X/Y are inverted so put them back
//				triProjected.p[0].x *= -1.0f;
//				triProjected.p[1].x *= -1.0f;
//				triProjected.p[2].x *= -1.0f;
//				triProjected.p[0].y *= -1.0f;
//				triProjected.p[1].y *= -1.0f;
//				triProjected.p[2].y *= -1.0f;

				SVector vOffsetView = new SVector(1.0f, 1.0f, 0.0f, 0.0f);
				triProjected.p[0].add(vOffsetView);
				triProjected.p[1].add(vOffsetView);
				triProjected.p[2].add(vOffsetView);

				triProjected.p[0].mult(fViewW, fViewH).mult(0.5f, 0.5f);
				triProjected.p[1].mult(fViewW, fViewH).mult(0.5f, 0.5f);
				triProjected.p[2].mult(fViewW, fViewH).mult(0.5f, 0.5f);

				// Store triangle for sorting
				trianglesToClip.add(triProjected);
			}
		}
		// Loop through all transformed, viewed, projected, and sorted triangles
		for (Triangle triToRaster : trianglesToClip) {
			// Clip triangles against all four screen edges, this could yield
			// a bunch of triangles, so create a queue that we traverse to
			// ensure we only test new triangles generated against planes
			ArrayList<Triangle> trianglesToRaster = new ArrayList<>();

			// Add initial triangle
			trianglesToRaster.add(triToRaster);
			int nNewTriangles = 1;

			for (int p = 0; p < 4; p++) {
				Triangle[] clipped = new Triangle[0];
				while (nNewTriangles > 0) {
					// Take triangle from front of queue
					Triangle test = trianglesToRaster.get(0);
					trianglesToRaster.remove(0);
					nNewTriangles--;

					// Clip it against a plane. We only need to test each
					// subsequent plane, against subsequent new triangles
					// as all triangles after a plane clip are guaranteed
					// to lie on the inside of the plane. I like how this
					// comment is almost completely and utterly justified
					switch (p) {
						case 0:
							clipped = test.Triangle_ClipAgainstPlane(new SVector(0.0f, 0.0f, 0.0f), new SVector(0.0f, 1.0f, 0.0f));
							break;
						case 1:
							clipped = test.Triangle_ClipAgainstPlane(new SVector(0.0f, fViewH - 1, 0.0f), new SVector(0.0f, -1.0f, 0.0f));
							break;
						case 2:
							clipped = test.Triangle_ClipAgainstPlane(new SVector(0.0f, 0.0f, 0.0f), new SVector(1.0f, 0.0f, 0.0f));
							break;
						case 3:
							clipped = test.Triangle_ClipAgainstPlane(new SVector(fViewW - 1, 0.0f, 0.0f), new SVector(-1.0f, 0.0f, 0.0f));
							break;
					}

					// Clipping may yield a variable number of triangles, so
					// add these new ones to the back of the queue for subsequent
					// clipping against next planes
					for (int w = 0; w < clipped.length; w++)
						trianglesToRaster.add(clipped[w]);
				}
				nNewTriangles = trianglesToRaster.size();
			}

			// Draw(Raster) the transformed, viewed, clipped, projected, sorted, clipped triangles
			for (Triangle t : trianglesToRaster) {
//				TexturedTriangle(
//						t.p[0].x, t.p[0].y, t.t[0].x, t.t[0].y, t.t[0].w,
//						t.p[1].x, t.p[1].y, t.t[1].x, t.t[1].y, t.t[1].w,
//						t.p[2].x, t.p[2].y, t.t[2].x, t.t[2].y, t.t[2].w);
//				

				@SuppressWarnings("unchecked")
				BiFunction<Float, Float, Integer>[] colorFun = new BiFunction[1];
				boolean drawTextured = false;

				if ((flags & RENDER_FLAT) == RENDER_FLAT) {
					colorFun[0] = (x, y) -> t.color;
					drawTextured = true;
				}

				if ((flags & RENDER_TEXTURED) == RENDER_TEXTURED) {
					colorFun[0] = (x, y) -> sprTexture.SampleColor(x, y);
					drawTextured = true;
				}
				if (drawTextured) DrawTriangleTextured(
						t.p[0].x, t.p[0].y, t.t[0].x, t.t[0].y, t.t[0].w,
						t.p[1].x, t.p[1].y, t.t[1].x, t.t[1].y, t.t[1].w,
						t.p[2].x, t.p[2].y, t.t[2].x, t.t[2].y, t.t[2].w,
						(x, y) -> t.shaderFun.apply(colorFun[0].apply(x, y)));

				if ((flags & RENDER_WIRE) == RENDER_WIRE) {
					DrawTriangleWire(t, RED);
				}

				nTriangleDrawnCount++;
			}
		}

		return nTriangleDrawnCount;
	}

	public void DrawTriangleFlat(Triangle tri) { DrawTriangleFlat(tri, tri.color); }

	public void DrawTriangleFlat(Triangle tri, int color) { DrawTriangleTextured(tri, (x, y) -> color); }

	public void DrawTriangleWire(Triangle tri) { DrawTriangleWire(tri, tri.color); }

	public void DrawTriangleWire(Triangle tri, int color) {
		game.noFill();
		game.stroke(color);
//		game.triangle(tri.p[0].x, tri.p[0].y, tri.p[1].x, tri.p[1].y, tri.p[2].x, tri.p[2].y);

		float x1 = tri.p[0].x;
		float x2 = tri.p[1].x;
		float x3 = tri.p[2].x;
		float y1 = tri.p[0].y;
		float y2 = tri.p[1].y;
		float y3 = tri.p[2].y;

		game.line(x1, y1, x2, y2);
		game.line(x2, y2, x3, y3);
		game.line(x3, y3, x1, y1);
	}

	public void DrawTriangleTextured(Triangle tri) {
		DrawTriangleTextured(tri, (x, y) -> sprTexture.SampleColor(x, y));
	}

	public void DrawTriangleTextured(Triangle tri, BiFunction<Float, Float, Integer> colorFun) {
		DrawTriangleTextured(
				tri.p[0].x, tri.p[0].y, tri.t[0].x, tri.t[0].y, tri.t[0].w,
				tri.p[1].x, tri.p[1].y, tri.t[1].x, tri.t[1].y, tri.t[1].w,
				tri.p[2].x, tri.p[2].y, tri.t[2].x, tri.t[2].y, tri.t[2].w,
				colorFun);
	}

	public void DrawTriangleTextured(// Triangle tri,
			float x1_, float y1_, float u1, float v1, float w1,
			float x2_, float y2_, float u2, float v2, float w2,
			float x3_, float y3_, float u3, float v3, float w3) {
		DrawTriangleTextured(
				x1_, y1_, u1, v1, w1,
				x2_, y2_, u2, v2, w2,
				x3_, y3_, u3, v3, w3,
				(x, y) -> sprTexture.SampleColor(x, y));
	}

	public void DrawTriangleTextured(// Triangle tri,
			float x1_, float y1_, float u1, float v1, float w1,
			float x2_, float y2_, float u2, float v2, float w2,
			float x3_, float y3_, float u3, float v3, float w3,
			BiFunction<Float, Float, Integer> colorFun) {
		// @formatter:off
		int x1 = (int)x1_; int x2 = (int)x2_; int x3 = (int)x3_;
		int y1 = (int)y1_; int y2 = (int)y2_; int y3 = (int)y3_;
		int iTemp; float fTemp;
		
		if (y2 < y1) { 
			iTemp = y1; y1 = y2; y2 = iTemp; // swap y1, y2
			iTemp = x1; x1 = x2; x2 = iTemp; // swap x1, x2
			fTemp = u1; u1 = u2; u2 = fTemp; // swap u1, u2
			fTemp = v1; v1 = v2; v2 = fTemp; // swap v1, v2
			fTemp = w1; w1 = w2; w2 = fTemp; // swap w1, w2
		}
		if (y3 < y1) { 
			iTemp = y1; y1 = y3; y3 = iTemp; // swap y1, y3
			iTemp = x1; x1 = x3; x3 = iTemp; // swap x1, x3
			fTemp = u1; u1 = u3; u3 = fTemp; // swap u1, u3
			fTemp = v1; v1 = v3; v3 = fTemp; // swap v1, v3
			fTemp = w1; w1 = w3; w3 = fTemp; // swap w1, w3
		}
		if (y3 < y2) { 
			iTemp = y2; y2 = y3; y3 = iTemp; // swap y2, y3
			iTemp = x2; x2 = x3; x3 = iTemp; // swap x2, x3
			fTemp = u2; u2 = u3; u3 = fTemp; // swap u2, u3
			fTemp = v2; v2 = v3; v3 = fTemp; // swap v2, v3
			fTemp = w2; w2 = w3; w3 = fTemp; // swap w2, w3
		}
		// @formatter:on

		int dy1 = y2 - y1;

		int dx1 = x2 - x1;
		float dv1 = v2 - v1;
		float du1 = u2 - u1;
		float dw1 = w2 - w1;

		int dy2 = y3 - y1;
		int dx2 = x3 - x1;
		float dv2 = v3 - v1;
		float du2 = u3 - u1;
		float dw2 = w3 - w1;

		float dax_step = 0, dbx_step = 0,
				du1_step = 0, dv1_step = 0,
				du2_step = 0, dv2_step = 0,
				dw1_step = 0, dw2_step = 0;

		if (dy2 != 0) {
			dbx_step = dx2 / (float) abs(dy2);
			du2_step = du2 / abs(dy2);
			dv2_step = dv2 / abs(dy2);
			dw2_step = dw2 / abs(dy2);
		}

		if (dy1 != 0) {
			dax_step = dx1 / (float) abs(dy1);
			du1_step = du1 / abs(dy1);
			dv1_step = dv1 / abs(dy1);
			dw1_step = dw1 / abs(dy1);
			DrawPartialTriangle(dax_step, dbx_step, du1_step, dv1_step, dw1_step, du2_step, dv2_step, dw2_step,
					x1, y1, u1, v1, w1,
					x1, y1, u1, v1, w1,
					y2, colorFun);
		}

		dy1 = y3 - y2;

		if (dy1 == 0) return;

		dx1 = x3 - x2;
		dv1 = v3 - v2;
		du1 = u3 - u2;
		dw1 = w3 - w2;

		if (dy1 != 0) dax_step = dx1 / (float) abs(dy1);
		if (dy2 != 0) dbx_step = dx2 / (float) abs(dy2);
		if (dy1 != 0) du1_step = du1 / abs(dy1);
		if (dy1 != 0) dv1_step = dv1 / abs(dy1);
		if (dy1 != 0) dw1_step = dw1 / abs(dy1);

		DrawPartialTriangle(dax_step, dbx_step, du1_step, dv1_step, dw1_step, du2_step, dv2_step, dw2_step,
				x1, y1, u1, v1, w1,
				x2, y2, u2, v2, w2,
				y3, colorFun);

	}

	private void DrawPartialTriangle(float dax_step, float dbx_step,
			float du1_step, float dv1_step, float dw1_step,
			float du2_step, float dv2_step, float dw2_step,
			int x1, int y1, float u1, float v1, float w1,
			int x2, int y2, float u2, float v2, float w2,
			int y3,
			BiFunction<Float, Float, Integer> colorFun) {
		for (int i = y2; i <= y3; i++) {
			int ax = (int) (x2 + (i - y2) * dax_step);
			int bx = (int) (x1 + (i - y1) * dbx_step);

			float tex_su = u2 + (i - y2) * du1_step;
			float tex_sv = v2 + (i - y2) * dv1_step;
			float tex_sw = w2 + (i - y2) * dw1_step;

			float tex_eu = u1 + (i - y1) * du2_step;
			float tex_ev = v1 + (i - y1) * dv2_step;
			float tex_ew = w1 + (i - y1) * dw2_step;

			// @formatter:off
				int iTemp; float fTemp;
				if (ax > bx) { 
					iTemp = ax; ax = bx; bx = iTemp; // swap ax, bx
					fTemp = tex_su; tex_su = tex_eu; tex_eu = fTemp; // swap tex_su, tex_eu
					fTemp = tex_sv; tex_sv = tex_ev; tex_ev = fTemp; // swap tex_sv, tex_ev
					fTemp = tex_sw; tex_sw = tex_ew; tex_ew = fTemp; // swap tex_sw, tex_ew
				}
				// @formatter:on

			float tex_u = tex_su;
			float tex_v = tex_sv;
			float tex_w = tex_sw;

			float tstep = 1.0f / (bx - ax);
			float t = 0.0f;

			for (int j = ax; j < bx; j++) {
				tex_u = (1.0f - t) * tex_su + t * tex_eu;
				tex_v = (1.0f - t) * tex_sv + t * tex_ev;
				tex_w = (1.0f - t) * tex_sw + t * tex_ew;

//					if (tex_w > m_DepthBuffer[i * game.width() + j]) {
//						int color = sprTexture.SampleColor(tex_u / tex_w, tex_v / tex_w);
//						game.pixel(j, i, color);
//						m_DepthBuffer[i * game.width() + j] = tex_w;
//					}
//				boolean draw = false;
//				if ((flags & RENDER_DEPTH) == RENDER_DEPTH) {
//					if (tex_w >= m_DepthBuffer[i * game.width() + j]) {
//						draw = true;
//						m_DepthBuffer[i * game.width() + j] = tex_w;
//					}
//				} else if ((flags & RENDER_DEPTH_LOCAL) == RENDER_DEPTH_LOCAL) {
//					if (tex_w >= m_DepthBuffer_Local[i * game.width() + j]) {
//						draw = true;
//						m_DepthBuffer_Local[i * game.width() + j] = tex_w;
//					}
//				} else {
//					draw = true;
//				}
//				if (draw) {
////						int color = sprTexture.SampleColor(tex_u / tex_w, tex_v / tex_w);
//					int color = colorFun.apply(tex_u / tex_w, tex_v / tex_w);
//					game.pixel(j, i, color);
//				}
				Draw(j, i, tex_u, tex_v, tex_w, colorFun);

				t += tstep;
			}
		}
	}

	public void Draw(int x, int y, float u, float v, float w, BiFunction<Float, Float, Integer> colorFun) {
		boolean draw = false;
		int index = y * game.width() + x;

		if (index < 0 || index >= m_DepthBuffer.length) return;

		if ((flags & RENDER_DEPTH) == RENDER_DEPTH) {
			if (w > m_DepthBuffer[index]) {
				draw = true;
				m_DepthBuffer[index] = w;
			}
		} else if ((flags & RENDER_DEPTH_LOCAL) == RENDER_DEPTH_LOCAL) {
			if (w > m_DepthBuffer_Local[index]) {
				draw = true;
				m_DepthBuffer_Local[index] = w;
			}
		} else {
			draw = true;
		}
		if (draw) {
//			int color = sprTexture.SampleColor(tex_u / tex_w, tex_v / tex_w);
			int color = colorFun.apply(u / w, v / w);
			game.pixel(x, y, color);
		}
	}
}
