import java.util.List;

/**
 * Calculate mean, median, p99, min, max
 * parameter is a sorted list of latencies
 */
public class CalculationUtils {
  public static double calculateMean(List<Long> latencies) {
    long sum = 0;
    for (Long latency : latencies) {
      sum += latency;
    }
    double mean = (double) sum / latencies.size();
    return mean;
  }

  public static double calculateMedian(List<Long> latencies) {
    int listSize = latencies.size();
    if (listSize % 2 == 0) {
      return (latencies.get(listSize / 2 - 1) + latencies.get(listSize / 2)) / 2.0;
    }
    return latencies.get(listSize / 2);
  }

  public static long calculateP99(List<Long> latencies) {
    return latencies.get((int)(0.99 * latencies.size()));
  }

  public static long calculateMin(List<Long> latencies) {
    return latencies.get(0);
  }

  public static long caculateMax(List<Long> latencies) {
    return latencies.get(latencies.size() - 1);
  }
}
