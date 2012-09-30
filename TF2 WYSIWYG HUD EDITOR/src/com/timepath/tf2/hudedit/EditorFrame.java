package com.timepath.tf2.hudedit;

import com.timepath.tf2.hudedit.loaders.ResLoader;
import com.timepath.tf2.hudedit.util.Element;
import com.timepath.tf2.hudedit.util.KVPair;
import com.timepath.tf2.hudedit.display.HudCanvas;
import com.timepath.tf2.hudedit.properties.PropertiesTable;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FileDialog;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import net.tomahawk.XFileDialog;

/**
 * Keep logic to a minimum, just interact and bridge components.
 *
 * Links of interest:
 *
 * UI principles:
 * http://developer.apple.com/legacy/mac/library/#technotes/tn/tn2042.html
 * http://developer.apple.com/library/mac/#technotes/tn2002/tn2110.html#//apple_ref/doc/uid/DTS10003202
 *
 * http://www.kdgregory.com/index.php?page=swing.async
 *
 * http://java.dzone.com/news/native-dialogs-swing-little
 * http://code.google.com/p/xfiledialog/
 * http://today.java.net/pub/a/today/2004/01/29/swing.html
 *
 * http://www.javaprogrammingforums.com/java-swing-tutorials/7944-how-use-jtree-create-file-system-viewer-tree.html
 *
 * http://www.horstmann.com/articles/Taming_the_GridBagLayout.html
 *
 * Reference editors:
 * https://developers.google.com/java-dev-tools/wbpro/
 * http://visualhud.pk69.com/
 * http://gamebanana.com/css/tools/4483
 * http://img13.imageshack.us/img13/210/hudmanagerss.png
 * http://plrf.org/superhudeditor/screens/0.3.0/superhudeditor-0.3.0-linux.jpg
 *
 * @author andrew
 */
@SuppressWarnings("serial")
public class EditorFrame extends JFrame implements ActionListener {

    public static void main(String... args) {
        boolean overrideSystemLAF = true;
        try {
            if(overrideSystemLAF) {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                EditorFrame e = new EditorFrame();
                e.start();
            }
        });
    }

    //<editor-fold defaultstate="collapsed" desc="OS specific code">
    private final static OS os;
    private final static int shortcutKey;

    private enum OS {
        Windows, Mac, Linux, Other
    }

    static {
        String osVer = System.getProperty("os.name").toLowerCase();
        if(osVer.indexOf("windows") != -1) {
            os = OS.Windows;
        } else if(osVer.indexOf("mac") != -1 || osVer.indexOf("OS X") != -1) {
            os = OS.Mac;
        } else if(osVer.indexOf("linux") != -1) {
            os = OS.Linux;
        } else {
            os = OS.Other;
            System.out.println("Unrecognised OS: " + osVer);
        }

        if(os == OS.Windows) {
            shortcutKey = ActionEvent.CTRL_MASK;
            XFileDialog.setTraceLevel(0);
        } else if(os == OS.Mac) {
            shortcutKey = ActionEvent.META_MASK;
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name application property", "TF2 HUD Editor");
        } else if(os == OS.Linux) {
            shortcutKey = ActionEvent.CTRL_MASK;
        } else {
            shortcutKey = ActionEvent.CTRL_MASK;
        }
    }
    //</editor-fold>

    public EditorFrame() {
        DisplayMode d = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
//        Dimension screenRes = Toolkit.getDefaultToolkit().getScreenSize();
//        long gcm = gcm(screenRes.width, screenRes.height);
//        System.out.println((screenRes.width / gcm) + ":" + (screenRes.height / gcm));

        this.setTitle("TimePath's WYSIWYG TF2 HUD Editor");
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        this.setMinimumSize(new Dimension(600, 400));
        this.setPreferredSize(new Dimension((int)(d.getWidth() / 1.5), (int)(d.getHeight() / 1.5)));
        this.setLocationRelativeTo(null);

        createMenu();

        JSplitPane browser = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        createTree(browser);
        createProperties(browser);

        browser.setResizeWeight(0.5);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createCanvas(), browser);
//        splitPane.setDividerLocation(f.getPreferredSize().width-350);
        splitPane.setResizeWeight(0.8);
        this.add(splitPane);

        this.pack();
    }

    /**
     * Finds the greatest common multiple
     * @param a
     * @param b
     * @return
     */
    public static long gcm(long a, long b) {
        return b == 0 ? a : gcm(b, a % b);
    }

    public HudCanvas canvas;
    private ResLoader resloader;

    private JTree fileSystem;

    private DefaultMutableTreeNode hudFilesRoot;
    private PropertiesTable propTable;

    public void start() {
        this.setVisible(true);
        this.createBufferStrategy(2);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.getAccessibleContext().setAccessibleDescription("The only menu in this program that has menu items");
        menuBar.add(fileMenu);

        JMenuItem openItem = new JMenuItem("Open...", KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutKey));
        openItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
        openItem.addActionListener(this);
        fileMenu.add(openItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
        exitItem.addActionListener(this);
        fileMenu.add(exitItem);



        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.getAccessibleContext().setAccessibleDescription("The only menu in this program that has menu items");
        menuBar.add(editMenu);

        JMenuItem selectAllItem = new JMenuItem("Select All", KeyEvent.VK_A);
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, shortcutKey));
        selectAllItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
        selectAllItem.addActionListener(this);
        editMenu.add(selectAllItem);

        JMenuItem resolutionItem = new JMenuItem("Change Resolution", KeyEvent.VK_R);
        resolutionItem.setEnabled(false);
        resolutionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcutKey));
        resolutionItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
        resolutionItem.addActionListener(this);
        editMenu.add(resolutionItem);

        this.setJMenuBar(menuBar);
    }

    private JScrollPane createCanvas() {
        canvas = new HudCanvas();
        canvas.setPreferredSize(new Dimension(800, 600));
        JScrollPane p = new JScrollPane(canvas);
        p.getHorizontalScrollBar().setUnitIncrement(16);
        p.getVerticalScrollBar().setUnitIncrement(16);
        p.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        p.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return p;
    }

    private void createTree(Container p) {
        hudFilesRoot = new DefaultMutableTreeNode("HUD");

        fileSystem = new JTree(hudFilesRoot);
        fileSystem.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        fileSystem.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultTableModel model = (DefaultTableModel) propTable.getModel();
                model.getDataVector().removeAllElements();
                propTable.scrollRectToVisible(new Rectangle(0, 0, 0, 0));

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileSystem.getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }

                Object nodeInfo = node.getUserObject();
                if(nodeInfo instanceof Element) {
                    Element element = (Element) nodeInfo;

                    for(Map.Entry<KVPair<String,String>,String> entry : element.getProps().entrySet()) {
                        model.insertRow(model.getRowCount(), new Object[] {entry.getKey().getKey(), entry.getKey().getValue(), entry.getValue()});
                    }

                    canvas.load(element);

//                    ColumnsAutoSizer.sizeColumnsToFit(propTable);
//
//                    for(int i = 0; i < propTable.getColumnCount(); i++) {
//                        propTable.getColumnModel().getColumn(i).setMinWidth(propTable.getColumnModel().getColumn(i).getMaxWidth());
//                    }
//
//                    propTable.getColumnModel().getColumn(propTable.getColumnCount() - 1).setMinWidth(1000);
//                    propTable.getColumnModel().getColumn(propTable.getColumnCount() - 1).setMaxWidth(1000);

                }
            }
        });

        JScrollPane scrollFileSystem = new JScrollPane(fileSystem);
        scrollFileSystem.setPreferredSize(new Dimension(400, 400));

        p.add(scrollFileSystem);
    }

    private void createProperties(Container p) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Key");
        model.addColumn("Value");
        model.addColumn("Info");

        propTable = new PropertiesTable(model);
        propTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propTable.setColumnSelectionAllowed(false);
        propTable.setRowSelectionAllowed(true);
        propTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPropTable = new JScrollPane(propTable);
        scrollPropTable.setPreferredSize(new Dimension(400, 400));

        p.add(scrollPropTable);
    }

    /**
     * Start in the home directory
     * linux = ~
     * windows = %userprofile%
     * mac = ?
     */
    private void locateHudDirectory() {
        String selection = null;
        if(os == OS.Windows) {
            XFileDialog fd = new XFileDialog(EditorFrame.this);
            fd.setTitle("Open HUD");
            selection = fd.getFolder();
            fd.dispose();
        } else if(os == OS.Mac) {
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
            FileDialog fd = new FileDialog(this, "Open HUD");
//            fd.setMultipleMode(false); // specific to java 7 - the default on anything lower
            fd.setVisible(true);
            selection = fd.getFile();
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "false");
//        } else if(os == OS.Linux) {
//            FileDialog fd = new FileDialog(this, "Open HUD");
//            fd.setMultipleMode(false); // specific to java 7 - the default on anything lower
//            fd.setVisible(true);
//            selection = fd.getFile();
        } else { // Fallback to swing
            JFileChooser fd = new JFileChooser();
            fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if(fd.showOpenDialog(EditorFrame.this) == JFileChooser.APPROVE_OPTION) {
                selection = fd.getSelectedFile().getPath();
            }
        }

        if(selection != null) {
            final File f = new File(selection);
            new Thread() {
                @Override
                public void run() {
                    loadHud(f);
                }
            }.start();
        } else {
            // Throw error or load archive
        }
    }

    private void loadHud(final File file) {
        System.out.println("You have selected: " + file);

        if(file.isDirectory()) {
            File[] folders = file.listFiles();
            boolean valid = false;
            for(int i = 0; i < folders.length; i++) {
                if(folders[i].isDirectory() && ("resource".equalsIgnoreCase(folders[i].getName()) || "scripts".equalsIgnoreCase(folders[i].getName()))) {
                    valid = true;
                    break;
                }
            }
            if(!valid) {
                // throw error
                return;
            }

            SwingWorker worker = new SwingWorker<Void, Void>() {
                @Override
                public Void doInBackground() {
                    while(!isCancelled()) {

                    }
                    return null;
                }

                @Override
                public void done() {

                }

                @Override
                protected void process(List<Void> chunks) {

                }
            };

            worker.execute();

            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            resloader = new ResLoader(file.getPath());
            hudFilesRoot.setUserObject(file.getName());//new MyTreeObject(file));
            resloader.populate(hudFilesRoot);

            DefaultTreeModel model = (DefaultTreeModel) fileSystem.getModel();
            model.reload();

            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if("Open...".equalsIgnoreCase(cmd)) {
            locateHudDirectory();
        } else if("Exit".equalsIgnoreCase(cmd)) {
            System.exit(0);
        } else if("Change Resolution".equalsIgnoreCase(cmd)) {
            changeResolution();
        } else if("Select All".equalsIgnoreCase(cmd)) {
            for(int i = 0; i < canvas.getElements().size(); i++)
            canvas.select(canvas.getElements().get(i));
        } else {
            System.out.println(e.getActionCommand());
        }
    }

    private void changeResolution() {
        final JOptionPane optionPane = new JOptionPane("Please enter a valid resolution", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

        final JDialog dialog = new JDialog(this, "Click a button", true);
        dialog.setContentPane(optionPane);
        dialog.setDefaultCloseOperation(
        JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
        public void windowClosing(WindowEvent we) {
        System.out.println("Thwarted user attempt to close window.");
        }
        });
        optionPane.addPropertyChangeListener(
        new PropertyChangeListener() {
            @Override
        public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (dialog.isVisible()
        && (e.getSource() == optionPane)
        && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
        //If you were going to check something
        //before closing the window, you'd do
        //it here.
        dialog.setVisible(false);
        }
        }
        });
        dialog.pack();
        dialog.setVisible(true);

        int value = ((Integer)optionPane.getValue()).intValue();
        if (value == JOptionPane.YES_OPTION) {
        System.out.println("Good.");
        } else if (value == JOptionPane.NO_OPTION) {
        System.out.println("Try using the window decorations "
        + "to close the non-auto-closing dialog. "
        + "You can't!");
        }

        canvas.setPreferredSize(new Dimension(1920, 1080));
    }

}