package vn.name.ChanhDai.QuanLySinhVien.view;

import vn.name.ChanhDai.QuanLySinhVien.dao.AdminDAO;
import vn.name.ChanhDai.QuanLySinhVien.dao.SinhVienDAO;
import vn.name.ChanhDai.QuanLySinhVien.entity.Admin;
import vn.name.ChanhDai.QuanLySinhVien.entity.SinhVien;
import vn.name.ChanhDai.QuanLySinhVien.utils.ViewUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * vn.name.ChanhDai.QuanLySinhVien.view
 *
 * @created by ncdai3651408 - StudentID : 18120113
 * @date 7/3/20 - 9:53 PM
 * @description
 */
public class LoginView {
    JFrame mainFrame;
    GiaoVuHomeView giaoVuHomeView;
    SinhVienHomeView sinhVienHomeView;

    JTextField textFieldTenDangNhap;
    JPasswordField passwordFieldMatKhau;

    JButton buttonLogin;

    public LoginView() {
        createUI();

        giaoVuHomeView = new GiaoVuHomeView(this);
        sinhVienHomeView = new SinhVienHomeView(this);
    }

    void createUI() {
        mainFrame = new JFrame();
        mainFrame.setTitle("Chào mừng bạn đến với Phần Mềm Quản Lý Sinh Viên");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        mainFrame.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(32, 64, 32, 64));
        form.setBackground(Color.WHITE);

        JLabel title = new JLabel("Đăng Nhập");
        title.setFont(new Font("", Font.BOLD, 20));
        form.add(title, ViewUtils.createFormConstraints(0, 0, 3, 0, 0, 8, 0));

        JRadioButton radioButtonGiaoVu = ViewUtils.createRadioButton("Quản Lý", 112, 24, SwingConstants.CENTER);
        JRadioButton radioButtonSinhVien = ViewUtils.createRadioButton("Sinh Viên", 112, 24, SwingConstants.CENTER);

        radioButtonGiaoVu.setSelected(true);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioButtonGiaoVu);
        buttonGroup.add(radioButtonSinhVien);

        form.add(radioButtonGiaoVu, ViewUtils.createFormConstraints(0, 1, 1));
        form.add(radioButtonSinhVien, ViewUtils.createFormConstraints(1, 1, 1));

        form.add(new JLabel("Tên Đăng Nhập"), ViewUtils.createFormConstraints(0, 2, 1, 8, 8, 8, 0));
        textFieldTenDangNhap = new JTextField(10);
        textFieldTenDangNhap.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Pressed Enter -> Focus passwordField
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    passwordFieldMatKhau.requestFocus();
                }
            }
        });
        form.add(textFieldTenDangNhap, ViewUtils.createFormConstraints(1, 2, 2, 8, 0));

        form.add(new JLabel("Mật Khẩu"), ViewUtils.createFormConstraints(0, 3, 1));
        passwordFieldMatKhau = new JPasswordField();
        passwordFieldMatKhau.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Pressed Enter -> Submit
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    buttonLogin.doClick();
                }
            }
        });
        form.add(passwordFieldMatKhau, ViewUtils.createFormConstraints(1, 3, 2));

        buttonLogin = new JButton("Đăng Nhập");
        form.add(buttonLogin, ViewUtils.createFormConstraints(0, 4, 3, 8, 0, 0, 0));

        buttonLogin.addActionListener(e -> {
            String tenDangNhap = textFieldTenDangNhap.getText();
            String matKhau = String.valueOf(passwordFieldMatKhau.getPassword());

            if (tenDangNhap.equals("") || matKhau.equals("")) {
                JOptionPane.showMessageDialog(null, "Bạn chưa nhập đủ thông tin!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (radioButtonGiaoVu.isSelected()) {
                // Dang nhap Quan Ly
                Admin admin = AdminDAO.login(tenDangNhap, matKhau);
                if (admin != null) {
                    mainFrame.setVisible(false);
                    giaoVuHomeView.setAdmin(admin);
                    giaoVuHomeView.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Sai Tên Đăng Nhập hoặc Mật Khẩu", "Thông báo", JOptionPane.ERROR_MESSAGE);
                    passwordFieldMatKhau.setText("");
                    passwordFieldMatKhau.requestFocus();
                }
            } else {
                // Dang nhap Sinh Vien
                SinhVien sinhVien = SinhVienDAO.login(tenDangNhap, matKhau);
                if (sinhVien != null) {
                    mainFrame.setVisible(false);
                    sinhVienHomeView.setSinhVien(sinhVien);
                    sinhVienHomeView.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Sai Tên Đăng Nhập hoặc Mật Khẩu", "Thông báo", JOptionPane.ERROR_MESSAGE);
                    passwordFieldMatKhau.setText("");
                    passwordFieldMatKhau.requestFocus();
                }
            }
        });

        JPanel temp = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        temp.setBorder(BorderFactory.createEmptyBorder(64, 64, 64, 64));
        temp.add(form);

        mainFrame.add(temp, BorderLayout.CENTER);

        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
    }

    public void setVisible(boolean visible) {
        mainFrame.setVisible(visible);

        if (visible) {
            textFieldTenDangNhap.setText("");
            passwordFieldMatKhau.setText("");

            textFieldTenDangNhap.requestFocus();
        }
    }
}
