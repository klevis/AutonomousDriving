package ramo.klevis;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_highgui.*;

public class CarVideoDetection {

    private static final String AUTONOMOUS_DRIVING_RAMOK_TECH = "Autonomous Driving(ramok.tech)";
    private volatile Frame[] videoFrame = new Frame[1];
    private volatile Mat[] v = new Mat[1];
    private Thread thread;
    private volatile boolean stop = false;
    private String winname;

    public static void main(String[] args) throws java.lang.Exception {
        new CarVideoDetection().startRealTimeVideoDetection("resources/videoSample.mp4");
    }

    public void startRealTimeVideoDetection(String videoFileName) throws java.lang.Exception {

        File f = new File(videoFileName);

        FFmpegFrameGrabber grabber;
        try {
            grabber = new FFmpegFrameGrabber(f);
            grabber.start();
        } catch (Exception e) {
            System.err.println("Failed start the grabber.");
            throw new RuntimeException(e);
        }
        while (!stop) {
            videoFrame[0] = grabber.grab();
            if (videoFrame[0] == null) {
                stop();
                break;
            }
            v[0] = new OpenCVFrameConverter.ToMat().convert(videoFrame[0]);
            if (v[0] == null) {
                continue;
            }
            if (winname == null) {
                winname = AUTONOMOUS_DRIVING_RAMOK_TECH + ThreadLocalRandom.current().nextInt();
            }

            if (thread == null) {
                thread = new Thread(() -> {
                    while (videoFrame[0] != null && !stop) {
                        try {
                            TinyYoloPrediction.getINSTANCE().markWithBoundingBox(v[0], videoFrame[0].imageWidth, videoFrame[0].imageHeight, true, winname);
                        } catch (java.lang.Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                thread.start();
            }

            TinyYoloPrediction.getINSTANCE().markWithBoundingBox(v[0], videoFrame[0].imageWidth, videoFrame[0].imageHeight, false, winname);

            imshow(winname, v[0]);

            char key = (char) waitKey(20);
            // Exit this loop on escape:
            if (key == 27) {
                stop();
                break;
            }
        }
    }

    public void stop() {
        if (!stop) {
            stop = true;
            destroyAllWindows();
        }
    }
}