package org.mozilla.javascript.tools.debugger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;

class FileHeader extends JPanel implements MouseListener {
   private static final long serialVersionUID = -2858905404778259127L;
   private int pressLine = -1;
   private FileWindow fileWindow;

   public FileHeader(FileWindow fileWindow) {
      super();
      this.fileWindow = fileWindow;
      this.addMouseListener(this);
      this.update();
   }

   public void update() {
      FileTextArea textArea = this.fileWindow.textArea;
      Font font = textArea.getFont();
      this.setFont(font);
      FontMetrics metrics = this.getFontMetrics(font);
      int h = metrics.getHeight();
      int lineCount = textArea.getLineCount() + 1;
      String dummy = Integer.toString(lineCount);
      if (dummy.length() < 2) {
         dummy = "99";
      }

      Dimension d = new Dimension();
      d.width = metrics.stringWidth(dummy) + 16;
      d.height = lineCount * h + 100;
      this.setPreferredSize(d);
      this.setSize(d);
   }

   public void paint(Graphics g) {
      super.paint(g);
      FileTextArea textArea = this.fileWindow.textArea;
      Font font = textArea.getFont();
      g.setFont(font);
      FontMetrics metrics = this.getFontMetrics(font);
      Rectangle clip = g.getClipBounds();
      g.setColor(this.getBackground());
      g.fillRect(clip.x, clip.y, clip.width, clip.height);
      int ascent = metrics.getMaxAscent();
      int h = metrics.getHeight();
      int lineCount = textArea.getLineCount() + 1;
      String dummy = Integer.toString(lineCount);
      if (dummy.length() < 2) {
         dummy = "99";
      }

      int startLine = clip.y / h;
      int endLine = (clip.y + clip.height) / h + 1;
      int width = this.getWidth();
      if (endLine > lineCount) {
         endLine = lineCount;
      }

      for(int i = startLine; i < endLine; ++i) {
         int pos = -2;

         try {
            pos = textArea.getLineStartOffset(i);
         } catch (BadLocationException var22) {
         }

         boolean isBreakPoint = this.fileWindow.isBreakPoint(i + 1);
         String text = Integer.toString(i + 1) + " ";
         int y = i * h;
         g.setColor(Color.blue);
         g.drawString(text, 0, y + ascent);
         int x = width - ascent;
         if (isBreakPoint) {
            g.setColor(new Color(128, 0, 0));
            int dy = y + ascent - 9;
            g.fillOval(x, dy, 9, 9);
            g.drawOval(x, dy, 8, 8);
            g.drawOval(x, dy, 9, 9);
         }

         if (pos == this.fileWindow.currentPos) {
            Polygon arrow = new Polygon();
            int dx = x;
            y += ascent - 10;
            int dy = y;
            arrow.addPoint(x, y + 3);
            arrow.addPoint(x + 5, y + 3);

            for(int var25 = x + 5; var25 <= dx + 10; ++y) {
               arrow.addPoint(var25, y);
               ++var25;
            }

            for(int var26 = dx + 9; var26 >= dx + 5; ++y) {
               arrow.addPoint(var26, y);
               --var26;
            }

            arrow.addPoint(dx + 5, dy + 7);
            arrow.addPoint(dx, dy + 7);
            g.setColor(Color.yellow);
            g.fillPolygon(arrow);
            g.setColor(Color.black);
            g.drawPolygon(arrow);
         }
      }

   }

   public void mouseEntered(MouseEvent e) {
   }

   public void mousePressed(MouseEvent e) {
      Font font = this.fileWindow.textArea.getFont();
      FontMetrics metrics = this.getFontMetrics(font);
      int h = metrics.getHeight();
      this.pressLine = e.getY() / h;
   }

   public void mouseClicked(MouseEvent e) {
   }

   public void mouseExited(MouseEvent e) {
   }

   public void mouseReleased(MouseEvent e) {
      if (e.getComponent() == this && (e.getModifiers() & 16) != 0) {
         int y = e.getY();
         Font font = this.fileWindow.textArea.getFont();
         FontMetrics metrics = this.getFontMetrics(font);
         int h = metrics.getHeight();
         int line = y / h;
         if (line == this.pressLine) {
            this.fileWindow.toggleBreakPoint(line + 1);
         } else {
            this.pressLine = -1;
         }
      }

   }
}
