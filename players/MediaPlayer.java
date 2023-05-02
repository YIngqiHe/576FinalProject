package players;

import org.wikijava.sound.playWave.PlaySound;
import org.wikijava.sound.playWave.PlayVideo;
import org.wikijava.sound.playWave.PlayWaveException;

import javax.sound.sampled.LineUnavailableException;
import java.io.FileNotFoundException;
import java.io.IOException;
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


  public MediaPlayer(String videoFilePath, String audioFilePath) throws IOException, PlayWaveException, LineUnavailableException, InterruptedException {
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
  }

  public void seek(double momentSeconds) throws InterruptedException, IOException, PlayWaveException, LineUnavailableException {
    // stop the play before seek
    stop();
    soundPlayer.seek(momentSeconds);
    videoPlayer.seek(momentSeconds);
    play();

  }

  public void stop() throws InterruptedException {
    stopSignal.set(true);
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
