package com.sunflow.game.olc;

import com.sunflow.gfx.SGraphics;
import com.sunflow.gfx.SImage;
import com.sunflow.util.GameUtils;
import com.sunflow.util.SConstants;

public class Sprite implements GameUtils, SConstants {
	public SImage img;

	public Sprite() {}

	public Sprite(SImage img) {
		this.img = img;
		this.img.loadPixels();
	}

	public Sprite(String string) {
		this(GameUtils.instance.loadSImage(string));
	}

	public Sprite(Sprite spr) {
		this(spr.img.copy());
	}

	public Sprite(Sprite origin, float x, float y, float width, float height) {
//		this(new SImage(origin.img.image.getSubimage((int) x, (int) y, (int) width, (int) height)));
		this(origin.img.get((int) x, (int) y, (int) width, (int) height));
	}

	public int SampleColor(float tex_u, float tex_v) {
		int x = (tex_u >= 1) ? (img.width - 1) : (int) (tex_u * img.width);
		int y = (tex_v >= 1) ? (img.height - 1) : (int) (tex_v * img.height);
		int index = img.index(x, y);
//		Log.debug(img.width, img.height, tex_u, tex_v, x, y, index);
//		Log.debug(img.pixels.length);
		int color = img.pixels[index];
		return color;
	}

	public void flipX() {
		SGraphics gr = createGraphics(img.width, img.height);
		gr.image(img, img.width, 0, -img.width, img.height);
//		img.image = gr.image;
		img.set(0, 0, gr);
		img.loadPixels();
	}

	public void flipY() {
		SGraphics gr = createGraphics(img.width, img.height);
		gr.image(img, 0, img.height, img.width, -img.height);
//		img.image = gr.image;
		img.set(0, 0, gr);
		img.loadPixels();
	}

	public void flip() {
		SGraphics gr = createGraphics(img.width, img.height);
		gr.image(img, img.width, img.height, -img.width, -img.height);
//		img.image = gr.image;
		img.set(0, 0, gr);
		img.loadPixels();
	}
}