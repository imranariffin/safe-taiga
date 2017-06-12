package app.controllers;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Filter;

import javax.servlet.*;
import javax.servlet.http.*;

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
		return "<form method='post' enctype='multipart/form-data'>"
				+ "    <input type='file' name='uploaded_file' accept='.png'>" + "    <button>Upload picture</button>"
				+ "</form>";
	};

	public static Route handleImageUpload = (Request request, Response response) -> {

		Path tempFile = Files.createTempFile(uploadDir.toPath(), "", "");

		request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

		try (InputStream input = request.raw().getPart("uploaded_file").getInputStream()) {
			Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
		}

		logInfo(request, tempFile);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("imagefile", tempFile.getFileName());
		// response.redirect("imageresult");
		return ViewUtil.render(request, model, Reference.Templates.DISPLAY_IMAGE,
				Reference.CommonStrings.IMAGEPROCESSING_NAME, "OK");
		// return "<h1>You uploaded this image:<h1><img src='" +
		// tempFile.getFileName() + "'>";

	};
}
