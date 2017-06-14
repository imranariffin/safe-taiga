package app.controllers;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Filter;

import javax.imageio.ImageIO;
import javax.servlet.*;
import javax.servlet.http.*;

import app.util.ImageProcessing;
import app.util.Reference;
import app.util.ViewUtil;
import app.util.Tools;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import static app.Application.*;

public class ImageProcessingController {

	// methods used for logging
	private static void logInfo(Request request, Path tempFile) throws IOException, ServletException {
		Tools.print("Uploaded file '" + getFileName(request.raw().getPart("uploaded_file")) + "' saved as '"
				+ tempFile.toAbsolutePath() + "'");
	}

	private static String getFileName(Part part) {
		for (String cd : part.getHeader("content-disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}

	public static Route serveImageUpload = (Request request, Response response) -> {
		Tools.println("FROM:ImageProcessingController:START:serveImageUpload");
		Map<String, Object> model = new HashMap<String, Object>();
		Tools.println("END:serveImageUpload");
		model.put("imagefile", "/images/other/image_placeholder.jpg");
		model.put("imagemessage", "your uploaded image will replace the empy image below:");
		return ViewUtil.render(request, model, Reference.Templates.IMAGE_UPLOAD, "Image Upload", "OK");
	};

	public static Route handleImageUpload = (Request request, Response response) -> {
		Tools.println("FROM:ImageProcessingController:START:handleImageUpload");

		Path tempFile = Files.createTempFile(uploadDir.toPath(), "", ".png");

		request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

		try (InputStream input = request.raw().getPart("uploaded_file").getInputStream()) {
			Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
		}

		String filename = tempFile.getFileName().toString();
		Tools.println("picture saved as:" + filename);
		try {
			ImageIO.write(
					ImageProcessing.partitionImage(ImageProcessing
							.resizeImage(ImageIO.read(new File("public/images/input/" + filename))), filename),
					"png", new File("public/images/output/partition/" + filename));
		} catch (IOException e) {

			e.printStackTrace();
		}

		logInfo(request, tempFile);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("imagefile", "/images/output/partition/" + filename);
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
}
