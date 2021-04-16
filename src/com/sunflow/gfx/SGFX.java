package com.sunflow.gfx;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import com.sunflow.util.Style;

public interface SGFX {

	void setPrimary(boolean primary);

	void setPath(String path);

	void setSize(int w, int h);

	/** Returns the java.awt.Graphics2D object used by this renderer. */
	Object getNative();

	Graphics2D checkImage();

	void beginDraw();

	void endDraw();

	void beginShape();

	/**
	 * @param mode
	 *            POINTS, LINES, TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, QUADS, and QUAD_STRIP
	 */
	void beginShape(int mode);

	void vertex(float x, float y);

	void vertex(int[] v);

	void vertex(float[] v);

	/**
	 * @param mode
	 *            OPEN or CLOSE
	 */
	void endShape(int mode);

	void endShape();

	/**
	 * ( begin auto-generated from blendMode.xml )
	 *
	 * This is a new reference entry for Processing 2.0. It will be updated shortly.
	 *
	 * ( end auto-generated )
	 *
	 * @webref rendering
	 * @param mode
	 *            the blending mode to use
	 */
	void blendMode(int mode);

	void smooth();

	/**
	 * 
	 * @param quality
	 *            0: all off
	 *            1 : Antialising
	 *            2 : + Text Antialising
	 *            3 : + Interpolation Bicubic
	 *            4 : ~ Interpolation Biliniear
	 *            5 : + Fractionalmetrics
	 *            6 : all default
	 */

	void smooth(int quality);

	void noSmooth();

	void strokeCap(int cap);

	void strokeJoin(int join);

	void strokeWeight(float weight);

	void noFill();

	/**
	 * @param rgb
	 *            color value in hexadecimal notation
	 */

	void fill(int rgb);

	/**
	 * @param alpha
	 *            opacity of the fill
	 */

	void fill(int rgb, float alpha);

	/**
	 * @param gray
	 *            number specifying value between white and black
	 */

	void fill(float gray);

	void fill(float gray, float alpha);

	/**
	 * @param v1
	 *            red or hue value (depending on current color mode)
	 * @param v2
	 *            green or saturation value (depending on current color mode)
	 * @param v3
	 *            blue or brightness value (depending on current color mode)
	 */

	void fill(float v1, float v2, float v3);

	void fill(float v1, float v2, float v3, float alpha);

	void noStroke();

	/**
	 * @param rgb
	 *            color value in hexadecimal notation
	 */

	void stroke(int rgb);

	/**
	 * @param alpha
	 *            opacity of the stroke
	 */

	void stroke(int rgb, float alpha);

	/**
	 * @param gray
	 *            specifies a value between white and black
	 */

	void stroke(float gray);

	void stroke(float gray, float alpha);

	/**
	 * @param v1
	 *            red or hue value (depending on current color mode)
	 * @param v2
	 *            green or saturation value (depending on current color mode)
	 * @param v3
	 *            blue or brightness value (depending on current color mode)
	 * @webref color:setting
	 */

	void stroke(float v1, float v2, float v3);

	void stroke(float v1, float v2, float v3, float alpha);

	/**
	 * ( begin auto-generated from noTint.xml )
	 *
	 * Removes the current fill value for displaying images and reverts to
	 * displaying images with their original hues.
	 *
	 * ( end auto-generated )
	 *
	 * @webref image:loading_displaying
	 * @usage web_application
	 * @see PGraphics#tint(float, float, float, float)
	 * @see PGraphics#image(PImage, float, float, float, float)
	 */
	void noTint();

	void tint(int rgb);

	/**
	 * @param alpha
	 *            opacity of the image
	 */
	void tint(int rgb, float alpha);

	/**
	 * @param gray
	 *            specifies a value between white and black
	 */
	void tint(float gray);

	void tint(float gray, float alpha);

	/**
	 * @param v1
	 *            red or hue value (depending on current color mode)
	 * @param v2
	 *            green or saturation value (depending on current color mode)
	 * @param v3
	 *            blue or brightness value (depending on current color mode)
	 */
	void tint(float v1, float v2, float v3);

	void tint(float v1, float v2, float v3, float alpha);

	/**
	 * @param rgb
	 *            color value in hexadecimal notation
	 */

	void background(int rgb);

	/**
	 * @param alpha
	 *            opacity of the background
	 */

	void background(int rgb, float alpha);

	/**
	 * @param gray
	 *            specifies a value between white and black
	 */

	void background(float gray);

	void background(float gray, float alpha);

	/**
	 * @param v1
	 *            red or hue value (depending on the current color mode)
	 * @param v2
	 *            green or saturation value (depending on the current color mode)
	 * @param v3
	 *            blue or brightness value (depending on the current color mode)
	 */

	void background(float v1, float v2, float v3);

	void background(float v1, float v2, float v3, float alpha);

	void clear();

	int color(int c);

	int color(float gray);

	int color(double gray);

	/**
	 * @param c
	 *            can be packed ARGB or a gray in this case
	 */

	int color(int c, int alpha);

	/**
	 * @param c
	 *            can be packed ARGB or a gray in this case
	 */

	int color(int c, float alpha);

	int color(float gray, float alpha);

	int color(int v1, int v2, int v3);

	int color(float v1, float v2, float v3);

	int color(int v1, int v2, int v3, int a);

	int color(float v1, float v2, float v3, float a);

	float alpha(int rgb);

	float red(int rgb);

	float green(int rgb);

	float blue(int rgb);

	float hue(int rgb);

	float saturation(int rgb);

	float brightness(int rgb);

	int lerpColor(int c1, int c2, float amt);

	/**
	 * @param mode
	 *            either CORNER, CORNERS, or CENTER
	 */

	void imageMode(int mode);

	/**
	 * @param mode
	 *            either CORNER, CORNERS, CENTER, or RADIUS
	 */

	void rectMode(int mode);

	/**
	 * @param mode
	 *            either CENTER, RADIUS, CORNER, or CORNERS
	 */

	void ellipseMode(int mode);

	/**
	 * @param mode
	 *            either CORNER, CORNERS, CENTER
	 */

	void shapeMode(int mode);

	/**
	 * @param mode
	 *            Either RGB or HSB, corresponding to Red/Green/Blue and Hue/Saturation/Brightness
	 */

	void colorMode(int mode);

	/**
	 * @param max
	 *            range for all color elements
	 */

	void colorMode(int mode, float max);

	/**
	 * @param max1
	 *            range for the red or hue depending on the current color mode
	 * @param max2
	 *            range for the green or saturation depending on the current color mode
	 * @param max3
	 *            range for the blue or brightness depending on the current color mode
	 */

	void colorMode(int mode, float max1, float max2, float max3);

	/**
	 * @param maxA
	 *            range for the alpha
	 */

	void colorMode(int mode,
			float max1, float max2, float max3, float maxA);

	/**
	 * @param size
	 *            the size of the letters in units of pixels
	 */

	void textFont(SFont which);

	/**
	 * @param size
	 *            the size of the letters in units of pixels
	 */

	void textFont(SFont which, float size);

	void textLeading(float leading);

	/**
	 * @param alignX
	 *            horizontal alignment, either LEFT, CENTER, or RIGHT
	 * @param alignY
	 *            vertical alignment, either TOP, BOTTOM, CENTER, or BASELINE
	 */

	void textAlign(int alignX, int alignY);

	/**
	 * @param mode
	 *            either MODEL or SHAPE
	 */

	void textMode(int mode);

	void textSize(float size);

	float textAscent();

	float textDescent();

	void string(String text, float x, float y);

	void text(String text, float x, float y);

	void circle(float x, float y, float r);

	void ellipse(float x, float y, float r);

	void ellipse(float x, float y, float w, float h);

	void square(float x, float y, float w);

	void rect(float x, float y, float w, float h);

	void point(float x, float y);

	void line(float x1, float y1, float x2, float y2);

	void triangle(float x1, float y1, float x2, float y2, float x3, float y3);

	void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4);

	/**
	 * @param img
	 *            the specified image to be drawn. This method does
	 *            nothing if <code>img</code> is null.
	 */
	void image(SImage img);

	/**
	 * @param img
	 *            the specified image to be drawn. This method does
	 *            nothing if <code>img</code> is null.
	 */
	void image(Image img);

	/**
	 * @param img
	 *            the specified image to be drawn. This method does
	 *            nothing if <code>img</code> is null.
	 * @param x
	 *            the <i>x</i> coordinate of the first corner of the
	 *            destination rectangle.
	 * @param y
	 *            the <i>y</i> coordinate of the first corner of the
	 *            destination rectangle.
	 */
	void image(SImage img, float x, float y);

	/**
	 * @param img
	 *            the specified image to be drawn. This method does
	 *            nothing if <code>img</code> is null.
	 * @param x
	 *            the <i>x</i> coordinate of the first corner of the
	 *            destination rectangle.
	 * @param y
	 *            the <i>y</i> coordinate of the first corner of the
	 *            destination rectangle.
	 */
	void image(Image img, float x, float y);

	/**
	 * @param img
	 *            the specified image to be drawn. This method does
	 *            nothing if <code>img</code> is null.
	 * @param x
	 *            the <i>x</i> coordinate of the first corner of the
	 *            destination rectangle.
	 * @param y
	 *            the <i>y</i> coordinate of the first corner of the
	 *            destination rectangle.
	 * @param w
	 *            the <i>width</i> of the destination rectangle.
	 * @param h
	 *            the <i>height</i> of the destination rectangle.
	 */
	void image(SImage img, float x, float y, float w, float h);

	/**
	 * @param img
	 *            the specified image to be drawn. This method does
	 *            nothing if <code>img</code> is null.
	 * @param x
	 *            the <i>x</i> coordinate of the first corner of the
	 *            destination rectangle.
	 * @param y
	 *            the <i>y</i> coordinate of the first corner of the
	 *            destination rectangle.
	 * @param w
	 *            the <i>width</i> of the destination rectangle.
	 * @param h
	 *            the <i>height</i> of the destination rectangle.
	 */
	void image(Image img, float x, float y, float w, float h);

	/**
	 * @param img
	 *            the specified image to be drawn. This method does
	 *            nothing if <code>img</code> is null.
	 * @param x
	 *            the <i>x</i> coordinate of the first corner of the
	 *            destination rectangle.
	 * @param y
	 *            the <i>y</i> coordinate of the first corner of the
	 *            destination rectangle.
	 * @param w
	 *            the <i>width</i> of the destination rectangle.
	 * @param h
	 *            the <i>height</i> of the destination rectangle.
	 * @param u1
	 *            the <i>x</i> coordinate of the first corner of the
	 *            source rectangle.
	 * @param v1
	 *            the <i>y</i> coordinate of the first corner of the
	 *            source rectangle.
	 * @param u2
	 *            the <i>x</i> coordinate of the second corner of the
	 *            source rectangle.
	 * @param v2
	 *            the <i>y</i> coordinate of the second corner of the
	 *            source rectangle.
	 */
	void image(SImage img,
			float x, float y, float w, float h,
			int u1, int v1, int u2, int v2);

	/**
	 * @param img
	 *            the specified image to be drawn. This method does
	 *            nothing if <code>img</code> is null.
	 * @param x
	 *            the <i>x</i> coordinate of the first corner of the
	 *            destination rectangle.
	 * @param y
	 *            the <i>y</i> coordinate of the first corner of the
	 *            destination rectangle.
	 * @param w
	 *            the <i>width</i> of the destination rectangle.
	 * @param h
	 *            the <i>height</i> of the destination rectangle.
	 * @param u1
	 *            the <i>x</i> coordinate of the first corner of the
	 *            source rectangle.
	 * @param v1
	 *            the <i>y</i> coordinate of the first corner of the
	 *            source rectangle.
	 * @param u2
	 *            the <i>x</i> coordinate of the second corner of the
	 *            source rectangle.
	 * @param v2
	 *            the <i>y</i> coordinate of the second corner of the
	 *            source rectangle.
	 */
	void image(Image img,
			float x, float y, float w, float h,
			int u1, int v1, int u2, int v2);

	void fillShape(Shape s);

	void strokeShape(Shape s);

	void drawShape(Shape s);

	void translate(double x, double y);

	void rotate(double theta);

	void rotate(double theta, double x, double y);

	void scale(double xy);

	void scale(double x, double y);

	void shear(double x, double y);

	void transform(AffineTransform affineTransform);

	void push();

	void pop();

	void pushStyle();

	void popStyle();

	void pushMatrix();

	void popMatrix();

	void resetMatrix();

	Style getStyle();

	Style getStyle(Style s);

	void style(Style s);

	void loadPixels();

	/**
	 * Update the pixels[] buffer to the PGraphics image.
	 * <P>
	 * Unlike in PImage, where updatePixels() only requests that the
	 * update happens, in PGraphicsJava2D, this will happen immediately.
	 */
	void updatePixels(int x, int y, int c, int d);

	int get(int x, int y);

	void pixel(int x, int y, int argb);

	void set(int x, int y, int argb);

	boolean save(String filename);

}