package edu.camera;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaceDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeCamera.class);

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
 	}
	
	private CascadeClassifier faceCascade;
	private int minFaceSize = 80;
	private int maxFaceSize = 300;
	
	public FaceDetector(String classifierPath) {
		faceCascade = new CascadeClassifier(classifierPath);
	}
	
	public MatOfRect detect(Mat image)
	{
		MatOfRect faces = new MatOfRect();
		Mat grayImage = new Mat();
		
		// convert the frame in gray scale
		Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
		
		// equalize the frame histogram to improve the result
		Imgproc.equalizeHist(grayImage, grayImage);
		
		// compute minimum face size (20% of the frame height, in our case)
		//if (this.absoluteFaceSize == 0)
		//{
		//	int height = grayFrame.rows();
		//	if (Math.round(height * 0.2f) > 0)
		//	{
		//		this.absoluteFaceSize = Math.round(height * 0.2f);
		//	}
		//}
		
		// detect faces
		faceCascade.detectMultiScale(grayImage, faces, 1.3 /* scaleFactor */,
				5 /* minNeighbors */, 0 | Objdetect.CASCADE_SCALE_IMAGE,
				new Size(minFaceSize, minFaceSize), new Size(maxFaceSize, maxFaceSize));

		LOGGER.debug("Detected: {}", faces.size());
				
		return faces;
	}
		
	public void drawDetected(Mat image, MatOfRect detected) {
		Rect[] rectangles = detected.toArray();
		for (int i = 0; i < rectangles.length; i++) {
			LOGGER.debug("Drawing detected: {}", rectangles[i]);
			Imgproc.rectangle(image, rectangles[i].tl(), rectangles[i].br(), new Scalar(0, 255, 0), 3);
		}
	}
    
	public static void main (String args[]) {
		try (EdgeCamera camera = new EdgeCamera("/home/pi/camera/data")) {
			camera.open();
	        Mat frame = camera.captureImage();        
	        FaceDetector faceDetector = new FaceDetector("/home/pi/opencv-3.1.0/data/haarcascades/haarcascade_frontalface_default.xml");
	        MatOfRect faces = faceDetector.detect(frame);
	        faceDetector.drawDetected(frame, faces);
	        Imgcodecs.imwrite("capture.jpg", frame);
		}
    }

}
