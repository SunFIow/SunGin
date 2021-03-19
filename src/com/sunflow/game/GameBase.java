package com.sunflow.game;

import java.awt.AWTException;
import java.awt.Graphics2D;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sunflow.Settings;
import com.sunflow.engine.Mouse;
import com.sunflow.engine.eventsystem.EventManager;
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
import com.sunflow.engine.eventsystem.listeners.EventListener;
import com.sunflow.engine.eventsystem.listeners.KeyInputListener;
import com.sunflow.engine.eventsystem.listeners.MouseInputListener;
import com.sunflow.engine.eventsystem.listeners.ScrollListener;
import com.sunflow.engine.eventsystem.listeners.WindowMoveListener;
import com.sunflow.engine.eventsystem.listeners.WindowResizeListener;
import com.sunflow.engine.screen.Screen;
import com.sunflow.engine.screen.ScreenJava;
import com.sunflow.engine.screen.ScreenOpenGL;
import com.sunflow.gfx.SGraphics;
import com.sunflow.interfaces.FrameLoopListener;
import com.sunflow.interfaces.GameLoopListener;
import com.sunflow.logging.Log;
import com.sunflow.math.OpenSimplexNoise;
import com.sunflow.math.SVector;
import com.sunflow.util.Constants;
import com.sunflow.util.GameUtils;
import com.sunflow.util.GeometryUtils;
import com.sunflow.util.MathUtils;

public abstract class GameBase extends SGraphics implements Runnable,
		Constants, MathUtils, GameUtils, GeometryUtils,
		MouseListener,
		MouseWheelListener, MouseMotionListener, KeyListener, ComponentListener,
		KeyInputListener, MouseInputListener, com.sunflow.engine.eventsystem.listeners.MouseMotionListener, ScrollListener, WindowResizeListener, WindowMoveListener {

	public static Settings settings = new Settings().defaultSettings();

//	protected GameBase game;

	protected boolean noLoop;
	public boolean isRunning;
	public boolean isPaused;

	private Thread thread;

	protected Screen screen;
	protected float width, height;
	protected Mouse mouse;
	protected float mouseX, mouseY;

	private long startTime;

	// SYNC or ASYNC
	protected byte syncMode;

	protected int fps, tps;

	protected int frameRate, tickRate;
	protected long frameCount, tickCount;
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
	private OpenSimplexNoise noise;

	private Random random;

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
		defaultSettings();
		screen.show();
		screen.requestFocus();

		this.width = width;
		this.height = height;
	}

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

		for (GameLoopListener gll : gameLoopListeners) gll.update();
		update();

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
		handleSmooth();
		for (FrameLoopListener fll : frameLoopListeners) fll.update();
		screen.preDraw();
	}

	protected void postDraw() { screen.postDraw(); }

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
			render();
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
					if (screen.isCreated()) // TODO: USE STH ELSE
						render();
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

	public final void addListener(EventListener l) {
		if (l instanceof GameLoopListener) gameLoopListeners.add((GameLoopListener) l);
		if (l instanceof FrameLoopListener) frameLoopListeners.add((FrameLoopListener) l);
		EventManager.addEventListener(l);

	}

	public final void removeListener(EventListener l) {
		if (l instanceof GameLoopListener) gameLoopListeners.remove(l);
		if (l instanceof FrameLoopListener) frameLoopListeners.remove(l);
		EventManager.removeEventListener(l);
	}

	public final void showOverlay(boolean show) { screen.showOverlay(show); }

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

	public final void saveFrame(String fileName) {
		for (int i = 10; i > 0; i--) {
			String s = "";
			for (int j = 0; j < i; j++) s += "#";

			if (fileName.contains(s)) {
				String f = String.format("%0" + i + "d", frameCount);
				saveImage(image, fileName.replace(s, f));
				return;
			}
		}
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

	public static void UncaughtException(Throwable e) {
		Log.log(Log.FATAL, "Uncaught Exception", e);
		System.exit(1);
	}
}
