package dev.wjteo;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MainFrame extends JFrame {
    private final JPanel contentPane = new JPanel();
    private final JPanel glassPane = new JPanel();

    private final MouseListener mouseListener = new MouseListener() {
        @Override
        public void mouseClicked(MouseEvent e) {
            e.consume();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            e.consume();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            e.consume();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            e.consume();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            e.consume();
        }
    };

    private final KeyListener keyListener = new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {
            e.consume();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            e.consume();
        }

        @Override
        public void keyReleased(KeyEvent e) {
            e.consume();
        }
    };

    public MainFrame() {
        initFrame();
        initContentPane();
        initGlassPane();
        pack();
        setVisible(true);
    }

    private void initFrame() {
        setResizable(false);
        setPreferredSize(new Dimension(600, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void initContentPane() {
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        JButton button = new JButton("TEST");
        button.addActionListener(e -> new ProgressDialog(this));

        JPanel panel = new JPanel();
        panel.add(button);
        contentPane.add(panel);
    }

    private void initGlassPane() {
        glassPane.setBackground(new Color(127, 127, 127, 127));
        glassPane.setOpaque(true);
        glassPane.setFocusable(true);
        glassPane.setVisible(false);
        glassPane.addMouseListener(mouseListener);
        glassPane.addKeyListener(keyListener);
        setGlassPane(glassPane);
    }

    public void setDimmed(boolean dim) {
        this.glassPane.setVisible(dim);
    }
}
