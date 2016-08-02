import java.io.IOException;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;


public class TwitchBotMain {

    public static void main(String[] args) throws NickAlreadyInUseException, IOException, IrcException {
        TwitchBot bot = new TwitchBot();

        bot.setVerbose(true);

        bot.connect("irc.chat.twitch.tv", 6667, BotProperties.TWITCH_OAUTH_KEY);

        bot.joinChannel(BotProperties.TWITCH_CHANNEL_NAME);
        bot.sendRawLine("CAP REQ :twitch.tv/commands");
        bot.sendMessage(BotProperties.TWITCH_CHANNEL_NAME, "/mods");


    }

}
