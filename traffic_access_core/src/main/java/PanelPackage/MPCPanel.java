package PanelPackage;

import Item.History;
import Item.MPCPolicy;
import Util.MyUtil;
import org.apache.commons.lang3.tuple.Pair;
import py.PlotInterface;
import sqlConnect.FrontEndSQL;
import statistics.StaticticForFederationNew;
import streamHandling.FederationToken;
import usrs.DataConsumer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.List;
import java.util.*;

public class MPCPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTextField textField_type;
    private List<MPCPolicy> policies = new ArrayList<MPCPolicy>();
    private List<MPCPolicy> select_policies = new ArrayList<MPCPolicy>();
    public JTextField textField_name;
    private JTextField starttime_textField;
    private JTextField endtime_textField;
    private JTextField textField;
    private JTextField textField_1;
    private JTextField textField_2;
    private JTextField textField_3;
    private JTextField textField_4;
    private JTextField textField_5;
    public static DataConsumer dataConsumer;


    /**
     * Create the panel.
     */
    public MPCPanel(ConsumerMainView consumermainview) throws Exception {
        this.setBackground(Color.WHITE);
        setLayout(null);

        textField_name = new JTextField();
        add(textField_name);
        textField_name.setColumns(10);
        textField_name.setVisible(false);

        dataConsumer = consumermainview.consumer;

        JLabel lblNewLabel_1 = new JLabel("联邦查询");
        lblNewLabel_1.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1.setBounds(10, 10, 87, 30);
        add(lblNewLabel_1);

        JSeparator separator = new JSeparator();
        separator.setForeground(Color.GRAY);
        separator.setBounds(10, 52, 955, 12);
        add(separator);

        JPanel panel = new JPanel();
        panel.setLayout(null);
//        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        panel.setBounds(10, 434, 955, 235);
        add(panel);

        JComboBox<String> comboBox_1_1 = new JComboBox<String>();
        comboBox_1_1.setBounds(0, 0, 0, 0);
        panel.add(comboBox_1_1);

        JComboBox<String> comboBox_1 = new JComboBox<String>();
        comboBox_1.setBounds(0, 0, 0, 0);
        panel.add(comboBox_1);

        JComboBox<String> comboBox = new JComboBox<String>();
        comboBox.setBounds(0, 0, 0, 0);
        panel.add(comboBox);

        JLabel lblNewLabel_2_2_1_1 = new JLabel("可查询的时间范围:");
        lblNewLabel_2_2_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_1.setBounds(0, 62, 157, 25);
        panel.add(lblNewLabel_2_2_1_1);

        starttime_textField = new JTextField();
        starttime_textField.setEditable(false);
        starttime_textField.setColumns(10);
        starttime_textField.setBounds(165, 66, 142, 21);
        panel.add(starttime_textField);

        endtime_textField = new JTextField();
        endtime_textField.setEditable(false);
        endtime_textField.setColumns(10);
        endtime_textField.setBounds(359, 66, 142, 21);
        panel.add(endtime_textField);

        JLabel lblNewLabel_2_2_1_1_1 = new JLabel("—");
        lblNewLabel_2_2_1_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_1_1.setBounds(310, 62, 32, 25);
        panel.add(lblNewLabel_2_2_1_1_1);

        JLabel lblNewLabel_2_2_1_1_2 = new JLabel("总加和:");
        lblNewLabel_2_2_1_1_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_1_2.setBounds(32, 112, 61, 25);
        panel.add(lblNewLabel_2_2_1_1_2);

        JLabel lblNewLabel_1_2_1_1 = new JLabel("查询结果");
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

        JLabel lblNewLabel_2_2_1_1_2_1 = new JLabel("总计数:");
        lblNewLabel_2_2_1_1_2_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1_1_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_1_2_1.setBounds(309, 112, 61, 25);
        panel.add(lblNewLabel_2_2_1_1_2_1);

        JLabel lblNewLabel_2_2_1_1_2_2 = new JLabel("总平方和:");
        lblNewLabel_2_2_1_1_2_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1_1_2_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_1_2_2.setBounds(577, 112, 83, 25);
        panel.add(lblNewLabel_2_2_1_1_2_2);

        JLabel lblNewLabel_2_2_1_1_2_3 = new JLabel("总平均值:");
        lblNewLabel_2_2_1_1_2_3.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1_1_2_3.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_1_2_3.setBounds(21, 164, 72, 25);
        panel.add(lblNewLabel_2_2_1_1_2_3);

        JLabel lblNewLabel_2_2_1_1_2_4 = new JLabel("总标准差:");
        lblNewLabel_2_2_1_1_2_4.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1_1_2_4.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_1_2_4.setBounds(274, 164, 96, 25);
        panel.add(lblNewLabel_2_2_1_1_2_4);

        JLabel lblNewLabel_2_2_1_1_2_5 = new JLabel("总方差:");
        lblNewLabel_2_2_1_1_2_5.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1_1_2_5.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1_1_2_5.setBounds(599, 164, 61, 25);
        panel.add(lblNewLabel_2_2_1_1_2_5);

        textField = new JTextField();
        textField.setEditable(false);
        textField.setColumns(10);
        textField.setBounds(103, 116, 142, 21);
        panel.add(textField);

        textField_1 = new JTextField();
        textField_1.setEditable(false);
        textField_1.setColumns(10);
        textField_1.setBounds(373, 116, 142, 21);
        panel.add(textField_1);

        textField_2 = new JTextField();
        textField_2.setEditable(false);
        textField_2.setColumns(10);
        textField_2.setBounds(666, 116, 142, 21);
        panel.add(textField_2);

        textField_3 = new JTextField();
        textField_3.setEditable(false);
        textField_3.setColumns(10);
        textField_3.setBounds(103, 168, 142, 21);
        panel.add(textField_3);

        textField_4 = new JTextField();
        textField_4.setEditable(false);
        textField_4.setColumns(10);
        textField_4.setBounds(373, 168, 142, 21);
        panel.add(textField_4);

        textField_5 = new JTextField();
        textField_5.setEditable(false);
        textField_5.setColumns(10);
        textField_5.setBounds(666, 168, 142, 21);
        panel.add(textField_5);

        JPanel panel_1 = new JPanel();
        panel_1.setLayout(null);
//        panel_1.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        panel_1.setBounds(10, 74, 955, 339);
        add(panel_1);

        JComboBox<String> comboBox_1_1_1 = new JComboBox<String>();
        comboBox_1_1_1.setBounds(0, 0, 0, 0);
        panel_1.add(comboBox_1_1_1);

        JComboBox<String> comboBox_1_2 = new JComboBox<String>();
        comboBox_1_2.setBounds(0, 0, 0, 0);
        panel_1.add(comboBox_1_2);

        JComboBox<String> comboBox_2 = new JComboBox<String>();
        comboBox_2.setBounds(0, 0, 0, 0);
        panel_1.add(comboBox_2);

        JLabel lblNewLabel_2_2_1 = new JLabel("流类型:");
        lblNewLabel_2_2_1.setBounds(10, 11, 66, 25);
        panel_1.add(lblNewLabel_2_2_1);
        lblNewLabel_2_2_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));

        JLabel lblNewLabel_2_2_1_3 = new JLabel("其他类型:");
        lblNewLabel_2_2_1_3.setBounds(267, 11, 76, 25);
        panel_1.add(lblNewLabel_2_2_1_3);
        lblNewLabel_2_2_1_3.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1_3.setFont(new Font("微软雅黑", Font.PLAIN, 15));

        textField_type = new JTextField();
        textField_type.setBounds(353, 15, 142, 21);
        panel_1.add(textField_type);
        textField_type.setEditable(false);
        textField_type.setColumns(10);

        JComboBox<String> comboBox_type = new JComboBox<String>();

        comboBox_type.setBounds(86, 14, 142, 23);
        panel_1.add(comboBox_type);
        comboBox_type.addItem("请选择");
        comboBox_type.addItem("心率");
//        comboBox_type.addItem("车流量");
        comboBox_type.addItem("速率");
        comboBox_type.addItem("流量");
        comboBox_type.addItem("其他");

        JButton btnNewButton = new JButton("流 查 询");
        btnNewButton.setForeground(Color.WHITE);
        btnNewButton.setBackground(new Color(90, 181, 94));
        btnNewButton.setBounds(543, 14, 93, 23);
        panel_1.add(btnNewButton);

        Object[] columnNames = {"选择", "拥有者", "策略", "流id", "开始时间", "结束时间"};

        // 创建表格模型
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            /**
             *
             */
            private static final long serialVersionUID = -5985738951961623846L;

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class; // 第一列为Boolean类型，用于显示复选框
                } else {
                    return super.getColumnClass(columnIndex);
                }
            }
        };


        // 将初始数据添加到表格模型中
//		 model.addRow(new Object[]{false, "","", "", "", "", ""});

        // 创建JTable并设置模型
        JTable table = new JTable(model);

        // 创建一个自定义的JCheckBox 渲染器
        class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {
            /**
             *
             */
            private static final long serialVersionUID = -1554226097024849122L;

            public CheckBoxRenderer() {
                setHorizontalAlignment(SwingConstants.LEFT); // 设置复选框靠左对齐
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setSelected((Boolean) value); // 设置复选框的选中状态
                return this;
            }
        }

        // 将第一列设置为复选框列

        table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JCheckBox()));
        table.getColumnModel().getColumn(0).setCellRenderer(table.getDefaultRenderer(Boolean.class));
        // 将第一列设置为复选框列，并且设置渲染器为CheckBoxRenderer
        table.getColumnModel().getColumn(0).setCellRenderer(new CheckBoxRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(20);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        for(int i =1;i<4;i++) {
            table.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(new JTextField()) {
                @Override
                public boolean isCellEditable(EventObject e) {
                    return false; // 禁用编辑
                }
            });
        }


        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(10, 45, 930, 246);
        panel_1.add(scrollPane);

        JButton printButton = new JButton("联 邦 查 询");
        printButton.setForeground(Color.WHITE);
        printButton.setBackground(new Color(90, 181, 94));

        printButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                select_policies.clear();
                for (int i = 0; i < table.getRowCount(); i++) {
                    Boolean isChecked = (Boolean) table.getValueAt(i, 0); // 获取第一列的复选框状态
                    if (isChecked) {
                        // 获取勾选行的信息
                        String username = (String) table.getValueAt(i, 1);
                        Long policyId = getValueAfterAsterisk((String)table.getValueAt(i, 2));
                        Long streamID = getValueAfterAsterisk((String)table.getValueAt(i, 3));
                        String starttime = (String) table.getValueAt(i, 4);
                        String endtime = (String) table.getValueAt(i, 5);

                        for (MPCPolicy policy : policies) {
                            if (policy.getStreamID() == streamID) {
                                select_policies.add(policy);
                            }
                        }
                        // 打印勾选行的信息
                        System.out.println("用户名: " + username + ", 策略id: " + policyId + ", 流id: " + streamID + ", 开始时间： " + starttime + ", 结束时间： " + endtime);
                    }
                }

                if (select_policies.size() == 1) {
                    JOptionPane.showMessageDialog(null, "请选择流两个或两个以上的流！");
                } else {
                    //寻找公共时间
                    String[] rs = new String[2];
                    rs = findCommonTimeRange(select_policies);
                    if (rs == null) {
                        System.out.println("没有公共时间");
                        JOptionPane.showMessageDialog(null, "没有可查询的时间范围！");
                    } else {
                        for (int i = 0; i < rs.length; i++) {
                            System.out.println(rs[i]);
                        }

                        starttime_textField.setText(rs[0]);
                        endtime_textField.setText(rs[1]);

                        /*	rs[0]   开始时间
                         * 	rs[1]   结束时间
                         * 	select_policies   需要查询的流列表，属性在MPCpolicy中看
                         **/
                        ArrayList<Pair<String, Long>> nameAndStreamList = new ArrayList<>();

                        for (MPCPolicy policy : select_policies) {
                            nameAndStreamList.add(Pair.of(policy.getOwnerName(), policy.getStreamID()));
                        }

                        String stream_mpc = "";

                        for (int i = 0; i < select_policies.size(); i++) {
                            if (i == select_policies.size() - 1) {
                                stream_mpc = stream_mpc + String.valueOf(select_policies.get(i).getStreamID());

                            } else {
                                stream_mpc = stream_mpc + String.valueOf(select_policies.get(i).getStreamID()) + "+";
                            }
                        }


                        //联邦查询-------------------------

                        long fromTime = MyUtil.str2Date(rs[0]).getTime();
                        long toTime = MyUtil.str2DateEnd(rs[1]).getTime();

                        System.out.println(fromTime);
                        System.out.println(toTime);

                        FederationToken fToken;
                        //StaticticForFederation res;
                        StaticticForFederationNew res;
                        try {
                            fToken = dataConsumer.getFederationToken(dataConsumer.getUsrName(), nameAndStreamList, fromTime, toTime);
                            //res = dataConsumer.getFederationInfo(fToken, nameAndStreamList, fromTime, toTime);
                            res = dataConsumer.getFederationInfoNew(fToken, nameAndStreamList, fromTime, toTime);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        } catch (ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        }

                        textField.setText(Long.toString(res.getSum()));
                        textField_1.setText(Long.toString(res.getCount()));
                        textField_2.setText(Long.toString(res.getSquare()));
                        textField_3.setText(Double.toString(res.getAverage()));
                        textField_4.setText(Double.toString(res.getStd()));
                        textField_5.setText(Double.toString(res.getVariance()));


                        //联邦查询-------------------------


                        long[] yCountValues = new long[6];
                        yCountValues[0] = res.getCount1();
                        yCountValues[1] = res.getCount2();
                        yCountValues[2] = res.getCount3();
                        yCountValues[3] = res.getCount4();
                        yCountValues[4] = res.getCount5();
                        yCountValues[5] = res.getCount6();
                        System.out.println("yCountValues:" + Arrays.toString(yCountValues));

                        double[] statisticValues = new double[6];
                        statisticValues[0] = res.getAverage();
                        statisticValues[1] = res.getStd();
                        statisticValues[2] = res.getVariance();
                        statisticValues[3] = res.getSum();
                        statisticValues[4] = res.getCount();
                        statisticValues[5] = res.getSquare();
                        double average = res.getAverage();
                        System.out.println("statisticValues:" + Arrays.toString(statisticValues));
                        System.out.println("average: " + average);

                        long time = (System.currentTimeMillis() / 1000) * 1000;
                        String path = "traffic_access_core/data/plot" + time + ".png";  // 固定的输出文件路径

						/*ServerController serverController = new ServerController();
						// 启动服务器
						String pythonInterpreter = "C:\\ProgramData\\miniconda3\\python.exe";
						String scriptPath = "traffic_access_core/src/main/java/py/server.py";
						serverController.startServer(pythonInterpreter, scriptPath);*/

                        try {
                            PlotInterface.generatePlotBar(yCountValues, statisticValues, path);
                        } finally {
                            // 关闭服务器
                            //serverController.stopServer();
                        }

                        //写入数据库
                        History history = new History();
                        history.setUsrName(textField_name.getText());
                        history.setType("联邦查询");
                        history.setTime(new Date(time));
                        history.setStreamID_MPC(stream_mpc);
                        history.setStartTime(MyUtil.str2Date(rs[0]));
                        history.setEndTime(MyUtil.str2Date(rs[1]));
                        history.setUrl(path);
                        FrontEndSQL sql = new FrontEndSQL();
                        sql.InsertHistory_MPC(history);
                        showpicture(path);

                    }
                }

            }
        });
        printButton.setBounds(816, 306, 93, 23);
        panel_1.add(printButton);


        comboBox_type.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                String string = (String) comboBox_type.getSelectedItem();
                if (string.equals("其他")) {
                    textField_type.setEditable(true);
//                    JOptionPane.showMessageDialog(null, "请输入自定义流类型！");
//
                } else {
                    textField_type.setEditable(false);
                }
            }
        });


        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if(textField_type.isEditable()) {//这是查地定义类型流，如果没有选择“其他”不会走着
                    if(textField_type.getText().equals("")) {
                        JOptionPane.showMessageDialog(null, "请输入自定义流类型！");
                    }else {
                        String type = textField_type.getText();
                        FrontEndSQL sql =new FrontEndSQL();
                        policies=sql.getMpcPolicies(textField_name.getText(), type);

                        if(policies.size()==0){
                            JOptionPane.showMessageDialog(null, "没有相关数据！");
                        }
                        ArrayList<String> nameIds = new ArrayList<>();
                        for (MPCPolicy policy : policies) {
                            nameIds.add(sql.searchStream_Name_ID(policy.getStreamID()));
                        }
                        //着为啥没有输出 我试直接用流id再查了一次流的名字  然后流查出来了 这里按理应该由输出不是



                        for (int i = 0; i < policies.size(); i++) {
                            MPCPolicy policy = policies.get(i);
                            String nameId=nameIds.get(i);
                            System.out.println(nameId+"sddd");
                            model.addRow(new Object[]{false, policy.getOwnerName(), policy.getPolicyName() + "*" + policy.getPolicyId(), nameId, MyUtil.date2Str(policy.getStartTime()), MyUtil.date2Str(policy.getEndTime())});
                            model.fireTableDataChanged();
                        }

//
                    }
                }else {//大部分代码都走着
                    String type = (String) comboBox_type.getSelectedItem();
                    FrontEndSQL sql =new FrontEndSQL();
                    policies=sql.getMpcPolicies(textField_name.getText(), type);
                    if(policies.size()==0){
                        JOptionPane.showMessageDialog(null, "没有相关数据！");
                    }
                    model.setRowCount(0);
                    ArrayList<String> nameIds = new ArrayList<>();
                    for (MPCPolicy policy : policies) {
                        nameIds.add(sql.searchStream_Name_ID(policy.getStreamID()));
                    }
                    //着为啥没有输出 我试直接用流id再查了一次流的名字  然后流查出来了 这里按理应该由输出不是
                    for(String s :nameIds){
                        System.out.println(s);
                    }

//
                    for (int i = 0; i < policies.size(); i++) {
                        MPCPolicy policy = policies.get(i);
                        String nameId=nameIds.get(i);
                        System.out.println(nameId+"sddd");
                        model.addRow(new Object[]{false, policy.getOwnerName(), policy.getPolicyName() + "*" + policy.getPolicyId(), nameId, MyUtil.date2Str(policy.getStartTime()) ,MyUtil.date2Str(policy.getEndTime()) });
                        model.fireTableDataChanged();
                    }
//
                }
            }
        });


    }

    public static String[] findCommonTimeRange(List<MPCPolicy> policies) {
        String[] result = new String[2];
        if (policies == null || policies.isEmpty()) {
            return null;
        }
        List<Date> starttime = new ArrayList<Date>();
        List<Date> endtime = new ArrayList<Date>();
        for (MPCPolicy policy : policies) {
            starttime.add(policy.getStartTime());
            endtime.add(policy.getEndTime());
        }

        Date maxStartTime = Collections.max(starttime);
        Date minEndTime = Collections.min(endtime);

        if (maxStartTime.before(minEndTime)) {
            result[0] = MyUtil.date2Str(maxStartTime);
            result[1] = MyUtil.date2Str(minEndTime);
        } else {
            return null;
        }

        return result;
    }


    //显示图片
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
            ImageIcon icon = new ImageIcon(img);
            JLabel label = new JLabel(icon);
            JOptionPane.showMessageDialog(null, label, "图片预览", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "无法加载图片: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
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
}
