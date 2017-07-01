package app;

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
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;

import app.util.ImageProcessing;
import app.util.ScriptCreator;
import app.util.Tools;
import app.util.AnimeObject;
import app.util.FileManager;

public class SettingUp {

	// new AnimeObject("yuruyuri-season2", 12),
	private static AnimeObject[] animeArray = new AnimeObject[] { new AnimeObject("idolmaster", 25) };

	public static void main(String[] args) {
		try {
			createImageDump();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// getImageDbAverageRGB();
		// getImageDbMinMax();
	}

	public static void createImageDump() throws IOException, Exception {

		/**
		 * Create required folders
		 */
		File DEV_OUTPUT_IMAGES_OUTPUT_PARTITION = new File("dev_output/images/output/partition");
		DEV_OUTPUT_IMAGES_OUTPUT_PARTITION.mkdirs();

		String animeName = "";
		for (int animeNumber = 0; animeNumber < animeArray.length; animeNumber++) {
			animeName = animeArray[animeNumber].getName();
			for (int episode = 1; episode <= animeArray[animeNumber].getNumberOfEpisodes(); episode++) {
				FFmpegFrameGrabber g = new FFmpegFrameGrabber(
						"videos/" + animeName + "/" + animeName + "_" + episode + ".mkv");
				g.start();
				Java2DFrameConverter frameConverter = new Java2DFrameConverter();

				/**
				 * Setting variables
				 */
				int frameSkip = 72; // determines how many frames we want to
									// skip

				/**
				 * Iterators
				 */
				int i = 0; // the frame iterator
				int panel = 0; // the panel iterator
				int[][][] partitionArrayRGB; // int array for RGB
				String outputImageName; // name of the output partitioned image
				String outputTextName; // name of the output partitioned text
										// dump
				BufferedImage image; // the image

				Tools.println("begin parsing video");
				Frame frame;
				while ((frame = g.grabImage()) != null) {
					if ((i % frameSkip) == 0) {
						image = frameConverter.getBufferedImage(frame);
						outputImageName = "dev_output/images/output/partition/" + animeName + "/" + animeName + "_"
								+ episode + "_" + panel + ".png";
						outputTextName = "dev_output/text/" + animeName + "/" + animeName + "_" + episode + "_" + panel
								+ ".txt";
						partitionArrayRGB = ImageProcessing.getImageRGBPartitionValues(image);
						ImageIO.write(
								ImageProcessing.partitionImage(ImageProcessing.resizeImage(image), partitionArrayRGB),
								"png", new File(outputImageName));
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
	}

	public static void getImageDbAverageRGB() {

		try (Connection connection = app.Application.getConnection()) {
			Statement stmt = connection.createStatement();
			Tools.println(ScriptCreator.selectAverageOfImageDb());
			ResultSet rs = stmt.executeQuery(ScriptCreator.selectAverageOfImageDb());

			rs.next();
			String averageOfRGB = "";
			String result = "";
			for (int a = 1; a <= 10; a++) {
				for (int b = 1; b <= 10; b++) {
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
			for (int a = 1; a <= 10; a++) {
				for (int b = 1; b <= 10; b++) {
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
}