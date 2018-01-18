package ramo.klevis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.Executors;

/**
 * Created by klevis.ramo on 1/18/2018.
 */
public class UI {

    private static final int FRAME_WIDTH = 400;
    private static final int FRAME_HEIGHT = 120;
    private JFrame mainFrame;
    private JPanel mainPanel;
    private File selectedFile = new File("resources/videoSample.mp4");
    private CarVideoDetection carVideoDetection;
    private ProgressBar progressBar;


    public void initUI() throws Exception {

        mainFrame = createMainFrame();
        mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton chooseVideo = new JButton("Choose Video");
        chooseVideo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFileAction();
            }
        });
        mainPanel.add(chooseVideo);
        JButton start = new JButton("Start Detection!");
        start.addActionListener(e -> {
            progressBar = new ProgressBar(mainFrame);
            SwingUtilities.invokeLater(() -> progressBar.showProgressBar("Detecting video..."));
            Executors.newCachedThreadPool().submit(() -> {
                try {
                    carVideoDetection = new CarVideoDetection();
                    carVideoDetection.startRealTimeVideoDetection(selectedFile.getAbsolutePath());
                } catch (Exception e1) {
                    throw new RuntimeException(e1);
                } finally {
                    progressBar.setVisible(false);
                }
            });
        });
        mainPanel.add(start);
        JButton stop = new JButton("Stop");
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                carVideoDetection.stop();

                progressBar.setVisible(false);
            }
        });
        mainPanel.add(stop);
        addSignature();

        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.setVisible(true);
    }

    public void chooseFileAction() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(new File("resources").getAbsolutePath()));
        int action = chooser.showOpenDialog(null);
        if (action == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();

        }
    }

    private JFrame createMainFrame() {
        JFrame mainFrame = new JFrame();
        mainFrame.setTitle("Autonomous Driving");
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });
        ImageIcon imageIcon = new ImageIcon("icon.png");
        mainFrame.setIconImage(imageIcon.getImage());

        return mainFrame;
    }

    private void addSignature() {
        JLabel signature = new JLabel("ramok.tech", JLabel.HORIZONTAL);
        signature.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 20));
        signature.setForeground(Color.BLUE);
        mainFrame.add(signature, BorderLayout.SOUTH);
    }
}
