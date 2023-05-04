package players;

import org.wikijava.sound.playWave.PlayVideo;
import org.wikijava.sound.playWave.PlayWaveException;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class MediaPlayerMain {
  private static String videoFilePath = "players/InputVideo.rgb";
  private static String soundFilePath = "players/InputAudio.wav";
  private static JFrame jFrame;
  private static MediaPlayer mediaPlayer;
  private static boolean isPaused = false;


  private static JLabel videoOutLabel;

  public static void main(String[] args) throws InterruptedException, IOException, PlayWaveException, LineUnavailableException {
    jFrame  = new JFrame();
    videoOutLabel = new JLabel();

    String input1 = "00:01:37.433";
    LocalTime localShotTime1 = LocalTime.parse(input1);
    double shotTime1 = localShotTime1.toSecondOfDay();


    String input2 = "00:02:37.433";
    LocalTime localShotTime2 = LocalTime.parse(input2);
    double shotTime2 = localShotTime2.toSecondOfDay();

    System.out.println("shotTime1 is: " + shotTime1);
    System.out.println("shotTime2 is: " + shotTime2);

    jFrame.setLayout(null);

    JButton shotButton1 = new JButton();
    JButton shotButton2 = new JButton();

    shotButton1.setText("shot1");
    shotButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {

        try {
          mediaPlayer.seek(shotTime1);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        } catch (IOException e) {
          throw new RuntimeException(e);
        } catch (PlayWaveException e) {
          throw new RuntimeException(e);
        } catch (LineUnavailableException e) {
          throw new RuntimeException(e);
        }
      }
    });

    shotButton2.setText("shot2");
    shotButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {

        try {
          mediaPlayer.seek(shotTime2);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        } catch (IOException e) {
          throw new RuntimeException(e);
        } catch (PlayWaveException e) {
          throw new RuntimeException(e);
        } catch (LineUnavailableException e) {
          throw new RuntimeException(e);
        }
      }
    });

    jFrame.add(shotButton1);
    jFrame.add(shotButton2);

    shotButton1.setBounds(20, 100, 60, 20);
    shotButton2.setBounds(20, 200, 60, 20);

    // Add button and click events.
    JButton pauseButton = new JButton();
    JButton stopButton = new JButton();
    JButton playButton = new JButton();

    pauseButton.setText("Pause");
    pauseButton.setToolTipText("");
    pauseButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        pauseButtonActionPerformed(evt);
      }
    });

    stopButton.setText("Stop");
    stopButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        stopButtonActionPerformed(evt);
      }
    });

    playButton.setText("Play");
    playButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        playButtonActionPerformed(evt);
      }
    });

    // Set buttons position, using null layout, we can change it toC ardLayout or Boxlayout
    playButton.setBounds(PlayVideo.width, 270, 60, 20);
    pauseButton.setBounds(PlayVideo.width + 200, 270, 60, 20);
    stopButton.setBounds(PlayVideo.width + 400, 270, 60, 20);

    jFrame.add(playButton);
    jFrame.add(pauseButton);
    jFrame.add(stopButton);

    mediaPlayer = new MediaPlayer(videoFilePath, soundFilePath, jFrame, videoOutLabel);
    mediaPlayer.play();
  }

  private static void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
    try {
      mediaPlayer.stop();
      setScreenToBlack();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }//GEN-LAST:event_stopButtonActionPerformed

  private static void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
    isPaused = true;
    mediaPlayer.pause();
  }//GEN-LAST:event_pauseButtonActionPerformed

  private static void playButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playButtonActionPerformed
    try {
      if (isPaused){
        mediaPlayer.resume();
      } else {
        mediaPlayer = new MediaPlayer(videoFilePath, soundFilePath, jFrame, videoOutLabel);
        mediaPlayer.play();
      }
      isPaused = false;
    } catch (InterruptedException | IOException | PlayWaveException | LineUnavailableException e) {
      throw new RuntimeException(e);
    }
  }

  public static void setScreenToBlack() {
    //Initialize to black screen
    BufferedImage image = new BufferedImage(PlayVideo.width*2, PlayVideo.height*2, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < PlayVideo.height; y++) {
      for (int x = PlayVideo.width; x < PlayVideo.width * 2; x++) {
        int pix = 0;
        image.setRGB(x, y, pix);
      }
    }
    videoOutLabel.setIcon(new ImageIcon(image));
    jFrame.validate();
    jFrame.repaint();
  }
}
