package sqlutil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class SQLUtilInitializer
{
  private static InitState initState = InitState.STATE_BEFORE;
  private static String key = null;
  
  public static InitState getInitState()
  {
    return initState;
  }
  
  public static String getKey()
  {
    return key;
  }
  
  public static void resetServerAddress(String address)
  {
    FinalValue.setSqlServerBaseAddress(address);
  }
  
  public static void initialize(String key, final InitializerListener listener)
  {
	  
    if (initState == InitState.STATE_ON_INIT) {
      listener.initializeResult(new IllegalAccessException("SQLUtilInitialize is running!"));
    }
    
    synchronized (initState)
    {
      initState = InitState.STATE_ON_INIT;
    }
    
    
    SQLUtilInitializer.key = key;
    
    new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          URL url = new URL(FinalValue.SQL_INITIALIZE_ADDRESS);
          HttpURLConnection connection = (HttpURLConnection)url.openConnection();
          connection.setRequestMethod("POST");
          connection.setConnectTimeout(8000);
          connection.setDoOutput(true);
          connection.setRequestProperty("Content-type", "application/json");

          
          JSONObject requestPara = new JSONObject();
          requestPara.put("APP_KEY", key);
          
          connection.getOutputStream().write(requestPara.toString().getBytes());
          if (connection.getResponseCode() != 200) {
            throw new RuntimeException("error! server not response 200!");
          }
          BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          
          JSONObject response = new JSONObject(reader.readLine().trim());
          if (response.getString("EXCEPTION").equals("no error!"))
          {
            listener.initializeResult(null);
            SQLUtilInitializer.initState = SQLUtilInitializer.InitState.STATE_INIT_FINISH;
            return;
          }
          listener.initializeResult(new Exception(response.getString("EXCEPTION")));
        }
        catch (Exception e)
        {
          listener.initializeResult(e);
        }
        SQLUtilInitializer.initState = SQLUtilInitializer.InitState.STATE_BEFORE;
      }
    })
    
      .start();
  }
  
  public static enum InitState
  {
    STATE_BEFORE,  STATE_ON_INIT,  STATE_INIT_FINISH;
  }
  
  public static abstract interface InitializerListener
  {
    public abstract void initializeResult(Exception paramException);
  }
}
