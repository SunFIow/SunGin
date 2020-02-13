package com.sunflow.tutorial_copy;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.sunflow.math3d.Vertex3F;

public class TutorialGame3D extends JPanel implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	public static void main(String[] args) {
		Dimension desktopSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension screenSize = new Dimension(desktopSize.width, desktopSize.height);

		new TutorialGame3D(screenSize.width / 2, screenSize.height / 2);
	}

	// ArrayList of all the 3D polygons - each 3D polygon has a 2D 'PolygonObject' inside called 'DrawablePolygon'
	private ArrayList<IModel> Models = new ArrayList<IModel>();
	private ArrayList<DPolygon> DPolygons = new ArrayList<DPolygon>();

	// The polygon that the mouse is currently over
	private PolygonObject PolygonOver = null;

	public Vertex3F vCameraPos = new Vertex3F(0, 0, 100);
	public Vertex3F vCameraDir = new Vertex3F(0, 0, 0);

	public Vertex3F vLightDir = new Vertex3F(1, 1, 1);

	// The smaller the zoom the more zoomed out you are and visa versa, although altering too far from 1000 will make it look pretty weird
	public static float zoom = 1000;

	private float minZoom = 500;
	private float maxZoom = 2500;

	// Used for keeping mouse in center
	private Robot r;
	@SuppressWarnings("unused")
	private float mouseX = 0;
	@SuppressWarnings("unused")
	private float mouseY = 0;

	private float movementSpeed = 0.2f;

	// FPS is a bit primitive, you can set the MaxFPS as high as u want
	private int drawFPS = 0, MaxFPS = 1000, framesSLFC;
	long LastRefresh = 0, LastFPSCheck = 0;

	@SuppressWarnings("unused")
	private float SleepTime = 1000.0f / MaxFPS, StartTime = System.currentTimeMillis();

	// VertLook goes from 0.999 to -0.999, minus being looking down and + looking up, HorLook takes any number and goes round in radians
	// aimSight changes the size of the center-cross. The lower HorRotSpeed or VertRotSpeed, the faster the camera will rotate in those directions
	private float vertLook = -0.99f, horLook = 0, aimSight = 4, horRotSpeed = 900, vertRotSpeed = 2200, sunPos = 0;

	// will hold the order that the polygons in the ArrayList DPolygon should be drawn meaning DPolygon.get(NewOrder[0]) gets drawn first
	private int[] newOrder;

	public boolean outlines = true;
	private boolean[] keys = new boolean[4];

	public int x, y, width, height;

	public TutorialGame3D(int width, int height) {
		this.width = width;
		this.height = height;
		try {
			r = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}

		addKeyListener(this);
		setFocusable(true);

		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);

		invisibleMouse();

		Models.add(new Pyramid(this, 0, -5, 0, 2, 2, 2, Color.green));

		Models.add(new Cube(this, 0, 0, 0, 2, 2, 2, Color.red));
		Models.add(new Cube(this, 18, -5, 0, 2, 2, 2, Color.red));
		Models.add(new Cube(this, 20, -5, 0, 2, 2, 2, Color.red));
		Models.add(new Cube(this, 22, -5, 0, 2, 2, 2, Color.red));
		Models.add(new Cube(this, 20, -5, 2, 2, 2, 2, Color.red));

		new GenerateTerrain(this, Models);

		JFrame F = new JFrame();
		F.setUndecorated(true);
		F.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		F.setSize(width, height);
		F.setLocationRelativeTo(null);
		this.x = F.getX();
		this.y = F.getY();

		F.add(this);
		F.setVisible(true);
	}

	@Override
	public void paintComponent(Graphics g) { // Clear screen and draw background color
		g.setColor(new Color(140, 180, 180));
		g.fillRect(0, 0, width, height);

		cameraMovement();

		// Calculated all that is general for this camera position
		Calculator.SetPrederterminedInfo(this);

		controlSunAndLight();

		// rotate and update shape examples
		Models.get(0).rotateY(0.005f);
		Models.get(1).rotateZ(0.005f);

		for (IModel m : Models) m.updatePolygon();

		// Set drawing order so closest polygons gets drawn last
		setOrder();

		// Set the polygon that the mouse is currently over
		setPolygonOver();

		// Draw Models in the Order that is set by the 'setOrder' function
		for (int i = 0; i < newOrder.length; i++)
			DPolygons.get(newOrder[i]).drawablePolygon.drawPolygon(g);

		// Draw the cross in the center of the screen
		drawMouseAim(g);

		// FPS display
		g.drawString("FPS: " + drawFPS + " (Benchmark)", 40, 40);
		float x = vCameraPos.x;
		float y = vCameraPos.y;
		float z = vCameraPos.z;
		g.drawString("[" + (int) x + "]" + "[" + (int) y + "]" + "[" + (int) z + "]", 40, 60);
		x = vCameraDir.x - x;
		y = vCameraDir.y - y;
		z = vCameraDir.z - z;
		g.drawString("[" + x + "]" + "[" + y + "]" + "[" + z + "]", 40, 80);

//		Log.info(vCameraPos);
//		Log.info(vCameraDir);

		SleepAndRefresh();
	}

	private void setOrder() {
		DPolygons.clear();

		for (IModel m : Models) {
			if (m instanceof DPolygon)
				DPolygons.add((DPolygon) m);
			else if (m instanceof IModel)
				for (DPolygon dp : m.polys)
					DPolygons.add(dp);
		}
		float[] k = new float[DPolygons.size()];
		newOrder = new int[DPolygons.size()];

		for (int i = 0; i < DPolygons.size(); i++) {
			k[i] = DPolygons.get(i).avgDist;
			newOrder[i] = i;
		}

		float temp;
		int tempr;
		for (int a = 0; a < k.length - 1; a++) for (int b = 0; b < k.length - 1; b++)
			if (k[b] < k[b + 1]) {
				temp = k[b];
				tempr = newOrder[b];
				newOrder[b] = newOrder[b + 1];
				k[b] = k[b + 1];

				newOrder[b + 1] = tempr;
				k[b + 1] = temp;
			}
	}

	private void invisibleMouse() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		BufferedImage cursorImage = new BufferedImage(1, 1, Transparency.TRANSLUCENT);
		Cursor invisibleCursor = toolkit.createCustomCursor(cursorImage, new Point(0, 0), "InvisibleCursor");
		setCursor(invisibleCursor);
	}

	private void drawMouseAim(Graphics g) {
		g.setColor(Color.black);
		g.drawLine((int) (width / 2 - aimSight), height / 2, (int) (width / 2 + aimSight), height / 2);
		g.drawLine(width / 2, (int) (height / 2 - aimSight), width / 2, (int) (height / 2 + aimSight));
	}

	private void SleepAndRefresh() {
		long current = System.currentTimeMillis();
		long timeSLU = current - LastRefresh;
		long timeSLFC = current - LastFPSCheck;

		if (timeSLFC > 1000.0f) {
			drawFPS = framesSLFC;
			LastFPSCheck = current;
			framesSLFC = 0;
		}

		if (timeSLU < 1000.0f / MaxFPS) {
			try {
				Thread.sleep((long) (1000.0f / MaxFPS - timeSLU));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		LastRefresh = current;
		framesSLFC++;

		repaint();
	}

	private void controlSunAndLight() {
		sunPos += 0.005f;
		float mapSize = GenerateTerrain.mapSize * GenerateTerrain.Size;
		vLightDir.x = mapSize / 2 - (mapSize / 2 + (float) Math.cos(sunPos) * mapSize * 10);
		vLightDir.y = mapSize / 2 - (mapSize / 2 + (float) Math.sin(sunPos) * mapSize * 10);
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

	private void setPolygonOver() {
		PolygonOver = null;
		DPolygons.forEach(p -> p.drawablePolygon.highlight = false);
		for (int i = newOrder.length - 1; i >= 0; i--) {
			DPolygon current = DPolygons.get(newOrder[i]);
//			current.drawablePolygon.highlight = true;
			if (current.draw && current.drawablePolygon.visible && current.drawablePolygon.MouseOver()) {
				PolygonOver = current.drawablePolygon;
				current.drawablePolygon.highlight = true;
				break;
			}
		}
	}

	private void mouseMovement(float NewMouseX, float NewMouseY) {
		float difX = (NewMouseX - width / 2);
		float difY = (NewMouseY - height / 2) * (6 - Math.abs(vertLook) * 5);

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

	private void centerMouse() { r.mouseMove(x + width / 2, y + height / 2); }

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

	@Override
	public void mousePressed(MouseEvent arg0) {
		if (arg0.getButton() == MouseEvent.BUTTON1) if (PolygonOver != null) PolygonOver.seeThrough = false;
		if (arg0.getButton() == MouseEvent.BUTTON3) if (PolygonOver != null) PolygonOver.seeThrough = true;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		if (arg0.getUnitsToScroll() > 0) if (zoom > minZoom) zoom -= 25 * arg0.getUnitsToScroll();
		else if (zoom < maxZoom) zoom -= 25 * arg0.getUnitsToScroll();
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mouseReleased(MouseEvent arg0) {}
}
