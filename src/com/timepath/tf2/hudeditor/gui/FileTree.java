package com.timepath.tf2.hudeditor.gui;

import com.timepath.tf2.hudeditor.util.Element;
import com.timepath.tf2.loaders.GCF;
import com.timepath.tf2.loaders.GCF.DirectoryEntry;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author timepath
 */
@SuppressWarnings("serial")
public class FileTree extends javax.swing.JTree {

    public FileTree(TreeNode root) {
        super(root);
        initComponents();
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setCellRenderer(new CustomTreeCellRenderer());
    }

    private class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

        CustomTreeCellRenderer() {
            super();
        }

        private void setIcons(JTree tree, Icon ico) {
            if(tree.isEnabled()) {
                this.setIcon(ico);
            } else {
                this.setDisabledIcon(ico);
            }
        }

        Color sameColor = Color.BLACK;

        Color diffColor = Color.BLUE;

        Color newColor = Color.GREEN.darker();

        /**
         * Configures the renderer based on the passed in components.
         * The value is set from messaging the tree with
         * <code>convertValueToText</code>, which ultimately invokes
         * <code>toString</code> on
         * <code>value</code>.
         * The foreground color is set based on the selection and the icon
         * is set based on on leaf and expanded.
         */
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            String valueText = value.toString();

            Color tColor = null;

            if(value instanceof DefaultMutableTreeNode) {
                Object nodeValue = ((DefaultMutableTreeNode) value).getUserObject();
                if(nodeValue instanceof String) {
                    tColor = sameColor;
                    setIcons(tree, UIManager.getIcon("FileView.computerIcon"));
                } else if(nodeValue instanceof File) { // this will either be an actual file on the system (directories included), or an element within a file
                    tColor = diffColor;
                    File f = ((File) nodeValue);
                    valueText = f.getName();
                    if(f.isDirectory()) {
                        setIcons(tree, UIManager.getIcon("FileView.directoryIcon"));
                    } else {
                        setIcons(tree, UIManager.getIcon("FileView.fileIcon"));
                    }
                } else if(nodeValue instanceof Element) {
                    tColor = newColor;
                    Element e = (Element) nodeValue;
                    if(e.getProps().isEmpty() && leaf) { // If no properties, warn because irrelevant. Only care if leaves are empty
                        setIcons(tree, UIManager.getIcon("FileChooser.detailsViewIcon"));
                    } else {
                        setIcons(tree, UIManager.getIcon("FileChooser.listViewIcon"));
                    }
                } else if(nodeValue instanceof GCF) {
                    Icon i = UIManager.getIcon("FileView.hardDriveIcon");
                    if(i == null) {
                        i = UIManager.getIcon("FileView.directoryIcon");
                    }
                    setIcons(tree, i);
                } else if(nodeValue instanceof DirectoryEntry) {
                    DirectoryEntry d = (DirectoryEntry) nodeValue;
                    if(d.attributes == 0) {
                        setIcons(tree, UIManager.getIcon("FileView.directoryIcon"));
                    } else {
                        setIcons(tree, UIManager.getIcon("FileView.fileIcon"));
                    }
                } else {
                    if(nodeValue != null) {
                        LOG.log(Level.FINE, "Node class: {0}", nodeValue.getClass());
                    }
                    setIcons(tree, null);
                }
            }
            String stringValue = tree.convertValueToText(valueText, sel, expanded, leaf, row, hasFocus);
            this.hasFocus = hasFocus;
            this.setText(stringValue);
            if(tColor != null) {
                this.setForeground(sel ? (tColor != newColor ? new Color(-tColor.getRed() + 255, -tColor.getGreen() + 255, -tColor.getBlue() + 255) : tColor.brighter()) : tColor);
            } else {
                this.setForeground(sel ? getTextSelectionColor() : getTextNonSelectionColor());
            }
            this.setEnabled(tree.isEnabled());
            this.setComponentOrientation(tree.getComponentOrientation());
            this.selected = sel;
            return this;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    private static final Logger LOG = Logger.getLogger(FileTree.class.getName());
}
