package players;

import org.wikijava.sound.playWave.PlayVideo;
import org.wikijava.sound.playWave.PlayWaveException;

public class PlayVideoThread extends Thread{

  private PlayVideo videoPlayer;

  public PlayVideoThread(PlayVideo vp) {
    videoPlayer = vp;
  }

  @Override
  public void run() {
    // plays the video
    videoPlayer.play();
    System.out.println("video thread is quitted");
  }
}