package com.timepath.tf2.hudedit.loaders;

import com.timepath.tf2.hudedit.util.Element;
import com.timepath.tf2.hudedit.properties.HudFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * TODO: everything above the root node is not read into anything.
 *
 * @author andrew
 */
public class ResLoader {

    private String hudFolder;
//    private static String[] platforms = {"WIN32", "X360", "OSX"};

    public ResLoader(String hudFolder) {
        this.hudFolder = hudFolder;
    }

    public void populate(DefaultMutableTreeNode top) {
        processPopulate(new File(hudFolder), -1, top);
    }

    public void analyze(String fileName, DefaultMutableTreeNode top) {
        if(new File(fileName).isDirectory()) {
            return;
        }
        Scanner s = null;
        try {
            s = new Scanner(new BufferedReader(new FileReader(fileName)));
            processAnalyze(s, top);
        } catch(FileNotFoundException ex) {
            Logger.getLogger(ResLoader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(s != null) {
                s.close();
            }
        }
    }

    /**
     * @todo Sort alphabetically and by directory. directories first, files second
     * @param f
     * @param depth recursive: -1 = infinite, 0 = nothing, 1 = immediate
     * @param top
     */
    private void processPopulate(File f, int depth, DefaultMutableTreeNode top) {
        if(depth == 0) {
            return;
        }
        File[] fileList = f.listFiles();
        Arrays.sort(fileList, dirAlphaComparator);

        for(int i = 0; i < fileList.length; i++) {
            boolean isDir = fileList[i].isDirectory();
            DefaultMutableTreeNode child = new DefaultMutableTreeNode();
            child.setUserObject(new HudFile(fileList[i]));
            if(isDir) {
                processPopulate(fileList[i], depth - 1, child);
                top.add(child);
            } else if(fileList[i].getName().endsWith(".res")) {
                analyze(fileList[i].getPath(), child);
                top.add(child);
            }
        }
    }

    private void processAnalyze(Scanner scanner, DefaultMutableTreeNode parent) {
        while(scanner.hasNext()) {
            String line = scanner.nextLine().trim();
            String key = line.split("[ \t\r\n]+")[0]; // is \r\n needed?
            String val = line.substring(key.length()).trim();
            String info = null;

            if(line.equals("}")) {
                break;
            }

            if(!(line.equals("") || key.equals("{"))) {
                if(val.contains("//")) {
                    int idx = val.indexOf("//");
                    info = val.substring(idx).trim();
                    val = val.substring(0, idx).trim();
                }
                if(val.contains("[")) {
                    int idx = val.indexOf("[");
                    info = val.substring(idx).trim();
                    val = val.substring(0, idx).trim();
                }

                if(line.startsWith("#")) {
                    key = "#";
                    val = line.substring(line.indexOf("#") + 1).trim();
                    info = "";
                } else if(line.startsWith("//")) {
                    key = "//";
                    val = line.substring(line.indexOf("//") + 2).trim();
                    info = "";
                } else if(val.equals("")) { // good assumption
                    val = "{";
                }


                if(val.equals("{")) { // subNode
                    Element childElement = new Element(key, info);

                    Object obj = parent.getUserObject();
                    if(obj instanceof Element) {
                        Element e = (Element) obj;
                        e.addElement(childElement);
//                        childElement.setParent(e);
                    }

                    DefaultMutableTreeNode child = new DefaultMutableTreeNode();
                    child.setUserObject(childElement);
                    parent.add(child);

                    processAnalyze(scanner, child);
                } else { // properties
                    Object obj = parent.getUserObject();
                    if(obj instanceof Element) {
                        Element e = (Element) obj;
                        e.addProp(key, val, info);
                    }
                }
            }
        }
    }

    private DirAlphaComparator dirAlphaComparator = new DirAlphaComparator();

    private class DirAlphaComparator implements Comparator<File> {

        // Comparator interface requires defining compare method.
        @Override
        public int compare(File filea, File fileb) {
            //... Sort directories before files,
            //    otherwise alphabetical ignoring case.
            if(filea.isDirectory() && !fileb.isDirectory()) {
                return -1;

            } else if(!filea.isDirectory() && fileb.isDirectory()) {
                return 1;

            } else {
                return filea.getName().compareToIgnoreCase(fileb.getName());
            }
        }

    }

}