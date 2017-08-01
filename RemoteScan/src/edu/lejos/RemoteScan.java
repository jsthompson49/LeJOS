package edu.lejos;

import java.io.File;
import java.util.UUID;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

import edu.camera.EdgeCamera;
import edu.camera.FaceDetector;

import lejos.nxt.Motor;
import lejos.nxt.remote.RemoteMotor;

public class RemoteScan {
	
	private static final String CAMERA_PATH = "/home/pi/camera/data";
	private static final long SCAN_INTERVAL = 500;
	private static final long MOTOR_INTERVAL = 500;
	private static final int CLEANUP_LOOP_COUNT = 100;
	private static final long STALE_MILLIS = 60 * 1000;
	
	public static void main (String[] args) {
		
		// Run camera scan in separate thread
		ViewScanner viewScanner = new ViewScanner(CAMERA_PATH, SCAN_INTERVAL);
		new Thread(viewScanner).start();
		
		RemoteMotor swivel = Motor.A;
		//RemoteMotor elbow = Motor.B;
		//RemoteMotor wrist = Motor.C;
		
		LimitReverseMonitor[] monitors = {
			setupRotationToggle(swivel, 40 /* degrees */, 4 /* seconds */, false /* start at midpoint */),		
			//setupRotationToggle(elbow, 5 /* degrees */, 2 /* seconds */, true /* start down */),		
			//setupRotationToggle(wrist, 5 /* degrees */, 2 /* seconds */, false /* start at midpoint */)		
		};
		
		ImageFileCleaner imageFileCleaner = new ImageFileCleaner(CAMERA_PATH, STALE_MILLIS);
		int loopCount = 0;
		try {
			while(true) {
				Thread.sleep(MOTOR_INTERVAL);
				for(int i = 0;i < monitors.length;i++) {
					monitors[i].process();
				}
				if((loopCount % CLEANUP_LOOP_COUNT) == 0) {
					new Thread(imageFileCleaner).start();
				}
				loopCount++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private static LimitReverseMonitor setupRotationToggle(RemoteMotor motor, int angle, int durationSeconds, boolean fullStart) {		
		int fullAngle = angle * 2;
		int speed = fullAngle / durationSeconds;
		motor.setSpeed(speed);
		motor.rotate(fullStart ? fullAngle : angle);		
		return new LimitReverseMonitor(motor, angle, !fullStart);
	}
	
	private static class LimitReverseMonitor {
		private RemoteMotor motor;
		private int angle;
		private int minTacho;
		private int maxTacho;
		
		public LimitReverseMonitor(RemoteMotor motor, int rotationAngle, boolean centered) {
			this.motor = motor;
			angle = 2 * rotationAngle;
			minTacho = centered ? -rotationAngle : 0;
			maxTacho = centered ? rotationAngle : angle;
		}
		
		public void process() {			
			int tachoCount = motor.getTachoCount();
			//System.out.println("Motor{" + motor.getId() + "}: tacho=" + tachoCount + " limitAngle=" + motor.getLimitAngle() + " stalled=" + motor.isStalled());
			if(tachoCount <= minTacho) {
				motor.rotate(angle, true /* immediate Return */);								
			}
			else if(tachoCount >= maxTacho) {
				motor.rotate(-angle, true /* immediate Return */);								
			}
		}
	}

	private static class ViewScanner implements Runnable {
		private EdgeCamera camera;
		private long interval;
		private FaceDetector faceDetector = new FaceDetector("/home/pi/opencv-3.1.0/data/haarcascades/haarcascade_frontalface_default.xml");

		public ViewScanner(String path, long interval) {
			camera = new EdgeCamera(path);
			this.interval = interval;
		}
		
		@Override
		public void run() {
			camera.open();
			while(true) {
		        Mat image = camera.captureImage();
		        if(image != null) {
					MatOfRect faces = faceDetector.detect(image);
			        Rect[] rectangles = faces.toArray();
			        System.out.println(new java.util.Date() + " Faces Detected: count=" + rectangles.length);
			        if(rectangles.length > 0) {
			            faceDetector.drawDetected(image, faces);
			        	camera.saveImage(image, UUID.randomUUID().toString());
			        }
		        }
		        
		        if(interval > 0) {
					try {
						Thread.sleep(interval);					
					}
					catch (InterruptedException ie) {
						// Ignore
					}
		        }
			}
		}

		@Override
		protected void finalize() {
			camera.close();
		}		
	}
	
	private static class ImageFileCleaner implements Runnable {
		private String path;
		private long staleMillis;

		public ImageFileCleaner(String path, long staleMillis) {
			this.path = path;
			this.staleMillis = staleMillis;
		}
		
		@Override
		public void run() {
			File imageFileDirectory = new File(path);
			File[] stale = imageFileDirectory.listFiles(f -> f.lastModified() < (System.currentTimeMillis() - staleMillis));
			int count = (stale == null) ? 0 : stale.length;
			System.out.println("Cleaning stale files: count=" + count);
			for(int i = 0;i < count;i++) {
				stale[i].delete();
			}
		}
	}

}
