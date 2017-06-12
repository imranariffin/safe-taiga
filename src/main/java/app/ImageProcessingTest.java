package app;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;

import app.util.ImageProcessing;
import app.util.Tools;

public class ImageProcessingTest {
	public static void main(String[] args) throws IOException, Exception {

		String filename = "love_live";
		System.out.println("filename:" + filename + ".mp4");
		FFmpegFrameGrabber g = new FFmpegFrameGrabber("videos/" + filename + ".mp4");
		g.start();
		Java2DFrameConverter frameConverter = new Java2DFrameConverter();
		int i = 0;
		int a = 0;
		String outputFileName;

		System.out.println("begin parsing video");
		Frame frame;
		while ((frame = g.grabImage()) != null) {
			if ((i % 96) == 0) {
				outputFileName = "video-frame-" + filename + "-" + a + ".png";
				ImageIO.write(
						ImageProcessing
								.partitionImage(ImageProcessing.resizeImage(frameConverter.getBufferedImage(frame))),
						"png", new File("src/main/resources/public/images/output/partition/" + outputFileName));
				Tools.println(outputFileName);
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