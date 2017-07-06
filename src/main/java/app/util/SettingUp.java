package app.util;

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

import Managers.FileManager;
import Managers.ScriptManager;
import app.structure.AnimeObject;

public class SettingUp {

	public static boolean PARTITION = false;
	public static boolean GLOBALDIFFERENCE = false;
	
	public static AnimeObject[] animeArray = new AnimeObject[] { new AnimeObject("yuruyuri-season1", 12),
			new AnimeObject("yuruyuri-season2", 12), new AnimeObject("yuruyuri-season3", 12),
			new AnimeObject("codegeass-season2", 25), new AnimeObject("codegeass-season1", 25) };

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

						partitionArrayRGB = FileManager.parseIntegerPartitionTextOutput("dev_output/text/"
								+ animeArray[animeNumber].getName() + "/" + animeArray[animeNumber].getName() + "_"
								+ episodeNumber + "_" + panelNumber + ".txt");

						insertScript = ScriptManager.insertIntoImagedbAnimeRgb(animeArray[animeNumber].getName(),
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

	public static void createImageDumpFloat() {
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
					int i = 0; // the frame iterator
					int panel = 0; // the panel iterator
					BufferedImage image; // the image
					Frame frame;

					/**
					 * VARIABLES FOR IMAGE PARTITIONING
					 */
					int[][][] partitionRGBArray; // float array for RGB
					String outputImageName; // name of the output partitioned
											// image
					String outputTextName; // name of the output partitioned
											// text dump

					/**
					 * VARIABLES FOR IMAGE GLOBAL DIFFERENCE
					 */
					int[][][] globalDifferenceRGBArray; // float array for RGB
					String globalDifferenceOutputImageName; // name of the
															// output global
															// difference image
					String globalDifferenceOutputTextName; // name of the output
															// global difference
															// text dump

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
						if ((i % ImageProcessing.FRAME_SKIP) == 0) {

							// Get the BufferedImage from the frame
							image = frameConverter.getBufferedImage(frame);

							// resize image
							image = ImageProcessing.resizeImage(image);

							/**
							 * PARTITION IMAGE
							 */
							// Assign the location we want to save the image and
							// the text file
							if (PARTITION) {
								outputImageName = "dev_output/images/output/partition/" + animeName + "/" + animeName
										+ "_" + episode + "_" + panel + ".png";
								outputTextName = "dev_output/text/partition/" + animeName + "/" + animeName + "_"
										+ episode + "_" + panel + ".txt";

								// Get the partition RGB array of the image
								partitionRGBArray = ImageProcessing.getPartitionArray(image);

								// Write the image based on the partition RGB
								// array
								ImageIO.write(ImageProcessing.getPartitionedBufferedImage(partitionRGBArray), "png",
										new File(outputImageName));

								// Write the text file
								// FileManager.writeTripleArrayToString(partitionRGBArray,
								// outputTextName);
							}
							/**
							 * GLOBAL DIFFERENCE
							 */
							if (GLOBALDIFFERENCE) {
								// Assign the location we want to save the image
								// and
								// the text file
								globalDifferenceOutputImageName = "dev_output/images/output/globaldifference/"
										+ animeName + "/" + animeName + "_" + episode + "_" + panel + ".png";
								globalDifferenceOutputTextName = "dev_output/text/globaldifference/" + animeName + "/"
										+ animeName + "_" + episode + "_" + panel + ".txt";

								// Get the partition RGB array of the image
								globalDifferenceRGBArray = ImageProcessing.getGlobalDifferenceArray(image);

								// Write the image based on the partition RGB
								// array
								ImageIO.write(ImageProcessing.getBufferedImageGivenArray(globalDifferenceRGBArray),
										"png", new File(globalDifferenceOutputImageName));

								// Write the text file
								// FileManager.writeTripleArrayToString(globalDifferenceRGBArray,globalDifferenceOutputTextName);
							}

							/**
							 * BASIC HISTOGRAM HASHING
							 */
							if (true) {
								try (Connection connection = app.Application.getConnection()) {
									Statement statement = connection.createStatement();
									statement.executeUpdate(ScriptManager.insertBasicHistogramHash(animeName, episode,
											panel,
											ImageHashing.basicHistogramHash(ImageHashing.getRGBHistogram(image))));
								} catch (SQLException e) {
									e.printStackTrace();
								} catch (URISyntaxException e) {
									e.printStackTrace();
								}
							}

							panel++; // move to the next panel
						}
						i++;
					}
					FileManager.log("" + panel, "dev_output/description/" + animeName + "_" + episode + ".txt");
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
			Tools.println("Execute script:" + ScriptManager.CREATE_IMAGEDB_ANIME_RGB);
			stmt.executeUpdate(ScriptManager.CREATE_IMAGEDB_ANIME_RGB);

			Tools.println("Execute script:" + ScriptManager.CREATE_IMAGEDB_USER_IMAGE_REQUEST_BYTE);
			stmt.executeUpdate(ScriptManager.CREATE_IMAGEDB_USER_IMAGE_REQUEST_BYTE);

			Tools.println("Execute script:" + ScriptManager.CREATE_IMAGEDB_ANIME_BASIC_HISTOGRAM_HASH);
			stmt.executeUpdate(ScriptManager.CREATE_IMAGEDB_ANIME_BASIC_HISTOGRAM_HASH);
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
			Tools.println("Execute script:" + ScriptManager.DROP_POSTS);
			stmt.executeUpdate(ScriptManager.DROP_POSTS);

			// Drop table if exist
			Tools.println("Execute script:" + ScriptManager.DROP_THREADS);
			stmt.executeUpdate(ScriptManager.DROP_THREADS);

			// Drop table if exist
			Tools.println("Execute script:" + ScriptManager.DROP_BOARDS);
			stmt.executeUpdate(ScriptManager.DROP_BOARDS);
			/**
			 * CREATE TABLES FOR TEXTBOARD
			 */
			// Create imagedb_anime_rgb table if not exist
			Tools.println("Execute script:" + ScriptManager.CREATE_BOARDS);
			stmt.executeUpdate(ScriptManager.CREATE_BOARDS);

			// Create imagedb_anime_rgb table if not exist
			Tools.println("Execute script:" + ScriptManager.CREATE_THREADS);
			stmt.executeUpdate(ScriptManager.CREATE_THREADS);

			// Create imagedb_anime_rgb table if not exist
			Tools.println("Execute script:" + ScriptManager.CREATE_POSTS);
			stmt.executeUpdate(ScriptManager.CREATE_POSTS);

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		Tools.println("END:createDatabases" + System.lineSeparator());
	}
}
