package sqlutil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

public class KeyManager
{
  private static final String targetAddress = FinalValue.SQL_KEY_MANAGER_ADDRESS;
  private static final String TAG_REQUEST_TYPE = "REQUEST_TYPE";
  private static final String TAG_TYPE_REQUEST_KEY = "REQUEST_KEY";
  private static final String TAG_TYPE_UNREGISTER_KEY = "UNREGISTER_KEY";
  
  public static void requestKey(String appName, String appAuthor, final OnGetRequestCallback callback)
  {
    callback.onGetRequestCallbackStart();
    
    JSONObject requestPara = new JSONObject();
    try
    {
      requestPara.put("APP_NAME", appName);
      requestPara.put("APP_AUTHOR", appAuthor);
      requestPara.put("REQUEST_TYPE", "REQUEST_KEY");
    }
    catch (JSONException e)
    {
      e.printStackTrace();
      callback.onGetRequest(null, new Exception("para error" + e.toString()));
      return;
    }
    new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          URL url = new URL(KeyManager.targetAddress);
          HttpURLConnection connection = (HttpURLConnection)url.openConnection();
          
          connection.setRequestMethod("POST");
          connection.setConnectTimeout(5000);
          connection.setDoOutput(true);
          connection.setRequestProperty("Content-type", "application/json");

          PrintStream printStream = new PrintStream(connection.getOutputStream());
          
                    
          printStream.print(requestPara.toString());
          
          BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          
          JSONObject response = new JSONObject(reader.readLine().trim());
          String appKey = response.getString("APP_KEY").trim();
          if ((appKey == null) || (appKey.equals("")))
          {
            callback.onGetRequest(null, new Exception(response.getString("EXCEPTION")));
            return;
          }
          callback.onGetRequest(appKey, null);
        }
        catch (Exception e)
        {
          callback.onGetRequest(null, e);
        }
      }
    })
    
      .start();
  }
  
  public static void unregisterKey(String appKey, final OnGetUnregisterCallback callback)
  {
    callback.onGetUnregisterStart();
    
    JSONObject para = new JSONObject();
    try
    {
      para.put("REQUEST_TYPE", "UNREGISTER_KEY");
      para.put("APP_KEY", appKey);
      
      new Thread(new Runnable()
      {
        public void run()
        {
          try
          {
            URL url = new URL(KeyManager.targetAddress);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-type", "application/json");

            PrintStream printStream = new PrintStream(connection.getOutputStream());
            printStream.print(para.toString());
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            
            JSONObject response = new JSONObject(reader.readLine().trim());
            String eMessage = response.getString("EXCEPTION");
            if ((eMessage != null) && (eMessage.equals("no error!"))) {
              callback.onGetUnregisterResult(null);
            } else {
              callback.onGetUnregisterResult(new Exception(eMessage));
            }
          }
          catch (Exception e)
          {
            callback.onGetUnregisterResult(e);
          }
        }
      })
      
        .start();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      callback.onGetUnregisterResult(e);
    }
  }
  
  public static abstract interface OnGetRequestCallback
  {
    public abstract void onGetRequestCallbackStart();
    
    public abstract void onGetRequest(String paramString, Exception paramException);
  }
  
  public static abstract interface OnGetUnregisterCallback
  {
    public abstract void onGetUnregisterStart();
    
    public abstract void onGetUnregisterResult(Exception paramException);
  }
}
