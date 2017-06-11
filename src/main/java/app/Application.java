package app;

import spark.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.servlet.MultipartConfigElement;

import app.util.*;
import app.controllers.*;

import static spark.Spark.*;
import static spark.debug.DebugScreen.*;

public class Application {

	public static HikariDataSource DATA_SOURCE;
	public static HikariConfig config;
	public static boolean devmode = true;

	public static void main(String[] args) {
		enableDebugScreen();
		System.out.println("SERVER:START:" + Integer.valueOf(System.getenv("PORT")));
		port(Integer.valueOf(System.getenv("PORT")));

		// staticFileLocation("public");
		staticFiles.externalLocation("upload");
		
		// staticFiles.externalLocation("public");
		config = new HikariConfig();
		config.setJdbcUrl(System.getenv("JDBC_DATABASE_URL"));
		DATA_SOURCE = (config.getJdbcUrl() != null) ? new HikariDataSource(config) : new HikariDataSource();

		// Set up before-filters (called before each get/post)
		before("*", Filters.addTrailingSlashes);

		/**
		 * GET ROUTES
		 */
		// get(Path.Web.LOGIN, LoginController.serveLoginPage);
		get(Reference.Web.ROOT, RootController.serveRootPage);
		get(Reference.Web.TEXTBOARD, TextboardController.serveTextboard_HOME);
		get(Reference.Web.TEXTBOARD_BOARD, TextboardController.serveTextboard_BOARD);
		get(Reference.Web.TEXTBOARD_BOARD_THREAD, TextboardController.serveTextboard_THREAD);
		// get(Reference.Web.IMAGE_PROCESSING,
		// ImageProcessingController.serveImageUpload);
		get(Reference.Web.IMAGE_PROCESSING,
				(req, res) -> "<form method='post' enctype='multipart/form-data'>"
						+ "    <input type='file' name='uploaded_file' accept='.png'>"
						+ "    <button>Upload picture</button>" + "</form>");
		/**
		 * POST ROUTES
		 */
		post(Reference.Web.TEXTBOARD, TextboardController.handleCreateBoard);
		post(Reference.Web.TEXTBOARD_BOARD, TextboardController.handleCreateThread);
		post(Reference.Web.TEXTBOARD_BOARD_THREAD, TextboardController.handleCreatePost);
		// post(Reference.Web.IMAGE_PROCESSING,
		// ImageProcessingController.handleImageUpload);
		post(Reference.Web.IMAGE_PROCESSING, (req, res) -> {
			// File uploadDir = new
			// File("src/main/resources/public/images/input/upload/");
			File uploadDir = new File("upload");
			uploadDir.mkdir();
			// exist
			Path tempFile = Files.createTempFile(uploadDir.toPath(), "", ".png");

			req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

			try (InputStream input = req.raw().getPart("uploaded_file").getInputStream()) {
				Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
			}
			Tools.print("image name:" + tempFile.getFileName().toString());
			return "<h1>You uploaded this image:<h1><img src='" + tempFile.getFileName() + "'>";
		});
		/**
		 * NOT FOUND
		 */
		get("*", ViewUtil.notFound);

		// Set up after-filters (called after each get/post)
		after("*", Filters.addGzipHeader);
		System.out.println("SERVER:END");
	}
}
