package players;

import org.wikijava.sound.playWave.PlaySound;
import org.wikijava.sound.playWave.PlayVideo;
import org.wikijava.sound.playWave.PlayWaveException;

import java.io.FileNotFoundException;

public class MediaPlayer {

  private String videoFilePath;

  private String audioFilePath;

  private PlayVideo videoPlayer;
  private PlaySound soundPlayer;

  private PlayVideoThread videoThread;
  private PlaySoundThread soundThread;

  private Object syncSignal = new Object();


  public MediaPlayer(String videoFilePath, String audioFilePath) throws FileNotFoundException, PlayWaveException {
    this.videoFilePath = videoFilePath;
    this.audioFilePath = audioFilePath;

    videoPlayer = new PlayVideo(videoFilePath);
    soundPlayer = new PlaySound(audioFilePath);

    videoThread = new PlayVideoThread(syncSignal, videoPlayer);
    soundThread = new PlaySoundThread(syncSignal, soundPlayer);
  }

  public void play() throws InterruptedException {
    videoThread.start();
    soundThread.start();

    videoThread.join();
    soundThread.join();
  }
}
