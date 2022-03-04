package com.sunflow.examples;

import java.awt.event.MouseEvent;
import java.util.List;

import com.sunflow.Settings;
import com.sunflow.Settings.ScreenType;
import com.sunflow.game.GameBase;

public class ExampleGame2D extends GameBase {
	public static void main(String[] args) {
		GameBase.settings = new Settings()
				.screentype(ScreenType.JAVA) //
		//		.screentype(ScreenType.OPENGL) // 
		;
		ExampleGame2D game = new ExampleGame2D();
		game.start();
	}

	private float ballR;
	private float ballX;
	private float ballY;
	private float ballVX;
	private float ballVY;

	@Override
	public void setup() {
		title("ExampleGame 2D");
		createCanvas(1000, 500);
		//		smooth();
		frameRate(0);
		//		showInfo(true);
		ballR = 25;
		ballX = width / 2;
		ballY = height / 2;
		ballVX = 400;
		ballVY = 200;
	}

	@Override
	public List<String> getInfo() {
		//		System.out.println(fElapsedTime);
		List<String> list = super.getInfo();

		list.add(0, "Position [" + ballX + "|[" + ballY + "]");
		list.add("Velocity [" + ballVX + "|[" + ballVY + "]");

		return list;
	}

	@Override
	public void mouseClicked(MouseEvent event) { isPaused = !isPaused; }

	@Override
	public void update() {
		ballX += ballVX * fElapsedTime;
		ballY += ballVY * fElapsedTime;
		if (ballX < ballR || ballX > width - ballR) {
			ballVX *= -1;
			ballX = ballX < ballR ? ballR : width - ballR;
		}
		if (ballY < ballR || ballY > height - ballR) {
			ballVY *= -1;
			ballY = ballY < ballR ? ballR : height - ballR;
		}
	}

	@Override
	public void draw() {
		background(25);
		fill(255);
		ellipse(ballX, ballY, ballR * 2, ballR * 2);
	}
}
