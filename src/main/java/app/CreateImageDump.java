package app;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;

import app.util.ImageProcessing;
import app.util.Tools;
import app.util.FileManager;

public class CreateImageDump {

	public static File DEV_OUTPUT_IMAGES_OUTPUT_PARTITION;

	public static void main(String[] args) throws IOException, Exception {

		/**
		 * Create required folders
		 */
		DEV_OUTPUT_IMAGES_OUTPUT_PARTITION = new File("dev_output/images/output/partition");
		DEV_OUTPUT_IMAGES_OUTPUT_PARTITION.mkdirs();

		for (int episode = 1; episode <= 25; episode++) {
			String animeName = "idolmaster";
			FFmpegFrameGrabber g = new FFmpegFrameGrabber(
					"videos/" + animeName + "/" + animeName + "_" + episode + ".mkv");
			g.start();
			Java2DFrameConverter frameConverter = new Java2DFrameConverter();

			/**
			 * Setting variables
			 */
			int frameSkip = 72; // determines how many frames we want to skip

			/**
			 * Iterators
			 */
			int i = 0; // the frame iterator
			int panel = 0; // the panel iterator
			int[][][] partitionArrayRGB; // int array for RGB
			String outputImageName; // name of the output partitioned image
			String outputTextName; // name of the output partitioned text dump
			BufferedImage image; // the image

			System.out.println("begin parsing video");
			Frame frame;
			while ((frame = g.grabImage()) != null) {
				if ((i % frameSkip) == 0) {
					image = frameConverter.getBufferedImage(frame);
					outputImageName = "dev_output/images/output/partition/" + animeName + "/" + animeName + "_"
							+ episode + "_" + panel + ".png";
					outputTextName = "dev_output/text/" + animeName + "/" + animeName + "_" + episode + "_" + panel
							+ ".txt";
					partitionArrayRGB = ImageProcessing.getImageRGBPartitionValues(image);
					ImageIO.write(ImageProcessing.partitionImage(ImageProcessing.resizeImage(image), partitionArrayRGB),
							"png", new File(outputImageName));
					FileManager.writeStringToFile(Tools.convertTripleArrayToString(partitionArrayRGB), outputTextName);
					Tools.println(outputImageName);
					panel++;
				} else {
					// do nothing
				}
				i++;
			}
			FileManager.writeStringToFile("" + panel, "dev_output/description/" + animeName + "_" + episode + ".txt");
			g.stop();
			System.out.println("end parsing video");
		}
	}
}