package com.sunflow.examples;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.List;

import com.sunflow.game.Game3D;
import com.sunflow.math3d.models.Base3DModel;
import com.sunflow.math3d.models.DPolygon;

public class ExampleGame3D extends Game3D {

	private Base3DModel model;

	private boolean auto = true;

	private com.sunflow.math3d.Cube model2;

	public static void main(String[] args) { new ExampleGame3D(); }

	@Override
	public void setup() {
		title("ExampleGame 3D");
		createCanvas(1280, 720);
		frameRate(600);
		showInfo(true);

//		model = new Sphere(this, 0, 0, 1500, 750, 30);
//		model = new Cube(this, 0, 0, 1500, 750, 750, 750);
//		model = new Cube(this, 0, 0, 0, 20, 20, 20);
//		Models.add(model);
		model2 = new com.sunflow.math3d.Cube(this, 0, 0, 0, 2, 2, 2, Color.red);
//		Models.add(model2);

//		for (int i = 0; i < 5; i++) {
//			int x = random.nextInt(4000) - 2000;
//			int y = random.nextInt(4000) - 2000;
//			int z = random.nextInt(3000) + 1000;
//			Base3DModel cube = new Cube(this, x, y, z, 500, 500, 500);
//			Models.add(cube);
//		}
	}

	double showDelta, showMult;

	@Override
	public List<String> getInfo() {
		List<String> info = super.getInfo();

		info.add(showDelta + " ∆");
		info.add(showMult + " x");

		return info;
	}

	@Override
	protected void update() {
		// rotate and update shape examples
		if (!auto) return;
		for (int i = 1; i < Models.size(); i++) {
			Base3DModel m = Models.get(i);
			m.rotateX((float) (0.5 * delta));
		}
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

		pushMatrix();
//		graphics.translate(width / 2, height / 2);

//		for (int i = 0; i < drawOrder.length; i++) {
//			DPolygon current = DPolygone.get(drawOrder[i]);
//
//			int scale = getFillScale(current);
//			Color fill = new Color(scale, scale, scale);
//			current.render(graphics, true, fill, outlines, new Color(50, 50, 50));
////			pol.render(privateG, true, outlines);
//		}

		// Draw Models in the Order that is set by the 'setOrder' function
//		for (int i = 0; i < drawOrder.length; i++) DPolygone.get(drawOrder[i]).render(privateG, true, outlines);
		model2.updatePolygon();
		model2.draw(graphics);
		graphics.setColor(Color.black);
		graphics.drawRect(600, 600, 60, 60);

//		info(vCameraPos);
//		info(vCameraDir);
		info(vertLook);

		ellipse(width / 2, height / 2, 10, 10);
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
