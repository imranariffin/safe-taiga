package app.util;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import app.imageprocessing.ImageProcessing;
import app.managers.FileManager;
import app.managers.ScriptManager;

public class Tools {

	public final static String SAFE_STRING = "X1U8N2YTR87134678V349T9V3841CM89XY4398V";
	public static boolean logging = false;

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

	public static void getImageDbAverageRGB() {
		try (Connection connection = app.Application.getConnection()) {
			Statement stmt = connection.createStatement();
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
				}
			}
			FileManager.log(averageOfRGB, "dev_output/averageOfRGB.txt");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static void getImageDbMinMax() {
		try (Connection connection = app.Application.getConnection()) {
			Statement stmt = connection.createStatement();
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
				}
			}
			FileManager.log(minMaxOfRGB, "dev_output/minMaxOfRGB.txt");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
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
}
