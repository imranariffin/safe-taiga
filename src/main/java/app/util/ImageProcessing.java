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

	/**
	 * TODO: USE DATABASE TO ASSIGN THESE VALUES
	 */
	public static final int IMG_WIDTH = 640; // Determine the width of the
												// parsed image, will also
												// determine the resized width
												// of
												// any image uploaded
	public static final int IMG_HEIGHT = 360; // Determine the height of the
												// parsed image,will also
												// determine the resized height
												// of any image uploaded
	public static final int DIVISOR_VALUE = 5; // Determine the number of box
												// (width and length)
	public static final int BUFFER_VALUE = 5; // Determine the range to check
												// for RGB values
	public static final int TRIAL_VALUE = 1; // Determine the width and length
												// of the nearby box to check
	public static final int FRAME_SKIP = 72; // Determine the frames to skip
												// when parsing video

	public static BufferedImage resizeImage(BufferedImage originalImage) throws IOException {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessing:START:resizeImage");

		int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_RGB : originalImage.getType();
		BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		g.dispose();

		Tools.println("END:resizeImage" + System.lineSeparator());
		return resizedImage;
	}

	public static void resizeImageWithHint(String fileName) throws IOException {
		Tools.println("System.lineSeperator()START:resizeImageWithHinting:FROM:ImageProcessing");
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
		Tools.println("END:resizeImageWithHinting" + System.lineSeparator());
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

	public static float[][][] getPartitionArray(BufferedImage originalImage) {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessing:START:getImageRGBPartitionValues");

		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		int blockSizeX = width / DIVISOR_VALUE;
		int blockSizeY = height / DIVISOR_VALUE;

		// Array for the average values of partitioned images
		float[][][] partitionArrayRGB = new float[DIVISOR_VALUE][DIVISOR_VALUE][3];

		// Variables for iterating through the image array
		int blockStartX = 0;
		int blockStartY = 0;
		int blockCardinality = blockSizeX * blockSizeY;

		// Placeholder variables when iterating the image
		float partitionTotalValueRed = 0;
		float partitionTotalValueGreen = 0;
		float partitionTotalValueBlue = 0;

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

		Tools.println("END:getImageRGBPartitionValues" + System.lineSeparator());
		return partitionArrayRGB;
	}

	public static BufferedImage getPartitionedBufferedImage(float[][][] partitionArrayRGB) {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessing:START:getPartitionedBufferedImage");

		BufferedImage bufferedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);

		// Parse required information about the image
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		int blockSizeX = width / DIVISOR_VALUE;
		int blockSizeY = height / DIVISOR_VALUE;

		/**
		 * Verify information of the image
		 */
		Tools.println("DIVISOR VALUE:" + DIVISOR_VALUE + System.lineSeparator() + "width:" + width
				+ System.lineSeparator() + "height:" + height + System.lineSeparator() + "blockSizeX:" + blockSizeX
				+ System.lineSeparator() + "blockSizeY:" + blockSizeY);

		// Variables for iterating through the image array
		int blockStartX = 0;
		int blockStartY = 0;

		// Validate values of RGB array
		Tools.println(Tools.convertTripleArrayToString(partitionArrayRGB));
		// Assign RGB color to the new image
		for (int a = 0; a < DIVISOR_VALUE; a++) { // Y-axis
			for (int b = 0; b < DIVISOR_VALUE; b++) { // X-axis
				// do stuff in the partition
				for (int c = 0; c < blockSizeY; c++) { // Y-axis
					for (int d = 0; d < blockSizeX; d++) { // X-axis
						Tools.println("Red:" + partitionArrayRGB[b][a][0] + System.lineSeparator() + "Green:"
								+ partitionArrayRGB[b][a][1] + System.lineSeparator() + "Blue:"
								+ partitionArrayRGB[b][a][2]);
						Color newColor = new Color(partitionArrayRGB[b][a][0], partitionArrayRGB[b][a][1],
								partitionArrayRGB[b][a][2]);
						bufferedImage.setRGB((d + blockStartX), (c + blockStartY), newColor.getRGB());
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

		Tools.println("END:getPartitionedBufferedImage" + System.lineSeparator());
		return bufferedImage;
	}

	public static float[][][] getGlobalDifferenceArray(BufferedImage resizedImage) {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessing:START:getGlobalDifferenceArray");

		int width = resizedImage.getWidth();
		int height = resizedImage.getHeight();

		float globalSumRed = 0, globalSumGreen = 0, globalSumBlue = 0;
		for (int y = 0; y < width; y++) { // y-axis
			for (int x = 0; x < height; x++) { // x-axis
				Color colorAtXY = new Color(resizedImage.getRGB(x, y));
				globalSumRed += colorAtXY.getRed();
				globalSumGreen += colorAtXY.getGreen();
				globalSumBlue += colorAtXY.getBlue();
			}
		}

		float globalAverageRed = globalSumRed / (width * height);
		float globalAverageGreen = globalSumGreen / (width * height);
		float globalAverageBlue = globalSumBlue / (width * height);

		Tools.println("END:getGlobalDifference" + System.lineSeparator());
		return null;
	}
}