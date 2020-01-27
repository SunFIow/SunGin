package com.sunflow.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.sunflow.gfx.DImage;
import com.sunflow.math.Vertex2F;

public interface GameUtils {
	default public int index(int x, int y, int width) { return x + y * width; }

	default public int indexRotated(int x, int y, int width, int height, byte rotation) {
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

	default public String nf(int num, int digits) { return String.format("%0" + digits + "d", num); }

	default public String nf(float num, int digits) { return String.format("%0" + digits + "f", num); }

	default public String nf(double num, int digits) { return String.format("%0" + digits + "d", num); }

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

	default DImage loadFileAsDImage(String fileName) {
		return new DImage(loadFileAsImage(fileName));
	}

	default BufferedImage loadFileAsImage(String fileName) {
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

	default void saveImageToFile(BufferedImage image, String fileName) {
		try {
			File outputfile = new File(fileName);
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	default Pair<Vertex2F, Float> getClosest(Vertex2F pos, Vertex2F[] list) {
		Vertex2F closest = null;
		float closestDist = Float.MAX_VALUE;

		int size = list.length;
		for (int i = 0; i < size; i++) {
			Vertex2F f = list[i];
			float dist = Vertex2F.dist(f, pos);
			if (dist < closestDist) {
				closestDist = dist;
				closest = f;
			}
		}
		return new Pair<Vertex2F, Float>(closest, closestDist);
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
