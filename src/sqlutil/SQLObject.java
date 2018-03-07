package sqlutil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.json.JSONObject;

public class SQLObject
{
  public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private String tableName;
  private String createdAt;
  private String updatedAt;
  private long objectId;
  private static final String TAG_TABLE_NAME = "tableName";
  private static final String TAG_CREATE_AT = "CREATE_AT";
  private static final String TAG_UPDATE_AT = "UPDATE_AT";
  private static final String TAG_DATA = "DATA";
  private static final String TAG_TARGET = "TARGET";
  private static final String TAG_DELETE = "TARGET_DELETE";
  private static final String TAG_CREATE = "TARGET_CREATE";
  private static final String TAG_SAVE = "TARGET_SAVE";
  private static final String TAG_UPDATE = "TARGET_UPDATE";
  private static final String TAG_ALTER = "TARGET_ALTER";
  
  public String getUpdatedAt()
  {
    return this.updatedAt;
  }
  
  public String getCreatedAt()
  {
    return this.createdAt;
  }
  
  public long getObjectId()
  {
    return this.objectId;
  }
  
  public String getTableName()
  {
    return this.tableName;
  }
  
  public SQLObject()
  {
    this.tableName = "m_"+getClass().getSimpleName();
    this.objectId = -1L;
  }
  
  private void buildTable(String target, final OnGetResultListener listener)
  {
    final JSONObject postData = new JSONObject();
    JSONObject data = new JSONObject();
    HashMap<String, Class> fieldsMap = getAllFieldsAndTypes();
    
    Gson gson = new GsonBuilder().serializeNulls().create();
    this.createdAt = dateFormat.format(new Date());
    this.updatedAt = dateFormat.format(new Date());
    data = new JSONObject(gson.toJson(this));
    for (String key : data.keySet()) {
      data.put(key, ((Class)fieldsMap.get(key)).getSimpleName());
    }
    data.put("tableName", this.tableName);
    
    postData.put("APP_KEY", SQLUtilInitializer.getKey());
    postData.put("TARGET", target);
    postData.put("DATA", data);
    
    new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          URL url = new URL(FinalValue.SQL_DATA_INTERFACE_ADDRESS);
          HttpURLConnection connection = (HttpURLConnection)url.openConnection();
          
          connection.setRequestMethod("POST");
          connection.setConnectTimeout(5000);
          connection.setRequestProperty("Content-type", "application/json");
          connection.setDoOutput(true);
          PrintStream printStream = new PrintStream(connection.getOutputStream());
          printStream.print(postData.toString());
          
          BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          
          JSONObject response = new JSONObject(reader.readLine().trim());
          String eMessage = response.getString("EXCEPTION");
          if ((eMessage != null) && (eMessage.equals("no error!"))) {
            listener.done(null);
          } else {
            listener.done(new Exception(eMessage));
          }
        }
        catch (Exception e)
        {
          listener.done(e);
        }
      }
    })
    
      .start();
  }
  
  public HashMap<String, Class> getAllFieldsAndTypes()
  {
    HashMap<String, Class> fieldsMap = new HashMap();
    for (Class clazz = getClass(); clazz != Object.class; clazz = clazz.getSuperclass())
    {
      Field[] fields = clazz.getDeclaredFields();
      
      System.out.println(clazz.toString());
      for (int i = 0; i < fields.length; i++) {
        fieldsMap.put(fields[i].getName(), fields[i].getType());
      }
    }
    return fieldsMap;
  }
  
  public void save(final SaveListener listener)
  {
    System.out.println("well");
    try
    {
      checkInitializerState();
      if (this.objectId != -1L) {
        throw new IllegalArgumentException("an existed object can not be save twice!");
      }
      JSONObject data = null;
      Gson gson = new GsonBuilder().serializeNulls().create();
      this.createdAt = dateFormat.format(new Date());
      this.updatedAt = dateFormat.format(new Date());
      data = new JSONObject(gson.toJson(this));
      
      System.out.println(data.toString());
      
      final JSONObject postData = new JSONObject();
      
      postData.put("APP_KEY", SQLUtilInitializer.getKey());
      postData.put("TARGET", "TARGET_SAVE");
      postData.put("DATA", data);
      
      new Thread(new Runnable()
      {
        public void run()
        {
          try
          {
            URL url = new URL(FinalValue.SQL_DATA_INTERFACE_ADDRESS);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setRequestProperty("Content-type", "application/json");
            connection.setDoOutput(true);
            PrintStream printStream = new PrintStream(connection.getOutputStream());
            printStream.print(postData.toString());
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            
            JSONObject response = new JSONObject(reader.readLine().trim());
            String eMessage = response.getString("EXCEPTION");
            eMessage = SQLObject.this.errorFilter(eMessage);
            if (eMessage == null)
            {
              listener.saveResult(new Exception("unknown error!"));
              return;
            }
            if (eMessage.equals("no error!"))
            {
              SQLObject.this.objectId = response.getLong("objectId");
              listener.saveResult(null);
              return;
            }
            if (eMessage.equals("NO TABLE"))
            {
              SQLObject.this.buildTable("TARGET_CREATE", new OnGetResultListener()
              {
                public void done(Exception e)
                {
                  if (e == null) {
                    SQLObject.this.save(listener);
                  } else {
                	  listener.saveResult(e);
                  }
                }
              });
              return;
            }
            if (eMessage.equals("NO COLUMN")) {
              SQLObject.this.buildTable("TARGET_ALTER", new OnGetResultListener()
              {
                public void done(Exception e)
                {
                  if (e == null) {
                    SQLObject.this.save(listener);
                  } else {
                    listener.saveResult(e);
                  }
                }
              });
            } else {
              listener.saveResult(new Exception(eMessage));
            }
          }
          catch (Exception e)
          {
            listener.saveResult(e);
          }
        }
      })
      
        .start();
    }
    catch (Exception e)
    {
      listener.saveResult(e);
    }
  }
  
  private String errorFilter(String eMessage)
  {
    String[] ems = eMessage.split(" ");
    String ans;
    if (ems.length > 3)
    {
      int length = ems.length;
      if ((ems[0].equals("Table")) && 
        (ems[(length - 2)].equals("doesn't")) && 
        (ems[(length - 1)].equals("exist"))) {
        return "NO TABLE";
      }
      if ((ems[0].equals("Unknown")) && 
        (ems[1].equals("column"))) {
        return "NO COLUMN";
      }
      ans = eMessage;
    }
    else
    {
      ans = eMessage;
    }
    return ans;
  }
  
  public void update(final UpdateListener listener)
  {
    try
    {
      checkInitializerState();
      if (this.objectId == -1L) {
        throw new IllegalArgumentException("an object can not be change before save!");
      }
      Gson gson = new GsonBuilder().serializeNulls().create();
      JSONObject data = null;
      
      this.updatedAt = dateFormat.format(new Date());
      data = new JSONObject(gson.toJson(this));
      
      final JSONObject postData = new JSONObject();
      
      postData.put("APP_KEY", SQLUtilInitializer.getKey());
      postData.put("TARGET", "TARGET_UPDATE");
      postData.put("DATA", data);
      
      new Thread(new Runnable()
      {
        public void run()
        {
          try
          {
            URL url = new URL(FinalValue.SQL_DATA_INTERFACE_ADDRESS);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setRequestProperty("Content-type", "application/json");
            connection.setDoOutput(true);
            PrintStream printStream = new PrintStream(connection.getOutputStream());
            printStream.print(postData.toString());
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            
            JSONObject response = new JSONObject(reader.readLine().trim());
            String eMessage = response.getString("EXCEPTION");
            if ((eMessage != null) && (eMessage.equals("no error!"))) {
              listener.updateResult(null);
            } else {
              listener.updateResult(new Exception(eMessage));
            }
          }
          catch (Exception e)
          {
            listener.updateResult(e);
          }
        }
      })
      
        .start();
    }
    catch (Exception e)
    {
      listener.updateResult(e);
      return;
    }
  }
  
  public void delete(final DeleteListener listener)
  {
    try
    {
      checkInitializerState();
      if (this.objectId == -1L) {
        throw new IllegalArgumentException("an  object can not be delete before save!");
      }
      JSONObject data = null;
      Gson gson = new GsonBuilder().serializeNulls().create();
      this.createdAt = dateFormat.format(new Date());
      this.updatedAt = dateFormat.format(new Date());
      data = new JSONObject(gson.toJson(this));
      
      final JSONObject postData = new JSONObject();
      
      postData.put("APP_KEY", SQLUtilInitializer.getKey());
      postData.put("TARGET", "TARGET_DELETE");
      postData.put("DATA", data);
      
      new Thread(new Runnable()
      {
        public void run()
        {
          try
          {
            URL url = new URL(FinalValue.SQL_DATA_INTERFACE_ADDRESS);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-type", "application/json");

            JSONObject object = new JSONObject();
            PrintStream printStream = new PrintStream(connection.getOutputStream());
            printStream.print(postData.toString());
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            
            JSONObject response = new JSONObject(reader.readLine().trim());
            String eMessage = response.getString("EXCEPTION");
            if ((eMessage != null) && (eMessage.equals("no error!"))) {
              listener.deleteResult(null);
            } else {
              listener.deleteResult(new Exception(eMessage));
            }
          }
          catch (Exception e)
          {
            listener.deleteResult(e);
          }
        }
      })
      
        .start();
    }
    catch (Exception e)
    {
      listener.deleteResult(e);
    }
  }
  
  private void checkInitializerState()
    throws IllegalAccessException
  {
    if (SQLUtilInitializer.getInitState() != SQLUtilInitializer.InitState.STATE_INIT_FINISH) {
      throw new IllegalAccessException("SQLUtil is not finished its initialize!");
    }
  }
  
  public void setObjectId(int objectId)
  {
    this.objectId = objectId;
  }
}
