/**
 * This Java FRC robot application is meant to demonstrate an example using the Motion Profile control mode
 * in Talon SRX.  The CANTalon class gives us the ability to buffer up trajectory points and execute them
 * as the roboRIO streams them into the Talon SRX.
 * 
 * There are many valid ways to use this feature and this example does not sufficiently demonstrate every possible
 * method.  Motion Profile streaming can be as complex as the developer needs it to be for advanced applications,
 * or it can be used in a simple fashion for fire-and-forget actions that require precise timing.
 * 
 * This application is an IterativeRobot project to demonstrate a minimal implementation not requiring the command 
 * framework, however these code excerpts could be moved into a command-based project.
 * 
 * The project also includes instrumentation.java which simply has debug printfs, and a MotionProfile.java which is generated
 * in @link https://docs.google.com/spreadsheets/d/1PgT10EeQiR92LNXEOEe3VGn737P7WDP4t0CQxQgC8k0/edit#gid=1813770630&vpid=A1
 * 
 * Logitech Gamepad mapping, use left y axis to drive Talon normally.  
 * Press and hold top-left-shoulder-button5 to put Talon into motion profile control mode.
 * This will start sending Motion Profile to Talon while Talon is neutral. 
 * 
 * While holding top-left-shoulder-button5, tap top-right-shoulder-button6.
 * This will signal Talon to fire MP.  When MP is done, Talon will "hold" the last setpoint position
 * and wait for another button6 press to fire again.
 * 
 * Release button5 to allow OpenVoltage control with left y axis.
 */

package org.usfirst.frc.team3539.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;

public class Robot extends IterativeRobot {

	/** The Talon we want to motion profile. */
	CANTalon _talon = new CANTalon(5);
	CANTalon _talonFollower = new CANTalon(6);
	CANTalon leftLead4 = new CANTalon(4);
	CANTalon leftFollow3 = new CANTalon(3);

	
	//set up lead and follow!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	
	
	/** some example logic on how one can manage an MP */
	RightMotionProfile _example = new RightMotionProfile(_talon);
	LeftMotionProfile leftProfile = new LeftMotionProfile(leftLead4);
	//change the "motion profile example" class to be right motion profile then change the above reference to match
	
	//copy right motion profile class and rename to left
	//repeat line 45 for left
	
	/** joystick for testing */
	Joystick _joy= new Joystick(0);

	/** cache last buttons so we can detect press events.  In a command-based project you can leverage the on-press event
	 * but for this simple example, lets just do quick compares to prev-btn-states */
	boolean [] _btnsLast = {false,false,false,false,false,false,false,false,false,false};


	public Robot() { // could also use RobotInit()
		_talon.setFeedbackDevice(CANTalon.FeedbackDevice.QuadEncoder);
		_talon.configEncoderCodesPerRev(1024);
		_talon.setF(.34);
		_talon.setP(.2);
		_talon.setI(0);
		_talon.setD(0);
		
		_talon.reverseOutput(false);
		_talon.reverseSensor(false);
		
		leftLead4.setFeedbackDevice(CANTalon.FeedbackDevice.QuadEncoder);
		leftLead4.configEncoderCodesPerRev(1024);
		leftLead4.setF(.34);
		leftLead4.setP(.2);
		leftLead4.setI(0);
		leftLead4.setD(0);
		
		leftLead4.reverseOutput(true);
		leftLead4.reverseSensor(true);
		/* keep sensor and motor in phase */
		//make sure this s the same as the one in test bed drivetrain!!!!!!!!!!!!!!!!!!!!!!!!!
		//be sure to havePIDF 
	
		_talonFollower.changeControlMode(TalonControlMode.Follower);
		_talonFollower.set(_talon.getDeviceID());
		
		leftFollow3.changeControlMode(TalonControlMode.Follower);
		leftFollow3.set(leftLead4.getDeviceID());
// pick up here, continuing to implement motion profile for the left hand side of the drivetrain!!!!!!!!!!!!!!!!!! 1/20/2017	
	}
	/**  function is called periodically during operator control */
    public void teleopPeriodic() {
		/* get buttons */
		boolean [] btns= new boolean [_btnsLast.length];
		for(int i=1;i<_btnsLast.length;++i)
			btns[i] = _joy.getRawButton(i);

		/* get the left joystick axis on Logitech Gampead */
		double leftYjoystick = -1 * _joy.getRawAxis(1); /* multiple by -1 so joystick forward is positive */
									// change this to xbox controller raw axis!!!!!!!!!!!!!!!!!!!!
		
		double rightYjoystick = -1 * _joy.getRawAxis(5);
		
		/* call this periodically, and catch the output.  Only apply it if user wants to run MP. */
		_example.control();
		leftProfile.control();
		
		if (btns[5] == false) { /* Check button 5 (top left shoulder on the logitech gamead). */
			/*
			 * If it's not being pressed, just do a simple drive.  This
			 * could be a RobotDrive class or custom drivetrain logic.
			 * The point is we want the switch in and out of MP Control mode.*/
		
			/* button5 is off so straight drive */
			_talon.changeControlMode(TalonControlMode.Voltage);
			_talon.set(12.0 * leftYjoystick);
			leftLead4.changeControlMode(TalonControlMode.Voltage);
			leftLead4.set(12.0 * rightYjoystick);

			_example.reset();
			leftProfile.reset();
			
		} else {
			/* Button5 is held down so switch to motion profile control mode => This is done in MotionProfileControl.
			 * When we transition from no-press to press,
			 * pass a "true" once to MotionProfileControl.
			 */
			_talon.changeControlMode(TalonControlMode.MotionProfile);
			
			
			leftLead4.changeControlMode(TalonControlMode.MotionProfile);	
			
			
			CANTalon.SetValueMotionProfile setOutput = _example.getSetValue();
			CANTalon.SetValueMotionProfile setOutputLeft = leftProfile.getSetValue();
					
			_talon.set(setOutput.value);
			leftLead4.set(setOutputLeft.value);

			/* if btn is pressed and was not pressed last time,
			 * In other words we just detected the on-press event.
			 * This will signal the robot to start a MP */
			if( (btns[6] == true) && (_btnsLast[6] == false) ) {
				/* user just tapped button 6 */
				_example.startMotionProfile();
				leftProfile.startMotionProfile();
			}
		}
//------------------------------ Repeating all of the steps for the left side of the drivetrain---------------------//

		/*if (btns[1] == false) { /* Check button 5 (top left shoulder on the logitech gamead). */
			/*
			 * If it's not being pressed, just do a simple drive.  This
			 * could be a RobotDrive class or custom drivetrain logic.
			 * The point is we want the switch in and out of MP Control mode.*/
		
			/* button5 is off so straight drive */
			/*leftLead4.changeControlMode(TalonControlMode.Voltage);
			leftLead4.set(12.0 * rightYjoystick);*/

			//leftProfile.reset();
		//} else {
			/* Button5 is held down so switch to motion profile control mode => This is done in MotionProfileControl.
			 * When we transition from no-press to press,
			 * pass a "true" once to MotionProfileControl.
			 */
			/*leftLead4.changeControlMode(TalonControlMode.MotionProfile);
			
			CANTalon.SetValueMotionProfile setOutput = leftProfile.getSetValue();
					
			leftLead4.set(setOutput.value);*/

			/* if btn is pressed and was not pressed last time,
			 * In other words we just detected the on-press event.
			 * This will signal the robot to start a MP */
			//if( (btns[2] == true) && (_btnsLast[2] == false) ) {
				/* user just tapped button 6 */
				/*leftProfile.startMotionProfile();
	
			}
		} */
    
   

		/* save buttons states for on-press detection */
		for(int i=1;i<10;++i)
			_btnsLast[i] = btns[i];

	}
	/**  function is called periodically during disable */
	public void disabledPeriodic() {
		/* it's generally a good idea to put motor controllers back
		 * into a known state when robot is disabled.  That way when you
		 * enable the robot doesn't just continue doing what it was doing before.
		 * BUT if that's what the application/testing requires than modify this accordingly */
		_talon.changeControlMode(TalonControlMode.PercentVbus);
		_talon.set( 0 );
		
		leftLead4.changeControlMode(TalonControlMode.PercentVbus);
		leftLead4.set(0);
		/* clear our buffer and put everything into a known state */
		_example.reset();
		leftProfile.reset();
	}
}
