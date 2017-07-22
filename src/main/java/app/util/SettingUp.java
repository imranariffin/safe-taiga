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
		public static boolean activeBool = false;
		public static boolean writeLogBool = false;

		public static int[][][] oldArray;
		public static int[][][] newArray;
		public static int[][][] panelDifferenceCountArray;

		public static boolean[][][] panelDifferenceArray;
	}

	public static class Partition {
		public static boolean activeBool = true;
		public static boolean writeLogBool = false;
		public static boolean writeToDatabase = true;
		public static boolean printBool = false;

		public static int[][][] tripleArray;

		public static String imageDir;
		public static String textDir;
	}

	public static class GlobalDifference {
		public static boolean activeBool = false;
		public static boolean writeLogBool = false;

		public static int[][][] tripleArray;

		public static String imageDir;
		public static String textDir;
	}

	public static class BasicHistogramHash {
		public static boolean activeBool = false;
		public static boolean writeLogBool = false;
	}

	public static class globalAverageRGB {
		public static float[][][] average = new float[ImageProcessing.DIVISOR_VALUE][ImageProcessing.DIVISOR_VALUE][3];
	}

	public static AnimeObject[] animeArray = new AnimeObject[] { new AnimeObject("eureka", 50) };

	public static void insertPartitionDumpToDatabase() {

		String insertScript = "";
		int[][][] tripleArray;

		Tools.println("beginning to insert " + animeArray.length + " anime into the database");

		for (int animeNumber = 0; animeNumber < animeArray.length; animeNumber++) {
			Tools.println("animeNumber:" + animeNumber);
			try {
				int[] tmpPanels = new int[animeArray[animeNumber].getNumberOfEpisodes()];
				for (int a = 1; a <= animeArray[animeNumber].getNumberOfEpisodes(); a++) {
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

	public static void createImageInfo() {
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
							image = frameConverter.getBufferedImage(frame);
							image = ImageProcessing.resizeImage(image);

							if (Partition.activeBool) {
								Partition.tripleArray = ImageProcessing.getPartitionArray(image);

								if (Partition.writeToDatabase) {
									Tools.println(animeName + " " + episode + " " + frameIterator);
									DatabaseManager.insertPartitionHash(animeName, episode, frameIterator,
											ImageHashing.partitionHash(Partition.tripleArray));
								}

								if (Partition.writeLogBool) {
									Partition.imageDir = "dev_output/images/output/partition/" + animeName + "/"
											+ animeName + "_" + episode + "_" + panelIterator + ".png";
									Partition.textDir = "dev_output/text/partition/" + animeName + "/" + animeName + "_"
											+ episode + "_" + panelIterator + ".txt";
								}
							}

							if (GlobalDifference.activeBool) {
								GlobalDifference.tripleArray = ImageProcessing.getGlobalDifferenceArray(image);

								if (GlobalDifference.writeLogBool) {
									GlobalDifference.imageDir = "dev_output/images/output/globaldifference/" + animeName
											+ "/" + animeName + "_" + episode + "_" + panelIterator + ".png";
									GlobalDifference.textDir = "dev_output/text/globaldifference/" + animeName + "/"
											+ animeName + "_" + episode + "_" + panelIterator + ".txt";
								}
							}

							/**
							 * BASIC HISTOGRAM HASHING
							 */
							if (BasicHistogramHash.activeBool) {
								try (Connection connection = app.Application.getConnection()) {
									Statement statement = connection.createStatement();
									statement.executeUpdate("INSERT INTO imagedb_test (hash) VALUES ('"
											+ ImageHashing.basicHistogramHash(ImageHashing.getRGBHistogram(image))
											+ "');");
								} catch (SQLException e) {
									e.printStackTrace();
								} catch (URISyntaxException e) {
									e.printStackTrace();
								}
							}

							/**
							 * CHECK PANEL DIFFERENCE
							 */
							if (CheckPanelDifference.activeBool) {

								if (panelIterator == 0) {
									CheckPanelDifference.oldArray = ImageProcessing.getArrayFromBufferedImage(image);
									CheckPanelDifference.panelDifferenceCountArray = new int[CheckPanelDifference.oldArray.length][CheckPanelDifference.oldArray[0].length][3];
								} else {
									CheckPanelDifference.newArray = ImageProcessing.getArrayFromBufferedImage(image);
									CheckPanelDifference.panelDifferenceArray = ImageProcessing.checkArrayDifference(
											CheckPanelDifference.oldArray, CheckPanelDifference.newArray);
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
							panelIterator++;
						}
						frameIterator++;
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
