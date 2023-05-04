package players;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wikijava.sound.playWave.PlayVideo;
import org.wikijava.sound.playWave.PlayWaveException;
import org.wikijava.sound.playWave.ShotDetector;
import shotclass.Scene;
import shotclass.Shot;
import shotclass.Subshot;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

public class MediaPlayerMain {
  private String videoFilePath = "players/InputVideo.rgb";
  private String soundFilePath = "players/InputAudio.wav";
  private String mp4FilePath = "players/InputAudio.wav";
  private static JFrame jFrame;
  private static MediaPlayer mediaPlayer;
  private static boolean isPaused = false;

  private static JLabel videoOutLabel;

  private static int shotPosition = 100;

  private static JScrollPane jScrollPane;

  private static JPanel content;

  public void MediaPlay(String mp4file, String rgbfile, String wavfile) throws JsonProcessingException, InterruptedException, IOException, PlayWaveException, LineUnavailableException {
    // if (args.length < 3) {
    //     System.err.println("Input must have .mp4 .rgb .wav files");
    //     return;
    // }
    // String mp4name = args[0];
    // String rgbfile = args[1];
    // String wavfile = args[2];
    mp4FilePath = mp4file;
    soundFilePath = wavfile;
    videoFilePath = rgbfile;

    jFrame  = new JFrame();
    videoOutLabel = new JLabel();
    // ObjectMapper objectMapper = new ObjectMapper();
    // File file = new File("players/example.json");
    // Scene[] scenes = objectMapper.readValue(file, Scene[].class);

    ShotDetector shotDetector = new ShotDetector("InputVideo.mp4");
    shotDetector.SceneDetect();
    String json = shotDetector.getJson();

    List<Scene> scenes = shotDetector.getScenes();


//    System.out.println(scenes[0].getStartTime());
    jFrame.setLayout(null);

    content = new JPanel();
    jScrollPane = new JScrollPane(content);
    BoxLayout layout = new BoxLayout(content, BoxLayout.Y_AXIS);
    content.setLayout(layout);

    // Set left panel.
    for(int i = 0; i < scenes.size(); i++) {
      Scene scene = scenes.get(i);
      setShotPanelLabel(new JButton(), "Scene " + i, parseTime(scene.getStartTime()), false, false);
      List<Shot> shots = scene.getShots();
      for (int j = 0; j < shots.size(); j++) {
        Shot shot = shots.get(j);
        setShotPanelLabel(new JButton(), "Shot " + j, parseTime(shot.getStartTime()), true, false);
        List<Subshot> subShots = shot.getSubshots();
        for (int k = 0; k < subShots.size(); k++) {
          Subshot subshot = subShots.get(k);
          setShotPanelLabel(new JButton(), "Subshot " + k, parseTime(shot.getStartTime()), false, true);
        }
      }
    }

    jScrollPane.setBounds(0,0,PlayVideo.width / 2 + 50,PlayVideo.height + 200);
    jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    jFrame.add(jScrollPane);

    // Add button and click events.
    JButton pauseButton = new JButton();
    JButton stopButton = new JButton();
    JButton playButton = new JButton();

    pauseButton.setText("Pause");
    pauseButton.setToolTipText("");
    pauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        pauseButtonActionPerformed(evt);
      }
    });

    stopButton.setText("Stop");
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        stopButtonActionPerformed(evt);
      }
    });

    playButton.setText("Play");
    playButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        playButtonActionPerformed(evt);
      }
    });

    // Set buttons position, using null layout, we can change it to CardLayout or Boxlayout
    playButton.setBounds(PlayVideo.width, 270, 60, 20);
    pauseButton.setBounds(PlayVideo.width + 200, 270, 60, 20);
    stopButton.setBounds(PlayVideo.width + 400, 270, 60, 20);

    jFrame.add(playButton);
    jFrame.add(pauseButton);
    jFrame.add(stopButton);

    mediaPlayer = new MediaPlayer(videoFilePath, soundFilePath, jFrame, videoOutLabel);
    mediaPlayer.play();
  }

  public static void main(String[] args) throws JsonProcessingException, InterruptedException, IOException, PlayWaveException, LineUnavailableException {
      if (args.length < 3) {
          System.err.println("Input must have .mp4 .rgb .wav files");
          return;
      }
      String mp4file = args[0];
      String rgbfile = args[1];
      String wavfile = args[2];

      MediaPlayerMain play = new MediaPlayerMain();
      play.MediaPlay(mp4file, rgbfile, wavfile);
  }

  private static void stopButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
    try {
      mediaPlayer.stop();
      setScreenToBlack();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }//GEN-LAST:event_stopButtonActionPerformed

  private static void pauseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
    isPaused = true;
    mediaPlayer.pause();
  }//GEN-LAST:event_pauseButtonActionPerformed

  private void playButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_playButtonActionPerformed
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

  private static void setShotPanelLabel(JButton button, String shotName, double shotTime, boolean isShot, boolean isScene) {
    button.setText(shotName);

    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        button.setForeground(Color.BLUE);
        try {
          mediaPlayer.seek(shotTime);
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

    if (isShot) {
      button.setAlignmentX(Component.CENTER_ALIGNMENT);
    } else if (isScene) {
      button.setAlignmentX(Component.LEFT_ALIGNMENT);
    } else {
      button.setAlignmentX(Component.RIGHT_ALIGNMENT);
    }
    content.add(button);
  }

  private static double parseTime(String input) {
    LocalTime localShotTime5 = LocalTime.parse(input);
    double shotTime = localShotTime5.toSecondOfDay();
    return shotTime;
  }
}
