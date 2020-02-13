package com.sunflow.tutorial;

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

import com.sunflow.math3d.Vertex3D;

public class TutorialGame3D extends JPanel implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	public static void main(String[] args) {
		Dimension desktopSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension screenSize = new Dimension(desktopSize.width, desktopSize.height);

		new TutorialGame3D(screenSize.width / 2, screenSize.height / 2);
	}

	// ArrayList of all the 3D polygons - each 3D polygon has a 2D 'PolygonObject' inside called 'DrawablePolygon'
	public ArrayList<IModel> Models = new ArrayList<IModel>();
	public ArrayList<DPolygon> DPolygons = new ArrayList<DPolygon>();

	// The polygon that the mouse is currently over
	public PolygonObject PolygonOver = null;

	public Vertex3D vCameraPos = new Vertex3D(15, 5, 10);
	public Vertex3D vCameraDir = new Vertex3D(0, 0, 0);

	public Vertex3D vLightDir = new Vertex3D(1, 1, 1);

	// The smaller the zoom the more zoomed out you are and visa versa, although altering too far from 1000 will make it look pretty weird
	public static double zoom = 1000;

	private double minZoom = 500;
	private double maxZoom = 2500;

	// Used for keeping mouse in center
	private Robot r;
	@SuppressWarnings("unused")
	private double mouseX = 0;
	@SuppressWarnings("unused")
	private double mouseY = 0;

	private double movementSpeed = 0.2;

	// FPS is a bit primitive, you can set the MaxFPS as high as u want
	private double drawFPS = 0, MaxFPS = 1000, LastRefresh = 0, LastFPSCheck = 0, Checks = 0;

	@SuppressWarnings("unused")
	private double SleepTime = 1000.0 / MaxFPS, StartTime = System.currentTimeMillis();

	// VertLook goes from 0.999 to -0.999, minus being looking down and + looking up, HorLook takes any number and goes round in radians
	// aimSight changes the size of the center-cross. The lower HorRotSpeed or VertRotSpeed, the faster the camera will rotate in those directions
	private double vertLook = -0.9, horLook = 0, aimSight = 4, horRotSpeed = 900, vertRotSpeed = 2200, sunPos = 0;

	// will hold the order that the polygons in the ArrayList DPolygon should be drawn meaning DPolygon.get(NewOrder[0]) gets drawn first
	private int[] newOrder;

	public boolean outlines = true;
	private boolean[] keys = new boolean[4];

	public int x, y, width, height;

	public TutorialGame3D(int width, int height) {
		this.width = width;
		this.height = height;

		addKeyListener(this);
		setFocusable(true);

		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);

		invisibleMouse();

		Models.add(new Cube(this, 0, -5, 0, 2, 2, 2, Color.red));
		Models.add(new Cube(this, 18, -5, 0, 2, 2, 2, Color.red));
		Models.add(new Cube(this, 20, -5, 0, 2, 2, 2, Color.red));
		Models.add(new Cube(this, 22, -5, 0, 2, 2, 2, Color.red));
		Models.add(new Cube(this, 20, -5, 2, 2, 2, 2, Color.red));

		new GenerateTerrain(this);
//		Models.add(new com.sunflow.math3d.models.Cube(this, 0, -5, 0, 2, 2, 2, Color.red));
//		Models.add(new Cube(this, 18, -5, 0, 2, 2, 2, Color.red));
//		Models.add(new Cube(this, 20, -5, 0, 2, 2, 2, Color.red));
//		Models.add(new Cube(this, 22, -5, 0, 2, 2, 2, Color.red));
//		Models.add(new Cube(this, 20, -5, 2, 2, 2, 2, Color.red));
//		com.sunflow.math3d.models.Cube

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
	public void paintComponent(Graphics g) {
		// Clear screen and draw background color
		g.setColor(new Color(140, 180, 180));
		g.fillRect(0, 0, width, height);

		cameraMovement();

		// Calculated all that is general for this camera position
		Calculator.SetPrederterminedInfo(this);

		controlSunAndLight();

		// rotate and update shape examples
		Models.get(0).rotate(0.005);

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
		g.drawString("FPS: " + (int) drawFPS + " (Benchmark)", 40, 40);

		SleepAndRefresh();
	}

	private void setOrder() {
		DPolygons.clear();

		for (IModel m : Models) {
			if (m instanceof DPolygon) {
				DPolygons.add((DPolygon) m);
			} else if (m instanceof IModel) {
				for (DPolygon dp : m.polys) {
					DPolygons.add(dp);
				}
			}
		}
		double[] k = new double[DPolygons.size()];
		newOrder = new int[DPolygons.size()];

		for (int i = 0; i < DPolygons.size(); i++) {
			k[i] = DPolygons.get(i).avgDist;
			newOrder[i] = i;
		}

		double temp;
		int tempr;
		for (int a = 0; a < k.length - 1; a++)
			for (int b = 0; b < k.length - 1; b++)
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
		long timeSLU = (long) (System.currentTimeMillis() - LastRefresh);

		Checks++;
		if (Checks >= 15) {
			drawFPS = Checks / ((System.currentTimeMillis() - LastFPSCheck) / 1000.0);
			LastFPSCheck = System.currentTimeMillis();
			Checks = 0;
		}

		if (timeSLU < 1000.0 / MaxFPS) {
			try {
				Thread.sleep((long) (1000.0 / MaxFPS - timeSLU));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		LastRefresh = System.currentTimeMillis();

		repaint();
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

		if (keys[0]) moveVector.add(viewVector.x, viewVector.y, viewVector.z);
		if (keys[2]) moveVector.sub(viewVector.x, viewVector.y, viewVector.z);
		if (keys[1]) moveVector.add(sideViewVector.x, sideViewVector.y, sideViewVector.z);
		if (keys[3]) moveVector.sub(sideViewVector.x, sideViewVector.y, sideViewVector.z);

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

	private void setPolygonOver() {
		PolygonOver = null;
		for (int i = newOrder.length - 1; i >= 0; i--) {
			DPolygon current = DPolygons.get(newOrder[i]);
			if (current.draw && current.drawablePolygon.visible && current.drawablePolygon.MouseOver()) {
				PolygonOver = DPolygons.get(newOrder[i]).drawablePolygon;
				break;
			}
		}
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
		try {
			r = new Robot();
			r.mouseMove(x + width / 2, y + height / 2);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

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

	@Override
	public void keyTyped(KeyEvent e) {}

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
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {
		if (arg0.getButton() == MouseEvent.BUTTON1)
			if (PolygonOver != null)
				PolygonOver.seeThrough = false;

		if (arg0.getButton() == MouseEvent.BUTTON3)
			if (PolygonOver != null)
				PolygonOver.seeThrough = true;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		if (arg0.getUnitsToScroll() > 0) {
			if (zoom > minZoom)
				zoom -= 25 * arg0.getUnitsToScroll();
		} else {
			if (zoom < maxZoom)
				zoom -= 25 * arg0.getUnitsToScroll();
		}
	}
}
