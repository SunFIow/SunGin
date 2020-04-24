package com.sunflow.game.olc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sunflow.logging.Log;
import com.sunflow.math.SVector;
import com.sunflow.util.GameUtils;

public class Mesh {
	public List<Triangle> tris = new ArrayList<>();

	public void color(int color) { tris.forEach((t) -> t.color = color); }

	public void AddFromObjectFile(String sFileName) { AddFromObjectFile(sFileName, false); }

	public void AddFromObjectFile(String sFileName, boolean hasTexture) {
		Mesh obj = LoadFromObjectFile(sFileName, hasTexture);
		tris.addAll(obj.tris);
	}

	static public Mesh LoadFromObjectFile(String sFileName) { return LoadFromObjectFile(sFileName, false); }

	static public Mesh LoadFromObjectFile(String sFileName, boolean hasTexture) {
		Mesh obj = new Mesh();
		String[] file = GameUtils.instance.loadFileAsStringArray(sFileName);
		List<SVector> verts = new ArrayList<>();
		List<SVector> texs = new ArrayList<>();
		int li = 0;
		for (String line : file) {
			String[] p = null;
			li++;
			try {
				if (line.length() < 1) continue;
				if (line.charAt(0) == 'v') {
					if (line.length() < 2) continue;
					if (line.charAt(1) == 't') {
						SVector v = new SVector();
						p = line.split(" ");
						v.x = Float.parseFloat(p[1]);
						v.y = Float.parseFloat(p[2]);
//						v.z = Float.parseFloat(p[3]);
						texs.add(v);
					} else {
						SVector v = new SVector();
						p = line.split(" ");
						v.x = Float.parseFloat(p[1]);
						v.y = Float.parseFloat(p[2]);
						v.z = Float.parseFloat(p[3]);
						verts.add(v);
					}
				} else if (!hasTexture) {
					if (line.charAt(0) == 'f') {
						p = line.split(" ");

						int[] f = new int[3];
						f[0] = Integer.parseInt(p[1]);
						f[1] = Integer.parseInt(p[2]);
						f[2] = Integer.parseInt(p[3]);
						Triangle tri = new Triangle(
								verts.get(f[0] - 1),
								verts.get(f[1] - 1),
								verts.get(f[2] - 1));
						obj.tris.add(tri);
					}
				} else {
					if (line.charAt(0) == 'f') {
						String a = line;
						String b = a.replaceAll(" ", "/");
						p = b.split("/");

						int[] f = new int[3];
						int[] t = new int[3];
						f[0] = Integer.parseInt(p[1]);
						f[1] = Integer.parseInt(p[3]);
						f[2] = Integer.parseInt(p[5]);

						t[0] = Integer.parseInt(p[2]);
						t[1] = Integer.parseInt(p[4]);
						t[2] = Integer.parseInt(p[6]);
						Triangle tri = new Triangle(
								verts.get(f[0] - 1),
								verts.get(f[1] - 1),
								verts.get(f[2] - 1),
								texs.get(t[0] - 1),
								texs.get(t[1] - 1),
								texs.get(t[2] - 1));
						obj.tris.add(tri);
					}
				}
			} catch (Exception e) {
				Log.debug(li, e);
			}
		}
		return obj;
	}

	static public Mesh Line() {
		return null;
	}

	static public Mesh CubeGrid() {
		Mesh cube = new Mesh();
		cube.tris = Arrays.asList(
				// 0,0,0
				new Triangle(
						0.0f, 0.0f, 0.0f,
						0.0f, 0.0f, 1.0f,
						0.0f, 0.0f, 1.01f),
				new Triangle(
						0.0f, 0.0f, 0.0f,
						0.0f, 1.0f, 0.0f,
						0.0f, 1.0f, 0.01f),
				new Triangle(
						0.0f, 0.0f, 0.0f,
						1.0f, 0.0f, 0.0f,
						1.0f, 0.0f, 0.01f),

				// 1,0,1
				new Triangle(
						1.0f, 0.0f, 1.0f,
						1.0f, 0.0f, 0.0f,
						1.0f, 0.0f, 0.01f),
				new Triangle(
						1.0f, 0.0f, 1.0f,
						1.0f, 1.0f, 1.0f,
						1.0f, 1.0f, 1.01f),
				new Triangle(
						1.0f, 0.0f, 1.0f,
						0.0f, 0.0f, 1.0f,
						0.0f, 0.0f, 1.01f),

				// 0,1,1
				new Triangle(
						0.0f, 1.0f, 1.0f,
						0.0f, 1.0f, 0.0f,
						0.0f, 1.0f, 0.01f),
				new Triangle(
						0.0f, 1.0f, 1.0f,
						0.0f, 0.0f, 1.0f,
						0.0f, 0.0f, 1.01f),
				new Triangle(
						0.0f, 1.0f, 1.0f,
						1.0f, 1.0f, 1.0f,
						1.0f, 1.0f, 1.01f),

				// 1,1,0
				new Triangle(
						1.0f, 1.0f, 0.0f,
						1.0f, 1.0f, 1.0f,
						1.0f, 1.0f, 1.01f),
				new Triangle(
						1.0f, 1.0f, 0.0f,
						1.0f, 0.0f, 0.0f,
						1.0f, 0.0f, 0.01f),
				new Triangle(
						1.0f, 1.0f, 0.0f,
						0.0f, 1.0f, 0.0f,
						0.0f, 1.0f, 0.01f));
		return cube;
	}

	static public Mesh Cube() {
		Mesh cube = new Mesh();
		cube.tris = Arrays.asList(
				// SOUTH
				new Triangle(
						0.0f, 0.0f, 0.0f,
						0.0f, 1.0f, 0.0f,
						1.0f, 1.0f, 0.0f,
						0.0f, 1.0f,
						0.0f, 0.0f,
						1.0f, 0.0f),
				new Triangle(
						0.0f, 0.0f, 0.0f,
						1.0f, 1.0f, 0.0f,
						1.0f, 0.0f, 0.0f,
						0.0f, 1.0f,
						1.0f, 0.0f,
						1.0f, 1.0f),

				// EAST
				new Triangle(
						1.0f, 0.0f, 0.0f,
						1.0f, 1.0f, 0.0f,
						1.0f, 1.0f, 1.0f,
						0.0f, 1.0f,
						0.0f, 0.0f,
						1.0f, 0.0f),
				new Triangle(
						1.0f, 0.0f, 0.0f,
						1.0f, 1.0f, 1.0f,
						1.0f, 0.0f, 1.0f,
						0.0f, 1.0f,
						1.0f, 0.0f,
						1.0f, 1.0f),

				// NORTH
				new Triangle(
						1.0f, 0.0f, 1.0f,
						1.0f, 1.0f, 1.0f,
						0.0f, 1.0f, 1.0f,
						0.0f, 1.0f,
						0.0f, 0.0f,
						1.0f, 0.0f),
				new Triangle(
						1.0f, 0.0f, 1.0f,
						0.0f, 1.0f, 1.0f,
						0.0f, 0.0f, 1.0f,
						0.0f, 1.0f,
						1.0f, 0.0f,
						1.0f, 1.0f),

				// WEST
				new Triangle(
						0.0f, 0.0f, 1.0f,
						0.0f, 1.0f, 1.0f,
						0.0f, 1.0f, 0.0f,
						0.0f, 1.0f,
						0.0f, 0.0f,
						1.0f, 0.0f),
				new Triangle(
						0.0f, 0.0f, 1.0f,
						0.0f, 1.0f, 0.0f,
						0.0f, 0.0f, 0.0f,
						0.0f, 1.0f,
						1.0f, 0.0f,
						1.0f, 1.0f),

				// TOP
				new Triangle(
						0.0f, 1.0f, 0.0f,
						0.0f, 1.0f, 1.0f,
						1.0f, 1.0f, 1.0f,
						0.0f, 1.0f,
						0.0f, 0.0f,
						1.0f, 0.0f),
				new Triangle(
						0.0f, 1.0f, 0.0f,
						1.0f, 1.0f, 1.0f,
						1.0f, 1.0f, 0.0f,
						0.0f, 1.0f,
						1.0f, 0.0f,
						1.0f, 1.0f),

				// BOTTOM
				new Triangle(
						1.0f, 0.0f, 1.0f,
						0.0f, 0.0f, 1.0f,
						0.0f, 0.0f, 0.0f,
						0.0f, 1.0f,
						0.0f, 0.0f,
						1.0f, 0.0f),
				new Triangle(
						1.0f, 0.0f, 1.0f,
						0.0f, 0.0f, 0.0f,
						1.0f, 0.0f, 0.0f,
						0.0f, 1.0f,
						1.0f, 0.0f,
						1.0f, 1.0f));
		return cube;
	}

	static public Mesh Flat() {
		Mesh mesh = new Mesh();
		mesh.tris = Arrays.asList(
				new Triangle(
						0.0f, 0.0f, 0.0f, 1.0f,
						0.0f, 1.0f, 0.0f, 1.0f,
						1.0f, 1.0f, 0.0f, 1.0f,
						0.0f, 0.0f, 1.0f,
						0.0f, 1.0f, 1.0f,
						1.0f, 1.0f, 1.0f),
				new Triangle(
						0.0f, 0.0f, 0.0f, 1.0f,
						1.0f, 1.0f, 0.0f, 1.0f,
						1.0f, 0.0f, 0.0f, 1.0f,
						0.0f, 0.0f, 1.0f,
						1.0f, 1.0f, 1.0f,
						1.0f, 0.0f, 1.0f));
		return mesh;
	}

	static public Mesh Wall() {
		Mesh mesh = new Mesh();
		mesh.tris = Arrays.asList(
				// EAST
				new Triangle(
						1.0f, 0.0f, 0.0f, 1.0f,
						1.0f, 1.0f, 0.0f, 1.0f,
						1.0f, 1.0f, 0.2f, 1.0f,
						1.0f, 1.0f, 0.0f,
						1.0f, 0.0f, 0.0f,
						0.0f, 0.0f, 0.0f),
				new Triangle(
						1.0f, 0.0f, 0.0f, 1.0f,
						1.0f, 1.0f, 0.2f, 1.0f,
						1.0f, 0.0f, 0.2f, 1.0f,
						1.0f, 1.0f, 0.0f,
						0.0f, 0.0f, 0.0f,
						0.0f, 1.0f, 0.0f),

				// WEST
				new Triangle(
						0.0f, 0.0f, 0.2f, 1.0f,
						0.0f, 1.0f, 0.2f, 1.0f,
						0.0f, 1.0f, 0.0f, 1.0f,
						0.0f, 1.0f, 0.0f,
						0.0f, 0.0f, 0.0f,
						1.0f, 0.0f, 0.0f),
				new Triangle(
						0.0f, 0.0f, 0.2f, 1.0f,
						0.0f, 1.0f, 0.0f, 1.0f,
						0.0f, 0.0f, 0.0f, 1.0f,
						0.0f, 1.0f, 0.0f,
						1.0f, 0.0f, 0.0f,
						1.0f, 1.0f, 0.0f),

				// TOP
				new Triangle(
						0.0f, 1.0f, 0.0f, 1.0f,
						0.0f, 1.0f, 0.2f, 1.0f,
						1.0f, 1.0f, 0.2f, 1.0f,
						1.0f, 0.0f, 0.0f,
						0.0f, 0.0f, 0.0f,
						0.0f, 1.0f, 0.0f),
				new Triangle(
						0.0f, 1.0f, 0.0f, 1.0f,
						1.0f, 1.0f, 0.2f, 1.0f,
						1.0f, 1.0f, 0.0f, 1.0f,
						1.0f, 0.0f, 0.0f,
						0.0f, 1.0f, 0.0f,
						1.0f, 1.0f, 0.0f),

				// BOTTOM
				new Triangle(
						1.0f, 0.0f, 0.2f, 1.0f,
						0.0f, 0.0f, 0.2f, 1.0f,
						0.0f, 0.0f, 0.0f, 1.0f,
						0.0f, 1.0f, 0.0f,
						0.0f, 0.0f, 0.0f,
						1.0f, 0.0f, 0.0f),
				new Triangle(
						1.0f, 0.0f, 0.2f, 1.0f,
						0.0f, 0.0f, 0.0f, 1.0f,
						1.0f, 0.0f, 0.0f, 1.0f,
						0.0f, 1.0f, 0.0f,
						1.0f, 0.0f, 0.0f,
						1.0f, 1.0f, 0.0f));
		return mesh;
	}
}