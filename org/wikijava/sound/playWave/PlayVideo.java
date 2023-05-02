package org.wikijava.sound.playWave;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.Color;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.ImageIcon;

public class PlayVideo {
    private String rgbFileName;
    private int width = 480; // width of the video frames
    private int height = 270; // height of the video frames
    private int fps = 30; // frames per second of the video
    private int numFrames = 8682; // number of frames in the video
    private JFrame frame;
    private JLabel label;
    private RandomAccessFile raf;
    private Object syncSignal;
    private AtomicBoolean pauseSignal;
    private AtomicBoolean stopSignal;

    private int startingFrameID = 0;
    private long startingOffset = 0;


    public PlayVideo(Object syncSignal, AtomicBoolean pauseSignal, AtomicBoolean stopSignal, String rgbFileName) throws FileNotFoundException {
        this.rgbFileName = rgbFileName;
        this.syncSignal = syncSignal;
        this.pauseSignal = pauseSignal;
        this.stopSignal = stopSignal;

        File file = new File(rgbFileName); // name of the RGB video file

        // create the JFrame and JLabel to display the video
        frame = new JFrame("Video Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(width, height));
        frame.setVisible(true);
        label = new JLabel();
        label.setPreferredSize(new Dimension(width, height));
        frame.add(label);
        raf = new RandomAccessFile(file, "r");
    }


    public void seek(double momentSeconds) {
        int bytesPerFrame = width * height * 3;
        startingFrameID = (int) (momentSeconds * fps);
        startingOffset = (long) bytesPerFrame * startingFrameID;
    }

    public void play() {
        // read the video file and display each frame
        if (stopSignal.get()) {
            return;
        }
        try {
            raf.seek(startingOffset);

            FileChannel channel = raf.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(width * height * 3);

            for (int i = startingFrameID; i < numFrames; i++) {
                while (pauseSignal.get()) {
                    // busy wait if pauseSignal set to true
                    if (stopSignal.get()) {
                        break;
                    }
                }
                if (stopSignal.get()) {
                    break;
                }
                buffer.clear();
                channel.read(buffer);
                buffer.rewind();
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int r = buffer.get() & 0xff;
                        int g = buffer.get() & 0xff;
                        int b = buffer.get() & 0xff;
                        int rgb = (r << 16) | (g << 8) | b;
                        image.setRGB(x, y, rgb);
                    }
                }
                label.setIcon(new ImageIcon(image));
                frame.validate();
                frame.repaint();
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
}
