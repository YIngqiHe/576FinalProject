package players;

import org.wikijava.sound.playWave.PlayWaveException;

import java.io.FileNotFoundException;

public class MediaPlayerMain {

  public static void main(String[] args) throws InterruptedException, FileNotFoundException, PlayWaveException {
    String videoFilePath = "Ready_Player_One_rgb/InputVideo.rgb";
    String soundFilePath = "Ready_Player_One_rgb/InputAudio.wav";


    MediaPlayer mediaPlayer = new MediaPlayer(videoFilePath, soundFilePath);
    mediaPlayer.play();
  }
}
