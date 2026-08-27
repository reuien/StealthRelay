package py;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlotInterface {

    //Data Example
    //xTimeValues:[1722232907000, 1722233147000, 1722233387000, 1722233627000, 1722233867000, 1722234107000, 1722234347000, 1722234587000, 1722234827000, 1722235067000, 1722235307000, 1722235547000, 1722235787000, 1722236027000, 1722236267000, 1722236507000, 1722236747000, 1722236987000, 1722237227000, 1722237467000, 1722237707000, 1722237947000, 1722238187000, 1722238427000, 1722238667000, 1722238907000, 1722239147000, 1722239387000, 1722239627000, 1722239867000, 1722240107000, 1722240347000, 1722240587000, 1722240827000, 1722241067000, 1722241307000, 1722241547000, 1722241787000, 1722242027000, 1722242267000, 1722242507000, 1722242747000, 1722242987000, 1722243227000, 1722243467000, 1722243707000, 1722243947000, 1722244187000, 1722244427000, 1722244667000, 1722244907000, 1722245147000, 1722245387000, 1722245627000, 1722245867000, 1722246107000, 1722246347000, 1722246587000, 1722246827000, 1722247067000]
    //yAveValues:[84.0, 90.8, 81.4, 82.3, 98.6, 78.1, 82.2, 73.8, 92.3, 86.2, 73.0, 82.7, 85.7, 88.5, 88.4, 85.9, 90.6, 86.0, 74.8, 94.4, 77.4, 83.8, 87.8, 84.2, 76.9, 95.3, 81.5, 81.5, 93.8, 84.4, 77.1, 89.3, 83.4, 80.6, 82.0, 96.5, 88.1, 83.8, 83.4, 91.3, 88.9, 95.2, 93.4, 88.4, 86.1, 97.2, 81.7, 86.7, 87.7, 76.2, 80.5, 70.0, 77.1, 84.7, 76.0, 89.7, 89.8, 88.3, 79.5, 80.4]
    //yCountValues:[2058, 2089, 2002, 2097, 2045, 4109]
    //statisticValues:[84.44680555555556, 20.224114547736537, 409.0148092399686, 1216034.0, 14400.0, 1.0858E8]
    public static void generatePlotLine(long[] xTimeValues, float[] yAveValues, long[] yCountValues, double[] statisticValues,
                                        double average, String outputFilePath) throws IOException {
        String[] categories = {"0-60", "60-70", "70-80", "80-90", "90-100", "100-120"};
        JSONObject statistic = new JSONObject();
        statistic.put("平均值", statisticValues[0]);
        statistic.put("标准差", statisticValues[1]);
        statistic.put("方差", statisticValues[2]);
        statistic.put("总和", statisticValues[3]);
        statistic.put("计数", statisticValues[4]);
        statistic.put("平方和", statisticValues[5]);
        createLineChart(average, yCountValues, categories, statistic, xTimeValues, yAveValues, outputFilePath);
    }

    public static void generatePlotBar(long[] yCountValues2, double[] statisticValues, String outputFilePath) {
        JSONObject statistic = new JSONObject();
        statistic.put("平均值", statisticValues[0]);
        statistic.put("标准差", statisticValues[1]);
        statistic.put("方差", statisticValues[2]);
        statistic.put("总和", statisticValues[3]);
        statistic.put("计数", statisticValues[4]);
        statistic.put("平方和", statisticValues[5]);
        String[] categories2 = {"0-60", "60-70", "70-80", "80-90", "90-100", "100-120"};
        JSONObject statisticValues2 = new JSONObject(statistic.toString());
        // 调用生成第二个条形图的方法
        createBarChartNew(yCountValues2, categories2, statisticValues2, outputFilePath);
    }

    private static void createLineChart(double average, long[] yCountValues, String[] categories, JSONObject statisticValues, long[] xTimeValues, float[] yAveValues, String outputFilePath) {
        HttpURLConnection conn = null;
        try {
            // 创建示例数据
            JSONObject data = new JSONObject();
            data.put("yCountValues", yCountValues);
            data.put("categories", categories);
            data.put("statisticValues", statisticValues);
            data.put("xTimeValues", xTimeValues);
            data.put("yAveValues", yAveValues);
            data.put("average_value", average);
            data.put("filepath", outputFilePath);  // 指定输出文件路径

            // 将JSON数据转换为字符串
            String jsonData = data.toString();

            // 记录开始时间
            long startTime = System.nanoTime();

            // 发送HTTP POST请求
            URL url = new URL("http://localhost:5029/plot1");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            // 发送请求数据
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 接收响应数据
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response);
                // 输出响应数据
                System.out.println("Bar chart created successfully at: " + outputFilePath);
            } catch (IOException e) {
                // 读取错误流并打印详细错误信息
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.err.println("Error response: " + response.toString());
                }
            }

            // 记录结束时间
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000; // 转换为毫秒

            System.out.println("Request duration: " + duration + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static void createBarChartNew(long[] yCountValues2, String[] categories2, JSONObject statisticValues2, String outputFilePath) {
        HttpURLConnection conn = null;
        try {
            // 创建示例数据
            JSONObject data = new JSONObject();
            data.put("yCountValues2", yCountValues2);
            data.put("categories2", categories2);
            data.put("statisticValues", statisticValues2);
            data.put("filepath", outputFilePath);  // 指定输出文件路径

            // 将JSON数据转换为字符串
            String jsonData = data.toString();

            // 记录开始时间
            long startTime = System.nanoTime();

            // 发送HTTP POST请求
            URL url = new URL("http://localhost:5029/plot2");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            // 发送请求数据
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 接收响应数据
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // 输出响应数据
                System.out.println("New bar chart created successfully at: " + outputFilePath);
            } catch (IOException e) {
                // 读取错误流并打印详细错误信息
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.err.println("Error response: " + response.toString());
                }
            }

            // 记录结束时间
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000; // 转换为毫秒

            System.out.println("Request duration: " + duration + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


}
