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
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import app.util.FileManager;
import app.util.ImageProcessing;
import app.util.Reference;
import app.util.ScriptCreator;
import app.util.Tools;
import app.util.ViewUtil;
import spark.Request;
import spark.Response;
import spark.Route;

public class ImageProcessingController {

	public static Route serveImageUpload = (Request request, Response response) -> {
		Tools.println("\nFROM:ImageProcessingController:START:serveImageUpload");
		Map<String, Object> model = new HashMap<String, Object>();
		Tools.println("END:serveImageUpload");
		model.put("imagefile", "/images/other/image_placeholder.jpg");
		model.put("imagemessage", "your uploaded image will replace the empty image below:");
		model.put("partitionArrayRGB", new int[ImageProcessing.DIVISOR_VALUE][ImageProcessing.DIVISOR_VALUE][3]);
		return ViewUtil.render(request, model, Reference.Templates.IMAGE_UPLOAD, "Image Upload", "OK");
	};

	public static Route handleImageUpload = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<String, Object>();
		Tools.println("\nFROM:ImageProcessingController:START:handleImageUpload");

		Path tempFile = Files.createTempFile(IMAGES_INPUT_DIR.toPath(), "", ".png");

		request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

		try (InputStream input = request.raw().getPart("uploaded_file").getInputStream()) {
			Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e){
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
		Tools.print("Uploaded file '" + getFileName(request.raw().getPart("uploaded_file")) + "' saved as '"
				+ tempFile.toAbsolutePath() + "'" + "\nbase filename:" + filename + "\ntemporary image is saved at:"
				+ savedImageDir + "\nresized image saved at:" + outputResizedImage
				+ "\ntext created from partitioning saved at:" + outputTextDir + "\npartitioned image created at:"
				+ outputPartitionedImage);

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
			 * Create image database if not exist
			 */
			Tools.println("Executing script:" + ScriptCreator.CREATE_IMAGEDB_USER_IMAGE_REQUEST);
			stmt.executeUpdate(ScriptCreator.CREATE_IMAGEDB_USER_IMAGE_REQUEST);

			/**
			 * Insert into database the image data sent by user
			 */
			String insertIntoImageDbUserImageRequest = ScriptCreator.insertIntoImageDbUserImageRequest(request.ip(),
					partitionArrayRGB);
			Tools.println("Executing script:" + insertIntoImageDbUserImageRequest);
			stmt.executeUpdate(insertIntoImageDbUserImageRequest);
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
		model.put("imagefile", outputPartitionedImage.substring(7, outputPartitionedImage.length())); // remove
																										// 'public/'
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
