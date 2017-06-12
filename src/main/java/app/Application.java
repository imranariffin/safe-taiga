package app;

import spark.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;

import app.util.*;
import app.controllers.*;

import static spark.Spark.*;
import static spark.debug.DebugScreen.*;

public class Application {

	public static HikariDataSource DATA_SOURCE;
	public static HikariConfig config;
	public static boolean devmode = true;

	public static File uploadDir;

	public static void main2(String[] args) {
		enableDebugScreen();

		Tools.print("SERVER:START:" + Integer.valueOf(System.getenv("PORT")));
		port(Integer.valueOf(System.getenv("PORT")));

		uploadDir = new File("src/main/resources/public/images/input/upload");
		Tools.println("creating directory if it does not exist");
		uploadDir.mkdir(); // create the upload directory if it doesn't exist

		staticFiles.location("/public");
		staticFiles.externalLocation("src/main/resources/public");

		config = new HikariConfig();
		config.setJdbcUrl(System.getenv("JDBC_DATABASE_URL"));
		DATA_SOURCE = (config.getJdbcUrl() != null) ? new HikariDataSource(config) : new HikariDataSource();

		// Set up before-filters (called before each get/post)
		// before("*", Filters.addTrailingSlashes);

		/**
		 * GET ROUTES
		 */
		get(Reference.Web.ROOT, RootController.serveRootPage);
		get(Reference.Web.TEXTBOARD, TextboardController.serveTextboard_HOME);
		get(Reference.Web.TEXTBOARD_BOARD, TextboardController.serveTextboard_BOARD);
		get(Reference.Web.TEXTBOARD_BOARD_THREAD, TextboardController.serveTextboard_THREAD);
		get(Reference.Web.IMAGEPROCESSING, ImageProcessingController.serveImageUpload);

		/**
		 * POST ROUTES
		 */
		post(Reference.Web.TEXTBOARD, TextboardController.handleCreateBoard);
		post(Reference.Web.TEXTBOARD_BOARD, TextboardController.handleCreateThread);
		post(Reference.Web.TEXTBOARD_BOARD_THREAD, TextboardController.handleCreatePost);
		post(Reference.Web.IMAGEPROCESSING, ImageProcessingController.handleImageUpload);
		/**
		 * NOT FOUND
		 */
		get("*", ViewUtil.notFound);

		// Set up after-filters (called after each get/post)
		// after("*", Filters.addGzipHeader);
		System.out.println("SERVER:END");
	}

	public static void main(String[] args) {
		enableDebugScreen();

		Tools.print("SERVER:START:" + Integer.valueOf(System.getenv("PORT")));
		port(Integer.valueOf(System.getenv("PORT")));

		File uploadDir = new File("upload");
		uploadDir.mkdir(); // create the upload directory if it doesn't exist

		staticFiles.externalLocation("upload");

		get("/", (req, res) -> "<form method='post' enctype='multipart/form-data'>"
				+ "    <input type='file' name='uploaded_file' accept='.png'>" + "    <button>Upload picture</button>"
				+ "</form>");

		post("/", (req, res) -> {

			Path tempFile = Files.createTempFile(uploadDir.toPath(), "", "");

			req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

			try (InputStream input = req.raw().getPart("uploaded_file").getInputStream()) {
				Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
			}

			logInfo(req, tempFile);
			return "<h1>You uploaded this image:<h1><img src='" + tempFile.getFileName() + "'>";

		});

	}

	// methods used for logging
	private static void logInfo(Request req, Path tempFile) throws IOException, ServletException {
		System.out.println("Uploaded file '" + getFileName(req.raw().getPart("uploaded_file")) + "' saved as '"
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

}
