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
	
	//Time (in milliseconds) in which games are unplayable after someone plays one
	private static final int TWITCH_TIME_BETWEEN_GAMES = 10000;
	private static final int TWITCH_TIME_BETWEEN_UPTIME_MESSAGE = 30000;
	
	private static final int TWITCH_TIME_BETWEEN_RANDOM_MESSAGES = 500000;
	
	private static final String TWITCH_STREAM_JSON_URL = "https://api.twitch.tv/kraken/streams/" + BotProperties.TWITCH_CHANNEL_NAME.substring(1);
	
	//Bot name. Must be all lowercase and match assigned OAUTH key
	private static final String TWITCH_BOT_NAME = "fsocietybot";
	
	private static List<String> messages = new ArrayList<>();
	private static List<String> moderators = new ArrayList<String>();
	// static long startTimeOfStream;
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
	
	private static long streamUptimeToMilli() throws IOException{
		String startTime = JsonParserFromUrl.getStreamTimeStart(TWITCH_STREAM_JSON_URL).replace("\"", "");
		if (startTime.equalsIgnoreCase("null")){
			return 0;
		}
		else{
			long startTimeToMilliseconds = Instant.parse(startTime.replace("\"", "")).toEpochMilli();
			return startTimeToMilliseconds;
		}
	}
	
	private static String streamUptimeToString(long streamUptimeMilliseconds){
		String channelName = BotProperties.TWITCH_CHANNEL_NAME.substring(1);
		if (streamUptimeMilliseconds == 0){
			return channelName + " is currently offline.";
		}
		else{
			return JsonParserFromUrl.currentUptimeOfStream(channelName, streamUptimeMilliseconds);
		}
	}
	
	
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
	
	public void test(String sender, String channel){
		User[] users = getUsers(channel);
		
		for (User user : users){
			System.out.println(user.getNick() + " " + user.getPrefix());
		}
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
				Thread.sleep(TWITCH_TIME_BETWEEN_RANDOM_MESSAGES);
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
		
		else if (message.equals("!uptime")){
			long globalEndTime = System.currentTimeMillis();
			if ( (globalEndTime - globalStartTime) > TWITCH_TIME_BETWEEN_UPTIME_MESSAGE){
				try {
					this.sendMessage(channel, streamUptimeToString(streamUptimeToMilli()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// Reset start time
				globalStartTime = System.currentTimeMillis();
			}
		}
		
		// Heads or tails game
		else if (message.startsWith("!flip")){
			
			String userChoice = "";
			
			try{
				userChoice = message.substring(6);
			}
			catch (StringIndexOutOfBoundsException e){
				this.sendMessage(channel, "@" + sender + " " + HeadsOrTails.HT_ERROR_STRING);
				return;
			}
			boolean checkIfCorrect = HeadsOrTails.checkUserChoice(userChoice);
			if (checkIfCorrect){
				long endTimeFlip = System.currentTimeMillis();
				//Check if enough time has passed between previous game
				if ( (endTimeFlip - startTimeFlip) > TWITCH_TIME_BETWEEN_GAMES){
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
		
		else if (message.startsWith("!rps")){
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
				if ( (endTimeRps - startTimeRps) > TWITCH_TIME_BETWEEN_GAMES){
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
		
		
		
		
		
		
	}
}


