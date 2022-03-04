package com.sunflow.math3d.models;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import com.sunflow.game.Game3D;
import com.sunflow.math.SVector;

public class GenerateTerrain {

	private Random r = new Random();
	private float roughness = 1.5f;
	public static int mapSize = 50;
	public static float Size = 2;
	private Color G = new Color(155, 155, 155);
//	static Color G = new Color(120, 100, 80);

	public GenerateTerrain(Game3D screen, ArrayList<BaseModel> Models) {
		this(screen, Models, mapSize, Size);
	}

	public GenerateTerrain(Game3D screen, ArrayList<BaseModel> Models, int mapSize, float Size) {
		float[] values1 = new float[mapSize];
		float[] values2 = new float[mapSize];

		for (int y = 0; y < values1.length / 2; y += 2) {

			for (int i = 0; i < values1.length; i++) {
				values1[i] = values2[i];
				values2[i] = r.nextFloat() * roughness;
			}

			if (y != 0) {
				for (int x = 0; x < values1.length / 2; x++) {
//					Models.add(new DPolygon(screen,
//					new float[] { (Size * x), (Size * x), Size + (Size * x) },
//					new float[] { (Size * y), Size + (Size * y), Size + (Size * y) },
//					new float[] { values1[x], values2[x], values2[x + 1] }, G, false));
//					Models.add(new DPolygon(screen,
//							new float[] { (Size * x), Size + (Size * x), Size + (Size * x) },
//							new float[] { (Size * y), Size + (Size * y), (Size * y) },
//							new float[] { values1[x], values2[x + 1], values1[x + 1] }, G, false));

					SVector v0 = new SVector(Size * x, Size * y, values1[x]);
					SVector v1 = new SVector(Size * x, Size + Size * y, values2[x]);
					SVector v2 = new SVector(Size + Size * x, Size + Size * y, values2[x + 1]);

					SVector v3 = new SVector(Size * x, Size * y, values1[x]);
					SVector v4 = new SVector(Size + Size * x, Size + Size * y, values2[x + 1]);
					SVector v5 = new SVector(Size + Size * x, Size * y, values1[x + 1]);

					DPolygon pol0 = new DPolygon(screen, v0, v1, v2);
					pol0.fill(G);

					DPolygon pol1 = new DPolygon(screen, v3, v4, v5);
					pol1.fill(G);

					Models.add(pol0);
					Models.add(pol1);
				}
			}

			for (int i = 0; i < values1.length; i++) {
				values1[i] = values2[i];
				values2[i] = r.nextFloat() * roughness;
			}

			if (y != 0) {
				for (int x = 0; x < values1.length / 2; x++) {
//					Models.add(new DPolygon(screen, 
//							new float[] { (Size * x), (Size * x), Size + (Size * x) }, 
//							new float[] { (Size * (y + 1)), Size + (Size * (y + 1)), Size + (Size * (y + 1)) }, 
//							new float[] { values1[x], values2[x], values2[x + 1] }, G, false));
//					Models.add(new DPolygon(screen, 
//							new float[] { (Size * x), Size + (Size * x), Size + (Size * x) }, 
//							new float[] { (Size * (y + 1)), Size + (Size * (y + 1)), (Size * (y + 1)) }, 
//							new float[] { values1[x], values2[x + 1], values1[x + 1] }, G, false));

					SVector v0 = new SVector(Size * x, Size * (y + 1), values1[x]);
					SVector v1 = new SVector(Size * x, Size + Size * (y + 1), values2[x]);
					SVector v2 = new SVector(Size + Size * x, Size + Size * (y + 1), values2[x + 1]);

					SVector v3 = new SVector(Size * x, Size * (y + 1), values1[x]);
					SVector v4 = new SVector(Size + Size * x, Size + Size * (y + 1), values2[x + 1]);
					SVector v5 = new SVector(Size + Size * x, Size * (y + 1), values1[x + 1]);

					DPolygon pol0 = new DPolygon(screen, v0, v1, v2);
					pol0.fill(G);

					DPolygon pol1 = new DPolygon(screen, v3, v4, v5);
					pol1.fill(G);

					Models.add(pol0);
					Models.add(pol1);
				}
			}
		}
	}
}
