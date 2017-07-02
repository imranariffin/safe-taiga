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
import java.util.LinkedList;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import app.util.AnimeObject;
import app.util.FileManager;
import app.util.ImageProcessing;
import app.util.Reference;
import app.util.ScriptCreator;
import app.util.Tools;
import app.util.ViewUtil;
import app.util.ImagePanelData;

import spark.Request;
import spark.Response;
import spark.Route;

public class ImageProcessingController {

	public static boolean BOOL_SCRIPT = false;
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
		BufferedImage originalImage;
		int[][][] partitionArrayRGB = null;

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

		/**
		 * Reformat the image
		 */
		originalImage = ImageIO.read(new File(savedImageDir)); // already saved
																// above
		originalImage = ImageProcessing.resizeImage(originalImage);
		// saved resized image for future inquiries
		ImageIO.write(originalImage, "png", new File(outputResizedImage));

		// get image RGB array
		partitionArrayRGB = ImageProcessing.getImageRGBPartitionValues(originalImage);

		originalImage = ImageProcessing.partitionImage(originalImage, partitionArrayRGB);
		// saved partitioned image for future inquiries
		ImageIO.write(originalImage, "png", new File(outputPartitionedImage));

		// write image data to a text file
		FileManager.writeStringToFile(ImageProcessing.getStringFromTripleArray(partitionArrayRGB), outputTextDir);

		/**
		 * Insert into database
		 */

		try (Connection connection = app.Application.getConnection()) {
			Statement stmt = connection.createStatement();

			/**
			 * Insert into database the image data sent by user
			 */
			String requestIp = request.ip();
			Tools.println("userIp:" + requestIp);

			String insertIntoImageDbUserImageRequest = ScriptCreator.insertIntoImageDbUserImageRequest(requestIp,
					partitionArrayRGB);
			stmt.executeUpdate(insertIntoImageDbUserImageRequest);

			/**
			 * INITIALIZE VARIABLES
			 * 
			 * for image searching
			 */
			ImagePanelData tmpImagePanel;
			Map<String, ImagePanelData> matchResult;
			String tmpString;
			String[] keys;
			ImagePanelData[] values;

			/**
			 * RANDOMIZED SEARCH
			 * 
			 * Find matching boxes given a randomized boxes
			 * 
			 * This is good for cropped pictures
			 */
			Tools.println("TEST 1");
			String findMatchingImageDataRandomized = ScriptCreator.findMatchingImageDataRandomized(partitionArrayRGB);
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
			String findMatchingImageDataRandomizedV2 = ScriptCreator
					.findMatchingImageDataRandomizedV2(partitionArrayRGB);
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
						findMatchingImageDataIncremental = ScriptCreator.findMatchingImageDataIncremental(a, b, c,
								partitionArrayRGB[a][b][c]);
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
					findMatchingImageDataIncrementalRGB = ScriptCreator.findMatchingImageDataIncrementalRGB(a, b,
							partitionArrayRGB[a][b]);
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

		if (partitionArrayRGB == null) {
			throw new NullPointerException("partitionArrayRGB is null");
		} else {
			model.put("partitionArrayRGB", partitionArrayRGB);
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
