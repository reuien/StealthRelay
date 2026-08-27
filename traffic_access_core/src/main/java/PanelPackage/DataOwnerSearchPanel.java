package PanelPackage;

import Item.Stream;
import Util.MyUtil;
import crypto.MACCheckFailed;
import exceptions.CouldNotReceiveException;
import exceptions.CouldNotStoreException;
import exceptions.InvalidQueryException;
import py.PlotInterface;
import sqlConnect.FrontEndSQL;
import statistics.StatisticInfo;
import statistics.StatisticInfoNew;
import streamHandling.Chunk;
import streamHandling.Digest;
import usrs.DataOwnerClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class DataOwnerSearchPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public JComboBox<String> comboBox = new JComboBox<String>();
    public static List<Stream> streamlist = OwnerMainView.streamlist;
    public static Map<String, Stream> streammap = OwnerMainView.streammap;
    public JTextField id_textField;


    private JTextField textField;
    private JTextField textField_1;
    private JTextField textField_2;
    private JTextField textField_3;
    private JTextField textField_4;
    private JTextField textField_5;

    private JComboBox<Integer> startYearBox;
    private JComboBox<Integer> startMonthBox;
    private JComboBox<Integer> startDayBox;
    private JComboBox<Integer> startHourBox;
    private JComboBox<Integer> startMinuteBox;
    private JComboBox<Integer> startSecondBox;

    private JComboBox<Integer> endYearBox;
    private JComboBox<Integer> endMonthBox;
    private JComboBox<Integer> endDayBox;
    private JComboBox<Integer> endHourBox;
    private JComboBox<Integer> endMinuteBox;
    private JComboBox<Integer> endSecondBox;
    private boolean isselectdate;
    public static DataOwnerClient doc;


    /**
     * Create the panel.
     */
    public DataOwnerSearchPanel(OwnerMainView ownermainview) throws Exception {
        setLayout(null);
        this.setBackground(Color.WHITE);
        //system.out.println("数据查询");
        startYearBox = new JComboBox<>();
        startMonthBox = new JComboBox<>();
        startDayBox = new JComboBox<>();
        startHourBox = new JComboBox<>();
        startMinuteBox = new JComboBox<>();
        startSecondBox = new JComboBox<>();

        endYearBox = new JComboBox<>();
        endMonthBox = new JComboBox<>();
        endDayBox = new JComboBox<>();
        endHourBox = new JComboBox<>();
        endMinuteBox = new JComboBox<>();
        endSecondBox = new JComboBox<>();

        id_textField = new JTextField();
        id_textField.setBounds(132, 30, 66, 21);
        add(id_textField);
        id_textField.setColumns(10);
        id_textField.setVisible(false);

        doc = ownermainview.doc;

        initializeDateTimeFields(startYearBox, startMonthBox, startDayBox, startHourBox, startMinuteBox, startSecondBox, 137, 11, isselectdate);
        initializeDateTimeFields(endYearBox, endMonthBox, endDayBox, endHourBox, endMinuteBox, endSecondBox, 137, 43, isselectdate);


        JLabel lblNewLabel = new JLabel("数据查询");
        lblNewLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel.setBounds(10, 22, 435, 30);
        add(lblNewLabel);

        JSeparator separator = new JSeparator();
        separator.setBounds(10, 64, 955, 12);
        separator.setForeground(Color.GRAY);
        add(separator);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        panel.setBounds(10, 86, 955, 197);
        add(panel);

        comboBox.removeAll();
        comboBox.setBounds(88, 23, 241, 23);
        panel.add(comboBox);

        textField = new JTextField();
        textField.setEditable(false);
        textField.setColumns(10);
        textField.setBounds(729, 24, 88, 21);
        panel.add(textField);

        JLabel lblNewLabel_1_1_1 = new JLabel("流描述：");
        lblNewLabel_1_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_1.setBounds(643, 21, 76, 23);
        panel.add(lblNewLabel_1_1_1);

        textField_1 = new JTextField();
        textField_1.setEditable(false);
        textField_1.setColumns(10);
        textField_1.setBounds(488, 24, 111, 21);
        panel.add(textField_1);

        JLabel lblNewLabel_1_1 = new JLabel("流名称：");
        lblNewLabel_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1.setBounds(402, 21, 76, 23);
        panel.add(lblNewLabel_1_1);

        JLabel lblNewLabel_1 = new JLabel("选择流：");
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1.setBounds(10, 21, 76, 23);
        panel.add(lblNewLabel_1);

        JLabel lblNewLabel_1_3 = new JLabel("设置的最小粒度：");
        lblNewLabel_1_3.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_3.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_3.setBounds(23, 106, 140, 23);
        panel.add(lblNewLabel_1_3);

        textField_2 = new JTextField();
        textField_2.setEditable(false);
        textField_2.setColumns(10);
        textField_2.setBounds(161, 109, 111, 21);
        panel.add(textField_2);

        JLabel lblNewLabel_1_3_1 = new JLabel("设置的更高粒度：");
        lblNewLabel_1_3_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_3_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_3_1.setBounds(351, 106, 140, 23);
        panel.add(lblNewLabel_1_3_1);

        textField_3 = new JTextField();
        textField_3.setEditable(false);
        textField_3.setColumns(10);
        textField_3.setBounds(488, 109, 111, 21);
        panel.add(textField_3);

        JLabel lblNewLabel_1_3_2 = new JLabel("设置的开始时间：");
        lblNewLabel_1_3_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_3_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_3_2.setBounds(23, 147, 140, 23);
        panel.add(lblNewLabel_1_3_2);

        JLabel lblNewLabel_1_3_1_1 = new JLabel("设置的结束时间：");
        lblNewLabel_1_3_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_3_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_3_1_1.setBounds(351, 147, 140, 23);
        panel.add(lblNewLabel_1_3_1_1);

        textField_4 = new JTextField();
        textField_4.setEditable(false);
        textField_4.setColumns(10);
        textField_4.setBounds(161, 150, 191, 21);
        panel.add(textField_4);

        textField_5 = new JTextField();
        textField_5.setEditable(false);
        textField_5.setColumns(10);
        textField_5.setBounds(488, 150, 207, 21);
        panel.add(textField_5);

        JSeparator separator_1 = new JSeparator();
        separator_1.setForeground(Color.GRAY);
        separator_1.setBounds(20, 63, 911, 12);
        panel.add(separator_1);

        JPanel panel_1 = new JPanel();
        panel_1.setLayout(null);
        panel_1.setBackground(Color.WHITE);
        panel_1.setBounds(10, 306, 955, 438);
        add(panel_1);

        panel_1.add(startYearBox);
        panel_1.add(startMonthBox);
        panel_1.add(startDayBox);
        panel_1.add(startHourBox);
        panel_1.add(startMinuteBox);
        panel_1.add(startSecondBox);

        panel_1.add(endYearBox);
        panel_1.add(endMonthBox);
        panel_1.add(endDayBox);
        panel_1.add(endHourBox);
        panel_1.add(endMinuteBox);
        panel_1.add(endSecondBox);

        JLabel lblNewLabel_1_3_1_2 = new JLabel("结束时间：");
        lblNewLabel_1_3_1_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_3_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_3_1_2.setBounds(0, 43, 140, 23);
        panel_1.add(lblNewLabel_1_3_1_2);

        JLabel lblNewLabel_1_3_2_1 = new JLabel("粒度倍数：");
        lblNewLabel_1_3_2_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_3_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_3_2_1.setBounds(0, 77, 140, 23);
        panel_1.add(lblNewLabel_1_3_2_1);

        JLabel lblNewLabel_1_3_1_1_1 = new JLabel("开始时间：");
        lblNewLabel_1_3_1_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_3_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_3_1_1_1.setBounds(0, 10, 140, 23);
        panel_1.add(lblNewLabel_1_3_1_1_1);


        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(10, 124, 935, 304);
        panel_1.add(scrollPane);

        JButton btnNewButton = new JButton("数据查询");
        btnNewButton.setForeground(Color.WHITE);
        btnNewButton.setBackground(new Color(90, 181, 94));
        btnNewButton.setBounds(712, 91, 93, 23);
        panel_1.add(btnNewButton);

        JButton btnNewButton_1 = new JButton("统计查询");
        btnNewButton_1.setForeground(Color.WHITE);
        btnNewButton_1.setBackground(new Color(251, 140, 0));
        btnNewButton_1.setBounds(821, 91, 93, 23);
        panel_1.add(btnNewButton_1);


        JComboBox<String> comboBox_1 = new JComboBox<String>();
        comboBox_1.setBounds(137, 79, 84, 23);
        panel_1.add(comboBox_1);
        for (long a = 1; a <= 60; a++) {
            comboBox_1.addItem(Long.toString(a));
        }

        JLabel lblNewLabel_2_2_1_2_1 = new JLabel("年");
        lblNewLabel_2_2_1_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1.setBounds(224, 11, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1);

        JLabel lblNewLabel_2_2_1_2_1_1 = new JLabel("月");
        lblNewLabel_2_2_1_2_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1.setBounds(311, 12, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1 = new JLabel("日");
        lblNewLabel_2_2_1_2_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1.setBounds(394, 12, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1 = new JLabel("时");
        lblNewLabel_2_2_1_2_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1.setBounds(472, 12, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1 = new JLabel("分");
        lblNewLabel_2_2_1_2_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1.setBounds(554, 12, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_1 = new JLabel("秒");
        lblNewLabel_2_2_1_2_1_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_1.setBounds(637, 12, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_2 = new JLabel("年");
        lblNewLabel_2_2_1_2_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_2.setBounds(224, 40, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_2 = new JLabel("月");
        lblNewLabel_2_2_1_2_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_2.setBounds(311, 41, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_1_2 = new JLabel("日");
        lblNewLabel_2_2_1_2_1_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_2.setBounds(394, 41, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_2 = new JLabel("时");
        lblNewLabel_2_2_1_2_1_1_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_2.setBounds(472, 41, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_2 = new JLabel("分");
        lblNewLabel_2_2_1_2_1_1_1_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_2.setBounds(554, 41, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_1_1 = new JLabel("秒");
        lblNewLabel_2_2_1_2_1_1_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_1_1.setBounds(637, 41, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1_1_1_1);


        comboBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                streamlist = OwnerMainView.streamlist;
                streammap = OwnerMainView.streammap;
                textArea.setText("");
                if (comboBox.getSelectedItem().equals("请选择")) {
                    textField.setText("");
                    textField_1.setText("");
                    textField_2.setText("");
                    textField_3.setText("");
                    textField_4.setText("");
                    textField_5.setText("");


                } else {
                    String selectedDeviceId = (String) comboBox.getSelectedItem();
                    textField_1.setText(streammap.get(selectedDeviceId).getName());
                    textField.setText(streammap.get(selectedDeviceId).getDesciption());
                    textField_4.setText(MyUtil.date2Str(streammap.get(selectedDeviceId).getStarttime()));
                    textField_5.setText(MyUtil.date2Str(streammap.get(selectedDeviceId).getEndtime()));
                    textField_2.setText(Long.toString(streammap.get(selectedDeviceId).getMingranularity()));
                    textField_3.setText(Long.toString(streammap.get(selectedDeviceId).getGranularity()));

                    //选择之后绘制出允许粒度下拉框

                    initializeDateTimeFieldsStart(startYearBox, startMonthBox, startDayBox, startHourBox, startMinuteBox, startSecondBox, streammap.get(selectedDeviceId).getStarttime(), streammap.get(selectedDeviceId).getEndtime(), streammap.get(selectedDeviceId).getMingranularity(), 137, 11, isselectdate);
                    initializeDateTimeFieldsEnd(endYearBox, endMonthBox, endDayBox, endHourBox, endMinuteBox, endSecondBox, streammap.get(selectedDeviceId).getStarttime(), streammap.get(selectedDeviceId).getEndtime(), streammap.get(selectedDeviceId).getMingranularity(), 137, 43, isselectdate);
                    isselectdate = true;
                }


            }
        });

        btnNewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (comboBox.getSelectedItem().equals("请选择")) {
                    JOptionPane.showMessageDialog(null, "请选择流！");
                } else {
                    streamlist = OwnerMainView.streamlist;
                    streammap = OwnerMainView.streammap;
                    String selectedDeviceId = (String) comboBox.getSelectedItem();
                    textArea.setText("");
                    String NameId = (String) comboBox.getSelectedItem();
                    Date startDate = getSelectedDate(startYearBox, startMonthBox, startDayBox, startHourBox, startMinuteBox, startSecondBox);
                    Date endDate = getEndtimeDate(endYearBox, endMonthBox, endDayBox, endHourBox, endMinuteBox, endSecondBox);
                    if (startDate.getTime() >= streammap.get(selectedDeviceId).getStarttime().getTime()
                            && endDate.getTime() <= streammap.get(selectedDeviceId).getEndtime().getTime()
                            && startDate.getTime() <= endDate.getTime()) {


                        comboBox.setEnabled(false);
                        btnNewButton_1.setEnabled(false);
                        btnNewButton.setEnabled(false);

                        System.out.println("start: " + startDate.getTime());
                        System.out.println("end  : " + endDate.getTime());

                        long sid = streammap.get(NameId).getId();//流id
                        List<Chunk> cks;
                        try {
                            cks = doc.getChunks(sid, startDate, endDate);
                        } catch (CouldNotReceiveException ex) {
                            throw new RuntimeException(ex);
                        } catch (CouldNotStoreException ex) {
                            throw new RuntimeException(ex);
                        }

                        for (Chunk chunk : cks) {
                            textArea.append(chunk.toString() + "\n");
                        }
                        textArea.append("数据查询结束");
                        JOptionPane.showMessageDialog(null, "数据查询结束！");

                        comboBox.setEnabled(true);
                        btnNewButton_1.setEnabled(true);
                        btnNewButton.setEnabled(true);
                    } else {
                        JOptionPane.showMessageDialog(null, "请选择符合要求的时间范围！");
                    }
                }
            }
        });

        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (comboBox.getSelectedItem().equals("请选择")) {
                    JOptionPane.showMessageDialog(null, "请选择流！");
                } else {
                    streamlist = OwnerMainView.streamlist;
                    streammap = OwnerMainView.streammap;
                    textArea.setText("");
                    String selectedDeviceId = (String) comboBox.getSelectedItem();
                    Date startDate = getSelectedDate(startYearBox, startMonthBox, startDayBox, startHourBox, startMinuteBox, startSecondBox);
                    Date endDate = getEndtimeDate(endYearBox, endMonthBox, endDayBox, endHourBox, endMinuteBox, endSecondBox);
                    if (startDate.getTime() >= streammap.get(selectedDeviceId).getStarttime().getTime()
                            && endDate.getTime() <= streammap.get(selectedDeviceId).getEndtime().getTime()
                            && startDate.getTime() < endDate.getTime()) {

                        String NameId = (String) comboBox.getSelectedItem();

                        long multiple = Long.parseLong((String) comboBox_1.getSelectedItem());//最小粒度倍数
                        long sid = streammap.get(NameId).getId();//流id

                        //添加方法----------------  统计查询

                        System.out.println("start: " + startDate.getTime());
                        System.out.println("end  : " + endDate.getTime());

                        //Date sta = new Date();
                        List<Digest> digests;
                        Digest allDigest;
                        try {
                            //digests = doc.getDigests(sid, startDate, endDate, (int)multiple);
                            digests = doc.getDigestsNew(sid, startDate, endDate, (int) multiple);
                            allDigest = doc.getAllNewDigestsOwner(sid, startDate, endDate);
                        } catch (CouldNotReceiveException ex) {
                            throw new RuntimeException(ex);
                        } catch (CouldNotStoreException ex) {
                            throw new RuntimeException(ex);
                        } catch (MACCheckFailed ex) {
                            throw new RuntimeException(ex);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        } catch (InvalidQueryException ex) {
                            throw new RuntimeException(ex);
                        }

                        FrontEndSQL sql = new FrontEndSQL();
                        streamHandling.Stream curStream = sql.getStream(sid);

                        for (Digest digest : digests) {
                            //textArea.append(StatisticInfo.getStatisticInfo(curStream, digest) +  "\n");
                            textArea.append(StatisticInfoNew.getStatisticInfo(curStream, digest) + "\n");
                        }
                        textArea.append("统计查询结束");


//                        int dataSize = digests.size();
//                        int numPoints = Math.min(dataSize, 60); // 实际要显示的数据点数量
//                        long[] xTimeValues = new long[numPoints];
//                        float[] yAveValues = new float[numPoints];
//
//                        int iter = dataSize / numPoints;
//                        for (int i = 0; i < numPoints; i++) {
//                            Digest curD = digests.get(i * iter);
//                            yAveValues[i] = (float) curD.getSum() / curD.getCount();
//                            xTimeValues[i] = curD.getStartTime(curStream);
//                        }
                        int dataSize = digests.size();
                        int numPoints = Math.min(dataSize, 60); // 实际要显示的数据点数量

// 计算间隔，确保 iter 最小为 1
                        int iter = Math.max(1, dataSize / numPoints);

                        long[] xTimeValues = new long[numPoints];
                        float[] yAveValues = new float[numPoints];

                        for (int i = 0; i < numPoints; i++) {
                            int index = Math.min(i * iter, dataSize - 1); // 确保索引不超出范围
                            Digest curD = digests.get(index);
                            yAveValues[i] = (float) curD.getSum() / curD.getCount();
                            xTimeValues[i] = curD.getStartTime(curStream);
                        }

                        System.out.println("xTimeValues:" + Arrays.toString(xTimeValues));
                        System.out.println("yAveValues:" + Arrays.toString(yAveValues));


                        long[] yCountValues = new long[6];
                        yCountValues[0] = allDigest.getCount1();
                        yCountValues[1] = allDigest.getCount2();
                        yCountValues[2] = allDigest.getCount3();
                        yCountValues[3] = allDigest.getCount4();
                        yCountValues[4] = allDigest.getCount5();
                        yCountValues[5] = allDigest.getCount6();

                        double[] statisticValues = new double[6];
                        StatisticInfo staInfo = StatisticInfo.getStatisticInfo(curStream, allDigest);
                        statisticValues[0] = staInfo.getAverage();
                        statisticValues[1] = staInfo.getStd();
                        statisticValues[2] = staInfo.getVariance();
                        statisticValues[3] = allDigest.getSum();
                        statisticValues[4] = allDigest.getCount();
                        statisticValues[5] = allDigest.getSquare();

                        double average = staInfo.getAverage();

                        System.out.println("yCountValues6:" + Arrays.toString(yCountValues));
                        System.out.println("statisticValues:" + Arrays.toString(statisticValues));
                        System.out.println("average: " + average);

                        String path = "traffic_access_core/data/plot.png";  // 固定的输出文件路径
                        //String picType = "histogram";
//                        String picType = "line";

						/*ServerController serverController = new ServerController();
						// 启动服务器
						String pythonInterpreter = "C:\\ProgramData\\miniconda3\\python.exe";
						String scriptPath = "traffic_access_core/src/main/java/py/server.py";
						serverController.startServer(pythonInterpreter, scriptPath);*/

                        try {
                            PlotInterface.generatePlotLine(xTimeValues, yAveValues, yCountValues, statisticValues, average, path);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        } finally {
                            // 关闭服务器
                            //serverController.stopServer();
                        }
                        //显示图片
                        showpicture(path);


//						JOptionPane.showMessageDialog(null, "统计查询结束！");
						/*Date end = new Date();
						textArea.append("start: "+sta+"\n");
						textArea.append("end  : "+end+"\n");
						textArea.append("用时  : "+(end.getTime()-sta.getTime())+"\n");*/
						/*List<Digest> digests;
						try {
							digests = doc.getDigests(sid, startDate, endDate, (int)multiple);
						} catch (CouldNotReceiveException ex) {
							throw new RuntimeException(ex);
						} catch (CouldNotStoreException ex) {
                            throw new RuntimeException(ex);
                        }

                        for (Digest digest : digests) {
							textArea.append(StatisticInfo.getStatisticInfo(digest) +  "\n");
						}
						textArea.append("查询结束\n");*/

                    } else {

                        JOptionPane.showMessageDialog(null, "请选择符合要求的时间范围！");
                    }

                }

            }
        });

    }

    public static List<Date> getDateRange(Date startDate, Date endDate, long granularity) {
        List<Date> dateList = new ArrayList<>();
        long currentTime = startDate.getTime();

        // 逐步增加时间，直到达到endDate
        while (currentTime < endDate.getTime()) {
            dateList.add(new Date(currentTime));
            currentTime += granularity; // 增加毫秒
        }

        return dateList;
    }


    private void initializeDateTimeFieldsStart(JComboBox<Integer> yearBox, JComboBox<Integer> monthBox, JComboBox<Integer> dayBox, JComboBox<Integer> hourBox, JComboBox<Integer> minuteBox, JComboBox<Integer> secondBox, Date startDate, Date endDate, long granularity, int x, int y, boolean b) {
        if (!isselectdate) {
            yearBox.setBounds(x, y, 84, 25);
            monthBox.setBounds(x + 120, y, 50, 25);
            dayBox.setBounds(x + 200, y, 50, 25);
            hourBox.setBounds(x + 280, y, 50, 25);
            minuteBox.setBounds(x + 360, y, 50, 25);
            secondBox.setBounds(x + 440, y, 50, 25);
        } else {
            yearBox.removeAllItems();
            monthBox.removeAllItems();
            dayBox.removeAllItems();
            hourBox.removeAllItems();
            minuteBox.removeAllItems();
            secondBox.removeAllItems();

            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);

            Set<Integer> yearOptions = new HashSet<>();
            Set<Integer> monthOptions = new HashSet<>();
            Set<Integer> dayOptions = new HashSet<>();
            Set<Integer> hourOptions = new HashSet<>();
            Set<Integer> minuteOptions = new HashSet<>();
            Set<Integer> secondOptions = new HashSet<>();

            Calendar currentCal = Calendar.getInstance();
            currentCal.setTime(startDate);

            while (currentCal.getTime().before(endDate)) {
                yearOptions.add(currentCal.get(Calendar.YEAR));
                monthOptions.add(currentCal.get(Calendar.MONTH) + 1);
                dayOptions.add(currentCal.get(Calendar.DAY_OF_MONTH));
                hourOptions.add(currentCal.get(Calendar.HOUR_OF_DAY));
                minuteOptions.add(currentCal.get(Calendar.MINUTE));
                secondOptions.add(currentCal.get(Calendar.SECOND));

                currentCal.add(Calendar.MILLISECOND, (int) granularity);
            }

            // Add options to JComboBoxes
            yearOptions.forEach(yearBox::addItem);
            monthOptions.forEach(monthBox::addItem);
            dayOptions.forEach(dayBox::addItem);
            hourOptions.forEach(hourBox::addItem);
            minuteOptions.forEach(minuteBox::addItem);
            secondOptions.forEach(secondBox::addItem);

            // Set default selected values based on start date
            yearBox.setSelectedItem(startCal.get(Calendar.YEAR));
            monthBox.setSelectedItem(startCal.get(Calendar.MONTH) + 1);
            dayBox.setSelectedItem(startCal.get(Calendar.DAY_OF_MONTH));
            hourBox.setSelectedItem(startCal.get(Calendar.HOUR_OF_DAY));
            minuteBox.setSelectedItem(startCal.get(Calendar.MINUTE));
            secondBox.setSelectedItem(startCal.get(Calendar.SECOND));

            // Set bounds for JComboBoxes
            yearBox.setBounds(x, y, 84, 25);
            monthBox.setBounds(x + 120, y, 50, 25);
            dayBox.setBounds(x + 200, y, 50, 25);
            hourBox.setBounds(x + 280, y, 50, 25);
            minuteBox.setBounds(x + 360, y, 50, 25);
            secondBox.setBounds(x + 440, y, 50, 25);
        }

    }

    private void initializeDateTimeFieldsEnd(JComboBox<Integer> yearBox, JComboBox<Integer> monthBox, JComboBox<Integer> dayBox, JComboBox<Integer> hourBox, JComboBox<Integer> minuteBox, JComboBox<Integer> secondBox, Date startDate, Date endDate, long granularity, int x, int y, boolean b) {
        if (!isselectdate) {
            yearBox.setBounds(x, y, 84, 25);
            monthBox.setBounds(x + 120, y, 50, 25);
            dayBox.setBounds(x + 200, y, 50, 25);
            hourBox.setBounds(x + 280, y, 50, 25);
            minuteBox.setBounds(x + 360, y, 50, 25);
            secondBox.setBounds(x + 440, y, 50, 25);
        } else {
            yearBox.removeAllItems();
            monthBox.removeAllItems();
            dayBox.removeAllItems();
            hourBox.removeAllItems();
            minuteBox.removeAllItems();
            secondBox.removeAllItems();

            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);

            Set<Integer> yearOptions = new HashSet<>();
            Set<Integer> monthOptions = new HashSet<>();
            Set<Integer> dayOptions = new HashSet<>();
            Set<Integer> hourOptions = new HashSet<>();
            Set<Integer> minuteOptions = new HashSet<>();
            Set<Integer> secondOptions = new HashSet<>();

            Calendar currentCal = Calendar.getInstance();
            currentCal.setTime(startDate);

            while (currentCal.getTime().before(endDate)) {
                yearOptions.add(currentCal.get(Calendar.YEAR));
                monthOptions.add(currentCal.get(Calendar.MONTH) + 1);
                dayOptions.add(currentCal.get(Calendar.DAY_OF_MONTH));
                hourOptions.add(currentCal.get(Calendar.HOUR_OF_DAY));
                minuteOptions.add(currentCal.get(Calendar.MINUTE));
                secondOptions.add(currentCal.get(Calendar.SECOND) - 1);

                currentCal.add(Calendar.MILLISECOND, (int) granularity);
            }

            // Add options to JComboBoxes
            yearOptions.forEach(yearBox::addItem);
            monthOptions.forEach(monthBox::addItem);
            dayOptions.forEach(dayBox::addItem);
            hourOptions.forEach(hourBox::addItem);
            minuteOptions.forEach(minuteBox::addItem);
            secondOptions.forEach(secondBox::addItem);

            // Set default selected values based on start date
            yearBox.setSelectedItem(endCal.get(Calendar.YEAR));
            monthBox.setSelectedItem(endCal.get(Calendar.MONTH) + 1);
            dayBox.setSelectedItem(endCal.get(Calendar.DAY_OF_MONTH));
            hourBox.setSelectedItem(endCal.get(Calendar.HOUR_OF_DAY));
            minuteBox.setSelectedItem(endCal.get(Calendar.MINUTE));
            secondBox.setSelectedItem(endCal.get(Calendar.SECOND));

            // Set bounds for JComboBoxes
            yearBox.setBounds(x, y, 84, 25);
            monthBox.setBounds(x + 120, y, 50, 25);
            dayBox.setBounds(x + 200, y, 50, 25);
            hourBox.setBounds(x + 280, y, 50, 25);
            minuteBox.setBounds(x + 360, y, 50, 25);
            secondBox.setBounds(x + 440, y, 50, 25);
        }

    }

    private void initializeDateTimeFields(JComboBox<Integer> yearBox, JComboBox<Integer> monthBox,
                                          JComboBox<Integer> dayBox, JComboBox<Integer> hourBox, JComboBox<Integer> minuteBox,
                                          JComboBox<Integer> secondBox, int i, int j, boolean isselectdate2) {
        yearBox.setBounds(i, j, 84, 25);
        monthBox.setBounds(i + 120, j, 50, 25);
        dayBox.setBounds(i + 200, j, 50, 25);
        hourBox.setBounds(i + 280, j, 50, 25);
        minuteBox.setBounds(i + 360, j, 50, 25);
        secondBox.setBounds(i + 440, j, 50, 25);

    }

    private Date getSelectedDate(JComboBox<Integer> yearBox, JComboBox<Integer> monthBox, JComboBox<Integer> dayBox, JComboBox<Integer> hourBox, JComboBox<Integer> minuteBox, JComboBox<Integer> secondBox) {
        Calendar selectedDateTime = Calendar.getInstance();
        selectedDateTime.set(
                yearBox.getItemAt(yearBox.getSelectedIndex()),
                monthBox.getItemAt(monthBox.getSelectedIndex()) - 1,
                dayBox.getItemAt(dayBox.getSelectedIndex()),
                hourBox.getItemAt(hourBox.getSelectedIndex()),
                minuteBox.getItemAt(minuteBox.getSelectedIndex()),
                secondBox.getItemAt(secondBox.getSelectedIndex())
        );
        selectedDateTime.set(Calendar.MILLISECOND, 0);
        return selectedDateTime.getTime();
    }

    private Date getEndtimeDate(JComboBox<Integer> yearBox, JComboBox<Integer> monthBox, JComboBox<Integer> dayBox, JComboBox<Integer> hourBox, JComboBox<Integer> minuteBox, JComboBox<Integer> secondBox) {
        Calendar selectedDateTime = Calendar.getInstance();
        selectedDateTime.set(
                yearBox.getItemAt(yearBox.getSelectedIndex()),
                monthBox.getItemAt(monthBox.getSelectedIndex()) - 1,
                dayBox.getItemAt(dayBox.getSelectedIndex()),
                hourBox.getItemAt(hourBox.getSelectedIndex()),
                minuteBox.getItemAt(minuteBox.getSelectedIndex()),
                secondBox.getItemAt(secondBox.getSelectedIndex())
        );
        selectedDateTime.set(Calendar.MILLISECOND, 999);
        return selectedDateTime.getTime();
    }

    public static boolean isDateInRange(Date date, Date startDate, Date endDate) {
        return (date.equals(startDate) || date.after(startDate)) && (date.equals(endDate) || date.before(endDate));
    }

    public void showpicture(String url) {
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(null, "请输入图片路径", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            File file = new File(url);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(null, "指定路径的图片不存在", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Image img = ImageIO.read(file);
            if (img == null) {
                JOptionPane.showMessageDialog(null, "无法加载图片", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 设置最大宽度和高度
            int maxWidth = 800;
            int maxHeight = 600;

            // 获取图片的原始宽度和高度
            int imgWidth = img.getWidth(null);
            int imgHeight = img.getHeight(null);

            // 计算新的宽度和高度，保持宽高比不变
            if (imgWidth > maxWidth || imgHeight > maxHeight) {
                double scaleWidth = (double) maxWidth / imgWidth;
                double scaleHeight = (double) maxHeight / imgHeight;
                double scale = Math.min(scaleWidth, scaleHeight);
                int newWidth = (int) (imgWidth * scale);
                int newHeight = (int) (imgHeight * scale);
                img = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            }

            ImageIcon icon = new ImageIcon(img);
            JLabel label = new JLabel(icon);
            JOptionPane.showMessageDialog(null, label, "图片预览", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "无法加载图片: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    //显示图片
//    public void showpicture(String url) {
//        if (url.isEmpty()) {
//            JOptionPane.showMessageDialog(null, "请输入图片路径", "错误", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        try {
//            File file = new File(url);
//            if (!file.exists()) {
//                JOptionPane.showMessageDialog(null, "指定路径的图片不存在", "错误", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//
//            Image img = ImageIO.read(file);
//            ImageIcon icon = new ImageIcon(img);
//            JLabel label = new JLabel(icon);
//            JOptionPane.showMessageDialog(null, label, "图片预览", JOptionPane.PLAIN_MESSAGE);
//        } catch (Exception ex) {
//            JOptionPane.showMessageDialog(null, "无法加载图片: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
//        }
//    }

}
