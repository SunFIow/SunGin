package com.sunflow.examples;

import java.awt.Color;
import java.awt.event.KeyEvent;

import com.sunflow.game.Game3D;
import com.sunflow.math3d.models.Base3DModel;
import com.sunflow.math3d.models.Cube;
import com.sunflow.math3d.models.DPolygon;
import com.sunflow.math3d.models.Sphere;

public class ExampleGame3D extends Game3D {

	private Base3DModel model;

	private boolean auto = true;

	public static void main(String[] args) {
		new ExampleGame3D();
	}

	@Override
	public void setup() {
		createCanvas(1280, 720);
		frameRate(144);
		showTPS();

		model = new Sphere(this, 0, 0, 1500, 750, 30);
//		model = new Cube(0, 0, 1500, 750, 750, 750);
		Models.add(model);

		for (int i = 0; i < 5; i++) {
			int x = random.nextInt(4000) - 2000;
			int y = random.nextInt(4000) - 2000;
			int z = random.nextInt(3000) + 1000;
			Base3DModel cube = new Cube(this, x, y, z, 500, 500, 500);
			Models.add(cube);
		}
	}

	@Override
	protected void update() {
		// rotate and update shape examples
		if (auto) {
			for (int i = 1; i < Models.size(); i++) {
				Base3DModel m = Models.get(i);
				m.rotateX(0.005 * multiplier);
			}
		}
	}

	@Override
	public void draw() {
//		// Calculated all that is general for this camera position
//		Calculator.SetPrederterminedInfo(this);
//		cameraMovement();

		background(168, 211, 255);

		pushMatrix();
		graphics.translate(width / 2, height / 2);

		for (int i = 0; i < drawOrder.length; i++) {
			DPolygon pol = DPolygone.get(drawOrder[i]);

			int scale = getFillScale(pol);
			Color fill = new Color(scale, scale, scale);
			pol.render(graphics, true, fill, outlines, new Color(50, 50, 50));
//			pol.render(privateG, true, outlines);
		}

		// Draw Models in the Order that is set by the 'setOrder' function
//		for (int i = 0; i < drawOrder.length; i++) DPolygone.get(drawOrder[i]).render(privateG, true, outlines);

//		info(vCameraPos);
//		info(vCameraDir);

		popMatrix();
	}

	double sourceMin, sourceMax;

	private int getFillScale(DPolygon pol) {
		Base3DModel polM = null;

		int scale;
		double value = pol.dist;

		for (Base3DModel m : Models) {
			for (DPolygon p : m.polys) {
				if (p.equals(pol)) {
					polM = m;
					break;
				}
			}
		}

//		sourceMin = DPolygone.get(drawOrder[drawOrder.length - 1]).dist;
//		sourceMax = DPolygone.get(drawOrder[0]).dist;

		sourceMin = sourceMax = polM.polys[0].dist;

		for (DPolygon p : polM.polys) {
			if (p.dist < sourceMin)
				sourceMin = p.dist;
			if (p.dist > sourceMax)
				sourceMax = p.dist;
		}

		scale = (int) map(value, sourceMin, sourceMax, 200, 0);
		return scale;
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
