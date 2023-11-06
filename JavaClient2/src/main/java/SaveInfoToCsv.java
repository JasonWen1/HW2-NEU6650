import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SaveInfoToCsv {

  private List<Info> infoList;

  public SaveInfoToCsv(List<Info> infoList) {
    this.infoList = infoList;
  }

  public SaveInfoToCsv() {
    infoList = new ArrayList<>();
  }

  public void printInfo(String requestType) {
    List<Long> latencies = new ArrayList<>();
    for(Info info : infoList) {
      if(info.requestType.equals(requestType)) {
        latencies.add(info.latency);
      }
    }
    Collections.sort(latencies);

    double mean = CalculationUtils.calculateMean(latencies);
    double median = CalculationUtils.calculateMedian(latencies);
    long p99 = CalculationUtils.calculateP99(latencies);
    long min = CalculationUtils.calculateMin(latencies);
    long max = CalculationUtils.caculateMax(latencies);
    System.out.println("The information of request type: " + requestType);
    System.out.println("The mean response time is: " + mean + "ms");
    System.out.println("The median response time is: " + median + "ms");
    System.out.println("The p99 response time is: " + p99 + "ms");
    System.out.println("The minimum response time is: " + min + "ms");
    System.out.println("The maximum response time is: " + max + "ms\n");
  }

  public void writeInfoToCsv(String csvName) {
    String[] header = {"startTime", "latency", "latency", "responseCode"};

    try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(csvName))
        .withSeparator(ICSVWriter.DEFAULT_SEPARATOR)
        .withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER)
        .withEscapeChar(ICSVWriter.NO_ESCAPE_CHARACTER)
        .withLineEnd(ICSVWriter.DEFAULT_LINE_END)
        .build()) {

      writer.writeNext(header);

      for (Info info : infoList) {
        writer.writeNext(new String[]{String.valueOf(info.startTime), info.requestType, String.valueOf(info.latency), String.valueOf(info.responseCode)});
      }

      System.out.println("Info has already been written to: " + csvName);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
