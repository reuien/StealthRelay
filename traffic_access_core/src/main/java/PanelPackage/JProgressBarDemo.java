package PanelPackage;


import exceptions.CouldNotReceiveException;
import exceptions.WriteException;
import usrs.DataOwnerClient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class JProgressBarDemo extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = 759440504372027963L;

    //static JProgressBarDemo frame;
    public JProgressBarDemo(long sId) {
        getContentPane().setLayout(null);
        setTitle("数据上传");
        JLabel label = new JLabel("数据上传");
        label.setBounds(22, 10, 70, 31);

        //创建一个进度条
        JProgressBar progressBar = new JProgressBar();
        progressBar.setBounds(92, 61, 195, 30);
        JButton button = new JButton("完成");
        button.setBounds(251, 114, 93, 23);
        button.setEnabled(false);
        getContentPane().add(label);    //添加标签
        getContentPane().add(progressBar);    //添加进度条
        getContentPane().add(button);    //添加按钮

        progressBar.setStringPainted(true);
//        //如果不需要进度上显示“升级进行中...”，可注释此行
        progressBar.setString("数据上传中...");
//        如果需要使用不确定模式，可使用此行
        progressBar.setIndeterminate(true);
        //开启一个线程处理进度
        new Progress(progressBar, button, sId).start();
        //单机“完成”按钮结束程序
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private class Progress extends Thread {
        JProgressBar progressBar;
        JButton button;
        DataOwnerClient doc;

        long streamId;

        //进度条上的数字
        Progress(JProgressBar progressBar, JButton button, long sId) {
            this.progressBar = progressBar;
            this.button = button;
            this.streamId = sId;
            this.doc = OwnerMainView.doc;
        }

        public void run() {
            //-------上传数据操作
            try {
                //doc.uploadData(streamId);
                doc.producerUploadData(streamId);
            } catch (CouldNotReceiveException e) {
                throw new RuntimeException(e);
            } catch (WriteException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //-------上传数据操作

            // 确保在 EDT 上执行 UI 更新操作
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressBar.setIndeterminate(false);
                    progressBar.setString("上传结束！");
                    button.setEnabled(true);
                }
            });
        }

    }
}
