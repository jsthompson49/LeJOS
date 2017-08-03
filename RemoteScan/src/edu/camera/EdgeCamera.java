package edu.camera;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeCamera implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeCamera.class);

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
 	}
	
	private String path;
	private VideoCapture camera = new VideoCapture();
	
	public EdgeCamera(String path) {
		this.path = path;
	}
	
	public boolean open() {
		return camera.open(0);
	}

	@Override
	public void close() {
		camera.release();
	}

	public Mat captureImage() {
		Mat image = null;
        
        if(camera.isOpened()){
            LOGGER.trace("Camera Opened, reading image");
        	image = new Mat();
        	boolean read = camera.read(image);
            LOGGER.info("Camera read opeation: success={}", read);
            if(!read) {
            	image = null;
            }            
        }
        else {
            LOGGER.info("Camera Closed");        	
        }
        
        return image;
	}
	
	public void saveImage(Mat image, String name) {
		Imgcodecs.imwrite(getImageFileName(name), image);
	}
	
	public String getImageFileName(String name) {
        return path + "/capture-" + name + ".jpg";
	}

	public static void main (String args[]) {
		try (EdgeCamera camera = new EdgeCamera("/home/pi/camera/data")) {
			camera.open();
	        Mat frame = camera.captureImage();        
	        Imgcodecs.imwrite("capture2.jpg", frame);
		}
    }
}
