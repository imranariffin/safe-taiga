package app.util;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Tools {

	public final static String SAFE_STRING = "X1U8N2YTR87134678V349T9V3841CM89XY4398V";
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

	public static void getImageDbAverageRGB() {

		try (Connection connection = app.Application.getConnection()) {
			Statement stmt = connection.createStatement();
			Tools.println(ScriptManager.selectAverageOfImageDb());
			ResultSet rs = stmt.executeQuery(ScriptManager.selectAverageOfImageDb());

			rs.next();
			String averageOfRGB = "";
			String result = "";
			for (int a = 1; a <= ImageProcessing.DIVISOR_VALUE; a++) {
				for (int b = 1; b <= ImageProcessing.DIVISOR_VALUE; b++) {
					for (int c = 1; c <= 3; c++) {
						result = "{" + rs.getString("" + a + ":" + b + ":" + c) + "}";
						averageOfRGB += result;
						Tools.print(result);
					}
					averageOfRGB += System.lineSeparator();
					Tools.println("");
				}
			}
			FileManager.writeStringToFile(averageOfRGB, "dev_output/averageOfRGB.txt");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static void getImageDbMinMax() {
		try (Connection connection = app.Application.getConnection()) {
			Statement stmt = connection.createStatement();
			Tools.println(ScriptManager.getMinMaxOfImageDb());
			ResultSet rs = stmt.executeQuery(ScriptManager.getMinMaxOfImageDb());

			rs.next();
			String minMaxOfRGB = "";
			String result = "";
			for (int a = 1; a <= ImageProcessing.DIVISOR_VALUE; a++) {
				for (int b = 1; b <= ImageProcessing.DIVISOR_VALUE; b++) {
					for (int c = 1; c <= 3; c++) {
						result = "{" + rs.getString("MIN:" + a + ":" + b + ":" + c) + ","
								+ rs.getString("MAX:" + a + ":" + b + ":" + c) + "}";
						minMaxOfRGB += result;
						Tools.print(result);
					}
					minMaxOfRGB += System.lineSeparator();
					Tools.println("");
				}
			}
			FileManager.writeStringToFile(minMaxOfRGB, "dev_output/minMaxOfRGB.txt");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static void println(int nextInt) {
		println(Integer.toString(nextInt));
	}

	public static String convertToQuerySafe(String givenString) {
		String safeString = "";
		for (int a = 0; a < givenString.length(); a++) {
			if (givenString.charAt(a) == '\'') {
				safeString += SAFE_STRING;
			} else {
				safeString += givenString.charAt(a);
			}
		}
		return safeString;
	}

	public static String revertQuerySafeString(String safeString) {
		return safeString.replaceAll(SAFE_STRING, "'");
	}

	public static String convertTripleArrayToString(int[][][] tripleArray) {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessing:START:getStringFromTripleArray");
		String outputText = "";

		Tools.println("y:" + tripleArray.length + System.lineSeparator() + "x:" + tripleArray[0].length
				+ System.lineSeparator() + "z:" + tripleArray[0][0].length);
		for (int y = 0; y < tripleArray.length; y++) { // Y-axis
			for (int x = 0; x < tripleArray[y].length; x++) { // X-axis
				for (int z = 0; z < tripleArray[y][x].length; z++) { // Z-axis
					outputText += tripleArray[y][x][z] + " ";
				}
			}
			outputText += System.lineSeparator();
		}
		Tools.println("END:getStringFromTripleArray" + System.lineSeparator());
		return outputText;
	}
}
