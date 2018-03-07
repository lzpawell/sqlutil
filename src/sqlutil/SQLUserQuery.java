package sqlutil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;

class SQLUserQuery
{
  private ArrayList<String> whereConditionList = new ArrayList();
  private String limitCondition = null;
  private boolean hasSQLQueryCondition = false;
  private Class userBeanClass;
  
  public SQLUserQuery(Class userBeanClass)
  {
    this.userBeanClass = userBeanClass;
  }
  
  public void query(final QueryUserListener listener)
  {
    new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          JSONArray dataArray = null;
          LinkedList<SQLUser> dataList = new LinkedList();
          
          String tableName = SQLUserQuery.this.userBeanClass.getSimpleName().toLowerCase();
          JSONObject requestPara = new JSONObject();
          
          String queryCondition = null;
          StringBuffer conditionBuffer = new StringBuffer();
          boolean isFirstCondition = true;
          for (String condition : SQLUserQuery.this.whereConditionList)
          {
            if (isFirstCondition) {
              isFirstCondition = false;
            } else {
              conditionBuffer.append(" AND ");
            }
            conditionBuffer.append(condition);
          }
          if (SQLUserQuery.this.limitCondition != null) {
            conditionBuffer.append(" " + SQLUserQuery.this.limitCondition);
          }
          conditionBuffer.append(";");
          queryCondition = conditionBuffer.toString();
          
          requestPara.put("tableName", tableName);
          requestPara.put("TARGET", "TARGET_QUERY");
          requestPara.put("APP_KEY", SQLUtilInitializer.getKey());
          requestPara.put("QUERY_CONDITION", queryCondition);
          
          URL url = new URL(FinalValue.SQL_DATA_INTERFACE_ADDRESS);
          HttpURLConnection connection = (HttpURLConnection)url.openConnection();
          
          connection.setRequestMethod("POST");
          connection.setConnectTimeout(5000);
          connection.setDoOutput(true);
          connection.setRequestProperty("Content-type", "application/json");

          
          PrintStream printStream = new PrintStream(connection.getOutputStream());
          printStream.print(requestPara.toString());
          
          BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          
          JSONObject response = new JSONObject(reader.readLine().trim());
          String eMessage = response.getString("EXCEPTION");
          eMessage = SQLUserQuery.this.errorFilter(eMessage);
          if ((eMessage != null) && (eMessage.equals("no error!")))
          {
            dataArray = response.getJSONArray("DATA");
            if (dataArray == null)
            {
              listener.done(dataList, null);
              return;
            }
            Gson gson = new GsonBuilder().serializeNulls().create();
            for (int i = 0; i < dataArray.length(); i++)
            {
              SQLUser data = (SQLUser)gson.fromJson(dataArray.get(i).toString(), SQLUserQuery.this.userBeanClass);
              dataList.add(data);
            }
            listener.done(dataList, null);
          }
          else
          {
            listener.done(null, new Exception(eMessage));
          }
        }
        catch (Exception e)
        {
          listener.done(null, e);
        }
      }
    })
    
      .start();
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
        return "no error!";
      }
      ans = eMessage;
    }
    else
    {
      ans = eMessage;
    }
    return ans;
  }
  
  public SQLUserQuery whereEqualTo(String where, Object equalTo)
  {
    if (this.hasSQLQueryCondition) {
      return this;
    }
    if (equalTo.getClass() == String.class) {
      this.whereConditionList.add(where + "='" + equalTo + "'");
    } else {
      this.whereConditionList.add(where + "=" + equalTo);
    }
    return this;
  }
  
  public SQLUserQuery whereLargerThan(String where, Object largerThan)
  {
    if (this.hasSQLQueryCondition) {
      return this;
    }
    if (largerThan.getClass() == String.class) {
      this.whereConditionList.add(where + ">'" + largerThan + "'");
    } else {
      this.whereConditionList.add(where + ">" + largerThan);
    }
    return this;
  }
  
  public SQLUserQuery whereSmallerThan(String where, Object smallerThan)
  {
    if (this.hasSQLQueryCondition) {
      return this;
    }
    if (smallerThan.getClass() == String.class) {
      this.whereConditionList.add(where + "<'" + smallerThan + "'");
    } else {
      this.whereConditionList.add(where + "<" + smallerThan);
    }
    return this;
  }
  
  public SQLUserQuery limit(int offset, int count)
  {
    if (this.hasSQLQueryCondition) {
      return this;
    }
    this.limitCondition = ("LIMIT " + offset + " " + count);
    return this;
  }
  
  public static abstract class QueryUserListener
  {
    public abstract void done(LinkedList<SQLUser> paramLinkedList, Exception paramException);
  }
}
