package com.sunflow.tutorial_copy;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

public class GenerateTerrain {

	private Random r = new Random();
	private float roughness = 1.5f;
	public static int mapSize = 50;
	public static float Size = 2;
	private Color G = new Color(155, 155, 155);
//	static Color G = new Color(120, 100, 80);

	public GenerateTerrain(TutorialGame3D screen, ArrayList<IModel> Models) {
		float[] values1 = new float[mapSize];
		float[] values2 = new float[mapSize];

		for (int y = 0; y < values1.length / 2; y += 2) {

			for (int i = 0; i < values1.length; i++) {
				values1[i] = values2[i];
				values2[i] = r.nextFloat() * roughness;
			}

			if (y != 0) {
				for (int x = 0; x < values1.length / 2; x++) {
					Models.add(new DPolygon(screen, new float[] { (Size * x), (Size * x), Size + (Size * x) }, new float[] { (Size * y), Size + (Size * y), Size + (Size * y) }, new float[] { values1[x], values2[x], values2[x + 1] }, G, false));
					Models.add(new DPolygon(screen, new float[] { (Size * x), Size + (Size * x), Size + (Size * x) }, new float[] { (Size * y), Size + (Size * y), (Size * y) }, new float[] { values1[x], values2[x + 1], values1[x + 1] }, G, false));
				}
			}

			for (int i = 0; i < values1.length; i++) {
				values1[i] = values2[i];
				values2[i] = r.nextFloat() * roughness;
			}

			if (y != 0) {
				for (int x = 0; x < values1.length / 2; x++) {
					Models.add(new DPolygon(screen, new float[] { (Size * x), (Size * x), Size + (Size * x) }, new float[] { (Size * (y + 1)), Size + (Size * (y + 1)), Size + (Size * (y + 1)) }, new float[] { values1[x], values2[x], values2[x + 1] }, G, false));
					Models.add(new DPolygon(screen, new float[] { (Size * x), Size + (Size * x), Size + (Size * x) }, new float[] { (Size * (y + 1)), Size + (Size * (y + 1)), (Size * (y + 1)) }, new float[] { values1[x], values2[x + 1], values1[x + 1] }, G, false));
				}
			}
		}
	}
}
