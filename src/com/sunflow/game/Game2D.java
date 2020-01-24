package com.sunflow.game;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import com.sunflow.math.OpenSimplexNoise;
import com.sunflow.math.Vertex2F;
import com.sunflow.util.Constants;
import com.sunflow.util.GameUtils;
import com.sunflow.util.GeometryUtils;
import com.sunflow.util.Log;
import com.sunflow.util.MathUtils;

public abstract class Game2D extends GameP5 implements Runnable, Constants, MathUtils, GameUtils, GeometryUtils {

	private Thread thread;

	protected JFrame frame;
	protected Canvas canvas;
	private BufferStrategy bs;

	private boolean createdCanvas;

	// Overlay
	protected boolean showOverlay;
	protected boolean showInfo;

	protected Random random;
//	private ImprovedNoise perlinnoise;
	private OpenSimplexNoise noise;

	protected boolean running;
	protected boolean paused;

	protected boolean noLoop;

	private byte mode;

	private double timePerTickNano, timePerTickMilli;
	private double timePerFrameNano, timePerFrameMilli;
	private double infoUpdateIntervall;

	private int fps, tps;

	protected int frameRate, tickRate;
	protected long frameCount, tickCount;
	private int frames = 0;

	protected double delta, deltaMin;
	protected double multiplier, multiplierMin;

	protected boolean fullscreen;

	private Vertex2F savedSize;
	private Point savedPos;

	@Override
	protected final void init() {
		super.init();
		privatePreSetup();
		preSetup();

		setup();
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
		frameCount = 0;
		frameRate = 0;
		tickCount = 0;
		tickRate = 0;

		frameWidth = 0;
		frameHeight = 0;
		width = 0;
		height = 0;

		x = 0;
		y = 0;

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

	void privatePreSetup() {
		Log.info("starting 2D Game");

		random = new Random();
		noise = new OpenSimplexNoise(random.nextLong());
//		perlinnoise = new ImprovedNoise();
		frameRate(60);
		mode(SYNC);
		noSmooth();

		multiplierMin = 5;
		deltaMin = timePerFrameNano / NANOSECOND * 5;

		savedSize = new Vertex2F();
		savedPos = new Point(0, 0);
	}

	protected void preSetup() {}

	protected void setup() {}

	void createFrame() {
		if (frame != null) frame.dispose();

		canvas = new Canvas();
		canvas.setFocusable(true);
		canvas.setPreferredSize(new Dimension(width(), height()));

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setVisible(false);
		frame.add(canvas);

		canvas.createBufferStrategy(3);
		bs = canvas.getBufferStrategy();

		frame.pack();
		frame.setLocationRelativeTo(null);

		x = frame.getX();
		y = frame.getY();

		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);
		canvas.addComponentListener(this);

		canvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
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
		});

		canvas.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				updateMousePosition(e.getX(), e.getY());
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				updateMousePosition(e.getX(), e.getY());
			}

			private void updateMousePosition(int x, int y) {
				mouseX = x;
				mouseY = y;
			}
		});

		canvas.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				width = e.getComponent().getWidth();
				height = e.getComponent().getHeight();
				resize(width, height);
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				x = e.getComponent().getX();
				y = e.getComponent().getY();
				if (!fullscreen) return;
				x += 4;
				y += 23;

			}

			@Override
			public void componentShown(ComponentEvent e) {}

			@Override
			public void componentHidden(ComponentEvent e) {}
		});

		frame.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				frameWidth = e.getComponent().getWidth();
				frameHeight = e.getComponent().getHeight();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				x = e.getComponent().getX();
				y = e.getComponent().getY();
				if (fullscreen) return;
				x += 4;
				y += 23;
			}

			@Override
			public void componentShown(ComponentEvent e) {}

			@Override
			public void componentHidden(ComponentEvent e) {}
		});
	}

	final protected void createCanvas(float width, float height) { createCanvas((int) width, (int) height); }

	final protected void createCanvas(int width, int height) {
		if (!createdCanvas) {
			createdCanvas = true;
			super.width = width;
			super.height = height;

			createFrame();

			defaultSettings();
		}
		if (running) frame.setVisible(true);

	}

	@Override
	final protected void defaultSettings() {
		showOverlay(false);
		showInfo(false);
		infoUpdateIntervall = NANOSECOND;
		paused = false;

		super.defaultSettings();
	}

	final protected void start() {
		if (running) return;
		running = true;
		if (createdCanvas) frame.setVisible(true);
		thread = new Thread(this);
		thread.start();
	}

	final protected void stop() { running = false; }

	final protected void noLoop() {
		noLoop = true;
		running = false;
		timePerFrameNano = Double.MAX_VALUE;
	}

	final protected void toggleFullscreen() {
		if (fullscreen) {
			width = savedSize.x;
			height = savedSize.y;
			frame.setLocation(savedPos);
			x = frame.getX();
			y = frame.getY();
		} else {
			savedSize.x = width;
			savedSize.y = height;
			savedPos = frame.getLocation();
			width = Toolkit.getDefaultToolkit().getScreenSize().width;
			height = Toolkit.getDefaultToolkit().getScreenSize().height;
			frame.setLocation(0, 0);
			x = 0;
			y = 0;
		}
		fullscreen = !fullscreen;
		frame.dispose();
		frame.setUndecorated(!(!fullscreen && frame.isOpaque()));
		frame.getContentPane().setPreferredSize(new Dimension(width(), height()));
		frame.pack();
		frame.setVisible(true);
//		frame.requestFocus();
	}

	final protected void tickRate(int newTarget) {
		tickRate = newTarget;
		timePerTickNano = newTarget < 1 ? 0 : NANOSECOND / tickRate;
		timePerTickMilli = newTarget < 1 ? 0 : MILLISECOND / tickRate;
		if (mode == SYNC) {
			frameRate = tickRate;
			timePerFrameNano = timePerTickNano;
			timePerFrameMilli = timePerTickMilli;
		}
	}

	final protected void frameRate(int newTarget) {
		frameRate = newTarget;
		timePerFrameNano = newTarget < 1 ? 0 : NANOSECOND / frameRate;
		timePerFrameMilli = newTarget < 1 ? 0 : MILLISECOND / frameRate;
		if (mode == SYNC) {
			tickRate = frameRate;
			timePerTickNano = timePerFrameNano;
			timePerTickMilli = timePerFrameMilli;
		}
	}

	/**
	 * @param mode
	 *            either SYNC or ASYNC
	 */
	final protected void mode(byte mode) { this.mode = mode; }

	void privateUpdate() {
		mouseScreenX = MouseInfo.getPointerInfo().getLocation().x;
		mouseScreenY = MouseInfo.getPointerInfo().getLocation().y;
	}

	protected void update() {}

	void privateDraw() { handleSmooth(); }

	protected void draw() {}

	protected void render(Graphics2D g) {}

	final public void background(Graphics2D g, Color c) {
		Color cSave = g.getColor();
		g.setColor(c);
		g.fillRect(0, 0, width(), height());
		g.setColor(cSave);
	}

//
	final public void textO(String text, float x, float y) {
		if (textFont == null) {
			textFont = createDefaultFont();
		}
		TextLayout tl = new TextLayout(text, textFont.deriveFont(textSize), canvas.getFontMetrics(textFont).getFontRenderContext());
		AffineTransform transform = new AffineTransform();
		transform.translate(x, y);
		Shape shape = tl.getOutline(transform);
		strokeShape(shape);
		fillShape(shape);
	}

	final public void saveFrame(String fileName) { saveImageToFile(image, fileName); }

	final public double noise(double xoff) { return noise.eval(xoff, 0); }

	final public double noise(double xoff, double yoff) { return noise.eval(xoff, yoff); }

	final public double noise(double xoff, double yoff, double zoff) { return noise.eval(xoff, yoff, zoff); }

	final public double noise(double xoff, double yoff, double zoff, double woff) { return noise.eval(xoff, yoff, zoff, woff); }

	@Override
	public void run() {
		long currentTime = 0;
		long passedTime = 0;
		long previousTime = System.nanoTime();
		long lastTick = System.nanoTime();

		double timeSinceLastTick = 0;
		double timeSinceLastFrame = 0;
		double timeSinceLastInfoUpdate = 0;

		int ticks = 0;

//		render();
		if (noLoop) return;
		while (running) {
			currentTime = System.nanoTime();
			passedTime = currentTime - previousTime;
			previousTime = currentTime;

			timeSinceLastTick += passedTime;
			timeSinceLastFrame += passedTime;
			timeSinceLastInfoUpdate += passedTime;

			if (timeSinceLastInfoUpdate >= infoUpdateIntervall) {
				timeSinceLastInfoUpdate -= infoUpdateIntervall;
				tps = ticks;
				ticks = 0;
				fps = frames;
				frames = 0;
			}

			if (timeSinceLastTick >= timePerTickNano) {
				timeSinceLastTick -= timePerTickNano;
				delta = (currentTime - lastTick) / NANOSECOND;
				delta = Math.min(delta, deltaMin);
				multiplier = (currentTime - lastTick) / timePerTickNano;
				multiplier = Math.min(multiplier, multiplierMin);
				lastTick = currentTime;
				ticks++;
				if (!paused) tick();
			}

			if (timeSinceLastFrame >= timePerFrameNano) {
				timeSinceLastFrame -= timePerFrameNano;
				render();
			}
		}
	}

	private void tick() {
		privateUpdate();
		update();
		tickCount++;
	}

	private void render() {
		do {
			do {
				Graphics g = bs.getDrawGraphics();

				if (graphics == null) {
					image = new BufferedImage(width(), height(), ARGB);
					graphics = image.createGraphics();
				}

				privateDraw();
				draw();

				if (showOverlay) drawOverlay();

				// Drawing the image.
//				BufferedImage image2 = image.getSubimage(0, 0, getWidth(), getHeight());
				g.drawImage(image, 0, 0, null);

				render((Graphics2D) g);

				g.dispose();
				frames++;
				frameCount++;

				g.dispose();
			} while (bs.contentsRestored());
			bs.show();
		} while (bs.contentsLost());
	}

	final public void showOverlay(boolean show) { showOverlay = show; }

	final public void showInfo(boolean show) {
		showInfo = show;
		handleOverlay();
	}

	private void handleOverlay() {
		if (showInfo) showOverlay = true; // if(showInfo || showX || show??? || ...
		else showOverlay = false;
	}

	final public void drawOverlay() {
		push();
		colorMode(RGB);
		smooth();
		fill(255, 255, 0);
		stroke(0, 100);
		strokeWeight(5);
		textSize(13);
		textAlign(LEFT, TOP);
		if (showInfo) drawInfo();
		// if(showX) drawX();
		// if(show???) draw???();
		pop();
	}

	final public void drawInfo() {
		List<String> infos = getInfo();
		if (infos == null || infos.isEmpty()) return;

		float xoff = 5, yoff = 0;
		float ychange = textSize * 1.25f;
		for (String info : infos) {
			text(info, xoff, yoff);
			yoff += ychange;
		}
	}

	public List<String> getInfo() {
		List<String> info = new ArrayList<>();
		info.add(fps + " FPS");
		info.add(tps + " TPS");
		return info;
	}
}
