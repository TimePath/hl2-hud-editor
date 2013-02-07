package com.timepath.plaf.linux;

import java.awt.Color;
import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

@SuppressWarnings("serial")
public class TestMenuFix extends JFrame {

    public static void main(String[] args) {
        try {
            adjustLAF();
        } catch(ClassNotFoundException ex) {
            Logger.getLogger(TestMenuFix.class.getName()).log(Level.SEVERE, null, ex);
        } catch(InstantiationException ex) {
            Logger.getLogger(TestMenuFix.class.getName()).log(Level.SEVERE, null, ex);
        } catch(IllegalAccessException ex) {
            Logger.getLogger(TestMenuFix.class.getName()).log(Level.SEVERE, null, ex);
        } catch(UnsupportedLookAndFeelException ex) {
            Logger.getLogger(TestMenuFix.class.getName()).log(Level.SEVERE, null, ex);
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                TestMenuFix test = new TestMenuFix();
                test.setDefaultCloseOperation(EXIT_ON_CLOSE);
                test.setPreferredSize(new Dimension(400, 300));
                test.pack();
                test.setLocationRelativeTo(null);

                JMenuBar menuBar = new JMenuBar();
                JMenu menu1 = new JMenu("Menu 1");
                menu1.add(new JMenuItem("Item 1.1"));
                menu1.add(new JMenuItem("Item 1.2"));
                menu1.add(new JMenuItem("Item 1.3"));
                menuBar.add(menu1);
                JMenu menu2 = new JMenu("Menu 2");
                menu2.add(new JMenuItem("Item 2.1"));
                menu2.add(new JMenuItem("Item 2.2"));
                menu2.add(new JMenuItem("Item 2.3"));
                menuBar.add(menu2);
                test.setJMenuBar(menuBar);

                test.setVisible(true);
            }
        });
    }

    private static void adjustLAF() throws ClassNotFoundException,
                                           InstantiationException, IllegalAccessException,
                                           UnsupportedLookAndFeelException {
        for(LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if("Nimbus".equals(info.getName())) {

                // Working
                UIManager.put("control", Color.GREEN);

                // Not working
                UIManager.getLookAndFeelDefaults().put(
                    "MenuItem[Enabled].textForeground", Color.RED);

                // Set the look and feel
                UIManager.setLookAndFeel(info.getClassName());

                // Not working
                UIManager.put("control", Color.GREEN);

                // Working
                UIManager.getLookAndFeelDefaults().put(
                    "MenuItem[Enabled].textForeground", Color.RED);

                break;
            }
        }

    }

    private static final Logger LOG = Logger.getLogger(TestMenuFix.class.getName());
}