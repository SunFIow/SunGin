package com.sunflow.examples;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import com.sunflow.game.Game3D;
import com.sunflow.math.SVector;
import com.sunflow.math3d.models.Base3DModel;
import com.sunflow.math3d.models.BaseModel;
import com.sunflow.math3d.models.Cube;
import com.sunflow.math3d.models.DPoint;
import com.sunflow.math3d.models.DPolygon;
import com.sunflow.math3d.models.GenerateTerrain;
import com.sunflow.math3d.models.Pyramid;
import com.sunflow.math3d.models.Sphere;

public class ExampleGame3D extends Game3D {
	public static void main(String[] args) { new ExampleGame3D().start(); }

	private Base3DModel model;

	private boolean auto = true;

	DPoint point;

	@Override
	public void setup() {
		title("ExampleGame 3D");
//		undecorated(true);
		isCameraActivated = true;
//		createCanvas(1280, 720);
		createCanvas(800, 600, 1, 1);
		syncMode(ASYNC);
		frameRate(144);
		tickRate(60);
		showInfo(true);
		highlight(true);
		noSmooth();

		vCameraPos = new SVector(0, 0, 50);

		Models.add(new Pyramid(this, 0, -5, 0, 2, 2, 2, Color.green));

		Models.add(new Cube(this, 0, 0, -5, 2, 2, 2));

		Models.add(new Cube(this, 18, -5, 0, 2, 2, 2, Color.red));
		Models.add(new Cube(this, 20, -5, 0, 2, 2, 2, Color.red));
		Models.add(new Cube(this, 22, -5, 0, 2, 2, 2, Color.red));
		Models.add(new Cube(this, 20, -5, 2, 2, 2, 2, Color.red));

		new GenerateTerrain(this, Models, 20, 3);

		model = new Sphere(this, 50, 0, 15, 10, 20);
		Models.add(model);

		horLook = 0;
		vertLook = -0.9999999f;

		DPoint[] Dpoints = new DPoint[8];
		Dpoints[0] = new DPoint(this, -10f, -10f, -10f);
		Dpoints[1] = new DPoint(this, 10f, -10f, -10f);
		Dpoints[2] = new DPoint(this, 10f, 10f, -10f);
		Dpoints[3] = new DPoint(this, -10f, 10f, -10f);
		Dpoints[4] = new DPoint(this, -10f, -10f, 10f);
		Dpoints[5] = new DPoint(this, 10f, -10f, 10f);
		Dpoints[6] = new DPoint(this, 10f, 10f, 10f);
		Dpoints[7] = new DPoint(this, -10f, 10f, 10f);
		Models.addAll(Arrays.asList(Dpoints));

//		point = new DPoint(this, 0, 0, 0);
//		Models.add(point);
	}

	double showElapsedTime, showMult;

	@Override
	public List<String> getInfo() {
		List<String> info = super.getInfo();

		info.add(showElapsedTime + " ∆");
		info.add(showMult + " x");

		float x = vCameraPos.x;
		float y = vCameraPos.y;
		float z = vCameraPos.z;
		info.add("[" + (int) x + "]" + "[" + (int) y + "]" + "[" + (int) z + "]");
		x = vCameraDir.x - x;
		y = vCameraDir.y - y;
		z = vCameraDir.z - z;
		info.add("[" + x + "]" + "[" + y + "]" + "[" + z + "]");

		x = horLook;
		y = 0;
		z = vertLook;
		info.add("[" + x + "]" + "[" + y + "]" + "[" + z + "]");
		return info;
	}

	@Override
	public void update() {
		// rotate and update shape examples
		if (!auto) return;
		Models.get(0).rotateX(2f * tElapsedTime);
		Models.get(1).rotateX(0.5f * tElapsedTime);
		Models.get(1).rotateY(0.2f * tElapsedTime);
		Models.get(1).rotateZ(0.1f * tElapsedTime);
	}

	float sx = 0;

	@Override
	public void draw() {
//		translate(sx, 0, 0);
		sx += 0.1;

		if (frameCount % frameRate == 0) {
			showElapsedTime = tElapsedTime;
			showMult = tMultiplier;
		}

		background(168, 211, 255);

		// Draw Models in the Order that is set by the 'setOrder' function
		for (int i = 0; i < drawOrder.length; i++) {
			BaseModel current = DModels.get(drawOrder[i]);
			if (current instanceof DPolygon) ((DPolygon) current).renderStroke(outlines);
			current.render();
		}

//		point.render();

		stroke(255, 0, 0);
		strokeWeight(10);
		point(0, 0, 0);
//		line(1, 2, 2, 2, 1, 2);
//		rotateZTo(frameCount / 100f);

//		beginShape();
//		vertex(0, 0, 0);
//		vertex(50, 50);
//		vertex(70, 30);
//		vertex(80, 60);
//		vertex(100, 120);
//		vertex(60, 110);
//		vertex(70, 220);
//		vertex(200, 150);
//		vertex(100, 100);
//		endShape(CLOSE);
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
