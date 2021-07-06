package com.sunflow.rt;

import com.sunflow.game.Game3D;
import com.sunflow.game.GameBase;
import com.sunflow.math.SVector;
import com.sunflow.util.MathUtils;
import com.sunflow.util.SConstants;

public abstract class GameObject {
	public abstract Ray cast(SVector p);

	public abstract void draw(GameBase game);

	public static RayResult cast(SVector pos, GameObject[] objects, float hitRange) {
		RayResult result = new RayResult(hitRange);
		for (GameObject object : objects) {
			Ray hit = object.cast(pos);
			result.update(hit);
		}
		return result;
	}

	public static class RayResult {
		float hitRange;
//		float closestDist;
//		SVector direction;
		Ray ray = Ray.DEFAULT;

		public RayResult(float hitRange) {
			this.hitRange = hitRange;
//			this.closestDist = Float.MAX_VALUE;
		}

		public void update(Ray _ray) {
			if (_ray.distance < ray.distance) {
				ray = _ray;
			}
		}

		public float closestDistance() {
			return ray.distance;
		}

		public boolean isIntersecting() {
			return ray.distance < hitRange;
		}

		public SVector getIntersection() {
			return ray.origin.added(ray.direction);
		}
	}

	public static class Ray {
//		public static final Ray DEFAULT = new Ray(Float.MAX_VALUE);
		public static final Ray DEFAULT = new Ray(Float.MAX_VALUE);

		private Ray(float distance) { this.distance = distance; }

		SVector origin;
		SVector direction;
		float distance;

		public Ray(SVector origin, SVector direction) { this(origin, direction, direction.length()); }

		public Ray(SVector origin, SVector direction, float distance) { this.origin = origin; this.direction = direction; this.distance = distance; }

	}

	public static class Rectangle extends GameObject {
		SVector pos;
		SVector size;

		public Rectangle(SVector pos, SVector size) {
			this.pos = pos;
			this.size = size;
		}

		@Override
		public Ray cast(SVector p) {
			return new Ray(p, new SVector(
					MathUtils.instance.max(MathUtils.instance.max(this.pos.x - p.x, p.x - (this.pos.x + this.size.x)), 0),
					MathUtils.instance.max(MathUtils.instance.max(this.pos.y - p.y, p.y - (this.pos.y + this.size.y)), 0)));
		}

		@Override
		public void draw(GameBase game) { game.rect(pos.x, pos.y, size.x, size.y); }

	}

	public static class Circle extends GameObject {
		SVector pos;
		float radius;

		public Circle(SVector pos, float radius) {
			this.pos = pos;
			this.radius = radius;
		}

		@Override
		public Ray cast(SVector p) {
			SVector p_pos = pos.subtracted(p);
			float p_pos_length = p_pos.length();
			float length = p_pos_length - radius;
			float scalar = length / p_pos_length;

			return new Ray(p, new SVector(p_pos.x * scalar, p_pos.y * scalar), length);
		}

		@Override
		public void draw(GameBase game) { game.ellipse(pos.x, pos.y, radius); }

	}

	public static class Rectangle_Circle extends GameObject {
		Rectangle rect;
		Circle circle;

		public Rectangle_Circle(SVector pos, float radius, SVector pos2, SVector size) {
			rect = new Rectangle(pos2, size);
			circle = new Circle(pos, radius);
		}

		@Override
		public Ray cast(SVector p) {
			Ray rectR = rect.cast(p);
			Ray circleR = circle.cast(p);
//			Ray result = new Ray(p, rectR.direction.sub(circleR.direction), rectR.distance - circleR.distance);
			Ray result = new Ray(p, rectR.direction.sub(circleR.direction));
			return result;
		}

		@Override
		public void draw(GameBase game) {
			rect.draw(game);
			circle.draw(game);
		}

	}

	public static class Box extends GameObject {
		SVector pos;
		SVector size;

		public Box(SVector pos, SVector size) {
			this.pos = pos;
			this.size = size;
		}

		@Override
		public Ray cast(SVector p) {
			return new Ray(p, new SVector(
					MathUtils.instance.max(MathUtils.instance.max(this.pos.x - p.x, p.x - (this.pos.x + this.size.x)), 0),
					MathUtils.instance.max(MathUtils.instance.max(this.pos.y - p.y, p.y - (this.pos.y + this.size.y)), 0),
					MathUtils.instance.max(MathUtils.instance.max(this.pos.z - p.z, p.z - (this.pos.z + this.size.z)), 0)));
		}

		@Override
		public void draw(GameBase game) { box(game, pos.x, pos.y, pos.z, size.x, size.y, size.z); }

		public final void box(GameBase game, float x, float y, float z, float lx, float ly, float lz) {
			float l = lx;

			game.beginShape(SConstants.QUADS);

			// Bottom
			game.vertex(x, y, z);
			game.vertex(x, y + l, z);
			game.vertex(x + l, y + l, z);
			game.vertex(x + l, y, z);

			// Top
			game.vertex(x, y, z + l);
			game.vertex(x, y + l, z + l);
			game.vertex(x + l, y + l, z + l);
			game.vertex(x + l, y, z + l);

			// Left
			game.vertex(x, y, z);
			game.vertex(x + l, y, z);
			game.vertex(x + l, y, z + l);
			game.vertex(x, y, z + l);

			// Right
			game.vertex(x, y + l, z);
			game.vertex(x + l, y + l, z);
			game.vertex(x + l, y + l, z + l);
			game.vertex(x, y + l, z + l);

			// Back
			game.vertex(x + l, y, z);
			game.vertex(x + l, y, z + l);
			game.vertex(x + l, y + l, z + l);
			game.vertex(x + l, y + l, z);

			// Front
			game.vertex(x, y, z);
			game.vertex(x, y, z + l);
			game.vertex(x, y + l, z + l);
			game.vertex(x, y + l, z);

			game.endShape(SConstants.CLOSE);
		}

	}

	public static class Sphere extends GameObject {
		SVector pos;
		float radius;

		public Sphere(SVector pos, float radius) {
			this.pos = pos;
			this.radius = radius;
		}

		@Override
		public Ray cast(SVector p) {
			SVector p_pos = pos.subtracted(p);
			float p_pos_length = p_pos.length();
			float length = p_pos_length - radius;
			float scalar = length / p_pos_length;

			return new Ray(p, new SVector(p_pos.x * scalar, p_pos.y * scalar, p_pos.z * scalar), length);
		}

		@Override
		public void draw(GameBase game) { sphere(game, pos.x, pos.y, pos.z, radius); }

		public final void sphere(GameBase game, float x, float y, float z, float r) {
			int rez = 10;
			float inc = SConstants.TWO_PI / rez;
			for (int i = 0; i < rez; i++) {
				for (int j = 0; j < rez; j++) {
					SVector p = SVector.fromAngle2(i * inc, j * inc, null).mult(r).add(x, y, z);
					((Game3D) game).point(p.x, p.y, p.z);
				}
			}
		}

	}

}
