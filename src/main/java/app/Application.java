package app;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;
import static spark.debug.DebugScreen.enableDebugScreen;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import app.controllers.ImageProcessingController;
import app.controllers.RootController;
import app.controllers.TextboardController;
import app.util.FileManager;
import app.util.Reference;
import app.util.ScriptCreator;
import app.util.Tools;
import app.util.ViewUtil;

public class Application {

	public static HikariDataSource DATA_SOURCE;
	public static HikariConfig config;
	public static boolean devmode = true;

	public static File IMAGES_INPUT_DIR, IMAGES_OTHER_DIR, IMAGES_OUTPUT_PARTITION_DIR, TEXT_OUTPUT_PARTITION_DIR,
			IMAGES_OUTPUT_RESIZED_DIR;

	public static void main(String[] args) {
		enableDebugScreen();

		Tools.print("SERVER:START:" + Integer.valueOf(System.getenv("PORT")));
		try {
			port(Integer.valueOf(System.getenv("PORT")));
		} catch (Exception e) {
			Tools.println("System running locally, setting port to the default 5000");
			port(5000);
		}

		IMAGES_OTHER_DIR = new File("public/images/other");
		IMAGES_OUTPUT_PARTITION_DIR = new File("public/images/output/partition");
		IMAGES_OUTPUT_RESIZED_DIR = new File("public/images/output/resized");
		TEXT_OUTPUT_PARTITION_DIR = new File("public/texts/output/partition");
		IMAGES_INPUT_DIR = new File("public/images/input");

		IMAGES_OTHER_DIR.mkdirs();
		IMAGES_OUTPUT_PARTITION_DIR.mkdirs();
		IMAGES_OUTPUT_RESIZED_DIR.mkdirs();
		TEXT_OUTPUT_PARTITION_DIR.mkdirs();
		IMAGES_INPUT_DIR.mkdirs();

		staticFiles.externalLocation("public");
		staticFiles.expireTime(600L);

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
		InsertTextDumpToDatabase();
		System.out.println("SERVER:END");
	}

	public static void InsertTextDumpToDatabase() {
		String baseFilename = "idolmaster_1-";
		String fileType = ".txt";
		String insertScript = "";
		int[][][] partitionArrayRGB = null;
		for (int id = 0; id < 482; id++) {
			try (Connection connection = DATA_SOURCE.getConnection()) {
				partitionArrayRGB = FileManager
						.parsePartitionTextOutput("dev_output/text/" + baseFilename + id + fileType);

				insertScript = ScriptCreator.INSERT_INTO_imagedb_partition_rgb(baseFilename, partitionArrayRGB);

				Statement stmt = connection.createStatement();
				Tools.println("Executing script:" + insertScript);
				stmt.executeUpdate(insertScript);

			} catch (IOException e) {
				Tools.println("id:" + id);
				Tools.println(e.getMessage());
			} catch (SQLException e) {
				Tools.println("id:" + id);
				Tools.println("query:" + insertScript);
				Tools.println(e.getMessage());
			}
		}
	}
}
