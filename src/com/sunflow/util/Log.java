package com.sunflow.util;

import java.util.Arrays;

public class Log {

	public static void info() {
		System.out.println();
		System.out.flush();
	}

	public static void info(Object msg) {
		if (msg instanceof Arrays) {
			Log.err("instance of Arrays");
		}
		System.out.println(getMsg(msg));
		System.out.flush();
	}

	public static void xinfo() {
		System.out.print("");
		System.out.flush();
	}

	public static void infoArray(Object msg) {
		System.out.println(getArrayMsg(msg));
		System.out.flush();
	}

	public static void xinfo(Object msg) {
		if (msg instanceof Arrays) {
			Log.err("instance of Arrays");
		}
		System.out.print(getMsg(msg));
		System.out.flush();
	}

	public static void xinfoArray(Object msg) {
		System.out.print(getArrayMsg(msg));
		System.out.flush();
	}

	public static void err() {
		System.err.println();
		System.err.flush();
	}

	public static void err(Object msg) {
		System.err.println(getMsg(msg));
		System.err.flush();
	}

	public static void errArray(Object msg) {
		System.err.print(getArrayMsg(msg));
		System.err.flush();
	}

	public static void xerr() {
		System.err.print("");
		System.err.flush();
	}

	public static void xerr(Object msg) {
		System.err.print(getMsg(msg));
		System.err.flush();
	}

	public static void xerrArray(Object msg) {
		System.err.print(getArrayMsg(msg));
		System.err.flush();
	}

	private static String getMsg(Object msg) {
		if (msg == null) {
			return "null";
		}
		return msg.toString();
	}

	private static String getArrayMsg(Object what) {
		String msg = "";
		if (what == null) {
			// special case since this does fuggly things on > 1.1
//			System.out.println("null");
			msg = "null";
		} else {
			String name = what.getClass().getName();
			if (name.charAt(0) == '[') {
				switch (name.charAt(1)) {
					case '[':
						// don't even mess with multi-dimensional arrays (case '[')
						// or anything else that's not int, float, boolean, char
//						System.out.println(what);
						msg = getMsg(what);
						break;

					case 'L':
						// print a 1D array of objects as individual elements
						Object poo[] = (Object[]) what;
						for (int i = 0; i < poo.length; i++) {
							if (poo[i] instanceof String) {
//								System.out.println("[" + i + "] \"" + poo[i] + "\"");
								msg += "[" + i + "] \"" + poo[i] + "\"" + System.lineSeparator();
							} else {
//								System.out.println("[" + i + "] " + poo[i]);
								msg += "[" + i + "] " + poo[i] + System.lineSeparator();
							}
						}
						break;

					case 'Z': // boolean
						boolean zz[] = (boolean[]) what;
						for (int i = 0; i < zz.length; i++) {
//							System.out.println("[" + i + "] " + zz[i]);
							msg += "[" + i + "] " + zz[i] + System.lineSeparator();
						}
						break;

					case 'B': // byte
						byte bb[] = (byte[]) what;
						for (int i = 0; i < bb.length; i++) {
//							System.out.println("[" + i + "] " + bb[i]);
							msg += "[" + i + "] " + bb[i] + System.lineSeparator();
						}
						break;

					case 'C': // char
						char cc[] = (char[]) what;
						for (int i = 0; i < cc.length; i++) {
//							System.out.println("[" + i + "] '" + cc[i] + "'");
							msg += "[" + i + "] '" + cc[i] + "'" + System.lineSeparator();
						}
						break;

					case 'I': // int
						int ii[] = (int[]) what;
						for (int i = 0; i < ii.length; i++) {
//							System.out.println("[" + i + "] " + ii[i]);
							msg += "[" + i + "] " + ii[i] + System.lineSeparator();
						}
						break;

					case 'J': // int
						long jj[] = (long[]) what;
						for (int i = 0; i < jj.length; i++) {
//							System.out.println("[" + i + "] " + jj[i]);
							msg += "[" + i + "] " + jj[i] + System.lineSeparator();
						}
						break;

					case 'F': // float
						float ff[] = (float[]) what;
						for (int i = 0; i < ff.length; i++) {
//							System.out.println("[" + i + "] " + ff[i]);
							msg += "[" + i + "] " + ff[i] + System.lineSeparator();
						}
						break;

					case 'D': // double
						double dd[] = (double[]) what;
						for (int i = 0; i < dd.length; i++) {
//							System.out.println("[" + i + "] " + dd[i]);
							msg += "[" + i + "] " + dd[i] + System.lineSeparator();
						}
						break;

					default:
//						System.out.println(what);
						msg = getMsg(what);
				}
			} else { // not an array
//				System.out.println(what);
				msg = getMsg(what);
			}
		}
//		System.out.flush();
		return msg;
	}

}
