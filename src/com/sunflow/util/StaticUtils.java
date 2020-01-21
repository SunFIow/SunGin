package com.sunflow.util;

public interface StaticUtils extends MathUtils, GameUtils, GeometryUtils, Constants {
	public static final StaticUtils instance = new StaticUtils() {};
}
