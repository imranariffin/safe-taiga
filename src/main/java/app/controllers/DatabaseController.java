package app.controllers;

import static app.Application.DATA_SOURCE;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.util.Path;
import app.util.ViewUtil;
import spark.Request;
import spark.Response;
import spark.Route;

public class DatabaseController {

	public static final String DATABASE_URL = "postgres://qmwadybfogflpr:0ddda9e0202368e60f528bdacaf76d25335239455d8ff3c2d76e2ac0f71465d6@ec2-23-21-76-49.compute-1.amazonaws.com:5432/ddre1bk422uvu2";

	public static Route serveDatabasePage = (Request request, Response response) -> {
		System.out.println("FROM:DatabaseController.java:START:serveDatabasePage");

		// Prepare model for velocity
		Map<String, Object> model = new HashMap<>();

		// Prepare arraylist for output from database
		ArrayList<String> output = new ArrayList<String>();
		try (Connection connection = DATA_SOURCE.getConnection()) {

			Statement stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
			stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
			ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

			while (rs.next()) {
				output.add("Read from DB: " + rs.getTimestamp("tick"));
			}

			System.out.println("START:printing content of output:");
			for (int a = 0; a < output.size(); a++) {
				System.out.println(output.get(a));
			}
			System.out.println("END:printing content of output");
		} catch (Exception e) {
			model.put("ERROR", "There was an error: " + e.getMessage());
			return ViewUtil.render(request, model, Path.Template.ERROR, Path.StaticStrings.ERROR, e.getMessage());
		}
		model.put("database_results", output);
		System.out.println("END:serveDatabasePage");
		return ViewUtil.render(request, model, Path.Template.DATABASE, Path.Web.DATABASE, "OK: default return");
	};
}
