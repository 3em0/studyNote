package org.richfaces.demo.paint2d;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class PaintBean {

	   private int fontSize;

	   public void paint(Graphics2D g2d, Object obj) {
	   
		   PaintData data = (PaintData) obj;
		   
		   int testLenght = data.text.length();
		   fontSize = testLenght < 8? 40 : 40 - (testLenght - 8);
		    if (fontSize < 12)fontSize = 12;
		    Font font = new Font("Serif", Font.HANGING_BASELINE, fontSize);
		    g2d.setFont(font);
		   
		    int x = 10;
		    int y = fontSize*5/2;
		    g2d.translate(x, y);
		    Color color = new Color(data.color );
		    
		    g2d.setPaint(new Color(color.getRed(),color.getGreen(), color.getBlue(), 30));
		    AffineTransform origTransform = g2d.getTransform();
		    g2d.shear(-0.5*data.scale, 0);
		    g2d.scale(1, data.scale);
		    g2d.drawString(data.text, 0, 0);
		    
		    g2d.setTransform(origTransform);
		    g2d.setPaint(color);
		    g2d.drawString(data.text, 0, 0);
	   }
}
