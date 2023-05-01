package players;

import org.wikijava.sound.playWave.PlayVideo;
import org.wikijava.sound.playWave.PlayWaveException;

public class PlayVideoThread extends Thread{

  private PlayVideo videoPlayer;
  private final Object syncSignal;

  public PlayVideoThread(Object syncSignal, PlayVideo vp) {
    this.syncSignal = syncSignal;
    videoPlayer = vp;
  }

  @Override
  public void run() {
    // plays the video
    videoPlayer.play();
  }
}
