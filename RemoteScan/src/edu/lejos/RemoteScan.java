package edu.lejos;

import lejos.nxt.Motor;
import lejos.nxt.remote.RemoteMotor;

public class RemoteScan {
	public static void main (String[] args) {
		RemoteMotor swivel = Motor.A;
		RemoteMotor elbow = Motor.B;
		RemoteMotor wrist = Motor.C;
		
		LimitReverseMonitor[] monitors = {
			setupRotationToggle(swivel, 60 /* degrees */, 6 /* seconds */, false /* start at midpoint */),		
			setupRotationToggle(elbow, 20 /* degrees */, 2 /* seconds */, true /* start down */),		
			setupRotationToggle(wrist, 10 /* degrees */, 2 /* seconds */, false /* start at midpoint */)		
		};
		
		try {
			while(true) {
				Thread.sleep(300);
				for(int i = 0;i < monitors.length;i++) {
					monitors[i].process();
				}
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
			System.out.println("Motor{" + motor.getId() + "}: tacho=" + tachoCount + " limitAngle=" + motor.getLimitAngle() + " stalled=" + motor.isStalled());
			if(tachoCount <= minTacho) {
				motor.rotate(angle, true /* immediate Return */);								
			}
			else if(tachoCount >= maxTacho) {
				motor.rotate(-angle, true /* immediate Return */);								
			}
		}
	}
}
