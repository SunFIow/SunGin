package com.sunflow.gfx;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.function.BiFunction;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import com.sunflow.game.GameBase;
import com.sunflow.logging.Log;
import com.sunflow.util.GameUtils;
import com.sunflow.util.MathUtils;
import com.sunflow.util.SConstants;

public class SImage implements Cloneable, SConstants {
	/**
	 * Format for this image, one of RGB, ARGB or ALPHA.
	 * note that RGB images still require 0xff in the high byte
	 * because of how they'll be manipulated by other functions
	 */
	public int format;

	public int[] pixels;

	/** 1 for most images, 2 for hi-dpi/retina */
	public int pixelDensity = 1;

	/** Actual dimensions of pixels array, taking into account the 2x setting. */
	public int pixelWidth;
	public int pixelHeight;

	public int width;
	public int height;

	/**
	 * Path to parent object that will be used with save().
	 * This prevents users from needing savePath() to use SImage.save().
	 */
	public GameBase parent;

	/** modified portion of the image */
	protected boolean modified;
	protected int mx1, my1, mx2, my2;

	/** Loaded pixels flag */
	public boolean loaded = false;

	// colour component bitmasks (moved from PConstants in 2.0b7)
	public static final int ALPHA_MASK = 0xff000000;
	public static final int RED_MASK = 0x00ff0000;
	public static final int GREEN_MASK = 0x0000ff00;
	public static final int BLUE_MASK = 0x000000ff;

	//////////////////////////////////////////////////////////////

	protected SImage() {
		format = ARGB; // default to ARGB images for release 0116
		pixelDensity = 1;
	}

	public SImage(int width, int height) {
		init(width, height, RGB, 1);
	}

	public SImage(int width, int height, int format) {
		init(width, height, format, 1);
	}

	public SImage(int width, int height, int format, int factor) {
		init(width, height, format, factor);
	}

	private void init(int width, int height, int format, int factor) {
		this.width = width;
		this.height = height;
		this.format = format;
		this.pixelDensity = factor;

		this.pixelWidth = width * pixelDensity;
		this.pixelHeight = height * pixelDensity;
		this.pixels = new int[pixelWidth * pixelHeight];

//		defaultSettings();
	}

//	public SImage(BufferedImage bi) {
//		this(bi, RGB);
//	}
//
//	public SImage(BufferedImage bi, int format) {
////		init(bi.getWidth(), bi.getHeight(), bi.getType() == ARGB ? ARGB : RGB);
//		init(bi.getWidth(), bi.getHeight(), format, 1);
//		Graphics2D g = image.createGraphics();
//		g.drawImage(bi, 0, 0, null);
//		g.dispose();
////		image = bi.getSubimage(0, 0, width, height);
//	}

	public SImage(Image img) {
		format = RGB;
		if (img instanceof BufferedImage) {
			BufferedImage bi = (BufferedImage) img;
			width = bi.getWidth();
			height = bi.getHeight();
			int type = bi.getType();
			if (type == BufferedImage.TYPE_3BYTE_BGR ||
					type == BufferedImage.TYPE_4BYTE_ABGR) {
				pixels = new int[width * height];
				bi.getRGB(0, 0, width, height, pixels, 0, width);
				if (type == BufferedImage.TYPE_4BYTE_ABGR) {
					format = ARGB;
				} else {
					opaque();
				}
			} else {
				DataBuffer db = bi.getRaster().getDataBuffer();
				if (db instanceof DataBufferInt) {
					pixels = ((DataBufferInt) db).getData();
					if (type == BufferedImage.TYPE_INT_ARGB) {
						format = ARGB;
					} else if (type == BufferedImage.TYPE_INT_RGB) {
						opaque();
					}
				}
			}
		}
		// Implements fall-through if not DataBufferInt above, or not a
		// known type, or not DataBufferInt for the data itself.
		if (pixels == null) { // go the old school Java 1.0 route
			width = img.getWidth(null);
			height = img.getHeight(null);
			pixels = new int[width * height];
			PixelGrabber pg = new PixelGrabber(img, 0, 0, width, height, pixels, 0, width);
			try {
				pg.grabPixels();
			} catch (InterruptedException e) {}
		}
		pixelDensity = 1;
		pixelWidth = width;
		pixelHeight = height;
	}

//	protected void defaultSettings() { image = new BufferedImage(width, height, format); }

	/**
	 * Use the getNative() method instead, which allows library interfaces to be
	 * written in a cross-platform fashion for desktop, Android, and others.
	 * This is still included for SGraphics objects, which may need the image.
	 */
	public BufferedImage getImage() { // ignore
		return (BufferedImage) getNative();
	}

	/**
	 * Returns a native BufferedImage from this SImage.
	 */
	public Object getNative() { // ignore
		loadPixels();
		int type = (format == RGB) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage image = new BufferedImage(pixelWidth, pixelHeight, type);
		WritableRaster wr = image.getRaster();
		wr.setDataElements(0, 0, pixelWidth, pixelHeight, pixels);
		return image;
	}

	/** Set the high bits of all pixels to opaque. */
	protected void opaque() {
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = 0xFF000000 | pixels[i];
		}
	}
	//////////////////////////////////////////////////////////////

	// MARKING IMAGE AS MODIFIED / FOR USE w/ GET/SET

	public boolean isModified() { // ignore
		return modified;
	}

	public void setModified() { // ignore
		modified = true;
		mx1 = 0;
		my1 = 0;
		mx2 = width;
		my2 = height;
	}

	public void setModified(boolean m) { // ignore
		modified = m;
	}

	public int getModifiedX1() { // ignore
		return mx1;
	}

	public int getModifiedX2() { // ignore
		return mx2;
	}

	public int getModifiedY1() { // ignore
		return my1;
	}

	public int getModifiedY2() { // ignore
		return my2;
	}

	public void loadPixels() { // ignore
		if (pixels == null || pixels.length != pixelWidth * pixelHeight) {
			pixels = new int[pixelWidth * pixelHeight];
		}
		setLoaded();
	}

	public void updatePixels() {
		updatePixels(0, 0, width, height);
	}

	public void updatePixels(int x, int y, int w, int h) { // ignore
		int x2 = x + w;
		int y2 = y + h;

		if (!modified) {
			mx1 = MathUtils.instance.max(0, x);
			mx2 = MathUtils.instance.min(pixelWidth, x2);
			my1 = MathUtils.instance.max(0, y);
			my2 = MathUtils.instance.min(pixelHeight, y2);
			modified = true;

		} else {
			if (x < mx1) mx1 = MathUtils.instance.max(0, x);
			if (x > mx2) mx2 = MathUtils.instance.min(pixelWidth, x);
			if (y < my1) my1 = MathUtils.instance.max(0, y);
			if (y > my2) my2 = MathUtils.instance.min(pixelHeight, y);

			if (x2 < mx1) mx1 = MathUtils.instance.max(0, x2);
			if (x2 > mx2) mx2 = MathUtils.instance.min(pixelWidth, x2);
			if (y2 < my1) my1 = MathUtils.instance.max(0, y2);
			if (y2 > my2) my2 = MathUtils.instance.min(pixelHeight, y2);
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException { // ignore
		return get();
	}

//	final public int[] loadPixels2() {
//		DataBuffer buffer = ((BufferedImage) getNative()).getRaster().getDataBuffer();
//		if (buffer instanceof DataBufferInt) {
//			pixels = ((DataBufferInt) buffer).getData();
//		} else if (buffer instanceof DataBufferByte) {
//			byte[] bytes = ((DataBufferByte) buffer).getData();
//			bytes = (byte[]) ((BufferedImage) getNative()).getRaster().getDataElements(0, 0, ((BufferedImage) getNative()).getWidth(), ((BufferedImage) getNative()).getHeight(), null);
//			this.pixels = new int[bytes.length];
//			for (int i = 0; i < bytes.length; i++) this.pixels[i] = bytes[i];
//		} else Log.error(buffer + " isn't of type Int / Byte");
//
//		return pixels;
//	}
//
//	final public int[][] loadPixels2D() {
//		pixels = ((DataBufferInt) ((BufferedImage) getNative()).getRaster().getDataBuffer()).getData();
//
//		final int width = image.getWidth();
//		final int height = image.getHeight();
//		final boolean hasAlphaChannel = image.getAlphaRaster() != null;
//
//		int[][] result = new int[width][height];
//		if (hasAlphaChannel) {
//			final int pixelLength = 4;
//			for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
//				int argb = 0;
//				argb += ((pixels[pixel] & 0xff) << 24); // alpha
//				argb += (pixels[pixel + 1] & 0xff); // blue
//				argb += ((pixels[pixel + 2] & 0xff) << 8); // green
//				argb += ((pixels[pixel + 3] & 0xff) << 16); // red
//				result[col][row] = argb;
//				col++;
//				if (col == width) {
//					col = 0;
//					row++;
//				}
//			}
//		} else {
//			final int pixelLength = 3;
//			for (int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += pixelLength) {
//				int argb = 0;
//				argb += -16777216; // 255 alpha
//				argb += (pixels[pixel] & 0xff); // blue
//				argb += ((pixels[pixel + 1] & 0xff) << 8); // green
//				argb += ((pixels[pixel + 2] & 0xff) << 16); // red
//				result[col][row] = argb;
//				col++;
//				if (col == width) {
//					col = 0;
//					row++;
//				}
//			}
//		}
//
//		return result;
//	}
//
//	final public void updatePixels2() { image.getRaster().setDataElements(0, 0, image.getWidth(), image.getHeight(), pixels); }
//
//	final public void updatePixels2(int[] data) { image.getRaster().setDataElements(0, 0, image.getWidth(), image.getHeight(), data); }
//
//	final public void updatePixels2(int x, int y, int w, int h, int[] pixels) {
//		if (pixels == null || w == 0 || h == 0) return;
//		else if (pixels.length < w * h) throw new IllegalArgumentException("pixels array must have a length" + " >= w*h");
//
//		if (format == ARGB || format == RGB) {
//			WritableRaster raster = image.getRaster();
//			raster.setDataElements(x, y, w, h, pixels);
//		} else {
//			image.setRGB(x, y, w, h, pixels, 0, w);
//		}
//	}

	final public void resize(float width, float height) { resize((int) width, (int) height); }

	final public void resize(int w, int h) {
		if (w <= 0 && h <= 0) {
			throw new IllegalArgumentException("width or height must be > 0 for resize");
		}

		if (w == 0) { // Use height to determine relative size
			float diff = (float) h / height;
			w = (int) (width * diff);
		} else if (h == 0) { // Use the width to determine relative size
			float diff = (float) w / width;
			h = (int) (height * diff);
		}

		BufferedImage img = shrinkImage((BufferedImage) getNative(), w * pixelDensity, h * pixelDensity);

		SImage temp = new SImage(img);
		this.pixelWidth = temp.width;
		this.pixelHeight = temp.height;

		// Get the resized pixel array
		this.pixels = temp.pixels;

		this.width = pixelWidth / pixelDensity;
		this.height = pixelHeight / pixelDensity;

		// Mark the pixels array as altered
		updatePixels();

//		BufferedImage oldImage = image.getSubimage(0, 0, this.width, this.height);
//
//		this.width = width;
//		this.height = height;
//		this.pixels = new int[width * height];
//
//		image = new BufferedImage(width, height, format);
//		Graphics2D g = image.createGraphics();
//		g.drawImage(oldImage, 0, 0, null);
//		g.dispose();
////		image = image.getSubimage(0, 0, width, height);
	}

	// Adapted from getFasterScaledInstance() method from page 111 of
	// "Filthy Rich Clients" by Chet Haase and Romain Guy
	// Additional modifications and simplifications have been added,
	// plus a fix to deal with an infinite loop if images are expanded.
	// http://code.google.com/p/processing/issues/detail?id=1463
	static private BufferedImage shrinkImage(BufferedImage img,
			int targetWidth, int targetHeight) {
		int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage outgoing = img;
		BufferedImage scratchImage = null;
		Graphics2D g2 = null;
		int prevW = outgoing.getWidth();
		int prevH = outgoing.getHeight();
		boolean isTranslucent = img.getTransparency() != Transparency.OPAQUE;

		// Use multi-step technique: start with original size, then scale down in
		// multiple passes with drawImage() until the target size is reached
		int w = img.getWidth();
		int h = img.getHeight();

		do {
			if (w > targetWidth) {
				w /= 2;
				// if this is the last step, do the exact size
				if (w < targetWidth) {
					w = targetWidth;
				}
			} else if (targetWidth >= w) {
				w = targetWidth;
			}
			if (h > targetHeight) {
				h /= 2;
				if (h < targetHeight) {
					h = targetHeight;
				}
			} else if (targetHeight >= h) {
				h = targetHeight;
			}
			if (scratchImage == null || isTranslucent) {
				// Use a single scratch buffer for all iterations and then copy
				// to the final, correctly-sized image before returning
				scratchImage = new BufferedImage(w, h, type);
				g2 = scratchImage.createGraphics();
			}
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.drawImage(outgoing, 0, 0, w, h, 0, 0, prevW, prevH, null);
			prevW = w;
			prevH = h;
			outgoing = scratchImage;
		} while (w != targetWidth || h != targetHeight);

		if (g2 != null) {
			g2.dispose();
		}

		// If we used a scratch buffer that is larger than our target size,
		// create an image of the right size and copy the results into it
		if (targetWidth != outgoing.getWidth() ||
				targetHeight != outgoing.getHeight()) {
			scratchImage = new BufferedImage(targetWidth, targetHeight, type);
			g2 = scratchImage.createGraphics();
			g2.drawImage(outgoing, 0, 0, null);
			g2.dispose();
			outgoing = scratchImage;
		}
		return outgoing;
	}

	//////////////////////////////////////////////////////////////

	// MARKING IMAGE AS LOADED / FOR USE IN RENDERERS

	public boolean isLoaded() { // ignore
		return loaded;
	}

	public void setLoaded() { // ignore
		loaded = true;
	}

	public void setLoaded(boolean l) { // ignore
		loaded = l;
	}

	//////////////////////////////////////////////////////////////

	// GET/SET PIXELS

	/**
	 * <h3>Advanced</h3>
	 * Returns an ARGB "color" type (a packed 32 bit int with the color.
	 * If the coordinate is outside the image, zero is returned
	 * (black, but completely transparent).
	 * <P>
	 * If the image is in RGB format (i.e. on a PVideo object),
	 * the value will get its high bits set, just to avoid cases where
	 * they haven't been set already.
	 * <P>
	 * If the image is in ALPHA format, this returns a white with its
	 * alpha value set.
	 * <P>
	 * This function is included primarily for beginners. It is quite
	 * slow because it has to check to see if the x, y that was provided
	 * is inside the bounds, and then has to check to see what image
	 * type it is. If you want things to be more efficient, access the
	 * pixels[] array directly.
	 *
	 * @webref image:pixels
	 * @brief Reads the color of any pixel or grabs a rectangle of pixels
	 * @usage web_application
	 * @param x
	 *            x-coordinate of the pixel
	 * @param y
	 *            y-coordinate of the pixel
	 */
	public int get(int x, int y) {
		if ((x < 0) || (y < 0) || (x >= pixelWidth) || (y >= pixelHeight)) return 0;

		switch (format) {
			case RGB:
				return pixels[y * pixelWidth + x] | 0xff000000;

			case ARGB:
				return pixels[y * pixelWidth + x];

			case ALPHA:
				return (pixels[y * pixelWidth + x] << 24) | 0xffffff;
		}
		return 0;
	}

	/**
	 * @param w
	 *            width of pixel rectangle to get
	 * @param h
	 *            height of pixel rectangle to get
	 */
	public SImage get(int x, int y, int w, int h) {
		int targetX = 0;
		int targetY = 0;
		int targetWidth = w;
		int targetHeight = h;
		boolean cropped = false;

		if (x < 0) {
			w += x; // x is negative, removes the left edge from the width
			targetX = -x;
			cropped = true;
			x = 0;
		}
		if (y < 0) {
			h += y; // y is negative, clip the number of rows
			targetY = -y;
			cropped = true;
			y = 0;
		}

		if (x + w > pixelWidth) {
			w = pixelWidth - x;
			cropped = true;
		}
		if (y + h > pixelHeight) {
			h = pixelHeight - y;
			cropped = true;
		}

		if (w < 0) {
			w = 0;
		}
		if (h < 0) {
			h = 0;
		}

		int targetFormat = format;
		if (cropped && format == RGB) {
			targetFormat = ARGB;
		}

		SImage target = new SImage(targetWidth / pixelDensity,
				targetHeight / pixelDensity,
				targetFormat, pixelDensity);
//	    target.parent = parent;  // parent may be null so can't use createImage()
		if (w > 0 && h > 0) {
			getImpl(x, y, w, h, target, targetX, targetY);
		}
		return target;
	}

	/**
	 * Returns a copy of this SImage. Equivalent to get(0, 0, width, height).
	 * Deprecated, just use copy() instead.
	 */
	public SImage get() {
		// Formerly this used clone(), which caused memory problems.
		// http://code.google.com/p/processing/issues/detail?id=42
		return get(0, 0, pixelWidth, pixelHeight);
	}

	public SImage copy() {
		return get(0, 0, pixelWidth, pixelHeight);
	}

	/**
	 * Internal function to actually handle getting a block of pixels that
	 * has already been properly cropped to a valid region. That is, x/y/w/h
	 * are guaranteed to be inside the image space, so the implementation can
	 * use the fastest possible pixel copying method.
	 */
	protected void getImpl(int sourceX, int sourceY,
			int sourceWidth, int sourceHeight,
			SImage target, int targetX, int targetY) {
		int sourceIndex = sourceY * pixelWidth + sourceX;
		int targetIndex = targetY * target.pixelWidth + targetX;
		for (int row = 0; row < sourceHeight; row++) {
			System.arraycopy(pixels, sourceIndex, target.pixels, targetIndex, sourceWidth);
			sourceIndex += pixelWidth;
			targetIndex += target.pixelWidth;
		}
	}

	public final void pixel(float x, float y, int color) {
		int i = index(x, y);
		if (i < 0 || i >= pixels.length) {
			Log.warn("Tried to set a pixel out of bounds. " + i + "/" + (pixels.length - 1));
			return;
		}
		pixels[i] = color;
	}

	public void set(int x, int y, int c) {
		if ((x < 0) || (y < 0) || (x >= pixelWidth) || (y >= pixelHeight)) return;
		pixels[y * pixelWidth + x] = c;
		updatePixels(x, y, 1, 1); // slow...
	}

//	public final void setRGB(float x, float y, int color) {
////		img.setRGB(Math.round(x), Math.round(y), color);
////		image.setRGB((int) x, (int) y, color);
//	}

	/**
	 * <h3>Advanced</h3>
	 * Efficient method of drawing an image's pixels directly to this surface.
	 * No variations are employed, meaning that any scale, tint, or imageMode
	 * settings will be ignored.
	 *
	 * @param img
	 *            image to copy into the original image
	 */
	public void set(int x, int y, SImage img) {
		int sx = 0;
		int sy = 0;
		int sw = img.pixelWidth;
		int sh = img.pixelHeight;

		if (x < 0) { // off left edge
			sx -= x;
			sw += x;
			x = 0;
		}
		if (y < 0) { // off top edge
			sy -= y;
			sh += y;
			y = 0;
		}
		if (x + sw > pixelWidth) { // off right edge
			sw = pixelWidth - x;
		}
		if (y + sh > pixelHeight) { // off bottom edge
			sh = pixelHeight - y;
		}

		// this could be nonexistent
		if ((sw <= 0) || (sh <= 0)) return;

		setImpl(img, sx, sy, sw, sh, x, y);
	}

	/**
	 * Internal function to actually handle setting a block of pixels that
	 * has already been properly cropped from the image to a valid region.
	 */
	protected void setImpl(SImage sourceImage,
			int sourceX, int sourceY,
			int sourceWidth, int sourceHeight,
			int targetX, int targetY) {
		int sourceOffset = sourceY * sourceImage.pixelWidth + sourceX;
		int targetOffset = targetY * pixelWidth + targetX;

		for (int y = sourceY; y < sourceY + sourceHeight; y++) {
			System.arraycopy(sourceImage.pixels, sourceOffset, pixels, targetOffset, sourceWidth);
			sourceOffset += sourceImage.pixelWidth;
			targetOffset += pixelWidth;
		}

		// updatePixelsImpl(targetX, targetY, sourceWidth, sourceHeight);
		updatePixels(targetX, targetY, sourceWidth, sourceHeight);
	}

	public final int index(float x, float y) {
		return index(Math.round(x), Math.round(y));
//		return index((int) x, (int) y);
	}

	public final int index(int x, int y) { return x + y * width; }

	public void filterOPAQUE() {
		loadPixels();
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] |= 0xff000000;
		}
		format = RGB;
		updatePixels(); // mark as modified
	}

	static public int blendColor(int c1, int c2, int mode) { // ignore
		switch (mode) {
			case REPLACE:
				return c2;
			case BLEND:
				return blend_blend(c1, c2);
			case ADD:
				return blend_add_pin(c1, c2);
			case SUBTRACT:
				return blend_sub_pin(c1, c2);

			case LIGHTEST:
				return blend_lightest(c1, c2);
			case DARKEST:
				return blend_darkest(c1, c2);

			case DIFFERENCE:
				return blend_difference(c1, c2);
			case EXCLUSION:
				return blend_exclusion(c1, c2);

			case MULTIPLY:
				return blend_multiply(c1, c2);
			case SCREEN:
				return blend_screen(c1, c2);

			case HARD_LIGHT:
				return blend_hard_light(c1, c2);
			case SOFT_LIGHT:
				return blend_soft_light(c1, c2);
			case OVERLAY:
				return blend_overlay(c1, c2);

			case DODGE:
				return blend_dodge(c1, c2);
			case BURN:
				return blend_burn(c1, c2);
		}
		return 0;
	}

	static public BiFunction<Integer, Integer, Integer> getBlendColorFunc(int mode) { // ignore
		switch (mode) {
			case REPLACE:
				return (c1, c2) -> c2;
			case BLEND:
				return SImage::blend_blend;
			case ADD:
				return SImage::blend_add_pin;
			case SUBTRACT:
				return SImage::blend_sub_pin;

			case LIGHTEST:
				return SImage::blend_lightest;
			case DARKEST:
				return SImage::blend_darkest;

			case DIFFERENCE:
				return SImage::blend_difference;
			case EXCLUSION:
				return SImage::blend_exclusion;

			case MULTIPLY:
				return SImage::blend_multiply;
			case SCREEN:
				return SImage::blend_screen;

			case HARD_LIGHT:
				return SImage::blend_hard_light;
			case SOFT_LIGHT:
				return SImage::blend_soft_light;
			case OVERLAY:
				return SImage::blend_overlay;

			case DODGE:
				return SImage::blend_dodge;
			case BURN:
				return SImage::blend_burn;
		}
		return (c1, c2) -> 0;
	}

	//////////////////////////////////////////////////////////////

	// internal blending methods

	private static int min(int a, int b) {
		return (a < b) ? a : b;
	}

	private static int max(int a, int b) {
		return (a > b) ? a : b;
	}

	/////////////////////////////////////////////////////////////

	// BLEND MODE IMPLEMENTATIONS

	/*
	 * Jakub Valtar
	 * All modes use SRC alpha to interpolate between DST and the result of
	 * the operation:
	 * R = (1 - SRC_ALPHA) * DST + SRC_ALPHA * <RESULT OF THE OPERATION>
	 * Comments above each mode only specify the formula of its operation.
	 * These implementations treat alpha 127 (=255/2) as a perfect 50 % mix.
	 * One alpha value between 126 and 127 is intentionally left out,
	 * so the step 126 -> 127 is twice as big compared to other steps.
	 * This is because our colors are in 0..255 range, but we divide
	 * by right shifting 8 places (=256) which is much faster than
	 * (correct) float division by 255.0f. The missing value was placed
	 * between 126 and 127, because limits of the range (near 0 and 255) and
	 * the middle value (127) have to blend correctly.
	 * Below you will often see RED and BLUE channels (RB) manipulated together
	 * and GREEN channel (GN) manipulated separately. It is sometimes possible
	 * because the operation won't use more than 16 bits, so we process the RED
	 * channel in the upper 16 bits and BLUE channel in the lower 16 bits. This
	 * decreases the number of operations per pixel and thus makes things faster.
	 * Some of the modes are hand tweaked (various +1s etc.) to be more accurate
	 * and to produce correct values in extremes. Below is a sketch you can use
	 * to check any blending function for
	 * 1) Discrepancies between color channels:
	 * - highlighted by the offending color
	 * 2) Behavior at extremes (set colorCount to 256):
	 * - values of all corners are printed to the console
	 * 3) Rounding errors:
	 * - set colorCount to lower value to better see color bands
	 * // use powers of 2 in range 2..256
	 * // to better see color bands
	 * final int colorCount = 256;
	 * final int blockSize = 3;
	 * void settings() {
	 * size(blockSize * 256, blockSize * 256);
	 * }
	 * void setup() { }
	 * void draw() {
	 * noStroke();
	 * colorMode(RGB, colorCount-1);
	 * int alpha = (mouseX / blockSize) << 24;
	 * int r, g, b, r2, g2, b2 = 0;
	 * for (int x = 0; x <= 0xFF; x++) {
	 * for (int y = 0; y <= 0xFF; y++) {
	 * int dst = (x << 16) | (x << 8) | x;
	 * int src = (y << 16) | (y << 8) | y | alpha;
	 * int result = testFunction(dst, src);
	 * r = r2 = (result >> 16 & 0xFF);
	 * g = g2 = (result >> 8 & 0xFF);
	 * b = b2 = (result >> 0 & 0xFF);
	 * if (r != g && r != b) r2 = (128 + r2) % 255;
	 * if (g != r && g != b) g2 = (128 + g2) % 255;
	 * if (b != r && b != g) b2 = (128 + b2) % 255;
	 * fill(r2 % colorCount, g2 % colorCount, b2 % colorCount);
	 * rect(x * blockSize, y * blockSize, blockSize, blockSize);
	 * }
	 * }
	 * println(
	 * "alpha:", mouseX/blockSize,
	 * "TL:", hex(get(0, 0)),
	 * "TR:", hex(get(width-1, 0)),
	 * "BR:", hex(get(width-1, height-1)),
	 * "BL:", hex(get(0, height-1)));
	 * }
	 * int testFunction(int dst, int src) {
	 * // your function here
	 * return dst;
	 * }
	 */

	private static final int RB_MASK = 0x00FF00FF;
	private static final int GN_MASK = 0x0000FF00;

	/**
	 * Blend
	 * O = S
	 */
	private static int blend_blend(int dst, int src) {
		int a = src >>> 24;

		int s_a = a + (a >= 0x7F ? 1 : 0);
		int d_a = 0x100 - s_a;

		return min((dst >>> 24) + a, 0xFF) << 24 |
				((dst & RB_MASK) * d_a + (src & RB_MASK) * s_a) >>> 8 & RB_MASK |
				((dst & GN_MASK) * d_a + (src & GN_MASK) * s_a) >>> 8 & GN_MASK;
	}

	/**
	 * Add
	 * O = MIN(D + S, 1)
	 */
	private static int blend_add_pin(int dst, int src) {
		int a = src >>> 24;

		int s_a = a + (a >= 0x7F ? 1 : 0);

		int rb = (dst & RB_MASK) + ((src & RB_MASK) * s_a >>> 8 & RB_MASK);
		int gn = (dst & GN_MASK) + ((src & GN_MASK) * s_a >>> 8);

		return min((dst >>> 24) + a, 0xFF) << 24 |
				min(rb & 0xFFFF0000, RED_MASK) |
				min(gn & 0x00FFFF00, GREEN_MASK) |
				min(rb & 0x0000FFFF, BLUE_MASK);
	}

	/**
	 * Subtract
	 * O = MAX(0, D - S)
	 */
	private static int blend_sub_pin(int dst, int src) {
		int a = src >>> 24;

		int s_a = a + (a >= 0x7F ? 1 : 0);

		int rb = ((src & RB_MASK) * s_a >>> 8);
		int gn = ((src & GREEN_MASK) * s_a >>> 8);

		return min((dst >>> 24) + a, 0xFF) << 24 |
				max((dst & RED_MASK) - (rb & RED_MASK), 0) |
				max((dst & GREEN_MASK) - (gn & GREEN_MASK), 0) |
				max((dst & BLUE_MASK) - (rb & BLUE_MASK), 0);
	}

	/**
	 * Lightest
	 * O = MAX(D, S)
	 */
	private static int blend_lightest(int dst, int src) {
		int a = src >>> 24;

		int s_a = a + (a >= 0x7F ? 1 : 0);
		int d_a = 0x100 - s_a;

		int rb = max(src & RED_MASK, dst & RED_MASK) |
				max(src & BLUE_MASK, dst & BLUE_MASK);
		int gn = max(src & GREEN_MASK, dst & GREEN_MASK);

		return min((dst >>> 24) + a, 0xFF) << 24 |
				((dst & RB_MASK) * d_a + rb * s_a) >>> 8 & RB_MASK |
				((dst & GN_MASK) * d_a + gn * s_a) >>> 8 & GN_MASK;
	}

	/**
	 * Darkest
	 * O = MIN(D, S)
	 */
	private static int blend_darkest(int dst, int src) {
		int a = src >>> 24;

		int s_a = a + (a >= 0x7F ? 1 : 0);
		int d_a = 0x100 - s_a;

		int rb = min(src & RED_MASK, dst & RED_MASK) |
				min(src & BLUE_MASK, dst & BLUE_MASK);
		int gn = min(src & GREEN_MASK, dst & GREEN_MASK);

		return min((dst >>> 24) + a, 0xFF) << 24 |
				((dst & RB_MASK) * d_a + rb * s_a) >>> 8 & RB_MASK |
				((dst & GN_MASK) * d_a + gn * s_a) >>> 8 & GN_MASK;
	}

	/**
	 * Difference
	 * O = ABS(D - S)
	 */
	private static int blend_difference(int dst, int src) {
		int a = src >>> 24;

		int s_a = a + (a >= 0x7F ? 1 : 0);
		int d_a = 0x100 - s_a;

		int r = (dst & RED_MASK) - (src & RED_MASK);
		int b = (dst & BLUE_MASK) - (src & BLUE_MASK);
		int g = (dst & GREEN_MASK) - (src & GREEN_MASK);

		int rb = (r < 0 ? -r : r) |
				(b < 0 ? -b : b);
		int gn = (g < 0 ? -g : g);

		return min((dst >>> 24) + a, 0xFF) << 24 |
				((dst & RB_MASK) * d_a + rb * s_a) >>> 8 & RB_MASK |
				((dst & GN_MASK) * d_a + gn * s_a) >>> 8 & GN_MASK;
	}

	/**
	 * Exclusion
	 * O = (1 - S)D + S(1 - D)
	 * O = D + S - 2DS
	 */
	private static int blend_exclusion(int dst, int src) {
		int a = src >>> 24;

		int s_a = a + (a >= 0x7F ? 1 : 0);
		int d_a = 0x100 - s_a;

		int d_rb = dst & RB_MASK;
		int d_gn = dst & GN_MASK;

		int s_gn = src & GN_MASK;

		int f_r = (dst & RED_MASK) >> 16;
		int f_b = (dst & BLUE_MASK);

		int rb_sub = ((src & RED_MASK) * (f_r + (f_r >= 0x7F ? 1 : 0)) |
				(src & BLUE_MASK) * (f_b + (f_b >= 0x7F ? 1 : 0))) >>> 7 & 0x01FF01FF;
		int gn_sub = s_gn * (d_gn + (d_gn >= 0x7F00 ? 0x100 : 0)) >>> 15 & 0x0001FF00;

		return min((dst >>> 24) + a, 0xFF) << 24 |
				(d_rb * d_a + (d_rb + (src & RB_MASK) - rb_sub) * s_a) >>> 8 & RB_MASK |
				(d_gn * d_a + (d_gn + s_gn - gn_sub) * s_a) >>> 8 & GN_MASK;
	}

	/*
	 * Multiply
	 * O = DS
	 */
	private static int blend_multiply(int dst, int src) {
		int a = src >>> 24;

		int s_a = a + (a >= 0x7F ? 1 : 0);
		int d_a = 0x100 - s_a;

		int d_gn = dst & GN_MASK;

		int f_r = (dst & RED_MASK) >> 16;
		int f_b = (dst & BLUE_MASK);

		int rb = ((src & RED_MASK) * (f_r + 1) |
				(src & BLUE_MASK) * (f_b + 1)) >>> 8 & RB_MASK;
		int gn = (src & GREEN_MASK) * (d_gn + 0x100) >>> 16 & GN_MASK;

		return min((dst >>> 24) + a, 0xFF) << 24 |
				((dst & RB_MASK) * d_a + rb * s_a) >>> 8 & RB_MASK |
				(d_gn * d_a + gn * s_a) >>> 8 & GN_MASK;
	}

	/**
	 * Screen
	 * O = 1 - (1 - D)(1 - S)
	 * O = D + S - DS
	 */
	private static int blend_screen(int dst, int src) {
		int a = src >>> 24;

		int s_a = a + (a >= 0x7F ? 1 : 0);
		int d_a = 0x100 - s_a;

		int d_rb = dst & RB_MASK;
		int d_gn = dst & GN_MASK;

		int s_gn = src & GN_MASK;

		int f_r = (dst & RED_MASK) >> 16;
		int f_b = (dst & BLUE_MASK);

		int rb_sub = ((src & RED_MASK) * (f_r + 1) |
				(src & BLUE_MASK) * (f_b + 1)) >>> 8 & RB_MASK;
		int gn_sub = s_gn * (d_gn + 0x100) >>> 16 & GN_MASK;

		return min((dst >>> 24) + a, 0xFF) << 24 |
				(d_rb * d_a + (d_rb + (src & RB_MASK) - rb_sub) * s_a) >>> 8 & RB_MASK |
				(d_gn * d_a + (d_gn + s_gn - gn_sub) * s_a) >>> 8 & GN_MASK;
	}

	/**
	 * Overlay
	 * O = 2 * MULTIPLY(D, S) = 2DS for D < 0.5
	 * O = 2 * SCREEN(D, S) - 1 = 2(S + D - DS) - 1 otherwise
	 */
	private static int blend_overlay(int dst, int src) {
		int a = src >>> 24;

		int s_a = a + (a >= 0x7F ? 1 : 0);
		int d_a = 0x100 - s_a;

		int d_r = dst & RED_MASK;
		int d_g = dst & GREEN_MASK;
		int d_b = dst & BLUE_MASK;

		int s_r = src & RED_MASK;
		int s_g = src & GREEN_MASK;
		int s_b = src & BLUE_MASK;

		int r = (d_r < 0x800000) ? d_r * ((s_r >>> 16) + 1) >>> 7 : 0xFF0000 - ((0x100 - (s_r >>> 16)) * (RED_MASK - d_r) >>> 7);
		int g = (d_g < 0x8000) ? d_g * (s_g + 0x100) >>> 15 : (0xFF00 - ((0x10000 - s_g) * (GREEN_MASK - d_g) >>> 15));
		int b = (d_b < 0x80) ? d_b * (s_b + 1) >>> 7 : (0xFF00 - ((0x100 - s_b) * (BLUE_MASK - d_b) << 1)) >>> 8;

		return min((dst >>> 24) + a, 0xFF) << 24 |
				((dst & RB_MASK) * d_a + ((r | b) & RB_MASK) * s_a) >>> 8 & RB_MASK |
				((dst & GN_MASK) * d_a + (g & GN_MASK) * s_a) >>> 8 & GN_MASK;
	}

	/**
	 * Hard Light
	 * O = OVERLAY(S, D)
	 *
	 * O = 2 * MULTIPLY(D, S) = 2DS for S < 0.5
	 * O = 2 * SCREEN(D, S) - 1 = 2(S + D - DS) - 1 otherwise
	 */
	private static int blend_hard_light(int dst, int src) {
		int a = src >>> 24;

		int s_a = a + (a >= 0x7F ? 1 : 0);
		int d_a = 0x100 - s_a;

		int d_r = dst & RED_MASK;
		int d_g = dst & GREEN_MASK;
		int d_b = dst & BLUE_MASK;

		int s_r = src & RED_MASK;
		int s_g = src & GREEN_MASK;
		int s_b = src & BLUE_MASK;

		int r = (s_r < 0x800000) ? s_r * ((d_r >>> 16) + 1) >>> 7 : 0xFF0000 - ((0x100 - (d_r >>> 16)) * (RED_MASK - s_r) >>> 7);
		int g = (s_g < 0x8000) ? s_g * (d_g + 0x100) >>> 15 : (0xFF00 - ((0x10000 - d_g) * (GREEN_MASK - s_g) >>> 15));
		int b = (s_b < 0x80) ? s_b * (d_b + 1) >>> 7 : (0xFF00 - ((0x100 - d_b) * (BLUE_MASK - s_b) << 1)) >>> 8;

		return min((dst >>> 24) + a, 0xFF) << 24 |
				((dst & RB_MASK) * d_a + ((r | b) & RB_MASK) * s_a) >>> 8 & RB_MASK |
				((dst & GN_MASK) * d_a + (g & GN_MASK) * s_a) >>> 8 & GN_MASK;
	}

	/**
	 * Soft Light (Pegtop)
	 * O = (1 - D) * MULTIPLY(D, S) + D * SCREEN(D, S)
	 * O = (1 - D) * DS + D * (1 - (1 - D)(1 - S))
	 * O = 2DS + DD - 2DDS
	 */
	private static int blend_soft_light(int dst, int src) {
		int a = src >>> 24;

		int s_a = a + (a >= 0x7F ? 1 : 0);
		int d_a = 0x100 - s_a;

		int d_r = dst & RED_MASK;
		int d_g = dst & GREEN_MASK;
		int d_b = dst & BLUE_MASK;

		int s_r1 = src & RED_MASK >> 16;
		int s_g1 = src & GREEN_MASK >> 8;
		int s_b1 = src & BLUE_MASK;

		int d_r1 = (d_r >> 16) + (s_r1 < 7F ? 1 : 0);
		int d_g1 = (d_g >> 8) + (s_g1 < 7F ? 1 : 0);
		int d_b1 = d_b + (s_b1 < 7F ? 1 : 0);

		int r = (s_r1 * d_r >> 7) + 0xFF * d_r1 * (d_r1 + 1) -
				((s_r1 * d_r1 * d_r1) << 1) & RED_MASK;
		int g = (s_g1 * d_g << 1) + 0xFF * d_g1 * (d_g1 + 1) -
				((s_g1 * d_g1 * d_g1) << 1) >>> 8 & GREEN_MASK;
		int b = (s_b1 * d_b << 9) + 0xFF * d_b1 * (d_b1 + 1) -
				((s_b1 * d_b1 * d_b1) << 1) >>> 16;

		return min((dst >>> 24) + a, 0xFF) << 24 |
				((dst & RB_MASK) * d_a + (r | b) * s_a) >>> 8 & RB_MASK |
				((dst & GN_MASK) * d_a + g * s_a) >>> 8 & GN_MASK;
	}

	/**
	 * Dodge
	 * O = D / (1 - S)
	 */
	private static int blend_dodge(int dst, int src) {
		int a = src >>> 24;

		int s_a = a + (a >= 0x7F ? 1 : 0);
		int d_a = 0x100 - s_a;

		int r = (dst & RED_MASK) / (256 - ((src & RED_MASK) >> 16));
		int g = ((dst & GREEN_MASK) << 8) / (256 - ((src & GREEN_MASK) >> 8));
		int b = ((dst & BLUE_MASK) << 8) / (256 - (src & BLUE_MASK));

		int rb = (r > 0xFF00 ? 0xFF0000 : ((r << 8) & RED_MASK)) |
				(b > 0x00FF ? 0x0000FF : b);
		int gn = (g > 0xFF00 ? 0x00FF00 : (g & GREEN_MASK));

		return min((dst >>> 24) + a, 0xFF) << 24 |
				((dst & RB_MASK) * d_a + rb * s_a) >>> 8 & RB_MASK |
				((dst & GN_MASK) * d_a + gn * s_a) >>> 8 & GN_MASK;
	}

	/**
	 * Burn
	 * O = 1 - (1 - A) / B
	 */
	private static int blend_burn(int dst, int src) {
		int a = src >>> 24;

		int s_a = a + (a >= 0x7F ? 1 : 0);
		int d_a = 0x100 - s_a;

		int r = ((0xFF0000 - (dst & RED_MASK))) / (1 + (src & RED_MASK >> 16));
		int g = ((0x00FF00 - (dst & GREEN_MASK)) << 8) / (1 + (src & GREEN_MASK >> 8));
		int b = ((0x0000FF - (dst & BLUE_MASK)) << 8) / (1 + (src & BLUE_MASK));

		int rb = RB_MASK -
				(r > 0xFF00 ? 0xFF0000 : ((r << 8) & RED_MASK)) -
				(b > 0x00FF ? 0x0000FF : b);
		int gn = GN_MASK -
				(g > 0xFF00 ? 0x00FF00 : (g & GREEN_MASK));

		return min((dst >>> 24) + a, 0xFF) << 24 |
				((dst & RB_MASK) * d_a + rb * s_a) >>> 8 & RB_MASK |
				((dst & GN_MASK) * d_a + gn * s_a) >>> 8 & GN_MASK;
	}

	protected String[] saveImageFormats;

	public boolean save(String filename) { // ignore
		boolean success = false;

		if (parent != null) {
			// use savePath(), so that the intermediate directories are created
			filename = parent.savePath(filename);

		} else {
			File file = new File(filename);
			if (file.isAbsolute()) {
				// make sure that the intermediate folders have been created
				GameUtils.instance.createPath(file);
			} else {
				String msg = "SImage.save() requires an absolute path. " +
						"Use createImage(), or pass savePath() to save().";
				SGraphics.showException(msg);
			}
		}

		// Make sure the pixel data is ready to go
		loadPixels();

		try {
			OutputStream os = null;

			if (saveImageFormats == null) {
				saveImageFormats = javax.imageio.ImageIO.getWriterFormatNames();
			}
			if (saveImageFormats != null) {
				for (int i = 0; i < saveImageFormats.length; i++) {
					if (filename.endsWith("." + saveImageFormats[i])) {
						if (!saveImageIO(filename)) {
							System.err.println("Error while saving image.");
							return false;
						}
						return true;
					}
				}
			}

			if (filename.toLowerCase().endsWith(".tga")) {
				os = new BufferedOutputStream(new FileOutputStream(filename), 32768);
				success = saveTGA(os); // , pixels, width, height, format);

			} else {
				if (!filename.toLowerCase().endsWith(".tif") &&
						!filename.toLowerCase().endsWith(".tiff")) {
					// if no .tif extension, add it..
					filename += ".tif";
				}
				os = new BufferedOutputStream(new FileOutputStream(filename), 32768);
				success = saveTIFF(os); // , pixels, width, height);
			}
			os.flush();
			os.close();

		} catch (IOException e) {
			System.err.println("Error while saving image.");
			e.printStackTrace();
			success = false;
		}
		return success;
	}

	//////////////////////////////////////////////////////////////

	// FILE I/O

	static byte TIFF_HEADER[] = {
			77, 77, 0, 42, 0, 0, 0, 8, 0, 9, 0, -2, 0, 4, 0, 0, 0, 1, 0, 0,
			0, 0, 1, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 0, 3, 0, 0, 0, 1,
			0, 0, 0, 0, 1, 2, 0, 3, 0, 0, 0, 3, 0, 0, 0, 122, 1, 6, 0, 3, 0,
			0, 0, 1, 0, 2, 0, 0, 1, 17, 0, 4, 0, 0, 0, 1, 0, 0, 3, 0, 1, 21,
			0, 3, 0, 0, 0, 1, 0, 3, 0, 0, 1, 22, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0,
			1, 23, 0, 4, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 8
	};

	static final String TIFF_ERROR = "Error: Processing can only read its own TIFF files.";

	static protected SImage loadTIFF(byte tiff[]) {
		if ((tiff[42] != tiff[102]) || // width/height in both places
				(tiff[43] != tiff[103])) {
			System.err.println(TIFF_ERROR);
			return null;
		}

		int width = ((tiff[30] & 0xff) << 8) | (tiff[31] & 0xff);
		int height = ((tiff[42] & 0xff) << 8) | (tiff[43] & 0xff);

		int count = ((tiff[114] & 0xff) << 24) |
				((tiff[115] & 0xff) << 16) |
				((tiff[116] & 0xff) << 8) |
				(tiff[117] & 0xff);
		if (count != width * height * 3) {
			System.err.println(TIFF_ERROR + " (" + width + ", " + height + ")");
			return null;
		}

		// check the rest of the header
		for (int i = 0; i < TIFF_HEADER.length; i++) {
			if ((i == 30) || (i == 31) || (i == 42) || (i == 43) ||
					(i == 102) || (i == 103) ||
					(i == 114) || (i == 115) || (i == 116) || (i == 117))
				continue;

			if (tiff[i] != TIFF_HEADER[i]) {
				System.err.println(TIFF_ERROR + " (" + i + ")");
				return null;
			}
		}

		SImage outgoing = new SImage(width, height, RGB);
		int index = 768;
		count /= 3;
		for (int i = 0; i < count; i++) {
			outgoing.pixels[i] = 0xFF000000 |
					(tiff[index++] & 0xff) << 16 |
					(tiff[index++] & 0xff) << 8 |
					(tiff[index++] & 0xff);
		}
		return outgoing;
	}

	protected boolean saveTIFF(OutputStream output) {
		// shutting off the warning, people can figure this out themselves
		/*
		 * if (format != RGB) {
		 * System.err.println("Warning: only RGB information is saved with " +
		 * ".tif files. Use .tga or .png for ARGB images and others.");
		 * }
		 */
		try {
			byte tiff[] = new byte[768];
			System.arraycopy(TIFF_HEADER, 0, tiff, 0, TIFF_HEADER.length);

			tiff[30] = (byte) ((pixelWidth >> 8) & 0xff);
			tiff[31] = (byte) ((pixelWidth) & 0xff);
			tiff[42] = tiff[102] = (byte) ((pixelHeight >> 8) & 0xff);
			tiff[43] = tiff[103] = (byte) ((pixelHeight) & 0xff);

			int count = pixelWidth * pixelHeight * 3;
			tiff[114] = (byte) ((count >> 24) & 0xff);
			tiff[115] = (byte) ((count >> 16) & 0xff);
			tiff[116] = (byte) ((count >> 8) & 0xff);
			tiff[117] = (byte) ((count) & 0xff);

			// spew the header to the disk
			output.write(tiff);

			for (int i = 0; i < pixels.length; i++) {
				output.write((pixels[i] >> 16) & 0xff);
				output.write((pixels[i] >> 8) & 0xff);
				output.write(pixels[i] & 0xff);
			}
			output.flush();
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Creates a Targa32 formatted byte sequence of specified
	 * pixel buffer using RLE compression.
	 * </p>
	 * Also figured out how to avoid parsing the image upside-down
	 * (there's a header flag to set the image origin to top-left)
	 * </p>
	 * Starting with revision 0092, the format setting is taken into account:
	 * <UL>
	 * <LI><TT>ALPHA</TT> images written as 8bit grayscale (uses lowest byte)
	 * <LI><TT>RGB</TT> &rarr; 24 bits
	 * <LI><TT>ARGB</TT> &rarr; 32 bits
	 * </UL>
	 * All versions are RLE compressed.
	 * </p>
	 * Contributed by toxi 8-10 May 2005, based on this RLE
	 * <A HREF="http://www.wotsit.org/download.asp?f=tga">specification</A>
	 */
	protected boolean saveTGA(OutputStream output) {
		byte header[] = new byte[18];

		if (format == ALPHA) { // save ALPHA images as 8bit grayscale
			header[2] = 0x0B;
			header[16] = 0x08;
			header[17] = 0x28;

		} else if (format == RGB) {
			header[2] = 0x0A;
			header[16] = 24;
			header[17] = 0x20;

		} else if (format == ARGB) {
			header[2] = 0x0A;
			header[16] = 32;
			header[17] = 0x28;

		} else {
			throw new RuntimeException("Image format not recognized inside save()");
		}
		// set image dimensions lo-hi byte order
		header[12] = (byte) (pixelWidth & 0xff);
		header[13] = (byte) (pixelWidth >> 8);
		header[14] = (byte) (pixelHeight & 0xff);
		header[15] = (byte) (pixelHeight >> 8);

		try {
			output.write(header);

			int maxLen = pixelHeight * pixelWidth;
			int index = 0;
			int col; // , prevCol;
			int[] currChunk = new int[128];

			// 8bit image exporter is in separate loop
			// to avoid excessive conditionals...
			if (format == ALPHA) {
				while (index < maxLen) {
					boolean isRLE = false;
					int rle = 1;
					currChunk[0] = col = pixels[index] & 0xff;
					while (index + rle < maxLen) {
						if (col != (pixels[index + rle] & 0xff) || rle == 128) {
							isRLE = (rle > 1);
							break;
						}
						rle++;
					}
					if (isRLE) {
						output.write(0x80 | (rle - 1));
						output.write(col);

					} else {
						rle = 1;
						while (index + rle < maxLen) {
							int cscan = pixels[index + rle] & 0xff;
							if ((col != cscan && rle < 128) || rle < 3) {
								currChunk[rle] = col = cscan;
							} else {
								if (col == cscan) rle -= 2;
								break;
							}
							rle++;
						}
						output.write(rle - 1);
						for (int i = 0; i < rle; i++) output.write(currChunk[i]);
					}
					index += rle;
				}
			} else { // export 24/32 bit TARGA
				while (index < maxLen) {
					boolean isRLE = false;
					currChunk[0] = col = pixels[index];
					int rle = 1;
					// try to find repeating bytes (min. len = 2 pixels)
					// maximum chunk size is 128 pixels
					while (index + rle < maxLen) {
						if (col != pixels[index + rle] || rle == 128) {
							isRLE = (rle > 1); // set flag for RLE chunk
							break;
						}
						rle++;
					}
					if (isRLE) {
						output.write(128 | (rle - 1));
						output.write(col & 0xff);
						output.write(col >> 8 & 0xff);
						output.write(col >> 16 & 0xff);
						if (format == ARGB) output.write(col >>> 24 & 0xff);

					} else { // not RLE
						rle = 1;
						while (index + rle < maxLen) {
							if ((col != pixels[index + rle] && rle < 128) || rle < 3) {
								currChunk[rle] = col = pixels[index + rle];
							} else {
								// check if the exit condition was the start of
								// a repeating colour
								if (col == pixels[index + rle]) rle -= 2;
								break;
							}
							rle++;
						}
						// write uncompressed chunk
						output.write(rle - 1);
						if (format == ARGB) {
							for (int i = 0; i < rle; i++) {
								col = currChunk[i];
								output.write(col & 0xff);
								output.write(col >> 8 & 0xff);
								output.write(col >> 16 & 0xff);
								output.write(col >>> 24 & 0xff);
							}
						} else {
							for (int i = 0; i < rle; i++) {
								col = currChunk[i];
								output.write(col & 0xff);
								output.write(col >> 8 & 0xff);
								output.write(col >> 16 & 0xff);
							}
						}
					}
					index += rle;
				}
			}
			output.flush();
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Use ImageIO functions from Java 1.4 and later to handle image save.
	 * Various formats are supported, typically jpeg, png, bmp, and wbmp.
	 * To get a list of the supported formats for writing, use: <BR>
	 * <TT>println(javax.imageio.ImageIO.getReaderFormatNames())</TT>
	 */
	protected boolean saveImageIO(String path) throws IOException {
		try {
			int outputFormat = (format == ARGB) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

			String extension = path.substring(path.lastIndexOf('.') + 1).toLowerCase();

			// JPEG and BMP images that have an alpha channel set get pretty unhappy.
			// BMP just doesn't write, and JPEG writes it as a CMYK image.
			// http://code.google.com/p/processing/issues/detail?id=415
			if (extension.equals("bmp") || extension.equals("jpg") || extension.equals("jpeg")) {
				outputFormat = BufferedImage.TYPE_INT_RGB;
			}

			BufferedImage bimage = new BufferedImage(pixelWidth, pixelHeight, outputFormat);
			bimage.setRGB(0, 0, pixelWidth, pixelHeight, pixels, 0, pixelWidth);

			File file = new File(path);

			ImageWriter writer = null;
			ImageWriteParam param = null;
			IIOMetadata metadata = null;

			if (extension.equals("jpg") || extension.equals("jpeg")) {
				if ((writer = imageioWriter("jpeg")) != null) {
					// Set JPEG quality to 90% with baseline optimization. Setting this
					// to 1 was a huge jump (about triple the size), so this seems good.
					// Oddly, a smaller file size than Photoshop at 90%, but I suppose
					// it's a completely different algorithm.
					param = writer.getDefaultWriteParam();
					param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					param.setCompressionQuality(0.9f);
				}
			}

			if (extension.equals("png")) {
				if ((writer = imageioWriter("png")) != null) {
					param = writer.getDefaultWriteParam();
//					if (false) {
//						metadata = imageioDPI(writer, param, 100);
//					}
				}
			}

			if (writer != null) {
				BufferedOutputStream output = new BufferedOutputStream(GameUtils.instance.createOutput(file));
				writer.setOutput(ImageIO.createImageOutputStream(output));
//	        		writer.write(null, new IIOImage(bimage, null, null), param);
				writer.write(metadata, new IIOImage(bimage, null, metadata), param);
				writer.dispose();

				output.flush();
				output.close();
				return true;
			}
			// If iter.hasNext() somehow fails up top, it falls through to here
			return javax.imageio.ImageIO.write(bimage, extension, file);

		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("image save failed.");
		}
	}

	private ImageWriter imageioWriter(String extension) {
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(extension);
		if (iter.hasNext()) {
			return iter.next();
		}
		return null;
	}

	private IIOMetadata imageioDPI(ImageWriter writer, ImageWriteParam param, double dpi) {
		// http://stackoverflow.com/questions/321736/how-to-set-dpi-information-in-an-image
		ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
		IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, param);

		if (!metadata.isReadOnly() && metadata.isStandardMetadataFormatSupported()) {
			// for PNG, it's dots per millimeter
			double dotsPerMilli = dpi / 25.4;

			IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
			horiz.setAttribute("value", Double.toString(dotsPerMilli));

			IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
			vert.setAttribute("value", Double.toString(dotsPerMilli));

			IIOMetadataNode dim = new IIOMetadataNode("Dimension");
			dim.appendChild(horiz);
			dim.appendChild(vert);

			IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
			root.appendChild(dim);

			try {
				metadata.mergeTree("javax_imageio_1.0", root);
				return metadata;

			} catch (IIOInvalidTreeException e) {
				System.err.println("Could not set the DPI of the output image");
				e.printStackTrace();
			}
		}
		return null;
	}

}
