package org.wikijava.sound.playWave;

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

public class ShotDetector {

    private String informationJson;

    public ShotDetector() {
        informationJson = "";
    }

    public String getJson() {
        return informationJson;
    }

    public class Scene {

        @JsonProperty("start_time")
        private String startTime;

        @JsonProperty("frame_id")
        private int frameId;

        @JsonProperty("shots")
        private List<Shot> shots;

        public Scene() {
            this.startTime = "00:00:00.000"; 
            this.frameId = 0;
            this.shots = new ArrayList<>();
        }

        public Scene(String startTime, int frameId) {
            this.startTime = startTime; 
            this.frameId = frameId;
            this.shots = new ArrayList<>();
        }

        public void addShot(Shot shot) {
            this.shots.add(shot);
        }

        public String getStartTime() {
            return startTime;
        }
    
        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }
    
        public int getFrameId() {
            return frameId;
        }
    
        public void setFrameId(int frameId) {
            this.frameId = frameId;
        }
    
        public List<Shot> getShots() {
            return shots;
        }
    
        public void setShots(List<Shot> shots) {
            this.shots = shots;
        }

    }

    public class Shot {
        @JsonProperty("start_time")
        private String startTime;

        @JsonProperty("frame_id")
        private int frameId;

        @JsonProperty("subshots")
        private List<Subshot> subshots;

        public Shot() {
            this.startTime = "00:00:00.000"; 
            this.frameId = 0;
            this.subshots = new ArrayList<>();
        }

        public Shot(String startTime, int frameId) {
            this.startTime = startTime; 
            this.frameId = frameId;
            this.subshots = new ArrayList<>();
        }

        public void addSubshot(Subshot subshot) {
            this.subshots.add(subshot);
        }

        public String getStartTime() {
            return startTime;
        }
    
        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }
    
        public int getFrameId() {
            return frameId;
        }
    
        public void setFrameId(int frameId) {
            this.frameId = frameId;
        }
    
        public List<Subshot> getSubshots() {
            return subshots;
        }
    
        public void setSubshots(List<Subshot> subshots) {
            this.subshots = subshots;
        }

    }

    public class Subshot {
        
        @JsonProperty("start_time")
        private String startTime;

        @JsonProperty("frame_id")
        private int frameId;

        public Subshot() {
            this.startTime = "00:00:00.000"; 
            this.frameId = 0;
        }

        public String getStartTime() {
            return startTime;
        }
    
        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }
    
        public int getFrameId() {
            return frameId;
        }
    
        public void setFrameId(int frameId) {
            this.frameId = frameId;
        }

    }


    public boolean isSameScene(Mat frame1, Mat frame2, double threshold) {
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


        Mat flow = new Mat();


        Size windowSize = new Size(15, 15);
        TermCriteria termCriteria = new TermCriteria(TermCriteria.MAX_ITER + TermCriteria.EPS, 20, 0.03);

        int frameIndex = 0;

        ArrayList<Integer> shotIndexes = new ArrayList<Integer>();
        shotIndexes.add(frameIndex);

        int lastIndex = 0;

        Mat lastFrame = currentFrame.clone();
        Mat lastTwoFrame = lastFrame.clone();

        while (cap.read(currentFrame)) {
            
            Mat currentGray = new Mat();
            Imgproc.cvtColor(currentFrame, currentGray, Imgproc.COLOR_BGR2GRAY);

            MatOfPoint featuredPoints = new MatOfPoint();
            Imgproc.goodFeaturesToTrack(previousGray, featuredPoints, 500, 0.01, 10);


            MatOfPoint2f previousPoints = new MatOfPoint2f(featuredPoints.toArray());
            MatOfPoint2f currentPoints = new MatOfPoint2f();
            MatOfByte status = new MatOfByte();
            

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

            // Video.calcOpticalFlowFarneback(previousGray, currentGray, flow, 0.5, 3, 15, 3, 5, 1.2, 0);

            // Scalar meanFlow = Core.mean(flow);
            // double flowMagnitude = Math.sqrt(meanFlow.val[0] * meanFlow.val[0] + meanFlow.val[1] * meanFlow.val[1]);

        
            double frameRate = cap.get(Videoio.CAP_PROP_FPS);

            long frameTimestamp = (long) (1000.0 * frameIndex / frameRate);


            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            String formattedTime = dateFormat.format(new Date(frameTimestamp));


            double threshold = 40.0;
            if ((sumFlow / successSize > threshold || successSize < 5) && frameIndex - lastIndex > 15) {
            // if (flowMagnitude > 10 && frameIndex - lastIndex > 15) {
                System.out.println("Shot change detected! Frame: " + frameIndex + ". Current Time: " + formattedTime);
                shotIndexes.add(frameIndex);
                lastIndex = frameIndex;
                

                // if (!isSameScene(lastFrame, currentFrame, 100) && !isSameScene(lastTwoFrame, currentFrame, 100)) {
                //     System.out.println("Scene changed!");
                // }

                Mat hsvLastFrame = new Mat();
                Mat hsvCurrentFrame = new Mat();
                Mat hsvLastTwoFrame = new Mat();
                Mat hsvPreviousFrame = new Mat();
                Imgproc.cvtColor(lastFrame, hsvLastFrame, Imgproc.COLOR_BGR2HSV);
                Imgproc.cvtColor(currentFrame, hsvCurrentFrame, Imgproc.COLOR_BGR2HSV);
                Imgproc.cvtColor(lastTwoFrame, hsvLastTwoFrame, Imgproc.COLOR_BGR2HSV);
                Imgproc.cvtColor(previousFrame, hsvPreviousFrame, Imgproc.COLOR_BGR2HSV);

                MatOfInt histSize = new MatOfInt(50, 60);
                float[] hRanges = { 0, 180 };
                float[] sRanges = { 0, 256 };
                MatOfFloat histRanges = new MatOfFloat(0f, 180f, 0f, 256f);
                Mat mask = new Mat();

                MatOfInt channels = new MatOfInt(0, 1);
                Mat hist1 = new Mat();
                Mat hist2 = new Mat();
                Mat hist3 = new Mat();
                Mat hist4 = new Mat();
                Imgproc.calcHist(Arrays.asList(hsvLastFrame), channels, mask, hist1, histSize, histRanges, false);
                Imgproc.calcHist(Arrays.asList(hsvCurrentFrame), channels, mask, hist2, histSize, histRanges, false);
                Imgproc.calcHist(Arrays.asList(hsvLastTwoFrame), channels, mask, hist3, histSize, histRanges, false);
                Imgproc.calcHist(Arrays.asList(hsvPreviousFrame), channels, mask, hist4, histSize, histRanges, false);

                Core.normalize(hist1, hist1, 0, 1, Core.NORM_MINMAX);
                Core.normalize(hist2, hist2, 0, 1, Core.NORM_MINMAX);
                Core.normalize(hist3, hist3, 0, 1, Core.NORM_MINMAX);
                Core.normalize(hist4, hist4, 0, 1, Core.NORM_MINMAX);

                double result1 = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CHISQR);
                double result2 = Imgproc.compareHist(hist2, hist3, Imgproc.CV_COMP_CHISQR);
                double result3 = Imgproc.compareHist(hist2, hist4, Imgproc.CV_COMP_CHISQR);
                
                System.out.println("Chi-square distance between the two histograms: " + result1 + ". Result2: " + result2);

                Shot shot = new Shot(formattedTime, frameIndex);
                if (result1 > 10 && result2 > 10 && result3 > 10 && result1+ result2 +result3 > 300) {
                    // System.out.println("Scene changed! Result1:" + result1 + ". Result2:" + result2);
                    lastTwoFrame = lastFrame.clone();
                    Scene scene = new Scene();
                    scene.addShot(shot);
                    sceneList.add(scene);
                } else {
                    sceneList.get(sceneList.size() - 1).addShot(shot);;
                }
                // lastTwoFrame = lastFrame.clone();
                lastFrame = currentFrame.clone();

            }

            previousFrame = currentFrame.clone();
            previousGray = currentGray.clone();
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


        cap.release();

    }

    // public static void main(String[] args){
    //     ShotDetector shotDetector = new ShotDetector();
    //     shotDetector.SceneDetect();
    // }
}