package app.managers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import app.util.Tools;
import processing.ImageProcessing;

public class FileManager {

	private static boolean logging = true;

	public static String readFile(String filename) throws IOException {

		String readString = "";
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String sCurrentLine;

		while ((sCurrentLine = br.readLine()) != null) {
			readString += sCurrentLine;
		}

		// always close file reader
		br.close();

		return readString;
	}

	public static int[][][] parseIntegerPartitionTextOutput(String filename) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(filename));
		String currentLine = "";
		String numberString = "";
		int[][][] partitionArrayRGB = new int[ImageProcessing.DIVISOR_VALUE][ImageProcessing.DIVISOR_VALUE][3];
		int x = 0;
		int y = 0;
		int z = 0;
		int xInt = 0; // xLong is a counter to how many integers we have
						// iterated in the string
		while ((currentLine = br.readLine()) != null) { // y-axis of text
			Tools.println(currentLine, false);
			char[] currentLineCharArray = currentLine.toCharArray();
			for (int a = 0; a < currentLineCharArray.length; a++) { // x-axis of
																	// text
				if (currentLineCharArray[a] == ' ') {
					z = xInt % 3;
					partitionArrayRGB[y][x][z] = Integer.valueOf(numberString);
					numberString = "";
					xInt++;
					if (z == 2) {
						x++;
					}
				} else {
					numberString += currentLineCharArray[a];
				}
			}
			xInt = 0;
			x = 0;
			y++;
		}

		// always close file reader
		br.close();

		return partitionArrayRGB;
	}

	public static float[][][] parseFloatPartitionTextOutput(String filename) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(filename));
		String currentLine = "";
		String numberString = "";
		float[][][] partitionArrayRGB = new float[ImageProcessing.DIVISOR_VALUE][ImageProcessing.DIVISOR_VALUE][3];
		int x = 0;
		int y = 0;
		int z = 0;
		int xInt = 0; // xLong is a counter to how many integers we have
						// iterated in the string
		while ((currentLine = br.readLine()) != null) { // y-axis of text
			Tools.println(currentLine, false);
			char[] currentLineCharArray = currentLine.toCharArray();
			for (int a = 0; a < currentLineCharArray.length; a++) { // x-axis of
																	// text
				if (currentLineCharArray[a] == ' ') {
					z = xInt % 3;
					partitionArrayRGB[y][x][z] = Float.valueOf(numberString);
					numberString = "";
					xInt++;
					if (z == 2) {
						x++;
					}
				} else {
					numberString += currentLineCharArray[a];
				}
			}
			xInt = 0;
			x = 0;
			y++;
		}

		// always close file reader
		br.close();

		return partitionArrayRGB;
	}

	public static void log(String text, String pathFile) {
		if (logging) {
			Tools.println("\nFROM:WriteFile:START:writeStringToFile");
			Tools.println("writing to:" + pathFile);
			FileWriter write;
			try {
				write = new FileWriter(pathFile);
				PrintWriter print_line = new PrintWriter(write);
				print_line.printf("%s", text);
				print_line.close();
			} catch (IOException e) {
				Tools.println("FAILURE WRITING FILE" + "\n" + "pathFile:" + pathFile + "\n" + "text:" + text);
				Tools.println("message" + e.getMessage());
			}
			Tools.println("END:writeStringToFile\n");
		}
	}

	public static void writeTripleArrayToString(int[][][] tripleArray, String pathFile) {
		Tools.println("writing to:" + pathFile);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(pathFile)));
			for (int a = 0; a < tripleArray.length; a++) {
				for (int b = 0; b < tripleArray[a].length; b++) {
					for (int c = 0; c < tripleArray[a][b].length; c++) {
						writer.write(tripleArray[a][b][c] + " ");
					}
				}

				if (a < (tripleArray.length - 1)) {
					writer.newLine();
				}
			}
		} catch (Exception e) {
			Tools.println(e.getMessage());
		} finally {
			try {
				// Close the writer regardless of what happens...
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void writeTripleArrayToString(float[][][] partitionArrayRGB, String pathFile) {
		Tools.println("writing to:" + pathFile);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(pathFile)));
			for (int a = 0; a < partitionArrayRGB.length; a++) {
				for (int b = 0; b < partitionArrayRGB[a].length; b++) {
					for (int c = 0; c < partitionArrayRGB[a][b].length; c++) {
						writer.write(partitionArrayRGB[a][b][c] + " ");
					}
				}

				if (a < (partitionArrayRGB.length - 1)) {
					writer.newLine();
				}
			}
		} catch (Exception e) {
			Tools.println(e.getMessage());
		} finally {
			try {
				// Close the writer regardless of what happens...
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}