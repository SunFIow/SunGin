package com.sunflow.examples;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.List;

import com.sunflow.game.Game3D;
import com.sunflow.math3d.models.Base3DModel;
import com.sunflow.math3d.models.Cube;
import com.sunflow.math3d.models.DPolygon;
import com.sunflow.math3d.models.GenerateTerrain;
import com.sunflow.math3d.models.Pyramid;
import com.sunflow.math3d.models.Sphere;

public class ExampleGame3D extends Game3D {
	public static void main(String[] args) { new ExampleGame3D(); }

	private Base3DModel model;

	private boolean auto = true;

	@Override
	public void setup() {
		title("ExampleGame 3D");
		undecorated(true);
		createCanvas(1280, 720);
		mode(ASYNC);
		frameRate(144);
		tickRate(60);
		showInfo(true);

		Models.add(new Pyramid(this, 0, -5, 0, 2, 2, 2, Color.green));

		Models.add(new Cube(this, 0, 0, 0, 2, 2, 2));
		Models.add(new Cube(this, 18, -5, 0, 2, 2, 2));
		Models.add(new Cube(this, 20, -5, 0, 2, 2, 2));
		Models.add(new Cube(this, 22, -5, 0, 2, 2, 2));
		Models.add(new Cube(this, 20, -5, 2, 2, 2, 2));

		new GenerateTerrain(this, Models);

		model = new Sphere(this, 50, 0, 15, 10, 30);
		Models.add(model);
	}

	double showDelta, showMult;

	@Override
	public List<String> getInfo() {
		List<String> info = super.getInfo();

		info.add(showDelta + " ∆");
		info.add(showMult + " x");

		float x = vCameraPos.x;
		float y = vCameraPos.y;
		float z = vCameraPos.z;
		info.add("[" + (int) x + "]" + "[" + (int) y + "]" + "[" + (int) z + "]");
		x = vCameraDir.x - x;
		y = vCameraDir.y - y;
		z = vCameraDir.z - z;
		info.add("[" + x + "]" + "[" + y + "]" + "[" + z + "]");
		return info;
	}

	@Override
	protected void update() {
		// rotate and update shape examples
//		if (!auto) return;
		Models.get(0).rotateX(2f * delta);
		Models.get(1).rotateX(0.5f * delta);
	}

	@Override
	public void draw() {
		if (frameCount % frameRate == 0) {
			showDelta = delta;
			showMult = multiplier;
		}

//		// Calculated all that is general for this camera position
//		Calculator.SetPrederterminedInfo(this);
//		cameraMovement();

		background(168, 211, 255);

		// Draw Models in the Order that is set by the 'setOrder' function
		for (int i = 0; i < drawOrder.length; i++) {
			DPolygon current = DPolygone.get(drawOrder[i]);
			current.renderOutline(outlines);
			current.render(graphics);
		}
	}

	@Override
	public void keyPressed(KeyEvent event) {
		float vel = 30f;
		float rotVel = 0.04f;
		switch (event.getKeyCode()) {
			case KeyEvent.VK_P:
				auto = !auto;
				break;
			case LEFT:
				if (event.isControlDown()) {
					if (event.isShiftDown()) {
						model.rotateZ(-rotVel);
					} else {
						model.rotateY(rotVel);
					}
				} else {
					model.translateModel(-vel, 0, 0);
				}
				break;
			case RIGHT:
				if (event.isControlDown()) {
					if (event.isShiftDown()) {
						model.rotateZ(rotVel);
					} else {
						model.rotateY(-rotVel);
					}
				} else {
					model.translateModel(vel, 0, 0);
				}
				break;
			case UP:
				if (event.isShiftDown()) {
					model.translateModel(0, 0, vel);
				} else if (event.isControlDown()) {
					model.rotateX(rotVel);
				} else {
					model.translateModel(0, -vel, 0);
				}
				break;
			case DOWN:
				if (event.isShiftDown()) {
					model.translateModel(0, 0, -vel);
				} else if (event.isControlDown()) {
					model.rotateX(-rotVel);
				} else {
					model.translateModel(0, vel, 0);
				}
				break;
		}
	}
}
