//package org.wikijava.sound.playWave;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//
//
///**
// * plays a wave file using PlaySound class
// *
// * @author Giulio
// */
//public class PlayWaveFile {
//
//	private AudioThread audioThread;
//	// private String filename;
//
//	private class AudioThread extends Thread {
//		private String fileName;
//
//		public AudioThread(String fileName) {
//			this.fileName = fileName;
//		}
//
//        public void run() {
//            try {
//				// opens the inputStream
//				FileInputStream inputStream;
//				try {
//					inputStream = new FileInputStream(fileName);
//					//inputStream = this.getClass().getResourceAsStream(filename);
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//					return;
//				}
//
//				// initializes the playSound Object
//				PlaySound playSound = new PlaySound(inputStream);
//
//				// plays the sound
//				try {
//					playSound.play();
//				} catch (PlayWaveException e) {
//					e.printStackTrace();
//					return;
//				}
//
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * <Replace this with one clearly defined responsibility this method does.>
//     *
//     * @paramargs
//     *            the name of the wave file to play
//     */
//    public void MediaPlay(String filename) {
//
//	// // opens the inputStream
//	// FileInputStream inputStream;
//	// try {
//	//     inputStream = new FileInputStream(filename);
//	//     //inputStream = this.getClass().getResourceAsStream(filename);
//	// } catch (FileNotFoundException e) {
//	//     e.printStackTrace();
//	//     return;
//	// }
//
//	PlayVideo player = new PlayVideo();
//	audioThread = new AudioThread(filename);
//	audioThread.start();
//	player.play();
//
//
//
//
//	// // initializes the playSound Object
//	// PlaySound playSound = new PlaySound(inputStream);
//
//	// // plays the sound
//	// try {
//	//     playSound.play();
//	// } catch (PlayWaveException e) {
//	//     e.printStackTrace();
//	//     return;
//	// }
//    }
//
//	public static void main(String[] args) {
//
//		// get the command line parameters
//		if (args.length < 1) {
//			System.err.println("usage: java -jar PlayWaveFile.jar [filename]");
//			return;
//		}
//		String filename = args[0];
//
//		// // opens the inputStream
//		// FileInputStream inputStream;
//		// try {
//		//     inputStream = new FileInputStream(filename);
//		//     //inputStream = this.getClass().getResourceAsStream(filename);
//		// } catch (FileNotFoundException e) {
//		//     e.printStackTrace();
//		//     return;
//		// }
//
//		// PlayVideo player = new PlayVideo();
//		// audioThread = new AudioThread();
//		// audioThread.start();
//		// player.play();
//
//		ShotDetector shotDetector = new ShotDetector();
//		shotDetector.SceneDetect();
//		String json = shotDetector.getJson();
//		System.out.println(json);
//
//		PlayWaveFile waveFile = new PlayWaveFile();
//		waveFile.MediaPlay(filename);
//
//		}
//
//
//}
