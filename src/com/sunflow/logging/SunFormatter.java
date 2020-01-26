package com.sunflow.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

class SunFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		Object[] params = record.getParameters();
		if (params != null) {
			Object[] paramsArraysHandeled = new Object[params.length];
			for (int i = 0; i < paramsArraysHandeled.length; i++)
				paramsArraysHandeled[i] = formatArray(params[i]);
			record.setParameters(paramsArraysHandeled);
		}
		StringBuilder builder = new StringBuilder();

		String date = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS").format(new Date(record.getMillis()));
		String thread = Thread.currentThread().getName();
		String level = record.getLevel().getLocalizedName();
		String name = record.getLoggerName();
		String marker = "";
		String message = formatMessage(record);
		String exception = "";

		if (record.getThrown() != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				exception = sw.toString();
				sw.close();
				if (message != "") message += "\n";
			} catch (Exception ex) { /* Do Nothing */ }
		}

		return builder
				.append("[").append(date).append("]")
				.append(" ")
				.append("[").append(thread).append("/").append(level).append("]")
				.append(" ")
				.append("[").append(name).append("/").append(marker).append("]")
				.append(": ").append(message)
				.append(exception)
				.append("\n").toString();
	}

	public static Object formatArray(Object what) {
		if (what == null) return what; // object is null
		String name = what.getClass().getName();
		if (name.charAt(0) != '[') return what; // not an Array
		StringBuilder msg = new StringBuilder("[");
		switch (name.charAt(1)) {
			case '[':
				// don't even mess with multi-dimensional arrays (case '[')
				// or anything else that's not int, float, boolean, char
				return what;

			case 'L': // String
				// print a 1D array of objects as individual elements
				Object poo[] = (Object[]) what;
				for (int i = 0; i < poo.length; i++)
					if (poo[i] instanceof String) msg.append("{" + i + "} ").append("\"" + poo[i] + "\"").append(i < poo.length - 1 ? ", " : "]");
					else msg.append("{" + i + "} ").append(poo[i]).append(i < poo.length - 1 ? ", " : "]");
				break;

			case 'Z': // boolean
				boolean zz[] = (boolean[]) what;
				for (int i = 0; i < zz.length; i++) msg.append("{" + i + "} ").append(zz[i]).append(i < zz.length - 1 ? ", " : "]");
				break;

			case 'C': // char
				char cc[] = (char[]) what;
				for (int i = 0; i < cc.length; i++) msg.append("{" + i + "} ").append("'" + cc[i] + "'").append(i < cc.length - 1 ? ", " : "]");
				break;

			case 'B': // byte
				byte bb[] = (byte[]) what;
				for (int i = 0; i < bb.length; i++) msg.append("{" + i + "} ").append(bb[i]).append(i < bb.length - 1 ? ", " : "]");
				break;

			case 'S': // short
				short ss[] = (short[]) what;
				for (int i = 0; i < ss.length; i++) msg.append("{" + i + "} ").append(ss[i]).append(i < ss.length - 1 ? ", " : "]");
				break;

			case 'I': // int
				int ii[] = (int[]) what;
				for (int i = 0; i < ii.length; i++) msg.append("{" + i + "}").append(ii[i]).append(i < ii.length - 1 ? ", " : "]");
				break;

			case 'J': // long
				long jj[] = (long[]) what;
				for (int i = 0; i < jj.length; i++) msg.append("{" + i + "}").append(jj[i]).append(i < jj.length - 1 ? ", " : "]");
				break;

			case 'F': // float
				float ff[] = (float[]) what;
				for (int i = 0; i < ff.length; i++) msg.append("{" + i + "}").append(ff[i]).append(i < ff.length - 1 ? ", " : "]");
				break;

			case 'D': // double
				double dd[] = (double[]) what;
				for (int i = 0; i < dd.length; i++) msg.append("{" + i + "}").append(dd[i]).append(i < dd.length - 1 ? ", " : "]");
				break;

			default:
				return what;
		}
		return msg;
	}
}
