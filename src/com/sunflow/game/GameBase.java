package com.sunflow.game;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.Image;
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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import com.sunflow.Settings;
import com.sunflow.engine.Mouse;
import com.sunflow.engine.eventsystem.events.KeyInputEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyPressedEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyReleasedEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyRepeatedEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent.MousePressedEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent.MouseReleasedEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent.MouseDraggedEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent.MouseMovedEvent;
import com.sunflow.engine.eventsystem.events.ScrollEvent;
import com.sunflow.engine.eventsystem.events.WindowMoveEvent;
import com.sunflow.engine.eventsystem.events.WindowResizeEvent;
import com.sunflow.engine.eventsystem.listeners.KeyInputListener;
import com.sunflow.engine.eventsystem.listeners.MouseInputListener;
import com.sunflow.engine.eventsystem.listeners.SEventListener;
import com.sunflow.engine.eventsystem.listeners.ScrollListener;
import com.sunflow.engine.eventsystem.listeners.WindowMoveListener;
import com.sunflow.engine.eventsystem.listeners.WindowResizeListener;
import com.sunflow.engine.screen.Screen;
import com.sunflow.engine.screen.ScreenJava;
import com.sunflow.engine.screen.ScreenOpenGL;
import com.sunflow.gfx.SGraphics;
import com.sunflow.gfx.SImage;
import com.sunflow.interfaces.FrameLoopListener;
import com.sunflow.interfaces.GameLoopListener;
import com.sunflow.logging.Log;
import com.sunflow.math.OpenSimplexNoise;
import com.sunflow.math.SVector;
import com.sunflow.util.GameUtils;
import com.sunflow.util.GeometryUtils;
import com.sunflow.util.LogUtils;
import com.sunflow.util.MathUtils;
import com.sunflow.util.SConstants;

public abstract class GameBase extends SGraphics implements Runnable,
		SConstants, MathUtils, GameUtils, GeometryUtils, LogUtils,
		MouseListener,
		MouseWheelListener, MouseMotionListener, KeyListener, ComponentListener,
		KeyInputListener, MouseInputListener, com.sunflow.engine.eventsystem.listeners.MouseMotionListener, ScrollListener, WindowResizeListener, WindowMoveListener {

	public static Settings settings = new Settings().defaultSettings();

//	protected GameBase game;

	protected boolean noLoop;
	public boolean isRunning;
	public boolean isPaused;

	private Thread thread;

	String renderer = JAVA2D;
	String outputPath;

	protected Screen screen;
	protected float width, height;
	protected Mouse mouse;
	protected float mouseX, mouseY;

	private long startTime;

	// SYNC or ASYNC
	protected byte syncMode;

	protected int fps, tps;

	public int frameRate, tickRate;
	public int frameCount, tickCount;
	int frames, ticks;

	private float timePerTickNano, timePerTickMilli;
	private float timePerFrameNano, timePerFrameMilli;

	protected float delta, deltaMin;
	protected float multiplier, multiplierMin;

	protected float fElapsedTime, fElapsedTimeMin;

//	protected float aimSize;
//	protected Color aimColor;

	protected ArrayList<GameLoopListener> gameLoopListeners;
	protected ArrayList<FrameLoopListener> frameLoopListeners;

//	private ImprovedNoise perlinnoise;
	protected OpenSimplexNoise noise;

	protected Random random;

	private Robot robot;

	public List<String> infos;

	public GameBase() {
		if (this instanceof Game3D) info("starting 3D Game");
		else if (this instanceof GameBase) info("starting 2D Game");
		else info("starting Custom Game");

		if (settings.autostart) start();
	}

	final public void reset() {
		stop();
		privateRefresh();
		refresh();
		init();
	}

	void privatePreSetup() {
//		game = this;
		infos = getInfo();
		random = new Random();
		noise = new OpenSimplexNoise(random.nextLong());
		mouse = new Mouse();

		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
//		aimSize = 8;
//		aimColor = Color.black;

		frameRate(60);
		syncMode(SYNC);

		multiplierMin = 5;
		deltaMin = timePerTickNano / NANOSECOND * multiplierMin;
		fElapsedTimeMin = timePerFrameNano / NANOSECOND * multiplierMin;

		gameLoopListeners = new ArrayList<>();
		frameLoopListeners = new ArrayList<>();
		startTime = System.currentTimeMillis();

		if (settings.screentype == Settings.ScreenType.OPENGL) screen = new ScreenOpenGL(this, mouse);
		else screen = new ScreenJava(this, mouse);
	}

	protected void preSetup() {}

	protected void setup() {}

	void privateRefresh() {
		frameCount = 0;
		frameRate = 0;
		tickCount = 0;
		tickRate = 0;

		multiplierMin = 0;
		deltaMin = 0;

		width = 0;
		height = 0;

		fps = 0;

		screen.refresh();
	}

	protected void refresh() {}

	protected void start() {
		if (isRunning) return;
		isRunning = true;

		thread = new Thread(this, "MainThread");
//		thread.start();
		thread.run();
	}

	public final void exit() { stop(); }

	public final void stop() {
		if (!isRunning) return;
		isRunning = false;
	}

	public final void createCanvas(float width, float height) { createCanvas(width, height, 1, 1); }

	public final void createCanvas(float width, float height, float scale) { createCanvas(width, height, scale, scale); }

	public void createCanvas(float width, float height, float scaleW, float scaleH) {
		screen.createCanvas(width, height, scaleW, scaleH);

		setParent(this);
		setPrimary(true);
		setSize(width(), height());
		graphics = checkImage();

		defaultSettings();
		screen.show();
		screen.requestFocus();

		this.width = width;
		this.height = height;
	}

	@Override
	public boolean external() { return false; }

	public final void title(String title) { screen.setTitle(title); }

	public final void undecorated(boolean undecorated) { screen.setUndecorated(undecorated); }

	@Override
	public void run() {
		init();
		loop();
		destroy();
	}

	void init() {
		privatePreSetup();
		preSetup();

		setup();

		thread.setName(screen.title + " MainThread");
		screen.show();
	}

	void tick() {
		screen.privateUpdate();

		width = screen.width;
		height = screen.height;
		mouseX = mouse.x;
		mouseY = mouse.y;

		for (GameLoopListener gll : gameLoopListeners) gll.preUpdate();
		update();
		for (GameLoopListener gll : gameLoopListeners) gll.postUpdate();

		ticks++;
		tickCount++;
	}

	protected void update() {}

//		if (!isRunning) return;
	private void render() {
		preDraw();

		push();
		draw();
		draw(graphics);
		pop();

		postDraw();

		if (!screen.render()) return;

		frames++;
		frameCount++;
	}

	protected void preDraw() {
		super.beginDraw();
//		graphics = checkImage();
//		handleSmooth();
		for (FrameLoopListener fll : frameLoopListeners) fll.preDraw();
		screen.preDraw();
		for (FrameLoopListener fll : frameLoopListeners) fll.postDraw();
	}

	protected void postDraw() { super.endDraw(); screen.postDraw(); }

	protected void draw() {}

	protected void draw(Graphics2D g) {}

	protected void info() {
		tps = ticks;
		ticks = 0;
		fps = frames;
		frames = 0;
		infos = getInfo();
//		frame.setTitle(title + " - FPS : " + fps);
		screen.setTitleInfo(" - FPS : " + fps);
	}

	void loop() {
		long currentTime;
		long previousTime = System.nanoTime();
		long passedTime;

		long timeSinceLastTick = 0;
		long timeSinceLastFrame = 0;
		long timeSinceLastInfoUpdate = 0;

		long lastTick = System.nanoTime();
		long lastFrame = System.nanoTime();

		if (noLoop) {
			if (!isPaused) tick();
			if (screen.isCreated()) render();
			return;
		}
		while (isRunning) {
			currentTime = System.nanoTime();
			passedTime = currentTime - previousTime;
			previousTime = currentTime;

			timeSinceLastTick += passedTime;
			timeSinceLastFrame += passedTime;
			timeSinceLastInfoUpdate += passedTime;

			if (timeSinceLastTick >= timePerTickNano) {
				timeSinceLastTick -= timePerTickNano;
				delta = (currentTime - lastTick) / (float) NANOSECOND;
				delta = Math.min(delta, deltaMin);
				multiplier = (currentTime - lastTick) / timePerTickNano;
				multiplier = Math.min(multiplier, multiplierMin);
				lastTick = currentTime;

				if (!isPaused) if (syncMode == ASYNC) tick();
			}

			if (timeSinceLastFrame >= timePerFrameNano) {
				timeSinceLastFrame -= timePerFrameNano;
				fElapsedTime = (currentTime - lastFrame) / (float) NANOSECOND;
				fElapsedTime = Math.min(fElapsedTime, fElapsedTimeMin);
				lastFrame = currentTime;

				if (syncMode == ASYNC) render();
				else {
					if (!isPaused) tick();
					if (screen.isCreated()) render(); // TODO: USE STH ELSE
				}
			}

			if (timeSinceLastInfoUpdate >= NANOSECOND) {
				info();
				timeSinceLastInfoUpdate -= NANOSECOND;
			}
		}
	}

	void destroy() {}

	public List<String> getInfo() {
		List<String> info = new ArrayList<>();
		info.add(fps + " FPS");
		info.add(tps + " TPS");
		return info;
	}

//	void updateMousePosition(float x, float y) {
//		screen.updateMousePosition(x,y);
//	}

	public final void noLoop() {
		noLoop = true;
		isRunning = false;
		timePerFrameNano = MAX_FLOAT;
	}

	@Override
	protected void defaultSettings() {
		screen.defaultSettings();

		showOverlay(false);
		showInfo(false);
		isPaused = false;

		super.width = width();
		super.height = height();
		super.format = RGB;

		super.defaultSettings();

//		noSmooth();
		smooth();
	}

	public final void tickRate(int newTarget) {
		tickRate = newTarget;
		timePerTickNano = newTarget < 1 ? 0 : NANOSECOND / tickRate;
		timePerTickMilli = newTarget < 1 ? 0 : MILLISECOND / tickRate;
		if (syncMode == SYNC) {
			frameRate = tickRate;
			timePerFrameNano = timePerTickNano;
			timePerFrameMilli = timePerTickMilli;
		}
	}

	public final void frameRate(int newTarget) {
		frameRate = newTarget;
		timePerFrameNano = newTarget < 1 ? 0 : NANOSECOND / frameRate;
		timePerFrameMilli = newTarget < 1 ? 0 : MILLISECOND / frameRate;
		if (syncMode == SYNC) {
			tickRate = frameRate;
			timePerTickNano = timePerFrameNano;
			timePerTickMilli = timePerFrameMilli;
		}
	}

	/**
	 * @param mode
	 *            either SYNC or ASYNC
	 */
	public final void syncMode(byte mode) { this.syncMode = mode; }

	public final boolean addListener(SEventListener listener) {
		if (listener instanceof GameLoopListener) gameLoopListeners.add((GameLoopListener) listener);
		if (listener instanceof FrameLoopListener) frameLoopListeners.add((FrameLoopListener) listener);
		return screen.addListener(listener);

	}

	public final boolean removeListener(SEventListener listener) {
		if (listener instanceof GameLoopListener) gameLoopListeners.remove(listener);
		if (listener instanceof FrameLoopListener) frameLoopListeners.remove(listener);
		return screen.addListener(listener);
	}

	public final boolean addListener(EventListener listener) { return screen.addListener(listener); }

	public final boolean removeListener(EventListener listener) { return screen.removeListener(listener); }

	public final void showOverlay(boolean show) { screen.showOverlay(show); }

	public final void infoSize(float size) { screen.infoSize(size); }

	public final void showInfo(boolean show) { screen.showInfo(show); }

	public final void showCrosshair(boolean show) { screen.showCrosshair(show); }

	public final long millis() { return System.currentTimeMillis() - startTime; }

	public final float noise(float xoff) { return (float) noise.eval(xoff, 0); }

	public final double noise(double xoff) { return noise.eval(xoff, 0); }

	public final float noise(float xoff, float yoff) { return (float) noise.eval(xoff, yoff); }

	public final double noise(double xoff, double yoff) { return noise.eval(xoff, yoff); }

	public final float noise(float xoff, float yoff, float zoff) { return (float) noise.eval(xoff, yoff, zoff); }

	public final double noise(double xoff, double yoff, double zoff) { return noise.eval(xoff, yoff, zoff); }

	public final float noise(float xoff, float yoff, float zoff, float woff) { return (float) noise.eval(xoff, yoff, zoff, woff); }

	public final double noise(double xoff, double yoff, double zoff, double woff) { return noise.eval(xoff, yoff, zoff, woff); }

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
//		if (file.isAbsolute()) {
//			// make sure that the intermediate folders have been created
//			PApplet.createPath(file);
//		} else {
//			String msg = "PImage.save() requires an absolute path. " +
//					"Use createImage(), or pass savePath() to save().";
//			PGraphics.showException(msg);
//		}
		try {
			OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
			ObjectOutputStream oos = new ObjectOutputStream(os);

			oos.writeObject(obj);
			oos.close();
		} catch (IOException e) {
			System.err.println("Error while serializing (" + obj + ").");
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
			InputStream is = new BufferedInputStream(new FileInputStream(file));
			ObjectInputStream ois = new ObjectInputStream(is);
			obj = (Serializable) ois.readObject();

			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return obj;
	}

	/**
	 * Simple utility function to
	 * load an Serializable obj from file
	 */
	public final static Serializable[] deserializeToArray(File file) {
		List<Serializable> objs = new ArrayList<>();

		ObjectInputStream ois = null;
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(file));
			ois = new ObjectInputStream(is);

			while (true) objs.add((Serializable) ois.readObject());

		} catch (EOFException e) {
			if (ois != null) try {
				ois.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		return objs.toArray(new Serializable[0]);
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

	public final void moveMouse(SVector v) {
		moveMouse(v.x(), v.y());
	}

	public final void moveMouse(float x, float y) {
		moveMouseTo(mouse.x + x, mouse.y + y);
	}

	public final void moveMouseTo(SVector v) {
		moveMouseTo(v.x, v.y);
	}

	public final void moveMouseTo(float x, float y) {
		robot.mouseMove((int) (x() + x), (int) (y() + y));
	}

	public final void moveMouseOnScreen(SVector v) {
		moveMouseOnScreen(v.x, v.y);
	}

	public final void moveMouseOnScreen(float x, float y) {
		moveMouseOnScreenTo(mouse.screenX + x, mouse.screenY + y);
	}

	public final void moveMouseOnScreenTo(SVector v) {
		moveMouseOnScreenTo(v.x, v.y);
	}

	public final void moveMouseOnScreenTo(float x, float y) {
		robot.mouseMove((int) x, (int) y);
	}

	@Override
	public final boolean save(String fileName) { return saveFrame(fileName); }

	public boolean saveFrame() {
		try {
//			saveImage(image, savePath("screen-" + nf(frameCount, 4) + ".tif"));
//			graphics.save(savePath("screen-" + nf(frameCount, 4) + ".tif"));
			return save(savePath("screen-" + nf(frameCount, 4) + ".tif"));
		} catch (SecurityException se) {
			System.err.println("Can't use saveFrame() when running in a browser, " +
					"unless using a signed applet.");
			return false;
		}
	}

	public boolean saveFrame(String fileName) {
		try {
//			saveImage(image, fileName.replace(s, f));
//			graphics.save(savePath(insertFrame(fileName)));
			return save(savePath(insertFrame(fileName)));
		} catch (SecurityException se) {
			System.err.println("Can't use saveFrame() when running in a browser, " +
					"unless using a signed applet.");
			return false;
		}
	}

	/**
	 * Check a string for #### signs to see if the frame number should be
	 * inserted. Used for functions like saveFrame() and beginRecord() to
	 * replace the # marks with the frame number. If only one # is used,
	 * it will be ignored, under the assumption that it's probably not
	 * intended to be the frame number.
	 */
	public String insertFrame(String what) {
		int first = what.indexOf('#');
		int last = what.lastIndexOf('#');

		if ((first != -1) && (last - first > 0)) {
			String prefix = what.substring(0, first);
			int count = last - first + 1;
			String suffix = what.substring(last + 1);
			return prefix + nf(frameCount, count) + suffix;
		}
		return what; // no change
	}

	public final float elapsedTime() { return delta(); }

	public final float delta() { return delta; }

	public final int x() { return screen.getX(); }

	public final int y() { return screen.getY(); }

	public final int frameX() { return screen.getScreenX(); }

	public final int frameY() { return screen.getScreenY(); }

	public final int width() { return screen.getWidth(); }

	public final int height() { return screen.getHeight(); }

	public final int mouseX() { return (int) mouse.x; }

	public final int mouseY() { return (int) mouse.y; }

	public final int lastMouseX() { return (int) mouse.lastX; }

	public final int lastMouseY() { return (int) mouse.lastY; }

	public final boolean keyIsDown(int key) { return screen.keyIsDown(key); }

	public final boolean keyIsPressed(int key) { return screen.keyIsPressed(key); }

	public final boolean keyIsHeld(int key) { return screen.keyIsHeld(key); }

	public final boolean keyIsReleased(int key) { return screen.keyIsReleased(key); }

	public final boolean keyIsDown(char key) { return screen.keyIsDown(key); }

	public final boolean keyIsPressed(char key) { return screen.keyIsPressed(key); }

	public final boolean keyIsHeld(char key) { return screen.keyIsHeld(key); }

	public final boolean keyIsReleased(char key) { return screen.keyIsReleased(key); }

	public final boolean mouseIsDown(int button) { return screen.mouseIsDown(button); }

	public final boolean mouseIsPressed(int button) { return screen.mouseIsPressed(button); }

	public final boolean mouseIsHeld(int button) { return screen.mouseIsHeld(button); }

	public final boolean mouseIsReleased(int button) { return screen.mouseIsReleased(button); }

	public final double mouseWheel() { return screen.mouseWheel(); }

	public final boolean mousePressed() { return screen.mousePressed(); }

	public final char key() { return screen.key(); }

	public final int keyCode() { return screen.keyCode(); }

	public final boolean[] keys() { return screen.keys(); }

	void updateMousePosition(float x, float y) {}

	/**
	 * @return if default functionality should be skipped
	 */
	public boolean keyPressed() { return false; }

	/**
	 * @return if default functionality should be skipped
	 */
	public boolean keyReleased() { return false; }

	/**
	 * the character associated with the key in this event
	 * the integer keyCode associated with the key in this event
	 * 
	 * @return if default functionality should be skipped
	 */
	public boolean keyTyped() { return false; }

	public boolean mouseOnPressed() { return false; }

	public boolean mouseOnReleased() { return false; }

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

	// CUSTOM EVENT SYSTEM

	@Override
	public void onMoved(WindowMoveEvent event) {}

	@Override
	public void onResized(WindowResizeEvent event) {}

	@Override
	public void onScrolled(ScrollEvent event) {}

	@Override
	public void onMouseMotion(MouseMotionEvent event) {}

	@Override
	public void onMouseMoved(MouseMovedEvent event) {}

	@Override
	public void onMouseDragged(MouseDraggedEvent event) {}

	@Override
	public void onMouseInput(MouseInputEvent event) {}

	@Override
	public void onMousePressed(MousePressedEvent event) {}

	@Override
	public void onMouseReleased(MouseReleasedEvent event) {}

	@Override
	public void onKeyInput(KeyInputEvent event) {}

	@Override
	public void onKeyPressed(KeyPressedEvent event) {}

	@Override
	public void onKeyReleased(KeyReleasedEvent event) {}

	@Override
	public void onKeyRepeated(KeyRepeatedEvent event) {}

	@Override
	public void printStackTrace(Throwable e) { e.printStackTrace(); }

	public static void UncaughtException(Throwable e) {
		Log.log(Log.FATAL, "Uncaught Exception", e);
		System.exit(1);
	}

	public InputStream createInput(String filename) {
		InputStream input = createInputRaw(filename);
		if (input != null) {
			// if it's gzip-encoded, automatically decode
			final String lower = filename.toLowerCase();
			if (lower.endsWith(".gz") || lower.endsWith(".svgz")) {
				try {
					// buffered has to go *around* the GZ, otherwise 25x slower
					return new BufferedInputStream(new GZIPInputStream(input));

				} catch (IOException e) {
					printStackTrace(e);
				}
			} else {
				return new BufferedInputStream(input);
			}
		}
		return null;
	}

	public InputStream createInputRaw(String filename) {
		if (filename == null) return null;

		if (sketchPath == null) {
			System.err.println("The sketch path is not set.");
			throw new RuntimeException("Files must be loaded inside setup() or after it has been called.");
		}

		if (filename.length() == 0) {
			// an error will be called by the parent function
			// System.err.println("The filename passed to openStream() was empty.");
			return null;
		}

		// First check whether this looks like a URL
		if (filename.contains(":")) { // at least smells like URL
			try {
				URL url = new URL(filename);
				URLConnection conn = url.openConnection();

				if (conn instanceof HttpURLConnection) {
					HttpURLConnection httpConn = (HttpURLConnection) conn;
					// Will not handle a protocol change (see below)
					httpConn.setInstanceFollowRedirects(true);
					int response = httpConn.getResponseCode();
					// Default won't follow HTTP -> HTTPS redirects for security reasons
					// http://stackoverflow.com/a/1884427
					if (response >= 300 && response < 400) {
						String newLocation = httpConn.getHeaderField("Location");
						return createInputRaw(newLocation);
					}
					return conn.getInputStream();
				} else if (conn instanceof JarURLConnection) {
					return url.openStream();
				}
			} catch (MalformedURLException mfue) {
				// not a url, that's fine

			} catch (FileNotFoundException fnfe) {
				// Added in 0119 b/c Java 1.5 throws FNFE when URL not available.
				// http://dev.processing.org/bugs/show_bug.cgi?id=403

			} catch (IOException e) {
				// changed for 0117, shouldn't be throwing exception
				printStackTrace(e);
				// System.err.println("Error downloading from URL " + filename);
				return null;
				// throw new RuntimeException("Error downloading from URL " + filename);
			}
		}

		InputStream stream = null;

		// Moved this earlier than the getResourceAsStream() checks, because
		// calling getResourceAsStream() on a directory lists its contents.
		// http://dev.processing.org/bugs/show_bug.cgi?id=716
		try {
			// First see if it's in a data folder. This may fail by throwing
			// a SecurityException. If so, this whole block will be skipped.
			File file = new File(dataPath(filename));
			if (!file.exists()) {
				// next see if it's just in the sketch folder
				file = sketchFile(filename);
			}

			if (file.isDirectory()) {
				return null;
			}
			if (file.exists()) {
				try {
					// handle case sensitivity check
					String filePath = file.getCanonicalPath();
					String filenameActual = new File(filePath).getName();
					// make sure there isn't a subfolder prepended to the name
					String filenameShort = new File(filename).getName();
					// if the actual filename is the same, but capitalized
					// differently, warn the user.
					// if (filenameActual.equalsIgnoreCase(filenameShort) &&
					// !filenameActual.equals(filenameShort)) {
					if (!filenameActual.equals(filenameShort)) {
						throw new RuntimeException("This file is named " +
								filenameActual + " not " +
								filename + ". Rename the file " +
								"or change your code.");
					}
				} catch (IOException e) {}
			}

			// if this file is ok, may as well just load it
			stream = new FileInputStream(file);
			if (stream != null) return stream;

			// have to break these out because a general Exception might
			// catch the RuntimeException being thrown above
		} catch (IOException ioe) {} catch (SecurityException se) {}

		// Using getClassLoader() prevents java from converting dots
		// to slashes or requiring a slash at the beginning.
		// (a slash as a prefix means that it'll load from the root of
		// the jar, rather than trying to dig into the package location)
		ClassLoader cl = getClass().getClassLoader();

		// by default, data files are exported to the root path of the jar.
		// (not the data folder) so check there first.
		stream = cl.getResourceAsStream("data/" + filename);
		if (stream != null) {
			String cn = stream.getClass().getName();
			// this is an irritation of sun's java plug-in, which will return
			// a non-null stream for an object that doesn't exist. like all good
			// things, this is probably introduced in java 1.5. awesome!
			// http://dev.processing.org/bugs/show_bug.cgi?id=359
			if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {
				return stream;
			}
		}

		// When used with an online script, also need to check without the
		// data folder, in case it's not in a subfolder called 'data'.
		// http://dev.processing.org/bugs/show_bug.cgi?id=389
		stream = cl.getResourceAsStream(filename);
		if (stream != null) {
			String cn = stream.getClass().getName();
			if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {
				return stream;
			}
		}

		try {
			// attempt to load from a local file, used when running as
			// an application, or as a signed applet
			try { // first try to catch any security exceptions
				try {
					stream = new FileInputStream(dataPath(filename));
					if (stream != null) return stream;
				} catch (IOException e2) {}

				try {
					stream = new FileInputStream(sketchPath(filename));
					if (stream != null) return stream;
				} catch (Exception e) {} // ignored

				try {
					stream = new FileInputStream(filename);
					if (stream != null) return stream;
				} catch (IOException e1) {}

			} catch (SecurityException se) {} // online, whups

		} catch (Exception e) {
			printStackTrace(e);
		}

		return null;
	}

	public File sketchFile(String where) {
		return new File(sketchPath(where));
	}

	/**
	 * <b>This function almost certainly does not do the thing you want it to.</b>
	 * The data path is handled differently on each platform, and should not be
	 * considered a location to write files. It should also not be assumed that
	 * this location can be read from or listed. This function is used internally
	 * as a possible location for reading files. It's still "public" as a
	 * holdover from earlier code.
	 * <p>
	 * Libraries should use createInput() to get an InputStream or createOutput()
	 * to get an OutputStream. sketchPath() can be used to get a location
	 * relative to the sketch. Again, <b>do not</b> use this to get relative
	 * locations of files. You'll be disappointed when your app runs on different
	 * platforms.
	 */
	public String dataPath(String where) {
		return dataFile(where).getAbsolutePath();
	}

	/**
	 * Return a full path to an item in the data folder as a File object.
	 * See the dataPath() method for more information.
	 */
	public File dataFile(String where) {
		// isAbsolute() could throw an access exception, but so will writing
		// to the local disk using the sketch path, so this is safe here.
		File why = new File(where);
		if (why.isAbsolute()) return why;

		URL jarURL = getClass().getProtectionDomain().getCodeSource().getLocation();
		// Decode URL
		String jarPath;
		try {
			jarPath = jarURL.toURI().getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
		if (jarPath.contains("Contents/Java/")) {
			File containingFolder = new File(jarPath).getParentFile();
			File dataFolder = new File(containingFolder, "data");
			return new File(dataFolder, where);
		}
		// Windows, Linux, or when not using a Mac OS X .app file
		File workingDirItem = new File(sketchPath + File.separator + "data" + File.separator + where);
//		    if (workingDirItem.exists()) {
		return workingDirItem;
//		    }
//		    // In some cases, the current working directory won't be set properly.
	}

	public static Object subset(Object list, int start, int count) {
		Class<?> type = list.getClass().getComponentType();
		Object outgoing = Array.newInstance(type, count);
		System.arraycopy(list, start, outgoing, 0, count);
		return outgoing;
	}

	static public int[] expand(int list[]) {
		return expand(list, list.length > 0 ? list.length << 1 : 1);
	}

	static public int[] expand(int list[], int newSize) {
		int temp[] = new int[newSize];
		System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
		return temp;
	}

	static public float[] expand(float list[]) {
		return expand(list, list.length > 0 ? list.length << 1 : 1);
	}

	static public float[] expand(float list[], int newSize) {
		float temp[] = new float[newSize];
		System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
		return temp;
	}

	public static Object expand(Object array) {
		int len = Array.getLength(array);
		return expand(array, len > 0 ? len << 1 : 1);
	}

	static public Object expand(Object list, int newSize) {
		Class<?> type = list.getClass().getComponentType();
		Object temp = Array.newInstance(type, newSize);
		System.arraycopy(list, 0, temp, 0,
				Math.min(Array.getLength(list), newSize));
		return temp;
	}

	@Override
	public SGraphics makeGraphics(int width, int height, String renderer, String path, boolean primary) {
		SGraphics sg = GameUtils.super.makeGraphics(width, height, renderer, path, primary);
		sg.setParent(this);
		return sg;
	}

	protected SGraphics createPrimaryGraphics() { return makeGraphics(width(), height(), renderer, outputPath, true); }

//	protected SGraphics createGraphics(BufferedImage bi) { return new SGraphics(bi); }

	@Override
	public SImage createImage(int width, int height) { SImage img = GameUtils.super.createImage(width, height); img.parent = this; return img; }

	@Override
	public SImage createImage(int width, int height, int format) { SImage img = GameUtils.super.createImage(width, height, format); img.parent = this; return img; }

	@Override
	public SImage createImage(Image bi) { SImage img = GameUtils.super.createImage(bi); img.parent = this; return img; }

	@Override
	public SImage loadSImage(String fileName) { SImage img = GameUtils.super.loadSImage(fileName); img.parent = this; return img; }

//	protected SImage loadSImage(String fileName, int format) {
//		return new SImage(loadImage(fileName, format));
//	}

}
