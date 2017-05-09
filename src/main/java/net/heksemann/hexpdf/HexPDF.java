/*
 * Copyright 2014 Frank J. Øynes, heksemann@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * 
 * HexPDF is a simple Java class making it easier to use Apache PDFBox
 * for creating pdf documents in from your Java application or web-service.
 * 
 * HexPDF adds stuff like automatic page adding, word-wrap, newline awareness,
 * left/right/center text alignment, table creation and image insertion.
 * Tables may contain images and text.
 * It also has support for text colour and page footers.
 * 
 */
package net.heksemann.hexpdf;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

/**
 * Simple class for generating pdf documents with support for tables and images,
 * uses Apache pdfBox for basic pdf creation functions.
 *
 * <p>
 * The class adds automatic word wrap, left, right, center alignment of text,
 * control over page borders, a simple table function with control over text
 * alignment for each column, and inclusion of images in the document.</p>
 *
 * <p>
 * Example usage</p>
 * <pre>
 * <code>
 *     HexPDF doc = new HexPDF();
 *  
 *     BufferedImage basemap = (your function for retrieving the image)
 *     BufferedImage overlay = (your function for retrieving the image)
 *     String title = "A simple pdf document";
 *     String exampleText = "Lorem ipsum dolor.......";
 *
 *     Object[][] table = {
 *                          {"Country",  "Area", "Population", "Info"},
 *                          {"Norway",   "col2", "col2", "col4"},
 *                          {"Sweden",   "col2", "col2", "col4"},
 *                          {"Denmark",  "col2", "col2", "col4"},
 *                          {"Vietnam",  "col2", "col2", "col4"},
 *                          {"Thaland",  "col2", "col2", "col4"},
 *                          {"Burma",    "col2", "col2", "col4"},
 *                          {"USA",      "col2", "col2", "col4"},
 *                          {"Germany",  "col2", "col2", "col4"}
 *                        };
 * 
 *     // Create the first page. IMPORTANT!
 *     doc.newPage();
 * 
 *     // Add a title
 *     doc.title1Style();
 *     doc.drawText("My simple document\n", HexPDF.CENTER);
 *
 *     doc.normalStyle();
 *     doc.drawText(exampleText); // Default left aligned
 *
 *     doc.drawImage(basemap, HexPDF.CENTER);
 *     doc.drawImage(overlay, HexPDF.CENTER | HexPDF.NEWLINE);
 *     doc.drawText("Figure 1: An example figure with overlay\n", HexPDF.CENTER);
 *
 *     doc.drawText("\n\n" + exampleText, HexPDF.RIGHT);
 *
 *     doc.drawTable(table,
 *                   new int[]{100, 50, 50, 300},
 *                   new int[]{HexPDF.LEFT, HexPDF.LEFT, HexPDF.LEFT, HexPDF.LEFT},
 *                   HexPDF.CENTER);
 *     doc.drawText("Table 1: Some countries I've been to\n", HexPDF.CENTER);
 *
 *     doc.drawText("\n\n" + exampleText + exampleText, HexPDF.LEFT);
 *
 *     doc.title1Style();
 *     doc.drawText("-- END OF DOCUMENT --", HexPDF.CENTER);
 *
 *     doc.finish("myfile.pdf");
 * </code>
 * </pre>
 *
 *
 * @author Frank J. Øynes, heksemann@gmail.com
 */
public class HexPDF extends PDDocument {
    private int orientation;
    private PDPageContentStream cs;
    private PDPage currentPage = null;
    private int numPages;
    private Footer footer = null;
    private Color normalColor;
    private Color titleColor;
    // Page setup
    private PDRectangle pageSize;
    private PDFont font;
    private float fontSize;
    private float topMargin;
    private float bottomMargin;
    private float leftMargin;
    private float rightMargin;
    private boolean ignorePagebleed;
    
    // Calculated dimensions
    private float pageWidth;
    private float pageHeight;

    /**
     * Page orientation portrait (default).
     * @see #setOrientation(int) 
     */
    public final static int PORTRAIT = 0;

    /**
     * Page orientation Landscape.
     * @see #setOrientation(int) 
     */
    public final static int LANDSCAPE = 1;

    /**
     * The height, in points, of the writable area between bottom and top
     * margins
     */
    protected float contentHeight;

    /**
     * The width, in points, of the writable area between left and right margins
     */
    protected float contentWidth;
    private float lineSep;

    /**
     * The starting (leftmost, lowest value) x position of writable area. Equal
     * to <code>leftMargin</code>
     */
    protected float contentStartX;

    /**
     * The starting (topmost, highest value) y position of writable area. Equal
     * to <code>bottomMargin + contentHeight</code>
     */
    protected float contentStartY;

    /**
     * The end (rightmost, highest value) x position of writable area. Equal to
     * <code>leftMargin + contentWidth</code>
     */
    protected float contentEndX;

    /**
     * The end (bottommost, lowest value) y position of writable area. Equal to
     * <code>bottomMargin</code>
     */
    protected float contentEndY;

    /**
     * The x-position in points of the current cursor position.
     */
    protected float cursorX;

    /**
     * The y-position in points of the current cursor position. Note that y
     * values are increasing towards the top of the page in pdf.
     */
    protected float cursorY;

    // Flags
    /**
     * Flag for image and text alignment and alignment of text in table cells.
     *
     * @see #LEFT
     * @see #RIGHT
     * @see #JUSTIFY
     */
    public static final int CENTER = 1;

    /**
     * Flag for image and text alignment and alignment of text in table cells.
     *
     * @see #CENTER
     * @see #RIGHT
     * @see #JUSTIFY
     */
    public static final int LEFT = 2;

    /**
     * Flag for image and text alignment and alignment of text in table cells.
     *
     * @see #LEFT
     * @see #CENTER
     * @see #JUSTIFY
     */
    public static final int RIGHT = 4;

    /**
     * Flag for image and text alignment and alignment of text in table cells.
     *
     * @see #LEFT
     * @see #RIGHT
     * @see #CENTER
     */
    public static final int JUSTIFY = 8;
    /**
     * Flag for drawImage - if set, move cursor to bottom after placement.
     */
    public static final int NEWLINE = 16;
    public static final int NEWPAGE = 32;
    // Text processing
    private static final String TXT_NEWLINE = "@@NeWlInE@@";

    // Styling
    private float normalFontSize = 10;
    private float title1FontSize = 20;
    private float title2FontSize = 15;

    /**
     * Default font size used for normalStyle.
     *
     * @see #normalStyle()
     * @see #setNormalFontSize(float)
     */
    public static final float DEFAULT_NORMAL_FONT_SIZE = 10;

    /**
     * Default font size used for title1Style.
     *
     * @see #title1Style()
     * @see #setTitle1FontSize(float)
     */
    public static final float DEFAULT_TITLE1_FONT_SIZE = 20;

    /**
     * Default font size used for title2Style.
     *
     * @see #title2Style()
     * @see #setTitle2FontSize(float)
     */
    public static final float DEFAULT_TITLE2_FONT_SIZE = 15;

    // Table
    private float tableCellMargin = 5;

    /**
     * Default margin in points between table cell border and table cell text.
     *
     * @see #settableCellMargin(float)
     * @see #drawTable(java.lang.Object[][], float[], int[], int)
     */
    public static final float DEFAULT_TABLE_CELL_MARGIN = 5;

    /**
     * Sole constructor, creates a new instance of HexPDF.
     */
    public HexPDF() {
        super();
        this.ignorePagebleed = false;
        this.numPages = 0;
        this.rightMargin = 50f;
        this.leftMargin = 50f;
        this.bottomMargin = 50f;
        this.topMargin = 50f;
        this.fontSize = 10;
        this.font = PDType1Font.HELVETICA;
        this.pageSize = PDPage.PAGE_SIZE_A4;
        this.normalColor = Color.black;
        this.titleColor = Color.BLUE;
        this.orientation = HexPDF.PORTRAIT;
        //firstPage();
    }

    /**
     * Recalculate page boundaries after a change of margins or page style.
     * Automatically called after margin changes.
     *
     * @see #contentHeight
     * @see #contentWidth
     * @see #contentStartX
     * @see #contentStartY
     * @see #contentEndX
     * @see #contentEndY
     * @see #cursorX
     * @see #cursorY
     */
    protected void setDimensions() {

        contentHeight = pageHeight - topMargin - bottomMargin;
        contentWidth  = pageWidth - leftMargin - rightMargin;

        contentStartX = leftMargin;
        contentEndX = leftMargin + contentWidth;
        contentStartY = pageHeight - topMargin;
        contentEndY = bottomMargin;

        // Make sure cursor is inside writable area
        cursorX = (cursorX < leftMargin) ? leftMargin : cursorX;
        cursorX = (cursorX > contentEndX) ? contentEndX : cursorX;
        cursorY = (cursorY > contentStartY) ? contentStartY : cursorY;
        cursorY = (cursorY < contentEndX) ? contentEndX : cursorY;

        // Set line separation according to current font-size
        lineSep = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
    }

    /**
     * Returns the width of the pdf representation of the given string in
     * points.
     *
     * @param txt The text to calulate pdf-width of
     * @return The width given currently selected font and fontsize
     */
    protected float textWidth(String txt) {
        try {
            return (font.getStringWidth(txt) * fontSize / 1000);
        } catch (IOException ex) {
            Logger.getLogger(HexPDF.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    private void savedoc(String filename) throws IOException, COSVisitorException {
        closePage();
        super.save(filename);
    }

    /**
     * Close the current page and add it to the document.
     *
     * @see #newPage()
     */
    protected void closePage() {
        if (currentPage != null) {
            try {
                cs.close();
                addPage(currentPage);
                currentPage = null;
            } catch (IOException ex) {
                Logger.getLogger(HexPDF.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private String replaceBookmarks(String str, int pagenum, int numpages) {
        String ret = str;

        String today = new SimpleDateFormat("dd MMM yyyy").format(Calendar.getInstance().getTime());
        String user = System.getProperty("user.name");

        ret = ret.replace(Footer.PAGENUM, "" + pagenum);
        ret = ret.replace(Footer.NUMPAGES, "" + numpages);
        ret = ret.replace(Footer.DATE, today);
        ret = ret.replace(Footer.USER, user);
        return ret;
    }

    private void drawFooters() {
        if (footer != null) {
            ignorePagebleed = true; // Now new pages while writing footers!

            int pg;
            int pagecounter = 0;
            int total = (footer.isCOUNT_FIRSTPAGE()) ? numPages : numPages - 1;
            List<PDPage> pages = this.getDocumentCatalog().getAllPages();
            for (PDPage page : pages) {
                pagecounter++;
                try {
                    cs = new PDPageContentStream(this, page, true, true);
                    setTextColor(footer.getTextColor());
                    setFont(footer.getFont());
                    setFontSize(footer.getFontsize());
                    if (pagecounter > 1 || false == footer.isOMIT_FIRSTPAGE()) {
                        pg = (footer.isCOUNT_FIRSTPAGE()) ? pagecounter : pagecounter - 1;

                        if (footer.getLeftText() != null && !footer.getLeftText().isEmpty()) {
                            String left = replaceBookmarks(footer.getLeftText(), pg, total);
                            setCursor(contentStartX, contentEndY - lineSep);
                            drawText(left, HexPDF.LEFT);
                        }
                        if (footer.getRightText() != null && !footer.getRightText().isEmpty()) {
                            String right = replaceBookmarks(footer.getRightText(), pg, total);
                            setCursor(contentStartX, contentEndY - lineSep);
                            drawText(right, HexPDF.RIGHT);
                        }
                        if (footer.getCenterText() != null && !footer.getCenterText().isEmpty()) {
                            String right = replaceBookmarks(footer.getCenterText(), pg, total);
                            setCursor(contentStartX, contentEndY - lineSep);
                            drawText(right, HexPDF.CENTER);
                        }
                    }
                    cs.close();
                } catch (IOException ex) {
                    Logger.getLogger(HexPDF.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            ignorePagebleed = false;
        }
    }

    /**
     * Close the current page (if any), and open a new one. Cursor position is
     * reset to top-left corner of writable area (within margins)
     *
     * @see #closePage()
     */
    public void newPage() {

        numPages++;
        if (currentPage != null) {
            closePage();
        }

        currentPage = new PDPage();
        float x1 = this.pageSize.getLowerLeftX();
        float y1 = this.pageSize.getLowerLeftY();
        float x2 = this.pageSize.getUpperRightX();
        float y2 = this.pageSize.getUpperRightY();
        float w  = this.pageSize.getWidth();
        float h  = this.pageSize.getHeight();
        if (orientation == HexPDF.PORTRAIT){
            pageWidth = w;
            pageHeight = h;
            currentPage.setMediaBox(new PDRectangle(new BoundingBox(x1, y1, x2, y2)));
        }
        else{
            pageWidth = h;
            pageHeight = w;
            currentPage.setMediaBox(new PDRectangle(new BoundingBox(y1, x1, y2, x2)));
        }
        
        setDimensions();
        cursorX = contentStartX;
        cursorY = contentStartY;
        try {
            cs = new PDPageContentStream(this, currentPage);
            cs.setFont(font, fontSize);
        } catch (IOException ex) {
            Logger.getLogger(HexPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Save and close the document.
     *
     * @param filename  Path to saved document
     * @see #save(java.lang.String)
     * @see #close()
     */
    public void finish(String filename) {
        try {
            setTextColor(footer.getTextColor());
            setFont(footer.getFont());
            setFontSize(footer.getFontsize());

            closePage();
            drawFooters();
            savedoc(filename);
            close();
        } catch (IOException ex) {
            Logger.getLogger(HexPDF.class.getName()).log(Level.SEVERE, null, ex);
        } catch (COSVisitorException ex) {
            Logger.getLogger(HexPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Move cursor to a new position on the current page.
     *
     * @param x cursor x-position (horizontal)
     * @param y cursor y-position (vertical)
     *
     * @see #setCursor(float[])
     */
    public void setCursor(float x, float y) {
        cursorX = x;
        cursorY = y;
    }

    /**
     * Move cursor to a new position on the current page.
     *
     * @param point a two-element float array giving x and y position of cursor
     *
     * @see #setCursor(float, float)
     */
    public void setCursor(float[] point) {
        setCursor(point[0], point[1]);
    }

    /**
     * Get current cursor position on current page.
     *
     * @return two-element float array giving x and y position of cursor
     */
    public float[] getCursor() {
        float[] ret = {cursorX, cursorY};
        return ret;
    }

    /**
     * Retrieve the current cursors horizontal position.
     *
     * @return x value of current cursor position
     *
     * @see #getCursor()
     */
    public float getCursorX() {
        return cursorX;
    }

    /**
     * Retrieve the current cursor vertical position.
     *
     * @return y value of current cursor position
     *
     * @see #getCursor()
     */
    public float getCursorY() {
        return cursorY;
    }

    private int makeLine(String[] words, int first, float maxlen) {
        int num = 0;
        String result = "";
        if (words[first].equals(HexPDF.TXT_NEWLINE)) {
            return -1;
        }
        for (int i = first; i < words.length; i++) {
            if (words[i].equals(HexPDF.TXT_NEWLINE)) {
                return num;
            }
            String word = words[i].trim();
            if (num == 0) {
                if (textWidth(" " + word) > maxlen) {
                    return 0;
                } else {
                    result = word;
                    num++;
                }
            } else if (textWidth(result + " " + word) > maxlen) {
                return num;
            } else {
                result += " " + word;
                num++;
            }
        }
        return num;
    }

    private void doDrawText(String line) {
        try {
            cs.beginText();
            cs.moveTextPositionByAmount(cursorX, cursorY);
            cs.drawString(line);
            cs.endText();
            cursorX += font.getStringWidth(line) * fontSize / 1000;
        } catch (IOException ex) {
            Logger.getLogger(HexPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String join(String[] words, int first, int num) {
        String ret = "";
        String sep = "";
        for (int i = first; i < first + num; i++) {
            if (i < words.length) {
                ret += sep + words[i];
                sep = " ";
            }
        }
        return ret;
    }
    
    // Estimate height of a table cell. Used by rowHeight to determine
    // if page break should be inserted in a table
    private float elemHeight(Object elem, float startx, float endx, int flags) {
        String txt;
        if (elem instanceof BufferedImage){
            return ((BufferedImage)elem).getHeight();
        }
        txt = (String)elem;
        
        float cystart = cursorY;
        float cxstart = cursorX;
        
        int align = HexPDF.LEFT;
        if ((flags & HexPDF.CENTER) > 0) {
            align = HexPDF.CENTER;
        } else if ((flags & HexPDF.RIGHT) > 0) {
            align = HexPDF.RIGHT;
        } else if ((flags & HexPDF.JUSTIFY) > 0) {
            align = HexPDF.JUSTIFY;
        }
        if (txt == null || txt.isEmpty()){
            return 0;
        }
        txt = txt.replace("\n", " " + HexPDF.TXT_NEWLINE + " ");
        String[] words = txt.split("\\s+");
        int i = 0;
        float height = 0;
        while (i < words.length) {
            int num = makeLine(words, i, endx - cursorX);
            if (num == -1) { // newline
                i++;
                cursorX = startx;
                cursorY -= lineSep;
                height += lineSep;
            } else if (num == 0) {
                if (cursorX > startx) {
                    // Something on line from start. Try a newline first, then recheck.
                    cursorX = startx;
                    cursorY -= lineSep;
                } else {
                    // a single word is too big for the box. Draw it!
                    cursorY -= lineSep;
                    cursorX = startx;
                    i++;
                }
            } else {
                String toDraw = join(words, i, num);
                float strlen = textWidth(toDraw);
                if ((cursorX == startx) && (align == HexPDF.RIGHT || align == HexPDF.CENTER || align == HexPDF.JUSTIFY)) {
                    boolean newline_after = ((i + num) >= words.length || words[i + num].equals(HexPDF.TXT_NEWLINE));
                    if (align == HexPDF.JUSTIFY) {
                        if (newline_after == false) {
                            cursorX = endx;
                        }
                        else{
                            cursorX += strlen;
                        }
                    } else {
                        cursorX += (align == HexPDF.RIGHT) ? endx : cursorX + strlen;
                    }
                }
                else {
                    cursorX += strlen;
                }

                i += num;
            }
        }
        height = lineSep + cystart - cursorY;
        setCursor(cxstart, cystart);
        return height;
    }

    // Estimate heght of a table row. Used for determining page breaks within
    // a table
    private float rowHeight(float x, float y, float[] w, Object[] cells, int[] flags){
        float maxh=0, thish, cellx = x;
        for (int i = 0; i < cells.length; i++){
            thish = (elemHeight(cells[i], cellx, w[i], flags[i]));
            cellx += w[i];
            maxh = (thish > maxh)?thish:maxh;
        }
        return maxh;
    }
            
    /**
     * Draw a text from the current cursor position. The text can be multi-line
     * and even multi-page. When crossing page boundaries
     * (<code>contentEndY</code>) a new page is created. Newline characters
     * force explicit lineshift. Normal word-wrap is performed to keep the text
     * between the stated boundaries <code>startx</code> and <code>endx</code>
     *
     * @param txt The text to be drawn
     * @param startx Left edge of area available for drawing
     * @param endx Right edge of area available for drawing
     * @param flags One of
     * <code>HexPDF.LEFT | HexPDF.CENTER | HexPDF.RIGHT | HexPDF.JUSTIFY</code>
     * for text alignment between startx and endx
     * @return Actual height of the string drawn
     *
     * @see #drawText(java.lang.String, int)
     * @see #drawText(java.lang.String, float, float, int)
     */
    protected float _drawText(String txt, float startx, float endx, int flags) {
        float cystart = cursorY;
        int align = HexPDF.LEFT;
        if ((flags & HexPDF.CENTER) > 0) {
            align = HexPDF.CENTER;
        } else if ((flags & HexPDF.RIGHT) > 0) {
            align = HexPDF.RIGHT;
        } else if ((flags & HexPDF.JUSTIFY) > 0) {
            align = HexPDF.JUSTIFY;
        }
        if (txt == null || txt.isEmpty()){
            return 0;
        }
        txt = txt.replace("\n", " " + HexPDF.TXT_NEWLINE + " ");
        String[] words = txt.split("\\s+");
        int i = 0;
        float height = 0;
        while (i < words.length) {
            int num = makeLine(words, i, endx - cursorX);
            if (num == -1) { // newline
                i++;
                cursorX = startx;
                cursorY -= lineSep;
                // New page?
                if (ignorePagebleed == false && ((cursorY - lineSep) < contentEndY)) {
                    newPage();
                    cursorX = startx;
                }
                height += lineSep;
            } else if (num == 0) {
                if (cursorX > startx) {
                    // Something on line from start. Try a newline first, then recheck.
                    cursorX = startx;
                    cursorY -= lineSep;
                    // New page?
                    if (ignorePagebleed == false && ((cursorY - lineSep) < contentEndY)) {
                        newPage();
                        cursorX = startx;
                    }
                } else {
                    // a single word is too big for the box. Draw it!
                    doDrawText(words[i]);
                    cursorY -= lineSep;
                    cursorX = startx;
                    i++;
                    // New page?
                    if (ignorePagebleed == false && ((cursorY - lineSep) < contentEndY)) {
                        newPage();
                        cursorX = startx;
                    }
                }
            } else {
                String toDraw = join(words, i, num);
                if ((cursorX == startx) && (align == HexPDF.RIGHT || align == HexPDF.CENTER || align == HexPDF.JUSTIFY)) {
                    float strlen = textWidth(toDraw);
                    float space = endx - startx - strlen;
                    boolean newline_after = ((i + num) >= words.length || words[i + num].equals(HexPDF.TXT_NEWLINE));
                    if (align == HexPDF.JUSTIFY) {
                        if (newline_after == false) {
                            // Only justify if this is the not last line of the paragraph.
                            try {
                                cs.appendRawCommands(String.format("%f Tc\n", space / (toDraw.length() - 1)).replace(',', '.'));
                                doDrawText(toDraw);
                                toDraw = null;
                                cs.appendRawCommands("0 Tc\n");
                            } catch (IOException ex) {
                                Logger.getLogger(HexPDF.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } else {
                        cursorX += (align == HexPDF.RIGHT) ? space : space / 2;
                    }
                }
                if (toDraw != null) {
                    doDrawText(toDraw);
                }
                i += num;
            }
        }
        return lineSep + cystart - cursorY;
    }

    /**
     * Draw a text from the current cursor position. The text can be multi-line
     * and even multi-page. When crossing page boundaries
     * (<code>contentEndY</code>) a new page is automatically created. Newline
     * characters force explicit lineshift. Normal word-wrap is performed to
     * keep the text between the current <code>leftMargin</code> and
     * <code>rightMargin</code>
     *
     * @param txt The text to be drawn
     * @param flags One of
     * <code>HexPDF.LEFT | HexPDF.CENTER | HexPDF.RIGHT | HexPDF.JUSTIFY</code>
     * for text alignment between startx and endx
     * @return Actual height of the string drawn
     * @see #drawText(java.lang.String, float, float, int)
     * @see #_drawText(java.lang.String, float, float, int)
     */
    public float drawText(String txt, int flags) {
        return _drawText(txt, contentStartX, contentEndX, flags);
    }

    /**
     * Draw a left-aligned text from the current cursor position. The text can
     * be multi-line and even multi-page. When crossing page boundaries
     * (<code>contentEndY</code>) a new page is automatically created. Newline
     * characters force explicit lineshift. Normal word-wrap is performed to
     * keep the text between the current <code>leftMargin</code> and
     * <code>rightMargin</code>
     *
     * @param txt The text to be drawn
     * @return height of text drawn
     * @see #drawText(java.lang.String, int)
     * @see #_drawText(java.lang.String, float, float, int)
     */
    public float drawText(String txt) {
        return drawText(txt, HexPDF.LEFT);
    }

    /**
     * Draw a text from the specified position. The text can be multi-line and
     * even multi-page. When crossing page boundaries (<code>contentEndY</code>)
     * a new page is automatically created. Newline characters force explicit
     * lineshift. Normal word-wrap is performed to keep the text between the
     * current <code>leftMargin</code> and <code>rightMargin</code>
     *
     * @param txt The text to be drawn
     * @param x Starting x-position for the text
     * @param y Starting y-position for the text
     * @param flags One of
     * <code>HexPDF.LEFT | HexPDF.CENTER | HexPDF.RIGHT | HexPDF.JUSTIFY</code>
     * for text alignment between startx and endx
     * @return Actual height of the string drawn
     *
     * @see #drawText(java.lang.String, int)
     * @see #_drawText(java.lang.String, float, float, int)
     */
    public float drawText(String txt, float x, float y, int flags) {
        setCursor(x, y);
        return drawText(txt, flags);
    }

    /**
     * Draw an image starting at current cursor location. If no flags are given,
     * the top-left corner of the image is positioned at the current cursor
     * position. If one of the flags
     * <code>HexPDF.LEFT | HexPDF.CENTER | HexPDF.RIGHT</code> is set, the image
     * is adjusted between <code>leftMargin, rightMargin</code> accordingly. The
     * cursor location is kept unchanged unless the flag
     * <code>HexPDF.NEWLINE</code> is set. This is useful for adding pictures as
     * layers. If <code>HexPDF.NEWLINE</code> is set, the cursor is positioned
     * at <code>leftMargin</code> immediately below the image.
     *
     * @param image the image to be added
     * @param flags see description
     */
    public void drawImage(BufferedImage image, int flags) {
        PDXObjectImage ximage = null;
        float imW = 0;
        float imH = 0;
        try {
            ximage = new PDPixelMap(this, image);
            imW = ximage.getWidth();
            imH = ximage.getHeight();
        } catch (IOException ex) {
            Logger.getLogger(HexPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
        // newpage if image cannot fit on rest of current page
        if ((cursorY - imH) < contentEndY){
            newPage();
        }
        float imgX = cursorX;
        float imgY = cursorY - imH;
        if ((flags & HexPDF.CENTER) > 0) {
            imgX = contentStartX+((contentWidth - imW) / 2);
        } else if ((flags & HexPDF.LEFT) > 0) {
            imgX = contentStartX;
        } else if ((flags & HexPDF.RIGHT) > 0) {
            imgX = contentEndX - imW;
        }

        try {
            cs.drawXObject(ximage, imgX, imgY, imW, imH);
        } catch (IOException ex) {
            Logger.getLogger(HexPDF.class.getName()).log(Level.SEVERE, null, ex);
        }

        if ((flags & HexPDF.NEWLINE) > 0) {
            setCursor(contentStartX, imgY - lineSep);
        }
    }

    /**
     * Draw an image at the given location. If no flags are given, the top-left
     * corner of the image is positioned at the specified <code>x, y</code>
     * location. If one of the flags
     * <code>HexPDF.LEFT | HexPDF.CENTER | HexPDF.RIGHT</code> is set, the image
     * is adjusted horizontally between <code>leftMargin, rightMargin</code>
     * accordingly. The top of the image will always be at the given
     * <code>y</code> position. The cursor location is kept unchanged unless the
     * flag <code>HexPDF.NEWLINE</code> is set. This is useful for adding
     * pictures as layers. If <code>HexPDF.NEWLINE</code> is set, the cursor is
     * positioned at <code>leftMargin</code> immediately below the image.
     *
     * @param image The image to be added
     * @param x wanted x-value of image top-left corner on the page
     * @param y wanted y-value of image top-left corner on the page
     * @param flags see description
     */
    public void drawImage(BufferedImage image, float x, float y, int flags) {
        setCursor(x, y);
        drawImage(image, flags);
    }

    // TABLE functions
    
    // Add a text cell to table
    private float addCell(float x, float y, float w, String txt, int flags) {
        setCursor(x + tableCellMargin, y - 0.8f * lineSep);
        return _drawText(txt, x + tableCellMargin, x + w - tableCellMargin, flags);
    }
    // Add an image cell to table
    private float addCell(float x, float y, float w, BufferedImage image, int flags) {
        if ((flags & HexPDF.CENTER) > 0){
            setCursor(x + 0.5f *(w - image.getWidth()), y);// - tableCellMargin);
        }
        else if((flags & HexPDF.RIGHT) > 0){
            setCursor(x + w - image.getWidth(), y);// - tableCellMargin);
        }
        else{
            setCursor(x + tableCellMargin, y);// - tableCellMargin);
        }
        drawImage(image, 0);
        return (image.getHeight());// + 2 * tableCellMargin);
    }

    private void addCellBorder(float x, float y, float w, float h) {
        try {
            cs.drawLine(x, y, x + w, y);
            cs.drawLine(x + w, y, x + w, y - h);
            cs.drawLine(x + w, y - h, x, y - h);
            cs.drawLine(x, y - h, x, y);
        } catch (IOException ex) {
            Logger.getLogger(HexPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private float addRow(float x, float y, float[] w, Object[] cells, int[] flags) {
        float maxh = 0;
        float thish = 0;
        float cellx = x;
        int i;
        for (i = 0; i < w.length; i++) {
            thish = 0;
            if (cells[i] instanceof String){
                thish = addCell(cellx, y, w[i], (String)cells[i], flags[i]);
            }
            else if (cells[i] instanceof BufferedImage){
                thish = addCell(cellx, y, w[i], (BufferedImage)cells[i], flags[i]);
            }
            cellx += w[i];
            maxh = (thish > maxh) ? thish : maxh;
        }
        cellx = x;
        for (i = 0; i < w.length; i++) {
            addCellBorder(cellx, y, w[i], maxh);
            cellx += w[i];
        }
        return maxh;
    }

    /**
     * Add a table to the document starting at current cursor location. The
     * input table must be a two-dimensional array of <code>Object</code>, all
     * rows must have the same number of columns.
     *
     * If the table extends beyond the page boundary <code>contentEndY</code> a
     * new page is automatically created and the table continues.
     *
     * Normal word-wrap is performed within each cell if the text is longer than
     * the column´ designated width.
     * 
     * A table element (Object) can be either a String or a BufferedImage
     *
     * @param table the table data. Objects should be String or BufferedImage
     * @param column_width array of column widths
     * @param column_flag array of flags for text alignment within columns, one
     * of <code>HexPDF.LEFT | HexPDF.CENTER | HexPDF.RIGHT</code>
     * @param table_align flag for alignment of the table itself
     * @return the height of the table - if multipage then return the height on
     * the last page.
     */
    public float drawTable(Object[][] table, float[] column_width, int[] column_flag, int table_align) {
        float tabheight = 0;
        float rowheight = 0;
        float table_width = 0;
        boolean oldIgnoreBleed = ignorePagebleed;
        ignorePagebleed = true; // No unintended page-breaks while adding table
        for (float colwidth : column_width) {
            table_width += colwidth;
        }
        float free_space = this.contentWidth - table_width;

        float x = cursorX;
        float y = cursorY;

        if (table_align == HexPDF.CENTER || table_align == HexPDF.RIGHT) {
            x += ((table_align == HexPDF.CENTER) ? free_space / 2 : free_space);
        }
        int rownum = 1;
        for (Object[] row : table) {
            if (row != null) {
                // Can the next row it fit on same page? Find the height of next
                // row and make a new page after adding this row if necessary.
                float guessRowHeight = 0;
                if (rownum < table.length){
                    guessRowHeight = rowHeight(x, contentStartY, column_width, table[rownum], column_flag);
                }
                rowheight = addRow(x, y - tabheight, column_width, row, column_flag);
                tabheight += rowheight;
                // Ne page before next row?
                if ((y - tabheight - guessRowHeight) < contentEndY){
                    newPage();
                    tabheight = 0;
                    y = contentStartY;
                }
            }
            rownum++;
        }
        cursorX = contentStartX;
        cursorY -= (rowheight + tableCellMargin);
        ignorePagebleed = oldIgnoreBleed;
        return tabheight;
    }

    // Setters and getters
    /**
     * Set current font-size and color to a title-1 style.
     *
     * @see #title1FontSize
     * @see #titleColor
     */
    public void title1Style() {
        setFontSize(title1FontSize);
        setTextColor(titleColor);
    }

    /**
     * Set current font-size and color to a title-2 style.
     *
     * @see #title2FontSize
     * @see #titleColor
     */
    public void title2Style() {
        setFontSize(title2FontSize);
        setTextColor(titleColor);
    }

    /**
     * Set current font-size and color to normal style.
     *
     * @see #normalFontSize
     * @see #normalColor
     */
    public void normalStyle() {
        setFontSize(normalFontSize);
        setTextColor(normalColor);
    }

    /**
     * Retrieve the currently active font size in points.
     *
     * @return current font size
     */
    public float getFontSize() {
        return fontSize;
    }

    /**
     * Set font size in points.
     * Normally you should not use this directly.
     * Instead, do setNormalFontSize() followed by normalStyle()
     *
     * @param fs font size in points
     */
    public void setFontSize(float fs) {
        this.fontSize = fs;
        if (currentPage != null){
            try {
                cs.setFont(font, fontSize);
            } catch (IOException ex) {
                Logger.getLogger(HexPDF.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        lineSep = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
    }

    /**
     * Retrieve the currently active font.
     *
     * @return current font as PDFont
     *
     * @see PDFont
     */
    public PDFont getFont() {
        return font;
    }

    /**
     * Set font type to use.
     *
     * @param font font to use, PDFont type
     *
     * @see PDFont
     */
    public void setFont(PDFont font) {
        this.font = font;
        try {
            cs.setFont(font, fontSize);
        } catch (IOException ex) {
            Logger.getLogger(HexPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
        lineSep = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
    }

    /**
     * Retrieve the height of the top margin.
     *
     * @return height of top margin in points
     */
    public float getTopMargin() {
        return topMargin;
    }

    /**
     * Set height of top margin. Height of writable area (contentHeight) is
     * adjusted accordingly.
     *
     * @param topMargin new height of top margin in points
     */
    public void setTopMargin(float topMargin) {
        this.topMargin = topMargin;
        setDimensions();
    }

    /**
     * Retrieve the height of bottom margin in points.
     *
     * @return height of bottom margin in points
     */
    public float getBottomMargin() {
        return bottomMargin;
    }

    /**
     * Set height of bottom margin. Height of writable area (contentHeigth) is
     * adjusted accordingly.
     *
     * @param bottomMargin new height of top margin in points
     */
    public void setBottomMargin(float bottomMargin) {
        this.bottomMargin = bottomMargin;
        setDimensions();
    }

    /**
     * Retrieve the width of left margin in points.
     *
     * @return width of left margin in points
     */
    public float getLeftMargin() {
        return leftMargin;
    }

    /**
     * Set width of left margin. Width of writable area (contentWidth) is
     * adjusted accordingly.
     *
     * @param leftMargin new height of top margin in points
     */
    public void setLeftMargin(float leftMargin) {
        this.leftMargin = leftMargin;
        setDimensions();
    }

    /**
     * Retrieve the width of right margin in points.
     *
     * @return width of right margin in points
     */
    public float getRightMargin() {
        return rightMargin;
    }

    /**
     * Retrieve the height of current page.
     *
     * @return page height in points
     */
    public float getPageHeight() {
        return pageHeight;
    }

    /**
     * Retrieve the width of current page.
     *
     * @return page width in points
     */
    public float getPageWidth() {
        return pageWidth;
    }

    /**
     * Retrieve the height of writable area (witin margins) of current page.
     *
     * @return height of page content area in points
     */
    public float getContentHeight() {
        return contentHeight;
    }

    /**
     * Retrieve the width of writable area (witin margins) of current page.
     *
     * @return width of page content area in points
     */
    public float getContentWidth() {
        return contentWidth;
    }

    /**
     * Set width of right margin. Width of writable area (contentWidth) is
     * adjusted accordingly.
     *
     * @param rightMargin new height of top margin in points
     */
    public void setRightMargin(float rightMargin) {
        this.rightMargin = rightMargin;
        setDimensions();
    }

    /**
     * Set the font size to be used for normal style.
     *
     * @param fontSize font size in points.
     *
     * @see #normalStyle()
     * @see #DEFAULT_NORMAL_FONT_SIZE
     */
    public void setNormalFontSize(float fontSize) {
        normalFontSize = fontSize;
    }

    /**
     * Set the font size to be used for title 1 style.
     *
     * @param fontSize font size in points.
     *
     * @see #title1Style()
     * @see #DEFAULT_TITLE1_FONT_SIZE
     */
    public void setTitle1FontSize(float fontSize) {
        title1FontSize = fontSize;
    }

    /**
     * Set the font size to be used for title 2 style.
     *
     * @param fontSize font size in points.
     *
     * @see #title2Style()
     * @see #DEFAULT_TITLE2_FONT_SIZE
     */
    public void setTitle2FontSize(float fontSize) {
        title2FontSize = fontSize;
    }

    /**
     * Set the margin to be used between table borders and table text
     *
     * @param cellMargin table margin in points
     *
     * @see #DEFAULT_TABLE_CELL_MARGIN
     */
    public void settableCellMargin(float cellMargin) {
        tableCellMargin = cellMargin;
    }

    /**
     * Returns the underlying PDPageContentStream from pdfBox. Note that the
     * PDPageContentStream will change whenever a new page is created, it is
     * hence important to call getPDPageContentStream() before any direct
     * operation on the content stream.
     *
     * @return PDPageContentStream in use
     */
    public PDPageContentStream getPDPageContentStream() {
        return cs;
    }

    /**
     * Set text color.
     * This will be reset if you change style.
     * @see #setTitleColor(java.awt.Color) 
     * @see #setNormalColor(java.awt.Color) 
     * @param color the new text color
     */
    public void setTextColor(Color color) {
        try {
            cs.setNonStrokingColor(color);
            //textColor = color;
        } catch (IOException ex) {
            Logger.getLogger(HexPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get the footer set for the document.
     * @return Footer
     */
    public Footer getFooter() {
        return footer;
    }

    /**
     * Set footer to use on all pages.
     * The footer is added when finalizing the document.
     * @param footer    Footer to use
     * @see net.heksemann.hexpdf.Footer
     */
    public void setFooter(Footer footer) {
        this.footer = footer;
    }

    /**
     * Get the color currently used for title style writing.
     * @return  Color
     */
    public Color getTitleColor() {
        return titleColor;
    }

    /**
     * Set the color to use for title style writing.
     * @param titleColor    Color
     */
    public void setTitleColor(Color titleColor) {
        this.titleColor = titleColor;
    }

    /**
     * Get the color currently used for normal style writing.
     * @return color    Color
     */
    public Color getNormalColor() {
        return normalColor;
    }

    /**
     * Set the color to use for normal style writing.
     * @param color Color
     */
    public void setNormalColor(Color color) {
        this.normalColor = color;
    }

    /**
     * Return the left boundary of the page content area.
     * @return x-coordinate points
     */
    public float getContentStartX() {
        return contentStartX;
    }

    /**
     * Return the upper boundary of the page content area.
     * @return y-coordinate points
     */
    public float getContentStartY() {
        return contentStartY;
    }

    /**
     * Return the right boundary of the page content area.
     * @return x-coordinate points
     */
    public float getContentEndX() {
        return contentEndX;
    }

    /**
     * Return the lower boundary of the page content area.
     * @return y-coordinate points
     */
    public float getContentEndY() {
        return contentEndY;
    }

    /**
     * Set page orientation.
     * Takes effect only next tine a newpage() is explicitly or implicitly called.
     * @param orientation   HexPDF.LANDSCAPE | HexPDF.PORTRAIT
     */
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    /**
     * Set page size to be used.
     * Takes effect on the next explicit or automatic newPage()
     * Default is <code>PDPage.PAGE_SIZE_A4</code>
     * @param pageSize  page dimension as <code>PDRectangle</code>
     * @see PDRectangle
     */
    public void setPageSize(PDRectangle pageSize) {
        this.pageSize = pageSize;
    }

}
