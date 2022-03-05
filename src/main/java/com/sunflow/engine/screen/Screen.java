package com.sunflow.engine.screen;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.Point;
import java.util.EventListener;

import com.sunflow.engine.Keyboard;
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

	protected Keyboard keyboard;
	protected Mouse mouse;

	// Overlay
	protected boolean showOverlay;
	protected boolean showInfo;
	protected boolean showCrosshair;

	protected GameBase game;
	protected SGraphics graphics;

	public Screen(GameBase game, Mouse mouse, Keyboard keyboard) {
		this.game = game;
		this.graphics = game.getGraphics();
		this.mouse = mouse;
		this.keyboard = keyboard;
		this.title = "";
		this.savedSize = new SVector();
		this.savedPos = new SVector();
	}

	public void refresh() {
		this.frameWidth = 0;
		this.frameHeight = 0;
		this.fullscreen = false;
		this.showInfo = true;
	}

	public void preUpdate() {}

	public void postUpdate() {}

	protected abstract void render();

	public void preDraw() {
		keyboard.update();

		Point mSP = MouseInfo.getPointerInfo().getLocation();
		mouse.updateScreenPosition(mSP.x, mSP.y);
		mouse.update();
	}

	public void postDraw() { render(); }

	public void setTitle(String title) { this.title = title; }

	public void setTitleInfo(String title_info) { this.title_info = title_info; }

	public void setUndecorated(boolean undecorated) { this.undecorated = undecorated; }

	public abstract void defaultSettings();

	public abstract void createScreen();

	protected abstract void createListeners();

	protected abstract void destroyListeners();

	public void createCanvas(int width, int height, float scaleW, float scaleH) {
		if (!isCreated) {
			isCreated = true;
			this.width = width;
			this.height = height;
			this.scaleWidth = scaleW;
			this.scaleHeight = scaleH;

			this.scaledWidth = (int) (width * scaleWidth);
			this.scaledHeight = (int) (height * scaleHeight);

			createScreen();
			createListeners();
		}
	}

	public abstract void show();

	public abstract void requestFocus();

	public abstract void toggleFullscreen();

	public abstract int getX();

	public abstract int getY();

	public abstract int getScreenX();

	public abstract int getScreenY();

	public int width() { return width; }

	public int height() { return height; }

	public boolean isCreated() { return isCreated; }

	public abstract void drawCrosshair();

	public abstract void drawOverlay();

	public abstract void drawInfo();

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

	//	public abstract GraphicsConfiguration getGC();

}
