package com.sunflow.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.sunflow.gfx.SGraphics;
import com.sunflow.gfx.SImage;
import com.sunflow.math.SVector;

public interface GameUtils {
	public static final GameUtils instance = new GameUtils() {};

	default SVector createVector() { return new SVector(); }

	default SVector createVector(float x, float y) { return new SVector(x, y); }

	default SVector createVector(float x, float y, float z) { return new SVector(x, y, z); }

	default SImage createImage(float width, float height) { return new SImage(width, height); }

	default SImage createImage(float width, float height, int format) { return new SImage(width, height, format); }

	default SImage createImage(BufferedImage bi) { return new SImage(bi); }

	default SGraphics createGraphics(float width, float height) { return new SGraphics(width, height); }

	default SGraphics createGraphics(float width, float height, int format) { return new SGraphics(width, height, format); }

	default SGraphics createGraphics(BufferedImage bi) { return new SGraphics(bi); }

	default int index(int x, int y, int width) { return x + y * width; }

	default int indexRotated(int x, int y, int width, int height, byte rotation) {
		switch (rotation) {
			case Constants.R0:
				return x + y * width;
			case Constants.R90:
				return width * (height - 1) + y - x * width;
			case Constants.R180:
				return width * height - 1 - (y * width) - x;
			case Constants.R270:
				return width - 1 - y + x * width;
			default:
				return -1;
		}
	}

	default String nf(int num, int digits) { return String.format("%0" + digits + "d", num); }

	default String nf(float num, int digits) { return String.format("%0" + digits + "f", num); }

	default String nf(double num, int digits) { return String.format("%0" + digits + "d", num); }

	default float[] convertArray(double[] arr) {
		float[] newArr = new float[arr.length];
		for (int i = 0; i < newArr.length; i++) {
			newArr[i] = (float) arr[i];
		}
		return newArr;
	}

	default double[] convertArray(float[] arr) {
		double[] newArr = new double[arr.length];
		for (int i = 0; i < newArr.length; i++) {
			newArr[i] = arr[i];
		}
		return newArr;
	}

	default String loadFileAsString(String fileName) {
		StringBuilder strBuilder = new StringBuilder();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new File(fileName)));
			String line = "";
			while ((line = br.readLine()) != null) {
				strBuilder.append(line);
				strBuilder.append(System.lineSeparator());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strBuilder.toString();
	}

	default String[] loadFileAsStringArray(String fileName) {
		ArrayList<String> strs = new ArrayList<>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new File(fileName)));
			String line = "";
			while ((line = br.readLine()) != null) {
				strs.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strs.toArray(new String[0]);
	}

	default SImage loadSImage(String fileName) {
		return new SImage(loadImage(fileName));
	}

	default BufferedImage loadImage(String fileName) {
		BufferedImage img = null;
		try {
//		    URL url = new URL(getCodeBase(), "examples/strawberry.jpg");
//		    img = ImageIO.read(url);
			File inputfile = new File(fileName);
			img = ImageIO.read(inputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}

	default void saveImage(BufferedImage image, String fileName) {
		try {
			File outputfile = new File(fileName);
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
