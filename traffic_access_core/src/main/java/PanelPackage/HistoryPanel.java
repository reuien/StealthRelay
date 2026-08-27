package PanelPackage;

import Item.History;
import Util.MyUtil;
import sqlConnect.FrontEndSQL;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

public class HistoryPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    public JTextField textField_3;
    private List<History> histories = new ArrayList<History>();
    private List<Integer> intlist = new ArrayList<>();

    /**
     * Create the panel.
     */
    public HistoryPanel(ConsumerMainView consumermainview) throws Exception {
        this.setBackground(Color.WHITE);
        setLayout(null);

        textField_3 = new JTextField();
        textField_3.setBounds(134, 18, 87, 21);
        add(textField_3);
        textField_3.setColumns(10);
        textField_3.setVisible(false);

        JLabel lblNewLabel_1 = new JLabel("历史记录");
        lblNewLabel_1.setFont(new Font("微软雅黑", Font.BOLD, 18));
        lblNewLabel_1.setBounds(10, 10, 87, 30);
        add(lblNewLabel_1);

        JPanel panel_1 = new JPanel();
        panel_1.setLayout(null);
//        panel_1.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        panel_1.setBounds(10, 78, 955, 659);
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

        JLabel lblNewLabel_2_2_1 = new JLabel("查询类型:");
        lblNewLabel_2_2_1.setHorizontalAlignment(SwingConstants.RIGHT);
        lblNewLabel_2_2_1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        lblNewLabel_2_2_1.setBounds(10, 11, 66, 25);
        panel_1.add(lblNewLabel_2_2_1);

        JComboBox<String> comboBox_type = new JComboBox<String>();
        comboBox_type.setBounds(86, 14, 142, 23);
        comboBox_type.addItem("请选择");
        comboBox_type.addItem("单流查询");
        comboBox_type.addItem("联邦查询");
        panel_1.add(comboBox_type);

        JButton btnNewButton = new JButton("查 询");
        btnNewButton.setForeground(Color.WHITE);
        btnNewButton.setBackground(new Color(90, 181, 94));
        btnNewButton.setBounds(265, 14, 93, 23);
        panel_1.add(btnNewButton);


        class CustomWrapTextAndScrollableRenderer extends JTextArea implements TableCellRenderer {
            public CustomWrapTextAndScrollableRenderer() {
                setLineWrap(true);
                setWrapStyleWord(true);
                setOpaque(true);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setText(value != null ? value.toString() : "");
                setPreferredSize(new Dimension(100, 50)); // 设置JTextArea的首选大小
                return this;
            }
        }

        class CustomWrapTextRenderer extends DefaultTableCellRenderer {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    label.setText("<html><div style='width:200px;'>" + value + "</div></html>"); // 设置换行宽度
                }

                return c;
            }
        }

        class ButtonRenderer extends JPanel implements TableCellRenderer {
            /**
             *
             */
            private static final long serialVersionUID = 1L;
            private JButton button;

            public ButtonRenderer() {
                button = new JButton("查看");
                button.setForeground(Color.WHITE);
                button.setBackground(new Color(90, 181, 94));
                button.setPreferredSize(new Dimension(80, 30)); // 设置按钮的固定大小
//		        button.setFocusPainted(false);
//		        button.setContentAreaFilled(false);
		        button.setBorderPainted(false);

                setLayout(new GridBagLayout()); // 使用 GridBagLayout 来居中按钮
                add(button);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                button.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                button.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                return this; // 返回包含按钮的面板
            }
        }



        class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
            /**
             *
             */
            private static final long serialVersionUID = 1L;
            private JButton button;

            public ButtonEditor(JTable table) {
                button = new JButton("查看");
                button.setForeground(Color.WHITE);
                button.setBackground(new Color(90, 181, 94));

                button.setPreferredSize(new Dimension(80, 30)); // 设置按钮的固定大小
                button.setFocusPainted(false);
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);

                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int selectedRow = table.getSelectedRow();
                        String url = (String) table.getValueAt(selectedRow, 4); //选择表格第五行数据


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
                        fireEditingStopped(); // 通知表格编辑完成
                    }
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                                                         boolean isSelected, int row, int column) {
                return button; // 返回按钮
            }

            @Override
            public Object getCellEditorValue() {
                return button.getText();
            }
        }


        class CustomTableModel extends DefaultTableModel {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public CustomTableModel(Object[] columnNames, int rowCount) {
                super(columnNames, rowCount);
            }


            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == getColumnCount() - 1) {
                    return JButton.class;
                } else {
                    return super.getColumnClass(columnIndex);
                }
            }
        }

        Object[] columnNames = {"查询时间", "流", "开始时间", "结束时间", "查询结果url", "按钮"};
        CustomTableModel model = new CustomTableModel(columnNames, 0);
        JTable table = new JTable(model);
//		  table.setRowHeight(50);
        // 创建自定义的单元格渲染器

        table.getColumnModel().getColumn(1).setCellRenderer(new CustomWrapTextRenderer());
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
//		  table.setDefaultRenderer(Object.class, renderer);


        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(table));

//        table.getColumnModel().getColumn(0).setCellEditor(null);
//        table.getColumnModel().getColumn(1).setCellEditor(null);
//        table.getColumnModel().getColumn(2).setCellEditor(null);
//        table.getColumnModel().getColumn(3).setCellEditor(null);
//        table.getColumnModel().getColumn(4).setCellEditor(null);
        for(int i =0;i<5;i++) {
            table.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(new JTextField()) {
                @Override
                public boolean isCellEditable(EventObject e) {
                    return false; // 禁用编辑
                }
            });
        }

        JScrollPane scrollPane1 = new JScrollPane(table);
        scrollPane1.setBounds(10, 45, 935, 565);
        panel_1.add(scrollPane1);

        JButton deleteButton = new JButton("删 除");
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setBackground(new Color(255, 96, 96));
        deleteButton.setBounds(824, 626, 93, 23);
        panel_1.add(deleteButton);
        deleteButton.setEnabled(false);

        JSeparator separator = new JSeparator();
        separator.setForeground(Color.GRAY);
        separator.setBounds(10, 52, 955, 12);
        add(separator);
        textField_3.setVisible(true);


        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (comboBox_type.getSelectedItem().equals("请选择")) {
                    JOptionPane.showMessageDialog(null, "请选择查询类型！");
                } else {
                    if (comboBox_type.getSelectedItem().equals("单流查询")) {
                        String type = (String) comboBox_type.getSelectedItem();
                        FrontEndSQL sql = new FrontEndSQL();
                        histories = sql.getHistory(textField_3.getText(), type);
                        model.setRowCount(0);
                        for (History history : histories) {

                            String des = sql.searchStream_Name_ID(history.getStreamID());
                            model.addRow(new Object[]{MyUtil.date2Str(history.getTime()), des, MyUtil.date2Str(history.getStartTime()), MyUtil.date2Str(history.getEndTime()), history.getUrl()});
                            model.fireTableDataChanged();
                        }

                        table.setRowHeight(50);
                    } else if (comboBox_type.getSelectedItem().equals("联邦查询")) {
                        String type = (String) comboBox_type.getSelectedItem();
                        FrontEndSQL sql = new FrontEndSQL();
                        histories = sql.getHistory_mpc(textField_3.getText(), type);
                        model.setRowCount(0);

                        for (int i = 0; i < histories.size(); i++) {
                            String str = histories.get(i).getStreamID_MPC();
                            String[] parts = str.split("\\+");

                            List<String> stringlist = new ArrayList<>();
                            for (String part : parts) {
                                stringlist.add(part);
                            }

                            List<Long> longList = new ArrayList<>();
                            for (String str1 : stringlist) {
                                Long number = Long.parseLong(str1);
                                longList.add(number);
                            }

                            intlist.add(longList.size());
                            String des = "";
                            for (int j = 0; j < longList.size(); j++) {
                                des = des + sql.searchStream_Name_ID(longList.get(j)) + "<br>";
                            }


                            model.addRow(new Object[]{MyUtil.date2Str(histories.get(i).getTime()), des, MyUtil.date2Str(histories.get(i).getStartTime()), MyUtil.date2Str(histories.get(i).getEndTime()), histories.get(i).getUrl()});

                            model.fireTableDataChanged();


                        }

                        for (int i = 0; i < histories.size(); i++) {
                            table.setRowHeight(i, 30 + 16 * intlist.get(i));
                        }


                    }

                }
            }
        });

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // Enable the delete button when rows are selected
                deleteButton.setEnabled(table.getSelectedRowCount() > 0);
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String nameString = textField_3.getText();
                String typeString = (String) comboBox_type.getSelectedItem();
                SwingUtilities.invokeLater(() -> {
                    int result = JOptionPane.showConfirmDialog(null, "<html>该操作会删除历史记录</html>确定要删除吗?", "删除确认", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        // 用户点击了"是"按钮，执行删除操作
                        SwingUtilities.invokeLater(() -> {
                            int result1 = JOptionPane.showConfirmDialog(null, "<html>需要删除原文件吗</html>该操作不可恢复确定要删除吗?", "删除确认", JOptionPane.YES_NO_OPTION);
                            if (result1 == JOptionPane.YES_OPTION) {
                                // 用户点击了"是"按钮，执行删除操作
                                System.out.println("已删除原文件");
                                FrontEndSQL sql = new FrontEndSQL();
                                sql.deleteHistoryFile(table, model, nameString, typeString);


                            }

                            System.out.println("用户点击了'是'按钮，执行删除操作");
                            FrontEndSQL sql = new FrontEndSQL();
                            try {
                                sql.deleteHistory(table, model, nameString, typeString);
                            } catch (NumberFormatException e1) {
                                e1.printStackTrace();
                            }

                        });


                    }
                });

            }
        });


    }
}


