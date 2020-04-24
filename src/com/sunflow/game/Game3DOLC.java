package com.sunflow.game;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import com.sunflow.game.olc.Mesh;
import com.sunflow.game.olc.PipeLineRenderer;
import com.sunflow.gfx.GraphicsMatrix;
import com.sunflow.math.SVector;
import com.sunflow.math3d.SMatrix;
import com.sunflow.util.MathUtils;

public class Game3DOLC extends Game2D {

	protected static float fFov = 90f;
	protected static float fNear = 0.1f;
	protected static float fFar = 1000.0f;

	protected SVector vCameraPos;
	protected SVector vCameraDir;
	protected SVector vUp;

	protected SVector light_pos;
	protected SVector light_dir;

	protected float fYaw, fPitch, fYawSpeed, fPitchSpeed;

//	protected SVector vLightDir;
	protected float sunPos;

	protected boolean isCameraActivated;

	// The smaller the zoom the more zoomed out you are and visa versa, although altering too far from 1000 will make it look pretty weird
	protected float zoom, minZoom, maxZoom;

	protected boolean[] keys;

	private float mouseDifX, mouseDifY;

	protected float movementSpeed;

	protected GraphicsMatrix gMatrix;

	protected PipeLineRenderer renderer;

	@Override
	final void privatePreSetup() {
		super.privatePreSetup();

		isCameraActivated = true;
//		vCameraPos = new SVector(197.0f, 198.0f, -980f);
		vCameraPos = new SVector(0.0f, 0.0f, -4.0f);
		vCameraDir = new SVector(0.0f, 0.0f, 1.0f);
		vUp = new SVector(0.0f, 1.0f, 0.0f);

		light_dir = new SVector(1.0f, 1.0f, 1.0f);

		zoom = 1000;
		minZoom = 100;
		maxZoom = 10000;

		keys = new boolean[6];

		movementSpeed = 2f;

		fYawSpeed = 0.0025f;
		fPitchSpeed = 0.0025f;

		sunPos = PI;

		showCrosshair = true;

		gMatrix = new GraphicsMatrix();

//		shapes = new ArrayList<>();
		updateView();
	}

	@Override
	final void createFrame() {
		super.createFrame();
		canvas.addKeyListener(new Game3DKeyListeners());
		Game3DMouseListeners ml = new Game3DMouseListeners();
		canvas.addMouseListener(ml);
		canvas.addMouseWheelListener(ml);
		if (isCameraActivated) invisibleMouse();
	}

	@Override
	public void defaultSettings() {
		super.defaultSettings();

		renderer = new PipeLineRenderer(this);
		renderer.ConfigureDisplay();
		renderer.SetProjection(fFov, height / width, fNear, fFar, 0.0f, 0.0f, width, height);
		renderer.SetTransform(SMatrix.Matrix_MakeIdentity());
		renderer.SetCamera(new SVector(), new SVector(), new SVector());
		SVector light_pos = new SVector(0.0f, 0.0f, 0.0f);
		SVector light_direction = new SVector(0.0f, 1.0f, -1.0f);
		int light_color = 0xffffffff;
		renderer.SetLightSource(light_pos, light_direction, light_color);
	}

	@Override
	final void privateDraw() {
		super.privateDraw();

		cameraMovement();

		controlSunAndLight();
	}

	public final void point(float x, float y, float z) {
		boolean draw = true;

		SVector applied = apply(x, y, z);
		SVector pos2d = convert3Dto2D(applied);
		if (pos2d == null) return;
		float x2d = pos2d.x;
		float y2d = pos2d.y;
		if (pos2d.z < 0) draw = false;

		if (draw) point(x2d, y2d);
	}

	public final void line(float x1, float y1, float z1, float x2, float y2, float z2) {
		boolean draw = true;

		SVector applied = apply(x1, y1, z1);
		SVector pos2d = convert3Dto2D(applied);
		if (pos2d == null) return;
		float x2d1 = pos2d.x;
		float y2d1 = pos2d.y;
		if (pos2d.z < 0) draw = false;

		applied = apply(x2, y2, z2);
		pos2d = convert3Dto2D(applied);
		if (pos2d == null) return;
		float x2d2 = pos2d.x;
		float y2d2 = pos2d.y;
		if (pos2d.z < 0) draw = false;

		if (draw) line(x2d1, y2d1, x2d2, y2d2);
	}

	public final void box(float l) { box(0, 0, 0, l); }

	public final void box(float x, float y, float z, float l) {
		if (!stroke && !fill) return;

		SMatrix matWorld = SMatrix.Matrix_MakeTranslation(x, y, z);
		renderer.SetTransform(matWorld);

		if (fill) {
			Mesh cube = Mesh.Cube();
			cube.color(fillColor);
			renderer.Render(cube.tris, RENDER_FLAT | RENDER_CULL_CW | RENDER_DEPTH | RENDER_LIGHTING_SUNLIGHT);
		}
	}

	public final void vertex(float x, float y, float z) {
		SVector applied = apply(x, y, z);
		SVector pos2d = convert3Dto2D(applied);
		if (pos2d == null) return;
		float x2d = pos2d.x;
		float y2d = pos2d.y;
		super.vertex(x2d, y2d);
	}

	@Override
	public final void vertex(float x, float y) { vertex(x, y, 0); }

	/**
	 * @param pos
	 *            3d position
	 * @return 2d screen position if onscreen, else returns null
	 */
	public final SVector convert3Dto2D(SVector pos) {
//		return convert3Dto2D(pos.x, pos.y, pos.z); 
		SVector v2d = renderer.convert3Dto2D(pos);
		return v2d;
	}

	public final SVector convert3Dto2D(float x, float y, float z) {
		return convert3Dto2D(new SVector(x, y, z));
	}

	private final void controlSunAndLight() {
		sunPos += 0.0025f;
//		sunPos = PI * 1.5f;
		light_dir.x = MathUtils.instance.cos(sunPos);
		light_dir.y = MathUtils.instance.sin(sunPos);
		light_dir.z = 0.0f;
	}

	// Forward + Sideways = faster
	private final void cameraMovement() {
		if (!isCameraActivated) return;
		float fSpeedM = 1.0f;
		if (keyIsDown(SHIFT)) fSpeedM *= 2;
		if (keyIsDown(ALT)) fSpeedM /= 4;

		SVector vForward = new SVector(vCameraDir);
		vForward.y = 0;
		vForward.normalize().mult(fSpeedM);

		SVector vRight = SVector.cross(vUp, vCameraDir);
		vRight.normalize().mult(fSpeedM);

		SVector moveVector = new SVector();

		if (keys[0]) moveVector.add(vForward);
		if (keys[2]) moveVector.sub(vForward);

		if (keys[1]) moveVector.add(vRight);
		if (keys[3]) moveVector.sub(vRight);

		if (keys[4]) moveVector.y += fSpeedM;
		if (keys[5]) moveVector.y -= fSpeedM;

		moveVector.mult(movementSpeed * fElapsedTime);
		vCameraPos.add(moveVector);

		updateView();
	}

	private final void mouseMovement(float NewMouseX, float NewMouseY) {
		mouseDifX = (NewMouseX - width / 2);
		mouseDifY = (NewMouseY - height / 2);

		fYaw += mouseDifX * fYawSpeed;
		fPitch -= mouseDifY * fPitchSpeed;

		fPitch = clamp(-HALF_PI * 0.99999f, fPitch, HALF_PI * 0.99999f);
		fYaw = fYaw % TWO_PI;

		updateView();
	}

	public final void updateView() {
		if (renderer == null) return;
		SVector vTarget = new SVector(0.0f, 0.0f, 1.0f);
		SMatrix matCameraRotX = SMatrix.Matrix_MakeRotationX(fPitch);
		SMatrix matCameraRotY = SMatrix.Matrix_MakeRotationY(fYaw);
		SMatrix matCameraRot = SMatrix.Matrix_MakeIdentity();
		matCameraRot = SMatrix.Matrix_MultiplyMatrix(matCameraRot, matCameraRotX);
		matCameraRot = SMatrix.Matrix_MultiplyMatrix(matCameraRot, matCameraRotY);
		vCameraDir = SMatrix.Matrix_MultiplyVector(matCameraRot, vTarget);
		vTarget = SVector.add(vCameraPos, vCameraDir);

//		renderer.SetCamera(vCameraPos, vTarget, SVector.neg(vUp));
		renderer.SetCamera(vCameraPos, vTarget, vUp);
	}

	private final void centerMouse() { moveMouseTo(width() / 2, height() / 2); }

	public final void invisibleMouse() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		BufferedImage cursorImage = new BufferedImage(1, 1, Transparency.TRANSLUCENT);
		Cursor invisibleCursor = toolkit.createCustomCursor(cursorImage, new Point(0, 0), "InvisibleCursor");
		canvas.setCursor(invisibleCursor);
	}

	public final void visibleMouse() { canvas.setCursor(Cursor.getDefaultCursor()); }

	public final List<String> getInfo3D(List<String> info) {
		float x = vCameraPos.x;
		float y = vCameraPos.y;
		float z = vCameraPos.z;
		info.add("[" + (int) x + "]" + "[" + (int) y + "]" + "[" + (int) z + "]");
		x = vCameraDir.x - x;
		y = vCameraDir.y - y;
		z = vCameraDir.z - z;
		info.add("[" + x + "]" + "[" + y + "]" + "[" + z + "]");

		x = fPitch;
		y = 0;
		z = fYaw;
		info.add("[" + x + "]" + "[" + y + "]" + "[" + z + "]");
		return info;
	}

	public final float zoom() { return zoom; }

	public final void translate(float x, float y, float z) { gMatrix.translate(x, y, z); }

	public final void translateTo(float x, float y, float z) { gMatrix.translateTo(x, y, z); }

	public final void scale(float n) { scale(n, n, n); };

	public final void scale(float x, float y, float z) { gMatrix.scale(x, y, z); };

	public final void rotateX(float angle) { gMatrix.rotateX(angle); }

	public final void rotateY(float angle) { gMatrix.rotateY(angle); }

	public final void rotateZ(float angle) { gMatrix.rotateZ(angle); }

	public final void rotateXTo(float angle) { gMatrix.rotateXTo(angle); }

	public final void rotateYTo(float angle) { gMatrix.rotateYTo(angle); }

	public final void rotateZTo(float angle) { gMatrix.rotateZTo(angle); }

	public final SVector apply(float x, float y, float z) { return gMatrix.apply(x, y, z); }

	public final SVector apply(SVector pos) { return gMatrix.apply(pos); }

	public final SVector translated(float x, float y, float z) { return gMatrix.translated(x, y, z); }

	public final SVector translated(SVector pos) { return gMatrix.translated(pos); }

	public final SVector scaled(float x, float y, float z) { return gMatrix.scaled(x, y, z); }

	public final SVector scaled(SVector pos) { return gMatrix.scaled(pos); }

	public final SVector rotated(float x, float y, float z) { return gMatrix.rotated(x, y, z); }

	public final SVector rotated(SVector pos) { return gMatrix.rotated(pos); }

	public final SMatrix getRotationMatrixX() { return gMatrix.getRotationMatrixX(); }

	public final SMatrix getRotationMatrixY() { return gMatrix.getRotationMatrixY(); }

	public final SMatrix getRotationMatrixZ() { return gMatrix.getRotationMatrixZ(); }

	public final SMatrix[] getRotationMatrix() { return gMatrix.getRotationMatrix(); }

	public final float getRotationX() { return gMatrix.getRotationX(); }

	public final float getRotationY() { return gMatrix.getRotationY(); }

	public final float getRotationZ() { return gMatrix.getRotationZ(); }

	public final float[] getRotation() { return gMatrix.getRotation(); }

	public final void rotation(float[] rotation) { gMatrix.rotation(rotation); }

	@Override
	public final void pushMatrix() {
		super.pushMatrix();
		gMatrix.pushMatrix();
	}

	@Override
	public final void popMatrix() {
		super.popMatrix();
		gMatrix.popMatrix();
	}

	@Override
	public final void resetMatrix() {
		super.resetMatrix();
		gMatrix.resetMatrix();
	}

	private final class Game3DKeyListeners extends KeyAdapter {
		@Override
		public final void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_W) keys[0] = true;
			if (e.getKeyCode() == KeyEvent.VK_A) keys[1] = true;
			if (e.getKeyCode() == KeyEvent.VK_S) keys[2] = true;
			if (e.getKeyCode() == KeyEvent.VK_D) keys[3] = true;
			if (e.getKeyCode() == KeyEvent.VK_SPACE) keys[4] = true;
			if (e.getKeyCode() == KeyEvent.VK_CONTROL) keys[5] = true;
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);
		}

		@Override
		public final void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_W) keys[0] = false;
			if (e.getKeyCode() == KeyEvent.VK_A) keys[1] = false;
			if (e.getKeyCode() == KeyEvent.VK_S) keys[2] = false;
			if (e.getKeyCode() == KeyEvent.VK_D) keys[3] = false;
			if (e.getKeyCode() == KeyEvent.VK_SPACE) keys[4] = false;
			if (e.getKeyCode() == KeyEvent.VK_CONTROL) keys[5] = false;
		}
	}

	@Override
	final void updateMousePosition(int x, int y) {
		if (!canvas.hasFocus()) return;
		if (!isCameraActivated) {
			super.updateMousePosition(x, y);
			return;
		}
		mouseMovement(x, y);
		super.updateMousePosition(x, y);
		centerMouse();
	}

	private final class Game3DMouseListeners extends MouseAdapter {
		@Override
		public final void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getUnitsToScroll() > 0) {
				if (zoom > minZoom) zoom -= 25 * e.getUnitsToScroll();
			} else if (zoom < maxZoom) zoom += 25 * -e.getUnitsToScroll();
		}

		@Override
		public void mouseEntered(MouseEvent e) { centerMouse(); }
	}
}
