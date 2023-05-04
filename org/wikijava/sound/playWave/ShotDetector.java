package org.wikijava.sound.playWave;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;
import org.opencv.videoio.*;
import shotclass.Scene;
import shotclass.Shot;
import shotclass.Subshot;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.System.getProperty;
import static java.lang.System.out;

public class ShotDetector {

   private String informationJson;
   private List<Scene> scenes;
   private String filePath;

   public ShotDetector(String filePath) {
       informationJson = "";
       scenes = new ArrayList<Scene>();
       this.filePath = filePath;
   }

   public String getJson() {
       return informationJson;
   }

    public List<Scene> getScenes() {
        return scenes;
    }

    public Mat getBackgroundMask(Mat frame1, Mat frame2) {

       // Convert frames to grayscale
       Mat gray1 = new Mat();
       Mat gray2 = new Mat();
       Imgproc.cvtColor(frame1, gray1, Imgproc.COLOR_BGR2GRAY);
       Imgproc.cvtColor(frame2, gray2, Imgproc.COLOR_BGR2GRAY);

       // Compute absolute difference between frames
       Mat diff = new Mat();
       Core.absdiff(gray1, gray2, diff);

       // Apply threshold to get foreground mask
       Mat mask = new Mat();
       Imgproc.threshold(diff, mask, 30, 255, Imgproc.THRESH_BINARY);

       // Apply morphology operations to remove noise and fill in gaps
       Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
       Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);
       Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernel);

       // Invert mask to get background mask
       Mat bgMask = new Mat();
       Core.bitwise_not(mask, bgMask);

       return bgMask;
   }


   public double getHistResult(Mat frame1, Mat frame2, Mat mask1, Mat mask2) {

       Mat hsvLastFrame = new Mat();
       Mat hsvCurrentFrame = new Mat();
       Imgproc.cvtColor(frame1, hsvLastFrame, Imgproc.COLOR_BGR2HSV);
       Imgproc.cvtColor(frame2, hsvCurrentFrame, Imgproc.COLOR_BGR2HSV);


       MatOfInt histSize = new MatOfInt(50, 60);
       MatOfFloat histRanges = new MatOfFloat(0f, 180f, 0f, 256f);
       Mat mask = new Mat();

       MatOfInt channels = new MatOfInt(0, 1);
       Mat hist1 = new Mat();
       Mat hist2 = new Mat();

       Imgproc.calcHist(Arrays.asList(hsvLastFrame), channels, mask1, hist1, histSize, histRanges, false);
       Imgproc.calcHist(Arrays.asList(hsvCurrentFrame), channels, mask2, hist2, histSize, histRanges, false);

       Core.normalize(hist1, hist1, 0, 1, Core.NORM_MINMAX);
       Core.normalize(hist2, hist2, 0, 1, Core.NORM_MINMAX);

       double result= Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CHISQR);
       return result;
   }

   public double getWeightedHistResult(Mat frame1, Mat frame2) {

       Mat frame1HSV = new Mat();
       Mat frame2HSV = new Mat();

       Imgproc.cvtColor(frame1, frame1HSV, Imgproc.COLOR_BGR2HSV);
       Imgproc.cvtColor(frame2, frame2HSV, Imgproc.COLOR_BGR2HSV);

       List<Mat> hsv_planes1 = new ArrayList<>();
       List<Mat> hsv_planes2 = new ArrayList<>();

       Core.split(frame1HSV, hsv_planes1);
       Core.split(frame2HSV, hsv_planes2);

       int histSize = 256;
       float[] range = {0, 256};
       MatOfFloat histRange = new MatOfFloat(range);

       Mat hist1 = new Mat();
       Mat hist2 = new Mat();

       // Weighted difference calculation
       double hueWeight = 2.0;
       double saturationWeight = 1.0;
       double valueWeight = 2.0;

       double totalDifference = 0.0;

       for (int i = 0; i < 3; i++) {
           Imgproc.calcHist(hsv_planes1.subList(i, i + 1), new MatOfInt(0), new Mat(), hist1, new MatOfInt(histSize), histRange);
           Imgproc.calcHist(hsv_planes2.subList(i, i + 1), new MatOfInt(0), new Mat(), hist2, new MatOfInt(histSize), histRange);

           double weight = i == 0 ? hueWeight : (i == 1 ? saturationWeight : valueWeight);
           double comparisonResult = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CORREL) * weight;
           totalDifference += comparisonResult;
       }

       totalDifference /= (hueWeight + saturationWeight + valueWeight);

//        out.println(totalDifference);

       return totalDifference;
   }

   public boolean isSameShotHSV(Mat frame1, Mat frame2, double threshold) {
       if (frame1.size().equals(frame2.size()) && frame1.type() == frame2.type()) {
           Mat frame1HSV = new Mat();
           Mat frame2HSV = new Mat();
           Mat diff = new Mat();

           Imgproc.cvtColor(frame1, frame1HSV, Imgproc.COLOR_BGR2HSV);
           Imgproc.cvtColor(frame2, frame2HSV, Imgproc.COLOR_BGR2HSV);
           Core.absdiff(frame1HSV, frame2HSV, diff);

           Scalar meanDifference = Core.mean(diff);
           double meanDiffValue = (meanDifference.val[0] + meanDifference.val[1] + meanDifference.val[2]) / 3;
           return meanDiffValue <= threshold;
       } else {
           return false;
       }
   }

   public static boolean isSameShotDenseOptical(Mat frame1, Mat frame2, double threshold) {
       if (frame1.size().equals(frame2.size()) && frame1.type() == frame2.type()) {
           Mat frame1Gray = new Mat();
           Mat frame2Gray = new Mat();
           Imgproc.cvtColor(frame1, frame1Gray, Imgproc.COLOR_BGR2GRAY);
           Imgproc.cvtColor(frame2, frame2Gray, Imgproc.COLOR_BGR2GRAY);

           Mat flow = new Mat();
           Video.calcOpticalFlowFarneback(
                   frame1Gray, frame2Gray, flow, 0.5, 3, 15, 3, 5, 1.2, 0);

           MatOfFloat flowMagnitude = new MatOfFloat();
           MatOfFloat flowAngle = new MatOfFloat();
           cartToPolar(flow, flowMagnitude, flowAngle);

           double meanFlowMagnitude = Core.mean(flowMagnitude).val[0];

           return meanFlowMagnitude <= threshold;
       } else {
           return false;
       }
   }

   public static void cartToPolar(Mat flow, Mat magnitude, Mat angle) {
       int width = flow.cols();
       int height = flow.rows();
       float[] data = new float[width * height * 2];
       flow.get(0, 0, data);
       float[] mag = new float[width * height];
       float[] ang = new float[width * height];

       for (int i = 0, j = 0; i < data.length; i += 2, j++) {
           float x = data[i];
           float y = data[i + 1];
           mag[j] = (float) Math.sqrt(x * x + y * y);
           ang[j] = (float) Math.atan2(y, x);
       }

       magnitude.create(height, width, CvType.CV_32F);
       angle.create(height, width, CvType.CV_32F);
       magnitude.put(0, 0, mag);
       angle.put(0, 0, ang);
   }

   public boolean isSameShot(Mat frame1, Mat frame2, double threshold) {
       Mat previousGray = new Mat();
       Imgproc.cvtColor(frame1, previousGray, Imgproc.COLOR_BGR2GRAY);
       Mat currentGray = new Mat();
       Imgproc.cvtColor(frame2, currentGray, Imgproc.COLOR_BGR2GRAY);

       MatOfPoint featuredPoints = new MatOfPoint();
       Imgproc.goodFeaturesToTrack(previousGray, featuredPoints, 500, 0.001, 10, new Mat(), 3, true);

       MatOfPoint2f previousPoints = new MatOfPoint2f(featuredPoints.toArray());
       MatOfPoint2f currentPoints = new MatOfPoint2f();
       MatOfByte status = new MatOfByte();

       Size windowSize = new Size(15, 15);
       TermCriteria termCriteria = new TermCriteria(TermCriteria.MAX_ITER + TermCriteria.EPS, 20, 0.03);
       try {
           Video.calcOpticalFlowPyrLK(previousGray, currentGray, previousPoints, currentPoints, status, new MatOfFloat(), windowSize, 3, termCriteria, 0, 0.001);
       }catch(Exception e){
           return isSameShotDenseOptical( frame1, frame2, 20);
       }

       List<Point> previousPointsList = previousPoints.toList();
       List<Point> currentPointsList = currentPoints.toList();
       List<Byte> statusList = status.toList();

       double sumFlow = 0.0;

       int successSize = 0;

       for (int i = 0; i < statusList.size(); i++) {
           if (statusList.get(i) == 1) {
               Point previousPoint = previousPointsList.get(i);
               Point currentPoint = currentPointsList.get(i);

               double flowX = Math.abs(previousPoint.x - currentPoint.x);
               double flowY = Math.abs(previousPoint.y - currentPoint.y);

               sumFlow += Math.sqrt(flowX*flowX + flowY*flowY);
               successSize += 1;
           }
       }

       if(sumFlow / successSize > threshold) {
           return false;
       } else {
           return true;
       }

   }

   public boolean isSameShotRGB(Mat frame1, Mat frame2, double threshold) {
       if (frame1.size().equals(frame2.size())) {
           Mat diff = new Mat();
           Core.absdiff(frame1, frame2, diff);
           Scalar meanDifference = Core.mean(diff);

           double sumMeanDifference = meanDifference.val[0] + meanDifference.val[1] + meanDifference.val[2];
           if (sumMeanDifference <= threshold) {
               return true;
           } else {
               return false;
           }
       } else {
           return false;
       }

   }

   public void SceneDetect() {
       System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

       List<Scene> sceneList = new ArrayList<Scene>();

       Scene scene0 = new Scene();

       Shot shot0 = new Shot();
       scene0.addShot(shot0);

       sceneList.add(scene0);

       VideoCapture cap = new VideoCapture(filePath);
       if (!cap.isOpened()) {
           System.out.println("Error opening video file.");
           return;
       }

       Mat previousFrame = new Mat();
       cap.read(previousFrame);

       Mat previousGray = new Mat();
       Imgproc.cvtColor(previousFrame, previousGray, Imgproc.COLOR_BGR2GRAY);

       Mat currentFrame = previousFrame.clone();

       int frameIndex = 0;

       ArrayList<Integer> shotIndexes = new ArrayList<Integer>();
       shotIndexes.add(frameIndex);

       int lastIndex = 0;

       Mat lastFrame = currentFrame.clone();
       Mat lastTwoFrame = lastFrame.clone();
       Mat lastSceneFrame = lastFrame.clone();

       int mask_status = 0;
       Mat frameMask1 = new Mat();
       Mat frameMask2 = new Mat();
       Mat frameMask3 = new Mat();

       while (cap.read(currentFrame)) {

           double frameRate = cap.get(Videoio.CAP_PROP_FPS);
           long frameTimestamp = (long) (1000.0 * frameIndex / frameRate);
           SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
           dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
           String formattedTime = dateFormat.format(new Date(frameTimestamp));

           // if (mask_status == 1) {
           //     lastFrame = lastTwoFrame.clone();
           //     lastTwoFrame = previousFrame.clone();
           //     mask_status++;
           //     // System.out.println(frameIndex);
           // } else if (mask_status > 1 && mask_status < 5) {
           //     mask_status++;
           // } else if (mask_status == 5) {
           // // if (mask_status == 1) {
           // //     lastFrame = lastTwoFrame.clone();
           // //     lastTwoFrame = previousFrame.clone();
           //     frameMask2 = getBackgroundMask(lastTwoFrame, currentFrame);
           //     frameMask3 = getBackgroundMask(lastTwoFrame, lastSceneFrame);
           //     double result1 = getHistResult(lastFrame, lastTwoFrame, frameMask1, frameMask2);
           //     double result2 = getHistResult(lastSceneFrame, lastTwoFrame, frameMask3, frameMask2);
           //     frameMask1 = frameMask2.clone();
           //     mask_status = 0;
           //     System.out.println("HistResult: " + result1 + ".  Result2: " + result2);
           //     if (result1 > 10 && result2> 50) {
           //         lastSceneFrame = lastTwoFrame.clone();
           //         System.out.println("Scene change detected! Frame: " + frameIndex + ". Current Time: "
           //             + formattedTime + ". HistResult: " + result1);
           //     }

           // }


           double subShotThreshold = 40.0;

           if (!isSameShot(previousFrame, currentFrame, subShotThreshold) && frameIndex - lastIndex > 12){
               // subshots to be added
           }

           double shotThreshold = 40.0;

           if (!isSameShot(previousFrame, currentFrame, shotThreshold) && frameIndex - lastIndex > 50) {
               System.out.println("Shot change detected! Frame: " + frameIndex + ". Current Time: " + formattedTime);
               shotIndexes.add(frameIndex);


//                double result1 = getHistResult(lastFrame, currentFrame, new Mat(), new Mat());
//                double result2 = getHistResult(lastTwoFrame, currentFrame, new Mat(), new Mat());
//                double result3 = getHistResult(previousFrame, currentFrame, new Mat(), new Mat());

               double result1 = getWeightedHistResult(lastFrame, currentFrame);
               double result2 = getWeightedHistResult(lastTwoFrame, currentFrame);
               double result3 = getWeightedHistResult(previousFrame, currentFrame);


               // System.out.println("Result1: " + result1 + ". Result2: " + result2 + ". Result3: " + result3);

               Shot shot = new Shot(formattedTime, frameIndex, frameTimestamp);

               double sceneThreshold = 0.85;

               if (result1 > sceneThreshold && result2 > sceneThreshold && result3 > sceneThreshold) {
                   System.out.println("Scene changed! Result1:" + result2 + ". Result2:" + result3);
                   lastTwoFrame = lastFrame.clone();
                   Scene scene = new Scene(formattedTime, frameIndex, frameTimestamp);
                   scene.addShot(shot);
                   sceneList.add(scene);
               } else {
                   if (frameIndex - lastIndex <= 30) {
                       Subshot subshot = new Subshot(formattedTime, frameIndex, frameTimestamp);
                       sceneList.get(sceneList.size() - 1).addSubshot(subshot);
                   } else {
                       sceneList.get(sceneList.size() - 1).addShot(shot);
                   }

               }

               lastIndex = frameIndex;
               lastTwoFrame = lastFrame.clone();
               lastFrame = currentFrame.clone();
               mask_status++;

           }

           previousFrame = currentFrame.clone();
           frameIndex++;
       }

       scenes = sceneList;

       ObjectMapper objectMapper = new ObjectMapper();
       try {
           String json = objectMapper.writeValueAsString(sceneList);
           informationJson = json;
           // System.out.println(json);
       } catch (JsonProcessingException e) {
           e.printStackTrace();
       }

       try (FileWriter file = new FileWriter("example.json")) {
           file.write(informationJson);

       } catch (IOException e) {
           e.printStackTrace();
       }

       cap.release();

   }

   public static void main(String[] arg){
       out.println("hi");
       out.println(getProperty("java.library.path"));
       ShotDetector shotDetector = new ShotDetector("InputVideo.mp4");
       shotDetector.SceneDetect();
   }

}
