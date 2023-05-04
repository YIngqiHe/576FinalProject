package players;

import org.wikijava.sound.playWave.PlaySound;
import org.wikijava.sound.playWave.PlayVideo;
import org.wikijava.sound.playWave.PlayWaveException;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static players.MediaPlayerMain.setScreenToBlack;

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
  private AtomicBoolean videoStartSignal;

  public MediaPlayer(String videoFilePath, String audioFilePath, JFrame parentFrame, JLabel videoOutLabel) throws IOException, LineUnavailableException, InterruptedException {
    this.videoFilePath = videoFilePath;
    this.audioFilePath = audioFilePath;
    this.pauseSignal = new AtomicBoolean(false);
    this.stopSignal = new AtomicBoolean(false);
    this.videoStartSignal = new AtomicBoolean(false);

    syncSignal = new Object();

    videoPlayer = new PlayVideo(videoStartSignal, pauseSignal, stopSignal, videoFilePath, parentFrame, videoOutLabel);
    soundPlayer = new PlaySound(videoStartSignal, pauseSignal, stopSignal, audioFilePath);

    videoThread = new PlayVideoThread(videoPlayer);
    soundThread = new PlaySoundThread(soundPlayer);
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

    syncSignal = new Object();
    this.pauseSignal.set(false);
    this.stopSignal.set(false);
    videoThread = new PlayVideoThread(videoPlayer);
    soundThread = new PlaySoundThread(soundPlayer);
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

}