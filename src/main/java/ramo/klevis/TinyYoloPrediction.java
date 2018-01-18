package ramo.klevis;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
import org.deeplearning4j.zoo.model.TinyYOLO;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_imgproc.putText;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;


public class TinyYoloPrediction {
    private static final Logger log = LoggerFactory.getLogger(TinyYoloPrediction.class);
    private static ComputationGraph pretrained;
    private static List<DetectedObject> predictedObjects;
    private static HashMap<Integer, String> map;

    public static void main(String[] args) throws Exception {

    }

    public static void predict2(Mat file, int imageWidth, int imageHeight, boolean predict) throws java.io.IOException, InterruptedException {
        putLabels();
        int width = 416;
        int height = 416;
        int gridWidth = 13;
        int gridHeight = 13;
        int w = imageWidth;
        int h = imageHeight;


//        double[][] priorBoxes = {{1.08, 1.19}, {3.42, 4.41}, {6.63, 11.38}, {9.42, 5.11}, {16.62, 10.52}};
        double detectionThreshold = 0.5;


        if (pretrained == null) {
            pretrained = (ComputationGraph) new TinyYOLO().initPretrained();
        }

        org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer yout =
                (org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer) pretrained.getOutputLayer(0);


        if (predict) {
            long l = System.currentTimeMillis();
            NativeImageLoader loader = new NativeImageLoader(height, width, 3);
            ImagePreProcessingScaler imagePreProcessingScaler = new ImagePreProcessingScaler(0, 1);
            INDArray indArray = loader.asMatrix(file);
            imagePreProcessingScaler.transform(indArray);
            System.out.println("0 " + (System.currentTimeMillis() - l));
            l = System.currentTimeMillis();
            INDArray results = pretrained.outputSingle(indArray);
            predictedObjects = yout.getPredictedObjects(results, detectionThreshold);
            System.out.println("1 " + (System.currentTimeMillis() - l));
            l = System.currentTimeMillis();
            System.out.println("objs = " + predictedObjects);
            System.out.println("2 " + (System.currentTimeMillis() - l));
            boundingBox(file, gridWidth, gridHeight, w, h);
        } else {
            boundingBox(file, gridWidth, gridHeight, w, h);
        }
        imshow("face_recognizer", file);
    }

    private static void putLabels() {
        if (map == null) {
            String s = "aeroplane\n" +
                    "bicycle\n" +
                    "bird\n" +
                    "boat\n" +
                    "bottle\n" +
                    "bus\n" +
                    "car\n" +
                    "cat\n" +
                    "chair\n" +
                    "cow\n" +
                    "diningtable\n" +
                    "dog\n" +
                    "horse\n" +
                    "motorbike\n" +
                    "person\n" +
                    "pottedplant\n" +
                    "sheep\n" +
                    "sofa\n" +
                    "train\n" +
                    "tvmonitor";
            String[] split = s.split("\\n");
            int i = 0;
            map = new HashMap<>();
            for (String s1 : split) {
                map.put(i++, s1);
            }
        }
    }

    private static void boundingBox(Mat file, int gridWidth, int gridHeight, int w, int h) {

        if (predictedObjects == null) {
            return;
        }
        ArrayList<DetectedObject> detectedObjects = new ArrayList<>(predictedObjects);

        while (!detectedObjects.isEmpty()) {
            Optional<DetectedObject> max = detectedObjects.stream().max((o1, o2) -> ((Double) o1.getConfidence()).compareTo(o2.getConfidence()));
            if (max.isPresent()) {
                DetectedObject maxObjectDetect = max.get();
                double[] bottomRightXY1 = maxObjectDetect.getBottomRightXY();
                double[] topLeftXY1 = maxObjectDetect.getTopLeftXY();
                detectedObjects.remove(maxObjectDetect);
                removeObjectsIntersectingWithMax(detectedObjects, bottomRightXY1, topLeftXY1);
                predict(file, gridWidth, gridHeight, w, h, maxObjectDetect);
            }
        }
    }

    private static void removeObjectsIntersectingWithMax(ArrayList<DetectedObject> detectedObjects, double[] bottomRightXY1, double[] topLeftXY1) {
        List<DetectedObject> removeIntersectingObjects = new ArrayList<>();
        for (DetectedObject detectedObject : detectedObjects) {
            double[] topLeftXY = detectedObject.getTopLeftXY();
            double[] bottomRightXY = detectedObject.getBottomRightXY();
            double iox1 = Math.max(topLeftXY[0], topLeftXY1[0]);
            double ioy1 = Math.min(topLeftXY[1], topLeftXY1[1]);

            double iox2 = Math.min(bottomRightXY[0], bottomRightXY1[0]);
            double ioy2 = Math.max(bottomRightXY[1], bottomRightXY1[1]);

            double inter_area = (ioy2 - ioy1) * (iox2 - iox1);

            double box1_area = (bottomRightXY1[1] - topLeftXY1[1]) * (bottomRightXY1[0] - topLeftXY1[0]);
            double box2_area = (bottomRightXY[1] - topLeftXY[1]) * (bottomRightXY[0] - topLeftXY[0]);

            double union_area = box1_area + box2_area - inter_area;
            double iou = inter_area / union_area;


            if (iou > 0.5) {
                removeIntersectingObjects.add(detectedObject);
            }

        }
        detectedObjects.removeAll(removeIntersectingObjects);
    }

    private static void predict(Mat file, int gridWidth, int gridHeight, int w, int h, DetectedObject obj) {
        long l;
        l = System.currentTimeMillis();
        double[] xy1 = obj.getTopLeftXY();
        double[] xy2 = obj.getBottomRightXY();
        int predictedClass = obj.getPredictedClass();
        System.out.println("predictedClass = " + predictedClass);
        int x1 = (int) Math.round(w * xy1[0] / gridWidth);
        int y1 = (int) Math.round(h * xy1[1] / gridHeight);
        int x2 = (int) Math.round(w * xy2[0] / gridWidth);
        int y2 = (int) Math.round(h * xy2[1] / gridHeight);
        rectangle(file, new Point(x1, y1), new Point(x2, y2), Scalar.RED);
        putText(file, map.get(predictedClass), new Point(x1 + 2, y2 - 2), FONT_HERSHEY_DUPLEX, 1, Scalar.GREEN);
        System.out.println("3 " + (System.currentTimeMillis() - l));
    }

}