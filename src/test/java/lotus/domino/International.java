package lotus.domino;

/**
 * This is a mock lotus.domino.International for testing XHodgePodge outside of a Domino environment.
 */
public class International {
  String timesep = ":";
  String datesep = "/";
  boolean isYMD = false;
  boolean isDMY = false;
  boolean isMDY = false;
  boolean is24Hour = true;
  String AMString = "AM";
  String PMString = "PM";

  public International(String timesep, String datesep, String ymddmymdy, boolean is24Hour,
      String amstring, String pmstring) {
    super();
    this.timesep = timesep;
    this.datesep = datesep;
    this.isYMD = ymddmymdy.equals("YMD");
    this.isDMY = ymddmymdy.equals("DMY");
    this.isMDY = ymddmymdy.equals("MDY");
    this.is24Hour = is24Hour;
    this.AMString = amstring;
    this.PMString = pmstring;
  }

  public String getTimeSep () {
    return timesep;
  }

  public String getDateSep () {
    return datesep;
  }

  public boolean isDateYMD () {
    return isYMD;
  }

  public boolean isDateDMY () {
    return isDMY;
  }

  public boolean isDateMDY () {
    return isMDY;
  }

  public boolean isTime24Hour () {
    return is24Hour;
  }

  public String getAMString () {
    return AMString;
  }

  public String getPMString () {
    return PMString;
  }
}
