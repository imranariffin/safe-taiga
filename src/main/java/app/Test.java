package app;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import Algorithms.ImageHashing;
import app.util.ImageProcessing;
import app.util.Tools;

public class Test {

	public static void main(String[] args) {
		int a = -1;
		while (true) {
			a++;
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Tools.println(a + " " + ImageHashing.toAlphabetic(a));
		}
	}
}