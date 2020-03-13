package com.sunflow.util;

import com.sunflow.math.Vertex2D;

public interface MathUtils {

	default int abs(int a) { return Math.abs(a); }

	default long abs(long a) { return Math.abs(a); }

	default float abs(float a) { return Math.abs(a); }

	default double abs(double a) { return Math.abs(a); }

	default int round(float a) { return Math.round(a); }

	default long round(double a) { return Math.round(a); }

	default int constrain(int val, int a, int b) { return min(max(val, b), a); }

	default long constrain(long val, long a, long b) { return min(max(val, b), a); }

	default float constrain(float val, float a, float b) { return min(max(val, b), a); }

	default double constrain(double val, double a, double b) { return min(max(val, b), a); }

	default int min(int a, int b) { return Math.min(a, b); }

	default long min(long a, long b) { return Math.min(a, b); }

	default float min(float a, float b) { return Math.min(a, b); }

	default double min(double a, double b) { return Math.min(a, b); }

	default int max(int a, int b) { return Math.max(a, b); }

	default long max(long a, long b) { return Math.max(a, b); }

	default float max(float a, float b) { return Math.max(a, b); }

	default double max(double a, double b) { return Math.max(a, b); }

	default float cos(float angle) { return (float) Math.cos(angle); }

	default double cos(double angle) { return Math.cos(angle); }

	default float sin(float angle) { return (float) Math.sin(angle); }

	default double sin(double angle) { return Math.sin(angle); }

	default float norm(float value, float min, float max) {
		return (float) norm((double) value, min, max);
	}

	default double norm(double value, double min, double max) {
		return (value - min) / (max - min);
	}

	default float lerp(float norm, float min, float max) {
		return (float) lerp((double) norm, min, max);
	}

	default double lerp(double norm, double min, double max) {
		return min + (max - min) * norm;
	}

	default float map(float value, float sourceMin, float sourceMax, float destMin, float destMax) {
		return (float) map((double) value, sourceMin, sourceMax, destMin, destMax);
	}

	default double map(double value, double sourceMin, double sourceMax, double destMin, double destMax) {
		return lerp(norm(value, sourceMin, sourceMax), destMin, destMax);
	}

	default float dot(float a, float b) { return (float) dot((double) a, b); }

	default double dot(double a, double b) { return (a * a + b * b); }

	default float dist(float x1, float y1, float x2, float y2) {
		return (float) dist((double) x1, y1, x2, y2);
	}

	default double dist(double x1, double y1, double x2, double y2) {
		return Vertex2D.dist(new Vertex2D(x1, y1), new Vertex2D(x2, y2));
	}

	default int random(int high) { return (int) random((float) high); }

	default int random(int low, int high) { return (int) random((float) low, (float) high); }

	default float random(float high) { return (float) random((double) high); }

	default float random(float low, float high) {
		return (float) random((double) low, (double) high);
	}

	default double random(Number high) { return random(high.doubleValue()); }

	default double random(Number low, Number high) {
		return random(low.doubleValue(), high.doubleValue());
	}

	default double random(double high) {
		// avoid an infinite loop when 0 or NaN are passed in
		if (high == 0 || high != high) return 0;

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
		if (low >= high) return low;
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
	default int clamp(int min, int value, int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * Simple utility function which clamps the given value to be strictly
	 * between the min and max values.
	 */
	default float clamp(float min, float value, float max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * Simple utility function which clamps the given value to be strictly
	 * between the min and max values.
	 */
	default double clamp(double min, double value, double max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * ( begin auto-generated from degrees.xml )
	 *
	 * Converts a radian measurement to its corresponding value in degrees.
	 * Radians and degrees are two ways of measuring the same thing. There are
	 * 360 degrees in a circle and 2*PI radians in a circle. For example,
	 * 90&deg; = PI/2 = 1.5707964. All trigonometric functions in Processing
	 * require their parameters to be specified in radians.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref math:trigonometry
	 * @param radians
	 *            radian value to convert to degrees
	 * @see PApplet#radians(float)
	 */
	default double degrees(double radians) {
		return radians * Constants.RAD_TO_DEG_D;
	}

	default float degrees(float radians) {
		return radians * Constants.RAD_TO_DEG;
	}

	/**
	 * ( begin auto-generated from radians.xml )
	 *
	 * Converts a degree measurement to its corresponding value in radians.
	 * Radians and degrees are two ways of measuring the same thing. There are
	 * 360 degrees in a circle and 2*PI radians in a circle. For example,
	 * 90&deg; = PI/2 = 1.5707964. All trigonometric functions in Processing
	 * require their parameters to be specified in radians.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref math:trigonometry
	 * @param degrees
	 *            degree value to convert to radians
	 * @see PApplet#degrees(float)
	 */
	default double radians(double degrees) {
		return degrees * Constants.DEG_TO_RAD_D;
	}

	default float radians(float degrees) {
		return degrees * Constants.DEG_TO_RAD;
	}
}
