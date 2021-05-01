package com.sunflow.engine.screen;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.util.EventListener;

import javax.swing.JFrame;

import com.sunflow.engine.Mouse;
import com.sunflow.engine.eventsystem.listeners.SEventListener;
import com.sunflow.game.GameBase;
import com.sunflow.gfx.SGraphics;
import com.sunflow.gfx.S_Shape;
import com.sunflow.util.SConstants;

public class ScreenJava extends Screen {
	GraphicsDevice displayDevice;

	// Note that x and y may not be zero, depending on the display configuration
	Rectangle screenRect;

//	Insets currentInsets = new Insets(0, 0, 0, 0);

	int gameWidth;
	int gameHeight;

	int windowScaleFactor;

	protected Frame frame;
	protected Canvas canvas;
//	protected BufferStrategy bs;

	// Overlay
	protected SGraphics overlay;

	public ScreenJava(GameBase game, Mouse mouse) { super(game, mouse); }

	public class SmoothCanvas extends Canvas {
		private static final long serialVersionUID = -3098873451251328369L;

		private Dimension oldSize = new Dimension(0, 0);
		private Dimension newSize = new Dimension(0, 0);

		// Turns out getParent() returns a JPanel on a JFrame. Yech.
		public Frame getFrame() {
			return frame;
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(gameWidth, gameHeight);
		}

		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getMaximumSize() {
			// return resizable ? super.getMaximumSize() : getPreferredSize();
			return frame.isResizable() ? super.getMaximumSize() : getPreferredSize();
		}

		@Override
		public void validate() {
			super.validate();
			newSize.width = getWidth();
			newSize.height = getHeight();
//	      if (oldSize.equals(newSize)) {
////	        System.out.println("validate() return " + oldSize);
//	        return;
//	      } else {
			if (!oldSize.equals(newSize)) {
//	        System.out.println("validate() render old=" + oldSize + " -> new=" + newSize);
				oldSize = newSize;
				game.setSize(newSize.width / windowScaleFactor, newSize.height / windowScaleFactor);
//	        try {
				render();
//	        } catch (IllegalStateException ise) {
//	          System.out.println(ise.getMessage());
//	        }
			}
		}

		@Override
		public void update(Graphics g) {
//	      System.out.println("updating");
			paint(g);
		}

		@Override
		public void paint(Graphics screen) {
//	      System.out.println("painting");
//	      if (useStrategy) {
			render();
			/*
			 * if (graphics != null) {
			 * System.out.println("drawing to screen " + canvas);
			 * screen.drawImage(graphics.image, 0, 0, sketchWidth, sketchHeight, null);
			 * }
			 */

//	      } else {
////	        new Exception("painting").printStackTrace(System.out);
////	        if (graphics.image != null) { // && !sketch.insideDraw) {
//	        if (onscreen != null) {
////	          synchronized (graphics.image) {
//	          // Needs the width/height to be set so that retina images are properly scaled down
////	          screen.drawImage(graphics.image, 0, 0, sketchWidth, sketchHeight, null);
//	          synchronized (offscreenLock) {
//	            screen.drawImage(onscreen, 0, 0, sketchWidth, sketchHeight, null);
//	          }
//	        }
//	      }
		}
	}

	@Override
	public void refresh() {
		super.refresh();

		frame.dispose();
		frame = null;
		canvas = null;
	}

	@Override
	synchronized protected void render() {
		if (!isCreated || !canvas.isDisplayable() || game.getGraphics().image == null) {
			System.out.println("not created");
			return;
		}

		BufferStrategy strategy = canvas.getBufferStrategy();
		if (strategy == null) {
			canvas.createBufferStrategy(2);
			strategy = canvas.getBufferStrategy();
		}
		// if (strategy != null) {
//		Drawing the image
		do {
			// The following loop ensures that the contents of the drawing buffer
			// are consistent in case the underlying surface was recreated
			do {
//				Graphics2D draw = (Graphics2D) strategy.getDrawGraphics();
				Graphics draw = strategy.getDrawGraphics();
				// draw to width/height, since this may be a 2x image
				draw.drawImage(game.getGraphics().image, 0, 0, scaledWidth, scaledHeight, null);
				if (showOverlay) draw.drawImage(overlay.image, 0, 0, null);
				draw.dispose();
			} while (strategy.contentsRestored());

			// Display the buffer
			strategy.show();

			// Repeat the rendering if the drawing buffer was lost
		} while (strategy.contentsLost());
		// }
	}

//	@Override
//	public void preDraw() {}

	@Override
	public void postDraw() {
		S_Shape.drawAll(game);
		if (showOverlay) drawOverlay();
		super.postDraw();
	}

	@Override
	public void createScreen() {
		if (frame != null) frame.dispose();

		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();

		int displayNum = game.getDisplay();
//			    System.out.println("display from sketch is " + displayNum);
		if (displayNum > 0) { // if -1, use the default device
			GraphicsDevice[] devices = environment.getScreenDevices();
			if (displayNum <= devices.length) {
				displayDevice = devices[displayNum - 1];
			} else {
				System.err.format("Display %d does not exist, " +
						"using the default display instead.%n", displayNum);
				for (int i = 0; i < devices.length; i++) {
					System.err.format("Display %d is %s%n", (i + 1), devices[i]);
				}
			}
		}
		if (displayDevice == null) {
			displayDevice = environment.getDefaultScreenDevice();
		}

		// Need to save the window bounds at full screen,
		// because pack() will cause the bounds to go to zero.
		// http://dev.processing.org/bugs/show_bug.cgi?id=923
		boolean spanDisplays = game.getDisplay() == SConstants.SPAN;
		screenRect = spanDisplays
				? getDisplaySpan()
				: displayDevice.getDefaultConfiguration().getBounds();
		// DisplayMode doesn't work here, because we can't get the upper-left
		// corner of the display, which is important for multi-display setups.

		// Set the displayWidth/Height variables inside PApplet, so that they're
		// usable and can even be returned by the gameWidth()/Height() methods.
		game.displayWidth = screenRect.width;
		game.displayHeight = screenRect.height;

		windowScaleFactor = GameBase.platform == SConstants.MACOSX ? 1 : game.pixelDensity;

		gameWidth = game.getWidth() * windowScaleFactor;
		gameHeight = game.getHeight() * windowScaleFactor;

		canvas = new Canvas();
		canvas.setFocusable(true);
//		canvas.setPreferredSize(new Dimension(width(), height()));
		canvas.setPreferredSize(new Dimension(scaledWidth, scaledHeight));
//		screen = new Screen(this);
//		screen.setFocusable(true);
//		screen.setPreferredSize(new Dimension(width(), height()));

		canvas.setIgnoreRepaint(true);

		frame = new JFrame(displayDevice.getDefaultConfiguration());

		final Color windowColor = new Color(game.getWindowColor(), false);
		if (frame instanceof JFrame) {
			((JFrame) frame).getContentPane().setBackground(windowColor);
		} else {
			frame.setBackground(windowColor);
		}

//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //TODO

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				game.exit(); // don't quit, need to just shut everything down (0133)
			}
		});

		if (title != null) frame.setTitle(title);
		frame.setUndecorated(undecorated);
		frame.add(canvas);
		frame.setVisible(true);
		frame.setVisible(false);
//		screen.addToFrame(frame);

//		canvas.createBufferStrategy(3);
//		bs = canvas.getBufferStrategy();

		frame.pack();
		frame.setLocationRelativeTo(null);

		canvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				key = e.getKeyChar();
				keyCode = e.getKeyCode();
				keys[keyCode] = true;
				if (game.keyPressed()) return;
				switch (e.getKeyCode()) {
					case KeyEvent.VK_F5:
						game.reset();
						break;
					case KeyEvent.VK_F9:
						game.isPaused = !game.isPaused;
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
				if (game.keyReleased()) return;
			}

			@Override
			public void keyTyped(KeyEvent e) {
				key = e.getKeyChar();
				keyCode = e.getKeyCode();
				if (game.keyTyped()) return;
			}
		});

		canvas.addMouseListener(new MouseListener() {
			@Override
			public void mousePressed(MouseEvent e) {
//				System.err.println("mouspressed");
				button = e.getButton();
				mouseButtons[e.getButton()] = true;
				if (e.getButton() == MouseEvent.BUTTON1)
					mousePressed = true;
				if (game.mouseOnPressed()) return;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				button = e.getButton();
				mouseButtons[e.getButton()] = false;
				if (e.getButton() == MouseEvent.BUTTON1)
					mousePressed = false;
				if (game.mouseOnReleased()) return;
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
			public void mouseMoved(MouseEvent e) { mouse.updatePosition(e.getX(), e.getY()); }

			@Override
			public void mouseDragged(MouseEvent e) { mouse.updatePosition(e.getX(), e.getY()); }
		});

		canvas.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) { mouseWheel = e.getPreciseWheelRotation(); }
		});

		canvas.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
//				cResized(e.getComponent().getWidth(), e.getComponent().getHeight());
				int w = e.getComponent().getWidth();
				int h = e.getComponent().getHeight();
				scaledWidth = w;
				scaledHeight = h;
				width = (int) (scaledWidth / scaleWidth);
				height = (int) (scaledHeight / scaleHeight);
				game.getGraphics().resize(width, height);
			}
		});

		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				frameWidth = e.getComponent().getWidth();
				frameHeight = e.getComponent().getHeight();
			}
		});

		canvas.addKeyListener(game);
		canvas.addMouseListener(game);
		canvas.addMouseMotionListener(game);
		canvas.addMouseWheelListener(game);
		canvas.addComponentListener(game);
	}

	@Override
	final public void createCanvas(int width, int height, float scaleW, float scaleH) {
		if (!isCreated) {
			this.width = width;
			this.height = height;
			this.scaleWidth = scaleW;
			this.scaleHeight = scaleH;

			this.scaledWidth = (int) (width * scaleWidth);
			this.scaledHeight = (int) (height * scaleHeight);

			createScreen();
			isCreated = true;
		}
	}

	@Override
	public void defaultSettings() {
//		overlay = new SGraphics(scaledWidth, scaledHeight, SGraphics.ARGB);
		overlay = game.createGraphics(scaledWidth, scaledHeight);
//		overlay.init(); //TODO
		overlay.smooth();
	}

	@Override
	public void show() { if (isCreated) frame.setVisible(true); }

	@Override
	public void requestFocus() {
		canvas.requestFocus();
	}

	@Override
	public void setTitle(String title) {
		super.setTitle(title);
		if (isCreated) frame.setTitle(title + title_info);
	}

	@Override
	public void setTitleInfo(String title_info) {
		super.setTitleInfo(title_info);
		if (isCreated) frame.setTitle(title + title_info);
	}

	@Override
	public void toggleFullscreen() {
//		synchronized (bs) {
		int w, h;
		if (fullscreen) {
			w = savedSize.x();
			h = savedSize.y();
			frame.setLocation(savedPos.get((Point) null));
		} else {
			savedSize.set(scaledWidth, scaledHeight);
			savedPos.set(frame.getLocation());
			w = Toolkit.getDefaultToolkit().getScreenSize().width;
			h = Toolkit.getDefaultToolkit().getScreenSize().height;
			frame.setLocation(0, 0);
		}
		fullscreen = !fullscreen;
		frame.dispose();
		frame.setUndecorated(!(!fullscreen && frame.isOpaque()));
		if (frame instanceof JFrame) ((JFrame) frame).getContentPane().setPreferredSize(new Dimension(w, h));
		frame.pack();
		frame.setVisible(true);
//		}
	}

	@Override
	public final void drawOverlay() {
		overlay.beginDraw();
		overlay.clear();
		if (showInfo) drawInfo();
		if (showCrosshair) drawCrosshair();
		// if(showX) drawX();
		// if(show???) draw???();
		overlay.endDraw();
	}

	@Override
	public final void infoSize(float size) { overlay.textSize(size); }

	@Override
	public final void drawInfo() {
		if (game.infos == null || game.infos.isEmpty()) return;

//		overlay.colorMode(RGB);
//		overlay.smooth();
		overlay.fill(255, 255, 0);
		overlay.stroke(0, 100);
		overlay.strokeWeight(5);
//		overlay.textSize(13);
		overlay.textAlign(SConstants.LEFT, SConstants.TOP);

		float xoff = 5, yoff = 5;
		float ychange = overlay.textSize * 1.25f;
		for (String info : game.infos) {
			overlay.text(info, xoff, yoff);
			yoff += ychange;
		}
	}

	@Override
	public final void drawCrosshair() {
		int aimStrokeWidth = (Math.round(aimSize / 8));
		int aimStroke = aimStrokeWidth * 2;
		overlay.noStroke();
		overlay.fill(aimColor.getRGB());
		overlay.rect((int) (mouse.x - aimSize), mouse.y - aimStrokeWidth, (int) (aimSize * 2), aimStroke);
		overlay.rect(mouse.x - aimStrokeWidth, (int) (mouse.y - aimSize), aimStroke, (int) (aimSize * 2));
	}

	@Override
	public boolean addListener(EventListener listener) {
		boolean added = false;
		if (listener instanceof KeyListener) {
			canvas.addKeyListener((KeyListener) listener);
			added = true;
		}
		if (listener instanceof MouseListener) {
			canvas.addMouseListener((MouseListener) listener);
			added = true;
		}
		if (listener instanceof MouseMotionListener) {
			canvas.addMouseMotionListener((MouseMotionListener) listener);
			added = true;
		}
		if (listener instanceof MouseWheelListener) {
			canvas.addMouseWheelListener((MouseWheelListener) listener);
			added = true;
		}
		return added;
	}

	@Override
	public boolean removeListener(EventListener listener) {
		boolean removed = false;
		if (listener instanceof KeyListener) {
			canvas.removeKeyListener((KeyListener) listener);
			removed = true;
		}
		if (listener instanceof MouseListener) {
			canvas.removeMouseListener((MouseListener) listener);
			removed = true;
		}
		if (listener instanceof MouseMotionListener) {
			canvas.removeMouseMotionListener((MouseMotionListener) listener);
			removed = true;
		}
		if (listener instanceof MouseWheelListener) {
			canvas.removeMouseWheelListener((MouseWheelListener) listener);
			removed = true;
		}
		return removed;
	}

	@Override
	public boolean addListener(SEventListener listener) { return false; }

	@Override
	public boolean removeListener(SEventListener listener) { return false; }

	@Override
	public void setCursor(Cursor cursor) { canvas.setCursor(cursor); }

	@Override
	public boolean hasFocus() { return canvas.hasFocus(); }

	@Override
	public int getX() { return canvas.getLocationOnScreen().x; }

	@Override
	public int getY() { return canvas.getLocationOnScreen().y; }

	@Override
	public int getScreenX() { return frame.getLocationOnScreen().x; }

	@Override
	public int getScreenY() { return frame.getLocationOnScreen().y; }

	/** Get the bounds rectangle for all displays. */
	static Rectangle getDisplaySpan() {
		Rectangle bounds = new Rectangle();
		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (GraphicsDevice device : environment.getScreenDevices()) {
			for (GraphicsConfiguration config : device.getConfigurations()) {
				Rectangle2D.union(bounds, config.getBounds(), bounds);
			}
		}
		return bounds;
	}

//	@Override
//	public GraphicsConfiguration getGC() { return displayDevice.getDefaultConfiguration(); }
//	public GraphicsConfiguration getGC() { return canvas.getGraphicsConfiguration(); }

}
