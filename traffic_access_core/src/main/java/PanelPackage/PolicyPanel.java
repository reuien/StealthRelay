package PanelPackage;

import Item.MPCPolicy;
import Item.PrivacyPolicy;
import Item.Stream;
import Util.MyUtil;
import sqlConnect.FrontEndSQL;
import usrs.DataOwnerClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.*;

public class PolicyPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    public JTextField id_textField;

    public static List<Stream> streamlist = OwnerMainView.streamlist;
    public static Map<String, Stream> streammap = OwnerMainView.streammap;
    public static Map<Long, List<Long>> Idmap = new HashMap<>();
    public static Map<Long, List<PrivacyPolicy>> Cpmap = new HashMap<>();
    public JComboBox<String> comboBox = new JComboBox<String>();

    public static PrivacyPolicy policy = new PrivacyPolicy();
    public static MPCPolicy mpcPolicy = new MPCPolicy();
    public static DataOwnerClient doc;
    private JTextField textField;
    private JTextField textField_1;


    private JTextField textField_2;
    private JTextField textField_3;
    private JTextField textField_4;
    private JTextField p_name;
    private JTextField p_name2;

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


    private JComboBox<Integer> startYearBox1;
    private JComboBox<Integer> startMonthBox1;
    private JComboBox<Integer> startDayBox1;
    private JComboBox<Integer> startHourBox1;
    private JComboBox<Integer> startMinuteBox1;
    private JComboBox<Integer> startSecondBox1;

    private JComboBox<Integer> endYearBox1;
    private JComboBox<Integer> endMonthBox1;
    private JComboBox<Integer> endDayBox1;
    private JComboBox<Integer> endHourBox1;
    private JComboBox<Integer> endMinuteBox1;
    private JComboBox<Integer> endSecondBox1;
    private boolean isselectdate;
    private JTextField custid_textField;

    /**
     * Create the panel.
     */
    public PolicyPanel(OwnerMainView ownermainview) throws Exception {
        setLayout(null);
		this.setBackground(Color.WHITE);
        System.out.println("隐私策略");

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


        startYearBox1 = new JComboBox<>();
        startMonthBox1 = new JComboBox<>();
        startDayBox1 = new JComboBox<>();
        startHourBox1 = new JComboBox<>();
        startMinuteBox1 = new JComboBox<>();
        startSecondBox1 = new JComboBox<>();

        endYearBox1 = new JComboBox<>();
        endMonthBox1 = new JComboBox<>();
        endDayBox1 = new JComboBox<>();
        endHourBox1 = new JComboBox<>();
        endMinuteBox1 = new JComboBox<>();
        endSecondBox1 = new JComboBox<>();


        doc = ownermainview.doc;

        initializeDateTimeFields(startYearBox, startMonthBox, startDayBox, startHourBox, startMinuteBox, startSecondBox, 191, 102, isselectdate);
        initializeDateTimeFields(endYearBox, endMonthBox, endDayBox, endHourBox, endMinuteBox, endSecondBox, 191, 146, isselectdate);

        id_textField = new JTextField();
        id_textField.setBounds(132, 30, 66, 21);
        add(id_textField);
        id_textField.setColumns(10);
        id_textField.setVisible(false);


        JLabel lblNewLabel = new JLabel("隐私策略");
        lblNewLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel.setBounds(10, 22, 435, 30);
        add(lblNewLabel);

        JSeparator separator = new JSeparator();
        separator.setBounds(10, 64, 955, 12);
        separator.setForeground(Color.GRAY);
        add(separator);

        JPanel panel = new JPanel();
        panel.setBounds(10, 86, 955, 147);
        panel.setLayout(null);
        add(panel);


        JLabel lblNewLabel_1 = new JLabel("选择流：");
        lblNewLabel_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1.setBounds(10, 60, 76, 23);
        panel.add(lblNewLabel_1);

        JPanel panel_1 = new JPanel();
        panel_1.setLayout(null);
        panel_1.setBounds(10, 252, 955, 249);
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


        JLabel lblNewLabel_1_1_2 = new JLabel("可访问的消费者：");
        lblNewLabel_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2.setBounds(10, 58, 158, 23);
        panel_1.add(lblNewLabel_1_1_2);

        JButton btnNewButton = new JButton("设置单流策略");
        btnNewButton.setForeground(Color.WHITE);
        btnNewButton.setBackground(new Color(90, 181, 94));
        btnNewButton.setBounds(773, 206, 110, 23);
        panel_1.add(btnNewButton);

        JComboBox<String> comboBox_consumer = new JComboBox<String>();
        comboBox_consumer.setBounds(169, 60, 202, 23);
        panel_1.add(comboBox_consumer);

        JLabel lblNewLabel_1_1_2_1 = new JLabel("允许访问的开始时间：");
        lblNewLabel_1_1_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_1.setBounds(10, 102, 158, 23);
        panel_1.add(lblNewLabel_1_1_2_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_1 = new JLabel("秒");
        lblNewLabel_2_2_1_2_1_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_1.setBounds(693, 102, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1 = new JLabel("分");
        lblNewLabel_2_2_1_2_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1.setBounds(610, 102, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1 = new JLabel("时");
        lblNewLabel_2_2_1_2_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1.setBounds(528, 102, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1 = new JLabel("日");
        lblNewLabel_2_2_1_2_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1.setBounds(450, 102, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1);

        JLabel lblNewLabel_2_2_1_2_1_1 = new JLabel("月");
        lblNewLabel_2_2_1_2_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1.setBounds(367, 102, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1);

        JLabel lblNewLabel_2_2_1_2_1 = new JLabel("年");
        lblNewLabel_2_2_1_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1.setBounds(280, 101, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1);

        JLabel lblNewLabel_1_1_2_1_1 = new JLabel("允许访问的结束时间：");
        lblNewLabel_1_1_2_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_1_1.setBounds(10, 146, 158, 23);
        panel_1.add(lblNewLabel_1_1_2_1_1);

        JLabel lblNewLabel_2_2_1_2_1_2 = new JLabel("年");
        lblNewLabel_2_2_1_2_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_2.setBounds(280, 145, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_2 = new JLabel("月");
        lblNewLabel_2_2_1_2_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_2.setBounds(367, 146, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_1_2 = new JLabel("日");
        lblNewLabel_2_2_1_2_1_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_2.setBounds(450, 146, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_2 = new JLabel("时");
        lblNewLabel_2_2_1_2_1_1_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_2.setBounds(528, 146, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_2 = new JLabel("分");
        lblNewLabel_2_2_1_2_1_1_1_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_2.setBounds(610, 146, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_1_1 = new JLabel("秒");
        lblNewLabel_2_2_1_2_1_1_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_1_1.setBounds(693, 146, 30, 25);
        panel_1.add(lblNewLabel_2_2_1_2_1_1_1_1_1_1_1);

        JLabel lblNewLabel_1_1_2_1_1_1 = new JLabel("允许访问的最小粒度倍数：");
        lblNewLabel_1_1_2_1_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_1_1_1.setBounds(0, 191, 189, 23);
        panel_1.add(lblNewLabel_1_1_2_1_1_1);

        JComboBox<String> comboBox_1_1 = new JComboBox<String>();
        comboBox_1_1.setBounds(203, 193, 72, 23);
        panel_1.add(comboBox_1_1);

        custid_textField = new JTextField();
        custid_textField.setBounds(401, 61, 66, 21);
        panel_1.add(custid_textField);
        custid_textField.setColumns(10);

        JLabel lblNewLabel_1_2_1 = new JLabel("单流策略");
        lblNewLabel_1_2_1.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1_2_1.setBounds(0, 0, panel_1.getWidth(), 50);
        lblNewLabel_1_2_1.setOpaque(true);
        lblNewLabel_1_2_1.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel_1_2_1.setBackground(new Color(42, 115, 197));
        lblNewLabel_1_2_1.setForeground(Color.WHITE);
        panel_1.add(lblNewLabel_1_2_1);

        custid_textField.setVisible(false);

        for (long a = 1; a <= 60; a++) {
            comboBox_1_1.addItem(Long.toString(a));
        }

        for (String name : getconsumerlist()) {
            comboBox_consumer.addItem(name);
        }

        comboBox.removeAll();
        comboBox.setBounds(82, 62, 241, 23);
        panel.add(comboBox);

        textField = new JTextField();
        textField.setBounds(403, 63, 221, 21);
        panel.add(textField);
        textField.setColumns(10);
        textField.setEditable(false);

        JLabel lblNewLabel_1_1 = new JLabel("流名称：");
        lblNewLabel_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1.setBounds(333, 60, 76, 23);
        panel.add(lblNewLabel_1_1);

        JLabel lblNewLabel_1_1_1 = new JLabel("流类型：");
        lblNewLabel_1_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_1.setBounds(635, 60, 76, 23);
        panel.add(lblNewLabel_1_1_1);

        textField_1 = new JTextField();
        textField_1.setEditable(false);
        textField_1.setColumns(10);
        textField_1.setBounds(710, 63, 221, 21);
        panel.add(textField_1);

        JLabel lblNewLabel_1_2_1_1 = new JLabel("流信息");
        lblNewLabel_1_2_1_1.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1_2_1_1.setBounds(0, 0, panel.getWidth(), 50);
        lblNewLabel_1_2_1_1.setOpaque(true);
        lblNewLabel_1_2_1_1.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel_1_2_1_1.setBackground(new Color(42, 115, 197));
        lblNewLabel_1_2_1_1.setForeground(Color.WHITE);
        panel.add(lblNewLabel_1_2_1_1);

        JSeparator separator_1_1_1 = new JSeparator();
        separator_1_1_1.setForeground(Color.GRAY);
        separator_1_1_1.setBounds(10, 38, 935, 12);
        panel.add(separator_1_1_1);

        JLabel lblNewLabel_1_1_2_2 = new JLabel("设置的开始时间：");
        lblNewLabel_1_1_2_2.setBounds(10, 101, 158, 23);
        panel.add(lblNewLabel_1_1_2_2);
        lblNewLabel_1_1_2_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));


        textField_2 = new JTextField();
        textField_2.setBounds(169, 104, 141, 21);
        panel.add(textField_2);
        textField_2.setEditable(false);
        textField_2.setColumns(10);

        JLabel lblNewLabel_1_1_2_2_1 = new JLabel("设置的结束时间：");
        lblNewLabel_1_1_2_2_1.setBounds(320, 101, 158, 23);
        panel.add(lblNewLabel_1_1_2_2_1);
        lblNewLabel_1_1_2_2_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));

        textField_3 = new JTextField();
        textField_3.setBounds(476, 104, 141, 21);
        panel.add(textField_3);
        textField_3.setColumns(10);
        textField_3.setEditable(false);

        JLabel lblNewLabel_1_1_2_2_1_1 = new JLabel("设置的最小粒度：");
        lblNewLabel_1_1_2_2_1_1.setBounds(651, 101, 149, 23);
        panel.add(lblNewLabel_1_1_2_2_1_1);
        lblNewLabel_1_1_2_2_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_2_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));

        textField_4 = new JTextField();
        textField_4.setBounds(798, 104, 141, 21);
        panel.add(textField_4);
        textField_4.setColumns(10);
        textField_4.setEditable(false);

        JPanel panel_2 = new JPanel();
        panel_2.setLayout(null);
        panel_2.setBounds(10, 521, 955, 223);
        add(panel_2);


        panel_2.add(startYearBox1);
        panel_2.add(startMonthBox1);
        panel_2.add(startDayBox1);
        panel_2.add(startHourBox1);
        panel_2.add(startMinuteBox1);
        panel_2.add(startSecondBox1);

        panel_2.add(endYearBox1);
        panel_2.add(endMonthBox1);
        panel_2.add(endDayBox1);
        panel_2.add(endHourBox1);
        panel_2.add(endMinuteBox1);
        panel_2.add(endSecondBox1);


        initializeDateTimeFields(startYearBox1, startMonthBox1, startDayBox1, startHourBox1, startMinuteBox1, startSecondBox1, 191, 99, isselectdate);
        initializeDateTimeFields(endYearBox1, endMonthBox1, endDayBox1, endHourBox1, endMinuteBox1, endSecondBox1, 191, 143, isselectdate);
        JLabel lblNewLabel_1_2 = new JLabel("联邦策略");
        lblNewLabel_1_2.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1_2.setBounds(0, 0, panel_2.getWidth(), 50);
        lblNewLabel_1_2.setOpaque(true);
        lblNewLabel_1_2.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel_1_2.setBackground(new Color(42, 115, 197));
        lblNewLabel_1_2.setForeground(Color.WHITE);
        panel_2.add(lblNewLabel_1_2);

        JSeparator separator_1 = new JSeparator();
        separator_1.setForeground(Color.GRAY);
        separator_1.setBounds(10, 38, 935, 12);
        panel_2.add(separator_1);

        JButton btnNewButton_1 = new JButton("设置联邦策略");
        btnNewButton_1.setForeground(Color.WHITE);
        btnNewButton_1.setBackground(new Color(90, 181, 94));
        btnNewButton_1.setBounds(779, 156, 110, 23);
        panel_2.add(btnNewButton_1);
        btnNewButton_1.setEnabled(false);

        JLabel lblNewLabel_1_1_2_3 = new JLabel("是否参与联邦：");
        lblNewLabel_1_1_2_3.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_3.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_3.setBounds(10, 60, 158, 23);
        panel_2.add(lblNewLabel_1_1_2_3);

        JLabel lblNewLabel_1_1_2_4 = new JLabel("可访问的消费者：");
        lblNewLabel_1_1_2_4.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_4.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_4.setBounds(280, 60, 158, 23);
        panel_2.add(lblNewLabel_1_1_2_4);

        JComboBox<String> comboBox_consumer_1 = new JComboBox<String>();
        comboBox_consumer_1.setBounds(438, 62, 202, 23);
        panel_2.add(comboBox_consumer_1);

        for (String name : getconsumerlist()) {
            comboBox_consumer_1.addItem(name);
        }

        JLabel lblNewLabel_1_1_2_1_2 = new JLabel("允许访问的开始时间：");
        lblNewLabel_1_1_2_1_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_1_2.setBounds(10, 99, 158, 23);
        panel_2.add(lblNewLabel_1_1_2_1_2);

        JLabel lblNewLabel_1_1_2_1_1_2 = new JLabel("允许访问的结束时间：");
        lblNewLabel_1_1_2_1_1_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_1_1_2.setBounds(10, 143, 158, 23);
        panel_2.add(lblNewLabel_1_1_2_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_3 = new JLabel("年");
        lblNewLabel_2_2_1_2_1_3.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_3.setBounds(280, 98, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_3);

        JLabel lblNewLabel_2_2_1_2_1_2_1 = new JLabel("年");
        lblNewLabel_2_2_1_2_1_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_2_1.setBounds(280, 142, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_2_1);

        JLabel lblNewLabel_2_2_1_2_1_1_3 = new JLabel("月");
        lblNewLabel_2_2_1_2_1_1_3.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_3.setBounds(367, 99, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_3);

        JLabel lblNewLabel_2_2_1_2_1_1_2_1 = new JLabel("月");
        lblNewLabel_2_2_1_2_1_1_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_2_1.setBounds(367, 143, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_2_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_3 = new JLabel("日");
        lblNewLabel_2_2_1_2_1_1_1_3.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_3.setBounds(450, 99, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1_3);

        JLabel lblNewLabel_2_2_1_2_1_1_1_2_1 = new JLabel("日");
        lblNewLabel_2_2_1_2_1_1_1_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_2_1.setBounds(450, 143, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1_2_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_3 = new JLabel("时");
        lblNewLabel_2_2_1_2_1_1_1_1_3.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_3.setBounds(528, 99, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1_1_3);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_2_1 = new JLabel("时");
        lblNewLabel_2_2_1_2_1_1_1_1_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_2_1.setBounds(528, 143, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1_1_2_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_3 = new JLabel("分");
        lblNewLabel_2_2_1_2_1_1_1_1_1_3.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_3.setBounds(610, 99, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1_1_1_3);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_2_1 = new JLabel("分");
        lblNewLabel_2_2_1_2_1_1_1_1_1_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_2_1.setBounds(610, 143, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1_1_1_2_1);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_1_2 = new JLabel("秒");
        lblNewLabel_2_2_1_2_1_1_1_1_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_1_2.setBounds(693, 99, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1_1_1_1_2);

        JLabel lblNewLabel_2_2_1_2_1_1_1_1_1_1_1_1 = new JLabel("秒");
        lblNewLabel_2_2_1_2_1_1_1_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_2_1_1_1_1_1_1_1_1.setBounds(693, 143, 30, 25);
        panel_2.add(lblNewLabel_2_2_1_2_1_1_1_1_1_1_1_1);

        JLabel lblNewLabel_pname = new JLabel("策略名称：");
        lblNewLabel_pname.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_pname.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_pname.setBounds(377, 58, 158, 23);
        panel_1.add(lblNewLabel_pname);

        JLabel lblNewLabel_streamname_1 = new JLabel("策略名称：");
        lblNewLabel_streamname_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_streamname_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_streamname_1.setBounds(591, 60, 158, 23);
        panel_2.add(lblNewLabel_streamname_1);

        p_name = new JTextField();
        p_name.setBounds(543, 61, 148, 21);
        panel_1.add(p_name);
        p_name.setColumns(10);

        p_name2 = new JTextField();
        p_name2.setColumns(10);
        p_name2.setBounds(757, 63, 148, 21);
        panel_2.add(p_name2);

        JComboBox<String> comboBox_MPC = new JComboBox<String>();

        comboBox_MPC.setBounds(169, 62, 105, 23);
        panel_2.add(comboBox_MPC);
        comboBox_MPC.addItem("否");
        comboBox_MPC.addItem("是");

        comboBox_MPC.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (comboBox.getSelectedItem().equals("请选择")) {
                    JOptionPane.showMessageDialog(null, "请选择流！");
                } else {
                    streamlist = OwnerMainView.streamlist;
                    streammap = OwnerMainView.streammap;

                    String s = (String) comboBox_MPC.getSelectedItem();
                    if (s.equals("是")) {
                        String selectedDeviceId = (String) comboBox.getSelectedItem();
                        btnNewButton_1.setEnabled(true);
                        initializeDateTimeFieldsStart(startYearBox1, startMonthBox1, startDayBox1, startHourBox1, startMinuteBox1, startSecondBox1, streammap.get(selectedDeviceId).getStarttime(), streammap.get(selectedDeviceId).getEndtime(), streammap.get(selectedDeviceId).getMingranularity(), 191, 99, isselectdate);
                        initializeDateTimeFieldsEnd(endYearBox1, endMonthBox1, endDayBox1, endHourBox1, endMinuteBox1, endSecondBox1, streammap.get(selectedDeviceId).getStarttime(), streammap.get(selectedDeviceId).getEndtime(), streammap.get(selectedDeviceId).getMingranularity(), 191, 143, isselectdate);
                        isselectdate = true;
                    } else {
                        btnNewButton_1.setEnabled(false);
                    }
                }
            }

        });
        comboBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                streamlist = OwnerMainView.streamlist;
                streammap = OwnerMainView.streammap;
                if (comboBox.getSelectedItem().equals("请选择")) {
                    textField.setText("");
                    textField_1.setText("");
                    textField_2.setText("");
                    textField_3.setText("");
                    textField_4.setText("");

                } else {
                    String selectedDeviceId = (String) comboBox.getSelectedItem();
                    textField.setText(streammap.get(selectedDeviceId).getName());
                    textField_1.setText(streammap.get(selectedDeviceId).getDesciption());
                    textField_2.setText(MyUtil.date2Str(streammap.get(selectedDeviceId).getStarttime()));
                    textField_3.setText(MyUtil.date2Str(streammap.get(selectedDeviceId).getEndtime()));
                    textField_4.setText(Long.toString(streammap.get(selectedDeviceId).getMingranularity()));

                    initializeDateTimeFieldsStart(startYearBox1, startMonthBox1, startDayBox1, startHourBox1, startMinuteBox1, startSecondBox1, streammap.get(selectedDeviceId).getStarttime(), streammap.get(selectedDeviceId).getEndtime(), streammap.get(selectedDeviceId).getMingranularity(), 191, 102, isselectdate);
                    initializeDateTimeFieldsEnd(endYearBox1, endMonthBox1, endDayBox1, endHourBox1, endMinuteBox1, endSecondBox1, streammap.get(selectedDeviceId).getStarttime(), streammap.get(selectedDeviceId).getEndtime(), streammap.get(selectedDeviceId).getMingranularity(), 191, 146, isselectdate);
                    initializeDateTimeFieldsStart(startYearBox, startMonthBox, startDayBox, startHourBox, startMinuteBox, startSecondBox, streammap.get(selectedDeviceId).getStarttime(), streammap.get(selectedDeviceId).getEndtime(), streammap.get(selectedDeviceId).getMingranularity(), 191, 102, isselectdate);
                    initializeDateTimeFieldsEnd(endYearBox, endMonthBox, endDayBox, endHourBox, endMinuteBox, endSecondBox, streammap.get(selectedDeviceId).getStarttime(), streammap.get(selectedDeviceId).getEndtime(), streammap.get(selectedDeviceId).getMingranularity(), 191, 146, isselectdate);
                    isselectdate = true;
                }


            }
        });


        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (comboBox.getSelectedItem().equals("请选择")) {
                    JOptionPane.showMessageDialog(null, "请选择流！");
                } else {
                    String selectedDeviceId = (String) comboBox.getSelectedItem();

                    Date startDate = getStarttimeDate(startYearBox, startMonthBox, startDayBox, startHourBox, startMinuteBox, startSecondBox);
                    Date endDate = getEndtimeDate(endYearBox, endMonthBox, endDayBox, endHourBox, endMinuteBox, endSecondBox);
//		            System.out.println("开始时间："+startDate.getTime());
//		            System.out.println("结束时间："+endDate.getTime());
//		            System.out.println("liu开始时间："+stream_polivymap.get(selectedDeviceId).getStarttime().getTime());
//		            System.out.println("liu结束时间："+stream_polivymap.get(selectedDeviceId).getEndtime().getTime());
//		            System.out.println("开始"+(startDate.getTime()>=stream_polivymap.get(selectedDeviceId).getStarttime().getTime()));
//		            System.out.println(endDate.getTime()<=stream_polivymap.get(selectedDeviceId).getEndtime().getTime());
//		           System.out.println(startDate.getTime()<=endDate.getTime());
                    if (p_name.getText().equals("")) {
                        JOptionPane.showMessageDialog(null, "请输入策略名称！");
                    } else {
                        if (startDate.getTime() >= streammap.get(selectedDeviceId).getStarttime().getTime()
                                && endDate.getTime() <= streammap.get(selectedDeviceId).getEndtime().getTime()
                                && startDate.getTime() < endDate.getTime()) {

                            policy.setStartTime(startDate);
                            policy.setEndTime(endDate);
                            policy.setCustname((String) comboBox_consumer.getSelectedItem());
                            policy.setUsrName(getUsrName(id_textField.getText()));
                            policy.setMinGranularity(Long.valueOf((String) (comboBox_1_1.getSelectedItem())));
                            policy.setStreamID(streammap.get(selectedDeviceId).getId());
                            policy.setPolicyName(p_name.getText());

                            //添加方法 ---------------- 单流策略设置，使用item包中的PrivacyPolicy的属性

                            System.out.println("start: " + startDate.getTime());
                            System.out.println("end  : " + endDate.getTime());
                            streamHandling.PrivacyPolicy pp;
                            try {
                                pp = doc.createPrivacyPolicy(policy.getCustname(), policy.getStreamID(),
                                        policy.getStartTime(), policy.getEndTime(), (int) policy.getMinGranularity());
                                policy.setPrivacyPolicyId(pp.getPrivacyPolicyId());
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }

                            //写入数据库
                            FrontEndSQL sql = new FrontEndSQL();
                            sql.insertPolicy(policy);

                            JOptionPane.showMessageDialog(null, "成功设置策略！");

                        } else {
                            JOptionPane.showMessageDialog(null, "请选择符合要求的时间范围！");
                        }
                    }

                }
            }
        });

        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedDeviceId = (String) comboBox.getSelectedItem();

                Date startDate = getStarttimeDate(startYearBox1, startMonthBox1, startDayBox1, startHourBox1, startMinuteBox1, startSecondBox1);
                Date endDate = getEndtimeDate(endYearBox1, endMonthBox1, endDayBox1, endHourBox1, endMinuteBox1, endSecondBox1);
                if (p_name2.equals("")) {
                    JOptionPane.showMessageDialog(null, "请输入策略名称！");
                } else {
                    if (startDate.getTime() >= streammap.get(selectedDeviceId).getStarttime().getTime()
                            && endDate.getTime() <= streammap.get(selectedDeviceId).getEndtime().getTime()
                            && startDate.getTime() < endDate.getTime()) {

                        mpcPolicy.setStartTime(startDate);
                        mpcPolicy.setEndTime(endDate);
                        mpcPolicy.setConsumerName((String) comboBox_consumer_1.getSelectedItem());
                        mpcPolicy.setOwnerName(getUsrName(id_textField.getText()));
                        mpcPolicy.setMinGranularity(Long.valueOf(textField_4.getText()));
                        mpcPolicy.setStreamID(streammap.get(selectedDeviceId).getId());
                        mpcPolicy.setPolicyName(p_name2.getText());

                        //添加方法 ---------------- 联邦策略设置，使用item包中的MPCPolicy的属性

                        System.out.println("start: " + startDate.getTime());
                        System.out.println("end  : " + endDate.getTime());
                        streamHandling.FederationPolicy fp;
                        try {
                            fp = doc.createFederationPolicy(mpcPolicy.getConsumerName(), mpcPolicy.getStreamID(),
                                    mpcPolicy.getStartTime(), mpcPolicy.getEndTime());
                            mpcPolicy.setPolicyId(fp.getFederationPolicyId());
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }

                        //写入数据库
                        FrontEndSQL sql = new FrontEndSQL();
                        sql.insertMpcPolicy(mpcPolicy);

                        JOptionPane.showMessageDialog(null, "成功设置策略！");
                    } else {
                        JOptionPane.showMessageDialog(null, "请选择符合要求的时间范围！");
                    }
                }


            }
        });

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

            // Set default selected values based on end date
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

    private Date getStarttimeDate(JComboBox<Integer> yearBox, JComboBox<Integer> monthBox, JComboBox<Integer> dayBox, JComboBox<Integer> hourBox, JComboBox<Integer> minuteBox, JComboBox<Integer> secondBox) {
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

    private List<String> getconsumerlist() {
        FrontEndSQL sql = new FrontEndSQL();
        return sql.searchCustom();
    }

    private String getUsrName(String id) {
        FrontEndSQL sql = new FrontEndSQL();
        String name = sql.searchName(id);
        return name;
    }
}
