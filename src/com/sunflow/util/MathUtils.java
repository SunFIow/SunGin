package com.sunflow.util;

import com.sunflow.math.Vertex2D;

public interface MathUtils {
	default float cos(float angle) {
		return (float) Math.cos(angle);
	}

	default double cos(double angle) {
		return Math.cos(angle);
	}

	default float sin(float angle) {
		return (float) Math.sin(angle);
	}

	default double sin(double angle) {
		return Math.sin(angle);
	}

	default double norm(double value, double min, double max) {
		return (value - min) / (max - min);
	}

	default double lerp(double norm, double min, double max) {
		return min + (max - min) * norm;
	}

	default double map(double value, double sourceMin, double sourceMax, double destMin, double destMax) {
		return lerp(norm(value, sourceMin, sourceMax), destMin, destMax);
	}

	default float map(float value, float sourceMin, float sourceMax, float destMin, float destMax) {
		return (float) map((double) value, sourceMin, sourceMax, destMin, destMax);
	}

	default double dot(double a, double b) {
		return (a * a + b * b);
	}

	default double dist(double x1, double y1, double x2, double y2) {
		return Vertex2D.dist(new Vertex2D(x1, y1), new Vertex2D(x2, y2));
	}

	default double random(Number high) {
		return random(high.doubleValue());
	}

	default double random(Number low, Number high) {
		return random(low.doubleValue(), high.doubleValue());
	}

	default int random(int high) {
		return (int) random((float) high);
	}

	default int random(int low, int high) {
		return (int) random((float) low, (float) high);
	}

	default float random(float high) {
		return (float) random((double) high);
	}

	default float random(float low, float high) {
		return (float) random((double) low, (double) high);
	}

	default double random(double high) {
		// avoid an infinite loop when 0 or NaN are passed in
		if (high == 0 || high != high) {
			return 0;
		}

		// for some reason (rounding error?) Math.random() * 3
		// can sometimes return '3' (once in ~30 million tries)
		// so a check was added to avoid the inclusion of 'howbig'
		double value = 0;
		do {
			value = Math.random() * high;
		} while (value == high);
		return value;
	}

	default double random(double low, double high) {
		if (low >= high)
			return low;
		double diff = high - low;
		double value = 0;
		// because of rounding error, can't just add low, otherwise it may hit high
		// https://github.com/processing/processing/issues/4551
		do {
			value = random(diff) + low;
		} while (value == high);
		return value;
	}

	/**
	 * Simple utility function which clamps the given value to be strictly
	 * between the min and max values.
	 */
	default float clamp(float min, float value, float max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	/**
	 * Simple utility function which clamps the given value to be strictly
	 * between the min and max values.
	 */
	default int clamp(int min, int value, int max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}
}
