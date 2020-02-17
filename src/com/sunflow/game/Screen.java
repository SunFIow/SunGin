package com.sunflow.game;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.JPanel;

@Deprecated
public class Screen {

	private Game2D game;

	public Component component;
//	protected Canvas canvas;
//	protected JLabel label;

	private BufferStrategy bs;

	private boolean useCanvas;

	public Screen(Game2D g) {
		game = g;
		if (useCanvas) component = new Canvas();
		else component = new JPanel() {
			private static final long serialVersionUID = 6266621545803835044L;

			@Override
			protected void paintComponent(Graphics g) {
				game.privateDraw();
				game.draw();
				game.render(game.graphics);

				if (game.showOverlay) game.drawOverlay();

				super.paintComponent(g);
				g.drawImage(game.image, 0, 0, null);
				game.frames++;
				game.frameCount++;
			}
		};
	}

	void render() {
		if (!useCanvas) {
			component.repaint();
			return;
		}

		game.privateDraw();
		game.draw();
		game.render(game.graphics);

		if (game.showOverlay) game.drawOverlay();
		do {
			do {
				Graphics g = bs.getDrawGraphics();
				g.drawImage(game.image, 0, 0, null);
				g.dispose();
			} while (bs.contentsRestored());
			bs.show();
		} while (bs.contentsLost());
		game.frames++;
		game.frameCount++;
	}

	public void addToFrame(JFrame frame) {
		frame.add(component);
		if (component instanceof Canvas) {
			Canvas canvas = (Canvas) component;
			canvas.createBufferStrategy(3);
			bs = canvas.getBufferStrategy();
		}
	}

	public void setFocusable(boolean b) { component.setFocusable(b); }

	public void setPreferredSize(Dimension dimension) { component.setPreferredSize(dimension); }

	public void requestFocus() { component.requestFocus(); }

	public FontMetrics getFontMetrics(Font font) { return component.getFontMetrics(font); }

	public void addKeyListener(KeyListener listener) { component.addKeyListener(listener); }

	public void addMouseListener(MouseListener listener) { component.addMouseListener(listener); }

	public void addMouseMotionListener(MouseMotionListener listener) { component.addMouseMotionListener(listener); }

	public void addMouseWheelListener(MouseWheelListener listener) { component.addMouseWheelListener(listener); }

	public void addComponentListener(ComponentListener listener) { component.addComponentListener(listener); }

	public void setCursor(Cursor cursor) { component.setCursor(cursor); }

}
