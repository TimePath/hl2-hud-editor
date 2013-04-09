package com.timepath.tf2.hudeditor;

import com.timepath.Utils;
import com.timepath.plaf.OS;
import com.timepath.plaf.OS.WindowToolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;

/**
 * Link dump:
 * https://docs.google.com/document/d/19jk3L-kyduz_AvTOhMXk4agh5gUYM9gWQCHafbMl3wY/edit
 *
 * @author timepath
 */
public class Main {

    public static final ResourceBundle strings = ResourceBundle.getBundle("com/timepath/tf2/hudeditor/resources/lang");

    public static final String appName = "TF2 HUD Editor";

    /**
     * Used for storing preferences. Do not localize
     * The window class on Linux systems
     * The app name on Mac systems
     */
    public static final String projectName = "tf2-hud-editor"; // in xfce, window grouping show this, unfortunately

    public static final Preferences prefs = Preferences.userRoot().node(projectName);

    public static final String myVer = Main.class.getPackage().getImplementationVersion();

    public static final String logFile;

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    static {
        //<editor-fold defaultstate="collapsed" desc="Debugging">
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable thrwbl) {
                Logger.getLogger(thread.getName()).log(Level.SEVERE, "Uncaught Exception", thrwbl);
            }
        });

        //<editor-fold defaultstate="collapsed" desc="Logging">
        Logger.getLogger("com.timepath").setLevel(Level.ALL);

        logFile = Utils.workingDirectory(Main.class) + "logs/" + System.currentTimeMillis() / 1000 + "_log.txt";
        try {
            new File(logFile).getParentFile().mkdirs();
            FileHandler fh = new FileHandler(logFile, 0, 1, false);
            fh.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            Logger.getLogger("").addHandler(fh);
            LOG.log(Level.INFO, "Logging to {0}", logFile);
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch(SecurityException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        com.timepath.plaf.x.filechooser.XFileDialogFileChooser.setTraceLevel(0);
        //</editor-fold>

        WindowToolkit.setWindowClass(projectName); // Wrapper.class.getName().replaceAll("\\.", "-");

        if(OS.isMac()) {
            System.setProperty("apple.awt.brushMetalLook", "false");
            System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
            System.setProperty("apple.awt.showGrowBox", "true");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.macos.smallTabs", "true");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("com.apple.mrj.application.live-resize", "true");
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Entry point">
    public static void main(String... args) {
        LOG.log(Level.INFO, "Executing from {0}", Utils.workingDirectory(EditorFrame.class));
        LOG.log(Level.INFO, "Args = {0}", Arrays.toString(args));
        LOG.log(Level.CONFIG, "Env = {0}", System.getenv().toString());
        LOG.log(Level.CONFIG, "Properties = {0}", System.getProperties().toString());


        for(int i = 0; i < args.length; i++) {
            if(args[i].equalsIgnoreCase("-u")) {
                try {
                    File destFile = new File(args[i + 1]);
                    LOG.info("Updating " + destFile);
                    File sourceFile = new File(Utils.workingDirectory(EditorFrame.class));
                    if(!destFile.exists()) {
                        destFile.createNewFile();
                    }

                    FileChannel source = null;
                    FileChannel destination = null;
                    try {
                        source = new RandomAccessFile(sourceFile, "rw").getChannel();
                        destination = new RandomAccessFile(destFile, "rw").getChannel();

                        long position = 0;
                        long count = source.size();

                        source.transferTo(position, count, destination);
                    } finally {
                        if(source != null) {
                            source.close();
                        }
                        if(destination != null) {
                            destination.close();
                        }
                    }
                    final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                    final ArrayList<String> cmd = new ArrayList<String>();
                    cmd.add(javaBin);
                    cmd.add("-jar");
                    cmd.add(destFile.getPath());
                    cmd.add("updated");
                    // TODO: carry other args
                    String[] exec = new String[cmd.size()];
                    cmd.toArray(exec);
                    final ProcessBuilder process = new ProcessBuilder(exec);
                    process.start();
                    System.exit(0);
                } catch(IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        int port = prefs.getInt("port", -1);
        if(port != -1) { // May not have been removed on shutdown
            LOG.info("Checking for daemon...");
            if(!startClient(port, args)) {
                LOG.info("Daemon not running, starting...");
                for(;;) {
                    if(startServer(port)) {
                        break;
                    } else if(startClient(port, args)) {
                        return;
                    }
                }
            }
        }
        start(args);
    }

    /**
     * Attempts to listen on the specified port
     *
     * @param port the port to listen on
     *
     * @return true if a server was started
     */
    private static boolean startServer(int port) {
        try {
            final ServerSocket sock = new ServerSocket(port, 0, InetAddress.getByName(null)); // cannot use java7 InetAddress.getLoopbackAddress(). On windows, this prevents firewall warnings. It's also good for security in general
            int truePort = sock.getLocalPort();
            prefs.putInt("port", truePort);
            prefs.flush();

            LOG.log(Level.INFO, "Listening on port {0}", truePort);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    LOG.info("Server shutting down...");
                    prefs.remove("port");
                    try {
                        prefs.flush();
                    } catch(BackingStoreException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.WARNING, null, ex);
                    }
                }
            });

            Thread server = new Thread(new Runnable() {
                public void run() {
                    while(!sock.isClosed()) {
                        try {
                            Socket client = sock.accept();
                            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

                            String cVer = in.readLine();
                            LOG.log(Level.INFO, "client {0} vs host {1}", new Object[]{cVer, myVer});
                            String request = "-noupdate " + in.readLine();
                            LOG.log(Level.INFO, "Request: {0}", request);
                            out.println(myVer);

                            if(cVer.equals("null") || (myVer != null && cVer.compareTo(myVer) > 0)) {
                                LOG.info("Surrendering control to other process");
                                out.flush();
                                sock.close();
                            } else {
                                start(request.split(" "));
                            }
                        } catch(Exception ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }
                    LOG.info("Exiting...");
                    System.exit(0);
                }
            }, "Process Listener");
            server.setDaemon(!OS.isMac()); // non-daemon threads work in the background. Stick around if on a mac until manually terminated
            //            server.setDaemon(false);
            server.start();
        } catch(BindException ex) {
            return false;
        } catch(Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     *
     * @param port
     * @param args
     *
     * @return true if connected
     */
    private static boolean startClient(int port, String... args) {
        try {
            Socket client = new Socket(InetAddress.getByName(null), port);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            out.println(myVer);
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }
            out.println(sb.toString());
            String sVer = in.readLine();
            if(myVer == null || (!sVer.equals("null") && sVer.compareTo(myVer) > 0)) {
                return false;
            } else {
                return true;
            }
        } catch(SocketException ex) {
        } catch(IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private static void start(String... args) {
        boolean flag = false;
        for(int i = 0; i < args.length; i++) {
            String cmd = args[i].toLowerCase();
            if("-noupdate".equals(cmd)) {
                flag = true;
                break;
            }
        }

        final boolean autoCheck = !flag;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                EditorFrame frame = new EditorFrame();
                frame.autoCheck = autoCheck;
                frame.setVisible(true);
            }
        });
    }
    //</editor-fold>

    /**
     *
     * 'return Main.strings.containsKey(key) ? Main.strings.getString(key) : key' is
     * unavailable prior to 1.6
     *
     * @param key
     * @param fallback
     *
     * @return
     */
    public static String getString(String key, String fallback) {
        return Collections.list(Main.strings.getKeys()).contains(key) ? Main.strings.getString(key) : fallback;
    }

    public static String getString(String key) {
        return getString(key, key);
    }
}
