package app.structure;

public class AnimeObject {

	private String name;
	private int numberOfEpisodes;
	int[] panels = null;

	public AnimeObject(String name, int numberOfEpisodes, int numberOfPanels) {
		this.name = name;
		this.numberOfEpisodes = numberOfEpisodes;
		this.panels = new int[numberOfPanels];
	}

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

	public int[] getPanels() {
		return panels;
	}

	public void setNumberOfPanels(int numberOfPanels) {
		this.panels = new int[numberOfPanels];
	}

	public void setPanels(int[] givenPanels) {
		panels = givenPanels;
	}
}
