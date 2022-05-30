package org.richfaces.demo.media;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;

public class MediaBean {

	public void paint(OutputStream out, Object data) throws IOException{
		if (data instanceof MediaData) {
			
		MediaData paintData = (MediaData) data;
		BufferedImage img = new BufferedImage(paintData.getWidth(),paintData.getHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = img.createGraphics();
		graphics2D.setBackground(paintData.getBackground());
		graphics2D.setColor(paintData.getDrawColor());
		graphics2D.clearRect(0,0,paintData.getWidth(),paintData.getHeight());
		graphics2D.drawLine(5,5,paintData.getWidth()-5,paintData.getHeight()-5);
		graphics2D.drawChars(new String("RichFaces").toCharArray(),0,9,40,15);
		graphics2D.drawChars(new String("mediaOutput").toCharArray(),0,11,5,45);
		
		ImageIO.write(img,"jpeg",out);
		
		}
	}
}