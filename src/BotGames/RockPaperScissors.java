package BotGames;

import java.util.Random;

public class RockPaperScissors implements BotGamesInterface {

    public static final String RPS_ERROR_MESSAGE = "You must either choose rock, paper, or scissors.";

    private String gameAction = "";

    public RockPaperScissors(String choice) {
        setUpGame(choice);
    }

    public static boolean checkUserChoice(String choice) {
        if (choice.equalsIgnoreCase("rock") || choice.equalsIgnoreCase("paper") || choice.equalsIgnoreCase("scissors")) {
            return true;
        }
        return false;
    }

    private RpsEnum computerSelectChoice() {
        Random rand = new Random();

        final RpsEnum[] choices = {RpsEnum.ROCK, RpsEnum.PAPER, RpsEnum.SCISSORS};
        int randomIndex = rand.nextInt(3);

        return choices[randomIndex];
    }

    public RpsEnum userChoiceToEnum(String choice) {
        switch (choice) {
            case "rock":
                return RpsEnum.ROCK;
            case "paper":
                return RpsEnum.PAPER;
            case "scissors":
                return RpsEnum.SCISSORS;
        }
        throw new IllegalArgumentException("This should not execute. " + choice + " was passed in.");
    }

    @Override
    public void setUpGame(String choice) {
        choice = choice.toLowerCase();
        RpsEnum choiceToEnum = userChoiceToEnum(choice);

        startGame(choiceToEnum);
    }

    private void startGame(RpsEnum choice) {
        RpsEnum computersChoice = computerSelectChoice();

        if (choice == computersChoice) {
            gameAction = "chose " + choice.toString() + " and I chose " + computersChoice.toString() + ". It's a tie!";
        } else if (choice == RpsEnum.ROCK && computersChoice == RpsEnum.PAPER) {
            gameAction = "chose " + choice.toString() + " and I chose " + computersChoice.toString() + ". You lose!";
        } else if (choice == RpsEnum.ROCK && computersChoice == RpsEnum.SCISSORS) {
            gameAction = "chose " + choice.toString() + " and I chose " + computersChoice.toString() + ". You win!";
        } else if (choice == RpsEnum.PAPER && computersChoice == RpsEnum.ROCK) {
            gameAction = "chose " + choice.toString() + " and I chose " + computersChoice.toString() + ". You win!";
        } else if (choice == RpsEnum.PAPER && computersChoice == RpsEnum.SCISSORS) {
            gameAction = "chose " + choice.toString() + " and I chose " + computersChoice.toString() + ". You lose!";
        } else if (choice == RpsEnum.SCISSORS && computersChoice == RpsEnum.ROCK) {
            gameAction = "chose " + choice.toString() + " and I chose " + computersChoice.toString() + ". You lose!";
        } else if (choice == RpsEnum.SCISSORS && computersChoice == RpsEnum.PAPER) {
            gameAction = "chose " + choice.toString() + " and I chose " + computersChoice.toString() + ". You win!";
        }
    }

    @Override
    public String getGameResult() {
        return this.gameAction;
    }

}
