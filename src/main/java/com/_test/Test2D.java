package com._test;

import com.sunflow.game.GameBase;

public class Test2D extends GameBase {
	public static void main(String[] args) {
		new Test2D();
	}

	private static final int rWidth = 256;
	private static final int rHeight = 256;

	@Override
	public void setup() {
		createCanvas(1000, 1000);

	}

	@Override
	public void draw() {
		float dx = (float) width / rWidth;
		float dy = (float) height / rHeight;
		for (int x = 0; x < rWidth; x++) {
			for (int y = 0; y < rHeight; y++) {
				noStroke();
//				imageStore(readWriteImage, ivec2(id.xy), vec4(id.x & id.y, x / 15.0, y / 15.0, 1.0));
				fill(x & y, (x & 15) / 15.0f, (y & 15) / 15);
				rect(x * dx, y * dy, dx, dy);
			}
		}

	}
}
