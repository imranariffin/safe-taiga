package app;

import java.util.concurrent.ThreadLocalRandom;

import app.util.ImageProcessing;
import app.util.Tools;

public class SettingUp {

	public static void main(String[] args) {
		while (true) {
			Tools.println(ThreadLocalRandom.current().nextInt(0,
					(ImageProcessing.DIVISOR_VALUE - ImageProcessing.TRIAL_VALUE) + 1));
		}
	}
}