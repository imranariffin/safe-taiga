package app.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileManager {

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
		String numberString = "";
		int[][][] partitionArrayRGB = new int[10][10][3];
		int x = 0;
		int y = 0;
		int z = 0;
		int xInt = 0; // xLong is a counter to how many integers we have
						// iterated in the string
		while ((currentLine = br.readLine()) != null) {
			char[] currentLineCharArray = currentLine.toCharArray();
			for (int a = 0; a < currentLineCharArray.length; a++) {
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

		br.close();
		return partitionArrayRGB;
	}

	public static void writeStringToFile(String text, String pathFile) {
		Tools.println("FROM:WriteFile:START:writeStringToFile");
		FileWriter write;
		try {
			write = new FileWriter(pathFile);
			PrintWriter print_line = new PrintWriter(write);
			print_line.printf("%s", text);
			print_line.close();
		} catch (IOException e) {
			Tools.println("FAILURE WRITING FILE" + "\n" + "pathFile" + pathFile + "\n" + "text:" + text);
			Tools.println(e.getMessage());
		}
		Tools.println("END:writeStringToFile");
	}
}