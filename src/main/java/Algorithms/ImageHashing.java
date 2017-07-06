package Algorithms;

import java.awt.Color;
import java.awt.image.BufferedImage;
import app.structure.IntegerPair;
import app.util.Tools;

public class ImageHashing {

	public static IntegerPair[][] getRGBHistogram(BufferedImage bufferedImage) {
		Tools.println(System.lineSeparator() + "FROM:ImageHashing:START:getRGBHistogram");
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

		Tools.println("END:getRGBHistogram");
		return histogram;
	}

	public static String basicHistogramHash(IntegerPair[][] histogram) {
		Tools.println(System.lineSeparator() + "FROM:ImageHashing:START:basicHistogramHash");
		/**
		 * MOST BASIC CONVERSION, IMAGES WITH THE SAME PIXEL, EXCEPT MOVED
		 * AROUND WILL COLLIDE
		 */
		String hashString = "";
		for (int colorValueIndex = 0; colorValueIndex < histogram.length; colorValueIndex++) {
			int sum = 0;
			if (histogram[colorValueIndex][0] != null) {
				sum += histogram[colorValueIndex][0].b;
			}
			if (histogram[colorValueIndex][1] != null) {
				sum += histogram[colorValueIndex][1].b;
			}
			if (histogram[colorValueIndex][1] != null) {
				sum += histogram[colorValueIndex][1].b;
			}
			hashString += toAlphabetic(sum);
		}

		Tools.println("END:basicHistogramHash" + System.lineSeparator());
		return hashString;
	}

	public static String toAlphabetic(int i) {

		// means negative value, so just recursively call this function again
		// but adding negative sign as prefix
		if (i < 0) {
			return "-" + toAlphabetic(-i - 1);
		}

		// find remainder
		int quot = i / (26 + 26 + 10);
		int rem = i % (26 + 26 + 10);

		// if x < 10 i.e. is a number
		if (rem < 10) {
			char letter = Character.forDigit(rem, 10);
			if (quot == 0) {
				return "" + letter;
			} else {
				return toAlphabetic(quot) + letter;
			}
		} else if (rem < 36) {
			char letter = (char) ((int) 'a' + (rem - 10));
			if (quot == 0) {
				return "" + letter;
			} else {
				return toAlphabetic(quot) + letter;
			}
		} else {
			// if x > 26 i.e. is a lowercase letter
			// if x > (26 + 10) i.e. is a capital letter
			char letter = (char) ((int) 'A' + (rem - 10 - 26));
			if (quot == 0) {
				return "" + letter;
			} else {
				return toAlphabetic(quot) + letter;
			}
		}
	}
}
