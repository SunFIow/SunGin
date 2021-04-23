package com.sunflow.gfx;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.InputStream;
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

import com.sunflow.game.GameBase;
import com.sunflow.math.SMatrix2D;
import com.sunflow.math.SMatrix_D;
import com.sunflow.math3d.SMatrix3D;
import com.sunflow.util.MathUtils;
import com.sunflow.util.SStyle;

public class SGraphics extends SImage implements SGFX {

	/**
	 * Java AWT Image object associated with this renderer. For the 1.0 version
	 * The offscreen drawing buffer.
	 */

	public Image image;

	/** Surface object that we're talking to */
	protected SSurface surface;

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

	public static final int R = 3; // actual rgb, after lighting
	public static final int G = 4; // fill stored here, transform in place
	public static final int B = 5; // TODO don't do that anymore (?)
	public static final int A = 6;

	public static final int U = 7; // texture
	public static final int V = 8;

	public static final int NX = 9; // normal
	public static final int NY = 10;
	public static final int NZ = 11;

	public static final int EDGE = 12;

	// stroke

	/** stroke argb values */
	public static final int SR = 13;
	public static final int SG = 14;
	public static final int SB = 15;
	public static final int SA = 16;

	/** stroke weight */
	public static final int SW = 17;

	// transformations (2D and 3D)

	public static final int TX = 18; // transformed xyzw
	public static final int TY = 19;
	public static final int TZ = 20;

	public static final int VX = 21; // view space coords
	public static final int VY = 22;
	public static final int VZ = 23;
	public static final int VW = 24;

	// material properties

	// Ambient color (usually to be kept the same as diffuse)
	// fill(_) sets both ambient and diffuse.
	public static final int AR = 25;
	public static final int AG = 26;
	public static final int AB = 27;

	// Diffuse is shared with fill.
	public static final int DR = 3; // TODO needs to not be shared, this is a material property
	public static final int DG = 4;
	public static final int DB = 5;
	public static final int DA = 6;

	// specular (by default kept white)
	public static final int SPR = 28;
	public static final int SPG = 29;
	public static final int SPB = 30;

	public static final int SHINE = 31;

	// emissive (by default kept black)
	public static final int ER = 32;
	public static final int EG = 33;
	public static final int EB = 34;

	// has this vertex been lit yet
	public static final int BEEN_LIT = 35;

	// has this vertex been assigned a normal yet
	public static final int HAS_NORMAL = 36;

	public static final int VERTEX_FIELD_COUNT = 37;

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
//	protected int shapeMode; // TODO

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

	public int ambientColor;
	public float ambientR, ambientG, ambientB;
	public boolean setAmbient;

	public int specularColor;
	public float specularR, specularG, specularB;

	public int emissiveColor;
	public float emissiveR, emissiveG, emissiveB;

	public float shininess;

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

	protected static final int MATRIX_STACK_DEPTH = 32;

	static final protected String ERROR_PUSHMATRIX_OVERFLOW = "Too many calls to pushMatrix().";
	static final protected String ERROR_PUSHMATRIX_UNDERFLOW = "Too many calls to popMatrix(), and not enough to pushMatrix().";

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

	protected int shape;

	// vertices
	public static final int DEFAULT_VERTICES = 512;
	protected float vertices[][] = new float[DEFAULT_VERTICES][VERTEX_FIELD_COUNT];
	protected int vertexCount;

	// ........................................................

	protected boolean bezierInited = false;
	public int bezierDetail = 20;

	// used by both curve and bezier, so just init here
	protected SMatrix3D bezierBasisMatrix = new SMatrix3D(-1, 3, -3, 1,
			3, -6, 3, 0,
			-3, 3, 0, 0,
			1, 0, 0, 0);

	// protected PMatrix3D bezierForwardMatrix;
	protected SMatrix3D bezierDrawMatrix;

	// ........................................................

	protected boolean curveInited = false;
	public int curveDetail = 20;
	public float curveTightness = 0;
	// catmull-rom basis matrix, perhaps with optional s parameter
	protected SMatrix3D curveBasisMatrix;
	protected SMatrix3D curveDrawMatrix;

	protected SMatrix3D bezierBasisInverse;
	protected SMatrix3D curveToBezierMatrix;

	// ........................................................

	// spline vertices

	protected float curveVertices[][];
	protected int curveVertexCount;

	// ........................................................

	// precalculate sin/cos lookup tables [toxi]
	// circle resolution is determined from the actual used radii
	// passed to ellipse() method. this will automatically take any
	// scale transformations into account too

	// [toxi 031031]
	// changed table's precision to 0.5 degree steps
	// introduced new vars for more flexible code
	static final protected float sinLUT[];
	static final protected float cosLUT[];
	static final protected float SINCOS_PRECISION = 0.5f;
	static final protected int SINCOS_LENGTH = (int) (360f / SINCOS_PRECISION);
	static {
		sinLUT = new float[SINCOS_LENGTH];
		cosLUT = new float[SINCOS_LENGTH];
		for (int i = 0; i < SINCOS_LENGTH; i++) {
			sinLUT[i] = (float) Math.sin(i * DEG_TO_RAD * SINCOS_PRECISION);
			cosLUT[i] = (float) Math.cos(i * DEG_TO_RAD * SINCOS_PRECISION);
		}
	}

	/**
	 * Internal buffer used by the text() functions
	 * because the String object is slow
	 */
	protected char[] textBuffer = new char[8 * 1024];
	protected char[] textWidthBuffer = new char[8 * 1024];

	protected int textBreakCount;
	protected int[] textBreakStart;
	protected int[] textBreakStop;

	// ........................................................

	public boolean edge = true;

	// ........................................................

	/// normal calculated per triangle
	static protected final int NORMAL_MODE_AUTO = 0;
	/// one normal manually specified per shape
	static protected final int NORMAL_MODE_SHAPE = 1;
	/// normals specified for each shape vertex
	static protected final int NORMAL_MODE_VERTEX = 2;

	/// Current mode for normals, one of AUTO, SHAPE, or VERTEX
	protected int normalMode;

	/// Keep track of how many calls to normal, to determine the mode.
	// protected int normalCount;

	protected boolean autoNormal;

	/** Current normal vector. */
	public float normalX, normalY, normalZ;

	// ........................................................

	/**
	 * Sets whether texture coordinates passed to
	 * vertex() calls will be based on coordinates that are
	 * based on the IMAGE or NORMALIZED.
	 */
	public int textureMode = IMAGE;

	/**
	 * Current horizontal coordinate for texture, will always
	 * be between 0 and 1, even if using textureMode(IMAGE).
	 */
	public float textureU;

	/** Current vertical coordinate for texture, see above. */
	public float textureV;

	/** Current image being used as a texture */
	public SImage textureImage;

	// ........................................................

	// [toxi031031] new & faster sphere code w/ support flexible resolutions
	// will be set by sphereDetail() or 1st call to sphere()
	protected float sphereX[], sphereY[], sphereZ[];

	/// Number of U steps (aka "theta") around longitudinally spanning 2*pi
	public int sphereDetailU = 0;
	/// Number of V steps (aka "phi") along latitudinally top-to-bottom spanning pi
	public int sphereDetailV = 0;

	public SGraphics() {
		// In 3.1.2, giving up on the async image saving as the default
		hints[DISABLE_ASYNC_SAVEFRAME] = true;
	}

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

	public void dispose() { // ignore
		if (primaryGraphics && asyncImageSaver != null) {
			asyncImageSaver.dispose();
			asyncImageSaver = null;
		}
	}

	public SSurface createSurface() {
		return surface = new SScreenNone(this);
	}

	//////////////////////////////////////////////////////////////

	// IMAGE METADATA FOR THIS RENDERER

	public void setCache(Object key, Object val) {
		cacheMap.put(key, val);
	}

	public Object getCache(Object key) {
		return cacheMap.get(key);
	}

	public void removeCache(Object key) {
		cacheMap.remove(key);
	}

	//////////////////////////////////////////////////////////////

	public void beginDraw() {}

	public void endDraw() {}

	public void flush() {
		// no-op, mostly for P3D to write sorted stuff
	}

	protected void checkSettings() {
		if (!settingsInited) defaultSettings();
		if (reapplySettings) reapplySettings();
	}

	protected void defaultSettings() {
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

//	    autoNormal = true;

		// no current font
		textFont = null;
		textSize = 12;
		textLeading = 14;
		textAlign = LEFT;
		textAlignY = BASELINE;
		textMode = MODEL;

//		background(0xffCCCCCC);
		if (primaryGraphics) {
			background(backgroundColor);
		}

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

	//////////////////////////////////////////////////////////////

	// HINTS

	public void hint(int which) {
		if (which == ENABLE_NATIVE_FONTS ||
				which == DISABLE_NATIVE_FONTS) {
			showWarning("hint(ENABLE_NATIVE_FONTS) no longer supported. " +
					"Use createFont() instead.");
		}
//		if (which == ENABLE_KEY_REPEAT) {
//			parent.keyRepeatEnabled = true; // TODO
//		} else if (which == DISABLE_KEY_REPEAT) {
//			parent.keyRepeatEnabled = false;
//		}

		if (which > 0) {
			hints[which] = true;
		} else {
			hints[-which] = false;
		}
	}

	@Override
	public void beginShape() { beginShape(POLYGON); }

	// POINTS,LINES, TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, QUADS, and QUAD_STRIP
	/**
	 * @param mode
	 *            POINTS, LINES, TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, QUADS, and QUAD_STRIP
	 */
	@Override
	public void beginShape(int mode) { shape = mode; }

	@Override
	public void edge(boolean edge) { this.edge = edge; }

	@Override
	public void normal(float nx, float ny, float nz) {
		normalX = nx;
		normalY = ny;
		normalZ = nz;

		// if drawing a shape and the normal hasn't been set yet,
		// then we need to set the normals for each vertex so far
		if (shape != 0) {
			if (normalMode == NORMAL_MODE_AUTO) {
				// One normal per begin/end shape
				normalMode = NORMAL_MODE_SHAPE;
			} else if (normalMode == NORMAL_MODE_SHAPE) {
				// a separate normal for each vertex
				normalMode = NORMAL_MODE_VERTEX;
			}
		}
	}

	@Override
	public void textureMode(int mode) {
		if (mode != IMAGE && mode != NORMAL) {
			throw new RuntimeException("textureMode() only supports IMAGE and NORMAL");
		}
		this.textureMode = mode;
	}

	@Override
	public void texture(SImage image) {
		textureImage = image;
	}

	@Override
	public void noTexture() {
		textureImage = null;
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
		vertexCheck();
		float[] vertex = vertices[vertexCount];

		curveVertexCount = 0;

		vertex[X] = x;
		vertex[Y] = y;
		vertex[Z] = 0;

		vertex[EDGE] = edge ? 1 : 0;

//	    if (fill) {
//	      vertex[R] = fillR;
//	      vertex[G] = fillG;
//	      vertex[B] = fillB;
//	      vertex[A] = fillA;
//	    }
		boolean textured = textureImage != null;
		if (fill || textured) {
			if (!textured) {
				vertex[R] = fillR;
				vertex[G] = fillG;
				vertex[B] = fillB;
				vertex[A] = fillA;
			} else {
				if (tint) {
					vertex[R] = tintR;
					vertex[G] = tintG;
					vertex[B] = tintB;
					vertex[A] = tintA;
				} else {
					vertex[R] = 1;
					vertex[G] = 1;
					vertex[B] = 1;
					vertex[A] = 1;
				}
			}
		}

		if (stroke) {
			vertex[SR] = strokeR;
			vertex[SG] = strokeG;
			vertex[SB] = strokeB;
			vertex[SA] = strokeA;
			vertex[SW] = strokeWeight;
		}

		if (textured) {
			vertex[U] = textureU;
			vertex[V] = textureV;
		}

		if (autoNormal) {
			float norm2 = normalX * normalX + normalY * normalY + normalZ * normalZ;
			if (norm2 < EPSILON) {
				vertex[HAS_NORMAL] = 0;
			} else {
				if (Math.abs(norm2 - 1) > EPSILON) {
					// The normal vector is not normalized.
					float norm = MathUtils.instance.sqrt(norm2);
					normalX /= norm;
					normalY /= norm;
					normalZ /= norm;
				}
				vertex[HAS_NORMAL] = 1;
			}
		} else {
			vertex[HAS_NORMAL] = 1;
		}

		vertexCount++;
	}

	@Override
	public void vertex(float x, float y, float z) {
		vertexCheck();
		float[] vertex = vertices[vertexCount];

		// only do this if we're using an irregular (POLYGON) shape that
		// will go through the triangulator. otherwise it'll do thinks like
		// disappear in mathematically odd ways
		// http://dev.processing.org/bugs/show_bug.cgi?id=444
		if (shape == POLYGON) {
			if (vertexCount > 0) {
				float pvertex[] = vertices[vertexCount - 1];
				if ((Math.abs(pvertex[X] - x) < EPSILON) &&
						(Math.abs(pvertex[Y] - y) < EPSILON) &&
						(Math.abs(pvertex[Z] - z) < EPSILON)) {
					// this vertex is identical, don't add it,
					// because it will anger the triangulator
					return;
				}
			}
		}

		// User called vertex(), so that invalidates anything queued up for curve
		// vertices. If this is internally called by curveVertexSegment,
		// then curveVertexCount will be saved and restored.
		curveVertexCount = 0;

		vertex[X] = x;
		vertex[Y] = y;
		vertex[Z] = z;

		vertex[EDGE] = edge ? 1 : 0;

		boolean textured = textureImage != null;
		if (fill || textured) {
			if (!textured) {
				vertex[R] = fillR;
				vertex[G] = fillG;
				vertex[B] = fillB;
				vertex[A] = fillA;
			} else {
				if (tint) {
					vertex[R] = tintR;
					vertex[G] = tintG;
					vertex[B] = tintB;
					vertex[A] = tintA;
				} else {
					vertex[R] = 1;
					vertex[G] = 1;
					vertex[B] = 1;
					vertex[A] = 1;
				}
			}

			vertex[AR] = ambientR;
			vertex[AG] = ambientG;
			vertex[AB] = ambientB;

			vertex[SPR] = specularR;
			vertex[SPG] = specularG;
			vertex[SPB] = specularB;
			// vertex[SPA] = specularA;

			vertex[SHINE] = shininess;

			vertex[ER] = emissiveR;
			vertex[EG] = emissiveG;
			vertex[EB] = emissiveB;
		}

		if (stroke) {
			vertex[SR] = strokeR;
			vertex[SG] = strokeG;
			vertex[SB] = strokeB;
			vertex[SA] = strokeA;
			vertex[SW] = strokeWeight;
		}

		if (textured) {
			vertex[U] = textureU;
			vertex[V] = textureV;
		}

		if (autoNormal) {
			float norm2 = normalX * normalX + normalY * normalY + normalZ * normalZ;
			if (norm2 < EPSILON) {
				vertex[HAS_NORMAL] = 0;
			} else {
				if (Math.abs(norm2 - 1) > EPSILON) {
					// The normal vector is not normalized.
					float norm = MathUtils.instance.sqrt(norm2);
					normalX /= norm;
					normalY /= norm;
					normalZ /= norm;
				}
				vertex[HAS_NORMAL] = 1;
			}
		} else {
			vertex[HAS_NORMAL] = 1;
		}

		vertex[NX] = normalX;
		vertex[NY] = normalY;
		vertex[NZ] = normalZ;

		vertex[BEEN_LIT] = 0;

		vertexCount++;
	}

	@Override
	public void vertex(int[] v) {
		vertexCheck();
		curveVertexCount = 0;
		float[] vertex = vertices[vertexCount];
		System.arraycopy(v, 0, vertex, 0, VERTEX_FIELD_COUNT);
		vertexCount++;
	}

	@Override
	public void vertex(float[] v) {
		vertexCheck();
		curveVertexCount = 0;
		float[] vertex = vertices[vertexCount];
		System.arraycopy(v, 0, vertex, 0, VERTEX_FIELD_COUNT);
		vertexCount++;
	}

	@Override
	public void vertex(float x, float y, float u, float v) {
		vertexTexture(u, v);
		vertex(x, y);
	}

	@Override
	public void vertex(float x, float y, float z, float u, float v) {
		vertexTexture(u, v);
		vertex(x, y, z);
	}

	protected void vertexTexture(float u, float v) {
		if (textureImage == null) {
			throw new RuntimeException("You must first call texture() before " +
					"using u and v coordinates with vertex()");
		}
		if (textureMode == IMAGE) {
			u /= textureImage.width;
			v /= textureImage.height;
		}

		textureU = u;
		textureV = v;

		if (textureU < 0) textureU = 0;
		else if (textureU > 1) textureU = 1;

		if (textureV < 0) textureV = 0;
		else if (textureV > 1) textureV = 1;
	}

	@Override
	public void endShape() { endShape(OPEN); }

	/**
	 * @param mode
	 *            OPEN or CLOSE
	 */
	@Override
	public void endShape(int mode) {}

	//////////////////////////////////////////////////////////////

	// CLIPPING

	/**
	 * ( begin auto-generated from clip.xml )
	 *
	 * Limits the rendering to the boundaries of a rectangle defined
	 * by the parameters. The boundaries are drawn based on the state
	 * of the <b>imageMode()</b> fuction, either CORNER, CORNERS, or CENTER.
	 *
	 * ( end auto-generated )
	 *
	 * @webref rendering
	 * @param a
	 *            x-coordinate of the rectangle, by default
	 * @param b
	 *            y-coordinate of the rectangle, by default
	 * @param c
	 *            width of the rectangle, by default
	 * @param d
	 *            height of the rectangle, by default
	 */
	public void clip(float a, float b, float c, float d) {
		if (imageMode == CORNER) {
			if (c < 0) { // reset a negative width
				a += c;
				c = -c;
			}
			if (d < 0) { // reset a negative height
				b += d;
				d = -d;
			}

			clipImpl(a, b, a + c, b + d);

		} else if (imageMode == CORNERS) {
			if (c < a) { // reverse because x2 < x1
				float temp = a;
				a = c;
				c = temp;
			}
			if (d < b) { // reverse because y2 < y1
				float temp = b;
				b = d;
				d = temp;
			}

			clipImpl(a, b, c, d);

		} else if (imageMode == CENTER) {
			// c and d are width/height
			if (c < 0) c = -c;
			if (d < 0) d = -d;
			float x1 = a - c / 2;
			float y1 = b - d / 2;

			clipImpl(x1, y1, x1 + c, y1 + d);
		}
	}

	protected void clipImpl(float x1, float y1, float x2, float y2) {
		showMissingWarning("clip");
	}

	/**
	 * ( begin auto-generated from noClip.xml )
	 *
	 * Disables the clipping previously started by the <b>clip()</b> function.
	 *
	 * ( end auto-generated )
	 *
	 * @webref rendering
	 */
	public void noClip() {
		showMissingWarning("noClip");
	}

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
		if (blendMode != BLEND) {
			showMissingWarning("blendMode");
		}
	}

	//////////////////////////////////////////////////////////////

	// CURVE/BEZIER VERTEX HANDLING

	protected void bezierVertexCheck() {
		bezierVertexCheck(shape, vertexCount);
	}

	protected void bezierVertexCheck(int shape, int vertexCount) {
		if (shape == 0 || shape != POLYGON) {
			throw new RuntimeException("beginShape() or beginShape(POLYGON) " +
					"must be used before bezierVertex() or quadraticVertex()");
		}
		if (vertexCount == 0) {
			throw new RuntimeException("vertex() must be used at least once " +
					"before bezierVertex() or quadraticVertex()");
		}
	}

	public void bezierVertex(float x2, float y2,
			float x3, float y3,
			float x4, float y4) {
		bezierInitCheck();
		bezierVertexCheck();
		SMatrix3D draw = bezierDrawMatrix;

		float[] prev = vertices[vertexCount - 1];
		float x1 = prev[X];
		float y1 = prev[Y];

		float xplot1 = draw.m10 * x1 + draw.m11 * x2 + draw.m12 * x3 + draw.m13 * x4;
		float xplot2 = draw.m20 * x1 + draw.m21 * x2 + draw.m22 * x3 + draw.m23 * x4;
		float xplot3 = draw.m30 * x1 + draw.m31 * x2 + draw.m32 * x3 + draw.m33 * x4;

		float yplot1 = draw.m10 * y1 + draw.m11 * y2 + draw.m12 * y3 + draw.m13 * y4;
		float yplot2 = draw.m20 * y1 + draw.m21 * y2 + draw.m22 * y3 + draw.m23 * y4;
		float yplot3 = draw.m30 * y1 + draw.m31 * y2 + draw.m32 * y3 + draw.m33 * y4;

		for (int j = 0; j < bezierDetail; j++) {
			x1 += xplot1;
			xplot1 += xplot2;
			xplot2 += xplot3;
			y1 += yplot1;
			yplot1 += yplot2;
			yplot2 += yplot3;
			vertex(x1, y1);
		}
	}

	/**
	 * ( begin auto-generated from bezierVertex.xml )
	 *
	 * Specifies vertex coordinates for Bezier curves. Each call to
	 * <b>bezierVertex()</b> defines the position of two control points and one
	 * anchor point of a Bezier curve, adding a new segment to a line or shape.
	 * The first time <b>bezierVertex()</b> is used within a
	 * <b>beginShape()</b> call, it must be prefaced with a call to
	 * <b>vertex()</b> to set the first anchor point. This function must be
	 * used between <b>beginShape()</b> and <b>endShape()</b> and only when
	 * there is no MODE parameter specified to <b>beginShape()</b>. Using the
	 * 3D version requires rendering with P3D (see the Environment reference
	 * for more information).
	 *
	 * ( end auto-generated )
	 * 
	 * @webref shape:vertex
	 * @param x2
	 *            the x-coordinate of the 1st control point
	 * @param y2
	 *            the y-coordinate of the 1st control point
	 * @param z2
	 *            the z-coordinate of the 1st control point
	 * @param x3
	 *            the x-coordinate of the 2nd control point
	 * @param y3
	 *            the y-coordinate of the 2nd control point
	 * @param z3
	 *            the z-coordinate of the 2nd control point
	 * @param x4
	 *            the x-coordinate of the anchor point
	 * @param y4
	 *            the y-coordinate of the anchor point
	 * @param z4
	 *            the z-coordinate of the anchor point
	 * @see PGraphics#curveVertex(float, float, float)
	 * @see PGraphics#vertex(float, float, float, float, float)
	 * @see PGraphics#quadraticVertex(float, float, float, float, float, float)
	 * @see PGraphics#bezier(float, float, float, float, float, float, float, float, float, float, float, float)
	 */
	public void bezierVertex(float x2, float y2, float z2,
			float x3, float y3, float z3,
			float x4, float y4, float z4) {
		bezierInitCheck();
		bezierVertexCheck();
		SMatrix3D draw = bezierDrawMatrix;

		float[] prev = vertices[vertexCount - 1];
		float x1 = prev[X];
		float y1 = prev[Y];
		float z1 = prev[Z];

		float xplot1 = draw.m10 * x1 + draw.m11 * x2 + draw.m12 * x3 + draw.m13 * x4;
		float xplot2 = draw.m20 * x1 + draw.m21 * x2 + draw.m22 * x3 + draw.m23 * x4;
		float xplot3 = draw.m30 * x1 + draw.m31 * x2 + draw.m32 * x3 + draw.m33 * x4;

		float yplot1 = draw.m10 * y1 + draw.m11 * y2 + draw.m12 * y3 + draw.m13 * y4;
		float yplot2 = draw.m20 * y1 + draw.m21 * y2 + draw.m22 * y3 + draw.m23 * y4;
		float yplot3 = draw.m30 * y1 + draw.m31 * y2 + draw.m32 * y3 + draw.m33 * y4;

		float zplot1 = draw.m10 * z1 + draw.m11 * z2 + draw.m12 * z3 + draw.m13 * z4;
		float zplot2 = draw.m20 * z1 + draw.m21 * z2 + draw.m22 * z3 + draw.m23 * z4;
		float zplot3 = draw.m30 * z1 + draw.m31 * z2 + draw.m32 * z3 + draw.m33 * z4;

		for (int j = 0; j < bezierDetail; j++) {
			x1 += xplot1;
			xplot1 += xplot2;
			xplot2 += xplot3;
			y1 += yplot1;
			yplot1 += yplot2;
			yplot2 += yplot3;
			z1 += zplot1;
			zplot1 += zplot2;
			zplot2 += zplot3;
			vertex(x1, y1, z1);
		}
	}

	/**
	 * @webref shape:vertex
	 * @param cx
	 *            the x-coordinate of the control point
	 * @param cy
	 *            the y-coordinate of the control point
	 * @param x3
	 *            the x-coordinate of the anchor point
	 * @param y3
	 *            the y-coordinate of the anchor point
	 * @see PGraphics#curveVertex(float, float, float)
	 * @see PGraphics#vertex(float, float, float, float, float)
	 * @see PGraphics#bezierVertex(float, float, float, float, float, float)
	 * @see PGraphics#bezier(float, float, float, float, float, float, float, float, float, float, float, float)
	 */
	public void quadraticVertex(float cx, float cy,
			float x3, float y3) {
		float[] prev = vertices[vertexCount - 1];
		float x1 = prev[X];
		float y1 = prev[Y];

		bezierVertex(x1 + ((cx - x1) * 2 / 3.0f), y1 + ((cy - y1) * 2 / 3.0f),
				x3 + ((cx - x3) * 2 / 3.0f), y3 + ((cy - y3) * 2 / 3.0f),
				x3, y3);
	}

	/**
	 * @param cz
	 *            the z-coordinate of the control point
	 * @param z3
	 *            the z-coordinate of the anchor point
	 */
	public void quadraticVertex(float cx, float cy, float cz,
			float x3, float y3, float z3) {
		float[] prev = vertices[vertexCount - 1];
		float x1 = prev[X];
		float y1 = prev[Y];
		float z1 = prev[Z];

		bezierVertex(x1 + ((cx - x1) * 2 / 3.0f), y1 + ((cy - y1) * 2 / 3.0f), z1 + ((cz - z1) * 2 / 3.0f),
				x3 + ((cx - x3) * 2 / 3.0f), y3 + ((cy - y3) * 2 / 3.0f), z3 + ((cz - z3) * 2 / 3.0f),
				x3, y3, z3);
	}

	protected void curveVertexCheck() {
		curveVertexCheck(shape);
	}

	/**
	 * Perform initialization specific to curveVertex(), and handle standard
	 * error modes. Can be overridden by subclasses that need the flexibility.
	 */
	protected void curveVertexCheck(int shape) {
		if (shape != POLYGON) {
			throw new RuntimeException("You must use beginShape() or " +
					"beginShape(POLYGON) before curveVertex()");
		}
		// to improve code init time, allocate on first use.
		if (curveVertices == null) {
			curveVertices = new float[128][3];
		}

		if (curveVertexCount == curveVertices.length) {
			// Can't use PApplet.expand() cuz it doesn't do the copy properly
			float[][] temp = new float[curveVertexCount << 1][3];
			System.arraycopy(curveVertices, 0, temp, 0, curveVertexCount);
			curveVertices = temp;
		}
		curveInitCheck();
	}

	/**
	 * ( begin auto-generated from curveVertex.xml )
	 *
	 * Specifies vertex coordinates for curves. This function may only be used
	 * between <b>beginShape()</b> and <b>endShape()</b> and only when there is
	 * no MODE parameter specified to <b>beginShape()</b>. The first and last
	 * points in a series of <b>curveVertex()</b> lines will be used to guide
	 * the beginning and end of a the curve. A minimum of four points is
	 * required to draw a tiny curve between the second and third points.
	 * Adding a fifth point with <b>curveVertex()</b> will draw the curve
	 * between the second, third, and fourth points. The <b>curveVertex()</b>
	 * function is an implementation of Catmull-Rom splines. Using the 3D
	 * version requires rendering with P3D (see the Environment reference for
	 * more information).
	 *
	 * ( end auto-generated )
	 *
	 * @webref shape:vertex
	 * @param x
	 *            the x-coordinate of the vertex
	 * @param y
	 *            the y-coordinate of the vertex
	 * @see PGraphics#curve(float, float, float, float, float, float, float, float, float, float, float, float)
	 * @see PGraphics#beginShape(int)
	 * @see PGraphics#endShape(int)
	 * @see PGraphics#vertex(float, float, float, float, float)
	 * @see PGraphics#bezier(float, float, float, float, float, float, float, float, float, float, float, float)
	 * @see PGraphics#quadraticVertex(float, float, float, float, float, float)
	 */
	public void curveVertex(float x, float y) {
		curveVertexCheck();
		float[] v = curveVertices[curveVertexCount];
		v[X] = x;
		v[Y] = y;
		curveVertexCount++;

		// draw a segment if there are enough points
		if (curveVertexCount > 3) {
			curveVertexSegment(curveVertices[curveVertexCount - 4][X],
					curveVertices[curveVertexCount - 4][Y],
					curveVertices[curveVertexCount - 3][X],
					curveVertices[curveVertexCount - 3][Y],
					curveVertices[curveVertexCount - 2][X],
					curveVertices[curveVertexCount - 2][Y],
					curveVertices[curveVertexCount - 1][X],
					curveVertices[curveVertexCount - 1][Y]);
		}
	}

	/**
	 * @param z
	 *            the z-coordinate of the vertex
	 */
	public void curveVertex(float x, float y, float z) {
		curveVertexCheck();
		float[] v = curveVertices[curveVertexCount];
		v[X] = x;
		v[Y] = y;
		v[Z] = z;
		curveVertexCount++;

		// draw a segment if there are enough points
		if (curveVertexCount > 3) {
			curveVertexSegment(curveVertices[curveVertexCount - 4][X],
					curveVertices[curveVertexCount - 4][Y],
					curveVertices[curveVertexCount - 4][Z],
					curveVertices[curveVertexCount - 3][X],
					curveVertices[curveVertexCount - 3][Y],
					curveVertices[curveVertexCount - 3][Z],
					curveVertices[curveVertexCount - 2][X],
					curveVertices[curveVertexCount - 2][Y],
					curveVertices[curveVertexCount - 2][Z],
					curveVertices[curveVertexCount - 1][X],
					curveVertices[curveVertexCount - 1][Y],
					curveVertices[curveVertexCount - 1][Z]);
		}
	}

	/**
	 * Handle emitting a specific segment of Catmull-Rom curve. This can be
	 * overridden by subclasses that need more efficient rendering options.
	 */
	protected void curveVertexSegment(float x1, float y1,
			float x2, float y2,
			float x3, float y3,
			float x4, float y4) {
		float x0 = x2;
		float y0 = y2;

		SMatrix3D draw = curveDrawMatrix;

		float xplot1 = draw.m10 * x1 + draw.m11 * x2 + draw.m12 * x3 + draw.m13 * x4;
		float xplot2 = draw.m20 * x1 + draw.m21 * x2 + draw.m22 * x3 + draw.m23 * x4;
		float xplot3 = draw.m30 * x1 + draw.m31 * x2 + draw.m32 * x3 + draw.m33 * x4;

		float yplot1 = draw.m10 * y1 + draw.m11 * y2 + draw.m12 * y3 + draw.m13 * y4;
		float yplot2 = draw.m20 * y1 + draw.m21 * y2 + draw.m22 * y3 + draw.m23 * y4;
		float yplot3 = draw.m30 * y1 + draw.m31 * y2 + draw.m32 * y3 + draw.m33 * y4;

		// vertex() will reset splineVertexCount, so save it
		int savedCount = curveVertexCount;

		vertex(x0, y0);
		for (int j = 0; j < curveDetail; j++) {
			x0 += xplot1;
			xplot1 += xplot2;
			xplot2 += xplot3;
			y0 += yplot1;
			yplot1 += yplot2;
			yplot2 += yplot3;
			vertex(x0, y0);
		}
		curveVertexCount = savedCount;
	}

	/**
	 * Handle emitting a specific segment of Catmull-Rom curve. This can be
	 * overridden by subclasses that need more efficient rendering options.
	 */
	protected void curveVertexSegment(float x1, float y1, float z1,
			float x2, float y2, float z2,
			float x3, float y3, float z3,
			float x4, float y4, float z4) {
		float x0 = x2;
		float y0 = y2;
		float z0 = z2;

		SMatrix3D draw = curveDrawMatrix;

		float xplot1 = draw.m10 * x1 + draw.m11 * x2 + draw.m12 * x3 + draw.m13 * x4;
		float xplot2 = draw.m20 * x1 + draw.m21 * x2 + draw.m22 * x3 + draw.m23 * x4;
		float xplot3 = draw.m30 * x1 + draw.m31 * x2 + draw.m32 * x3 + draw.m33 * x4;

		float yplot1 = draw.m10 * y1 + draw.m11 * y2 + draw.m12 * y3 + draw.m13 * y4;
		float yplot2 = draw.m20 * y1 + draw.m21 * y2 + draw.m22 * y3 + draw.m23 * y4;
		float yplot3 = draw.m30 * y1 + draw.m31 * y2 + draw.m32 * y3 + draw.m33 * y4;

		// vertex() will reset splineVertexCount, so save it
		int savedCount = curveVertexCount;

		float zplot1 = draw.m10 * z1 + draw.m11 * z2 + draw.m12 * z3 + draw.m13 * z4;
		float zplot2 = draw.m20 * z1 + draw.m21 * z2 + draw.m22 * z3 + draw.m23 * z4;
		float zplot3 = draw.m30 * z1 + draw.m31 * z2 + draw.m32 * z3 + draw.m33 * z4;

		vertex(x0, y0, z0);
		for (int j = 0; j < curveDetail; j++) {
			x0 += xplot1;
			xplot1 += xplot2;
			xplot2 += xplot3;
			y0 += yplot1;
			yplot1 += yplot2;
			yplot2 += yplot3;
			z0 += zplot1;
			zplot1 += zplot2;
			zplot2 += zplot3;
			vertex(x0, y0, z0);
		}
		curveVertexCount = savedCount;
	}

	//////////////////////////////////////////////////////////////

	// SIMPLE SHAPES WITH ANALOGUES IN beginShape()

	/**
	 * ( begin auto-generated from point.xml )
	 *
	 * Draws a point, a coordinate in space at the dimension of one pixel. The
	 * first parameter is the horizontal value for the point, the second value
	 * is the vertical value for the point, and the optional third value is the
	 * depth value. Drawing this shape in 3D with the <b>z</b> parameter
	 * requires the P3D parameter in combination with <b>size()</b> as shown in
	 * the above example.
	 *
	 * ( end auto-generated )
	 *
	 * @webref shape:2d_primitives
	 * @param x
	 *            x-coordinate of the point
	 * @param y
	 *            y-coordinate of the point
	 * @see PGraphics#stroke(int)
	 */
	@Override
	public void point(float x, float y) {
		beginShape(POINTS);
		vertex(x, y);
		endShape();
	}

	/**
	 * @param z
	 *            z-coordinate of the point
	 */
	public void point(float x, float y, float z) {
		beginShape(POINTS);
		vertex(x, y, z);
		endShape();
	}

	/**
	 * ( begin auto-generated from line.xml )
	 *
	 * Draws a line (a direct path between two points) to the screen. The
	 * version of <b>line()</b> with four parameters draws the line in 2D. To
	 * color a line, use the <b>stroke()</b> function. A line cannot be filled,
	 * therefore the <b>fill()</b> function will not affect the color of a
	 * line. 2D lines are drawn with a width of one pixel by default, but this
	 * can be changed with the <b>strokeWeight()</b> function. The version with
	 * six parameters allows the line to be placed anywhere within XYZ space.
	 * Drawing this shape in 3D with the <b>z</b> parameter requires the P3D
	 * parameter in combination with <b>size()</b> as shown in the above example.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref shape:2d_primitives
	 * @param x1
	 *            x-coordinate of the first point
	 * @param y1
	 *            y-coordinate of the first point
	 * @param x2
	 *            x-coordinate of the second point
	 * @param y2
	 *            y-coordinate of the second point
	 * @see PGraphics#strokeWeight(float)
	 * @see PGraphics#strokeJoin(int)
	 * @see PGraphics#strokeCap(int)
	 * @see PGraphics#beginShape()
	 */
	@Override
	public void line(float x1, float y1, float x2, float y2) {
		beginShape(LINES);
		vertex(x1, y1);
		vertex(x2, y2);
		endShape();
	}

	/**
	 * @param z1
	 *            z-coordinate of the first point
	 * @param z2
	 *            z-coordinate of the second point
	 */
	public void line(float x1, float y1, float z1,
			float x2, float y2, float z2) {
		beginShape(LINES);
		vertex(x1, y1, z1);
		vertex(x2, y2, z2);
		endShape();
	}

	/**
	 * ( begin auto-generated from triangle.xml )
	 *
	 * A triangle is a plane created by connecting three points. The first two
	 * arguments specify the first point, the middle two arguments specify the
	 * second point, and the last two arguments specify the third point.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref shape:2d_primitives
	 * @param x1
	 *            x-coordinate of the first point
	 * @param y1
	 *            y-coordinate of the first point
	 * @param x2
	 *            x-coordinate of the second point
	 * @param y2
	 *            y-coordinate of the second point
	 * @param x3
	 *            x-coordinate of the third point
	 * @param y3
	 *            y-coordinate of the third point
	 * @see PApplet#beginShape()
	 */
	@Override
	public void triangle(float x1, float y1, float x2, float y2,
			float x3, float y3) {
		beginShape(TRIANGLES);
		vertex(x1, y1);
		vertex(x2, y2);
		vertex(x3, y3);
		endShape();
	}

	/**
	 * ( begin auto-generated from quad.xml )
	 *
	 * A quad is a quadrilateral, a four sided polygon. It is similar to a
	 * rectangle, but the angles between its edges are not constrained to
	 * ninety degrees. The first pair of parameters (x1,y1) sets the first
	 * vertex and the subsequent pairs should proceed clockwise or
	 * counter-clockwise around the defined shape.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref shape:2d_primitives
	 * @param x1
	 *            x-coordinate of the first corner
	 * @param y1
	 *            y-coordinate of the first corner
	 * @param x2
	 *            x-coordinate of the second corner
	 * @param y2
	 *            y-coordinate of the second corner
	 * @param x3
	 *            x-coordinate of the third corner
	 * @param y3
	 *            y-coordinate of the third corner
	 * @param x4
	 *            x-coordinate of the fourth corner
	 * @param y4
	 *            y-coordinate of the fourth corner
	 */
	@Override
	public void quad(float x1, float y1, float x2, float y2,
			float x3, float y3, float x4, float y4) {
		beginShape(QUADS);
		vertex(x1, y1);
		vertex(x2, y2);
		vertex(x3, y3);
		vertex(x4, y4);
		endShape();
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
		quad(x1, y1, x2, y1, x2, y2, x1, y2);
	}

	public void rect(float a, float b, float c, float d, float r) {
		rect(a, b, c, d, r, r, r, r);
	}

	/**
	 * @param tl
	 *            radius for top-left corner
	 * @param tr
	 *            radius for top-right corner
	 * @param br
	 *            radius for bottom-right corner
	 * @param bl
	 *            radius for bottom-left corner
	 */
	public void rect(float a, float b, float c, float d,
			float tl, float tr, float br, float bl) {
		float hradius, vradius;
		switch (rectMode) {
			case CORNERS:
				break;
			case CORNER:
				c += a;
				d += b;
				break;
			case RADIUS:
				hradius = c;
				vradius = d;
				c = a + hradius;
				d = b + vradius;
				a -= hradius;
				b -= vradius;
				break;
			case CENTER:
				hradius = c / 2.0f;
				vradius = d / 2.0f;
				c = a + hradius;
				d = b + vradius;
				a -= hradius;
				b -= vradius;
		}

		if (a > c) {
			float temp = a;
			a = c;
			c = temp;
		}

		if (b > d) {
			float temp = b;
			b = d;
			d = temp;
		}

		float maxRounding = MathUtils.instance.min((c - a) / 2, (d - b) / 2);
		if (tl > maxRounding) tl = maxRounding;
		if (tr > maxRounding) tr = maxRounding;
		if (br > maxRounding) br = maxRounding;
		if (bl > maxRounding) bl = maxRounding;

		rectImpl(a, b, c, d, tl, tr, br, bl);
	}

	protected void rectImpl(float x1, float y1, float x2, float y2,
			float tl, float tr, float br, float bl) {
		beginShape();
//		    vertex(x1+tl, y1);
		if (tr != 0) {
			vertex(x2 - tr, y1);
			quadraticVertex(x2, y1, x2, y1 + tr);
		} else {
			vertex(x2, y1);
		}
		if (br != 0) {
			vertex(x2, y2 - br);
			quadraticVertex(x2, y2, x2 - br, y2);
		} else {
			vertex(x2, y2);
		}
		if (bl != 0) {
			vertex(x1 + bl, y2);
			quadraticVertex(x1, y2, x1, y2 - bl);
		} else {
			vertex(x1, y2);
		}
		if (tl != 0) {
			vertex(x1, y1 + tl);
			quadraticVertex(x1, y1, x1 + tl, y1);
		} else {
			vertex(x1, y1);
		}
//		    endShape();
		endShape(CLOSE);
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
		showMissingWarning("ellipseImpl");
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
		showMissingWarning("arc");
	}

	//////////////////////////////////////////////////////////////

	// BOX

	/**
	 * ( begin auto-generated from box.xml )
	 *
	 * A box is an extruded rectangle. A box with equal dimension on all sides
	 * is a cube.
	 *
	 * ( end auto-generated )
	 *
	 * @webref shape:3d_primitives
	 * @param size
	 *            dimension of the box in all dimensions (creates a cube)
	 * @see PGraphics#sphere(float)
	 */
	public void box(float size) {
		box(size, size, size);
	}

	/**
	 * @param w
	 *            dimension of the box in the x-dimension
	 * @param h
	 *            dimension of the box in the y-dimension
	 * @param d
	 *            dimension of the box in the z-dimension
	 */
	public void box(float w, float h, float d) {
		float x1 = -w / 2f;
		float x2 = w / 2f;
		float y1 = -h / 2f;
		float y2 = h / 2f;
		float z1 = -d / 2f;
		float z2 = d / 2f;

		// TODO not the least bit efficient, it even redraws lines
		// along the vertices. ugly ugly ugly!

		beginShape(QUADS);

		// front
		normal(0, 0, 1);
		vertex(x1, y1, z1);
		vertex(x2, y1, z1);
		vertex(x2, y2, z1);
		vertex(x1, y2, z1);

		// right
		normal(1, 0, 0);
		vertex(x2, y1, z1);
		vertex(x2, y1, z2);
		vertex(x2, y2, z2);
		vertex(x2, y2, z1);

		// back
		normal(0, 0, -1);
		vertex(x2, y1, z2);
		vertex(x1, y1, z2);
		vertex(x1, y2, z2);
		vertex(x2, y2, z2);

		// left
		normal(-1, 0, 0);
		vertex(x1, y1, z2);
		vertex(x1, y1, z1);
		vertex(x1, y2, z1);
		vertex(x1, y2, z2);

		// top
		normal(0, 1, 0);
		vertex(x1, y1, z2);
		vertex(x2, y1, z2);
		vertex(x2, y1, z1);
		vertex(x1, y1, z1);

		// bottom
		normal(0, -1, 0);
		vertex(x1, y2, z1);
		vertex(x2, y2, z1);
		vertex(x2, y2, z2);
		vertex(x1, y2, z2);

		endShape();
	}

	//////////////////////////////////////////////////////////////

	// SPHERE

	/**
	 * ( begin auto-generated from sphereDetail.xml )
	 *
	 * Controls the detail used to render a sphere by adjusting the number of
	 * vertices of the sphere mesh. The default resolution is 30, which creates
	 * a fairly detailed sphere definition with vertices every 360/30 = 12
	 * degrees. If you're going to render a great number of spheres per frame,
	 * it is advised to reduce the level of detail using this function. The
	 * setting stays active until <b>sphereDetail()</b> is called again with a
	 * new parameter and so should <i>not</i> be called prior to every
	 * <b>sphere()</b> statement, unless you wish to render spheres with
	 * different settings, e.g. using less detail for smaller spheres or ones
	 * further away from the camera. To control the detail of the horizontal
	 * and vertical resolution independently, use the version of the functions
	 * with two parameters.
	 *
	 * ( end auto-generated )
	 *
	 * <h3>Advanced</h3>
	 * Code for sphereDetail() submitted by toxi [031031].
	 * Code for enhanced u/v version from davbol [080801].
	 *
	 * @param res
	 *            number of segments (minimum 3) used per full circle revolution
	 * @webref shape:3d_primitives
	 * @see PGraphics#sphere(float)
	 */
	public void sphereDetail(int res) {
		sphereDetail(res, res);
	}

	/**
	 * @param ures
	 *            number of segments used longitudinally per full circle revolutoin
	 * @param vres
	 *            number of segments used latitudinally from top to bottom
	 */
	public void sphereDetail(int ures, int vres) {
		if (ures < 3) ures = 3; // force a minimum res
		if (vres < 2) vres = 2; // force a minimum res
		if ((ures == sphereDetailU) && (vres == sphereDetailV)) return;

		float delta = (float) SINCOS_LENGTH / ures;
		float[] cx = new float[ures];
		float[] cz = new float[ures];
		// calc unit circle in XZ plane
		for (int i = 0; i < ures; i++) {
			cx[i] = cosLUT[(int) (i * delta) % SINCOS_LENGTH];
			cz[i] = sinLUT[(int) (i * delta) % SINCOS_LENGTH];
		}
		// computing vertexlist
		// vertexlist starts at south pole
		int vertCount = ures * (vres - 1) + 2;
		int currVert = 0;

		// re-init arrays to store vertices
		sphereX = new float[vertCount];
		sphereY = new float[vertCount];
		sphereZ = new float[vertCount];

		float angle_step = (SINCOS_LENGTH * 0.5f) / vres;
		float angle = angle_step;

		// step along Y axis
		for (int i = 1; i < vres; i++) {
			float curradius = sinLUT[(int) angle % SINCOS_LENGTH];
			float currY = cosLUT[(int) angle % SINCOS_LENGTH];
			for (int j = 0; j < ures; j++) {
				sphereX[currVert] = cx[j] * curradius;
				sphereY[currVert] = currY;
				sphereZ[currVert++] = cz[j] * curradius;
			}
			angle += angle_step;
		}
		sphereDetailU = ures;
		sphereDetailV = vres;
	}

	/**
	 * ( begin auto-generated from sphere.xml )
	 *
	 * A sphere is a hollow ball made from tessellated triangles.
	 *
	 * ( end auto-generated )
	 *
	 * <h3>Advanced</h3>
	 * <P>
	 * Implementation notes:
	 * <P>
	 * cache all the points of the sphere in a static array
	 * top and bottom are just a bunch of triangles that land
	 * in the center point
	 * <P>
	 * sphere is a series of concentric circles who radii vary
	 * along the shape, based on, er.. cos or something
	 * 
	 * <PRE>
	 * [toxi 031031] new sphere code. removed all multiplies with
	 * radius, as scale() will take care of that anyway
	 *
	 * [toxi 031223] updated sphere code (removed modulos)
	 * and introduced sphereAt(x,y,z,r)
	 * to avoid additional translate()'s on the user/sketch side
	 *
	 * [davbol 080801] now using separate sphereDetailU/V
	 * </PRE>
	 *
	 * @webref shape:3d_primitives
	 * @param r
	 *            the radius of the sphere
	 * @see PGraphics#sphereDetail(int)
	 */
	public void sphere(float r) {
		if ((sphereDetailU < 3) || (sphereDetailV < 2)) {
			sphereDetail(30);
		}

		edge(false);

		// 1st ring from south pole
		beginShape(TRIANGLE_STRIP);
		for (int i = 0; i < sphereDetailU; i++) {
			normal(0, -1, 0);
			vertex(0, -r, 0);
			normal(sphereX[i], sphereY[i], sphereZ[i]);
			vertex(r * sphereX[i], r * sphereY[i], r * sphereZ[i]);
		}
		normal(0, -r, 0);
		vertex(0, -r, 0);
		normal(sphereX[0], sphereY[0], sphereZ[0]);
		vertex(r * sphereX[0], r * sphereY[0], r * sphereZ[0]);
		endShape();

		int v1, v11, v2;

		// middle rings
		int voff = 0;
		for (int i = 2; i < sphereDetailV; i++) {
			v1 = v11 = voff;
			voff += sphereDetailU;
			v2 = voff;
			beginShape(TRIANGLE_STRIP);
			for (int j = 0; j < sphereDetailU; j++) {
				normal(sphereX[v1], sphereY[v1], sphereZ[v1]);
				vertex(r * sphereX[v1], r * sphereY[v1], r * sphereZ[v1++]);
				normal(sphereX[v2], sphereY[v2], sphereZ[v2]);
				vertex(r * sphereX[v2], r * sphereY[v2], r * sphereZ[v2++]);
			}
			// close each ring
			v1 = v11;
			v2 = voff;
			normal(sphereX[v1], sphereY[v1], sphereZ[v1]);
			vertex(r * sphereX[v1], r * sphereY[v1], r * sphereZ[v1]);
			normal(sphereX[v2], sphereY[v2], sphereZ[v2]);
			vertex(r * sphereX[v2], r * sphereY[v2], r * sphereZ[v2]);
			endShape();
		}

		// add the northern cap
		beginShape(TRIANGLE_STRIP);
		for (int i = 0; i < sphereDetailU; i++) {
			v2 = voff + i;
			normal(sphereX[v2], sphereY[v2], sphereZ[v2]);
			vertex(r * sphereX[v2], r * sphereY[v2], r * sphereZ[v2]);
			normal(0, 1, 0);
			vertex(0, r, 0);
		}
		normal(sphereX[voff], sphereY[voff], sphereZ[voff]);
		vertex(r * sphereX[voff], r * sphereY[voff], r * sphereZ[voff]);
		normal(0, 1, 0);
		vertex(0, r, 0);
		endShape();

		edge(true);
	}

	//////////////////////////////////////////////////////////////

	// BEZIER

	/**
	 * ( begin auto-generated from bezierPoint.xml )
	 *
	 * Evaluates the Bezier at point t for points a, b, c, d. The parameter t
	 * varies between 0 and 1, a and d are points on the curve, and b and c are
	 * the control points. This can be done once with the x coordinates and a
	 * second time with the y coordinates to get the location of a bezier curve
	 * at t.
	 *
	 * ( end auto-generated )
	 *
	 * <h3>Advanced</h3>
	 * For instance, to convert the following example:
	 * 
	 * <PRE>
	 * stroke(255, 102, 0);
	 * line(85, 20, 10, 10);
	 * line(90, 90, 15, 80);
	 * stroke(0, 0, 0);
	 * bezier(85, 20, 10, 10, 90, 90, 15, 80);
	 *
	 * // draw it in gray, using 10 steps instead of the default 20
	 * // this is a slower way to do it, but useful if you need
	 * // to do things with the coordinates at each step
	 * stroke(128);
	 * beginShape(LINE_STRIP);
	 * for (int i = 0; i <= 10; i++) {
	 * 	float t = i / 10.0f;
	 * 	float x = bezierPoint(85, 10, 90, 15, t);
	 * 	float y = bezierPoint(20, 10, 90, 80, t);
	 * 	vertex(x, y);
	 * }
	 * endShape();
	 * </PRE>
	 *
	 * @webref shape:curves
	 * @param a
	 *            coordinate of first point on the curve
	 * @param b
	 *            coordinate of first control point
	 * @param c
	 *            coordinate of second control point
	 * @param d
	 *            coordinate of second point on the curve
	 * @param t
	 *            value between 0 and 1
	 * @see PGraphics#bezier(float, float, float, float, float, float, float, float, float, float, float, float)
	 * @see PGraphics#bezierVertex(float, float, float, float, float, float)
	 * @see PGraphics#curvePoint(float, float, float, float, float)
	 */
	public float bezierPoint(float a, float b, float c, float d, float t) {
		float t1 = 1.0f - t;
		return (a * t1 + 3 * b * t) * t1 * t1 + (3 * c * t1 + d * t) * t * t;
	}

	/**
	 * ( begin auto-generated from bezierTangent.xml )
	 *
	 * Calculates the tangent of a point on a Bezier curve. There is a good
	 * definition of <a href="http://en.wikipedia.org/wiki/Tangent"
	 * target="new"><em>tangent</em> on Wikipedia</a>.
	 *
	 * ( end auto-generated )
	 *
	 * <h3>Advanced</h3>
	 * Code submitted by Dave Bollinger (davol) for release 0136.
	 *
	 * @webref shape:curves
	 * @param a
	 *            coordinate of first point on the curve
	 * @param b
	 *            coordinate of first control point
	 * @param c
	 *            coordinate of second control point
	 * @param d
	 *            coordinate of second point on the curve
	 * @param t
	 *            value between 0 and 1
	 * @see PGraphics#bezier(float, float, float, float, float, float, float, float, float, float, float, float)
	 * @see PGraphics#bezierVertex(float, float, float, float, float, float)
	 * @see PGraphics#curvePoint(float, float, float, float, float)
	 */
	public float bezierTangent(float a, float b, float c, float d, float t) {
		return (3 * t * t * (-a + 3 * b - 3 * c + d) +
				6 * t * (a - 2 * b + c) +
				3 * (-a + b));
	}

	protected void bezierInitCheck() {
		if (!bezierInited) {
			bezierInit();
		}
	}

	protected void bezierInit() {
		// overkill to be broken out, but better parity with the curve stuff below
		bezierDetail(bezierDetail);
		bezierInited = true;
	}

	/**
	 * ( begin auto-generated from bezierDetail.xml )
	 *
	 * Sets the resolution at which Beziers display. The default value is 20.
	 * This function is only useful when using the P3D renderer as the default
	 * P2D renderer does not use this information.
	 *
	 * ( end auto-generated )
	 *
	 * @webref shape:curves
	 * @param detail
	 *            resolution of the curves
	 * @see PGraphics#curve(float, float, float, float, float, float, float, float, float, float, float, float)
	 * @see PGraphics#curveVertex(float, float, float)
	 * @see PGraphics#curveTightness(float)
	 */
	public void bezierDetail(int detail) {
		bezierDetail = detail;

		if (bezierDrawMatrix == null) {
			bezierDrawMatrix = new SMatrix3D();
		}

		// setup matrix for forward differencing to speed up drawing
		splineForward(detail, bezierDrawMatrix);

		// multiply the basis and forward diff matrices together
		// saves much time since this needn't be done for each curve
		// mult_spline_matrix(bezierForwardMatrix, bezier_basis, bezierDrawMatrix, 4);
		// bezierDrawMatrix.set(bezierForwardMatrix);
		bezierDrawMatrix.apply(bezierBasisMatrix);
	}

	public void bezier(float x1, float y1,
			float x2, float y2,
			float x3, float y3,
			float x4, float y4) {
		beginShape();
		vertex(x1, y1);
		bezierVertex(x2, y2, x3, y3, x4, y4);
		endShape();
	}

	/**
	 * ( begin auto-generated from bezier.xml )
	 *
	 * Draws a Bezier curve on the screen. These curves are defined by a series
	 * of anchor and control points. The first two parameters specify the first
	 * anchor point and the last two parameters specify the other anchor point.
	 * The middle parameters specify the control points which define the shape
	 * of the curve. Bezier curves were developed by French engineer Pierre
	 * Bezier. Using the 3D version requires rendering with P3D (see the
	 * Environment reference for more information).
	 *
	 * ( end auto-generated )
	 *
	 * <h3>Advanced</h3>
	 * Draw a cubic bezier curve. The first and last points are
	 * the on-curve points. The middle two are the 'control' points,
	 * or 'handles' in an application like Illustrator.
	 * <P>
	 * Identical to typing:
	 * 
	 * <PRE>
	 * beginShape();
	 * vertex(x1, y1);
	 * bezierVertex(x2, y2, x3, y3, x4, y4);
	 * endShape();
	 * </PRE>
	 * 
	 * In Postscript-speak, this would be:
	 * 
	 * <PRE>
	 * moveto(x1, y1);
	 * curveto(x2, y2, x3, y3, x4, y4);
	 * </PRE>
	 * 
	 * If you were to try and continue that curve like so:
	 * 
	 * <PRE>
	 * curveto(x5, y5, x6, y6, x7, y7);
	 * </PRE>
	 * 
	 * This would be done in processing by adding these statements:
	 * 
	 * <PRE>
	 * bezierVertex(x5, y5, x6, y6, x7, y7)
	 * </PRE>
	 * 
	 * To draw a quadratic (instead of cubic) curve,
	 * use the control point twice by doubling it:
	 * 
	 * <PRE>
	 * bezier(x1, y1, cx, cy, cx, cy, x2, y2);
	 * </PRE>
	 *
	 * @webref shape:curves
	 * @param x1
	 *            coordinates for the first anchor point
	 * @param y1
	 *            coordinates for the first anchor point
	 * @param z1
	 *            coordinates for the first anchor point
	 * @param x2
	 *            coordinates for the first control point
	 * @param y2
	 *            coordinates for the first control point
	 * @param z2
	 *            coordinates for the first control point
	 * @param x3
	 *            coordinates for the second control point
	 * @param y3
	 *            coordinates for the second control point
	 * @param z3
	 *            coordinates for the second control point
	 * @param x4
	 *            coordinates for the second anchor point
	 * @param y4
	 *            coordinates for the second anchor point
	 * @param z4
	 *            coordinates for the second anchor point
	 *
	 * @see PGraphics#bezierVertex(float, float, float, float, float, float)
	 * @see PGraphics#curve(float, float, float, float, float, float, float, float, float, float, float, float)
	 */
	public void bezier(float x1, float y1, float z1,
			float x2, float y2, float z2,
			float x3, float y3, float z3,
			float x4, float y4, float z4) {
		beginShape();
		vertex(x1, y1, z1);
		bezierVertex(x2, y2, z2,
				x3, y3, z3,
				x4, y4, z4);
		endShape();
	}

	//////////////////////////////////////////////////////////////

	// CATMULL-ROM CURVE

	/**
	 * ( begin auto-generated from curvePoint.xml )
	 *
	 * Evalutes the curve at point t for points a, b, c, d. The parameter t
	 * varies between 0 and 1, a and d are the control points, and b and c are
	 * the points on the curve. This can be done once with the x coordinates and a
	 * second time with the y coordinates to get the location of a curve at t.
	 *
	 * ( end auto-generated )
	 *
	 * @webref shape:curves
	 * @param a
	 *            coordinate of first control point
	 * @param b
	 *            coordinate of first point on the curve
	 * @param c
	 *            coordinate of second point on the curve
	 * @param d
	 *            coordinate of second control point
	 * @param t
	 *            value between 0 and 1
	 * @see PGraphics#curve(float, float, float, float, float, float, float, float, float, float, float, float)
	 * @see PGraphics#curveVertex(float, float)
	 * @see PGraphics#bezierPoint(float, float, float, float, float)
	 */
	public float curvePoint(float a, float b, float c, float d, float t) {
		curveInitCheck();

		float tt = t * t;
		float ttt = t * tt;
		SMatrix3D cb = curveBasisMatrix;

		// not optimized (and probably need not be)
		return (a * (ttt * cb.m00 + tt * cb.m10 + t * cb.m20 + cb.m30) +
				b * (ttt * cb.m01 + tt * cb.m11 + t * cb.m21 + cb.m31) +
				c * (ttt * cb.m02 + tt * cb.m12 + t * cb.m22 + cb.m32) +
				d * (ttt * cb.m03 + tt * cb.m13 + t * cb.m23 + cb.m33));
	}

	/**
	 * ( begin auto-generated from curveTangent.xml )
	 *
	 * Calculates the tangent of a point on a curve. There's a good definition
	 * of <em><a href="http://en.wikipedia.org/wiki/Tangent"
	 * target="new">tangent</em> on Wikipedia</a>.
	 *
	 * ( end auto-generated )
	 *
	 * <h3>Advanced</h3>
	 * Code thanks to Dave Bollinger (Bug #715)
	 *
	 * @webref shape:curves
	 * @param a
	 *            coordinate of first point on the curve
	 * @param b
	 *            coordinate of first control point
	 * @param c
	 *            coordinate of second control point
	 * @param d
	 *            coordinate of second point on the curve
	 * @param t
	 *            value between 0 and 1
	 * @see PGraphics#curve(float, float, float, float, float, float, float, float, float, float, float, float)
	 * @see PGraphics#curveVertex(float, float)
	 * @see PGraphics#curvePoint(float, float, float, float, float)
	 * @see PGraphics#bezierTangent(float, float, float, float, float)
	 */
	public float curveTangent(float a, float b, float c, float d, float t) {
		curveInitCheck();

		float tt3 = t * t * 3;
		float t2 = t * 2;
		SMatrix3D cb = curveBasisMatrix;

		// not optimized (and probably need not be)
		return (a * (tt3 * cb.m00 + t2 * cb.m10 + cb.m20) +
				b * (tt3 * cb.m01 + t2 * cb.m11 + cb.m21) +
				c * (tt3 * cb.m02 + t2 * cb.m12 + cb.m22) +
				d * (tt3 * cb.m03 + t2 * cb.m13 + cb.m23));
	}

	/**
	 * ( begin auto-generated from curveDetail.xml )
	 *
	 * Sets the resolution at which curves display. The default value is 20.
	 * This function is only useful when using the P3D renderer as the default
	 * P2D renderer does not use this information.
	 *
	 * ( end auto-generated )
	 *
	 * @webref shape:curves
	 * @param detail
	 *            resolution of the curves
	 * @see PGraphics#curve(float, float, float, float, float, float, float, float, float, float, float, float)
	 * @see PGraphics#curveVertex(float, float)
	 * @see PGraphics#curveTightness(float)
	 */
	public void curveDetail(int detail) {
		curveDetail = detail;
		curveInit();
	}

	/**
	 * ( begin auto-generated from curveTightness.xml )
	 *
	 * Modifies the quality of forms created with <b>curve()</b> and
	 * <b>curveVertex()</b>. The parameter <b>squishy</b> determines how the
	 * curve fits to the vertex points. The value 0.0 is the default value for
	 * <b>squishy</b> (this value defines the curves to be Catmull-Rom splines)
	 * and the value 1.0 connects all the points with straight lines. Values
	 * within the range -5.0 and 5.0 will deform the curves but will leave them
	 * recognizable and as values increase in magnitude, they will continue to deform.
	 *
	 * ( end auto-generated )
	 *
	 * @webref shape:curves
	 * @param tightness
	 *            amount of deformation from the original vertices
	 * @see PGraphics#curve(float, float, float, float, float, float, float, float, float, float, float, float)
	 * @see PGraphics#curveVertex(float, float)
	 */
	public void curveTightness(float tightness) {
		curveTightness = tightness;
		curveInit();
	}

	protected void curveInitCheck() {
		if (!curveInited) {
			curveInit();
		}
	}

	/**
	 * Set the number of segments to use when drawing a Catmull-Rom
	 * curve, and setting the s parameter, which defines how tightly
	 * the curve fits to each vertex. Catmull-Rom curves are actually
	 * a subset of this curve type where the s is set to zero.
	 * <P>
	 * (This function is not optimized, since it's not expected to
	 * be called all that often. there are many juicy and obvious
	 * opimizations in here, but it's probably better to keep the
	 * code more readable)
	 */
	protected void curveInit() {
		// allocate only if/when used to save startup time
		if (curveDrawMatrix == null) {
			curveBasisMatrix = new SMatrix3D();
			curveDrawMatrix = new SMatrix3D();
			curveInited = true;
		}

		float s = curveTightness;
		curveBasisMatrix.set((s - 1) / 2f, (s + 3) / 2f, (-3 - s) / 2f, (1 - s) / 2f,
				(1 - s), (-5 - s) / 2f, (s + 2), (s - 1) / 2f,
				(s - 1) / 2f, 0, (1 - s) / 2f, 0,
				0, 1, 0, 0);

		// setup_spline_forward(segments, curveForwardMatrix);
		splineForward(curveDetail, curveDrawMatrix);

		if (bezierBasisInverse == null) {
			bezierBasisInverse = bezierBasisMatrix.get();
			bezierBasisInverse.invert();
			curveToBezierMatrix = new SMatrix3D();
		}

		// TODO only needed for PGraphicsJava2D? if so, move it there
		// actually, it's generally useful for other renderers, so keep it
		// or hide the implementation elsewhere.
		curveToBezierMatrix.set(curveBasisMatrix);
		curveToBezierMatrix.preApply(bezierBasisInverse);

		// multiply the basis and forward diff matrices together
		// saves much time since this needn't be done for each curve
		curveDrawMatrix.apply(curveBasisMatrix);
	}

	/**
	 * ( begin auto-generated from curve.xml )
	 *
	 * Draws a curved line on the screen. The first and second parameters
	 * specify the beginning control point and the last two parameters specify
	 * the ending control point. The middle parameters specify the start and
	 * stop of the curve. Longer curves can be created by putting a series of
	 * <b>curve()</b> functions together or using <b>curveVertex()</b>. An
	 * additional function called <b>curveTightness()</b> provides control for
	 * the visual quality of the curve. The <b>curve()</b> function is an
	 * implementation of Catmull-Rom splines. Using the 3D version requires
	 * rendering with P3D (see the Environment reference for more information).
	 *
	 * ( end auto-generated )
	 *
	 * <h3>Advanced</h3>
	 * As of revision 0070, this function no longer doubles the first
	 * and last points. The curves are a bit more boring, but it's more
	 * mathematically correct, and properly mirrored in curvePoint().
	 * <P>
	 * Identical to typing out:
	 * 
	 * <PRE>
	 * beginShape();
	 * curveVertex(x1, y1);
	 * curveVertex(x2, y2);
	 * curveVertex(x3, y3);
	 * curveVertex(x4, y4);
	 * endShape();
	 * </PRE>
	 *
	 * @webref shape:curves
	 * @param x1
	 *            coordinates for the beginning control point
	 * @param y1
	 *            coordinates for the beginning control point
	 * @param x2
	 *            coordinates for the first point
	 * @param y2
	 *            coordinates for the first point
	 * @param x3
	 *            coordinates for the second point
	 * @param y3
	 *            coordinates for the second point
	 * @param x4
	 *            coordinates for the ending control point
	 * @param y4
	 *            coordinates for the ending control point
	 * @see PGraphics#curveVertex(float, float)
	 * @see PGraphics#curveTightness(float)
	 * @see PGraphics#bezier(float, float, float, float, float, float, float, float, float, float, float, float)
	 */
	public void curve(float x1, float y1,
			float x2, float y2,
			float x3, float y3,
			float x4, float y4) {
		beginShape();
		curveVertex(x1, y1);
		curveVertex(x2, y2);
		curveVertex(x3, y3);
		curveVertex(x4, y4);
		endShape();
	}

	/**
	 * @param z1
	 *            coordinates for the beginning control point
	 * @param z2
	 *            coordinates for the first point
	 * @param z3
	 *            coordinates for the second point
	 * @param z4
	 *            coordinates for the ending control point
	 */
	public void curve(float x1, float y1, float z1,
			float x2, float y2, float z2,
			float x3, float y3, float z3,
			float x4, float y4, float z4) {
		beginShape();
		curveVertex(x1, y1, z1);
		curveVertex(x2, y2, z2);
		curveVertex(x3, y3, z3);
		curveVertex(x4, y4, z4);
		endShape();
	}

	//////////////////////////////////////////////////////////////

	// SPLINE UTILITY FUNCTIONS (used by both Bezier and Catmull-Rom)

	/**
	 * Setup forward-differencing matrix to be used for speedy
	 * curve rendering. It's based on using a specific number
	 * of curve segments and just doing incremental adds for each
	 * vertex of the segment, rather than running the mathematically
	 * expensive cubic equation.
	 * 
	 * @param segments
	 *            number of curve segments to use when drawing
	 * @param matrix
	 *            target object for the new matrix
	 */
	protected void splineForward(int segments, SMatrix3D matrix) {
		float f = 1.0f / segments;
		float ff = f * f;
		float fff = ff * f;

		matrix.set(0, 0, 0, 1,
				fff, ff, f, 0,
				6 * fff, 2 * ff, 0, 0,
				6 * fff, 0, 0, 0);
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
		boolean savedStroke = stroke;
//	    boolean savedFill = fill;
		int savedTextureMode = textureMode;

		stroke = false;
//	    fill = true;
		textureMode = IMAGE;

//	    float savedFillR = fillR;
//	    float savedFillG = fillG;
//	    float savedFillB = fillB;
//	    float savedFillA = fillA;
		//
//	    if (tint) {
//	      fillR = tintR;
//	      fillG = tintG;
//	      fillB = tintB;
//	      fillA = tintA;
		//
//	    } else {
//	      fillR = 1;
//	      fillG = 1;
//	      fillB = 1;
//	      fillA = 1;
//	    }

		u1 *= img.pixelDensity;
		u2 *= img.pixelDensity;
		v1 *= img.pixelDensity;
		v2 *= img.pixelDensity;

		beginShape(QUADS);
		texture(img);
		vertex(x, y, u1, v1);
		vertex(x, y, u1, v2);
		vertex(x, y, u2, v2);
		vertex(x, y, u2, v1);
		endShape();

		stroke = savedStroke;
//	    fill = savedFill;
		textureMode = savedTextureMode;

//	    fillR = savedFillR;
//	    fillG = savedFillG;
//	    fillB = savedFillB;
//	    fillA = savedFillA;
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
			float x, float y, float w, float h,
			int u1, int v1, int u2, int v2) {
		SImage simg = new SImage(img);
		imageImpl(simg, x, y, w, h, u1, v1, u2, v2);
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
	public void fillShape(Shape s) {
		showMissingWarning("fillShape");
	}

	@Override
	public void strokeShape(Shape s) {
		showMissingWarning("strokeShape");
	}

	@Override
	public void drawShape(Shape s) {
		showMissingWarning("drawShape");
	}

	//////////////////////////////////////////////////////////////

	// TEXT/FONTS

	protected SFont createDefaultFont(float size) {
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
		return textFont.ascent() * textSize;
	}

	@Override
	public float textDescent() {
		if (textFont == null) defaultFontOrDeath("textDescent");
		return textFont.descent() * textSize;
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
		if (size <= 0) {
			System.err.println("textFont: ignoring size " + size + " px:" +
					"the text size must be larger than zero");
			size = textSize;
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
			showWarning("Since Processing 1.0 beta, textMode() is now textAlign().");
			return;
		}
		if (mode == SCREEN) {
			showWarning("textMode(SCREEN) has been removed from Processing 2.0.");
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
			showWarning("textMode(" + modeStr + ") is not supported by this renderer.");
		}
	}

	protected boolean textModeCheck(int mode) { return true; }

	@Override
	public void textSize(float size) {
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
		for (int index = start; index < stop; index++) {
			textCharImpl(buffer[index], x, y);

			// this doesn't account for kerning
			x += textWidth(buffer[index]);
		}
//	      textX = x;
//	      textY = y;
//	      textZ = 0;  // this will get set by the caller if non-zero
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
	public void push() { pushStyle(); pushMatrix(); }

	@Override
	public void pop() { popStyle(); popMatrix(); }

	@Override
	public void pushMatrix() { showMethodWarning("pushMatrix"); }

	@Override
	public void popMatrix() { showMethodWarning("popMatrix"); }

	//////////////////////////////////////////////////////////////

	// MATRIX TRANSFORMS

	@Override
	public void translate(float x, float y) { showMissingWarning("translate"); }

	@Override
	public void rotate(float theta) { showMissingWarning("rotate"); }

	@Override
	public void rotateX(float angle) { showMethodWarning("rotateX"); }

	@Override
	public void rotateY(float angle) { showMethodWarning("rotateY"); }

	@Override
	public void rotateZ(float angle) { showMethodWarning("rotateZ"); }

	@Override
	public void rotate(float theta, float x, float y) { showMissingWarning("rotate"); }

	@Override
	public void rotate(float theta, float x, float y, float z) { showMissingWarning("rotate"); }

	@Override
	public void scale(float xy) { showMissingWarning("scale"); };

	@Override
	public void scale(float x, float y) { showMissingWarning("scale"); };

	@Override
	public void scale(float x, float y, float z) { showMissingWarning("scale"); }

	@Override
	public void shear(float x, float y) { showMissingWarning("shear"); };

	@Override
	public void shearX(float angle) { showMissingWarning("shearX"); }

	@Override
	public void shearY(float angle) { showMissingWarning("shearY"); }

	@Override
	public void transform(AffineTransform affineTransform) { showMissingWarning("transform"); };

	@Override
	public void setTransform(AffineTransform affineTransform) { showMissingWarning("setTransform"); };

	//////////////////////////////////////////////////////////////

	// MATRIX MORE

	@Override
	public void resetMatrix() { showMethodWarning("resetMatrix"); }

	@Override
	public void applyMatrix(SMatrix_D source) {
		if (source instanceof SMatrix2D) {
			applyMatrix((SMatrix2D) source);
		} else if (source instanceof SMatrix3D) {
			applyMatrix(source);
		}
	}

	@Override
	public void applyMatrix(SMatrix2D source) {
		applyMatrix(source.m00, source.m01, source.m02,
				source.m10, source.m11, source.m12);
	}

	@Override
	public void applyMatrix(float n00, float n01, float n02,
			float n10, float n11, float n12) {
		showMissingWarning("applyMatrix");
	}

	@Override
	public void applyMatrix(SMatrix3D source) {
		applyMatrix(source.m00, source.m01, source.m02, source.m03,
				source.m10, source.m11, source.m12, source.m13,
				source.m20, source.m21, source.m22, source.m23,
				source.m30, source.m31, source.m32, source.m33);
	}

	@Override
	public void applyMatrix(float n00, float n01, float n02, float n03,
			float n10, float n11, float n12, float n13,
			float n20, float n21, float n22, float n23,
			float n30, float n31, float n32, float n33) {
		showMissingWarning("applyMatrix");
	}
	//////////////////////////////////////////////////////////////

	// MATRIX GET/SET

	@Override
	public SMatrix_D getMatrix() { return getMatrix((SMatrix2D) null); }

	/**
	 * Copy the current transformation matrix into the specified target.
	 * Pass in null to create a new matrix.
	 */
	@Override
	public SMatrix2D getMatrix(SMatrix2D target) {
		showMissingWarning("getMatrix");
		return null;
	}

	/**
	 * Copy the current transformation matrix into the specified target.
	 * Pass in null to create a new matrix.
	 */
	@Override
	public SMatrix3D getMatrix(SMatrix3D target) {
		showMissingWarning("getMatrix");
		return null;
	}

	/**
	 * Set the current transformation matrix to the contents of another.
	 */
	@Override
	public void setMatrix(SMatrix_D source) {
		if (source instanceof SMatrix2D) {
			setMatrix((SMatrix2D) source);
		} else if (source instanceof SMatrix3D) {
			setMatrix(source);
		}
	}

	/**
	 * Set the current transformation to the contents of the specified source.
	 */
	@Override
	public void setMatrix(SMatrix2D source) {
		showMissingWarning("setMatrix");
	}

	/**
	 * Set the current transformation to the contents of the specified source.
	 */
	@Override
	public void setMatrix(SMatrix3D source) {
		showMissingWarning("setMatrix");
	}

	@Override
	public void printMatrix() { showMethodWarning("printMatrix"); }

	//////////////////////////////////////////////////////////////

	// SCREEN and MODEL transforms

	@Override
	public float screenX(float x, float y) { showMissingWarning("screenX"); return 0; }

	@Override
	public float screenY(float x, float y) { showMissingWarning("screenY"); return 0; }

	/**
	 * @param z
	 *            3D z-coordinate to be mapped
	 */
	@Override
	public float screenX(float x, float y, float z) { showMissingWarning("screenX"); return 0; }

	/**
	 * @param z
	 *            3D z-coordinate to be mapped
	 */
	@Override
	public float screenY(float x, float y, float z) { showMissingWarning("screenY"); return 0; }

	@Override
	public float screenZ(float x, float y, float z) { showMissingWarning("screenZ"); return 0; }

	//////////////////////////////////////////////////////////////

	// STYLE

	@Override
	public void pushStyle() {
		if (styleStackDepth == styleStack.length) {
			styleStack = (SStyle[]) GameBase.expand(styleStack);
		}
		SStyle s = styleStack[styleStackDepth];
		if (s == null) {
			styleStack[styleStackDepth] = s = new SStyle();
		}
		styleStackDepth++;
		getStyle(s);
	}

	@Override
	public void popStyle() {
		if (styleStackDepth == 0) {
			throw new RuntimeException("Too many popStyle() without enough pushStyle()");
		}
		styleStackDepth--;
		style(styleStack[styleStackDepth]);
	}

	@Override
	public void style(SStyle s) {
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

//		colorMode(RGB, 1);
//	    ambient(s.ambientR, s.ambientG, s.ambientB);
//	    emissive(s.emissiveR, s.emissiveG, s.emissiveB);
//	    specular(s.specularR, s.specularG, s.specularB);
//	    shininess(s.shininess);

		colorMode(s.colorMode,
				s.colorModeX, s.colorModeY, s.colorModeZ, s.colorModeA);

		if (s.textFont != null) {
			textFont(s.textFont, s.textSize);
			textLeading(s.textLeading);
		}
		textAlign(s.textAlign, s.textAlignY);
		textMode(s.textMode);
	}

	@Override
	public SStyle getStyle() { return getStyle(null); }

	@Override
	public SStyle getStyle(SStyle s) {
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

//	    s.ambientR = ambientR;
//	    s.ambientG = ambientG;
//	    s.ambientB = ambientB;
//	    s.specularR = specularR;
//	    s.specularG = specularG;
//	    s.specularB = specularB;
//	    s.emissiveR = emissiveR;
//	    s.emissiveG = emissiveG;
//	    s.emissiveB = emissiveB;
//	    s.shininess = shininess;

		s.textFont = textFont;
		s.textAlign = textAlign;
		s.textAlignY = textAlignY;
		s.textMode = textMode;
		s.textSize = textSize;
		s.textLeading = textLeading;

		return s;
	}
	//////////////////////////////////////////////////////////////

	// STROKE CAP/JOIN/WEIGHT

	@Override
	public void strokeWeight(float weight) {
		strokeWeight = weight;
	}

	@Override
	public void strokeJoin(int join) {
		strokeJoin = join;
	}

	@Override
	public void strokeCap(int cap) {
		strokeCap = cap;
	}

	//////////////////////////////////////////////////////////////

	// STROKE COLOR

	@Override
	public void noStroke() { stroke = false; }

	/**
	 * @param rgb
	 *            color value in hexadecimal notation
	 */
	@Override
	public void stroke(int rgb) {
		colorCalc(rgb);
		strokeFromCalc();
	}

	/**
	 * @param alpha
	 *            opacity of the stroke
	 */
	@Override
	public void stroke(int rgb, float alpha) {
		colorCalc(rgb, alpha);
		strokeFromCalc();
	}

	/**
	 * @param gray
	 *            specifies a value between white and black
	 */
	@Override
	public void stroke(float gray) {
		colorCalc(gray);
		strokeFromCalc();
	}

	@Override
	public void stroke(float gray, float alpha) {
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
	public void stroke(float v1, float v2, float v3) {
		colorCalc(v1, v2, v3);
		strokeFromCalc();
	}

	@Override
	public void stroke(float v1, float v2, float v3, float alpha) {
		colorCalc(v1, v2, v3, alpha);
		strokeFromCalc();
	}

	protected void strokeFromCalc() {
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
	}

	//////////////////////////////////////////////////////////////

	// FILL COLOR

	@Override
	public void noFill() {
		fill = false;
	}

	/**
	 * @param rgb
	 *            color value in hexadecimal notation
	 */
	@Override
	public void fill(int rgb) {
		colorCalc(rgb);
		fillFromCalc();
	}

	/**
	 * @param alpha
	 *            opacity of the fill
	 */
	@Override
	public void fill(int rgb, float alpha) {
		colorCalc(rgb, alpha);
		fillFromCalc();
	}

	/**
	 * @param gray
	 *            number specifying value between white and black
	 */
	@Override
	public void fill(float gray) {
		colorCalc(gray);
		fillFromCalc();
	}

	@Override
	public void fill(float gray, float alpha) {
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
	public void fill(float v1, float v2, float v3) {
		colorCalc(v1, v2, v3);
		fillFromCalc();
	}

	@Override
	public void fill(float v1, float v2, float v3, float alpha) {
		colorCalc(v1, v2, v3, alpha);
		fillFromCalc();
	}

	protected void fillFromCalc() {
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
	}

	//////////////////////////////////////////////////////////////

	// BACKGROUND
	/**
	 * @param rgb
	 *            color value in hexadecimal notation
	 */
	@Override
	public void background(int rgb) {
		colorCalc(rgb);
		backgroundFromCalc();
	}

	/**
	 * @param alpha
	 *            opacity of the background
	 */
	@Override
	public void background(int rgb, float alpha) {
		colorCalc(rgb, alpha);
		backgroundFromCalc();
	}

	/**
	 * @param gray
	 *            specifies a value between white and black
	 */
	@Override
	public void background(float gray) {
		colorCalc(gray);
		backgroundFromCalc();
	}

	@Override
	public void background(float gray, float alpha) {
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
	public void background(float v1, float v2, float v3) {
		colorCalc(v1, v2, v3);
		backgroundFromCalc();
	}

	@Override
	public void background(float v1, float v2, float v3, float alpha) {
		colorCalc(v1, v2, v3, alpha);
		backgroundFromCalc();
	}

	@Override
	public void clear() { background(0, 0, 0, 0); }

	protected void backgroundFromCalc() {
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

	protected void backgroundImpl() {
		pushStyle();
		pushMatrix();
		resetMatrix();
		noStroke();
		fill(backgroundColor);
		rect(0, 0, width, height);
		popMatrix();
		popStyle();
	}

	//////////////////////////////////////////////////////////////

	// COLOR MODE

	/**
	 * @param mode
	 *            Either RGB or HSB, corresponding to Red/Green/Blue and Hue/Saturation/Brightness
	 */
	@Override
	public void colorMode(int mode) {
		colorMode(mode, colorModeX, colorModeY, colorModeZ, colorModeA);
	}

	/**
	 * @param max
	 *            range for all color elements
	 */
	@Override
	public void colorMode(int mode, float max) {
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
	public void colorMode(int mode, float max1, float max2, float max3) {
		colorMode(mode, max1, max2, max3, colorModeA);
	}

	/**
	 * @param maxA
	 *            range for the alpha
	 */
	@Override
	public void colorMode(int mode,
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

	protected void colorCalc(int rgb) {
		if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {
			colorCalc((float) rgb);
		} else {
			colorCalcARGB(rgb, colorModeA);
		}
	}

	protected void colorCalc(int rgb, float alpha) {
		if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) { // see above
			colorCalc((float) rgb, alpha);
		} else {
			colorCalcARGB(rgb, alpha);
		}
	}

	protected void colorCalc(float gray) {
		colorCalc(gray, colorModeA);
	}

	protected void colorCalc(float gray, float alpha) {
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

	protected void colorCalc(float x, float y, float z) {
		colorCalc(x, y, z, colorModeA);
	}

	protected void colorCalc(float x, float y, float z, float a) {
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

	protected void colorCalcARGB(int argb, float alpha) {
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
	public int lerpColor(int c1, int c2, float amt) {
		return lerpColor(c1, c2, amt, colorMode);
	}

	static float[] lerpColorHSB1;
	static float[] lerpColorHSB2;

	/**
	 * @nowebref
	 *           Interpolate between two colors. Like lerp(), but for the
	 *           individual color components of a color supplied as an int value.
	 */
	public static int lerpColor(int c1, int c2, float amt, int mode) {
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

	/**
	 * Version of showWarning() that takes a parsed String.
	 */
	static public void showWarning(String msg, Object... args) { // ignore
		showWarning(String.format(msg, args));
	}

	/**
	 * Display a warning that the specified method is only available with 3D.
	 * 
	 * @param method
	 *            The method name (no parentheses)
	 */
	static public void showDepthWarning(String method) {
		showWarning(method + "() can only be used with a renderer that " +
				"supports 3D, such as P3D.");
	}

	/**
	 * Display a warning that the specified method that takes x, y, z parameters
	 * can only be used with x and y parameters in this renderer.
	 * 
	 * @param method
	 *            The method name (no parentheses)
	 */
	static public void showDepthWarningXYZ(String method) {
		showWarning(method + "() with x, y, and z coordinates " +
				"can only be used with a renderer that " +
				"supports 3D, such as P3D. " +
				"Use a version without a z-coordinate instead.");
	}

	/**
	 * Display a warning that the specified method is simply unavailable.
	 */
	static public void showMethodWarning(String method) {
		showWarning(method + "() is not available with this renderer.");
	}

	/**
	 * Error that a particular variation of a method is unavailable (even though
	 * other variations are). For instance, if vertex(x, y, u, v) is not
	 * available, but vertex(x, y) is just fine.
	 */
	static public void showVariationWarning(String str) {
		showWarning(str + " is not available with this renderer.");
	}

	/**
	 * Display a warning that the specified method is not implemented, meaning
	 * that it could be either a completely missing function, although other
	 * variations of it may still work properly.
	 */
	static public void showMissingWarning(String method) {
		showWarning(method + "(), or this particular variation of it, " +
				"is not available with this renderer.");
	}

	/**
	 * Show an renderer-related exception that halts the program. Currently just
	 * wraps the message as a RuntimeException and throws it, but might do
	 * something more specific might be used in the future.
	 */
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

		public void saveTargetAsync(final SGraphics renderer, final SImage target, // ignore
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
	public void pixel(int x, int y) { pixel(x, y, strokeColor); }

}