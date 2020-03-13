package com.sunflow.gfx;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import com.sunflow.game.Game3D;
import com.sunflow.interfaces.FrameLoopListener;
import com.sunflow.util.Constants;
import com.sunflow.util.MathUtils;

public class PeasyCam implements FrameLoopListener, MouseMotionListener, MathUtils, Constants {

	private Game3D screen;

	private float xr, yr, zr;

	private boolean flipedX;
	private boolean flipedY;

	public PeasyCam(Game3D screen) {
		this.screen = screen;
		screen.addListener(this);
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
		screen.rotateXTo(xr);
		screen.rotateYTo(yr);
		screen.rotateZTo(zr);
		screen.updateView();
//		zr += 0.01f;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		float change = 0.006f;
		float mxc = (screen.mouseX - screen.prevMouseX) * (flipedX ? -change : change);
		float myc = (screen.mouseY - screen.prevMouseY) * (flipedY ? -change : change);
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
	public void mouseMoved(MouseEvent e) {}
}
