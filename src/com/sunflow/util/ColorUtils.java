package com.sunflow.util;

import java.awt.Color;

public interface ColorUtils {
	public static final ColorUtils instance = new ColorUtils() {};

	default int colorShade(int rgb, float lum) {
		float calcR = ColorUtils.instance.red(rgb);
		float calcG = ColorUtils.instance.green(rgb);
		float calcB = ColorUtils.instance.blue(rgb);
		return ColorUtils.instance.color(calcR * lum, calcG * lum, calcB * lum);
	}

	default float alpha(int rgb) { return (rgb >> 24) & 0xff; }

	default float red(int rgb) { return (rgb >> 16) & 0xff; }

	default float green(int rgb) { return (rgb >> 8) & 0xff; }

	default float blue(int rgb) { return (rgb) & 0xff; }

	default float hue(int rgb) {
		float[] cacheHsbValue = new float[3];
		Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff,
				rgb & 0xff, cacheHsbValue);
		return cacheHsbValue[0] * 255;
	}

	default float saturation(int rgb) {
		float[] cacheHsbValue = new float[3];
		Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff,
				rgb & 0xff, cacheHsbValue);
		return cacheHsbValue[1] * 255;
	}

	default float brightness(int rgb) {
		float[] cacheHsbValue = new float[3];
		Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff,
				rgb & 0xff, cacheHsbValue);
		return cacheHsbValue[2] * 255;
	}

	default int color(int c) { // ignore
		return colorCalc(c);
	}

	default int color(float gray) { // ignore
		return colorCalc(gray);
	}

	default int color(double gray) { // ignore
		return colorCalc((float) gray);
	}

	/**
	 * @param c
	 *            can be packed ARGB or a gray in this case
	 */

	default int color(int c, int alpha) { // ignore
		return colorCalc(c, alpha);
	}

	/**
	 * @param c
	 *            can be packed ARGB or a gray in this case
	 */

	default int color(int c, float alpha) { // ignore
		return colorCalc(c, alpha);
	}

	default int color(float gray, float alpha) { // ignore
		return colorCalc(gray, alpha);
	}

	default int color(int v1, int v2, int v3) { // ignore
		return colorCalc(v1, v2, v3);
	}

	default int color(float v1, float v2, float v3) { // ignore
		return colorCalc(v1, v2, v3);
	}

	default int color(int v1, int v2, int v3, int a) { // ignore
		return colorCalc(v1, v2, v3, a);
	}

	default int color(float v1, float v2, float v3, float a) { // ignore
		return colorCalc(v1, v2, v3, a);
	}

	default int colorCalc(int rgb, float alpha) {
		if (((rgb & 0xff000000) == 0) && (rgb <= 255)) { // see above
			return colorCalc((float) rgb, alpha);

		} else {
			return colorCalcARGB(rgb, alpha);
		}
	}

	default int colorCalc(float gray) {
		return colorCalc(gray, 255);
	}

	default int colorCalc(float gray, float alpha) {
		if (gray > 255) gray = 255;
		if (alpha > 255) alpha = 255;

		if (gray < 0) gray = 0;
		if (alpha < 0) alpha = 0;

		int calcRi = (int) gray;
		int calcGi = (int) gray;
		int calcBi = (int) gray;
		int calcAi = (int) alpha;
		int calcColor = (calcAi << 24) | (calcRi << 16) | (calcGi << 8) | calcBi;

		return calcColor;
	}

	default int colorCalc(float x, float y, float z) {
		return colorCalc(x, y, z, 255);
	}

	default int colorCalc(float x, float y, float z, float a) {
		if (x > 255) x = 255;
		if (y > 255) y = 255;
		if (z > 255) z = 255;
		if (a > 255) a = 255;

		if (x < 0) x = 0;
		if (y < 0) y = 0;
		if (z < 0) z = 0;
		if (a < 0) a = 0;

		int calcRi = (int) x;
		int calcGi = (int) y;
		int calcBi = (int) z;
		int calcAi = (int) z;
		int calcColor = (calcAi << 24) | (calcRi << 16) | (calcGi << 8) | calcBi;
		return calcColor;
	}

	default int colorCalcARGB(int argb, float alpha) {
		int calcAi, calcColor;
		if (alpha == 255) {
			calcAi = (argb >> 24) & 0xff;
			calcColor = argb;
		} else {
			calcAi = (int) (((argb >> 24) & 0xff) * MathUtils.instance.clamp(0, (alpha / 255), 1));
			calcColor = (calcAi << 24) | (argb & 0xFFFFFF);
		}
		return calcColor;
	}
}
