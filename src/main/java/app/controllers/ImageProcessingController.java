package app.controllers;

import static app.Application.IMAGES_INPUT_DIR;
import static app.Application.IMAGES_OUTPUT_PARTITION_DIR;
import static app.Application.IMAGES_OUTPUT_RESIZED_DIR;
import static app.Application.TEXT_OUTPUT_PARTITION_DIR;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
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
import app.util.ImagePanelData;

import spark.Request;
import spark.Response;
import spark.Route;

public class ImageProcessingController {

	public static boolean BOOL_SCRIPT = true;
	public static boolean BOOL_MATCHING_NAME = true;

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
		float[][][] partitioningRGBArray, globalDifferenceRGBArray;

		// Remove any file type post fix
		String filename = tempFile.getFileName().toString().substring(0,
				tempFile.getFileName().toString().length() - 4);

		// Files directory
		String savedImageDir = IMAGES_INPUT_DIR.toPath() + "/" + filename + ".png";
		String outputTextDir = TEXT_OUTPUT_PARTITION_DIR.toPath() + "/" + filename + ".txt";
		String outputResizedImage = IMAGES_OUTPUT_RESIZED_DIR.toPath() + "/" + filename + ".png";
		String outputPartitionedImage = IMAGES_OUTPUT_PARTITION_DIR.toPath() + "/" + filename + ".png";

		// Logging
		Tools.print("Uploaded file '" +

				getFileName(request.raw().getPart("uploaded_file")) + "' saved as '" + tempFile.toAbsolutePath() + "'"
				+ "\nbase filename:" + filename + "\ntemporary image is saved at:" + savedImageDir
				+ "\nresized image saved at:" + outputResizedImage + "\ntext created from partitioning saved at:"
				+ outputTextDir + "\npartitioned image created at:" + outputPartitionedImage);

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
		FileManager.writeStringToFile(Tools.convertTripleArrayToString(partitioningRGBArray), outputTextDir);

		/**
		 * GLOBAL DIFFERENCE
		 * 
		 * Find the global average RGB values of the image and take the
		 * difference of all individual RGB with the global average
		 */

		globalDifferenceRGBArray = ImageProcessing.getGlobalDifferenceArray(resizedImage);
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

			// Initialize variable for image search
			ImagePanelData tmpImagePanel;
			Map<String, ImagePanelData> matchResult;
			ImagePanelData[] values;

			Tools.println("TEST 1");
			String findMatchingImageDataRandomized = ScriptManager
					.findMatchingImageDataRandomized(partitioningRGBArray);
			Tools.println(findMatchingImageDataRandomized, BOOL_SCRIPT);

			ResultSet rs = stmt.executeQuery(findMatchingImageDataRandomized);

			matchResult = new HashMap<String, ImagePanelData>();
			while (rs.next()) {
				Tools.println("matching name:" + rs.getString("name") + " " + rs.getString("episode") + " "
						+ rs.getString("panel"), BOOL_MATCHING_NAME);
				tmpImagePanel = new ImagePanelData(rs.getString("name"), rs.getString("episode"),
						rs.getString("panel"));
				matchResult.put(tmpImagePanel.getKey(), tmpImagePanel);
			}

			if (matchResult.isEmpty()) {
				Tools.println("Test 1: None found");
				model.put("test_1_boolean", false);
			} else {
				Tools.println("Test 1: Found");
				model.put("test_1_boolean", true);
				values = new ImagePanelData[matchResult.size()];

				int index = 0;
				for (Map.Entry<String, ImagePanelData> mapEntry : matchResult.entrySet()) {
					values[index] = mapEntry.getValue();
					index++;
				}

				model.put("test_1_result", values); // Return a list of result,
													// since this is randomized
													// search specialized in
													// cropped
													// pictures, duplicates are
													// expected
			}

			/**
			 * RANDOMIZED SEARCH VERSION 2
			 * 
			 * Find a matching BOX given a randomized BOX, so we will start from
			 * a pixel, then iterate x -- > x + a and y --> y + a
			 * 
			 * This is also specially good for cropped pictures
			 */
			Tools.println("TEST 2");
			String findMatchingImageDataRandomizedV2 = ScriptManager
					.findMatchingImageDataRandomizedV2(partitioningRGBArray);
			Tools.println(findMatchingImageDataRandomizedV2, BOOL_SCRIPT);

			rs = stmt.executeQuery(findMatchingImageDataRandomizedV2);

			matchResult = new HashMap<String, ImagePanelData>();
			while (rs.next()) {
				Tools.println("matching name:" + rs.getString("name") + " " + rs.getString("episode") + " "
						+ rs.getString("panel"), BOOL_MATCHING_NAME);
				tmpImagePanel = new ImagePanelData(rs.getString("name"), rs.getString("episode"),
						rs.getString("panel"));
				matchResult.put(tmpImagePanel.getKey(), tmpImagePanel);
			}

			if (matchResult.isEmpty()) {
				Tools.println("Test 2: None found");
				model.put("test_2_boolean", false);
			} else {
				Tools.println("Test 2: Found");
				model.put("test_2_boolean", true);
				values = new ImagePanelData[matchResult.size()];

				int index = 0;
				for (Map.Entry<String, ImagePanelData> mapEntry : matchResult.entrySet()) {
					values[index] = mapEntry.getValue();
					index++;
				}

				model.put("test_2_result", values); // Return a list of result,
													// since this is randomized
													// search specialized in
													// cropped
													// pictures, duplicates are
													// expected
			}

			/**
			 * INCREMENTAL SEARCH NON-RGB
			 * 
			 * Incremental search using non-RGB i.e. we search each RGB as 3
			 * separate 1-tuple
			 */
			Tools.println("TEST 3");
			boolean test3Found = false;
			String findMatchingImageDataIncremental;
			matchResult = new HashMap<String, ImagePanelData>();

			for (int a = 0; a < ImageProcessing.DIVISOR_VALUE; a++) {
				for (int b = 0; b < ImageProcessing.DIVISOR_VALUE; b++) {
					for (int c = 0; c < 3; c++) {
						findMatchingImageDataIncremental = ScriptManager.findMatchingImageDataIncremental(a, b, c,
								partitioningRGBArray[a][b][c]);
						Tools.println("Execute Query:" + findMatchingImageDataIncremental, BOOL_SCRIPT);

						rs = stmt.executeQuery(findMatchingImageDataIncremental);

						while (rs.next()) {
							Tools.println("matching name:" + rs.getString("name") + " " + rs.getString("episode") + " "
									+ rs.getString("panel"), BOOL_MATCHING_NAME);
							ImagePanelData panelData = new ImagePanelData(rs.getString("name"), rs.getInt(2),
									rs.getInt(3));
							if (!(matchResult.containsKey(panelData.getKey()))) {
								matchResult.put(panelData.getKey(), panelData);
							} else {
								test3Found = true;
								matchResult.get(panelData.getKey()).incrementWeight();
							}

						}
					}
				}
			}

			if (!test3Found) {
				Tools.println("Test 3: None found");
				model.put("test_3_boolean", false);
			} else {
				Tools.println("Test 3: Found");
				model.put("test_3_boolean", true);
				values = new ImagePanelData[matchResult.size()];

				/**
				 * Convert map to array
				 */
				int index = 0;
				for (Map.Entry<String, ImagePanelData> mapEntry : matchResult.entrySet()) {
					values[index] = mapEntry.getValue();
					index++;
				}

				/**
				 * Find the image with the highest weight <-- can be further
				 * optimized by merging this process with above conversion
				 */
				int maxIndex = -1;
				int maxValue = -1;
				for (int a = 0; a < values.length; a++) {
					if (values[a].getWeight() > maxValue) {
						maxValue = values[a].getWeight();
						maxIndex = a;
					}
				}
				if (maxIndex != -1) {
					Tools.println(maxValue + " " + values[maxIndex].getKey());
					model.put("test_3_result", new ImagePanelData(values[maxIndex].getKey().split(":")[0],
							values[maxIndex].getKey().split(":")[1], values[maxIndex].getKey().split(":")[2]));
				} else {
					Tools.println("maxIndex is -1");
				}
			}

			/**
			 * INCREMENTAL SEARCH RGB
			 * 
			 * Incremental search using RGB i.e. we search matching RGB as
			 * 3-tuple
			 */
			Tools.println("TEST 4");
			boolean test4Found = false;
			String findMatchingImageDataIncrementalRGB;
			matchResult = new HashMap<String, ImagePanelData>();
			for (int a = 0; a < ImageProcessing.DIVISOR_VALUE; a++) {
				for (int b = 0; b < ImageProcessing.DIVISOR_VALUE; b++) {
					findMatchingImageDataIncrementalRGB = ScriptManager.findMatchingImageDataIncrementalRGB(a, b,
							partitioningRGBArray[a][b]);
					Tools.println("Execute Query:" + findMatchingImageDataIncrementalRGB, BOOL_SCRIPT);

					rs = stmt.executeQuery(findMatchingImageDataIncrementalRGB);

					while (rs.next()) {
						Tools.println("matching name:" + rs.getString("name") + " " + rs.getString("episode") + " "
								+ rs.getString("panel"), BOOL_MATCHING_NAME);
						String key = "" + rs.getString("name") + ":" + rs.getInt(2) + ":" + rs.getInt(3);
						if (!(matchResult.containsKey(key))) {
							matchResult.put(key,
									new ImagePanelData("" + rs.getString("name"), rs.getInt(2), rs.getInt(3)));
						} else {
							test4Found = true;
							matchResult.get(key).incrementWeight();
						}

					}
				}
			}

			if (!test4Found) {
				Tools.println("Test 4: None found");
				model.put("test_4_boolean", false);
			} else {
				Tools.println("Test 4: Found");
				model.put("test_4_boolean", true);
				values = new ImagePanelData[matchResult.size()];

				/**
				 * Convert map to array
				 */
				int index = 0;
				for (Map.Entry<String, ImagePanelData> mapEntry : matchResult.entrySet()) {
					values[index] = mapEntry.getValue();
					index++;
				}

				/**
				 * Find the image with the highest weight <-- can be further
				 * optimized by merging this process with above conversion
				 */
				int maxIndex = -1;
				int maxValue = -1;
				for (int a = 0; a < values.length; a++) {
					if (values[a].getWeight() > maxValue) {
						maxValue = values[a].getWeight();
						maxIndex = a;
					}
				}
				if (maxIndex != -1) {
					Tools.println(maxValue + " " + values[maxIndex].getKey());
					model.put("test_4_result", new ImagePanelData(values[maxIndex].getKey().split(":")[0],
							values[maxIndex].getKey().split(":")[1], values[maxIndex].getKey().split(":")[2]));
				} else {
					Tools.println("maxIndex is -1");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return ViewUtil.renderErrorMessage(request, e.getMessage(), Reference.CommonStrings.LINK_IMAGEPROCESSING,
					Reference.CommonStrings.NAME_IMAGEPROCESSING);
		}

		if (partitioningRGBArray == null) {
			throw new NullPointerException("partitionArrayRGB is null");
		} else {
			model.put("partitionArrayRGB", partitioningRGBArray);
		}
		model.put("imagefile", outputPartitionedImage.substring(7, outputPartitionedImage.length()));
		model.put("imagemessage", "partitioned image");

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
