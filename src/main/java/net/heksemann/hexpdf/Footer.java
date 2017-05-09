/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.heksemann.hexpdf;

import java.awt.Color;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 *
 * @author Frank J. Øynes, heksemann@gmail.com
 */
public class Footer {

    private Color textColor;
    private String leftText;
    private String rightText;
    private String centerText;
    private boolean OMIT_FIRSTPAGE;
    private boolean COUNT_FIRSTPAGE;
    private PDFont font;
    private float fontsize;

    /**
     * Keyword replaced by the current page number. if COUNT_FIRSTPAGE is false,
     * the first page will be number zero, otherwise 1.
     *
     * @see #setCOUNT_FIRSTPAGE(boolean)
     */
    public static final String PAGENUM = "${PAGE}";

    /**
     * Keyword replaced by the current date. Format is fixed to "dd MMM yyyy"
     */
    public static final String DATE = "${DATE}";

    /**
     * Keyword replaced by name of the currently logged in user.
     */
    public static final String USER = "${USER}";
    /**
     * Keyword replaced by the total number of pages in the document. If
     * COUNT_FIRSTPAGE is false, the front page is omittet from the count.
     *
     * @see #setCOUNT_FIRSTPAGE(boolean)
     */
    public static final String NUMPAGES = "${NUMPAGES}";

    /**
     * Convenience function for returning a default footer style. The default
     * footer typesets the footer in gray TIMES_BOLD, sz 8. The left part of the
     * text is todays date, center is username, and the right part is PAGENUM of
     * NUMPAGES
     */
    public static Footer defaultFooter = new Footer();

    /**
     * Sole constructor.
     * Return a footer in gray TIMES_BOLD, sz 8.
     * The left part of the text is todays date, 
     * center is username, 
     * and the right part is PAGENUM of NUMPAGES
     */
    public Footer() {
        textColor = Color.gray;
        leftText = Footer.DATE;
        centerText = Footer.USER;
        rightText = "Page " + Footer.PAGENUM + " of " + Footer.NUMPAGES;
        OMIT_FIRSTPAGE = true;
        COUNT_FIRSTPAGE = true;
        font = PDType1Font.TIMES_BOLD;
        fontsize = 8;
    }

    /**
     * Gets currently selected text color for the footer.
     *
     * @return color, default is java.awt.Color.gray
     */
    public Color getTextColor() {
        return textColor;
    }

    /**
     * Set the color to use for footer text.
     *
     * @param textColor new text color
     */
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    /**
     * The footer is divided in three parts - this sets the left part.
     *
     * @param leftText left part of footer text
     * @see #PAGENUM
     * @see #NUMPAGES
     */
    public void setLeftText(String leftText) {
        this.leftText = leftText;
    }

    /**
     * The footer is divided in three parts - this sets the right part.
     *
     * @param rightText right part of footer text
     * @see #PAGENUM
     * @see #NUMPAGES
     */
    public void setRightText(String rightText) {
        this.rightText = rightText;
    }

    /**
     * The footer is divided in three parts - this sets the center part.
     *
     * @param centerText center part of footer text
     * @see #PAGENUM
     * @see #NUMPAGES
     */
    public void setCenterText(String centerText) {
        this.centerText = centerText;
    }

    /**
     * Tells whether the first page should have footer or not.
     *
     * @return true or false, default is true
     */
    public boolean isOMIT_FIRSTPAGE() {
        return OMIT_FIRSTPAGE;
    }

    /**
     * Set this flag to false to include footer also on first page
     *
     * @param OMIT_FIRSTPAGE    true or false
     */
    public void setOMIT_FIRSTPAGE(boolean OMIT_FIRSTPAGE) {
        this.OMIT_FIRSTPAGE = OMIT_FIRSTPAGE;
    }

    /**
     * Tells whether the first page is included in total page count or not.
     *
     * @return true or false, default is true
     *
     * @see #PAGENUM
     * @see #NUMPAGES
     */
    public boolean isCOUNT_FIRSTPAGE() {
        return COUNT_FIRSTPAGE;
    }

    /**
     * Set this flag to false to omit the first page from total page count.
     *
     * @param COUNT_FIRSTPAGE   true or false
     * @see #PAGENUM
     * @see #NUMPAGES
     */
    public void setCOUNT_FIRSTPAGE(boolean COUNT_FIRSTPAGE) {
        this.COUNT_FIRSTPAGE = COUNT_FIRSTPAGE;
    }

    /**
     * Returns the font selected for footer text.
     *
     * @return currently selected font, default PDType1Font.TIMES_BOLD
     */
    public PDFont getFont() {
        return font;
    }

    /**
     * Set the font to use for footer text.
     *
     * @param font font size in points
     */
    public void setFont(PDFont font) {
        this.font = font;
    }

    /**
     * Returns the font size selected for footer text.
     *
     * @return font size in points, default is 8
     */
    public float getFontsize() {
        return fontsize;
    }

    /**
     * Set font size in points for use in footer text
     *
     * @param fontsize font size in points
     */
    public void setFontsize(float fontsize) {
        this.fontsize = fontsize;
    }

    /**
     * Return the currently selected left part of footer.
     *
     * @return left part of footer, default is DATE
     */
    public String getLeftText() {
        return leftText;
    }

    /**
     * Return the currently selected right part of footer.
     *
     * @return right part of footer, default is PAGENUM of NUMPAGES
     */
    public String getRightText() {
        return rightText;
    }

    /**
     * Return the currently selected center part of footer.
     *
     * @return center part of footer, default is USER
     */
    public String getCenterText() {
        return centerText;
    }

}
