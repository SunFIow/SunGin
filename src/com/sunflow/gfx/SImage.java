package com.sunflow.gfx;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.util.Arrays;

import com.sunflow.util.Constants;
import com.sunflow.util.LogUtils;
import com.sunflow.util.MathUtils;

public class SImage implements Cloneable, Constants, MathUtils, LogUtils {
	public int format;

	public int width;
	public int height;

	public int[] pixels;
	private int[] clearPixels;

	public BufferedImage image;

	protected SImage() {}

	public SImage(float width, float height) {
		init((int) width, (int) height, RGB);
	}

	public SImage(float width, float height, int format) {
		init((int) width, (int) height, format);
	}

	public SImage(BufferedImage bi) {
//		init(bi.getWidth(), bi.getHeight(), bi.getType() == ARGB ? ARGB : RGB);
		init(bi.getWidth(), bi.getHeight(), RGB);
		Graphics2D g = image.createGraphics();
		g.drawImage(bi, 0, 0, null);
		g.dispose();
	}

	private void init(int width, int height, int format) {
		this.width = width;
		this.height = height;
		this.format = format;
		this.pixels = new int[width * height];
		defaultSettings();
	}

	protected void defaultSettings() { // ignore
		image = new BufferedImage(width, height, format);
//		graphics = image.createGraphics();
//		background(0xffCCCCCC);
	}

	final public void resize(float width, float height) { resize((int) width, (int) height); }

	final public void resize(int width, int height) {
		BufferedImage oldImage = image.getSubimage(0, 0, this.width, this.height);
		this.width = width;
		this.height = height;
		image = new BufferedImage(width, height, format);
//		graphics = image.createGraphics();
//		defaultComposite = graphics.getComposite();

		Graphics2D g = image.createGraphics();
		g.drawImage(oldImage, 0, 0, null);
		g.dispose();
	}

	final public int[] loadPixels() {
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		return pixels;
	}

	final public int[][] loadPixels2D() {
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;

		int[][] result = new int[width][height];
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
				int argb = 0;
				argb += ((pixels[pixel] & 0xff) << 24); // alpha
				argb += (pixels[pixel + 1] & 0xff); // blue
				argb += ((pixels[pixel + 2] & 0xff) << 8); // green
				argb += ((pixels[pixel + 3] & 0xff) << 16); // red
				result[col][row] = argb;
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += pixelLength) {
				int argb = 0;
				argb += -16777216; // 255 alpha
				argb += (pixels[pixel] & 0xff); // blue
				argb += ((pixels[pixel + 1] & 0xff) << 8); // green
				argb += ((pixels[pixel + 2] & 0xff) << 16); // red
				result[col][row] = argb;
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		}

		return result;
	}

	final public void updatePixels() { image.getRaster().setDataElements(0, 0, image.getWidth(), image.getHeight(), pixels); }

	final public void updatePixels(int[] data) { image.getRaster().setDataElements(0, 0, image.getWidth(), image.getHeight(), data); }

	final public void updatePixels(int x, int y, int w, int h, int[] pixels) {
		if (pixels == null || w == 0 || h == 0) return;
		else if (pixels.length < w * h) throw new IllegalArgumentException("pixels array must have a length" + " >= w*h");

		if (format == ARGB || format == RGB) {
			WritableRaster raster = image.getRaster();
			raster.setDataElements(x, y, w, h, pixels);
		} else {
			image.setRGB(x, y, w, h, pixels, 0, w);
		}
	}

	public final void pixel(float x, float y, int color) {
		int i = index(x, y);
		if (i < 0 || i > pixels.length - 1) {
//			Log.warn("Tried to set a pixel out of bounds. " + i + "/" + (pixels.length - 1));
			return;
		}
		pixels[i] = color;
//		image.setRGB(Math.round(x), Math.round(y), color);
	}

	public final void setRGB(float x, float y, int color) {
		image.setRGB(Math.round(x), Math.round(y), color);
//		image.setRGB((int) x, (int) y, color);
	}

	public final int index(float x, float y) {
		return index(Math.round(x), Math.round(y));
//		return index((int) x, (int) y);
	}

	public final int index(int x, int y) { return x + y * width; }

	public void clear() { clearPixels(0); }

	public final void clearPixels(int color) {
		// On a hi-res display, image may be larger than width/height
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);

		// Create a small array that can be used to set the pixels several times.
		// Using a single-pixel line of length 'width' is a tradeoff between
		// speed (setting each pixel individually is too slow) and memory
		// (an array for width*height would waste lots of memory if it stayed
		// resident, and would terrify the gc if it were re-created on each trip
		// to background().
//	    WritableRaster raster = ((BufferedImage) image).getRaster();
//	    WritableRaster raster = image.getRaster();
		WritableRaster raster = image.getRaster();
		if ((clearPixels == null) || (clearPixels.length < imageWidth)) {
			clearPixels = new int[imageWidth];
		}
		Arrays.fill(clearPixels, 0, imageWidth, color);
		for (int i = 0; i < imageHeight; i++) {
			raster.setDataElements(0, i, imageWidth, 1, clearPixels);
		}
	}
}
