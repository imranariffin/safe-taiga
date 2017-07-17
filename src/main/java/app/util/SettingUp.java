package app.util;

import java.awt.TextField;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import Algorithms.ImageHashing;

import org.bytedeco.javacv.FrameGrabber.Exception;

import app.imageprocessing.ImageProcessing;
import app.managers.FileManager;
import app.managers.ScriptManager;
import app.structure.AnimeObject;

public class SettingUp {

	public static class CheckPanelDifference {
		/**
		 * Initiate triggers
		 */
		public static boolean activeBool = false;
		public static boolean writeLogBool = false;

		/**
		 * Initiate required variables
		 */
		public static int[][][] oldArray;
		public static int[][][] newArray;
		public static boolean[][][] panelDifferenceArray;
		public static int[][][] panelDifferenceCountArray;
	}

	public static class Partition {
		/**
		 * Initiate triggers
		 */
		public static boolean activeBool = true;
		public static boolean writeLogBool = false;
		public static boolean writeToDatabase = false;
		public static boolean printBool = false;

		/**
		 * Initiate required variables
		 */
		public static int[][][] tripleArray; // float array for RGB
		public static String imageDir; // name of the output partitioned
		// image
		public static String textDir; // name of the output partitioned
		// text dump
	}

	public static class GlobalDifference {
		/**
		 * Initiate triggers
		 */
		public static boolean activeBool = false;
		public static boolean writeLogBool = false;

		/**
		 * Initiate required variables
		 */
		public static int[][][] tripleArray; // float array for RGB
		public static String imageDir; // name of the output global difference
										// image
		public static String textDir; // name of the output global difference
										// text dump
	}

	public static class BasicHistogramHash {
		/**
		 * Initiate triggers
		 */
		public static boolean activeBool = false;
		public static boolean writeLogBool = false;
	}

	public static class globalAverageRGB {
		/**
		 * Initiate triggers
		 */

		public static float[][][] average = new float[ImageProcessing.DIVISOR_VALUE][ImageProcessing.DIVISOR_VALUE][3];
	}

	public static AnimeObject[] animeArray = new AnimeObject[] { new AnimeObject("yuruyuri-season1", 1),
			new AnimeObject("yuruyuri-season2", 12), new AnimeObject("yuruyuri-season3", 12),
			new AnimeObject("codegeass-season2", 25), new AnimeObject("codegeass-season1", 25) };

	public static void insertPartitionDumpToDatabase() {

		String insertScript = "";
		int[][][] tripleArray;

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
				for (int panelNumber = 0; panelNumber < animeArray[animeNumber].getPanels()[episodeNumber
						- 1]; panelNumber++) {
					try (Connection connection = app.Application.getConnection()) {

						Statement stmt = connection.createStatement();

						tripleArray = FileManager.parseIntegerPartitionTextOutput("dev_output/text/"
								+ animeArray[animeNumber].getName() + "/" + animeArray[animeNumber].getName() + "_"
								+ episodeNumber + "_" + panelNumber + ".txt");

						insertScript = ScriptManager.insertIntoImagedbAnimeRgbInteger(animeArray[animeNumber].getName(),
								episodeNumber, panelNumber, tripleArray);

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

	public static void createImageInfo(TextField[][] textField) {
		try {
			/**
			 * Create required folders
			 */
			File DEV_OUTPUT_IMAGES_OUTPUT_PARTITION = new File("dev_output/images/output/partition");
			DEV_OUTPUT_IMAGES_OUTPUT_PARTITION.mkdirs();

			String animeName = "";
			int globalPanelCount = 1;
			for (int animeNumber = 0; animeNumber < animeArray.length; animeNumber++) {
				animeName = animeArray[animeNumber].getName();
				for (int episode = 1; episode <= animeArray[animeNumber].getNumberOfEpisodes(); episode++) {
					Java2DFrameConverter frameConverter = new Java2DFrameConverter();

					/**
					 * GLOBAL VARIABLES
					 */
					int frameIterator = 0; // the frame iterator
					int panelIterator = 0; // the panel iterator
					BufferedImage image; // the image
					Frame frame;

					Tools.println("begin parsing video");

					/**
					 * Create image and text folders
					 */

					File IMAGE_FOLDER = new File("dev_output/images/output/partition/" + animeName);
					IMAGE_FOLDER.mkdirs();
					File TEXT_FOLDER = new File("dev_output/text/partition/" + animeName);
					TEXT_FOLDER.mkdirs();

					File IMAGE_FOLDER_GLOBALDIFFERENCE = new File(
							"dev_output/images/output/globaldifference/" + animeName);
					IMAGE_FOLDER_GLOBALDIFFERENCE.mkdirs();
					File TEXT_FOLDER_GLOBALDIFFERENCE = new File("dev_output/text/globaldifference/" + animeName);
					TEXT_FOLDER_GLOBALDIFFERENCE.mkdirs();

					FFmpegFrameGrabber g = new FFmpegFrameGrabber(
							"videos/" + animeName + "/" + animeName + "_" + episode + ".mkv");
					g.start();
					while ((frame = g.grabImage()) != null) {
						if ((frameIterator % ImageProcessing.FRAME_SKIP) == 0) {
							// Get the BufferedImage from the frame
							image = frameConverter.getBufferedImage(frame);

							// resize image
							image = ImageProcessing.resizeImage(image);

							/**
							 * PARTITION IMAGE
							 */
							// Assign the location we want to save the image and
							// the text file
							if (Partition.activeBool) {
								// Get the partition RGB array of the image
								Partition.tripleArray = ImageProcessing.getPartitionArray(image);

								// Calculate the average and display it to the
								// GUI

								globalAverageRGB.average = Tools.getNewAverage(globalAverageRGB.average,
										Partition.tripleArray, globalPanelCount);

								for (int y = 0; y < globalAverageRGB.average.length; y++) {
									for (int x = 0; x < globalAverageRGB.average[y].length; x++) {
										String textString = "";
										for (int z = 0; z < 3; z++) {
											textString += Float.toString(globalAverageRGB.average[y][x][z]) + " ";
										}
										textField[y][x].setText(textString);
									}
								}
								if (Partition.writeToDatabase) {

									Tools.println("inserting:" + animeName + ":" + episode + ":" + panelIterator,
											Partition.printBool);
									try (Connection connection = app.Application.getConnection()) {
										Statement statement = connection.createStatement();
										statement.executeUpdate(ScriptManager.insertIntoImagedbAnimeRgbInteger(
												animeName, episode, panelIterator, Partition.tripleArray));
									} catch (SQLException e) {
										e.printStackTrace();
									} catch (URISyntaxException e) {
										e.printStackTrace();
									}
								}

								if (Partition.writeLogBool) {

									// Assign the location we want to save the
									// image and the text file
									Partition.imageDir = "dev_output/images/output/partition/" + animeName + "/"
											+ animeName + "_" + episode + "_" + panelIterator + ".png";
									Partition.textDir = "dev_output/text/partition/" + animeName + "/" + animeName + "_"
											+ episode + "_" + panelIterator + ".txt";

									ImageIO.write(ImageProcessing.getPartitionedBufferedImage(Partition.tripleArray),
											"png", new File(Partition.imageDir));

									// Write the text file
									FileManager.writeTripleArrayToString(Partition.tripleArray, Partition.textDir);
								}
							}

							/**
							 * GLOBAL DIFFERENCE
							 */
							if (GlobalDifference.activeBool) {
								// Get the partition RGB array of the image
								GlobalDifference.tripleArray = ImageProcessing.getGlobalDifferenceArray(image);

								// Write the image based on the partition RGB
								// array

								if (GlobalDifference.writeLogBool) {

									// Assign the location we want to save the
									// image and the text file
									GlobalDifference.imageDir = "dev_output/images/output/globaldifference/" + animeName
											+ "/" + animeName + "_" + episode + "_" + panelIterator + ".png";
									GlobalDifference.textDir = "dev_output/text/globaldifference/" + animeName + "/"
											+ animeName + "_" + episode + "_" + panelIterator + ".txt";

									ImageIO.write(
											ImageProcessing.getBufferedImageGivenArray(GlobalDifference.tripleArray),
											"png", new File(GlobalDifference.imageDir));

									// Write the text file
									FileManager.writeTripleArrayToString(GlobalDifference.tripleArray,
											GlobalDifference.textDir);
								}
							}

							/**
							 * BASIC HISTOGRAM HASHING
							 */
							if (BasicHistogramHash.activeBool) {
								try (Connection connection = app.Application.getConnection()) {
									Statement statement = connection.createStatement();
									// statement.executeUpdate(ScriptManager.insertBasicHistogramHash(animeName,
									// episode,panel,ImageHashing.basicHistogramHash(ImageHashing.getRGBHistogram(image))));
									statement.executeUpdate("INSERT INTO imagedb_test (hash) VALUES ('"
											+ ImageHashing.basicHistogramHash(ImageHashing.getRGBHistogram(image))
											+ "');");
								} catch (SQLException e) {
									e.printStackTrace();
								} catch (URISyntaxException e) {
									e.printStackTrace();
								}
							}

							if (CheckPanelDifference.activeBool) {

								if (panelIterator == 0) { // If this is the
															// first panel, then
															// simply assign the
															// array
									CheckPanelDifference.oldArray = ImageProcessing.getArrayFromBufferedImage(image);
									CheckPanelDifference.panelDifferenceCountArray = new int[CheckPanelDifference.oldArray.length][CheckPanelDifference.oldArray[0].length][3];
								} else {
									CheckPanelDifference.newArray = ImageProcessing.getArrayFromBufferedImage(image);
									CheckPanelDifference.panelDifferenceArray = ImageProcessing.checkArrayDifference(
											CheckPanelDifference.oldArray, CheckPanelDifference.newArray);
									// Iterate through the boolean array
									for (int y = 0; y < CheckPanelDifference.oldArray.length; y++) {
										for (int x = 0; x < CheckPanelDifference.oldArray[y].length; x++) {
											for (int z = 0; z < CheckPanelDifference.oldArray[y][x].length; z++) {
												if (CheckPanelDifference.panelDifferenceArray[y][x][z]) {
													CheckPanelDifference.panelDifferenceCountArray[y][x][z]++;
												}
											}
										}
									}
								}
							}

							globalPanelCount++;
							panelIterator++; // move to the next panel
						}
						frameIterator++; // move to the next frame
					}
					FileManager.log("" + panelIterator, "dev_output/description/" + animeName + "_" + episode + ".txt");
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
		Tools.println(System.lineSeparator() + "FROM:SettingUp:START:createDatabases");
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
			// ScriptManager.CREATE_IMAGEDB_ANIME_RGB_INTEGER);
			// stmt.executeUpdate(ScriptManager.CREATE_IMAGEDB_ANIME_RGB_INTEGER);

			// Tools.println("Execute script:" +
			// ScriptManager.CREATE_IMAGEDB_USER_IMAGE_REQUEST_BYTE);
			// stmt.executeUpdate(ScriptManager.CREATE_IMAGEDB_USER_IMAGE_REQUEST_BYTE);

			// Tools.println("Execute script:" +
			// ScriptManager.CREATE_IMAGEDB_ANIME_BASIC_HISTOGRAM_HASH);
			// stmt.executeUpdate(ScriptManager.CREATE_IMAGEDB_ANIME_BASIC_HISTOGRAM_HASH);

			// Tools.println("Execute script:" +
			// ScriptManager.CLEAR_IMAGEDB_ANIME_BASIC_HISTOGRAM_HASH);
			// stmt.executeUpdate(ScriptManager.CLEAR_IMAGEDB_ANIME_BASIC_HISTOGRAM_HASH);

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
			// Tools.println("Execute script:" + ScriptManager.DROP_POSTS);
			// stmt.executeUpdate(ScriptManager.DROP_POSTS);

			// Drop table if exist
			// Tools.println("Execute script:" + ScriptManager.DROP_THREADS);
			// stmt.executeUpdate(ScriptManager.DROP_THREADS);

			// Drop table if exist
			// Tools.println("Execute script:" + ScriptManager.DROP_BOARDS);
			// stmt.executeUpdate(ScriptManager.DROP_BOARDS);
			/**
			 * CREATE TABLES FOR TEXTBOARD
			 */
			// Create imagedb_anime_rgb table if not exist
			// Tools.println("Execute script:" + ScriptManager.CREATE_BOARDS);
			// stmt.executeUpdate(ScriptManager.CREATE_BOARDS);

			// Create imagedb_anime_rgb table if not exist
			// Tools.println("Execute script:" + ScriptManager.CREATE_THREADS);
			// stmt.executeUpdate(ScriptManager.CREATE_THREADS);

			// Create imagedb_anime_rgb table if not exist
			// Tools.println("Execute script:" + ScriptManager.CREATE_POSTS);
			// stmt.executeUpdate(ScriptManager.CREATE_POSTS);

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		Tools.println("END:createDatabases" + System.lineSeparator());
	}
}
