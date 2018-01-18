package ramo.klevis;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ScheduledExecutorService;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javax.swing.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static ramo.klevis.Run.predict2;

/**
 * This is an example how to detect face in a video file with javacv
 *
 * @author Vincent He (chinadragon0515@gmail.com)
 */
public class FaceRecognizerInVideo {

    private static ScheduledExecutorService scheduledExecutorService;
    private static Thread thread;
    static volatile Frame[] videoFrame = new Frame[1];
    static volatile Mat[] v = new Mat[1];

    public static void main(String[] args) throws IOException, InterruptedException {

        if (args.length < 2) {
            System.out.println("Two parameters are required to run this program, first parameter is the analized video and second parameter is the trained result for fisher faces.");
        }

        String videoFileName = "C:\\Users\\klevis.ramo\\down\\videoplayback (6).mp4";

        File f = new File(videoFileName);

        FFmpegFrameGrabber grabber = null;
        try {
            grabber = new FFmpegFrameGrabber(f);
//            grabber.setFrameNumber(100);
            grabber.start();
        } catch (Exception e) {
            System.err.println("Failed start the grabber.");
        }


        while (true) {
            videoFrame[0] = grabber.grab();
            v[0] = new OpenCVFrameConverter.ToMat().convert(videoFrame[0]);
            if (v[0] == null) {
                continue;
            }


            SwingUtilities.invokeLater(() -> {
                try {
                    predict2(v[0], videoFrame[0].imageWidth, videoFrame[0].imageHeight, true);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            predict2(v[0], videoFrame[0].imageWidth, videoFrame[0].imageHeight, false);
            imshow("face_recognizer", v[0]);

            char key = (char) waitKey(20);
            // Exit this loop on escape:
            if (key == 27) {
                destroyAllWindows();
                break;
            }
        }
    }

}