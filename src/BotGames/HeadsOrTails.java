package BotGames;

import java.util.Random;

public class HeadsOrTails implements BotGamesInterface {

    public static final String HT_ERROR_STRING = "You must choose either heads or tails.";
    private Random rand;
    private String gameResult = "";


    public HeadsOrTails(String choice) {
        setUpGame(choice);
    }

    @Override
    public void setUpGame(String choice) {
        rand = new Random();

        choice = choice.toLowerCase();
        HtEnum choiceToEnum = userChoiceToEnum(choice);

        startGame(choiceToEnum);
    }

    private HtEnum userChoiceToEnum(String choice) {
        switch (choice) {
            case "heads":
                return HtEnum.HEADS;
            case "tails":
                return HtEnum.TAILS;
        }
        throw new IllegalArgumentException("This should not execute. " + choice + " was passed in.");
    }

    private HtEnum computerFlipCoin() {
        final HtEnum[] enumArray = {HtEnum.HEADS, HtEnum.TAILS};

        int randomIndex = rand.nextInt(2);
        return enumArray[randomIndex];
    }

    public static boolean checkUserChoice(String choice) {
        choice = choice.toLowerCase();
        if (choice.equals("heads") || choice.equals("tails")) {
            return true;
        }
        return false;
    }

    private void startGame(HtEnum choice) {
        HtEnum flip = computerFlipCoin();

        if (flip == choice) {
            gameResult = " chose " + choice.toString() + " and it's " + flip.toString() + ". You win!";
        } else {
            gameResult = " chose " + choice.toString() + " and it's " + flip.toString() + ". You lose!";
        }

    }

    @Override
    public String getGameResult() {
        return this.gameResult;
    }

}
