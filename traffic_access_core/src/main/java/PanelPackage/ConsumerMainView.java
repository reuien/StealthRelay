package PanelPackage;

import Item.PrivacyPolicy;
import Item.Stream;
import Util.MyUtil;
import com.formdev.flatlaf.FlatLightLaf;
import frame.mainframe_consumer;
import py.ServerController;
import sqlConnect.FrontEndSQL;
import usrs.DataConsumer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.*;

public class ConsumerMainView extends JFrame {

    private static final long serialVersionUID = 1L;

    public static List<Stream> streamlist = new ArrayList<Stream>();
    public static Map<String, Stream> streammap = new HashMap<>();
    public static List<PrivacyPolicy> polivylist = new ArrayList<PrivacyPolicy>();
    public static Map<Long, List<PrivacyPolicy>> Cpmap = new HashMap<>();
    public static Stream testOb;

    private JFrame frame;
    private DataConsumerQueryPanel dataquerypanel;
    private DaoPanel1 daopanel1;
    private HistoryPanel historypanel;
    private MPCPanel mpcpanel;
    private JPanel timePanel;
    private JLabel timeLabel;
    private JLabel displayArea;
    private String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private String time;
    private int ONE_SECOND = 1000;
    private List<JPanel> panelist = new ArrayList<JPanel>();
    private List<String> ownernamelist = new ArrayList<String>();

    public static DataConsumer consumer;
    private ServerController serverController;
    private ConsumerMainView consumermainview;

    public ConsumerMainView getConsumerMainView() {
        return consumermainview;
    }

    public void setRegidtrarMainView(ConsumerMainView consumermainview) {
        this.consumermainview = consumermainview;
    }

    public ConsumerMainView(String ID, ServerController serverController) throws Exception {
        this.serverController = serverController;
        initialize(ID);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 设置为释放资源
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

    private void load_ownerlist() {
        Set<String> nameSet = new HashSet<>();
        for (PrivacyPolicy policy : polivylist) {
            nameSet.add(policy.getUsrName());
            ownernamelist = new ArrayList<>(nameSet);
        }
    }


    private void initialize(String ID) throws Exception {
        //添加方法
        FlatLightLaf.setup();
        frame = new JFrame();
        frame = this;
        frame.setResizable(false);
        frame.setBackground(Color.WHITE);

        String usrNameDC = getUsrName(ID);  // 用户名
        consumer = new DataConsumer(usrNameDC);


        daopanel1 = new DaoPanel1(consumermainview);
        daopanel1.setBackground(Color.WHITE);
        daopanel1.setVisible(true);
//    	 daopanel1.setBorder(new LineBorder(Color.GRAY,1));
        daopanel1.setBounds(311, 2, 974, 755);
        getContentPane().add(daopanel1);


        this.setTitle("数据消费者者系统");
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
                            mainframe_consumer window = new mainframe_consumer();
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

        JSeparator separator_1_1_1 = new JSeparator();
//        separator_1_1_1.setOrientation(SwingConstants.VERTICAL);
        separator_1_1_1.setBounds(309, 0, 10, 770);
        getContentPane().add(separator_1_1_1);

        JLabel label_2 = new JLabel("菜单选择：");
        label_2.setFont(new Font("微软雅黑", Font.BOLD, 20));
        label_2.setBounds(10, 208, 104, 35);
        getContentPane().add(label_2);

        JButton Search_Button = new JButton("单流查询");
        Search_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                daopanel1.setVisible(false);

                if (dataquerypanel != null) {
                    frame.remove(dataquerypanel);
                    try {
                        dataquerypanel = new DataConsumerQueryPanel(consumermainview);
                        panelist.add(dataquerypanel);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else {
                    try {
                        dataquerypanel = new DataConsumerQueryPanel(consumermainview);
                        panelist.add(dataquerypanel);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                closepanel(panelist, dataquerypanel);
//                dataquerypanel.setBorder(new LineBorder(Color.GRAY, 1));
                dataquerypanel.setBounds(311, 2, 974, 755);
                getContentPane().add(dataquerypanel);

                FrontEndSQL sql = new FrontEndSQL();
                polivylist = sql.searchPolicy(label_name.getText());

                //读取拥有者列表
                load_ownerlist();


                dataquerypanel.comboBox1.addItem("请选择");

                for (String name : ownernamelist) {
                    dataquerypanel.comboBox1.addItem(name);
                }

                dataquerypanel.textField_3.setText(getUsrName(ID));

            }
        });
        Search_Button.setIcon(MyUtil.setIcon("traffic_access_core/src/main/java/imge/高级查询.png"));
        Search_Button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        Search_Button.setBounds(163, 253, 133, 35);
        getContentPane().add(Search_Button);

        JButton Notice_Button = new JButton("通知文件");
        Notice_Button.setIcon(MyUtil.setIcon("traffic_access_core/src/main/java/imge/通知.png"));
        Notice_Button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        Notice_Button.setBounds(10, 364, 133, 35);
        getContentPane().add(Notice_Button);

        JButton More_Button = new JButton("更多功能");
        More_Button.setIcon(MyUtil.setIcon("traffic_access_core/src/main/java/imge/更多功能.png"));
        More_Button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        More_Button.setBounds(163, 364, 133, 35);
        getContentPane().add(More_Button);

        JButton Default_Button = new JButton("导航主页");
        Default_Button.setIcon(MyUtil.setIcon("traffic_access_core/src/main/java/imge/导航.png"));
        Default_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                daopanel1.setVisible(true);
                closepanel_dao(panelist);
            }
        });
        Default_Button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        Default_Button.setBounds(10, 253, 133, 35);
        getContentPane().add(Default_Button);

        JButton MPC_Button = new JButton("联邦查询");

        MPC_Button.setIcon(MyUtil.setIcon("traffic_access_core/src/main/java/imge/联邦查询.png"));
        MPC_Button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        MPC_Button.setBounds(10, 307, 133, 35);
        getContentPane().add(MPC_Button);

        JButton History_Button = new JButton("历史记录");

        History_Button.setIcon(MyUtil.setIcon("traffic_access_core/src/main/java/imge/历史记录.png"));
        History_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                daopanel1.setVisible(false);

                if (historypanel != null) {
                    frame.remove(historypanel);
                    try {
                        historypanel = new HistoryPanel(consumermainview);
                        panelist.add(historypanel);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else {
                    try {
                        historypanel = new HistoryPanel(consumermainview);
                        panelist.add(historypanel);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                closepanel(panelist, historypanel);
//                historypanel.setBorder(new LineBorder(Color.GRAY, 1));
                historypanel.setBounds(311, 2, 974, 755);
                getContentPane().add(historypanel);

                historypanel.textField_3.setText(getUsrName(ID));
                historypanel.textField_3.setBounds(109, 18, 66, 21);
                historypanel.textField_3.setVisible(false);


            }
        });
        History_Button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        History_Button.setBounds(163, 307, 133, 35);
        getContentPane().add(History_Button);


        MPC_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                daopanel1.setVisible(false);

                if (mpcpanel != null) {
                    frame.remove(mpcpanel);
                    try {
                        mpcpanel = new MPCPanel(consumermainview);
                        panelist.add(mpcpanel);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else {
                    try {
                        mpcpanel = new MPCPanel(consumermainview);
                        panelist.add(mpcpanel);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                closepanel(panelist, mpcpanel);
//                mpcpanel.setBorder(new LineBorder(Color.GRAY, 1));
                mpcpanel.setBounds(311, 2, 974, 755);
                getContentPane().add(mpcpanel);

                mpcpanel.textField_name.setText(getUsrName(ID));
                mpcpanel.textField_name.setBounds(109, 18, 66, 21);
            }
        });

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

    private void closepanel_dao(List<JPanel> panelist) {
        for (JPanel panel : panelist) {

            if (isPanelInContainer(frame.getContentPane(), panel)) {


                panel.setVisible(false);
                panel.setEnabled(false);
                frame.remove(panel);

            }
        }
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

    private String getUsrName(String id) {
        FrontEndSQL sql = new FrontEndSQL();
        String name = sql.searchName(id);
        return name;
    }


}
