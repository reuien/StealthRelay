package PanelPackage;

import Item.History;
import Item.PrivacyPolicy;
import Item.Stream;
import Util.MyUtil;
import exceptions.CouldNotReceiveException;
import py.PlotInterface;
import sqlConnect.FrontEndSQL;
import statistics.StatisticInfo;
import statistics.StatisticInfoNew;
import streamHandling.ChunkForDC;
import streamHandling.Digest;
import streamHandling.Token;
import usrs.DataConsumer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class DataConsumerQueryPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public static List<Stream> streamlist = new ArrayList<Stream>();
    public static Map<String, Stream> streammap = new HashMap<>();
    public static Map<Long, List<PrivacyPolicy>> Cpmap = new HashMap<>();
    public JComboBox<String> comboBox = new JComboBox<String>();
    public static Map<Long, List<Long>> Idmap = new HashMap<>();
    public static Map<Long, PrivacyPolicy> policymap = new HashMap<>();
    public static List<PrivacyPolicy> policylist = ConsumerMainView.polivylist;
    public JComboBox<String> comboBox1;
    public JTextField textField_3;

    private JTextField starttime_textField;
    private JTextField endtime_textField;
    private JTextField multiple_textField;
    private JTextField mingranularity_textField;

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

    public static DataConsumer dataConsumer;


    //	 private JTextField textField_4;
    public DataConsumerQueryPanel(ConsumerMainView consumermainview) throws Exception {
        setLayout(null);
        this.setBackground(Color.WHITE);

        textField_3 = new JTextField();
        textField_3.setBounds(134, 18, 87, 21);
        add(textField_3);
        textField_3.setColumns(10);
        textField_3.setVisible(false);

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

        dataConsumer = consumermainview.consumer;

        initializeDateTimeFields(startYearBox, startMonthBox, startDayBox, startHourBox, startMinuteBox, startSecondBox, 169, 14, isselectdate);
        initializeDateTimeFields(endYearBox, endMonthBox, endDayBox, endHourBox, endMinuteBox, endSecondBox, 169, 47, isselectdate);

        JLabel lblNewLabel_1 = new JLabel("数据查询");
        lblNewLabel_1.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1.setBounds(10, 10, 87, 30);
        add(lblNewLabel_1);

        JSeparator separator = new JSeparator();
        separator.setForeground(Color.GRAY);
        separator.setBounds(10, 52, 955, 12);
        add(separator);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
//        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        panel.setBounds(10, 74, 955, 126);
        add(panel);

        JComboBox<String> comboBox_1_1 = new JComboBox<String>();

        panel.add(comboBox_1_1);

        JLabel lblNewLabel_1_1_1_1 = new JLabel("允许的隐私策略：");
        lblNewLabel_1_1_1_1.setBounds(632, 13, 120, 23);
        panel.add(lblNewLabel_1_1_1_1);
        lblNewLabel_1_1_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));

        JComboBox<String> comboBox_1 = new JComboBox<String>();

        panel.add(comboBox_1);

        JLabel lblNewLabel_1_1_1 = new JLabel("流选择：");
        lblNewLabel_1_1_1.setBounds(347, 13, 76, 23);
        panel.add(lblNewLabel_1_1_1);
        lblNewLabel_1_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));


//		comboBox = new JComboBox<String>();

//		panel.add(comboBox);
//		comboBox.addItem("小明");


        JLabel lblNewLabel_1_1 = new JLabel("数据拥有者：");
        lblNewLabel_1_1.setBounds(66, 13, 104, 23);
        panel.add(lblNewLabel_1_1);
        lblNewLabel_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));

        JLabel lblNewLabel_1_3 = new JLabel("允许访问的开始时间：");
        lblNewLabel_1_3.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_3.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_3.setBounds(76, 53, 152, 23);
        panel.add(lblNewLabel_1_3);

        starttime_textField = new JTextField();
        starttime_textField.setEditable(false);
        starttime_textField.setColumns(10);
        starttime_textField.setBounds(230, 56, 152, 21);
        panel.add(starttime_textField);

        JLabel lblNewLabel_1_3_2 = new JLabel("允许访问的最小粒度倍数：");
        lblNewLabel_1_3_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_3_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_3_2.setBounds(375, 84, 185, 23);
        panel.add(lblNewLabel_1_3_2);

        JLabel lblNewLabel_1_3_1 = new JLabel("允许访问的结束时间：");
        lblNewLabel_1_3_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_3_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_3_1.setBounds(404, 53, 152, 23);
        panel.add(lblNewLabel_1_3_1);

        endtime_textField = new JTextField();
        endtime_textField.setEditable(false);
        endtime_textField.setColumns(10);
        endtime_textField.setBounds(557, 56, 152, 21);
        panel.add(endtime_textField);

        multiple_textField = new JTextField();
        multiple_textField.setEditable(false);
        multiple_textField.setColumns(10);
        multiple_textField.setBounds(557, 87, 66, 21);
        panel.add(multiple_textField);

        JLabel lblNewLabel_1_3_2_2 = new JLabel("允许访问的最小粒度：");
        lblNewLabel_1_3_2_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_3_2_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_3_2_2.setBounds(43, 86, 185, 23);
        panel.add(lblNewLabel_1_3_2_2);

        mingranularity_textField = new JTextField();
        mingranularity_textField.setEditable(false);
        mingranularity_textField.setColumns(10);
        mingranularity_textField.setBounds(230, 86, 66, 21);
        panel.add(mingranularity_textField);

        JPanel panel_1_1_1 = new JPanel();
        panel_1_1_1.setBounds(10, 10, 35, 106);
        panel.add(panel_1_1_1);
        panel_1_1_1.setLayout(null);
        panel_1_1_1.setBackground(Color.WHITE);
//        panel_1_1_1.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        JLabel lblNewLabel_1_2 = new JLabel("策");
        lblNewLabel_1_2.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1_2.setBounds(10, 10, 25, 24);
        panel_1_1_1.add(lblNewLabel_1_2);

        JLabel lblNewLabel_1_2_1 = new JLabel("略");
        lblNewLabel_1_2_1.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1_2_1.setBounds(10, 32, 25, 24);
        panel_1_1_1.add(lblNewLabel_1_2_1);

        JLabel lblNewLabel_1_2_2 = new JLabel("选");
        lblNewLabel_1_2_2.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1_2_2.setBounds(10, 54, 25, 24);
        panel_1_1_1.add(lblNewLabel_1_2_2);

        JLabel lblNewLabel_1_2_3 = new JLabel("择");
        lblNewLabel_1_2_3.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1_2_3.setBounds(10, 76, 25, 24);
        panel_1_1_1.add(lblNewLabel_1_2_3);

        JPanel panel_1 = new JPanel();
        panel_1.setLayout(null);
        panel_1.setBackground(Color.WHITE);
//        panel_1.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        panel_1.setBounds(10, 222, 955, 159);
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

        JPanel panel_1_1_1_1 = new JPanel();
        panel_1_1_1_1.setLayout(null);
        panel_1_1_1_1.setBackground(Color.WHITE);
//        panel_1_1_1_1.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        panel_1_1_1_1.setBounds(10, 12, 35, 106);
        panel_1.add(panel_1_1_1_1);

        JLabel lblNewLabel_1_2_4 = new JLabel("查");
        lblNewLabel_1_2_4.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1_2_4.setBounds(10, 10, 25, 24);
        panel_1_1_1_1.add(lblNewLabel_1_2_4);

        JLabel lblNewLabel_1_2_1_1 = new JLabel("询");
        lblNewLabel_1_2_1_1.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1_2_1_1.setBounds(10, 32, 25, 24);
        panel_1_1_1_1.add(lblNewLabel_1_2_1_1);

        JLabel lblNewLabel_1_2_2_1 = new JLabel("设");
        lblNewLabel_1_2_2_1.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1_2_2_1.setBounds(10, 54, 25, 24);
        panel_1_1_1_1.add(lblNewLabel_1_2_2_1);

        JLabel lblNewLabel_1_2_3_1 = new JLabel("置");
        lblNewLabel_1_2_3_1.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1_2_3_1.setBounds(10, 76, 25, 24);
        panel_1_1_1_1.add(lblNewLabel_1_2_3_1);

        JLabel lblNewLabel_1_1_2_1 = new JLabel("开始时间：");
        lblNewLabel_1_1_2_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_1.setBounds(67, 14, 92, 23);
        panel_1.add(lblNewLabel_1_1_2_1);

        JLabel lblNewLabel_1_1_2_1_1 = new JLabel("结束时间：");
        lblNewLabel_1_1_2_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_1_1.setBounds(77, 47, 82, 23);
        panel_1.add(lblNewLabel_1_1_2_1_1);

        JLabel lblNewLabel_1_1_2_1_1_1 = new JLabel("最小粒度倍数：");
        lblNewLabel_1_1_2_1_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_1_1_1.setBounds(41, 80, 118, 23);
        panel_1.add(lblNewLabel_1_1_2_1_1_1);

        JComboBox<String> multiple_comboBox = new JComboBox<String>();
        multiple_comboBox.setBounds(169, 82, 72, 23);
        panel_1.add(multiple_comboBox);





        JButton btnNewButton = new JButton("数据查询");
        btnNewButton.setForeground(Color.WHITE);
        btnNewButton.setBackground(new Color(90, 181, 94));
        btnNewButton.setBounds(699, 136, 93, 23);
        panel_1.add(btnNewButton);

        JButton btnNewButton_1 = new JButton("统计查询");
        btnNewButton_1.setForeground(Color.WHITE);
        btnNewButton_1.setBackground(new Color(251, 140, 0));
        btnNewButton_1.setBounds(810, 136, 93, 23);
        panel_1.add(btnNewButton_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_1 = new JLabel("秒");
        lblNewLabel_2_2_1_2_1_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_1.setBounds(670, 14, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1 = new JLabel("分");
        lblNewLabel_2_2_1_2_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1.setBounds(587, 14, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1 = new JLabel("时");
        lblNewLabel_2_2_1_2_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1.setBounds(505, 14, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1 = new JLabel("日");
        lblNewLabel_2_2_1_2_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1.setBounds(427, 14, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1 = new JLabel("月");
        lblNewLabel_2_2_1_2_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1.setBounds(344, 14, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1);

        JLabel lblNewLabel_2_2_1_2_1 = new JLabel("年");
        lblNewLabel_2_2_1_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1.setBounds(257, 13, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1);

        JLabel lblNewLabel_2_2_1_2_1_2 = new JLabel("年");
        lblNewLabel_2_2_1_2_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_2.setBounds(257, 47, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_2 = new JLabel("月");
        lblNewLabel_2_2_1_2_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_2.setBounds(344, 48, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_1_2 = new JLabel("日");
        lblNewLabel_2_2_1_2_1_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_2.setBounds(427, 48, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_2 = new JLabel("时");
        lblNewLabel_2_2_1_2_1_1_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_2.setBounds(505, 48, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_2 = new JLabel("分");
        lblNewLabel_2_2_1_2_1_1_1_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_2.setBounds(587, 48, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_1_1 = new JLabel("秒");
        lblNewLabel_2_2_1_2_1_1_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_1_1.setBounds(670, 48, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1_1_1_1);

        JPanel panel_1_1 = new JPanel();
        panel_1_1.setLayout(null);
        panel_1_1.setBackground(Color.WHITE);
//        panel_1_1.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        panel_1_1.setBounds(10, 384, 955, 365);
        add(panel_1_1);


        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(10, 10, 935, 345);
        panel_1_1.add(scrollPane);

//		textField_4 = new JTextField();
//		textField_4.setBounds(274, 18, 93, 21);
//		add(textField_4);
//		textField_4.setColumns(10);
//		textField_4.setVisible(false);

        comboBox1 = new JComboBox<>();
        JComboBox<String> comboBox2 = new JComboBox<>();
        JComboBox<String> comboBox3 = new JComboBox<>();

        comboBox1.setBounds(167, 15, 163, 23);
        comboBox2.setBounds(415, 15, 207, 23);
        comboBox3.setBounds(762, 15, 183, 23);
        panel.add(comboBox1);
        panel.add(comboBox2);
        panel.add(comboBox3);
        comboBox2.setEnabled(false);
        comboBox3.setEnabled(false);

        JTextField pname_textField = new JTextField();
        pname_textField.setEditable(false);
        pname_textField.setColumns(10);
        pname_textField.setBounds(751, 87, 152, 21);
        panel.add(pname_textField);

        JLabel lblNewLabel_1_3_1_1 = new JLabel("策略名称：");
        lblNewLabel_1_3_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_3_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_3_1_1.setBounds(662, 87, 91, 23);
        panel.add(lblNewLabel_1_3_1_1);
        comboBox1.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    policylist = ConsumerMainView.polivylist;
                    String selected = (String) comboBox1.getSelectedItem();
                    if ("请选择".equals(selected)) {
                        starttime_textField.setText("");
                        endtime_textField.setText("");
                        multiple_textField.setText("");
                        mingranularity_textField.setText("");
                        comboBox2.removeAllItems();
                        comboBox2.setEnabled(false);
                        comboBox3.removeAllItems();
                        comboBox3.setEnabled(false);
                        multiple_comboBox.removeAllItems();

                    } else {
                        multiple_comboBox.removeAllItems();
                        starttime_textField.setText("");
                        endtime_textField.setText("");
                        multiple_textField.setText("");
                        mingranularity_textField.setText("");
                        String selected_name = (String) comboBox1.getSelectedItem();

                        List<Long> streamidList = new ArrayList<Long>();
                        Set<Long> nameSet = new HashSet<>();
                        for (PrivacyPolicy policy : policylist) {
                            if (policy.getUsrName().equals(selected_name)) {
                                nameSet.add(policy.getStreamID());
                                streamidList = new ArrayList<>(nameSet);
                            }

                        }


                        comboBox2.setEnabled(true);
                        comboBox2.removeAll();
                        List<String> stream_nameidList = new ArrayList<String>();
                        FrontEndSQL sql = new FrontEndSQL();
                        //stream_nameidList.add("请选择");
                        for (Long id : streamidList) {
                            stream_nameidList.add(sql.searchStream_Name_ID(id));
                            System.out.println(sql.searchStream_Name_ID(id));
                        }
                        String[] array = stream_nameidList.toArray(new String[stream_nameidList.size()]);

                        String[] newStrArray = new String[array.length + 1];
                        // 将要插入的字符串赋值给新数组的第一个元素
                        newStrArray[0] = "请选择";

                        // 将原数组的元素复制到新数组中
                        for (int i = 0; i < array.length; i++) {
                            newStrArray[i + 1] = array[i];
                        }
                        comboBox2.setModel(new DefaultComboBoxModel<>(newStrArray));
                        comboBox3.setEnabled(false);
                        comboBox3.removeAllItems();

                    }
                }
            }
        });

        comboBox2.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selected = (String) comboBox2.getSelectedItem();
                    List<String> s_policyList = new ArrayList<String>();

                    Long id = getValueAfterAsterisk(selected);
                    FrontEndSQL sql = new FrontEndSQL();
                    s_policyList = sql.getPolicyidlist(id, textField_3.getText());
                    List<String> stringList = new ArrayList<>();
                    for (String num : s_policyList) {
                        stringList.add(num);
                    }

                    String[] array = stringList.toArray(new String[stringList.size()]);
                    comboBox3.setModel(new DefaultComboBoxModel<>(array));
                    comboBox3.setEnabled(true);
                    starttime_textField.setText("");
                    endtime_textField.setText("");
                    multiple_textField.setText("");
                    mingranularity_textField.setText("");
                    multiple_comboBox.removeAllItems();
                }
            }
        });

        comboBox3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selected3 = (String) comboBox3.getSelectedItem();
                if(selected3==null){
                    System.out.println("无策略");
                }else{
                    Long id =  getValueAfterAsterisk(selected3);
                    FrontEndSQL sql =new FrontEndSQL();
                    String[] message = null;
                    if (!(selected3 == null)) {
                        message = sql.getSelected_p(id);
                        starttime_textField.setText(message[1]);
                        endtime_textField.setText(message[2]);
                        multiple_textField.setText(message[3]);
                        pname_textField.setText(message[4]);

                        String select4 = (String) comboBox2.getSelectedItem();
                        Long streamid = getValueAfterAsterisk(select4);

                        mingranularity_textField.setText(Long.toString(sql.getMingranularity(streamid)));


                        initializeDateTimeFieldsStart(startYearBox, startMonthBox, startDayBox, startHourBox, startMinuteBox, startSecondBox, MyUtil.str2Date(message[1]), MyUtil.str2Date(message[2]), Long.parseLong(mingranularity_textField.getText()), 169, 14, true);
                        initializeDateTimeFieldsEnd(endYearBox, endMonthBox, endDayBox, endHourBox, endMinuteBox, endSecondBox, MyUtil.str2Date(message[1]), MyUtil.str2Date(message[2]), Long.parseLong(mingranularity_textField.getText()), 169, 47, true);
                        isselectdate = true;


                        int b = Integer.parseInt(multiple_textField.getText());

                        List<String> string_list = new ArrayList<String>();
                        for (int a = b; a <= 60; a++) {

                            string_list.add(String.valueOf(a));
                        }

                        String[] array = string_list.toArray(new String[string_list.size()]);
                        multiple_comboBox.setModel(new DefaultComboBoxModel<>(array));
                    }
                }


            }
        });


        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (mingranularity_textField.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "请选择完整信息！");
                } else {
                    if (!multiple_textField.getText().equals("1")) {
                        JOptionPane.showMessageDialog(null, "不允许数据查询");
                    } else {

                        textArea.setText("");
                        Long ppIdS =  getValueAfterAsterisk((String) comboBox3.getSelectedItem());
//                        String ppIdS = (String) comboBox3.getSelectedItem();

                        String NameId = (String) comboBox2.getSelectedItem();

                        Date startDate = getstartDate(startYearBox, startMonthBox, startDayBox, startHourBox, startMinuteBox, startSecondBox);
                        Date endDate = getEndtimeDate(endYearBox, endMonthBox, endDayBox, endHourBox, endMinuteBox, endSecondBox);

                        if (startDate.getTime() >= MyUtil.str2Date(starttime_textField.getText()).getTime()
                                && endDate.getTime() <= MyUtil.str2DateEnd(endtime_textField.getText()).getTime()
                                && endDate.getTime() <= MyUtil.str2DateEnd(endtime_textField.getText()).getTime()
                                && startDate.getTime() < endDate.getTime()) {


                            //startDate 开始时间    endDate  结束时间
                            long ppId = ppIdS; //策略id
                            long sid = getValueAfterAsterisk(NameId);//流id
                            String ownerName = (String) comboBox1.getSelectedItem();//拥有者名称
                            String consumerName = dataConsumer.getUsrName();
                            //添加方法  数据查询----------------------------------


                            Token tk;
                            List<ChunkForDC> cks;
                            try {
                                tk = dataConsumer.sendRequest(consumerName, ownerName, ppId, sid, startDate, endDate, 1);
                                //System.out.println(tk==null);
                                cks = dataConsumer.getChunksDC(tk, startDate, endDate);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            } catch (ClassNotFoundException ex) {
                                throw new RuntimeException(ex);
                            } catch (CouldNotReceiveException ex) {
                                throw new RuntimeException(ex);
                            }

                            for (ChunkForDC chunk : cks) {
                                textArea.append(chunk.toString() + "\n");
                            }


                            //添加方法  数据查询----------------------------------

                            textArea.append("数据查询结束");


                            JOptionPane.showMessageDialog(null, "数据查询结束！");
                        } else {
                            JOptionPane.showMessageDialog(null, "请选择正确的时间范围！");
                        }


                    }
                }
            }
        });


        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (mingranularity_textField.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "请选择完整信息！");
                } else {
                    textArea.setText("");
                    Long ppIdS =  getValueAfterAsterisk((String) comboBox3.getSelectedItem());
//                    String ppIdS = (String) comboBox3.getSelectedItem();
                    String NameId = (String) comboBox2.getSelectedItem();

                    Date startDate = getstartDate(startYearBox, startMonthBox, startDayBox, startHourBox, startMinuteBox, startSecondBox);
                    Date endDate = getEndtimeDate(endYearBox, endMonthBox, endDayBox, endHourBox, endMinuteBox, endSecondBox);

                    if (startDate.getTime() >= MyUtil.str2Date(starttime_textField.getText()).getTime()
                            && endDate.getTime() <= MyUtil.str2DateEnd(endtime_textField.getText()).getTime()
                            && startDate.getTime() < endDate.getTime()) {

                        //startDate 开始时间    endDate  结束时间
                        long ppId = ppIdS;//策略id
                        long sid = getValueAfterAsterisk(NameId);//流id
                        String ownerName = (String) comboBox1.getSelectedItem();//拥有者名称
                        long multiple = Long.parseLong((String) multiple_comboBox.getSelectedItem());//最小粒度倍数
                        //添加方法  统计查询
                        String consumerName = dataConsumer.getUsrName();

                        Token tk;
                        List<Digest> digests;
                        Digest allDigest;
                        try {
                            tk = dataConsumer.sendRequest(consumerName, ownerName, ppId, sid, startDate, endDate, multiple);
                            //digests = dataConsumer.getDigestsDC(tk, startDate, endDate, (int)tk.getGranularity());
                            digests = dataConsumer.getNewDigestsDC(tk, startDate, endDate, (int) tk.getGranularity());
                            allDigest = dataConsumer.getAllNewDigestsDC(tk, startDate, endDate);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        } catch (ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        } catch (CouldNotReceiveException ex) {
                            throw new RuntimeException(ex);
                        }

                        FrontEndSQL sql = new FrontEndSQL();
                        streamHandling.Stream curStream = sql.getStream(sid);

                        for (Digest digest : digests) {
                            textArea.append(StatisticInfoNew.getStatisticInfo(curStream, digest) + "\n");
                        }

                        //添加方法
                        textArea.append("统计查询结束");

//						//本此生成图片URL
//						long[] xTimeValues = new long[60];
//						float[] yAveValues = new float[60];
//						int iter = digests.size()/60;
//						for (int i = 0; i < 60; i++) {
//							Digest curD = digests.get(i*iter);
//							yAveValues[i] = (float) curD.getSum() /curD.getCount();
//							xTimeValues[i] = curD.getStartTime(curStream);
//						}

//						int dataSize = digests.size();
//						int numPoints = Math.min(dataSize, 60); // 实际要显示的数据点数量
//						long[] xTimeValues = new long[numPoints];
//						float[] yAveValues = new float[numPoints];
//
//						int iter = dataSize / numPoints;
//						for (int i = 0; i < numPoints; i++) {
//							Digest curD = digests.get(i * iter);
//							yAveValues[i] = (float) curD.getSum() / curD.getCount();
//							xTimeValues[i] = curD.getStartTime(curStream);
//						}

                        int dataSize = digests.size();
                        int numLabels = 7; // 希望显示的标签数量
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
//						System.out.println("sdffffffffffffffffffffffffffffffffffffff");
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

//						System.out.println("yCountValues:"+ Arrays.toString(yCountValues));
//						System.out.println("statisticValues:"+ Arrays.toString(statisticValues));
//						NSystem.out.println("average: "+ average);

                        long time = (System.currentTimeMillis() / 1000) * 1000;
                        String path = "traffic_access_core/data/plot" + time + ".png";  // 固定的输出文件路径
                        //String picType = "histogram";
                        String picType = "line";

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

                        //写入数据库
                        History history = new History();
                        history.setUsrName(textField_3.getText());
                        history.setType("单流查询");
                        history.setTime(new Date(time));
                        history.setStreamID(sid);
                        history.setStartTime(startDate);
                        history.setEndTime(endDate);
                        history.setUrl(path);
                        FrontEndSQL sql1 = new FrontEndSQL();
                        sql1.InsertHistory(history);

                        //显示图片
                        showpicture(path);


//						JOptionPane.showMessageDialog(null, "统计查询结束！");
                    } else {
                        JOptionPane.showMessageDialog(null, "请选择正确的时间范围！");
                    }
                }
            }
        });


    }

    private void initializeDateTimeFieldsStart(JComboBox<Integer> yearBox, JComboBox<Integer> monthBox, JComboBox<Integer> dayBox, JComboBox<Integer> hourBox, JComboBox<Integer> minuteBox, JComboBox<Integer> secondBox, Date startDate, Date endDate, long granularity, int x, int y, boolean b) {
        if (!b) {
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
        if (!b) {
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

    private Date getstartDate(JComboBox<Integer> yearBox, JComboBox<Integer> monthBox, JComboBox<Integer> dayBox, JComboBox<Integer> hourBox, JComboBox<Integer> minuteBox, JComboBox<Integer> secondBox) {
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
        //selectedDateTime.add(Calendar.SECOND, -1);
        return selectedDateTime.getTime();
    }


    public static boolean isDateInRange(Date date, Date startDate, Date endDate) {
        return (date.equals(startDate) || date.after(startDate)) && (date.equals(endDate) || date.before(endDate));
    }

    public static long getValueAfterAsterisk(String str) {
        int index = str.indexOf("*");

        if (index != -1) {
            String numStr = str.substring(index + 1);
            try {
                return Long.parseLong(numStr);
            } catch (NumberFormatException e) {
                // 处理转换异常
                return -1;
            }
        }

        return -1;
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
//	public void showpicture(String url) {
//		if (url.isEmpty()) {
//			JOptionPane.showMessageDialog(null, "请输入图片路径", "错误", JOptionPane.ERROR_MESSAGE);
//			return;
//		}
//
//		try {
//			File file = new File(url);
//			if (!file.exists()) {
//				JOptionPane.showMessageDialog(null, "指定路径的图片不存在", "错误", JOptionPane.ERROR_MESSAGE);
//				return;
//			}
//
//			Image img = ImageIO.read(file);
//			ImageIcon icon = new ImageIcon(img);
//			JLabel label = new JLabel(icon);
//			JOptionPane.showMessageDialog(null, label, "图片预览", JOptionPane.PLAIN_MESSAGE);
//		} catch (Exception ex) {
//			JOptionPane.showMessageDialog(null, "无法加载图片: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
//		}
//	}
}
