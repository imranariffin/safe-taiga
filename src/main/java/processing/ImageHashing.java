package processing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import app.structure.IntegerPair;
import app.util.Tools;

public class ImageHashing {

	public static IntegerPair[][] getRGBHistogram(BufferedImage bufferedImage) {
		IntegerPair[][] histogram = new IntegerPair[256][3];

		for (int y = 0; y < bufferedImage.getHeight(); y++) {
			for (int x = 0; x < bufferedImage.getWidth(); x++) {
				Color colorAtXY = new Color(bufferedImage.getRGB(x, y));
				if (histogram[colorAtXY.getRed()][0] == null) {
					histogram[colorAtXY.getRed()][0] = new IntegerPair(0, 1);
				} else {
					histogram[colorAtXY.getRed()][0].b++;
				}

				if (histogram[colorAtXY.getGreen()][1] == null) {
					histogram[colorAtXY.getGreen()][1] = new IntegerPair(0, 1);
				} else {
					histogram[colorAtXY.getGreen()][1].b++;
				}

				if (histogram[colorAtXY.getBlue()][2] == null) {
					histogram[colorAtXY.getBlue()][2] = new IntegerPair(0, 1);
				} else {
					histogram[colorAtXY.getBlue()][2].b++;
				}
			}
		}

		return histogram;
	}

	public static String basicHistogramHash(IntegerPair[][] histogram) {
		/**
		 * MOST BASIC CONVERSION, IMAGES WITH THE SAME PIXEL, EXCEPT MOVED AROUND WILL
		 * COLLIDE
		 * 
		 * Apparently this hashing algorithm is GOD AWFUL because every frame has the
		 * same value 'EX1' or something
		 */
		int sum = 0;
		for (int colorValueIndex = 0; colorValueIndex < histogram.length; colorValueIndex++) {
			if (histogram[colorValueIndex][0] != null) {
				sum += histogram[colorValueIndex][0].b;
			}
			if (histogram[colorValueIndex][1] != null) {
				sum += histogram[colorValueIndex][1].b;
			}
			if (histogram[colorValueIndex][2] != null) {
				sum += histogram[colorValueIndex][2].b;
			}
		}
		String hashString = Tools.toSixtyTwoRadix(sum);

		return hashString;
	}

	public static int[] horizontalBinaryHash(int[][][] array) {
		int[] hashes = new int[3];
		for (int y = 0; y < array.length; y++) {
			for (int z = 0; z < array[0][0].length; z++) {
				if (array[y][0][z] > 127) {
					hashes[z] += Math.pow((float) 2, (float) (y + 1));
				}
			}
		}

		return hashes;
	}

	public static int[] partitionHash(int[][][] array) {
		int[] hashes = new int[3];

		for (int y = 0; y < array.length; y++) {
			for (int x = 0; x < array[y].length; x++) {
				for (int z = 0; z < array[y][x].length; z++) {
					if (array[y][x][z] > 249) {
						array[y][x][z] = 249;
					}
					// 15 radix
					int currentValue = (array[y][x][z] / 25) * ((int) (Math.pow(10f, (float) ((y * 3) + x))));
					hashes[z] += currentValue;
				}
			}
		}
		return hashes;
	}

	public static int[] distinctPartitionHash(int[][][] array) {
		int[] hashes = new int[array.length * array[0].length];
		int tmpValue = 0;

		for (int y = 0; y < array.length; y++) {
			for (int x = 0; x < array[y].length; x++) {
				for (int z = 0; z < array[y][x].length; z++) {
					if (array[y][x][z] > 249) {
						tmpValue = 249;
					} else {
						tmpValue = array[y][x][z];
					}
					hashes[z] += (((tmpValue) / 25) * ((int) (Math.pow(10f, (float) ((y * 3) + x)))));
				}
			}
		}
		return hashes;
	}
}
