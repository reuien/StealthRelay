package py;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerController {
    private Process pythonProcess;

    public void startServer(String pythonInterpreter, String scriptPath) {
        try {
            pythonProcess = new ProcessBuilder(pythonInterpreter, scriptPath)
                    .redirectErrorStream(true)
                    .start();

            // 读取Python服务器的输出
            BufferedReader serverOutput = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
            new Thread(() -> {
                String line;
                try {
                    while ((line = serverOutput.readLine()) != null) {
                        //system.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // 等待服务器启动
            Thread.sleep(2000); // 等待3秒，确保服务器启动
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        if (pythonProcess != null) {
            pythonProcess.destroy();
            try {
                if (pythonProcess.isAlive()) {
                    pythonProcess.destroyForcibly();
                }
                pythonProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
