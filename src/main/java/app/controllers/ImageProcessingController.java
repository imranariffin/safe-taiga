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
		return "<h1>You uploaded this image:<h1><img src='/img/image.png'>";
		/**
		 * Tools.print("FROM:ImageProcessingController:START:serveImageUpload");
		 * Map<String, Object> model = new HashMap<>();
		 * 
		 * Tools.print("END:imageProcessingController"); return
		 * ViewUtil.render(request, model,
		 * Reference.Templates.IMAGE_PROCESSING_UPLOAD, "IMAGE UPLOAD", "OK");
		 **/
	};
}
