package app.util;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class WriteFile {
	private String path;
	private boolean append_to_file = false;

	public WriteFile(String file_path) {
		path = file_path;
	}

	public WriteFile(String file_path, boolean append_value) {
		path = file_path;
		append_to_file = append_value;
	}

	public void writeToFile(String textLine) throws IOException {
		Tools.println("FROM:WriteFile:START:writeToFile");
		FileWriter write = new FileWriter(path, append_to_file);
		PrintWriter print_line = new PrintWriter(write);

		print_line.printf("%s", textLine);

		print_line.close();
		Tools.println("END:writeToFile");
	}

	public static void writeStringToFile(String text, String pathFile) {
		Tools.println("FROM:WriteFile:START:writeStringToFile");
		WriteFile write = new WriteFile(pathFile);
		try {
			write.writeToFile(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Tools.println("END:writeStringToFile");
	}
}