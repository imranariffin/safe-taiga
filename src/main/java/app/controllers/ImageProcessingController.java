package app.controllers;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Filter;

import javax.imageio.ImageIO;
import javax.servlet.*;
import javax.servlet.http.*;

import app.util.EasyFileReader;
import app.util.ImageProcessing;
import app.util.Reference;
import app.util.Tools;
import app.util.ViewUtil;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import static app.Application.*;

public class ImageProcessingController {

	public static Route serveImageUpload = (Request request, Response response) -> {
		Tools.println("FROM:ImageProcessingController:START:serveImageUpload");
		Map<String, Object> model = new HashMap<String, Object>();
		Tools.println("END:serveImageUpload");
		model.put("imagefile", "/images/other/image_placeholder.jpg");
		model.put("imagemessage", "your uploaded image will replace the empy image below:");
		model.put("partitionArrayRGB", new int[10][10][3]);
		return ViewUtil.render(request, model, Reference.Templates.IMAGE_UPLOAD, "Image Upload", "OK");
	};

	public static Route handleImageUpload = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<String, Object>();
		Tools.println("FROM:ImageProcessingController:START:handleImageUpload");

		Path tempFile = Files.createTempFile(IMAGES_INPUT_DIR.toPath(), "", ".png");

		request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

		try (InputStream input = request.raw().getPart("uploaded_file").getInputStream()) {
			Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
		}

		int[][][] partitionArrayRGB = null;
		String filename = tempFile.getFileName().toString().substring(0,
				tempFile.getFileName().toString().length() - 4);
		// Files directory
		String savedImageDir = IMAGES_INPUT_DIR.toPath() + "/" + filename + ".png";
		String outputTextDir = TEXT_OUTPUT_PARTITION_DIR.toPath() + "/" + filename + ".txt";
		String outputPartitionedImage = IMAGES_OUTPUT_PARTITION_DIR.toPath() + "/" + filename + ".png";

		// Logging
		Tools.print("Uploaded file '" + getFileName(request.raw().getPart("uploaded_file")) + "' saved as '"
				+ tempFile.toAbsolutePath() + "'" + "\nbase filename:" + filename + "\ntemporary image is saved at:"
				+ savedImageDir + "\ntext created from partitioning saved at:" + outputTextDir
				+ "\npartitioned image  created at:" + outputPartitionedImage);

		try {
			ImageIO.write(ImageProcessing
					.partitionImage(ImageProcessing.resizeImage(ImageIO.read(new File(savedImageDir))), outputTextDir),
					"png", new File(outputPartitionedImage));
		} catch (IOException e) {
			Tools.println("ERROR PARTITIONING IMAGE");
			Tools.println(e.getMessage());
		}

		try {
			partitionArrayRGB = EasyFileReader.parsePartitionTextOutput(outputTextDir);
		} catch (Exception e) {
			Tools.println("ERROR READING OUTPUT TEXT");
			Tools.println(e.getMessage());
		}

		if (partitionArrayRGB == null) {
			throw new NullPointerException("partitionArrayRGB is null");
		} else {
			model.put("partitionArrayRGB", partitionArrayRGB);
		}

		model.put("imagefile", outputPartitionedImage.substring(7, outputPartitionedImage.length()));
		model.put("imagemessage", "you uploaded this image:");
		Tools.println("END:handleImageUpload");
		return ViewUtil.render(request, model, Reference.Templates.IMAGE_UPLOAD,
				Reference.CommonStrings.IMAGEPROCESSING_NAME, "OK");
	};

	public static String getTrueFileName(String givenFileName) {
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
