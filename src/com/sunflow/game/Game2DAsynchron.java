package com.sunflow.game;

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
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.sunflow.math.OpenSimplexNoise;
import com.sunflow.math.Vertex2F;
import com.sunflow.util.Constants;
import com.sunflow.util.GameUtils;
import com.sunflow.util.GeometryUtils;
import com.sunflow.util.Log;
import com.sunflow.util.MathUtils;

public abstract class Game2DAsynchron extends GameP5 implements Runnable, Constants, MathUtils, GameUtils, GeometryUtils {

	private Thread logicThread;
	private Thread renderThread;
	protected JFrame frame;

	protected Random random;

	protected long frameCount;
	protected int frameRate;
	private double timePerTickNano;
	private double timePerTickMilli;

	protected boolean running;
	protected boolean fullscreen;
	protected boolean showFPS;

	private boolean paused;

	private Vertex2F savedSize;
	private Point savedPos;

	private boolean createdCanvas;

	private int fps;
	protected double delta;

//	private ImprovedNoise perlinnoise;
	private OpenSimplexNoise noise;

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

		frameWidth = 0;
		frameHeight = 0;
		width = 0;
		height = 0;

		x = 0;
		y = 0;

		fullscreen = false;
		smooth(0);
		showFPS = true;

		mouseX = 0;
		mouseY = 0;
		mouseScreenX = 0;
		mouseScreenY = 0;

		createdCanvas = false;

		fps = 0;
	}

	void privatePreSetup() {
		Log.info("starting 2D Game");

		random = new Random();
		noise = new OpenSimplexNoise(random.nextLong());
//		perlinnoise = new ImprovedNoise();

		savedSize = new Vertex2F();
		savedPos = new Point(0, 0);
	}

	protected void preSetup() {}

	protected void setup() {}

	void initFrame() {
		if (frame != null) frame.dispose();

		panel = new GamePanel();
		panel.setFocusable(true);
		panel.setPreferredSize(new Dimension(width(), height()));

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.setVisible(true);
		frame.createBufferStrategy(2);
		frame.setVisible(false);

		frame.pack();
		frame.setLocationRelativeTo(null);

		x = frame.getX();
		y = frame.getY();

		panel.addKeyListener(this);
		panel.addMouseListener(this);
		panel.addMouseMotionListener(this);
		panel.addMouseWheelListener(this);
		panel.addComponentListener(this);

		panel.addKeyListener(new KeyAdapter() {
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

		panel.addMouseMotionListener(new MouseMotionListener() {
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

		panel.addComponentListener(new ComponentListener() {
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
				if (fullscreen) return;
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

			initFrame();

			defaultSettings();
		}
		if (running) frame.setVisible(true);
	}

	@Override
	final protected void defaultSettings() {
		frameRate(60);
		showFPS();
		paused = false;

		super.defaultSettings();
	}

	final protected void start() {
		if (running) return;
		running = true;
		logicThread = new Thread(this);
		logicThread.start();
		renderThread = new Thread((GamePanel) panel);
		renderThread.start();
		if (createdCanvas) frame.setVisible(true);
	}

	final protected void stop() {
		if (!running) return;
		running = false;
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

	final protected void frameRate(int newTargetFrameRate) {
		frameRate = newTargetFrameRate;
		timePerTickNano = NANOSECOND / frameRate;
		timePerTickMilli = MILLISECOND / frameRate;
	}

	void privateUpdate(double delta) {
		mouseScreenX = MouseInfo.getPointerInfo().getLocation().x;
		mouseScreenY = MouseInfo.getPointerInfo().getLocation().y;
	}

	protected void update(double delta) {}

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
		TextLayout tl = new TextLayout(text, textFont.deriveFont(textSize), panel.getFontMetrics(textFont).getFontRenderContext());
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
		long currentTime;
		long passedTime;
		long previousTime = System.nanoTime();
		long lastTick = System.nanoTime();

		double delta = 0;

		// TODO: make tickRate, frameRate seperate modifiable
		@SuppressWarnings("unused")
		long ticks = 0;

		while (running) {
			currentTime = System.nanoTime();
			passedTime = currentTime - previousTime;
			previousTime = currentTime;

			delta += passedTime;

			if (delta >= timePerTickNano) {
				this.delta = (currentTime - lastTick) / NANOSECOND;
				this.delta = Math.min(this.delta, 0.15);
				lastTick = currentTime;
				delta -= timePerTickNano;
//				Log.err(ticks);
				ticks = 0;
				if (!paused) tick();
				else try {
					Thread.sleep((long) Math.min(33, timePerTickMilli));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			ticks++;
		}
	}

	private void tick() {
		privateUpdate(delta);
		update(delta);
		frameCount++;
	}

	@SuppressWarnings("serial")
	private class GamePanel extends JPanel implements Runnable {
		@Override
		public void run() {
			long currentTime;
			long passedTime;
			long previousTime = System.nanoTime();

			double delta = 0;
			int frames = 0;

			while (running) {
				currentTime = System.nanoTime();
				passedTime = currentTime - previousTime;
				previousTime = currentTime;

				delta += passedTime;

				if (delta >= NANOSECOND) {
					fps = frames;
					delta -= NANOSECOND;
					frames = 0;
				}
				frames++;
				if (!paused) {
					panel.repaint();
					synchronized (this) {
						try {
							wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} else try {
					Thread.sleep((long) Math.min(33, timePerTickMilli));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;

			if (graphics == null) {
				image = new BufferedImage(width(), height(), RGB);
				graphics = image.createGraphics();
			}

			privateDraw();
			draw();

			if (showFPS) drawFps();

			// Drawing the image.
//			BufferedImage image1 = image.getSubimage(0, 0, getWidth(), getHeight());
			g2.drawImage(image, 0, 0, null);

			render(g2);

			g2.dispose();

			synchronized (this) {
				notify();
			}
		}
	}

	final public void showFPS() { showFPS = true; }

	final public void hideFPS() { showFPS = false; }

	private void drawFps() {
		push();
		colorMode(RGB);
		fill(255, 255, 0);
		stroke(0, 100);
		strokeWeight(5);
		textSize(16);
		textAlign(LEFT, TOP);
		text(fps + " FPS", 8, 5);
		pop();
	}
}
