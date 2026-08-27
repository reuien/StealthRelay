package PanelPackage;

import javax.swing.*;
import java.awt.*;

public class DaoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Create the panel.
     */
    public DaoPanel(OwnerMainView ownermainview) throws Exception {
        setLayout(null);
        this.setBackground(Color.WHITE);
        JLabel lblNewLabel = new JLabel("导航界面");
        lblNewLabel.setBackground(new Color(255, 255, 255));
        lblNewLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel.setBounds(10, 22, 435, 30);
        add(lblNewLabel);


        JSeparator separator = new JSeparator();
        separator.setBounds(10, 64, 955, 12);
        separator.setForeground(Color.GRAY);
        add(separator);

        JLabel lblNewLabel_1 = new JLabel("该界面为数据拥有者界面");
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


        JLabel lblNewLabel_1_1_1 = new JLabel("<html>①如需绑定/解除设备请到 <b>设备管理</b> 界面</html>");
        lblNewLabel_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        lblNewLabel_1_1_1.setBounds(10, 220, 435, 35);
        add(lblNewLabel_1_1_1);

        JLabel lblNewLabel_1_1_1_1 = new JLabel("<html>②完成设备绑定后请到 <b>基本设置</b> 界面</html>");
        lblNewLabel_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        lblNewLabel_1_1_1_1.setBounds(10, 255, 435, 35);
        add(lblNewLabel_1_1_1_1);

        JLabel lblNewLabel_1_1_1_1_1 = new JLabel("<html>③如需删除流数据/策略信息请到 <b>信息管理</b> 界面</html>");
        lblNewLabel_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        lblNewLabel_1_1_1_1_1.setBounds(10, 290, 435, 35);
        add(lblNewLabel_1_1_1_1_1);

        JLabel lblNewLabel_1_1_1_1_1_1 = new JLabel("<html>④如需设置隐私策略请到 <b>隐私策略</b> 界面</html>");
        lblNewLabel_1_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        lblNewLabel_1_1_1_1_1_1.setBounds(10, 325, 435, 35);
        add(lblNewLabel_1_1_1_1_1_1);

        JLabel lblNewLabel_1_1_1_1_1_1_1 = new JLabel("<html>⑥系统信息通知查询请到 <b>通知文件</b> 界面</html>");
        lblNewLabel_1_1_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        lblNewLabel_1_1_1_1_1_1_1.setBounds(10, 395, 435, 35);
        add(lblNewLabel_1_1_1_1_1_1_1);

        JLabel lblNewLabel_1_1_1_1_1_1_1_1 = new JLabel("<html>⑦ <b>更多功能</b> 将根据用户反馈后续开发</html>");
        lblNewLabel_1_1_1_1_1_1_1_1.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        lblNewLabel_1_1_1_1_1_1_1_1.setBounds(10, 430, 435, 35);
        add(lblNewLabel_1_1_1_1_1_1_1_1);

        JLabel lblNewLabel_1_1_1_1_1_1_2 = new JLabel("<html>⑤如需查询数据请到 <b>数据查询</b> 界面</html>");
        lblNewLabel_1_1_1_1_1_1_2.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        lblNewLabel_1_1_1_1_1_1_2.setBounds(10, 360, 435, 35);
        add(lblNewLabel_1_1_1_1_1_1_2);


    }
}
