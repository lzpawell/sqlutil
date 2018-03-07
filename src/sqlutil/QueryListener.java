package sqlutil;

import java.util.List;

public abstract class QueryListener<T>
{
  public abstract void queryResult(List<T> paramList, Exception paramException);
}
