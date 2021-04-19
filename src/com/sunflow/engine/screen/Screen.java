package com.sunflow.engine.screen;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.EventListener;

import com.sunflow.engine.Mouse;
import com.sunflow.game.GameBase;
import com.sunflow.gfx.SGraphics;
import com.sunflow.math.SVector;

public abstract class Screen {

	protected static float aimSize = 3;
	protected static Color aimColor = Color.black;

	public String title, title_info;

	public int width, height;
	public float scaleWidth, scaleHeight;
	public int scaledWidth, scaledHeight;

	protected int frameWidth, frameHeight;

	protected boolean isCreated;

	public boolean undecorated;
	protected boolean fullscreen;

	protected SVector savedSize;
	protected SVector savedPos;

//	public float mouseX, mouseY;
//	public float lastMouseX, lastMouseY;
//	public float mouseScreenX, mouseScreenY;

	protected boolean mousePressed;
	protected int button;
	protected double mouseWheel;
	protected double mouseWheelOld;
	protected double mouseWheelNew;
	protected char key;
	protected int keyCode;

	protected boolean[] keys = new boolean[65536];
	protected boolean[] keysNew = new boolean[keys.length];
	protected boolean[] keysOld = new boolean[keys.length];

	protected boolean[] mouseButtons = new boolean[MouseInfo.getNumberOfButtons()];
	protected boolean[] mouseButtonsNew = new boolean[mouseButtons.length];
	protected boolean[] mouseButtonsOld = new boolean[mouseButtons.length];

	// Overlay
	protected boolean showOverlay;
	protected boolean showInfo;
	protected boolean showCrosshair;

	protected GameBase game;
	protected SGraphics graphics;

	protected Mouse mouse;

	public Screen(GameBase game, Mouse mouse) {
		this.game = game;
		this.graphics = game.getGraphics();
		this.mouse = mouse;
		this.title = "";
		this.savedSize = new SVector();
		this.savedPos = new SVector();
	}

	public void refresh() {
		this.frameWidth = 0;
		this.frameHeight = 0;

//		this.mouseX = 0;
//		this.mouseY = 0;
//		this.mouseScreenX = 0;
//		this.mouseScreenY = 0;

		this.fullscreen = false;
		this.showInfo = true;
	}

	public void privateUpdate() {
		for (int i = 0; i < keys.length; i++) keysOld[i] = keysNew[i];
		for (int i = 0; i < mouseButtons.length; i++) mouseButtonsOld[i] = mouseButtonsNew[i];

		for (int i = 0; i < keys.length; i++) keysNew[i] = keys[i];
		for (int i = 0; i < mouseButtons.length; i++) mouseButtonsNew[i] = mouseButtons[i];
		mouseWheelOld = mouseWheelNew;
		mouseWheelNew = mouseWheel;
		mouseWheel = 0;

//		mouseScreenX = MouseInfo.getPointerInfo().getLocation().x;
//		mouseScreenY = MouseInfo.getPointerInfo().getLocation().y;
		Point mSP = MouseInfo.getPointerInfo().getLocation();
		mouse.updateScreenPosition(mSP.x, mSP.y);
	}

	public abstract boolean render();

	public void preDraw() {}

	public void postDraw() {}

	public void setTitle(String title) { this.title = title; }

	public void setTitleInfo(String title_info) { this.title_info = title_info; }

	public void setUndecorated(boolean undecorated) { this.undecorated = undecorated; }

	public abstract void defaultSettings();

	public abstract void createScreen();

	public abstract void createCanvas(int width, int height, float scaleW, float scaleH);

	public abstract void show();

	public abstract void requestFocus();

	public abstract void toggleFullscreen();

	public abstract int getX();

	public abstract int getY();

	public abstract int getScreenX();

	public abstract int getScreenY();

	public char key() { return key; }

	public int keyCode() { return keyCode; }

	public boolean[] keys() { return keys; }

	public boolean keyIsDown(char key) { return keyIsDown(KeyEvent.getExtendedKeyCodeForChar(key)); }

	public boolean keyIsPressed(char key) { return keyIsPressed(KeyEvent.getExtendedKeyCodeForChar(key)); }

	public boolean keyIsHeld(char key) { return keyIsHeld(KeyEvent.getExtendedKeyCodeForChar(key)); }

	public boolean keyIsReleased(char key) { return keyIsReleased(KeyEvent.getExtendedKeyCodeForChar(key)); }

	public boolean keyIsDown(int key) { if (key < 0 || key > keys.length) return false; return keysNew[key]; }

	public boolean keyIsPressed(int key) { if (key < 0 || key > keys.length) return false; return !keysOld[key] && keysNew[key]; }

	public boolean keyIsHeld(int key) { if (key < 0 || key > keys.length) return false; return keysOld[key] && keysNew[key]; }

	public boolean keyIsReleased(int key) { if (key < 0 || key > keys.length) return false; return keysOld[key] && !keysNew[key]; }

	public boolean mouseIsDown(int button) { if (button < 0 || button > mouseButtons.length) return false; return mouseButtonsNew[button]; }

	public boolean mouseIsPressed(int button) { if (button < 0 || button > mouseButtons.length) return false; return !mouseButtonsOld[button] && mouseButtonsNew[button]; }

	public boolean mouseIsHeld(int button) { if (button < 0 || button > mouseButtons.length) return false; return mouseButtonsOld[button] && mouseButtonsNew[button]; }

	public boolean mouseIsReleased(int button) { if (button < 0 || button > mouseButtons.length) return false; return mouseButtonsOld[button] && !mouseButtonsNew[button]; }

	public double mouseWheel() { return mouseWheelNew; }

	public boolean mousePressed() { return mousePressed; }

//	public int getMouseX() { return (int) mouseX; }
//
//	public int getMouseY() { return (int) mouseY; }
//
//	public int getLastMouseX() { return (int) lastMouseX; }
//
//	public int getLastMouseY() { return (int) lastMouseY; }

	public int getWidth() { return width; }

	public int getHeight() { return height; }

	public boolean isCreated() { return isCreated; }

	public abstract void drawCrosshair();

	public abstract void drawOverlay();

	public abstract void drawInfo();

//	public abstract void updateMousePosition(float x, float y);

	public abstract boolean addListener(EventListener listener);

	public abstract boolean removeListener(EventListener listener);

	public abstract boolean addListener(com.sunflow.engine.eventsystem.listeners.SEventListener listener);

	public abstract boolean removeListener(com.sunflow.engine.eventsystem.listeners.SEventListener listener);

	public abstract void setCursor(Cursor cursor);

	public abstract boolean hasFocus();

	public final void showOverlay(boolean show) { showOverlay = show; }

	public final void showInfo(boolean show) {
		showInfo = show;
		handleOverlay();
	}

	public final void showCrosshair(boolean show) {
		showCrosshair = show;
		handleOverlay();
	}

	private void handleOverlay() {
		if (showInfo || showCrosshair) showOverlay = true; // if(showInfo || showX || show??? || ...
		else showOverlay = false;
	}

	public abstract void infoSize(float size);

}
