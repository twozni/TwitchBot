package BotGames;

import java.util.Random;

public class RockPaperScissorsGame implements Game {

    public static final String RPS_ERROR_MESSAGE = "You must either choose rock, paper, or scissors.";

    private String gameAction = "";

    public RockPaperScissorsGame(String choice) {
        setUpGame(choice);
    }

    public static boolean checkUserChoice(String choice) {
        if (choice.equalsIgnoreCase("rock") || choice.equalsIgnoreCase("paper") || choice.equalsIgnoreCase("scissors")) {
            return true;
        }
        return false;
    }

    private RockPaperScissors computerSelectChoice() {
        Random rand = new Random();

        final RockPaperScissors[] choices = {RockPaperScissors.ROCK, RockPaperScissors.PAPER, RockPaperScissors.SCISSORS};
        int randomIndex = rand.nextInt(3);

        return choices[randomIndex];
    }

    public RockPaperScissors userChoiceToEnum(String choice) {
        switch (choice) {
            case "rock":
                return RockPaperScissors.ROCK;
            case "paper":
                return RockPaperScissors.PAPER;
            case "scissors":
                return RockPaperScissors.SCISSORS;
        }
        throw new IllegalArgumentException("This should not execute. " + choice + " was passed in.");
    }

    @Override
    public void setUpGame(String choice) {
        choice = choice.toLowerCase();
        RockPaperScissors choiceToEnum = userChoiceToEnum(choice);

        startGame(choiceToEnum);
    }

    private void startGame(RockPaperScissors choice) {
        RockPaperScissors computersChoice = computerSelectChoice();

        if (choice == computersChoice) {
            gameAction = "chose " + choice.toString() + " and I chose " + computersChoice.toString() + ". It's a tie!";
        } else if (choice == RockPaperScissors.ROCK && computersChoice == RockPaperScissors.PAPER) {
            gameAction = "chose " + choice.toString() + " and I chose " + computersChoice.toString() + ". You lose!";
        } else if (choice == RockPaperScissors.ROCK && computersChoice == RockPaperScissors.SCISSORS) {
            gameAction = "chose " + choice.toString() + " and I chose " + computersChoice.toString() + ". You win!";
        } else if (choice == RockPaperScissors.PAPER && computersChoice == RockPaperScissors.ROCK) {
            gameAction = "chose " + choice.toString() + " and I chose " + computersChoice.toString() + ". You win!";
        } else if (choice == RockPaperScissors.PAPER && computersChoice == RockPaperScissors.SCISSORS) {
            gameAction = "chose " + choice.toString() + " and I chose " + computersChoice.toString() + ". You lose!";
        } else if (choice == RockPaperScissors.SCISSORS && computersChoice == RockPaperScissors.ROCK) {
            gameAction = "chose " + choice.toString() + " and I chose " + computersChoice.toString() + ". You lose!";
        } else if (choice == RockPaperScissors.SCISSORS && computersChoice == RockPaperScissors.PAPER) {
            gameAction = "chose " + choice.toString() + " and I chose " + computersChoice.toString() + ". You win!";
        }
    }

    @Override
    public String getGameResult() {
        return this.gameAction;
    }

}
