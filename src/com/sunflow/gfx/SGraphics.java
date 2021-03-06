package com.sunflow.gfx;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiFunction;

import com.sunflow.logging.Log;
import com.sunflow.util.MathUtils;
import com.sunflow.util.Style;

public class SGraphics extends SImage {

	/**
	 * Storage for renderer-specific image data. In 1.x, renderers wrote cache
	 * data into the image object. In 2.x, the renderer has a weak-referenced
	 * map that points at any of the images it has worked on already. When the
	 * images go out of scope, they will be properly garbage collected.
	 * 
	 * Also caches already used Composites
	 */
	protected WeakHashMap<Object, Object> cacheMap = new WeakHashMap<>();

	// ........................................................

	public BufferedImage image;

	public Graphics2D graphics;
//	public Graphics2D g2;

	/// the anti-aliasing level for renderers that support it
	public int smooth;

	// ........................................................

	/// true if defaults() has been called a first time
	protected boolean settingsInited;

	/// true if settings should be re-applied on next beginDraw()
	protected boolean reapplySettings;

	// ........................................................

	/** path to the file being saved for this renderer (if any) */
	protected String path;

	/**
	 * True if this is the main graphics context for a sketch.
	 * False for offscreen buffers retrieved via createGraphics().
	 */
	protected boolean primaryGraphics;

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

	// Tint color for images

	/**
	 * True if tint() is enabled (read-only).
	 *
	 * Using tint/tintColor seems a better option for naming than
	 * tintEnabled/tint because the latter seems ugly, even though
	 * g.tint as the actual color seems a little more intuitive,
	 * it's just that g.tintEnabled is even more unintuitive.
	 * Same goes for fill and stroke, et al.
	 */
	public boolean tint;

	/** tint that was last set (read-only) */
	public int tintColor;

	protected boolean tintAlpha;
	protected float tintR, tintG, tintB, tintA;
	protected int tintRi, tintGi, tintBi, tintAi;

	// ........................................................

	// Fill color

	/** true if fill() is enabled, (read-only) */
	protected boolean fill;

	/** fill that was last set (read-only) */
	protected int fillColor;

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

	// Additional stroke properties

	static protected final float DEFAULT_STROKE_WEIGHT = 1;
	static protected final int DEFAULT_STROKE_JOIN = MITER;
	static protected final int DEFAULT_STROKE_CAP = ROUND;

	protected float strokeWeight = DEFAULT_STROKE_WEIGHT;
	protected int strokeJoin = DEFAULT_STROKE_JOIN;
	protected int strokeCap = DEFAULT_STROKE_CAP;

	// ........................................................

	/** Last background color that was set, zero if an image */
	protected int backgroundColor = 0xffCCCCCC;

	protected boolean backgroundAlpha;
	protected float backgroundR, backgroundG, backgroundB, backgroundA;
	protected int backgroundRi, backgroundGi, backgroundBi, backgroundAi;

	// ........................................................

	/** The current blending mode. */
	protected int blendMode;

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

	protected Color tintColorObject;

	protected Color fillColorObject;
	public boolean fillGradient;
	public Paint fillGradientObject;

	protected Stroke strokeObject;
	protected Color strokeColorObject;
	public boolean strokeGradient;
	public Paint strokeGradientObject;

	Font fontObject;

	private Composite defaultComposite;
	private static final String ERROR_TEXTFONT_NULL_PFONT = "A null Font was passed to textFont()";

//	public SGraphics() {
//		// In 3.1.2, giving up on the async image saving as the default
//		hints[DISABLE_ASYNC_SAVEFRAME] = true;
//	}

//	public SGraphics(float width, float height) {
//		super(width, height);
//	}

//	public SGraphics(float width, float height, int format) {
//		super(width, height, format);
//	}

//	public SGraphics(BufferedImage bi) { super(bi); }

//	public void setParent(PApplet parent) { // ignore
//		this.parent = parent;
//
//		// Some renderers (OpenGL) need to know what smoothing level will be used
//		// before the rendering surface is even created.
//		smooth = parent.sketchSmooth();
//		pixelDensity = parent.sketchPixelDensity();
//	}

	public void setPrimary(boolean primary) { // ignore
		this.primaryGraphics = primary;

		// base images must be opaque (for performance and general
		// headache reasons.. argh, a semi-transparent opengl surface?)
		// use createGraphics() if you want a transparent surface.
		if (primaryGraphics) {
			format = RGB;
		}
	}

	public void setPath(String path) { // ignore
		this.path = path;
	}

	public void setSize(int w, int h) { // ignore
		width = w;
		height = h;

		/** {@link PImage.pixelFactor} set in {@link PImage#PImage()} */
		pixelWidth = width * pixelDensity;
		pixelHeight = height * pixelDensity;

//		    if (surface != null) {
//		      allocate();
//		    }
//		    reapplySettings();
		reapplySettings = true;
	}

	protected void checkSettings() {
		if (!settingsInited) defaultSettings();
		if (reapplySettings) reapplySettings();
	}

	protected void defaultSettings() { // ignore
//		image = new BufferedImage(width, height, format);
//		super.defaultSettings();
//		graphics = image.createGraphics();

		defaultComposite = graphics.getComposite();

		colorMode(RGB, 255);
		fill(255);
		stroke(0);

		strokeWeight(DEFAULT_STROKE_WEIGHT);
		strokeJoin(DEFAULT_STROKE_JOIN);
		strokeCap(DEFAULT_STROKE_CAP);

		// init shape stuff
		shape = 0;

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
		background(backgroundColor);

		blendMode(BLEND);

		settingsInited = true;
		// defaultSettings() overlaps reapplySettings(), don't do both
		reapplySettings = false;
	}

	protected void reapplySettings() {
		// This might be called by allocate... So if beginDraw() has never run,
		// we don't want to reapply here, we actually just need to let
		// defaultSettings() get called a little from inside beginDraw().
		if (!settingsInited) return; // if this is the initial setup, no need to reapply

		colorMode(colorMode, colorModeX, colorModeY, colorModeZ);
		if (fill) {
//	      PApplet.println("  fill " + PApplet.hex(fillColor));
			fill(fillColor);
		} else {
			noFill();
		}
		if (stroke) {
			stroke(strokeColor);

			// The if() statements should be handled inside the functions,
			// otherwise an actual reset/revert won't work properly.
			// if (strokeWeight != DEFAULT_STROKE_WEIGHT) {
			strokeWeight(strokeWeight);
			// }
//	      if (strokeCap != DEFAULT_STROKE_CAP) {
			strokeCap(strokeCap);
//	      }
//	      if (strokeJoin != DEFAULT_STROKE_JOIN) {
			strokeJoin(strokeJoin);
//	      }
		} else {
			noStroke();
		}
		if (tint) {
			tint(tintColor);
		} else {
			noTint();
		}
//	    if (smooth) {
//	      smooth();
//	    } else {
//	      // Don't bother setting this, cuz it'll anger P3D.
//	      noSmooth();
//	    }
		if (textFont != null) {
//	      System.out.println("  textFont in reapply is " + textFont);
			// textFont() resets the leading, so save it in case it's changed
			float saveLeading = textLeading;
			textFont(textFont, textSize);
			textLeading(saveLeading);
		}
		textMode(textMode);
		textAlign(textAlign, textAlignY);
		background(backgroundColor);

		blendMode(blendMode);

		reapplySettings = false;
	}
//	  @Override
//	  public PSurface createSurface() {
//	    return surface = new PSurfaceAWT(this);
//	  }

	/**
	 * Still need a means to get the java.awt.Image object, since getNative()
	 * is going to return the {@link Graphics2D} object.
	 */
	@Override
	public BufferedImage getImage() {
		return image;
	}

	/** Returns the java.awt.Graphics2D object used by this renderer. */
	@Override
	public Object getNative() {
		return graphics;
	}

	public Graphics2D checkImage() {
		if (image == null ||
				image.getWidth() != width * pixelDensity ||
				image.getHeight() != height * pixelDensity) {
			int wide = width * pixelDensity;
			int high = height * pixelDensity;
			image = new BufferedImage(wide, high, BufferedImage.TYPE_INT_ARGB);
		}
		return (Graphics2D) image.getGraphics();
	}

	public void beginDraw() {
		graphics = checkImage();

		// Calling getGraphics() seems to nuke several settings.
		// It seems to be re-creating a new Graphics2D object each time.
		// https://github.com/processing/processing/issues/3331
		if (strokeObject != null) {
			graphics.setStroke(strokeObject);
		}
		// https://github.com/processing/processing/issues/2617
		if (fontObject != null) {
			graphics.setFont(fontObject);
		}
		// https://github.com/processing/processing/issues/4019
		if (blendMode != 0) {
			blendMode(blendMode);
		}
		handleSmooth();

		checkSettings();
		resetMatrix(); // reset model matrix
//		vertexCount = 0;
	}

	public void endDraw() {
		if (primaryGraphics) {} else {
			// TODO this is probably overkill for most tasks...
			loadPixels();
		}
		setModified();
		graphics.dispose();
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

	// BLEND

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
	public void blendMode(int mode) {
		this.blendMode = mode;
		blendModeImpl();
	}

	protected void blendModeImpl() {
		if (blendMode == BLEND) {
			graphics.setComposite(defaultComposite);
		} else {
			Composite comp = (Composite) getCache(blendMode);
			if (comp == null) {
				comp = new Composite() {

					@Override
					public CompositeContext createContext(ColorModel srcColorModel,
							ColorModel dstColorModel,
							RenderingHints hints) {
						return new BlendingContext(blendMode);
					}
				};
				setCache(blendMode, comp);
			}
			graphics.setComposite(comp);
		}
	}

	// Blending implementation cribbed from portions of Romain Guy's
	// demo and terrific writeup on blending modes in Java 2D.
	// http://www.curious-creature.org/2006/09/20/new-blendings-modes-for-java2d/
	private static final class BlendingContext implements CompositeContext {
//		private int mode;
		private BiFunction<Integer, Integer, Integer> blendColorFunc;

		private BlendingContext(int mode) {
//			this.mode = mode;
			this.blendColorFunc = getBlendColorFunc(mode);
		}

		@Override
		public void dispose() {}

		@Override
		public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
			// not sure if this is really necessary, since we control our buffers
			if (src.getSampleModel().getDataType() != DataBuffer.TYPE_INT ||
					dstIn.getSampleModel().getDataType() != DataBuffer.TYPE_INT ||
					dstOut.getSampleModel().getDataType() != DataBuffer.TYPE_INT) {
				throw new IllegalStateException("Source and destination must store pixels as INT.");
			}

			int width = Math.min(src.getWidth(), dstIn.getWidth());
			int height = Math.min(src.getHeight(), dstIn.getHeight());

			int[] srcPixels = new int[width];
			int[] dstPixels = new int[width];

			for (int y = 0; y < height; y++) {
				src.getDataElements(0, y, width, 1, srcPixels);
				dstIn.getDataElements(0, y, width, 1, dstPixels);
				for (int x = 0; x < width; x++) {
//					dstPixels[x] = blendColor(dstPixels[x], srcPixels[x], mode);
					dstPixels[x] = blendColorFunc.apply(dstPixels[x], srcPixels[x]);
				}
				dstOut.setDataElements(0, y, width, 1, dstPixels);
			}
		}
	}

	//////////////////////////////////////////////////////////////

	final public void smooth() { smooth(3); }

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

	final public void smooth(int quality) { // ignore
		if (smooth == quality) return;
		if (quality < 0 || quality > 6) quality = 0;
		this.smooth = quality;
//		handleSmooth();
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

	/**
	 * Smoothing for Java2D is 2 for bilinear, and 3 for bicubic (the default).
	 * Internally, smooth(1) is the default, smooth(0) is noSmooth().
	 */
	protected void handleSmooth2() {
		if (smooth == 0) {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		} else {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			if (smooth == 1 || smooth == 3) { // default is bicubic
				graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			} else if (smooth == 2) {
				graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			}

			// http://docs.oracle.com/javase/tutorial/2d/text/renderinghints.html
			// Oracle Java text anti-aliasing on OS X looks like s*t compared to the
			// text rendering with Apple's old Java 6. Below, several attempts to fix:
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			// Turns out this is the one that actually makes things work.
			// Kerning is still screwed up, however.
			graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
					RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//	    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//	                        RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
//	    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//	                         RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

//	    g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
//	                        RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
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
	public void noTint() {
		tint = false;
	}

	public void tint(int rgb) {
		colorCalc(rgb);
		tintFromCalc();
	}

	/**
	 * @param alpha
	 *            opacity of the image
	 */
	public void tint(int rgb, float alpha) {
		colorCalc(rgb, alpha);
		tintFromCalc();
	}

	/**
	 * @param gray
	 *            specifies a value between white and black
	 */
	public void tint(float gray) {
		colorCalc(gray);
		tintFromCalc();
	}

	public void tint(float gray, float alpha) {
		colorCalc(gray, alpha);
		tintFromCalc();
	}

	/**
	 * @param v1
	 *            red or hue value (depending on current color mode)
	 * @param v2
	 *            green or saturation value (depending on current color mode)
	 * @param v3
	 *            blue or brightness value (depending on current color mode)
	 */
	public void tint(float v1, float v2, float v3) {
		colorCalc(v1, v2, v3);
		tintFromCalc();
	}

	public void tint(float v1, float v2, float v3, float alpha) {
		colorCalc(v1, v2, v3, alpha);
		tintFromCalc();
	}

	protected void tintFromCalc() {
		tint = true;
		tintR = calcR;
		tintG = calcG;
		tintB = calcB;
		tintA = calcA;
		tintRi = calcRi;
		tintGi = calcGi;
		tintBi = calcBi;
		tintAi = calcAi;
		tintColor = calcColor;
		tintAlpha = calcAlpha;
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

	//////////////////////////////////////////////////////////////

	// BACKGROUND

	int[] clearPixels;

	protected void clearPixels(int color) {
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
		WritableRaster raster = getRaster();
		if ((clearPixels == null) || (clearPixels.length < imageWidth)) {
			clearPixels = new int[imageWidth];
		}
		Arrays.fill(clearPixels, 0, imageWidth, backgroundColor);
		for (int i = 0; i < imageHeight; i++) {
			raster.setDataElements(0, i, imageWidth, 1, clearPixels);
		}
	}

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

//			Composite oldComposite = graphics.getComposite();
//			graphics.setComposite(defaultComposite);
//			AffineTransform at = graphics.getTransform();

			pushMatrix();
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
			calcAi = (int) (((argb >> 24) & 0xff) * MathUtils.instance.clamp(0, (alpha / colorModeA), 1));
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
			Log.error("Since Processing 1.0 beta, textMode() is now textAlign().");
			return;
		}
		if (mode == SCREEN) {
			Log.error("textMode(SCREEN) has been removed from Processing 2.0.");
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
			Log.error("textMode(" + modeStr + ") is not supported by this renderer.");
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

	public final void circle(float x, float y, float r) {
		ellipse(x, y, r, r);
	}

	public final void ellipse(float x, float y, float r) {
		ellipse(x, y, r, r);
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

	/**
	 * @param img
	 *            the specified image to be drawn. This method does
	 *            nothing if <code>img</code> is null.
	 */
	public final void image(SImage img) { image(img, 0, 0, img.width, img.height, 0, 0, img.width, img.height); }

	/**
	 * @param img
	 *            the specified image to be drawn. This method does
	 *            nothing if <code>img</code> is null.
	 */
	public final void image(Image img) { image(img, 0, 0, img.getWidth(null), img.getHeight(null), 0, 0, img.getWidth(null), img.getHeight(null)); }

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
	public final void image(SImage img, float x, float y) {
		if (img == null || img.width <= 0 || img.height <= 0) return;

		if (imageMode == CORNER || imageMode == CORNERS) {
			imageImpl(img,
					x, y, x + img.width, y + img.height,
					0, 0, img.width, img.height);

		} else if (imageMode == CENTER) {
			float x1 = x - img.width / 2;
			float y1 = y - img.height / 2;
			imageImpl(img,
					x1, y1, x1 + img.width, y1 + img.height,
					0, 0, img.width, img.height);
		}
	}

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
	public final void image(Image img, float x, float y) {
		if (img == null || img.getWidth(null) <= 0 || img.getHeight(null) <= 0) return;

		if (imageMode == CORNER || imageMode == CORNERS) {
			imageImpl(img,
					x, y, x + img.getWidth(null), y + img.getHeight(null),
					0, 0, img.getWidth(null), img.getHeight(null));

		} else if (imageMode == CENTER) {
			float x1 = x - img.getWidth(null) / 2;
			float y1 = y - img.getHeight(null) / 2;
			imageImpl(img,
					x1, y1, x1 + img.getWidth(null), y1 + img.getHeight(null),
					0, 0, img.getWidth(null), img.getHeight(null));
		}
	}

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
	public final void image(SImage img, float x, float y, float w, float h) {
		image(img, x, y, w, h, 0, 0, img.width, img.height);
	}

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
	public final void image(Image img, float x, float y, float w, float h) {
		image(img, x, y, w, h, 0, 0, img.getWidth(null), img.getHeight(null));
	}

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
	public final void image(SImage img,
			float x, float y, float w, float h,
			int u1, int v1, int u2, int v2) {
		if (img == null || img.width <= 0 || img.height <= 0) return;

		if (imageMode == CORNER) {
			if (w < 0) { // reset a negative width
				x += w;
				w = -w;
			}
			if (h < 0) { // reset a negative height
				y += h;
				h = -h;
			}

			imageImpl(img,
					x, y, x + w, y + h,
					u1, v1, u2, v2);

		} else if (imageMode == CORNERS) {
			if (w < x) { // reverse because x2 < x1
				float temp = x;
				x = w;
				w = temp;
			}
			if (h < y) { // reverse because y2 < y1
				float temp = y;
				y = h;
				h = temp;
			}

			imageImpl(img,
					x, y, w, h,
					u1, v1, u2, v2);

		} else if (imageMode == CENTER) {
			// c and d are width/height
			if (w < 0) w = -w;
			if (h < 0) h = -h;
			float x1 = x - w / 2;
			float y1 = y - h / 2;

			imageImpl(img,
					x1, y1, x1 + w, y1 + h,
					u1, v1, u2, v2);
		}
	}

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
	public final void image(Image img,
			float x, float y, float w, float h,
			int u1, int v1, int u2, int v2) {
		if (img == null || img.getWidth(null) <= 0 || img.getHeight(null) <= 0) return;

		if (imageMode == CORNER) {
			if (w < 0) { // reset a negative width
				x += w;
				w = -w;
			}
			if (h < 0) { // reset a negative height
				y += h;
				h = -h;
			}

			imageImpl(img,
					x, y, x + w, y + h,
					u1, v1, u2, v2);

		} else if (imageMode == CORNERS) {
			if (w < x) { // reverse because x2 < x1
				float temp = x;
				x = w;
				w = temp;
			}
			if (h < y) { // reverse because y2 < y1
				float temp = y;
				y = h;
				h = temp;
			}

			imageImpl(img,
					x, y, w, h,
					u1, v1, u2, v2);

		} else if (imageMode == CENTER) {
			// c and d are width/height
			if (w < 0) w = -w;
			if (h < 0) h = -h;
			float x1 = x - w / 2;
			float y1 = y - h / 2;

			imageImpl(img,
					x1, y1, x1 + w, y1 + h,
					u1, v1, u2, v2);
		}
	}

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
	protected void imageImpl(SImage img,
			float x, float y, float w, float h,
			int u1, int v1, int u2, int v2) {
		// Image not ready yet, or an error
		if (img == null || img.width <= 0 || img.height <= 0) return;

		ImageCache cash = (ImageCache) getCache(img);

		// Nuke the cache if the image was resized
		if (cash != null) {
			if (img.width != cash.image.getWidth() ||
					img.height != cash.image.getHeight()) {
				cash = null;
			}
		}

		if (cash == null) {
			// System.out.println("making new image cache");
			cash = new ImageCache(); // who);
			setCache(img, cash);
			img.updatePixels(); // mark the whole thing for update
			img.setModified();
		}

		// If image previously was tinted, or the color changed
		// or the image was tinted, and tint is now disabled
		if ((tint && !cash.tinted) ||
				(tint && (cash.tintedColor != tintColor)) ||
				(!tint && cash.tinted)) {
			// For tint change, mark all pixels as needing update.
			img.updatePixels();
		}

		if (img.isModified()) {
			if (img.pixels == null) {
				// This might be a PGraphics that hasn't been drawn to yet.
				// Can't just bail because the cache has been created above.
				// https://github.com/processing/processing/issues/2208
				img.pixels = new int[img.width * img.height];
			}
			cash.update(img, tint, tintColor);
			img.setModified(false);
		}

//	    u1 *= who.pixelDensity;
//	    v1 *= who.pixelDensity;
//	    u2 *= who.pixelDensity;
//	    v2 *= who.pixelDensity;

		graphics.drawImage(((ImageCache) getCache(img)).image,
				(int) x, (int) y, (int) w, (int) h,
				u1, v1, u2, v2, null);
	}

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
	protected void imageImpl(Image img,
			float x1, float y1, float x2, float y2,
			int u1, int v1, int u2, int v2) {
		// Image not ready yet, or an error
		if (img == null || img.getWidth(null) <= 0 || img.getHeight(null) <= 0) return;

		ImageCache cash = (ImageCache) getCache(img);

		// Nuke the cache if the image was resized
		if (cash != null) {
			if (img.getWidth(null) != cash.image.getWidth() ||
					img.getHeight(null) != cash.image.getHeight()) {
				cash = null;
			}
		}

		if (cash == null) {
			// System.out.println("making new image cache");
			cash = new ImageCache(); // who);
			setCache(img, cash);
//			who.updatePixels(); // mark the whole thing for update
//			who.setModified();
		}

		// If image previously was tinted, or the color changed
		// or the image was tinted, and tint is now disabled
//		if ((tint && !cash.tinted) ||
//				(tint && (cash.tintedColor != tintColor)) ||
//				(!tint && cash.tinted)) {
//			// For tint change, mark all pixels as needing update.
//			who.updatePixels();
//		}

//		if (who.isModified()) {
//			if (who.pixels == null) {
//				// This might be a PGraphics that hasn't been drawn to yet.
//				// Can't just bail because the cache has been created above.
//				// https://github.com/processing/processing/issues/2208
//				who.pixels = new int[who.width * who.height];
//			}
		cash.update(img, tint, tintColor);
//			who.setModified(false);
//		}

//	    u1 *= who.pixelDensity;
//	    v1 *= who.pixelDensity;
//	    u2 *= who.pixelDensity;
//	    v2 *= who.pixelDensity;

		graphics.drawImage(((ImageCache) getCache(img)).image,
				(int) x1, (int) y1, (int) x2, (int) y2,
				u1, v1, u2, v2, null);
	}

	public void setCache(Object key, Object val) { // ignore
		cacheMap.put(key, val);
	}

	public Object getCache(Object key) { // ignore
		return cacheMap.get(key);
	}

	static class ImageCache {
		boolean tinted;
		int tintedColor;
		int[] tintedTemp; // one row of tinted pixels
		BufferedImage image;
//	    BufferedImage compat;

//	    public ImageCache(PImage source) {
////	      this.source = source;
//	      // even if RGB, set the image type to ARGB, because the
//	      // image may have an alpha value for its tint().
////	      int type = BufferedImage.TYPE_INT_ARGB;
//	      //System.out.println("making new buffered image");
////	      image = new BufferedImage(source.width, source.height, type);
//	    }

		/**
		 * Update the pixels of the cache image. Already determined that the tint
		 * has changed, or the pixels have changed, so should just go through
		 * with the update without further checks.
		 */
		public void update(SImage source, boolean tint, int tintColor) {
			// int bufferType = BufferedImage.TYPE_INT_ARGB;
			int targetType = ARGB;
			boolean opaque = (tintColor & 0xFF000000) == 0xFF000000;
			if (source.format == RGB) {
				if (!tint || (tint && opaque)) {
					// bufferType = BufferedImage.TYPE_INT_RGB;
					targetType = RGB;
				}
			}
//	      boolean wrongType = (image != null) && (image.getType() != bufferType);
//	      if ((image == null) || wrongType) {
//	        image = new BufferedImage(source.width, source.height, bufferType);
//	      }
			// Must always use an ARGB image, otherwise will write zeros
			// in the alpha channel when drawn to the screen.
			// https://github.com/processing/processing/issues/2030
			if (image == null) {
				image = new BufferedImage(source.width, source.height,
						BufferedImage.TYPE_INT_ARGB);
			}

			WritableRaster wr = image.getRaster();
			if (tint) {
				if (tintedTemp == null || tintedTemp.length != source.width) {
					tintedTemp = new int[source.width];
				}
				int a2 = (tintColor >> 24) & 0xff;
//	        System.out.println("tint color is " + a2);
//	        System.out.println("source.pixels[0] alpha is " + (source.pixels[0] >>> 24));
				int r2 = (tintColor >> 16) & 0xff;
				int g2 = (tintColor >> 8) & 0xff;
				int b2 = (tintColor) & 0xff;

				// if (bufferType == BufferedImage.TYPE_INT_RGB) {
				if (targetType == RGB) {
					// The target image is opaque, meaning that the source image has no
					// alpha (is not ARGB), and the tint has no alpha.
					int index = 0;
					for (int y = 0; y < source.height; y++) {
						for (int x = 0; x < source.width; x++) {
							int argb1 = source.pixels[index++];
							int r1 = (argb1 >> 16) & 0xff;
							int g1 = (argb1 >> 8) & 0xff;
							int b1 = (argb1) & 0xff;

							// Prior to 2.1, the alpha channel was commented out here,
							// but can't remember why (just thought unnecessary b/c of RGB?)
							// https://github.com/processing/processing/issues/2030
							tintedTemp[x] = 0xFF000000 |
									(((r2 * r1) & 0xff00) << 8) |
									((g2 * g1) & 0xff00) |
									(((b2 * b1) & 0xff00) >> 8);
						}
						wr.setDataElements(0, y, source.width, 1, tintedTemp);
					}
					// could this be any slower?
//	          float[] scales = { tintR, tintG, tintB };
//	          float[] offsets = new float[3];
//	          RescaleOp op = new RescaleOp(scales, offsets, null);
//	          op.filter(image, image);

					// } else if (bufferType == BufferedImage.TYPE_INT_ARGB) {
				} else if (targetType == ARGB) {
					if (source.format == RGB &&
							(tintColor & 0xffffff) == 0xffffff) {
						int hi = tintColor & 0xff000000;
						int index = 0;
						for (int y = 0; y < source.height; y++) {
							for (int x = 0; x < source.width; x++) {
								tintedTemp[x] = hi | (source.pixels[index++] & 0xFFFFFF);
							}
							wr.setDataElements(0, y, source.width, 1, tintedTemp);
						}
					} else {
						int index = 0;
						for (int y = 0; y < source.height; y++) {
							if (source.format == RGB) {
								int alpha = tintColor & 0xFF000000;
								for (int x = 0; x < source.width; x++) {
									int argb1 = source.pixels[index++];
									int r1 = (argb1 >> 16) & 0xff;
									int g1 = (argb1 >> 8) & 0xff;
									int b1 = (argb1) & 0xff;
									tintedTemp[x] = alpha |
											(((r2 * r1) & 0xff00) << 8) |
											((g2 * g1) & 0xff00) |
											(((b2 * b1) & 0xff00) >> 8);
								}
							} else if (source.format == ARGB) {
								for (int x = 0; x < source.width; x++) {
									int argb1 = source.pixels[index++];
									int a1 = (argb1 >> 24) & 0xff;
									int r1 = (argb1 >> 16) & 0xff;
									int g1 = (argb1 >> 8) & 0xff;
									int b1 = (argb1) & 0xff;
									tintedTemp[x] = (((a2 * a1) & 0xff00) << 16) |
											(((r2 * r1) & 0xff00) << 8) |
											((g2 * g1) & 0xff00) |
											(((b2 * b1) & 0xff00) >> 8);
								}
							} else if (source.format == ALPHA) {
								int lower = tintColor & 0xFFFFFF;
								for (int x = 0; x < source.width; x++) {
									int a1 = source.pixels[index++];
									tintedTemp[x] = (((a2 * a1) & 0xff00) << 16) | lower;
								}
							}
							wr.setDataElements(0, y, source.width, 1, tintedTemp);
						}
					}
					// Not sure why ARGB images take the scales in this order...
//	          float[] scales = { tintR, tintG, tintB, tintA };
//	          float[] offsets = new float[4];
//	          RescaleOp op = new RescaleOp(scales, offsets, null);
//	          op.filter(image, image);
				}
			} else { // !tint
				if (targetType == RGB && (source.pixels[0] >> 24 == 0)) {
					// If it's an RGB image and the high bits aren't set, need to set
					// the high bits to opaque because we're drawing ARGB images.
					source.filterOPAQUE();
					// Opting to just manipulate the image here, since it shouldn't
					// affect anything else (and alpha(get(x, y)) should return 0xff).
					// Wel also make no guarantees about the values of the pixels array
					// in a PImage and how the high bits will be set.
				}
				// If no tint, just shove the pixels on in there verbatim
				wr.setDataElements(0, 0, source.width, source.height, source.pixels);
			}
			this.tinted = tint;
			this.tintedColor = tintColor;

//	      GraphicsConfiguration gc = parent.getGraphicsConfiguration();
//	      compat = gc.createCompatibleImage(image.getWidth(),
//	                                        image.getHeight(),
//	                                        Transparency.TRANSLUCENT);
			//
//	      Graphics2D g = compat.createGraphics();
//	      g.drawImage(image, 0, 0, null);
//	      g.dispose();
		}

		private BufferedImage toBufferedImage(Image img) {
			if (img instanceof BufferedImage) {
				BufferedImage bimage = (BufferedImage) img;
				if (bimage.getType() == BufferedImage.TYPE_INT_RGB ||
						bimage.getType() == BufferedImage.TYPE_INT_ARGB)
					return bimage;
			}

			// Create a buffered image with transparency
			BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

			// Draw the image on to the buffered image
			Graphics2D bGr = bimage.createGraphics();
			bGr.drawImage(img, 0, 0, null);
			bGr.dispose();

			// Return the buffered image
			return bimage;
		}

		public void update(Image source, boolean tint, int tintColor) {
			BufferedImage bisource = toBufferedImage(source);
			source = bisource;
			int sourceFormat = bisource.getType();
			int sourceWidth = bisource.getWidth();
			int sourceHeight = bisource.getHeight();
//			System.out.println(sourceFormat);
//			System.out.println(sourceWidth);
//			System.out.println(sourceHeight);

			int[] sourcePixels = new int[sourceWidth * sourceHeight];
			{
				WritableRaster raster = bisource.getRaster();
				raster.getDataElements(0, 0, sourceWidth, sourceHeight, sourcePixels);
				if (raster.getNumBands() == 3) {
					// Java won't set the high bits when RGB, returns 0 for alpha
					// https://github.com/processing/processing/issues/2030
					for (int i = 0; i < sourcePixels.length; i++) {
						sourcePixels[i] = 0xff000000 | sourcePixels[i];
					}
				}
			}

			// int bufferType = BufferedImage.TYPE_INT_ARGB;
			int targetType = ARGB;
			boolean opaque = (tintColor & 0xFF000000) == 0xFF000000;
			if (sourceFormat == RGB) {
				if (!tint || (tint && opaque)) {
					// bufferType = BufferedImage.TYPE_INT_RGB;
					targetType = RGB;
				}
			}
//	      boolean wrongType = (image != null) && (image.getType() != bufferType);
//	      if ((image == null) || wrongType) {
//	        image = new BufferedImage(source.width, source.height, bufferType);
//	      }
			// Must always use an ARGB image, otherwise will write zeros
			// in the alpha channel when drawn to the screen.
			// https://github.com/processing/processing/issues/2030
			if (image == null) {
				image = new BufferedImage(sourceWidth, sourceHeight,
						BufferedImage.TYPE_INT_ARGB);
			}

			WritableRaster wr = image.getRaster();
			if (tint) {
				if (tintedTemp == null || tintedTemp.length != sourceWidth) {
					tintedTemp = new int[sourceWidth];
				}
				int a2 = (tintColor >> 24) & 0xff;
//	        System.out.println("tint color is " + a2);
//	        System.out.println("source.pixels[0] alpha is " + (source.pixels[0] >>> 24));
				int r2 = (tintColor >> 16) & 0xff;
				int g2 = (tintColor >> 8) & 0xff;
				int b2 = (tintColor) & 0xff;

				// if (bufferType == BufferedImage.TYPE_INT_RGB) {
				if (targetType == RGB) {
					// The target image is opaque, meaning that the source image has no
					// alpha (is not ARGB), and the tint has no alpha.
					int index = 0;
					for (int y = 0; y < sourceHeight; y++) {
						for (int x = 0; x < sourceWidth; x++) {
							int argb1 = sourcePixels[index++];
							int r1 = (argb1 >> 16) & 0xff;
							int g1 = (argb1 >> 8) & 0xff;
							int b1 = (argb1) & 0xff;

							// Prior to 2.1, the alpha channel was commented out here,
							// but can't remember why (just thought unnecessary b/c of RGB?)
							// https://github.com/processing/processing/issues/2030
							tintedTemp[x] = 0xFF000000 |
									(((r2 * r1) & 0xff00) << 8) |
									((g2 * g1) & 0xff00) |
									(((b2 * b1) & 0xff00) >> 8);
						}
						wr.setDataElements(0, y, sourceWidth, 1, tintedTemp);
					}
					// could this be any slower?
//	          float[] scales = { tintR, tintG, tintB };
//	          float[] offsets = new float[3];
//	          RescaleOp op = new RescaleOp(scales, offsets, null);
//	          op.filter(image, image);

					// } else if (bufferType == BufferedImage.TYPE_INT_ARGB) {
				} else if (targetType == ARGB) {
					if (sourceFormat == RGB &&
							(tintColor & 0xffffff) == 0xffffff) {
						int hi = tintColor & 0xff000000;
						int index = 0;
						for (int y = 0; y < sourceHeight; y++) {
							for (int x = 0; x < sourceWidth; x++) {
								tintedTemp[x] = hi | (sourcePixels[index++] & 0xFFFFFF);
							}
							wr.setDataElements(0, y, sourceWidth, 1, tintedTemp);
						}
					} else {
						int index = 0;
						for (int y = 0; y < sourceHeight; y++) {
							if (sourceFormat == RGB) {
								int alpha = tintColor & 0xFF000000;
								for (int x = 0; x < sourceWidth; x++) {
									int argb1 = sourcePixels[index++];
									int r1 = (argb1 >> 16) & 0xff;
									int g1 = (argb1 >> 8) & 0xff;
									int b1 = (argb1) & 0xff;
									tintedTemp[x] = alpha |
											(((r2 * r1) & 0xff00) << 8) |
											((g2 * g1) & 0xff00) |
											(((b2 * b1) & 0xff00) >> 8);
								}
							} else if (sourceFormat == ARGB) {
								for (int x = 0; x < sourceWidth; x++) {
									int argb1 = sourcePixels[index++];
									int a1 = (argb1 >> 24) & 0xff;
									int r1 = (argb1 >> 16) & 0xff;
									int g1 = (argb1 >> 8) & 0xff;
									int b1 = (argb1) & 0xff;
									tintedTemp[x] = (((a2 * a1) & 0xff00) << 16) |
											(((r2 * r1) & 0xff00) << 8) |
											((g2 * g1) & 0xff00) |
											(((b2 * b1) & 0xff00) >> 8);
								}
							} else if (sourceFormat == ALPHA) {
								int lower = tintColor & 0xFFFFFF;
								for (int x = 0; x < sourceWidth; x++) {
									int a1 = sourcePixels[index++];
									tintedTemp[x] = (((a2 * a1) & 0xff00) << 16) | lower;
								}
							}
							wr.setDataElements(0, y, sourceWidth, 1, tintedTemp);
						}
					}
					// Not sure why ARGB images take the scales in this order...
//	          float[] scales = { tintR, tintG, tintB, tintA };
//	          float[] offsets = new float[4];
//	          RescaleOp op = new RescaleOp(scales, offsets, null);
//	          op.filter(image, image);
				}
			} else { // !tint
				if (targetType == RGB && (sourcePixels[0] >> 24 == 0)) {
					// If it's an RGB image and the high bits aren't set, need to set
					// the high bits to opaque because we're drawing ARGB images.
//					source.filterOPAQUE();
					for (int i = 0; i < sourcePixels.length; i++) {
						sourcePixels[i] |= 0xff000000;
					}
//					sourceFormat = RGB;
					// Opting to just manipulate the image here, since it shouldn't
					// affect anything else (and alpha(get(x, y)) should return 0xff).
					// Wel also make no guarantees about the values of the pixels array
					// in a PImage and how the high bits will be set.
				}
				// If no tint, just shove the pixels on in there verbatim
				wr.setDataElements(0, 0, sourceWidth, sourceHeight, sourcePixels);
			}
			this.tinted = tint;
			this.tintedColor = tintColor;

//	      GraphicsConfiguration gc = parent.getGraphicsConfiguration();
//	      compat = gc.createCompatibleImage(image.getWidth(),
//	                                        image.getHeight(),
//	                                        Transparency.TRANSLUCENT);
			//
//	      Graphics2D g = compat.createGraphics();
//	      g.drawImage(image, 0, 0, null);
//	      g.dispose();
		}
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

		s.blendMode = blendMode;

		s.colorMode = colorMode;
		s.colorModeX = colorModeX;
		s.colorModeY = colorModeY;
		s.colorModeZ = colorModeZ;
		s.colorModeA = colorModeA;

		s.tint = tint;
		s.tintColor = tintColor;
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

		if (blendMode != s.blendMode) {
			blendMode(s.blendMode);
		}

		if (s.tint) {
			tint(s.tintColor);
		} else {
			noTint();
		}
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

	protected WritableRaster getRaster() {
		WritableRaster raster = null;
//		if (primaryGraphics) { // TODO Can image ever be an VolatileImage ? 
//			/*
//			 * // 'offscreen' will probably be removed in the next release
//			 * if (useOffscreen) {
//			 * raster = offscreen.getRaster();
//			 * } else
//			 */
//			if (image instanceof VolatileImage) {
//				// when possible, we'll try VolatileImage
//				raster = ((VolatileImage) image).getSnapshot().getRaster();
//			}
//		}
		if (raster == null) {
			raster = image.getRaster();
		}

		// On Raspberry Pi (and perhaps other platforms, the color buffer won't
		// necessarily be the int array that we'd like. Need to convert it here.
		// Not that this would probably mean getRaster() would need to work more
		// like loadRaster/updateRaster because the pixels will need to be
		// temporarily moved to (and later from) a buffer that's understood by
		// the rest of the Processing source.
		// https://github.com/processing/processing/issues/2010
		if (raster.getTransferType() != DataBuffer.TYPE_INT) {
			System.err.println("See https://github.com/processing/processing/issues/2010");
			throw new RuntimeException("Pixel operations are not supported on this device.");
		}
		return raster;
	}

	@Override
	public void loadPixels() {
		if (pixels == null || (pixels.length != pixelWidth * pixelHeight)) {
			pixels = new int[pixelWidth * pixelHeight];
		}

		WritableRaster raster = getRaster();
		raster.getDataElements(0, 0, pixelWidth, pixelHeight, pixels);
		if (raster.getNumBands() == 3) {
			// Java won't set the high bits when RGB, returns 0 for alpha
			// https://github.com/processing/processing/issues/2030
			for (int i = 0; i < pixels.length; i++) {
				pixels[i] = 0xff000000 | pixels[i];
			}
		}
		// ((BufferedImage) image).getRGB(0, 0, width, height, pixels, 0, width);
//	    WritableRaster raster = ((BufferedImage) (useOffscreen && primarySurface ? offscreen : image)).getRaster();
//	    WritableRaster raster = image.getRaster();
	}

	/**
	 * Update the pixels[] buffer to the PGraphics image.
	 * <P>
	 * Unlike in PImage, where updatePixels() only requests that the
	 * update happens, in PGraphicsJava2D, this will happen immediately.
	 */
	@Override
	public void updatePixels(int x, int y, int c, int d) {
		// if ((x == 0) && (y == 0) && (c == width) && (d == height)) {
//	    System.err.format("%d %d %d %d .. w/h = %d %d .. pw/ph = %d %d %n", x, y, c, d, width, height, pixelWidth, pixelHeight);
		if ((x != 0) || (y != 0) || (c != pixelWidth) || (d != pixelHeight)) {
			// Show a warning message, but continue anyway.
			showWarning("updatePixels(x, y, w, h) is not available with this renderer.");
//	      new Exception().printStackTrace(System.out);
		}
//	    updatePixels();
		if (pixels != null) {
			getRaster().setDataElements(0, 0, pixelWidth, pixelHeight, pixels);
		}
		modified = true;
	}

	static protected Map<String, Object> warnings;

	static public void showWarning(String msg) { // ignore
		if (warnings == null) {
			warnings = new HashMap<>();
		}
		if (!warnings.containsKey(msg)) {
			System.err.println(msg);
			warnings.put(msg, new Object());
		}
	}

	//////////////////////////////////////////////////////////////

	// GET/SET

	static int getset[] = new int[1];

	@Override
	public int get(int x, int y) {
		if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) return 0;
		// return ((BufferedImage) image).getRGB(x, y);
//	    WritableRaster raster = ((BufferedImage) (useOffscreen && primarySurface ? offscreen : image)).getRaster();
		WritableRaster raster = getRaster();
		raster.getDataElements(x, y, getset);
		if (raster.getNumBands() == 3) {
			// https://github.com/processing/processing/issues/2030
			return getset[0] | 0xff000000;
		}
		return getset[0];
	}

	@Override
	protected void getImpl(int sourceX, int sourceY,
			int sourceWidth, int sourceHeight,
			SImage target, int targetX, int targetY) {
		// last parameter to getRGB() is the scan size of the *target* buffer
		// ((BufferedImage) image).getRGB(x, y, w, h, output.pixels, 0, w);
//	    WritableRaster raster =
//	      ((BufferedImage) (useOffscreen && primarySurface ? offscreen : image)).getRaster();
		WritableRaster raster = getRaster();

		if (sourceWidth == target.pixelWidth && sourceHeight == target.pixelHeight) {
			raster.getDataElements(sourceX, sourceY, sourceWidth, sourceHeight, target.pixels);
			// https://github.com/processing/processing/issues/2030
			if (raster.getNumBands() == 3) {
				target.filterOPAQUE();
			}

		} else {
			// TODO optimize, incredibly inefficient to reallocate this much memory
			int[] temp = new int[sourceWidth * sourceHeight];
			raster.getDataElements(sourceX, sourceY, sourceWidth, sourceHeight, temp);

			// Copy the temporary output pixels over to the outgoing image
			int sourceOffset = 0;
			int targetOffset = targetY * target.pixelWidth + targetX;
			for (int y = 0; y < sourceHeight; y++) {
				if (raster.getNumBands() == 3) {
					for (int i = 0; i < sourceWidth; i++) {
						// Need to set the high bits for this feller
						// https://github.com/processing/processing/issues/2030
						target.pixels[targetOffset + i] = 0xFF000000 | temp[sourceOffset + i];
					}
				} else {
					System.arraycopy(temp, sourceOffset, target.pixels, targetOffset, sourceWidth);
				}
				sourceOffset += sourceWidth;
				targetOffset += target.pixelWidth;
			}
		}
	}

	@Override
	public void set(int x, int y, int argb) {
		if ((x < 0) || (y < 0) || (x >= pixelWidth) || (y >= pixelHeight)) return;
//	    ((BufferedImage) image).setRGB(x, y, argb);
		getset[0] = argb;
//	    WritableRaster raster = ((BufferedImage) (useOffscreen && primarySurface ? offscreen : image)).getRaster();
//	    WritableRaster raster = image.getRaster();
		getRaster().setDataElements(x, y, getset);
	}

	@Override
	protected void setImpl(SImage sourceImage,
			int sourceX, int sourceY,
			int sourceWidth, int sourceHeight,
			int targetX, int targetY) {
		WritableRaster raster = getRaster();
//	      ((BufferedImage) (useOffscreen && primarySurface ? offscreen : image)).getRaster();

		if ((sourceX == 0) && (sourceY == 0) &&
				(sourceWidth == sourceImage.pixelWidth) &&
				(sourceHeight == sourceImage.pixelHeight)) {
//	      System.out.format("%d %d  %dx%d  %d%n", targetX, targetY,
//	                             sourceImage.width, sourceImage.height,
//	                             sourceImage.pixels.length);
			raster.setDataElements(targetX, targetY,
					sourceImage.pixelWidth, sourceImage.pixelHeight,
					sourceImage.pixels);
		} else {
			// TODO optimize, incredibly inefficient to reallocate this much memory
			SImage temp = sourceImage.get(sourceX, sourceY, sourceWidth, sourceHeight);
			raster.setDataElements(targetX, targetY, temp.pixelWidth, temp.pixelHeight, temp.pixels);
		}
	}

	static public void showException(String msg) { // ignore
		throw new RuntimeException(msg);
	}
	//////////////////////////////////////////////////////////////

	// ASYNC IMAGE SAVING

	@Override
	public boolean save(String filename) { // ignore
//		if (hints[DISABLE_ASYNC_SAVEFRAME]) {
		return super.save(filename);
//		}

//		if (asyncImageSaver == null) {
//			asyncImageSaver = new AsyncImageSaver();
//		}
//
//		if (!loaded) loadPixels();
//		PImage target = asyncImageSaver.getAvailableTarget(pixelWidth, pixelHeight,
//				format);
//		if (target == null) return false;
//		int count = PApplet.min(pixels.length, target.pixels.length);
//		System.arraycopy(pixels, 0, target.pixels, 0, count);
//		asyncImageSaver.saveTargetAsync(this, target, parent.sketchFile(filename));
//
//		return true;
	}

}
