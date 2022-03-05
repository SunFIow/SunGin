package com.sunflow.game;

import java.awt.AWTException;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.sunflow.Settings;
import com.sunflow.engine.Keyboard;
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
import com.sunflow.engine.eventsystem.listeners.KeyInputListener;
import com.sunflow.engine.eventsystem.listeners.MouseInputListener;
import com.sunflow.engine.eventsystem.listeners.SEventListener;
import com.sunflow.engine.eventsystem.listeners.ScrollListener;
import com.sunflow.engine.eventsystem.listeners.WindowMoveListener;
import com.sunflow.engine.eventsystem.listeners.WindowResizeListener;
import com.sunflow.engine.screen.Screen;
import com.sunflow.engine.screen.ScreenJava;
import com.sunflow.engine.screen.ScreenOpenGL;
import com.sunflow.gfx.SFont;
import com.sunflow.gfx.SGFX;
import com.sunflow.gfx.SGraphics;
import com.sunflow.gfx.SGraphicsJava2D;
import com.sunflow.gfx.SImage;
import com.sunflow.gfx.SSurface;
import com.sunflow.interfaces.FrameLoopListener;
import com.sunflow.interfaces.GameLoopListener;
import com.sunflow.logging.LogManager;
import com.sunflow.math.OpenSimplexNoise;
import com.sunflow.math.SMatrix2D;
import com.sunflow.math.SMatrix_D;
import com.sunflow.math.SVector;
import com.sunflow.math3d.SMatrix3D;
import com.sunflow.util.GameUtils;
import com.sunflow.util.GeometryUtils;
import com.sunflow.util.LogUtils;
import com.sunflow.util.MathUtils;
import com.sunflow.util.SConstants;
import com.sunflow.util.SStyle;

public class GameBase implements SGFX,
		SConstants, MathUtils, GameUtils, GeometryUtils, LogUtils,
		MouseListener,
		MouseWheelListener, MouseMotionListener, KeyListener, ComponentListener,
		KeyInputListener, MouseInputListener, com.sunflow.engine.eventsystem.listeners.MouseMotionListener, ScrollListener, WindowResizeListener, WindowMoveListener {

	public static Settings settings = new Settings().defaultSettings();

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/**
	 * Position of the upper-lefthand corner of the editor window
	 * that launched this applet.
	 */
	static public final String ARGS_EDITOR_LOCATION = "--editor-location";

	static public final String ARGS_EXTERNAL = "--external";

	/**
	 * Location for where to position the applet window on screen.
	 * <p>
	 * This is used by the editor to when saving the previous applet
	 * location, or could be used by other classes to launch at a
	 * specific position on-screen.
	 */
	static public final String ARGS_LOCATION = "--location";

	/** Used by the PDE to suggest a display (set in prefs, passed on Run) */
	static public final String ARGS_DISPLAY = "--display";

	// static public final String ARGS_SPAN_DISPLAYS = "--span";

	static public final String ARGS_WINDOW_COLOR = "--window-color";

	static public final String ARGS_PRESENT = "--present";

	static public final String ARGS_STOP_COLOR = "--stop-color";

	static public final String ARGS_HIDE_STOP = "--hide-stop";

	/**
	 * Allows the user or PdeEditor to set a specific sketch folder path.
	 * <p>
	 * Used by PdeEditor to pass in the location where saveFrame()
	 * and all that stuff should write things.
	 */
	static public final String ARGS_SKETCH_FOLDER = "--sketch-path";

	static public final String ARGS_DENSITY = "--density";

	/**
	 * When run externally to a PdeEditor,
	 * this is sent by the sketch when it quits.
	 */
	static public final String EXTERNAL_STOP = "__STOP__";

	/**
	 * When run externally to a PDE Editor, this is sent by the applet
	 * whenever the window is moved.
	 * <p>
	 * This is used so that the editor can re-open the sketch window
	 * in the same position as the user last left it.
	 */
	static public final String EXTERNAL_MOVE = "__MOVE__";

	/** true if this sketch is being run by the PDE */
	boolean external = false;

	static final String ERROR_MIN_MAX = "Cannot use min() or max() on an empty array.";

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/**
	 * Current platform in use, one of the
	 * PConstants WINDOWS, MACOSX, MACOS9, LINUX or OTHER.
	 */
	static public int platform;

	static {
		String osname = System.getProperty("os.name");

		if (osname.indexOf("Mac") != -1) {
			platform = MACOSX;

		} else if (osname.indexOf("Windows") != -1) {
			platform = WINDOWS;

		} else if (osname.equals("Linux")) { // true for the ibm vm
			platform = LINUX;

		} else {
			platform = OTHER;
		}
	}

	//	protected GameBase game;

	protected SSurface surface;

	public SSurface getSurface() { return surface; }

	protected SGraphics g;

	public int displayWidth;
	public int displayHeight;

	private String sketchPath;

	boolean insideSettings;

	String renderer = JAVA2D;
	//  int quality = 2;
	int smooth = 1; // default smoothing (whatever that means for the renderer)

	boolean fullScreen;
	int display = -1; // use default
	GraphicsDevice[] displayDevices;
	// Unlike the others above, needs to be public to support
	// the pixelWidth and pixelHeight fields.
	public int pixelDensity = 1;
	int suggestedDensity = -1;

	boolean present;

	String outputPath;
	OutputStream outputStream;

	// Background default needs to be different from the default value in
	// PGraphics.backgroundColor, otherwise size(100, 100) bg spills over.
	// https://github.com/processing/processing/issues/2297
	int windowColor = 0xffDDDDDD;

	//	protected PScreen screen;

	protected Screen screen;

	public int[] pixels;

	public int width;
	public int height;
	public int pixelWidth;
	public int pixelHeight;

	protected Mouse mouse;
	protected int button;
	protected boolean mousePressed;
	protected float mouseX, mouseY;
	protected float pmouseX, pmouseY;
	protected float mouseWheel, mouseWheelX, mouseWheelY;

	protected Keyboard keyboard;
	protected int key;
	protected int keyCode;

	protected boolean noLoop;
	public boolean isRunning;
	public boolean isPaused;

	private Thread thread;
	private long startTime;

	// SYNC or ASYNC
	protected byte syncMode;

	protected int fps, tps;

	public int frameRate, tickRate;
	public int frameCount, tickCount;
	int frames, ticks;

	private float timePerTickNano, timePerTickMilli;
	private float timePerFrameNano, timePerFrameMilli;

	protected float tMultiplier, tMultiplierMax;
	protected float tElapsedTime, tElapsedTimeMax;

	protected float fMultiplier, fMultiplierMax;
	protected float fElapsedTime, fElapsedTimeMax;

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
		//		if (insideDraw) {
		//			new Thread(() -> {
		//				while (insideDraw);
		//				System.out.println("hurra");
		//				reset();
		//			}).start();
		//			return;
		//		}
		if (insideDraw) {
			reset = true;
			return;
		}
		stop();
		_refresh();
		refresh();
		start();
	}

	boolean reset;

	/**
	 * @param method
	 *            "size" or "fullScreen"
	 * @param args
	 *            parameters passed to the function so we can show the user
	 * @return true if safely inside the settings() method
	 */
	boolean insideSettings(String method, Object... args) {
		if (insideSettings) return true;

		final String url = "https://processing.org/reference/" + method + "_.html";
		if (!external) { // post a warning for users of Eclipse and other IDEs
			//			String argList = new StringList(args).join(", ");
			String argList = join(", ", args);
			System.err.println("When not using the PDE, " + method + "() can only be used inside settings().");
			System.err.println("Remove the " + method + "() method from setup(), and add the following:");
			System.err.println("public void settings() {");
			System.err.println("  " + method + "(" + argList + ");");
			System.err.println("}");
		}
		throw new IllegalStateException(method + "() cannot be used here, see " + url);
	}

	private String join(String separator, Object... args) {
		int count = args.length;
		if (count == 0) return "";

		StringBuilder sb = new StringBuilder();
		sb.append(args[0]);
		for (int i = 1; i < count; i++) {
			sb.append(separator);
			sb.append(args[i]);
		}
		return sb.toString();
	}

	void handleRunSketch(final String[] args, final GameBase constructedSketch) {

		System.setProperty("sun.awt.noerasebackground", "true");

		// Remove 60fps limit on the JavaFX "pulse" timer
		System.setProperty("javafx.animation.fullspeed", "true");
		// Catch any HeadlessException to provide more useful feedback
		try {
			// Call validate() while resize events are in progress
			Toolkit.getDefaultToolkit().setDynamicLayout(true);
		} catch (HeadlessException e) {
			System.err.println("Cannot run sketch without a display. Read this for possible solutions:");
			System.err.println("https://github.com/processing/processing/wiki/Running-without-a-Display");
			System.exit(1);
		}

		// So that the system proxy setting are used by default
		System.setProperty("java.net.useSystemProxies", "true");

		boolean external = false;
		int[] location = null;
		int[] editorLocation = null;

		String name = null;
		int windowColor = 0;
		int stopColor = 0xff808080;
		boolean hideStop = false;

		int displayNum = -1; // use default
		//	    boolean fullScreen = false;
		boolean present = false;
		//	    boolean spanDisplays = false;
		int density = -1;

		String param = null, value = null;
		String folder = calcSketchPath();

		int argIndex = 0;
		while (argIndex < args.length) {
			int equals = args[argIndex].indexOf('=');
			if (equals != -1) {
				param = args[argIndex].substring(0, equals);
				value = args[argIndex].substring(equals + 1);

				if (param.equals(ARGS_EDITOR_LOCATION)) {
					external = true;
					editorLocation = parseInt(split(value, ','));

				} else if (param.equals(ARGS_DISPLAY)) {
					displayNum = parseInt(value, -2);
					if (displayNum == -2) {
						// this means the display value couldn't be parsed properly
						System.err.println(value + " is not a valid choice for " + ARGS_DISPLAY);
						displayNum = -1; // use the default
					}

				} else if (param.equals(ARGS_WINDOW_COLOR)) {
					if (value.charAt(0) == '#' && value.length() == 7) {
						value = value.substring(1);
						windowColor = 0xff000000 | Integer.parseInt(value, 16);
					} else {
						System.err.println(ARGS_WINDOW_COLOR + " should be a # followed by six digits");
					}

				} else if (param.equals(ARGS_STOP_COLOR)) {
					if (value.charAt(0) == '#' && value.length() == 7) {
						value = value.substring(1);
						stopColor = 0xff000000 | Integer.parseInt(value, 16);
					} else {
						System.err.println(ARGS_STOP_COLOR + " should be a # followed by six digits");
					}

				} else if (param.equals(ARGS_SKETCH_FOLDER)) {
					folder = value;

				} else if (param.equals(ARGS_LOCATION)) {
					location = parseInt(split(value, ','));

				} else if (param.equals(ARGS_DENSITY)) {
					density = parseInt(value, -1);
					if (density == -1) {
						System.err.println("Could not parse " + value + " for " + ARGS_DENSITY);
					} else if (density != 1 && density != 2) {
						density = -1;
						System.err.println(ARGS_DENSITY + " should be 1 or 2");
					}
				}

			} else {
				if (args[argIndex].equals(ARGS_PRESENT)) {
					present = true;

					//	        } else if (args[argIndex].equals(ARGS_SPAN_DISPLAYS)) {
					//	          spanDisplays = true;

				} else if (args[argIndex].equals(ARGS_HIDE_STOP)) {
					hideStop = true;

				} else if (args[argIndex].equals(ARGS_EXTERNAL)) {
					external = true;

				} else {
					name = args[argIndex];
					break; // because of break, argIndex won't increment again
				}
			}
			argIndex++;
		}

		final GameBase sketch;
		if (constructedSketch != null) {
			sketch = constructedSketch;
		} else {
			try {
				Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(name);
				sketch = (GameBase) c.getDeclaredConstructor().newInstance();
			} catch (RuntimeException re) {
				// Don't re-package runtime exceptions
				throw re;
			} catch (Exception e) {
				// Package non-runtime exceptions so we can throw them freely
				throw new RuntimeException(e);
			}
		}

		if (platform == MACOSX) {
			try {
				final String td = "processing.core.ThinkDifferent";
				Class<?> thinkDifferent = Thread.currentThread().getContextClassLoader().loadClass(td);
				Method method = thinkDifferent.getMethod("init", new Class[] { GameBase.class });
				method.invoke(null, new Object[] { sketch });
			} catch (Exception e) {
				e.printStackTrace(); // That's unfortunate
			}
		}

		// Set the suggested display that's coming from the command line
		// (and most likely, from the PDE's preference setting).
		sketch.display = displayNum;

		// Set the suggested density that is coming from command line
		// (most likely set from the PDE based on a system DPI scaling)
		sketch.suggestedDensity = density;

		sketch.present = present;

		// For 3.0.1, moved this above handleSettings() so that loadImage() can be
		// used inside settings(). Sets a terrible precedent, but the alternative
		// of not being able to size a sketch to an image is driving people loopy.
		// A handful of things that need to be set before init/start.
		//		    if (folder == null) {
		//		      folder = calcSketchPath();
		//		    }
		sketch.sketchPath = folder;

		sketch.handleSettings();

		sketch.external = external;

		if (windowColor != 0) {
			sketch.windowColor = windowColor;
		}

		final SSurface surface = sketch.initSurface(); // TODO

		if (present) {
			if (hideStop) {
				stopColor = 0; // they'll get the hint
			}
			surface.placePresent(stopColor);
		} else {
			surface.placeWindow(location, editorLocation);
		}

		// not always running externally when in present mode
		// moved above setVisible() in 3.0 alpha 11
		if (sketch.external) {
			surface.setupExternalMessages();
		}
		//
		sketch.showSurface();
		sketch.startSurface();
	}

	/** Danger: available for advanced subclassing, but here be dragons. */
	protected void showSurface() {
		if (getGraphics().displayable()) {
			//			surface.setVisible(true);
		}
	}

	/** See warning in showSurface() */
	protected void startSurface() {
		//		surface.startThread();
	}

	protected SSurface initSurface() {
		g = createPrimaryGraphics();
		surface = g.createSurface();

		// Create fake Frame object to warn user about the changes
		//		if (g.displayable()) {
		//			frame = new Frame() {
		//				@Override
		//				public void setResizable(boolean resizable) {
		//					deprecationWarning("setResizable");
		//					surface.setResizable(resizable);
		//				}
		//
		//				@Override
		//				public void setVisible(boolean visible) {
		//					deprecationWarning("setVisible");
		//					surface.setVisible(visible);
		//				}
		//
		//				@Override
		//				public void setTitle(String title) {
		//					deprecationWarning("setTitle");
		//					surface.setTitle(title);
		//				}
		//
		//				@Override
		//				public void setUndecorated(boolean ignored) {
		//					throw new RuntimeException("'frame' has been removed from Processing 3, " +
		//							"use fullScreen() to get an undecorated full screen frame");
		//				}
		//
		//				// Can't override this one because it's called by Window's constructor
		//				/*
		//				 * @Override
		//				 * public void setLocation(int x, int y) {
		//				 * deprecationWarning("setLocation");
		//				 * surface.setLocation(x, y);
		//				 * }
		//				 */
		//
		//				@Override
		//				public void setSize(int w, int h) {
		//					deprecationWarning("setSize");
		//					surface.setSize(w, h);
		//				}
		//
		//				private void deprecationWarning(String method) {
		//					PGraphics.showWarning("Use surface." + method + "() instead of " +
		//							"frame." + method + " in Processing 3");
		//					// new Exception(method).printStackTrace(System.out);
		//				}
		//			};
		//
		//			surface.initFrame(this); // , backgroundColor, displayNum, fullScreen, spanDisplays);
		//			surface.setTitle(getClass().getSimpleName());
		//
		//		} else {
		//			surface.initOffscreen(this); // for PDF/PSurfaceNone and friends
		//		}

		//		    init();
		return surface;
	}

	void handleSettings() {
		insideSettings = true;

		// Need the list of display devices to be queried already for usage below.
		// https://github.com/processing/processing/issues/3295
		// https://github.com/processing/processing/issues/3296
		// Not doing this from a static initializer because it may cause
		// PApplet to cache and the values to stick through subsequent runs.
		// Instead make it a runtime thing and a local variable.
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = ge.getDefaultScreenDevice();
		displayDevices = ge.getScreenDevices();

		// Default or unparsed will be -1, spanning will be 0, actual displays will
		// be numbered from 1 because it's too weird to say "display 0" in prefs.
		if (display > 0 && display <= displayDevices.length) {
			device = displayDevices[display - 1];
		}
		// Set displayWidth and displayHeight for people still using those.
		DisplayMode displayMode = device.getDisplayMode();
		displayWidth = displayMode.getWidth();
		displayHeight = displayMode.getHeight();

		// Here's where size(), fullScreen(), smooth(N) and noSmooth() might
		// be called, conjuring up the demons of various rendering configurations.
		settings();

		if (display == SPAN && platform == MACOSX) {
			// Make sure "Displays have separate Spaces" is unchecked
			// in System Preferences > Mission Control
			Process p = exec("defaults", "read", "com.apple.spaces", "spans-displays");
			BufferedReader outReader = createReader(p.getInputStream());
			BufferedReader errReader = createReader(p.getErrorStream());
			StringBuilder stdout = new StringBuilder();
			StringBuilder stderr = new StringBuilder();
			String line = null;
			try {
				while ((line = outReader.readLine()) != null) {
					stdout.append(line);
				}
				while ((line = errReader.readLine()) != null) {
					stderr.append(line);
				}
			} catch (IOException e) {
				printStackTrace(e);
			}

			int resultCode = -1;
			try {
				resultCode = p.waitFor();
			} catch (InterruptedException e) {}

			String result = trim(stdout.toString());
			if ("0".equals(result)) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						checkLookAndFeel();
						final String msg = "To use fullScreen(SPAN), first turn off “Displays have separate spaces”\n" +
								"in System Preferences \u2192 Mission Control. Then log out and log back in.";
						JOptionPane.showMessageDialog(null, msg, "Apple's Defaults Stink",
								JOptionPane.WARNING_MESSAGE);
					}
				});
			} else if (!"1".equals(result)) {
				System.err.println("Could not check the status of “Displays have separate spaces.”");
				System.err.format("Received message '%s' and result code %d.%n", trim(stderr.toString()), resultCode);
			}
		}

		insideSettings = false;
	}

	public void settings() {
		// is this necessary? (doesn't appear to be, so removing)
		// size(DEFAULT_WIDTH, DEFAULT_HEIGHT, JAVA2D);
	}

	final public int getWidth() { return width; }

	final public int getHeight() { return height; }

	final public String getRenderer() { return renderer; }

	/*
	 * Named quality instead of smooth to avoid people trying to set (or get)
	 * the current smooth level this way. Also that smooth(number) isn't really
	 * public or well-known API. It's specific to the capabilities of the
	 * rendering surface, and somewhat independent of whether the sketch is
	 * smoothing at any given time. It's also a bit like getFill() would return
	 * true/false for whether fill was enabled, getFillColor() would return the
	 * color itself. Or at least that's what I can recall at the moment. [fry]
	 */
	//	public int sketchQuality() {
	//		// return 2;
	//		return quality;
	//	}

	// smoothing 1 is default.. 0 is none.. 2,4,8 depend on renderer
	final public int getSmooth() { return smooth; }

	final public boolean getFullScreen() {
		// return false;
		return fullScreen;
	}

	// // Could be named 'screen' instead of display since it's the people using
	// // full screen who will be looking for it. On the other hand, screenX/Y/Z
	// // makes things confusing, and if 'displayIndex' exists...
	// public boolean sketchSpanDisplays() {
	//	    //return false;
	//	    return spanDisplays;
	// }

	// Numbered from 1, SPAN (0) means all displays, -1 means the default display
	final public int getDisplay() { return display; }

	final public String getOutputPath() {
		// return null;
		return outputPath;
	}

	final public OutputStream getOutputStream() {
		// return null;
		return outputStream;
	}

	final public int getWindowColor() { return windowColor; }

	final public int getPixelDensity() { return pixelDensity; }

	public void setup() {}

	void _refresh() {
		frameCount = 0;
		frameRate = 0;
		tickCount = 0;
		tickRate = 0;

		tMultiplierMax = 0;
		tElapsedTimeMax = 0;

		width = 0;
		height = 0;

		fps = 0;

		screen.refresh();
	}

	public void refresh() {}

	public final void start() {
		if (isRunning) return;
		isRunning = true;

		thread = new Thread(() -> {
			init();
			loop();
			//			destroy();
		}, "MainThread");

		//		thread.start();
		thread.run();
	}

	public final void exit() { destroy(); }

	public final void stop() {
		if (!isRunning) return;
		isRunning = false;
	}

	public final void createCanvas(float width, float height) { createCanvas((int) width, (int) height, 1, 1); }

	public final void createCanvas(float width, float height, float scale) { createCanvas((int) width, (int) height, scale, scale); }

	public void createCanvas(float width, float height, float scaleW, float scaleH) { createCanvas((int) width, (int) height, scaleW, scaleH); }

	public final void createCanvas(int width, int height) { createCanvas(width, height, 1, 1); }

	public final void createCanvas(int width, int height, float scale) { createCanvas(width, height, scale, scale); }

	public void createCanvas(int width, int height, float scaleW, float scaleH) {
		this.width = width;
		this.height = height;

		screen.createCanvas(width, height, scaleW, scaleH);

		//		setParent(this);
		//		setPrimary(true);
		//		setSize(width(), height());
		//		graphics = checkImage();

		defaultSettings();
		screen.show();
		screen.requestFocus();
	}

	public final void title(String title) { screen.setTitle(title); }

	public final void undecorated(boolean undecorated) { screen.setUndecorated(undecorated); }

	void init() {
		final String className = this.getClass().getSimpleName();
		final String cleanedClass = className.replaceAll("__[^_]+__\\$", "").replaceAll("\\$\\d+", "");

		String[] args = new String[] { cleanedClass };

		//		final String[] sketchArgs = null;
		//		if (sketchArgs != null) args = concat(args, sketchArgs);

		handleRunSketch(args, this);

		//	game = this;
		random = new Random();
		noise = new OpenSimplexNoise(random.nextLong());
		mouse = new Mouse();
		keyboard = new Keyboard();

		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		//		aimSize = 8;
		//		aimColor = Color.black;

		frameRate(60);
		syncMode(SYNC);

		tMultiplierMax = 5;
		tElapsedTimeMax = timePerTickNano / NANOSECOND * tMultiplierMax;
		fMultiplierMax = 5;
		fElapsedTimeMax = timePerFrameNano / NANOSECOND * tMultiplierMax;

		gameLoopListeners = new ArrayList<>();
		frameLoopListeners = new ArrayList<>();
		startTime = System.currentTimeMillis();

		if (settings.screentype == Settings.ScreenType.OPENGL) screen = new ScreenOpenGL(this, mouse, keyboard);
		else screen = new ScreenJava(this, mouse, keyboard);

		//	setup();

		thread.setName(screen.title + " MainThread");
		screen.show();
	}

	private void tick() {
		screen.preUpdate();
		for (GameLoopListener gll : gameLoopListeners) gll.preUpdate();

		update();

		screen.postUpdate();
		for (GameLoopListener gll : gameLoopListeners) gll.postUpdate();

		ticks++;
		tickCount++;
	}

	public void update() {}

	boolean insideDraw;

	private void render() {
		if (g == null) return;
		//		if(noLoop && !redraw) return;

		if (insideDraw) {
			new IllegalStateException("render() called before finishing").printStackTrace();
			System.exit(1);
		}
		insideDraw = true;

		if (width != 0) g.beginDraw();

		if (frameCount == 0) {
			setup();
		} else {
			preDraw();

			push();
			draw();
			draw(((SGraphicsJava2D) g).graphics);
			pop();

			postDraw();
		}
		g.endDraw();

		EventManager.pollEvents();
		EventManager.processEvents(fElapsedTime);

		insideDraw = false;

		frames++;
		frameCount++;
	}

	void preDraw() {
		screen.preDraw();

		width = screen.width();
		height = screen.height();

		key = keyboard.key();
		keyCode = keyboard.keyCode();

		button = mouse.button();
		mousePressed = mouse.pressed();
		mouseX = mouse.x();
		mouseY = mouse.y();
		pmouseX = mouse.lastX();
		pmouseY = mouse.lastY();
		mouseWheel = mouse.mouseWheel();
		mouseWheelX = mouse.mouseWheelX();
		mouseWheelY = mouse.mouseWheelY();

		//	graphics = checkImage();
		//	handleSmooth();

		for (FrameLoopListener fll : frameLoopListeners) fll.preDraw();
	}

	void postDraw() {
		screen.postDraw();
		for (FrameLoopListener fll : frameLoopListeners) fll.postDraw();
	}

	public void draw() {}

	public void draw(Graphics2D g) {}

	void info() {
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
				tElapsedTime = (currentTime - lastTick) / (float) NANOSECOND;
				tElapsedTime = Math.min(tElapsedTime, tElapsedTimeMax);
				tMultiplier = (currentTime - lastTick) / timePerTickNano;
				tMultiplier = Math.min(tMultiplier, tMultiplierMax);
				lastTick = currentTime;

				if (!isPaused && syncMode == ASYNC) tick();
			}

			if (timeSinceLastFrame >= timePerFrameNano) {
				timeSinceLastFrame -= timePerFrameNano;
				fElapsedTime = (currentTime - lastFrame) / (float) NANOSECOND;
				fElapsedTime = Math.min(fElapsedTime, fElapsedTimeMax);
				fMultiplier = (currentTime - lastFrame) / timePerFrameNano;
				fMultiplier = Math.min(fMultiplier, fMultiplierMax);
				lastFrame = currentTime;

				if (!isPaused && syncMode == SYNC && frameCount > 0) tick();
				render();// TODO: USE STH ELSE
			}

			if (timeSinceLastInfoUpdate >= NANOSECOND) {
				info();
				timeSinceLastInfoUpdate -= NANOSECOND;
			}

			if (reset) {
				reset = false;
				reset();
			}
		}

		dispose(); // call to shutdown libs?

		// If the user called the exit() function, the window should close,
		// rather than the sketch just halting.
		if (exitCalled) {
			exitActual();
		}
	}

	private boolean exitCalled;

	void destroy() {
		if (isStopped()) {
			// exit immediately, dispose() has already been called,
			// meaning that the main thread has long since exited
			exitActual();

		} else if (isRunning) {
			// dispose() will be called as the thread exits
			isRunning = false;
			// tell the code to call exitActual() to do a System.exit()
			// once the next draw() has completed
			exitCalled = true;

		} else if (!isRunning) {
			dispose();

			// now get out
			exitActual();
		}
	}

	public boolean isStopped() { return thread == null || !thread.isAlive(); }

	void exitActual() {
		try {
			System.exit(0);
		} catch (SecurityException e) {
			// don't care about applet security exceptions
			e.printStackTrace();
		}
	}

	/**
	 * Called to dispose of resources and shut down the sketch.
	 * Destroys the thread, dispose the renderer,and notify listeners.
	 * <p>
	 * Not to be called or overriden by users. If called multiple times,
	 * will only notify listeners once. Register a dispose listener instead.
	 */
	public void dispose() {
		// moved here from stop()

		if (thread != null) {
			thread = null;
			if (g != null) g.dispose();
		}

		//		if (platform == MACOSX) {
		////			com.sun.glass.ui.Application application = null;
		////			if (application == null) {
		////				application = com.sun.glass.ui.Application.getApplication();
		////			}
		////			application.setQuitHandler(null);
		//
		//			try {
		//				final String td = "processing.core.ThinkDifferent";
		//				final Class<?> thinkDifferent = getClass().getClassLoader().loadClass(td);
		//				thinkDifferent.getMethod("cleanup").invoke(null);
		//			} catch (Exception e) {
		//				e.printStackTrace();
		//			}
		//		}

	}

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

	protected void defaultSettings() {
		screen.defaultSettings();

		showOverlay(false);
		showInfo(false);
		isPaused = false;

		g.pixelDensity = getPixelDensity();
		g.setSize(width, height);
		g.format = RGB;

		g.beginDraw();

		//		g.defaultSettings();

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
	 * adds "rec/" to the filename
	 * if it contains no "/".
	 */
	public final static void serialize(String filename, Serializable obj) { serialize(getFile(filename), obj); }

	/**
	 * Simple utility function to
	 * save an Serializable obj to a file
	 */
	public final static void serialize(File file, Serializable obj) {
		//		if (file.isAbsolute()) {
		//			// make sure that the intermediate folders have been created
		//			PApplet.createPath(file);
		//		} else {
		//			String msg = "SImage.save() requires an absolute path. " +
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
	 * adds "rec/" to the filename
	 * if it contains no "/".
	 */
	public final static Serializable deserialize(String filename) { return deserialize(getFile(filename)); }

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
	 * adds "rec/" to the filename
	 * if it contains no "/".
	 */
	private final static File getFile(String name) {
		if (!name.contains("/")) name = "rec/".concat(name);
		return new File(name);
	}

	public final void moveMouse(SVector v) { moveMouse(v.x(), v.y()); }

	public final void moveMouse(float x, float y) { moveMouseTo(mouse.x() + x, mouse.y() + y); }

	public final void moveMouseTo(SVector v) { moveMouseTo(v.x, v.y); }

	public final void moveMouseTo(float x, float y) { robot.mouseMove((int) (x() + x), (int) (y() + y)); }

	public final void moveMouseOnScreen(SVector v) { moveMouseOnScreen(v.x, v.y); }

	public final void moveMouseOnScreen(float x, float y) { moveMouseOnScreenTo(mouse.screenX() + x, mouse.screenY() + y); }

	public final void moveMouseOnScreenTo(SVector v) { moveMouseOnScreenTo(v.x, v.y); }

	public final void moveMouseOnScreenTo(float x, float y) { robot.mouseMove((int) x, (int) y); }

	@Override
	public final boolean save(String filename) { return saveFrame(filename); }

	public boolean saveFrame() {
		try {
			//			saveImage(image, savePath("screen-" + nf(frameCount, 4) + ".tif"));
			//			graphics.save(savePath("screen-" + nf(frameCount, 4) + ".tif"));
			return g.save(savePath("screen-" + nf(frameCount, 4) + ".tif"));
		} catch (SecurityException se) {
			System.err.println("Can't use saveFrame() when running in a browser, " +
					"unless using a signed applet.");
			return false;
		}
	}

	public boolean saveFrame(String filename) {
		try {
			//			saveImage(image, filename.replace(s, f));
			//			graphics.save(savePath(insertFrame(filename)));
			return g.save(savePath(insertFrame(filename)));
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

	public final float delta() { return tElapsedTime; }

	public final int x() { return screen.getX(); }

	public final int y() { return screen.getY(); }

	public final int frameX() { return screen.getScreenX(); }

	public final int frameY() { return screen.getScreenY(); }

	public final int width() { return screen.width(); }

	public final int height() { return screen.height(); }

	public final char key() { return keyboard.key(); }

	public final int keyCode() { return keyboard.keyCode(); }

	// public final boolean[] keys() { return keyboard.keys(); }

	public boolean isKeyDown(char key) { return isKeyDown(KeyEvent.getExtendedKeyCodeForChar(key)); }

	public boolean isKeyPressed(char key) { return isKeyPressed(KeyEvent.getExtendedKeyCodeForChar(key)); }

	public boolean isKeyHeld(char key) { return isKeyHeld(KeyEvent.getExtendedKeyCodeForChar(key)); }

	public boolean isKeyReleased(char key) { return isKeyReleased(KeyEvent.getExtendedKeyCodeForChar(key)); }

	public final boolean isKeyDown(int key) { return keyboard.isKeyDown(key); }

	public final boolean isKeyPressed(int key) { return keyboard.isKeyPressed(key); }

	public final boolean isKeyHeld(int key) { return keyboard.isKeyHeld(key); }

	public final boolean isKeyReleased(int key) { return keyboard.isKeyReleased(key); }

	public final float mouseX() { return mouse.x(); }

	public final float mouseY() { return mouse.y(); }

	public final float mouseLastX() { return mouse.lastX(); }

	public final float mouseLastY() { return mouse.lastY(); }

	public final int button() { return mouse.button(); }

	public final boolean mousePressed() { return mouse.pressed(); }

	// public int buttons() { return mouse.buttons(); }

	public final boolean isMouseDown(int button) { return mouse.isButtonDown(button); }

	public final boolean isMousePressed(int button) { return mouse.isButtonPressed(button); }

	public final boolean isMouseHeld(int button) { return mouse.isButtonHeld(button); }

	public final boolean isMouserReleased(int button) { return mouse.isButtonReleased(button); }

	public final double mouseWheel() { return mouse.mouseWheel(); }

	void updateMousePosition(float x, float y) {}

	/**
	 * <b>>GameBase.key</b> the character associated with the key in this event
	 * <p>
	 * <b>>GameBase.keyCode</b> the integer keyCode associated with the key in this event
	 * 
	 * @return <b>true</b> if default functionality should be skipped
	 */
	public boolean keyPressed() { return false; }

	/**
	 * <b>>GameBase.key</b> the character associated with the key in this event
	 * <p>
	 * <b>>GameBase.keyCode</b> the integer keyCode associated with the key in this event
	 * 
	 * @return <b>true</b> if default functionality should be skipped
	 */
	public boolean keyReleased() { return false; }

	/**
	 * <b>>GameBase.key</b> the character associated with the key in this event
	 * <p>
	 * <b>keyCode</b> the integer keyCode associated with the key in this event
	 * 
	 * @return <b>true</b> if default functionality should be skipped
	 */
	public boolean keyTyped() { return false; }

	/**
	 * <b>GameBase.key</b> the character associated with the key in this event
	 * <p>
	 * <b>>GameBase.keyCode</b> the integer keyCode associated with the key in this event
	 * 
	 * @return <b>true</b> if default functionality should be skipped
	 */
	public boolean mouseOnPressed() { return false; }

	/**
	 * <b>>GameBase.button</b> the button associated with this event
	 * 
	 * @return <b>true</b> if default functionality should be skipped
	 */
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
		LogManager.log(LogManager.FATAL, "Uncaught Exception", e);
		System.exit(1);
	}

	protected SGraphics createPrimaryGraphics() { return makeGraphics(getWidth(), getHeight(), renderer, outputPath, true); }

	//	protected SGraphics createGraphics(BufferedImage bi) { return new SGraphics(bi); }

	@Override
	public SGraphics createGraphics(int width, int height) { return createGraphics(width, height, SConstants.JAVA2D); }

	@Override
	public SGraphics createGraphics(int width, int height, String renderer) { return createGraphics(width, height, renderer, null); }

	public SGraphics createGraphics(int width, int height, String renderer, String path) { return makeGraphics(width, height, renderer, path, false); }

	protected SGraphics makeGraphics(int width, int height, String renderer, String path, boolean primary) {
		//		if (!primary && !g.isGL()) {
		//			if (renderer.equals(P2D)) {
		//				throw new RuntimeException("createGraphics() with P2D requires size() to use P2D or P3D");
		//			} else if (renderer.equals(P3D)) {
		//				throw new RuntimeException("createGraphics() with P3D or OPENGL requires size() to use P2D or P3D");
		//			}
		//		}

		try {
			Class<?> rendererClass = Thread.currentThread().getContextClassLoader().loadClass(renderer);

			Constructor<?> constructor = rendererClass.getConstructor(new Class[] {});
			SGraphics pg = (SGraphics) constructor.newInstance();

			pg.setParent(this);
			pg.setPrimary(primary);
			if (path != null) {
				pg.setPath(savePath(path));
			}
			//		      pg.setQuality(sketchQuality());
			//		      if (!primary) {
			//		        surface.initImage(pg, w, h);
			//		      }
			pg.setSize(width, height);

			// everything worked, return it
			return pg;

		} catch (InvocationTargetException ite) {
			String msg = ite.getTargetException().getMessage();
			if ((msg != null) &&
					(msg.indexOf("no jogl in java.library.path") != -1)) {
				// Is this true anymore, since the JARs contain the native libs?
				throw new RuntimeException("The jogl library folder needs to be " +
						"specified with -Djava.library.path=/path/to/jogl");

			} else {
				printStackTrace(ite.getTargetException());
				Throwable target = ite.getTargetException();
				/*
				 * // removing for 3.2, we'll see
				 * if (platform == MACOSX) {
				 * target.printStackTrace(System.out); // OS X bug (still true?)
				 * }
				 */
				throw new RuntimeException(target.getMessage());
			}

		} catch (ClassNotFoundException cnfe) {
			//		      if (cnfe.getMessage().indexOf("processing.opengl.PGraphicsOpenGL") != -1) {
			//		        throw new RuntimeException(openglError +
			//		                                   " (The library .jar file is missing.)");
			//		      } else {
			if (external) {
				throw new RuntimeException("You need to use \"Import Library\" " +
						"to add " + renderer + " to your sketch.");
			} else {
				throw new RuntimeException("The " + renderer +
						" renderer is not in the class path.");
			}

		} catch (Exception e) {
			if ((e instanceof IllegalArgumentException) ||
					(e instanceof NoSuchMethodException) ||
					(e instanceof IllegalAccessException)) {
				if (e.getMessage().contains("cannot be <= 0")) {
					// IllegalArgumentException will be thrown if w/h is <= 0
					// http://code.google.com/p/processing/issues/detail?id=983
					throw new RuntimeException(e);

				} else {
					printStackTrace(e);
					String msg = renderer + " needs to be updated " +
							"for the current release of Processing.";
					throw new RuntimeException(msg);
				}
			} else {
				/*
				 * if (platform == MACOSX) {
				 * e.printStackTrace(System.out); // OS X bug (still true?)
				 * }
				 */
				printStackTrace(e);
				throw new RuntimeException(e.getMessage());
			}
		}
	}

	public SImage createImage(int width, int height) {
		SImage img = new SImage(width, height);
		img.parent = this;
		return img;
	}

	public SImage createImage(int width, int height, int format) {
		SImage img = new SImage(width, height, format);
		img.parent = this;
		return img;
	}

	public SImage createImage(Image bi) {
		SImage img = new SImage(bi);
		img.parent = this;
		return img;
	}

	@Override
	public SImage loadImage(String filename) { return loadImage(filename, null); }

	protected String[] loadImageFormats;

	public SImage loadImage(String filename, String extension) { // , Object params) {

		// awaitAsyncSaveCompletion() has to run on the main thread, because P2D
		// and P3D call GL functions. If this runs on background, requestImage()
		// already called awaitAsyncSaveCompletion() on the main thread.
		if (g != null && !Thread.currentThread().getName().startsWith(REQUEST_IMAGE_THREAD_PREFIX)) {
			g.awaitAsyncSaveCompletion(filename);
		}

		if (extension == null) {
			String lower = filename.toLowerCase();
			int dot = filename.lastIndexOf('.');
			if (dot == -1) {
				extension = "unknown"; // no extension found

			} else {
				extension = lower.substring(dot + 1);

				// check for, and strip any parameters on the url, i.e.
				// filename.jpg?blah=blah&something=that
				int question = extension.indexOf('?');
				if (question != -1) {
					extension = extension.substring(0, question);
				}
			}
		}

		// just in case. them users will try anything!
		extension = extension.toLowerCase();

		if (extension.equals("tga")) {
			try {
				SImage image = loadImageTGA(filename);
				//	        if (params != null) {
				//	          image.setParams(g, params);
				//	        }
				return image;
			} catch (IOException e) {
				printStackTrace(e);
				return null;
			}
		}

		if (extension.equals("tif") || extension.equals("tiff")) {
			byte bytes[] = loadBytes(filename);
			SImage image = (bytes == null) ? null : SImage.loadTIFF(bytes);
			//	      if (params != null) {
			//	        image.setParams(g, params);
			//	      }
			return image;
		}

		// For jpeg, gif, and png, load them using createImage(),
		// because the javax.imageio code was found to be much slower.
		// http://dev.processing.org/bugs/show_bug.cgi?id=392
		try {
			if (extension.equals("jpg") || extension.equals("jpeg") ||
					extension.equals("gif") || extension.equals("png") ||
					extension.equals("unknown")) {
				byte bytes[] = loadBytes(filename);
				if (bytes == null) {
					return null;
				} else {
					// Image awtImage = Toolkit.getDefaultToolkit().createImage(bytes);
					Image awtImage = new ImageIcon(bytes).getImage();

					if (awtImage instanceof BufferedImage) {
						BufferedImage buffImage = (BufferedImage) awtImage;
						int space = buffImage.getColorModel().getColorSpace().getType();
						if (space == ColorSpace.TYPE_CMYK) {
							System.err.println(filename + " is a CMYK image, " +
									"only RGB images are supported.");
							return null;
							/*
							 * // wishful thinking, appears to not be supported
							 * // https://community.oracle.com/thread/1272045?start=0&tstart=0
							 * BufferedImage destImage =
							 * new BufferedImage(buffImage.getWidth(),
							 * buffImage.getHeight(),
							 * BufferedImage.TYPE_3BYTE_BGR);
							 * ColorConvertOp op = new ColorConvertOp(null);
							 * op.filter(buffImage, destImage);
							 * image = new SImage(destImage);
							 */
						}
					}

					SImage image = new SImage(awtImage);
					if (image.width == -1) {
						System.err.println("The file " + filename +
								" contains bad image data, or may not be an image.");
					}

					// if it's a .gif image, test to see if it has transparency
					if (extension.equals("gif") || extension.equals("png") ||
							extension.equals("unknown")) {
						image.checkAlpha();
					}

					//					if (params != null) {
					//						image.setParams(g, params);
					//					}
					image.parent = this;
					return image;
				}
			}
		} catch (Exception e) {
			// show error, but move on to the stuff below, see if it'll work
			printStackTrace(e);
		}

		if (loadImageFormats == null) {
			loadImageFormats = ImageIO.getReaderFormatNames();
		}
		if (loadImageFormats != null) {
			for (int i = 0; i < loadImageFormats.length; i++) {
				if (extension.equals(loadImageFormats[i])) {
					return loadImageIO(filename);
					//	          SImage image = loadImageIO(filename);
					//	          if (params != null) {
					//	            image.setParams(g, params);
					//	          }
					//	          return image;
				}
			}
		}

		// failed, could not load image after all those attempts
		System.err.println("Could not find a method to load " + filename);
		return null;
	}

	static private final String REQUEST_IMAGE_THREAD_PREFIX = "requestImage";
	// fixed-size thread pool used by requestImage()
	ExecutorService requestImagePool;

	public SImage requestImage(String filename) { return requestImage(filename, null); }

	/**
	 * ( begin auto-generated from requestImage.xml )
	 *
	 * This function load images on a separate thread so that your sketch does
	 * not freeze while images load during <b>setup()</b>. While the image is
	 * loading, its width and height will be 0. If an error occurs while
	 * loading the image, its width and height will be set to -1. You'll know
	 * when the image has loaded properly because its width and height will be
	 * greater than 0. Asynchronous image loading (particularly when
	 * downloading from a server) can dramatically improve performance.<br />
	 * <br/>
	 * <b>extension</b> parameter is used to determine the image type in
	 * cases where the image filename does not end with a proper extension.
	 * Specify the extension as the second parameter to <b>requestImage()</b>.
	 *
	 * ( end auto-generated )
	 *
	 * @webref image:loading_displaying
	 * @param filename
	 *            name of the file to load, can be .gif, .jpg, .tga, or a handful of other image types depending on your platform
	 * @param extension
	 *            the type of image to load, for example "png", "gif", "jpg"
	 * @see SImage
	 * @see PApplet#loadImage(String, String)
	 */
	public SImage requestImage(String filename, String extension) {
		// Make sure saving to this file completes before trying to load it
		// Has to be called on main thread, because P2D and P3D need GL functions
		if (g != null) {
			g.awaitAsyncSaveCompletion(filename);
		}
		SImage vessel = createImage(0, 0, ARGB);

		// if the image loading thread pool hasn't been created, create it
		if (requestImagePool == null) {
			ThreadFactory factory = new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) { return new Thread(r, REQUEST_IMAGE_THREAD_PREFIX); }
			};
			requestImagePool = Executors.newFixedThreadPool(4, factory);
		}
		requestImagePool.execute(() -> {
			SImage actual = loadImage(filename, extension);

			// An error message should have already printed
			if (actual == null) {
				vessel.width = -1;
				vessel.height = -1;

			} else {
				vessel.width = actual.width;
				vessel.height = actual.height;
				vessel.format = actual.format;
				vessel.pixels = actual.pixels;

				vessel.pixelWidth = actual.width;
				vessel.pixelHeight = actual.height;
				vessel.pixelDensity = 1;
			}
		});
		return vessel;
	}

	/**
	 * Use Java 1.4 ImageIO methods to load an image.
	 */
	protected SImage loadImageIO(String filename) {
		InputStream stream = createInput(filename);
		if (stream == null) {
			System.err.println("The image " + filename + " could not be found.");
			return null;
		}

		try {
			BufferedImage bi = ImageIO.read(stream);
			SImage outgoing = new SImage(bi.getWidth(), bi.getHeight());
			outgoing.parent = this;

			bi.getRGB(0, 0, outgoing.width, outgoing.height,
					outgoing.pixels, 0, outgoing.width);

			// check the alpha for this image
			// was gonna call getType() on the image to see if RGB or ARGB,
			// but it's not actually useful, since gif images will come through
			// as TYPE_BYTE_INDEXED, which means it'll still have to check for
			// the transparency. also, would have to iterate through all the other
			// types and guess whether alpha was in there, so.. just gonna stick
			// with the old method.
			outgoing.checkAlpha();

			stream.close();
			// return the image
			return outgoing;

		} catch (Exception e) {
			printStackTrace(e);
			return null;
		}
	}

	/**
	 * Targa image loader for RLE-compressed TGA files.
	 * <p>
	 * Rewritten for 0115 to read/write RLE-encoded targa images.
	 * For 0125, non-RLE encoded images are now supported, along with
	 * images whose y-order is reversed (which is standard for TGA files).
	 * <p>
	 * A version of this function is in MovieMaker.java. Any fixes here
	 * should be applied over in MovieMaker as well.
	 * <p>
	 * Known issue with RLE encoding and odd behavior in some apps:
	 * https://github.com/processing/processing/issues/2096
	 * Please help!
	 */
	protected SImage loadImageTGA(String filename) throws IOException {
		InputStream is = createInput(filename);
		if (is == null) return null;

		byte header[] = new byte[18];
		int offset = 0;
		do {
			int count = is.read(header, offset, header.length - offset);
			if (count == -1) return null;
			offset += count;
		} while (offset < 18);

		/*
		 * header[2] image type code
		 * 2 (0x02) - Uncompressed, RGB images.
		 * 3 (0x03) - Uncompressed, black and white images.
		 * 10 (0x0A) - Run-length encoded RGB images.
		 * 11 (0x0B) - Compressed, black and white images. (grayscale?)
		 * header[16] is the bit depth (8, 24, 32)
		 * header[17] image descriptor (packed bits)
		 * 0x20 is 32 = origin upper-left
		 * 0x28 is 32 + 8 = origin upper-left + 32 bits
		 * 7 6 5 4 3 2 1 0
		 * 128 64 32 16 8 4 2 1
		 */

		int format = 0;

		if (((header[2] == 3) || (header[2] == 11)) && // B&W, plus RLE or not
				(header[16] == 8) && // 8 bits
				((header[17] == 0x8) || (header[17] == 0x28))) { // origin, 32 bit
			format = ALPHA;

		} else if (((header[2] == 2) || (header[2] == 10)) && // RGB, RLE or not
				(header[16] == 24) && // 24 bits
				((header[17] == 0x20) || (header[17] == 0))) { // origin
					format = RGB;

				} else
			if (((header[2] == 2) || (header[2] == 10)) &&
					(header[16] == 32) &&
					((header[17] == 0x8) || (header[17] == 0x28))) { // origin, 32
						format = ARGB;
					}

		if (format == 0) {
			System.err.println("Unknown .tga file format for " + filename);
			// " (" + header[2] + " " +
			// (header[16] & 0xff) + " " +
			// hex(header[17], 2) + ")");
			return null;
		}

		int w = ((header[13] & 0xff) << 8) + (header[12] & 0xff);
		int h = ((header[15] & 0xff) << 8) + (header[14] & 0xff);
		SImage outgoing = createImage(w, h, format);

		// where "reversed" means upper-left corner (normal for most of
		// the modernized world, but "reversed" for the tga spec)
		// boolean reversed = (header[17] & 0x20) != 0;
		// https://github.com/processing/processing/issues/1682
		boolean reversed = (header[17] & 0x20) == 0;

		if ((header[2] == 2) || (header[2] == 3)) { // not RLE encoded
			if (reversed) {
				int index = (h - 1) * w;
				switch (format) {
					case ALPHA:
						for (int y = h - 1; y >= 0; y--) {
							for (int x = 0; x < w; x++) {
								outgoing.pixels[index + x] = is.read();
							}
							index -= w;
						}
						break;
					case RGB:
						for (int y = h - 1; y >= 0; y--) {
							for (int x = 0; x < w; x++) {
								outgoing.pixels[index + x] = is.read() | (is.read() << 8) | (is.read() << 16) |
										0xff000000;
							}
							index -= w;
						}
						break;
					case ARGB:
						for (int y = h - 1; y >= 0; y--) {
							for (int x = 0; x < w; x++) {
								outgoing.pixels[index + x] = is.read() | (is.read() << 8) | (is.read() << 16) |
										(is.read() << 24);
							}
							index -= w;
						}
				}
			} else { // not reversed
				int count = w * h;
				switch (format) {
					case ALPHA:
						for (int i = 0; i < count; i++) {
							outgoing.pixels[i] = is.read();
						}
						break;
					case RGB:
						for (int i = 0; i < count; i++) {
							outgoing.pixels[i] = is.read() | (is.read() << 8) | (is.read() << 16) |
									0xff000000;
						}
						break;
					case ARGB:
						for (int i = 0; i < count; i++) {
							outgoing.pixels[i] = is.read() | (is.read() << 8) | (is.read() << 16) |
									(is.read() << 24);
						}
						break;
				}
			}

		} else { // header[2] is 10 or 11
			int index = 0;
			int px[] = outgoing.pixels;

			while (index < px.length) {
				int num = is.read();
				boolean isRLE = (num & 0x80) != 0;
				if (isRLE) {
					num -= 127; // (num & 0x7F) + 1
					int pixel = 0;
					switch (format) {
						case ALPHA:
							pixel = is.read();
							break;
						case RGB:
							pixel = 0xFF000000 |
									is.read() | (is.read() << 8) | (is.read() << 16);
							// (is.read() << 16) | (is.read() << 8) | is.read();
							break;
						case ARGB:
							pixel = is.read() |
									(is.read() << 8) | (is.read() << 16) | (is.read() << 24);
							break;
					}
					for (int i = 0; i < num; i++) {
						px[index++] = pixel;
						if (index == px.length) break;
					}
				} else { // write up to 127 bytes as uncompressed
					num += 1;
					switch (format) {
						case ALPHA:
							for (int i = 0; i < num; i++) {
								px[index++] = is.read();
							}
							break;
						case RGB:
							for (int i = 0; i < num; i++) {
								px[index++] = 0xFF000000 |
										is.read() | (is.read() << 8) | (is.read() << 16);
								// (is.read() << 16) | (is.read() << 8) | is.read();
							}
							break;
						case ARGB:
							for (int i = 0; i < num; i++) {
								px[index++] = is.read() | // (is.read() << 24) |
										(is.read() << 8) | (is.read() << 16) | (is.read() << 24);
								// (is.read() << 16) | (is.read() << 8) | is.read();
							}
							break;
					}
				}
			}

			if (!reversed) {
				int[] temp = new int[w];
				for (int y = 0; y < h / 2; y++) {
					int z = (h - 1) - y;
					System.arraycopy(px, y * w, temp, 0, w);
					System.arraycopy(px, z * w, px, y * w, w);
					System.arraycopy(temp, 0, px, z * w, w);
				}
			}
		}
		is.close();
		return outgoing;
	}

	@Override
	public BufferedImage loadIOImage(String filename) {
		BufferedImage img = null;
		try {
			//		    URL url = new URL(getCodeBase(), "examples/strawberry.jpg");
			//		    img = ImageIO.read(url);
			File inputfile = new File(filename);
			System.out.println(inputfile.getAbsolutePath());
			img = ImageIO.read(inputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}

	//	protected SImage loadSImage(String filename, int format) {
	//		return new SImage(loadImage(filename, format));
	//	}

	public void saveImage(SImage image, String filename) { image.save(savePath(filename)); }

	//	public void saveImage(SImage image, String filename) { saveImage((BufferedImage)image.getImage(), filename); }

	public void saveImage(BufferedImage image, String filename) {
		try {
			Path dir = Paths.get(savePath(filename));
			if (Files.notExists(dir.getParent())) try {
				Files.createDirectory(dir.getParent());
			} catch (IOException e) {
				LogManager.error("File Directory could't be created", e);
			}
			File outputfile = dir.toFile();
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//////////////////////////////////////////////////////////////

	// READERS AND WRITERS

	/**
	 * ( begin auto-generated from createReader.xml )
	 *
	 * Creates a <b>BufferedReader</b> object that can be used to read files
	 * line-by-line as individual <b>String</b> objects. This is the complement
	 * to the <b>createWriter()</b> function.
	 * <br/>
	 * <br/>
	 * Starting with Processing release 0134, all files loaded and saved by the
	 * Processing API use UTF-8 encoding. In previous releases, the default
	 * encoding for your platform was used, which causes problems when files
	 * are moved to other platforms.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref input:files
	 * @param filename
	 *            name of the file to be opened
	 * @see BufferedReader
	 * @see PApplet#createWriter(String)
	 * @see PrintWriter
	 */
	public BufferedReader createReader(String filename) {
		InputStream is = createInput(filename);
		if (is == null) {
			System.err.println("The file \"" + filename + "\" " +
					"is missing or inaccessible, make sure " +
					"the URL is valid or that the file has been " +
					"added to your sketch and is readable.");
			return null;
		}
		return createReader(is);
	}

	/**
	 * @nowebref
	 */
	static public BufferedReader createReader(File file) {
		try {
			InputStream is = new FileInputStream(file);
			if (file.getName().toLowerCase().endsWith(".gz")) {
				is = new GZIPInputStream(is);
			}
			return createReader(is);

		} catch (IOException e) {
			// Re-wrap rather than forcing novices to learn about exceptions
			throw new RuntimeException(e);
		}
	}

	/**
	 * @nowebref
	 *           I want to read lines from a stream. If I have to type the
	 *           following lines any more I'm gonna send Sun my medical bills.
	 */
	static public BufferedReader createReader(InputStream input) {
		InputStreamReader isr = new InputStreamReader(input, StandardCharsets.UTF_8);

		BufferedReader reader = new BufferedReader(isr);
		// consume the Unicode BOM (byte order marker) if present
		try {
			reader.mark(1);
			int c = reader.read();
			// if not the BOM, back up to the beginning again
			if (c != '\uFEFF') {
				reader.reset();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reader;
	}

	/**
	 * ( begin auto-generated from createWriter.xml )
	 *
	 * Creates a new file in the sketch folder, and a <b>PrintWriter</b> object
	 * to write to it. For the file to be made correctly, it should be flushed
	 * and must be closed with its <b>flush()</b> and <b>close()</b> methods
	 * (see above example).
	 * <br/>
	 * <br/>
	 * Starting with Processing release 0134, all files loaded and saved by the
	 * Processing API use UTF-8 encoding. In previous releases, the default
	 * encoding for your platform was used, which causes problems when files
	 * are moved to other platforms.
	 *
	 * ( end auto-generated )
	 *
	 * @webref output:files
	 * @param filename
	 *            name of the file to be created
	 * @see PrintWriter
	 * @see PApplet#createReader
	 * @see BufferedReader
	 */
	public PrintWriter createWriter(String filename) { return createWriter(saveFile(filename)); }

	/**
	 * @nowebref
	 *           I want to print lines to a file. I have RSI from typing these
	 *           eight lines of code so many times.
	 */
	static public PrintWriter createWriter(File file) {
		if (file == null) {
			throw new RuntimeException("File passed to createWriter() was null");
		}
		try {
			createPath(file); // make sure in-between folders exist
			OutputStream output = new FileOutputStream(file);
			if (file.getName().toLowerCase().endsWith(".gz")) {
				output = new GZIPOutputStream(output);
			}
			return createWriter(output);

		} catch (Exception e) {
			throw new RuntimeException("Couldn't create a writer for " +
					file.getAbsolutePath(), e);
		}
	}

	/**
	 * @nowebref
	 *           I want to print lines to a file. Why am I always explaining myself?
	 *           It's the JavaSoft API engineers who need to explain themselves.
	 */
	static public PrintWriter createWriter(OutputStream output) {
		BufferedOutputStream bos = new BufferedOutputStream(output, 8192);
		OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
		return new PrintWriter(osw);
	}

	//////////////////////////////////////////////////////////////

	// FILE INPUT

	/**
	 * ( begin auto-generated from createInput.xml )
	 *
	 * This is a function for advanced programmers to open a Java InputStream.
	 * It's useful if you want to use the facilities provided by PApplet to
	 * easily open files from the data folder or from a URL, but want an
	 * InputStream object so that you can use other parts of Java to take more
	 * control of how the stream is read.<br />
	 * <br />
	 * The filename passed in can be:<br />
	 * - A URL, for instance <b>openStream("http://processing.org/")</b><br />
	 * - A file in the sketch's <b>data</b> folder<br />
	 * - The full path to a file to be opened locally (when running as an
	 * application)<br />
	 * <br />
	 * If the requested item doesn't exist, null is returned. If not online,
	 * this will also check to see if the user is asking for a file whose name
	 * isn't properly capitalized. If capitalization is different, an error
	 * will be printed to the console. This helps prevent issues that appear
	 * when a sketch is exported to the web, where case sensitivity matters, as
	 * opposed to running from inside the Processing Development Environment on
	 * Windows or Mac OS, where case sensitivity is preserved but ignored.<br />
	 * <br />
	 * If the file ends with <b>.gz</b>, the stream will automatically be gzip
	 * decompressed. If you don't want the automatic decompression, use the
	 * related function <b>createInputRaw()</b>.
	 * <br />
	 * In earlier releases, this function was called <b>openStream()</b>.<br />
	 * <br />
	 *
	 * ( end auto-generated )
	 *
	 * <h3>Advanced</h3>
	 * Simplified method to open a Java InputStream.
	 * <p>
	 * This method is useful if you want to use the facilities provided
	 * by PApplet to easily open things from the data folder or from a URL,
	 * but want an InputStream object so that you can use other Java
	 * methods to take more control of how the stream is read.
	 * <p>
	 * If the requested item doesn't exist, null is returned.
	 * (Prior to 0096, die() would be called, killing the applet)
	 * <p>
	 * For 0096+, the "data" folder is exported intact with subfolders,
	 * and openStream() properly handles subdirectories from the data folder
	 * <p>
	 * If not online, this will also check to see if the user is asking
	 * for a file whose name isn't properly capitalized. This helps prevent
	 * issues when a sketch is exported to the web, where case sensitivity
	 * matters, as opposed to Windows and the Mac OS default where
	 * case sensitivity is preserved but ignored.
	 * <p>
	 * It is strongly recommended that libraries use this method to open
	 * data files, so that the loading sequence is handled in the same way
	 * as functions like loadBytes(), loadImage(), etc.
	 * <p>
	 * The filename passed in can be:
	 * <UL>
	 * <LI>A URL, for instance openStream("http://processing.org/");
	 * <LI>A file in the sketch's data folder
	 * <LI>Another file to be opened locally (when running as an application)
	 * </UL>
	 *
	 * @webref input:files
	 * @param filename
	 *            the name of the file to use as input
	 * @see PApplet#createOutput(String)
	 * @see PApplet#selectOutput(String,String)
	 * @see PApplet#selectInput(String,String)
	 *
	 */
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

	/**
	 * Call openStream() without automatic gzip decompression.
	 */
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

	/**
	 * @nowebref
	 */
	static public InputStream createInput(File file) {
		if (file == null) {
			throw new IllegalArgumentException("File passed to createInput() was null");
		}
		if (!file.exists()) {
			System.err.println(file + " does not exist, createInput() will return null");
			return null;
		}
		try {
			InputStream input = new FileInputStream(file);
			final String lower = file.getName().toLowerCase();
			if (lower.endsWith(".gz") || lower.endsWith(".svgz")) {
				return new BufferedInputStream(new GZIPInputStream(input));
			}
			return new BufferedInputStream(input);

		} catch (IOException e) {
			System.err.println("Could not createInput() for " + file);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ( begin auto-generated from loadBytes.xml )
	 *
	 * Reads the contents of a file or url and places it in a byte array. If a
	 * file is specified, it must be located in the sketch's "data"
	 * directory/folder.<br />
	 * <br />
	 * The filename parameter can also be a URL to a file found online. For
	 * security reasons, a Processing sketch found online can only download
	 * files from the same server from which it came. Getting around this
	 * restriction requires a <a
	 * href="http://wiki.processing.org/w/Sign_an_Applet">signed applet</a>.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref input:files
	 * @param filename
	 *            name of a file in the data folder or a URL.
	 * @see PApplet#loadStrings(String)
	 * @see PApplet#saveStrings(String, String[])
	 * @see PApplet#saveBytes(String, byte[])
	 *
	 */
	public byte[] loadBytes(String filename) {
		String lower = filename.toLowerCase();
		// If it's not a .gz file, then we might be able to uncompress it into
		// a fixed-size buffer, which should help speed because we won't have to
		// reallocate and resize the target array each time it gets full.
		if (!lower.endsWith(".gz")) {
			// If this looks like a URL, try to load it that way. Use the fact that
			// URL connections may have a content length header to size the array.
			if (filename.contains(":")) { // at least smells like URL
				InputStream input = null;
				try {
					URL url = new URL(filename);
					URLConnection conn = url.openConnection();
					int length = -1;

					if (conn instanceof HttpURLConnection) {
						HttpURLConnection httpConn = (HttpURLConnection) conn;
						// Will not handle a protocol change (see below)
						httpConn.setInstanceFollowRedirects(true);
						int response = httpConn.getResponseCode();
						// Default won't follow HTTP -> HTTPS redirects for security reasons
						// http://stackoverflow.com/a/1884427
						if (response >= 300 && response < 400) {
							String newLocation = httpConn.getHeaderField("Location");
							return loadBytes(newLocation);
						}
						length = conn.getContentLength();
						input = conn.getInputStream();
					} else if (conn instanceof JarURLConnection) {
						length = conn.getContentLength();
						input = url.openStream();
					}

					if (input != null) {
						byte[] buffer = null;
						if (length != -1) {
							buffer = new byte[length];
							int count;
							int offset = 0;
							while ((count = input.read(buffer, offset, length - offset)) > 0) {
								offset += count;
							}
						} else {
							buffer = loadBytes(input);
						}
						input.close();
						return buffer;
					}
				} catch (MalformedURLException mfue) {
					// not a url, that's fine

				} catch (FileNotFoundException fnfe) {
					// Java 1.5+ throws FNFE when URL not available
					// http://dev.processing.org/bugs/show_bug.cgi?id=403

				} catch (IOException e) {
					printStackTrace(e);
					return null;

				} finally {
					if (input != null) {
						try {
							input.close();
						} catch (IOException e) {
							// just deal
						}
					}
				}
			}
		}

		InputStream is = createInput(filename);
		if (is != null) {
			byte[] outgoing = loadBytes(is);
			try {
				is.close();
			} catch (IOException e) {
				printStackTrace(e); // shouldn't happen
			}
			return outgoing;
		}

		System.err.println("The file \"" + filename + "\" " +
				"is missing or inaccessible, make sure " +
				"the URL is valid or that the file has been " +
				"added to your sketch and is readable.");
		return null;
	}

	/**
	 * @nowebref
	 */
	static public byte[] loadBytes(InputStream input) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];

			int bytesRead = input.read(buffer);
			while (bytesRead != -1) {
				out.write(buffer, 0, bytesRead);
				bytesRead = input.read(buffer);
			}
			out.flush();
			return out.toByteArray();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @nowebref
	 */
	static public byte[] loadBytes(File file) {
		if (!file.exists()) {
			System.err.println(file + " does not exist, loadBytes() will return null");
			return null;
		}

		try {
			InputStream input;
			int length;

			if (file.getName().toLowerCase().endsWith(".gz")) {
				RandomAccessFile raf = new RandomAccessFile(file, "r");
				raf.seek(raf.length() - 4);
				int b4 = raf.read();
				int b3 = raf.read();
				int b2 = raf.read();
				int b1 = raf.read();
				length = (b1 << 24) | (b2 << 16) + (b3 << 8) + b4;
				raf.close();

				// buffered has to go *around* the GZ, otherwise 25x slower
				input = new BufferedInputStream(new GZIPInputStream(new FileInputStream(file)));

			} else {
				long len = file.length();
				// http://stackoverflow.com/a/3039805
				int maxArraySize = Integer.MAX_VALUE - 5;
				if (len > maxArraySize) {
					System.err.println("Cannot use loadBytes() on a file larger than " + maxArraySize);
					return null;
				}
				length = (int) len;
				input = new BufferedInputStream(new FileInputStream(file));
			}
			byte[] buffer = new byte[length];
			int count;
			int offset = 0;
			// count will come back 0 when complete (or -1 if somehow going long?)
			while ((count = input.read(buffer, offset, length - offset)) > 0) {
				offset += count;
			}
			input.close();
			return buffer;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @nowebref
	 */
	static public String[] loadStrings(File file) {
		if (!file.exists()) {
			System.err.println(file + " does not exist, loadStrings() will return null");
			return null;
		}

		InputStream is = createInput(file);
		if (is != null) {
			String[] outgoing = loadStrings(is);
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return outgoing;
		}
		return null;
	}

	/**
	 * ( begin auto-generated from loadStrings.xml )
	 *
	 * Reads the contents of a file or url and creates a String array of its
	 * individual lines. If a file is specified, it must be located in the
	 * sketch's "data" directory/folder.<br />
	 * <br />
	 * The filename parameter can also be a URL to a file found online. For
	 * security reasons, a Processing sketch found online can only download
	 * files from the same server from which it came. Getting around this
	 * restriction requires a <a
	 * href="http://wiki.processing.org/w/Sign_an_Applet">signed applet</a>.
	 * <br />
	 * If the file is not available or an error occurs, <b>null</b> will be
	 * returned and an error message will be printed to the console. The error
	 * message does not halt the program, however the null value may cause a
	 * NullPointerException if your code does not check whether the value
	 * returned is null.
	 * <br/>
	 * <br/>
	 * Starting with Processing release 0134, all files loaded and saved by the
	 * Processing API use UTF-8 encoding. In previous releases, the default
	 * encoding for your platform was used, which causes problems when files
	 * are moved to other platforms.
	 *
	 * ( end auto-generated )
	 *
	 * <h3>Advanced</h3>
	 * Load data from a file and shove it into a String array.
	 * <p>
	 * Exceptions are handled internally, when an error, occurs, an
	 * exception is printed to the console and 'null' is returned,
	 * but the program continues running. This is a tradeoff between
	 * 1) showing the user that there was a problem but 2) not requiring
	 * that all i/o code is contained in try/catch blocks, for the sake
	 * of new users (or people who are just trying to get things done
	 * in a "scripting" fashion. If you want to handle exceptions,
	 * use Java methods for I/O.
	 *
	 * @webref input:files
	 * @param filename
	 *            name of the file or url to load
	 * @see PApplet#loadBytes(String)
	 * @see PApplet#saveStrings(String, String[])
	 * @see PApplet#saveBytes(String, byte[])
	 */
	public String[] loadStrings(String filename) {
		InputStream is = createInput(filename);
		if (is != null) {
			String[] strArr = loadStrings(is);
			try {
				is.close();
			} catch (IOException e) {
				printStackTrace(e);
			}
			return strArr;
		}

		System.err.println("The file \"" + filename + "\" " +
				"is missing or inaccessible, make sure " +
				"the URL is valid or that the file has been " +
				"added to your sketch and is readable.");
		return null;
	}

	/**
	 * @nowebref
	 */
	static public String[] loadStrings(InputStream input) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
			return loadStrings(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	static public String[] loadStrings(BufferedReader reader) {
		try {
			String lines[] = new String[100];
			int lineCount = 0;
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (lineCount == lines.length) {
					String temp[] = new String[lineCount << 1];
					System.arraycopy(lines, 0, temp, 0, lineCount);
					lines = temp;
				}
				lines[lineCount++] = line;
			}
			reader.close();

			if (lineCount == lines.length) {
				return lines;
			}

			// resize array to appropriate amount for these lines
			String output[] = new String[lineCount];
			System.arraycopy(lines, 0, output, 0, lineCount);
			return output;

		} catch (IOException e) {
			e.printStackTrace();
			// throw new RuntimeException("Error inside loadStrings()");
		}
		return null;
	}

	//////////////////////////////////////////////////////////////

	// FILE OUTPUT

	public OutputStream createOutput(String filename) { return createOutput(saveFile(filename)); }

	static public OutputStream createOutput(File file) {
		try {
			createPath(file); // make sure the path exists
			OutputStream output = new FileOutputStream(file);
			if (file.getName().toLowerCase().endsWith(".gz")) {
				return new BufferedOutputStream(new GZIPOutputStream(output));
			}
			return new BufferedOutputStream(output);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ( begin auto-generated from saveStream.xml )
	 *
	 * Save the contents of a stream to a file in the sketch folder. This is
	 * basically <b>saveBytes(blah, loadBytes())</b>, but done more efficiently
	 * (and with less confusing syntax).<br />
	 * <br />
	 * When using the <b>targetFile</b> parameter, it writes to a <b>File</b>
	 * object for greater control over the file location. (Note that unlike
	 * some other functions, this will not automatically compress or uncompress
	 * gzip files.)
	 *
	 * ( end auto-generated )
	 *
	 * @webref output:files
	 * @param target
	 *            name of the file to write to
	 * @param source
	 *            location to read from (a filename, path, or URL)
	 * @see PApplet#createOutput(String)
	 */
	public boolean saveStream(String target, String source) { return saveStream(saveFile(target), source); }

	/**
	 * Identical to the other saveStream(), but writes to a File
	 * object, for greater control over the file location.
	 * <p/>
	 * Note that unlike other api methods, this will not automatically
	 * compress or uncompress gzip files.
	 */
	public boolean saveStream(File target, String source) { return saveStream(target, createInputRaw(source)); }

	/**
	 * @nowebref
	 */
	public boolean saveStream(String target, InputStream source) { return saveStream(saveFile(target), source); }

	/**
	 * @nowebref
	 */
	static public boolean saveStream(File target, InputStream source) {
		File tempFile = null;
		try {
			// make sure that this path actually exists before writing
			createPath(target);
			tempFile = createTempFile(target);
			FileOutputStream targetStream = new FileOutputStream(tempFile);

			saveStream(targetStream, source);
			targetStream.close();
			targetStream = null;

			if (target.exists()) {
				if (!target.delete()) {
					System.err.println("Could not replace " +
							target.getAbsolutePath() + ".");
				}
			}
			if (!tempFile.renameTo(target)) {
				System.err.println("Could not rename temporary file " +
						tempFile.getAbsolutePath());
				return false;
			}
			return true;

		} catch (IOException e) {
			if (tempFile != null) {
				tempFile.delete();
			}
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @nowebref
	 */
	static public void saveStream(OutputStream target,
			InputStream source) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(source, 16384);
		BufferedOutputStream bos = new BufferedOutputStream(target);

		byte[] buffer = new byte[8192];
		int bytesRead;
		while ((bytesRead = bis.read(buffer)) != -1) {
			bos.write(buffer, 0, bytesRead);
		}

		bos.flush();
	}

	/**
	 * ( begin auto-generated from saveBytes.xml )
	 *
	 * Opposite of <b>loadBytes()</b>, will write an entire array of bytes to a
	 * file. The data is saved in binary format. This file is saved to the
	 * sketch's folder, which is opened by selecting "Show sketch folder" from
	 * the "Sketch" menu.<br />
	 * <br />
	 * It is not possible to use saveXxxxx() functions inside a web browser
	 * unless the sketch is <a
	 * href="http://wiki.processing.org/w/Sign_an_Applet">signed applet</A>. To
	 * save a file back to a server, see the <a
	 * href="http://wiki.processing.org/w/Saving_files_to_a_web-server">save to
	 * web</A> code snippet on the Processing Wiki.
	 *
	 * ( end auto-generated )
	 *
	 * @webref output:files
	 * @param filename
	 *            name of the file to write to
	 * @param data
	 *            array of bytes to be written
	 * @see PApplet#loadStrings(String)
	 * @see PApplet#loadBytes(String)
	 * @see PApplet#saveStrings(String, String[])
	 */
	public void saveBytes(String filename, byte[] data) { saveBytes(saveFile(filename), data); }

	/**
	 * Creates a temporary file based on the name/extension of another file
	 * and in the same parent directory. Ensures that the same extension is used
	 * (i.e. so that .gz files are gzip compressed on output) and that it's done
	 * from the same directory so that renaming the file later won't cross file
	 * system boundaries.
	 */
	static private File createTempFile(File file) throws IOException {
		File parentDir = file.getParentFile();
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}
		String name = file.getName();
		String prefix;
		String suffix = null;
		int dot = name.lastIndexOf('.');
		if (dot == -1) {
			prefix = name;
		} else {
			// preserve the extension so that .gz works properly
			prefix = name.substring(0, dot);
			suffix = name.substring(dot);
		}
		// Prefix must be three characters
		if (prefix.length() < 3) {
			prefix += "processing";
		}
		return File.createTempFile(prefix, suffix, parentDir);
	}

	/**
	 * @nowebref
	 *           Saves bytes to a specific File location specified by the user.
	 */
	static public void saveBytes(File file, byte[] data) {
		File tempFile = null;
		try {
			tempFile = createTempFile(file);

			OutputStream output = createOutput(tempFile);
			saveBytes(output, data);
			output.close();
			output = null;

			if (file.exists()) {
				if (!file.delete()) {
					System.err.println("Could not replace " + file.getAbsolutePath());
				}
			}

			if (!tempFile.renameTo(file)) {
				System.err.println("Could not rename temporary file " +
						tempFile.getAbsolutePath());
			}

		} catch (IOException e) {
			System.err.println("error saving bytes to " + file);
			if (tempFile != null) {
				tempFile.delete();
			}
			e.printStackTrace();
		}
	}

	/**
	 * @nowebref
	 *           Spews a buffer of bytes to an OutputStream.
	 */
	static public void saveBytes(OutputStream output, byte[] data) {
		try {
			output.write(data);
			output.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//

	/**
	 * ( begin auto-generated from saveStrings.xml )
	 *
	 * Writes an array of strings to a file, one line per string. This file is
	 * saved to the sketch's folder, which is opened by selecting "Show sketch
	 * folder" from the "Sketch" menu.<br />
	 * <br />
	 * It is not possible to use saveXxxxx() functions inside a web browser
	 * unless the sketch is <a
	 * href="http://wiki.processing.org/w/Sign_an_Applet">signed applet</A>. To
	 * save a file back to a server, see the <a
	 * href="http://wiki.processing.org/w/Saving_files_to_a_web-server">save to
	 * web</A> code snippet on the Processing Wiki.<br/>
	 * <br/ >
	 * Starting with Processing 1.0, all files loaded and saved by the
	 * Processing API use UTF-8 encoding. In previous releases, the default
	 * encoding for your platform was used, which causes problems when files
	 * are moved to other platforms.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref output:files
	 * @param filename
	 *            filename for output
	 * @param data
	 *            string array to be written
	 * @see PApplet#loadStrings(String)
	 * @see PApplet#loadBytes(String)
	 * @see PApplet#saveBytes(String, byte[])
	 */
	public void saveStrings(String filename, String data[]) { saveStrings(saveFile(filename), data); }

	/**
	 * @nowebref
	 */
	static public void saveStrings(File file, String data[]) { saveStrings(createOutput(file), data); }

	/**
	 * @nowebref
	 */
	static public void saveStrings(OutputStream output, String[] data) {
		PrintWriter writer = createWriter(output);
		for (int i = 0; i < data.length; i++) {
			writer.println(data[i]);
		}
		writer.flush();
		writer.close();
	}

	//////////////////////////////////////////////////////////////

	protected static String calcSketchPath() {
		// try to get the user folder. if running under java web start,
		// this may cause a security exception if the code is not signed.
		// http://processing.org/discourse/yabb_beta/YaBB.cgi?board=Integrate;action=display;num=1159386274
		String folder = null;
		try {
			folder = System.getProperty("user.dir");

			URL jarURL = GameBase.class.getProtectionDomain().getCodeSource().getLocation();
			// Decode URL
			String jarPath = jarURL.toURI().getSchemeSpecificPart();

			//			// Workaround for bug in Java for OS X from Oracle (7u51)
			//			// https://github.com/processing/processing/issues/2181
			if (platform == MACOSX) {
				if (jarPath.contains("Contents/Java/")) {
					String appPath = jarPath.substring(0, jarPath.indexOf(".app") + 4);
					File containingFolder = new File(appPath).getParentFile();
					folder = containingFolder.getAbsolutePath();
				}
			} else {
				// Working directory may not be set properly, try some options
				// https://github.com/processing/processing/issues/2195
				if (jarPath.contains("/lib/")) {
					// Windows or Linux, back up a directory to get the executable
					folder = new File(jarPath, "../..").getCanonicalPath();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return folder;
	}

	public String sketchPath() {
		if (sketchPath == null) {
			sketchPath = calcSketchPath();
		}
		return sketchPath;
	}

	public String sketchPath(String where) {
		if (sketchPath() == null) {
			return where;
		}
		// isAbsolute() could throw an access exception, but so will writing
		// to the local disk using the sketch path, so this is safe here.
		// for 0120, added a try/catch anyways.
		try {
			if (new File(where).isAbsolute()) return where;
		} catch (Exception e) {}

		return sketchPath() + File.separator + where;
	}

	public File sketchFile(String where) { return new File(sketchPath(where)); }

	public String savePath(String where) {
		if (where == null) return null;
		String filename = sketchPath(where);
		createPath(filename);
		return filename;
	}

	public File saveFile(String where) { return new File(savePath(where)); }

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
	public String dataPath(String where) { return dataFile(where).getAbsolutePath(); }

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

	public static void createPath(String path) { createPath(new File(path)); }

	public static void createPath(File file) {
		try {
			String parent = file.getParent();
			if (parent != null) {
				File unit = new File(parent);
				if (!unit.exists()) unit.mkdirs();
			}
		} catch (SecurityException se) {
			System.err.println("You don't have permissions to create " +
					file.getAbsolutePath());
		}
	}

	// XXX

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/**
	 * ( begin auto-generated from displayDensity.xml )
	 *
	 * This function returns the number "2" if the screen is a high-density
	 * screen (called a Retina display on OS X or high-dpi on Windows and Linux)
	 * and a "1" if not. This information is useful for a program to adapt to
	 * run at double the pixel density on a screen that supports it.
	 *
	 * ( end auto-generated )
	 *
	 * @webref environment
	 * @see PApplet#pixelDensity(int)
	 * @see PApplet#size(int,int)
	 */
	public int displayDensity() {
		if (display != SPAN && (fullScreen || present)) {
			return displayDensity(display);
		}
		// walk through all displays, use 2 if any display is 2
		for (int i = 0; i < displayDevices.length; i++) {
			if (displayDensity(i + 1) == 2) {
				return 2;
			}
		}
		// If nobody's density is 2 then everyone is 1
		return 1;
	}

	/**
	 * @param display
	 *            the display number to check
	 */
	public int displayDensity(int display) {
		if (GameBase.platform == SConstants.MACOSX) {
			// This should probably be reset each time there's a display change.
			// A 5-minute search didn't turn up any such event in the Java 7 API.
			// Also, should we use the Toolkit associated with the editor window?
			final String javaVendor = System.getProperty("java.vendor");
			if (javaVendor.contains("Oracle")) {
				GraphicsDevice device;
				GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

				if (display == -1) {
					device = env.getDefaultScreenDevice();

				} else if (display == SConstants.SPAN) {
					throw new RuntimeException("displayDensity() only works with specific display numbers");

				} else {
					GraphicsDevice[] devices = env.getScreenDevices();
					if (display > 0 && display <= devices.length) {
						device = devices[display - 1];
					} else {
						if (devices.length == 1) {
							System.err.println("Only one display is currently known, use displayDensity(1).");
						} else {
							System.err.format("Your displays are numbered %d through %d, " +
									"pass one of those numbers to displayDensity()%n", 1, devices.length);
						}
						throw new RuntimeException("Display " + display + " does not exist.");
					}
				}

				try {
					Field field = device.getClass().getDeclaredField("scale");
					if (field != null) {
						field.setAccessible(true);
						Object scale = field.get(device);

						if (scale instanceof Integer && ((Integer) scale).intValue() == 2) {
							return 2;
						}
					}
				} catch (Exception ignore) {}
			}
		} else if (GameBase.platform == SConstants.WINDOWS ||
				GameBase.platform == SConstants.LINUX) {
					if (suggestedDensity == -1) {
						// TODO: detect and return DPI scaling using JNA; Windows has
						// a system-wide value, not sure how it works on Linux
						return 1;
					} else if (suggestedDensity == 1 || suggestedDensity == 2) {
						return suggestedDensity;
					}
				}
		return 1;
	}

	/**
	 * @webref environment
	 * @param density
	 *            1 or 2
	 * @see PApplet#pixelWidth
	 * @see PApplet#pixelHeight
	 */
	public void pixelDensity(int density) {
		// println(density + " " + this.pixelDensity);
		if (density != this.pixelDensity) {
			if (insideSettings("pixelDensity", density)) {
				if (density != 1 && density != 2) {
					throw new RuntimeException("pixelDensity() can only be 1 or 2");
				}
				this.pixelDensity = density;
			} else {
				System.err.println("not inside settings");
				// this should only be reachable when not running in the PDE,
				// so saying it's a settings()--not just setup()--issue should be ok
				throw new RuntimeException("pixelDensity() can only be used inside settings()");
			}
		}
	}

	/**
	 * Called by PSurface objects to set the width and height variables,
	 * and update the pixelWidth and pixelHeight variables.
	 */
	@Override
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
		pixelWidth = width * pixelDensity;
		pixelHeight = height * pixelDensity;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/** smooth(1); */
	@Override
	public void smooth() { smooth(1); }

	/**
	 * @param level
	 *            either 2, 3, 4, or 8 depending on the renderer
	 * 
	 * @param level
	 *            0: all off <br/>
	 *            1 : Antialising <br/>
	 *            2 : + Text Antialising <br/>
	 *            3 : + Interpolation Bicubic <br/>
	 *            4 : ~ Interpolation Biliniear <br/>
	 *            5 : + Fractionalmetrics <br/>
	 *            6 : all default
	 */
	@Override
	public void smooth(int level) {
		this.smooth = level;
		g.smooth(level);
	}

	/**
	 * @webref environment
	 */
	@Override
	public void noSmooth() {
		this.smooth = 0;
		g.noSmooth();
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public SGraphics getGraphics() { return g; }

	////////////////////////////////////////////////

	// SGFX implementation

	@Override
	public void beginShape() { g.beginShape(); }

	@Override
	public void beginShape(int mode) { g.beginShape(mode); }

	@Override
	public void edge(boolean edge) { g.edge(edge); }

	@Override
	public void normal(float x, float y, float z) { g.normal(x, y, z); }

	@Override
	public void textureMode(int mode) { g.textureMode(mode); }

	@Override
	public void texture(SImage texture) { g.texture(texture); }

	@Override
	public void noTexture() { g.noTexture(); }

	@Override
	public void vertex(float x, float y) { g.vertex(x, y); }

	@Override
	public void vertex(float x, float y, float z) { g.vertex(x, y, z); }

	@Override
	public void vertex(int[] v) { g.vertex(v); }

	@Override
	public void vertex(float[] v) { g.vertex(v); }

	@Override
	public void vertex(float x, float y, float u, float v) { g.vertex(x, y, u, v); }

	@Override
	public void vertex(float x, float y, float z, float u, float v) { g.vertex(x, y, z, u, v); }

	@Override
	public void endShape() { g.endShape(); }

	@Override
	public void endShape(int mode) { g.endShape(mode); }

	@Override
	public void blendMode(int mode) { g.blendMode(mode); }

	@Override
	public void point(float x, float y) { g.point(x, y); }

	@Override
	public void line(float x1, float y1, float x2, float y2) { g.line(x1, y1, x2, y2); }

	@Override
	public void triangle(float x1, float y1, float x2, float y2, float x3, float y3) { g.triangle(x1, y1, x2, y2, x3, y3); }

	@Override
	public void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) { g.quad(x1, y1, x2, y2, x3, y3, x4, y4); }

	@Override
	public void rectMode(int mode) { g.rectMode(mode); }

	@Override
	public void rect(float x, float y, float w, float h) { g.rect(x, y, w, h); }

	@Override
	public void square(float x, float y, float w) { g.square(x, y, w); }

	@Override
	public void ellipseMode(int mode) { g.ellipseMode(mode); }

	@Override
	public void ellipse(float x, float y, float w, float h) { g.ellipse(x, y, w, h); }

	@Override
	public void ellipse(float x, float y, float r) { g.ellipse(x, y, r); }

	@Override
	public void circle(float x, float y, float r) { g.circle(x, y, r); }

	@Override
	public void arc(float a, float b, float c, float d, float start, float stop) { g.arc(a, b, c, d, start, stop); }

	@Override
	public void arc(float a, float b, float c, float d, float start, float stop, int mode) { g.arc(a, b, c, d, start, stop, mode); }

	@Override
	public void imageMode(int mode) { g.imageMode(mode); }

	@Override
	public void image(SImage img) { g.image(img); }

	@Override
	public void image(Image img) { g.image(img); }

	@Override
	public void image(SImage img, float x, float y) { g.image(img, x, y); }

	@Override
	public void image(Image img, float x, float y) { g.image(img, x, y); }

	@Override
	public void image(SImage img, float x, float y, float w, float h) { g.image(img, x, y, w, h); }

	@Override
	public void image(Image img, float x, float y, float w, float h) { g.image(img, x, y, w, h); }

	@Override
	public void image(SImage img, float x, float y, float w, float h, int u1, int v1, int u2, int v2) { g.image(img, x, y, w, h, u1, v1, u2, v2); }

	@Override
	public void image(Image img, float x, float y, float w, float h, int u1, int v1, int u2, int v2) { g.image(img, x, y, w, h, u1, v1, u2, v2); }

	@Override
	public void fillShape(Shape s) { g.fillShape(s); }

	@Override
	public void strokeShape(Shape s) { g.strokeShape(s); }

	@Override
	public void drawShape(Shape s) { g.drawShape(s); }

	@Override
	public void textAlign(int alignX) { g.textAlign(alignX); }

	@Override
	public void textAlign(int alignX, int alignY) { g.textAlign(alignX, alignY); }

	@Override
	public float textAscent() { return g.textAscent(); }

	@Override
	public float textDescent() { return g.textDescent(); }

	@Override
	public void textFont(SFont which) { g.textFont(which); }

	@Override
	public void textFont(SFont which, float size) { g.textFont(which, size); }

	@Override
	public void textLeading(float leading) { g.textLeading(leading); }

	@Override
	public void textMode(int mode) { g.textMode(mode); }

	@Override
	public void textSize(float size) { g.textSize(size); }

	@Override
	public float textWidth(char c) { return g.textWidth(c); }

	@Override
	public float textWidth(String str) { return g.textWidth(str); }

	@Override
	public float textWidth(char[] chars, int start, int length) { return g.textWidth(chars, start, length); }

	@Override
	public void string(String text, float x, float y) { g.string(text, x, y); }

	@Override
	public void text(char c, float x, float y) { g.text(c, x, y); }

	@Override
	public void text(String str, float x, float y) { g.text(str, x, y); }

	@Override
	public void text(char[] chars, int start, int stop, float x, float y) { g.text(chars, start, stop, x, y); }

	@Override
	public void text(String str, float x1, float y1, float x2, float y2) { g.text(str, x1, y1, x2, y2); }

	@Override
	public void text(int num, float x, float y) { g.text(num, x, y); }

	@Override
	public void text(float num, float x, float y) { g.text(num, x, y); }

	@Override
	public void push() { g.push(); }

	@Override
	public void pop() { g.pop(); }

	@Override
	public void pushMatrix() { g.pushMatrix(); }

	@Override
	public void popMatrix() { g.popMatrix(); }

	@Override
	public void translate(float x, float y) { g.translate(x, y); }

	@Override
	public void rotate(float theta) { g.rotate(theta); }

	@Override
	public void rotateX(float theta) { g.rotateX(theta); }

	@Override
	public void rotateY(float theta) { g.rotateY(theta); }

	@Override
	public void rotateZ(float theta) { g.rotateZ(theta); }

	@Override
	public void rotate(float theta, float x, float y) { g.rotate(theta, x, y); }

	@Override
	public void rotate(float theta, float x, float y, float z) { g.rotate(theta, x, y, z); }

	@Override
	public void scale(float xy) { g.scale(xy); }

	@Override
	public void scale(float x, float y) { g.scale(x, y); }

	@Override
	public void scale(float x, float y, float z) { g.scale(x, y, z); }

	@Override
	public void shear(float x, float y) { g.shear(x, y); }

	@Override
	public void shearX(float angle) { g.shearX(angle); }

	@Override
	public void shearY(float angle) { g.shearY(angle); }

	@Override
	public void transform(AffineTransform affineTransform) { g.transform(affineTransform); }

	@Override
	public void setTransform(AffineTransform affineTransform) { g.setTransform(affineTransform); }

	@Override
	public void resetMatrix() { g.resetMatrix(); }

	@Override
	public void applyMatrix(SMatrix_D source) { g.applyMatrix(source); }

	@Override
	public void applyMatrix(SMatrix2D source) { g.applyMatrix(source); }

	@Override
	public void applyMatrix(
			float n00, float n01, float n02,
			float n10, float n11, float n12) { g.applyMatrix(
					n00, n01, n02,
					n10, n11, n12); }

	@Override
	public void applyMatrix(SMatrix3D source) { g.applyMatrix(source); }

	@Override
	public void applyMatrix(
			float n00, float n01, float n02, float n03,
			float n10, float n11, float n12, float n13,
			float n20, float n21, float n22, float n23,
			float n30, float n31, float n32, float n33) { g.applyMatrix(
					n00, n01, n02, n03,
					n10, n11, n12, n13,
					n20, n21, n22, n23,
					n30, n31, n32, n33); }

	@Override
	public SMatrix_D getMatrix() { return g.getMatrix(); }

	@Override
	public SMatrix2D getMatrix(SMatrix2D target) { return g.getMatrix(target); }

	@Override
	public SMatrix3D getMatrix(SMatrix3D target) { return g.getMatrix(target); }

	@Override
	public void setMatrix(SMatrix_D source) { g.setMatrix(source); }

	@Override
	public void setMatrix(SMatrix2D source) { g.setMatrix(source); }

	@Override
	public void setMatrix(SMatrix3D source) { g.setMatrix(source); }

	@Override
	public void printMatrix() { g.printMatrix(); }

	@Override
	public float screenX(float x, float y) { return g.screenX(x, y); }

	@Override
	public float screenY(float x, float y) { return g.screenY(x, y); }

	@Override
	public float screenX(float x, float y, float z) { return g.screenX(x, y, z); }

	@Override
	public float screenY(float x, float y, float z) { return g.screenY(x, y, z); }

	@Override
	public float screenZ(float x, float y, float z) { return g.screenZ(x, y, z); }

	@Override
	public void pushStyle() { g.pushStyle(); }

	@Override
	public void popStyle() { g.popStyle(); }

	@Override
	public SStyle getStyle() { return g.getStyle(); }

	@Override
	public SStyle getStyle(SStyle s) { return g.getStyle(); }

	@Override
	public void style(SStyle s) { g.style(s); }

	@Override
	public void strokeWeight(float weight) { g.strokeWeight(weight); }

	@Override
	public void strokeJoin(int join) { g.strokeJoin(join); }

	@Override
	public void strokeCap(int cap) { g.strokeCap(cap); }

	@Override
	public void noStroke() { g.noStroke(); }

	@Override
	public void stroke(int rgb) { g.stroke(rgb); }

	@Override
	public void stroke(int rgb, float alpha) { g.stroke(rgb, alpha); }

	@Override
	public void stroke(float gray) { g.stroke(gray); }

	@Override
	public void stroke(float gray, float alpha) { g.stroke(gray, alpha); }

	@Override
	public void stroke(float v1, float v2, float v3) { g.stroke(v1, v2, v3); }

	@Override
	public void stroke(float v1, float v2, float v3, float alpha) { g.stroke(v1, v2, v3, alpha); }

	@Override
	public void noTint() { g.noTint(); }

	@Override
	public void tint(int rgb) { g.tint(rgb); }

	@Override
	public void tint(int rgb, float alpha) { g.tint(rgb, alpha); }

	@Override
	public void tint(float gray) { g.tint(gray); }

	@Override
	public void tint(float gray, float alpha) { g.tint(gray, alpha); }

	@Override
	public void tint(float v1, float v2, float v3) { g.tint(v1, v2, v3); }

	@Override
	public void tint(float v1, float v2, float v3, float alpha) { g.tint(v1, v2, v3, alpha); }

	@Override
	public void noFill() { g.noFill(); }

	@Override
	public void fill(int rgb) { g.fill(rgb); }

	@Override
	public void fill(int rgb, float alpha) { g.fill(rgb, alpha); }

	@Override
	public void fill(float gray) { g.fill(gray); }

	@Override
	public void fill(float gray, float alpha) { g.fill(gray, alpha); }

	@Override
	public void fill(float v1, float v2, float v3) { g.fill(v1, v2, v3); }

	@Override
	public void fill(float v1, float v2, float v3, float alpha) { g.fill(v1, v2, v3, alpha); }

	@Override
	public void background(int rgb) { g.background(rgb); }

	@Override
	public void background(int rgb, float alpha) { g.background(rgb, alpha); }

	@Override
	public void background(float gray) { g.background(gray); }

	@Override
	public void background(float gray, float alpha) { g.background(gray, alpha); }

	@Override
	public void background(float v1, float v2, float v3) { g.background(v1, v2, v3); }

	@Override
	public void background(float v1, float v2, float v3, float alpha) { g.background(v1, v2, v3, alpha); }

	@Override
	public void clear() { g.clear(); }

	@Override
	public void background(SImage image) { g.background(image); }

	@Override
	public void colorMode(int mode) { g.colorMode(mode); }

	@Override
	public void colorMode(int mode, float max) { g.colorMode(mode, max); }

	@Override
	public void colorMode(int mode, float max1, float max2, float max3) { g.colorMode(mode, max1, max2, max3); }

	@Override
	public void colorMode(int mode, float max1, float max2, float max3, float maxA) { g.colorMode(mode, max1, max2, max3, maxA); }

	@Override
	public int color(int c) { return g.color(c); }

	@Override
	public int color(float gray) { return g.color(gray); }

	@Override
	public int color(int c, int alpha) { return g.color(c, alpha); }

	@Override
	public int color(int c, float alpha) { return g.color(c, alpha); }

	@Override
	public int color(float gray, float alpha) { return g.color(gray, alpha); }

	@Override
	public int color(int v1, int v2, int v3) { return g.color(v1, v2, v3); }

	@Override
	public int color(float v1, float v2, float v3) { return g.color(v1, v2, v3); }

	@Override
	public int color(int v1, int v2, int v3, int a) { return g.color(v1, v2, v3, a); }

	@Override
	public int color(float v1, float v2, float v3, float a) { return g.color(v1, v2, v3, a); }

	@Override
	public float alpha(int rgb) { return g.alpha(rgb); }

	@Override
	public float red(int rgb) { return g.red(rgb); }

	@Override
	public float green(int rgb) { return g.green(rgb); }

	@Override
	public float blue(int rgb) { return g.blue(rgb); }

	@Override
	public float hue(int rgb) { return g.hue(rgb); }

	@Override
	public float saturation(int rgb) { return g.saturation(rgb); }

	@Override
	public float brightness(int rgb) { return g.brightness(rgb); }

	@Override
	public int lerpColor(int c1, int c2, float amt) {
		if (g != null) return g.lerpColor(c1, c2, amt);
		// use the default mode (RGB) if lerpColor is called before setup()
		return SGraphics.lerpColor(c1, c2, amt, RGB);
	}

	public static int lerpColor(int c1, int c2, float amt, int mode) { return SGraphics.lerpColor(c1, c2, amt, mode); }

	@Override
	public void loadPixels() {
		g.loadPixels();
		pixels = g.pixels;
	}

	@Override
	public void updatePixels() { g.updatePixels(); }

	@Override
	public void updatePixels(int x, int y, int w, int h) { g.updatePixels(x, y, w, h); }

	@Override
	public int get(int x, int y) { return g.get(x, y); }

	public SImage get(int x, int y, int w, int h) { return g.get(x, y, w, h); }

	public SImage get() { return g.get(); }

	public SImage copy() { return g.copy(); }

	@Override
	public void pixel(int x, int y) { g.pixel(x, y); }

	@Override
	public void pixel(int x, int y, int argb) { g.pixel(x, y, argb); }

	@Override
	public void set(int x, int y, int argb) { g.set(x, y, argb); }

	public void set(int x, int y, SImage img) { g.set(x, y, img); }

	public void filter(int kind) { g.filter(kind); }

	@Override
	public void copy(int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) { g.copy(sx, sy, sw, sh, dx, dy, dw, dh); }

	@Override
	public void copy(SImage src, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) { g.copy(src, sx, sy, sw, sh, dx, dy, dw, dh); }

	///////////////////////////////////////////////////////////////////

	static public Process exec(String... args) {
		try {
			return Runtime.getRuntime().exec(args);
		} catch (Exception e) {
			throw new RuntimeException("Exception while attempting " + join(args, ' '), e);
		}
	}

	//////////////////////////////////////////////////////////////

	// URL ENCODING

	static public String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) { // oh c'mon
			return null;
		}
	}

	// DO NOT use for file paths, URLDecoder can't handle RFC2396
	// "The recommended way to manage the encoding and decoding of
	// URLs is to use URI, and to convert between these two classes
	// using toURI() and URI.toURL()."
	// https://docs.oracle.com/javase/8/docs/api/java/net/URL.html
	static public String urlDecode(String str) {
		try {
			return URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) { // safe per the JDK source
			return null;
		}
	}

	//////////////////////////////////////////////////////////////

	// SORT

	/**
	 * ( begin auto-generated from sort.xml )
	 *
	 * Sorts an array of numbers from smallest to largest and puts an array of
	 * words in alphabetical order. The original array is not modified, a
	 * re-ordered array is returned. The <b>count</b> parameter states the
	 * number of elements to sort. For example if there are 12 elements in an
	 * array and if count is the value 5, only the first five elements on the
	 * array will be sorted. <!--As of release 0126, the alphabetical ordering
	 * is case insensitive.-->
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:array_functions
	 * @param list
	 *            array to sort
	 * @see PApplet#reverse(boolean[])
	 */
	static public byte[] sort(byte list[]) { return sort(list, list.length); }

	/**
	 * @param count
	 *            number of elements to sort, starting from 0
	 */
	static public byte[] sort(byte[] list, int count) {
		byte[] outgoing = new byte[list.length];
		System.arraycopy(list, 0, outgoing, 0, list.length);
		Arrays.sort(outgoing, 0, count);
		return outgoing;
	}

	static public char[] sort(char list[]) { return sort(list, list.length); }

	static public char[] sort(char[] list, int count) {
		char[] outgoing = new char[list.length];
		System.arraycopy(list, 0, outgoing, 0, list.length);
		Arrays.sort(outgoing, 0, count);
		return outgoing;
	}

	static public int[] sort(int list[]) { return sort(list, list.length); }

	static public int[] sort(int[] list, int count) {
		int[] outgoing = new int[list.length];
		System.arraycopy(list, 0, outgoing, 0, list.length);
		Arrays.sort(outgoing, 0, count);
		return outgoing;
	}

	static public float[] sort(float list[]) { return sort(list, list.length); }

	static public float[] sort(float[] list, int count) {
		float[] outgoing = new float[list.length];
		System.arraycopy(list, 0, outgoing, 0, list.length);
		Arrays.sort(outgoing, 0, count);
		return outgoing;
	}

	static public String[] sort(String list[]) { return sort(list, list.length); }

	static public String[] sort(String[] list, int count) {
		String[] outgoing = new String[list.length];
		System.arraycopy(list, 0, outgoing, 0, list.length);
		Arrays.sort(outgoing, 0, count);
		return outgoing;
	}

	//////////////////////////////////////////////////////////////

	// ARRAY UTILITIES

	/**
	 * ( begin auto-generated from arrayCopy.xml )
	 *
	 * Copies an array (or part of an array) to another array. The <b>src</b>
	 * array is copied to the <b>dst</b> array, beginning at the position
	 * specified by <b>srcPos</b> and into the position specified by
	 * <b>dstPos</b>. The number of elements to copy is determined by
	 * <b>length</b>. The simplified version with two arguments copies an
	 * entire array to another of the same size. It is equivalent to
	 * "arrayCopy(src, 0, dst, 0, src.length)". This function is far more
	 * efficient for copying array data than iterating through a <b>for</b> and
	 * copying each element.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:array_functions
	 * @param src
	 *            the source array
	 * @param srcPosition
	 *            starting position in the source array
	 * @param dst
	 *            the destination array of the same data type as the source array
	 * @param dstPosition
	 *            starting position in the destination array
	 * @param length
	 *            number of array elements to be copied
	 * @see PApplet#concat(boolean[], boolean[])
	 */
	static public void arrayCopy(Object src, int srcPosition,
			Object dst, int dstPosition,
			int length) { System.arraycopy(src, srcPosition, dst, dstPosition, length); }

	/**
	 * Convenience method for arraycopy().
	 * Identical to <CODE>arraycopy(src, 0, dst, 0, length);</CODE>
	 */
	static public void arrayCopy(Object src, Object dst, int length) { System.arraycopy(src, 0, dst, 0, length); }

	/**
	 * Shortcut to copy the entire contents of
	 * the source into the destination array.
	 * Identical to <CODE>arraycopy(src, 0, dst, 0, src.length);</CODE>
	 */
	static public void arrayCopy(Object src, Object dst) { System.arraycopy(src, 0, dst, 0, Array.getLength(src)); }

	/**
	 * Use arrayCopy() instead.
	 */
	@Deprecated
	static public void arraycopy(Object src, int srcPosition,
			Object dst, int dstPosition,
			int length) { System.arraycopy(src, srcPosition, dst, dstPosition, length); }

	/**
	 * Use arrayCopy() instead.
	 */
	@Deprecated
	static public void arraycopy(Object src, Object dst, int length) { System.arraycopy(src, 0, dst, 0, length); }

	/**
	 * Use arrayCopy() instead.
	 */
	@Deprecated
	static public void arraycopy(Object src, Object dst) { System.arraycopy(src, 0, dst, 0, Array.getLength(src)); }

	/**
	 * ( begin auto-generated from expand.xml )
	 *
	 * Increases the size of an array. By default, this function doubles the
	 * size of the array, but the optional <b>newSize</b> parameter provides
	 * precise control over the increase in size.
	 * <br/>
	 * <br/>
	 * When using an array of objects, the data returned from the function must
	 * be cast to the object array's data type. For example: <em>SomeClass[]
	 * items = (SomeClass[]) expand(originalArray)</em>.
	 *
	 * ( end auto-generated )
	 *
	 * @webref data:array_functions
	 * @param list
	 *            the array to expand
	 * @see PApplet#shorten(boolean[])
	 */
	static public boolean[] expand(boolean list[]) { return expand(list, list.length > 0 ? list.length << 1 : 1); }

	/**
	 * @param newSize
	 *            new size for the array
	 */
	static public boolean[] expand(boolean list[], int newSize) {
		boolean temp[] = new boolean[newSize];
		System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
		return temp;
	}

	static public byte[] expand(byte list[]) { return expand(list, list.length > 0 ? list.length << 1 : 1); }

	static public byte[] expand(byte list[], int newSize) {
		byte temp[] = new byte[newSize];
		System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
		return temp;
	}

	static public char[] expand(char list[]) { return expand(list, list.length > 0 ? list.length << 1 : 1); }

	static public char[] expand(char list[], int newSize) {
		char temp[] = new char[newSize];
		System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
		return temp;
	}

	static public int[] expand(int list[]) { return expand(list, list.length > 0 ? list.length << 1 : 1); }

	static public int[] expand(int list[], int newSize) {
		int temp[] = new int[newSize];
		System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
		return temp;
	}

	static public long[] expand(long list[]) { return expand(list, list.length > 0 ? list.length << 1 : 1); }

	static public long[] expand(long list[], int newSize) {
		long temp[] = new long[newSize];
		System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
		return temp;
	}

	static public float[] expand(float list[]) { return expand(list, list.length > 0 ? list.length << 1 : 1); }

	static public float[] expand(float list[], int newSize) {
		float temp[] = new float[newSize];
		System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
		return temp;
	}

	static public double[] expand(double list[]) { return expand(list, list.length > 0 ? list.length << 1 : 1); }

	static public double[] expand(double list[], int newSize) {
		double temp[] = new double[newSize];
		System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
		return temp;
	}

	static public String[] expand(String list[]) { return expand(list, list.length > 0 ? list.length << 1 : 1); }

	static public String[] expand(String list[], int newSize) {
		String temp[] = new String[newSize];
		// in case the new size is smaller than list.length
		System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
		return temp;
	}

	/**
	 * @nowebref
	 */
	static public Object expand(Object array) {
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

	// contract() has been removed in revision 0124, use subset() instead.
	// (expand() is also functionally equivalent)

	/**
	 * ( begin auto-generated from append.xml )
	 *
	 * Expands an array by one element and adds data to the new position. The
	 * datatype of the <b>element</b> parameter must be the same as the
	 * datatype of the array.
	 * <br/>
	 * <br/>
	 * When using an array of objects, the data returned from the function must
	 * be cast to the object array's data type. For example: <em>SomeClass[]
	 * items = (SomeClass[]) append(originalArray, element)</em>.
	 *
	 * ( end auto-generated )
	 *
	 * @webref data:array_functions
	 * @param array
	 *            array to append
	 * @param value
	 *            new data for the array
	 * @see PApplet#shorten(boolean[])
	 * @see PApplet#expand(boolean[])
	 */
	static public byte[] append(byte array[], byte value) {
		array = expand(array, array.length + 1);
		array[array.length - 1] = value;
		return array;
	}

	static public char[] append(char array[], char value) {
		array = expand(array, array.length + 1);
		array[array.length - 1] = value;
		return array;
	}

	static public int[] append(int array[], int value) {
		array = expand(array, array.length + 1);
		array[array.length - 1] = value;
		return array;
	}

	static public float[] append(float array[], float value) {
		array = expand(array, array.length + 1);
		array[array.length - 1] = value;
		return array;
	}

	static public String[] append(String array[], String value) {
		array = expand(array, array.length + 1);
		array[array.length - 1] = value;
		return array;
	}

	static public Object append(Object array, Object value) {
		int length = Array.getLength(array);
		array = expand(array, length + 1);
		Array.set(array, length, value);
		return array;
	}

	/**
	 * ( begin auto-generated from shorten.xml )
	 *
	 * Decreases an array by one element and returns the shortened array.
	 * <br/>
	 * <br/>
	 * When using an array of objects, the data returned from the function must
	 * be cast to the object array's data type. For example: <em>SomeClass[]
	 * items = (SomeClass[]) shorten(originalArray)</em>.
	 *
	 * ( end auto-generated )
	 *
	 * @webref data:array_functions
	 * @param list
	 *            array to shorten
	 * @see PApplet#append(byte[], byte)
	 * @see PApplet#expand(boolean[])
	 */
	static public boolean[] shorten(boolean list[]) { return subset(list, 0, list.length - 1); }

	static public byte[] shorten(byte list[]) { return subset(list, 0, list.length - 1); }

	static public char[] shorten(char list[]) { return subset(list, 0, list.length - 1); }

	static public int[] shorten(int list[]) { return subset(list, 0, list.length - 1); }

	static public float[] shorten(float list[]) { return subset(list, 0, list.length - 1); }

	static public String[] shorten(String list[]) { return subset(list, 0, list.length - 1); }

	static public Object shorten(Object list) {
		int length = Array.getLength(list);
		return subset(list, 0, length - 1);
	}

	/**
	 * ( begin auto-generated from splice.xml )
	 *
	 * Inserts a value or array of values into an existing array. The first two
	 * parameters must be of the same datatype. The <b>array</b> parameter
	 * defines the array which will be modified and the second parameter
	 * defines the data which will be inserted.
	 * <br/>
	 * <br/>
	 * When using an array of objects, the data returned from the function must
	 * be cast to the object array's data type. For example: <em>SomeClass[]
	 * items = (SomeClass[]) splice(array1, array2, index)</em>.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:array_functions
	 * @param list
	 *            array to splice into
	 * @param value
	 *            value to be spliced in
	 * @param index
	 *            position in the array from which to insert data
	 * @see PApplet#concat(boolean[], boolean[])
	 * @see PApplet#subset(boolean[], int, int)
	 */
	static final public boolean[] splice(boolean list[],
			boolean value, int index) {
		boolean outgoing[] = new boolean[list.length + 1];
		System.arraycopy(list, 0, outgoing, 0, index);
		outgoing[index] = value;
		System.arraycopy(list, index, outgoing, index + 1,
				list.length - index);
		return outgoing;
	}

	static final public boolean[] splice(boolean list[],
			boolean value[], int index) {
		boolean outgoing[] = new boolean[list.length + value.length];
		System.arraycopy(list, 0, outgoing, 0, index);
		System.arraycopy(value, 0, outgoing, index, value.length);
		System.arraycopy(list, index, outgoing, index + value.length,
				list.length - index);
		return outgoing;
	}

	static final public byte[] splice(byte list[],
			byte value, int index) {
		byte outgoing[] = new byte[list.length + 1];
		System.arraycopy(list, 0, outgoing, 0, index);
		outgoing[index] = value;
		System.arraycopy(list, index, outgoing, index + 1,
				list.length - index);
		return outgoing;
	}

	static final public byte[] splice(byte list[],
			byte value[], int index) {
		byte outgoing[] = new byte[list.length + value.length];
		System.arraycopy(list, 0, outgoing, 0, index);
		System.arraycopy(value, 0, outgoing, index, value.length);
		System.arraycopy(list, index, outgoing, index + value.length,
				list.length - index);
		return outgoing;
	}

	static final public char[] splice(char list[],
			char value, int index) {
		char outgoing[] = new char[list.length + 1];
		System.arraycopy(list, 0, outgoing, 0, index);
		outgoing[index] = value;
		System.arraycopy(list, index, outgoing, index + 1,
				list.length - index);
		return outgoing;
	}

	static final public char[] splice(char list[],
			char value[], int index) {
		char outgoing[] = new char[list.length + value.length];
		System.arraycopy(list, 0, outgoing, 0, index);
		System.arraycopy(value, 0, outgoing, index, value.length);
		System.arraycopy(list, index, outgoing, index + value.length,
				list.length - index);
		return outgoing;
	}

	static final public int[] splice(int list[],
			int value, int index) {
		int outgoing[] = new int[list.length + 1];
		System.arraycopy(list, 0, outgoing, 0, index);
		outgoing[index] = value;
		System.arraycopy(list, index, outgoing, index + 1,
				list.length - index);
		return outgoing;
	}

	static final public int[] splice(int list[],
			int value[], int index) {
		int outgoing[] = new int[list.length + value.length];
		System.arraycopy(list, 0, outgoing, 0, index);
		System.arraycopy(value, 0, outgoing, index, value.length);
		System.arraycopy(list, index, outgoing, index + value.length,
				list.length - index);
		return outgoing;
	}

	static final public float[] splice(float list[],
			float value, int index) {
		float outgoing[] = new float[list.length + 1];
		System.arraycopy(list, 0, outgoing, 0, index);
		outgoing[index] = value;
		System.arraycopy(list, index, outgoing, index + 1,
				list.length - index);
		return outgoing;
	}

	static final public float[] splice(float list[],
			float value[], int index) {
		float outgoing[] = new float[list.length + value.length];
		System.arraycopy(list, 0, outgoing, 0, index);
		System.arraycopy(value, 0, outgoing, index, value.length);
		System.arraycopy(list, index, outgoing, index + value.length,
				list.length - index);
		return outgoing;
	}

	static final public String[] splice(String list[],
			String value, int index) {
		String outgoing[] = new String[list.length + 1];
		System.arraycopy(list, 0, outgoing, 0, index);
		outgoing[index] = value;
		System.arraycopy(list, index, outgoing, index + 1,
				list.length - index);
		return outgoing;
	}

	static final public String[] splice(String list[],
			String value[], int index) {
		String outgoing[] = new String[list.length + value.length];
		System.arraycopy(list, 0, outgoing, 0, index);
		System.arraycopy(value, 0, outgoing, index, value.length);
		System.arraycopy(list, index, outgoing, index + value.length,
				list.length - index);
		return outgoing;
	}

	static final public Object splice(Object list, Object value, int index) {
		Class<?> type = list.getClass().getComponentType();
		Object outgoing = null;
		int length = Array.getLength(list);

		// check whether item being spliced in is an array
		if (value.getClass().getName().charAt(0) == '[') {
			int vlength = Array.getLength(value);
			outgoing = Array.newInstance(type, length + vlength);
			System.arraycopy(list, 0, outgoing, 0, index);
			System.arraycopy(value, 0, outgoing, index, vlength);
			System.arraycopy(list, index, outgoing, index + vlength, length - index);

		} else {
			outgoing = Array.newInstance(type, length + 1);
			System.arraycopy(list, 0, outgoing, 0, index);
			Array.set(outgoing, index, value);
			System.arraycopy(list, index, outgoing, index + 1, length - index);
		}
		return outgoing;
	}

	static public boolean[] subset(boolean[] list, int start) { return subset(list, start, list.length - start); }

	/**
	 * ( begin auto-generated from subset.xml )
	 *
	 * Extracts an array of elements from an existing array. The <b>array</b>
	 * parameter defines the array from which the elements will be copied and
	 * the <b>offset</b> and <b>length</b> parameters determine which elements
	 * to extract. If no <b>length</b> is given, elements will be extracted
	 * from the <b>offset</b> to the end of the array. When specifying the
	 * <b>offset</b> remember the first array element is 0. This function does
	 * not change the source array.
	 * <br/>
	 * <br/>
	 * When using an array of objects, the data returned from the function must
	 * be cast to the object array's data type. For example: <em>SomeClass[]
	 * items = (SomeClass[]) subset(originalArray, 0, 4)</em>.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:array_functions
	 * @param list
	 *            array to extract from
	 * @param start
	 *            position to begin
	 * @param count
	 *            number of values to extract
	 * @see PApplet#splice(boolean[], boolean, int)
	 */
	static public boolean[] subset(boolean[] list, int start, int count) {
		boolean[] output = new boolean[count];
		System.arraycopy(list, start, output, 0, count);
		return output;
	}

	static public byte[] subset(byte[] list, int start) { return subset(list, start, list.length - start); }

	static public byte[] subset(byte[] list, int start, int count) {
		byte[] output = new byte[count];
		System.arraycopy(list, start, output, 0, count);
		return output;
	}

	static public char[] subset(char[] list, int start) { return subset(list, start, list.length - start); }

	static public char[] subset(char[] list, int start, int count) {
		char[] output = new char[count];
		System.arraycopy(list, start, output, 0, count);
		return output;
	}

	static public int[] subset(int[] list, int start) { return subset(list, start, list.length - start); }

	static public int[] subset(int[] list, int start, int count) {
		int[] output = new int[count];
		System.arraycopy(list, start, output, 0, count);
		return output;
	}

	static public long[] subset(long[] list, int start) { return subset(list, start, list.length - start); }

	static public long[] subset(long[] list, int start, int count) {
		long[] output = new long[count];
		System.arraycopy(list, start, output, 0, count);
		return output;
	}

	static public float[] subset(float[] list, int start) { return subset(list, start, list.length - start); }

	static public float[] subset(float[] list, int start, int count) {
		float[] output = new float[count];
		System.arraycopy(list, start, output, 0, count);
		return output;
	}

	static public double[] subset(double[] list, int start) { return subset(list, start, list.length - start); }

	static public double[] subset(double[] list, int start, int count) {
		double[] output = new double[count];
		System.arraycopy(list, start, output, 0, count);
		return output;
	}

	static public String[] subset(String[] list, int start) { return subset(list, start, list.length - start); }

	static public String[] subset(String[] list, int start, int count) {
		String[] output = new String[count];
		System.arraycopy(list, start, output, 0, count);
		return output;
	}

	static public Object subset(Object list, int start) {
		int length = Array.getLength(list);
		return subset(list, start, length - start);
	}

	static public Object subset(Object list, int start, int count) {
		Class<?> type = list.getClass().getComponentType();
		Object outgoing = Array.newInstance(type, count);
		System.arraycopy(list, start, outgoing, 0, count);
		return outgoing;
	}

	/**
	 * ( begin auto-generated from concat.xml )
	 *
	 * Concatenates two arrays. For example, concatenating the array { 1, 2, 3
	 * } and the array { 4, 5, 6 } yields { 1, 2, 3, 4, 5, 6 }. Both parameters
	 * must be arrays of the same datatype.
	 * <br/>
	 * <br/>
	 * When using an array of objects, the data returned from the function must
	 * be cast to the object array's data type. For example: <em>SomeClass[]
	 * items = (SomeClass[]) concat(array1, array2)</em>.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:array_functions
	 * @param a
	 *            first array to concatenate
	 * @param b
	 *            second array to concatenate
	 * @see PApplet#splice(boolean[], boolean, int)
	 * @see PApplet#arrayCopy(Object, int, Object, int, int)
	 */
	static public boolean[] concat(boolean a[], boolean b[]) {
		boolean c[] = new boolean[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	static public byte[] concat(byte a[], byte b[]) {
		byte c[] = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	static public char[] concat(char a[], char b[]) {
		char c[] = new char[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	static public int[] concat(int a[], int b[]) {
		int c[] = new int[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	static public float[] concat(float a[], float b[]) {
		float c[] = new float[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	static public String[] concat(String a[], String b[]) {
		String c[] = new String[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	static public Object concat(Object a, Object b) {
		Class<?> type = a.getClass().getComponentType();
		int alength = Array.getLength(a);
		int blength = Array.getLength(b);
		Object outgoing = Array.newInstance(type, alength + blength);
		System.arraycopy(a, 0, outgoing, 0, alength);
		System.arraycopy(b, 0, outgoing, alength, blength);
		return outgoing;
	}

	//

	/**
	 * ( begin auto-generated from reverse.xml )
	 *
	 * Reverses the order of an array.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:array_functions
	 * @param list
	 *            booleans[], bytes[], chars[], ints[], floats[], or Strings[]
	 * @see PApplet#sort(String[], int)
	 */
	static public boolean[] reverse(boolean list[]) {
		boolean outgoing[] = new boolean[list.length];
		int length1 = list.length - 1;
		for (int i = 0; i < list.length; i++) {
			outgoing[i] = list[length1 - i];
		}
		return outgoing;
	}

	static public byte[] reverse(byte list[]) {
		byte outgoing[] = new byte[list.length];
		int length1 = list.length - 1;
		for (int i = 0; i < list.length; i++) {
			outgoing[i] = list[length1 - i];
		}
		return outgoing;
	}

	static public char[] reverse(char list[]) {
		char outgoing[] = new char[list.length];
		int length1 = list.length - 1;
		for (int i = 0; i < list.length; i++) {
			outgoing[i] = list[length1 - i];
		}
		return outgoing;
	}

	static public int[] reverse(int list[]) {
		int outgoing[] = new int[list.length];
		int length1 = list.length - 1;
		for (int i = 0; i < list.length; i++) {
			outgoing[i] = list[length1 - i];
		}
		return outgoing;
	}

	static public float[] reverse(float list[]) {
		float outgoing[] = new float[list.length];
		int length1 = list.length - 1;
		for (int i = 0; i < list.length; i++) {
			outgoing[i] = list[length1 - i];
		}
		return outgoing;
	}

	static public String[] reverse(String list[]) {
		String outgoing[] = new String[list.length];
		int length1 = list.length - 1;
		for (int i = 0; i < list.length; i++) {
			outgoing[i] = list[length1 - i];
		}
		return outgoing;
	}

	static public Object reverse(Object list) {
		Class<?> type = list.getClass().getComponentType();
		int length = Array.getLength(list);
		Object outgoing = Array.newInstance(type, length);
		for (int i = 0; i < length; i++) {
			Array.set(outgoing, i, Array.get(list, (length - 1) - i));
		}
		return outgoing;
	}

	//////////////////////////////////////////////////////////////

	// STRINGS

	/**
	 * ( begin auto-generated from trim.xml )
	 *
	 * Removes whitespace characters from the beginning and end of a String. In
	 * addition to standard whitespace characters such as space, carriage
	 * return, and tab, this function also removes the Unicode "nbsp" character.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:string_functions
	 * @param str
	 *            any string
	 * @see PApplet#split(String, String)
	 * @see PApplet#join(String[], char)
	 */
	static public String trim(String str) {
		if (str == null) {
			return null;
		}
		return str.replace('\u00A0', ' ').trim();
	}

	/**
	 * @param array
	 *            a String array
	 */
	static public String[] trim(String[] array) {
		if (array == null) {
			return null;
		}
		String[] outgoing = new String[array.length];
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null) {
				outgoing[i] = trim(array[i]);
			}
		}
		return outgoing;
	}

	/**
	 * ( begin auto-generated from join.xml )
	 *
	 * Combines an array of Strings into one String, each separated by the
	 * character(s) used for the <b>separator</b> parameter. To join arrays of
	 * ints or floats, it's necessary to first convert them to strings using
	 * <b>nf()</b> or <b>nfs()</b>.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:string_functions
	 * @param list
	 *            array of Strings
	 * @param separator
	 *            char or String to be placed between each item
	 * @see PApplet#split(String, String)
	 * @see PApplet#trim(String)
	 * @see PApplet#nf(float, int, int)
	 * @see PApplet#nfs(float, int, int)
	 */
	static public String join(String[] list, char separator) { return join(list, String.valueOf(separator)); }

	static public String join(String[] list, String separator) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.length; i++) {
			if (i != 0) sb.append(separator);
			sb.append(list[i]);
		}
		return sb.toString();
	}

	static public String[] splitTokens(String value) { return splitTokens(value, WHITESPACE); }

	/**
	 * ( begin auto-generated from splitTokens.xml )
	 *
	 * The splitTokens() function splits a String at one or many character
	 * "tokens." The <b>tokens</b> parameter specifies the character or
	 * characters to be used as a boundary.
	 * <br/>
	 * <br/>
	 * If no <b>tokens</b> character is specified, any whitespace character is
	 * used to split. Whitespace characters include tab (\\t), line feed (\\n),
	 * carriage return (\\r), form feed (\\f), and space. To convert a String
	 * to an array of integers or floats, use the datatype conversion functions
	 * <b>int()</b> and <b>float()</b> to convert the array of Strings.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:string_functions
	 * @param value
	 *            the String to be split
	 * @param delim
	 *            list of individual characters that will be used as separators
	 * @see PApplet#split(String, String)
	 * @see PApplet#join(String[], String)
	 * @see PApplet#trim(String)
	 */
	static public String[] splitTokens(String value, String delim) {
		StringTokenizer toker = new StringTokenizer(value, delim);
		String pieces[] = new String[toker.countTokens()];

		int index = 0;
		while (toker.hasMoreTokens()) {
			pieces[index++] = toker.nextToken();
		}
		return pieces;
	}

	/**
	 * ( begin auto-generated from split.xml )
	 *
	 * The split() function breaks a string into pieces using a character or
	 * string as the divider. The <b>delim</b> parameter specifies the
	 * character or characters that mark the boundaries between each piece. A
	 * String[] array is returned that contains each of the pieces.
	 * <br/>
	 * <br/>
	 * If the result is a set of numbers, you can convert the String[] array to
	 * to a float[] or int[] array using the datatype conversion functions
	 * <b>int()</b> and <b>float()</b> (see example above).
	 * <br/>
	 * <br/>
	 * The <b>splitTokens()</b> function works in a similar fashion, except
	 * that it splits using a range of characters instead of a specific
	 * character or sequence.
	 * <!-- /><br />
	 * This function uses regular expressions to determine how the <b>delim</b>
	 * parameter divides the <b>str</b> parameter. Therefore, if you use
	 * characters such parentheses and brackets that are used with regular
	 * expressions as a part of the <b>delim</b> parameter, you'll need to put
	 * two blackslashes (\\\\) in front of the character (see example above).
	 * You can read more about <a
	 * href="http://en.wikipedia.org/wiki/Regular_expression">regular
	 * expressions</a> and <a
	 * href="http://en.wikipedia.org/wiki/Escape_character">escape
	 * characters</a> on Wikipedia.
	 * -->
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:string_functions
	 * @usage web_application
	 * @param value
	 *            the String to be split
	 * @param delim
	 *            the character or String used to separate the data
	 */
	static public String[] split(String value, char delim) {
		// do this so that the exception occurs inside the user's
		// program, rather than appearing to be a bug inside split()
		if (value == null) return null;
		// return split(what, String.valueOf(delim)); // huh

		char chars[] = value.toCharArray();
		int splitCount = 0; // 1;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == delim) splitCount++;
		}
		// make sure that there is something in the input string
		// if (chars.length > 0) {
		// if the last char is a delimeter, get rid of it..
		// if (chars[chars.length-1] == delim) splitCount--;
		// on second thought, i don't agree with this, will disable
		// }
		if (splitCount == 0) {
			String splits[] = new String[1];
			splits[0] = value;
			return splits;
		}
		// int pieceCount = splitCount + 1;
		String splits[] = new String[splitCount + 1];
		int splitIndex = 0;
		int startIndex = 0;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == delim) {
				splits[splitIndex++] = new String(chars, startIndex, i - startIndex);
				startIndex = i + 1;
			}
		}
		// if (startIndex != chars.length) {
		splits[splitIndex] = new String(chars, startIndex, chars.length - startIndex);
		// }
		return splits;
	}

	static public String[] split(String value, String delim) {
		List<String> items = new ArrayList<>();
		int index;
		int offset = 0;
		while ((index = value.indexOf(delim, offset)) != -1) {
			items.add(value.substring(offset, index));
			offset = index + delim.length();
		}
		items.add(value.substring(offset));
		String[] outgoing = new String[items.size()];
		items.toArray(outgoing);
		return outgoing;
	}

	static protected LinkedHashMap<String, Pattern> matchPatterns;

	static Pattern matchPattern(String regexp) {
		Pattern p = null;
		if (matchPatterns == null) {
			matchPatterns = new LinkedHashMap<String, Pattern>(16, 0.75f, true) {
				private static final long serialVersionUID = 8106882067241770175L;

				@Override
				protected boolean removeEldestEntry(Map.Entry<String, Pattern> eldest) {
					// Limit the number of match patterns at 10 most recently used
					return size() == 10;
				}
			};
		} else {
			p = matchPatterns.get(regexp);
		}
		if (p == null) {
			p = Pattern.compile(regexp, Pattern.MULTILINE | Pattern.DOTALL);
			matchPatterns.put(regexp, p);
		}
		return p;
	}

	/**
	 * ( begin auto-generated from match.xml )
	 *
	 * The match() function is used to apply a regular expression to a piece of
	 * text, and return matching groups (elements found inside parentheses) as
	 * a String array. No match will return null. If no groups are specified in
	 * the regexp, but the sequence matches, an array of length one (with the
	 * matched text as the first element of the array) will be returned.<br />
	 * <br />
	 * To use the function, first check to see if the result is null. If the
	 * result is null, then the sequence did not match. If the sequence did
	 * match, an array is returned.
	 * If there are groups (specified by sets of parentheses) in the regexp,
	 * then the contents of each will be returned in the array.
	 * Element [0] of a regexp match returns the entire matching string, and
	 * the match groups start at element [1] (the first group is [1], the
	 * second [2], and so on).<br />
	 * <br />
	 * The syntax can be found in the reference for Java's <a
	 * href="http://download.oracle.com/javase/6/docs/api/">Pattern</a> class.
	 * For regular expression syntax, read the <a
	 * href="http://download.oracle.com/javase/tutorial/essential/regex/">Java
	 * Tutorial</a> on the topic.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:string_functions
	 * @param str
	 *            the String to be searched
	 * @param regexp
	 *            the regexp to be used for matching
	 * @see PApplet#matchAll(String, String)
	 * @see PApplet#split(String, String)
	 * @see PApplet#splitTokens(String, String)
	 * @see PApplet#join(String[], String)
	 * @see PApplet#trim(String)
	 */
	static public String[] match(String str, String regexp) {
		Pattern p = matchPattern(regexp);
		Matcher m = p.matcher(str);
		if (m.find()) {
			int count = m.groupCount() + 1;
			String[] groups = new String[count];
			for (int i = 0; i < count; i++) {
				groups[i] = m.group(i);
			}
			return groups;
		}
		return null;
	}

	/**
	 * ( begin auto-generated from matchAll.xml )
	 *
	 * This function is used to apply a regular expression to a piece of text,
	 * and return a list of matching groups (elements found inside parentheses)
	 * as a two-dimensional String array. No matches will return null. If no
	 * groups are specified in the regexp, but the sequence matches, a two
	 * dimensional array is still returned, but the second dimension is only of
	 * length one.<br />
	 * <br />
	 * To use the function, first check to see if the result is null. If the
	 * result is null, then the sequence did not match at all. If the sequence
	 * did match, a 2D array is returned. If there are groups (specified by
	 * sets of parentheses) in the regexp, then the contents of each will be
	 * returned in the array.
	 * Assuming, a loop with counter variable i, element [i][0] of a regexp
	 * match returns the entire matching string, and the match groups start at
	 * element [i][1] (the first group is [i][1], the second [i][2], and so
	 * on).<br />
	 * <br />
	 * The syntax can be found in the reference for Java's <a
	 * href="http://download.oracle.com/javase/6/docs/api/">Pattern</a> class.
	 * For regular expression syntax, read the <a
	 * href="http://download.oracle.com/javase/tutorial/essential/regex/">Java
	 * Tutorial</a> on the topic.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:string_functions
	 * @param str
	 *            the String to be searched
	 * @param regexp
	 *            the regexp to be used for matching
	 * @see PApplet#match(String, String)
	 * @see PApplet#split(String, String)
	 * @see PApplet#splitTokens(String, String)
	 * @see PApplet#join(String[], String)
	 * @see PApplet#trim(String)
	 */
	static public String[][] matchAll(String str, String regexp) {
		Pattern p = matchPattern(regexp);
		Matcher m = p.matcher(str);
		List<String[]> results = new ArrayList<>();
		int count = m.groupCount() + 1;
		while (m.find()) {
			String[] groups = new String[count];
			for (int i = 0; i < count; i++) {
				groups[i] = m.group(i);
			}
			results.add(groups);
		}
		if (results.isEmpty()) {
			return null;
		}
		String[][] matches = new String[results.size()][count];
		for (int i = 0; i < matches.length; i++) {
			matches[i] = results.get(i);
		}
		return matches;
	}

	//////////////////////////////////////////////////////////////

	// CASTING FUNCTIONS, INSERTED BY PREPROC

	/**
	 * Convert a char to a boolean. 'T', 't', and '1' will become the
	 * boolean value true, while 'F', 'f', or '0' will become false.
	 */
	/*
	 * static final public boolean parseBoolean(char what) {
	 * return ((what == 't') || (what == 'T') || (what == '1'));
	 * }
	 */

	/**
	 * <p>
	 * Convert an integer to a boolean. Because of how Java handles upgrading
	 * numbers, this will also cover byte and char (as they will upgrade to
	 * an int without any sort of explicit cast).
	 * </p>
	 * <p>
	 * The preprocessor will convert boolean(what) to parseBoolean(what).
	 * </p>
	 * 
	 * @return false if 0, true if any other number
	 */
	static final public boolean parseBoolean(int what) { return (what != 0); }

	/*
	 * // removed because this makes no useful sense
	 * static final public boolean parseBoolean(float what) {
	 * return (what != 0);
	 * }
	 */

	/**
	 * Convert the string "true" or "false" to a boolean.
	 * 
	 * @return true if 'what' is "true" or "TRUE", false otherwise
	 */
	static final public boolean parseBoolean(String what) { return Boolean.parseBoolean(what); }

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/*
	 * // removed, no need to introduce strange syntax from other languages
	 * static final public boolean[] parseBoolean(char what[]) {
	 * boolean outgoing[] = new boolean[what.length];
	 * for (int i = 0; i < what.length; i++) {
	 * outgoing[i] =
	 * ((what[i] == 't') || (what[i] == 'T') || (what[i] == '1'));
	 * }
	 * return outgoing;
	 * }
	 */

	/**
	 * Convert a byte array to a boolean array. Each element will be
	 * evaluated identical to the integer case, where a byte equal
	 * to zero will return false, and any other value will return true.
	 * 
	 * @return array of boolean elements
	 */
	/*
	 * static final public boolean[] parseBoolean(byte what[]) {
	 * boolean outgoing[] = new boolean[what.length];
	 * for (int i = 0; i < what.length; i++) {
	 * outgoing[i] = (what[i] != 0);
	 * }
	 * return outgoing;
	 * }
	 */

	/**
	 * Convert an int array to a boolean array. An int equal
	 * to zero will return false, and any other value will return true.
	 * 
	 * @return array of boolean elements
	 */
	static final public boolean[] parseBoolean(int what[]) {
		boolean outgoing[] = new boolean[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = (what[i] != 0);
		}
		return outgoing;
	}

	/*
	 * // removed, not necessary... if necessary, convert to int array first
	 * static final public boolean[] parseBoolean(float what[]) {
	 * boolean outgoing[] = new boolean[what.length];
	 * for (int i = 0; i < what.length; i++) {
	 * outgoing[i] = (what[i] != 0);
	 * }
	 * return outgoing;
	 * }
	 */

	static final public boolean[] parseBoolean(String what[]) {
		boolean outgoing[] = new boolean[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = Boolean.parseBoolean(what[i]);
		}
		return outgoing;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	static final public byte parseByte(boolean what) { return what ? (byte) 1 : 0; }

	static final public byte parseByte(char what) { return (byte) what; }

	static final public byte parseByte(int what) { return (byte) what; }

	static final public byte parseByte(float what) { return (byte) what; }

	/*
	 * // nixed, no precedent
	 * static final public byte[] parseByte(String what) { // note: array[]
	 * return what.getBytes();
	 * }
	 */

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	static final public byte[] parseByte(boolean what[]) {
		byte outgoing[] = new byte[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = what[i] ? (byte) 1 : 0;
		}
		return outgoing;
	}

	static final public byte[] parseByte(char what[]) {
		byte outgoing[] = new byte[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = (byte) what[i];
		}
		return outgoing;
	}

	static final public byte[] parseByte(int what[]) {
		byte outgoing[] = new byte[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = (byte) what[i];
		}
		return outgoing;
	}

	static final public byte[] parseByte(float what[]) {
		byte outgoing[] = new byte[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = (byte) what[i];
		}
		return outgoing;
	}

	/*
	 * static final public byte[][] parseByte(String what[]) { // note: array[][]
	 * byte outgoing[][] = new byte[what.length][];
	 * for (int i = 0; i < what.length; i++) {
	 * outgoing[i] = what[i].getBytes();
	 * }
	 * return outgoing;
	 * }
	 */

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/*
	 * static final public char parseChar(boolean what) { // 0/1 or T/F ?
	 * return what ? 't' : 'f';
	 * }
	 */

	static final public char parseChar(byte what) { return (char) (what & 0xff); }

	static final public char parseChar(int what) { return (char) what; }

	/*
	 * static final public char parseChar(float what) { // nonsensical
	 * return (char) what;
	 * }
	 * static final public char[] parseChar(String what) { // note: array[]
	 * return what.toCharArray();
	 * }
	 */

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/*
	 * static final public char[] parseChar(boolean what[]) { // 0/1 or T/F ?
	 * char outgoing[] = new char[what.length];
	 * for (int i = 0; i < what.length; i++) {
	 * outgoing[i] = what[i] ? 't' : 'f';
	 * }
	 * return outgoing;
	 * }
	 */

	static final public char[] parseChar(byte what[]) {
		char outgoing[] = new char[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = (char) (what[i] & 0xff);
		}
		return outgoing;
	}

	static final public char[] parseChar(int what[]) {
		char outgoing[] = new char[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = (char) what[i];
		}
		return outgoing;
	}

	/*
	 * static final public char[] parseChar(float what[]) { // nonsensical
	 * char outgoing[] = new char[what.length];
	 * for (int i = 0; i < what.length; i++) {
	 * outgoing[i] = (char) what[i];
	 * }
	 * return outgoing;
	 * }
	 * static final public char[][] parseChar(String what[]) { // note: array[][]
	 * char outgoing[][] = new char[what.length][];
	 * for (int i = 0; i < what.length; i++) {
	 * outgoing[i] = what[i].toCharArray();
	 * }
	 * return outgoing;
	 * }
	 */

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	static final public int parseInt(boolean what) { return what ? 1 : 0; }

	/**
	 * Note that parseInt() will un-sign a signed byte value.
	 */
	static final public int parseInt(byte what) { return what & 0xff; }

	/**
	 * Note that parseInt('5') is unlike String in the sense that it
	 * won't return 5, but the ascii value. This is because ((int) someChar)
	 * returns the ascii value, and parseInt() is just longhand for the cast.
	 */
	static final public int parseInt(char what) { return what; }

	/**
	 * Same as floor(), or an (int) cast.
	 */
	static final public int parseInt(float what) { return (int) what; }

	/**
	 * Parse a String into an int value. Returns 0 if the value is bad.
	 */
	static final public int parseInt(String what) { return parseInt(what, 0); }

	/**
	 * Parse a String to an int, and provide an alternate value that
	 * should be used when the number is invalid.
	 */
	static final public int parseInt(String what, int otherwise) {
		try {
			int offset = what.indexOf('.');
			if (offset == -1) {
				return Integer.parseInt(what);
			} else {
				return Integer.parseInt(what.substring(0, offset));
			}
		} catch (NumberFormatException e) {}
		return otherwise;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	static final public int[] parseInt(boolean what[]) {
		int list[] = new int[what.length];
		for (int i = 0; i < what.length; i++) {
			list[i] = what[i] ? 1 : 0;
		}
		return list;
	}

	static final public int[] parseInt(byte what[]) { // note this unsigns
		int list[] = new int[what.length];
		for (int i = 0; i < what.length; i++) {
			list[i] = (what[i] & 0xff);
		}
		return list;
	}

	static final public int[] parseInt(char what[]) {
		int list[] = new int[what.length];
		for (int i = 0; i < what.length; i++) {
			list[i] = what[i];
		}
		return list;
	}

	static public int[] parseInt(float what[]) {
		int inties[] = new int[what.length];
		for (int i = 0; i < what.length; i++) {
			inties[i] = (int) what[i];
		}
		return inties;
	}

	/**
	 * Make an array of int elements from an array of String objects.
	 * If the String can't be parsed as a number, it will be set to zero.
	 *
	 * String s[] = { "1", "300", "44" };
	 * int numbers[] = parseInt(s);
	 *
	 * numbers will contain { 1, 300, 44 }
	 */
	static public int[] parseInt(String what[]) { return parseInt(what, 0); }

	/**
	 * Make an array of int elements from an array of String objects.
	 * If the String can't be parsed as a number, its entry in the
	 * array will be set to the value of the "missing" parameter.
	 *
	 * String s[] = { "1", "300", "apple", "44" };
	 * int numbers[] = parseInt(s, 9999);
	 *
	 * numbers will contain { 1, 300, 9999, 44 }
	 */
	static public int[] parseInt(String what[], int missing) {
		int output[] = new int[what.length];
		for (int i = 0; i < what.length; i++) {
			try {
				output[i] = Integer.parseInt(what[i]);
			} catch (NumberFormatException e) {
				output[i] = missing;
			}
		}
		return output;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/*
	 * static final public float parseFloat(boolean what) {
	 * return what ? 1 : 0;
	 * }
	 */

	/**
	 * Convert an int to a float value. Also handles bytes because of
	 * Java's rules for upgrading values.
	 */
	static final public float parseFloat(int what) { // also handles byte
		return what;
	}

	static final public float parseFloat(String what) { return parseFloat(what, Float.NaN); }

	static final public float parseFloat(String what, float otherwise) {
		try {
			return Float.parseFloat(what);
		} catch (NumberFormatException e) {}

		return otherwise;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/*
	 * static final public float[] parseFloat(boolean what[]) {
	 * float floaties[] = new float[what.length];
	 * for (int i = 0; i < what.length; i++) {
	 * floaties[i] = what[i] ? 1 : 0;
	 * }
	 * return floaties;
	 * }
	 * static final public float[] parseFloat(char what[]) {
	 * float floaties[] = new float[what.length];
	 * for (int i = 0; i < what.length; i++) {
	 * floaties[i] = (char) what[i];
	 * }
	 * return floaties;
	 * }
	 */

	static final public float[] parseFloat(byte what[]) {
		float floaties[] = new float[what.length];
		for (int i = 0; i < what.length; i++) {
			floaties[i] = what[i];
		}
		return floaties;
	}

	static final public float[] parseFloat(int what[]) {
		float floaties[] = new float[what.length];
		for (int i = 0; i < what.length; i++) {
			floaties[i] = what[i];
		}
		return floaties;
	}

	static final public float[] parseFloat(String what[]) { return parseFloat(what, Float.NaN); }

	static final public float[] parseFloat(String what[], float missing) {
		float output[] = new float[what.length];
		for (int i = 0; i < what.length; i++) {
			try {
				output[i] = Float.parseFloat(what[i]);
			} catch (NumberFormatException e) {
				output[i] = missing;
			}
		}
		return output;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	static final public String str(boolean x) { return String.valueOf(x); }

	static final public String str(byte x) { return String.valueOf(x); }

	static final public String str(char x) { return String.valueOf(x); }

	static final public String str(int x) { return String.valueOf(x); }

	static final public String str(float x) { return String.valueOf(x); }

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	static final public String[] str(boolean x[]) {
		String s[] = new String[x.length];
		for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
		return s;
	}

	static final public String[] str(byte x[]) {
		String s[] = new String[x.length];
		for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
		return s;
	}

	static final public String[] str(char x[]) {
		String s[] = new String[x.length];
		for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
		return s;
	}

	static final public String[] str(int x[]) {
		String s[] = new String[x.length];
		for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
		return s;
	}

	static final public String[] str(float x[]) {
		String s[] = new String[x.length];
		for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
		return s;
	}

	//////////////////////////////////////////////////////////////

	// INT NUMBER FORMATTING

	static public String nf(float num) {
		int inum = (int) num;
		if (num == inum) {
			return str(inum);
		}
		return str(num);
	}

	static public String[] nf(float[] nums) {
		String[] outgoing = new String[nums.length];
		for (int i = 0; i < nums.length; i++) {
			outgoing[i] = nf(nums[i]);
		}
		return outgoing;
	}

	/**
	 * Integer number formatter.
	 */

	static private NumberFormat int_nf;
	static private int int_nf_digits;
	static private boolean int_nf_commas;

	/**
	 * ( begin auto-generated from nf.xml )
	 *
	 * Utility function for formatting numbers into strings. There are two
	 * versions, one for formatting floats and one for formatting ints. The
	 * values for the <b>digits</b>, <b>left</b>, and <b>right</b> parameters
	 * should always be positive integers.<br />
	 * <br />
	 * As shown in the above
	 * example, <b>nf()</b> is used to add zeros to the left and/or right of a
	 * number. This is typically for aligning a list of numbers. To
	 * <em>remove</em> digits from a floating-point number, use the
	 * <b>int()</b>, <b>ceil()</b>, <b>floor()</b>, or <b>round()</b>
	 * functions.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:string_functions
	 * @param nums
	 *            the numbers to format
	 * @param digits
	 *            number of digits to pad with zero
	 * @see PApplet#nfs(float, int, int)
	 * @see PApplet#nfp(float, int, int)
	 * @see PApplet#nfc(float, int)
	 * @see <a href="https://processing.org/reference/intconvert_.html">int(float)</a>
	 */

	static public String[] nf(int nums[], int digits) {
		String formatted[] = new String[nums.length];
		for (int i = 0; i < formatted.length; i++) {
			formatted[i] = nf(nums[i], digits);
		}
		return formatted;
	}

	/**
	 * @param num
	 *            the number to format
	 */
	static public String nf(int num, int digits) {
		if ((int_nf != null) &&
				(int_nf_digits == digits) &&
				!int_nf_commas) {
			return int_nf.format(num);
		}

		int_nf = NumberFormat.getInstance();
		int_nf.setGroupingUsed(false); // no commas
		int_nf_commas = false;
		int_nf.setMinimumIntegerDigits(digits);
		int_nf_digits = digits;
		return int_nf.format(num);
	}

	/**
	 * ( begin auto-generated from nfc.xml )
	 *
	 * Utility function for formatting numbers into strings and placing
	 * appropriate commas to mark units of 1000. There are two versions, one
	 * for formatting ints and one for formatting an array of ints. The value
	 * for the <b>digits</b> parameter should always be a positive integer.
	 * <br/>
	 * <br/>
	 * For a non-US locale, this will insert periods instead of commas, or
	 * whatever is apprioriate for that region.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:string_functions
	 * @param nums
	 *            the numbers to format
	 * @see PApplet#nf(float, int, int)
	 * @see PApplet#nfp(float, int, int)
	 * @see PApplet#nfs(float, int, int)
	 */
	static public String[] nfc(int nums[]) {
		String formatted[] = new String[nums.length];
		for (int i = 0; i < formatted.length; i++) {
			formatted[i] = nfc(nums[i]);
		}
		return formatted;
	}

	/**
	 * @param num
	 *            the number to format
	 */
	static public String nfc(int num) {
		if ((int_nf != null) &&
				(int_nf_digits == 0) &&
				int_nf_commas) {
			return int_nf.format(num);
		}

		int_nf = NumberFormat.getInstance();
		int_nf.setGroupingUsed(true);
		int_nf_commas = true;
		int_nf.setMinimumIntegerDigits(0);
		int_nf_digits = 0;
		return int_nf.format(num);
	}

	/**
	 * number format signed (or space)
	 * Formats a number but leaves a blank space in the front
	 * when it's positive so that it can be properly aligned with
	 * numbers that have a negative sign in front of them.
	 */

		/**
		 * ( begin auto-generated from nfs.xml )
		 *
		 * Utility function for formatting numbers into strings. Similar to
		 * <b>nf()</b> but leaves a blank space in front of positive numbers so
		 * they align with negative numbers in spite of the minus symbol. There are
		 * two versions, one for formatting floats and one for formatting ints. The
		 * values for the <b>digits</b>, <b>left</b>, and <b>right</b> parameters
		 * should always be positive integers.
		 *
		 * ( end auto-generated )
		 * 
		 * @webref data:string_functions
		 * @param num
		 *            the number to format
		 * @param digits
		 *            number of digits to pad with zeroes
		 * @see PApplet#nf(float, int, int)
		 * @see PApplet#nfp(float, int, int)
		 * @see PApplet#nfc(float, int)
		 */
	static public String nfs(int num, int digits) { return (num < 0) ? nf(num, digits) : (' ' + nf(num, digits)); }

	/**
	 * @param nums
	 *            the numbers to format
	 */
	static public String[] nfs(int nums[], int digits) {
		String formatted[] = new String[nums.length];
		for (int i = 0; i < formatted.length; i++) {
			formatted[i] = nfs(nums[i], digits);
		}
		return formatted;
	}

	//

	/**
	 * number format positive (or plus)
	 * Formats a number, always placing a - or + sign
	 * in the front when it's negative or positive.
	 */
		/**
		 * ( begin auto-generated from nfp.xml )
		 *
		 * Utility function for formatting numbers into strings. Similar to
		 * <b>nf()</b> but puts a "+" in front of positive numbers and a "-" in
		 * front of negative numbers. There are two versions, one for formatting
		 * floats and one for formatting ints. The values for the <b>digits</b>,
		 * <b>left</b>, and <b>right</b> parameters should always be positive integers.
		 *
		 * ( end auto-generated )
		 * 
		 * @webref data:string_functions
		 * @param num
		 *            the number to format
		 * @param digits
		 *            number of digits to pad with zeroes
		 * @see PApplet#nf(float, int, int)
		 * @see PApplet#nfs(float, int, int)
		 * @see PApplet#nfc(float, int)
		 */
	static public String nfp(int num, int digits) { return (num < 0) ? nf(num, digits) : ('+' + nf(num, digits)); }

	/**
	 * @param nums
	 *            the numbers to format
	 */
	static public String[] nfp(int nums[], int digits) {
		String formatted[] = new String[nums.length];
		for (int i = 0; i < formatted.length; i++) {
			formatted[i] = nfp(nums[i], digits);
		}
		return formatted;
	}

	//////////////////////////////////////////////////////////////

	// FLOAT NUMBER FORMATTING

	static private NumberFormat float_nf;
	static private int float_nf_left, float_nf_right;
	static private boolean float_nf_commas;

	/**
	 * @param left
	 *            number of digits to the left of the decimal point
	 * @param right
	 *            number of digits to the right of the decimal point
	 */
	static public String[] nf(float nums[], int left, int right) {
		String formatted[] = new String[nums.length];
		for (int i = 0; i < formatted.length; i++) {
			formatted[i] = nf(nums[i], left, right);
		}
		return formatted;
	}

	static public String nf(float num, int left, int right) {
		if ((float_nf != null) &&
				(float_nf_left == left) &&
				(float_nf_right == right) &&
				!float_nf_commas) {
			return float_nf.format(num);
		}

		float_nf = NumberFormat.getInstance();
		float_nf.setGroupingUsed(false);
		float_nf_commas = false;

		if (left != 0) float_nf.setMinimumIntegerDigits(left);
		if (right != 0) {
			float_nf.setMinimumFractionDigits(right);
			float_nf.setMaximumFractionDigits(right);
		}
		float_nf_left = left;
		float_nf_right = right;
		return float_nf.format(num);
	}

	/**
	 * @param right
	 *            number of digits to the right of the decimal point
	 */
	static public String[] nfc(float nums[], int right) {
		String formatted[] = new String[nums.length];
		for (int i = 0; i < formatted.length; i++) {
			formatted[i] = nfc(nums[i], right);
		}
		return formatted;
	}

	static public String nfc(float num, int right) {
		if ((float_nf != null) &&
				(float_nf_left == 0) &&
				(float_nf_right == right) &&
				float_nf_commas) {
			return float_nf.format(num);
		}

		float_nf = NumberFormat.getInstance();
		float_nf.setGroupingUsed(true);
		float_nf_commas = true;

		if (right != 0) {
			float_nf.setMinimumFractionDigits(right);
			float_nf.setMaximumFractionDigits(right);
		}
		float_nf_left = 0;
		float_nf_right = right;
		return float_nf.format(num);
	}

	/**
	 * @param left
	 *            the number of digits to the left of the decimal point
	 * @param right
	 *            the number of digits to the right of the decimal point
	 */
	static public String[] nfs(float nums[], int left, int right) {
		String formatted[] = new String[nums.length];
		for (int i = 0; i < formatted.length; i++) {
			formatted[i] = nfs(nums[i], left, right);
		}
		return formatted;
	}

	static public String nfs(float num, int left, int right) { return (num < 0) ? nf(num, left, right) : (' ' + nf(num, left, right)); }

	/**
	 * @param left
	 *            the number of digits to the left of the decimal point
	 * @param right
	 *            the number of digits to the right of the decimal point
	 */
	static public String[] nfp(float nums[], int left, int right) {
		String formatted[] = new String[nums.length];
		for (int i = 0; i < formatted.length; i++) {
			formatted[i] = nfp(nums[i], left, right);
		}
		return formatted;
	}

	static public String nfp(float num, int left, int right) { return (num < 0) ? nf(num, left, right) : ('+' + nf(num, left, right)); }

	//////////////////////////////////////////////////////////////

	// HEX/BINARY CONVERSION

	/**
	 * ( begin auto-generated from hex.xml )
	 *
	 * Converts a byte, char, int, or color to a String containing the
	 * equivalent hexadecimal notation. For example color(0, 102, 153) will
	 * convert to the String "FF006699". This function can help make your geeky
	 * debugging sessions much happier.
	 * <br/>
	 * <br/>
	 * Note that the maximum number of digits is 8, because an int value can
	 * only represent up to 32 bits. Specifying more than eight digits will
	 * simply shorten the string to eight anyway.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:conversion
	 * @param value
	 *            the value to convert
	 * @see PApplet#unhex(String)
	 * @see PApplet#binary(byte)
	 * @see PApplet#unbinary(String)
	 */
	static final public String hex(byte value) { return hex(value, 2); }

	static final public String hex(char value) { return hex(value, 4); }

	static final public String hex(int value) { return hex(value, 8); }

	/**
	 * @param digits
	 *            the number of digits (maximum 8)
	 */
	static final public String hex(int value, int digits) {
		String stuff = Integer.toHexString(value).toUpperCase();
		if (digits > 8) {
			digits = 8;
		}

		int length = stuff.length();
		if (length > digits) {
			return stuff.substring(length - digits);

		} else if (length < digits) {
			return "00000000".substring(8 - (digits - length)) + stuff;
		}
		return stuff;
	}

	/**
	 * ( begin auto-generated from unhex.xml )
	 *
	 * Converts a String representation of a hexadecimal number to its
	 * equivalent integer value.
	 *
	 * ( end auto-generated )
	 *
	 * @webref data:conversion
	 * @param value
	 *            String to convert to an integer
	 * @see PApplet#hex(int, int)
	 * @see PApplet#binary(byte)
	 * @see PApplet#unbinary(String)
	 */
	static final public int unhex(String value) {
		// has to parse as a Long so that it'll work for numbers bigger than 2^31
		return (int) (Long.parseLong(value, 16));
	}

	//

	/**
	 * Returns a String that contains the binary value of a byte.
	 * The returned value will always have 8 digits.
	 */
	static final public String binary(byte value) { return binary(value, 8); }

	/**
	 * Returns a String that contains the binary value of a char.
	 * The returned value will always have 16 digits because chars
	 * are two bytes long.
	 */
	static final public String binary(char value) { return binary(value, 16); }

	/**
	 * Returns a String that contains the binary value of an int. The length
	 * depends on the size of the number itself. If you want a specific number
	 * of digits use binary(int what, int digits) to specify how many.
	 */
	static final public String binary(int value) { return binary(value, 32); }

	/*
	 * Returns a String that contains the binary value of an int.
	 * The digits parameter determines how many digits will be used.
	 */

	/**
	 * ( begin auto-generated from binary.xml )
	 *
	 * Converts a byte, char, int, or color to a String containing the
	 * equivalent binary notation. For example color(0, 102, 153, 255) will
	 * convert to the String "11111111000000000110011010011001". This function
	 * can help make your geeky debugging sessions much happier.
	 * <br/>
	 * <br/>
	 * Note that the maximum number of digits is 32, because an int value can
	 * only represent up to 32 bits. Specifying more than 32 digits will simply
	 * shorten the string to 32 anyway.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:conversion
	 * @param value
	 *            value to convert
	 * @param digits
	 *            number of digits to return
	 * @see PApplet#unbinary(String)
	 * @see PApplet#hex(int,int)
	 * @see PApplet#unhex(String)
	 */
	static final public String binary(int value, int digits) {
		String stuff = Integer.toBinaryString(value);
		if (digits > 32) {
			digits = 32;
		}

		int length = stuff.length();
		if (length > digits) {
			return stuff.substring(length - digits);

		} else if (length < digits) {
			int offset = 32 - (digits - length);
			return "00000000000000000000000000000000".substring(offset) + stuff;
		}
		return stuff;
	}

	/**
	 * ( begin auto-generated from unbinary.xml )
	 *
	 * Converts a String representation of a binary number to its equivalent
	 * integer value. For example, unbinary("00001000") will return 8.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref data:conversion
	 * @param value
	 *            String to convert to an integer
	 * @see PApplet#binary(byte)
	 * @see PApplet#hex(int,int)
	 * @see PApplet#unhex(String)
	 */
	static final public int unbinary(String value) { return Integer.parseInt(value, 2); }

	static public int blendColor(int c1, int c2, int mode) { return SImage.blendColor(c1, c2, mode); }

	//////////////////////////////////////////////////////////////

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	static private boolean lookAndFeelCheck;

	/**
	 * Initialize the Look & Feel if it hasn't been already.
	 * Call this before using any Swing-related code in PApplet methods.
	 */
	static private void checkLookAndFeel() {
		if (!lookAndFeelCheck) {
			if (platform == WINDOWS) {
				// Windows is defaulting to Metal or something else awful.
				// Which also is not scaled properly with HiDPI interfaces.
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {}
			}
			lookAndFeelCheck = true;
		}
	}

	//////////////////////////////////////////////////////////////

	/**
	 * ( begin auto-generated from print.xml )
	 *
	 * Writes to the console area of the Processing environment. This is often
	 * helpful for looking at the data a program is producing. The companion
	 * function <b>println()</b> works like <b>print()</b>, but creates a new
	 * line of text for each call to the function. Individual elements can be
	 * separated with quotes ("") and joined with the addition operator (+).<br />
	 * <br />
	 * Beginning with release 0125, to print the contents of an array, use
	 * println(). There's no sensible way to do a <b>print()</b> of an array,
	 * because there are too many possibilities for how to separate the data
	 * (spaces, commas, etc). If you want to print an array as a single line,
	 * use <b>join()</b>. With <b>join()</b>, you can choose any delimiter you
	 * like and <b>print()</b> the result.<br />
	 * <br />
	 * Using <b>print()</b> on an object will output <b>null</b>, a memory
	 * location that may look like "@10be08," or the result of the
	 * <b>toString()</b> method from the object that's being printed. Advanced
	 * users who want more useful output when calling <b>print()</b> on their
	 * own classes can add a <b>toString()</b> method to the class that returns
	 * a String.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref output:text_area
	 * @usage IDE
	 * @param what
	 *            data to print to console
	 * @see PApplet#println()
	 * @see PApplet#printArray(Object)
	 * @see PApplet#join(String[], char)
	 */
	static public void print(byte what) {
		System.out.print(what);
		System.out.flush();
	}

	static public void print(boolean what) {
		System.out.print(what);
		System.out.flush();
	}

	static public void print(char what) {
		System.out.print(what);
		System.out.flush();
	}

	static public void print(int what) {
		System.out.print(what);
		System.out.flush();
	}

	static public void print(long what) {
		System.out.print(what);
		System.out.flush();
	}

	static public void print(float what) {
		System.out.print(what);
		System.out.flush();
	}

	static public void print(double what) {
		System.out.print(what);
		System.out.flush();
	}

	static public void print(String what) {
		System.out.print(what);
		System.out.flush();
	}

	/**
	 * @param variables
	 *            list of data, separated by commas
	 */
	static public void print(Object... variables) {
		StringBuilder sb = new StringBuilder();
		for (Object o : variables) {
			if (sb.length() != 0) {
				sb.append(" ");
			}
			if (o == null) {
				sb.append("null");
			} else {
				sb.append(o.toString());
			}
		}
		System.out.print(sb.toString());
	}

	/*
	 * static public void print(Object what) {
	 * if (what == null) {
	 * // special case since this does fuggly things on > 1.1
	 * System.out.print("null");
	 * } else {
	 * System.out.println(what.toString());
	 * }
	 * }
	 */

	/**
	 * ( begin auto-generated from println.xml )
	 *
	 * Writes to the text area of the Processing environment's console. This is
	 * often helpful for looking at the data a program is producing. Each call
	 * to this function creates a new line of output. Individual elements can
	 * be separated with quotes ("") and joined with the string concatenation
	 * operator (+). See <b>print()</b> for more about what to expect in the output.
	 * <br/>
	 * <br/>
	 * <b>println()</b> on an array (by itself) will write the
	 * contents of the array to the console. This is often helpful for looking
	 * at the data a program is producing. A new line is put between each
	 * element of the array. This function can only print one dimensional
	 * arrays. For arrays with higher dimensions, the result will be closer to
	 * that of <b>print()</b>.
	 *
	 * ( end auto-generated )
	 * 
	 * @webref output:text_area
	 * @usage IDE
	 * @see PApplet#print(byte)
	 * @see PApplet#printArray(Object)
	 */
	static public void println() { System.out.println(); }

	/**
	 * @param what
	 *            data to print to console
	 */
	static public void println(byte what) {
		System.out.println(what);
		System.out.flush();
	}

	static public void println(boolean what) {
		System.out.println(what);
		System.out.flush();
	}

	static public void println(char what) {
		System.out.println(what);
		System.out.flush();
	}

	static public void println(int what) {
		System.out.println(what);
		System.out.flush();
	}

	static public void println(long what) {
		System.out.println(what);
		System.out.flush();
	}

	static public void println(float what) {
		System.out.println(what);
		System.out.flush();
	}

	static public void println(double what) {
		System.out.println(what);
		System.out.flush();
	}

	static public void println(String what) {
		System.out.println(what);
		System.out.flush();
	}

	/**
	 * @param variables
	 *            list of data, separated by commas
	 */
	static public void println(Object... variables) {
		//	    System.out.println("got " + variables.length + " variables");
		print(variables);
		println();
	}

	/*
	 * // Breaking this out since the compiler doesn't know the difference between
	 * // Object... and just Object (with an array passed in). This should take care
	 * // of the confusion for at least the most common case (a String array).
	 * // On second thought, we're going the printArray() route, since the other
	 * // object types are also used frequently.
	 * static public void println(String[] array) {
	 * for (int i = 0; i < array.length; i++) {
	 * System.out.println("[" + i + "] \"" + array[i] + "\"");
	 * }
	 * System.out.flush();
	 * }
	 */

	/**
	 * For arrays, use printArray() instead. This function causes a warning
	 * because the new print(Object...) and println(Object...) functions can't
	 * be reliably bound by the compiler.
	 */
	static public void println(Object what) {
		if (what == null) {
			System.out.println("null");
		} else if (what.getClass().isArray()) {
			printArray(what);
		} else {
			System.out.println(what.toString());
			System.out.flush();
		}
	}

	/**
	 * ( begin auto-generated from printArray.xml )
	 *
	 * To come...
	 *
	 * ( end auto-generated )
	 * 
	 * @webref output:text_area
	 * @param what
	 *            one-dimensional array
	 * @usage IDE
	 * @see PApplet#print(byte)
	 * @see PApplet#println()
	 */
	static public void printArray(Object what) {
		if (what == null) {
			// special case since this does fuggly things on > 1.1
			System.out.println("null");

		} else {
			String name = what.getClass().getName();
			if (name.charAt(0) == '[') {
				switch (name.charAt(1)) {
					case '[':
						// don't even mess with multi-dimensional arrays (case '[')
						// or anything else that's not int, float, boolean, char
						System.out.println(what);
						break;

					case 'L':
						// print a 1D array of objects as individual elements
						Object poo[] = (Object[]) what;
						for (int i = 0; i < poo.length; i++) {
							if (poo[i] instanceof String) {
								System.out.println("[" + i + "] \"" + poo[i] + "\"");
							} else {
								System.out.println("[" + i + "] " + poo[i]);
							}
						}
						break;

					case 'Z': // boolean
						boolean zz[] = (boolean[]) what;
						for (int i = 0; i < zz.length; i++) {
							System.out.println("[" + i + "] " + zz[i]);
						}
						break;

					case 'B': // byte
						byte bb[] = (byte[]) what;
						for (int i = 0; i < bb.length; i++) {
							System.out.println("[" + i + "] " + bb[i]);
						}
						break;

					case 'C': // char
						char cc[] = (char[]) what;
						for (int i = 0; i < cc.length; i++) {
							System.out.println("[" + i + "] '" + cc[i] + "'");
						}
						break;

					case 'I': // int
						int ii[] = (int[]) what;
						for (int i = 0; i < ii.length; i++) {
							System.out.println("[" + i + "] " + ii[i]);
						}
						break;

					case 'J': // int
						long jj[] = (long[]) what;
						for (int i = 0; i < jj.length; i++) {
							System.out.println("[" + i + "] " + jj[i]);
						}
						break;

					case 'F': // float
						float ff[] = (float[]) what;
						for (int i = 0; i < ff.length; i++) {
							System.out.println("[" + i + "] " + ff[i]);
						}
						break;

					case 'D': // double
						double dd[] = (double[]) what;
						for (int i = 0; i < dd.length; i++) {
							System.out.println("[" + i + "] " + dd[i]);
						}
						break;

					default:
						System.out.println(what);
				}
			} else { // not an array
				System.out.println(what);
			}
		}
		System.out.flush();
	}

	//	static public void debug(String msg) {
	//		if (DEBUG) println(msg);
	//	}

	//	public GraphicsConfiguration getGC() { return screen.getGC(); }
}
