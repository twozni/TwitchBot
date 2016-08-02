package BotGames;

public enum HtEnum {

    HEADS,
    TAILS;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

}
