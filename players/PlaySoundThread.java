package players;

import org.wikijava.sound.playWave.PlaySound;
import org.wikijava.sound.playWave.PlayWaveException;

public class PlaySoundThread extends Thread{

  private PlaySound soundPlayer;

  public PlaySoundThread(PlaySound sp) {
    soundPlayer = sp;
  }

  @Override
  public void run() {
    // plays the sound
    try {
      soundPlayer.play();
    } catch (PlayWaveException e) {
      e.printStackTrace();
    } finally {
      System.out.println("sound thread is quitted");
    }
  }
}
