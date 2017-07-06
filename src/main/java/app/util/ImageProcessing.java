package app.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
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
	public static final int IMG_WIDTH = 320; // Determine the width of the
												// parsed image, will also
												// determine the resized width
												// of
												// any image uploaded
	public static final int IMG_HEIGHT = 180; // Determine the height of the
												// parsed image,will also
												// determine the resized height
												// of any image uploaded
	public static final int DIVISOR_VALUE = 5; // Determine the number of box
												// (width and length)
	public static final int BUFFER_VALUE = 5; // Determine the range to check
												// for RGB values
	public static final int TRIAL_VALUE = 1; // Determine the width and length
												// of the nearby box to check
	public static final int FRAME_SKIP = 144; // Determine the frames to skip
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

	public static int[][][] getPartitionArray(BufferedImage givenImage) {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessing:START:getImageRGBPartitionValues");

		int width = givenImage.getWidth();
		int height = givenImage.getHeight();

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
						Color colorAtXY = new Color(givenImage.getRGB(d + blockStartX, c + blockStartY));
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

	public static BufferedImage getPartitionedBufferedImage(int[][][] givenArray) {
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

		// Assign RGB color to the new image
		for (int a = 0; a < DIVISOR_VALUE; a++) { // Y-axis
			for (int b = 0; b < DIVISOR_VALUE; b++) { // X-axis
				// do stuff in the partition
				for (int c = 0; c < blockSizeY; c++) { // Y-axis
					for (int d = 0; d < blockSizeX; d++) { // X-axis
						Color newColor = new Color(givenArray[b][a][0], givenArray[b][a][1], givenArray[b][a][2]);
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

	public static int[][][] getGlobalDifferenceArray(BufferedImage givenImage) {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessing:START:getGlobalDifferenceArray");

		int width = givenImage.getWidth(); // Y-axis
		int height = givenImage.getHeight(); // X-axis

		/**
		 * Verify the information of the image
		 */
		Tools.println("width:" + width + System.lineSeparator() + "height:" + height);

		int[][][] RGBArray = new int[height][width][3];
		float globalSumRed = 0, globalSumGreen = 0, globalSumBlue = 0;

		for (int y = 0; y < height; y++) { // y-axis
			for (int x = 0; x < width; x++) { // x-axis
				Color colorAtXY = new Color(givenImage.getRGB(x, y));
				globalSumRed += colorAtXY.getRed();
				globalSumGreen += colorAtXY.getGreen();
				globalSumBlue += colorAtXY.getBlue();

				RGBArray[y][x][0] = colorAtXY.getRed();
				RGBArray[y][x][1] = colorAtXY.getGreen();
				RGBArray[y][x][2] = colorAtXY.getBlue();
			}
		}

		int globalAverageRed = Math.round(globalSumRed / (width * height));
		int globalAverageGreen = Math.round(globalSumGreen / (width * height));
		int globalAverageBlue = Math.round(globalSumBlue / (width * height));

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				RGBArray[y][x][0] = RGBArray[y][x][0] - globalAverageRed;
				RGBArray[y][x][1] = RGBArray[y][x][1] - globalAverageGreen;
				RGBArray[y][x][2] = RGBArray[y][x][2] - globalAverageBlue;

				// Filter out negatives
				if (RGBArray[y][x][0] < 0) {
					RGBArray[y][x][0] = 0;
				}

				if (RGBArray[y][x][1] < 0) {
					RGBArray[y][x][1] = 0;
				}
				if (RGBArray[y][x][2] < 0) {
					RGBArray[y][x][2] = 0;
				}
			}
		}

		Tools.println("END:getGlobalDifference" + System.lineSeparator());
		return RGBArray;
	}

	public static int[][][] getGlobalDifferenceArrayBinary(BufferedImage givenImage) {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessing:START:getGlobalDifferenceArray");

		int width = givenImage.getWidth(); // Y-axis
		int height = givenImage.getHeight(); // X-axis

		/**
		 * Verify the information of the image
		 */
		Tools.println("width:" + width + System.lineSeparator() + "height:" + height);

		int[][][] RGBArray = new int[height][width][3];
		float globalSum = 0;

		for (int y = 0; y < height; y++) { // y-axis
			for (int x = 0; x < width; x++) { // x-axis
				Color colorAtXY = new Color(givenImage.getRGB(x, y));
				globalSum += (colorAtXY.getRed() + colorAtXY.getGreen() + colorAtXY.getBlue());

				RGBArray[y][x][0] = colorAtXY.getRed();
				RGBArray[y][x][1] = colorAtXY.getGreen();
				RGBArray[y][x][2] = colorAtXY.getBlue();
			}
		}

		int globalAverage = Math.round(globalSum / (width * height));

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int localValue = RGBArray[y][x][0] + RGBArray[y][x][1] + RGBArray[y][x][2];
				if (localValue < globalSum) {
					RGBArray[y][x][0] = 0;
					RGBArray[y][x][1] = 0;
					RGBArray[y][x][2] = 0;
				} else {
					RGBArray[y][x][0] = 255;
					RGBArray[y][x][1] = 255;
					RGBArray[y][x][2] = 255;
				}
			}
		}

		Tools.println("END:getGlobalDifference" + System.lineSeparator());
		return RGBArray;
	}

	public static BufferedImage getBufferedImageGivenArray(int[][][] givenArray) {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessing:START:getBufferedImageGivenArray");

		BufferedImage bufferedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);

		/**
		 * Verify information of the image
		 */
		Tools.println("width:" + givenArray.length + System.lineSeparator() + "height:" + givenArray[0].length);

		// Assign RGB color to the new image
		for (int y = 0; y < givenArray.length; y++) { // Y-axis
			for (int x = 0; x < givenArray[y].length; x++) { // X-axis
				Color newColor = new Color(givenArray[y][x][0], givenArray[y][x][1], givenArray[y][x][2]);
				bufferedImage.setRGB(x, y, newColor.getRGB());
			}
		}

		Tools.println("END:getBufferedImageGivenArray" + System.lineSeparator());
		return bufferedImage;
	}

	public static byte[] extractBytes(BufferedImage bufferedImage) throws IOException {
		// get DataBufferBytes from Raster
		WritableRaster raster = bufferedImage.getRaster();
		DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
		return (data.getData());
	}

	public static byte[] extractBytes(String ImageName) throws IOException {
		// open image
		File imgPath = new File(ImageName);
		BufferedImage bufferedImage = ImageIO.read(imgPath);

		// get DataBufferBytes from Raster
		WritableRaster raster = bufferedImage.getRaster();
		DataBufferByte data = (DataBufferByte) raster.getDataBuffer();

		return (data.getData());
	}
}