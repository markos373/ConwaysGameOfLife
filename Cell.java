import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.border.LineBorder;

public class Cell extends JComponent{
	private int isLive;
	public Cell(Color c) {
		this.setBorder(new LineBorder(Color.black,5));
		this.setBackground(c);
	}
	public void setDead() {
		isLive = 0;
		this.setBackground(Color.white);
	}
	public void setLive(Color c) {
		isLive = 1;
		this.setBackground(c);
	}
	public void paint(Graphics g) {
		super.paintComponent(g);
		g.setColor(getBackground());
		g.fillRect(0,0,10,20);
	}
	
}
