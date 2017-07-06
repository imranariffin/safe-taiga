package app.controllers;

import static app.Application.IMAGES_INPUT_DIR;
import static app.Application.IMAGES_OUTPUT_PARTITION_DIR;
import static app.Application.IMAGES_OUTPUT_GLOBALDIFFERENCE_DIR;
import static app.Application.IMAGES_OUTPUT_RESIZED_DIR;
import static app.Application.TEXT_OUTPUT_PARTITION_DIR;
import static app.Application.TEXT_OUTPUT_GLOBALDIFFERENCE_DIR;
import static app.Application.IMAGES_OUTPUT_GLOBALDIFFERENCEBINARY_DIR;
import static app.Application.IMAGES_OUTPUT_GLOBALDIFFERENCEBINARYRGB_DIR;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import Algorithms.ImageHashing;
import Managers.ImageProcessingManager;
import app.util.ImageProcessing;
import app.util.Reference;
import app.util.Tools;
import app.util.ViewUtil;
import spark.Request;
import spark.Response;
import spark.Route;

public class ImageProcessingController {

	public static boolean BOOL_SCRIPT = true;
	public static boolean BOOL_MATCHING_NAME = false;

	public static Route serveImageUpload = (Request request, Response response) -> {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessingController:START:serveImageUpload");
		Map<String, Object> model = new HashMap<String, Object>();

		model.put("imagefile", "/images/other/image_placeholder.jpg");
		model.put("imagemessage", "your uploaded image will replace the empty image below:");
		model.put("partitionArrayRGB", new int[ImageProcessing.DIVISOR_VALUE][ImageProcessing.DIVISOR_VALUE][3]);

		Tools.println("END:serveImageUpload" + System.lineSeparator());
		return ViewUtil.render(request, model, Reference.Templates.IMAGE_PROCESSING, "Image Upload", "OK");
	};

	public static Route handleImageUpload = (Request request, Response response) -> {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessingController:START:handleImageUpload");
		Map<String, Object> model = new HashMap<String, Object>();

		/**
		 * Get uploaded image file
		 */
		Path tempFile = Files.createTempFile(IMAGES_INPUT_DIR.toPath(), "", ".png");
		request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

		try (InputStream input = request.raw().getPart("uploaded_file").getInputStream()) {
			Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			e.printStackTrace();
			Tools.println("END:handleImageUpload" + System.lineSeparator());
			return ViewUtil.renderErrorMessage(request, e.getMessage(), Reference.CommonStrings.LINK_IMAGEPROCESSING,
					Reference.CommonStrings.NAME_IMAGEPROCESSING);
		}

		/**
		 * Prepare required variables
		 */
		BufferedImage originalImage, resizedImage, partitionedImage, globalDifferenceImage, globalDifferenceBinaryImage,
				globalDifferenceBinaryRGBImage;
		int[][][] partitioningArray, globalDifferenceArray, globalDifferenceBinaryArray, globalDifferenceBinaryRGBArray;

		// Remove any file type post fix
		String filename = tempFile.getFileName().toString().substring(0,
				tempFile.getFileName().toString().length() - 4);

		// Files directory
		String outputOriginalImage = IMAGES_INPUT_DIR.toPath() + "/" + filename + ".png";
		String outputResizedImage = IMAGES_OUTPUT_RESIZED_DIR.toPath() + "/" + filename + ".png";

		String outputPartitionedImage = IMAGES_OUTPUT_PARTITION_DIR.toPath() + "/" + filename + ".png";
		String outputPartitionedtText = TEXT_OUTPUT_PARTITION_DIR.toPath() + "/" + filename + ".txt";

		String outputGlobalDifferenceImage = IMAGES_OUTPUT_GLOBALDIFFERENCE_DIR.toPath() + "/" + filename + ".png";
		String outputGlobalDifferenceText = TEXT_OUTPUT_GLOBALDIFFERENCE_DIR.toPath() + "/" + filename + ".txt";

		String outputGlobalDifferenceBinaryImage = IMAGES_OUTPUT_GLOBALDIFFERENCEBINARY_DIR.toPath() + "/" + filename
				+ ".png";

		String outputGlobalDifferenceBinaryRGBImage = IMAGES_OUTPUT_GLOBALDIFFERENCEBINARYRGB_DIR.toPath() + "/"
				+ filename + ".png";

		// Logging
		Tools.print("Uploaded file '" +

				getFileName(request.raw().getPart("uploaded_file")) + "' saved as '" + tempFile.toAbsolutePath() + "'"
				+ "\nbase filename:" + filename + "\ntemporary image is saved at:" + outputOriginalImage
				+ "\nresized image saved at:" + outputResizedImage + "\ntext created from partitioning saved at:"
				+ outputPartitionedtText + "\npartitioned image created at:" + outputPartitionedImage);

		originalImage = ImageIO.read(new File(outputOriginalImage));

		// Resize the image
		// resizedImage = ImageProcessing.resizeImage(originalImage);

		resizedImage = originalImage;
		// Save resized image for future inquiries
		ImageIO.write(resizedImage, "png", new File(outputResizedImage));

		/**
		 * PARTITION IMAGE
		 * 
		 * Divide the image into several equally sized boxes and find the
		 * average RGB values for each box then assign them to the box
		 */
		// Get resized image partitioning RGB array
		partitioningArray = ImageProcessing.getPartitionArray(resizedImage);

		// Partition the resized image based on the partitioning array
		partitionedImage = ImageProcessing.getPartitionedBufferedImage(partitioningArray);

		// Save the partitioned image for future inquiries
		ImageIO.write(partitionedImage, "png", new File(outputPartitionedImage));

		/**
		 * GLOBAL DIFFERENCE
		 * 
		 * Find the global average RGB values of the image and take the
		 * difference of all individual RGB with the global average
		 */
		// Get resized image global difference RGB array
		globalDifferenceArray = ImageProcessing.getGlobalDifferenceArray(resizedImage);

		// Get the BufferedImage from the global difference RGB array
		globalDifferenceImage = ImageProcessing.getBufferedImageGivenArray(globalDifferenceArray);

		// Save the partitioned image for future inquiries
		ImageIO.write(globalDifferenceImage, "png", new File(outputGlobalDifferenceImage));

		/**
		 * GLOBAL DIFFERENCE BINARY
		 * 
		 * The same as Global Difference but now strictly binary output 0 or 255
		 */
		// Get the resized image global difference binary RGB array
		globalDifferenceBinaryArray = ImageProcessing.getGlobalDifferenceBinaryArray(resizedImage);

		// Get the buffered image from the array
		globalDifferenceBinaryImage = ImageProcessing.getBufferedImageGivenArray(globalDifferenceBinaryArray);

		// Save the image for future reference
		ImageIO.write(globalDifferenceBinaryImage, "png", new File(outputGlobalDifferenceBinaryImage));

		/**
		 * GLOBAL DIFFERENCE BINARY RGB
		 * 
		 * The same as Global Difference but now strictly binary output 0 or 255
		 */
		// Get the resized image global difference binary RGB array
		globalDifferenceBinaryRGBArray = ImageProcessing.getGlobalDifferenceBinaryRGBArray(resizedImage);

		// Get the buffered image from the array
		globalDifferenceBinaryRGBImage = ImageProcessing.getBufferedImageGivenArray(globalDifferenceBinaryRGBArray);

		// Save the image for future reference
		ImageIO.write(globalDifferenceBinaryRGBImage, "png", new File(outputGlobalDifferenceBinaryRGBImage));

		/**
		 * Insert into database the image data sent by user
		 */
		ImageProcessingManager.insertImageDataToDatabase(request.ip(), resizedImage);
		/**
		 * RANDOMIZED SEARCH
		 * 
		 * Find matching boxes given a randomized boxes
		 * 
		 * This is good for cropped pictures
		 */
		Tools.println("TEST 1");
		ImageProcessingManager.findMatchingImageDataRandomized(model, partitioningArray);

		/**
		 * RANDOMIZED SEARCH VERSION 2
		 * 
		 * Find a matching BOX given a randomized BOX, so we will start from a
		 * pixel, then iterate x -- > x + a and y --> y + a
		 * 
		 * This is also specially good for cropped pictures
		 */
		Tools.println("TEST 2");
		ImageProcessingManager.findMatchingImageDataRandomizedV2(model, partitioningArray);

		/**
		 * INCREMENTAL SEARCH NON-RGB
		 * 
		 * Incremental search using non-RGB i.e. we search each RGB as 3
		 * separate 1-tuple
		 */
		Tools.println("TEST 3");
		ImageProcessingManager.findMatchingImageDataIncremental(model, partitioningArray);

		/**
		 * INCREMENTAL SEARCH RGB
		 * 
		 * Incremental search using RGB i.e. we search matching RGB as 3-tuple
		 */
		Tools.println("TEST 4");
		ImageProcessingManager.findMatchingImageDataIncrementalRGB(model, partitioningArray);

		model.put("ORIGINAL_IMAGE_MESSAGE", "The original image, resized:");
		// 7 to remove the substring 'public/'
		Tools.println("Original image directory:" + outputOriginalImage);
		model.put("ORIGINAL_IMAGE_FILE", outputResizedImage.substring(7, outputResizedImage.length()));

		/**
		 * Partitioned image
		 */
		model.put("PARTITIONED_IMAGE_MESSAGE", "Partitioned");
		// 7 to remove the substring 'public/'
		Tools.println("Partitioned image directory:" + outputPartitionedImage);
		model.put("PARTITIONED_IMAGE_FILE", outputPartitionedImage.substring(7, outputPartitionedImage.length()));

		/**
		 * Global Difference
		 */
		model.put("GLOBALDIFFERENCE_IMAGE_MESSAGE", "Global Difference:");
		// 7 to remove the substring 'public/'
		Tools.println("Global difference image directory:" + outputGlobalDifferenceImage);
		model.put("GLOBALDIFFERENCE_IMAGE_FILE",
				outputGlobalDifferenceImage.substring(7, outputGlobalDifferenceImage.length()));

		/**
		 * Global Difference Binary
		 */
		model.put("GLOBALDIFFERENCEBINARY_IMAGE_MESSAGE", "Global Difference Binary:");
		// 7 to remove the substring 'public/'
		Tools.println("Global difference binary image directory:" + outputGlobalDifferenceBinaryImage);
		model.put("GLOBALDIFFERENCEBINARY_IMAGE_FILE",
				outputGlobalDifferenceBinaryImage.substring(7, outputGlobalDifferenceBinaryImage.length()));

		/**
		 * Global Difference Binary RGB
		 */
		model.put("GLOBALDIFFERENCEBINARYRGB_IMAGE_MESSAGE", "Global Difference Binary RGB:");
		// 7 to remove the substring 'public/'
		Tools.println("Global difference binary RGB image directory:" + outputGlobalDifferenceBinaryRGBImage);
		model.put("GLOBALDIFFERENCEBINARYRGB_IMAGE_FILE",
				outputGlobalDifferenceBinaryRGBImage.substring(7, outputGlobalDifferenceBinaryRGBImage.length()));
		/**
		 * BASIC HISTOGRAM HASHING
		 */
		model.put("BASIC_HASH_STRING", ImageHashing.basicHistogramHash(ImageHashing.getRGBHistogram(resizedImage)));
		Tools.println("END:handleImageUpload" + System.lineSeparator());
		return ViewUtil.render(request, model, Reference.Templates.IMAGE_UPLOAD,
				Reference.CommonStrings.NAME_IMAGEPROCESSING, "OK");
	};

	private static String getFileName(Part part) {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessing:START:getFileName");
		for (String cd : part.getHeader("content-disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				Tools.println("END:getFileName" + System.lineSeparator());
				return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		Tools.println("END:getFileName" + System.lineSeparator());
		return null;
	}
}
