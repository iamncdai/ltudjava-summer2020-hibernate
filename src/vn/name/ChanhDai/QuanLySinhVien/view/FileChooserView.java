package vn.name.ChanhDai.QuanLySinhVien.view;

import vn.name.ChanhDai.QuanLySinhVien.utils.CSVUtils;
import vn.name.ChanhDai.QuanLySinhVien.utils.SimpleTableModel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * vn.edu.hcmus.fit.sv18120113.QuanLySinhVien.view
 *
 * @created by ncdai3651408 - StudentID : 18120113
 * @date 6/30/20 - 9:48 PM
 * @description
 */
public abstract class FileChooserView {
    private JFileChooser fileChooser;
    private JFrame importCSVFrame;
    private JTable tablePreview;

    public FileChooserView(JButton buttonTarget) {
        buttonTarget.addActionListener(e -> chooseFile());

        createUI();
    }

    void chooseFile() {
        int returnVal = fileChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            System.out.println("File : " + file.getAbsolutePath());

            SimpleTableModel tablePreviewModel = (SimpleTableModel) tablePreview.getModel();
            tablePreviewModel.clearRows();

            String fileName = file.getAbsolutePath();
            List<String[]> list = CSVUtils.reader(fileName);

            for (String[] item : list) {
                System.out.println(Arrays.toString(item));
                Vector<String> row = parseTableRow(item);

                if (row != null) {
                    tablePreviewModel.addRow(row);
                }
            }

            tablePreviewModel.fireTableDataChanged();
            importCSVFrame.setVisible(true);

        } else {
            System.out.println("File : Cancel");
        }
    }

    public void createUI() {
        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn File CSV");
        fileChooser.setCurrentDirectory(new File("./data")); // DEBUG

        FileNameExtensionFilter filter = new FileNameExtensionFilter("File CSV", "csv");
        fileChooser.setFileFilter(filter);

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

        tablePreview = new JTable(new SimpleTableModel(getColumnNames(), null));
        tablePreview.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(tablePreview);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.WHITE, 8));

        Container contentPance = importCSVFrame.getContentPane();
        contentPance.add(panelHeader, BorderLayout.PAGE_START);
        contentPance.add(scrollPane, BorderLayout.CENTER);

        buttonChooseAnotherFile.addActionListener(e -> chooseFile());

        buttonImport.addActionListener(e -> startImport(tablePreview));

        importCSVFrame.pack();
        importCSVFrame.setLocationRelativeTo(null);
    }

    public abstract Vector<String> getColumnNames();

    public abstract Vector<String> parseTableRow(String[] str);

    public abstract void startImport(JTable tablePreview);
}
