package com.sunflow.game;

import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.sunflow.logging.Log;
import com.sunflow.math3d.Calculator;
import com.sunflow.math3d.Vertex3F;
import com.sunflow.math3d.models.Base3DModel;
import com.sunflow.math3d.models.DPolygon;
import com.sunflow.tutorial_copy.GenerateTerrain;

public class Game3D extends Game2D {

	// ArrayList of all the 3D polygons - each 3D polygon has a 2D 'PolygonObject' inside called 'DrawablePolygon'
	protected ArrayList<Base3DModel> Models;
	protected ArrayList<DPolygon> DPolygone;

	// The polygon that the mouse is currently over
	private DPolygon PolygonOver = null;

//	public Vertex3F vCameraPos;
//	public Vertex3F vCameraDir;
//
//	public Vertex3F vLightDir;

	public Vertex3F vCameraPos;
	public Vertex3F vCameraDir;

	public Vertex3F vLightDir;

	// The smaller the zoom the more zoomed out you are and visa versa, although altering too far from 1000 will make it look pretty weird
	public float zoom;
	protected float minZoom, maxZoom;

	protected int[] drawOrder;

	protected boolean[] keys;
	public boolean outlines;

	protected float movementSpeed;
	protected float vertLook, horLook, horRotSpeed, vertRotSpeed, sunPos;

	@Override
	void privatePreSetup() {
		super.privatePreSetup();

		Models = new ArrayList<Base3DModel>();
		DPolygone = new ArrayList<DPolygon>();

		vCameraPos = new Vertex3F(0, 0, 100);
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
	}

	@Override
	void createFrame() {
		super.createFrame();
		canvas.addKeyListener(new Game3DKeyListeners());
		Game3DMouseListeners ml = new Game3DMouseListeners();
		canvas.addMouseListener(ml);
		canvas.addMouseMotionListener(ml);
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

		for (Base3DModel model : Models) if (model.needsUpdate()) model.project();

		// Set drawing order so closest polygons gets drawn last
		setDrawOrder();
		// Set the polygon that the mouse is currently over
		setPolygonOver();

//		drawCrosshair();
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

	private void mouseMovement(float NewMouseX, float NewMouseY) {
		if (robot) return;
		float difX = (NewMouseX - mouseX);
		float difY = (NewMouseY - mouseY) * (6 - Math.abs(vertLook) * 5);
		Log.debug(difY);

		vertLook -= difY / vertRotSpeed;
		horLook += difX / horRotSpeed;

		if (vertLook > 0.999f) vertLook = 0.999f;
		if (vertLook < -0.999f) vertLook = -0.999f;

		updateView();
	}

	private void updateView() {
		float r = (float) Math.sqrt(1 - (vertLook * vertLook));
		vCameraDir.x = vCameraPos.x + r * (float) Math.cos(horLook);
		vCameraDir.y = vCameraPos.y + r * (float) Math.sin(horLook);
		vCameraDir.z = vCameraPos.z + vertLook;
	}

	boolean robot;

//	private void centerMouse() { moveMouseTo(width() / 2, height() / 2); }
	private void centerMouse() {
		try {
			Robot r = new Robot();
			robot = true;
			r.mouseMove(x + width() / 2, y + height() / 2);
			robot = false;
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	private void invisibleMouse() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		BufferedImage cursorImage = new BufferedImage(1, 1, Transparency.TRANSLUCENT);
		Cursor invisibleCursor = toolkit.createCustomCursor(cursorImage, new Point(0, 0), "InvisibleCursor");
		canvas.setCursor(invisibleCursor);
	}

	private void setPolygonOver() {
		int mX = mouseX - width() / 2;
		int mY = mouseY - height() / 2;

		DPolygone.forEach(p -> p.highlight = false);

		for (int i = drawOrder.length - 1; i >= 0; i--) {
			DPolygon current = DPolygone.get(drawOrder[i]);
//			if (current.draw && current.visible && current.isOver(mX, mY)) {
			if (current.contains(mX, mY)) {
				PolygonOver = current;
				current.highlight = true;
				break;
			}
		}
	}

	private void setDrawOrder() {
		DPolygone.clear();

		for (Base3DModel m : Models) {
			for (DPolygon pol : m.polys) {
				DPolygone.add(pol);
			}
		}
		float[] dists = new float[DPolygone.size()];
		drawOrder = new int[DPolygone.size()];

		for (int i = 0; i < DPolygone.size(); i++) {
			dists[i] = DPolygone.get(i).dist;
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
		mouseMovement(x, y);
		super.updateMousePosition(x, y);
		centerMouse();
	}

	private class Game3DMouseListeners extends MouseAdapter {
		@Override
		public void mouseDragged(MouseEvent e) {
//			mouseMovement(e.getX(), e.getY());
//			mouseX = e.getX();
//			mouseY = e.getY();
//			centerMouse();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
//			mouseMovement(e.getX(), e.getY());
//			mouseX = e.getX();
//			mouseY = e.getY();
//			centerMouse();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) if (PolygonOver != null) PolygonOver.seeThrough = false;
			if (e.getButton() == MouseEvent.BUTTON3) if (PolygonOver != null) PolygonOver.seeThrough = true;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getUnitsToScroll() > 0) if (zoom > minZoom) zoom -= 25 * e.getUnitsToScroll();
			else if (zoom < maxZoom) zoom -= 25 * e.getUnitsToScroll();
		}
	}
}
