package com.sunflow.gfx;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import com.sunflow.game.Game3D;
import com.sunflow.interfaces.FrameLoopListener;

public class PeasyCam implements FrameLoopListener, MouseMotionListener {

	private Game3D screen;

	private float zr;
	private float yr;

	private boolean flipedZ;
	private boolean flipedY;

	public PeasyCam(Game3D screen) {
		this.screen = screen;
		screen.addListener(this);
	}

	public void flip() {
		flipedZ = !flipedZ;
		flipedY = !flipedY;
	}

	public void flipZ() {
		flipedY = !flipedY;
	}

	public void flipY() {
		flipedY = !flipedY;
	}

	@Override
	public void update() {
		screen.rotateZTo(zr);
		screen.rotateYTo(yr);
		screen.updateView();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		float fz, fy;
		fz = fy = 0.01f;
		if (flipedZ) fz *= -1;
		if (flipedY) fy *= -1;

		zr += (screen.prevMouseX - screen.mouseX) * fz;
		yr += (screen.prevMouseY - screen.mouseY) * fy;
	}

	@Override
	public void mouseMoved(MouseEvent e) {}
}
