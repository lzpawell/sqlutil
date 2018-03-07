package sqlutil;

import java.util.LinkedList;

public class SQLUser
  extends SQLObject
{
  public static final String TAG_USER_ID = "userId";
  public static final String TAG_PASSWORD = "password";
  public static SQLUser currentUser = null;
  private String userId = null;
  private String password = null;
  
  public String getUserId()
  {
    return this.userId;
  }
  
  public void setUserId(String userId)
  {
    this.userId = userId;
  }
  
  public String getPassword()
  {
    return this.password;
  }
  
  public void setPassword(String password)
  {
    this.password = password;
  }
  
  public static SQLUser getCurrentUser()
  {
    return currentUser;
  }
  
  public void login(final OnGetResultListener listener)
  {
    Class entityClass = getClass();
    SQLUserQuery query = new SQLUserQuery(entityClass);
    
    query.whereEqualTo("userId", this.userId);
    query.whereEqualTo("password", this.password);
    query.query(new SQLUserQuery.QueryUserListener()
    {
      public void done(LinkedList<SQLUser> dataList, Exception e)
      {
        if (e == null)
        {
          if (dataList.size() == 0)
          {
            listener.onGetResult(new Exception("user not exists"));
          }
          else
          {
            SQLUser.currentUser = (SQLUser)dataList.get(0);
            listener.onGetResult(null);
          }
        }
        else {
          listener.onGetResult(e);
        }
      }
    });
  }
  
  public void logout(OnGetResultListener listener)
  {
    currentUser = null;
  }
  
  public void register(final OnGetResultListener listener)
  {
    Class entityClass = getClass();
    SQLUserQuery query = new SQLUserQuery(entityClass);
    
    query.whereEqualTo("userId", this.userId);
    query.query(new SQLUserQuery.QueryUserListener()
    {
      public void done(LinkedList<SQLUser> dataList, Exception e)
      {
        if (e == null)
        {
          if ((dataList == null) || ((dataList != null) && (dataList.size() == 0))) {
            SQLUser.this.save(new SaveListener()
            {
              public void saveResult(Exception e)
              {
                listener.onGetResult(e);
              }
            });
          } else {
            listener.onGetResult(new Exception("user name is exists!"));
          }
        }
        else {
          listener.onGetResult(e);
        }
      }
    });
  }
  
  public void unregister(final OnGetResultListener listener)
  {
    if (currentUser == null)
    {
      listener.onGetResult(new Exception("no current user"));
      return;
    }
    currentUser.delete(new DeleteListener()
    {
      public void deleteResult(Exception e)
      {
        listener.onGetResult(e);
      }
    });
  }
  
  public static abstract class OnGetResultListener
  {
    public abstract void onGetResult(Exception paramException);
  }
}
