package frame;
import java.awt.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import Item.Custom;
import PanelPackage.ConsumerMainView;
import com.formdev.flatlaf.FlatLightLaf;
import py.ServerController;
import sqlConnect.FrontEndSQL;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Random;

import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter; // 导入 WindowAdapter 类
import java.awt.event.WindowEvent; // 导入 WindowEvent 类

public class mainframe_consumer extends JFrame {

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private JPanel contentPane;
	private JFrame frame;
	private JTextField text_name;
	private JPasswordField text_password;
	private static ServerController serverController = new ServerController();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		String pythonInterpreter = "python";
		String scriptPath = "traffic_access_core/src/main/java/py/server.py";
		serverController.startServer(pythonInterpreter, scriptPath);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mainframe_consumer window = new mainframe_consumer();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws IOException
	 */
	public mainframe_consumer() throws IOException {
		initialize();
	}

	public void initialize() throws IOException {
		FlatLightLaf.setup();

		frame = new JFrame();
		frame.setResizable(false);

		frame.setVisible(true);
		frame.setTitle("端到端的可定制化流数据访问控制系统");
		frame.setIconImage(new ImageIcon("traffic_access_core/src/main/java/imge/1.jpg").getImage());
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 设置默认关闭操作为不执行任何操作
		frame.setBounds(400, 250, 424, 300);
		frame.getContentPane().setLayout(null);
		frame.getContentPane().setBackground(Color.white);

		// 添加窗口监听器
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// 自定义关闭操作
				int confirmed = JOptionPane.showConfirmDialog(null,
						"确定要关闭程序吗？", "关闭确认",
						JOptionPane.YES_NO_OPTION);

				if (confirmed == JOptionPane.YES_OPTION) {
					serverController.stopServer(); // 停止服务器
					frame.dispose(); // 释放 JFrame 资源
					System.exit(0); // 退出程序
				}
			}
		});

		JLabel lblNewLabel = new JLabel("数 据 消 费 者 登 录");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
		lblNewLabel.setBounds(60, 23, 287, 17);
		frame.getContentPane().add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("账 号：");
		lblNewLabel_1.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		lblNewLabel_1.setBounds(87, 80, 67, 18);
		frame.getContentPane().add(lblNewLabel_1);

		text_name = new JTextField();
		text_name.setBounds(154, 77, 141, 21);
		frame.getContentPane().add(text_name);
		text_name.setColumns(10);

		JLabel label = new JLabel("密 码：");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		label.setBounds(87, 118, 67, 18);
		frame.getContentPane().add(label);

		text_password = new JPasswordField();
		text_password.setBounds(154, 115, 141, 21);
		frame.getContentPane().add(text_password);

		JButton login_button = new JButton("登录");
		login_button.setBackground(new Color(42, 115, 197));
		login_button.setForeground(Color.WHITE);
		login_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String user = text_name.getText();
				String password = String.valueOf(text_password.getPassword());
				String result = null;

				if (user.equals("") || user == null) {
					JFrame frame = new JFrame();
					JOptionPane.showMessageDialog(frame, "您好，帐号不能为空！");
					return;
				} else if (password.equals("") || password == null) {
					JFrame frame = new JFrame();
					JOptionPane.showMessageDialog(frame, "您好，密码不能为空！");
					return;
				} else {
					FrontEndSQL sqlsearch = new FrontEndSQL();
					boolean matchFound = sqlsearch.Consumer_Login(user, password);
					if (matchFound) {
						result = "登录成功！";
						JOptionPane.showMessageDialog(frame, result);
						try {
							new ConsumerMainView(user, serverController); // 传递 serverController
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						frame.dispose();
					} else {
						JFrame frame = new JFrame();
						result = "密码错误请重新输入！";
						JOptionPane.showMessageDialog(frame, result);
					}
				}
			}
		});

		login_button.setBounds(106, 172, 76, 23);
		frame.getContentPane().add(login_button);

		JButton button_1 = new JButton("退出");
		button_1.setBackground(new Color(255, 96, 96));
		button_1.setForeground(Color.WHITE);
		button_1.setBounds(224, 172, 76, 23);
		button_1.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				serverController.stopServer();
				System.exit(0);
			}
		});
		frame.getContentPane().add(button_1);

		JSeparator separator = new JSeparator();
		separator.setBounds(10, 50, 416, 2);
		frame.getContentPane().add(separator);

		JButton regist_button = new JButton("注 册");
		regist_button.setBackground(new Color(251, 140, 0));
		regist_button.setForeground(Color.WHITE);
		regist_button.setBounds(302, 228, 76, 23);
		frame.getContentPane().add(regist_button);

		regist_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showInputDialog();
			}

			private void showInputDialog() {
				Random random = new Random();
				int accountNumber = 1000 + random.nextInt(9000);
				JTextField usernameField = new JTextField();
				JPasswordField passwordField = new JPasswordField();
				JTextField numberField = new JTextField();
				numberField.setText(Integer.toString(accountNumber));
				JPanel panel = new JPanel(new GridLayout(0, 1));

				panel.add(new JLabel("用户名:"));
				panel.add(usernameField);
				panel.add(new JLabel("密码:"));
				panel.add(passwordField);
				panel.add(new JLabel("账号:"));
				panel.add(numberField);

				int result = JOptionPane.showConfirmDialog(null, panel, "注册信息",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

				if (result == JOptionPane.OK_OPTION) {
					String name = usernameField.getText();
					String password = String.valueOf(passwordField.getPassword());
					String number = numberField.getText();

					// 检查输入是否为空
					if (name.isEmpty() || password.isEmpty() || number.isEmpty()) {
						JOptionPane.showMessageDialog(null, "所填信息不能为空", "Error", JOptionPane.ERROR_MESSAGE);
						showInputDialog();
					} else {
						if (number.length() == 4) {
							FrontEndSQL sql = new FrontEndSQL();
							if (!sql.NumberIsExisted(number, "消费者")) {
								if (!sql.NameIsExisted(name, "消费者")) {
									Custom custom = new Custom(name, number, password, "消费者");
									sql.Owner_Regist(custom);
									System.out.println("Username: " + name);
									System.out.println("Password: " + password);
									System.out.println("Number: " + number);
								} else {
									JOptionPane.showMessageDialog(null, "用户名重复请重新输入！");
									showInputDialog();
								}
							} else {
								JOptionPane.showMessageDialog(null, "账号重复请重新输入！");
								showInputDialog();
							}
						} else {
							JOptionPane.showMessageDialog(null, "账号长度必须是四位！");
							showInputDialog();
						}
					}
				}
			}
		});
	}
}
