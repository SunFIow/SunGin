package com.sunflow.engine;

import java.awt.Cursor;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import com.sunflow.engine.input.Input;

public class WindowLWJGL {

	private long windowID;
	private int width, height;
	private int posX, posY;
	private String title;

	private long frameCount;

	private GLFWErrorCallback errorCallback;

	private boolean isFocused;
	private boolean isFullscreen;

	public void createWindow(int width, int height, String title) {
		this.width = width;
		this.height = height;
		this.title = title;

		if (!GLFW.glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

		// Setup an error callback. It will print the error message in System.err.
		GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

		// Configure GLFW
		GLFW.glfwDefaultWindowHints(); // optional, the current window hints are already the default

		// Create the window
		windowID = GLFW.glfwCreateWindow(width, height, title, 0, 0);
		if (windowID == MemoryUtil.NULL) throw new RuntimeException("Failed to create the GLFW window");

		// Make the OpenGL context current
		GLFW.glfwMakeContextCurrent(windowID);

		Input.setWindowID(windowID);

		setupCallbacks();

		// Enable v-sync
		GLFW.glfwSwapInterval(0);

		GL.createCapabilities();

		GL11.glClearColor(1, 0, 1, 1);

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		// Depth
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	private void setupCallbacks() {}

	public void prepare() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}

	public void render() {
		frameCount++;
		GLFW.glfwSwapBuffers(windowID);
	}

	public void destroy() {
		// Free the window callbacks and destroy the window
//		Callbacks.glfwFreeCallbacks(windowID);
		GLFW.glfwDestroyWindow(windowID);
		// Terminate GLFW and free the error callback
		GLFW.glfwTerminate();
		GLFW.glfwSetErrorCallback(null).free();
	}

	public boolean isCloseRequested() {
		return GLFW.glfwWindowShouldClose(windowID);
	}

	public int getWidth() { return height; }

	public int getHeight() { return width; }

	public String getTitle() { return title; }

	public long getID() { return windowID; }

	public void setVisible(boolean visible) {
		if (visible) GLFW.glfwShowWindow(windowID);
		else GLFW.glfwHideWindow(windowID);
	}

	public void requestFocus() {
		GLFW.glfwFocusWindow(windowID);
	}

	public boolean hasFocus() {
		return GLFW.glfwGetWindowAttrib(windowID, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
	}

	public void setTitle(String title) {
		this.title = title;
		GLFW.glfwSetWindowTitle(windowID, title);
	}

	public void setCursor(Cursor cursor) {
		if (cursor.getName() == "InvisibleCursor") {
			GLFW.glfwSetCursor(windowID, GLFW.GLFW_CURSOR_HIDDEN);
		}
	}

	public void toggleFullscreen() { setFullscreen(!isFullscreen); }

	public void setFullscreen(boolean b) {
		if (isFullscreen == b) return;
		isFullscreen = b;
		if (isFullscreen) {
			int[] windowPosX = new int[1], windowPosY = new int[1];
			GLFW.glfwGetWindowPos(windowID, windowPosX, windowPosY);
			posX = windowPosX[0];
			posY = windowPosY[0];
			GLFW.glfwSetWindowMonitor(windowID, GLFW.glfwGetPrimaryMonitor(), 0, 0, width, height, 0);
		} else {
			GLFW.glfwSetWindowMonitor(windowID, 0, posX, posY, width, height, 0);
		}
	}

	public int getX() {
		int[] windowPosX = new int[1], windowPosY = new int[1];
		GLFW.glfwGetWindowPos(windowID, windowPosX, windowPosY);
		return windowPosX[0];
	}

	public int getY() {
		int[] windowPosX = new int[1], windowPosY = new int[1];
		GLFW.glfwGetWindowPos(windowID, windowPosX, windowPosY);
		return windowPosY[0];
	}

}
