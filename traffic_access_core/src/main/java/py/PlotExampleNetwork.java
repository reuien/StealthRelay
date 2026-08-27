package py;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlotExampleNetwork {
    public static void main(String[] args) {
        // 固定的数据
        int[] yCountValues = {2058, 2089, 2002, 2097, 2045, 4109};

        String[] categories = {"0-60", "60-70", "70-80", "80-90", "90-100", "100-120"};
//        double[] yAveValues = {
//                  69.27055, 68.96833, 69.44611, 70.30778, 69.85861, 68.951385, 69.88361, 69.96917, 69.1625, 69.779724, 69.59583, 69.53333, 69.54583, 69.77055, 69.5075    };
////        long[] xTimeValues = {  1722527546000L, 1722531146000L, 1722534746000L, 1722538346000L, 1722541946000L, 1722545546000L, 1722549146000L, 1722552746000L, 1722556346000L, 1722559946000L, 1722563546000L, 1722567146000L, 1722570746000L, 1722574346000L, 1722577946000L };
//        long[] xTimeValues = {
//                1722495146000L, 1722498086000L, 1722501026000L, 1722503966000L, 1722506906000L,  };
//        double[] yAveValues = {
//                68.935715, 69.695915, 69.734695, 69.17517, 69.35748,
//        };
        long[] xTimeValues = {1722232907000L, 1722233147000L, 1722233387000L, 1722233627000L, 1722233867000L, 1722234107000L,
                1722234347000L, 1722234587000L, 1722234827000L, 1722235067000L, 1722235307000L, 1722235547000L,
                1722235787000L, 1722236027000L, 1722236267000L, 1722236507000L, 1722236747000L, 1722236987000L,
                1722237227000L, 1722237467000L, 1722237707000L, 1722237947000L, 1722238187000L, 1722238427000L,
                1722238667000L, 1722238907000L, 1722239147000L, 1722239387000L, 1722239627000L, 1722239867000L,
                1722240107000L, 1722240347000L, 1722240587000L, 1722240827000L, 1722241067000L, 1722241307000L,
                1722241547000L, 1722241787000L, 1722242027000L, 1722242267000L, 1722242507000L, 1722242747000L,
                1722242987000L, 1722243227000L, 1722243467000L, 1722243707000L, 1722243947000L, 1722244187000L,
                1722244427000L, 1722244667000L, 1722244907000L, 1722245147000L, 1722245387000L, 1722245627000L,
                1722245867000L, 1722246107000L, 1722246347000L, 1722246587000L, 1722246827000L, 1722247067000L};
        double[] yAveValues = {
                84, 91, 81, 82, 99, 78, 82, 74, 92, 86, 73, 83, 86, 89, 88, 86, 91, 86, 75, 94, 77, 84, 88, 84, 77, 95, 82, 82, 94, 84, 77, 89, 83, 81, 82, 97, 88, 84, 83, 91, 89, 95, 93, 88, 86, 97, 82, 87, 88, 76, 81, 70, 77, 85, 76, 90, 90, 88, 80, 80
        };
        double average=78.9736565;
        JSONObject statisticValues = new JSONObject();
        statisticValues.put("平均值", 84.44680555555556);
        statisticValues.put("标准差", 20.224114547736537);
        statisticValues.put("方差", 409.0148092399686);
        statisticValues.put("总和", 1216034.0);
        statisticValues.put("计数", 14400.0);
        statisticValues.put("平方和", 1.0858E8);

        int[] yCountValues2 = {1000, 1500, 1200, 1700, 1600, 1800};
        String[] categories2 = {"0-60", "60-70", "70-80", "80-90", "90-100", "100-120"};
        JSONObject statisticValues2 = new JSONObject(statisticValues.toString());

////        // 启动服务器
//        String pythonInterpreter = "python";
//        String scriptPath = "traffic_access_core/src/main/java/py/server.py";
//        ServerController serverController = new ServerController();
//        serverController.startServer(pythonInterpreter, scriptPath);

        try {
            // 调用生成第一个条形图的方法
            createLineChart(average,yCountValues, categories, statisticValues, xTimeValues, yAveValues, "output/plot1.png");

            // 调用生成第二个条形图的方法
            createBarChartNew(yCountValues2, categories2, statisticValues2, xTimeValues, yAveValues, "output/plot2.png");
        } finally {
            // 关闭服务器
//            serverController.stopServer();
        }
//        createBarChartNew(yCountValues2, categories2, statisticValues2, xTimeValues, yAveValues, "output/plot3.png");

    }

    private static void createLineChart(double average, int[] yCountValues, String[] categories, JSONObject statisticValues, long[] xTimeValues, double[] yAveValues, String outputFilePath) {
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
            URL url = new URL("http://localhost:5028/plot1");
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

    private static void createBarChartNew(int[] yCountValues2, String[] categories2, JSONObject statisticValues2, long[] xTimeValues, double[] yAveValues, String outputFilePath) {
        HttpURLConnection conn = null;
        try {
            // 创建示例数据
            JSONObject data = new JSONObject();
            data.put("yCountValues2", yCountValues2);
            data.put("categories2", categories2);
            data.put("statisticValues", statisticValues2);
            data.put("xTimeValues", xTimeValues);
            data.put("yAveValues", yAveValues);
            data.put("filepath", outputFilePath);  // 指定输出文件路径

            // 将JSON数据转换为字符串
            String jsonData = data.toString();

            // 记录开始时间
            long startTime = System.nanoTime();

            // 发送HTTP POST请求
            URL url = new URL("http://localhost:5028/plot2");
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
