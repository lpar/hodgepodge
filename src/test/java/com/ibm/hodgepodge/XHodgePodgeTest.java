package com.ibm.hodgepodge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import lotus.domino.International;

class XHodgePodgeTest {

  private void runTest (final DateTimeFormatter dtf, final String datetime, final String correctdatetime) {
    ZonedDateTime zdt = ZonedDateTime.from(dtf.parse(datetime));
    String isodatetime = DateTimeFormatter.ISO_INSTANT.format(zdt);
    assertEquals(isodatetime, correctdatetime);
  }

  @Test
  void testMDY () {
    // e.g. USA
    International i18n = new International(":", "/", "MDY", false, "AM", "PM");
    DateTimeFormatter dtf = XHodgePodge.buildFormatter(i18n);
    this.runTest(dtf, "10/9/2018 11:10:09 PM MST", "2018-10-10T05:10:09Z");
    this.runTest(dtf, "1/11/1952 1:10:09 AM EDT", "1952-01-11T06:10:09Z");
    this.runTest(dtf, "2/29/2000 1:10:09 AM PST", "2000-02-29T09:10:09Z");
  }

  @Test
  void testDMY () {
    // e.g. Germany
    International i18n = new International(":", ".", "DMY", true, "AM", "PM");
    DateTimeFormatter dtf = XHodgePodge.buildFormatter(i18n);
    this.runTest(dtf, "8.9.2018 09:42:55 CET", "2018-09-08T07:42:55Z");
    this.runTest(dtf, "29.2.2000 15:44:55 CET", "2000-02-29T14:44:55Z");
    this.runTest(dtf, "29.12.2019 8:12:31 CET", "2019-12-29T07:12:31Z");
  }
  
  @Test
  void testYMD () {
    // e.g. Japan
    International i18n = new International(":", "-", "YMD", true, "AM", "PM");
    DateTimeFormatter dtf = XHodgePodge.buildFormatter(i18n);
    this.runTest(dtf, "2000-02-29 9:17:22 JST","2000-02-29T00:17:22Z");
    this.runTest(dtf, "2023-11-01 19:22:52 JST","2023-11-01T10:22:52Z");
    this.runTest(dtf, "1954-02-09 04:11:11 JST","1954-02-08T19:11:11Z");
  }

}
