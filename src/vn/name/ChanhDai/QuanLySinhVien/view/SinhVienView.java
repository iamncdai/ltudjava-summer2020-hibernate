package vn.name.ChanhDai.QuanLySinhVien.view;

import vn.name.ChanhDai.QuanLySinhVien.utils.*;
import vn.name.ChanhDai.QuanLySinhVien.dao.SinhVienDAO;
import vn.name.ChanhDai.QuanLySinhVien.entity.SinhVien;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Vector;

class GetSinhVienThread extends Thread {
    JTable table;
    String maLop;

    public GetSinhVienThread(JTable table, String maLop) {
        this.table = table;
        this.maLop = maLop;
    }

    public void run() {
        List<SinhVien> list;
        if (maLop.equals("all") || maLop.equals("")) {
            list = SinhVienDAO.getList();
        } else {
            list = SinhVienDAO.getListByMaLop(maLop);
        }

        SimpleTableModel model = (SimpleTableModel) table.getModel();

        // Reset Table
        model.clearRows();

        for (SinhVien sinhVien : list) {
            model.addRow(TableUtils.toRow(sinhVien));
        }

        model.fireTableDataChanged();
    }
}

class ImportCSVThread extends Thread {
    JTable tableDraft;
    JTable tableTarget;

    ImportCSVThread(JTable tableDraft, JTable tableTarget) {
        this.tableDraft = tableDraft;
        this.tableTarget = tableTarget;
    }

    @Override
    public void run() {
        SimpleTableModel tableDraftModel = (SimpleTableModel)tableDraft.getModel();
        SimpleTableModel tableTargetModel = (SimpleTableModel)tableTarget.getModel();

        int desiredImportQuantity = tableDraftModel.getRowCount();
        int actualImportQuantity = 0;

        for (int i = 0; i < desiredImportQuantity; ++i) {
            SinhVien sinhVien = TableUtils.parseSinhVien(tableDraftModel.getRow(i));

            boolean success = SinhVienDAO.create(sinhVien);
            if (success) {
                tableDraftModel.setValueAt( i, 5, "[SUCCESS]");
                tableTargetModel.addRow(TableUtils.toRow(sinhVien));
                ++actualImportQuantity;
            } else {
                tableDraftModel.setValueAt( i, 5, "[FAILED]");
            }

            tableDraftModel.fireTableDataChanged();
            tableTargetModel.fireTableDataChanged();
        }

        JOptionPane.showMessageDialog(null, "Đã nhập dữ liệu thành công (" + actualImportQuantity + "/" + desiredImportQuantity + " sinh viên)", "Kết quả", JOptionPane.INFORMATION_MESSAGE);
    }
}

class GetComboBoxMaLopThread extends Thread {
    JComboBox<SimpleComboBoxItem> comboBox;

    GetComboBoxMaLopThread(JComboBox<SimpleComboBoxItem> comboBox) {
        this.comboBox = comboBox;
    }

    @Override
    public void run() {
        List<String> list = SinhVienDAO.getLopList();
        SimpleComboBoxModel model = (SimpleComboBoxModel)comboBox.getModel();
        for (String item : list) {
            model.addElement(new SimpleComboBoxItem(item, item));
        }
    }
}

public class SinhVienView {
    JFrame mainFrame;
    JFrame importCSVFrame;

    JTable tableSinhVien;
    JTable tablePreview;

    JComboBox<SimpleComboBoxItem> comboBoxMaLop;

    JFileChooser fileChooser = new JFileChooser();

    JTextField textFieldMaSinhVien;
    JTextField textFieldHoTen;
    JComboBox<String> comboBoxGioiTinh;
    JTextField textFieldCMND;
    JTextField textFieldMaLop;

    JRadioButton radioButtonUpdate;
    JRadioButton radioButtonCreate;
    JRadioButton radioButtonDelete;

    public SinhVienView() {
        createAndShowUI();
        createImportCSVUI();

        new GetComboBoxMaLopThread(comboBoxMaLop).start();
        new GetSinhVienThread(tableSinhVien, "all").start();
    }

    public void createSinhVien(SinhVien sinhVien) {
        boolean success = SinhVienDAO.create(sinhVien);
        if (success) {
            SimpleTableModel tableModel = (SimpleTableModel) tableSinhVien.getModel();
            tableModel.addRow(TableUtils.toRow(sinhVien));
            tableModel.fireTableDataChanged();

            JOptionPane.showMessageDialog(mainFrame, "Thêm sinh viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(mainFrame, "Thêm sinh viên thất bại!", "Thông báo", JOptionPane.ERROR_MESSAGE);
    }

    public void updateSinhVien(SinhVien sinhVien, int row) {
        boolean success = SinhVienDAO.update(sinhVien);
        if (success) {
            SimpleTableModel tableModel = (SimpleTableModel) tableSinhVien.getModel();
            tableModel.updateRow(row, TableUtils.toRow(sinhVien));
            tableModel.fireTableDataChanged();

            JOptionPane.showMessageDialog(mainFrame, "Cập nhật thông tin sinh viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(mainFrame, "Cập nhật thông tin sinh viên thất bại!", "Thông báo", JOptionPane.ERROR_MESSAGE);
    }

    public void deleteSinhVien(SinhVien sinhVien, int row) {
        int confirm = JOptionPane.showConfirmDialog(
            mainFrame,
            "Bạn chắn chắn muốn xóa sinh viên " + sinhVien.getMaSinhVien() + "?",
            "Xác nhận",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = SinhVienDAO.delete(sinhVien.getMaSinhVien());
        if (success) {
            SimpleTableModel tableModel = (SimpleTableModel) tableSinhVien.getModel();
            tableModel.removeRow(row);
            tableModel.fireTableDataChanged();
            if (tableModel.getRowCount() > 0) {
                tableSinhVien.setRowSelectionInterval(0, 0);
            }

            JOptionPane.showMessageDialog(mainFrame, "Xóa sinh viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(mainFrame, "Xóa sinh viên thất bại!", "Thông báo", JOptionPane.ERROR_MESSAGE);
    }

    public SinhVien getSeletedRow() {
        int rowIndex = tableSinhVien.getSelectedRow();
        if (rowIndex == -1) {
            return null;
        }

        SimpleTableModel tableModel = (SimpleTableModel) tableSinhVien.getModel();
        return TableUtils.parseSinhVien(tableModel.getRow(rowIndex));
    }

    public void setFormValues(String maSinhVien, String hoTen, String gioiTinh, String cmnd, String maLop) {
        textFieldMaSinhVien.setText(maSinhVien);
        textFieldHoTen.setText(hoTen);
        comboBoxGioiTinh.setSelectedItem(gioiTinh);
        textFieldCMND.setText(cmnd);
        textFieldMaLop.setText(maLop);
    }

    public void setFormValuesBySeletedRow() {
        SinhVien sinhVien = getSeletedRow();
        if (sinhVien == null) return;

        this.setFormValues(
            sinhVien.getMaSinhVien(),
            sinhVien.getHoTen(),
            sinhVien.getGioiTinh(),
            sinhVien.getCmnd(),
            sinhVien.getMaLop()
        );
    }

    public void setFormEnabled(boolean enabled) {
        textFieldMaSinhVien.setEnabled(enabled);
        textFieldHoTen.setEnabled(enabled);
        comboBoxGioiTinh.setEnabled(enabled);
        textFieldCMND.setEnabled(enabled);
        textFieldMaLop.setEnabled(enabled);
    }

    public void resetForm() {
        setFormValues("", "", "", "", "");
    }

    public void createImportCSVUI() {
        importCSVFrame = new JFrame();
        importCSVFrame.setTitle("Nhập File CSV");

        BorderLayout borderLayout = new BorderLayout(0, 8);
        importCSVFrame.setLayout(borderLayout);

        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.X_AXIS));
        panelHeader.setBackground(Color.WHITE);
        panelHeader.setBorder(BorderFactory.createLineBorder(Color.WHITE, 8));

        JButton buttonChooseAnotherFile = new JButton("Chọn File khác");
        JButton buttonImport = new JButton("Bắt đầu nhập");

        panelHeader.add(new JLabel("Xem trước"));
        panelHeader.add(Box.createHorizontalGlue());
        panelHeader.add(buttonChooseAnotherFile);
        panelHeader.add(Box.createRigidArea(new Dimension(8,0)));
        panelHeader.add(buttonImport);

        tablePreview = new JTable();

        Vector<String> columnNames = new Vector<>();
        columnNames.add("MSSV");
        columnNames.add("Họ tên");
        columnNames.add("Giới tính");
        columnNames.add("CMND");
        columnNames.add("Lớp");
        columnNames.add("Trạng thái");

        tablePreview.setModel(new SimpleTableModel(columnNames, null));
        tablePreview.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(tablePreview);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.WHITE, 8));

        Container contentPance = importCSVFrame.getContentPane();
        contentPance.add(panelHeader, BorderLayout.PAGE_START);
        contentPance.add(scrollPane, BorderLayout.CENTER);

        buttonChooseAnotherFile.addActionListener(e -> {
            handleImportCSVClick();
        });

        buttonImport.addActionListener(e -> {
            new ImportCSVThread(tablePreview, tableSinhVien).start();
        });

        importCSVFrame.pack();
        importCSVFrame.setLocationRelativeTo(null);
    }

    void handleImportCSVClick() {
        int returnVal = fileChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            System.out.println("File : " + file.getAbsolutePath());

            SimpleTableModel tablePreviewModel = (SimpleTableModel) tablePreview.getModel();
            tablePreviewModel.clearRows();

            String fileName = file.getAbsolutePath();
            List<String[]> list = CSVUtils.reader(fileName);
            for (String[] item : list) {
                SinhVien sinhVien = CSVUtils.parseSinhVien(item);
                if (sinhVien != null) {
                    Vector<String> row = TableUtils.toRow(sinhVien);
                    row.add("[PENDING]");
                    tablePreviewModel.addRow(row);
                }
            }

            tablePreviewModel.fireTableDataChanged();
            importCSVFrame.setVisible(true);
        } else {
            System.out.println("File : Cancel");
        }
    }

    public void createAndShowUI() {
        mainFrame = new JFrame();
        mainFrame.setTitle("Danh Sách Sinh Viên");

        BorderLayout layout = new BorderLayout();
        mainFrame.setLayout(layout);

        JLabel title = new JLabel("Danh Sách Sinh Viên");
        title.setFont(new Font("", Font.BOLD, 24));
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        headerPanel.add(title);

        JLabel labelFilter = new JLabel("Xem lớp");

        Vector<SimpleComboBoxItem> maLopList = new Vector<>();
        maLopList.add(new SimpleComboBoxItem("all", "Tất cả"));

        SimpleComboBoxModel maLopModel = new SimpleComboBoxModel(maLopList);

        comboBoxMaLop = new JComboBox<>(maLopModel);
        comboBoxMaLop.addActionListener(e -> {
            SimpleComboBoxItem item = (SimpleComboBoxItem) comboBoxMaLop.getSelectedItem();
            if (item != null) {
                System.out.println(item.getLabel() + " " + item.getValue());
                String maLop = item.getValue();
                new GetSinhVienThread(tableSinhVien, maLop).start();
            }
        });

        JButton importCSVButton = new JButton("Nhập File CSV");
        importCSVButton.setPreferredSize(new Dimension(120, 24));
        importCSVButton.addActionListener(e -> {
            handleImportCSVClick();
        });

        JPanel topMenuPanel = new JPanel();
        topMenuPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 8));
        topMenuPanel.setBackground(Color.WHITE);
        BoxLayout topMenuPanelLayout = new BoxLayout(topMenuPanel, BoxLayout.X_AXIS);
        topMenuPanel.setLayout(topMenuPanelLayout);
        topMenuPanel.add(labelFilter);
        topMenuPanel.add(Box.createRigidArea(new Dimension(8,0)));
        topMenuPanel.add(comboBoxMaLop);
        topMenuPanel.add(Box.createHorizontalGlue());
        topMenuPanel.add(importCSVButton);

        JPanel centerPanel = new JPanel();
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        centerPanel.setLayout(new BorderLayout(0, 8));
        centerPanel.add(topMenuPanel, BorderLayout.PAGE_START);

        Vector<String> columnNames = new Vector<>();
        columnNames.add("MSSV");
        columnNames.add("Họ tên");
        columnNames.add("Giới tính");
        columnNames.add("CMND");
        columnNames.add("Lớp");

        tableSinhVien = new JTable(new SimpleTableModel(columnNames, null));
        tableSinhVien.setFillsViewportHeight(true);

        tableSinhVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableSinhVien.setDefaultEditor(Object.class, null);

        ListSelectionModel selectionModel = tableSinhVien.getSelectionModel();
        selectionModel.addListSelectionListener(e -> {
            if (radioButtonCreate.isSelected()) {
                resetForm();
            } else {
                setFormValuesBySeletedRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableSinhVien);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.WHITE, 8));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createLineBorder(Color.WHITE, 8));
        formPanel.add(form);

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;

        radioButtonUpdate = new JRadioButton("Cập nhật");
        radioButtonUpdate.setPreferredSize(new Dimension(96, 24));
        radioButtonUpdate.setHorizontalAlignment(SwingConstants.CENTER);
        radioButtonUpdate.setBackground(Color.decode("#f5f5f5"));
//        radioButtonUpdate.setForeground(Color.WHITE);
        radioButtonUpdate.setSelected(true);
        radioButtonUpdate.addActionListener(e -> {
            System.out.println("radioButtonUpdate " + radioButtonUpdate.isSelected());

            resetForm();
            setFormValuesBySeletedRow();

            setFormEnabled(true);
            textFieldMaSinhVien.setEnabled(false);
        });

        radioButtonCreate = new JRadioButton("Thêm");
        radioButtonCreate.setPreferredSize(new Dimension(96, 24));
        radioButtonCreate.setHorizontalAlignment(SwingConstants.CENTER);
        radioButtonCreate.setBackground(Color.decode("#f5f5f5"));
//        radioButtonCreate.setForeground(Color.WHITE);
        radioButtonCreate.addActionListener(e -> {
            System.out.println("radioButtonCreate " + radioButtonCreate.isSelected());

            resetForm();
            setFormEnabled(true);
        });

        radioButtonDelete = new JRadioButton("Xóa");
        radioButtonDelete.setPreferredSize(new Dimension(96, 24));
        radioButtonDelete.setHorizontalAlignment(SwingConstants.CENTER);
        radioButtonDelete.setBackground(Color.decode("#f5f5f5"));
//        radioButtonDelete.setForeground(Color.WHITE);
        radioButtonDelete.addActionListener(e -> {
            System.out.println("radioButtonDelete " + radioButtonDelete.isSelected());

            resetForm();
            setFormEnabled(false);
            setFormValuesBySeletedRow();
        });

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioButtonUpdate);
        buttonGroup.add(radioButtonCreate);
        buttonGroup.add(radioButtonDelete);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.insets = new Insets(0, 0, 4, 0);
        form.add(radioButtonUpdate, c);

        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        form.add(radioButtonCreate, c);

        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        form.add(radioButtonDelete, c);

        c.gridx = 0;
        c.gridy = 1;
        c.ipadx = 0;
        c.insets = new Insets(4, 0, 4, 0);
        c.gridwidth = 1;
        form.add(new JLabel("MSSV"), c);

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 2;
        textFieldMaSinhVien = new JTextField();
        textFieldMaSinhVien.setEnabled(false);
        form.add(textFieldMaSinhVien, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        form.add(new JLabel("Họ và tên"), c);

        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 2;
        textFieldHoTen = new JTextField();
        form.add(textFieldHoTen, c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        form.add(new JLabel("Giới tính"), c);

        String[] gioiTinhList = {"Nam", "Nữ", "Khác"};
        comboBoxGioiTinh = new JComboBox<>(gioiTinhList);
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 2;
        form.add(comboBoxGioiTinh, c);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        form.add(new JLabel("CMND"), c);

        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 2;
        textFieldCMND = new JTextField();
        form.add(textFieldCMND, c);

        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        form.add(new JLabel("Mã lớp"), c);

        c.gridx = 1;
        c.gridy = 5;
        c.gridwidth = 2;
        textFieldMaLop = new JTextField();
        form.add(textFieldMaLop, c);

        JButton buttonSubmit = new JButton("Thực hiện");
        buttonSubmit.addActionListener(e -> {
            String maSinhVien = textFieldMaSinhVien.getText();
            String hoTen = textFieldHoTen.getText();
            String gioiTinh = (String) comboBoxGioiTinh.getSelectedItem();
            String cmnd = textFieldCMND.getText();
            String maLop = textFieldMaLop.getText();

            if (maSinhVien.equals("") || hoTen.equals("") || gioiTinh == null || gioiTinh.equals("") || cmnd.equals("") || maLop.equals("")) {
                JOptionPane.showMessageDialog(mainFrame, "Bạn chưa nhập đủ thông tin!", "Thông báo", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SinhVien sinhVien = new SinhVien();
            sinhVien.setMaSinhVien(maSinhVien);
            sinhVien.setHoTen(hoTen);
            sinhVien.setGioiTinh(gioiTinh);
            sinhVien.setCmnd(cmnd);
            sinhVien.setMaLop(maLop);
            sinhVien.setMatKhau(maSinhVien);

            int rowSelectedIndex = tableSinhVien.getSelectedRow();

            if (radioButtonCreate.isSelected()) {

                // Create
                System.out.println("buttonSubmit -> Create");
                createSinhVien(sinhVien);

            } else if (radioButtonUpdate.isSelected()) {

                // Update
                System.out.println("buttonSubmit -> Update");
                updateSinhVien(sinhVien, rowSelectedIndex);

            } else if (radioButtonDelete.isSelected()) {

                // Delete
                System.out.println("buttonSubmit -> Delete");
                deleteSinhVien(sinhVien, rowSelectedIndex);
            }

        });

        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 3;
        c.insets = new Insets(4, 0, 0, 0);
        form.add(buttonSubmit, c);

        sidebarPanel.add(formPanel);

        Container pane = mainFrame.getContentPane();
        pane.add(headerPanel, BorderLayout.PAGE_START);
        pane.add(centerPanel, BorderLayout.CENTER);
        pane.add(sidebarPanel, BorderLayout.LINE_END);

        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
}
