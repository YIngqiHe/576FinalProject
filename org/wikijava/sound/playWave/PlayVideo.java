package org.wikijava.sound.playWave;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.List;

public class PlayVideo {
    private String rgbFileName;
    public static final int width = 480; // width of the video frames
    public static final int height = 270; // height of the video frames
    private int fps = 30; // frames per second of the video
    private int numFrames = 8682; // number of frames in the video
    private JFrame frame;
    private JLabel videoOutLabel;
    private RandomAccessFile raf = null;
    private Object syncSignal;
    private AtomicBoolean pauseSignal;
    private AtomicBoolean stopSignal;

    private int startingFrameID = 0;
    private long startingOffset = 0;

    private List<Integer> shotIndexes = new ArrayList<Integer>();

    public BufferedImage image;
    public PlayVideo(Object syncSignal, AtomicBoolean pauseSignal, AtomicBoolean stopSignal, String rgbFileName, JFrame parentFrame, JLabel videoOutLabel, List<Integer> shotIndexes)  throws FileNotFoundException {
        this.rgbFileName = rgbFileName;
        this.syncSignal = syncSignal;
        this.pauseSignal = pauseSignal;
        this.stopSignal = stopSignal;
        this.videoOutLabel = videoOutLabel;

        this.shotIndexes = shotIndexes;

        // create the JFrame and JLabel to display the video
        frame = parentFrame;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(width*2, height*2));
        frame.setVisible(true);
        videoOutLabel.setPreferredSize(new Dimension(width, height));
        videoOutLabel.setBounds(width,0, width - 10, height - 10);
        frame.add(videoOutLabel);
        seek(0, 0);
    }

    public JFrame getVideoDisplayFrame() {
        return frame;
    }

    public void seek(double momentSeconds, int frameIndex) throws FileNotFoundException {
        File file = new File(rgbFileName); // name of the RGB video file
        raf = new RandomAccessFile(file, "r");
        int bytesPerFrame = width * height * 3;
        try{
            numFrames = (int) (raf.length() / bytesPerFrame);
        }catch(Exception e){

        }

        startingFrameID = frameIndex;

        int rgbIndex = (int) startingFrameID/24 * 30;

        startingOffset = (long) bytesPerFrame * rgbIndex;
    }

    public void play(JPanel panel) {
        // read the video file and display each frame
        if (stopSignal.get()) {
            return;
        }
        try {
            raf.seek(startingOffset);

            FileChannel channel = raf.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(width * height * 3);

            Component[] components = panel.getComponents();
            

            // int buttonSize = components.length;
            // int buttonId = 0;


            int shotIndex = shotIndexes.indexOf(startingFrameID);

            for (int i = startingFrameID; i < numFrames; i++) {
                while (pauseSignal.get()) {
                    // busy wait if pauseSignal set to true
                    if (stopSignal.get()) {
                        break;
                    }
                }

                if (shotIndex < shotIndexes.size() && i == shotIndexes.get(shotIndex)) {

                    String buttonName = "" + i;
                    for (Component component : panel.getComponents()) {
                        if (component instanceof JButton) {
                            JButton button = (JButton) component;

                            if (buttonName.equals(button.getName())) {
                                button.setBackground(Color.RED);
                                button.repaint();
                            } else {
                                button.setBackground(Color.WHITE);
                                button.repaint();
                            }

                        }
                    }

                    shotIndex ++;

                }
                
            
                if (stopSignal.get()) {
                    break;
                }
                buffer.clear();
                channel.read(buffer);
                buffer.rewind();
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int r = buffer.get() & 0xff;
                        int g = buffer.get() & 0xff;
                        int b = buffer.get() & 0xff;
                        int rgb = (r << 16) | (g << 8) | b;
                        image.setRGB(x, y, rgb);
                    }
                }
                videoOutLabel.setIcon(new ImageIcon(image));
                // frame.validate();
                // frame.repaint();
                try {
                    Thread.sleep(1000 / fps);
                    // Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (i == 2) {
                    synchronized (syncSignal) {
                        syncSignal.notify();
                        System.out.println("Notify the video thread");
                    }
                }
            }

            channel.close();
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() throws IOException {
        if (raf != null) {
            raf.close();
        }
        raf = null;
    }
}
