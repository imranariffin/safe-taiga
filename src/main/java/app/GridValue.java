package app;

import java.awt.*; // Using AWT layouts
import javax.swing.*; // Using Swing components and containers

// A Swing GUI application inherits from top-level container javax.swing.JFrame
public class GridValue extends JFrame { // JFrame instead of Frame
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	TextField[] textField;

	// Constructor to setup the GUI components and event handlers
	public GridValue(TextField[][] textField, int x, int y) {
		// Retrieve the content-pane of the top-level container JFrame
		// All operations done on the content-pane
		setLayout(new GridLayout(x, y));
		for (int a = 0; a < y; a++) {
			for (int b = 0; b < x; b++) {
				textField[a][b] = new TextField("0" + " " + "0" + " " + "0");
				add(textField[a][b]);
			}
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit program if
															// close-window
															// button
															// clicked
			setTitle("Swing Counter"); // "super" JFrame sets title
			setSize(1600, 900); // "super" JFrame sets initial size
			setVisible(true); // "super" JFrame shows
		}
	}
}