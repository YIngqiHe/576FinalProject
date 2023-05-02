package org.wikijava.sound.playWave;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

/**
 *
 * <Replace this with a short description of the class.>
 *
 * @author Giulio
 */
public class PlaySound {

  private String audioFilePath;

  private InputStream waveStream;

  private AudioInputStream audioInputStream;

  private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

  private Info info;

  private SourceDataLine dataLine = null;

  private Object syncSignal;

  private AtomicBoolean pauseSignal;
  private AtomicBoolean stopSignal;

  private boolean isStarted = false;
  private boolean isStopped = false;

  /**
   * CONSTRUCTOR
   */
  public PlaySound(Object syncSignal, AtomicBoolean pauseSignal, AtomicBoolean stopSignal, String audioFilePath) throws PlayWaveException, LineUnavailableException, IOException, InterruptedException {
    this.audioFilePath = audioFilePath;
    this.syncSignal = syncSignal;
    this.pauseSignal = pauseSignal;
    this.stopSignal = stopSignal;

    seek(0);
  }

  public void seek(double momentSeconds) throws PlayWaveException, LineUnavailableException, IOException, InterruptedException {
    try {
      this.waveStream = new FileInputStream(this.audioFilePath);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    audioInputStream = null;
    try {
      //audioInputStream = AudioSystem.getAudioInputStream(this.waveStream);

      //add buffer for mark/reset support, modified by Jian
      InputStream bufferedIn = new BufferedInputStream(this.waveStream);
      audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
    } catch (UnsupportedAudioFileException e1) {
      throw new PlayWaveException(e1);
    } catch (IOException e1) {
      throw new PlayWaveException(e1);
    }

    // Obtain the information about the AudioInputStream
    AudioFormat audioFormat = audioInputStream.getFormat();
    info = new Info(SourceDataLine.class, audioFormat);

    System.out.println("The Audio Info: ");
    System.out.println(info);

    dataLine = (SourceDataLine) AudioSystem.getLine(info);
    dataLine.open(audioFormat);

    long bytePosition = (long) (momentSeconds * audioFormat.getFrameRate() * audioFormat.getFrameSize());

    // Skip the data until the desired position
    audioInputStream.skip(bytePosition);
  }

  public void stop() {
    if (dataLine != null && dataLine.isActive()) {
      // plays what's left and and closes the audioChannel
      dataLine.drain();
      dataLine.close();
    }
  }

  public void pause() {
    System.out.println("Audio is paused");
    dataLine.stop();
  }

  public void play() throws PlayWaveException {
    // Starts the music :P
    if (stopSignal.get()) {
      return;
    }
    if (!isStarted) {
      synchronized (syncSignal) {
        try {
          System.out.println("Wait for video");
          syncSignal.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    isStarted = true;
    dataLine.start();


    int readBytes = 0;
    byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];

    try {
      while (readBytes != -1) {
        while (pauseSignal.get()) {
          // busy wait if pauseSignal set to true
          if (!isStopped) {
            isStopped = true;
            dataLine.stop();
          }
          if (stopSignal.get()) {
            break;
          }
        }
        if (stopSignal.get()) {
          break;
        }
        if (isStopped) {
          isStopped = false;
          dataLine.start();
        }
        readBytes = audioInputStream.read(audioBuffer, 0,
            audioBuffer.length);
        if (readBytes >= 0) {
          dataLine.write(audioBuffer, 0, readBytes);
        }
      }
    } catch (IOException e1) {
      throw new PlayWaveException(e1);
    } finally {
      // plays what's left and and closes the audioChannel
      dataLine.drain();
      dataLine.close();
    }

  }
}
