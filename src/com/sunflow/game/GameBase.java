package com.sunflow.game;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.sunflow.gfx.SImage;
import com.sunflow.math.Vertex2F;

abstract class GameBase extends SImage implements MouseListener, MouseWheelListener, MouseMotionListener, KeyListener, ComponentListener {

//	protected int x, y;
	public float width;
	public float height;

	protected SImage overlay;

	public int width() { return (int) width; }

	public int height() { return (int) height; }

	public int mouseX() { return (int) mouseX; }

	public int mouseY() { return (int) mouseY; }

	protected int frameWidth, frameHeight;

	public float mouseX, mouseY;
	public float prevMouseX, prevMouseY;
	public float mouseScreenX, mouseScreenY;

	protected float aimSize;
	protected Color aimColor;

	protected Robot robot;

	public GameBase() { init(); }

	protected void init() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		aimSize = 8;
		aimColor = Color.black;
	}

	@Override
	protected void defaultSettings() {
		super.width = width();
		super.height = height();
		super.format = RGB;
		super.defaultSettings();
	}

	protected abstract int x();

	protected abstract int y();

	protected abstract int frameX();

	protected abstract int frameY();

	public final static SImage createImage(float width, float height) {
		return new SImage(width, height);
	}

	public final static SImage createImage(float width, float height, int format) {
		return new SImage(width, height, format);
	}

	public final static SImage createImage(BufferedImage bi) {
		return new SImage(bi);
	}

	/**
	 * Simple utility function to
	 * save an Serializable obj to a file
	 * adds "rec/" to the fileName
	 * if it contains no "/".
	 */
	public final static void serialize(String fileName, Serializable obj) {
		serialize(getFile(fileName), obj);
	}

	/**
	 * Simple utility function to
	 * save an Serializable obj to a file
	 */
	public final static void serialize(File file, Serializable obj) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(obj);
			oos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Simple utility function to
	 * load an Serializable obj from file
	 * adds "rec/" to the fileName
	 * if it contains no "/".
	 */
	public final static Serializable deserialize(String fileName) {
		return deserialize(getFile(fileName));
	}

	/**
	 * Simple utility function to
	 * load an Serializable obj from file
	 */
	public final static Serializable deserialize(File file) {
		Serializable obj = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);

			try {
				while (true) {
					Object o = ois.readObject();
					obj = (Serializable) o;
				}
			} catch (EOFException e) {} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			ois.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return obj;
	}

	/*
	 * Simple utility function which
	 * adds "rec/" to the fileName
	 * if it contains no "/".
	 */
	private final static File getFile(String name) {
		if (!name.contains("/")) name = "rec/".concat(name);
		return new File(name);
	}

	public final void drawCrosshair() {
		int aimStrokeWidth = (Math.round(aimSize / 8));
		int aimStroke = aimStrokeWidth * 2;
		overlay.noStroke();
		overlay.fill(aimColor.getRGB());
		overlay.rect((int) (mouseX - aimSize), mouseY - aimStrokeWidth, (int) (aimSize * 2), aimStroke);
		overlay.rect(mouseX - aimStrokeWidth, (int) (mouseY - aimSize), aimStroke, (int) (aimSize * 2));
	}

	public final void moveMouse(Vertex2F v) {
		moveMouse(v.x(), v.y());
	}

	public final void moveMouse(float x, float y) {
		moveMouseTo(mouseX + x, mouseY + y);
	}

	public final void moveMouseTo(Vertex2F v) {
		moveMouseTo(v.x, v.y);
	}

	public void moveMouseTo(float x, float y) {
		robot.mouseMove((int) (x() + x), (int) (y() + y));
	}

	public final void moveMouseOnScreen(Vertex2F v) {
		moveMouseOnScreen(v.x, v.y);
	}

	public final void moveMouseOnScreen(float x, float y) {
		moveMouseOnScreenTo(mouseScreenX + x, mouseScreenY + y);
	}

	public final void moveMouseOnScreenTo(Vertex2F v) {
		moveMouseOnScreenTo(v.x, v.y);
	}

	public final void moveMouseOnScreenTo(float x, float y) {
		robot.mouseMove((int) x, (int) y);
	}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {}

	@Override
	public void mouseMoved(MouseEvent e) {}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {}

	@Override
	public void componentResized(ComponentEvent e) {}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentShown(ComponentEvent e) {}

	@Override
	public void componentHidden(ComponentEvent e) {}

}
