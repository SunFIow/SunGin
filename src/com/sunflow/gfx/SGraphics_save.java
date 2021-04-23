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
import java.awt.font.TextAttribute;
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
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import com.sunflow.game.GameBase;
import com.sunflow.logging.Log;
import com.sunflow.math.SMatrix2D;
import com.sunflow.math.SMatrix_D;
import com.sunflow.math3d.SMatrix3D;
import com.sunflow.util.MathUtils;
import com.sunflow.util.SStyle;

public class SGraphics_save extends SImage implements SGFX {

	/**
	 * Java AWT Image object associated with this renderer. For the 1.0 version
	 * The offscreen drawing buffer.
	 */

	public Image image;

	/** Surface object that we're talking to */
	protected SSurface surface;

	public Graphics2D graphics;

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

	/**
	 * Array of hint[] items. These are hacks to get around various
	 * temporary workarounds inside the environment.
	 * <p/>
	 * Note that this array cannot be static, as a hint() may result in a
	 * runtime change specific to a renderer. For instance, calling
	 * hint(DISABLE_DEPTH_TEST) has to call glDisable() right away on an
	 * instance of PGraphicsOpenGL.
	 * <p/>
	 * The hints[] array is allocated early on because it might
	 * be used inside beginDraw(), allocate(), etc.
	 */
	protected boolean[] hints = new boolean[HINT_COUNT];

	// ........................................................

	/**
	 * Storage for renderer-specific image data. In 1.x, renderers wrote cache
	 * data into the image object. In 2.x, the renderer has a weak-referenced
	 * map that points at any of the images it has worked on already. When the
	 * images go out of scope, they will be properly garbage collected.
	 * 
	 * Also caches already used Composites
	 */
	protected WeakHashMap<Object, Object> cacheMap = new WeakHashMap<>();

	////////////////////////////////////////////////////////////

	// Vertex fields, moved from PConstants (after 2.0a8) because they're too
	// general to show up in all sketches as defined variables.

	// X, Y and Z are still stored in PConstants because of their general
	// usefulness, and that X we'll always want to be 0, etc.

//	public static final int R = 3; // actual rgb, after lighting
//	public static final int G = 4; // fill stored here, transform in place
//	public static final int B = 5; // TODO don't do that anymore (?)
//	public static final int A = 6;
//
//	public static final int U = 7; // texture
//	public static final int V = 8;
//
//	public static final int NX = 9; // normal
//	public static final int NY = 10;
//	public static final int NZ = 11;
//
//	public static final int EDGE = 12;
//
//	// stroke
//
//	/** stroke argb values */
//	public static final int SR = 13;
//	public static final int SG = 14;
//	public static final int SB = 15;
//	public static final int SA = 16;
//
//	/** stroke weight */
//	public static final int SW = 17;
//
//	// transformations (2D and 3D)
//
//	public static final int TX = 18; // transformed xyzw
//	public static final int TY = 19;
//	public static final int TZ = 20;
//
//	public static final int VX = 21; // view space coords
//	public static final int VY = 22;
//	public static final int VZ = 23;
//	public static final int VW = 24;
//
//	// material properties
//
//	// Ambient color (usually to be kept the same as diffuse)
//	// fill(_) sets both ambient and diffuse.
//	public static final int AR = 25;
//	public static final int AG = 26;
//	public static final int AB = 27;
//
//	// Diffuse is shared with fill.
//	public static final int DR = 3; // TODO needs to not be shared, this is a material property
//	public static final int DG = 4;
//	public static final int DB = 5;
//	public static final int DA = 6;
//
//	// specular (by default kept white)
//	public static final int SPR = 28;
//	public static final int SPG = 29;
//	public static final int SPB = 30;
//
//	public static final int SHINE = 31;
//
//	// emissive (by default kept black)
//	public static final int ER = 32;
//	public static final int EG = 33;
//	public static final int EB = 34;
//
//	// has this vertex been lit yet
//	public static final int BEEN_LIT = 35;
//
//	// has this vertex been assigned a normal yet
//	public static final int HAS_NORMAL = 36;

	public static final int VERTEX_FIELD_COUNT = 2; // 37;

	////////////////////////////////////////////////////////////

	// ........................................................

	/** The current colorMode */
	public int colorMode; // = RGB;

	/** Max value for red/green/blue/alpha (or hue/saturation/value/alpha) set by colorMode */
	public float colorModeX, colorModeY, colorModeZ, colorModeA; // = 255;

	/** True if colors are not in the range 0..1 */
	public boolean colorModeScale; // = true;
	/**
	 * True if colorMode(RGB, 255). Defaults to true so that color()
	 * used as part of a field declaration will properly assign values.
	 */
	public boolean colorModeDefault = true;

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
	public boolean fill;

	/** fill that was last set (read-only) */
	public int fillColor;

	protected boolean fillAlpha;
	protected float fillR, fillG, fillB, fillA;
	protected int fillRi, fillGi, fillBi, fillAi;

	// ........................................................

	// Stroke color

	/** true if stroke() is enabled, (read-only) */
	public boolean stroke;

	/** stroke that was last set (read-only) */
	public int strokeColor;

	protected boolean strokeAlpha;
	protected float strokeR, strokeG, strokeB, strokeA;
	protected int strokeRi, strokeGi, strokeBi, strokeAi;

	// Additional stroke properties

	protected static final float DEFAULT_STROKE_WEIGHT = 1;
	protected static final int DEFAULT_STROKE_JOIN = MITER;
	protected static final int DEFAULT_STROKE_CAP = ROUND;

	public float strokeWeight = DEFAULT_STROKE_WEIGHT;
	public int strokeJoin = DEFAULT_STROKE_JOIN;
	public int strokeCap = DEFAULT_STROKE_CAP;

	// ........................................................

	// Shape placement properties

	// imageMode() is inherited from PImage

	/** The current rect mode (read-only) */
	public int rectMode;

	/** The current ellipse mode (read-only) */
	public int ellipseMode;

	/** The current shape alignment mode (read-only) */
//	protected int shapeMode;

	/** The current image alignment (read-only) */
	public int imageMode = CORNER;

	// ........................................................

	// Text and font properties

	/** The current text font (read-only) */
	public SFont textFont;

	/** The current text align (read-only) */
	public int textAlign = LEFT;

	/** The current vertical text alignment (read-only) */
	public int textAlignY = BASELINE;

	/** The current text mode (read-only) */
	public int textMode = MODEL;

	/** The current text size (read-only) */
	public float textSize;

	/** The current text leading (read-only) */
	public float textLeading;

	private static final String ERROR_TEXTFONT_NULL_FONT = "A null Font was passed to textFont()";

	// ........................................................

	// Style stack

	private static final int STYLE_STACK_DEPTH = 64;
	private SStyle[] styleStack = new SStyle[STYLE_STACK_DEPTH];
	private int styleStackDepth;

	////////////////////////////////////////////////////////////

	/** Last background color that was set, zero if an image */
	public int backgroundColor = 0xffCCCCCC;

	protected boolean backgroundAlpha;
	protected float backgroundR, backgroundG, backgroundB, backgroundA;
	protected int backgroundRi, backgroundGi, backgroundBi, backgroundAi;

	/** The current blending mode. */
	protected int blendMode;

	// ........................................................

	private int transformCount;
	private static final int MATRIX_STACK_DEPTH = 32;
	AffineTransform transformStack[] = new AffineTransform[MATRIX_STACK_DEPTH];

	// ........................................................

	// internal color for setting/calculating
	protected float calcR, calcG, calcB, calcA;
	protected int calcRi, calcGi, calcBi, calcAi;
	protected int calcColor;
	protected boolean calcAlpha;

	/** The last RGB value converted to HSB */
	private int cacheHsbKey;
	/** Result of the last conversion to HSB */
	private float[] cacheHsbValue = new float[3];

	private Line2D.Float line = new Line2D.Float();
	private Ellipse2D.Float ellipse = new Ellipse2D.Float();
	private Rectangle2D.Float rect = new Rectangle2D.Float();
	private Arc2D.Float arc = new Arc2D.Float();

	protected int shape;

	// vertices
	public static final int DEFAULT_VERTICES = 512;
	protected float vertices[][] = new float[DEFAULT_VERTICES][VERTEX_FIELD_COUNT];
	protected int vertexCount;

	private Composite defaultComposite;

	protected GeneralPath gpath;
//	protected ArrayList<Shape> shapes_tmp;

	protected Color tintColorObject;

	protected Color fillColorObject;
	public boolean fillGradient;
	public Paint fillGradientObject;

	protected Stroke strokeObject;
	protected Color strokeColorObject;
	public boolean strokeGradient;
	public Paint strokeGradientObject;

	private Font fontObject;

	/**
	 * Internal buffer used by the text() functions
	 * because the String object is slow
	 */
	protected char[] textBuffer = new char[8 * 1024];
	protected char[] textWidthBuffer = new char[8 * 1024];

	protected int textBreakCount;
	protected int[] textBreakStart;
	protected int[] textBreakStop;

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

	public void setParent(GameBase parent) {
		this.parent = parent;

		// Some renderers (OpenGL) need to know what smoothing level will be used
		// before the rendering surface is even created.
		smooth = parent.getSmooth();
		pixelDensity = parent.getPixelDensity();
	}

	public void setPrimary(boolean primary) {
		this.primaryGraphics = primary;

		// base images must be opaque (for performance and general
		// headache reasons.. argh, a semi-transparent opengl surface?)
		// use createGraphics() if you want a transparent surface.
		if (primaryGraphics) {
			format = RGB;
		}
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public void setSize(int w, int h) {
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

	public void setCache(Object key, Object val) {
		cacheMap.put(key, val);
	}

	public Object getCache(Object key) {
		return cacheMap.get(key);
	}

	public void removeCache(Object key) {
		cacheMap.remove(key);
	}

	public void dispose() { // ignore
		if (primaryGraphics && asyncImageSaver != null) {
			asyncImageSaver.dispose();
			asyncImageSaver = null;
		}
	}

	public SSurface createSurface() {
		return surface = new SScreenAWT(this);
	}

	// JAVA2D
	/**
	 * Still need a means to get the java.awt.Image object, since getNative()
	 * is going to return the {@link Graphics2D} object.
	 */
	@Override
	public Image getImage() {
		return image;
	}

	// JAVA2D
	/** Returns the java.awt.Graphics2D object used by this renderer. */
	@Override
	public Object getNative() {
		return graphics;
	}

	// JAVA2D

	public Graphics2D checkImage() {
//		System.out.println(image);
		int wide = width * pixelDensity;
		int high = height * pixelDensity;
		if (image == null ||
				((BufferedImage) image).getWidth() != wide ||
				((BufferedImage) image).getHeight() != high) {
			image = new BufferedImage(wide, high, BufferedImage.TYPE_INT_ARGB);
		}

//		else {
//			if (image instanceof BufferedImage &&
//					(((BufferedImage) image).getWidth() != width * pixelDensity ||
//							((BufferedImage) image).getHeight() != height * pixelDensity)) {
//				GraphicsConfiguration gc = parent.getGC();
//				image = new BufferedImage(wide, high, BufferedImage.TYPE_INT_ARGB);
//				System.out.println(image);
//				image = gc.createCompatibleImage(wide, high, Transparency.TRANSLUCENT);
//				System.out.println(image);
//			} else if (image instanceof VolatileImage) {
//				GraphicsConfiguration gc = parent.getGC();
//				VolatileImage vimage = (VolatileImage) image;
//				if (vimage.getWidth() != wide || vimage.getHeight() != high || vimage.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
//					image = gc.createCompatibleVolatileImage(wide, high, Transparency.OPAQUE);
//				}
//			}
//
//		}
//		if (primaryGraphics) {
//			if (image == null || ((VolatileImage) image).getWidth() != wide ||
//					((VolatileImage) image).getHeight() != high || ((VolatileImage) image).validate(parent.getGC()) == VolatileImage.IMAGE_INCOMPATIBLE) {
//				GraphicsConfiguration gc = parent.getGC();
//				image = gc.createCompatibleVolatileImage(wide, high, VolatileImage.OPAQUE);
//			}
//		} else {
//			if (image == null || ((BufferedImage) image).getWidth() != wide ||
//					((BufferedImage) image).getHeight() != high) {
//				image = new BufferedImage(wide, high, BufferedImage.TYPE_INT_ARGB);
//			}
//		}
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

	protected void checkSettings() {
		if (!settingsInited) defaultSettings();
		if (reapplySettings) reapplySettings();
	}

	protected void defaultSettings() {
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
		gpath = new GeneralPath();

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

	@Override
	public final void beginShape() { beginShape(POLYGON); }

	// POINTS,LINES, TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, QUADS, and QUAD_STRIP
	/**
	 * @param mode
	 *            POINTS, LINES, TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, QUADS, and QUAD_STRIP
	 */
	@Override
	public void beginShape(int mode) {
		shape = mode;
		vertexCount = 0;
		gpath.reset();
		S_Shape.beginShape(parent);
	}

	protected void vertexCheck() {
		if (vertexCount == vertices.length) {
			float temp[][] = new float[vertexCount << 1][VERTEX_FIELD_COUNT];
			System.arraycopy(vertices, 0, temp, 0, vertexCount);
			vertices = temp;
		}
	}

	@Override
	public void vertex(float x, float y) {
		vertexCheck();

		// not everyone needs this, but just easier to store rather
		// than adding another moving part to the code...
		vertices[vertexCount][X] = x;
		vertices[vertexCount][Y] = y;
		vertexCount++;

		switch (shape) {
			case POINTS:
				point(x, y);
				break;

			case LINES:
				if ((vertexCount % 2) == 0) {
					line(vertices[vertexCount - 2][X],
							vertices[vertexCount - 2][Y], x, y);
				}
				break;

			case TRIANGLES:
				if ((vertexCount % 3) == 0) {
					triangle(vertices[vertexCount - 3][X],
							vertices[vertexCount - 3][Y],
							vertices[vertexCount - 2][X],
							vertices[vertexCount - 2][Y],
							x, y);
					S_Shape.addShape(parent);
				}
				break;

			case TRIANGLE_STRIP:
				if (vertexCount >= 3) {
					triangle(vertices[vertexCount - 2][X],
							vertices[vertexCount - 2][Y],
							vertices[vertexCount - 1][X],
							vertices[vertexCount - 1][Y],
							vertices[vertexCount - 3][X],
							vertices[vertexCount - 3][Y]);
					S_Shape.addShape(parent);
				}
				break;

			case TRIANGLE_FAN:
				if (vertexCount >= 3) {
					// This is an unfortunate implementation because the stroke for an
					// adjacent triangle will be repeated. However, if the stroke is not
					// redrawn, it will replace the adjacent line (when it lines up
					// perfectly) or show a faint line (when off by a small amount).
					// The alternative would be to wait, then draw the shape as a
					// polygon fill, followed by a series of vertices. But that's a
					// poor method when used with PDF, DXF, or other recording objects,
					// since discrete triangles would likely be preferred.
					triangle(vertices[0][X],
							vertices[0][Y],
							vertices[vertexCount - 2][X],
							vertices[vertexCount - 2][Y],
							x, y);
					S_Shape.addShape(parent);
				}
				break;

			case QUAD:
			case QUADS:
				if ((vertexCount % 4) == 0) {
					quad(vertices[vertexCount - 4][X],
							vertices[vertexCount - 4][Y],
							vertices[vertexCount - 3][X],
							vertices[vertexCount - 3][Y],
							vertices[vertexCount - 2][X],
							vertices[vertexCount - 2][Y],
							x, y);
					S_Shape.addShape(parent);
				}
				break;

			case QUAD_STRIP:
				// 0---2---4
				// | | |
				// 1---3---5
				if ((vertexCount >= 4) && ((vertexCount % 2) == 0)) {
					quad(vertices[vertexCount - 4][X],
							vertices[vertexCount - 4][Y],
							vertices[vertexCount - 2][X],
							vertices[vertexCount - 2][Y],
							x, y,
							vertices[vertexCount - 3][X],
							vertices[vertexCount - 3][Y]);
					S_Shape.addShape(parent);
				}
				break;

			case POLYGON:
				if (gpath.getCurrentPoint() == null) {
					gpath.reset();
					gpath.moveTo(x, y);
				} else {
					gpath.lineTo(x, y);
				}
				break;
		}
	}

	@Override
	public void vertex(int[] v) { vertex(v[X], v[Y]); }

	@Override
	public void vertex(float[] v) { vertex(v[X], v[Y]); }

	@Override
	public void endShape() { endShape(OPEN); }

	/**
	 * @param mode
	 *            OPEN or CLOSE
	 */
	@Override
	public void endShape(int mode) {
		if (gpath.getCurrentPoint() == null || shape != POLYGON) {
			S_Shape.endShape(parent);
			shape = 0;
			return;
		}

		if (mode == CLOSE) gpath.closePath();
		drawShape(gpath);

		S_Shape.addShape(parent);
		S_Shape.endShape(parent);

//		boolean completeShape = true;
//		if (shape == POINTS && vertexCount < 1) completeShape = false;
//		if (shape == LINES && vertexCount < 2) completeShape = false;
//		if (shape == TRIANGLES && vertexCount < 3) completeShape = false;
////		if (shape == TRIANGLE_FAN && vertexCount < 0) completeShape = false;
////		if (shape == TRIANGLE_STRIP && vertexCount < 0) completeShape = false;
//		if (shape == QUADS && vertexCount < 4) completeShape = false;
////		if (shape == QUAD_STRIP && vertexCount < 0) completeShape = false;
//
//		if (completeShape) SShape.addShape(this);
//		SShape.endShape(this);
	}

//	private void testVertex() {
//		boolean end = false;
//		if (shape == POINTS && vertexCount == 1) end = true;
//		if (shape == LINES && vertexCount == 2) end = true;
//		if (shape == TRIANGLES && vertexCount == 3) end = true;
////		if (shape == TRIANGLE_FAN && vNum == ??) end = true;
////		if (shape == TRIANGLE_STRIP && vNum == 4) end = true;
//		if (shape == QUADS && vertexCount == 4) end = true;
////		if (shape == QUAD_STRIP && vNum == 4) end = true;
//
//		if (end) {
//			SShape.tempShape = true;
//			endShape(CLOSE);
//			beginShape(shape);
//			SShape.tempShape = false;
//		}
//	}

	//////////////////////////////////////////////////////////////

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
	@Override
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
				setCache(blendMode, comp = new Composite() {

					@Override
					public CompositeContext createContext(ColorModel srcColorModel,
							ColorModel dstColorModel,
							RenderingHints hints) {
						return new BlendingContext(blendMode);
					}
				});
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

	// POINT, LINE, TRIANGLE, QUAD

	@Override
	public void point(float x, float y) {
		if (stroke) line(x, y, x + EPSILON, y + EPSILON);
	}

	@Override
	public void line(float x1, float y1, float x2, float y2) {
		line.setLine(x1, y1, x2, y2);
		strokeShape(line);
	}

	@Override
	public void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
//		gpath = new GeneralPath();
		gpath.reset();
		gpath.moveTo(x1, y1);
		gpath.lineTo(x2, y2);
		gpath.lineTo(x3, y3);
		gpath.closePath();
		drawShape(gpath);
	}

	@Override
	public void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		gpath.reset();
		gpath.moveTo(x1, y1);
		gpath.lineTo(x2, y2);
		gpath.lineTo(x3, y3);
		gpath.lineTo(x4, y4);
		gpath.closePath();
		drawShape(gpath);
	}

	//////////////////////////////////////////////////////////////

	// RECT

	/**
	 * @param mode
	 *            either CORNER, CORNERS, CENTER, or RADIUS
	 */
	@Override
	public void rectMode(int mode) { rectMode = mode; }

	@Override
	public void rect(float x, float y, float w, float h) {
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

	protected void rectImpl(float x1, float y1, float x2, float y2) {
		rect.setFrame(x1, y1, x2 - x1, y2 - y1);
		drawShape(rect);
	}

	@Override
	public void square(float x, float y, float w) { rect(x, y, w, w); }

	//////////////////////////////////////////////////////////////

	// ELLIPSE AND ARC

	/**
	 * @param mode
	 *            either CENTER, RADIUS, CORNER, or CORNERS
	 */
	@Override
	public final void ellipseMode(int mode) { ellipseMode = mode; }

	@Override
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

	protected void ellipseImpl(float x, float y, float w, float h) {
		ellipse.setFrame(x, y, w, h);
		drawShape(ellipse);
	}

	@Override
	public final void ellipse(float x, float y, float r) { ellipse(x, y, r, r); }

	@Override
	public final void circle(float x, float y, float r) { ellipse(x, y, r, r); }

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
	@Override
	public void arc(float a, float b, float c, float d,
			float start, float stop) {
		arc(a, b, c, d, start, stop, 0);
	}

	/*
	 * @param mode either OPEN, CHORD, or PIE
	 */
	@Override
	public void arc(float a, float b, float c, float d,
			float start, float stop, int mode) {
		float x = a;
		float y = b;
		float w = c;
		float h = d;

		if (ellipseMode == CORNERS) {
			w = c - a;
			h = d - b;

		} else if (ellipseMode == RADIUS) {
			x = a - c;
			y = b - d;
			w = c * 2;
			h = d * 2;

		} else if (ellipseMode == CENTER) {
			x = a - c / 2f;
			y = b - d / 2f;
		}

// make sure the loop will exit before starting while
		if (!Float.isInfinite(start) && !Float.isInfinite(stop)) {
// ignore equal and degenerate cases
			if (stop > start) {
				// make sure that we're starting at a useful point
				while (start < 0) {
					start += TWO_PI;
					stop += TWO_PI;
				}

				if (stop - start > TWO_PI) {
					// don't change start, it is visible in PIE mode
					stop = start + TWO_PI;
				}
				arcImpl(x, y, w, h, start, stop, mode);
			}
		}
	}

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

	//////////////////////////////////////////////////////////////

	// SMOOTHING

	@Override
	public final void smooth() { smooth(3); }

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
	@Override
	public final void smooth(int quality) {
		if (smooth == quality) return;
		if (quality < 0 || quality > 6) quality = 0;
		this.smooth = quality;
//		handleSmooth();
	}

	@Override
	public final void noSmooth() {
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

	//////////////////////////////////////////////////////////////

	// IMAGE

	/**
	 * @param mode
	 *            either CORNER, CORNERS, or CENTER
	 */
	@Override
	public final void imageMode(int mode) {
		if ((mode == CORNER) || (mode == CORNERS) || (mode == CENTER)) {
			imageMode = mode;
		} else {
			String msg = "imageMode() only works with CORNER, CORNERS, or CENTER";
			throw new RuntimeException(msg);
		}
	}

	/**
	 * @param img
	 *            the specified image to be drawn. This method does
	 *            nothing if <code>img</code> is null.
	 */
	@Override
	public final void image(SImage img) { image(img, 0, 0, img.width, img.height, 0, 0, img.width, img.height); }

	/**
	 * @param img
	 *            the specified image to be drawn. This method does
	 *            nothing if <code>img</code> is null.
	 */
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
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

		u1 *= img.pixelDensity;
		v1 *= img.pixelDensity;
		u2 *= img.pixelDensity;
		v2 *= img.pixelDensity;

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
					source.filter(OPAQUE);
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

	//////////////////////////////////////////////////////////////

	// SHAPE

//	/**
//	 * @param mode
//	 *            either CORNER, CORNERS, CENTER
//	 */
//	@Override
//	public final void shapeMode(int mode) { this.shapeMode = mode; }

	@Override
	public final void fillShape(Shape s) {
		if (fillGradient) {
			graphics.setPaint(fillGradientObject);
			graphics.fill(s);
		} else if (fill) {
			graphics.setColor(fillColorObject);
			graphics.fill(s);
		}
	}

	@Override
	public final void strokeShape(Shape s) {
		if (strokeGradient) {
			graphics.setPaint(strokeGradientObject);
			graphics.draw(s);
		} else if (stroke) {
			graphics.setColor(strokeColorObject);
			graphics.draw(s);
		}
	}

	@Override
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

	//////////////////////////////////////////////////////////////

	// TEXT/FONTS

	protected SFont createDefaultFont(float size) {
//		return new Font("Lucida Sans", Font.PLAIN, (int) size);
		Font baseFont = new Font("Lucida Sans", Font.PLAIN, 1);
		return createFont(baseFont, size, true, null, false);
	}

	protected SFont createFont(String name, float size,
			boolean smooth, char[] charset) {
		String lowerName = name.toLowerCase();
		Font baseFont = null;

		try {
			InputStream stream = null;
			if (lowerName.endsWith(".otf") || lowerName.endsWith(".ttf")) {
				stream = parent.createInput(name);
				if (stream == null) {
					System.err.println("The font \"" + name + "\" " +
							"is missing or inaccessible, make sure " +
							"the URL is valid or that the file has been " +
							"added to your sketch and is readable.");
					return null;
				}
				baseFont = Font.createFont(Font.TRUETYPE_FONT, parent.createInput(name));

			} else {
				baseFont = SFont.findFont(name);
			}
			return createFont(baseFont, size, smooth, charset, stream != null);

		} catch (Exception e) {
			System.err.println("Problem with createFont(\"" + name + "\")");
			e.printStackTrace();
			return null;
		}
	}

	private SFont createFont(Font baseFont, float size,
			boolean smooth, char[] charset, boolean stream) {
		return new SFont(baseFont.deriveFont(size * parent.pixelDensity),
				smooth, charset, stream,
				parent.pixelDensity);
	}

	@Override
	public void textAlign(int alignX) { textAlign(alignX, BASELINE); }

	/**
	 * @param alignX
	 *            horizontal alignment, either LEFT, CENTER, or RIGHT
	 * @param alignY
	 *            vertical alignment, either TOP, BOTTOM, CENTER, or BASELINE
	 */
	@Override
	public void textAlign(int alignX, int alignY) {
		textAlign = alignX;
		textAlignY = alignY;
	}

	@Override
	public float textAscent() {
		if (textFont == null) defaultFontOrDeath("textAscent");

		Font font = (Font) textFont.getNative();
		if (font != null) return graphics.getFontMetrics(font).getAscent();
		return textFont.ascent() * textSize;

//		return graphics.getFontMetrics(textFont).getAscent();		
//		return ((float) graphics.getFontMetrics(textFont).getAscent() / textFont.getSize()) * textSize;
	}

	@Override
	public float textDescent() {
		if (textFont == null) defaultFontOrDeath("textDescent");

		Font font = (Font) textFont.getNative();
		if (font != null) return graphics.getFontMetrics(font).getDescent();
		return textFont.descent() * textSize;

//		return graphics.getFontMetrics(textFont).getDescent();
//		return ((float) graphics.getFontMetrics(textFont).getDescent() / textFont.getSize()) * textSize;
	}

	@Override
	public void textFont(SFont which) {
		if (which == null) {
			throw new RuntimeException(ERROR_TEXTFONT_NULL_FONT);
		}
//		textFontImpl(which, which.getSize2D());
		textFontImpl(which, which.getDefaultSize());
	}

	/**
	 * @param size
	 *            the size of the letters in units of pixels
	 */

	@Override
	public void textFont(SFont which, float size) {
		if (which == null) {
			throw new RuntimeException(ERROR_TEXTFONT_NULL_FONT);
		}
		textFontImpl(which, size);
	}

	protected void textFontImpl(SFont which, float size) {
		textFont = which;

//		textSize(size);
		handleTextSize(size);
	}

	@Override
	public void textLeading(float leading) { textLeading = leading; }

	/**
	 * @param mode
	 *            either MODEL or SHAPE
	 */
	@Override
	public void textMode(int mode) {
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

	protected boolean textModeCheck(int mode) { return mode == MODEL; }

	@Override
	public final void textSize(float size) {
		// https://github.com/processing/processing/issues/3110
		if (size <= 0) {
			// Using System.err instead of showWarning to avoid running out of
			// memory with a bunch of textSize() variants (cause of this bug is
			// usually something done with map() or in a loop).
			System.err.println("textSize(" + size + ") ignored: " +
					"the text size must be larger than zero");
			return;
		}
		if (textFont == null) {
			defaultFontOrDeath("textSize", size);
		}
		textSizeImpl(size);
	}

	protected void textSizeImpl(float size) {
		handleTextSize(size);
	}

	protected void handleTextSize(float size) {
//		Font font = textFont;
		Font font = (Font) textFont.getNative();

		// don't derive again if the font size has not changed
		if (font != null) {
			if (font.getSize2D() != size) {
				Map<TextAttribute, Object> map = new HashMap<>();
				map.put(TextAttribute.SIZE, size);
				map.put(TextAttribute.KERNING,
						TextAttribute.KERNING_ON);
//	     	 	map.put(TextAttribute.TRACKING,
//	              	 	TextAttribute.TRACKING_TIGHT);
				font = font.deriveFont(map);
			}
			graphics.setFont(font);
			textFont.setNative(font);
			fontObject = font;

			/*
			 * Map<TextAttribute, ?> attrs = font.getAttributes();
			 * for (TextAttribute ta : attrs.keySet()) {
			 * System.out.println(ta + " -> " + attrs.get(ta));
			 * }
			 */
		}

		textSize = size;
		textLeading = (textAscent() + textDescent()) * 1.275f;
	}

	/**
	 * @param c
	 *            the character to measure
	 */
	@Override
	public float textWidth(char c) {
		textWidthBuffer[0] = c;
		return textWidthImpl(textWidthBuffer, 0, 1);
	}

	/**
	 * @param str
	 *            the String of characters to measure
	 */
	@Override
	public float textWidth(String str) {
		if (textFont == null) {
			defaultFontOrDeath("textWidth");
		}

		int length = str.length();
		if (length > textWidthBuffer.length) {
			textWidthBuffer = new char[length + 10];
		}
		str.getChars(0, length, textWidthBuffer, 0);

		float wide = 0;
		int index = 0;
		int start = 0;

		while (index < length) {
			if (textWidthBuffer[index] == '\n') {
				wide = Math.max(wide, textWidthImpl(textWidthBuffer, start, index));
				start = index + 1;
			}
			index++;
		}
		if (start < length) {
			wide = Math.max(wide, textWidthImpl(textWidthBuffer, start, index));
		}
		return wide;
	}

	@Override
	public float textWidth(char[] chars, int start, int length) {
		return textWidthImpl(chars, start, start + length);
	}

	/**
	 * @return the text width of the chars [start, stop) in the buffer.
	 */
	protected float textWidthImpl(char buffer[], int start, int stop) {
		if (textFont == null) defaultFontOrDeath("textWidth");

		// Avoid "Zero length string passed to TextLayout constructor" error
		if (start == stop) return 0;

		Font font = (Font) textFont.getNative();
		if (font != null) {
			FontMetrics metrics = graphics.getFontMetrics(font);
			return (float) metrics.getStringBounds(buffer, start, stop, graphics).getWidth();
		}

		float wide = 0;
		for (int i = start; i < stop; i++) {
			// could add kerning here, but it just ain't implemented
			wide += textFont.width(buffer[i]) * textSize;
		}
		return wide;
	}

	// ........................................................

	@Override
	public void string(String text, float x, float y) { text(text, x, y); }

	@Override
	public void text(char c, float x, float y) {
		if (textFont == null) defaultFontOrDeath("text");

		if (textAlignY == CENTER) y += textAscent() / 2;
		else if (textAlignY == TOP) y += textAscent();
		else if (textAlignY == BOTTOM) y -= textDescent();

		textBuffer[0] = c;
		textLineAlignImpl(textBuffer, 0, 1, x, y);
	}

	/**
	 * <h3>Advanced</h3>
	 * Draw a chunk of text.
	 * Newlines that are \n (Unix newline or linefeed char, ascii 10)
	 * are honored, but \r (carriage return, Windows and Mac OS) are
	 * ignored.
	 */
	@Override
	public void text(String str, float x, float y) {
		if (textFont == null) defaultFontOrDeath("text");

		int length = str.length();
		if (length > textBuffer.length) textBuffer = new char[length + 10];

		str.getChars(0, length, textBuffer, 0);
		text(textBuffer, 0, length, x, y);
	}

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
	@Override
	public void text(char[] chars, int start, int stop, float x, float y) {
		// If multiple lines, sum the height of the additional lines
		float high = 0; // -textAscent();
		for (int i = start; i < stop; i++) {
			if (chars[i] == '\n') {
				high += textLeading;
			}
		}
		if (textAlignY == CENTER) {
			// for a single line, this adds half the textAscent to y
			// for multiple lines, subtract half the additional height
			// y += (textAscent() - textDescent() - high)/2;
			y += (textAscent() - high) / 2;
		} else if (textAlignY == TOP) {
			// for a single line, need to add textAscent to y
			// for multiple lines, no different
			y += textAscent();
		} else if (textAlignY == BOTTOM) {
			// for a single line, this is just offset by the descent
			// for multiple lines, subtract leading for each line
			y -= textDescent() + high;
			// } else if (textAlignY == BASELINE) {
			// do nothing
		}

//	    int start = 0;
		int index = 0;
		while (index < stop) { // length) {
			if (chars[index] == '\n') {
				textLineAlignImpl(chars, start, index, x, y);
				start = index + 1;
				y += textLeading;
			}
			index++;
		}
		if (start < stop) { // length) {
			textLineAlignImpl(chars, start, index, x, y);
		}
	}

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
	@Override
	public void text(String str, float x1, float y1, float x2, float y2) {
		if (textFont == null) {
			defaultFontOrDeath("text");
		}

		float hradius, vradius;
		switch (rectMode) {
			case CORNER:
				x2 += x1;
				y2 += y1;
				break;
			case RADIUS:
				hradius = x2;
				vradius = y2;
				x2 = x1 + hradius;
				y2 = y1 + vradius;
				x1 -= hradius;
				y1 -= vradius;
				break;
			case CENTER:
				hradius = x2 / 2.0f;
				vradius = y2 / 2.0f;
				x2 = x1 + hradius;
				y2 = y1 + vradius;
				x1 -= hradius;
				y1 -= vradius;
		}
		if (x2 < x1) {
			float temp = x1;
			x1 = x2;
			x2 = temp;
		}
		if (y2 < y1) {
			float temp = y1;
			y1 = y2;
			y2 = temp;
		}

//	    float currentY = y1;
		float boxWidth = x2 - x1;

//	    // ala illustrator, the text itself must fit inside the box
//	    currentY += textAscent(); //ascent() * textSize;
//	    // if the box is already too small, tell em to f off
//	    if (currentY > y2) return;

		float spaceWidth = textWidth(' ');

		if (textBreakStart == null) {
			textBreakStart = new int[20];
			textBreakStop = new int[20];
		}
		textBreakCount = 0;

		int length = str.length();
		if (length + 1 > textBuffer.length) {
			textBuffer = new char[length + 1];
		}
		str.getChars(0, length, textBuffer, 0);
		// add a fake newline to simplify calculations
		textBuffer[length++] = '\n';

		int sentenceStart = 0;
		for (int i = 0; i < length; i++) {
			if (textBuffer[i] == '\n') {
//	        currentY = textSentence(textBuffer, sentenceStart, i,
//	                                lineX, boxWidth, currentY, y2, spaceWidth);
				boolean legit = textSentence(textBuffer, sentenceStart, i, boxWidth, spaceWidth);
				if (!legit) break;
//	      if (Float.isNaN(currentY)) break;  // word too big (or error)
//	      if (currentY > y2) break;  // past the box
				sentenceStart = i + 1;
			}
		}

		// lineX is the position where the text starts, which is adjusted
		// to left/center/right based on the current textAlign
		float lineX = x1; // boxX1;
		if (textAlign == CENTER) {
			lineX = lineX + boxWidth / 2f;
		} else if (textAlign == RIGHT) {
			lineX = x2; // boxX2;
		}

		float boxHeight = y2 - y1;
		// int lineFitCount = 1 + PApplet.floor((boxHeight - textAscent()) / textLeading);
		// incorporate textAscent() for the top (baseline will be y1 + ascent)
		// and textDescent() for the bottom, so that lower parts of letters aren't
		// outside the box. [0151]
		float topAndBottom = textAscent() + textDescent();
		int lineFitCount = 1 + MathUtils.instance.floor((boxHeight - topAndBottom) / textLeading);
		int lineCount = Math.min(textBreakCount, lineFitCount);

		if (textAlignY == CENTER) {
			float lineHigh = textAscent() + textLeading * (lineCount - 1);
			float y = y1 + textAscent() + (boxHeight - lineHigh) / 2;
			for (int i = 0; i < lineCount; i++) {
				textLineAlignImpl(textBuffer, textBreakStart[i], textBreakStop[i], lineX, y);
				y += textLeading;
			}

		} else if (textAlignY == BOTTOM) {
			float y = y2 - textDescent() - textLeading * (lineCount - 1);
			for (int i = 0; i < lineCount; i++) {
				textLineAlignImpl(textBuffer, textBreakStart[i], textBreakStop[i], lineX, y);
				y += textLeading;
			}

		} else { // TOP or BASELINE just go to the default
			float y = y1 + textAscent();
			for (int i = 0; i < lineCount; i++) {
				textLineAlignImpl(textBuffer, textBreakStart[i], textBreakStop[i], lineX, y);
				y += textLeading;
			}
		}
	}

	/**
	 * Emit a sentence of text, defined as a chunk of text without any newlines.
	 * 
	 * @param stop
	 *            non-inclusive, the end of the text in question
	 * @return false if cannot fit
	 */
	protected boolean textSentence(char[] buffer, int start, int stop,
			float boxWidth, float spaceWidth) {
		float runningX = 0;

		// Keep track of this separately from index, since we'll need to back up
		// from index when breaking words that are too long to fit.
		int lineStart = start;
		int wordStart = start;
		int index = start;
		while (index <= stop) {
			// boundary of a word or end of this sentence
			if ((buffer[index] == ' ') || (index == stop)) {
//	        System.out.println((index == stop) + " " + wordStart + " " + index);
				float wordWidth = 0;
				if (index > wordStart) {
					// we have a non-empty word, measure it
					wordWidth = textWidthImpl(buffer, wordStart, index);
				}

				if (runningX + wordWidth >= boxWidth) {
					if (runningX != 0) {
						// Next word is too big, output the current line and advance
						index = wordStart;
						textSentenceBreak(lineStart, index);
						// Eat whitespace before the first word on the next line.
						while ((index < stop) && (buffer[index] == ' ')) {
							index++;
						}
					} else { // (runningX == 0)
						// If this is the first word on the line, and its width is greater
						// than the width of the text box, then break the word where at the
						// max width, and send the rest of the word to the next line.
						if (index - wordStart < 25) {
							do {
								index--;
								if (index == wordStart) {
									// Not a single char will fit on this line. screw 'em.
									return false;
								}
								wordWidth = textWidthImpl(buffer, wordStart, index);
							} while (wordWidth > boxWidth);
						} else {
							// This word is more than 25 characters long, might be faster to
							// start from the beginning of the text rather than shaving from
							// the end of it, which is super slow if it's 1000s of letters.
							// https://github.com/processing/processing/issues/211
							int lastIndex = index;
							index = wordStart + 1;
							// walk to the right while things fit
							while ((wordWidth = textWidthImpl(buffer, wordStart, index)) < boxWidth) {
								index++;
								if (index > lastIndex) { // Unreachable?
									break;
								}
							}
							index--;
							if (index == wordStart) {
								return false; // nothing fits
							}
						}

						// textLineImpl(buffer, lineStart, index, x, y);
						textSentenceBreak(lineStart, index);
					}
					lineStart = index;
					wordStart = index;
					runningX = 0;

				} else if (index == stop) {
					// last line in the block, time to unload
					// textLineImpl(buffer, lineStart, index, x, y);
					textSentenceBreak(lineStart, index);
//	          y += textLeading;
					index++;

				} else { // this word will fit, just add it to the line
					runningX += wordWidth;
					wordStart = index; // move on to the next word including the space before the word
					index++;
				}
			} else { // not a space or the last character
				index++; // this is just another letter
			}
		}
//	    return y;
		return true;
	}

	protected void textSentenceBreak(int start, int stop) {
		if (textBreakCount == textBreakStart.length) {
			textBreakStart = GameBase.expand(textBreakStart);
			textBreakStop = GameBase.expand(textBreakStop);
		}
		textBreakStart[textBreakCount] = start;
		textBreakStop[textBreakCount] = stop;
		textBreakCount++;
	}

	@Override
	public void text(int num, float x, float y) { text(String.valueOf(num), x, y); }

	@Override
	public void text(float num, float x, float y) { text(GameBase.nfs(num, 0, 3), x, y); }

	//////////////////////////////////////////////////////////////

	// TEXT IMPL

	// These are most likely to be overridden by subclasses, since the other
	// (public) functions handle generic features like setting alignment.

	protected void textLineAlignImpl(char buffer[], int start, int stop,
			float x, float y) {
		if (textAlign == CENTER) {
			x -= textWidthImpl(buffer, start, stop) / 2f;

		} else if (textAlign == RIGHT) {
			x -= textWidthImpl(buffer, start, stop);
		}

		textLineImpl(buffer, start, stop, x, y);
	}

	/**
	 * Implementation of actual drawing for a line of text.
	 */
	protected void textLineImpl(char buffer[], int start, int stop,
			float x, float y) {
		Font font = (Font) textFont.getNative();
//	    if (font != null && (textFont.isStream() || hints[ENABLE_NATIVE_FONTS])) {
		if (font != null) {
			/*
			 * // save the current setting for text smoothing. note that this is
			 * // different from the smooth() function, because the font smoothing
			 * // is controlled when the font is created, not now as it's drawn.
			 * // fixed a bug in 0116 that handled this incorrectly.
			 * Object textAntialias =
			 * g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
			 * // override the current text smoothing setting based on the font
			 * // (don't change the global smoothing settings)
			 * g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			 * textFont.smooth ?
			 * RenderingHints.VALUE_ANTIALIAS_ON :
			 * RenderingHints.VALUE_ANTIALIAS_OFF);
			 */
			Object antialias = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			if (antialias == null) {
				// if smooth() and noSmooth() not called, this will be null (0120)
				antialias = RenderingHints.VALUE_ANTIALIAS_DEFAULT;
			}

			// override the current smoothing setting based on the font
			// also changes global setting for antialiasing, but this is because it's
			// not possible to enable/disable them independently in some situations.
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					textFont.isSmooth() ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);

			graphics.setColor(fillColorObject);

			int length = stop - start;
			if (length != 0) {
				graphics.drawChars(buffer, start, length, (int) (x + 0.5f), (int) (y + 0.5f));
				// better to use round here? also, drawChars now just calls drawString
//	      g2.drawString(new String(buffer, start, stop - start), Math.round(x), Math.round(y));

				// better to use drawString() with floats? (nope, draws the same)
				// g2.drawString(new String(buffer, start, length), x, y);

				// this didn't seem to help the scaling issue, and creates garbage
				// because of a fairly heavyweight new temporary object
//	      java.awt.font.GlyphVector gv =
//	        font.createGlyphVector(g2.getFontRenderContext(), new String(buffer, start, stop - start));
//	      g2.drawGlyphVector(gv, x, y);
			}

			// return to previous smoothing state if it was changed
			// g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, textAntialias);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialias);

		} else { // otherwise just do the default
			for (int index = start; index < stop; index++) {
				textCharImpl(buffer[index], x, y);

				// this doesn't account for kerning
				x += textWidth(buffer[index]);
			}
		}
	}

	private static String hex(int value, int digits) {
		String stuff = Integer.toHexString(value).toUpperCase();
		if (digits > 8) {
			digits = 8;
		}

		int length = stuff.length();
		if (length > digits) {
			return stuff.substring(length - digits);

		} else if (length < digits) {
			return "00000000".substring(8 - (digits - length)) + stuff;
		}
		return stuff;
	}

	protected void textCharImpl(char ch, float x, float y) {
		SFont.Glyph glyph = textFont.getGlyph(ch);

		if (glyph != null) {
			if (textMode == MODEL) {
				float high = glyph.height / (float) textFont.getSize();
				float bwidth = glyph.width / (float) textFont.getSize();
				float lextent = glyph.leftExtent / (float) textFont.getSize();
				float textent = glyph.topExtent / (float) textFont.getSize();

				float x1 = x + lextent * textSize;
				float y1 = y - textent * textSize;
				float x2 = x1 + bwidth * textSize;
				float y2 = y1 + high * textSize;

				textCharModelImpl(glyph.image,
						x1, y1, x2, y2,
						glyph.width, glyph.height);
			}
		} else if (ch != ' ' && ch != 127) {
//			showWarning("No glyph found for the " + ch + " (\\u" + GameBase.hex(ch, 4) + ") character");
			showWarning("No glyph found for the " + ch + " (\\u" + hex(ch, 4) + ") character");
		}
	}

	protected void textCharModelImpl(SImage glyph,
			float x1, float y1, // float z1,
			float x2, float y2, // float z2,
			int u2, int v2) {
		boolean savedTint = tint;
		int savedTintColor = tintColor;
		float savedTintR = tintR;
		float savedTintG = tintG;
		float savedTintB = tintB;
		float savedTintA = tintA;
		boolean savedTintAlpha = tintAlpha;

		tint = true;
		tintColor = fillColor;
		tintR = fillR;
		tintG = fillG;
		tintB = fillB;
		tintA = fillA;
		tintAlpha = fillAlpha;

		imageImpl(glyph, x1, y1, x2, y2, 0, 0, u2, v2);

		tint = savedTint;
		tintColor = savedTintColor;
		tintR = savedTintR;
		tintG = savedTintG;
		tintB = savedTintB;
		tintA = savedTintA;
		tintAlpha = savedTintAlpha;
	}

	//////////////////////////////////////////////////////////////

	// MATRIX STACK

	@Override
	public final void push() {
		pushStyle();
		pushMatrix();
	}

	@Override
	public final void pop() {
		popStyle();
		popMatrix();
	}

	@Override
	public void pushMatrix() {
		if (transformCount == transformStack.length) {
			throw new RuntimeException("pushMatrix() cannot use push more than " +
					transformStack.length + " times");
		}
		transformStack[transformCount] = graphics.getTransform();
		transformCount++;
	}

	@Override
	public void popMatrix() {
		if (transformCount == 0) {
			throw new RuntimeException("missing a pushMatrix() " +
					"to go with that popMatrix()");
		}
		transformCount--;
		graphics.setTransform(transformStack[transformCount]);
	}

	//////////////////////////////////////////////////////////////

	// MATRIX TRANSFORMS

	@Override
	public final void translate(float x, float y) { graphics.translate(x, y); }

	@Override
	public final void rotate(float theta) { graphics.rotate(theta); }

	@Override
	public final void rotate(float theta, float x, float y) { graphics.rotate(theta, x, y); }

	@Override
	public final void scale(float xy) { graphics.scale(xy, xy); };

	@Override
	public final void scale(float x, float y) { graphics.scale(x, y); };

	@Override
	public final void shear(float x, float y) { graphics.shear(x, y); };

	@Override
	public void shearX(float angle) { graphics.shear(Math.tan(angle), 0); }

	@Override
	public void shearY(float angle) { graphics.shear(0, Math.tan(angle)); }

	@Override
	public final void transform(AffineTransform affineTransform) { graphics.transform(affineTransform); };

	@Override
	public final void setTransform(AffineTransform affineTransform) { graphics.setTransform(affineTransform); };

	//////////////////////////////////////////////////////////////

	// MATRIX MORE

	@Override
	public void resetMatrix() {
		graphics.setTransform(new AffineTransform());
		graphics.scale(pixelDensity, pixelDensity);
	}

	@Override
	public void applyMatrix(SMatrix2D source) {
		applyMatrix(source.m00, source.m01, source.m02,
				source.m10, source.m11, source.m12);
	}

	@Override
	public void applyMatrix(float n00, float n01, float n02,
			float n10, float n11, float n12) {
		graphics.transform(new AffineTransform(n00, n10, n01, n11, n02, n12));
	}

	//////////////////////////////////////////////////////////////

	// MATRIX GET/SET

	@Override
	public SMatrix_D getMatrix() { return getMatrix((SMatrix2D) null); }

	double[] transform = new double[6];

	@Override
	public SMatrix2D getMatrix(SMatrix2D target) {
		if (target == null) {
			target = new SMatrix2D();
		}
		graphics.getTransform().getMatrix(transform);
		target.set((float) transform[0], (float) transform[2], (float) transform[4],
				(float) transform[1], (float) transform[3], (float) transform[5]);
		return target;
	}

	@Override
	public void setMatrix(SMatrix2D source) {
		graphics.setTransform(new AffineTransform(source.m00, source.m10,
				source.m01, source.m11,
				source.m02, source.m12));
	}

	@Override
	public void printMatrix() {
		getMatrix((SMatrix2D) null).print();
	}

	//////////////////////////////////////////////////////////////

	// SCREEN and MODEL transforms

	@Override
	public float screenX(float x, float y) {
		graphics.getTransform().getMatrix(transform);
		return (float) transform[0] * x + (float) transform[2] * y + (float) transform[4];
	}

	@Override
	public float screenY(float x, float y) {
		graphics.getTransform().getMatrix(transform);
		return (float) transform[1] * x + (float) transform[3] * y + (float) transform[5];
	}

	//////////////////////////////////////////////////////////////

	// STYLE

	@Override
	public final void pushStyle() {
		if (styleStackDepth == styleStack.length) {
			throw new RuntimeException("pushStyle() cannot use push more than " +
					styleStack.length + " times");
		}
		styleStack[styleStackDepth] = getStyle();
		styleStackDepth++;
	}

	@Override
	public final void popStyle() {
		if (styleStackDepth == 0) {
			throw new RuntimeException("Too many popStyle() without enough pushStyle()");
		}
		styleStackDepth--;
		style(styleStack[styleStackDepth]);
	}

	@Override
	public final SStyle getStyle() { return getStyle(null); }

	@Override
	public final SStyle getStyle(SStyle s) {
		if (s == null) s = new SStyle();

		s.smooth = smooth;

		s.imageMode = imageMode;
		s.rectMode = rectMode;
		s.ellipseMode = ellipseMode;
//		s.shapeMode = shapeMode;

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

	@Override
	public final void style(SStyle s) {
		smooth(s.smooth);

		imageMode(s.imageMode);
		rectMode(s.rectMode);
		ellipseMode(s.ellipseMode);
//		shapeMode(s.shapeMode);

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

	//////////////////////////////////////////////////////////////

	// STROKE CAP/JOIN/WEIGHT

	@Override
	public final void strokeWeight(float weight) {
		strokeWeight = weight;
		strokeImpl();
	}

	@Override
	public final void strokeJoin(int join) {
		strokeJoin = join;
		strokeImpl();
	}

	@Override
	public final void strokeCap(int cap) {
		strokeCap = cap;
		strokeImpl();
	}

	protected final void strokeImpl() {
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

	//////////////////////////////////////////////////////////////

	// STROKE COLOR

	@Override
	public final void noStroke() { stroke = false; }

	/**
	 * @param rgb
	 *            color value in hexadecimal notation
	 */
	@Override
	public final void stroke(int rgb) {
		colorCalc(rgb);
		strokeFromCalc();
	}

	/**
	 * @param alpha
	 *            opacity of the stroke
	 */
	@Override
	public final void stroke(int rgb, float alpha) {
		colorCalc(rgb, alpha);
		strokeFromCalc();
	}

	/**
	 * @param gray
	 *            specifies a value between white and black
	 */
	@Override
	public final void stroke(float gray) {
		colorCalc(gray);
		strokeFromCalc();
	}

	@Override
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
	@Override
	public final void stroke(float v1, float v2, float v3) {
		colorCalc(v1, v2, v3);
		strokeFromCalc();
	}

	@Override
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

	//////////////////////////////////////////////////////////////

	// TINT COLOR

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
	@Override
	public void noTint() {
		tint = false;
	}

	@Override
	public void tint(int rgb) {
		colorCalc(rgb);
		tintFromCalc();
	}

	/**
	 * @param alpha
	 *            opacity of the image
	 */
	@Override
	public void tint(int rgb, float alpha) {
		colorCalc(rgb, alpha);
		tintFromCalc();
	}

	/**
	 * @param gray
	 *            specifies a value between white and black
	 */
	@Override
	public void tint(float gray) {
		colorCalc(gray);
		tintFromCalc();
	}

	@Override
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
	@Override
	public void tint(float v1, float v2, float v3) {
		colorCalc(v1, v2, v3);
		tintFromCalc();
	}

	@Override
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

		tintColorObject = new Color(tintColor, true);
	}

	//////////////////////////////////////////////////////////////

	// FILL COLOR

	@Override
	public final void noFill() {
		fill = false;
	}

	/**
	 * @param rgb
	 *            color value in hexadecimal notation
	 */
	@Override
	public final void fill(int rgb) {
		colorCalc(rgb);
		fillFromCalc();
	}

	/**
	 * @param alpha
	 *            opacity of the fill
	 */
	@Override
	public final void fill(int rgb, float alpha) {
		colorCalc(rgb, alpha);
		fillFromCalc();
	}

	/**
	 * @param gray
	 *            number specifying value between white and black
	 */
	@Override
	public final void fill(float gray) {
		colorCalc(gray);
		fillFromCalc();
	}

	@Override
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
	@Override
	public final void fill(float v1, float v2, float v3) {
		colorCalc(v1, v2, v3);
		fillFromCalc();
	}

	@Override
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

	//////////////////////////////////////////////////////////////

	// BACKGROUND
	/**
	 * @param rgb
	 *            color value in hexadecimal notation
	 */
	@Override
	public final void background(int rgb) {
		colorCalc(rgb);
		backgroundFromCalc();
	}

	/**
	 * @param alpha
	 *            opacity of the background
	 */
	@Override
	public final void background(int rgb, float alpha) {
		colorCalc(rgb, alpha);
		backgroundFromCalc();
	}

	/**
	 * @param gray
	 *            specifies a value between white and black
	 */
	@Override
	public final void background(float gray) {
		colorCalc(gray);
		backgroundFromCalc();
	}

	@Override
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
	@Override
	public final void background(float v1, float v2, float v3) {
		colorCalc(v1, v2, v3);
		backgroundFromCalc();
	}

	@Override
	public final void background(float v1, float v2, float v3, float alpha) {
		colorCalc(v1, v2, v3, alpha);
		backgroundFromCalc();
	}

	@Override
	public final void clear() { background(0, 0, 0, 0); }

	protected final void backgroundFromCalc() {
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

	private static final String ERROR_BACKGROUND_IMAGE_SIZE = "background image must be the same size as your application";
	private static final String ERROR_BACKGROUND_IMAGE_FORMAT = "background images should be RGB or ARGB";

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
	@Override
	public void background(SImage image) {
		if ((image.pixelWidth != pixelWidth) || (image.pixelHeight != pixelHeight)) {
			throw new RuntimeException(ERROR_BACKGROUND_IMAGE_SIZE);
		}
		if ((image.format != RGB) && (image.format != ARGB)) {
			throw new RuntimeException(ERROR_BACKGROUND_IMAGE_FORMAT);
		}
		backgroundColor = 0; // just zero it out for images
		backgroundImpl(image);
	}

	/**
	 * Actually set the background image. This is separated from the error
	 * handling and other semantic goofiness that is shared across renderers.
	 */
	protected void backgroundImpl(SImage image) {
		// blit image to the screen
		set(0, 0, image);
	}

	private final void backgroundImpl() {
		if (backgroundAlpha) {
			clearPixels(backgroundColor);
		} else {
			Color bgColor = new Color(backgroundColor);
//			Color bgColor = new Color(backgroundColor, calcAlpha); // TODO
			// seems to fire an additional event that causes flickering,
			// like an extra background erase on OS X
//	      if (canvas != null) {
//	        canvas.setBackground(bgColor);
//	      }
			// new Exception().printStackTrace(System.out);
			// in case people do transformations before background(),
			// need to handle this with a push/reset/pop

			Composite oldComposite = graphics.getComposite();
			graphics.setComposite(defaultComposite);
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
			graphics.setComposite(oldComposite);
		}
	}

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

	//////////////////////////////////////////////////////////////

	// COLOR MODE

	/**
	 * @param mode
	 *            Either RGB or HSB, corresponding to Red/Green/Blue and Hue/Saturation/Brightness
	 */
	@Override
	public final void colorMode(int mode) {
		colorMode(mode, colorModeX, colorModeY, colorModeZ, colorModeA);
	}

	/**
	 * @param max
	 *            range for all color elements
	 */
	@Override
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
	@Override
	public final void colorMode(int mode, float max1, float max2, float max3) {
		colorMode(mode, max1, max2, max3, colorModeA);
	}

	/**
	 * @param maxA
	 *            range for the alpha
	 */
	@Override
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

	//////////////////////////////////////////////////////////////

	// COLOR CALCULATIONS

	// Given input values for coloring, these functions will fill the calcXxxx
	// variables with values that have been properly filtered through the
	// current colorMode settings.

	// Renderers that need to subclass any drawing properties such as fill or
	// stroke will usally want to override methods like fillFromCalc (or the
	// same for stroke, ambient, etc.) That way the color calcuations are
	// covered by this based PGraphics class, leaving only a single function
	// to override/implement in the subclass.

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

	//////////////////////////////////////////////////////////////

	// COLOR DATATYPE STUFFING

	@Override
	public final int color(int c) {
		colorCalc(c);
		return calcColor;
	}

	@Override
	public final int color(float gray) {
		colorCalc(gray);
		return calcColor;
	}

	/**
	 * @param c
	 *            can be packed ARGB or a gray in this case
	 */

	@Override
	public final int color(int c, int alpha) {
		colorCalc(c, alpha);
		return calcColor;
	}

	/**
	 * @param c
	 *            can be packed ARGB or a gray in this case
	 */

	@Override
	public final int color(int c, float alpha) {
		colorCalc(c, alpha);
		return calcColor;
	}

	@Override
	public final int color(float gray, float alpha) {
		colorCalc(gray, alpha);
		return calcColor;
	}

	@Override
	public final int color(int v1, int v2, int v3) {
		colorCalc(v1, v2, v3);
		return calcColor;
	}

	@Override
	public final int color(float v1, float v2, float v3) {
		colorCalc(v1, v2, v3);
		return calcColor;
	}

	@Override
	public final int color(int v1, int v2, int v3, int a) {
		colorCalc(v1, v2, v3, a);
		return calcColor;
	}

	@Override
	public final int color(float v1, float v2, float v3, float a) {
		colorCalc(v1, v2, v3, a);
		return calcColor;
	}

	//////////////////////////////////////////////////////////////

	// COLOR DATATYPE EXTRACTION

	@Override
	public final float alpha(int rgb) {
		float outgoing = (rgb >> 24) & 0xff;
		if (colorModeA == 255) return outgoing;
		return (outgoing / 255.0f) * colorModeA;
	}

	@Override
	public final float red(int rgb) {
		float c = (rgb >> 16) & 0xff;
		if (colorModeDefault) return c;
		return (c / 255.0f) * colorModeX;
	}

	@Override
	public final float green(int rgb) {
		float c = (rgb >> 8) & 0xff;
		if (colorModeDefault) return c;
		return (c / 255.0f) * colorModeY;
	}

	@Override
	public final float blue(int rgb) {
		float c = (rgb) & 0xff;
		if (colorModeDefault) return c;
		return (c / 255.0f) * colorModeZ;
	}

	@Override
	public final float hue(int rgb) {
		if (rgb != cacheHsbKey) {
			Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff,
					rgb & 0xff, cacheHsbValue);
			cacheHsbKey = rgb;
		}
		return cacheHsbValue[0] * colorModeX;
	}

	@Override
	public final float saturation(int rgb) {
		if (rgb != cacheHsbKey) {
			Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff,
					rgb & 0xff, cacheHsbValue);
			cacheHsbKey = rgb;
		}
		return cacheHsbValue[1] * colorModeY;
	}

	@Override
	public final float brightness(int rgb) {
		if (rgb != cacheHsbKey) {
			Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff,
					rgb & 0xff, cacheHsbValue);
			cacheHsbKey = rgb;
		}
		return cacheHsbValue[2] * colorModeZ;
	}

	//////////////////////////////////////////////////////////////

	// COLOR DATATYPE INTERPOLATION

	@Override
	public final int lerpColor(int c1, int c2, float amt) {
		return lerpColor(c1, c2, amt, colorMode);
	}

	static float[] lerpColorHSB1;
	static float[] lerpColorHSB2;

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

			float ho = MathUtils.instance.lerp(amt, lerpColorHSB1[0], lerpColorHSB2[0]);
			float so = MathUtils.instance.lerp(amt, lerpColorHSB1[1], lerpColorHSB2[1]);
			float bo = MathUtils.instance.lerp(amt, lerpColorHSB1[2], lerpColorHSB2[2]);

			return alfa | (Color.HSBtoRGB(ho, so, bo) & 0xFFFFFF);
		}
		return 0;
	}

	//////////////////////////////////////////////////////////////

	// WARNINGS and EXCEPTIONS

	protected static Map<String, Object> warnings;

	public static void showWarning(String msg) {
		if (warnings == null) {
			warnings = new HashMap<>();
		}
		if (!warnings.containsKey(msg)) {
			System.err.println(msg);
			warnings.put(msg, new Object());
		}
	}

	public static void showWarning(String msg, Object... args) { showWarning(String.format(msg, args)); }

	public static void showException(String msg) { throw new RuntimeException(msg); }

	/**
	 * Same as below, but defaults to a 12 point font, just as MacWrite intended.
	 */
	protected void defaultFontOrDeath(String method) { defaultFontOrDeath(method, 12); }

	/**
	 * First try to create a default font, but if that's not possible, throw
	 * an exception that halts the program because textFont() has not been used
	 * prior to the specified method.
	 */
	protected void defaultFontOrDeath(String method, float size) {
		if (parent != null) {
			textFont = createDefaultFont(size);
		} else {
			throw new RuntimeException("Use textFont() before " + method + "()");
		}
	}

	//////////////////////////////////////////////////////////////

	// SIMAGE METHODS

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
//			if (image instanceof VolatileImage) {
//				// when possible, we'll try VolatileImage
//				raster = ((VolatileImage) image).getSnapshot().getRaster();
//			} else {
			raster = ((BufferedImage) image).getRaster();
//			}
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
	public void updatePixels(int x, int y, int w, int h) {
		// if ((x == 0) && (y == 0) && (c == width) && (d == height)) {
//	    System.err.format("%d %d %d %d .. w/h = %d %d .. pw/ph = %d %d %n", x, y, c, d, width, height, pixelWidth, pixelHeight);
		if ((x != 0) || (y != 0) || (w != pixelWidth) || (h != pixelHeight)) {
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

	//////////////////////////////////////////////////////////////

	// GET/SET

	static int getset[] = new int[1];

	// JAVA2D
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

	// JAVA2D
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
				target.filter(OPAQUE);
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

	// JAVA2D
	@Override
	public void pixel(int x, int y) { pixel(x, y, strokeColor); }

	// JAVA2D
	@Override
	public void pixel(int x, int y, int argb) {
		if ((x < 0) || (y < 0) || (x >= pixelWidth) || (y >= pixelHeight)) return;
//	    ((BufferedImage) image).setRGB(x, y, argb);
		getset[0] = argb;
//	    WritableRaster raster = ((BufferedImage) (useOffscreen && primarySurface ? offscreen : image)).getRaster();
//	    WritableRaster raster = image.getRaster();
		getRaster().setDataElements(x, y, getset);
	}

	// JAVA2D
	@Override
	public void set(int x, int y, int argb) {
		if ((x < 0) || (y < 0) || (x >= pixelWidth) || (y >= pixelHeight)) return;
//	    ((BufferedImage) image).setRGB(x, y, argb);
		getset[0] = argb;
//	    WritableRaster raster = ((BufferedImage) (useOffscreen && primarySurface ? offscreen : image)).getRaster();
//	    WritableRaster raster = image.getRaster();
		getRaster().setDataElements(x, y, getset);
	}

	// JAVA2D
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

	//////////////////////////////////////////////////////////////

	// COPY

	// JAVA2D
	@Override
	public void copy(int sx, int sy, int sw, int sh,
			int dx, int dy, int dw, int dh) {
		if ((sw != dw) || (sh != dh)) {
			graphics.drawImage(image, dx, dy, dx + dw, dy + dh, sx, sy, sx + sw, sy + sh, null);

		} else {
			dx = dx - sx; // java2d's "dx" is the delta, not dest
			dy = dy - sy;
			graphics.copyArea(sx, sy, sw, sh, dx, dy);
		}
	}

	// JAVA2D
	@Override
	public void copy(SImage src,
			int sx, int sy, int sw, int sh,
			int dx, int dy, int dw, int dh) {
		graphics.drawImage((Image) src.getNative(),
				dx, dy, dx + dw, dy + dh,
				sx, sy, sx + sw, sy + sh, null);
	}
	//////////////////////////////////////////////////////////////

	// ASYNC IMAGE SAVING

	@Override
	public boolean save(String filename) {
		if (hints[DISABLE_ASYNC_SAVEFRAME]) {
			return super.save(filename);
		}

		if (asyncImageSaver == null) {
			asyncImageSaver = new AsyncImageSaver();
		}

		if (!loaded) loadPixels();
		SImage target = asyncImageSaver.getAvailableTarget(pixelWidth, pixelHeight,
				format);
		if (target == null) return false;
		int count = MathUtils.instance.min(pixels.length, target.pixels.length);
		System.arraycopy(pixels, 0, target.pixels, 0, count);
		asyncImageSaver.saveTargetAsync(this, target, parent.sketchFile(filename));

		return true;
	}

	protected void processImageBeforeAsyncSave(SImage image) {}

	/**
	 * If there is running async save task for this file, blocks until it completes.
	 * Has to be called on main thread because OpenGL overrides this and calls GL.
	 * 
	 * @param filename
	 */
	public void awaitAsyncSaveCompletion(String filename) {
		if (asyncImageSaver != null) {
			asyncImageSaver.awaitAsyncSaveCompletion(parent.sketchFile(filename));
		}
	}

	protected static AsyncImageSaver asyncImageSaver;

	protected static class AsyncImageSaver {

		static final int TARGET_COUNT = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

		BlockingQueue<SImage> targetPool = new ArrayBlockingQueue<>(TARGET_COUNT);
		ExecutorService saveExecutor = Executors.newFixedThreadPool(TARGET_COUNT);

		int targetsCreated = 0;

		Map<File, Future<?>> runningTasks = new HashMap<>();
		final Object runningTasksLock = new Object();

		static final int TIME_AVG_FACTOR = 32;

		volatile long avgNanos = 0;
		long lastTime = 0;
		int lastFrameCount = 0;

		public AsyncImageSaver() {} // ignore

		public void dispose() { // ignore
			saveExecutor.shutdown();
			try {
				saveExecutor.awaitTermination(5000, TimeUnit.SECONDS);
			} catch (InterruptedException e) {}
		}

		public boolean hasAvailableTarget() { // ignore
			return targetsCreated < TARGET_COUNT || targetPool.isEmpty();
		}

		/**
		 * After taking a target, you must call saveTargetAsync() or
		 * returnUnusedTarget(), otherwise one thread won't be able to run
		 */
		public SImage getAvailableTarget(int requestedWidth, int requestedHeight, // ignore
				int format) {
			try {
				SImage target;
				if (targetsCreated < TARGET_COUNT && targetPool.isEmpty()) {
					target = new SImage(requestedWidth, requestedHeight);
					targetsCreated++;
				} else {
					target = targetPool.take();
					if (target.pixelWidth != requestedWidth ||
							target.pixelHeight != requestedHeight) {
						// TODO: this kills performance when saving different sizes
						target = new SImage(requestedWidth, requestedHeight);
					}
				}
				target.format = format;
				return target;
			} catch (InterruptedException e) {
				return null;
			}
		}

		public void returnUnusedTarget(SImage target) { // ignore
			targetPool.offer(target);
		}

		public void saveTargetAsync(final SGraphics_save renderer, final SImage target, // ignore
				final File file) {
			target.parent = renderer.parent;

			// if running every frame, smooth the framerate
			if (target.parent.frameCount - 1 == lastFrameCount && TARGET_COUNT > 1) {

				// count with one less thread to reduce jitter
				// 2 cores - 1 save thread - no wait
				// 4 cores - 3 save threads - wait 1/2 of save time
				// 8 cores - 7 save threads - wait 1/6 of save time
				long avgTimePerFrame = avgNanos / (Math.max(1, TARGET_COUNT - 1));
				long now = System.nanoTime();
				long delay = MathUtils.instance.round((lastTime + avgTimePerFrame - now) / 1e6f);
				try {
					if (delay > 0) Thread.sleep(delay);
				} catch (InterruptedException e) {}
			}

			lastFrameCount = target.parent.frameCount;
			lastTime = System.nanoTime();

			awaitAsyncSaveCompletion(file);

			// Explicit lock, because submitting a task and putting it into map
			// has to be atomic (and happen before task tries to remove itself)
			synchronized (runningTasksLock) {
				try {
					Future<?> task = saveExecutor.submit(() -> {
						try {
							long startTime = System.nanoTime();
							renderer.processImageBeforeAsyncSave(target);
							target.save(file.getAbsolutePath());
							long saveNanos = System.nanoTime() - startTime;
							synchronized (AsyncImageSaver.this) {
								if (avgNanos == 0) {
									avgNanos = saveNanos;
								} else if (saveNanos < avgNanos) {
									avgNanos = (avgNanos * (TIME_AVG_FACTOR - 1) + saveNanos) /
											(TIME_AVG_FACTOR);
								} else {
									avgNanos = saveNanos;
								}
							}
						} finally {
							targetPool.offer(target);
							synchronized (runningTasksLock) {
								runningTasks.remove(file);
							}
						}
					});
					runningTasks.put(file, task);
				} catch (RejectedExecutionException e) {
					// the executor service was probably shut down, no more saving for us
				}
			}
		}

		public void awaitAsyncSaveCompletion(final File file) { // ignore
			Future<?> taskWithSameFilename;
			synchronized (runningTasksLock) {
				taskWithSameFilename = runningTasks.get(file);
			}

			if (taskWithSameFilename != null) {
				try {
					taskWithSameFilename.get();
				} catch (InterruptedException | ExecutionException e) {}
			}
		}

	}

	@Override
	public void edge(boolean edge) {}

	@Override
	public void normal(float nx, float ny, float nz) {}

	@Override
	public void textureMode(int mode) {}

	@Override
	public void texture(SImage texture) {}

	@Override
	public void noTexture() {}

	@Override
	public void vertex(float x, float y, float z) {}

	@Override
	public void vertex(float x, float y, float u, float v) {}

	@Override
	public void vertex(float x, float y, float z, float u, float v) {}

	@Override
	public void rotateX(float theta) {}

	@Override
	public void rotateY(float theta) {}

	@Override
	public void rotateZ(float theta) {}

	@Override
	public void rotate(float theta, float x, float y, float z) {}

	@Override
	public void scale(float x, float y, float z) {}

	@Override
	public void applyMatrix(SMatrix_D source) {}

	@Override
	public void applyMatrix(SMatrix3D source) {}

	@Override
	public void applyMatrix(float n00, float n01, float n02, float n03, float n10, float n11, float n12, float n13, float n20, float n21, float n22, float n23, float n30, float n31, float n32, float n33) {}

	@Override
	public SMatrix3D getMatrix(SMatrix3D target) {
		return null;
	}

	@Override
	public void setMatrix(SMatrix_D source) {}

	@Override
	public void setMatrix(SMatrix3D source) {}

	@Override
	public float screenX(float x, float y, float z) {
		return 0;
	}

	@Override
	public float screenY(float x, float y, float z) {
		return 0;
	}

	@Override
	public float screenZ(float x, float y, float z) {
		return 0;
	}

}