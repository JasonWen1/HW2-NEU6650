/**
 * Info represents the data we save in the csv file,
 * it contains the following elements:
 * {startTime, requestType, latency, responseCode}
 */
public class Info {
  public long startTime;
  public String requestType;
  public long latency;
  public int responseCode;

  /**
   * Constructor for Info class
   */
  public Info(long startTime, String requestType, long latency, int responseCode) {
    this.startTime = startTime;
    this.requestType = requestType;
    this.latency = latency;
    this.responseCode = responseCode;
  }
}
