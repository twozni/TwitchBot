import java.io.IOException;
import java.time.Instant;
import java.util.*;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import BotGames.HeadsOrTails;
import BotGames.RockPaperScissors;


public class TwitchBot extends PircBot {

    //Time (in seconds) in which games are unplayable after someone plays one
    private static final int TWITCH_TIME_BETWEEN_GAMES = secondsToMilliseconds(30);

    //Time (in seconds) in which the !uptime command is on cooldown
    private static final int TWITCH_TIME_BETWEEN_UPTIME_MESSAGE = secondsToMilliseconds(30);

    private static final String TWITCH_STREAM_JSON_URL = "https://api.twitch.tv/kraken/streams/" + BotProperties.TWITCH_CHANNEL_NAME.substring(1);

    //Bot name. Must be all lowercase and match assigned OAUTH key
    private static final String TWITCH_BOT_NAME = "fsocietybot";

    private static final String TWITCH_BROADCASTER_NAME = BotProperties.TWITCH_CHANNEL_NAME.substring(1);

    private List<String> messages = new ArrayList<>();

    private final Timer timer = new Timer();
    private TimerTask randomMessageTask;

    private long globalStartTime;
    private long startTimeFlip;
    private long startTimeRps;

    public TwitchBot() {
        this.setName(TWITCH_BOT_NAME);

        initializeRandomMessageList();

        globalStartTime = System.currentTimeMillis();
        startTimeFlip = System.currentTimeMillis();
        startTimeRps = System.currentTimeMillis();

        initializeTimerTask();
        timer.schedule(randomMessageTask, 0l, secondsToMilliseconds(300));
    }

    // TODO
    public void initializeRandomMessageList() {
        String m1 = "Welcome to the channel!";
        String m2 = "Please read the rules!";
        String m3 = "Test string!";
        String m4 = "Test 2!";
        String m5 = "Hello from Java!";
        String m6 = "Hello, World!";
        String m7 = "fizz!";
        String m8 = "buzz!";
        messages.add(m1);
        messages.add(m2);
        messages.add(m3);
        messages.add(m4);
        messages.add(m5);
        messages.add(m6);
        messages.add(m7);
        messages.add(m8);
    }

    //Convert seconds to milliseconds
    private static int secondsToMilliseconds(int seconds) {
        return seconds * 1000;
    }

    private void initializeTimerTask() {
        randomMessageTask = new TimerTask() {
            @Override
            public void run() {
                sendRandomMessage();
            }
        };
    }

    //Gets stream start time from URL
    private static long streamUptimeToMilliseconds() throws IOException {
        String startTime = JsonParserFromUrl.getStreamTimeStart(TWITCH_STREAM_JSON_URL).replace("\"", "");
        if (startTime.equalsIgnoreCase(JsonParserFromUrl.STREAM_OFFLINE)) {
            return 0;
        } else {
            long startTimeToMilliseconds = Instant.parse(startTime.replace("\"", "")).toEpochMilli();
            return startTimeToMilliseconds;
        }
    }


    //Converts stream start time to string
    private static String streamUptimeToString(long streamUptimeMilliseconds) {
        if (streamUptimeMilliseconds == 0) {
            return TWITCH_BROADCASTER_NAME + " is currently offline.";
        } else {
            return JsonParserFromUrl.currentUptimeOfStream(TWITCH_BROADCASTER_NAME, streamUptimeMilliseconds);
        }
    }

    private boolean isSenderMod(String tags) {
        if (tags.contains("mod=1") || tags.contains("badges=broadcaster")) {
            return true;
        }
        return false;
    }

    public void sendRandomMessage() {
        Random rand = new Random();
        int num = rand.nextInt(messages.size());

        this.sendMessage(BotProperties.TWITCH_CHANNEL_NAME, messages.get(num));
    }

    @Override
    protected void onDisconnect() {
        this.log("Bot has been disconnected.");
        while (!this.isConnected()) {
            try {
                this.log("Attempting to reconnect...");
                this.reconnect();
            } catch (IOException | IrcException e) {
                this.log("Failed to reconnect. Reason: " + e.getMessage());
            }
        }
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            this.log("Attempt to reconnect has been interrupted. Reason: " + e.getMessage());
        }
    }

    private void getStreamUptime(String channel) {
        long globalEndTime = System.currentTimeMillis();
        if ((globalEndTime - globalStartTime) > TWITCH_TIME_BETWEEN_UPTIME_MESSAGE) {
            try {
                this.sendMessage(channel, streamUptimeToString(streamUptimeToMilliseconds()));
            } catch (IOException e) {
                this.log("Error: " + e.getMessage());
            }

            // Reset start time
            globalStartTime = System.currentTimeMillis();
        }
    }

    private void onHeadsOrTailsMessage(String channel, String sender, String message) {
        String userChoice = "";

        try {
            userChoice = message.substring(6);
        } catch (StringIndexOutOfBoundsException e) {
            this.sendMessage(channel, "@" + sender + " " + HeadsOrTails.HT_ERROR_STRING);
            this.log("Error: " + e.getMessage());
            return;
        }
        boolean checkIfCorrect = HeadsOrTails.checkUserChoice(userChoice);
        if (checkIfCorrect) {
            long endTimeFlip = System.currentTimeMillis();
            //Check if enough time has passed between previous game
            if ((endTimeFlip - startTimeFlip) > TWITCH_TIME_BETWEEN_GAMES) {
                HeadsOrTails headsOrTails = new HeadsOrTails(userChoice);

                this.sendMessage(channel, sender + " " + headsOrTails.getGameResult());
                //Reset start time
                startTimeFlip = System.currentTimeMillis();
            }
        } else {
            this.sendMessage(channel, "@" + sender + " " + HeadsOrTails.HT_ERROR_STRING);
        }
    }

    private void onRockPaperScissorsMessage(String channel, String sender, String message) {
        String userChoice = "";
        try {
            userChoice = message.substring(5);
        } catch (StringIndexOutOfBoundsException e) {
            this.sendMessage(channel, "@" + sender + " " + RockPaperScissors.RPS_ERROR_MESSAGE);
            return;
        }
        boolean userChoiceIsValid = RockPaperScissors.checkUserChoice(userChoice);
        if (userChoiceIsValid) {
            long endTimeRps = System.currentTimeMillis();
            if ((endTimeRps - startTimeRps) > TWITCH_TIME_BETWEEN_GAMES) {
                RockPaperScissors rps = new RockPaperScissors(userChoice);
                this.sendMessage(channel, sender + " " + rps.getGameResult());

                //Reset start time
                startTimeRps = System.currentTimeMillis();
            }
        } else {
            this.sendMessage(channel, "@" + sender + " " + RockPaperScissors.RPS_ERROR_MESSAGE);
        }
    }

    @Override
    protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
        // TODO
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message, String tags) {
        //Timeout user if message is over 400 characters
        if (message.length() > 400 && !isSenderMod(tags)) {
            this.sendMessage(channel, "/timeout " + sender + " 60");
            this.sendMessage(channel, sender + " has been timed out. Reason: message too long.");
        }

        //Timeout user if message contains URL
        else if ((message.contains("http://") || message.contains(".com")) && !isSenderMod(tags)) {
            this.sendMessage(channel, "/timeout " + sender + " 120");
            this.sendMessage(channel, sender + " has been timed out. Reason: no links allowed!");
        }

        // Return how long the stream has been online
        else if (message.equals("!uptime")) {
            getStreamUptime(channel);
        }

        // Heads or tails game
        else if (message.startsWith("!flip")) {
            onHeadsOrTailsMessage(channel, sender, message);
        }

        // Rock, Paper, Scissors game
        else if (message.startsWith("!rps")) {
            onRockPaperScissorsMessage(channel, sender, message);
        }


    }

}


