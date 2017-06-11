package app.controllers;

import spark.*;
import javax.servlet.*;
import javax.servlet.http.*;

import app.util.Reference;
import app.util.ViewUtil;
import app.util.Tools;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

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
		Tools.print("FROM:ImageProcessingController:START:serveImageUpload");
		Map<String, Object> model = new HashMap<>();

		Tools.print("END:imageProcessingController");
		return ViewUtil.render(request, model, Reference.Templates.IMAGE_PROCESSING_UPLOAD, "IMAGE UPLOAD", "OK");
	};

	public static Route handleImageUpload = (Request request, Response response) -> {
		Tools.print("FROM:ImageProcessingController:START:handleImageUpload");
		Map<String, Object> model = new HashMap<>();

		File uploadDir = new File("src/main/resources/public/images/input/upload/");
		uploadDir.mkdir(); // create the upload directory if it doesn't exist
		Path tempFile = Files.createTempFile(uploadDir.toPath(), "", ".png");

		request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

		try (InputStream input = request.raw().getPart("uploaded_file").getInputStream()) {
			Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
			Tools.print("finished copying the image file");
		}

		logInfo(request, tempFile);
		Tools.print("image name:" + tempFile.getFileName().toString());
		model.put("imagename", tempFile.getFileName());
		Tools.print("END:handleImageUpload");
		// return "<h1>You uploaded this image:<h1><img
		// src='/images/input/upload/" + tempFile.getFileName().toString() +
		// "'>";
		return "<h1>You uploaded this image:<h1><img src='/images/input/upload/image.png'>";
		//return "<h1>You uploaded this image:<h1><img src='/img/image.png'>";
		// return ViewUtil.render(request, model,
		// Reference.Templates.DISPLAY_IMAGE, "IMAGE DISPLAY", "OK");
	};
}
