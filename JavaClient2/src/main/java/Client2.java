import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Client2 {
  protected static AtomicInteger SUCCESSFUL_REQUESTS = new AtomicInteger(0);
  private static final int INITIAL_THREAD_COUNT = 10;
  private static final int INITIAL_CALLS_PER_THREAD = 100;
  private static final int CALLS_PER_THREAD = 1000;

  public static final CopyOnWriteArrayList<Info> INFO_LIST = new CopyOnWriteArrayList<>();
  public static class AlbumMetaData {

    public String getAlbumId() {
      return albumId;
    }

    public void setAlbumId(String albumId) {
      this.albumId = albumId;
    }

    public int getImageSize() {
      return imageSize;
    }

    public void setImageSize(int imageSize) {
      this.imageSize = imageSize;
    }

    private String albumId;
    private int imageSize;
  }

  private static String sendPostRequest(String serverUrl) {
    long startTime = System.currentTimeMillis();
    JsonObject jsonProfile = new JsonObject();
    jsonProfile.addProperty("artist", "BBB");
    jsonProfile.addProperty("title", "ToTheMoon");
    jsonProfile.addProperty("year", "1999");
    //We use CRLF instead of CR
    String CRLF = "\r\n";
    //Instead of using random number, use time as boundary also can ensure uniqueness
    String boundary = Long.toHexString(System.currentTimeMillis());

    //String content = "abc";
    //byte[] imageBytes = content.getBytes(StandardCharsets.UTF_8);
    String basePath = new File("").getAbsolutePath();
    String fileName = "nmtb.png";
    String imagePath = basePath + '/' + fileName;

    try {
      URL url = new URL(serverUrl);
      HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
      httpURLConnection.setDoOutput(true);
      httpURLConnection.setRequestMethod("POST");
      httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

      OutputStream outputStream = httpURLConnection.getOutputStream();
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream,
          StandardCharsets.UTF_8), true);
      writer.append("--" + boundary).append(CRLF);
      writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"").append(CRLF);
      writer.append("Content-Type: image/jpeg").append(CRLF);
      writer.append(CRLF).flush();

      File file = new File(imagePath);
      byte[] imageBytes = Files.readAllBytes(file.toPath());
      outputStream.write(imageBytes);
      outputStream.flush();
      writer.append(CRLF).flush();
      writer.append("--" + boundary).append(CRLF);
      writer.append("Content-Disposition: form-data; name=\"profile\"").append(CRLF);
      writer.append("Content-Type: application/json; charset=UTF-8").append(CRLF);
      writer.append(CRLF).append(jsonProfile.toString()).append(CRLF).flush();
      writer.append("--" + boundary + "--").append(CRLF).flush();


      int responseCode = httpURLConnection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          response.append(line);
        }
        reader.close();
        Gson gson = new Gson();
        AlbumMetaData albumMetaData = gson.fromJson(response.toString(), AlbumMetaData.class);
        SUCCESSFUL_REQUESTS.incrementAndGet();
        System.out.println(Thread.currentThread().getName() + "POST request succeeded! AlbumId: " + albumMetaData.getAlbumId());
        long endTime = System.currentTimeMillis();
        INFO_LIST.add(new Info(startTime, "POST", endTime - startTime, responseCode));
        return albumMetaData.getAlbumId();
      } else {
        System.out.println(Thread.currentThread().getName() + "POST request failed! Response status code is: " + responseCode);
        return null;
      }
    } catch (Exception e) {
      System.out.println(Thread.currentThread().getName() + "POST request failed! Error: " + e.getMessage());
      return null;
    }
  }

  private static void sendGetRequest(String serverUrl) {
    long startTime = System.currentTimeMillis();
    int responseCode = 0;
    try {
      URL url = new URL(serverUrl);
      HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
      httpURLConnection.setRequestMethod("GET");
      httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
      responseCode = httpURLConnection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuffer response = new StringBuffer();
        String line;
        while((line = reader.readLine()) != null) {
          response.append(line);
        }
        reader.close();
        SUCCESSFUL_REQUESTS.incrementAndGet();
        System.out.println(Thread.currentThread().getName() + "GET request succeeded! Response String is: " + response.toString());
      } else {
        System.out.println(Thread.currentThread().getName() + "GET request failed! Response status code is: " + responseCode);
      }
    } catch (Exception e) {
      System.out.println(Thread.currentThread().getName() + "GET request failed! Error: " + e.getMessage());
    }
    long endTime = System.currentTimeMillis();
    INFO_LIST.add(new Info(startTime, "GET", endTime - startTime, responseCode));
  }

  private static void initRequest(String serverUrl) {
    String albumId = sendPostRequest(serverUrl);
    for(int i = 0; i < INITIAL_CALLS_PER_THREAD; i++) {
      sendGetRequest(serverUrl + "/" + albumId);
    }
  }

  private static void sendRequest(String serverUrl) {
    for(int i = 0; i < CALLS_PER_THREAD; i++) {
      String albumId = sendPostRequest(serverUrl);
      sendGetRequest(serverUrl + "/" + albumId);
    }

  }


  public static void main(String[] args) throws InterruptedException {

    if (args.length != 4) {
      System.out.println("Run main method need 4 parameters: <threadGroupSize> <numThreadGroups> <delay> <IPAddr>");
      return;
    }

    int threadGroupSize = Integer.parseInt(args[0]);
    int numThreadGroups = Integer.parseInt(args[1]);
    int delay = Integer.parseInt(args[2]);
    String IPAddr = args[3];

    long startTime, endTime;

    //Test base case initialization
    ExecutorService servicePool = Executors.newFixedThreadPool(10);
    for (int i = 0; i < INITIAL_THREAD_COUNT; i++) {
      servicePool.submit(() -> {
        initRequest(IPAddr);
      });
    }
    servicePool.shutdown();
    servicePool.awaitTermination(3, TimeUnit.HOURS);

    startTime = System.currentTimeMillis();
    //Test POST and GET request
    SUCCESSFUL_REQUESTS.set(0);
    ExecutorService executorService = Executors.newFixedThreadPool(threadGroupSize);
    for (int i = 0; i < numThreadGroups; i++) {
      for (int j = 0; j < threadGroupSize; j++) {
        executorService.submit(() -> {
          sendRequest(IPAddr);
        });
      }
      Thread.sleep(1000L * delay);
    }
    executorService.shutdown();
    executorService.awaitTermination(3, TimeUnit.HOURS);

    endTime = System.currentTimeMillis();

    DecimalFormat decimalFormat = new DecimalFormat("#.##");
    double wallTime = (endTime - startTime) * 0.001;
    SaveInfoToCsv handler = new SaveInfoToCsv(INFO_LIST);

    System.out.println("----------------------------------------------------");
    System.out.println("-------- Printing Results For JavaClient2 --------");
    //System.out.println("---------Go Server----------------------");
    System.out.println("---------Java Server----------------------");
    System.out.println("threadGroupSize : " + threadGroupSize);
    System.out.println("numThreadGroups : " + numThreadGroups);
    System.out.println("delay : " + delay);
    System.out.println("Number of Successful Requests: " + SUCCESSFUL_REQUESTS.get());
    System.out.println("Number of failed Requests: " + (threadGroupSize * numThreadGroups * CALLS_PER_THREAD * 2 - SUCCESSFUL_REQUESTS.get()));
    System.out.println("Wall Time: " + decimalFormat.format(wallTime) + " (sec)");
    System.out.println("Throughput: " + decimalFormat.format(SUCCESSFUL_REQUESTS.get() / wallTime) + " (req/sec)");
    System.out.println("\n\n");
    handler.printInfo("POST");
    handler.printInfo("GET");
    String csvName = "Java_" + threadGroupSize + "_" + numThreadGroups + "_" + delay + ".csv";
    //String csvName = "Go_" + threadGroupSize + "_" + numThreadGroups + "_" + delay + ".csv";
    handler.writeInfoToCsv(csvName);
  }
}
