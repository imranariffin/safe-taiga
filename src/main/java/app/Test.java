package app;

import java.awt.TextField;

import javax.swing.SwingUtilities;

import app.util.SettingUp;
import app.util.Tools;
import processing.ImageProcessing;

public class Test {

	public static void main(String[] args) {

		/**
		 * TextField[][] textField = new
		 * TextField[ImageProcessing.DIVISOR_VALUE][ImageProcessing.DIVISOR_VALUE];
		 * SwingUtilities.invokeLater(new Runnable() {
		 * 
		 * @Override public void run() { new GridValue(textField,
		 *           ImageProcessing.DIVISOR_VALUE, ImageProcessing.DIVISOR_VALUE); }
		 *           }); SettingUp.createImageInfo(textField);
		 **/

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				for (int a = 0; a < 255; a++) {
					// y + " " + x + " " + a + " " + ((a / 17) * (Math.pow((float) 15, (float) ((y *
					// 3) + x))))
					Tools.println(y + " " + x + " " + a + " " + (a / 51) + " " + ((y * 3) + x) + " "
							+ (int) (Math.pow(5f, (float) ((y * 3) + x))) + " "
							+ ((a / 51) * ((int) (Math.pow(5f, (float) ((y * 3) + x))))));
				}
			}
		}
	}
}
