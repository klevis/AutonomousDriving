package ramo.klevis;

import javax.swing.*;
import java.awt.*;

/**
 * Created by klevis.ramo on 11/29/2017.
 */
public class ProgressBar {

    private final JFrame mainFrame;
    private JProgressBar progressBar;
    private boolean unDecoreate = false;

    public ProgressBar(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        progressBar = createProgressBar(mainFrame);
    }

    public ProgressBar(JFrame mainFrame, boolean unDecoreate) {
        this.mainFrame = mainFrame;
        progressBar = createProgressBar(mainFrame);
        this.unDecoreate = unDecoreate;
    }

    public void showProgressBar(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (unDecoreate) {
                mainFrame.setLocationRelativeTo(null);
                mainFrame.setUndecorated(true);
            }
            progressBar = createProgressBar(mainFrame);
            progressBar.setString(msg);
            progressBar.setStringPainted(true);
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            mainFrame.add(progressBar, BorderLayout.NORTH);
            if (unDecoreate) {
                mainFrame.pack();
                mainFrame.setVisible(true);
            }
            mainFrame.repaint();
        });
    }


    private JProgressBar createProgressBar(JFrame mainFrame) {
        JProgressBar jProgressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        jProgressBar.setVisible(false);
        mainFrame.add(jProgressBar, BorderLayout.NORTH);
        return jProgressBar;
    }

    public void setVisible(boolean visible) {
        progressBar.setVisible(visible);
    }
}
