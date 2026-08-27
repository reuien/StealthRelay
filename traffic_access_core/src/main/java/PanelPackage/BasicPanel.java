package PanelPackage;

import Item.Equipment;
import Item.Stream;
import exceptions.CouldNotStoreException;
import sqlConnect.FrontEndSQL;
import streamHandling.TimeUtil;
import streamHandling.TimeUtil.Precision;
import usrs.DataOwnerClient;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BasicPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    public List<Equipment> equipments = OwnerMainView.equipments;
    public static Stream stream = new Stream();

    private JTextField port_text;
    private JTextField ip_text;
    private JTextField strid_text;
    private JTextField type_text;

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

    JTextField id_textField;
    private JTextField eq_name;
    private JTextField textField_1;

    public static DataOwnerClient doc;


    /**
     * Create the panel.
     */
    public BasicPanel(OwnerMainView ownermainview) throws Exception {
        setLayout(null);
        this.setBackground(Color.WHITE);

        //system.out.println("基本设置");

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

        doc = ownermainview.doc;


        // Initialize start date time
        initializeDateTimeFieldsStart(startYearBox, startMonthBox, startDayBox, startHourBox, startMinuteBox, startSecondBox, 383, 67);

        // Initialize end date time
        initializeDateTimeFieldsEnd(endYearBox, endMonthBox, endDayBox, endHourBox, endMinuteBox, endSecondBox, 383, 104);


        JLabel lblNewLabel = new JLabel("基本设置");
        lblNewLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel.setBounds(10, 22, 100, 30);
        add(lblNewLabel);


        JSeparator separator = new JSeparator();
        separator.setBounds(10, 64, 955, 12);
        separator.setForeground(Color.GRAY);
        add(separator);


        JPanel panel = new JPanel();
        panel.setBounds(10, 86, 955, 107);
        panel.setLayout(null);
        add(panel);

        JLabel lblNewLabel_1 = new JLabel("设备信息");
        lblNewLabel_1.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1.setBounds(0, 0, panel.getWidth(), 50);
        lblNewLabel_1.setOpaque(true);
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel_1.setBackground(new Color(42, 115, 197));
        lblNewLabel_1.setForeground(Color.WHITE);
        panel.add(lblNewLabel_1);

        JLabel lblNewLabel_2 = new JLabel("设备id:");
        lblNewLabel_2.setBounds(0, 68, 66, 25);
        lblNewLabel_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lblNewLabel_2);

        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setBounds(76, 70, 93, 23);
        comboBox.addItem("请选择");
        panel.add(comboBox);

        for (Equipment eq : equipments) {
            comboBox.addItem(eq.getIdnum());
        }

        comboBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                if (comboBox.getSelectedItem().equals("请选择")) {
                    port_text.setText("");
                    ip_text.setText("");
                    eq_name.setText("");
                } else {
                    String selectedDeviceId = (String) comboBox.getSelectedItem();
                    FrontEndSQL sql = new FrontEndSQL();
                    String[] message = sql.getSelectEq(selectedDeviceId, doc.getUsrId());
                    eq_name.setText(message[0]);
                    port_text.setText(message[1]);
                    ip_text.setText(message[2]);
                }

            }
        });

        JLabel lblNewLabel_2_1 = new JLabel("设备端口号:");
        lblNewLabel_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_1.setBounds(372, 68, 101, 25);
        panel.add(lblNewLabel_2_1);

        port_text = new JTextField();
        port_text.setBounds(483, 70, 75, 21);
        port_text.setEditable(false);
        panel.add(port_text);
        port_text.setColumns(10);

        JLabel lblNewLabel_2_1_1 = new JLabel("设备ip:");
        lblNewLabel_2_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_1_1.setBounds(568, 68, 66, 25);
        lblNewLabel_2_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lblNewLabel_2_1_1);

        ip_text = new JTextField();
        ip_text.setColumns(10);
        ip_text.setBounds(644, 70, 124, 21);
        ip_text.setEditable(false);
        panel.add(ip_text);

        JLabel lblNewLabel_2_1_2 = new JLabel("设备名称:");
        lblNewLabel_2_1_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_1_2.setBounds(166, 68, 101, 25);
        panel.add(lblNewLabel_2_1_2);

        eq_name = new JTextField();
        eq_name.setEditable(false);
        eq_name.setColumns(10);
        eq_name.setBounds(277, 70, 75, 21);
        panel.add(eq_name);

        JButton btnNewButton_1 = new JButton("连 接");
        btnNewButton_1.setBackground(new Color(42, 115, 197));
        btnNewButton_1.setForeground(Color.WHITE);
        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //-----------

                boolean ok;
                try {
                    String producerId = (String) comboBox.getSelectedItem();
                    ok = doc.linkProducer(Long.parseLong(producerId), eq_name.getText());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                //----------
                JFrame frame = new JFrame();
                String result;
                if (ok) {
                    result = "连接成功！";
                } else {
                    result = "连接失败！";
                }
                JOptionPane.showMessageDialog(frame, result);
            }
        });
        btnNewButton_1.setBounds(804, 70, 93, 23);
        panel.add(btnNewButton_1);


        JPanel panel_2 = new JPanel();
        panel_2.setLayout(null);
        panel_2.setBounds(10, 221, 955, 360);
        add(panel_2);

        JLabel lblNewLabel_1_2 = new JLabel("数据流设置");
        lblNewLabel_1_2.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1_2.setBounds(0, 0, panel_2.getWidth(), 50);
        lblNewLabel_1_2.setOpaque(true);
        lblNewLabel_1_2.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel_1_2.setBackground(new Color(42, 115, 197));
        lblNewLabel_1_2.setForeground(Color.WHITE);
        panel_2.add(lblNewLabel_1_2);

        JLabel lblNewLabel_2_2 = new JLabel("流名称:");
        lblNewLabel_2_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2.setBounds(20, 67, 66, 25);
        panel_2.add(lblNewLabel_2_2);

        strid_text = new JTextField();
        strid_text.setColumns(10);
        strid_text.setBounds(96, 69, 142, 21);
        panel_2.add(strid_text);

        JLabel lblNewLabel_2_2_1 = new JLabel("流类型:");
        lblNewLabel_2_2_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1.setBounds(20, 104, 66, 25);
        panel_2.add(lblNewLabel_2_2_1);

        type_text = new JTextField();
        type_text.setColumns(10);
        type_text.setBounds(96, 143, 142, 21);
        panel_2.add(type_text);
        type_text.setEnabled(false);

        JLabel lblNewLabel_2_2_1_2 = new JLabel("开始时间:");
        lblNewLabel_2_2_1_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2.setBounds(307, 67, 66, 25);
        panel_2.add(lblNewLabel_2_2_1_2);

        JLabel lblNewLabel_2_2_1_2_1 = new JLabel("年");
        lblNewLabel_2_2_1_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1.setBounds(477, 67, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1);


        panel_2.add(startYearBox);
        panel_2.add(startMonthBox);
        panel_2.add(startDayBox);
        panel_2.add(startHourBox);
        panel_2.add(startMinuteBox);
        panel_2.add(startSecondBox);

        panel_2.add(endYearBox);
        panel_2.add(endMonthBox);
        panel_2.add(endDayBox);
        panel_2.add(endHourBox);
        panel_2.add(endMinuteBox);
        panel_2.add(endSecondBox);

        JLabel lblNewLabel_2_2_1_2_1_1 = new JLabel("月");
        lblNewLabel_2_2_1_2_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1.setBounds(557, 67, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1 = new JLabel("日");
        lblNewLabel_2_2_1_2_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1.setBounds(638, 67, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1 = new JLabel("时");
        lblNewLabel_2_2_1_2_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1.setBounds(716, 67, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1 = new JLabel("分");
        lblNewLabel_2_2_1_2_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1.setBounds(798, 67, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_1 = new JLabel("秒");
        lblNewLabel_2_2_1_2_1_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_1.setBounds(881, 67, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1_1_1_1);


        JLabel lblNewLabel_2_2_1_2_2 = new JLabel("结束时间:");
        lblNewLabel_2_2_1_2_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1_2_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_2.setBounds(307, 104, 66, 25);
        panel_2.add(lblNewLabel_2_2_1_2_2);


        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_1_1 = new JLabel("\u79D2");
        lblNewLabel_2_2_1_2_1_1_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_1_1.setBounds(881, 104, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1_1_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_2 = new JLabel("\u5206");
        lblNewLabel_2_2_1_2_1_1_1_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_2.setBounds(798, 104, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_2 = new JLabel("\u65F6");
        lblNewLabel_2_2_1_2_1_1_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_2.setBounds(716, 104, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_1_2 = new JLabel("\u65E5");
        lblNewLabel_2_2_1_2_1_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_2.setBounds(638, 104, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_2 = new JLabel("\u6708");
        lblNewLabel_2_2_1_2_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_2.setBounds(557, 104, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_2 = new JLabel("\u5E74");
        lblNewLabel_2_2_1_2_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_2.setBounds(477, 104, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_2);

        JLabel lblNewLabel_2_2_1_1 = new JLabel("最小粒度:");
        lblNewLabel_2_2_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_1.setBounds(20, 174, 66, 25);
        panel_2.add(lblNewLabel_2_2_1_1);


        JComboBox<Precision> precisionComboBox = new JComboBox<>(Precision.values());
        precisionComboBox.setBounds(96, 177, 142, 23);
        panel_2.add(precisionComboBox);

        JLabel lblNewLabel_2_2_1_1_1 = new JLabel("更高粒度:");
        lblNewLabel_2_2_1_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_1_1.setBounds(20, 209, 66, 25);
        panel_2.add(lblNewLabel_2_2_1_1_1);


        JComboBox<Precision> higherPrecisionComboBox = new JComboBox<>();
        higherPrecisionComboBox.setBounds(96, 210, 142, 23);
        panel_2.add(higherPrecisionComboBox);

        JButton btnNewButton = new JButton("创建流配置");
        btnNewButton.setForeground(Color.WHITE);
        btnNewButton.setBackground(new Color(90, 181, 94));

        btnNewButton.setBounds(718, 330, 93, 23);
        panel_2.add(btnNewButton);

        JButton runButton = new JButton("上传流数据");
        runButton.setForeground(Color.WHITE);
        runButton.setBackground(new Color(251, 140, 0));
        runButton.setBounds(827, 330, 93, 23);
        panel_2.add(runButton);

        JComboBox<String> comboBox_type = new JComboBox<String>();

        comboBox_type.setBounds(96, 107, 142, 23);
        panel_2.add(comboBox_type);

        JLabel lblNewLabel_2_2_1_3 = new JLabel("其他类型:");
        lblNewLabel_2_2_1_3.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1_3.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_3.setBounds(10, 139, 76, 25);
        panel_2.add(lblNewLabel_2_2_1_3);

        textField_1 = new JTextField();
        textField_1.setBounds(265, 143, 66, 21);
        panel_2.add(textField_1);
        textField_1.setColumns(10);
        textField_1.setVisible(false);


        comboBox_type.addItem("心率");
//        comboBox_type.addItem("车流量");
        comboBox_type.addItem("速率");
        comboBox_type.addItem("流量");
        comboBox_type.addItem("其他");


        id_textField = new JTextField();
        id_textField.setBounds(132, 30, 66, 21);
        add(id_textField);
        id_textField.setColumns(10);
        id_textField.setVisible(false);


        precisionComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Precision selectedPrecision = (Precision) precisionComboBox.getSelectedItem();
                Precision[] higherPrecisions = TimeUtil.getHigherPrecisions(selectedPrecision);
                higherPrecisionComboBox.removeAllItems(); // 清除之前的选项
                for (Precision p : higherPrecisions) {
                    higherPrecisionComboBox.addItem(p); // 添加更高粒度的选项
                }
                higherPrecisionComboBox.setVisible(true); // 显示更高粒度的下拉框
            }
        });

        comboBox_type.addActionListener(new ActionListener() {
            boolean dialogShown = false;

            public void actionPerformed(ActionEvent e) {

                String select = (String) comboBox_type.getSelectedItem();
                if (select.equals("其他") && !dialogShown) {
                    JOptionPane.showMessageDialog(null, "请输入自定义流类型！");
                    type_text.setEnabled(true);
                    textField_1.setText("0");
                    dialogShown = true;
                } else {
                    type_text.setEnabled(false);
                    textField_1.setText("1");
                    dialogShown = false;
                }
            }
        });


        btnNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Date startDate = getSelectedDateStart(startYearBox, startMonthBox, startDayBox, startHourBox, startMinuteBox, startSecondBox);
                Date endDate = getSelectedDateEnd(endYearBox, endMonthBox, endDayBox, endHourBox, endMinuteBox, endSecondBox);
                String select = (String) comboBox_type.getSelectedItem();

                if (strid_text.getText().equals("")) {
                    JFrame frame = new JFrame();
                    String result = "请输入完整信息！";
                    JOptionPane.showMessageDialog(frame, result);
                } else if (select.equals("其他") && type_text.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "请输入自定义类型！");
                } else {
                    if (startDate.after(endDate) || startDate.equals(endDate)) {
                        JFrame frame = new JFrame();
                        String result = "请输入正确的起止时间！";
                        JOptionPane.showMessageDialog(frame, result);
                    } else {
                        if (select.equals("其他")) {
                            stream.setDesciption(type_text.getText());
                        } else {
                            stream.setDesciption(select);
                        }

                        stream.setName(strid_text.getText());
                        stream.setStarttime(startDate);
                        stream.setEndtime(endDate);
                        stream.setMingranularity(((Precision) precisionComboBox.getSelectedItem()).getMillis());
                        stream.setGranularity(((Precision) higherPrecisionComboBox.getSelectedItem()).getMillis());
                        Precision chunkSize = (Precision) precisionComboBox.getSelectedItem();
                        List<Precision> resolutionLevels = new ArrayList<>();
                        //添加方法--------------------流设置

                        long createStreamId;
                        //System.out.println("basic doc:  "+doc.toString());
                        System.out.println("start: " + startDate.getTime());
                        System.out.println("end  : " + endDate.getTime());
                        try {
                            //**********************************************

                            createStreamId = doc.createStream(stream.getName(), stream.getDesciption(), stream.getStarttime(),
                                    stream.getEndtime(), chunkSize, resolutionLevels);
                            stream.setId(createStreamId);
                        } catch (CouldNotStoreException ex) {
                            throw new RuntimeException(ex);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        } catch (NoSuchAlgorithmException ex) {
                            throw new RuntimeException(ex);
                        } catch (InvalidKeySpecException ex) {
                            throw new RuntimeException(ex);
                        } catch (InvalidKeyException ex) {
                            throw new RuntimeException(ex);
                        } catch (InvalidAlgorithmParameterException ex) {
                            throw new RuntimeException(ex);
                        } catch (NoSuchPaddingException ex) {
                            throw new RuntimeException(ex);
                        } catch (ShortBufferException ex) {
                            throw new RuntimeException(ex);
                        } catch (IllegalBlockSizeException ex) {
                            throw new RuntimeException(ex);
                        } catch (BadPaddingException ex) {
                            throw new RuntimeException(ex);
                        }

                        FrontEndSQL sql = new FrontEndSQL();
                        sql.insertStream(stream);
                        sql.insertOwner_Stream(id_textField.getText(), stream.getId());


                        System.out.println(stream.toString());
                        JFrame frame = new JFrame();
                        String result = "成功创建流！";
                        JOptionPane.showMessageDialog(frame, result);
                    }
                }
            }
        });


        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 模拟数据上传过程
                JProgressBarDemo frame = new JProgressBarDemo(stream.getId());
                frame.setBounds(800, 400, 400, 200);    //设置容器的大小
                frame.setVisible(true);
            }
        });

        // 创建一个弹窗

    }


    private void initializeDateTimeFieldsStart(JComboBox<Integer> yearBox, JComboBox<Integer> monthBox, JComboBox<Integer> dayBox, JComboBox<Integer> hourBox, JComboBox<Integer> minuteBox, JComboBox<Integer> secondBox, int x, int y) {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());

        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1;
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);
        int currentSecond = now.get(Calendar.SECOND);
        ;

        initializeDate(yearBox, monthBox, dayBox, hourBox, minuteBox, secondBox, x, y, currentYear, currentMonth, currentDay, currentHour, currentMinute, currentSecond);
    }

    private void initializeDateTimeFieldsEnd(JComboBox<Integer> yearBox, JComboBox<Integer> monthBox, JComboBox<Integer> dayBox, JComboBox<Integer> hourBox, JComboBox<Integer> minuteBox, JComboBox<Integer> secondBox, int x, int y) {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());

        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1;
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);
        int currentSecond = now.get(Calendar.SECOND);

        initializeDate(yearBox, monthBox, dayBox, hourBox, minuteBox, secondBox, x, y, currentYear, currentMonth, currentDay, currentHour, currentMinute, currentSecond);
    }

    private void initializeDate(JComboBox<Integer> yearBox, JComboBox<Integer> monthBox, JComboBox<Integer> dayBox, JComboBox<Integer> hourBox, JComboBox<Integer> minuteBox, JComboBox<Integer> secondBox, int x, int y, int currentYear, int currentMonth, int currentDay, int currentHour, int currentMinute, int currentSecond) {
        for (int i = 2000; i <= 2030; i++) {
            yearBox.addItem(i);
        }

        for (int i = 1; i <= 12; i++) {
            monthBox.addItem(i);
        }

        for (int i = 1; i <= 31; i++) {
            dayBox.addItem(i);
        }

        for (int i = 0; i <= 23; i++) {
            hourBox.addItem(i);
        }

        for (int i = 0; i <= 59; i++) {
            minuteBox.addItem(i);
            secondBox.addItem(i);
        }

        yearBox.setSelectedItem(currentYear);
        monthBox.setSelectedItem(currentMonth);
        dayBox.setSelectedItem(currentDay);
        hourBox.setSelectedItem(currentHour);
        minuteBox.setSelectedItem(currentMinute);
        secondBox.setSelectedItem(currentSecond);


        yearBox.setBounds(x, y, 84, 25);
        monthBox.setBounds(x + 120, y, 50, 25);
        dayBox.setBounds(x + 200, y, 50, 25);
        hourBox.setBounds(x + 280, y, 50, 25);
        minuteBox.setBounds(x + 360, y, 50, 25);
        secondBox.setBounds(x + 440, y, 50, 25);
    }

    private Date getSelectedDateStart(JComboBox<Integer> yearBox, JComboBox<Integer> monthBox, JComboBox<Integer> dayBox, JComboBox<Integer> hourBox, JComboBox<Integer> minuteBox, JComboBox<Integer> secondBox) {
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

    private Date getSelectedDateEnd(JComboBox<Integer> yearBox, JComboBox<Integer> monthBox, JComboBox<Integer> dayBox, JComboBox<Integer> hourBox, JComboBox<Integer> minuteBox, JComboBox<Integer> secondBox) {
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
        selectedDateTime.add(Calendar.SECOND, -1);
        return selectedDateTime.getTime();
    }

}
