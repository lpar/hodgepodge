package com.ibm.hodgepodge;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeFormats {

  private final static SimpleDateFormat sdate = new SimpleDateFormat("yyyy-MM-dd");
  private final static SimpleDateFormat stime = new SimpleDateFormat("HH:mm:ss");
  private final static SimpleDateFormat sdatetime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
  
  private static TimeZone UTC = TimeZone.getTimeZone("UTC");

  public static String toDate (final Date x, TimeZone z) {
    synchronized (sdate) {
      sdate.setTimeZone(z);
      return sdate.format(x);
    }
  }

  public static String toTime (final Date x, TimeZone z) {
    synchronized (stime) {
      stime.setTimeZone(z);
      return stime.format(x);
    }
  }

  public static String toDateTime (final Date x, TimeZone z) {
    synchronized (sdatetime) {
      sdatetime.setTimeZone(z);
      return sdatetime.format(x);
    }
  }
  
public static String toDateTimeLocal (final Date x, TimeZone z) {
    synchronized (sdatetime) {
      sdatetime.setTimeZone(TimeZone.getDefault());
      return sdatetime.format(x);
    }
  }

public static String toDate(java.sql.Date sqldate) {
  return TimeFormats.toDate(sqldate, UTC);
}

  public static String toDate (final Calendar x) {
    return TimeFormats.toDate(x.getTime(), x.getTimeZone());
  }

  public static String toTime (final Calendar x) {
    return TimeFormats.toTime(x.getTime(), x.getTimeZone());
  }

  public static String toDateTime (final Calendar x) {
    return TimeFormats.toDateTime(x.getTime(), x.getTimeZone());
  }

  public static String toDate (final Object x) {
    if (x instanceof TemporalAccessor) {
      return DateTimeFormatter.ISO_DATE.format((TemporalAccessor) x);
    }
     if (x instanceof Calendar) {
      return TimeFormats.toDate((Calendar) x);
    } 
    if (x instanceof Date) {
      return TimeFormats.toDate((Date) x, UTC);
    } 
    return "[toDate can't handle " + x.getClass().getName() + "]";
  }

  public static String toTime (final Object x) {
    if (x instanceof ZonedDateTime) {
      ZonedDateTime t = (ZonedDateTime) x;
      return DateTimeFormatter.ISO_TIME.format(t.truncatedTo(ChronoUnit.SECONDS));
    }
    if (x instanceof LocalDateTime) {
      LocalDateTime t = (LocalDateTime) x;
      return DateTimeFormatter.ISO_TIME.format(t.truncatedTo(ChronoUnit.SECONDS));
    }
    if (x instanceof LocalTime) {
      LocalTime t = (LocalTime) x;
      return DateTimeFormatter.ISO_TIME.format(t.truncatedTo(ChronoUnit.SECONDS));
    }
    if (x instanceof Calendar) {
      return TimeFormats.toTime((Calendar) x);
    } 
    if (x instanceof Date) {
      return TimeFormats.toTime((Date) x, UTC);
    }
    return "[toTime can't handle " + x.getClass().getName() + "]";
  }

  public static String toDateTime (final Object x) {
    if (x instanceof ZonedDateTime) {
      ZonedDateTime t = (ZonedDateTime) x;
      return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(t.truncatedTo(ChronoUnit.SECONDS));
    }
    if (x instanceof Instant) {
      Instant t = (Instant) x;
      return DateTimeFormatter.ISO_INSTANT.format(t.truncatedTo(ChronoUnit.SECONDS));
    }
    if (x instanceof LocalDateTime) {
      LocalDateTime t = (LocalDateTime) x;
      return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(t.truncatedTo(ChronoUnit.SECONDS));
    }
    if (x instanceof Calendar) {
      return TimeFormats.toDateTime((Calendar) x);
    } 
    if (x instanceof Date) {
      return TimeFormats.toDateTime((Date) x, UTC);
    }
    return "[toDateTime can't handle " + x.getClass().getName() + "]";
  }
  
}
