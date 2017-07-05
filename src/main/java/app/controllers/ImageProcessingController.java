package app.controllers;

import static app.Application.IMAGES_INPUT_DIR;
import static app.Application.IMAGES_OUTPUT_PARTITION_DIR;
import static app.Application.IMAGES_OUTPUT_GLOBALDIFFERENCE_DIR;
import static app.Application.IMAGES_OUTPUT_RESIZED_DIR;
import static app.Application.TEXT_OUTPUT_PARTITION_DIR;
import static app.Application.TEXT_OUTPUT_GLOBALDIFFERENCE_DIR;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import app.util.FileManager;
import app.util.ImageProcessing;
import app.util.Reference;
import app.util.ScriptManager;
import app.util.Tools;
import app.util.ViewUtil;
import app.util.AnimePanel;

import spark.Request;
import spark.Response;
import spark.Route;

public class ImageProcessingController {

	public static boolean BOOL_SCRIPT = true;
	public static boolean BOOL_MATCHING_NAME = false;

	public static Route serveImageUpload = (Request request, Response response) -> {
		Tools.println("\nFROM:ImageProcessingController:START:serveImageUpload");
		Map<String, Object> model = new HashMap<String, Object>();

		model.put("imagefile", "/images/other/image_placeholder.jpg");
		model.put("imagemessage", "your uploaded image will replace the empty image below:");
		model.put("partitionArrayRGB", new int[ImageProcessing.DIVISOR_VALUE][ImageProcessing.DIVISOR_VALUE][3]);

		Tools.println("END:serveImageUpload\n");
		return ViewUtil.render(request, model, Reference.Templates.IMAGE_PROCESSING, "Image Upload", "OK");
	};

	public static Route handleImageUpload = (Request request, Response response) -> {
		Tools.println("\nFROM:ImageProcessingController:START:handleImageUpload");
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
			return ViewUtil.renderErrorMessage(request, e.getMessage(), Reference.CommonStrings.LINK_IMAGEPROCESSING,
					Reference.CommonStrings.NAME_IMAGEPROCESSING);
		}

		/**
		 * Prepare required variables
		 */
		BufferedImage originalImage, resizedImage, partitionedImage, globalDifferenceImage;
		int[][][] partitioningRGBArray, globalDifferenceRGBArray;

		// Remove any file type post fix
		String filename = tempFile.getFileName().toString().substring(0,
				tempFile.getFileName().toString().length() - 4);

		// Files directory
		String savedImageDir = IMAGES_INPUT_DIR.toPath() + "/" + filename + ".png";
		String outputResizedImage = IMAGES_OUTPUT_RESIZED_DIR.toPath() + "/" + filename + ".png";

		String outputPartitionedImage = IMAGES_OUTPUT_PARTITION_DIR.toPath() + "/" + filename + ".png";
		String outputPartitionedtText = TEXT_OUTPUT_PARTITION_DIR.toPath() + "/" + filename + ".txt";

		String outputGlobalDifferenceImage = IMAGES_OUTPUT_GLOBALDIFFERENCE_DIR.toPath() + "/" + filename + ".png";
		String outputGlobalDifferenceText = TEXT_OUTPUT_GLOBALDIFFERENCE_DIR.toPath() + "/" + filename + ".txt";

		// Logging
		Tools.print("Uploaded file '" +

				getFileName(request.raw().getPart("uploaded_file")) + "' saved as '" + tempFile.toAbsolutePath() + "'"
				+ "\nbase filename:" + filename + "\ntemporary image is saved at:" + savedImageDir
				+ "\nresized image saved at:" + outputResizedImage + "\ntext created from partitioning saved at:"
				+ outputPartitionedtText + "\npartitioned image created at:" + outputPartitionedImage);

		originalImage = ImageIO.read(new File(savedImageDir));

		// Resize the image
		resizedImage = ImageProcessing.resizeImage(originalImage);

		// Save resized image for future inquiries
		ImageIO.write(resizedImage, "png", new File(outputResizedImage));

		/**
		 * PARTITION IMAGE
		 * 
		 * Divide the image into several equally sized boxes and find the
		 * average RGB values for each box then assign them to the box
		 */
		// Get resized image partitioning RGB array
		partitioningRGBArray = ImageProcessing.getPartitionArray(resizedImage);

		// Partition the resized image based on the partitioning array
		partitionedImage = ImageProcessing.getPartitionedBufferedImage(partitioningRGBArray);

		// Save the partitioned image for future inquiries
		ImageIO.write(partitionedImage, "png", new File(outputPartitionedImage));

		// Write partitioned image data to a text file
		// FileManager.log(Tools.convertTripleArrayToString(partitioningRGBArray),
		// outputPartitionedtText);

		/**
		 * GLOBAL DIFFERENCE
		 * 
		 * Find the global average RGB values of the image and take the
		 * difference of all individual RGB with the global average
		 */
		// Get resized image global difference RGB array
		globalDifferenceRGBArray = ImageProcessing.getGlobalDifferenceArray(resizedImage);

		// Get the BufferedImage from the global difference RGB array
		globalDifferenceImage = ImageProcessing.getBufferedImageGivenArray(globalDifferenceRGBArray);

		// Save the partitioned image for future inquiries
		ImageIO.write(globalDifferenceImage, "png", new File(outputGlobalDifferenceImage));

		// Write partitioned image data to a text file
		// FileManager.log(Tools.convertTripleArrayToString(globalDifferenceRGBArray),outputGlobalDifferenceText);
		/**
		 * Insert into database
		 */

		try (Connection connection = app.Application.getConnection()) {
			Statement stmt = connection.createStatement();

			/**
			 * Insert into database the image data sent by user
			 */
			// ImageProcessingManager.insertImageDataToDatabase(stmt,
			// request.ip(), partitioningRGBArray);

			/**
			 * RANDOMIZED SEARCH
			 * 
			 * Find matching boxes given a randomized boxes
			 * 
			 * This is good for cropped pictures
			 */
			ImageProcessingManager.findMatchingImageDataRandomized(stmt, model, partitioningRGBArray);

			/**
			 * RANDOMIZED SEARCH VERSION 2
			 * 
			 * Find a matching BOX given a randomized BOX, so we will start from
			 * a pixel, then iterate x -- > x + a and y --> y + a
			 * 
			 * This is also specially good for cropped pictures
			 */
			Tools.println("TEST 3");
			ImageProcessingManager.findMatchingImageDataRandomizedV2(stmt, model, partitioningRGBArray);
			/**
			 * INCREMENTAL SEARCH NON-RGB
			 * 
			 * Incremental search using non-RGB i.e. we search each RGB as 3
			 * separate 1-tuple
			 */
			Tools.println("TEST 3");
			ImageProcessingManager.findMatchingImageDataIncremental(stmt, model, partitioningRGBArray);
			/**
			 * INCREMENTAL SEARCH RGB
			 * 
			 * Incremental search using RGB i.e. we search matching RGB as
			 * 3-tuple
			 */
			Tools.println("TEST 4");
			ImageProcessingManager.findMatchingImageDataIncrementalRGB(stmt, model, partitioningRGBArray);

		} catch (SQLException e) {
			e.printStackTrace();
			return ViewUtil.renderErrorMessage(request, e.getMessage(), Reference.CommonStrings.LINK_IMAGEPROCESSING,
					Reference.CommonStrings.NAME_IMAGEPROCESSING);
		}

		if (partitioningRGBArray == null) {
			throw new NullPointerException("partitionArrayRGB is null");
		} else {
			model.put("partitionArrayRGB", partitioningRGBArray);
		}
		model.put("ORIGINAL_IMAGE_MESSAGE", "The original image");
		// 7 to remove the substring 'public/'
		Tools.println("original image directory:" + savedImageDir);
		model.put("ORIGINAL_IMAGE_FILE", savedImageDir.substring(7, savedImageDir.length()));

		Tools.println("END:handleImageUpload\n");
		return ViewUtil.render(request, model, Reference.Templates.IMAGE_UPLOAD,
				Reference.CommonStrings.NAME_IMAGEPROCESSING, "OK");
	};

	public static String printTrueFileName(String givenFileName) {
		Tools.print("");
		for (int a = givenFileName.length(); a > 0; a--) {
			Tools.print(givenFileName.charAt(a) + "");
		}
		return "";
	}

	private static String getFileName(Part part) {
		for (String cd : part.getHeader("content-disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}
}
