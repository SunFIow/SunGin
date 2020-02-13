package com.sunflow.tutorial;

import java.awt.Color;
import java.util.Random;

public class GenerateTerrain {

	private Random r = new Random();
	private double roughness = 1.5;
	public static int mapSize = 50;
	public static double Size = 2;
	private Color G = new Color(155, 155, 155);
//	static Color G = new Color(120, 100, 80);

	public GenerateTerrain(TutorialGame3D screen) {
		double[] values1 = new double[mapSize];
		double[] values2 = new double[mapSize];

		for (int y = 0; y < values1.length / 2; y += 2) {

			for (int i = 0; i < values1.length; i++) {
				values1[i] = values2[i];
				values2[i] = r.nextDouble() * roughness;
			}

			if (y != 0) {
				for (int x = 0; x < values1.length / 2; x++) {
					screen.Models.add(new DPolygon(screen, new double[] { (Size * x), (Size * x), Size + (Size * x) }, new double[] { (Size * y), Size + (Size * y), Size + (Size * y) }, new double[] { values1[x], values2[x], values2[x + 1] }, G, false));
					screen.Models.add(new DPolygon(screen, new double[] { (Size * x), Size + (Size * x), Size + (Size * x) }, new double[] { (Size * y), Size + (Size * y), (Size * y) }, new double[] { values1[x], values2[x + 1], values1[x + 1] }, G, false));
				}
			}

			for (int i = 0; i < values1.length; i++) {
				values1[i] = values2[i];
				values2[i] = r.nextDouble() * roughness;
			}

			if (y != 0) {
				for (int x = 0; x < values1.length / 2; x++) {
					screen.Models.add(new DPolygon(screen, new double[] { (Size * x), (Size * x), Size + (Size * x) }, new double[] { (Size * (y + 1)), Size + (Size * (y + 1)), Size + (Size * (y + 1)) }, new double[] { values1[x], values2[x], values2[x + 1] }, G, false));
					screen.Models.add(new DPolygon(screen, new double[] { (Size * x), Size + (Size * x), Size + (Size * x) }, new double[] { (Size * (y + 1)), Size + (Size * (y + 1)), (Size * (y + 1)) }, new double[] { values1[x], values2[x + 1], values1[x + 1] }, G, false));
				}
			}
		}
	}
}
