package app.util;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ImageProcessing.ImageProcessing;
import Managers.FileManager;
import Managers.ScriptManager;

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
		Tools.println(System.lineSeparator() + "FROM:Tools:START:getImageDbAverageRGB");

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
			FileManager.log(averageOfRGB, "dev_output/averageOfRGB.txt");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		Tools.println("END:getImageDbAverageRGB");
	}

	public static void getImageDbMinMax() {
		Tools.println(System.lineSeparator() + "FROM:Tools:START:getImageDbMinMax");

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
			FileManager.log(minMaxOfRGB, "dev_output/minMaxOfRGB.txt");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		Tools.println("END:getImageDbMinMax");
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
		Tools.println(System.lineSeparator() + "FROM:Tools:START:convertToQuerySafe");

		String safeString = "";
		for (int a = 0; a < givenString.length(); a++) {
			if (givenString.charAt(a) == '\'') {
				safeString += SAFE_STRING;
			} else {
				safeString += givenString.charAt(a);
			}
		}

		Tools.println("END:convertToQuerySafe");
		return safeString;
	}

	public static String revertQuerySafeString(String safeString) {
		Tools.println("FROM:Tools:START:revertQuerySafeString");
		Tools.println("END:revertQuerySafeString");
		return safeString.replaceAll(SAFE_STRING, "'");
	}
}
