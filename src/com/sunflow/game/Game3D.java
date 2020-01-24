package com.sunflow.game;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.sunflow.math1.GenerateTerrain;
import com.sunflow.math3d.Calculator;
import com.sunflow.math3d.Vertex3D;
import com.sunflow.math3d.models.Base3DModel;
import com.sunflow.math3d.models.DPolygon;
import com.sunflow.util.Log;

public class Game3D extends Game2D {

	protected ArrayList<Base3DModel> Models;
	protected ArrayList<DPolygon> DPolygone;

	public Vertex3D vCameraPos;
	public Vertex3D vCameraDir;

	public Vertex3D vLightDir;

	public double zoom;

	protected int[] drawOrder;

	private boolean[] keys;
	public boolean outlines;

	private double movementSpeed;
	private double vertLook, horLook, horRotSpeed, vertRotSpeed, sunPos;

	@Override
	void privatePreSetup() {
		super.privatePreSetup();
		Log.info("starting 3D Game");

		Models = new ArrayList<Base3DModel>();
		DPolygone = new ArrayList<DPolygon>();

		vCameraPos = new Vertex3D(0, 0, -100);
		vCameraDir = new Vertex3D(0, 0, 0);

		vLightDir = new Vertex3D(1, 1, 1);

		zoom = 1000;

		keys = new boolean[4];
		outlines = true;

		movementSpeed = 0.2;
		vertLook = -0.9;
		horLook = 0;
		horRotSpeed = 900;
		vertRotSpeed = 2200;
		sunPos = 0;
	}

	@Override
	void createFrame() {
		super.createFrame();

		canvas.addKeyListener(new Game3DKeyListeners());
		canvas.addMouseMotionListener(new Game3DMouseMousenListeners());
		invisibleMouse();
	}

	@Override
	void privateDraw() {
		super.privateDraw();

		cameraMovement();

		// Calculated all that is general for this camera position
		Calculator.SetPrederterminedInfo(this);

		controlSunAndLight();

		for (Base3DModel model : Models) {
			if (model.needsUpdate()) {
				model.project();
			}
		}

		// Set drawing order so closest polygons gets drawn last
		setDrawOrder();
		// Set the polygon that the mouse is currently over
		setPolygonOver();

		drawMouseAim();
	}

	private void controlSunAndLight() {
		sunPos += 0.005;
		double mapSize = GenerateTerrain.mapSize * GenerateTerrain.Size;
		vLightDir.x = mapSize / 2 - (mapSize / 2 + Math.cos(sunPos) * mapSize * 10);
		vLightDir.y = mapSize / 2 - (mapSize / 2 + Math.sin(sunPos) * mapSize * 10);
		vLightDir.z = -200;
	}

	private void cameraMovement() {
		Vertex3D moveVector = new Vertex3D();
		Vertex3D viewVector = new Vertex3D(vCameraDir.x - vCameraPos.x, vCameraDir.y - vCameraPos.y, vCameraDir.z - vCameraPos.z);
		Vertex3D verticalVector = new Vertex3D(0, 0, 1);
		Vertex3D sideViewVector = Vertex3D.cross(viewVector, verticalVector).normalized();

		if (keys[0]) {
			moveVector.add(viewVector.x, viewVector.y, viewVector.z);
		}

		if (keys[2]) {
			moveVector.sub(viewVector.x, viewVector.y, viewVector.z);
		}

		if (keys[1]) {
			moveVector.add(sideViewVector.x, sideViewVector.y, sideViewVector.z);
		}

		if (keys[3]) {
			moveVector.sub(sideViewVector.x, sideViewVector.y, sideViewVector.z);
		}
		moveVector.mult(movementSpeed);

		Vertex3D newLocation = Vertex3D.add(vCameraPos, moveVector);

		moveTo(newLocation.x, newLocation.y, newLocation.z);
	}

	private void moveTo(double x, double y, double z) {
		vCameraPos.x = x;
		vCameraPos.y = y;
		vCameraPos.z = z;
		updateView();
	}

	private void mouseMovement(double NewMouseX, double NewMouseY) {
		double difX = (NewMouseX - width / 2);
		double difY = (NewMouseY - height / 2);
		difY *= 6 - Math.abs(vertLook) * 5;
		vertLook -= difY / vertRotSpeed;
		horLook += difX / horRotSpeed;

		if (vertLook > 0.999)
			vertLook = 0.999;

		if (vertLook < -0.999)
			vertLook = -0.999;

		updateView();
	}

	private void updateView() {
		double r = Math.sqrt(1 - (vertLook * vertLook));
		vCameraDir.x = vCameraPos.x + r * Math.cos(horLook);
		vCameraDir.y = vCameraPos.y + r * Math.sin(horLook);
		vCameraDir.z = vCameraPos.z + vertLook;
	}

	private void centerMouse() {
		moveMouseTo(width() / 2, height() / 2);
//		try {
//			r = new Robot();
//			r.mouseMove(x + width / 2, y + height / 2);
//		} catch (AWTException e) {
//			e.printStackTrace();
//		}
	}

	private void invisibleMouse() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		BufferedImage cursorImage = new BufferedImage(1, 1, Transparency.TRANSLUCENT);
		Cursor invisibleCursor = toolkit.createCustomCursor(cursorImage, new Point(0, 0), "InvisibleCursor");
		canvas.setCursor(invisibleCursor);
	}

	private void setPolygonOver() {
		boolean isOver = false;
		int mX = mouseX - width() / 2;
		int mY = mouseY - height() / 2;

		for (int i = drawOrder.length - 1; i >= 0; i--) {
			DPolygone.get(drawOrder[i]).highlight = false;
		}

		for (int i = drawOrder.length - 1; i >= 0; i--) {
			DPolygon poly = DPolygone.get(drawOrder[i]);
			if (!isOver && poly.isOver(mX, mY)) {
				poly.highlight = true;
				isOver = true;
			} else {
				poly.highlight = false;
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
		double[] dists = new double[DPolygone.size()];
		drawOrder = new int[DPolygone.size()];

		for (int i = 0; i < DPolygone.size(); i++) {
			dists[i] = DPolygone.get(i).dist;
			drawOrder[i] = i;
		}

		double temp;
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
			if (e.getKeyCode() == KeyEvent.VK_W)
				keys[0] = true;
			if (e.getKeyCode() == KeyEvent.VK_A)
				keys[1] = true;
			if (e.getKeyCode() == KeyEvent.VK_S)
				keys[2] = true;
			if (e.getKeyCode() == KeyEvent.VK_D)
				keys[3] = true;
			if (e.getKeyCode() == KeyEvent.VK_O)
				outlines = !outlines;
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				System.exit(0);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_W)
				keys[0] = false;
			if (e.getKeyCode() == KeyEvent.VK_A)
				keys[1] = false;
			if (e.getKeyCode() == KeyEvent.VK_S)
				keys[2] = false;
			if (e.getKeyCode() == KeyEvent.VK_D)
				keys[3] = false;
		}
	}

	private class Game3DMouseMousenListeners implements MouseMotionListener {
		@Override
		public void mouseDragged(MouseEvent arg0) {
			mouseMovement(arg0.getX(), arg0.getY());
			mouseX = arg0.getX();
			mouseY = arg0.getY();
			centerMouse();
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			mouseMovement(arg0.getX(), arg0.getY());
			mouseX = arg0.getX();
			mouseY = arg0.getY();
			centerMouse();
		}
	}
}
