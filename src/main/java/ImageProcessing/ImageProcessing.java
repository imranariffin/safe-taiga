package ImageProcessing;

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
	public static final int FRAME_SKIP = 1; // Determine the frames to skip
											// when parsing video

	public static BufferedImage resizeImage(BufferedImage originalImage) throws IOException {

		int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_RGB : originalImage.getType();
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

	public static int[][][] getPartitionArray(BufferedImage givenImage) {
		int width = givenImage.getWidth();
		int height = givenImage.getHeight();

		int blockSizeX = width / DIVISOR_VALUE;
		int blockSizeY = height / DIVISOR_VALUE;

		// Array for the average values of partitioned images
		int[][][] array = new int[DIVISOR_VALUE][DIVISOR_VALUE][3];

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
				array[b][a][0] = partitionTotalValueRed / blockCardinality;
				array[b][a][1] = partitionTotalValueGreen / blockCardinality;
				array[b][a][2] = partitionTotalValueBlue / blockCardinality;

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
		return array;
	}

	public static BufferedImage getPartitionedBufferedImage(int[][][] givenArray) {
		BufferedImage bufferedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);

		// Parse required information about the image
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();

		int blockSizeX = width / DIVISOR_VALUE;
		int blockSizeY = height / DIVISOR_VALUE;

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
		return bufferedImage;
	}

	public static int[][][] getGlobalDifferenceArray(BufferedImage givenImage) {
		int width = givenImage.getWidth(); // X-axis
		int height = givenImage.getHeight(); // Y-axis

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

		return RGBArray;
	}

	public static int[][][] getGlobalDifferenceBinaryArray(BufferedImage givenImage) {

		int width = givenImage.getWidth(); // Y-axis
		int height = givenImage.getHeight(); // X-axis

		int[][][] RGBArray = new int[height][width][3];
		int globalSum = 0;

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
				if (localValue < globalAverage) {
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
		return RGBArray;
	}

	public static int[][][] getGlobalDifferenceBinaryRGBArray(BufferedImage givenImage) {
		int width = givenImage.getWidth(); // Y-axis
		int height = givenImage.getHeight(); // X-axis

		int[][][] RGBArray = new int[height][width][3];
		int[] RGBglobalSum = new int[] { 0, 0, 0 };

		for (int y = 0; y < height; y++) { // y-axis
			for (int x = 0; x < width; x++) { // x-axis
				Color colorAtXY = new Color(givenImage.getRGB(x, y));
				RGBglobalSum[0] += colorAtXY.getRed();
				RGBglobalSum[1] += colorAtXY.getGreen();
				RGBglobalSum[2] += colorAtXY.getBlue();

				RGBArray[y][x][0] = colorAtXY.getRed();
				RGBArray[y][x][1] = colorAtXY.getGreen();
				RGBArray[y][x][2] = colorAtXY.getBlue();
			}
		}

		int[] globalAverage = new int[] { Math.round(RGBglobalSum[0] / (width * height)),
				Math.round(RGBglobalSum[1] / (width * height)), Math.round(RGBglobalSum[2] / (width * height)) };

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int[] localValue = new int[] { RGBArray[y][x][0], RGBArray[y][x][1], RGBArray[y][x][2] };
				if (localValue[0] < globalAverage[0]) {
					RGBArray[y][x][0] = 0;
				} else {
					RGBArray[y][x][0] = 255;
				}
				if (localValue[1] < globalAverage[1]) {
					RGBArray[y][x][1] = 0;
				} else {
					RGBArray[y][x][1] = 255;
				}
				if (localValue[2] < globalAverage[2]) {
					RGBArray[y][x][2] = 0;
				} else {
					RGBArray[y][x][2] = 255;
				}
			}
		}
		return RGBArray;

	}

	public static BufferedImage getBufferedImageGivenArray(int[][][] givenArray) {
		BufferedImage bufferedImage = new BufferedImage(givenArray[0].length, givenArray.length,
				BufferedImage.TYPE_INT_RGB);

		// Assign RGB color to the new image
		for (int y = 0; y < givenArray.length; y++) { // Y-axis
			for (int x = 0; x < givenArray[y].length; x++) { // X-axis
				Color newColor = new Color(givenArray[y][x][0], givenArray[y][x][1], givenArray[y][x][2]);
				bufferedImage.setRGB(x, y, newColor.getRGB());
			}
		}
		return bufferedImage;
	}

	/**
	 * Check if the int values in oldArray and newArray are the same, returns an
	 * array of boolean of the same size. If the index (X,Y) != (X',Y') then it
	 * will return true, returns false otherwise
	 * 
	 * @param oldArray
	 * @param newArray
	 * @return Returns an double array of equality between old and new array
	 */
	public static boolean[][][] checkArrayDifference(int[][][] oldArray, int[][][] newArray) {

		// check if the sizes of oldArray and newArray are identical
		if ((oldArray.length != newArray.length) || (oldArray[0].length != newArray[0].length)
				|| (oldArray[0][0].length != newArray[0][0].length)) {
			throw new IllegalArgumentException("oldArray and newArray sizes are not the same.");
		}
		boolean[][][] changeBool = new boolean[oldArray.length][oldArray[0].length][oldArray[0][0].length];
		for (int y = 0; y < oldArray.length; y++) { // Y-axis
			for (int x = 0; x < oldArray[y].length; x++) { // X-axis
				for (int z = 0; z < oldArray[y][x].length; z++) {
					if (Math.abs(oldArray[y][x][z] - newArray[y][x][z]) > BUFFER_VALUE) {
						changeBool[y][x][z] = true;
					} else {
						changeBool[y][x][z] = false;
					}
				}
			}
		}

		return changeBool;
	}

	public static int[][][] getArrayFromBufferedImage(BufferedImage image) {

		int[][][] array = new int[image.getHeight()][image.getWidth()][3];
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				Color colorAtXY = new Color(image.getRGB(x, y));
				array[y][x][0] = colorAtXY.getRed();
				array[y][x][1] = colorAtXY.getGreen();
				array[y][x][2] = colorAtXY.getBlue();
			}
		}

		return array;
	}

	/**
	 * Convert a bufferedImage to bytes array
	 * 
	 * @param bufferedImage
	 * @return
	 */
	public static byte[] extractBytes(BufferedImage bufferedImage) {
		// get DataBufferBytes from Raster
		WritableRaster raster = bufferedImage.getRaster();
		DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
		return (data.getData());
	}

	/**
	 * Convert an imagefile to bytes array
	 * 
	 * @param ImageName
	 * @return
	 * @throws IOException
	 *             -- if the file cannot be specified
	 */
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