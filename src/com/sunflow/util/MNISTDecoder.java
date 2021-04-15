package com.sunflow.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.sunflow.gfx.SImage;

public class MNISTDecoder {
	private static final int SIZE = 28;

	public static int toUnsignedByte(byte b) { return b & 0xFF; }

	public static List<Digit> loadDataSet(String datas, String labels) {
		Path dataPath = Paths.get(datas);
		Path labelPath = Paths.get(labels);

		try {
			byte[] dataByte = Files.readAllBytes(dataPath);
			byte[] labelByte = Files.readAllBytes(labelPath);

			List<Digit> digits = new ArrayList<>();

			int readHeadData = 16;
			int readHeadLabel = 8;
			while (readHeadData < dataByte.length) {
				byte[][] data = new byte[SIZE][SIZE];
				for (int y = 0; y < SIZE; y++) for (int x = 0; x < SIZE; x++) {
					data[y][x] = dataByte[readHeadData++];
				}
				int label = toUnsignedByte(labelByte[readHeadLabel++]);

				digits.add(new Digit(label, data));
			}

			return digits;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return new ArrayList<>();
	}

	public static class Digit extends SImage {
		public int label;
		public byte[][] data;

		public Digit(int label, byte[][] data) {
			super(SIZE, SIZE);

			this.label = label;
			this.data = data;
			loadPixels();
			for (int y = 0; y < SIZE; y++) for (int x = 0; x < SIZE; x++) {
				int gray = MNISTDecoder.toUnsignedByte(data[x][y]);
				pixel(x, y, (gray << 16) | (gray << 8) | gray);
			}
			updatePixels();
		}

		@Override
		public String toString() { return Integer.toString(label); }

		public String toData() {
			StringBuilder builder = new StringBuilder();
			builder.append(label)
					.append(":")
					.append(System.lineSeparator());
			for (int y = 0; y < SIZE; y++) {
				for (int x = 0; x < SIZE; x++) {
					int gray = MNISTDecoder.toUnsignedByte(data[x][y]);
					builder.append((gray < 10 ? "  " : gray < 100 ? gray + " " : "") + gray);
				}
				builder.append(System.lineSeparator());
			}
			return builder.toString();
		}
	}
}