package PanelPackage;

import Item.PrivacyPolicy;
import Item.Stream;
import Util.FileUtil;
import Util.MyUtil;
import exceptions.CouldNotStoreException;
import sqlConnect.FrontEndSQL;
import usrs.DataOwnerClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManagePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public static List<Stream> streamlist = OwnerMainView.streamlist;
    public static Map<String, Stream> streammap = OwnerMainView.streammap;
    public JComboBox<String> comboBox = new JComboBox<String>();
    public JTextField id_textField;
    public static Map<Long, List<Long>> Idmap = new HashMap<>();
    public static Map<Long, PrivacyPolicy> policymap = new HashMap<>();
    public static PrivacyPolicy policy = new PrivacyPolicy();
    public static List<PrivacyPolicy> policylist1;
    public static DataOwnerClient doc;

    private JTextField textField_2;
    private JTextField textField_3;
    private JTextField textField_4;
    private JTextField textField_5;
    private JTextField textField_6;
    private JTextField textField_7;
    private JTextField textField;
    private JTextField textField_1;
    private JTextField textField_8;
    private JTextField textField_9;
    private JTextField policyid_textField;
    private JTextField streamid_textField;
    private JTextField textField_10;
    private JTextField textField_11;
    private JTextField textField_12;
    private JTextField mpcpolicyid_textField;
    private JTextField pname;
    private JTextField mpcname;

    /**
     * Create the panel.
     */
    public DataManagePanel(OwnerMainView ownermainview) throws Exception {
        this.setBackground(Color.WHITE);
        setLayout(null);
        System.out.println("数据管理");

        doc = ownermainview.doc;

        JLabel lblNewLabel = new JLabel("信息管理");
        lblNewLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel.setBounds(10, 22, 435, 30);
        add(lblNewLabel);

        JSeparator separator = new JSeparator();
        separator.setBounds(10, 64, 955, 12);
        separator.setForeground(Color.GRAY);
        add(separator);

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBounds(0, 77, 953, 182);

        panel.setLayout(null);
        add(panel);

        comboBox.setBounds(88, 23, 241, 23);
        panel.add(comboBox);

        textField_2 = new JTextField();
        textField_2.setEditable(false);
        textField_2.setColumns(10);
        textField_2.setBounds(729, 24, 88, 21);
        panel.add(textField_2);

        JLabel lblNewLabel_1_1_1 = new JLabel("流类型：");
        lblNewLabel_1_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_1.setBounds(643, 21, 76, 23);
        panel.add(lblNewLabel_1_1_1);

        textField_3 = new JTextField();
        textField_3.setEditable(false);
        textField_3.setColumns(10);
        textField_3.setBounds(488, 24, 111, 21);
        panel.add(textField_3);

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
        lblNewLabel_1_3.setBounds(30, 85, 140, 23);
        panel.add(lblNewLabel_1_3);

        textField_4 = new JTextField();
        textField_4.setEditable(false);
        textField_4.setColumns(10);
        textField_4.setBounds(168, 88, 111, 21);
        panel.add(textField_4);

        JLabel lblNewLabel_1_3_1 = new JLabel("设置的更高粒度：");
        lblNewLabel_1_3_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_3_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_3_1.setBounds(358, 85, 140, 23);
        panel.add(lblNewLabel_1_3_1);

        textField_5 = new JTextField();
        textField_5.setEditable(false);
        textField_5.setColumns(10);
        textField_5.setBounds(495, 88, 111, 21);
        panel.add(textField_5);

        JLabel lblNewLabel_1_3_2 = new JLabel("设置的开始时间：");
        lblNewLabel_1_3_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_3_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_3_2.setBounds(30, 126, 140, 23);
        panel.add(lblNewLabel_1_3_2);

        JLabel lblNewLabel_1_3_1_1 = new JLabel("设置的结束时间：");
        lblNewLabel_1_3_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_3_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_3_1_1.setBounds(358, 126, 140, 23);
        panel.add(lblNewLabel_1_3_1_1);

        textField_6 = new JTextField();
        textField_6.setEditable(false);
        textField_6.setColumns(10);
        textField_6.setBounds(168, 129, 191, 21);
        panel.add(textField_6);

        textField_7 = new JTextField();
        textField_7.setEditable(false);
        textField_7.setColumns(10);
        textField_7.setBounds(495, 129, 207, 21);
        panel.add(textField_7);

        JButton d1_Button = new JButton("删 除");
        d1_Button.setBackground(new Color(255, 96, 96));
        d1_Button.setForeground(Color.WHITE);
        d1_Button.setBounds(778, 128, 93, 23);
        panel.add(d1_Button);

        JSeparator separator_1 = new JSeparator();
        separator_1.setForeground(Color.GRAY);
        separator_1.setBounds(20, 63, 911, 12);
        panel.add(separator_1);

        streamid_textField = new JTextField();
        streamid_textField.setBounds(785, 92, 66, 21);
        panel.add(streamid_textField);
        streamid_textField.setColumns(10);
        streamid_textField.setVisible(false);

        JPanel panel_1 = new JPanel();
        panel_1.setLayout(null);
        panel_1.setBounds(0, 279, 955, 218);
        add(panel_1);


        JPanel panel_2 = new JPanel();
        panel_2.setLayout(null);
        panel_2.setBounds(0, 519, 955, 218);
        add(panel_2);


        JLabel lblNewLabel_1_2_1_1 = new JLabel("联邦策略");
        lblNewLabel_1_2_1_1.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1_2_1_1.setBounds(0, 0, panel_2.getWidth(), 50);
        lblNewLabel_1_2_1_1.setOpaque(true);
        lblNewLabel_1_2_1_1.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel_1_2_1_1.setBackground(new Color(42, 115, 197));
        lblNewLabel_1_2_1_1.setForeground(Color.WHITE);
        panel_2.add(lblNewLabel_1_2_1_1);

        textField_10 = new JTextField();
        textField_10.setEditable(false);
        textField_10.setColumns(10);
        textField_10.setBounds(555, 160, 161, 21);
        panel_2.add(textField_10);

        JLabel lblNewLabel_1_1_2_1_1_2 = new JLabel("允许访问的结束时间：");
        lblNewLabel_1_1_2_1_1_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_1_1_2.setBounds(400, 157, 158, 23);
        panel_2.add(lblNewLabel_1_1_2_1_1_2);

        textField_11 = new JTextField();
        textField_11.setEditable(false);
        textField_11.setColumns(10);
        textField_11.setBounds(186, 160, 161, 21);
        panel_2.add(textField_11);

        JLabel lblNewLabel_1_1_2_1_3 = new JLabel("允许访问的开始时间：");
        lblNewLabel_1_1_2_1_3.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_1_3.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_1_3.setBounds(20, 157, 158, 23);
        panel_2.add(lblNewLabel_1_1_2_1_3);

        textField_12 = new JTextField();
        textField_12.setEditable(false);
        textField_12.setColumns(10);
        textField_12.setBounds(188, 116, 111, 21);
        panel_2.add(textField_12);

        JLabel lblNewLabel_1_1_2_1_2_1 = new JLabel("允许访问的消费者：");
        lblNewLabel_1_1_2_1_2_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_1_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_1_2_1.setBounds(20, 113, 158, 23);
        panel_2.add(lblNewLabel_1_1_2_1_2_1);

        JComboBox<String> comboBox_2 = new JComboBox<String>();
        comboBox_2.setEnabled(false);
        comboBox_2.setBounds(116, 69, 241, 23);
        panel_2.add(comboBox_2);

        JLabel lblNewLabel_1_2_2 = new JLabel("选择策略：");
        lblNewLabel_1_2_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_2_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_2_2.setBounds(30, 67, 76, 23);
        panel_2.add(lblNewLabel_1_2_2);

        JButton d3_Button = new JButton("删 除");
        d3_Button.setBackground(new Color(255, 96, 96));
        d3_Button.setForeground(Color.WHITE);
        d3_Button.setBounds(778, 159, 93, 23);
        panel_2.add(d3_Button);

        mpcpolicyid_textField = new JTextField();
        mpcpolicyid_textField.setBounds(384, 70, 66, 21);
        panel_2.add(mpcpolicyid_textField);
        mpcpolicyid_textField.setColumns(10);
        mpcpolicyid_textField.setVisible(false);


        JComboBox<String> comboBox_1 = new JComboBox<String>();
        comboBox_1.setBounds(117, 69, 241, 23);
        panel_1.add(comboBox_1);
        comboBox_1.setEnabled(false);

        JLabel lblNewLabel_1_2 = new JLabel("选择策略：");
        lblNewLabel_1_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_2.setBounds(31, 67, 76, 23);
        panel_1.add(lblNewLabel_1_2);

        JLabel lblNewLabel_1_1_2_1 = new JLabel("允许访问的开始时间：");
        lblNewLabel_1_1_2_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_1.setBounds(21, 157, 158, 23);
        panel_1.add(lblNewLabel_1_1_2_1);

        JLabel lblNewLabel_1_1_2_1_1 = new JLabel("允许访问的结束时间：");
        lblNewLabel_1_1_2_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_1_1.setBounds(401, 157, 158, 23);
        panel_1.add(lblNewLabel_1_1_2_1_1);

        JLabel lblNewLabel_1_1_2_1_1_1 = new JLabel("允许访问的最小粒度倍数：");
        lblNewLabel_1_1_2_1_1_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_1_1_1.setBounds(365, 113, 189, 23);
        panel_1.add(lblNewLabel_1_1_2_1_1_1);


        JLabel lblNewLabel_1_1_2_1_2 = new JLabel("允许访问的消费者：");
        lblNewLabel_1_1_2_1_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_1_2.setBounds(21, 113, 158, 23);
        panel_1.add(lblNewLabel_1_1_2_1_2);

        JLabel lblNewLabel_1_1_2 = new JLabel("策略名称：");
        lblNewLabel_1_1_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2.setBounds(401, 68, 76, 23);
        panel_1.add(lblNewLabel_1_1_2);

        pname = new JTextField();
        pname.setEditable(false);
        pname.setColumns(10);
        pname.setBounds(487, 71, 111, 21);
        panel_1.add(pname);

        JLabel lblNewLabel_1_1_2_2 = new JLabel("策略名称：");
        lblNewLabel_1_1_2_2.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_1_1_2_2.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_1_1_2_2.setBounds(400, 68, 76, 23);
        panel_2.add(lblNewLabel_1_1_2_2);

        mpcname = new JTextField();
        mpcname.setEditable(false);
        mpcname.setColumns(10);
        mpcname.setBounds(486, 71, 111, 21);
        panel_2.add(mpcname);


        textField = new JTextField();
        textField.setEditable(false);
        textField.setColumns(10);
        textField.setBounds(189, 116, 111, 21);
        panel_1.add(textField);

        textField_1 = new JTextField();
        textField_1.setEditable(false);
        textField_1.setColumns(10);
        textField_1.setBounds(187, 160, 161, 21);
        panel_1.add(textField_1);

        textField_8 = new JTextField();
        textField_8.setEditable(false);
        textField_8.setColumns(10);
        textField_8.setBounds(556, 160, 161, 21);
        panel_1.add(textField_8);

        textField_9 = new JTextField();
        textField_9.setEditable(false);
        textField_9.setColumns(10);
        textField_9.setBounds(556, 116, 111, 21);
        panel_1.add(textField_9);

        JButton d2_Button = new JButton("删 除");
        d2_Button.setBackground(new Color(255, 96, 96));
        d2_Button.setForeground(Color.WHITE);
        d2_Button.setBounds(778, 159, 93, 23);
        panel_1.add(d2_Button);

        policyid_textField = new JTextField();
        policyid_textField.setBounds(398, 70, 28, 21);
        panel_1.add(policyid_textField);
        policyid_textField.setColumns(10);

        JLabel lblNewLabel_1_2_1 = new JLabel("单流策略");
        lblNewLabel_1_2_1.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1_2_1.setBounds(0, 0, panel_1.getWidth(), 50);
        lblNewLabel_1_2_1.setOpaque(true);
        lblNewLabel_1_2_1.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel_1_2_1.setBackground(new Color(42, 115, 197));
        lblNewLabel_1_2_1.setForeground(Color.WHITE);
        panel_1.add(lblNewLabel_1_2_1);
        policyid_textField.setVisible(false);

        id_textField = new JTextField();
        id_textField.setBounds(129, 30, 34, 21);
        add(id_textField);
        id_textField.setColumns(10);
        id_textField.setVisible(false);

        comboBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    streamlist = OwnerMainView.streamlist;
                    streammap = OwnerMainView.streammap;
                    String selected = (String) comboBox.getSelectedItem();
                    if (selected.equals("请选择")) {
                        textField.setText("");
                        textField_1.setText("");
                        textField_2.setText("");
                        textField_3.setText("");
                        textField_4.setText("");
                        textField_5.setText("");
                        textField_6.setText("");
                        textField_7.setText("");
                        textField_8.setText("");
                        textField_9.setText("");
                        textField_10.setText("");
                        textField_11.setText("");
                        textField_12.setText("");
                        pname.setText("");
                        mpcname.setText("");
                        String[] s_array = new String[1];
                        s_array[0] = "请选择";
                        comboBox_1.setModel(new DefaultComboBoxModel<>(s_array));
                        comboBox_1.setEnabled(false);
                        comboBox_2.setModel(new DefaultComboBoxModel<>(s_array));
                        comboBox_2.setEnabled(false);

                    } else {
//							 for (Entry<String, Stream> entry : streammap.entrySet()) {
//						            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue().toString());
//						        }
                        selected = (String) comboBox.getSelectedItem();
                        System.out.println(selected);
                        textField_3.setText(streammap.get(selected).getName());
                        textField_2.setText(streammap.get(selected).getDesciption());
                        textField_6.setText(MyUtil.date2Str(streammap.get(selected).getStarttime()));
                        textField_7.setText(MyUtil.date2Str(streammap.get(selected).getEndtime()));
                        textField_4.setText(Long.toString(streammap.get(selected).getMingranularity()));
                        textField_5.setText(Long.toString(streammap.get(selected).getGranularity()));
                        streamid_textField.setText(Long.toString(streammap.get(selected).getId()));


                        comboBox_1.setEnabled(true);
                        FrontEndSQL sql = new FrontEndSQL();
                        List<String> pidList = sql.getSelect_plist(Long.parseLong(streamid_textField.getText()));
                        if (pidList.isEmpty()) {
                            String[] array = {"无策略"};
                            comboBox_1.setModel(new DefaultComboBoxModel<>(array));
                            textField.setText("");
                            textField_9.setText("");
                            textField_1.setText("");
                            textField_8.setText("");
                            pname.setText("");
                        } else {
                            String[] array1 = {"请选择"};
                            String[] array2 = pidList.toArray(new String[pidList.size()]);
                            String[] mergedArray = new String[array1.length + array2.length];
                            System.arraycopy(array1, 0, mergedArray, 0, array1.length);
                            System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);
                            comboBox_1.setModel(new DefaultComboBoxModel<>(mergedArray));
                        }

                        comboBox_2.setEnabled(true);
                        List<String> mpcidList = sql.getSelect_mpclist(Long.parseLong(streamid_textField.getText()));
                        if (mpcidList.isEmpty()) {
                            String[] array = {"无策略"};
                            comboBox_2.setModel(new DefaultComboBoxModel<>(array));
                            textField_10.setText("");
                            textField_11.setText("");
                            textField_12.setText("");
                            mpcname.setText("");
                        } else {
                            String[] array2 = mpcidList.toArray(new String[mpcidList.size()]);
                            String[] array1 = {"请选择"};
                            String[] mergedArray = new String[array1.length + array2.length];
                            System.arraycopy(array1, 0, mergedArray, 0, array1.length);
                            System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);
                            comboBox_2.setModel(new DefaultComboBoxModel<>(mergedArray));
                        }

                    }
                }
            }
        });

        comboBox_1.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                String string = (String) comboBox_1.getSelectedItem();
                if (string.equals("请选择")) {
                    textField.setText("");
                    textField_9.setText("");
                    textField_1.setText("");
                    textField_8.setText("");
                    policyid_textField.setText("");
                    pname.setText("");
                } else {
                    String[] parts = ((String) comboBox_1.getSelectedItem()).split("\\+"); // 使用正则表达式分割字符串
                    policyid_textField.setText(parts[1]);
                    String s = policyid_textField.getText();
                    String[] p = getp(s);
                    if (p.length == 0) {
                        textField.setText("");
                        textField_9.setText("");
                        textField_1.setText("");
                        textField_8.setText("");
                        pname.setText("");
                    } else {
                        textField.setText(p[0]);
                        textField_9.setText(p[3]);
                        textField_1.setText(p[1]);
                        textField_8.setText(p[2]);
                        pname.setText(p[4]);
                    }
                }
            }
        });

        comboBox_2.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                String string = (String) comboBox_2.getSelectedItem();
                if (string.equals("请选择")) {
                    textField_10.setText("");
                    textField_11.setText("");
                    textField_12.setText("");
                    mpcpolicyid_textField.setText("");
                    mpcname.setText("");
                } else {
                    String[] parts = ((String) comboBox_2.getSelectedItem()).split("\\+"); // 使用正则表达式分割字符串
                    mpcpolicyid_textField.setText(parts[1]);
                    String s = mpcpolicyid_textField.getText();
                    FrontEndSQL sql = new FrontEndSQL();
                    String[] p = sql.getSelected_mpc(Long.parseLong(s));
                    if (p.length == 0) {
                        textField_10.setText("");
                        textField_11.setText("");
                        textField_12.setText("");
                        mpcname.setText("");
                    } else {
                        textField_10.setText(p[2]);
                        textField_11.setText(p[1]);
                        textField_12.setText(p[0]);
                        mpcname.setText(p[3]);
                    }
                }
            }
        });

        d1_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (streamid_textField.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "请选择流！");
                } else {
                    SwingUtilities.invokeLater(() -> {
                        int result = JOptionPane.showConfirmDialog(null, "<html>该操作会删除流以及该流所有的隐私策略</html>确定要删除吗?", "删除确认", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            // 用户点击了"是"按钮，执行删除操作
                            System.out.println("用户点击了'是'按钮，执行删除操作");

                            //删除流需要添加的操作（删除流，单流策略，联邦策略）
                            try {
                                doc.deleteStream(Long.parseLong(streamid_textField.getText()));
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            } catch (CouldNotStoreException ex) {
                                throw new RuntimeException(ex);
                            }
                            //删除流需要添加的操作

                            FrontEndSQL sql0 = new FrontEndSQL();
                            try {
                                sql0.deleteStream(Long.parseLong(streamid_textField.getText()));
                            } catch (NumberFormatException e1) {
                                e1.printStackTrace();
                            } catch (SQLException e1) {
                                System.out.println("数据库操作出错");
                                e1.printStackTrace();
                            }

                            //刷新页面所有信息
                            FileUtil<Stream> RegidtrarReader = new FileUtil<>();
                            FrontEndSQL sql1 = new FrontEndSQL();
                            if (sql1.searchStream(id_textField.getText()).isEmpty()) {
                                String[] array = {"请选择"};
                                comboBox.setModel(new DefaultComboBoxModel<>(array));
                                //comboBox.addItem("请选择");
                                textField.setText("");
                                textField_1.setText("");
                                textField_2.setText("");
                                textField_3.setText("");
                                textField_4.setText("");
                                textField_5.setText("");
                                textField_6.setText("");
                                textField_7.setText("");
                                textField_8.setText("");
                                textField_9.setText("");
                                textField_10.setText("");
                                textField_11.setText("");
                                textField_12.setText("");
                                pname.setText("");
                                mpcname.setText("");
                                String[] array1 = {"无策略"};
                                comboBox_1.setModel(new DefaultComboBoxModel<>(array1));
                                //comboBox.addItem("请选择");
                                comboBox_1.setEnabled(false);
                                System.out.println("该用户没有流设置");
                            } else {
                                textField.setText("");
                                textField_1.setText("");
                                textField_2.setText("");
                                textField_3.setText("");
                                textField_4.setText("");
                                textField_5.setText("");
                                textField_6.setText("");
                                textField_7.setText("");
                                textField_8.setText("");
                                textField_9.setText("");
                                textField_10.setText("");
                                textField_11.setText("");
                                textField_12.setText("");
                                pname.setText("");
                                mpcname.setText("");
                                List<Stream> streamlist = new ArrayList<Stream>();
                                streamlist = sql1.searchStream(id_textField.getText());
                                comboBox.removeAllItems();
                                String[] array1 = {"请选择"};
                                String[] array2 = new String[streamlist.size()];

                                for (int i = 0; i < streamlist.size(); i++) {
                                    array2[i] = streamlist.get(i).getName() + "*" + Long.toString(streamlist.get(i).getId());
                                }

                                String[] mergedArray = new String[array1.length + array2.length];
                                System.arraycopy(array1, 0, mergedArray, 0, array1.length);
                                System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);
                                comboBox.setModel(new DefaultComboBoxModel<>(mergedArray));
                            }
                        } else {
                            // 用户点击了"否"按钮，取消删除操作
                            System.out.println("用户点击了'否'按钮，取消删除操作");
                        }
                    });
                }
            }
        });

        d2_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (policyid_textField.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "请选择策略！");
                } else {
                    SwingUtilities.invokeLater(() -> {
                        int result = JOptionPane.showConfirmDialog(null, "<html>该操作会删除此条隐私策略</html>确定要删除吗?", "删除确认", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            // 用户点击了"是"按钮，执行删除操作
                            System.out.println("用户点击了'是'按钮，执行删除操作");
                            FrontEndSQL sql0 = new FrontEndSQL();
                            sql0.deletePolicy(Long.parseLong(policyid_textField.getText()));

                            //刷新策略选择框
                            FrontEndSQL sql1 = new FrontEndSQL();
                            List<String> pidList = sql1.getSelect_plist(Long.parseLong(streamid_textField.getText()));
                            if (pidList.isEmpty()) {
                                String[] array = {"无策略"};
                                comboBox_1.setModel(new DefaultComboBoxModel<>(array));
                                textField.setText("");
                                textField_9.setText("");
                                textField_1.setText("");
                                textField_8.setText("");
                                pname.setText("");
                            } else {
                                String[] array1 = {"请选择"};
                                String[] array2 = pidList.toArray(new String[pidList.size()]);
                                String[] mergedArray = new String[array1.length + array2.length];
                                System.arraycopy(array1, 0, mergedArray, 0, array1.length);
                                System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);
                                comboBox_1.setModel(new DefaultComboBoxModel<>(mergedArray));
                                textField.setText("");
                                textField_9.setText("");
                                textField_1.setText("");
                                textField_8.setText("");
                                pname.setText("");
                            }

                            //删除单流策略需要添加的操作------加在下面


                            //删除策略需要添加的操作------康硕

                        } else {
                            // 用户点击了"否"按钮，取消删除操作
                            System.out.println("用户点击了'否'按钮，取消删除操作");
                        }
                    });
                }
            }
        });


        d3_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (mpcpolicyid_textField.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "请选择策略！");
                } else {
                    SwingUtilities.invokeLater(() -> {
                        int result = JOptionPane.showConfirmDialog(null, "<html>该操作会删除此条隐私策略</html>确定要删除吗?", "删除确认", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            // 用户点击了"是"按钮，执行删除操作
                            System.out.println("用户点击了'是'按钮，执行删除操作");
                            FrontEndSQL sql0 = new FrontEndSQL();

                            sql0.deleteMpcPolicy(Long.parseLong(mpcpolicyid_textField.getText()));

                            //刷新策略选择框
                            FrontEndSQL sql1 = new FrontEndSQL();
                            List<String> mpcidList = sql1.getSelect_mpclist(Long.parseLong(streamid_textField.getText()));
                            if (mpcidList.isEmpty()) {
                                String[] array = {"无策略"};
                                comboBox_1.setModel(new DefaultComboBoxModel<>(array));
                                textField_10.setText("");
                                textField_11.setText("");
                                textField_12.setText("");
                                mpcname.setText("");
                            } else {
                                String[] array1 = {"请选择"};
                                String[] array2 = mpcidList.toArray(new String[mpcidList.size()]);
                                String[] mergedArray = new String[array1.length + array2.length];
                                System.arraycopy(array1, 0, mergedArray, 0, array1.length);
                                System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);
                                comboBox_2.setModel(new DefaultComboBoxModel<>(mergedArray));
                                textField_10.setText("");
                                textField_11.setText("");
                                textField_12.setText("");
                                mpcname.setText("");
                            }

                            //删除联邦策略策略需要添加的操作------加在下面


                            //删除策略需要添加的操作------康硕

                        } else {
                            // 用户点击了"否"按钮，取消删除操作
                            System.out.println("用户点击了'否'按钮，取消删除操作");
                        }
                    });
                }
            }

        });

    }

    private String[] getp(String id) {
        FrontEndSQL sql = new FrontEndSQL();
        String[] name = sql.getSelected_p(Long.parseLong(id));
        return name;
    }

    /*Long数组转换成String数组*/
    private String[] LongArrayToStringArray(Long[] longArray) {
        String[] stringArray = new String[longArray.length + 1];
        String Default = "请选择";
        stringArray[0] = Default;
        // 遍历Long数组并将每个Long元素转换为String类型
        for (int i = 1; i < longArray.length + 1; i++) {
            stringArray[i] = String.valueOf(longArray[i - 1]);
        }
        return stringArray;
    }
}
