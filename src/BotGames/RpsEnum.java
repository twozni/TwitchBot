package BotGames;

public enum RpsEnum {
	
	ROCK,
	PAPER,
	SCISSORS;
	
	@Override
	public String toString(){
		return this.name().toLowerCase();
	}

}
