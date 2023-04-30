package config;

public class Config {
  private int videoWidth = 480;
  private int videoHeight = 270;
  private int videoFps = 70;
  private int videoNumOfFrames = 8682;

  public Config() {
  }

  public int getVideoWidth() {
    return videoWidth;
  }

  public void setVideoWidth(int videoWidth) {
    this.videoWidth = videoWidth;
  }

  public int getVideoHeight() {
    return videoHeight;
  }

  public void setVideoHeight(int videoHeight) {
    this.videoHeight = videoHeight;
  }

  public int getVideoFps() {
    return videoFps;
  }

  public void setVideoFps(int videoFps) {
    this.videoFps = videoFps;
  }

  public int getVideoNumOfFrames() {
    return videoNumOfFrames;
  }

  public void setVideoNumOfFrames(int videoNumOfFrames) {
    this.videoNumOfFrames = videoNumOfFrames;
  }
}
