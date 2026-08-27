package PanelPackage;


import javax.swing.*;
import java.awt.*;

public class DaoPanel1 extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Create the panel.
     */
    public DaoPanel1(ConsumerMainView consumermainview) throws Exception {
        setLayout(null);


        JLabel lblNewLabel = new JLabel("导航界面");
        lblNewLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel.setBounds(10, 22, 435, 30);
        add(lblNewLabel);


        JSeparator separator = new JSeparator();
        separator.setBounds(10, 64, 955, 12);
        separator.setForeground(Color.GRAY);
        add(separator);

        JLabel lblNewLabel_1 = new JLabel("该界面为数据消费者界面");
        lblNewLabel_1.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        lblNewLabel_1.setBounds(10, 86, 435, 35);
        add(lblNewLabel_1);

        JLabel lblNewLabel_1_1 = new JLabel("<html>如有疑问请联系客服： <b>XXXXXXXX</b> </html>");
        lblNewLabel_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        lblNewLabel_1_1.setBounds(10, 121, 435, 35);
        add(lblNewLabel_1_1);


        JLabel lblNewLabel_2 = new JLabel("功能导航：");
        lblNewLabel_2.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_2.setBounds(10, 182, 435, 30);
        add(lblNewLabel_2);


        JLabel lblNewLabel_1_1_1 = new JLabel("<html>①单用户数据查询请到 <b>单流查询</b> 界面</html>");
        lblNewLabel_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        lblNewLabel_1_1_1.setBounds(10, 220, 435, 35);
        add(lblNewLabel_1_1_1);

        JLabel lblNewLabel_1_1_1_1_1_1_1 = new JLabel("<html>③系统信息通知查询请到 <b>通知文件</b> 界面</html>");
        lblNewLabel_1_1_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        lblNewLabel_1_1_1_1_1_1_1.setBounds(10, 295, 435, 35);
        add(lblNewLabel_1_1_1_1_1_1_1);

        JLabel lblNewLabel_1_1_1_1_1_1_1_1 = new JLabel("<html>④ <b>更多功能</b> 将根据用户反馈后续开发</html>");
        lblNewLabel_1_1_1_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        lblNewLabel_1_1_1_1_1_1_1_1.setBounds(10, 331, 435, 35);
        add(lblNewLabel_1_1_1_1_1_1_1_1);

        JLabel lblNewLabel_1_1_1_1 = new JLabel("<html>②跨用户数据查询请到 <b>联邦查询</b> 界面</html>");
        lblNewLabel_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        lblNewLabel_1_1_1_1.setBounds(10, 258, 435, 35);
        add(lblNewLabel_1_1_1_1);


    }
}
