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
		Map<String, Object> model = new HashMap<String, Object>();
		return ViewUtil.render(request, model, Reference.Templates.IMAGE_UPLOAD, "Image Upload", "OK");
	};

	public static Route handleImageUpload = (Request request, Response response) -> {

		Path tempFile = Files.createTempFile(uploadDir.toPath(), "", ".png");

		request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

		try (InputStream input = request.raw().getPart("uploaded_file").getInputStream()) {
			Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
		}

		String FILENAME = tempFile.getFileName().toString();
		Tools.println("picture saved as:" + FILENAME);
		try {
			ImageIO.write(
					ImageProcessing.partitionImage(ImageProcessing.resizeImage(
							ImageIO.read(new File("src/main/resources/public/images/input/upload/" + FILENAME)))),
					"png", new File("src/main/resources/public/images/output/partition/" + FILENAME));
		} catch (IOException e) {

			e.printStackTrace();
		}

		logInfo(request, tempFile);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("imagefile", "/images/output/partition/" + FILENAME);
		return ViewUtil.render(request, model, Reference.Templates.DISPLAY_IMAGE,
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
