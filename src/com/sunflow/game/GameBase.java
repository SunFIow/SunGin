package com.sunflow.game;

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
import java.util.Random;

import com.sunflow.gfx.SImage;
import com.sunflow.math.OpenSimplexNoise;
import com.sunflow.math.Vertex2F;

abstract class GameBase extends SImage implements MouseListener, MouseWheelListener, MouseMotionListener, KeyListener, ComponentListener {

	// RADIANDS or DEGREES
	protected byte mode;

	protected Random random;
//	private ImprovedNoise perlinnoise;
	private OpenSimplexNoise noise;

	protected GameBase game;

	public GameBase() { init(); }

	protected void init() {
		game = this;
		privatePreSetup();
		preSetup();

		setup();
	}

	void privatePreSetup() {
		random = new Random();
		noise = new OpenSimplexNoise(random.nextLong());
		mode(RADIANS);
	}

	protected void preSetup() {}

	protected void setup() {}

	@Override
	protected void defaultSettings() { super.defaultSettings(); }

	protected abstract int x();

	protected abstract int y();

	protected abstract int frameX();

	protected abstract int frameY();

	final public static Vertex2F createVector() {
		return createVector(0, 0);
	}

	final public static Vertex2F createVector(float x, float y) {
		return new Vertex2F(x, y);
	}

	final public static SImage createImage(float width, float height) {
		return new SImage(width, height);
	}

	final public static SImage createImage(float width, float height, int format) {
		return new SImage(width, height, format);
	}

	final public static SImage createImage(BufferedImage bi) {
		return new SImage(bi);
	}

	/**
	 * @param mode
	 *            either RADIANDS or DEGREES
	 */
	final public void mode(byte mode) {
		this.mode = mode;
	}

	@Override
	public double sin(double angle) {
		return super.sin(mode == RADIANS ? angle : radians(angle));
	}

	@Override
	public float sin(float angle) {
		return super.sin(mode == RADIANS ? angle : radians(angle));
	}

	@Override
	public double cos(double angle) {
		return super.cos(mode == RADIANS ? angle : radians(angle));
	}

	@Override
	public float cos(float angle) {
		return super.cos(mode == RADIANS ? angle : radians(angle));
	}

	final public double noise(double xoff) { return noise.eval(xoff, 0); }

	final public double noise(double xoff, double yoff) { return noise.eval(xoff, yoff); }

	final public double noise(double xoff, double yoff, double zoff) { return noise.eval(xoff, yoff, zoff); }

	final public double noise(double xoff, double yoff, double zoff, double woff) { return noise.eval(xoff, yoff, zoff, woff); }

	/**
	 * Simple utility function to
	 * save an Serializable obj to a file
	 * adds "rec/" to the fileName
	 * if it contains no "/".
	 */
	final public static void serialize(String fileName, Serializable obj) {
		serialize(getFile(fileName), obj);
	}

	/**
	 * Simple utility function to
	 * save an Serializable obj to a file
	 */
	final public static void serialize(File file, Serializable obj) {
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
	final public static Serializable deserialize(String fileName) {
		return deserialize(getFile(fileName));
	}

	/**
	 * Simple utility function to
	 * load an Serializable obj from file
	 */
	final public static Serializable deserialize(File file) {
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
