package com.sunflow.gfx;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

public class SSImage extends BufferedImage {

	public SSImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) { super(cm, raster, isRasterPremultiplied, properties); }

	public SSImage(int width, int height, int imageType, IndexColorModel cm) { super(width, height, imageType, cm); }

	public SSImage(int width, int height, int imageType) { super(width, height, imageType); }

}
