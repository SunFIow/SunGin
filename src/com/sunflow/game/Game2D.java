package com.sunflow.game;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.JFrame;

import com.sunflow.gfx.SImage;
import com.sunflow.gfx.SShape;
import com.sunflow.interfaces.FrameLoopListener;
import com.sunflow.interfaces.GameLoopListener;
import com.sunflow.logging.Log;
import com.sunflow.math.Vertex2F;
import com.sunflow.util.Constants;
import com.sunflow.util.GameUtils;
import com.sunflow.util.GeometryUtils;
import com.sunflow.util.MathUtils;

public abstract class Game2D extends GameBase implements Constants, MathUtils, GameUtils, GeometryUtils { // , Runnable {

	public float width, height;

	private float scaleWidth, scaleHeight;
	private int scaledWidth, scaledHeight;

	protected int frameWidth, frameHeight;

	public float mouseX, mouseY;
	public float prevMouseX, prevMouseY;
	public float mouseScreenX, mouseScreenY;

	protected float aimSize;
	protected Color aimColor;

	protected Robot robot;

//	private Thread thread;
	private Thread threadTick;
	private Thread threadRender;

	protected JFrame frame;
	protected Canvas canvas;
	private BufferStrategy bs;

//	protected Screen screen;
//	protected boolean useCanvas;

	private boolean createdCanvas;

	// Overlay
//	protected DImage overlay;
	protected boolean showOverlay;
	protected boolean showInfo;
	protected boolean showCrosshair;

	protected boolean running;
	protected boolean paused;

	protected boolean noLoop;

	// SYNC or ASYNC
	protected byte syncMode;

	private float timePerTickNano, timePerTickMilli;
	private float timePerFrameNano, timePerFrameMilli;
	private float infoUpdateIntervall;

	protected int fps, tps;

	protected int frameRate, tickRate;
	protected long frameCount, tickCount;
	int frames, ticks;

	protected float delta, deltaMin;
	protected float multiplier, multiplierMin;

	protected boolean fullscreen;

	private Dimension savedSize;
	private Point savedPos;

	private String title;
	private boolean undecorated;
	private ArrayList<GameLoopListener> gameLoopListeners;
	private ArrayList<FrameLoopListener> frameLoopListeners;

	private long startTime;
	protected boolean mousePressed;

	protected SImage overlay;

//	public Game2D() {
//		super();
//	}

	@Override
	protected final void init() {
		if (this instanceof Game3D) info("starting 3D Game");
		else if (this instanceof Game2D) info("starting 2D Game");
		else info("starting Custom Game");

		super.init();
//		privatePreSetup();
//		preSetup();
//
//		setup();
		start();
	}

	final protected void reset() {
		stop();
		privateRefresh();
		refresh();
		init();
	}

	protected void refresh() {}

	void privateRefresh() {
		frame.dispose();
		frame = null;
		canvas = null;

		frameCount = 0;
		frameRate = 0;
		tickCount = 0;
		tickRate = 0;

		frameWidth = 0;
		frameHeight = 0;
		width = 0;
		height = 0;

		fullscreen = false;

		showInfo = true;

		mouseX = 0;
		mouseY = 0;
		mouseScreenX = 0;
		mouseScreenY = 0;

		createdCanvas = false;

		fps = 0;

		multiplierMin = 0;
		deltaMin = 0;
	}

	@Override
	void privatePreSetup() {
		super.privatePreSetup();

		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		aimSize = 8;
		aimColor = Color.black;

//		random = new Random();
//		noise = new OpenSimplexNoise(random.nextLong());
//		perlinnoise = new ImprovedNoise();
		frameRate(60);
		syncMode(SYNC);

		multiplierMin = 5;
		deltaMin = timePerFrameNano / NANOSECOND * multiplierMin;

		savedSize = new Dimension();
		savedPos = new Point(0, 0);

		gameLoopListeners = new ArrayList<>();
		frameLoopListeners = new ArrayList<>();
		startTime = System.currentTimeMillis();
	}

	void createFrame() {
		if (frame != null) frame.dispose();

		canvas = new Canvas();
		canvas.setFocusable(true);
//		canvas.setPreferredSize(new Dimension(width(), height()));
		canvas.setPreferredSize(new Dimension(scaledWidth, scaledHeight));
//		screen = new Screen(this);
//		screen.setFocusable(true);
//		screen.setPreferredSize(new Dimension(width(), height()));

		canvas.setIgnoreRepaint(true);

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if (title != null) frame.setTitle(title);
		frame.setUndecorated(undecorated);
		frame.add(canvas);
		frame.setVisible(true);
		frame.setVisible(false);
//		screen.addToFrame(frame);

		canvas.createBufferStrategy(3);
		bs = canvas.getBufferStrategy();

		frame.pack();
		frame.setLocationRelativeTo(null);

		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);
		canvas.addComponentListener(this);

		canvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				key = e.getKeyChar();
				keyCode = e.getKeyCode();
				keys[keyCode] = true;
				if (((Game2D) game).keyPressed()) return;
				switch (e.getKeyCode()) {
					case KeyEvent.VK_F5:
						reset();
						break;
					case KeyEvent.VK_F9:
						paused = !paused;
						break;
					case KeyEvent.VK_F11:
						toggleFullscreen();
						break;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				key = e.getKeyChar();
				keyCode = e.getKeyCode();
				keys[keyCode] = false;
				if (((Game2D) game).keyReleased()) return;
			}

			@Override
			public void keyTyped(KeyEvent e) {
				key = e.getKeyChar();
				keyCode = e.getKeyCode();
				if (((Game2D) game).keyTyped()) return;
			}
		});

		canvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				mousePressed = false;
			}

			@Override
			public void mousePressed(MouseEvent e) {
				mousePressed = true;
			}

			@Override
			public void mouseExited(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseClicked(MouseEvent e) {}
		});

		canvas.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) { updateMousePosition(e.getX(), e.getY()); }

			@Override
			public void mouseDragged(MouseEvent e) { updateMousePosition(e.getX(), e.getY()); }
		});

		canvas.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
//				cResized(e.getComponent().getWidth(), e.getComponent().getHeight());
				int w = e.getComponent().getWidth();
				int h = e.getComponent().getHeight();
				scaledWidth = w;
				scaledHeight = h;
				width = scaledWidth / scaleWidth;
				height = scaledHeight / scaleHeight;
				resize(width, height);
			}
		});

		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				frameWidth = e.getComponent().getWidth();
				frameHeight = e.getComponent().getHeight();
			}
		});
	}

	protected char key;
	protected int keyCode;

	/**
	 * the character associated with the key in this event
	 * the integer keyCode associated with the key in this event
	 * 
	 * @return if default functionality should be skipped
	 */
	protected boolean keyPressed() { return false; }

	/**
	 * the character associated with the key in this event
	 * the integer keyCode associated with the key in this event
	 * 
	 * @return if default functionality should be skipped
	 */
	protected boolean keyReleased() { return false; }

	/**
	 * the character associated with the key in this event
	 * the integer keyCode associated with the key in this event
	 * 
	 * @return if default functionality should be skipped
	 */
	protected boolean keyTyped() { return false; }

	private static boolean[] keys = new boolean[65536];

	protected boolean keyIsDown(char key) {
		return keyIsDown(KeyEvent.getExtendedKeyCodeForChar(key));
	}

	protected boolean keyIsDown(int key) { return keys[key]; }

	void updateMousePosition(int x, int y) {
		prevMouseX = mouseX;
		prevMouseY = mouseY;
		mouseX = x / scaleWidth;
		mouseY = y / scaleHeight;
	}

	final public void createCanvas(float width, float height) { createCanvas(width, height, 1, 1); }

	final public void createCanvas(float width, float height, float scale) { createCanvas(width, height, scale, scale); }

	final public void createCanvas(float width, float height, float scaleW, float scaleH) {
		if (!createdCanvas) {
			createdCanvas = true;
			this.width = width;
			this.height = height;
			this.scaleWidth = scaleW;
			this.scaleHeight = scaleH;

			this.scaledWidth = (int) (width * scaleWidth);
			this.scaledHeight = (int) (height * scaleHeight);

			createFrame();

			defaultSettings();
		}
		if (running) frame.setVisible(true);
		canvas.requestFocus();
//		screen.requestFocus();
	}

	final public void title(String title) {
		this.title = title;
		if (createdCanvas) frame.setTitle(title);
	}

	final public void undecorated(boolean undecorated) {
		if (createdCanvas && this.undecorated != undecorated) {
			boolean visible = frame.isVisible();
			frame.dispose();
			frame.setUndecorated(undecorated);
			frame.setVisible(visible);
		}
		this.undecorated = undecorated;
	}

	final public void noLoop() {
		noLoop = true;
		running = false;
		timePerFrameNano = MAX_FLOAT;
	}

	@Override
	final public void defaultSettings() {
		showOverlay(false);
		showInfo(false);
		infoUpdateIntervall = NANOSECOND;
		paused = false;

		super.width = width();
		super.height = height();
		super.format = RGB;
		super.defaultSettings();

//		noSmooth();
		smooth();

		overlay = new SImage(scaledWidth, scaledHeight, ARGB);
		overlay.smooth();
	}

	final public void start() {
		if (running) return;
		running = true;
		if (createdCanvas) frame.setVisible(true);
//		thread = new Thread(this);
//		thread.start();
		threadTick = new TickThread();
		threadTick.start();
		threadRender = new RenderThread();
		threadRender.start();
	}

	final public void stop() { running = false; }

	final public void toggleFullscreen() {
		synchronized (bs) {
			int w, h;
			if (fullscreen) {
				w = savedSize.width;
				h = savedSize.height;
				frame.setLocation(savedPos);
			} else {
				savedSize.setSize(scaledWidth, scaledHeight);
				savedPos = frame.getLocation();
				w = Toolkit.getDefaultToolkit().getScreenSize().width;
				h = Toolkit.getDefaultToolkit().getScreenSize().height;
				frame.setLocation(0, 0);
			}
			fullscreen = !fullscreen;
			frame.dispose();
			frame.setUndecorated(!(!fullscreen && frame.isOpaque()));
			frame.getContentPane().setPreferredSize(new Dimension(w, h));
			frame.pack();
			frame.setVisible(true);
		}
	}

	final public void tickRate(int newTarget) {
		tickRate = newTarget;
		timePerTickNano = newTarget < 1 ? 0 : NANOSECOND / tickRate;
		timePerTickMilli = newTarget < 1 ? 0 : MILLISECOND / tickRate;
		if (syncMode == SYNC) {
			frameRate = tickRate;
			timePerFrameNano = timePerTickNano;
			timePerFrameMilli = timePerTickMilli;
		}
	}

	final public void frameRate(int newTarget) {
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
	final public void syncMode(byte mode) {
		this.syncMode = mode;
	}

	final public void addListener(EventListener l) {
		if (l instanceof GameLoopListener) gameLoopListeners.add((GameLoopListener) l);
		if (l instanceof FrameLoopListener) frameLoopListeners.add((FrameLoopListener) l);
		if (l instanceof MouseListener) canvas.addMouseListener((MouseListener) l);
		if (l instanceof MouseMotionListener) canvas.addMouseMotionListener((MouseMotionListener) l);
		if (l instanceof MouseWheelListener) canvas.addMouseWheelListener((MouseWheelListener) l);
		if (l instanceof KeyListener) canvas.addKeyListener((KeyListener) l);
	}

	final public void removeListener(EventListener l) {
		if (l instanceof GameLoopListener) gameLoopListeners.remove(l);
		if (l instanceof FrameLoopListener) frameLoopListeners.remove(l);
		if (l instanceof MouseListener) canvas.removeMouseListener((MouseListener) l);
		if (l instanceof MouseMotionListener) canvas.removeMouseMotionListener((MouseMotionListener) l);
		if (l instanceof MouseWheelListener) canvas.removeMouseWheelListener((MouseWheelListener) l);
		if (l instanceof KeyListener) canvas.removeKeyListener((KeyListener) l);
	}

	void privateUpdate() {
		mouseScreenX = MouseInfo.getPointerInfo().getLocation().x;
		mouseScreenY = MouseInfo.getPointerInfo().getLocation().y;

		for (GameLoopListener gll : gameLoopListeners) gll.update();
	}

	protected void update() {}

	void privateDraw() {
		handleSmooth();
		for (FrameLoopListener fll : frameLoopListeners) fll.update();
	}

	protected void draw() {}

	protected void draw(Graphics2D g) {}

	void postDraw() {
		SShape.drawAll(this);
	}

	final public void background(Graphics2D g, Color c) {
		Color cSave = g.getColor();
		g.setColor(c);
		g.fillRect(0, 0, width(), height());
		g.setColor(cSave);
	}

	final public void saveFrame(String fileName) { saveImage(image, fileName); }

	@Override
	protected int x() { return canvas.getLocationOnScreen().x; }

	@Override
	protected int y() { return canvas.getLocationOnScreen().y; }

	@Override
	protected int frameX() { return frame.getLocationOnScreen().x; }

	@Override
	protected int frameY() { return frame.getLocationOnScreen().y; }

	public int width() { return (int) width; }

	public int height() { return (int) height; }

	public int mouseX() { return (int) mouseX; }

	public int mouseY() { return (int) mouseY; }

	final public long millis() {
		return System.currentTimeMillis() - startTime;
	}

	private void tick() {
		privateUpdate();
		update();
		ticks++;
		tickCount++;
	}

	private void render() {
		if (!createdCanvas) return;
		if (!running) return;
		push();
		privateDraw();
		draw();
		draw(graphics);
		pop();

		postDraw();

		if (showOverlay) drawOverlay();

//		Drawing the image
//		screen.render();
		do {
			do {
				Graphics g = bs.getDrawGraphics();
//				g.drawImage(image, 0, 0, null);
				g.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
				g.drawImage(overlay.image, 0, 0, null);
				g.dispose();
			} while (bs.contentsRestored());
			bs.show();
		} while (bs.contentsLost());

		frames++;
		frameCount++;
	}

	final public void showOverlay(boolean show) { showOverlay = show; }

	final public void showInfo(boolean show) {
		showInfo = show;
		handleOverlay();
	}

	final public void showCrosshair(boolean show) {
		showCrosshair = show;
		handleOverlay();
	}

	private void handleOverlay() {
		if (showInfo || showCrosshair) showOverlay = true; // if(showInfo || showX || show??? || ...
		else showOverlay = false;
	}

	final public void drawOverlay() {
		overlay.clear();
		if (showInfo) drawInfo();
		if (showCrosshair) drawCrosshair();
		// if(showX) drawX();
		// if(show???) draw???();
	}

	final public void drawInfo() {
		List<String> infos = getInfo();
		if (infos == null || infos.isEmpty()) return;

//		overlay.colorMode(RGB);
//		overlay.smooth();
		overlay.fill(255, 255, 0);
		overlay.stroke(0, 100);
		overlay.strokeWeight(5);
		overlay.textSize(13);
		overlay.textAlign(LEFT, TOP);

		float xoff = 5, yoff = 5;
		float ychange = textSize * 1.25f;
		for (String info : infos) {
			overlay.text(info, xoff, yoff);
			yoff += ychange;
		}
	}

	public List<String> getInfo() {
		List<String> info = new ArrayList<>();
		info.add(fps + " FPS");
		info.add(tps + " TPS");
		return info;
	}

	final public void drawCrosshair() {
		int aimStrokeWidth = (Math.round(aimSize / 8));
		int aimStroke = aimStrokeWidth * 2;
		overlay.noStroke();
		overlay.fill(aimColor.getRGB());
		overlay.rect((int) (mouseX - aimSize), mouseY - aimStrokeWidth, (int) (aimSize * 2), aimStroke);
		overlay.rect(mouseX - aimStrokeWidth, (int) (mouseY - aimSize), aimStroke, (int) (aimSize * 2));
	}

	final public void moveMouse(Vertex2F v) {
		moveMouse(v.x(), v.y());
	}

	final public void moveMouse(float x, float y) {
		moveMouseTo(mouseX + x, mouseY + y);
	}

	final public void moveMouseTo(Vertex2F v) {
		moveMouseTo(v.x, v.y);
	}

	public void moveMouseTo(float x, float y) {
		robot.mouseMove((int) (x() + x), (int) (y() + y));
	}

	final public void moveMouseOnScreen(Vertex2F v) {
		moveMouseOnScreen(v.x, v.y);
	}

	final public void moveMouseOnScreen(float x, float y) {
		moveMouseOnScreenTo(mouseScreenX + x, mouseScreenY + y);
	}

	final public void moveMouseOnScreenTo(Vertex2F v) {
		moveMouseOnScreenTo(v.x, v.y);
	}

	final public void moveMouseOnScreenTo(float x, float y) {
		robot.mouseMove((int) x, (int) y);
	}

	private class TickThread extends Thread {
		@Override
		public void run() {
			long currentTime = 0;
			long passedTime = 0;
			long previousTime = System.nanoTime();
			long lastTick = System.nanoTime();

			double timeSinceLastTick = 0;

			if (noLoop) return;
			while (running) {
				currentTime = System.nanoTime();
				passedTime = currentTime - previousTime;
				previousTime = currentTime;

				timeSinceLastTick += passedTime;

				if (timeSinceLastTick >= timePerTickNano) {
					timeSinceLastTick -= timePerTickNano;
					delta = (currentTime - lastTick) / (float) NANOSECOND;
					delta = Math.min(delta, deltaMin);
					multiplier = (currentTime - lastTick) / timePerTickNano;
					multiplier = Math.min(multiplier, multiplierMin);
					lastTick = currentTime;
					if (!paused) if (syncMode == ASYNC) tick();

				}
			}
		}
	}

	private class RenderThread extends Thread {
		@Override
		public void run() {
			long currentTime = 0;
			long passedTime = 0;
			long previousTime = System.nanoTime();

			double timeSinceLastFrame = 0;
			double timeSinceLastInfoUpdate = 0;

			if (noLoop) {
				if (!paused) tick();
				render();
				return;
			}
			while (running) {
				currentTime = System.nanoTime();
				passedTime = currentTime - previousTime;
				previousTime = currentTime;

				timeSinceLastFrame += passedTime;
				timeSinceLastInfoUpdate += passedTime;

				if (timeSinceLastInfoUpdate >= infoUpdateIntervall) {
					timeSinceLastInfoUpdate -= infoUpdateIntervall;
					tps = ticks;
					ticks = 0;
					fps = frames;
					frames = 0;
				}

				if (timeSinceLastFrame >= timePerFrameNano) {
					timeSinceLastFrame -= timePerFrameNano;
					if (syncMode == ASYNC) render();
					else {
						if (!paused) tick();
						render();
					}
				}
			}
		}
	}

	public static void UncaughtException(Throwable e) {
		Log.log(Log.FATAL, "Uncaught Exception", e);
		System.exit(1);
	}
}
