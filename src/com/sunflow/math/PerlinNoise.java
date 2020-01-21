package com.sunflow.math;

import java.util.Random;

public class PerlinNoise {
	private final Random random = new Random();

	public final int WIDTH;
	public final int HEIGHT;

	private float[][] noiseMap;
	public int octaves = 8;
	public int startFrequencyX = 3;
	public int startFrequencyY = 3;
	public float alpha = 20;

	public PerlinNoise(int width, int height) {
		WIDTH = width;
		HEIGHT = height;

		noiseMap = new float[width][height];
		calculate();
		normalize();
	}

	public void calculate() {
		int currentFrequencyX = startFrequencyX;
		int currentFrequencyY = startFrequencyY;
		float currentAlpha = alpha;

		for (int oc = 0; oc < octaves; oc++) {
			float[][] discretePoints = new float[currentFrequencyX + 1][currentFrequencyY + 1];
			for (int i = 0; i < discretePoints.length; i++) {
				for (int k = 0; k < discretePoints[0].length; k++) {
					discretePoints[i][k] = (random.nextFloat()) * currentAlpha;
				}
			}

			for (int i = 0; i < WIDTH; i++) {
				for (int k = 0; k < HEIGHT; k++) {
					float currentX = i / (float) WIDTH * currentFrequencyX;
					float currentY = k / (float) HEIGHT * currentFrequencyY;
					int indexX = (int) currentX;
					int indexY = (int) currentY;

					float w0 = interpolate(discretePoints[indexX][indexY], discretePoints[indexX + 1][indexY], currentX - indexX);
					float w1 = interpolate(discretePoints[indexX][indexY + 1], discretePoints[indexX + 1][indexY + 1], currentX - indexX);
					float w = interpolate(w0, w1, currentY - indexY);

					noiseMap[i][k] += w;
				}
			}

			currentFrequencyX *= 2;
			currentFrequencyY *= 2;
			currentAlpha /= 2;
		}
	}

	public void normalize() {
		float min = noiseMap[0][0];
		for (int i = 0; i < WIDTH; i++) {
			for (int k = 0; k < HEIGHT; k++) {
				if (noiseMap[i][k] < min) {
					min = noiseMap[i][k];
				}
			}
		}
		System.out.println(min);
		for (int i = 0; i < WIDTH; i++) {
			for (int k = 0; k < HEIGHT; k++) {
				noiseMap[i][k] -= min;
			}
		}
		float max = noiseMap[0][0];
		for (int i = 0; i < WIDTH; i++) {
			for (int k = 0; k < HEIGHT; k++) {
				if (noiseMap[i][k] > max) {
					max = noiseMap[i][k];
				}
			}
		}
		System.out.println(max);
		for (int i = 0; i < WIDTH; i++) {
			for (int k = 0; k < HEIGHT; k++) {
				noiseMap[i][k] /= max;
			}
		}
	}

	public float[][] getHeightMap() {
		return noiseMap;
	}

	private float interpolate(float a, float b, float t) {
		return (a * (1 - t) + b * t);
	}

	public float noise(int xoff, int yoff) {
		return noiseMap[xoff][yoff];
	}

}
