package PanelPackage;

import Item.Equipment;
import sqlConnect.FrontEndSQL;
import usrs.DataOwnerClient;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import javax.swing.table.*;
public class EqPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    public List<Equipment> equipments = OwnerMainView.equipments;
    public JTextField id_textField;
    public static DataOwnerClient doc;


    public EqPanel(OwnerMainView ownermainview) throws Exception {
        this.setBackground(Color.WHITE);
        setLayout(null);

        System.out.println("设备管理");
        id_textField = new JTextField();
        id_textField.setBounds(98, 30, 66, 21);
        add(id_textField);
        id_textField.setColumns(10);
        id_textField.setVisible(false);

        doc = ownermainview.doc;


        JLabel lblNewLabel = new JLabel("设备管理");

        lblNewLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel.setBounds(10, 22, 435, 30);
        add(lblNewLabel);

        JSeparator separator = new JSeparator();
        separator.setBounds(10, 64, 955, 12);
        separator.setForeground(Color.GRAY);
        add(separator);

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBounds(10, 86, 955, 528);
        panel.setLayout(null);
        add(panel);

        int rows = equipments.size();
        int cols = 5; // Number of attributes in Equipment class

        String[][] result = new String[rows][cols];

        for (int i = 0; i < rows; i++) {
            Equipment equipment = equipments.get(i);
            result[i][0] = equipment.getOwner();
            result[i][1] = equipment.getIdnum();
            result[i][2] = equipment.getName();
            result[i][3] = equipment.getPort();
            result[i][4] = equipment.getiP();
        }


        Object[] columnNames = {"用户id", "设备id", "设备名称", "设备端口号", "设备IP"};

        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        JTable table = new JTable(model);
// 设置表头颜色
        // 设置表头颜色
        JTableHeader header = table.getTableHeader();
//        header.setBackground(new Color(173, 216, 230)); // 浅蓝色
        header.setBackground(new Color(70, 130, 180)); // 钢蓝色（Steel Blue）

        header.setForeground(Color.WHITE); // 黑色字体

// 设置表格内容颜色
        table.setBackground(new Color(245, 245, 245)); // 浅灰色
        table.setForeground(Color.BLACK); // 黑色字体
// 设置选中行的颜色
        table.setSelectionBackground(new Color(173, 216, 230)); // 浅绿色（Light Green）
//        table.setSelectionBackground(new Color(144, 238, 144)); // 浅绿色（Light Green）
        table.setSelectionForeground(Color.BLACK); // 选中行的字体颜色
        // 将初始数据添加到表格模型中
        for (Equipment equipment : equipments) {
            model.addRow(new Object[]{equipment.getOwner(), equipment.getIdnum(), equipment.getName(), equipment.getPort(), equipment.getiP()});
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(10, 10, 935, 428); // Adjust the bounds as needed
        panel.add(scrollPane);

        JButton deleteButton = new JButton("删除");
        deleteButton.setBounds(828, 448, 93, 23);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setBackground(new Color(255, 96, 96));
        panel.add(deleteButton);
        deleteButton.setEnabled(false);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // Enable the delete button when rows are selected
                deleteButton.setEnabled(table.getSelectedRowCount() > 0);
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String id = id_textField.getText();
                FrontEndSQL sql = new FrontEndSQL();
                sql.deleteEqData(table, model, id);
            }
        });


        JButton addButton = new JButton("增添");
        addButton.setBounds(725, 448, 93, 23);
        addButton.setForeground(Color.WHITE);
        addButton.setBackground(new Color(42, 115, 197));
        panel.add(addButton);


        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    showInputDialog();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            private void showInputDialog() throws IOException {
                JTextField nameField = new JTextField();
                JTextField portField = new JTextField();
                JTextField ipAddressField = new JTextField();

                JPanel panel = new JPanel(new GridLayout(0, 1));
                panel.add(new JLabel("设备名称:"));
                panel.add(nameField);
                panel.add(new JLabel("端口号:"));
                panel.add(portField);
                panel.add(new JLabel("IP地址:"));
                panel.add(ipAddressField);

                int result = JOptionPane.showConfirmDialog(null, panel, "增添设备信息",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String name = nameField.getText();
                    String port = portField.getText();
                    String ipAddress = ipAddressField.getText();

                    // 检查输入是否为空
                    if (name.isEmpty() || port.isEmpty() || ipAddress.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "所填信息不能为空", "Error", JOptionPane.ERROR_MESSAGE);
                        // 递归调用，再次显示填写界面
                        showInputDialog();
                    } else {
                        // 获取上一个行的id值

                        FrontEndSQL sql1 = new FrontEndSQL();
                        String eqLastID = sql1.serarchLastID(id_textField.getText());
                        int eqLastID_int = Integer.parseInt(eqLastID);

                        String eqid = Integer.toString(eqLastID_int + 1);
                        String owner_id = id_textField.getText();

                        //******************************
                        //联动生产者
                        System.out.println("start");
                        System.out.println("id: " + (eqLastID_int + 1));
                        System.out.println("name: " + name);
                        System.out.println("ip: " + ipAddress);
                        System.out.println("port: " + Integer.parseInt(port));
                        if (doc.registerProducer(eqLastID_int + 1, name, ipAddress, Integer.parseInt(port))) {

                            System.out.println("register  ok");
                        }
                        System.out.println("ok");
                        //联动生产者
                        //******************************

                        // 将输入的数据添加到表格模型中
                        model.addRow(new Object[]{owner_id, eqid, name, port, ipAddress});
                        //写入数据库
                        FrontEndSQL sql = new FrontEndSQL();
                        sql.insertEqData(eqid, owner_id, name, port, ipAddress);
                    }
                }
            }
        });


    }
}
