package Algorithms;

import java.awt.Color;
import java.awt.image.BufferedImage;

import app.structure.IntegerPair;

public class ImageHashing {

	public static IntegerPair[][] getRGBHistogram(BufferedImage bufferedImage) {
		IntegerPair[][] histogram = new IntegerPair[255][3];

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
					histogram[colorAtXY.getRed()][1].b++;
				}

				if (histogram[colorAtXY.getBlue()][2] == null) {
					histogram[colorAtXY.getBlue()][2] = new IntegerPair(0, 1);
				} else {
					histogram[colorAtXY.getRed()][2].b++;
				}
			}
		}
		return histogram;
	}
}
