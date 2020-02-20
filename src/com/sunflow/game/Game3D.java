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

import com.sunflow.math3d.Calculator;
import com.sunflow.math3d.MatrixF;
import com.sunflow.math3d.Vertex3F;
import com.sunflow.math3d.models.Base3DModel;
import com.sunflow.math3d.models.BaseModel;
import com.sunflow.math3d.models.DPolygon;
import com.sunflow.math3d.models.GenerateTerrain;

public class Game3D extends Game2D {

	// ArrayList of all the 3D polygons - each 3D polygon has a 2D 'PolygonObject' inside called 'DrawablePolygon'
	protected ArrayList<BaseModel> Models;
	protected ArrayList<BaseModel> DModels;

	// The polygon that the mouse is currently over
	private BaseModel PolygonOver = null;

	public Vertex3F vCameraPos;
	public Vertex3F vCameraDir;
	protected float vertLook, horLook, horRotSpeed, vertRotSpeed;

	public Vertex3F vLightDir;
	protected float sunPos;

	public boolean isCameraActivated;

	// The smaller the zoom the more zoomed out you are and visa versa, although altering too far from 1000 will make it look pretty weird
	protected float zoom, minZoom, maxZoom;

	public float zoom() { return zoom; }

	protected int[] drawOrder;
	public boolean outlines;
	public boolean highlight;

	protected boolean[] keys;

	protected float movementSpeed;

	@Override
	void privatePreSetup() {
		super.privatePreSetup();

		Models = new ArrayList<>();
		DModels = new ArrayList<>();

		vCameraPos = new Vertex3F(0, 0, 20);
		vCameraDir = new Vertex3F(0, 0, 0);
		vLightDir = new Vertex3F(1, 1, 1);

		zoom = 1000;
		minZoom = 500;
		maxZoom = 2500;

		keys = new boolean[4];
		outlines = true;

		movementSpeed = 0.2f;
		vertLook = -0.99f;
		horLook = 0;
		horRotSpeed = 900;
		vertRotSpeed = 2200;
		sunPos = 0;

		showCrosshair = true;

		rotateX(0);
		rotateY(0);
		rotateZ(0);
	}

	@Override
	void createFrame() {
		super.createFrame();
		canvas.addKeyListener(new Game3DKeyListeners());
		Game3DMouseListeners ml = new Game3DMouseListeners();
		canvas.addMouseListener(ml);
		canvas.addMouseWheelListener(ml);
		invisibleMouse();
	}

	@Override
	void privateDraw() {
		super.privateDraw();

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

	protected void render() {
//		for (int i = 0; i < drawOrder.length; i++) {
//			DPolygon current = DModels.get(drawOrder[i]);
//			current.renderStroke(outlines);
//			current.render();
//		}

		// Draw Models in the Order that is set by the 'setOrder' function
		for (int i = 0; i < drawOrder.length; i++) {
			BaseModel current = DModels.get(drawOrder[i]);
			if (current instanceof DPolygon) ((DPolygon) current).renderStroke(outlines);
			current.render();
		}
	}

	public final void point(float x, float y, float z) {
		float x2d, y2d;
		boolean draw = true;

//		Vertex3F rotated = matmul(rotY, new Vertex3F(x, y, z));
//		float[] pos2d = convert3Dto2D(rotated);
		float[] pos2d = convert3Dto2D(rotated(x, y, z));
		x2d = pos2d[0];
		y2d = pos2d[1];
		if (pos2d[2] < 0) draw = false;

		if (draw) point(x2d, y2d);
	}

	public final void line(float x1, float y1, float z1, float x2, float y2, float z2) {
		float x2d1, y2d1, x2d2, y2d2;
		boolean draw = true;

		float[] pos2d = convert3Dto2D(rotated(x1, y1, z1));
		x2d1 = pos2d[0];
		y2d1 = pos2d[1];
		if (pos2d[2] < 0) draw = false;

		pos2d = convert3Dto2D(rotated(x2, y2, z2));
		x2d2 = pos2d[0];
		y2d2 = pos2d[1];
		if (pos2d[2] < 0) draw = false;

		if (draw) line(x2d1, y2d1, x2d2, y2d2);
	}

	public final float[] convert3Dto2D(Vertex3F pos) { return convert3Dto2D(pos.x, pos.y, pos.z); }

	public final float[] convert3Dto2D(float x, float y, float z) {
		float[] calcPos = Calculator.CalculatePositionP(vCameraPos, vCameraDir, x, y, z);
		float x2d = (width / 2 - Calculator.calcFocusPos[0]) + calcPos[0] * zoom();
		float y2d = (height / 2 - Calculator.calcFocusPos[1]) + calcPos[1] * zoom();
		return new float[] { x2d, y2d, Calculator.t };
	}

	public final Vertex3F rotated(float x, float y, float z) { return rotated(new Vertex3F(x, y, z)); }

	public final Vertex3F rotated(Vertex3F pos) {
		Vertex3F rotated = matmul(rotX, pos);
		rotated = matmul(rotY, rotated);
		rotated = matmul(rotZ, rotated);
		return rotated;
	}

	public final Vertex3F matmul(MatrixF a, Vertex3F b) {
		MatrixF m = b.toMatrix();
		MatrixF matmul = a.dot(m);
		return Vertex3F.fromMatrix(matmul);
	}

	public final void rotateX(float angle) {
		float[][] rotArrX = {
				{ 1, 0, 0 },
				{ 0, cos(angle), -sin(angle) },
				{ 0, sin(angle), cos(angle) }
		};
		rotX = new MatrixF(3, 3);
		rotX.set(rotArrX);
	}

	MatrixF rotX, rotY, rotZ;

	public final void rotateY(float angle) {
		float[][] rotArrY = {
				{ cos(angle), 0, sin(angle) },
				{ 0, 1, 0 },
				{ -sin(angle), 0, cos(angle) }
		};
		rotY = new MatrixF(3, 3);
		rotY.set(rotArrY);
	}

	public final void rotateZ(float angle) {
		float[][] rotArrZ = {
				{ cos(angle), -sin(angle), 0 },
				{ sin(angle), cos(angle), 0 },
				{ 0, 0, 1 }
		};
		rotZ = new MatrixF(3, 3);
		rotZ.set(rotArrZ);
	}

	private void controlSunAndLight() {
		sunPos += 0.005f;
		float mapSize = GenerateTerrain.mapSize * GenerateTerrain.Size;
		vLightDir.x = mapSize / 2 - (mapSize / 2 + cos(sunPos) * mapSize * 10);
		vLightDir.y = mapSize / 2 - (mapSize / 2 + sin(sunPos) * mapSize * 10);
		vLightDir.z = -200.0f;
	}

	private void cameraMovement() {
		Vertex3F viewVector = new Vertex3F(vCameraDir.x - vCameraPos.x, vCameraDir.y - vCameraPos.y, vCameraDir.z - vCameraPos.z);
		Vertex3F verticalVector = new Vertex3F(0, 0, 1);
		Vertex3F sideViewVector = Vertex3F.cross(viewVector, verticalVector).normalized();

		Vertex3F moveVector = new Vertex3F();
		if (keys[0]) moveVector.add(viewVector.x, viewVector.y, viewVector.z);
		if (keys[2]) moveVector.sub(viewVector.x, viewVector.y, viewVector.z);
		if (keys[1]) moveVector.add(sideViewVector.x, sideViewVector.y, sideViewVector.z);
		if (keys[3]) moveVector.sub(sideViewVector.x, sideViewVector.y, sideViewVector.z);
		moveVector.mult(movementSpeed);

		vCameraPos.add(moveVector);
		updateView();
	}

	float difX, difY;

	private void mouseMovement(float NewMouseX, float NewMouseY) {
		difX = (NewMouseX - width / 2);
		difY = (NewMouseY - height / 2) * (6 - Math.abs(vertLook) * 5);

		vertLook -= difY / vertRotSpeed;
		horLook += difX / horRotSpeed;

		if (vertLook > 0.9999f) vertLook = 0.9999f;
		if (vertLook < -0.9999f) vertLook = -0.9999f;

		updateView();
	}

	private void updateView() {
		float r = (float) Math.sqrt(1 - (vertLook * vertLook));
		vCameraDir.x = vCameraPos.x + r * (float) Math.cos(horLook);
		vCameraDir.y = vCameraPos.y + r * (float) Math.sin(horLook);
		vCameraDir.z = vCameraPos.z + vertLook;

		Models.forEach(model -> model.markDirty());
	}

	private void centerMouse() { moveMouseTo(width() / 2, height() / 2); }

//	private void centerMouse() { moveMouse((int) -difX, (int) -difY); }

	private void invisibleMouse() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		BufferedImage cursorImage = new BufferedImage(1, 1, Transparency.TRANSLUCENT);
		Cursor invisibleCursor = toolkit.createCustomCursor(cursorImage, new Point(0, 0), "InvisibleCursor");
		canvas.setCursor(invisibleCursor);
	}

	final public void highlight(boolean highlight) {
		this.highlight = highlight;
	}

	private void setModelOver() {
//		int mX = mouseX - width() / 2;
//		int mY = mouseY - height() / 2;
//
//		DPolygone.forEach(p -> p.highlight(false));
//
//		for (int i = drawOrder.length - 1; i >= 0; i--) {
//			DPolygon current = DPolygone.get(drawOrder[i]);
////			if (current.draw && current.visible && current.isOver(mX, mY)) {
//			if (current.contains(mX, mY)) {
//				PolygonOver = current;
//				current.highlight(true);
//				break;
//			}
//		}

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

	private void setDrawOrder() {
		DModels.clear();

		Models.forEach(model -> {
//			if (model instanceof DPolygon) DModels.add((DPolygon) model);
//			else if (model instanceof Base3DModel) for (DPolygon pol : ((Base3DModel) model).polys) DModels.add(pol);
			if (model instanceof Base3DModel) for (DPolygon pol : ((Base3DModel) model).polys) DModels.add(pol);
			else DModels.add(model);
		});

		float[] dists = new float[DModels.size()];
		drawOrder = new int[DModels.size()];

		for (int i = 0; i < DModels.size(); i++) {
			dists[i] = DModels.get(i).dist();
			drawOrder[i] = i;
		}

		float temp;
		int tempr;
		for (int a = 0; a < dists.length - 1; a++) {
			for (int b = 0; b < dists.length - 1; b++) {
				if (dists[b] < dists[b + 1]) {
					temp = dists[b];
					tempr = drawOrder[b];
					drawOrder[b] = drawOrder[b + 1];
					dists[b] = dists[b + 1];

					drawOrder[b + 1] = tempr;
					dists[b + 1] = temp;
				}
			}
		}
	}

	private class Game3DKeyListeners extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_W) keys[0] = true;
			if (e.getKeyCode() == KeyEvent.VK_A) keys[1] = true;
			if (e.getKeyCode() == KeyEvent.VK_S) keys[2] = true;
			if (e.getKeyCode() == KeyEvent.VK_D) keys[3] = true;
			if (e.getKeyCode() == KeyEvent.VK_O) outlines = !outlines;
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_W) keys[0] = false;
			if (e.getKeyCode() == KeyEvent.VK_A) keys[1] = false;
			if (e.getKeyCode() == KeyEvent.VK_S) keys[2] = false;
			if (e.getKeyCode() == KeyEvent.VK_D) keys[3] = false;
		}
	}

	@Override
	void updateMousePosition(int x, int y) {
		if (!canvas.hasFocus()) return;
		if (!isCameraActivated) {
			super.updateMousePosition(x, y);
			return;
		}
		mouseMovement(x, y);
		super.updateMousePosition(x, y);
		centerMouse();
	}

	private class Game3DMouseListeners extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) if (PolygonOver != null) PolygonOver.seeThrough(false);
			if (e.getButton() == MouseEvent.BUTTON3) if (PolygonOver != null) PolygonOver.seeThrough(true);
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getUnitsToScroll() > 0) {
				if (zoom > minZoom) zoom -= 25 * e.getUnitsToScroll();
			} else if (zoom < maxZoom) zoom += 25 * -e.getUnitsToScroll();
		}
	}
}
