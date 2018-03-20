package com.ibm.hodgepodge;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class HodgePodge {

  private static TimeZone UTC = TimeZone.getTimeZone("UTC");

  //// toDate
  //// Quoting the JavaSE docs: "the Date class is intended to reflect coordinated universal time (UTC)"

  // Convert Calendar to Date
  public final static Date toDate (final Calendar cal) {
    return cal.getTime();
  }

  // Convert Instant to Date
  public final static Date toDate (final Instant ins) {
    return Date.from(ins);
  }

  // Convert ZonedDateTime to Date
  public final static Date toDate (final ZonedDateTime zdt) {
    ZonedDateTime zdtutc = zdt.withZoneSameInstant(ZoneOffset.UTC);
    return Date.from(zdtutc.toInstant());
  }

  // Convert LocalDate to Date (in UTC, at start of day)
  public final static Date toDate (final LocalDate ld) {
    return Date.from(ld.atStartOfDay(ZoneOffset.UTC).toInstant());
  }

  // Convert LocalTime to Date (in UTC, on specified date)
  public final static Date toDate (final LocalTime lt, final LocalDate ondate) {
    LocalDateTime ldt = lt.atDate(ondate);
    return Date.from(ldt.atZone(ZoneOffset.UTC).toInstant());
  }

  // Convert LocalTime to Date (local time today)
  public final static Date toDate (final LocalTime lt) {
    return HodgePodge.toDate(lt, LocalDate.now());
  }

  // Convert LocalDateTime to Date
  public final static Date toDate (final LocalDateTime ldt) {
    // Date is UTC, so convert in UTC
    return Date.from(ldt.atZone(ZoneOffset.UTC).toInstant());
  }

  //// to Calendar

  // Convert Date to Calendar
  public final static Calendar toCalendar (final Date d) {
    return HodgePodge.toCalendar(d, UTC);
  }

  public final static Calendar toCalendar (final Date d, final TimeZone z) {
    GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance(z);
    cal.setTime(d);
    return cal;
  }

  // Convert Instant to Calendar
  public final static Calendar toCalendar (final Instant ins) {
    GregorianCalendar cal = new GregorianCalendar(UTC);
    cal.setTimeInMillis(ins.toEpochMilli());
    return cal;
  }

  // Convert ZonedDateTime to Calendar
  public final static Calendar toCalendar (final ZonedDateTime zdt) {
    return GregorianCalendar.from(zdt);
  }

  // Convert LocalDate to Calendar
  public final static Calendar toCalendar (final LocalDate ld) {
    Calendar cal = new GregorianCalendar();
    cal.set(ld.getYear(), ld.getMonthValue() - 1, ld.getDayOfMonth());
    return cal;
  }

  // Convert LocalTime to Calendar
  public final static Calendar toCalendar (final LocalTime lt) {
    Calendar cal = new GregorianCalendar();
    cal.set(Calendar.HOUR_OF_DAY, lt.getHour());
    cal.set(Calendar.MINUTE, lt.getMinute());
    cal.set(Calendar.SECOND, lt.getSecond());
    return cal;
  }

  // Convert LocalDateTime to Calendar
  public final static Calendar toCalendar (final LocalDateTime ldt) {
    return GregorianCalendar.from(ZonedDateTime.of(ldt, ZoneId.systemDefault()));
  }

  // Convert Date to Instant
  public final static Instant toInstant (final Date d) {
    return d.toInstant();
  }

  // Convert Calendar to Instant
  public final static Instant toInstant (final Calendar cal) {
    return cal.toInstant();
  }

  // Convert ZonedDateTime to Instant
  public final static Instant toInstant (final ZonedDateTime zdt) {
    return zdt.toInstant();
  }

  // Convert LocalDate to Instant - inappropriate as Instant requires a time

  // Convert LocalTime to Instant - inappropriate as Instant requires a date

  // Convert LocalDateTime to Instant
  public final static Instant toInstant (final LocalDateTime ldt, final ZoneOffset z) {
    return ldt.toInstant(z);
  }

  // Convert Date to ZonedDateTime
  public final static ZonedDateTime toZonedDateTime (final Date d) {
    return ZonedDateTime.ofInstant(d.toInstant(), ZoneOffset.UTC);
  }

  /**
   * Converts the specified Date in UTC to a ZonedDateTime, in whatever
   * the local time zone is at the appropriate Instant.
   *
   * @param d
   *          the Date
   * @return
   */
  public final static ZonedDateTime toZonedDateTimeLocal (final Date d) {
    Instant ins = d.toInstant();
    ZoneId systemZone = ZoneId.systemDefault();
    ZoneOffset offset = systemZone.getRules().getOffset(ins);
    return ZonedDateTime.ofInstant(ins, offset);
  }

  // Convert Calendar to ZonedDateTime
  public final static ZonedDateTime toZonedDateTime (final Calendar cal) {
    if (!(cal instanceof GregorianCalendar)) {
      throw new DateTimeException("Cannot convert non-Gregorian Calendars");
    }
    return ((GregorianCalendar) cal).toZonedDateTime();
  }

  // Convert Instant to ZonedDateTime
  public final static ZonedDateTime toZonedDateTime (final Instant ins, final ZoneId zone) {
    return ins.atZone(zone);
  }

  public final static ZonedDateTime toZonedDateTime (final Instant ins) {
    return ins.atZone(ZoneOffset.UTC);
  }

  // Convert LocalDate to ZonedDateTime - inappropriate

  // Convert LocalTime to ZonedDateTime - inappropriate

  // Convert LocalDateTime to ZonedDateTime - out of scope

  // Convert SqlDate to ZonedDateTime - inappropriate

  // Convert Date to LocalDate
  public final static LocalDate toLocalDate (final Date d) {
    return d.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
  }

  // Convert Calendar to LocalDate (assumes calendar is in the right time zone)
  public final static LocalDate toLocalDate (final Calendar cal) {
    return LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
  }

  // Convert Instant to LocalDate - out of scope

  // Convert ZonedDateTime to LocalDate - out of scope

  // Convert LocalTime to LocalDate - inappropriate

  // Convert LocalDateTime to LocalDate - out of scope

  // Convert Date to LocalTime
  public final static LocalTime toLocalTime (final Date d) {
    Instant ins = Instant.ofEpochMilli(d.getTime());
    return LocalDateTime.ofInstant(ins, ZoneOffset.UTC).toLocalTime();
  }

  // Convert Calendar to LocalTime
  public final static LocalTime toLocalTime (final Calendar cal) {
    return LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
  }

  // Convert Instant to LocalTime - out of scope

  // Convert ZonedDateTime to LocalTime - out of scope

  // Convert LocalDate to LocalTime - out of scope

  // Convert LocalDateTime to LocalTime - out of scope

  // Convert SQL Date to LocalTime - inappropriate

  // Convert Date to LocalDateTime
  public final static LocalDateTime toLocalDateTime (final Date d) {
    return LocalDateTime.ofInstant(d.toInstant(), ZoneOffset.UTC);
  }

  // Convert Calendar to LocalDateTime
  public final static LocalDateTime toLocalDateTime (final Calendar cal) {
    TimeZone tz = cal.getTimeZone();
    ZoneId zid = tz == null ? ZoneId.systemDefault() : tz.toZoneId();
    return LocalDateTime.ofInstant(cal.toInstant(), zid);
  }

  // Convert Instant to LocalDateTime - out of scope

  // Convert ZonedDateTime to LocalDateTime - out of scope

  // Convert LocalDate to LocalDateTime - out of scope

  // Convert LocalTime to LocalDateTime - out of scope

}
