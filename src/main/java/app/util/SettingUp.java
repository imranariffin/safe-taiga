package app.util;

import java.awt.TextField;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.FrameGrabber.Exception;

import app.managers.FileManager;
import app.managers.DatabaseManager;
import app.structure.AnimeObject;
import processing.ImageHashing;
import processing.ImageProcessing;

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

						insertScript = ScriptCreator.insertIntoImagedbAnimeRgbInteger(animeArray[animeNumber].getName(),
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
										statement.executeUpdate(ScriptCreator.insertIntoImagedbAnimeRgbInteger(
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

									// ImageIO.write(ImageProcessing.getPartitionedBufferedImage(Partition.tripleArray),
									// "png", new File(Partition.imageDir));

									// Write the text file
									// FileManager.writeTripleArrayToString(Partition.tripleArray,
									// Partition.textDir);
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

									// ImageIO.write(ImageProcessing.getBufferedImageGivenArray(GlobalDifference.tripleArray),
									// "png", new File(GlobalDifference.imageDir));

									// Write the text file
									// FileManager.writeTripleArrayToString(GlobalDifference.tripleArray,
									// GlobalDifference.textDir);
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

							globalPanelCount++; // move to the next panel iterator
							panelIterator++; // move to the next panel value
						}
						frameIterator++; // move to the next frame
					}
					FileManager.log("" + panelIterator, "dev_output/description/" + animeName + "_" + episode + ".txt");
					g.stop();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void prepareDatabase() {
		try {
			DatabaseManager.createTableBoards();
			DatabaseManager.createTableThreads();
			DatabaseManager.createTablePosts();
		} catch (SQLException | URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
