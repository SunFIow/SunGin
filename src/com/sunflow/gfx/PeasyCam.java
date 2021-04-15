package com.sunflow.gfx;

import com.sunflow.engine.eventsystem.events.MouseMotionEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent.MouseDraggedEvent;
import com.sunflow.engine.eventsystem.events.MouseMotionEvent.MouseMovedEvent;
import com.sunflow.engine.eventsystem.listeners.MouseMotionListener;
import com.sunflow.game.Game3D;
import com.sunflow.interfaces.FrameLoopListener;
import com.sunflow.util.SConstants;
import com.sunflow.util.MathUtils;

public class PeasyCam implements FrameLoopListener, MouseMotionListener, MathUtils, SConstants {

	private Game3D game;

	private float xr, yr, zr;

	private boolean flipedX;
	private boolean flipedY;

	public PeasyCam(Game3D game) {
		this.game = game;
		game.addListener(this);
	}

	public void flip() {
		flipedX = !flipedX;
		flipedY = !flipedY;
	}

	public void flipX() {
		flipedY = !flipedY;
	}

	public void flipY() {
		flipedY = !flipedY;
	}

	@Override
	public void update() {
		game.rotateXTo(xr);
		game.rotateYTo(yr);
		game.rotateZTo(zr);
		game.updateView();
//		zr += 0.01f;
	}

//	@Override
//	public void mouseDragged(MouseEvent e) {
	@Override
	public void onMouseDragged(MouseDraggedEvent event) {
		float change = 0.006f;
		float mxc = (game.mouseX() - game.lastMouseX()) * (flipedX ? -change : change);
		float myc = (game.mouseY() - game.lastMouseY()) * (flipedY ? -change : change);
		float xc = 0, yc = 0, zc = 0;

		// x - axis
		xc -= myc;

		// y - axis
		yc += mxc;

		// z - axis
//		zc += myc;

		xr += xc;
		yr += yc;
		zr += zc;
	}

//	@Override
//	public void mouseDragged(MouseEvent e) {
//		float change = 0.003f;
//		float mxc = (screen.mouseX - screen.prevMouseX) * (flipedX ? -change : change);
//		float myc = (screen.mouseY - screen.prevMouseY) * (flipedY ? -change : change);
//		float xc = 0, yc = 0, zc = 0;
//
//		// x - axis
//		xc -= myc * cos(yr);
//		xc -= myc * cos(zr);
//		xc -= mxc * sin(zr);
//
//		// y - axis
//		yc += mxc * cos(xr);
//		yc += mxc * cos(zr);
//		yc += myc * sin(zr);
//
//		// z - axis
//		zc -= mxc * sin(xr);
//		zc -= myc * sin(yr);
//
//		xr += xc;
//		yr += yc;
//		zr += zc;
//	}

	@Override
	public void onMouseMotion(MouseMotionEvent event) {}

	@Override
	public void onMouseMoved(MouseMovedEvent event) {}

}
