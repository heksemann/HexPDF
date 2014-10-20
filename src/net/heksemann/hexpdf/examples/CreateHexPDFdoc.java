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
 * 
 * This file, CreateHexPDFdoc.java is an example showing basic HexPDF usage.
 *
 */

package net.heksemann.hexpdf.examples;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.heksemann.hexpdf.HexPDF;

/**
 * Create a simple, two-page, A4 document with text, images and a table.
 * Demonstrates usage of HexPDF
 * 
 * @author Frank J. Øynes, heksemann@gmail.com
 */
public class CreateHexPDFdoc {

    private void createDocument() {
        HexPDF doc = new HexPDF();

        BufferedImage basemap = getImage("http://upload.wikimedia.org/wikipedia/commons/thumb/a/ae/Dollnstein_-_Burgzugang.jpg/800px-Dollnstein_-_Burgzugang.jpg");
        BufferedImage overlay = getImage("http://upload.wikimedia.org/wikipedia/commons/thumb/4/47/PNG_transparency_demonstration_1.png/560px-PNG_transparency_demonstration_1.png");
        String[][] table = getTable("table.txt");

        // Add a main title, centered in shiny colours
        doc.title1Style();
        doc.setTextColor(new Color(0x2020ff));
        doc.drawText("A simple pdf document\n\n", HexPDF.CENTER);

        // Typeset everything else in boring black
        doc.setTextColor(Color.black);

        // Add a left aligned sub title
        doc.title2Style();
        doc.drawText("\n\nLeft aligned text\n\n");
        
        // Add some text, default left aligned
        doc.normalStyle();
        doc.drawText(getText(4));

        // Add a centered image with another image as overlay
        doc.drawImage(basemap, HexPDF.CENTER);
        doc.drawImage(overlay, HexPDF.CENTER | HexPDF.NEWLINE);

        // Add a figure caption centered under the image
        doc.drawText("Figure 1: An example figure with overlay\n", HexPDF.CENTER);

        // A second left aligned sub title
        doc.title2Style();
        doc.drawText("\n\nRight aligned text\n\n");

        // Add some more text - right aligned this time
        doc.normalStyle();
        doc.drawText(getText(3), HexPDF.RIGHT);

        // Add a table centered on page. Four columns, aligned left, center, right, left
        doc.drawTable(table,
                new float[]{100, 60, 70, 200},
                new int[]{HexPDF.LEFT, HexPDF.CENTER, HexPDF.RIGHT, HexPDF.LEFT}, 
                HexPDF.CENTER);
        // Add a caption under the table
        doc.drawText("Table 1: A reather odd table, centered on page\n", HexPDF.CENTER);

        // The final sub title, still left aligned
        doc.title2Style();
        doc.drawText("\n\nCentered text\n\n");

        // Add last block of text - centered
        doc.normalStyle();
        doc.drawText(getText(5), HexPDF.CENTER);

        // The end...
        doc.title1Style();
        doc.drawText("\n\n-- END OF DOCUMENT --", HexPDF.CENTER);

        // Save the document
        doc.finish("myHexPDFfile.pdf");
    }

    public CreateHexPDFdoc() {
        //
    }

    private BufferedImage getImage(String fn) {
        BufferedImage image = null;
        final int w = 400;
        final int h = 300;
        try {
            URL url = new URL(fn);
            BufferedImage src = ImageIO.read(url);
            Image scale = src.getScaledInstance(w, h, Image.SCALE_SMOOTH);

            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.getGraphics();
            g.drawImage(scale, 0, 0, null);
            g.dispose();
        } catch (IOException ex) {
            Logger.getLogger(CreateHexPDFdoc.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image;
    }

    private String getText(int num) {
        String[] txt = {"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Multa sunt dicta ab antiquis de contemnendis ac despiciendis rebus humanis; Duo Reges: constructio interrete. Id Sextilius factum negabat. Tum Quintus: Est plane, Piso, ut dicis, inquit.\n\n",
                        "Quasi vero, inquit, perpetua oratio rhetorum solum, non etiam philosophorum sit. Septem autem illi non suo, sed populorum suffragio omnium nominati sunt. Cur post Tarentum ad Archytam? Quia nec honesto quic quam honestius nec turpi turpius. Re mihi non aeque satisfacit, et quidem locis pluribus. Unum nescio, quo modo possit, si luxuriosus sit, finitas cupiditates habere. Ad quorum et cognitionem et usum iam corroborati natura ipsa praeeunte deducimur.\n\n",
                        "Qualem igitur hominem natura inchoavit? Tum ille timide vel potius verecunde: Facio, inquit. Cur iustitia laudatur? Haec bene dicuntur, nec ego repugno, sed inter sese ipsa pugnant. Erit enim mecum, si tecum erit. Ab hoc autem quaedam non melius quam veteres, quaedam omnino relicta.\n\n",
                        "Vestri haec verecundius, illi fortasse constantius. Si quicquam extra virtutem habeatur in bonis. Utinam quidem dicerent alium alio beatiorem! Iam ruinas videres. At certe gravius. Aliter enim nosmet ipsos nosse non possumus. Suo enim quisque studio maxime ducitur.\n\n",
                        "Ut non sine causa ex iis memoriae ducta sit disciplina. Sint ista Graecorum; Sed nonne merninisti licere mihi ista probare, quae sunt a te dicta? Dicimus aliquem hilare vivere; Quamquam tu hanc copiosiorem etiam soles dicere. Sed vos squalidius, illorum vides quam niteat oratio. Si mala non sunt, iacet omnis ratio Peripateticorum. Hoc non est positum in nostra actione. Quae cum magnifice primo dici viderentur, considerata minus probabantur. Honesta oratio, Socratica, Platonis etiam.\n\n"};

        String ret = "";
        for (int i = 0; i < num && i < txt.length; i++){
            ret += txt[i];
        }
        return ret;
    }

    private String[][] getTable(String fn) {
        String[][] tab = {
            {"Country", "Area", "Population", "Info"},
            {"Norway", "col2", "col2", "col4"},
            {"Sweden", "col2", "col2", "col4"},
            {"Denmark", "col2", "col2", "col4"},
            {"Vietnam", "col2", "col2", "col4"},
            {"Thaland", "col2", "col2", "col4"},
            {"Burma", "col2", "col2", "col4"},
            {"USA", "col2", "col2", "col4"},
            {"Germany", "col2", "col2", "col4"}
        };

        return tab;
    }
 
    public static void main(String[] args){
        CreateHexPDFdoc hex = new CreateHexPDFdoc();
        hex.createDocument();
    }
}
