package app.util;

import java.io.File;

public class Tools {

	public static boolean logging = true;

	public static void println(String text) {
		if (logging) {
			System.out.println(text);
		}
	}

	public static void println(String text, boolean bool) {
		if (bool) {
			System.out.println(text);
		}
	}

	public static void println(int number, boolean bool) {
		println(Integer.toString(number), bool);
	}

	public static void print(String text) {
		if (logging) {
			System.out.print(text);
		}
	}

	public static void print(String text, boolean bool) {
		if (bool) {
			System.out.print(text);
		}
	}

	public static void println(int nextInt) {
		println(Integer.toString(nextInt));
	}

	public static float[][][] getNewAverage(float[][][] averageArray, int[][][] newArray, int currentCount) {

		float[][][] resultAverage = new float[averageArray.length][averageArray[0].length][averageArray[0][0].length];
		for (int y = 0; y < averageArray.length; y++) {
			for (int x = 0; x < averageArray[y].length; x++) {
				for (int z = 0; z < averageArray[y][x].length; z++) {
					resultAverage[y][x][z] = ((averageArray[y][x][z] * currentCount) + newArray[y][x][z])
							/ (currentCount + 1);
				}
			}
		}

		return resultAverage;
	}

	public static String convertTripleArrayToString(int[][][] array) {
		String string = "";
		for (int y = 0; y < array.length; y++) {
			for (int x = 0; x < array[y].length; x++) {
				for (int z = 0; z < array[y][x].length; z++) {
					string += array[y][x][z] + " ";
				}
			}
			string += System.lineSeparator();
		}
		return string;
	}

	public static File IMAGES_INPUT_DIR, IMAGES_OTHER_DIR, IMAGES_OUTPUT_PARTITION_DIR, TEXT_OUTPUT_PARTITION_DIR,
			IMAGES_OUTPUT_RESIZED_DIR, IMAGES_OUTPUT_GLOBALDIFFERENCE_DIR, TEXT_OUTPUT_GLOBALDIFFERENCE_DIR,
			IMAGES_OUTPUT_GLOBALDIFFERENCEBINARYRGB_DIR, IMAGES_OUTPUT_GLOBALDIFFERENCEBINARY_DIR,
			IMAGES_OUTPUT_MINIMIZEDGLOBALDIFFERENCEBINARY_DIR;

	public static void createFolders() {

		/**
		 * Required Directory
		 */
		IMAGES_OTHER_DIR = new File("public/images/other");
		IMAGES_INPUT_DIR = new File("public/images/input");
		IMAGES_OUTPUT_RESIZED_DIR = new File("public/images/output/resized");

		/**
		 * Partition Directory
		 */
		IMAGES_OUTPUT_PARTITION_DIR = new File("public/images/output/partition");
		/**
		 * Global Difference Directory
		 */
		IMAGES_OUTPUT_GLOBALDIFFERENCE_DIR = new File("public/images/output/globaldifference");

		/**
		 * Global Difference Binary Directory
		 */
		IMAGES_OUTPUT_GLOBALDIFFERENCEBINARY_DIR = new File("public/images/output/globaldifferencebinary");

		/**
		 * Global Difference Binary RGB Directory
		 */
		IMAGES_OUTPUT_GLOBALDIFFERENCEBINARYRGB_DIR = new File("public/images/output/globaldifferencebinaryRGB");

		/**
		 * Minimized Global Difference
		 */

		IMAGES_OUTPUT_MINIMIZEDGLOBALDIFFERENCEBINARY_DIR = new File("public/images/minimizedglobaldifferencebinary");

		IMAGES_OTHER_DIR.mkdirs();
		IMAGES_OUTPUT_PARTITION_DIR.mkdirs();
		IMAGES_OUTPUT_RESIZED_DIR.mkdirs();
		IMAGES_INPUT_DIR.mkdirs();
		IMAGES_OUTPUT_GLOBALDIFFERENCE_DIR.mkdirs();
		IMAGES_OUTPUT_GLOBALDIFFERENCEBINARY_DIR.mkdirs();
		IMAGES_OUTPUT_GLOBALDIFFERENCEBINARYRGB_DIR.mkdirs();
		IMAGES_OUTPUT_MINIMIZEDGLOBALDIFFERENCEBINARY_DIR.mkdirs();
	}
}
