import java.io.IOException;
import java.net.URL;


public class TwitchModChecker {
	
	public static void readChannelJSON(String tmiURL){
		
		try{
			URL url = new URL(tmiURL);
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}

}
