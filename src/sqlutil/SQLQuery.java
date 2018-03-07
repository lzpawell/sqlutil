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

public class SQLQuery<T>
{
  private ArrayList<String> whereConditionList = new ArrayList();
  private String limitCondition = null;
  private boolean hasSQLQueryCondition = false;
  private String SQLQyeryCondition;
  
  public void query(final QueryListener<T> listener)
  {
    new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          Class entityClass = (Class)((java.lang.reflect.ParameterizedType)listener.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
          JSONArray dataArray = null;
          LinkedList<T> dataList = new LinkedList();
          
          String tableName =  "m_"+ entityClass.getSimpleName();
          System.out.println(tableName);
          JSONObject requestPara = new JSONObject();
          
          String queryCondition = null;
          if (SQLQuery.this.hasSQLQueryCondition)
          {
            queryCondition = SQLQuery.this.SQLQyeryCondition;
          }
          else
          {
            StringBuffer conditionBuffer = new StringBuffer();
            boolean isFirstCondition = true;
            for (String condition : SQLQuery.this.whereConditionList)
            {
              if (isFirstCondition) {
                isFirstCondition = false;
              } else {
                conditionBuffer.append(" AND ");
              }
              conditionBuffer.append(condition);
            }
            if (SQLQuery.this.limitCondition != null) {
              conditionBuffer.append(" " + SQLQuery.this.limitCondition);
            }
            conditionBuffer.append(";");
            queryCondition = conditionBuffer.toString();
            
            System.out.println("���������� " + queryCondition);
          }
          requestPara.put("tableName", tableName);
          System.out.println(requestPara.get("tableName"));
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
          if ((eMessage != null) && (eMessage.equals("no error!")))
          {
            dataArray = response.getJSONArray("DATA");
            if (dataArray == null)
            {
              listener.queryResult(dataList, null);
              return;
            }
            Gson gson = new GsonBuilder().serializeNulls().create();
            for (int i = 0; i < dataArray.length(); i++)
            {
              T data = (T) gson.fromJson(dataArray.get(i).toString(), entityClass);
              dataList.add(data);
            }
            listener.queryResult(dataList, null);
          }
          else
          {
            listener.queryResult(null, new Exception(eMessage));
          }
        }
        catch (Exception e)
        {
          listener.queryResult(null, e);
        }
      }
    })
    
      .start();
  }
  
  public void queryObjectById(long objectId, QueryListener<T> listener)
  {
    this.hasSQLQueryCondition = false;
    whereEqualTo("objectId", Long.valueOf(objectId));
    query(listener);
  }
  
  public SQLQuery whereEqualTo(String where, Object equalTo)
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
  
  public SQLQuery whereLargerThan(String where, Object largerThan)
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
  
  public SQLQuery whereSmallerThan(String where, Object smallerThan)
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
  
  public SQLQuery limit(int offset, int count)
  {
    if (this.hasSQLQueryCondition) {
      return this;
    }
    this.limitCondition = ("LIMIT " + offset + " " + count);
    return this;
  }
  
  public SQLQuery setQueryConditionBySQL(String SQLQueryCondition)
  {
    this.hasSQLQueryCondition = true;
    this.SQLQyeryCondition = SQLQueryCondition;
    return this;
  }
}
