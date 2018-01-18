package ramo.klevis;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_highgui.*;

public class CarVideoDetection {

    public static final String AUTONOMOUS_DRIVING_RAMOK_TECH = "Autonomous Driving(ramok.tech)";
    private volatile Frame[] videoFrame = new Frame[1];
    private volatile Mat[] v = new Mat[1];
    private Thread thread;

    public static void main(String[] args) throws java.lang.Exception {
        new CarVideoDetection().startRealTimeVideoDetection("resources/videoSample.mp4");
    }

    public void startRealTimeVideoDetection(String videoFileName) throws java.lang.Exception {

        File f = new File(videoFileName);

        FFmpegFrameGrabber grabber = null;
        try {
            grabber = new FFmpegFrameGrabber(f);
            grabber.start();
        } catch (Exception e) {
            System.err.println("Failed start the grabber.");
            throw new RuntimeException(e);
        }
        while (true) {
            videoFrame[0] = grabber.grab();
            if (videoFrame[0] == null) {
                stop();
                break;
            }
            v[0] = new OpenCVFrameConverter.ToMat().convert(videoFrame[0]);
            if (v[0] == null) {
                continue;
            }

            if (thread == null) {
                thread = new Thread(() -> {
                    while (videoFrame[0] != null) {
                        try {
                            TinyYoloPrediction.getINSTANCE().markWithBoundingBox(v[0], videoFrame[0].imageWidth, videoFrame[0].imageHeight, true);
                        } catch (java.lang.Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                thread.start();
            }

            TinyYoloPrediction.getINSTANCE().markWithBoundingBox(v[0], videoFrame[0].imageWidth, videoFrame[0].imageHeight, false);
            imshow(AUTONOMOUS_DRIVING_RAMOK_TECH, v[0]);

            char key = (char) waitKey(20);
            // Exit this loop on escape:
            if (key == 27) {
                destroyAllWindows();
                break;
            }
        }
    }

    public void stop() {
        destroyAllWindows();
    }
}