package org.mort11.ee;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import org.mort11.util.Constants;
import org.mort11.util.JaguarDealer;

/**
 * End Effector: Arm
 * @author MORT
 * @version 02.12.2011.3
 */
public class Arm {

    private CANJaguar motor;

    private final double UPPER_LIMIT = Constants.Arm.UPPER_LIMIT;
    private final double LOWER_LIMIT = Constants.Arm.LOWER_LIMIT;

    public final double HOME  = Constants.Arm.HOME;
    public final double TROLL = Constants.Arm.TROLL;
    public final double HIGH  = Constants.Arm.HIGH;
    public final double MID   = Constants.Arm.MID;
    public final double LOW   = Constants.Arm.LOW;

    public boolean runningPreset;

    private boolean isRampUp, isRampDown, startRamp;
    private double armSpeed;

    /**
     * Constructs a new Arm with a CANJaguar set to ID 2.
     */
    public Arm() {
        isRampUp = false;
        isRampDown = false;
        startRamp = true;

        armSpeed = 0;
        try {
            motor = JaguarDealer.getJag(2);
            motor.setPositionReference(CANJaguar.PositionReference.kPotentiometer);
            motor.configPotentiometerTurns(1);
            motor.configNeutralMode(CANJaguar.NeutralMode.kBrake);
        } catch(CANTimeoutException ex) {
//            DriverStationLCD.getInstance().println(DriverStationLCD.Line.kMain6, 1, ex.getMessage());
//            DriverStationLCD.getInstance().updateLCD();
            ex.printStackTrace();
        }
    }

    /**
     * Get the speed controller associated with the Arm.
     * @return CANJaguar object that runs the Arm.
     */
    public CANJaguar getSpeedController() {
        return motor;
    }

    /**
     * The position of the Arm.
     * @return A double that represents the percent rotation around the one-turn potentiometer.
     */
    public double getPos() {
        try {
            return motor.getPosition();
        } catch(CANTimeoutException ex) {
//            DriverStationLCD.getInstance().println(DriverStationLCD.Line.kMain6, 1, ex.getMessage());
//            DriverStationLCD.getInstance().updateLCD();
            ex.printStackTrace();
        }
        return -1;
    }

    public void setPos(double preset) {
        double distToPreset = Math.abs(getPos()-preset);
        boolean goingDown = (getPos()-preset > 0);

        if (startRamp) {
            isRampUp = true;
            startRamp = false;
        }

        if (isRampUp) {
            //System.out.println(rampUpDest-getPos());
            armSpeed += 0.05;
            if (armSpeed > 0.95) {
                isRampUp = false;
                armSpeed = 1;
            }
        } else if (isRampDown) {
            armSpeed = Math.abs((preset-getPos())/0.08);
            if (armSpeed < 0.5) {
                armSpeed = 0.5;
            }
        }

        if (distToPreset < 0.005) {
            startRamp = true;
            runningPreset = false;
            isRampDown = false;
            isRampUp = false;
            armSpeed = 0;
        } else if (distToPreset < 0.08) {
            isRampUp = false;
            isRampDown = true;
        }

        if (goingDown) {
            setSpeed(-armSpeed);
        } else {
            setSpeed(armSpeed);
        }
    }

    public void stop() {
        isRampDown = false;
        isRampUp = false;
        startRamp = true;
        armSpeed = 0;
        runningPreset = false;
    }


    /**
     * Pass in the percent voltage and the Arm moves within the constraints.
     * @param speed Percent voltage to send to the Arm speed controller.
     */
    public void setSpeed(double speed) {
        //TODO: Slow down when approaching limit
        speed = -1 * speed;
        double curPos = getPos();
        if(curPos > UPPER_LIMIT || curPos < LOWER_LIMIT || speed == 0.0) {
            try {
                motor.setX(0);
            } catch (CANTimeoutException ex) {
//                DriverStationLCD.getInstance().println(DriverStationLCD.Line.kMain6, 1, ex.getMessage());
//                DriverStationLCD.getInstance().updateLCD();
                ex.printStackTrace();
            }
        }
        if(curPos > LOWER_LIMIT && speed > 0) {
            try {
                motor.setX(speed);
            } catch (CANTimeoutException ex) {
//                DriverStationLCD.getInstance().println(DriverStationLCD.Line.kMain6, 1, ex.getMessage());
//                DriverStationLCD.getInstance().updateLCD();
                ex.printStackTrace();
            }
        }
        if(curPos < UPPER_LIMIT && speed < 0) {
            try {
                motor.setX(speed);
            } catch (CANTimeoutException ex) {
//                DriverStationLCD.getInstance().println(DriverStationLCD.Line.kMain6, 1, ex.getMessage());
//                DriverStationLCD.getInstance().updateLCD();
                ex.printStackTrace();
            }
        }
    }

    /**
     * Manual override to move the Arm without constraints.
     * @param speed Percent voltage to send to the Arm speed controller.
     */
    public void manualMove(double speed) {
        try {
            motor.setX(-speed);
        } catch (CANTimeoutException ex) {
//            DriverStationLCD.getInstance().println(DriverStationLCD.Line.kMain6, 1, ex.getMessage());
//            DriDriverStationLCD.getInstance().println(DriverStationLCD.Line.kMain6, 1, ex.getMessage());verStationLCD.getInstance().updateLCD();
            ex.printStackTrace();
        }
    }
}
