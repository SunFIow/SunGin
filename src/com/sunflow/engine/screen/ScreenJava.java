package com.sunflow.engine.screen;

import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
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

	protected JFrame frame;
	protected Canvas canvas;
//	protected BufferStrategy bs;

	// Overlay
	protected SGraphics overlay;

	public ScreenJava(GameBase game, Mouse mouse) { super(game, mouse); }

	@Override
	public void refresh() {
		super.refresh();

		frame.dispose();
		frame = null;
		canvas = null;
	}

	@Override
	public boolean render() {
		if (!isCreated) {
			System.out.println("not created");
			return false;
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
				draw.drawImage(game.image, 0, 0, scaledWidth, scaledHeight, null);
				if (showOverlay) draw.drawImage(overlay.image, 0, 0, null);
				draw.dispose();
			} while (strategy.contentsRestored());

			// Display the buffer
			strategy.show();

			// Repeat the rendering if the drawing buffer was lost
		} while (strategy.contentsLost());
		// }
		return true;
	}

	@Override
	public void preDraw() {
		super.preDraw();
	}

	@Override
	public void postDraw() {
		super.postDraw();
		S_Shape.drawAll(game);
		if (showOverlay) drawOverlay();
	}

	@Override
	public void createScreen() {
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
				width = scaledWidth / scaleWidth;
				height = scaledHeight / scaleHeight;
				game.resize((int) width, (int) height);
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
	final public void createCanvas(float width, float height, float scaleW, float scaleH) {
		if (!isCreated) {
			isCreated = true;
			this.width = width;
			this.height = height;
			this.scaleWidth = scaleW;
			this.scaleHeight = scaleH;

			this.scaledWidth = (int) (width * scaleWidth);
			this.scaledHeight = (int) (height * scaleHeight);

			createScreen();
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
		frame.getContentPane().setPreferredSize(new Dimension(w, h));
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

}
