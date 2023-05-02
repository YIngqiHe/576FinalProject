package players;

import org.wikijava.sound.playWave.PlayWaveException;

import javax.sound.sampled.LineUnavailableException;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MediaPlayerMain {

  public static void main(String[] args) throws InterruptedException, IOException, PlayWaveException, LineUnavailableException {
    String videoFilePath = "Ready_Player_One_rgb/InputVideo.rgb";
    String soundFilePath = "Ready_Player_One_rgb/InputAudio.wav";


    MediaPlayer mediaPlayer = new MediaPlayer(videoFilePath, soundFilePath);
    mediaPlayer.play();
  }
}
