package com.sunflow.gfx;

import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import com.sunflow.math.SMatrix2D;
import com.sunflow.math.SMatrix_D;
import com.sunflow.util.SStyle;

public interface SGFX {

//	void setParent(GameBase parent);

//	void setPrimary(boolean primary);

//	void setPath(String path);

	void setSize(int w, int h);

//	void setCache(Object key, Object val);

//	Object getCache(Object key);

//	void removeCache(Object key);

//	SScreen createScreen();

//	/**
//	 * Still need a means to get the java.awt.Image object, since getNative()
//	 * is going to return the {@link Graphics2D} object.
//	 */
//	Image getImage();
//
//	/** Returns the java.awt.Graphics2D object used by this renderer. */
//	Object getNative();
//
//	Graphics2D checkImage();

//	void beginDraw();

//	void endDraw();

	void beginShape();

	// POINTS,LINES, TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, QUADS, and QUAD_STRIP
	/**
	 * @param mode
	 *            POINTS, LINES, TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, QUADS, and QUAD_STRIP
	 */
	void beginShape(int mode);

	void vertex(float x, float y);

	void vertex(int[] v);

	void vertex(float[] v);

	void endShape();

	/**
	 * @param mode
	 *            OPEN or CLOSE
	 */
	void endShape(int mode);

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

	void point(float x, float y);

	void line(float x1, float y1, float x2, float y2);

	void triangle(float x1, float y1, float x2, float y2, float x3, float y3);

	void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4);

	/**
	 * @param mode
	 *            either CORNER, CORNERS, CENTER, or RADIUS
	 */
	void rectMode(int mode);

	void rect(float x, float y, float w, float h);

	void square(float x, float y, float w);

	/**
	 * @param mode
	 *            either CENTER, RADIUS, CORNER, or CORNERS
	 */
	void ellipseMode(int mode);

	void ellipse(float x, float y, float w, float h);

	void ellipse(float x, float y, float r);

	void circle(float x, float y, float r);

	/**
	 * @param a
	 *            x-coordinate of the arc's ellipse
	 * @param b
	 *            y-coordinate of the arc's ellipse
	 * @param c
	 *            width of the arc's ellipse by default
	 * @param d
	 *            height of the arc's ellipse by default
	 * @param start
	 *            angle to start the arc, specified in radians
	 * @param stop
	 *            angle to stop the arc, specified in radians
	 */
	void arc(float a, float b, float c, float d,
			float start, float stop);

	/*
	 * @param mode either OPEN, CHORD, or PIE
	 */
	void arc(float a, float b, float c, float d,
			float start, float stop, int mode);

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

	/**
	 * @param mode
	 *            either CORNER, CORNERS, or CENTER
	 */
	void imageMode(int mode);

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

//	/**
//	 * @param mode
//	 *            either CORNER, CORNERS, CENTER
//	 */
//	void shapeMode(int mode);

	void fillShape(Shape s);

	void strokeShape(Shape s);

	void drawShape(Shape s);

	void textAlign(int alignX);

	/**
	 * @param alignX
	 *            horizontal alignment, either LEFT, CENTER, or RIGHT
	 * @param alignY
	 *            vertical alignment, either TOP, BOTTOM, CENTER, or BASELINE
	 */
	void textAlign(int alignX, int alignY);

	float textAscent();

	float textDescent();

	void textFont(SFont which);

	/**
	 * @param size
	 *            the size of the letters in units of pixels
	 */

	void textFont(SFont which, float size);

	void textLeading(float leading);

	/**
	 * @param mode
	 *            either MODEL or SHAPE
	 */
	void textMode(int mode);

	void textSize(float size);

	/**
	 * @param c
	 *            the character to measure
	 */
	float textWidth(char c);

	/**
	 * @param str
	 *            the String of characters to measure
	 */
	float textWidth(String str);

	float textWidth(char[] chars, int start, int length);

	void string(String text, float x, float y);

	void text(char c, float x, float y);

	/**
	 * <h3>Advanced</h3>
	 * Draw a chunk of text.
	 * Newlines that are \n (Unix newline or linefeed char, ascii 10)
	 * are honored, but \r (carriage return, Windows and Mac OS) are
	 * ignored.
	 */
	void text(String str, float x, float y);

	/**
	 * <h3>Advanced</h3>
	 * Method to draw text from an array of chars. This method will usually be
	 * more efficient than drawing from a String object, because the String will
	 * not be converted to a char array before drawing.
	 * 
	 * @param chars
	 *            the alphanumberic symbols to be displayed
	 * @param start
	 *            array index at which to start writing characters
	 * @param stop
	 *            array index at which to stop writing characters
	 */
	void text(char[] chars, int start, int stop, float x, float y);

	/**
	 * <h3>Advanced</h3>
	 * Draw text in a box that is constrained to a particular size.
	 * The current rectMode() determines what the coordinates mean
	 * (whether x1/y1/x2/y2 or x/y/w/h).
	 * <P/>
	 * Note that the x,y coords of the start of the box
	 * will align with the *ascent* of the text, not the baseline,
	 * as is the case for the other text() functions.
	 * <P/>
	 * Newlines that are \n (Unix newline or linefeed char, ascii 10)
	 * are honored, and \r (carriage return, Windows and Mac OS) are
	 * ignored.
	 *
	 * @param x1
	 *            by default, the x-coordinate of text, see rectMode() for more info
	 * @param y1
	 *            by default, the y-coordinate of text, see rectMode() for more info
	 * @param x2
	 *            by default, the width of the text box, see rectMode() for more info
	 * @param y2
	 *            by default, the height of the text box, see rectMode() for more info
	 */
	void text(String str, float x1, float y1, float x2, float y2);

	void text(int num, float x, float y);

	void text(float num, float x, float y);

	void push();

	void pop();

	void pushMatrix();

	void popMatrix();

	void translate(float x, float y);

	void rotate(float theta);

	void rotate(float theta, float x, float y);

	void scale(float xy);

	void scale(float x, float y);

	void shear(float x, float y);

	void shearX(float angle);

	void shearY(float angle);

	void transform(AffineTransform affineTransform);

	void setTransform(AffineTransform affineTransform);

	void resetMatrix();

	void applyMatrix(SMatrix2D source);

	void applyMatrix(float n00, float n01, float n02,
			float n10, float n11, float n12);

	SMatrix_D getMatrix();

	SMatrix2D getMatrix(SMatrix2D target);

	void setMatrix(SMatrix2D source);

	void printMatrix();

	float screenX(float x, float y);

	float screenY(float x, float y);

	void pushStyle();

	void popStyle();

	SStyle getStyle();

	SStyle getStyle(SStyle s);

	void style(SStyle s);

	void strokeWeight(float weight);

	void strokeJoin(int join);

	void strokeCap(int cap);

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

	// BACKGROUND
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

	/**
	 * Takes an RGB or ARGB image and sets it as the background.
	 * The width and height of the image must be the same size as the sketch.
	 * Use image.resize(width, height) to make short work of such a task.<br/>
	 * <br/>
	 * Note that even if the image is set as RGB, the high 8 bits of each pixel
	 * should be set opaque (0xFF000000) because the image data will be copied
	 * directly to the screen, and non-opaque background images may have strange
	 * behavior. Use image.filter(OPAQUE) to handle this easily.<br/>
	 * <br/>
	 * When using 3D, this will also clear the zbuffer (if it exists).
	 *
	 * @param image
	 *            SImage to set as background (must be same size as the sketch window)
	 */
	void background(SImage image);

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

	int color(int c);

	int color(float gray);

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

	void loadPixels();

	void updatePixels();

	/**
	 * Update the pixels[] buffer to the PGraphics image.
	 * <P>
	 * Unlike in PImage, where updatePixels() only requests that the
	 * update happens, in PGraphicsJava2D, this will happen immediately.
	 */
	void updatePixels(int x, int y, int w, int h);

	int get(int x, int y);

	void pixel(int x, int y);

	void pixel(int x, int y, int argb);

	void set(int x, int y, int argb);

	void copy(int sx, int sy, int sw, int sh,
			int dx, int dy, int dw, int dh);

	void copy(SImage src,
			int sx, int sy, int sw, int sh,
			int dx, int dy, int dw, int dh);
	//////////////////////////////////////////////////////////////

	boolean save(String filename);

}