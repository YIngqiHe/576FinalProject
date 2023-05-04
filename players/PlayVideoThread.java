package players;

import javax.swing.JPanel;

import org.wikijava.sound.playWave.PlayVideo;
import org.wikijava.sound.playWave.PlayWaveException;

public class PlayVideoThread extends Thread{

  private PlayVideo videoPlayer;
  private JPanel panel;

  public PlayVideoThread(PlayVideo vp, JPanel panel) {
    videoPlayer = vp;
    this.panel = panel;
  }

  @Override
  public void run() {
    // plays the video
    videoPlayer.play(panel);
    System.out.println("video thread is quitted");
  }
}
