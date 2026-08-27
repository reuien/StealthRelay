package PanelPackage;

import Item.Equipment;
import Item.Stream;
import Util.FileUtil;
import Util.MyUtil;
import com.formdev.flatlaf.FlatLightLaf;
import frame.mainframe_owner;
import py.ServerController;
import sqlConnect.FrontEndSQL;
import usrs.DataOwnerClient;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.Timer;
import java.util.Map.Entry;


public class OwnerMainView extends JFrame {

    private static final long serialVersionUID = 1L;

    public static List<Stream> streamlist = new ArrayList<Stream>();
    public static Map<String, Stream> streammap = new HashMap<>();
    public static List<Equipment> equipments = new ArrayList<Equipment>();
    public static Map<String, Equipment> equipmentsmap = new HashMap<>();
    public static Stream testOb;

    private JFrame frame;
    private BasicPanel basicpanel;
    private EqPanel eqpanel;
    private DaoPanel daoPanel;
    private PolicyPanel policypanel;
    private DataManagePanel datamanagepanel;
    private DataOwnerSearchPanel datasearchPanelOwner;
    private JPanel timePanel;
    private JLabel timeLabel;
    private JLabel displayArea;
    private String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private String time;
    private int ONE_SECOND = 1000;
    private List<JPanel> panelist = new ArrayList<JPanel>();
    public static DataOwnerClient doc;


    private OwnerMainView ownermainview;

    private ServerController serverController;

    public OwnerMainView getOwnerMainView() {
        return ownermainview;
    }

    public void setRegidtrarMainView(OwnerMainView ownermainview) {
        this.ownermainview = ownermainview;
    }

    public OwnerMainView(String ID, ServerController serverController) throws Exception {
        this.serverController = serverController;
        initialize(ID);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 窗口关闭时释放资源
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                onClose();
            }
        });
    }

    private void onClose() {
        serverController.stopServer(); // 停止服务器
        frame.dispose(); // 释放 JFrame 资源
        System.exit(0); // 退出程序
        // 其他清理工作
    }

    private void listload_stream(String ID) {
        FileUtil<Stream> RegidtrarReader = new FileUtil<>();

        FrontEndSQL sql = new FrontEndSQL();
        if (sql.searchStream(ID).isEmpty()) {
            //system.out.println("该用户没有流设置");
        } else {
            streamlist = sql.searchStream(ID);
            streammap = RegidtrarReader.loadin_StreamPolicyMap(streamlist);
        }
    }


    private void initialize(String ID) throws Exception {
        //添加方法

        FlatLightLaf.setup();
        frame = new JFrame();
        frame = this;
        frame.setResizable(false);
        frame.setBackground(Color.WHITE);
        //读取
        listload_stream(ID);

        String usrName = getUsrName(ID);  // 用户名
        String keyStorePassword = "usrTestCryptPassword";  // 用户密钥库密码
        doc = new DataOwnerClient(ID, usrName, keyStorePassword);
        System.out.println("doc.getUsrName(): " + doc.getUsrName());
        System.out.println(doc.toString());

        daoPanel = new DaoPanel(ownermainview);
		daoPanel.setBackground(Color.WHITE);
        daoPanel.setVisible(true);
        daoPanel.setBounds(311, 2, 974, 755);
        getContentPane().add(daoPanel);


        this.setTitle("数据拥有者系统");
        this.setIconImage(new ImageIcon("traffic_access_core/src/main/java/imge/1.jpg").getImage());
        this.setBounds(100, 100, 1303, 799);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setLayout(null);
        this.getContentPane().setBackground(Color.WHITE);

        JSeparator separator = new JSeparator();
        separator.setBounds(22, 10, 268, 9);
        separator.setForeground(Color.GRAY);
        getContentPane().add(separator);

        JLabel label = new JLabel("账号：");
        label.setFont(new Font("微软雅黑", Font.BOLD, 20));
        label.setBounds(85, 29, 80, 35);
        getContentPane().add(label);

        JLabel label_1 = new JLabel("姓名：");
        label_1.setFont(new Font("微软雅黑", Font.BOLD, 20));
        label_1.setBounds(85, 74, 80, 35);
        getContentPane().add(label_1);

        JLabel label_count = new JLabel(ID);
        label_count.setFont(new Font("微软雅黑", Font.BOLD, 20));
        label_count.setBounds(163, 29, 151, 35);
        getContentPane().add(label_count);

        JLabel label_name = new JLabel(getUsrName(ID));
        label_name.setFont(new Font("微软雅黑", Font.BOLD, 20));
        label_name.setBounds(163, 74, 151, 35);
        getContentPane().add(label_name);

        JSeparator separator_1 = new JSeparator();
        separator_1.setBounds(22, 120, 268, 9);
        separator_1.setForeground(Color.GRAY);
        getContentPane().add(separator_1);
        this.setVisible(true);

        timePanel = new JPanel();
        timePanel.setBounds(49, 147, 222, 31);
        timeLabel = new JLabel("");
        timeLabel.setBackground(Color.WHITE);
        timeLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        displayArea = new JLabel();
        configTimeArea();
        timePanel.add(timeLabel);
        timePanel.add(displayArea);
        getContentPane().add(timePanel);


        JLabel label_2 = new JLabel("菜单选择：");
        label_2.setFont(new Font("微软雅黑", Font.BOLD, 20));
        label_2.setBounds(21, 199, 104, 35);
        getContentPane().add(label_2);


        JButton bt_logout = new JButton("退出登录");

        bt_logout.setBackground(new Color(255, 96, 96));
        bt_logout.setForeground(Color.WHITE);
        bt_logout.setIcon(MyUtil.setIcon("traffic_access_core/src/main/java/imge/退出.png"));

        bt_logout.setFont(new Font("微软雅黑", Font.BOLD, 14));
        bt_logout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            @SuppressWarnings("unused")
                            mainframe_owner window = new mainframe_owner();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                frame.dispose();
            }
        });
        bt_logout.setBounds(22, 719, 133, 35);
        getContentPane().add(bt_logout);
        JButton Default_Button = new JButton("导航主页");
        Default_Button.setIcon(MyUtil.setIcon("traffic_access_core/src/main/java/imge/导航.png"));
        Default_Button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        Default_Button.setBounds(22, 245, 133, 35);
        getContentPane().add(Default_Button);
        Default_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                daoPanel.setVisible(true);
                closepanel_dao(panelist);


            }


        });


        JButton EqManage_Button = new JButton("设备管理");
        EqManage_Button.setIcon(MyUtil.setIcon("traffic_access_core/src/main/java/imge/设备管理.png"));
        EqManage_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FrontEndSQL sqlsearch = new FrontEndSQL();
                equipments = sqlsearch.getEqResults(ID);
                daoPanel.setVisible(false);


                if (eqpanel != null) {
                    frame.remove(eqpanel);
                    try {
                        eqpanel = new EqPanel(ownermainview);
                        panelist.add(eqpanel);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else {
                    try {
                        eqpanel = new EqPanel(ownermainview);
                        panelist.add(eqpanel);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }

                closepanel(panelist, eqpanel);
                eqpanel.setBorder(new LineBorder(Color.white, 1));
                eqpanel.setBounds(311, 2, 974, 755);
                getContentPane().add(eqpanel);

                eqpanel.setVisible(true);
                eqpanel.setEnabled(true);

                eqpanel.id_textField.setText(ID);

            }
        });
        EqManage_Button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        EqManage_Button.setBounds(22, 290, 133, 35);
        getContentPane().add(EqManage_Button);

        JButton basic_Button = new JButton("基本设置");
        basic_Button.setIcon(MyUtil.setIcon("traffic_access_core/src/main/java/imge/设置.png"));
        basic_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                daoPanel.setVisible(false);
                FrontEndSQL sqlsearch = new FrontEndSQL();
                equipments = sqlsearch.getEqResults(ID);
                if (basicpanel != null) {
                    frame.remove(basicpanel);
                    try {
                        basicpanel = new BasicPanel(ownermainview);
                        panelist.add(basicpanel);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else {
                    try {
                        basicpanel = new BasicPanel(ownermainview);
                        panelist.add(basicpanel);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                closepanel(panelist, basicpanel);
                basicpanel.setBorder(new LineBorder(Color.white, 1));
                basicpanel.setBounds(311, 2, 974, 755);
                getContentPane().add(basicpanel);

                basicpanel.setVisible(true);
                basicpanel.setEnabled(true);
                basicpanel.id_textField.setText(ID);
            }
        });
        basic_Button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        basic_Button.setBounds(160, 245, 133, 35);
        getContentPane().add(basic_Button);

        JButton StreamMannage_Button = new JButton("信息管理");
        StreamMannage_Button.setIcon(MyUtil.setIcon("traffic_access_core/src/main/java/imge/数据管理.png"));
        StreamMannage_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                daoPanel.setVisible(false);

                if (datamanagepanel != null) {
                    frame.remove(datamanagepanel);
                    try {
                        datamanagepanel = new DataManagePanel(ownermainview);
                        panelist.add(datamanagepanel);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else {
                    try {
                        datamanagepanel = new DataManagePanel(ownermainview);
                        panelist.add(datamanagepanel);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                closepanel(panelist, datamanagepanel);
                datamanagepanel.setBorder(new LineBorder(Color.white, 1));
                datamanagepanel.setBounds(311, 2, 974, 755);
                getContentPane().add(datamanagepanel);
                datamanagepanel.setVisible(true);
                datamanagepanel.setEnabled(true);


                listload_stream(ID);
                datamanagepanel.comboBox.removeAll();
                datamanagepanel.comboBox.addItem("请选择");

                FileUtil<Stream> RegidtrarReader = new FileUtil<>();
                FrontEndSQL sql = new FrontEndSQL();
                if (sql.searchStream(ID).isEmpty()) {
                    System.out.println("该用户没有流设置");
                } else {
                    streamlist = sql.searchStream(ID);
                    streammap = RegidtrarReader.loadin_StreamPolicyMap(streamlist);
                    for (Entry<String, Stream> entry : streammap.entrySet()) {
                        System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue().toString());
                    }
                    for (Stream eq : streamlist) {
                        datamanagepanel.comboBox.addItem(eq.getName() + "*" + Long.toString(eq.getId()));
                    }
                }


                datamanagepanel.id_textField.setText(ID);

            }
        });
        StreamMannage_Button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        StreamMannage_Button.setBounds(160, 290, 133, 35);
        getContentPane().add(StreamMannage_Button);

        JButton Policy_Button = new JButton("隐私策略");
        Policy_Button.setIcon(MyUtil.setIcon("traffic_access_core/src/main/java/imge/策略.png"));
        Policy_Button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        Policy_Button.setBounds(22, 335, 133, 35);
        Policy_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                daoPanel.setVisible(false);

                if (policypanel != null) {
                    frame.remove(policypanel);
                    try {
                        policypanel = new PolicyPanel(ownermainview);
                        panelist.add(policypanel);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else {
                    try {
                        policypanel = new PolicyPanel(ownermainview);
                        panelist.add(policypanel);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                closepanel(panelist, policypanel);
                policypanel.setBorder(new LineBorder(Color.white, 1));
                policypanel.setBounds(311, 2, 974, 755);
                getContentPane().add(policypanel);

                listload_stream(ID);

                policypanel.setVisible(true);
                policypanel.setEnabled(true);

                FileUtil<Stream> RegidtrarReader = new FileUtil<>();
                FrontEndSQL sql = new FrontEndSQL();
                if (sql.searchStream(ID).isEmpty()) {
                    System.out.println("该用户没有流设置");
                } else {
                    streamlist = sql.searchStream(ID);
                    streammap = RegidtrarReader.loadin_StreamPolicyMap(streamlist);
                }
                policypanel.comboBox.removeAll();

                policypanel.comboBox.addItem("请选择");
                for (Stream eq : streamlist) {
                    policypanel.comboBox.addItem(eq.getName() + "*" + Long.toString(eq.getId()));

                }


                policypanel.id_textField.setText(ID);
            }
        });

        getContentPane().add(Policy_Button);

        JButton Notice_Button = new JButton("通知文件");
        Notice_Button.setIcon(MyUtil.setIcon("traffic_access_core/src/main/java/imge/通知.png"));
        Notice_Button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        Notice_Button.setBounds(22, 380, 133, 35);
        getContentPane().add(Notice_Button);

        JButton More_Button = new JButton("更多功能");
        More_Button.setIcon(MyUtil.setIcon("traffic_access_core/src/main/java/imge/更多功能.png"));
        More_Button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        More_Button.setBounds(160, 380, 133, 35);
        getContentPane().add(More_Button);

        JButton Search_Button = new JButton("数据查询");
        Search_Button.setIcon(MyUtil.setIcon("traffic_access_core/src/main/java/imge/高级查询.png"));
        Search_Button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        Search_Button.setBounds(160, 335, 133, 35);
        Search_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                daoPanel.setVisible(false);

                if (datasearchPanelOwner != null) {
                    frame.remove(datasearchPanelOwner);
                    try {
                        datasearchPanelOwner = new DataOwnerSearchPanel(ownermainview);
                        panelist.add(datasearchPanelOwner);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else {
                    try {
                        datasearchPanelOwner = new DataOwnerSearchPanel(ownermainview);
                        panelist.add(datasearchPanelOwner);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }

                closepanel(panelist, datasearchPanelOwner);
                datasearchPanelOwner.setBorder(new LineBorder(Color.white, 1));
                datasearchPanelOwner.setBounds(311, 2, 974, 755);
                getContentPane().add(datasearchPanelOwner);

                listload_stream(ID);


                datasearchPanelOwner.setVisible(true);
                datasearchPanelOwner.setEnabled(true);

                datasearchPanelOwner.comboBox.removeAll();
                FileUtil<Stream> RegidtrarReader = new FileUtil<>();
                FrontEndSQL sql = new FrontEndSQL();
                if (sql.searchStream(ID).isEmpty()) {
                    System.out.println("该用户没有流设置");
                } else {
                    streamlist = sql.searchStream(ID);
                    streammap = RegidtrarReader.loadin_StreamPolicyMap(streamlist);
                }
                datasearchPanelOwner.comboBox.addItem("请选择");
                for (Stream eq : streamlist) {

                    datasearchPanelOwner.comboBox.addItem(eq.getName() + "*" + Long.toString(eq.getId()));

                }
                datasearchPanelOwner.id_textField.setText(ID);


            }
        });
        getContentPane().add(Search_Button);


    }

    /**
     * Timer task 更新时间显示区
     */

    protected class JLabelTimerTask extends TimerTask {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(
                DEFAULT_TIME_FORMAT);

        @Override

        public void run() {

            time = dateFormatter.format(Calendar.getInstance().getTime());
            displayArea.setText(time);
        }
    }

    private static boolean isPanelInContainer(Container container, JPanel panel) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            if (component == panel) {
                return true;
            }
        }
        return false;
    }

    private void configTimeArea() {
        Timer tmr = new Timer();
        tmr.scheduleAtFixedRate(new JLabelTimerTask(), new Date(), ONE_SECOND);

        Font font = new Font("微软雅黑", Font.BOLD, 15); // 创建一个新字体，可以根据需求自定义字体样式
        displayArea.setFont(font); // 设置时间显示区域的字体
    }

    private void closepanel(List<JPanel> panelist, JPanel p) {
        for (JPanel panel : panelist) {
            if (panel == p) {
                continue; // 跳过处理含有p的面板
            }
            if (isPanelInContainer(frame.getContentPane(), panel)) {
                panel.setVisible(false);
                panel.setEnabled(false);
                frame.remove(panel);

            }
        }
    }

    private void closepanel_dao(List<JPanel> panelist) {
        for (JPanel panel : panelist) {

            if (isPanelInContainer(frame.getContentPane(), panel)) {


                panel.setVisible(false);
                panel.setEnabled(false);
                frame.remove(panel);

            }
        }
    }

    private String getUsrName(String id) {
        FrontEndSQL sql = new FrontEndSQL();
        String name = sql.searchName(id);
        return name;
    }


}
