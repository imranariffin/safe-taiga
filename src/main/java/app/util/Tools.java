package app.util;

import java.io.File;
import java.util.ArrayList;

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
			IMAGES_OUTPUT_MINIMIZEDGLOBALDIFFERENCEBINARY_DIR, IMAGES_OUTPUT_HORIZONTALAVERAGERGB_DIR;

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

		/**
		 * Horizontal Average RGB
		 */
		IMAGES_OUTPUT_HORIZONTALAVERAGERGB_DIR = new File("public/images/horizontalaveragergb");

		IMAGES_OTHER_DIR.mkdirs();
		IMAGES_OUTPUT_PARTITION_DIR.mkdirs();
		IMAGES_OUTPUT_RESIZED_DIR.mkdirs();
		IMAGES_INPUT_DIR.mkdirs();
		IMAGES_OUTPUT_GLOBALDIFFERENCE_DIR.mkdirs();
		IMAGES_OUTPUT_GLOBALDIFFERENCEBINARY_DIR.mkdirs();
		IMAGES_OUTPUT_GLOBALDIFFERENCEBINARYRGB_DIR.mkdirs();
		IMAGES_OUTPUT_MINIMIZEDGLOBALDIFFERENCEBINARY_DIR.mkdirs();
		IMAGES_OUTPUT_HORIZONTALAVERAGERGB_DIR.mkdirs();
	}

	public static String toSixtyTwoRadix(int i) {

		// means negative value, so just recursively call this function again
		// but adding negative sign as prefix
		if (i < 0) {
			return "-" + toSixtyTwoRadix(-i - 1);
		}

		// find remainder
		int quot = i / (26 + 26 + 10);
		int rem = i % (26 + 26 + 10);

		// if x < 10 i.e. is a number
		if (rem < 10) {
			char letter = Character.forDigit(rem, 10);
			if (quot == 0) {
				return "" + letter;
			} else {
				return toSixtyTwoRadix(quot) + letter;
			}
		} else if (rem < 36) {
			char letter = (char) ((int) 'a' + (rem - 10));
			if (quot == 0) {
				return "" + letter;
			} else {
				return toSixtyTwoRadix(quot) + letter;
			}
		} else {
			// if x > 26 i.e. is a lowercase letter
			// if x > (26 + 10) i.e. is a capital letter
			char letter = (char) ((int) 'A' + (rem - 10 - 26));
			if (quot == 0) {
				return "" + letter;
			} else {
				return toSixtyTwoRadix(quot) + letter;
			}
		}
	}

	public static void print(int i) {
		print(Integer.toString(i));
	}

	public static ArrayList<Integer> convertIntArrayToIntArrayList(int[] array) {
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		for (int a = 0; a < array.length; a++) {
			arrayList.add(array[a]);
		}

		return arrayList;
	}
}
