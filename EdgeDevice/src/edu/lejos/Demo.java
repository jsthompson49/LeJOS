package edu.lejos;

import lejos.nxt.Button;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;

public class Demo {
	public static void main (String[] args) {
		NXTRegulatedMotor swivel = new NXTRegulatedMotor(MotorPort.A);
		NXTRegulatedMotor elbow = new NXTRegulatedMotor(MotorPort.B);
		NXTRegulatedMotor wrist = new NXTRegulatedMotor(MotorPort.C);
		
		setupRotationToggle(swivel, 60 /* degrees */, 6 /* seconds */, false /* start at midpoint */);		
		setupRotationToggle(elbow, 10 /* degrees */, 4 /* seconds */, true /* start down */);		
		setupRotationToggle(wrist, 5 /* degrees */, 2 /* seconds */, false /* start at midpoint */);		
		
		while(Button.ESCAPE.isUp()) {
			System.out.println("Swivel(tach)=" + swivel.getTachoCount());
		}
	}

	private static void setupRotationToggle(NXTRegulatedMotor motor, int angle, int durationSeconds, boolean fullStart) {		
		int fullAngle = angle * 2;
		motor.addListener(new LimitReverseListener(fullAngle));
		int speed = fullAngle / durationSeconds;
		motor.setSpeed(speed);
		motor.rotate(fullStart ? fullAngle : angle);		
	}
	
	private static class LimitReverseListener implements RegulatedMotorListener {
		private int rotationAngle;
		
		public LimitReverseListener(int rotationAngle) {
			this.rotationAngle = rotationAngle;
		}

		@Override
		public void rotationStarted(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp) {
			// Not needed
		}

		@Override
		public void rotationStopped(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp) {
			rotationAngle = -rotationAngle;
			motor.rotate(rotationAngle, true /* immediate Return */);			
		}
	}
}


