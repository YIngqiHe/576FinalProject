package players;

import org.wikijava.sound.playWave.PlaySound;
import org.wikijava.sound.playWave.PlayWaveException;

public class PlaySoundThread extends Thread{

  private PlaySound soundPlayer;

  private final Object syncSignal;

  public PlaySoundThread(Object syncSignal, PlaySound sp) {
    this.syncSignal = syncSignal;
    soundPlayer = sp;
  }

  @Override
  public void run() {
    // plays the sound
    try {
      synchronized (syncSignal) {
        syncSignal.notify();
      }
      soundPlayer.play();
    } catch (PlayWaveException e) {
      e.printStackTrace();
    }
  }
}
