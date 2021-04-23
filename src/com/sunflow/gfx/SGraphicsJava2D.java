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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.sunflow.math.SMatrix2D;
import com.sunflow.math.SMatrix_D;
import com.sunflow.math3d.SMatrix3D;

public class SGraphicsJava2D extends SGraphics {

	public static final int VERTEX_FIELD_COUNT = 2; // 37;

	public Graphics2D graphics;

	private Composite defaultComposite;

	protected GeneralPath gpath;

	// path for contours so gpath can be closed
	GeneralPath auxPath;

	boolean openContour;

	/// break the shape at the next vertex (next vertex() call is a moveto())
	boolean breakShape;

	/// coordinates for internal curve calculation
	float[] curveCoordX;
	float[] curveCoordY;
	float[] curveDrawX;
	float[] curveDrawY;

	private int transformCount;
	AffineTransform transformStack[] = new AffineTransform[MATRIX_STACK_DEPTH];
	private double[] transform = new double[6];

//	protected ArrayList<Shape> shapes_tmp;

	private Line2D.Float line = new Line2D.Float();
	private Ellipse2D.Float ellipse = new Ellipse2D.Float();
	private Rectangle2D.Float rect = new Rectangle2D.Float();
	private Arc2D.Float arc = new Arc2D.Float();

	protected Color tintColorObject;

	protected Color fillColorObject;
	public boolean fillGradient;
	public Paint fillGradientObject;

	protected Stroke strokeObject;
	protected Color strokeColorObject;
	public boolean strokeGradient;
	public Paint strokeGradientObject;

	private Font fontObject;

//	public SGraphicsJava2D() {}

	@Override
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

	@Override
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
		vertexCount = 0;
	}

	protected void handleSmooth() {
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

	@Override
	public void endDraw() {
		if (primaryGraphics) {} else {
			// TODO this is probably overkill for most tasks...
			loadPixels();
		}
		setModified();
		graphics.dispose();
	}

	@Override
	protected void defaultSettings() {
//		image = new BufferedImage(width, height, format);
//		graphics = image.createGraphics();

		defaultComposite = graphics.getComposite();
		gpath = new GeneralPath();

		super.defaultSettings();
	}

	//////////////////////////////////////////////////////////////

	// HINT

	@Override
	public void hint(int which) {
		// take care of setting the hint
		super.hint(which);

		// Avoid badness when drawing shorter strokes.
		// http://code.google.com/p/processing/issues/detail?id=1068
		// Unfortunately cannot always be enabled, because it makes the
		// stroke in many standard Processing examples really gross.
		if (which == ENABLE_STROKE_PURE) {
			graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
					RenderingHints.VALUE_STROKE_PURE);
		} else if (which == DISABLE_STROKE_PURE) {
			graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
					RenderingHints.VALUE_STROKE_DEFAULT);
		}
	}

	// POINTS,LINES, TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, QUADS, and QUAD_STRIP
	/**
	 * @param mode
	 *            POINTS, LINES, TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, QUADS, and QUAD_STRIP
	 */
	@Override
	public void beginShape(int mode) {
		shape = mode;
		vertexCount = 0;
//	    curveVertexCount = 0;

		gpath.reset();
//	    auxPath.reset();
//	    auxPath = null;

		S_Shape.beginShape(parent);
	}

	@Override
	public void texture(SImage image) {
		showMethodWarning("texture");
	}

	@Override
	public void vertex(float x, float y) {
		curveVertexCount = 0;

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
	public void vertex(float x, float y, float z) {
		showDepthWarningXYZ("vertex");
	}

	@Override
	public void vertex(int[] v) { vertex(v[X], v[Y]); }

	@Override
	public void vertex(float[] v) { vertex(v[X], v[Y]); }

	@Override
	public void vertex(float x, float y, float u, float v) {
		showVariationWarning("vertex(x, y, u, v)");
	}

	@Override
	public void vertex(float x, float y, float z, float u, float v) {
		showDepthWarningXYZ("vertex");
	}

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

	// CLIPPING

	@Override
	protected void clipImpl(float x1, float y1, float x2, float y2) {
		graphics.setClip(new Rectangle2D.Float(x1, y1, x2 - x1, y2 - y1));
	}

	@Override
	public void noClip() {
		graphics.setClip(null);
	}

	//////////////////////////////////////////////////////////////

	// BLEND

	@Override
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

	// BEZIER VERTICES

	@Override
	public void bezierVertex(float x1, float y1,
			float x2, float y2,
			float x3, float y3) {
		bezierVertexCheck();
		gpath.curveTo(x1, y1, x2, y2, x3, y3);
	}

	@Override
	public void bezierVertex(float x2, float y2, float z2,
			float x3, float y3, float z3,
			float x4, float y4, float z4) {
		showDepthWarningXYZ("bezierVertex");
	}

	//////////////////////////////////////////////////////////////

	// QUADRATIC BEZIER VERTICES

	@Override
	public void quadraticVertex(float ctrlX, float ctrlY,
			float endX, float endY) {
		bezierVertexCheck();
		Point2D cur = gpath.getCurrentPoint();

		float x1 = (float) cur.getX();
		float y1 = (float) cur.getY();

		bezierVertex(x1 + ((ctrlX - x1) * 2 / 3.0f), y1 + ((ctrlY - y1) * 2 / 3.0f),
				endX + ((ctrlX - endX) * 2 / 3.0f), endY + ((ctrlY - endY) * 2 / 3.0f),
				endX, endY);
	}

	@Override
	public void quadraticVertex(float x2, float y2, float z2,
			float x4, float y4, float z4) {
		showDepthWarningXYZ("quadVertex");
	}

	//////////////////////////////////////////////////////////////

	// CURVE VERTICES

	@Override
	protected void curveVertexCheck() {
		super.curveVertexCheck();

		if (curveCoordX == null) {
			curveCoordX = new float[4];
			curveCoordY = new float[4];
			curveDrawX = new float[4];
			curveDrawY = new float[4];
		}
	}

	@Override
	protected void curveVertexSegment(float x1, float y1,
			float x2, float y2,
			float x3, float y3,
			float x4, float y4) {
		curveCoordX[0] = x1;
		curveCoordY[0] = y1;

		curveCoordX[1] = x2;
		curveCoordY[1] = y2;

		curveCoordX[2] = x3;
		curveCoordY[2] = y3;

		curveCoordX[3] = x4;
		curveCoordY[3] = y4;

		curveToBezierMatrix.mult(curveCoordX, curveDrawX);
		curveToBezierMatrix.mult(curveCoordY, curveDrawY);

		// since the paths are continuous,
		// only the first point needs the actual moveto
		if (gpath == null) {
			gpath = new GeneralPath();
			gpath.moveTo(curveDrawX[0], curveDrawY[0]);
		}

		gpath.curveTo(curveDrawX[1], curveDrawY[1],
				curveDrawX[2], curveDrawY[2],
				curveDrawX[3], curveDrawY[3]);
	}

	@Override
	public void curveVertex(float x, float y, float z) {
		showDepthWarningXYZ("curveVertex");
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

	@Override
	protected void rectImpl(float x1, float y1, float x2, float y2) {
		rect.setFrame(x1, y1, x2 - x1, y2 - y1);
		drawShape(rect);
	}

	//////////////////////////////////////////////////////////////

	// ELLIPSE AND ARC

	@Override
	protected void ellipseImpl(float x, float y, float w, float h) {
		ellipse.setFrame(x, y, w, h);
		drawShape(ellipse);
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

	// BOX

	@Override
	public void box(float w, float h, float d) { showMethodWarning("box"); }

	@Override
	public void sphere(float r) { showMethodWarning("sphere"); }

	//////////////////////////////////////////////////////////////

	// BEZIER

	@Override
	public void bezierDetail(int detail) {}

	@Override
	public void curveDetail(int detail) {}
	//////////////////////////////////////////////////////////////

	// IMAGE

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
	@Override
	protected void imageImpl(Image img,
			float x, float y, float w, float h,
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
				(int) x, (int) y, (int) w, (int) h,
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
	public void fillShape(Shape s) {
		if (fillGradient) {
			graphics.setPaint(fillGradientObject);
			graphics.fill(s);
		} else if (fill) {
			graphics.setColor(fillColorObject);
			graphics.fill(s);
		}
	}

	@Override
	public void strokeShape(Shape s) {
		if (strokeGradient) {
			graphics.setPaint(strokeGradientObject);
			graphics.draw(s);
		} else if (stroke) {
			graphics.setColor(strokeColorObject);
			graphics.draw(s);
		}
	}

	@Override
	public void drawShape(Shape s) {
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

	@Override
	public float textAscent() {
		if (textFont == null) defaultFontOrDeath("textAscent");

		Font font = (Font) textFont.getNative();
		if (font != null) return graphics.getFontMetrics(font).getAscent();

		return super.textAscent();
	}

	@Override
	public float textDescent() {
		if (textFont == null) defaultFontOrDeath("textDescent");

		Font font = (Font) textFont.getNative();
		if (font != null) return graphics.getFontMetrics(font).getDescent();

		return super.textDescent();
	}

	@Override
	protected boolean textModeCheck(int mode) { return mode == MODEL; }

	@Override
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

		super.handleTextSize(size);
	}

	/**
	 * @return the text width of the chars [start, stop) in the buffer.
	 */
	@Override
	protected float textWidthImpl(char buffer[], int start, int stop) {
		if (textFont == null) defaultFontOrDeath("textWidth");

		// Avoid "Zero length string passed to TextLayout constructor" error
		if (start == stop) return 0;

		Font font = (Font) textFont.getNative();
		if (font != null) {
			FontMetrics metrics = graphics.getFontMetrics(font);
			return (float) metrics.getStringBounds(buffer, start, stop, graphics).getWidth();
		}

		return super.textWidthImpl(buffer, start, stop);
	}

	/**
	 * Implementation of actual drawing for a line of text.
	 */
	@Override
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
			super.textLineImpl(buffer, start, stop, x, y);
		}
	}

	//////////////////////////////////////////////////////////////

	// MATRIX STACK

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
	public void translate(float x, float y) { graphics.translate(x, y); }

	@Override
	public void rotate(float theta) { graphics.rotate(theta); }

	@Override
	public void rotateX(float angle) { showDepthWarning("rotateX"); }

	@Override
	public void rotateY(float angle) { showDepthWarning("rotateY"); }

	@Override
	public void rotateZ(float angle) { showDepthWarning("rotateZ"); }

	@Override
	public void rotate(float theta, float x, float y) { graphics.rotate(theta, x, y); }

	@Override
	public void rotate(float theta, float x, float y, float z) { showVariationWarning("rotate"); }

	@Override
	public void scale(float xy) { graphics.scale(xy, xy); };

	@Override
	public void scale(float x, float y) { graphics.scale(x, y); };

	@Override
	public void scale(float x, float y, float z) { showDepthWarningXYZ("scale"); }

	@Override
	public void shear(float x, float y) { graphics.shear(x, y); };

	@Override
	public void shearX(float angle) { graphics.shear(Math.tan(angle), 0); }

	@Override
	public void shearY(float angle) { graphics.shear(0, Math.tan(angle)); }

	@Override
	public void transform(AffineTransform affineTransform) { graphics.transform(affineTransform); };

	@Override
	public void setTransform(AffineTransform affineTransform) { graphics.setTransform(affineTransform); };

	//////////////////////////////////////////////////////////////

	// MATRIX MORE

	@Override
	public void resetMatrix() {
		graphics.setTransform(new AffineTransform());
		graphics.scale(pixelDensity, pixelDensity);
	}

	@Override
	public void applyMatrix(float n00, float n01, float n02,
			float n10, float n11, float n12) {
		graphics.transform(new AffineTransform(n00, n10, n01, n11, n02, n12));
	}

	@Override
	public void applyMatrix(float n00, float n01, float n02, float n03,
			float n10, float n11, float n12, float n13,
			float n20, float n21, float n22, float n23,
			float n30, float n31, float n32, float n33) {
		showVariationWarning("applyMatrix");
	}

	//////////////////////////////////////////////////////////////

	// MATRIX GET/SET

	@Override
	public SMatrix_D getMatrix() { return getMatrix((SMatrix2D) null); }

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
	public SMatrix3D getMatrix(SMatrix3D target) {
		showVariationWarning("getMatrix");
		return target;
	}

	@Override
	public void setMatrix(SMatrix2D source) {
		graphics.setTransform(new AffineTransform(source.m00, source.m10,
				source.m01, source.m11,
				source.m02, source.m12));
	}

	@Override
	public void setMatrix(SMatrix3D source) {
		showVariationWarning("setMatrix");
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

	@Override
	public float screenX(float x, float y, float z) { showDepthWarningXYZ("screenX"); return 0; }

	@Override
	public float screenY(float x, float y, float z) { showDepthWarningXYZ("screenY"); return 0; }

	@Override
	public float screenZ(float x, float y, float z) { showDepthWarningXYZ("screenZ"); return 0; }

	//////////////////////////////////////////////////////////////

	// STROKE CAP/JOIN/WEIGHT

	@Override
	public void strokeWeight(float weight) {
		super.strokeWeight(weight);
		strokeImpl();
	}

	@Override
	public void strokeJoin(int join) {
		super.strokeJoin(join);
		strokeImpl();
	}

	@Override
	public void strokeCap(int cap) {
		super.strokeCap(cap);
		strokeImpl();
	}

	protected void strokeImpl() {
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
	protected void strokeFromCalc() {
		super.strokeFromCalc();
		strokeColorObject = new Color(strokeColor, true);
		strokeGradient = false;
	}

	//////////////////////////////////////////////////////////////

	// TINT COLOR

	@Override
	protected void tintFromCalc() {
		super.tintFromCalc();
		tintColorObject = new Color(tintColor, true);
	}

	//////////////////////////////////////////////////////////////

	// FILL COLOR

	@Override
	protected final void fillFromCalc() {
		super.fillFromCalc();
		fillColorObject = new Color(fillColor, true);
		fillGradient = false;
	}

	//////////////////////////////////////////////////////////////

	// BACKGROUND

	@Override
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

	@Override
	public void backgroundImpl() {
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

	@Override
	public void pixel(int x, int y, int argb) {
		if ((x < 0) || (y < 0) || (x >= pixelWidth) || (y >= pixelHeight)) return;
//	    ((BufferedImage) image).setRGB(x, y, argb);
		getset[0] = argb;
//	    WritableRaster raster = ((BufferedImage) (useOffscreen && primarySurface ? offscreen : image)).getRaster();
//	    WritableRaster raster = image.getRaster();
		getRaster().setDataElements(x, y, getset);
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

	//////////////////////////////////////////////////////////////

	// MASK

	static final String MASK_WARNING = "mask() cannot be used on the main drawing surface";

	@Override
	public void mask(int[] alpha) {
		if (primaryGraphics) {
			showWarning(MASK_WARNING);

		} else {
			super.mask(alpha);
		}
	}

	@Override
	public void mask(SImage alpha) {
		if (primaryGraphics) {
			showWarning(MASK_WARNING);

		} else {
			super.mask(alpha);
		}
	}

	//////////////////////////////////////////////////////////////

	// COPY

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

	@Override
	public void copy(SImage src,
			int sx, int sy, int sw, int sh,
			int dx, int dy, int dw, int dh) {
		graphics.drawImage((Image) src.getNative(),
				dx, dy, dx + dw, dy + dh,
				sx, sy, sx + sw, sy + sh, null);
	}
}