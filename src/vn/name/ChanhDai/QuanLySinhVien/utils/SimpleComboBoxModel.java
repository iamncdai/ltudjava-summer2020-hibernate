package vn.name.ChanhDai.QuanLySinhVien.utils;

import javax.swing.*;
import java.util.Vector;

/**
 * vn.name.ChanhDai.QuanLySinhVien.utils
 *
 * @created by ncdai3651408 - StudentID : 18120113
 * @date 7/1/20 - 9:52 AM
 * @description
 */
public class SimpleComboBoxModel extends DefaultComboBoxModel<SimpleComboBoxItem> {
    public SimpleComboBoxModel(Vector<SimpleComboBoxItem> items) {
        super(items);
    }

    @Override
    public SimpleComboBoxItem getSelectedItem() {
        return (SimpleComboBoxItem) super.getSelectedItem();
    }
}
