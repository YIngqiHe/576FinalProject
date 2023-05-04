package org.wikijava.sound.playWave;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;


import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;
import org.opencv.videoio.*;

import shotclass.*;

public class ShotDetector {

    private String informationJson;

    public ShotDetector() {
        informationJson = "";
    }

    public String getJson() {
        return informationJson;
    }

    // public class Scene {

    //     @JsonProperty("start_time")
    //     private String startTime;

    //     @JsonProperty("frame_id")
    //     private int frameId;

    //     @JsonProperty("int_time")
    //     private long intTime;

    //     @JsonProperty("shots")
    //     private List<Shot> shots;

    //     public Scene() {
    //         this.startTime = "00:00:00.000"; 
    //         this.frameId = 0;
    //         this.intTime = 0;
    //         this.shots = new ArrayList<>();
    //     }

    //     public Scene(String startTime, int frameId, long intTime) {
    //         this.startTime = startTime; 
    //         this.frameId = frameId;
    //         this.intTime = intTime;
    //         this.shots = new ArrayList<>();
    //     }

    //     public void addShot(Shot shot) {
    //         this.shots.add(shot);
    //     }

    //     public void addSubshot(Subshot subshot) {
    //         Shot shot = this.shots.get(shots.size() - 1);
    //         String start_time = shot.getStartTime();
    //         int frame_id = shot.getFrameId();
    //         long int_time = shot.getIntTime();
    //         if (shot.subshots.size() == 0) {
    //            Subshot firstSubshot = new Subshot(start_time, frame_id, int_time);
    //            this.shots.get(shots.size() - 1).subshots.add(firstSubshot);
               
    //         }
    //         this.shots.get(shots.size() - 1).subshots.add(subshot);
    //     }

    //     public String getStartTime() {
    //         return startTime;
    //     }
    
    //     public void setStartTime(String startTime) {
    //         this.startTime = startTime;
    //     }
    
    //     public int getFrameId() {
    //         return frameId;
    //     }
    
    //     public void setFrameId(int frameId) {
    //         this.frameId = frameId;
    //     }

    //     public long getIntTime() {
    //         return intTime;
    //     }
    
    //     public void setIntTime(long intTime) {
    //         this.intTime = intTime;
    //     }
    
    //     public List<Shot> getShots() {
    //         return shots;
    //     }
    
    //     public void setShots(List<Shot> shots) {
    //         this.shots = shots;
    //     }

    // }

    // public class Shot {
    //     @JsonProperty("start_time")
    //     private String startTime;

    //     @JsonProperty("frame_id")
    //     private int frameId;

    //     @JsonProperty("int_time")
    //     private long intTime;

    //     @JsonProperty("subshots")
    //     private List<Subshot> subshots;

    //     public Shot() {
    //         this.startTime = "00:00:00.000"; 
    //         this.frameId = 0;
    //         this.intTime = 0;
    //         this.subshots = new ArrayList<>();
    //     }

    //     public Shot(String startTime, int frameId, long intTime) {
    //         this.startTime = startTime; 
    //         this.frameId = frameId;
    //         this.intTime = intTime;
    //         this.subshots = new ArrayList<>();
    //     }

    //     public void addSubshot(Subshot subshot) {
    //         this.subshots.add(subshot);
    //     }

    //     public String getStartTime() {
    //         return startTime;
    //     }
    
    //     public void setStartTime(String startTime) {
    //         this.startTime = startTime;
    //     }
    
    //     public int getFrameId() {
    //         return frameId;
    //     }
    
    //     public void setFrameId(int frameId) {
    //         this.frameId = frameId;
    //     }

    //     public long getIntTime() {
    //         return intTime;
    //     }
    
    //     public void setIntTime(long intTime) {
    //         this.intTime = intTime;
    //     }
    
    //     public List<Subshot> getSubshots() {
    //         return subshots;
    //     }
    
    //     public void setSubshots(List<Subshot> subshots) {
    //         this.subshots = subshots;
    //     }

    // }

    // public class Subshot {
        
    //     @JsonProperty("start_time")
    //     private String startTime;

    //     @JsonProperty("frame_id")
    //     private int frameId;

    //     @JsonProperty("int_time")
    //     private long intTime;

    //     public Subshot() {
    //         this.startTime = "00:00:00.000"; 
    //         this.frameId = 0;
    //         this.intTime = 0;
    //     }

    //     public Subshot(String startTime, int frameId, long intTime) {
    //         this.startTime = startTime; 
    //         this.frameId = frameId;
    //         this.intTime = intTime;
    //     }

    //     public String getStartTime() {
    //         return startTime;
    //     }
    
    //     public void setStartTime(String startTime) {
    //         this.startTime = startTime;
    //     }
    
    //     public int getFrameId() {
    //         return frameId;
    //     }
    
    //     public void setFrameId(int frameId) {
    //         this.frameId = frameId;
    //     }

    //     public long getIntTime() {
    //         return intTime;
    //     }
    
    //     public void setIntTime(long intTime) {
    //         this.intTime = intTime;
    //     }

    // }

    
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

    public boolean isSameShot(Mat frame1, Mat frame2, double threshold) {
        Mat previousGray = new Mat();
        Imgproc.cvtColor(frame1, previousGray, Imgproc.COLOR_BGR2GRAY);
        Mat currentGray = new Mat();
        Imgproc.cvtColor(frame2, currentGray, Imgproc.COLOR_BGR2GRAY);

        MatOfPoint featuredPoints = new MatOfPoint();
        Imgproc.goodFeaturesToTrack(previousGray, featuredPoints, 500, 0.01, 10);

        MatOfPoint2f previousPoints = new MatOfPoint2f(featuredPoints.toArray());
        MatOfPoint2f currentPoints = new MatOfPoint2f();
        MatOfByte status = new MatOfByte();
        
        Size windowSize = new Size(15, 15);
        TermCriteria termCriteria = new TermCriteria(TermCriteria.MAX_ITER + TermCriteria.EPS, 20, 0.03);
        Video.calcOpticalFlowPyrLK(previousGray, currentGray, previousPoints, currentPoints, status, new MatOfFloat(), windowSize, 3, termCriteria, 0, 0.001);
        
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

    public void SceneDetect() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        List<Scene> sceneList = new ArrayList<Scene>();

        Scene scene0 = new Scene();

        Shot shot0 = new Shot();
        scene0.addShot(shot0);

        sceneList.add(scene0);
  
        VideoCapture cap = new VideoCapture("InputVideo.mp4");
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

            double threshold = 40.0;


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
            
            if (!isSameShot(previousFrame, currentFrame, threshold) && frameIndex - lastIndex > 15) {
                System.out.println("Shot change detected! Frame: " + frameIndex + ". Current Time: " + formattedTime);
                shotIndexes.add(frameIndex);
               
                
                double result1 = getHistResult(lastFrame, currentFrame, new Mat(), new Mat());
                double result2 = getHistResult(lastTwoFrame, currentFrame, new Mat(), new Mat());
                double result3 = getHistResult(previousFrame, currentFrame, new Mat(), new Mat());

                
                // System.out.println("Result1: " + result1 + ". Result2: " + result2 + ". Result3: " + result3);

                Shot shot = new Shot(formattedTime, frameIndex, frameTimestamp);
                if (result1 > 40 && result2 > 40 && result3 > 40) {
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

}
