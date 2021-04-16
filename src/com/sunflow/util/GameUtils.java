package com.sunflow.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import com.sunflow.game.GameBase;
import com.sunflow.gfx.SGraphics;
import com.sunflow.gfx.SImage;
import com.sunflow.logging.Log;
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

	default void printStackTrace(Throwable e) { e.printStackTrace(); }

	default boolean external() { return false; }

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

	default SGraphics createGraphics(int width, int height) { return createGraphics(width, height, SConstants.JAVA2D); }

	default SGraphics createGraphics(int width, int height, String renderer) { return createGraphics(width, height, renderer, null); }

	default SGraphics createGraphics(int width, int height, String renderer, String path) { return makeGraphics(width, height, renderer, path, false); }

	default SGraphics makeGraphics(int width, int height, String renderer, String path, boolean primary) {
//		if (!primary && !g.isGL()) {
//			if (renderer.equals(P2D)) {
//				throw new RuntimeException("createGraphics() with P2D requires size() to use P2D or P3D");
//			} else if (renderer.equals(P3D)) {
//				throw new RuntimeException("createGraphics() with P3D or OPENGL requires size() to use P2D or P3D");
//			}
//		}

		try {
			Class<?> rendererClass = Thread.currentThread().getContextClassLoader().loadClass(renderer);

			Constructor<?> constructor = rendererClass.getConstructor(new Class[] {});
			SGraphics pg = (SGraphics) constructor.newInstance();

			pg.setPrimary(primary);
			if (path != null) {
				pg.setPath(savePath(path));
			}
//		      pg.setQuality(sketchQuality());
//		      if (!primary) {
//		        surface.initImage(pg, w, h);
//		      }
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

		} catch (ClassNotFoundException cnfe) {
//		      if (cnfe.getMessage().indexOf("processing.opengl.PGraphicsOpenGL") != -1) {
//		        throw new RuntimeException(openglError +
//		                                   " (The library .jar file is missing.)");
//		      } else {
			if (external()) {
				throw new RuntimeException("You need to use \"Import Library\" " +
						"to add " + renderer + " to your sketch.");
			} else {
				throw new RuntimeException("The " + renderer +
						" renderer is not in the class path.");
			}

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

//		SGraphics sg = new SGraphics();
//		sg.setPrimary(false);
//		if (path != null) {
//			sg.setPath(savePath(path));
//		}
//		sg.setSize(width, height);
	}

	default SImage createImage(int width, int height) { return new SImage(width, height); }

	default SImage createImage(int width, int height, int format) { return new SImage(width, height, format); }

	default SImage createImage(Image bi) { return new SImage(bi); }

	default SImage loadSImage(String fileName) { return new SImage(loadImage(fileName)); }

	default BufferedImage loadImage(String fileName) {
		BufferedImage img = null;
		try {
//		    URL url = new URL(getCodeBase(), "examples/strawberry.jpg");
//		    img = ImageIO.read(url);
			File inputfile = new File(fileName);
			System.out.println(inputfile.getAbsolutePath());
			img = ImageIO.read(inputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}

	default void saveImage(SImage image, String fileName) { image.save(savePath(fileName)); }

//	default void saveImage(SImage image, String fileName) { saveImage((BufferedImage)image.getImage(), fileName); }

	default void saveImage(BufferedImage image, String fileName) {
		try {
			Path dir = Paths.get(savePath(fileName));
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

	default OutputStream createOutput(File file) {
		try {
			createPath(file); // make sure the path exists
			OutputStream output = new FileOutputStream(file);
			if (file.getName().toLowerCase().endsWith(".gz")) {
				return new BufferedOutputStream(new GZIPOutputStream(output));
			}
			return new BufferedOutputStream(output);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	default String savePath(String where) {
		if (where == null) return null;
		String filename = sketchPath(where);
		createPath(filename);
		return filename;
	}

	default void createPath(String path) {
		createPath(new File(path));
	}

	default void createPath(File file) {
		try {
			String parent = file.getParent();
			if (parent != null) {
				File unit = new File(parent);
				if (!unit.exists()) unit.mkdirs();
			}
		} catch (SecurityException se) {
			System.err.println("You don't have permissions to create " +
					file.getAbsolutePath());
		}
	}

	default String sketchPath(String where) {
		if (sketchPath() == null) {
			return where;
		}
		// isAbsolute() could throw an access exception, but so will writing
		// to the local disk using the sketch path, so this is safe here.
		// for 0120, added a try/catch anyways.
		try {
			if (new File(where).isAbsolute()) return where;
		} catch (Exception e) {}

		return sketchPath() + File.separator + where;
	}

	String[] sketchPath = new String[1];

	default String sketchPath() {
		if (sketchPath[0] == null) {
			sketchPath[0] = calcSketchPath();
		}
		return sketchPath[0];
	}

	default String calcSketchPath() {
		// try to get the user folder. if running under java web start,
		// this may cause a security exception if the code is not signed.
		// http://processing.org/discourse/yabb_beta/YaBB.cgi?board=Integrate;action=display;num=1159386274
		String folder = null;
		try {
			folder = System.getProperty("user.dir");

			URL jarURL = GameBase.class.getProtectionDomain().getCodeSource().getLocation();
			// Decode URL
			String jarPath = jarURL.toURI().getSchemeSpecificPart();

//			// Workaround for bug in Java for OS X from Oracle (7u51)
//			// https://github.com/processing/processing/issues/2181
//			if (platform == MACOSX) {
//				if (jarPath.contains("Contents/Java/")) {
//					String appPath = jarPath.substring(0, jarPath.indexOf(".app") + 4);
//					File containingFolder = new File(appPath).getParentFile();
//					folder = containingFolder.getAbsolutePath();
//				}
//			} else {
			// Working directory may not be set properly, try some options
			// https://github.com/processing/processing/issues/2195
			if (jarPath.contains("/lib/")) {
				// Windows or Linux, back up a directory to get the executable
				folder = new File(jarPath, "../..").getCanonicalPath();
			}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return folder;
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
