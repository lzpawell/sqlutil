package sqlutil;

public class FinalValue
{
  public static String SQL_SERVER_BASE_ADDRESS = "http://192.168.191.1:8080";
  public static final String BASE_SQL_DATA_INTERFACE_ADDRESS = "/SQLUtil/DataAccessInterface";
  public static final String BASE_SQL_INITIALIZE_ADDRESS = "/SQLUtil/initialize";
  public static final String BASE_SQL_KEY_MANAGER_ADDRESS = "/SQLUtil/requestKey";
  public static String SQL_DATA_INTERFACE_ADDRESS = SQL_SERVER_BASE_ADDRESS + "/SQLUtil/DataAccessInterface";
  public static String SQL_INITIALIZE_ADDRESS = SQL_SERVER_BASE_ADDRESS + "/SQLUtil/initialize";
  public static String SQL_KEY_MANAGER_ADDRESS = SQL_SERVER_BASE_ADDRESS + "/SQLUtil/requestKey";
  public static final String TAG_EXCEPTION = "EXCEPTION";
  public static final String TAG_ERROR_NO_TABLE = "NO TABLE";
  public static final String TAG_ERROR_NO_COLUMN = "NO COLUMN";
  public static final String TAG_ERROR_NO_ERROR = "no error!";
  public static final String TAG_APP_KEY = "APP_KEY";
  public static final String TAG_OBJECT_ID = "objectId";
  public static final String TAG_TABLE_NAME = "tableName";
  public static final String TAG_CREATE_AT = "CREATE_AT";
  public static final String TAG_UPDATE_AT = "UPDATE_AT";
  public static final String TAG_DATA = "DATA";
  public static final String TAG_TARGET = "TARGET";
  public static final String TAG_DELETE = "TARGET_DELETE";
  public static final String TAG_SAVE = "TARGET_SAVE";
  public static final String TAG_UPDATE = "TARGET_UPDATE";
  public static final String TAG_QUERY = "TARGET_QUERY";
  public static final String TAG_QUERY_CONDITION = "QUERY_CONDITION";
  
  public static void setSqlServerBaseAddress(String address)
  {
    SQL_SERVER_BASE_ADDRESS = address;
    SQL_DATA_INTERFACE_ADDRESS = SQL_SERVER_BASE_ADDRESS + "/SQLUtil/DataAccessInterface";
    SQL_INITIALIZE_ADDRESS = SQL_SERVER_BASE_ADDRESS + "/SQLUtil/initialize";
  }
}