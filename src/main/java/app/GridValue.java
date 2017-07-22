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

	public GridValue(TextField[][] textField, int x, int y) {
		setLayout(new GridLayout(x, y));
		for (int a = 0; a < y; a++) {
			for (int b = 0; b < x; b++) {
				textField[a][b] = new TextField("0" + " " + "0" + " " + "0");
				add(textField[a][b]);
			}
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setTitle("Swing Counter");
			setSize(1600, 900);
			setVisible(true);
		}
	}
}