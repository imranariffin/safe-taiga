package app.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import javax.imageio.ImageIO;

public class ImageProcessing {

	private static final int IMG_WIDTH = 1280;
	private static final int IMG_HEIGHT = 720;

	public static BufferedImage partitionImage(BufferedImage originalImage) {

		// BufferedImage image;
		int width;
		int height;
		int divisorSize = 30;

		try {
			width = originalImage.getWidth();
			height = originalImage.getHeight();

			int blockSizeX = width / divisorSize;
			int blockSizeY = height / divisorSize;

			// Array for the average values of partitioned images
			int[][] partitionArrayRed = new int[divisorSize][divisorSize];
			int[][] partitionArrayGreen = new int[divisorSize][divisorSize];
			int[][] partitionArrayBlue = new int[divisorSize][divisorSize];

			// Variables for iterating through the image array
			int blockStartX = 0;
			int blockStartY = 0;
			int blockCardinality = blockSizeX * blockSizeY;

			// Placeholder variables when iterating the image
			int partitionTotalValueRed = 0;
			int partitionTotalValueGreen = 0;
			int partitionTotalValueBlue = 0;

			for (int a = 0; a < divisorSize; a++) { // Y-axis
				for (int b = 0; b < divisorSize; b++) { // X-axis
					// do stuff in the partition
					for (int c = 0; c < blockSizeY; c++) { // Y-axis
						for (int d = 0; d < blockSizeX; d++) { // X-axis
							Color colorAtXY = new Color(originalImage.getRGB(d + blockStartX, c + blockStartY));
							partitionTotalValueRed += colorAtXY.getRed();
							partitionTotalValueGreen += colorAtXY.getGreen();
							partitionTotalValueBlue += colorAtXY.getBlue();
						}
					}

					// reaching here means we are done with a block

					// now assigning the average to the partitionArrays
					partitionArrayRed[b][a] = partitionTotalValueRed / blockCardinality;
					partitionArrayGreen[b][a] = partitionTotalValueGreen / blockCardinality;
					partitionArrayBlue[b][a] = partitionTotalValueBlue / blockCardinality;

					// reset partitionTotalValues
					partitionTotalValueRed = 0;
					partitionTotalValueGreen = 0;
					partitionTotalValueBlue = 0;

					// move to the next X block
					blockStartX += blockSizeX;
				}

				// move to the next Y block
				blockStartY += blockSizeY;

				// reset blockStartX
				blockStartX = 0;
			}
			// Variables for iterating through the image array
			blockStartX = 0;
			blockStartY = 0;

			// Assign RGB color to the new image
			for (int a = 0; a < divisorSize; a++) { // Y-axis
				for (int b = 0; b < divisorSize; b++) { // X-axis
					// do stuff in the partition
					for (int c = 0; c < blockSizeY; c++) { // Y-axis
						for (int d = 0; d < blockSizeX; d++) { // X-axis
							Color newColor = new Color(partitionArrayRed[b][a], partitionArrayGreen[b][a],
									partitionArrayBlue[b][a]);
							originalImage.setRGB((d + blockStartX), (c + blockStartY), newColor.getRGB());
						}
					}
					// move to the next X block
					blockStartX += blockSizeX;
				}
				// move to the next Y block
				blockStartY += blockSizeY;

				// reset blockStartX
				blockStartX = 0;
			}
			return originalImage; // now modified
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static BufferedImage resizeImage(BufferedImage originalImage) throws IOException {
		int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

		BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		g.dispose();

		return resizedImage;
	}

	public static void resizeImageWithHint(String fileName) throws IOException {
		BufferedImage originalImage = ImageIO.read(new File("images/input/" + fileName));
		int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
		BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		ImageIO.write(resizedImage, "png", new File("images/input/resizedwithhint/" + fileName));
	}

	public static void convertToPng(String filename) {
		File file = new File("images/input/" + filename);
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(FileUtils.readFileToByteArray(file));
			BufferedImage image = ImageIO.read(bais);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			OutputStream outputStream = new FileOutputStream(
					"images/input/png/" + filename.substring(0, filename.length() - 3) + "png");
			baos.writeTo(outputStream);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}