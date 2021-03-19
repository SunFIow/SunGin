package com.sunflow.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import com.sunflow.gfx.SGraphics;
import com.sunflow.gfx.SImage;
import com.sunflow.logging.Log;
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

	//////////////////////////////////////////////////////////////

	// SORT

	/**
	 * ( begin auto-generated from sort.xml )
	 *
	 * Sorts an array of numbers from smallest to largest and puts an array of
	 * words in alphabetical order. The original array is not modified, a
	 * re-ordered array is returned. The <b>count</b> parameter states the
	 * number of elements to sort. For example if there are 12 elements in an
	 * array and if count is the value 5, only the first five elements on the
	 * array will be sorted. <!--As of release 0126, the alphabetical ordering
	 * is case insensitive.-->
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:array_functions
	 * @param list
	 *            array to sort
	 * @see PApplet#reverse(boolean[])
	 */
	default byte[] sort(byte list[]) {
		return sort(list, list.length);
	}

	/**
	 * @param count
	 *            number of elements to sort, starting from 0
	 */
	default byte[] sort(byte[] list, int count) {
		byte[] outgoing = new byte[list.length];
		System.arraycopy(list, 0, outgoing, 0, list.length);
		Arrays.sort(outgoing, 0, count);
		return outgoing;
	}

	default char[] sort(char list[]) {
		return sort(list, list.length);
	}

	default char[] sort(char[] list, int count) {
		char[] outgoing = new char[list.length];
		System.arraycopy(list, 0, outgoing, 0, list.length);
		Arrays.sort(outgoing, 0, count);
		return outgoing;
	}

	default int[] sort(int list[]) {
		return sort(list, list.length);
	}

	default int[] sort(int[] list, int count) {
		int[] outgoing = new int[list.length];
		System.arraycopy(list, 0, outgoing, 0, list.length);
		Arrays.sort(outgoing, 0, count);
		return outgoing;
	}

	default float[] sort(float list[]) {
		return sort(list, list.length);
	}

	default float[] sort(float[] list, int count) {
		float[] outgoing = new float[list.length];
		System.arraycopy(list, 0, outgoing, 0, list.length);
		Arrays.sort(outgoing, 0, count);
		return outgoing;
	}

	default String[] sort(String list[]) {
		return sort(list, list.length);
	}

	default String[] sort(String[] list, int count) {
		String[] outgoing = new String[list.length];
		System.arraycopy(list, 0, outgoing, 0, list.length);
		Arrays.sort(outgoing, 0, count);
		return outgoing;
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
			Path dir = Paths.get(fileName);
			if (Files.notExists(dir.getParent())) try {
				Files.createDirectory(dir.getParent());
			} catch (IOException e) {
				Log.error("File Directory could't be created", e);
			}
			File outputfile = dir.toFile();
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
