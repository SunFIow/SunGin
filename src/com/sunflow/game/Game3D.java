package com.sunflow.game;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.sunflow.engine.eventsystem.EventManager;
import com.sunflow.engine.eventsystem.adapters.KeyInputAdapter;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyPressedEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyReleasedEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent.MousePressedEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent.MouseReleasedEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent.MouseDraggedEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent.MouseMovedEvent;
import com.sunflow.engine.eventsystem.events.ScrollEvent;
import com.sunflow.engine.eventsystem.listeners.MouseInputListener;
import com.sunflow.engine.eventsystem.listeners.MouseMotionListener;
import com.sunflow.engine.eventsystem.listeners.ScrollListener;
import com.sunflow.gfx.GraphicsMatrix;
import com.sunflow.math.SVector;
import com.sunflow.math3d.Calculator;
import com.sunflow.math3d.SMatrix;
import com.sunflow.math3d.models.Base3DModel;
import com.sunflow.math3d.models.BaseModel;
import com.sunflow.math3d.models.DPolygon;
import com.sunflow.math3d.models.GenerateTerrain;
import com.sunflow.util.MathUtils;

public class Game3D extends GameBase {

	// ArrayList of all the 3D polygons - each 3D polygon has a 2D 'PolygonObject' inside called 'DrawablePolygon'
	public ArrayList<BaseModel> Models;
	public ArrayList<BaseModel> DModels;

	// The polygon that the mouse is currently over
	private BaseModel PolygonOver = null;

	public SVector vCameraPos;
	public SVector vCameraDir;
	protected float vertLook, horLook, horRotSpeed, vertRotSpeed;

	public SVector vLightDir;
	protected float sunPos;

	public boolean isCameraActivated;

	// The smaller the zoom the more zoomed out you are and visa versa, although altering too far from 1000 will make it look pretty weird
	protected float zoom, minZoom, maxZoom;

	protected int[] drawOrder;
	public boolean outlines;
	public boolean highlight;

	protected boolean[] keys;

	private float mouseDifX, mouseDifY;

	protected float movementSpeed;

	public GraphicsMatrix gMatrix;

	public ArrayList<SVector> vertices;

	@Override
	final void privatePreSetup() {

		Models = new ArrayList<>();
		DModels = new ArrayList<>();

//		vCameraPos = new SVector(197, 198, -980f);
		vCameraPos = new SVector(197, 198, 980f);
		vCameraDir = new SVector(0, 0, 0);
		vLightDir = new SVector(1, 1, 1);

		zoom = 1000;
		minZoom = 100;
		maxZoom = 10000;

		keys = new boolean[4];
		outlines = true;

		movementSpeed = 0.2f;

		vertLook = -0.9999999f;
		horLook = HALF_PI + PI;

		horRotSpeed = 900;
		vertRotSpeed = 2200;

		sunPos = 0;

		gMatrix = new GraphicsMatrix();

		super.privatePreSetup();

//		shapes = new ArrayList<>();
		updateView();
	}

	@Override
	public void createCanvas(float width, float height, float scaleW, float scaleH) {
		super.createCanvas(width, height, scaleW, scaleH);

//		screen.addKeyListener(new Game3DKeyListeners());
//		Game3DMouseListeners ml = new Game3DMouseListeners();
//		screen.addMouseListener(ml);
//		screen.addMouseWheelListener(ml);

		EventManager.addKeyInputListener(new Game3DKeyInputListeners());
		Game3DMouseInputListeners mil = new Game3DMouseInputListeners();
		EventManager.addMouseInputListener(mil);
		EventManager.addMouseMotionListener(mil);
		EventManager.addScrollListener(mil);

		if (isCameraActivated) invisibleMouse();
		showCrosshair(true);
	}

	@Override
	protected final void preDraw() {
		super.preDraw();

		cameraMovement();

		// Calculated all that is general for this camera position
		Calculator.SetPrederterminedInfo(this);

		controlSunAndLight();

//		Models.forEach(model -> model.updateModel());

		Models.forEach(model -> {
			if (model.needsUpdate()) model.updateModel();
		});

		// Set drawing order so closest polygons gets drawn last
		setDrawOrder();
		// Set the polygon that the mouse is currently over
		setModelOver();

//		drawCrosshair();
	}

	protected final void render() {
		// Draw Models in the Order that is set by the 'setOrder' function
		for (int i = 0; i < drawOrder.length; i++) {
			BaseModel current = DModels.get(drawOrder[i]);
			if (current instanceof DPolygon) ((DPolygon) current).renderStroke(outlines);
			current.render();
		}
	}

	public final void point(float x, float y, float z) {
		boolean draw = true;

		SVector applied = apply(x, y, z);
		float[] pos2d = convert3Dto2D(applied);
		float x2d = pos2d[0];
		float y2d = pos2d[1];
		if (pos2d[2] < 0) draw = false;

		if (draw) point(x2d, y2d);
	}

	public final void line(float x1, float y1, float z1, float x2, float y2, float z2) {
		boolean draw = true;

		SVector applied = apply(x1, y1, z1);
		float[] pos2d = convert3Dto2D(applied);
		float x2d1 = pos2d[0];
		float y2d1 = pos2d[1];
		if (pos2d[2] < 0) draw = false;

		applied = apply(x2, y2, z2);
		pos2d = convert3Dto2D(applied);
		float x2d2 = pos2d[0];
		float y2d2 = pos2d[1];
		if (pos2d[2] < 0) draw = false;

		if (draw) line(x2d1, y2d1, x2d2, y2d2);
	}

	public final void box(float l) { box(0, 0, 0, l); }

	public final void box(float x, float y, float z, float l) {
		beginShape(QUADS);

		// Bottom
		vertex(x, y, z);
		vertex(x, y + l, z);
		vertex(x + l, y + l, z);
		vertex(x + l, y, z);

		// Top
		vertex(x, y, z + l);
		vertex(x, y + l, z + l);
		vertex(x + l, y + l, z + l);
		vertex(x + l, y, z + l);

		// Left
		vertex(x, y, z);
		vertex(x + l, y, z);
		vertex(x + l, y, z + l);
		vertex(x, y, z + l);

		// Right
		vertex(x, y + l, z);
		vertex(x + l, y + l, z);
		vertex(x + l, y + l, z + l);
		vertex(x, y + l, z + l);

		// Back
		vertex(x + l, y, z);
		vertex(x + l, y, z + l);
		vertex(x + l, y + l, z + l);
		vertex(x + l, y + l, z);

		// Front
		vertex(x, y, z);
		vertex(x, y, z + l);
		vertex(x, y + l, z + l);
		vertex(x, y + l, z);

		endShape(CLOSE);
	}

	public final void vertex(float x, float y, float z) {
		SVector applied = apply(x, y, z);
		float[] pos2d = convert3Dto2D(applied);
		float x2d = pos2d[0];
		float y2d = pos2d[1];

		vertices.add(applied);
		super.vertex(x2d, y2d);
	}

	@Override
	public final void vertex(float x, float y) { vertex(x, y, 0); }

	@Override
	public void beginShape(int mode) {
		super.beginShape(mode);
		vertices = new ArrayList<>();
	}

	public final float[] convert3Dto2D(SVector pos) { return convert3Dto2D(pos.x, pos.y, pos.z); }

	public final float[] convert3Dto2D(float x, float y, float z) {
		float[] calcPos = Calculator.CalculatePositionP(vCameraPos, vCameraDir, x, y, z);
		float x2d = (width / 2 - Calculator.calcFocusPos[0]) + calcPos[0] * zoom();
		float y2d = (height / 2 - Calculator.calcFocusPos[1]) + calcPos[1] * zoom();
		return new float[] { x2d, y2d, Calculator.t };
	}

	private final void controlSunAndLight() {
		sunPos += 0.005f;
		float mapSize = GenerateTerrain.mapSize * GenerateTerrain.Size;
		vLightDir.x = mapSize / 2 - (mapSize / 2 + MathUtils.instance.cos(sunPos) * mapSize * 10);
		vLightDir.y = mapSize / 2 - (mapSize / 2 + MathUtils.instance.sin(sunPos) * mapSize * 10);
		vLightDir.z = -200.0f;
	}

	private final void cameraMovement() {
		if (!isCameraActivated) return;
		SVector viewVector = new SVector(vCameraDir.x - vCameraPos.x, vCameraDir.y - vCameraPos.y, vCameraDir.z - vCameraPos.z);
		SVector verticalVector = new SVector(0, 0, 1);
		SVector sideViewVector = SVector.cross(viewVector, verticalVector).normalized();

		SVector moveVector = new SVector();
		if (keys[0]) moveVector.add(viewVector.x, viewVector.y, viewVector.z);
		if (keys[2]) moveVector.sub(viewVector.x, viewVector.y, viewVector.z);
		if (keys[1]) moveVector.add(sideViewVector.x, sideViewVector.y, sideViewVector.z);
		if (keys[3]) moveVector.sub(sideViewVector.x, sideViewVector.y, sideViewVector.z);
		moveVector.mult(movementSpeed * fElapsedTime * 33);

		vCameraPos.add(moveVector);

		updateView();
	}

	private final void mouseMovement(float NewMouseX, float NewMouseY) {
		mouseDifX = (NewMouseX - width / 2);
		mouseDifY = (NewMouseY - height / 2) * (6 - Math.abs(vertLook) * 5);

		horLook += mouseDifX / horRotSpeed;
		vertLook -= mouseDifY / vertRotSpeed;

		if (vertLook > 0.9999999f) vertLook = 0.9999999f;
		if (vertLook < -0.9999999f) vertLook = -0.9999999f;

		updateView();
	}

	public final void updateView() {
		float r = (float) Math.sqrt(1 - (vertLook * vertLook));
		vCameraDir.x = vCameraPos.x + r * (float) Math.cos(horLook);
		vCameraDir.y = vCameraPos.y + r * (float) Math.sin(horLook);
		vCameraDir.z = vCameraPos.z + vertLook;

		Models.forEach(model -> model.markDirty());
	}

	private final void centerMouse() { moveMouseTo(width() / 2, height() / 2); }

//	private final void centerMouse() { moveMouse((int) -difX, (int) -difY); }

	public final void invisibleMouse() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		BufferedImage cursorImage = new BufferedImage(1, 1, Transparency.TRANSLUCENT);
		Cursor invisibleCursor = toolkit.createCustomCursor(cursorImage, new Point(0, 0), "InvisibleCursor");
		screen.setCursor(invisibleCursor);
	}

	public final void visibleMouse() { screen.setCursor(Cursor.getDefaultCursor()); }

	public final void highlight(boolean highlight) { this.highlight = highlight; }

	private final void setModelOver() {
		PolygonOver = null;
		DModels.forEach(p -> p.highlight(false));
		for (int i = drawOrder.length - 1; i >= 0; i--) {
			BaseModel current = DModels.get(drawOrder[i]);
//			current.drawablePolygon.highlight = true;
			if (current.draw() && current.visible() && current.contains(width / 2, height / 2)) {
				PolygonOver = current;
				if (highlight) current.highlight(true);
				break;
			}
		}
	}

	private final void setDrawOrder() {
		DModels.clear();

		Models.forEach(model -> {
//			if (model instanceof DPolygon) DModels.add((DPolygon) model);
//			else if (model instanceof Base3DModel) for (DPolygon pol : ((Base3DModel) model).polys) DModels.add(pol);
			if (model instanceof Base3DModel) for (DPolygon pol : ((Base3DModel) model).polys) DModels.add(pol);
			else DModels.add(model);
		});

		int size = DModels.size();

		float[] dists = new float[size];
		drawOrder = new int[size];

		for (int i = 0; i < size; i++) {
			dists[i] = DModels.get(i).dist();
			drawOrder[i] = i;
		}

		float ftemp;
		int itemp;
		for (int a = 0; a < size - 1; a++) {
			for (int b = 0; b < size - 1; b++) {
				if (dists[b] < dists[b + 1]) {
					ftemp = dists[b];
					itemp = drawOrder[b];
					drawOrder[b] = drawOrder[b + 1];
					dists[b] = dists[b + 1];

					drawOrder[b + 1] = itemp;
					dists[b + 1] = ftemp;
				}
			}
		}
	}

	public final List<String> getInfo3D(List<String> info) {
		float x = vCameraPos.x;
		float y = vCameraPos.y;
		float z = vCameraPos.z;
		info.add("[" + (int) x + "]" + "[" + (int) y + "]" + "[" + (int) z + "]");
		x = vCameraDir.x - x;
		y = vCameraDir.y - y;
		z = vCameraDir.z - z;
		info.add("[" + x + "]" + "[" + y + "]" + "[" + z + "]");

		x = horLook;
		y = 0;
		z = vertLook;
		info.add("[" + x + "]" + "[" + y + "]" + "[" + z + "]");
		return info;
	}

	public final float zoom() { return zoom; }

	public final void translate(float x, float y, float z) { gMatrix.translate(x, y, z); }

	public final void translateTo(float x, float y, float z) { gMatrix.translateTo(x, y, z); }

	public final void scale(float n) { scale(n, n, n); };

	public final void scale(float x, float y, float z) { gMatrix.scale(x, y, z); };

	public final void rotateX(float angle) { gMatrix.rotateX(angle); }

	public final void rotateY(float angle) { gMatrix.rotateY(angle); }

	public final void rotateZ(float angle) { gMatrix.rotateZ(angle); }

	public final void rotateXTo(float angle) { gMatrix.rotateXTo(angle); }

	public final void rotateYTo(float angle) { gMatrix.rotateYTo(angle); }

	public final void rotateZTo(float angle) { gMatrix.rotateZTo(angle); }

	public final SVector apply(float x, float y, float z) { return gMatrix.apply(x, y, z); }

	public final SVector apply(SVector pos) { return gMatrix.apply(pos); }

	public final SVector translated(float x, float y, float z) { return gMatrix.translated(x, y, z); }

	public final SVector translated(SVector pos) { return gMatrix.translated(pos); }

	public final SVector scaled(float x, float y, float z) { return gMatrix.scaled(x, y, z); }

	public final SVector scaled(SVector pos) { return gMatrix.scaled(pos); }

	public final SVector rotated(float x, float y, float z) { return gMatrix.rotated(x, y, z); }

	public final SVector rotated(SVector pos) { return gMatrix.rotated(pos); }

	public final SMatrix getRotationMatrixX() { return gMatrix.getRotationMatrixX(); }

	public final SMatrix getRotationMatrixY() { return gMatrix.getRotationMatrixY(); }

	public final SMatrix getRotationMatrixZ() { return gMatrix.getRotationMatrixZ(); }

	public final SMatrix[] getRotationMatrix() { return gMatrix.getRotationMatrix(); }

	public final float getRotationX() { return gMatrix.getRotationX(); }

	public final float getRotationY() { return gMatrix.getRotationY(); }

	public final float getRotationZ() { return gMatrix.getRotationZ(); }

	public final float[] getRotation() { return gMatrix.getRotation(); }

	public final void rotation(float[] rotation) { gMatrix.rotation(rotation); }

	@Override
	public final void pushMatrix() {
		super.pushMatrix();
		gMatrix.pushMatrix();
	}

	@Override
	public final void popMatrix() {
		super.popMatrix();
		gMatrix.popMatrix();
	}

	@Override
	public final void resetMatrix() {
		super.resetMatrix();
		gMatrix.resetMatrix();
	}

	private final class Game3DKeyListeners extends KeyAdapter {
		@Override
		public final void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_W) keys[0] = true;
			if (e.getKeyCode() == KeyEvent.VK_A) keys[1] = true;
			if (e.getKeyCode() == KeyEvent.VK_S) keys[2] = true;
			if (e.getKeyCode() == KeyEvent.VK_D) keys[3] = true;
			if (e.getKeyCode() == KeyEvent.VK_O) outlines = !outlines;
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);
		}

		@Override
		public final void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_W) keys[0] = false;
			if (e.getKeyCode() == KeyEvent.VK_A) keys[1] = false;
			if (e.getKeyCode() == KeyEvent.VK_S) keys[2] = false;
			if (e.getKeyCode() == KeyEvent.VK_D) keys[3] = false;
		}
	}

	private final class Game3DMouseListeners extends MouseAdapter {
		@Override
		public final void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) if (PolygonOver != null) PolygonOver.seeThrough(false);
			if (e.getButton() == MouseEvent.BUTTON3) if (PolygonOver != null) PolygonOver.seeThrough(true);
		}

		@Override
		public final void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getUnitsToScroll() > 0) {
				if (zoom > minZoom) zoom -= 25 * e.getUnitsToScroll();
			} else if (zoom < maxZoom) zoom += 25 * -e.getUnitsToScroll();
		}
	}

	private final class Game3DKeyInputListeners extends KeyInputAdapter {
		@Override
		public void onKeyPressed(KeyPressedEvent e) {
			if (e.getKeyCode() == GLFW.GLFW_KEY_W) keys[0] = true;
			if (e.getKeyCode() == GLFW.GLFW_KEY_A) keys[1] = true;
			if (e.getKeyCode() == GLFW.GLFW_KEY_S) keys[2] = true;
			if (e.getKeyCode() == GLFW.GLFW_KEY_D) keys[3] = true;
			if (e.getKeyCode() == GLFW.GLFW_KEY_O) outlines = !outlines;
			if (e.getKeyCode() == GLFW.GLFW_KEY_ESCAPE) System.exit(0);
		}

		@Override
		public void onKeyReleased(KeyReleasedEvent e) {
			if (e.getKeyCode() == GLFW.GLFW_KEY_W) keys[0] = false;
			if (e.getKeyCode() == GLFW.GLFW_KEY_A) keys[1] = false;
			if (e.getKeyCode() == GLFW.GLFW_KEY_S) keys[2] = false;
			if (e.getKeyCode() == GLFW.GLFW_KEY_D) keys[3] = false;
		}
	}

	private final class Game3DMouseInputListeners implements MouseInputListener, MouseMotionListener, ScrollListener {
		@Override
		public void onMousePressed(MousePressedEvent e) {
			if (e.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) if (PolygonOver != null) PolygonOver.seeThrough(false);
			if (e.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) if (PolygonOver != null) PolygonOver.seeThrough(true);
		}

		@Override
		public void onScrolled(ScrollEvent e) {
			if (e.getAmountY() > 0) {
				if (zoom > minZoom) zoom -= 25 * e.getAmountY();
			} else if (zoom < maxZoom) zoom += 25 * -e.getAmountY();
		}

		@Override
		public void onMouseMotion(MouseMotionEvent event) {
			if (!screen.hasFocus()) return;
			if (!isCameraActivated) return;
			mouseMovement((float) event.getMouseX(), (float) event.getMouseY());
			centerMouse();
		}

		@Override
		public void onMouseInput(MouseInputEvent event) {}

		@Override
		public void onMouseReleased(MouseReleasedEvent event) {}

		@Override
		public void onMouseMoved(MouseMovedEvent event) {}

		@Override
		public void onMouseDragged(MouseDraggedEvent event) {}
	}
}
