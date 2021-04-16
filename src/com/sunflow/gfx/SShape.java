package com.sunflow.gfx;

import com.sunflow.game.GameBase;
import com.sunflow.util.SConstants;

public class SShape implements SConstants {

	private static final String OUTSIDE_BEGIN_END_ERROR = "%1$s can only be called between beginShape() and endShape()";

	/** The shape type, one of GROUP, PRIMITIVE, PATH, or GEOMETRY. */
	protected int family;

	/** ELLIPSE, LINE, QUAD; TRIANGLE_FAN, QUAD_STRIP; etc. */
	protected int kind;

	public float width;
	public float height;

	private boolean openShape = false;
	private boolean openContour = false;

	private int vertexCount;
	/**
	 * When drawing POLYGON shapes, the second param is an array of length
	 * VERTEX_FIELD_COUNT. When drawing PATH shapes, the second param has only
	 * two variables.
	 */
	private float[][] vertices;

	private int vertexCodeCount;
	private int[] vertexCodes;

	private boolean close;

	public SShape(int family) { this.family = family; }

	public void beginShape() { beginShape(POLYGON); }

	public void beginShape(int kind) { this.kind = kind; openShape = true; }

	public void beginContour() {
		if (!openShape) {
			SGraphics.showWarning(OUTSIDE_BEGIN_END_ERROR, "beginContour()");
			return;
		}

		if (family == GROUP) {
			SGraphics.showWarning("Cannot begin contour in GROUP shapes");
			return;
		}

		if (openContour) {
			SGraphics.showWarning("Already called beginContour().");
			return;
		}
		openContour = true;
		beginContourImpl();
	}

	protected void beginContourImpl() {
		if (vertexCodes == null) {
			vertexCodes = new int[10];
		} else if (vertexCodes.length == vertexCodeCount) {
			vertexCodes = GameBase.expand(vertexCodes);
		}
		vertexCodes[vertexCodeCount++] = BREAK;
	}

	public void vertex(float x, float y) {
		if (vertices == null) {
			vertices = new float[10][2];
		} else if (vertices.length == vertexCount) {
			vertices = (float[][]) GameBase.expand(vertices);
		}
		vertices[vertexCount++] = new float[] { x, y };

		if (vertexCodes == null) {
			vertexCodes = new int[10];
		} else if (vertexCodes.length == vertexCodeCount) {
			vertexCodes = GameBase.expand(vertexCodes);
		}
		vertexCodes[vertexCodeCount++] = VERTEX;

		if (x > width) {
			width = x;
		}
		if (y > height) {
			height = y;
		}
	}

	public void quadraticVertex(float cx, float cy,
			float x3, float y3) {
		if (vertices == null) {
			vertices = new float[10][];
		} else if (vertexCount + 1 >= vertices.length) {
			vertices = (float[][]) GameBase.expand(vertices);
		}
		vertices[vertexCount++] = new float[] { cx, cy };
		vertices[vertexCount++] = new float[] { x3, y3 };

		// vertexCodes must be allocated because a vertex() call is required
		if (vertexCodes.length == vertexCodeCount) {
			vertexCodes = GameBase.expand(vertexCodes);
		}
		vertexCodes[vertexCodeCount++] = QUADRATIC_VERTEX;

		if (x3 > width) {
			width = x3;
		}
		if (y3 > height) {
			height = y3;
		}
	}

	public void quadraticVertex(float cx, float cy, float cz,
			float x3, float y3, float z3) {
	}

	public void endContour() {
		if (!openShape) {
			SGraphics.showWarning(OUTSIDE_BEGIN_END_ERROR, "endContour()");
			return;
		}

		if (family == GROUP) {
			SGraphics.showWarning("Cannot end contour in GROUP shapes");
			return;
		}

		if (!openContour) {
			SGraphics.showWarning("Need to call beginContour() first.");
			return;
		}
		endContourImpl();
		openContour = false;
	}

	protected void endContourImpl() {}

	public void endShape(int mode) {
		if (family == GROUP) {
			SGraphics.showWarning("Cannot end GROUP shape");
			return;
		}

		if (!openShape) {
			SGraphics.showWarning("Need to call beginShape() first");
			return;
		}

		close = (mode == CLOSE);

		// this is the state of the shape
		openShape = false;
	}

}
