package BotGames;

public enum RockPaperScissors {

    ROCK,
    PAPER,
    SCISSORS;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

}
