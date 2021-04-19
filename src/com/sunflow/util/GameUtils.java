package com.sunflow.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.sunflow.gfx.SGraphics;
import com.sunflow.gfx.SImage;
import com.sunflow.math.DVector;
import com.sunflow.math.SVector;

public interface GameUtils {
	public static final GameUtils instance = new GameUtils() {};

	default SVector createVector() { return new SVector(); }

	default SVector createVector(float x, float y) { return new SVector(x, y); }

	default SVector createVector(float x, float y, float z) { return new SVector(x, y, z); }

	default SVector createVector(double x, double y) { return new SVector(x, y); }

	default SVector createVector(double x, double y, double z) { return new SVector(x, y, z); }

	default DVector createDVector() { return new DVector(); }

	default DVector createDVector(double x, double y) { return new DVector(x, y); }

	default DVector createDVector(double x, double y, double z) { return new DVector(x, y, z); }

	default SGraphics createGraphics(int width, int height) { return createGraphics(width, height, SConstants.JAVA2D); }

	default SGraphics createGraphics(int width, int height, String renderer) {
		Class<?> rendererClass;
		try {
			rendererClass = Thread.currentThread().getContextClassLoader().loadClass(renderer);

			Constructor<?> constructor = rendererClass.getConstructor(new Class[] {});
			SGraphics pg = (SGraphics) constructor.newInstance();

			pg.setPrimary(false);
			pg.setSize(width, height);

			// everything worked, return it
			return pg;
		} catch (InvocationTargetException ite) {
			String msg = ite.getTargetException().getMessage();
			if ((msg != null) &&
					(msg.indexOf("no jogl in java.library.path") != -1)) {
				// Is this true anymore, since the JARs contain the native libs?
				throw new RuntimeException("The jogl library folder needs to be " +
						"specified with -Djava.library.path=/path/to/jogl");

			} else {
				printStackTrace(ite.getTargetException());
				Throwable target = ite.getTargetException();
				/*
				 * // removing for 3.2, we'll see
				 * if (platform == MACOSX) {
				 * target.printStackTrace(System.out); // OS X bug (still true?)
				 * }
				 */
				throw new RuntimeException(target.getMessage());
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("The " + renderer +
					" renderer is not in the class path.");
		} catch (Exception e) {
			if ((e instanceof IllegalArgumentException) ||
					(e instanceof NoSuchMethodException) ||
					(e instanceof IllegalAccessException)) {
				if (e.getMessage().contains("cannot be <= 0")) {
					// IllegalArgumentException will be thrown if w/h is <= 0
					// http://code.google.com/p/processing/issues/detail?id=983
					throw new RuntimeException(e);

				} else {
					printStackTrace(e);
					String msg = renderer + " needs to be updated " +
							"for the current release of Processing.";
					throw new RuntimeException(msg);
				}
			} else {
				/*
				 * if (platform == MACOSX) {
				 * e.printStackTrace(System.out); // OS X bug (still true?)
				 * }
				 */
				printStackTrace(e);
				throw new RuntimeException(e.getMessage());
			}
		}
	}

	default SImage loadImage(String filename) { return new SImage(loadIOImage(filename)); }

	default BufferedImage loadIOImage(String filename) {
		BufferedImage img = null;
		try {
//		    URL url = new URL(getCodeBase(), "examples/strawberry.jpg");
//		    img = ImageIO.read(url);
			File inputfile = new File(filename);
			img = ImageIO.read(inputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}

	default void printStackTrace(Throwable t) { t.printStackTrace(); }

	default int index(float x, float y, int width) { return index((int) x, (int) y, width); }

	default int index(int x, int y, int width) { return x + y * width; }

	default int indexRotated(int x, int y, int width, int height, byte rotation) {
		switch (rotation) {
			case SConstants.R0:
				return x + y * width;
			case SConstants.R90:
				return width * (height - 1) + y - x * width;
			case SConstants.R180:
				return width * height - 1 - (y * width) - x;
			case SConstants.R270:
				return width - 1 - y + x * width;
			default:
				return -1;
		}
	}

	default float[] convert(double[] arr) {
		float[] newArr = new float[arr.length];
		for (int i = 0; i < newArr.length; i++) {
			newArr[i] = (float) arr[i];
		}
		return newArr;
	}

	default double[] convert(float[] arr) {
		double[] newArr = new double[arr.length];
		for (int i = 0; i < newArr.length; i++) {
			newArr[i] = arr[i];
		}
		return newArr;
	}

	default String loadFileAsString(String fileName) {
		StringBuilder strBuilder = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
			String line = "";
			while ((line = br.readLine()) != null) {
				strBuilder.append(line);
				strBuilder.append(System.lineSeparator());
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strBuilder.toString();
	}

	default String[] loadFileAsStringArray(String fileName) {
		ArrayList<String> strs = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
			String line = "";
			while ((line = br.readLine()) != null) {
				strs.add(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strs.toArray(new String[0]);
	}

	default Pair<SVector, Float> getClosest(SVector pos, SVector[] list) {
		SVector closest = null;
		float closestDist = Float.MAX_VALUE;

		int size = list.length;
		for (int i = 0; i < size; i++) {
			SVector f = list[i];
			float dist = SVector.dist(f, pos);
			if (dist < closestDist) {
				closestDist = dist;
				closest = f;
			}
		}
		return new Pair<SVector, Float>(closest, closestDist);
	}

	class Pair<A, B> {
		public A a;
		public B b;

		public Pair(A first, B second) {
			this.a = first;
			this.b = second;
		}
	}
}
