package app.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.FrameGrabber.Exception;

public class Tools {

	public static AnimeObject[] animeArray = new AnimeObject[] { new AnimeObject("yuruyuri-season1", 12),
			new AnimeObject("yuruyuri-season2", 12), new AnimeObject("yuruyuri-season3", 12),
			new AnimeObject("codegeass-season2", 25), new AnimeObject("codegeass-season1", 25) };
	public final static String SAFE_STRING = "X1U8N2YTR87134678V349T9V3841CM89XY4398V";
	public static boolean devmode = true;

	public static void println(String text) {
		if (devmode) {
			System.out.println(text);
		}
	}

	public static void println(String text, boolean bool) {
		if (bool) {
			System.out.println(text);
		}
	}

	public static void print(String text) {
		if (devmode) {
			System.out.print(text);
		}
	}

	public static void print(String text, boolean bool) {
		if (bool) {
			System.out.print(text);
		}
	}

	public static String convertTripleArrayToString(int[][][] tripleArray) {
		/**
		 * create string for the RGBs
		 */

		String script = "'{";
		for (int a = 0; a < tripleArray.length; a++) { // y-axis
			script += "{";
			for (int b = 0; b < tripleArray[a].length; b++) { // x-axis
				script += "{";
				for (int c = 0; c < tripleArray[a][b].length; c++) {
					script += tripleArray[a][b][c];
					if (c < (tripleArray[a][b].length - 1)) {
						script += ",";
					}
				}
				if (b < (tripleArray[a].length - 1)) {
					script += "},";
				} else {
					script += "}";
				}
			}
			if (a < (tripleArray.length - 1)) {
				script += "},";
			} else {
				script += "}";
			}
		}
		script += "}'";
		return script;
	}

	public static void InsertTextDumpToDatabase() {

		String insertScript = "";
		int[][][] partitionArrayRGB = null;

		Tools.println("beginning to insert " + animeArray.length + " anime into the database");

		for (int animeNumber = 0; animeNumber < animeArray.length; animeNumber++) {
			Tools.println("animeNumber:" + animeNumber);
			try {
				int[] tmpPanels = new int[animeArray[animeNumber].getNumberOfEpisodes()];
				for (int a = 1; a <= animeArray[animeNumber].getNumberOfEpisodes(); a++) {
					Tools.println("a:" + a);
					tmpPanels[a - 1] = Integer.valueOf(FileManager.readFile(
							"dev_output/description/" + animeArray[animeNumber].getName() + "_" + a + ".txt"));
				}
				animeArray[animeNumber].setPanels(tmpPanels);
			} catch (IOException e) {
				Tools.println("FAIL READING DESCRIPTION TEXT");
				Tools.println(e.getMessage());
			}
			for (int episodeNumber = 1; episodeNumber <= animeArray[animeNumber]
					.getNumberOfEpisodes(); episodeNumber++) {
				Tools.println("episodeNumber:" + episodeNumber);
				Tools.println("panelNumbers: " + animeArray[animeNumber].getPanels()[episodeNumber - 1]);
				for (int panelNumber = 0; panelNumber < animeArray[animeNumber].getPanels()[episodeNumber
						- 1]; panelNumber++) {
					try (Connection connection = app.Application.getConnection()) {

						Statement stmt = connection.createStatement();

						partitionArrayRGB = FileManager.parsePartitionTextOutput("dev_output/text/"
								+ animeArray[animeNumber].getName() + "/" + animeArray[animeNumber].getName() + "_"
								+ episodeNumber + "_" + panelNumber + ".txt");

						insertScript = ScriptCreator.insertIntoImagedbAnimeRgb(animeArray[animeNumber].getName(),
								episodeNumber, panelNumber, partitionArrayRGB);

						Tools.println("Executing script:" + insertScript);
						stmt.executeUpdate(insertScript);

					} catch (IOException e) {
						Tools.println("id:" + panelNumber);
						Tools.println(e.getMessage());
					} catch (SQLException e) {
						Tools.println("id:" + panelNumber);
						Tools.println("query:" + insertScript);
						Tools.println(e.getMessage());
					} catch (URISyntaxException e) {
						Tools.println("id:" + panelNumber);
						Tools.println(e.getMessage());
					}
				}
			}
		}
	}

	public static void createImageDump() {
		try {
			/**
			 * Create required folders
			 */
			File DEV_OUTPUT_IMAGES_OUTPUT_PARTITION = new File("dev_output/images/output/partition");
			DEV_OUTPUT_IMAGES_OUTPUT_PARTITION.mkdirs();

			String animeName = "";
			for (int animeNumber = 0; animeNumber < animeArray.length; animeNumber++) {
				animeName = animeArray[animeNumber].getName();
				for (int episode = 1; episode <= animeArray[animeNumber].getNumberOfEpisodes(); episode++) {
					Java2DFrameConverter frameConverter = new Java2DFrameConverter();

					/**
					 * Iterators
					 */
					int i = 0; // the frame iterator
					int panel = 0; // the panel iterator
					int[][][] partitionArrayRGB; // int array for RGB
					String outputImageName; // name of the output partitioned
											// image
					String outputTextName; // name of the output partitioned
											// text
											// dump
					BufferedImage image; // the image

					Tools.println("begin parsing video");
					Frame frame;

					/**
					 * Create image and text folders
					 */

					File IMAGE_FOLDER = new File("dev_output/images/output/partition/" + animeName);
					IMAGE_FOLDER.mkdirs();
					File TEXT_FOLDER = new File("dev_output/text/" + animeName);
					TEXT_FOLDER.mkdirs();

					FFmpegFrameGrabber g = new FFmpegFrameGrabber(
							"videos/" + animeName + "/" + animeName + "_" + episode + ".mkv");
					g.start();
					while ((frame = g.grabImage()) != null) {
						if ((i % ImageProcessing.FRAME_SKIP) == 0) {
							image = frameConverter.getBufferedImage(frame);
							outputImageName = "dev_output/images/output/partition/" + animeName + "/" + animeName + "_"
									+ episode + "_" + panel + ".png";
							outputTextName = "dev_output/text/" + animeName + "/" + animeName + "_" + episode + "_"
									+ panel + ".txt";
							partitionArrayRGB = ImageProcessing.getImageRGBPartitionValues(image);
							ImageIO.write(ImageProcessing.partitionImage(ImageProcessing.resizeImage(image),
									partitionArrayRGB), "png", new File(outputImageName));
							FileManager.writeTripleArrayToString(partitionArrayRGB, outputTextName);
							Tools.println(outputImageName);
							panel++;
						} else {
							// do nothing
						}
						i++;
					}
					FileManager.writeStringToFile("" + panel,
							"dev_output/description/" + animeName + "_" + episode + ".txt");
					g.stop();
					Tools.println("end parsing video");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void createDatabases() {

		try (Connection connection = app.Application.getConnection()) {

			Statement stmt = connection.createStatement();

			/**
			 * CREATE TABLES FOR IMAGEPROCESSING
			 */
			// Create imagedb_anime_rgb table if not exist
			// Tools.println("Execute script:" +
			// ScriptCreator.DROP_IMAGEDB_ANIME_RGB);
			// stmt.executeUpdate(ScriptCreator.DROP_IMAGEDB_ANIME_RGB);
			// Tools.println("Execute script:" +
			// ScriptCreator.CREATE_IMAGEDB_ANIME_RGB);
			// stmt.executeUpdate(ScriptCreator.CREATE_IMAGEDB_ANIME_RGB);

			// Create imagedb_anime_rgb table if not exist
			// Tools.println("Execute script:" +
			// ScriptCreator.DROP_IMAGEDB_USER_IMAGE_REQUEST);
			// stmt.executeUpdate(ScriptCreator.DROP_IMAGEDB_USER_IMAGE_REQUEST);
			// Tools.println("Execute script:" +
			// ScriptCreator.CREATE_IMAGEDB_USER_IMAGE_REQUEST);
			// stmt.executeUpdate(ScriptCreator.CREATE_IMAGEDB_USER_IMAGE_REQUEST);

			/**
			 * DROP TABLES FOR TEXTBOARD
			 */
			// Drop table if exist
			Tools.println("Execute script:" + ScriptCreator.DROP_POSTS);
			stmt.executeUpdate(ScriptCreator.DROP_POSTS);

			// Drop table if exist
			Tools.println("Execute script:" + ScriptCreator.DROP_THREADS);
			stmt.executeUpdate(ScriptCreator.DROP_THREADS);

			// Drop table if exist
			Tools.println("Execute script:" + ScriptCreator.DROP_BOARDS);
			stmt.executeUpdate(ScriptCreator.DROP_BOARDS);
			/**
			 * CREATE TABLES FOR TEXTBOARD
			 */
			// Create imagedb_anime_rgb table if not exist
			Tools.println("Execute script:" + ScriptCreator.CREATE_BOARDS);
			stmt.executeUpdate(ScriptCreator.CREATE_BOARDS);

			// Create imagedb_anime_rgb table if not exist
			Tools.println("Execute script:" + ScriptCreator.CREATE_THREADS);
			stmt.executeUpdate(ScriptCreator.CREATE_THREADS);

			// Create imagedb_anime_rgb table if not exist
			Tools.println("Execute script:" + ScriptCreator.CREATE_POSTS);
			stmt.executeUpdate(ScriptCreator.CREATE_POSTS);

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static void getImageDbAverageRGB() {

		try (Connection connection = app.Application.getConnection()) {
			Statement stmt = connection.createStatement();
			Tools.println(ScriptCreator.selectAverageOfImageDb());
			ResultSet rs = stmt.executeQuery(ScriptCreator.selectAverageOfImageDb());

			rs.next();
			String averageOfRGB = "";
			String result = "";
			for (int a = 1; a <= ImageProcessing.DIVISOR_VALUE; a++) {
				for (int b = 1; b <= ImageProcessing.DIVISOR_VALUE; b++) {
					for (int c = 1; c <= 3; c++) {
						result = "{" + rs.getString("" + a + ":" + b + ":" + c) + "}";
						averageOfRGB += result;
						Tools.print(result);
					}
					averageOfRGB += System.lineSeparator();
					Tools.println("");
				}
			}
			FileManager.writeStringToFile(averageOfRGB, "dev_output/averageOfRGB.txt");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static void getImageDbMinMax() {
		try (Connection connection = app.Application.getConnection()) {
			Statement stmt = connection.createStatement();
			Tools.println(ScriptCreator.getMinMaxOfImageDb());
			ResultSet rs = stmt.executeQuery(ScriptCreator.getMinMaxOfImageDb());

			rs.next();
			String minMaxOfRGB = "";
			String result = "";
			for (int a = 1; a <= ImageProcessing.DIVISOR_VALUE; a++) {
				for (int b = 1; b <= ImageProcessing.DIVISOR_VALUE; b++) {
					for (int c = 1; c <= 3; c++) {
						result = "{" + rs.getString("MIN:" + a + ":" + b + ":" + c) + ","
								+ rs.getString("MAX:" + a + ":" + b + ":" + c) + "}";
						minMaxOfRGB += result;
						Tools.print(result);
					}
					minMaxOfRGB += System.lineSeparator();
					Tools.println("");
				}
			}
			FileManager.writeStringToFile(minMaxOfRGB, "dev_output/minMaxOfRGB.txt");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static void println(int nextInt) {
		println(Integer.toString(nextInt));
	}

	public static String convertToQuerySafe(String givenString) {
		String safeString = "";
		for (int a = 0; a < givenString.length(); a++) {
			if (givenString.charAt(a) == '\'') {
				safeString += SAFE_STRING;
			} else {
				safeString += givenString.charAt(a);
			}
		}
		return safeString;
	}

	public static String revertQuerySafeString(String safeString) {
		return safeString.replaceAll(SAFE_STRING, "'");
	}
}
