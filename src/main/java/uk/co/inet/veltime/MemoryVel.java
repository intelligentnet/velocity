package uk.co.inet.veltime;

import java.io.*;

import java.net.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.*;

// return class for standard velocity so all calculations can be done once
public class MemoryVel extends Vel
{
/**
 *  A class to handle general Persistence of short term data locally on the
 *  machine. It's very fast and suitable for tasks such as velocity over a
 *  short time frame (typically 24 hours) for which support functions are
 *  supplied. Other velocity tasks may require longer time intervals for which
 *  a traditional database would be more suitable.
 *
 *  This class also contains methods to support velocity. velocity can be done
 *  on any arbitrary key and value. The key is the particular objects (or
 *  set of objects) on which a velocity test is being done. If the value matches
 *  then the velocity has succeeded and a counter is incremented.
 *
 *  There are two types of counters available which can be used individually or
 *  together, namely a simple count of matches and a monetary value match,
 *  where the monetary value can change on each call. There is also methods
 *  where if the key matches then the value should match too, if it does
 *  not then the counter is increased.
 */
  private LongHashMap<Base> tm = new LongHashMap<>();

  public MemoryVel(String nm)
  {
    type = "MemoryVel";
  }

  protected Base get(long key)
  {
    return tm.get(key);
  }

  protected Base removeKey(long key)
  {
    return tm.remove(key);
  }

  public static void init(String mn)
  {
    init(mn, null, false);
  }

  public static void init(String mn, String fn, boolean ro)
  {
    if (! maps.containsKey(mn))
    {
      MemoryVel p = new MemoryVel(mn);

      maps.put(mn, p);
    }
  }

  public static Vel context(String db)
  {
    return context(db, "MemoryVel");
  }

  public String show(String key)
  {
    return tm.get(vhash(key)).toString();
  }

  public synchronized Base set(long key, Base item)
  {
    if (item == null)
      tm.remove(key);
    else
      tm.put(key, item);

    return item;
  }

  /**
   *  show the database
   *
   * @return    The string representation of the className
   */
  public String toString()
  {
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;

    for (LongHashMap.HashMapIterator<Base> it = (LongHashMap.HashMapIterator<Base>) tm.valuesIterator(); it.hasNext(); )
    {
      LongHashMap.Entry<Base> e = it.nextEntry();

      if (! first)
        sb.append(",");
      else
        first = false;

      sb.append(showItem(e.key, e.value));
    }

    sb.append("}");

    return sb.toString();
  }

  // Copy from one implementation to another
  public Vel copy(String type, String nm, boolean merge)
  {
    Vel p = getVel(type, nm);

    if (p != null)
    {
      for (LongHashMap.HashMapIterator<Base> it = (LongHashMap.HashMapIterator<Base>) tm.valuesIterator(); it.hasNext(); )
      {
        LongHashMap.Entry<Base> e = it.nextEntry();

        if (merge)
          p.merge(e.key, e.value, max);
        else
          p.set(e.key, e.value);
      }
    }

    return p;
  }

  // count records and display every Nth record
  public int count(int n)
  {
    int cnt = 0;

    for (LongHashMap.HashMapIterator<Base> it = (LongHashMap.HashMapIterator<Base>) tm.valuesIterator(); it.hasNext(); )
    {
      LongHashMap.Entry<Base> e = it.nextEntry();

      if ((cnt % n) == 0)
      {
        String item = showItem(e.key, e.value);

        System.out.println(cnt + " : " + item);
      }

      cnt++;
    }

    return cnt;
  }

  protected synchronized int purgeDb(int now)
  {
    int cnt = 0;

    for (Iterator<Base> it = tm.valuesIterator(); it.hasNext(); )
    {
      Base val = it.next();

      if (val != null)
      {
        int dt = 0;

        if (val instanceof Item)
          dt = ((Item) val).ts;
        else if (((Items) val).items[0] != null)
          dt = ((Items) val).items[0].ts;

        if (dt > 20000000 && now > dt)
        {
          it.remove();

          cnt++;
        }
      }
    }

    return cnt;
  }

  public int recCount()
  {
    return tm.size();
  }
}
