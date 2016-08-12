package BotGames;

public enum HeadsOrTails {

    HEADS,
    TAILS;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

}
