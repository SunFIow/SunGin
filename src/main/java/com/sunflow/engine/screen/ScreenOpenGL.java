package com.sunflow.engine.screen;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.EventListener;

import com.sunflow.engine.Keyboard;
import com.sunflow.engine.Mouse;
import com.sunflow.engine.WindowLWJGL;
import com.sunflow.engine.eventsystem.EventManager;
import com.sunflow.engine.eventsystem.adapters.KeyInputAdapter;
import com.sunflow.engine.eventsystem.adapters.MouseInputAdapter;
import com.sunflow.engine.eventsystem.adapters.MouseMotionAdapter;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyPressedEvent;
import com.sunflow.engine.eventsystem.events.KeyInputEvent.KeyReleasedEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent.MousePressedEvent;
import com.sunflow.engine.eventsystem.events.MouseInputEvent.MouseReleasedEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent.MouseMovedEvent;
import com.sunflow.engine.eventsystem.events.ScrollEvent;
import com.sunflow.engine.eventsystem.events.WindowResizeEvent;
import com.sunflow.engine.eventsystem.listeners.KeyInputListener;
import com.sunflow.engine.eventsystem.listeners.MouseInputListener;
import com.sunflow.engine.eventsystem.listeners.MouseMotionListener;
import com.sunflow.engine.eventsystem.listeners.SEventListener;
import com.sunflow.engine.eventsystem.listeners.ScrollListener;
import com.sunflow.engine.eventsystem.listeners.WindowResizeListener;
import com.sunflow.engine.input.InputConstants;
import com.sunflow.game.GameBase;
import com.sunflow.gfx.SGraphics;
import com.sunflow.gfx.S_Shape;
import com.sunflow.util.SConstants;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL46;

public class ScreenOpenGL extends Screen {

	protected WindowLWJGL window;

	protected SGraphics overlay;

	private int textureID;

	private ByteBuffer buffer;

	public ScreenOpenGL(GameBase game, Mouse mouse, Keyboard keyboard) { super(game, mouse, keyboard); }

	@Override
	public void refresh() {
		super.refresh();

		window.destroy();
		window = null;

		destroyListeners();
	}

	float xfactor = 0, yfactor = 0;

	@Override
	synchronized protected void render() {
		if (!isCreated) return;

		BufferedImage image = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.drawImage(game.getGraphics().image, 0, scaledHeight, scaledWidth, -scaledHeight, null);
		if (showOverlay) g.drawImage(overlay.image, 0, scaledHeight, scaledWidth, -scaledHeight, null);
		g.dispose();

		//		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		//		genTexture(image);

		//		xfactor += 0.1;
		//		GL46.glPixelZoom(xfactor, yfactor);
		//		int[] pixels = loadPixels(image);
		//		int[] pixels = new int[getWidth() * getHeight()];
		//		IntBuffer pixels = BufferUtils.createIntBuffer(getWidth() * getHeight());
		//		pixels.put(loadPixels(image));
		//		game.loadPixels();
		//		pixels.put(game.pixels);
		//		pixels.flip();
		//		GL46.glReadPixels(0, 0, getWidth(), getHeight(), GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, pixels);
		//		GL46.glDrawPixels(getWidth(), getHeight(), GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, loadPixels(image));

		genTexture(image);
		GL46.glDrawPixels(width(), height(), GL46.GL_RGBA, GL46.GL_UNSIGNED_BYTE, buffer);

		window.render();
	}

	final public ByteBuffer loadPixels(BufferedImage image) {
		int BYTES_PER_PIXEL = 4; // 4 for RGBA, 3 for RGB
		if (buffer == null) buffer = BufferUtils.createByteBuffer(width() * height() * BYTES_PER_PIXEL);
		//		ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL);

		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int pixel = pixels[y * image.getWidth() + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
				buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
				buffer.put((byte) (pixel & 0xFF)); // Blue component
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component. Only for RGBA
			}
		}

		buffer.flip();
		return buffer;
	}

	private void genTexture(BufferedImage image) {
		int BYTES_PER_PIXEL = 4; // 4 for RGBA, 3 for RGB
		if (buffer == null) buffer = BufferUtils.createByteBuffer(width() * height() * BYTES_PER_PIXEL);
		//		ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL);

		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int pixel = pixels[y * image.getWidth() + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
				buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
				buffer.put((byte) (pixel & 0xFF)); // Blue component
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component. Only for RGBA
			}
		}

		buffer.flip(); // FOR THE LOVE OF GOD DO NOT FORGET THIS

		// You now have a ByteBuffer filled with the color data of each pixel.
		// Now just create a texture ID and bind it. Then you can load it using
		// whatever OpenGL method you want, for example:

		//		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL46.GL_RGB8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

		//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
	}

	@Override
	public void preUpdate() {
		if (window.isCloseRequested()) game.isRunning = false;

		super.preUpdate();
	}

	@Override
	public void preDraw() { window.prepare(); }

	@Override
	public void postDraw() {
		S_Shape.drawAll(game);
		if (showOverlay) drawOverlay();
		super.postDraw();
	}

	@Override
	public void createScreen() {
		//		if (frame != null) frame.dispose();
		if (window != null) window.destroy();

		window = new WindowLWJGL();
		window.createWindow(scaledWidth, scaledHeight, title);

		buffer = BufferUtils.createByteBuffer(width() * height() * 4);
	}

	private KeyInputListener keyInput;
	private MouseInputListener mouseInput;
	private MouseMotionListener mouseMotion;
	private ScrollListener mouseScroll;
	private WindowResizeListener windowResize;

	@Override
	protected void createListeners() {
		EventManager.setupRawCallbacks(window.getID());

		EventManager.addKeyInputListener(keyInput = new KeyInputAdapter() {
			@Override
			public void onKeyPressed(KeyPressedEvent e) {
				keyboard.updateKey(e.getKeyChar(), e.getKeyCode(), true);
				if (game.keyPressed()) return;
				switch (e.getKeyCode()) {
					case InputConstants.KEY_F5:
						game.reset();
						break;
					case InputConstants.KEY_F9:
						game.isPaused = !game.isPaused;
						break;
					case InputConstants.KEY_F11:
						toggleFullscreen();
						break;
				}
			}

			@Override
			public void onKeyReleased(KeyReleasedEvent e) {
				keyboard.updateKey(e.getKeyChar(), e.getKeyCode(), false);
				if (game.keyReleased()) return;
			}
		});

		EventManager.addMouseInputListener(mouseInput = new MouseInputAdapter() {
			@Override
			public void onMousePressed(MousePressedEvent e) {
				mouse.updateButton(e.getButton(), true, GLFW.GLFW_MOUSE_BUTTON_LEFT);
				if (game.mouseOnPressed()) return;
			}

			@Override
			public void onMouseReleased(MouseReleasedEvent e) {
				mouse.updateButton(e.getButton(), false, GLFW.GLFW_MOUSE_BUTTON_LEFT);
				if (game.mouseOnReleased()) return;
			}
		});

		EventManager.addMouseMotionListener(mouseMotion = new MouseMotionAdapter() {
			@Override
			public void onMouseMotion(MouseMotionEvent e) { mouse.updatePosition((float) e.getMouseX(), (float) e.getMouseY()); }

			@Override
			public void onMouseMoved(MouseMovedEvent e) { mouse.updatePosition((float) e.getMouseX(), (float) e.getMouseY()); }
		});

		EventManager.addScrollListener(mouseScroll = new ScrollListener() {
			@Override
			public void onScrolled(ScrollEvent e) { mouse.updateWheel((float) e.getAmountX(), (float) e.getAmountY()); }
		});

		EventManager.addWindowResizeListener(windowResize = new WindowResizeListener() {
			@Override
			public void onResized(WindowResizeEvent e) {
				int w = e.getWidth();
				int h = e.getHeight();
				scaledWidth = w;
				scaledHeight = h;
				width = (int) (scaledWidth / scaleWidth);
				height = (int) (scaledHeight / scaleHeight);
				frameWidth = w;
				frameHeight = h;
				game.getGraphics().resize(width, height);
			}
		});

		EventManager.addKeyInputListener(game);
		EventManager.addMouseInputListener(game);
		EventManager.addMouseMotionListener(game);
		EventManager.addScrollListener(game);
		EventManager.addWindowResizeListener(game);
		EventManager.addWindowMoveListener(game);
	}

	@Override
	protected void destroyListeners() {
		EventManager.removeKeyInputListener(keyInput);
		EventManager.removeMouseInputListener(mouseInput);
		EventManager.removeMouseMotionListener(mouseMotion);
		EventManager.removeScrollListener(mouseScroll);
		EventManager.removeWindowResizeListener(windowResize);

		EventManager.removeKeyInputListener(game);
		EventManager.removeMouseInputListener(game);
		EventManager.removeMouseMotionListener(game);
		EventManager.removeScrollListener(game);
		EventManager.removeWindowResizeListener(game);
		EventManager.removeWindowMoveListener(game);
	}

	@Override
	public void defaultSettings() {
		//		overlay = new SGraphics(scaledWidth, scaledHeight);
		overlay = game.createGraphics(scaledWidth, scaledHeight);
		//		overlay.init();
		overlay.smooth();

		textureID = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
	}

	@Override
	public void show() { if (isCreated) window.setVisible(true); }

	@Override
	public void requestFocus() { if (isCreated) window.requestFocus(); }

	@Override
	public void setTitle(String title) {
		super.setTitle(title);
		if (isCreated) window.setTitle(title + title_info);
	}

	@Override
	public void setTitleInfo(String title_info) {
		super.setTitleInfo(title_info);
		if (isCreated) window.setTitle(title + title_info);
	}

	@Override
	public void toggleFullscreen() { window.toggleFullscreen(); }

	static float aimSize = 3;
	static Color aimColor = Color.black;

	@Override
	public final void drawOverlay() {
		overlay.clear();
		if (showInfo) drawInfo();
		if (showCrosshair) drawCrosshair();
		// if(showX) drawX();
		// if(show???) draw???();
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
		overlay.rect((int) (mouse.x() - aimSize), mouse.y() - aimStrokeWidth, (int) (aimSize * 2), aimStroke);
		overlay.rect(mouse.x() - aimStrokeWidth, (int) (mouse.y() - aimSize), aimStroke, (int) (aimSize * 2));
	}

	@Override
	public boolean addListener(EventListener listener) { return false; }

	@Override
	public boolean removeListener(EventListener listener) { return false; }

	@Override
	public boolean addListener(SEventListener listener) { return EventManager.addEventListener(listener); }

	@Override
	public boolean removeListener(SEventListener listener) { return EventManager.removeEventListener(listener); }

	@Override
	public void setCursor(Cursor cursor) { window.setCursor(cursor); }

	@Override
	public boolean hasFocus() { return window.hasFocus(); }

	@Override
	public int getX() { return window.getX(); }

	@Override
	public int getY() { return window.getY(); }

	@Override
	public int getScreenX() { return getX(); }

	@Override
	public int getScreenY() { return getY(); }

	//	@Override
	//	public GraphicsConfiguration getGC() { return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration(); }
}
