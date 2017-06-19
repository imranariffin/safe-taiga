package app.util;

public class AnimeObject {

	private String name;
	private int numberOfEpisodes;
	int panel = 0;

	public AnimeObject(String name, int numberOfEpisodes) {
		this.name = name;
		this.numberOfEpisodes = numberOfEpisodes;
	}

	public String getName() {
		return name;
	}

	public int getNumberOfEpisodes() {
		return numberOfEpisodes;
	}

	public int getNumberOfPanels() {
		if (panel == 0){
			Tools.println("panel count for " + this.name + " is currently 0, something is wrong!");
		}
		return panel;
	}

	public void setNumberOfPanels(int panelCount) {
		this.panel = panelCount;
	}
}
