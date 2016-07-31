import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import BotGames.HeadsOrTails;
import BotGames.RockPaperScissors;


public class TwitchBot extends PircBot{
	
	//Time (in seconds) in which games are unplayable after someone plays one
	private static final int TWITCH_TIME_BETWEEN_GAMES = secondsToMilliseconds(20);
	
	//Time (in seconds) in which the !uptime command is on cooldown
	private static final int TWITCH_TIME_BETWEEN_UPTIME_MESSAGE = secondsToMilliseconds(30);
	
	//Time (in seconds) between random bot messages
	private static final int TWITCH_TIME_BETWEEN_RANDOM_MESSAGES = secondsToMilliseconds(500);
	
	private static final String TWITCH_STREAM_JSON_URL = "https://api.twitch.tv/kraken/streams/" + BotProperties.TWITCH_CHANNEL_NAME.substring(1);
	
	//Bot name. Must be all lowercase and match assigned OAUTH key
	private static final String TWITCH_BOT_NAME = "fsocietybot";
	
	private static List<String> messages = new ArrayList<>();
	private static List<String> moderators = new ArrayList<String>();
	
	private static long globalStartTime;
	private static long startTimeFlip;
	private static long startTimeRps;
	
	public TwitchBot(){
		this.setName(TWITCH_BOT_NAME);
		initializeRandomMessageList();
		globalStartTime = System.currentTimeMillis();
		startTimeFlip = System.currentTimeMillis();
		startTimeRps = System.currentTimeMillis();
	}
	
	public void initializeRandomMessageList(){
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
	
	public static List<String> getModerators(){
		return moderators;
	}
	
	public static void addModerator(String username){
		moderators.add(username);
	}
	
	//Convert seconds to milliseconds
	private static int secondsToMilliseconds(int seconds){
		return seconds * 1000;
	}
	
	//Gets stream start time from URL
	private static long streamUptimeToMilli() throws IOException{
		String startTime = JsonParserFromUrl.getStreamTimeStart(TWITCH_STREAM_JSON_URL).replace("\"", "");
		if (startTime.equalsIgnoreCase(JsonParserFromUrl.STREAM_OFFLINE)){
			return 0;
		}
		else{
			long startTimeToMilliseconds = Instant.parse(startTime.replace("\"", "")).toEpochMilli();
			return startTimeToMilliseconds;
		}
	}
	
	
	//Converts stream start time to string
	private static String streamUptimeToString(long streamUptimeMilliseconds){
		String channelName = BotProperties.TWITCH_CHANNEL_NAME.substring(1);
		if (streamUptimeMilliseconds == 0){
			return channelName + " is currently offline.";
		}
		else{
			return JsonParserFromUrl.currentUptimeOfStream(channelName, streamUptimeMilliseconds);
		}
	}
	
	
	// TODO
	public boolean isOp(String sender, String channel){
		final User[] users = getUsers(channel);
		
		for (final User user: users){
			System.out.println(user.getPrefix());
			if (user.getNick().equals(sender)){
				System.out.println(user.getPrefix());
				if(user.getPrefix().startsWith("+")){
					return true;
				}
			}
		}
		return false;
	}
	
	public void sendRandomMessage(){
		Random rand = new Random();
		int num = rand.nextInt(messages.size());
		this.sendMessage(BotProperties.TWITCH_CHANNEL_NAME, messages.get(num));
	}
	
	public static void startRandomMessages(TwitchBot bot){
		while(true){
			bot.sendRandomMessage();
			try{
				Thread.sleep(secondsToMilliseconds(TWITCH_TIME_BETWEEN_RANDOM_MESSAGES));
			}
			catch (InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onDisconnect(){
		this.log("Attempting to reconnect...");
		while(true){
			if (!this.isConnected()){
				try {
					this.reconnect();
				} catch (IOException | IrcException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try{
				Thread.sleep(30000);
			}
			catch(InterruptedException e){
				e.printStackTrace();
			}
		}	
	}
	
	
	private void getStreamUptime(String channel){
		long globalEndTime = System.currentTimeMillis();
		if ( (globalEndTime - globalStartTime) > secondsToMilliseconds(TWITCH_TIME_BETWEEN_UPTIME_MESSAGE)){
			try {
				this.sendMessage(channel, streamUptimeToString(streamUptimeToMilli()));
			} catch (IOException e) {
				this.log("Error: " + e.getMessage());
			}
			
			// Reset start time
			globalStartTime = System.currentTimeMillis();
		}
	}
	
	private void onHeadsOrTailsMessage(String channel, String sender, String message){
		String userChoice = "";
		
		try{
			userChoice = message.substring(6);
		}
		catch (StringIndexOutOfBoundsException e){
			this.sendMessage(channel, "@" + sender + " " + HeadsOrTails.HT_ERROR_STRING);
			this.log("Error: " + e.getMessage());
			return;
		}
		boolean checkIfCorrect = HeadsOrTails.checkUserChoice(userChoice);
		if (checkIfCorrect){
			long endTimeFlip = System.currentTimeMillis();
			//Check if enough time has passed between previous game
			if ( (endTimeFlip - startTimeFlip) > secondsToMilliseconds(TWITCH_TIME_BETWEEN_GAMES)){
				HeadsOrTails headsOrTails = new HeadsOrTails(userChoice);
				
				this.sendMessage(channel, sender + " " + headsOrTails.getGameResult());
				//Reset start time
				startTimeFlip = System.currentTimeMillis();
			}
		}	
		else{
			this.sendMessage(channel, "@" + sender + " " + HeadsOrTails.HT_ERROR_STRING);
		}
	}
	
	private void onRockPaperScissorsMessage(String channel, String sender, String message){
		String userChoice = "";
		try{
			userChoice = message.substring(5);
		}
		catch(StringIndexOutOfBoundsException e){
			this.sendMessage(channel, "@" + sender + " " + RockPaperScissors.RPS_ERROR_MESSAGE);
			return;
		}
		boolean userChoiceIsValid = RockPaperScissors.checkUserChoice(userChoice);
		if(userChoiceIsValid){
			long endTimeRps = System.currentTimeMillis();
			if ( (endTimeRps - startTimeRps) > secondsToMilliseconds(TWITCH_TIME_BETWEEN_GAMES)){
				RockPaperScissors rps = new RockPaperScissors(userChoice);
				this.sendMessage(channel, sender + " " + rps.getGameResult());
				
				//Reset start time
				startTimeRps = System.currentTimeMillis();
			}
		}
		else{
			this.sendMessage(channel, "@" + sender + " " + RockPaperScissors.RPS_ERROR_MESSAGE);
		}
	}
	
	
	@Override
	protected void onMessage(String channel, String sender, String login, String hostname, String message){

		//Timeout user if message is over 400 characters
		if (message.length() > 400 && !isOp(sender, channel)){
			this.setMessageDelay(0);
			this.sendMessage(channel, "/timeout " + sender + " 60");
			this.sendMessage(channel, sender + " has been timed out. Reason: message too long.");
		}
		
		//Timeout user if message contains URL
		else if (message.contains("http://") && !isOp(sender, channel)){
			this.setMessageDelay(0);
			this.sendMessage(channel, "/timeout " + sender + " 120");
			this.sendMessage(channel, sender + " has been timed out. Reason: no links allowed!");
		}
		
		// Return how long the stream has been online
		else if (message.equals("!uptime")){
			getStreamUptime(channel);
		}
		
		// Heads or tails game
		else if (message.startsWith("!flip")){
			onHeadsOrTailsMessage(channel, sender, message);
		}
		
		// Rock, Paper, Scissors game
		else if (message.startsWith("!rps")){
			onRockPaperScissorsMessage(channel, sender, message);
		}
		
		
		
		
		
		
	}
}


