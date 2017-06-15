package app.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class EasyFileReader {

	private static BufferedReader br;

	public static String readFile(String filename) throws IOException {

		String readString = "";
		br = new BufferedReader(new FileReader(filename));

		String sCurrentLine;

		while ((sCurrentLine = br.readLine()) != null) {
			readString += sCurrentLine;
		}
		return readString;
	}

	public static int[][][] parsePartitionTextOutput(String filename) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String currentLine = "";
		int[][][] partitionArrayRGB = new int[10][10][3];
		String numberString = "";
		int number = 0;
		int x = 0;
		int y = 0;
		int z = 0;
		int xLong = 0; // xLong is a counter to how many integers we have
						// iterated in the string
		while ((currentLine = br.readLine()) != null) {
			char[] currentLineCharArray = currentLine.toCharArray();
			for (int a = 0; a < currentLineCharArray.length; a++) {
				if (currentLineCharArray[a] == ' ') {
					z = xLong % 3;
					number = Integer.valueOf(numberString);
					partitionArrayRGB[x][y][z] = number;
					numberString = "";
					xLong++;
					if (z == 2) {
						x++;
					}
				} else {
					numberString += currentLineCharArray[a];
				}
			}
			xLong = 0;
			x = 0;
			y++;
		}

		br.close();
		return partitionArrayRGB;
	}
}