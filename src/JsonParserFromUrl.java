import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class JsonParserFromUrl {

    public static final String STREAM_OFFLINE = "null";

    public static String getStreamTimeStart(String streamUrl) throws IOException {
        URL url = new URL(streamUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.connect();

        JsonParser jsonParser = new JsonParser();
        JsonElement rootElement = jsonParser.parse(new InputStreamReader((InputStream) urlConnection.getContent()));
        JsonObject rootObject = rootElement.getAsJsonObject();
        if (rootObject.get("stream").toString().equals(STREAM_OFFLINE)) {
            return STREAM_OFFLINE;
        } else {
            JsonObject streamObj = rootObject.getAsJsonObject("stream");
            String createdAt = streamObj.get("created_at").toString();
            return createdAt;
        }
    }

    public static void getModeratorsFromUrl(String url) {
        // TODO
    }

    public static String currentUptimeOfStream(String channelName, long milliseconds) {

        long time = System.currentTimeMillis() - milliseconds;

        long days = TimeUnit.MILLISECONDS.toDays(time);
        time -= TimeUnit.DAYS.toMillis(days);

        long hours = TimeUnit.MILLISECONDS.toHours(time);
        time -= TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(time);


        StringBuilder sb = new StringBuilder();
        sb.append(channelName + " has been live for ");

        if (days > 0){
            sb.append(days + " days ");
        }
        if (hours > 0){
            sb.append(hours + " hours ");
        }
        if (minutes > 0){
            sb.append(minutes + " minutes ");
        }
        if (seconds > 0){
            sb.append(seconds + " seconds");
        }
        return sb.append(".").toString();
    }

}
