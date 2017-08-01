package edu.camera;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

public class EdgeCamera implements AutoCloseable {

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
            //System.out.println("Camera Opened, reading image");
        	image = new Mat();
        	boolean read = camera.read(image);
            System.out.println("Camera read opeation: " + (read ? "success" : "fail"));
            if(!read) {
            	image = null;
            }            
        }
        else {
            System.out.println("Camera Closed");        	
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
