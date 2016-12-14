import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;

public class test 
{
	public static void main(String[] args) {
		String path = "https://s3.amazonaws.com/stationbuilder-files/update.txt";
        StringBuffer sb = new StringBuffer();
        String line = null;
        BufferedReader reader = null;
        try {
          URL url = new URL(path);
          HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
          urlConnection.connect();
          reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
          while ((line = reader.readLine()) != null) {
            sb.append(line);
          }
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          try {
            if (reader != null) {
              reader.close();
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        String info = sb.toString();
        String apkVersion = info.split("&")[1];
        System.out.println(apkVersion);
	}
}