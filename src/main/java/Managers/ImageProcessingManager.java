package Managers;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ImageProcessing.ImageProcessing;
import app.structure.AnimePanel;
import app.structure.IntegerPair;
import app.util.Tools;

public class ImageProcessingManager {

	private static boolean BOOL_SCRIPT = false;
	private static boolean BOOL_MATCHING_NAME = false;

	public static void insertImageDataToDatabase(String ipAddress, BufferedImage image) {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessingManager:START:insertImageDataToDatabase");

		String script = "INSERT INTO imagedb_user_image_request_byte (request_ip, imagefile) VALUES (?,?)";
		try (Connection connection = app.Application.getConnection()) {

			PreparedStatement pst = connection.prepareStatement(script);
			pst.setString(1, ipAddress);
			pst.setBytes(2, ImageProcessing.extractBytes(image));
			pst.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		Tools.println("END:insertImageDataToDatabase" + System.lineSeparator());
	}

	public static void findMatchingImageDataRandomized(Map<String, Object> model, int[][][] array) {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessingManager:START:findMatchingImageDataRandomized");

		try (Connection connection = app.Application.getConnection()) {

			Statement stmt = connection.createStatement();

			/**
			 * Initialize variable for image search
			 * 
			 * IntegerPair is defined as <index, weight>
			 */
			Map<String, IntegerPair> matchResultMap = new HashMap<String, IntegerPair>();
			ArrayList<AnimePanel> values = new ArrayList<AnimePanel>();
			int maxIndex = 0;
			int valueIterator = -1; // use this to check if the algorithm even
									// found
									// any

			String findMatchingImageDataRandomized = ScriptManager.findMatchingImageDataRandomized(array);
			Tools.println(findMatchingImageDataRandomized, BOOL_SCRIPT);

			ResultSet rs = stmt.executeQuery(findMatchingImageDataRandomized);

			while (rs.next()) {
				Tools.println("matching name:" + rs.getString("name") + " " + rs.getString("episode") + " "
						+ rs.getString("panel"), BOOL_MATCHING_NAME);

				String key = rs.getString("name") + ":" + rs.getString("episode");
				if (matchResultMap.containsKey(key)) { // the fact that this is
														// true
														// means that there are
														// at
														// least 1 relation that
														// exist in the map
					matchResultMap.get(key).b++;

					// check if this is larger than the weight that we currently
					// have. If so replace.

					if (matchResultMap.get(key).b > matchResultMap.get(values.get(maxIndex).getKey(2)).b) {
						maxIndex = matchResultMap.get(key).a;
					}
				} else {
					valueIterator++;
					AnimePanel animePanel = new AnimePanel(rs.getString("name"), rs.getString("episode"),
							rs.getString("panel"));
					matchResultMap.put(animePanel.getKey(2), new IntegerPair(valueIterator, 0));
					values.add(animePanel);
				}
			}

			if (values.isEmpty()) {
				Tools.println("Test 1: None found");
				model.put("test_1_boolean", false);
			} else {
				Tools.println("Test 1: Found");
				model.put("test_1_boolean", true);
				model.put("test_1_weight", matchResultMap.get(values.get(maxIndex).getKey(2)).b);
				model.put("test_1_result", values.get(maxIndex)); // Return
																	// a
																	// list
																	// of
				// result,
				// since this is randomized
				// search specialized in
				// cropped
				// pictures, duplicates are
				// expected
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		Tools.println("END:findMatchingImageDataRandomized" + System.lineSeparator());
	}

	public static void findMatchingImageDataRandomizedV2(Map<String, Object> model, int[][][] array) {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessingManager:START:findMatchingImageDataRandomizedV2");

		try (Connection connection = app.Application.getConnection()) {
			Statement stmt = connection.createStatement();
			/**
			 * Initialize variables for image search
			 */
			Map<String, IntegerPair> matchResultMap = new HashMap<String, IntegerPair>();
			ArrayList<AnimePanel> values = new ArrayList<AnimePanel>();
			int maxIndex = 0;
			int valueIterator = -1; // use this to check if the algorithm even
									// found
									// any

			String findMatchingImageDataRandomizedV2 = ScriptManager.findMatchingImageDataRandomizedV2(array);
			Tools.println(findMatchingImageDataRandomizedV2, BOOL_SCRIPT);
			ResultSet rs = stmt.executeQuery(findMatchingImageDataRandomizedV2);

			while (rs.next()) {
				Tools.println("matching name:" + rs.getString("name") + " " + rs.getString("episode") + " "
						+ rs.getString("panel"), BOOL_MATCHING_NAME);

				String key = rs.getString("name") + ":" + rs.getString("episode");
				if (matchResultMap.containsKey(key)) { // the fact that this is
														// true
														// means that there are
														// at
														// least 1 relation that
														// exist in the map
					matchResultMap.get(key).b++;

					// check if this is larger than the weight that we currently
					// have. If so replace.

					if (matchResultMap.get(key).b > matchResultMap.get(values.get(maxIndex).getKey(2)).b) {
						maxIndex = matchResultMap.get(key).a;
					}
				} else {
					valueIterator++;
					AnimePanel animePanel = new AnimePanel(rs.getString("name"), rs.getString("episode"),
							rs.getString("panel"));
					matchResultMap.put(animePanel.getKey(2), new IntegerPair(valueIterator, 0));
					values.add(animePanel);
				}
			}

			if (values.isEmpty()) {
				Tools.println("Test 2: None found");
				model.put("test_2_boolean", false);
			} else {
				Tools.println("Test 2: Found");
				model.put("test_2_boolean", true);
				model.put("test_2_weight", matchResultMap.get(values.get(maxIndex).getKey(2)).b);
				model.put("test_2_result", values.get(maxIndex)); // Return
																	// a
																	// list
																	// of
				// result,
				// since this is randomized
				// search specialized in
				// cropped
				// pictures, duplicates are
				// expected
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		Tools.println("END:findMatchingImageDataRandomizedV2" + System.lineSeparator());
	}

	public static void findMatchingImageDataIncremental(Map<String, Object> model, int[][][] array) {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessingManager:START:findMatchingImageDataIncremental");

		try (Connection connection = app.Application.getConnection()) {
			Statement stmt = connection.createStatement();

			boolean test3Found = false;
			Map<String, AnimePanel> matchResultMap = new HashMap<String, AnimePanel>();
			AnimePanel[] values;
			ResultSet rs;

			String findMatchingImageDataIncremental;

			for (int a = 0; a < ImageProcessing.DIVISOR_VALUE; a++) {
				for (int b = 0; b < ImageProcessing.DIVISOR_VALUE; b++) {
					for (int c = 0; c < 3; c++) {
						findMatchingImageDataIncremental = ScriptManager.findMatchingImageDataIncremental(a, b, c,
								array[a][b][c]);
						Tools.println("Execute Query:" + findMatchingImageDataIncremental, BOOL_SCRIPT);

						rs = stmt.executeQuery(findMatchingImageDataIncremental);

						while (rs.next()) {
							Tools.println("matching name:" + rs.getString("name") + " " + rs.getString("episode") + " "
									+ rs.getString("panel"), BOOL_MATCHING_NAME);
							AnimePanel panelData = new AnimePanel(rs.getString("name"), rs.getInt(2), rs.getInt(3));
							if (!(matchResultMap.containsKey(panelData.getKey()))) {
								matchResultMap.put(panelData.getKey(), panelData);
							} else {
								test3Found = true;
								matchResultMap.get(panelData.getKey()).incrementWeight();
							}

						}
					}
				}
			}

			if (!test3Found) {
				Tools.println("Test 3: None found");
				model.put("test_3_boolean", false);
			} else {
				Tools.println("Test 3: Found");
				model.put("test_3_boolean", true);
				values = new AnimePanel[matchResultMap.size()];

				/**
				 * Convert map to array
				 */
				int index = 0;
				for (Map.Entry<String, AnimePanel> mapEntry : matchResultMap.entrySet()) {
					values[index] = mapEntry.getValue();
					index++;
				}

				/**
				 * Find the image with the highest weight <-- can be further
				 * optimized by merging this process with above conversion
				 */
				int maxIndex = -1;
				int maxValue = -1;
				for (int a = 0; a < values.length; a++) {
					if (values[a].getWeight() > maxValue) {
						maxValue = values[a].getWeight();
						maxIndex = a;
					}
				}
				if (maxIndex != -1) {
					Tools.println(maxValue + " " + values[maxIndex].getKey());
					model.put("test_3_weight", maxValue);
					model.put("test_3_result", new AnimePanel(values[maxIndex].getKey().split(":")[0],
							values[maxIndex].getKey().split(":")[1], values[maxIndex].getKey().split(":")[2]));
				} else {
					Tools.println("maxIndex is -1");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		Tools.println("END:findMatchingImageDataIncremental" + System.lineSeparator());
	}

	public static void findMatchingImageDataIncrementalRGB(Map<String, Object> model, int[][][] array) {
		Tools.println(System.lineSeparator() + "FROM:ImageProcessingManager:START:findMatchingImageDataIncrementalRGB");

		try (Connection connection = app.Application.getConnection()) {
			Statement stmt = connection.createStatement();
			boolean test4Found = false;
			String findMatchingImageDataIncrementalRGB;
			Map<String, AnimePanel> matchResult = new HashMap<String, AnimePanel>();
			AnimePanel[] values;
			ResultSet rs;

			for (int a = 0; a < ImageProcessing.DIVISOR_VALUE; a++) {
				for (int b = 0; b < ImageProcessing.DIVISOR_VALUE; b++) {
					findMatchingImageDataIncrementalRGB = ScriptManager.findMatchingImageDataIncrementalRGB(a, b,
							array[a][b]);
					Tools.println("Execute Query:" + findMatchingImageDataIncrementalRGB, BOOL_SCRIPT);

					rs = stmt.executeQuery(findMatchingImageDataIncrementalRGB);

					while (rs.next()) {
						Tools.println("matching name:" + rs.getString("name") + " " + rs.getString("episode") + " "
								+ rs.getString("panel"), BOOL_MATCHING_NAME);
						String key = "" + rs.getString("name") + ":" + rs.getInt(2) + ":" + rs.getInt(3);
						if (!(matchResult.containsKey(key))) {
							matchResult.put(key, new AnimePanel("" + rs.getString("name"), rs.getInt(2), rs.getInt(3)));
						} else {
							test4Found = true;
							matchResult.get(key).incrementWeight();
						}

					}
				}
			}

			if (!test4Found) {
				Tools.println("Test 4: None found");
				model.put("test_4_boolean", false);
			} else {
				Tools.println("Test 4: Found");
				model.put("test_4_boolean", true);
				values = new AnimePanel[matchResult.size()];

				/**
				 * Convert map to array
				 */
				int index = 0;
				for (Map.Entry<String, AnimePanel> mapEntry : matchResult.entrySet()) {
					values[index] = mapEntry.getValue();
					index++;
				}

				/**
				 * Find the image with the highest weight <-- can be further
				 * optimized by merging this process with above conversion
				 */
				int maxIndex = -1;
				int maxValue = -1;
				for (int a = 0; a < values.length; a++) {
					if (values[a].getWeight() > maxValue) {
						maxValue = values[a].getWeight();
						maxIndex = a;
					}
				}
				if (maxIndex != -1) {
					Tools.println(maxValue + " " + values[maxIndex].getKey());
					model.put("test_4_weight", maxValue);
					model.put("test_4_result", new AnimePanel(values[maxIndex].getKey().split(":")[0],
							values[maxIndex].getKey().split(":")[1], values[maxIndex].getKey().split(":")[2]));
				} else {
					Tools.println("maxIndex is -1");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		Tools.println("END:findMatchingImageDataIncrementalRGB" + System.lineSeparator());
	}
}
