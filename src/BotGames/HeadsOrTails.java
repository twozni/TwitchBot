package BotGames;

import java.util.Random;

public class HeadsOrTails implements BotGamesInterface{
	
	public static final String HT_ERROR_STRING = "You must choose either heads or tails.";
	private Random rand;
	private String gameResult = "";
	
	
	public HeadsOrTails(String choice){
		setUpGame(choice);
	}
	
	@Override
	public void setUpGame(String choice) {
		rand = new Random();
		choice = choice.toLowerCase();
		startGame(choice);
	}
	
	private HtEnum userChoiceToEnum(String choice){
		switch(choice){
			case "heads":
				return HtEnum.HEADS;
			case "tails":
				return HtEnum.TAILS;
		}
		throw new IllegalArgumentException("This should not execute. " + choice + " was passed in.");
	}

	public static boolean checkUserChoice(String choice) {
		choice = choice.toLowerCase();
		if (choice.equals("heads") || choice.equals("tails")){
			return true;
		}
		else{
			return false;
		}
		
	}

	public void startGame(String choice) {
		final HtEnum[] enumArray = { HtEnum.HEADS, HtEnum.TAILS };
		int randomIndex = rand.nextInt(2);
		
		HtEnum flip = enumArray[randomIndex];
		HtEnum choiceToEnum = userChoiceToEnum(choice);
		
		if (flip == choiceToEnum){
			gameResult = " chose " + choiceToEnum.toString() + " and it's " + flip.toString() + ". You win!";
		}
		else{
			gameResult = " chose " + choiceToEnum.toString() + " and it's " + flip.toString() + ". You lose!";
		}
		
	}

	@Override
	public String getGameResult() {
		return this.gameResult;
	}

}
