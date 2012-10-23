package com.timepath.tf2.hudedit.util;

import com.timepath.tf2.hudedit.EditorFrame;
import com.timepath.tf2.hudedit.display.HudCanvas;
import com.timepath.tf2.hudedit.loaders.ResLoader;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO: edge cases. I think elements without x and y coordinates default to the centre (c0)
 * @author andrew
 */
public class Element {
    
    static final Logger logger = Logger.getLogger(Element.class.getName());
    
    public static Map<String, Element> areas = new HashMap<String, Element>();

    HudCanvas canvas = EditorFrame.canvas;

    public Element(HudCanvas canvas) {
        this.canvas = canvas;
    }

    public Element(String name, String info) {
        this.name = name;
        this.info = info;
    }
    
    public String save() {
        String str = "";
        // preceding header
        for(int i = 0; i < this.propMap.size(); i++) {
            Property p = this.propMap.get(i);
            if(p.getValue().equals("")) {
                if(p.getKey().equals("\\n")) {
                    str += "\n";
                }
                if(p.getKey().equals("//")) {
                    str += "//" + p.getInfo() + "\n";
                }
            }
        }
        str += this.name + "\n";
        str += "{\n";
        for(int i = 0; i < this.propMap.size(); i++) {
            Property p = this.propMap.get(i);
            if(!p.getValue().equals("")) {
                if(p.getKey().equals("\\n")) {
                    str += "\t    \n";
                } else {
                    str += "\t    " + p.getKey() + "\t    " + p.getValue() + (p.getInfo() != null ? " " + p.getInfo() : "") + "\n";
                }
            }
        }
        str += "}\n";
        return str;
    }
    
    private String name;
    
    public String getName() {
        return name;
    }
    
    private String info;
    private ArrayList<Property> propMap = new ArrayList<Property>();

    public ArrayList<Property> getProps() {
        return propMap;
    }

    public void addProp(Property p) {
        logger.log(Level.FINE, "Adding prop: {0} to: {1}", new Object[]{p, this});
        propMap.add(p);
    }

    public void addProps(ArrayList<Property> p) {
        for (int i = 0; i < p.size(); i++) {
            addProp(p.get(i));
        }
        p.clear();
    }
    private Element parent;
    public ArrayList<Element> children = new ArrayList<Element>();

    public void addChild(Element e) {
        if(e == this || e == this.getParent()) {
            logger.log(Level.INFO, "Cannot add element {0} to {1}", new Object[]{e, this});
        }
        if (!children.contains(e)) {
            e.validateLoad();
            children.add(e);
            e.setParent(this);
        }
    }
    
    public void removeAllChildren() {
        for(int i = 0; i < children.size(); i++) {
            children.remove(i);
        }
    }

    // Extra stuff
    public int getSize() { // works well unless they are exactly the same size
        return wide * tall;
    }

    public Rectangle getBounds() {
        int minX = (int)Math.round(this.getX() * ((double)canvas.hudRes.width / (double)canvas.internalRes.width) * canvas.scale);
        int minY = (int)Math.round(this.getY() * ((double)canvas.hudRes.height / (double)canvas.internalRes.height) * canvas.scale);
        int maxX = (int)Math.round(this.getWidth() * ((double)canvas.hudRes.width / (double)canvas.internalRes.width) * canvas.scale);
        int maxY = (int)Math.round(this.getHeight() * ((double)canvas.hudRes.height / (double)canvas.internalRes.height) * canvas.scale);
        return new Rectangle(minX, minY, maxX + 1, maxY + 1);
    }

    @Override
    public String toString() {
        String displayInfo = (info != null ? (" ~ " + info) : ""); // elements cannot have a value, only info
        return name + displayInfo;
    }
    // Properties
    private int xPos;

    public int getLocalX() {
        return xPos;
    }

    public void setLocalX(int x) {
        this.xPos = x;
    }

    public int getX() {
        if (parent == null || parent.name.replaceAll("\"", "").endsWith(".res")) {
            if (this.getXAlignment() == Element.Alignment.Center) {
                return (xPos + Math.round(853 / 2));
            } else if (this.getXAlignment() == Element.Alignment.Right) {
                return (853 - xPos);
            } else {
                return xPos;
            }
        } else {
            int x;
            if (this.getXAlignment() == Element.Alignment.Center) {
                x = (parent.getWidth() / 2) + xPos;
            } else if (this.getXAlignment() == Element.Alignment.Right) {
                x = (parent.getWidth()) - xPos;
            } else {
                x = xPos;
            }
            return x + parent.getX();
        }
    }

    private int yPos;

    public int getLocalY() {
        return yPos;
    }

    public void setLocalY(int y) {
        this.yPos = y;
    }

    public int getY() {
        if (parent == null || parent.name.replaceAll("\"", "").endsWith(".res")) {
            if (this.getYAlignment() == Element.Alignment.Center) {
                return (yPos + Math.round(480 / 2));
            } else if (this.getYAlignment() == Element.Alignment.Right) {
                return (480 - yPos);
            } else {
                return yPos;
            }
        }
        int y;
        if (this.getYAlignment() == Element.Alignment.Center) {
            y = (parent.getHeight() / 2) + yPos;
        } else if (this.getYAlignment() == Element.Alignment.Right) {
            y = parent.getHeight() - yPos;
        } else {
            y = yPos;
        }
        return y + parent.getY();
    }

    // > 0 = out of screen
    private int zPos;

    public int getLayer() {
        return zPos;
    }

    public void setLayer(int z) {
        this.zPos = z;
    }
    private int wide;

    public int getLocalWidth() {
        return wide;
    }

    public void setLocalWidth(int wide) {
        this.wide = wide;
    }

    public int getWidth() {
        if (this.getWidthMode() == DimensionMode.Mode2) {
//            if(this.parent !hudRes= null) {
//                return this.parent.getWidth() - wide;
//            } else {
            return canvas.internalRes.width - wide;
//            }
        } else {
            return wide;
        }
    }
    
    private DimensionMode _wideMode = DimensionMode.Mode1;

    public DimensionMode getWidthMode() {
        return _wideMode;
    }

    public void setWidthMode(DimensionMode mode) {
        this._wideMode = mode;
    }
    private int tall;

    public int getLocalHeight() {
        return tall;
    }

    public void setLocalHeight(int tall) {
        this.tall = tall;
    }

    public int getHeight() {
        return (this.getHeightMode() == DimensionMode.Mode2 ? (this.parent != null ? this.parent.getHeight() - tall : canvas.internalRes.height - tall) : tall);
    }
    
    private DimensionMode _tallMode = DimensionMode.Mode1;

    public DimensionMode getHeightMode() {
        return _tallMode;
    }

    public void setHeightMode(DimensionMode mode) {
        this._tallMode = mode;
    }
    private boolean visible;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    private Font font; // http://www.3rd-evolution.de/tkrammer/docs/java_font_size.html

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }
    private Color fgColor;

    public Color getFgColor() {
        return fgColor;
    }

    public void setFgColor(Color fgColor) {
        this.fgColor = fgColor;
    }
    
    // TODO: remove duplicate keys (only keep the latest, or highlight duplicates)
    public void validateLoad() {
        for (int n = 0; n < this.getProps().size(); n++) {
            Property entry = this.getProps().get(n);
            String k = entry.getKey();
            if (k != null && k.contains("\"")) { // assumes one set of quotes
                k = k.substring(1, k.length() - 1);
            }
            String v = entry.getValue();
            if (v != null && v.contains("\"")) {
                v = v.substring(1, v.length() - 1);
            }
            String i = entry.getInfo();
            if (i != null && i.contains("\"")) {
                i = i.substring(1, i.length() - 1);
            }

            if ("enabled".equalsIgnoreCase(k)) {
                this.setEnabled(Integer.parseInt(v) == 1);
            } else if ("visible".equalsIgnoreCase(k)) {
                this.setVisible(Integer.parseInt(v) == 1);
            } else if ("xpos".equalsIgnoreCase(k)) {
                if (v.startsWith("c")) {
                    this.setXAlignment(Alignment.Center);
                    v = v.substring(1);
                } else if (v.startsWith("r")) {
                    this.setXAlignment(Alignment.Right);
                    v = v.substring(1);
                } else {
                    this.setXAlignment(Alignment.Left);
                }
                this.setLocalX(Integer.parseInt(v));
            } else if ("ypos".equalsIgnoreCase(k)) {
                if (v.startsWith("c")) {
                    this.setYAlignment(Alignment.Center);
                    v = v.substring(1);
                } else if (v.startsWith("r")) {
                    this.setYAlignment(Alignment.Right);
                    v = v.substring(1);
                } else {
                    this.setYAlignment(Alignment.Left);
                }
                this.setLocalY(Integer.parseInt(v));
            } else if ("zpos".equalsIgnoreCase(k)) {
                this.setLayer(Integer.parseInt(v));
            } else if ("wide".equalsIgnoreCase(k)) {
                if (v.startsWith("f")) {
                    v = v.substring(1);
                    this.setWidthMode(DimensionMode.Mode2);
                }
                this.setLocalWidth(Integer.parseInt(v));
            } else if ("tall".equalsIgnoreCase(k)) {
                if (v.startsWith("f")) {
                    v = v.substring(1);
                    this.setHeightMode(DimensionMode.Mode2);
                }
                this.setLocalHeight(Integer.parseInt(v));
            } else if ("labelText".equalsIgnoreCase(k)) {
                this.setLabelText(v);
            } else if ("ControlName".equalsIgnoreCase(k)) { // others are areas
                this.setControlName(v);
            } else if ("fgcolor".equalsIgnoreCase(k)) {
                String[] c = v.split(" ");
                try {
                    this.setFgColor(new Color(Integer.parseInt(c[0]), Integer.parseInt(c[1]), Integer.parseInt(c[2]), Integer.parseInt(c[3])));
                } catch(NumberFormatException e) {
                    
                }
            } else {
//                System.out.println("Other property: " + k);
            }
        }
        
        if(this.getControlName() != null) { // temp
            if("CExLabel".equalsIgnoreCase(controlName)) {
            } else if("CIconPanel".equalsIgnoreCase(controlName)) {
            } else if("CTeamMenu".equalsIgnoreCase(controlName)) {
            } else if("CTFClassInfoPanel".equalsIgnoreCase(controlName)) {
            } else if("CTFArrowPanel".equalsIgnoreCase(controlName)) {
            } else if("CTFImagePanel".equalsIgnoreCase(controlName)) {
            } else if("CEmbeddedItemModelPanel".equalsIgnoreCase(controlName)) { // ooh, fancy
            } else if("CTFHudTimeStatus".equalsIgnoreCase(controlName)) {
            } else if("CWaveStatusPanel".equalsIgnoreCase(controlName)) { // MvM?
            } else if("CControlPointCountdown".equalsIgnoreCase(controlName)) { // control points
            } else if("CWaveCompleteSummaryPanel".equalsIgnoreCase(controlName)) { // MvM?
            } else if("CTFTextWindow".equalsIgnoreCase(controlName)) { // text motd?
            } else if("CTFRichText".equalsIgnoreCase(controlName)) {
            } else if("CTankStatusPanel".equalsIgnoreCase(controlName)) {
            } else if("CExImageButton".equalsIgnoreCase(controlName)) {
            } else if("CEngyDestroyMenuItem".equalsIgnoreCase(controlName)) {
            } else if("CCurrencyStatusPanel".equalsIgnoreCase(controlName)) {
            } else if("CTFClassImage".equalsIgnoreCase(controlName)) {
            } else if("CInWorldCurrencyStatus".equalsIgnoreCase(controlName)) { // MvM in world currency
            } else if("CTFClientScoreBoardDialog".equalsIgnoreCase(controlName)) { // scoreboard
            } else if("CTFHudEscort".equalsIgnoreCase(controlName)) { // payload?
            } else if("CWarningSwoop".equalsIgnoreCase(controlName)) {
            } else if("CItemModelPanel".equalsIgnoreCase(controlName)) {
            } else if("CStoreItemControlsPanel".equalsIgnoreCase(controlName)) {
            } else if("CExButton".equalsIgnoreCase(controlName)) {
            } else if("CStorePreviewItemPanel".equalsIgnoreCase(controlName)) {
            } else if("CStorePricePanel".equalsIgnoreCase(controlName)) {
            } else if("CRichTextWithScrollbarBorders".equalsIgnoreCase(controlName)) {
            } else if("CTFPlayerModelPanel".equalsIgnoreCase(controlName)) {
            } else if("CArmoryPanel".equalsIgnoreCase(controlName)) {
            } else if("CNotificationsPresentPanel".equalsIgnoreCase(controlName)) {
            } else if("CEconItemDetailsRichText".equalsIgnoreCase(controlName)) {
            } else if("CTFMapStampsInfoDialog".equalsIgnoreCase(controlName)) {
            } else if("CStorePreviewItemIcon".equalsIgnoreCase(controlName)) {
            } else if("CMouseMessageForwardingPanel".equalsIgnoreCase(controlName)) {
            } else if("CGenericNotificationToast".equalsIgnoreCase(controlName)) {
            } else if("CStorePreviewClassIcon".equalsIgnoreCase(controlName)) {
            } else if("CNavigationPanel".equalsIgnoreCase(controlName)) {
            } else if("CAvatarImagePanel".equalsIgnoreCase(controlName)) {
            } else if("CNotificationQueuePanel".equalsIgnoreCase(controlName)) {
            } else if("CPreviewRotButton".equalsIgnoreCase(controlName)) {
            } else if("CNotificationToastControl".equalsIgnoreCase(controlName)) {
            } else if("CItemMaterialCustomizationIconPanel".equalsIgnoreCase(controlName)) {
            } else if("CImagePanel".equalsIgnoreCase(controlName)) {
            } else if("CExplanationPopup".equalsIgnoreCase(controlName)) {
            } else if("CRGBAImagePanel".equalsIgnoreCase(controlName)) {
            } else if("CBackpackPanel".equalsIgnoreCase(controlName)) {
            } else if("CModePanel".equalsIgnoreCase(controlName)) {
            } else if("CTrainingDialog".equalsIgnoreCase(controlName)) {
            } else if("CAchievementsDialog".equalsIgnoreCase(controlName)) {
            } else if("CClassMenu".equalsIgnoreCase(controlName)) {
            } else if("CBitmapPanel".equalsIgnoreCase(controlName)) {
            } else if("CModeSelectionPanel".equalsIgnoreCase(controlName)) {
            } else if("CCustomTextureImagePanel".equalsIgnoreCase(controlName)) {
            } else if("CTFClassTipsItemPanel".equalsIgnoreCase(controlName)) {
            } else if("CBasicTraining_ClassSelectionPanel".equalsIgnoreCase(controlName)) {
            } else if("CBasicTraining_ClassDetailsPanel".equalsIgnoreCase(controlName)) {
            } else if("COfflinePractice_ModeSelectionPanel".equalsIgnoreCase(controlName)) {
            } else if("COfflinePractice_MapSelectionPanel".equalsIgnoreCase(controlName)) {
            } else if("CLoadoutPresetPanel".equalsIgnoreCase(controlName)) {
            } else if("CClassLoadoutPanel".equalsIgnoreCase(controlName)) {
            } else if("CBuildingHealthBar".equalsIgnoreCase(controlName)) {
            } else if("CBuildingStatusAlertTray".equalsIgnoreCase(controlName)) {
            } else if("CTFFreezePanelHealth".equalsIgnoreCase(controlName)) {
            } else if("CTFTeamButton".equalsIgnoreCase(controlName)) {
            } else if("CModelPanel".equalsIgnoreCase(controlName)) {
            } else if("CTFFooter".equalsIgnoreCase(controlName)) {
            } else if("CMvMBombCarrierProgress".equalsIgnoreCase(controlName)) {
            } else if("CTFProgressBar".equalsIgnoreCase(controlName)) {
            } else if("CVictorySplash".equalsIgnoreCase(controlName)) {
            } else if("CMvMVictoryPanelContainer".equalsIgnoreCase(controlName)) {
            } else if("CMvMWaveLossPanel".equalsIgnoreCase(controlName)) {
            } else if("CExRichText".equalsIgnoreCase(controlName)) {
            } else if("CTFIntroMenu".equalsIgnoreCase(controlName)) {
            } else if("CTFVideoPanel".equalsIgnoreCase(controlName)) {
            } else if("CTFLayeredMapItemPanel".equalsIgnoreCase(controlName)) {
            } else if("CTFClassTipsPanel".equalsIgnoreCase(controlName)) {
            } else if("CBaseModelPanel".equalsIgnoreCase(controlName)) {
            } else if("CCreditDisplayPanel".equalsIgnoreCase(controlName)) {
            } else if("CCreditSpendPanel".equalsIgnoreCase(controlName)) {
            } else if("CVictoryPanel".equalsIgnoreCase(controlName)) {
            } else if("CMvMVictoryMannUpPanel".equalsIgnoreCase(controlName)) {
            } else if("CMvMVictoryMannUpEntry".equalsIgnoreCase(controlName)) {
            } else if("CTFHudEscortProgressBar".equalsIgnoreCase(controlName)) {
            } else if("CPublishFileDialog".equalsIgnoreCase(controlName)) {
            } else if("CPublishedFileBrowserDialog".equalsIgnoreCase(controlName)) {
            } else if("CQuickPlayBusyDialog".equalsIgnoreCase(controlName)) {
            } else if("CQuickplayDialog".equalsIgnoreCase(controlName)) {
            } else if("CMainMenuNotificationsControl".equalsIgnoreCase(controlName)) {
            } else if("CSteamWorkshopDialog".equalsIgnoreCase(controlName)) {
            } else if("CSteamWorkshopItemPanel".equalsIgnoreCase(controlName)) {
            } else if("CTankProgressBar".equalsIgnoreCase(controlName)) {
            } else if("CPanelListPanel".equalsIgnoreCase(controlName)) {
            } else if("CTrainingItemPanel".equalsIgnoreCase(controlName)) {
            } else if("CTFTrainingComplete".equalsIgnoreCase(controlName)) {
            } else if("CImageButton".equalsIgnoreCase(controlName)) {
            } else if("CCommentaryExplanationDialog".equalsIgnoreCase(controlName)) {
            } else if("CCommentaryItemPanel".equalsIgnoreCase(controlName)) {
            } else if("CTGAImagePanel".equalsIgnoreCase(controlName)) {
            } else if("COfflinePracticeServerPanel".equalsIgnoreCase(controlName)) {
            } else if("CLoadGameDialog".equalsIgnoreCase(controlName)) {
            } else if("CNewGameDialog".equalsIgnoreCase(controlName)) {
            } else if("COptionsSubMultiplayer".equalsIgnoreCase(controlName)) {
            } else if("CPlayerListDialog".equalsIgnoreCase(controlName)) {
            } else if("CVoteSetupDialog".equalsIgnoreCase(controlName)) {
            } else if("CCvarSlider".equalsIgnoreCase(controlName)) {
            } else if("CControllerMap".equalsIgnoreCase(controlName)) {
            } else if("CScenarioInfoPanel".equalsIgnoreCase(controlName)) {
            } else if("CTFButton".equalsIgnoreCase(controlName)) {
            } else if("CTFImageButton".equalsIgnoreCase(controlName)) {
            } else if("CTFFlagStatus".equalsIgnoreCase(controlName)) {
            } else if("CTFHudMannVsMachineScoreboard".equalsIgnoreCase(controlName)) {
            } else if("CReplayReminderPanel".equalsIgnoreCase(controlName)) {
                
                
            } else if("CircularProgressBar".equalsIgnoreCase(controlName)) { // what the hell is this?
            } else if("PanelListPanel".equalsIgnoreCase(controlName)) {
            } else if("ImageButton".equalsIgnoreCase(controlName)) {
            } else if("RichText".equalsIgnoreCase(controlName)) {
            } else if("SectionedListPanel".equalsIgnoreCase(controlName)) {
            } else if("ListPanel".equalsIgnoreCase(controlName)) {
            } else if("RoundInfoOverlay".equalsIgnoreCase(controlName)) {
            } else if("ProgressBar".equalsIgnoreCase(controlName)) {
            } else if("Slider".equalsIgnoreCase(controlName)) {
            } else if("Divider".equalsIgnoreCase(controlName)) {
            } else if("AnalogBar".equalsIgnoreCase(controlName)) {
            } else if("FooterPanel".equalsIgnoreCase(controlName)) {
            } else if("AnimatingImagePanel".equalsIgnoreCase(controlName)) {
            } else if("RotatingProgressBar".equalsIgnoreCase(controlName)) {
            } else if("MaterialButton".equalsIgnoreCase(controlName)) {
            } else if("CustomTextureStencilGradientMapWidget".equalsIgnoreCase(controlName)) {
            } else if("RadioButton".equalsIgnoreCase(controlName)) {
            } else if("ScrollableEditablePanel".equalsIgnoreCase(controlName)) {
            } else if("CheckButton".equalsIgnoreCase(controlName)) {
            } else if("ComboBox".equalsIgnoreCase(controlName)) {
            } else if("ScrollBar".equalsIgnoreCase(controlName)) {
            } else if("Button".equalsIgnoreCase(controlName)) {
            } else if("Panel".equalsIgnoreCase(controlName)) {
            } else if("ImagePanel".equalsIgnoreCase(controlName)) {
            } else if("ContinuousProgressBar".equalsIgnoreCase(controlName)) {
            } else if("Menu".equalsIgnoreCase(controlName)) {
            } else if("EditablePanel".equalsIgnoreCase(controlName)) {
            } else if("Frame".equalsIgnoreCase(controlName)) {
            } else if("ScalableImagePanel".equalsIgnoreCase(controlName)) {
            } else if("Label".equalsIgnoreCase(controlName)) {
            } else if("HTML".equalsIgnoreCase(controlName)) {
            } else if("TextEntry".equalsIgnoreCase(controlName)) {
            } else {
                System.out.println(controlName);
            }
            if(this.getFgColor() == null) {
                this.setFgColor(new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255), 32));
            }
        } else if(this.getFile().equalsIgnoreCase("hudlayout")) {
            areas.put(this.name, this);
//            System.out.println("adding " + this.name + " to areas");
        }
    }
    
    // TODO: remove duplicate keys (only keep the latest, or highlight duplicates)
    public void validateDisplay() { 
        for (int n = 0; n < propMap.size(); n++) {
            Property entry = propMap.get(n);
            String k = entry.getKey();
            if(k == null) {
                continue;
            }
            if (k.contains("\"")) { // assumes one set of quotes
                k = k.substring(1, k.length() - 1);
            }

            if ("enabled".equalsIgnoreCase(k)) {
                entry.setValue(this.isEnabled() ? 1 : 0);
            } else if ("visible".equalsIgnoreCase(k)) {
                entry.setValue(this.isVisible() ? 1 : 0);
            } else if ("xpos".equalsIgnoreCase(k)) {
                entry.setValue(this.getXAlignment().name().substring(0, 1).toLowerCase().replaceFirst("l", "") + this.getLocalX());
            } else if ("ypos".equalsIgnoreCase(k)) {
                entry.setValue(this.getYAlignment().name().substring(0, 1).toLowerCase().replaceFirst("l", "") + this.getLocalY());
            } else if ("zpos".equalsIgnoreCase(k)) {
                entry.setValue(this.getLayer());
            } else if ("wide".equalsIgnoreCase(k)) {
                entry.setValue((this.getWidthMode() == DimensionMode.Mode2 ? "f" : "") + this.getLocalWidth());
            } else if ("tall".equalsIgnoreCase(k)) {
                entry.setValue((this.getHeightMode() == DimensionMode.Mode2 ? "f" : "") + this.getLocalHeight());
            } else if ("labelText".equalsIgnoreCase(k)) {
                entry.setValue(this.getLabelText());
            } else if ("ControlName".equalsIgnoreCase(k)) {
                entry.setValue(this.getControlName());
            }
            if(!entry.getKey().equals("//") && !entry.getKey().equals("\\n")) {
                entry.setValue("\"" + entry.getValue() + "\"");
            }
        }
    }

    public HudCanvas getCanvas() {
        return canvas;
    }

    public void setCanvas(HudCanvas canvas) {
        this.canvas = canvas;
    }

    public Element getParent() {
        return parent;
    }

    public void setParent(Element newParent) {
        this.parent = newParent;
    }
    
    private String controlName;

    public void setControlName(String controlName) {
        this.controlName = controlName;
    }

    public String getControlName() {
        return controlName;
    }

    private String fileName;
    
    public void setParentFile(File file) { // todo: case insensitivity
        if(file.getName().contains(".")) {
            this.fileName = file.getName().split("\\.")[0];
        } else {
            this.fileName = file.getName();
        }
    }
    
    public String getFile() {
        return fileName;
    }
    
    public static enum Alignment {

        Left, Center, Right
    }

    public static enum DimensionMode {

        Mode1, Mode2
    }
    private Alignment _xAlignment = Alignment.Left;

    public Alignment getXAlignment() {
        return _xAlignment;
    }

    public void setXAlignment(Alignment _xAlignment) {
        this._xAlignment = _xAlignment;
    }
    private Alignment _yAlignment = Alignment.Left;

    public Alignment getYAlignment() {
        return _yAlignment;
    }

    public void setYAlignment(Alignment _yAlignment) {
        this._yAlignment = _yAlignment;
    }
    private String labelText;

    public String getLabelText() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText = labelText;
    }
    private Image image;

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
//    private boolean scaleImage;
//    private boolean pinCorner;
}