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

	public static final int IMG_WIDTH = 640;
	public static final int IMG_HEIGHT = 360;
	public static final int DIVISOR_VALUE = 10;

	public static BufferedImage partitionImage(BufferedImage originalImage, int[][][] partitionArrayRGB) {
		Tools.println("FROM:ImageProcessing:START:partitionImage");

		// parse required information about the image
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		int blockSizeX = width / DIVISOR_VALUE;
		int blockSizeY = height / DIVISOR_VALUE;

		/**
		 * Verify information of the image
		 */
		Tools.println("divisor image:" + DIVISOR_VALUE + "\n" + "width:" + width + "\n" + "height:" + height + "\n"
				+ "blockSizeX:" + blockSizeX + "\n" + "blockSizeY:" + blockSizeY);

		// Variables for iterating through the image array
		int blockStartX = 0;
		int blockStartY = 0;

		// Assign RGB color to the new image
		for (int a = 0; a < DIVISOR_VALUE; a++) { // Y-axis
			for (int b = 0; b < DIVISOR_VALUE; b++) { // X-axis
				// do stuff in the partition
				for (int c = 0; c < blockSizeY; c++) { // Y-axis
					for (int d = 0; d < blockSizeX; d++) { // X-axis
						Color newColor = new Color(partitionArrayRGB[b][a][0], partitionArrayRGB[b][a][1],
								partitionArrayRGB[b][a][2]);
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

		Tools.println("END:partitionImage");
		return originalImage;
	}

	public static BufferedImage resizeImage(BufferedImage originalImage) throws IOException {
		Tools.println("FROM:ImageProcessing:START:resizeImage");

		int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_RGB : originalImage.getType();
		BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		g.dispose();

		Tools.println("END:resizeImage");
		return resizedImage;
	}

	public static void resizeImageWithHint(String fileName) throws IOException {
		Tools.println("START:resizeImageWithHinting:FROM:ImageProcessing");
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
		Tools.println("END:resizeImageWithHinting");
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

	public static int[][][] getImageRGBPartitionValues(BufferedImage originalImage) {
		Tools.println("FROM:ImageProcessing:START:getImageRGBPartitionValues");

		// BufferedImage image;
		int width;
		int height;
		width = originalImage.getWidth();
		height = originalImage.getHeight();

		int blockSizeX = width / DIVISOR_VALUE;
		int blockSizeY = height / DIVISOR_VALUE;

		// Array for the average values of partitioned images
		int[][][] partitionArrayRGB = new int[DIVISOR_VALUE][DIVISOR_VALUE][3];

		// Variables for iterating through the image array
		int blockStartX = 0;
		int blockStartY = 0;
		int blockCardinality = blockSizeX * blockSizeY;

		// Placeholder variables when iterating the image
		int partitionTotalValueRed = 0;
		int partitionTotalValueGreen = 0;
		int partitionTotalValueBlue = 0;

		for (int a = 0; a < DIVISOR_VALUE; a++) { // Y-axis
			for (int b = 0; b < DIVISOR_VALUE; b++) { // X-axis
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
				partitionArrayRGB[b][a][0] = partitionTotalValueRed / blockCardinality;
				partitionArrayRGB[b][a][1] = partitionTotalValueGreen / blockCardinality;
				partitionArrayRGB[b][a][2] = partitionTotalValueBlue / blockCardinality;

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

		Tools.println("END:getImageRGBPartitionValues");
		return partitionArrayRGB;
	}

	public static String getStringFromTripleArray(int[][][] tripleArray) {
		Tools.println("FROM:ImageProcessing:START:getStringFromTripleArray");
		String outputText = "";
		for (int a = 0; a < tripleArray.length; a++) { // Y-axis
			for (int b = 0; b < tripleArray[a].length; b++) { // X-axis
				for (int c = 0; c < tripleArray[a][b].length; c++) {
					outputText += tripleArray[b][a][c] + " ";
				}
			}
			outputText += "\n";
		}
		Tools.println("END:getStringFromTripleArray");
		return outputText;
	}
}