package com.sunflow.gfx;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.sunflow.util.Style;

public class SGraphics extends SImage {

	public Graphics2D graphics;

	public int smooth;

	// ........................................................

	/** The current colorMode */
	protected int colorMode; // = RGB;

	/** Max value for red/green/blue/alpha (or hue/saturation/value/alpha) set by colorMode */
	protected float colorModeX, colorModeY, colorModeZ, colorModeA; // = 255;

	/** True if colors are not in the range 0..1 */
	protected boolean colorModeScale; // = true;
	/**
	 * True if colorMode(RGB, 255). Defaults to true so that color()
	 * used as part of a field declaration will properly assign values.
	 */
	protected boolean colorModeDefault = true;

	// ........................................................

	// Fill color

	/** true if fill() is enabled, (read-only) */
	protected boolean fill;

	/** fill that was last set (read-only) */
	protected int fillColor;

	public int getFillColor() { return fillColor; }

	protected boolean fillAlpha;
	protected float fillR, fillG, fillB, fillA;
	protected int fillRi, fillGi, fillBi, fillAi;

	// ........................................................

	// Stroke color

	/** true if stroke() is enabled, (read-only) */
	protected boolean stroke;

	/** stroke that was last set (read-only) */
	protected int strokeColor;

	protected boolean strokeAlpha;
	protected float strokeR, strokeG, strokeB, strokeA;
	protected int strokeRi, strokeGi, strokeBi, strokeAi;

	protected float strokeWeight;

	// ........................................................

	/** Last background color that was set, zero if an image */
	protected int backgroundColor = 0xffCCCCCC;

	protected boolean backgroundAlpha;
	protected float backgroundR, backgroundG, backgroundB, backgroundA;
	protected int backgroundRi, backgroundGi, backgroundBi, backgroundAi;

	// ........................................................

	// Shape placement properties

	// imageMode() is inherited from PImage

	/** The current rect mode (read-only) */
	protected int rectMode;

	/** The current ellipse mode (read-only) */
	protected int ellipseMode;

	/** The current shape alignment mode (read-only) */
	protected int shapeMode;

	/** The current image alignment (read-only) */
	protected int imageMode = CORNER;

	// ........................................................

	// Text and font properties

	/** The current text font (read-only) */
	protected Font textFont;

	/** The current text align (read-only) */
	protected int textAlign = LEFT;

	/** The current vertical text alignment (read-only) */
	protected int textAlignY = BASELINE;

	/** The current text mode (read-only) */
	protected int textMode = MODEL;

	/** The current text size (read-only) */
	public float textSize;

	/** The current text leading (read-only) */
	protected float textLeading;

	// ........................................................

	private int transformCount;
	private static final int MATRIX_STACK_DEPTH = 32;
	AffineTransform transformStack[] = new AffineTransform[MATRIX_STACK_DEPTH];

	private int styleStackDepth;
	private static final int STYLE_STACK_DEPTH = 64;
	private Style[] styleStack = new Style[STYLE_STACK_DEPTH];

	// ........................................................

	// internal color for setting/calculating
	private float calcR, calcG, calcB, calcA;
	private int calcRi, calcGi, calcBi, calcAi;
	private int calcColor;
	private boolean calcAlpha;

	/** The last RGB value converted to HSB */
	private int cacheHsbKey;
	/** Result of the last conversion to HSB */
	private float[] cacheHsbValue = new float[3];

	private Line2D.Float line = new Line2D.Float();
	private Ellipse2D.Float ellipse = new Ellipse2D.Float();
	private Rectangle2D.Float rect = new Rectangle2D.Float();
	private Arc2D.Float arc = new Arc2D.Float();

	protected GeneralPath gpath;
	protected ArrayList<Shape> shapes_tmp;

	private Color fillColorObject;
	private boolean fillGradient;
	private Paint fillGradientObject;

	private Color strokeColorObject;
	private boolean strokeGradient;
	private Paint strokeGradientObject;

	private int strokeCap;
	private int strokeJoin;
	private BasicStroke strokeObject;
	@SuppressWarnings("unused")
	private Composite defaultComposite;
	private static final String ERROR_TEXTFONT_NULL_PFONT = "A null Font was passed to textFont()";

	protected SGraphics() { super(); }

	public SGraphics(float width, float height) {
		super(width, height);
	}

	public SGraphics(float width, float height, int format) {
		super(width, height, format);
	}

	public SGraphics(BufferedImage bi) { super(bi); }

	@Override
	protected void defaultSettings() { // ignore
//		image = new BufferedImage(width, height, format);
		super.defaultSettings();
		graphics = image.createGraphics();
		defaultComposite = graphics.getComposite();

		colorMode(RGB, 255);
		fill(0xffFFFFFF);
		stroke(0xff000000);

		strokeWeight(1);
		strokeJoin(8);
		strokeCap(2);

		rectMode(CORNER);
		ellipseMode(DIAMETER);

		// no current font
		textFont = null;
		textSize = 12;
		textLeading = 14;
		textAlign = LEFT;
		textAlignY = BASELINE;
		textMode = MODEL;

//		background(0xffCCCCCC);
	}

	public final void pixel(float x, float y) { pixel(x, y, strokeColor); }

// 	PROCESSING CODE
// 	| | | | | | | |
// 	V V V V V V V V

	protected int shape;
	protected int vNum;

	public final void beginShape() { beginShape(POLYGON); }

	// POINTS,LINES, TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, QUADS, and QUAD_STRIP
	/**
	 * @param mode
	 *            POINTS, LINES, TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, QUADS, and QUAD_STRIP
	 */
	public void beginShape(int mode) {
		shape = mode;
		vNum = 0;
		gpath = null;
		SShape.beginShape(this);
	}

	public void vertex(float x, float y) {
		if (gpath == null) {
			gpath = new GeneralPath();
			gpath.moveTo(x, y);
		} else gpath.lineTo(x, y);
		vNum++;
		testVertex();
	}

	private void testVertex() {
		boolean end = false;
		if (shape == POINTS && vNum == 1) end = true;
		if (shape == LINES && vNum == 2) end = true;
		if (shape == TRIANGLES && vNum == 3) end = true;
//		if (shape == TRIANGLE_FAN && vNum == ??) end = true;
//		if (shape == TRIANGLE_STRIP && vNum == 4) end = true;
		if (shape == QUADS && vNum == 4) end = true;
//		if (shape == QUAD_STRIP && vNum == 4) end = true;

		if (end) {
			SShape.tempShape = true;
			endShape(CLOSE);
			beginShape(shape);
			SShape.tempShape = false;
		}
	}

	public final void closeShape() { gpath.closePath(); }

	/**
	 * @param mode
	 *            OPEN or CLOSE
	 */
	public void endShape(int mode) {
		if (mode == CLOSE) if (gpath != null) gpath.closePath();
		endShape();
	}

	public void endShape() {
		boolean completeShape = true;
		if (shape == POINTS && vNum < 1) completeShape = false;
		if (shape == LINES && vNum < 2) completeShape = false;
		if (shape == TRIANGLES && vNum < 3) completeShape = false;
//		if(shape == TRIANGLE_FAN && vNum < 0) bla = false;
//		if(shape == TRIANGLE_STRIP && vNum < 0)bla = false;
		if (shape == QUADS && vNum < 4) completeShape = false;
//		if(shape == QUAD_STRIP && vNum < 0) bla = false;

//		drawShape(gpath);
		if (completeShape && gpath != null) SShape.addShape(this);
		SShape.endShape(this);
	}

// COPY PASTA

	final public void smooth() { smooth(1); }

	/**
	 * 
	 * @param quality
	 *            0: all off
	 *            1 : Antialising
	 *            2 : + Text Antialising
	 *            3 : + Interpolation Bicubic
	 *            4 : + Interpolation Biliniear
	 *            5 : + Fractionalmetrics
	 *            6 : all default
	 */

	final public void smooth(int quality) { // ignore
		if (smooth == quality) return;
		if (quality < 0 || quality > 5) quality = 0;
		this.smooth = quality;
		handleSmooth();
	}

	public final void noSmooth() { // ignore
		smooth(0);
	}

	final protected void handleSmooth() {
		if (smooth == 0) {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
					RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		} else if (smooth == 6) {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_DEFAULT);
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
					RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
		} else {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			if (smooth == 1) return;

			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			if (smooth == 2) return;

			if (smooth == 3) { // default is bicubic
				graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				return;
			} else if (smooth == 4) {
				graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				return;
			}
			graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
					RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		}
	}

	public final void strokeCap(int cap) {
		strokeCap = cap;
		strokeImpl();
	}

	public final void strokeJoin(int join) {
		strokeJoin = join;
		strokeImpl();
	}

	public final void strokeWeight(float weight) {
		strokeWeight = weight;
		strokeImpl();
	}

	private final void strokeImpl() {
		int cap = BasicStroke.CAP_BUTT;
		if (strokeCap == ROUND) {
			cap = BasicStroke.CAP_ROUND;
		} else if (strokeCap == PROJECT) {
			cap = BasicStroke.CAP_SQUARE;
		}

		int join = BasicStroke.JOIN_BEVEL;
		if (strokeJoin == MITER) {
			join = BasicStroke.JOIN_MITER;
		} else if (strokeJoin == ROUND) {
			join = BasicStroke.JOIN_ROUND;
		}

		strokeObject = new BasicStroke(strokeWeight, cap, join);
		graphics.setStroke(strokeObject);
	}

	public final void noFill() {
		fill = false;
	}

	/**
	 * @param rgb
	 *            color value in hexadecimal notation
	 */

	public final void fill(int rgb) {
		colorCalc(rgb);
		fillFromCalc();
	}

	/**
	 * @param alpha
	 *            opacity of the fill
	 */

	public final void fill(int rgb, float alpha) {
		colorCalc(rgb, alpha);
		fillFromCalc();
	}

	/**
	 * @param gray
	 *            number specifying value between white and black
	 */

	public final void fill(float gray) {
		colorCalc(gray);
		fillFromCalc();
	}

	public final void fill(float gray, float alpha) {
		colorCalc(gray, alpha);
		fillFromCalc();
	}

	/**
	 * @param v1
	 *            red or hue value (depending on current color mode)
	 * @param v2
	 *            green or saturation value (depending on current color mode)
	 * @param v3
	 *            blue or brightness value (depending on current color mode)
	 */

	public final void fill(float v1, float v2, float v3) {
		colorCalc(v1, v2, v3);
		fillFromCalc();
	}

	public final void fill(float v1, float v2, float v3, float alpha) {
		colorCalc(v1, v2, v3, alpha);
		fillFromCalc();
	}

	private final void fillFromCalc() {
		fill = true;
		fillR = calcR;
		fillG = calcG;
		fillB = calcB;
		fillA = calcA;
		fillRi = calcRi;
		fillGi = calcGi;
		fillBi = calcBi;
		fillAi = calcAi;
		fillColor = calcColor;
		fillAlpha = calcAlpha;

		fillColorObject = new Color(fillColor, true);
		fillGradient = false;
	}

	public final void noStroke() {
		stroke = false;
	}

	/**
	 * @param rgb
	 *            color value in hexadecimal notation
	 */

	public final void stroke(int rgb) {
		colorCalc(rgb);
		strokeFromCalc();
	}

	/**
	 * @param alpha
	 *            opacity of the stroke
	 */

	public final void stroke(int rgb, float alpha) {
		colorCalc(rgb, alpha);
		strokeFromCalc();
	}

	/**
	 * @param gray
	 *            specifies a value between white and black
	 */

	public final void stroke(float gray) {
		colorCalc(gray);
		strokeFromCalc();
	}

	public final void stroke(float gray, float alpha) {
		colorCalc(gray, alpha);
		strokeFromCalc();
	}

	/**
	 * @param v1
	 *            red or hue value (depending on current color mode)
	 * @param v2
	 *            green or saturation value (depending on current color mode)
	 * @param v3
	 *            blue or brightness value (depending on current color mode)
	 * @webref color:setting
	 */

	public final void stroke(float v1, float v2, float v3) {
		colorCalc(v1, v2, v3);
		strokeFromCalc();
	}

	public final void stroke(float v1, float v2, float v3, float alpha) {
		colorCalc(v1, v2, v3, alpha);
		strokeFromCalc();
	}

	private final void strokeFromCalc() {
		stroke = true;
		strokeR = calcR;
		strokeG = calcG;
		strokeB = calcB;
		strokeA = calcA;
		strokeRi = calcRi;
		strokeGi = calcGi;
		strokeBi = calcBi;
		strokeAi = calcAi;
		strokeColor = calcColor;
		strokeAlpha = calcAlpha;

		strokeColorObject = new Color(strokeColor, true);
		strokeGradient = false;
	}

	/**
	 * @param rgb
	 *            color value in hexadecimal notation
	 */

	public final void background(int rgb) {
		colorCalc(rgb);
		backgroundFromCalc();
	}

	/**
	 * @param alpha
	 *            opacity of the background
	 */

	public final void background(int rgb, float alpha) {
		colorCalc(rgb, alpha);
		backgroundFromCalc();
	}

	/**
	 * @param gray
	 *            specifies a value between white and black
	 */

	public final void background(float gray) {
		colorCalc(gray);
		backgroundFromCalc();
	}

	public final void background(float gray, float alpha) {
		if (format == RGB) {
			background(gray); // ignore alpha for main drawing surface

		} else {
			colorCalc(gray, alpha);
			backgroundFromCalc();
		}
	}

	/**
	 * @param v1
	 *            red or hue value (depending on the current color mode)
	 * @param v2
	 *            green or saturation value (depending on the current color mode)
	 * @param v3
	 *            blue or brightness value (depending on the current color mode)
	 */

	public final void background(float v1, float v2, float v3) {
		colorCalc(v1, v2, v3);
		backgroundFromCalc();
	}

	public final void background(float v1, float v2, float v3, float alpha) {
		colorCalc(v1, v2, v3, alpha);
		backgroundFromCalc();
	}

	@Override
	public final void clear() { background(0, 0, 0, 0); }

	private final void backgroundFromCalc() {
		backgroundR = calcR;
		backgroundG = calcG;
		backgroundB = calcB;
		// backgroundA = (format == RGB) ? colorModeA : calcA;
		// If drawing surface is opaque, this maxes out at 1.0. [fry 150513]
		backgroundA = (format == RGB) ? 1 : calcA;
		backgroundRi = calcRi;
		backgroundGi = calcGi;
		backgroundBi = calcBi;
		backgroundAi = (format == RGB) ? 255 : calcAi;
		backgroundAlpha = (format == RGB) ? false : calcAlpha;
		backgroundColor = calcColor;

		backgroundImpl();
	}

	boolean b = false;

	private final void backgroundImpl() {
		if (backgroundAlpha) {
			clearPixels(backgroundColor);
		} else {
//			Color bgColor = new Color(backgroundColor);
			Color bgColor = new Color(backgroundColor, calcAlpha);
			// seems to fire an additional event that causes flickering,
			// like an extra background erase on OS X
//	      if (canvas != null) {
//	        canvas.setBackground(bgColor);
//	      }
			// new Exception().printStackTrace(System.out);
			// in case people do transformations before background(),
			// need to handle this with a push/reset/pop

			pushMatrix();
//			Composite oldComposite = graphics.getComposite();
//			graphics.setComposite(defaultComposite);
//			AffineTransform at = graphics.getTransform();
			resetMatrix();

			graphics.setColor(bgColor); // , backgroundAlpha));
//	      	g2.fillRect(0, 0, width, height);
			// On a hi-res display, image may be larger than width/height
			if (image != null) {
				// image will be null in subclasses (i.e. PDF)
				graphics.fillRect(0, 0, image.getWidth(null), image.getHeight(null));
			} else {
				// hope for the best if image is null
				graphics.fillRect(0, 0, width, height);
			}

			popMatrix();
//			graphics.setTransform(at);
//			graphics.setComposite(oldComposite);
		}
	}

	private final void colorCalc(int rgb) {
		if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {
			colorCalc((float) rgb);

		} else {
			colorCalcARGB(rgb, colorModeA);
		}
	}

	private final void colorCalc(int rgb, float alpha) {
		if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) { // see above
			colorCalc((float) rgb, alpha);

		} else {
			colorCalcARGB(rgb, alpha);
		}
	}

	private final void colorCalc(float gray) {
		colorCalc(gray, colorModeA);
	}

	private final void colorCalc(float gray, float alpha) {
		if (gray > colorModeX) gray = colorModeX;
		if (alpha > colorModeA) alpha = colorModeA;

		if (gray < 0) gray = 0;
		if (alpha < 0) alpha = 0;

		calcR = colorModeScale ? (gray / colorModeX) : gray;
		calcG = calcR;
		calcB = calcR;
		calcA = colorModeScale ? (alpha / colorModeA) : alpha;

		calcRi = (int) (calcR * 255);
		calcGi = (int) (calcG * 255);
		calcBi = (int) (calcB * 255);
		calcAi = (int) (calcA * 255);
		calcColor = (calcAi << 24) | (calcRi << 16) | (calcGi << 8) | calcBi;
		calcAlpha = (calcAi != 255);
	}

	private final void colorCalc(float x, float y, float z) {
		colorCalc(x, y, z, colorModeA);
	}

	private final void colorCalc(float x, float y, float z, float a) {
		if (x > colorModeX) x = colorModeX;
		if (y > colorModeY) y = colorModeY;
		if (z > colorModeZ) z = colorModeZ;
		if (a > colorModeA) a = colorModeA;

		if (x < 0) x = 0;
		if (y < 0) y = 0;
		if (z < 0) z = 0;
		if (a < 0) a = 0;

		switch (colorMode) {
			case RGB:
				if (colorModeScale) {
					calcR = x / colorModeX;
					calcG = y / colorModeY;
					calcB = z / colorModeZ;
					calcA = a / colorModeA;
				} else {
					calcR = x;
					calcG = y;
					calcB = z;
					calcA = a;
				}
				break;

			case HSB:
				x /= colorModeX; // h
				y /= colorModeY; // s
				z /= colorModeZ; // b

				calcA = colorModeScale ? (a / colorModeA) : a;

				if (y == 0) { // saturation == 0
					calcR = calcG = calcB = z;

				} else {
					float which = (x - (int) x) * 6.0f;
					float f = which - (int) which;
					float p = z * (1.0f - y);
					float q = z * (1.0f - y * f);
					float t = z * (1.0f - (y * (1.0f - f)));

					switch ((int) which) {
						case 0:
							calcR = z;
							calcG = t;
							calcB = p;
							break;
						case 1:
							calcR = q;
							calcG = z;
							calcB = p;
							break;
						case 2:
							calcR = p;
							calcG = z;
							calcB = t;
							break;
						case 3:
							calcR = p;
							calcG = q;
							calcB = z;
							break;
						case 4:
							calcR = t;
							calcG = p;
							calcB = z;
							break;
						case 5:
							calcR = z;
							calcG = p;
							calcB = q;
							break;
					}
				}
				break;
		}
		calcRi = (int) (255 * calcR);
		calcGi = (int) (255 * calcG);
		calcBi = (int) (255 * calcB);
		calcAi = (int) (255 * calcA);
		calcColor = (calcAi << 24) | (calcRi << 16) | (calcGi << 8) | calcBi;
		calcAlpha = (calcAi != 255);
	}

	private final void colorCalcARGB(int argb, float alpha) {
		if (alpha == colorModeA) {
			calcAi = (argb >> 24) & 0xff;
			calcColor = argb;
		} else {
			calcAi = (int) (((argb >> 24) & 0xff) * clamp(0, (alpha / colorModeA), 1));
			calcColor = (calcAi << 24) | (argb & 0xFFFFFF);
		}
		calcRi = (argb >> 16) & 0xff;
		calcGi = (argb >> 8) & 0xff;
		calcBi = argb & 0xff;
		calcA = calcAi / 255.0f;
		calcR = calcRi / 255.0f;
		calcG = calcGi / 255.0f;
		calcB = calcBi / 255.0f;
		calcAlpha = (calcAi != 255);
	}

	public final int color(int c) { // ignore
		colorCalc(c);
		return calcColor;
	}

	public final int color(float gray) { // ignore
		colorCalc(gray);
		return calcColor;
	}

	public final int color(double gray) { // ignore
		colorCalc((float) gray);
		return calcColor;
	}

	/**
	 * @param c
	 *            can be packed ARGB or a gray in this case
	 */

	public final int color(int c, int alpha) { // ignore
		colorCalc(c, alpha);
		return calcColor;
	}

	/**
	 * @param c
	 *            can be packed ARGB or a gray in this case
	 */

	public final int color(int c, float alpha) { // ignore
		colorCalc(c, alpha);
		return calcColor;
	}

	public final int color(float gray, float alpha) { // ignore
		colorCalc(gray, alpha);
		return calcColor;
	}

	public final int color(int v1, int v2, int v3) { // ignore
		colorCalc(v1, v2, v3);
		return calcColor;
	}

	public final int color(float v1, float v2, float v3) { // ignore
		colorCalc(v1, v2, v3);
		return calcColor;
	}

	public final int color(int v1, int v2, int v3, int a) { // ignore
		colorCalc(v1, v2, v3, a);
		return calcColor;
	}

	public final int color(float v1, float v2, float v3, float a) { // ignore
		colorCalc(v1, v2, v3, a);
		return calcColor;
	}

	public final float alpha(int rgb) {
		float outgoing = (rgb >> 24) & 0xff;
		if (colorModeA == 255) return outgoing;
		return (outgoing / 255.0f) * colorModeA;
	}

	public final float red(int rgb) {
		float c = (rgb >> 16) & 0xff;
		if (colorModeDefault) return c;
		return (c / 255.0f) * colorModeX;
	}

	public final float green(int rgb) {
		float c = (rgb >> 8) & 0xff;
		if (colorModeDefault) return c;
		return (c / 255.0f) * colorModeY;
	}

	public final float blue(int rgb) {
		float c = (rgb) & 0xff;
		if (colorModeDefault) return c;
		return (c / 255.0f) * colorModeZ;
	}

	public final float hue(int rgb) {
		if (rgb != cacheHsbKey) {
			Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff,
					rgb & 0xff, cacheHsbValue);
			cacheHsbKey = rgb;
		}
		return cacheHsbValue[0] * colorModeX;
	}

	public final float saturation(int rgb) {
		if (rgb != cacheHsbKey) {
			Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff,
					rgb & 0xff, cacheHsbValue);
			cacheHsbKey = rgb;
		}
		return cacheHsbValue[1] * colorModeY;
	}

	public final float brightness(int rgb) {
		if (rgb != cacheHsbKey) {
			Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff,
					rgb & 0xff, cacheHsbValue);
			cacheHsbKey = rgb;
		}
		return cacheHsbValue[2] * colorModeZ;
	}

	public final int lerpColor(int c1, int c2, float amt) { // ignore
		return lerpColor(c1, c2, amt, colorMode);
	}

	private static float[] lerpColorHSB1;
	private static float[] lerpColorHSB2;

	/**
	 * @nowebref
	 *           Interpolate between two colors. Like lerp(), but for the
	 *           individual color components of a color supplied as an int value.
	 */
	public final static int lerpColor(int c1, int c2, float amt, int mode) {
		if (amt < 0) amt = 0;
		if (amt > 1) amt = 1;

		if (mode == RGB) {
			float a1 = ((c1 >> 24) & 0xff);
			float r1 = (c1 >> 16) & 0xff;
			float g1 = (c1 >> 8) & 0xff;
			float b1 = c1 & 0xff;
			float a2 = (c2 >> 24) & 0xff;
			float r2 = (c2 >> 16) & 0xff;
			float g2 = (c2 >> 8) & 0xff;
			float b2 = c2 & 0xff;

			return ((Math.round(a1 + (a2 - a1) * amt) << 24) |
					(Math.round(r1 + (r2 - r1) * amt) << 16) |
					(Math.round(g1 + (g2 - g1) * amt) << 8) |
					(Math.round(b1 + (b2 - b1) * amt)));

		} else if (mode == HSB) {
			if (lerpColorHSB1 == null) {
				lerpColorHSB1 = new float[3];
				lerpColorHSB2 = new float[3];
			}

			float a1 = (c1 >> 24) & 0xff;
			float a2 = (c2 >> 24) & 0xff;
			int alfa = (Math.round(a1 + (a2 - a1) * amt)) << 24;

			Color.RGBtoHSB((c1 >> 16) & 0xff, (c1 >> 8) & 0xff, c1 & 0xff,
					lerpColorHSB1);
			Color.RGBtoHSB((c2 >> 16) & 0xff, (c2 >> 8) & 0xff, c2 & 0xff,
					lerpColorHSB2);

			float ho = (float) lerpStatic(amt, lerpColorHSB1[0], lerpColorHSB2[0]);
			float so = (float) lerpStatic(amt, lerpColorHSB1[1], lerpColorHSB2[1]);
			float bo = (float) lerpStatic(amt, lerpColorHSB1[2], lerpColorHSB2[2]);

			return alfa | (Color.HSBtoRGB(ho, so, bo) & 0xFFFFFF);
		}
		return 0;
	}

	public static double lerpStatic(double norm, double min, double max) {
		return min + (max - min) * norm;
	}

	/**
	 * @param mode
	 *            either CORNER, CORNERS, or CENTER
	 */

	public final void imageMode(int mode) {
		if ((mode == CORNER) || (mode == CORNERS) || (mode == CENTER)) {
			imageMode = mode;
		} else {
			String msg = "imageMode() only works with CORNER, CORNERS, or CENTER";
			throw new RuntimeException(msg);
		}
	}

	/**
	 * @param mode
	 *            either CORNER, CORNERS, CENTER, or RADIUS
	 */

	public final void rectMode(int mode) {
		rectMode = mode;
	}

	/**
	 * @param mode
	 *            either CENTER, RADIUS, CORNER, or CORNERS
	 */

	public final void ellipseMode(int mode) {
		ellipseMode = mode;

	}

	/**
	 * @param mode
	 *            either CORNER, CORNERS, CENTER
	 */

	public final void shapeMode(int mode) {
		this.shapeMode = mode;
	}

	/**
	 * @param mode
	 *            Either RGB or HSB, corresponding to Red/Green/Blue and Hue/Saturation/Brightness
	 */

	public final void colorMode(int mode) {
		colorMode(mode, colorModeX, colorModeY, colorModeZ, colorModeA);
	}

	/**
	 * @param max
	 *            range for all color elements
	 */

	public final void colorMode(int mode, float max) {
		colorMode(mode, max, max, max, max);
	}

	/**
	 * @param max1
	 *            range for the red or hue depending on the current color mode
	 * @param max2
	 *            range for the green or saturation depending on the current color mode
	 * @param max3
	 *            range for the blue or brightness depending on the current color mode
	 */

	public final void colorMode(int mode, float max1, float max2, float max3) {
		colorMode(mode, max1, max2, max3, colorModeA);
	}

	/**
	 * @param maxA
	 *            range for the alpha
	 */

	public final void colorMode(int mode,
			float max1, float max2, float max3, float maxA) {
		colorMode = mode;

		colorModeX = max1; // still needs to be set for hsb
		colorModeY = max2;
		colorModeZ = max3;
		colorModeA = maxA;

		// if color max values are all 1, then no need to scale
		colorModeScale = ((maxA != 1) || (max1 != max2) || (max2 != max3) || (max3 != maxA));

		// if color is rgb/0..255 this will make it easier for the
		// red() green() etc functions
		colorModeDefault = (colorMode == RGB) &&
				(colorModeA == 255) && (colorModeX == 255) &&
				(colorModeY == 255) && (colorModeZ == 255);
	}

	/**
	 * @param size
	 *            the size of the letters in units of pixels
	 */

	public final void textFont(Font which) {
		if (which == null) {
			throw new RuntimeException(ERROR_TEXTFONT_NULL_PFONT);
		}
		textFontImpl(which, which.getSize2D());
	}

	/**
	 * @param size
	 *            the size of the letters in units of pixels
	 */

	public final void textFont(Font which, float size) {
		if (which == null) {
			throw new RuntimeException(ERROR_TEXTFONT_NULL_PFONT);
		}
		// https://github.com/processing/processing/issues/3110
		if (size <= 0) {
			// Using System.err instead of showWarning to avoid running out of
			// memory with a bunch of textSize() variants (cause of this bug is
			// usually something done with map() or in a loop).
			System.err.println("textFont: ignoring size " + size + " px:" +
					"the text size must be larger than zero");
			size = which.getSize2D();
		}
		textFontImpl(which, size);
	}

	public final void textLeading(float leading) {
		textLeading = leading;
	}

	/**
	 * @param alignX
	 *            horizontal alignment, either LEFT, CENTER, or RIGHT
	 * @param alignY
	 *            vertical alignment, either TOP, BOTTOM, CENTER, or BASELINE
	 */

	public final void textAlign(int alignX, int alignY) {
		textAlign = alignX;
		textAlignY = alignY;
	}

	/**
	 * @param mode
	 *            either MODEL or SHAPE
	 */

	public final void textMode(int mode) {
		// CENTER and MODEL overlap (they're both 3)
		if ((mode == LEFT) || (mode == RIGHT)) {
			error("Since Processing 1.0 beta, textMode() is now textAlign().");
			return;
		}
		if (mode == SCREEN) {
			error("textMode(SCREEN) has been removed from Processing 2.0.");
			return;
		}

		if (textModeCheck(mode)) {
			textMode = mode;
		} else {
			String modeStr = String.valueOf(mode);
			switch (mode) {
				case MODEL:
					modeStr = "MODEL";
					break;
				case SHAPE:
					modeStr = "SHAPE";
					break;
			}
			error("textMode(" + modeStr + ") is not supported by this renderer.");
		}
	}

	private final boolean textModeCheck(int mode) {
		return true;
	}

	private final void textFontImpl(Font which, float size) {
		textFont = which;

		textSize(size);
	}

	public final void textSize(float size) {
		textSize = size;
		textLeading = (textAscent() + textDescent()) * 1.275f;
	}

	public final float textAscent() {
		if (textFont == null) textFont = createDefaultFont();

		return ((float) graphics.getFontMetrics(textFont).getAscent() / textFont.getSize()) * textSize;
	}

	public final float textDescent() {
		if (textFont == null) textFont = createDefaultFont();

		return ((float) graphics.getFontMetrics(textFont).getDescent() / textFont.getSize()) * textSize;
	}

	protected final Font createDefaultFont() {
		return new Font("Lucida Sans", Font.PLAIN, 12);
	}

	public final void string(String text, float x, float y) { text(text, x, y); }

	public final void text(String text, float x, float y) {
		if (textFont == null) textFont = createDefaultFont();

		Font f = textFont.deriveFont(textSize);
		FontMetrics fm = graphics.getFontMetrics(f);
		FontRenderContext frc = fm.getFontRenderContext();
		TextLayout tl = new TextLayout(text, f, frc);
		Shape shape = tl.getOutline(null);

		float w = (float) tl.getBounds().getWidth();
		float h = (float) tl.getBounds().getHeight();

		if (textAlign == CENTER) x -= w / 2;
		else if (textAlign == RIGHT) x -= w;
//		else if (textAlign == LEFT) {} // default

		if (textAlignY == TOP) y += h;
		else if (textAlignY == CENTER) y += h / 2;
		else if (textAlignY == BOTTOM) y -= textDescent();
//		 else if (textAlign == BASELINE) {} // default

		pushMatrix();
		translate(x, y);
		strokeShape(shape);
		fillShape(shape);
		popMatrix();
	}

	public final void circle(float x, float y, float w) {
		ellipse(x, y, w, w);
	}

	public final void ellipse(float x, float y, float w, float h) {
		if (ellipseMode == CORNERS) {
			w = w - x;
			h = h - y;
		} else if (ellipseMode == RADIUS) {
			x = x - w;
			y = y - h;
			w = w * 2;
			h = h * 2;
		} else if (ellipseMode == DIAMETER) {
			x = x - w / 2f;
			y = y - h / 2f;
		}

		if (w < 0) { // undo negative width
			x += w;
			w = -w;
		}

		if (h < 0) { // undo negative height
			y += h;
			h = -h;
		}

		ellipseImpl(x, y, w, h);
	}

	public final void square(float x, float y, float w) {
		rect(x, y, w, w);
	}

	public final void rect(float x, float y, float w, float h) {
		float hradius, vradius;
		switch (rectMode) {
			case CORNERS:
				break;
			case CORNER:
				w += x;
				h += y;
				break;
			case RADIUS:
				hradius = w;
				vradius = h;
				w = x + hradius;
				h = y + vradius;
				x -= hradius;
				y -= vradius;
				break;
			case CENTER:
				hradius = w / 2.0f;
				vradius = h / 2.0f;
				w = x + hradius;
				h = y + vradius;
				x -= hradius;
				y -= vradius;
		}

		if (x > w) {
			float temp = x;
			x = w;
			w = temp;
		}

		if (y > h) {
			float temp = y;
			y = h;
			h = temp;
		}

		rectImpl(x, y, w, h);
	}

	public final void point(float x, float y) {
		if (stroke) {
			line(x, y, x + EPSILON, y + EPSILON);
		}
	}

	public final void line(float x1, float y1, float x2, float y2) {
		line.setLine(x1, y1, x2, y2);
		strokeShape(line);
	}

	public final void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
		gpath = new GeneralPath();
		gpath.moveTo(x1, y1);
		gpath.lineTo(x2, y2);
		gpath.lineTo(x3, y3);
		gpath.closePath();
		drawShape(gpath);
	}

	public final void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		GeneralPath gp = new GeneralPath();
		gp.moveTo(x1, y1);
		gp.lineTo(x2, y2);
		gp.lineTo(x3, y3);
		gp.lineTo(x4, y4);
		gp.closePath();
		drawShape(gp);

	}

	public final void image(SImage img) { image(img.image); }

	public final void image(Image img) { graphics.drawImage(img, 0, 0, null); }

	public final void image(SImage img, float x, float y) { image(img.image, x, y); }

	public final void image(Image img, float x, float y) {
		int _x = Math.round(x);
		int _y = Math.round(y);
		graphics.drawImage(img, _x, _y, null);
	}

	public final void image(SImage img, float x, float y, float w, float h) { image(img.image, x, y, w, h); }

	public final void image(Image img, float x, float y, float w, float h) {
		int _x = Math.round(x);
		int _y = Math.round(y);
		int _w = Math.round(w);
		int _h = Math.round(h);
		graphics.drawImage(img, _x, _y, _w, _h, null);
	}

	public final void image(SImage img,
			float x1, float y1, float x2, float y2,
			float u1, float v1, float u2, float v2) { image(img.image, x1, y1, x2, y2, u1, v1, u2, v2); }

	public final void image(Image img,
			float x1, float y1, float x2, float y2,
			float u1, float v1, float u2, float v2) {
		int _x1 = Math.round(x1);
		int _y1 = Math.round(y1);
		int _x2 = Math.round(x2);
		int _y2 = Math.round(y2);
		int _u1 = Math.round(u1);
		int _v1 = Math.round(v1);
		int _u2 = Math.round(u2);
		int _v2 = Math.round(v2);
		graphics.drawImage(img, _x1, _y1, _x2, _y2, _u1, _v1, _u2, _v2, null);
	}

	private final void rectImpl(float x1, float y1, float x2, float y2) {
		rect.setFrame(x1, y1, x2 - x1, y2 - y1);
		drawShape(rect);
	}

	private final void ellipseImpl(float x, float y, float w, float h) {
		ellipse.setFrame(x, y, w, h);
		drawShape(ellipse);
	}

	@SuppressWarnings("unused")
	private final void arcImpl(float x, float y, float w, float h, float start, float stop, int mode) {
		start = -start * RAD_TO_DEG;
		stop = -stop * RAD_TO_DEG;
		float sweep = stop - start;

		int fillMode = Arc2D.PIE;
		int strokeMode = Arc2D.OPEN;

		if (mode == OPEN) {
			fillMode = Arc2D.OPEN;

		} else if (mode == PIE) {
			strokeMode = Arc2D.PIE;

		} else if (mode == CHORD) {
			fillMode = Arc2D.CHORD;
			strokeMode = Arc2D.CHORD;
		}

		if (fill) {
			arc.setArc(x, y, w, h, start, sweep, fillMode);
			fillShape(arc);
		}
		if (stroke) {
			arc.setArc(x, y, w, h, start, sweep, strokeMode);
			strokeShape(arc);
		}
	}

	public final void fillShape(Shape s) {
		if (fillGradient) {
			graphics.setPaint(fillGradientObject);
			graphics.fill(s);
		} else if (fill) {
			graphics.setColor(fillColorObject);
			graphics.fill(s);
		}
	}

	public final void strokeShape(Shape s) {
		if (strokeGradient) {
			graphics.setPaint(strokeGradientObject);
			graphics.draw(s);
		} else if (stroke) {
			graphics.setColor(strokeColorObject);
			graphics.draw(s);
		}
	}

	public final void drawShape(Shape s) {
		if (fillGradient) {
			graphics.setPaint(fillGradientObject);
			graphics.fill(s);
		} else if (fill) {
			graphics.setColor(fillColorObject);
			graphics.fill(s);
		}
		if (strokeGradient) {
			graphics.setPaint(strokeGradientObject);
			graphics.draw(s);
		} else if (stroke) {
			graphics.setColor(strokeColorObject);
			graphics.draw(s);
		}
	}

	public final void translate(double x, double y) { graphics.translate(x, y); }

	public final void rotate(double theta) { graphics.rotate(theta); }

	public final void rotate(double theta, double x, double y) { graphics.rotate(theta, x, y); }

	public final void scale(double xy) { graphics.scale(xy, xy); };

	public final void scale(double x, double y) { graphics.scale(x, y); };

	public final void shear(double x, double y) { graphics.shear(x, y); };

	public final void transform(AffineTransform affineTransform) { graphics.transform(affineTransform); };

	public final void push() {
		pushStyle();
		pushMatrix();
	}

	public final void pop() {
		popStyle();
		popMatrix();
	}

	public final void pushStyle() {
		if (styleStackDepth == styleStack.length) {
			throw new RuntimeException("pushStyle() cannot use push more than " +
					styleStack.length + " times");
		}
		styleStack[styleStackDepth] = getStyle();
		styleStackDepth++;
	}

	public final void popStyle() {
		if (styleStackDepth == 0) {
			throw new RuntimeException("Too many popStyle() without enough pushStyle()");
		}
		styleStackDepth--;
		style(styleStack[styleStackDepth]);
	}

	public void pushMatrix() {
		if (transformCount == transformStack.length) {
			throw new RuntimeException("pushMatrix() cannot use push more than " +
					transformStack.length + " times");
		}
		transformStack[transformCount] = graphics.getTransform();
		transformCount++;
	}

	public void popMatrix() {
		if (transformCount == 0) {
			throw new RuntimeException("missing a pushMatrix() " +
					"to go with that popMatrix()");
		}
		transformCount--;
		graphics.setTransform(transformStack[transformCount]);
	}

	public void resetMatrix() {
		graphics.setTransform(new AffineTransform());
		graphics.scale(1, 1);
	}

	public final Style getStyle() { return getStyle(null); }

	public final Style getStyle(Style s) { // ignore
		if (s == null) s = new Style();

		s.smooth = smooth;

		s.imageMode = imageMode;
		s.rectMode = rectMode;
		s.ellipseMode = ellipseMode;
		s.shapeMode = shapeMode;

		s.colorMode = colorMode;
		s.colorModeX = colorModeX;
		s.colorModeY = colorModeY;
		s.colorModeZ = colorModeZ;
		s.colorModeA = colorModeA;

		s.fill = fill;
		s.fillColor = fillColor;
		s.stroke = stroke;
		s.strokeColor = strokeColor;
		s.strokeWeight = strokeWeight;
		s.strokeCap = strokeCap;
		s.strokeJoin = strokeJoin;

		s.textFont = textFont;
		s.textAlign = textAlign;
		s.textAlignY = textAlignY;
		s.textMode = textMode;
		s.textSize = textSize;
		s.textLeading = textLeading;

		return s;
	}

	public final void style(Style s) {
		smooth(s.smooth);

		imageMode(s.imageMode);
		rectMode(s.rectMode);
		ellipseMode(s.ellipseMode);
		shapeMode(s.shapeMode);

		if (s.fill) {
			fill(s.fillColor);
		} else {
			noFill();
		}
		if (s.stroke) {
			stroke(s.strokeColor);
		} else {
			noStroke();
		}
		strokeWeight(s.strokeWeight);
		strokeCap(s.strokeCap);
		strokeJoin(s.strokeJoin);

		colorMode(s.colorMode,
				s.colorModeX, s.colorModeY, s.colorModeZ, s.colorModeA);

		if (s.textFont != null) {
			textFont(s.textFont, s.textSize);
			textLeading(s.textLeading);
		}
		textAlign(s.textAlign, s.textAlignY);
		textMode(s.textMode);
	}
}
