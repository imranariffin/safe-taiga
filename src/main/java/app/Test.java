package app;

import java.awt.TextField;

import javax.swing.SwingUtilities;

import app.util.SettingUp;
import processing.ImageProcessing;

public class Test {

	public static void main(String[] args) {

		TextField[][] textField = new TextField[ImageProcessing.DIVISOR_VALUE][ImageProcessing.DIVISOR_VALUE];
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new GridValue(textField, ImageProcessing.DIVISOR_VALUE, ImageProcessing.DIVISOR_VALUE);
			}
		});
		SettingUp.createImageInfo(textField);
	}
}
