package com.sunflow.engine.screen;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

import com.sunflow.game.GameBase;
import com.sunflow.math.SVector;

public abstract class Screen {

	protected static float aimSize = 3;
	protected static Color aimColor = Color.black;

	public String title, title_info;

	public float width, height;
	public float scaleWidth, scaleHeight;
	public int scaledWidth, scaledHeight;

	protected int frameWidth, frameHeight;

	protected boolean createdScreen;

	public boolean undecorated;
	protected boolean fullscreen;

	protected SVector savedSize;
	protected SVector savedPos;

	public float mouseX, mouseY;
	public float lastMouseX, lastMouseY;
	public float mouseScreenX, mouseScreenY;

	protected boolean mousePressed;
	protected int button;
	protected char key;
	protected int keyCode;

	protected boolean[] keys = new boolean[65536];
	protected boolean[] mouse = new boolean[MouseInfo.getNumberOfButtons()];

	// Overlay
	protected boolean showOverlay;
	protected boolean showInfo;
	protected boolean showCrosshair;

	protected GameBase game;

	public Screen(GameBase game) {
		this.game = game;
		title = "";
		savedSize = new SVector();
		savedPos = new SVector();
	}

	public void refresh() {
		frameWidth = 0;
		frameHeight = 0;

		mouseX = 0;
		mouseY = 0;
		mouseScreenX = 0;
		mouseScreenY = 0;

		fullscreen = false;
		showInfo = true;
	}

	public void privateUpdate() {}

	public abstract boolean render();

	public void preDraw() {}

	public void postDraw() {}

	public void setTitle(String title) { this.title = title; }

	public void setTitleInfo(String title_info) { this.title_info = title_info; }

	public void setUndecorated(boolean undecorated) { this.undecorated = undecorated; }

	public abstract void defaultSettings();

	public abstract void createScreen();

	public abstract void createCanvas(float width, float height, float scaleW, float scaleH);

	public abstract void show();

	public abstract void requestFocus();

	public abstract void toggleFullscreen();

	public abstract int getX();

	public abstract int getY();

	public abstract int getScreenX();

	public abstract int getScreenY();

	public abstract boolean keyIsDown(char key);

	public abstract boolean keyIsDown(int key);

	public abstract boolean mouseIsDown(int button);

	public boolean mousePressed() { return mousePressed; }

	public int getMouseX() { return (int) mouseX; }

	public int getMouseY() { return (int) mouseY; }

	public int getLastMouseX() { return (int) lastMouseX; }

	public int getLastMouseY() { return (int) lastMouseY; }

	public int getWidth() { return (int) width; }

	public int getHeight() { return (int) height; }

	public abstract void drawCrosshair();

	public abstract void drawOverlay();

	public abstract void drawInfo();

//	public abstract void updateMousePosition(float x, float y);

	public abstract boolean addKeyListener(KeyListener listener);

	public abstract boolean addMouseListener(MouseListener listener);

	public abstract boolean addMouseWheelListener(MouseWheelListener listener);

	public abstract boolean addMouseMotionListener(MouseMotionListener listener);

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

	public void updateMousePosition(float x, float y) {
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		mouseX = x / scaleWidth;
		mouseY = y / scaleHeight;
	}
}
