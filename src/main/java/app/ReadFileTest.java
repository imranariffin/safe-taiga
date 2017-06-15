package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import app.util.EasyFileReader;
import app.util.Tools;

public class ReadFileTest {

	public static void main(String[] args) {

		String baseFilename = "idolmaster_1-";
		String fileType = ".txt";
		int id = 0;
		boolean fileExist = true;
		try {
			while (fileExist) {
				// Tools.println("\n" +
				// EasyFileReader.readFile("dev_output/text/" + baseFilename +
				// "-" + id + ".txt"));
				// id++;
				EasyFileReader.parsePartitionTextOutput("dev_output/text/" + baseFilename + id + fileType);
				id++;
			}
		} catch (IOException e) {
			System.out.println("DONE READING ALL FILES:" + id);
			fileExist = false;
		}
	}
}
