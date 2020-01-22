package com.sunflow.examples;

import java.awt.event.MouseEvent;

import com.sunflow.game.Game2D;

public class ExampleGame2D extends Game2D {
	private float ballR;
	private float ballX;
	private float ballY;
	private float ballVX;
	private float ballVY;

	public static void main(String[] args) {
		new ExampleGame2D();
	}

	@Override
	public void setup() {
		createCanvas(1000, 500);
		smooth();
		frameRate(60);
		ballR = 25;
		ballX = width / 2;
		ballY = height / 2;
		ballVX = 4;
		ballVY = 2;
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		if (!running) {
			start();
		} else {
			stop();
		}
	}

	@Override
	public void update() {
		ballX += ballVX * multiplier;
		ballY += ballVY * multiplier;
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
	protected void draw() {
		background(25);
		fill(255);
		ellipse(ballX, ballY, ballR * 2, ballR * 2);
	}
}
