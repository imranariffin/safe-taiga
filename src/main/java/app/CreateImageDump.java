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
	public static void main(String[] args) throws IOException, Exception {

		for (int id = 1; id < 5; id++) {
			// String filename = "[HorribleSubs]_The_iDOLM@STER_0" + id;
			String filename = "idolmaster_" + id;
			System.out.println("filename:" + filename + ".mp4");
			FFmpegFrameGrabber g = new FFmpegFrameGrabber("videos/" + filename + ".mp4");
			g.start();
			Java2DFrameConverter frameConverter = new Java2DFrameConverter();

			int i = 0;
			int a = 0;
			String outputImageName;
			String outputTextName;
			BufferedImage image;
			int[][][] partitionArrayRGB;
			System.out.println("begin parsing video");
			Frame frame;
			while ((frame = g.grabImage()) != null) {
				if ((i % 72) == 0) {
					image = frameConverter.getBufferedImage(frame);
					outputImageName = "dev_output/images/output/partition/video-frame-" + filename + "-" + a + ".png";
					outputTextName = "dev_output/text/" + filename + "-" + a + ".txt";
					partitionArrayRGB = ImageProcessing.getImageRGBPartitionValues(image);
					ImageIO.write(ImageProcessing.partitionImage(ImageProcessing.resizeImage(image), partitionArrayRGB),
							"png", new File(outputImageName));
					FileManager.writeStringToFile(
							ImageProcessing.getStringFromTripleArray(ImageProcessing.getImageRGBPartitionValues(image)),
							outputTextName);
					Tools.println(outputImageName);
					a++;
				} else {
					// do nothing
				}
				i++;
			}
			g.stop();
			System.out.println("end parsing video");
		}
	}
}