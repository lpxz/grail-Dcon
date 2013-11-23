package graphviz.lib;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class ShowImage extends Panel {
	BufferedImage image;

	public ShowImage() {
		try {
			System.out.println("Enter image name\n");
			BufferedReader bf = new BufferedReader(new InputStreamReader(
					System.in));
			String imageName = bf.readLine();
			File input = new File(imageName);
			image = ImageIO.read(input);
		} catch (IOException ie) {
			System.out.println("Error:" + ie.getMessage());
		}
	}
	
	public ShowImage(String imageName) {
		try {
			File input = new File(imageName);
			image = ImageIO.read(input);
		} catch (IOException ie) {
			System.out.println("Error:" + ie.getMessage());
		}
	}

	public static void showImageAPI(String imageName)
	{
		JFrame frame = new JFrame("Display image");
//		Panel panel = new ShowImage(imageName);
//		frame.setLocation(100, 100);
//		frame.setSize(640, 480);
//		frame.setLayout(new BorderLayout());
//		frame.add(panel, BorderLayout.CENTER);
//		//frame.getContentPane().add(panel);
//		frame.setVisible(true);		
		
	    ImageIcon ii = new ImageIcon(imageName);
	    JScrollPane jsp = new JScrollPane(new JLabel(ii));
	    frame.getContentPane().add(jsp);
	    frame.setSize(300, 250);
	    frame.setVisible(true);
		
	}
	public void paint(Graphics g) {
		g.drawImage(image, 0, 0, null);
	}

	static public void main(String args[]) throws Exception {
		JFrame frame = new JFrame("Display image");
		Panel panel = new ShowImage("out106.gif");
		frame.getContentPane().add(panel);
		frame.setSize(500, 500);
		frame.setVisible(true);
	}
}