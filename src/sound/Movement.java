package sound;

import java.util.Random;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.NXTLightSensor;
import lejos.hardware.sensor.NXTSoundSensor;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;

public class Movement {

	NXTSoundSensor soundNXT;
	SoundSensor soundSensor;

	NXTLightSensor light;
	SampleProvider lightSampleProvider;
	float[] lightSampleArray;

	EV3ColorSensor colorSensor;
	SampleProvider colorProvider;
	float[] colorSample;

	MovePilot pilot;
	Random rand;
	TouchSensor touch;

	public static void main(String[] args) {
		new Movement();
	}

	// Movement is controlled by two sensors, the touch sensor, and the color
	// sensor.
	// Touch changes direction/angle
	// Color controls where the robot ultimately stops, try to get both sensors
	// on black before stopping

	public Movement() {
		intro();
		// approximate measurements in inches, measure.
		Wheel leftWheel = WheeledChassis.modelWheel(Motor.B, 1.5f).offset(-2.25);
		Wheel rightWheel = WheeledChassis.modelWheel(Motor.C, 1.5f).offset(2.25);
		Chassis chassis = new WheeledChassis(new Wheel[] { leftWheel, rightWheel }, WheeledChassis.TYPE_DIFFERENTIAL);
		pilot = new MovePilot(chassis);
		rand = new Random();
		Brick brick = BrickFinder.getDefault();
		Port s3 = brick.getPort("S3"); // port 3 is where the touch sensor is
		Port s1 = brick.getPort("S1"); // port 1 is ev3 color sensor
		Port s4 = brick.getPort("S4"); // port 2 is the recommended port for nxt
										// light sensors
		Port s2 = brick.getPort("S2");

		EV3TouchSensor sensor = new EV3TouchSensor(s4);

		soundNXT = new NXTSoundSensor(s2);
		soundSensor = new SoundSensor(soundNXT.getDBMode());

		light = new NXTLightSensor(s3);
		light.setFloodlight(true);
		lightSampleProvider = light.getRedMode();

		lightSampleArray = new float[light.sampleSize()];

		colorSensor = new EV3ColorSensor(s1);
		colorProvider = colorSensor.getColorIDMode();
		colorSample = new float[colorSensor.sampleSize()];

		touch = new TouchSensor(sensor);
		pilot.setLinearSpeed(2.5f);

		//TESTME
		while (soundSensor.music()) {
			
			pilot.forward();
			
			while (true) {

				// do not sample too quickly !!!! Always check the light ranges
				// for the light sensor
				Delay.msDelay(30);

				colorSensor.fetchSample(colorSample, 0);
				lightSampleProvider.fetchSample(lightSampleArray, 0);

				// user cancel:
				if (Button.ESCAPE.isDown()) {
					pilot.stop();
					sensor.close();
					colorSensor.close();
					light.close();

					System.exit(0);
				}

				// bumper code for touch sensor
				if (touch.pressed()) { // if pressed is true
					pilot.stop();
					pilot.travel(-6); // back up 6 inches
					if (rand.nextBoolean()) {
						pilot.rotate(45); // right rotate
					} else {

						pilot.rotate(-45); // left rotate
					}
					pilot.forward();

				}

				if ((colorSensor.getColorID() == Color.BLACK) && (lightSampleArray[0] <= .39f)) { // winner
					pilot.stop();
					System.out.println("winner");
					sensor.close();
					colorSensor.close();
					light.close();
					System.exit(0);

				}

				// left colorSample has black, right lSA does not have black
				if ((colorSensor.getColorID() == Color.BLACK) && (lightSampleArray[0] >= .39f)) {
					System.out.println("right no black " + lightSampleArray[0]);
					pilot.stop();
					pilot.arc(2, -15);
					pilot.forward();

				}

				// left colorSample does not have black, right lSA has black
				if ((colorSensor.getColorID() != Color.BLACK) && (lightSampleArray[0] <= .39f)) {
					System.out.println("left no black " + lightSampleArray[0]);
					pilot.stop();
					pilot.arc(2, 15);
					pilot.forward();
				}

			} // end while true
		} // end while sound
	}// end movement

	// this method starts the program gracefully with go and quit
	public void intro() {

		GraphicsLCD g = LocalEV3.get().getGraphicsLCD();
		g.setFont(Font.getSmallFont());
		int y_quit = 100;
		int width_quit = 45;
		int height_quit = width_quit / 2;
		int arc_diam = 6;
		g.drawString("QUIT", 9, y_quit + 7, 0);
		g.drawLine(0, y_quit, 45, y_quit); // top line
		g.drawLine(0, y_quit, 0, y_quit + height_quit - arc_diam / 2); // left
		g.drawLine(width_quit, y_quit, width_quit, y_quit + height_quit / 2); // right
		g.drawLine(0 + arc_diam / 2, y_quit + height_quit, width_quit - 10, y_quit + height_quit); // bottom
		g.drawLine(width_quit - 10, y_quit + height_quit, width_quit, y_quit + height_quit / 2); // diagonal
		g.drawArc(0, y_quit + height_quit - arc_diam, arc_diam, arc_diam, 180, 90);
		g.fillRect(width_quit + 10, y_quit, height_quit, height_quit);
		g.drawString("GO", width_quit + 15, y_quit + 7, 0, true);
		Button.waitForAnyPress();
		if (Button.ESCAPE.isDown())
			System.exit(0);
		g.clear();
	}

}// end class movement
