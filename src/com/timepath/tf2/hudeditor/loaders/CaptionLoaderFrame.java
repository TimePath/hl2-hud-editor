package com.timepath.tf2.hudeditor.loaders;

import com.timepath.tf2.hudeditor.Utils;
import com.timepath.tf2.hudeditor.util.DataUtils;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author TimePath
 */
public class CaptionLoaderFrame extends javax.swing.JFrame {
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Caption Reader");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "CRC32", "Key", "Value"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane1.setViewportView(jTable1);
        jTable1.getColumnModel().getColumn(0).setMinWidth(85);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(85);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(85);
        jTable1.getColumnModel().getColumn(1).setMinWidth(160);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(160);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(160);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("CRC32"));

        jTextField4.setEditable(false);
        jTextField4.setText("0");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField4)
                    .addComponent(jTextField3))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 14, Short.MAX_VALUE))
        );

        jMenu1.setText("File");

        jMenuItem1.setText("Open");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadCaptions(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem3.setText("Import");
        jMenuItem3.setEnabled(false);
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importCaptions(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuItem2.setText("Save");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveCaptions(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loadCaptions(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadCaptions
        JFileChooser fc = new JFileChooser();
        
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return (file.getName().startsWith("closecaption_") && (file.getName().endsWith(".dat"))) || file.isDirectory();
            }
            public String getDescription() {
                return "VCCD Files (.dat)";
            }
        };
        fc.setFileFilter(filter);
        
        int returnVal = fc.showOpenDialog(this);

        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            ArrayList<Entry> entries = cl.loadFile(file.getAbsolutePath().toString());
            logger.log(Level.INFO, "Entries: {0}", entries.size());
            
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            for(int i = model.getRowCount() - 1; i >= 0 ; i--) {
                model.removeRow(i);
            }
            for(int i = 0; i < entries.size(); i++) {
                model.addRow(new Object[]{hexFormat(entries.get(i).getKey()), attemptDecode(entries.get(i).getKey()), entries.get(i).getValue()});
            }
        }
    }//GEN-LAST:event_loadCaptions

    private void saveCaptions(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveCaptions
        JFileChooser fc = new JFileChooser();
        
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return (file.getName().startsWith("closecaption_") && (file.getName().endsWith(".dat"))) || file.isDirectory();
            }
            public String getDescription() {
                return "VCCD Files (.dat)";
            }
        };
        fc.setFileFilter(filter);
        
        int returnVal = fc.showSaveDialog(this);

        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            
            ArrayList<Entry> entries = new ArrayList<Entry>();
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            for(int i = model.getRowCount() - 1; i >= 0 ; i--) {
                Entry e = new Entry();
                e.setKey(Long.parseLong(model.getValueAt(i, 0).toString().toLowerCase(), 16));
                e.setValue(model.getValueAt(i, 2).toString());
                entries.add(e);
            }
            cl.saveFile(file.getAbsolutePath().toString(), entries);
        }
    }//GEN-LAST:event_saveCaptions

    private void importCaptions(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importCaptions
        JFileChooser fc = new JFileChooser();
        
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return (file.getName().startsWith("closecaption_") && (file.getName().endsWith(".txt"))) || file.isDirectory();
            }
            public String getDescription() {
                return "VCCD Source Files (.txt)";
            }
        };
        fc.setFileFilter(filter);
        
        int returnVal = fc.showOpenDialog(this);

        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            ArrayList<Entry> entries = cl.importFile(file.getAbsolutePath().toString());
            logger.log(Level.INFO, "Entries: {0}", entries.size());
            
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            for(int i = model.getRowCount() - 1; i >= 0 ; i--) {
                model.removeRow(i);
            }
            for(int i = 0; i < entries.size(); i++) {
                model.addRow(new Object[]{hexFormat(entries.get(i).getKey()), attemptDecode(entries.get(i).getKey()), entries.get(i).getValue()});
            }
        }
    }//GEN-LAST:event_importCaptions
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables

    //<editor-fold defaultstate="collapsed" desc="Entry point">
    /**
     * Creates new form CaptionLoaderFrame
     */
    public CaptionLoaderFrame() {
        initComponents();
        
        jTextField3.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                update();
            }
            public void removeUpdate(DocumentEvent e) {
                update();
            }
            public void insertUpdate(DocumentEvent e) {
                update();
            }
            
            public void update() {
                jTextField4.setText(hexFormat(takeCRC32(jTextField3.getText())));
            }
        });
        
        hashmap = generateHash();
        cl = new CaptionLoader();
    }
    
    private static final Logger logger = Logger.getLogger(CaptionLoader.class.getName());
    
    /**
     * @param args the command line arguments
     */
    public static void main(String... args) {
        final JFrame f = new JFrame("Loading caption reader...");
        JProgressBar pb = new JProgressBar();
        pb.setIndeterminate(true);
        f.add(pb);
        f.setMinimumSize(new Dimension(300, 50));
        f.setLocationRelativeTo(null);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                f.setVisible(true);
            }
        }).start();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                CaptionLoaderFrame c = new CaptionLoaderFrame();
                c.setLocationRelativeTo(null);
                c.setVisible(true);
                f.dispose();
            }
        }).start();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Hash codes">
    private HashMap<Integer, String> hashmap;
    
    private HashMap<Integer, String> generateHash() {
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        logger.info("Generating hash codes ...");
        try {
            GcfFile gcf = GcfFile.load(new File(Utils.locateSteamAppsDirectory() + "/games/team fortress 2 content.gcf"));
            
            CRC32 crc = new CRC32();
            
            String all = new String(gcf.ls);
            String[] ls = all.split("\0");
            for(int i = 0; i < ls.length; i++) {
                int end = ls[i].length();
                int ext = ls[i].lastIndexOf(".");
                if(ext != -1) {
                    end = ext;
                }
                String sp = ls[i].substring(0, end);
                if(ls[i].toLowerCase().endsWith(".wav") || ls[i].toLowerCase().endsWith(".mp3")// ||
//                    ls[i].toLowerCase().endsWith(".vcd") || ls[i].toLowerCase().endsWith(".bsp") ||
//                    ls[i].toLowerCase().endsWith(".mp3") || ls[i].toLowerCase().endsWith(".bat") ||
//                    ls[i].toLowerCase().endsWith(".doc") || ls[i].toLowerCase().endsWith(".raw") ||
//                    ls[i].toLowerCase().endsWith(".pcf") || ls[i].toLowerCase().endsWith(".cfg") ||
//                    ls[i].toLowerCase().endsWith(".vbsp") || ls[i].toLowerCase().endsWith(".inf") ||
//                    ls[i].toLowerCase().endsWith(".rad") || ls[i].toLowerCase().endsWith(".vdf") ||
//                    ls[i].toLowerCase().endsWith(".ctx") || ls[i].toLowerCase().endsWith(".vdf") ||
//                    ls[i].toLowerCase().endsWith(".lst") || ls[i].toLowerCase().endsWith(".res") ||
//                    ls[i].toLowerCase().endsWith(".pop") || ls[i].toLowerCase().endsWith(".dll") ||
//                    ls[i].toLowerCase().endsWith(".dylib") || ls[i].toLowerCase().endsWith(".so") ||
//                    ls[i].toLowerCase().endsWith(".scr") || ls[i].toLowerCase().endsWith(".rc") ||
//                    ls[i].toLowerCase().endsWith(".vfe") || ls[i].toLowerCase().endsWith(".pre") ||
//                    ls[i].toLowerCase().endsWith(".cache") || ls[i].toLowerCase().endsWith(".nav") ||
//                    ls[i].toLowerCase().endsWith(".lmp") || ls[i].toLowerCase().endsWith(".bik") ||
//                    ls[i].toLowerCase().endsWith(".mov") || ls[i].toLowerCase().endsWith(".snd") ||
//                    ls[i].toLowerCase().endsWith(".midi") || ls[i].toLowerCase().endsWith(".png") ||
//                    ls[i].toLowerCase().endsWith(".ttf") || ls[i].toLowerCase().endsWith(".ico") ||
//                    ls[i].toLowerCase().endsWith(".dat") || ls[i].toLowerCase().endsWith(".pl") ||
//                    ls[i].toLowerCase().endsWith(".ain") || ls[i].toLowerCase().endsWith(".db") ||
//                    ls[i].toLowerCase().endsWith(".py") || ls[i].toLowerCase().endsWith(".xsc") ||
//                    ls[i].toLowerCase().endsWith(".bmp") || ls[i].toLowerCase().endsWith(".icns") ||
//                    ls[i].toLowerCase().endsWith(".txt") || ls[i].toLowerCase().endsWith(".manifest")
                        ) {
                    String str = sp;
                    if(str.split("_").length == 2) {
                        str = str.replaceAll("_", ".").replaceAll(" ", "");// + "\0";
                    }
//                    System.out.println(str);
                    crc.update(str.toLowerCase().getBytes());
                    map.put((int)crc.getValue(), str); // HASH >
//                    logger.log(Level.INFO, "{0} > {1}", new Object[]{crc.getValue(), str});
                    crc.reset();
                } else {
//                    logger.info(ls[i]);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CaptionLoader.class.getName()).log(Level.WARNING, "Error generating hash codes", ex);
        }
        return map;
    }
    
    public static int takeCRC32(String in) {
        CRC32 crc = new CRC32();
        crc.update(in.toLowerCase().getBytes());
        return (int)crc.getValue();
    }
    
    public static String hexFormat(int in) {
        return Integer.toHexString(in).toUpperCase();
    }
    
    private String attemptDecode(int hash) {
        if(!hashmap.containsKey(hash)) {
//            logger.log(Level.INFO, "hashmap does not contain {0}", hash);
            return null;
        }
        return hashmap.get(hash);
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="CaptionWorker">
    private CaptionLoader cl;
    
    /**
     * Used for writing captions
     * @param curr
     * @param round
     * @return 
     */
    private static int alignValue(double curr, double round) {
        return (int)(Math.ceil(curr/round) * round);
    }
    
    private static String captionIdentifier = "VCCD";
    
    private static int captionVer = 1;
    
    private class CaptionLoader {
        
        String currentFile;
        
        public CaptionLoader() {
            
        }
        
        public ArrayList<Entry> loadFile(String file) {
            if(file == null) {
                return null;
            }
            this.currentFile = file;
            ArrayList<Entry> list = new ArrayList<Entry>();
            try {
                RandomAccessFile rf = new RandomAccessFile(file, "r");
                String magic = new String(new byte[]{DataUtils.readChar(rf), DataUtils.readChar(rf), DataUtils.readChar(rf),DataUtils.readChar(rf)});
                if(!magic.equals(captionIdentifier)) {
                    logger.severe("Header mismatch");
                }
                int ver = DataUtils.readLEInt(rf);
                int blocks = DataUtils.readLEInt(rf);
                int blockSize = DataUtils.readLEInt(rf);
                int directorySize = DataUtils.readLEInt(rf);
                int dataOffset = DataUtils.readLEInt(rf);
                logger.log(Level.INFO, "Header: {0}, Version: {1}, Blocks: {2}, BlockSize: {3}, DirectorySize: {4}, DataOffset: {5}", new Object[]{magic, ver, blocks, blockSize, directorySize, dataOffset});
                
                Entry[] entries = new Entry[directorySize];
                for(int i = 0; i < directorySize; i++) {
                    Entry e = new Entry();
                    e.setKey(DataUtils.readULong(rf));
                    e.setBlock(DataUtils.readLEInt(rf));
                    e.setOffset(DataUtils.readUShort(rf));
                    e.setLength(DataUtils.readUShort(rf));
                    entries[i] = e;
                }
                rf.seek(dataOffset);
                for(int i = 0; i < directorySize; i++) {
                    rf.seek(dataOffset + (entries[i].block * blockSize) + entries[i].offset);
                    StringBuilder sb = new StringBuilder((entries[i].length / 2) - 1);
                    for(int x = 0; x < (entries[i].length / 2) - 1; x++) {
                        sb.append(DataUtils.readUTFChar(rf));
                    }
                    rf.skipBytes(2);
                    entries[i].setValue(sb.toString());
                    list.add(entries[i]);
                }
                rf.close(); // The rest of the file is garbage
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            saveFile(file + ".test", list); // debugging
            return list;
        }
        
        public void saveFile(String file, ArrayList<Entry> list) {
            if(file == null) {
                return;
            }
            try {
                Entry[] entries = new Entry[list.size()];
                list.toArray(entries);
//                Arrays.sort(entries);
//                Collections.sort(list);
                
                int directorySize = entries.length;
                int blockSize = 8192;
                int length = 0;
                int blocks = 1;
                for(int i = 0; i < directorySize; i++) {
                    int eval = length + entries[i].getLength();
                    if(eval > blockSize) {
                        blocks++;
                        length = entries[i].getLength();
                    } else {
                        length = eval;
                    }
                }
                
                System.out.println("Blocks: " + blocks);
                
                int dataOffset = (int) alignValue((6 * 4) + (directorySize * 12), 512);
                
                RandomAccessFile rf = new RandomAccessFile(file, "rw");
                rf.write(captionIdentifier.getBytes()); // Big endian
                DataUtils.writeLEInt(rf, 1);
                DataUtils.writeLEInt(rf, blocks);
                DataUtils.writeLEInt(rf, blockSize);
                DataUtils.writeLEInt(rf, directorySize);
                DataUtils.writeLEInt(rf, dataOffset);
                
                int currentBlock = 0;
                int firstInBlock = 0;
                for(int i = 0; i < directorySize; i++) {
                    Entry e = entries[i];
                    e.setBlock(0);
                    e.setOffset(0);
                    int offset = 0;
                    for(int j = firstInBlock; j < i; j++) {
                        offset += e.length;
                    }
                    if((offset + e.getLength()) > ((currentBlock + 1) * blockSize)) {
                        currentBlock++;
                        offset = 0;
                        firstInBlock = i;
                        System.out.println("Doesn't fit; new block");
                    }
                    
                    e.setBlock(currentBlock);
                    e.setOffset(offset);
                    
                    System.out.println(i + " - " + e);
                    
                    DataUtils.writeULong(rf, e.getKey());
                    DataUtils.writeLEInt(rf, e.getBlock());
                    DataUtils.writeUShort(rf, (short)e.getOffset());
                    DataUtils.writeUShort(rf, (short)e.getLength());
                }
                
                for(int i = 0; i < 10; i++) {
                    System.out.println(entries[6]);
                }
                
                rf.write(new byte[(dataOffset - (int)rf.getFilePointer())]);
                
//                int lastBlock = 0;
                for(int i = 0; i < directorySize; i++) {
                    Entry e = entries[i];
//                    if(e.getBlock() > lastBlock) {
//                        lastBlock = e.getBlock();
//                        rf.write(new byte[(dataOffset + ((e.block + 1) * blockSize) - (int)rf.getFilePointer())]);
//                    }
                    byte[] out = e.getValue().getBytes();
                    for(int j = 0; j < out.length; j++) {
                        rf.write(out[j]);
                        rf.writeByte(0);
                    }
                    rf.writeByte(0);
                    rf.writeByte(0);
                }
                rf.close(); // The rest of the file is garbage
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            logger.log(Level.INFO, "Saved {0}", file);
        }

        private ArrayList<Entry> importFile(String file) {
            return null;
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Entry">
    /**
     * Entries are stored alphabetically by original value of hash
     */
    private class Entry implements Comparable<Entry> {
        
        Entry() {
            
        }
        
        private long key;
        
        public int getKey() {
            return (int) key;
        }
        
        public void setKey(long key) {
            this.key = key;
        }
        
        private int block;
        
        public int getBlock() {
            return block;
        }
        
        public void setBlock(int block) {
            this.block = block;
        }
        
        private int offset;
        
        public int getOffset() {
            return offset;
        }
        
        public void setOffset(int offset) {
            this.offset = offset;
        }
        
        private int length;
        
        public int getLength() {
            return length;
        }
        
        private void setLength(int length) {
            this.length = length;
            if(this.value != null) {
                this.value = value.substring(0, (length / 2) - 1);
            }
        }
        
        private String value;
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String string) {
            this.value = string;
            this.length = (string.length() + 1) * 2;
        }
        
        @Override
        public String toString() {
            return new StringBuilder().append("H: ").append(key).append(", b: ").append(block).append(", o: ").append(offset).append(", l: ").append(length).toString();
        }

        public int compareTo(Entry t) {
            String e1 = attemptDecode((int)this.key);
            if(e1 == null) {
                e1 = "";
            }
            String e2 = attemptDecode((int)t.key);
            if(e2 == null) {
                e2 = "";
            }
            return e1.compareToIgnoreCase(e2);
        }
        
    }
    //</editor-fold>

}