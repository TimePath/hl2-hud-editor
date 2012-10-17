package com.timepath.tf2.hudedit;

import com.timepath.tf2.hudedit.display.HudCanvas;
import com.timepath.tf2.hudedit.loaders.ResLoader;
import com.timepath.tf2.hudedit.properties.PropertiesTable;
import com.timepath.tf2.hudedit.util.Element;
import com.timepath.tf2.hudedit.util.Property;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
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
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import net.tomahawk.XFileDialog;

/**
 * Keep logic to a minimum, just interact and bridge components.
 *
 * Current bug: the file choose dialog on windows 'paints' over the frame.
 *
 * libs:
 * http://code.google.com/p/xfiledialog/ - windows "open folder" dialog
 * http://code.google.com/p/java-swing-ayatana/ - ubuntu global appmenu/hud support
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
        boolean metal = false;
        boolean nimbus = true; // takes precedence
        //<editor-fold defaultstate="collapsed" desc="Try and get nimbus look and feel, if it is installed.">
        try {
            if(nimbus) {
                for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } else {
                if(!metal) {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } else {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                }
            }
        } catch(Exception ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Display the editor">
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                EditorFrame frame = new EditorFrame();
                frame.start();
            }

        });
        //</editor-fold>
    }

    //<editor-fold defaultstate="collapsed" desc="OS specific code">
    private final static OS os;

    private final static int shortcutKey;
    private JScrollPane canvasPane;

    private enum OS {

        Windows, Mac, Linux, Other

    }

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ex) {
            Logger.getLogger(EditorFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        String osVer = System.getProperty("os.name").toLowerCase();
        if(osVer.indexOf("windows") != -1) {
            os = OS.Windows;
        } else if(osVer.indexOf("mac") != -1 || osVer.indexOf("OS X") != -1) {
            os = OS.Mac;
        } else if(osVer.indexOf("linux") != -1) {
            os = OS.Linux;
//            if ("GTK look and feel".equals(UIManager.getLookAndFeel().getName())) {
//                UIManager.put("FileChooserUI", "eu.kostia.gtkjfilechooser.ui.GtkFileChooserUI");
//            }
        } else {
            os = OS.Other;
            System.out.println("Unrecognised OS: " + osVer);
        }
        
        if(os == OS.Windows) {
            shortcutKey = ActionEvent.CTRL_MASK;
            XFileDialog.setTraceLevel(0);
        } else if(os == OS.Mac) {
            shortcutKey = ActionEvent.META_MASK;
            System.setProperty("apple.awt.showGrowBox", "true");
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
        this.setTitle("TimePath's WYSIWYG TF2 HUD Editor");
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

        });
        this.setMinimumSize(new Dimension(640, 480));
        DisplayMode d = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        this.setPreferredSize(new Dimension((int) (d.getWidth() / 1.5), (int) (d.getHeight() / 1.5)));
        this.setLocation((d.getWidth() / 2) - (this.getPreferredSize().width / 2), (d.getHeight() / 2) - (this.getPreferredSize().height / 2));
//        this.setLocationByPlatform(true);
//        this.setLocationRelativeTo(null);

        JScrollPane p = createCanvas();

        createMenu();

        JSplitPane browser = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        createTree(browser);
        createProperties(browser);

        browser.setResizeWeight(0.5);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, p, browser);
//        splitPane.setDividerLocation(f.getPreferredSize().width-350);
        splitPane.setResizeWeight(0.8);
        this.add(splitPane);

        this.pack();
        this.setFocusableWindowState(true);
    }

    public static HudCanvas canvas;

    private ResLoader resloader;

    private JTree fileSystem;

    private DefaultMutableTreeNode hudFilesRoot;

    private PropertiesTable propTable;

    public void start() {
        this.setVisible(true);
        this.createBufferStrategy(2);
    }

    private void createMenu() {
        final JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenuItem openItem = new JMenuItem("Open...", KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutKey));
        openItem.addActionListener(this);
        fileMenu.add(openItem);

        JMenuItem closeItem = new JMenuItem("Close HUD", KeyEvent.VK_C);
        closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcutKey));
        closeItem.addActionListener(this);
        fileMenu.add(closeItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, shortcutKey));
        exitItem.addActionListener(this);
        fileMenu.add(exitItem);

        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(editMenu);

        JMenuItem deleteItem = new JMenuItem("Delete", KeyEvent.VK_DELETE);
        deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteItem.addActionListener(this);
        editMenu.add(deleteItem);

        JMenuItem selectAllItem = new JMenuItem("Select All", KeyEvent.VK_A);
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, shortcutKey));
        selectAllItem.addActionListener(this);
        editMenu.add(selectAllItem);
        
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(viewMenu);

        JMenuItem resolutionItem = new JMenuItem("Change Resolution", KeyEvent.VK_R);
        resolutionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcutKey));
        resolutionItem.addActionListener(this);
        viewMenu.add(resolutionItem);
        
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);

        JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                String aboutText = "<html><h2>This is a WYSIWYG HUD Editor for TF2.</h2>";
                aboutText += "<p>You can graphically edit TF2 HUDs with it!<br>";
                aboutText += "<p>It was written by <a href=\"http://www.reddit.com/user/TimePath/\">TimePath</a></p>";
                aboutText += "<p>Please give feedback or suggestions on my Reddit profile</p>";
                aboutText += "</html>";
                final JEditorPane panel = new JEditorPane("text/html", aboutText);
                panel.setEditable(false);
                panel.setOpaque(false);
                panel.addHyperlinkListener(new HyperlinkListener() {

                    @Override
                    public void hyperlinkUpdate(HyperlinkEvent he) {
                        if (he.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                            try {
                                Desktop.getDesktop().browse(he.getURL().toURI()); // http://stackoverflow.com/questions/5116473/linux-command-to-open-url-in-default-browser
                            } catch(Exception e) {
//                                e.printStackTrace();
                            }
                        }
                    }
                    
                });
                JOptionPane.showMessageDialog(menuBar.getParent(), panel, "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        helpMenu.add(aboutItem);

        this.setJMenuBar(menuBar);
    }
    
//    private void selectSteamLocation() {
//        boolean installPathValid = false;
//            File steamFolder = new File("");
//        File installDir;
//            if (installDir != null && installDir.exists()) {
//                    steamFolder = installDir.getParentFile().getParentFile().getParentFile().getParentFile();
//            }
//            final JFileChooser chooser = new JFileChooser(steamFolder);
//            chooser.setDialogTitle("Select Steam\\ folder");
//            chooser.setToolTipText("Please select you Steam\\ folder! Not any subfolders of it.");
//            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
//            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//
//            final int returnVal = chooser.showOpenDialog(this);
//        File zipFile = null;
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                    final File steamappsFolder = new File(chooser.getSelectedFile(), "SteamApps");
//                    if (!steamappsFolder.exists()) {
//                            showErrorDialog("Invalid path to ...\\Steam\\SteamApps\\: " + steamappsFolder.getAbsolutePath(), "No SteamApps\\ Folder");
//                    }
//                    else if (!steamappsFolder.isDirectory()) {
//                            showErrorDialog("The entered path is not a folder: " + steamappsFolder.getAbsolutePath(), "This is not a Folder");
//                    }
//                    else {
//                            // Steam-User ausw�hlen lassen
//                            // DropDown erstellen
//                            final JComboBox dropDown = new JComboBox();
//                            final File[] userFolders = steamappsFolder.listFiles();
//                            for (int i = 0; i < userFolders.length; i++) {
//                                    if (userFolders[i].isDirectory() && !userFolders[i].getName().equalsIgnoreCase("common")
//                                                    && !userFolders[i].getName().equalsIgnoreCase("sourcemods")) {
//                                            // �berpr�fen, ob in dem User-Ordner ein tf2 Ordner
//                                            // vorhanden ist
//                                            final Collection<String> gameFolders = Arrays.asList(userFolders[i].list());
//                                            if (gameFolders.contains("team fortress 2")) {
//                                                    dropDown.addItem(userFolders[i].getName());
//                                            }
//                                    }
//                            }
//
//                            // �berpr�fen ob dropdown elemente hat und dialog anzeigen
//                            if (dropDown.getItemCount() > 0) {
//                                    final JPanel dialogPanel = new JPanel();
//                                    dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
//                                    dialogPanel.add(new JLabel("Please choose for which user you want to install the HUD"));
//                                    dialogPanel.add(dropDown);
//                                    JOptionPane.showMessageDialog(this, dialogPanel, "Select user", JOptionPane.QUESTION_MESSAGE);
//                            }
//                            else {
//                                    showErrorDialog("No users have TF2 installed!", "No TF2 found");
//                                    return;
//                            }
//
//                            installDir = new File(steamappsFolder, dropDown.getSelectedItem() + File.separator + "team fortress 2" + File.separator + "tf");
//                            if (installDir.isDirectory() && installDir.exists()) {
//                                    installPathValid = true;
//                                    steamInput.setText(installDir.getAbsolutePath());
//                                    try {
//                                            String zipFilePath = "";
//                                            if (zipFile != null && zipFileValid) {
//                                                    zipFilePath = zipFile.getAbsolutePath();
//                                            }
//                                            saveInstallPath(installDir.getAbsolutePath(), zipFilePath);
//                                    }
//                                    catch (final IOException e1) {
//                                            showErrorDialog(e1.getMessage(), "Could not save installpath");
//                                            e1.printStackTrace();
//                                    }
//                            }
//                            else {
//                                    showErrorDialog("This is not a valid install location for broeselhud", "No valid installpath");
//                            }
//                    }
//            }
//    }

    private JScrollPane createCanvas() {
        canvas = new HudCanvas();
        canvasPane = new JScrollPane(canvas);
//        p.getHorizontalScrollBar().setUnitIncrement(16);
//        p.getVerticalScrollBar().setUnitIncrement(16);
        canvasPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        canvasPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        return canvasPane;
    }
    
    class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
        
        private void setIcons(JTree tree, Icon ico) {
            if(tree.isEnabled()) {
                this.setIcon(ico);
            } else {
                this.setDisabledIcon(ico);
            }
        }
        
        JFileChooser iconFinder = new JFileChooser();
        
        /**
          * Configures the renderer based on the passed in components.
          * The value is set from messaging the tree with
          * <code>convertValueToText</code>, which ultimately invokes
          * <code>toString</code> on <code>value</code>.
          * The foreground color is set based on the selection and the icon
          * is set based on on leaf and expanded.
          */
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Object valueText = value;
            
            if(value instanceof DefaultMutableTreeNode) {
                Object nodeValue = ((DefaultMutableTreeNode) value).getUserObject();
                if(nodeValue instanceof String) {
                    setIcons(tree, UIManager.getIcon("FileView.computerIcon"));
                } else if(nodeValue instanceof File) { // this will either be an actual file on the system (directories included), or an element within a file
                    File f = ((File) nodeValue);
                    valueText = f.getName();
                    setIcons(tree, iconFinder.getIcon(f));
                } else if(nodeValue instanceof Element) {
                    Element e = (Element) nodeValue;
                    if(e.getProps().isEmpty() && leaf) { // If no properties, warn because irrelevant. Only care if leaves are empty
                        setIcons(tree, UIManager.getIcon("FileChooser.detailsViewIcon"));
                    } else {
                        setIcons(tree, UIManager.getIcon("FileChooser.listViewIcon"));
                    }
                } else {
                    if(nodeValue != null) {
                        System.out.println(nodeValue.getClass());
                    }
                    setIcons(tree, null);
                }
            }
            String stringValue = tree.convertValueToText(valueText, sel, expanded, leaf, row, hasFocus);
            this.hasFocus = hasFocus;
            this.setText(stringValue);
            this.setForeground(sel ? getTextSelectionColor() : getTextNonSelectionColor());
            this.setEnabled(tree.isEnabled());
            this.setComponentOrientation(tree.getComponentOrientation());
            this.selected = sel;
            return this;
        }
    }

    private void createTree(Container p) {
        hudFilesRoot = new DefaultMutableTreeNode(null);

        fileSystem = new JTree(hudFilesRoot);
        fileSystem.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        fileSystem.setCellRenderer(new CustomTreeCellRenderer());
        fileSystem.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultTableModel model = (DefaultTableModel) propTable.getModel();
                model.getDataVector().removeAllElements();
                propTable.scrollRectToVisible(new Rectangle(0, 0, 0, 0));

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileSystem.getLastSelectedPathComponent();
                if(node == null) {
                    return;
                }

                Object nodeInfo = node.getUserObject();
                if(nodeInfo instanceof Element) {
                    Element element = (Element) nodeInfo;
                    canvas.load(element);
                    if(element.getProps().isEmpty()) {
                        model.insertRow(0, new Object[] {"", "", ""});
                    } else {
                        element.validate2();
                        for(int i = 0; i < element.getProps().size(); i++) {
                            Property entry = element.getProps().get(i);
                            model.insertRow(model.getRowCount(), new Object[] {entry.getKey(), entry.getValue(), entry.getInfo()});
                        }
                    }
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
     * System.getProperty("user.home")
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
        } else
        if(os == OS.Mac) {
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
            FileDialog fd = new FileDialog(this, "Open HUD");
//            fd.setMultipleMode(false); // specific to java 7 - the default on anything lower
            fd.setVisible(true);
            selection = fd.getFile();
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "false");
//        } else
//        if(os == OS.Linux) {
////            FileDialog fd = new FileDialog(this, "Open HUD");
//////            fd.setMultipleMode(false); // specific to java 7 - the default on anything lower
////            fd.setVisible(true);
////            selection = fd.getFile();
        } else { // Fall back to swing
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

    private void closeHud() {
        canvas.removeAllElements();

        hudFilesRoot.removeAllChildren();
        hudFilesRoot.setUserObject(null);
        DefaultTreeModel model1 = (DefaultTreeModel) fileSystem.getModel();
        model1.reload();

        DefaultTableModel model2 = (DefaultTableModel) propTable.getModel();
        model2.setRowCount(0);
        propTable.repaint();
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
                JOptionPane.showMessageDialog(this, "Selection not valid. Please choose a folder containing \'resources\' or \'scripts\'.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

//            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
//
//                @Override
//                public Void doInBackground() {
//                    while(!isCancelled()) {
//                    }
//                    return null;
//                }
//
//                @Override
//                public void done() {
//                }
//
//                @Override
//                protected void process(List<Void> chunks) {
//                }
//
//            };
//            worker.execute();

            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            final long start = System.currentTimeMillis();

            resloader = new ResLoader(file.getPath());
            hudFilesRoot.setUserObject(file.getName());//new MyTreeObject(file));
            resloader.populate(hudFilesRoot);

            DefaultTreeModel model = (DefaultTreeModel) fileSystem.getModel();
            model.reload();
            
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    System.out.println(System.currentTimeMillis()-start);
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    System.out.println("loaded hud");
                }
            });
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if("Open...".equalsIgnoreCase(cmd)) {
            locateHudDirectory();
        } else if("Close HUD".equalsIgnoreCase(cmd)) {
            closeHud();
        } else if("Exit".equalsIgnoreCase(cmd)) {
            System.exit(0);
        } else if("Change Resolution".equalsIgnoreCase(cmd)) {
            changeResolution();
        } else if("Select All".equalsIgnoreCase(cmd)) {
            for(int i = 0; i < canvas.getElements().size(); i++) {
                canvas.select(canvas.getElements().get(i));
            }
        } else {
            System.out.println(e.getActionCommand());
        }
    }

    private void changeResolution() {
        final JOptionPane optionPane = new JOptionPane("Change resoluton to 1920 * 1080? (There will be a way to put actual numbers in here soon)", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

        final JDialog dialog = new JDialog(this, "Click a button", true);
        dialog.setContentPane(optionPane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
            }
        });
        optionPane.addPropertyChangeListener(
            new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent e) {
                    String prop = e.getPropertyName();

                    if(dialog.isVisible()
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

        int value = ((Integer) optionPane.getValue()).intValue();
        if(value == JOptionPane.YES_OPTION) {
            canvas.setPreferredSize(new Dimension(1920, 1080));
        } else if(value == JOptionPane.NO_OPTION) {

        }
    }

}