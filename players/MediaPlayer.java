package players;

import org.wikijava.sound.playWave.PlaySound;
import org.wikijava.sound.playWave.PlayVideo;
import org.wikijava.sound.playWave.PlayWaveException;

import java.io.FileNotFoundException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MediaPlayer {

  private String videoFilePath;

  private String audioFilePath;

  private PlayVideo videoPlayer;
  private PlaySound soundPlayer;

  private PlayVideoThread videoThread;
  private PlaySoundThread soundThread;

  private Object syncSignal;
  private AtomicBoolean pauseSignal;
  private AtomicBoolean stopSignal;


  public MediaPlayer(String videoFilePath, String audioFilePath) throws FileNotFoundException, PlayWaveException {
    this.videoFilePath = videoFilePath;
    this.audioFilePath = audioFilePath;
    this.pauseSignal = new AtomicBoolean(false);

    syncSignal = new Object();

    videoPlayer = new PlayVideo(syncSignal, pauseSignal, stopSignal, videoFilePath);
    soundPlayer = new PlaySound(syncSignal, pauseSignal, stopSignal, audioFilePath);

    videoThread = new PlayVideoThread(syncSignal, videoPlayer);
    soundThread = new PlaySoundThread(syncSignal, soundPlayer);
  }

  public void play() throws InterruptedException {
    videoThread.start();
    soundThread.start();

    videoThread.join();
    soundThread.join();
  }


  public void pause() {
    pauseSignal.set(true);
  }

  public void resume() {
    pauseSignal.set(false);
  }

  public void seek() {

  }

}
