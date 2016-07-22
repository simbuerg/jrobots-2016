import java.awt.Color;

import jrobots.simulation.simulationObjects.JRobot2015_3;
import jrobots.utils.Angle;
import jrobots.utils.SonarTrace;
import jrobots.utils.Vector;

public class Random_Test_Bot extends JRobot2015_3 {
	private static final long serialVersionUID = 1L;

	private SonarTrace oldScan, newScan = null;

	private Angle latest_angle;

	private int counter;

	private double speed;

	private boolean help;
	private double help_angle = 100;

	public void init() {
		latest_angle = Angle.NORTH.add(new Angle(help_angle / 2, "d"));
		speed = 5;
		setBoost();

		setBodyColor(new Color(0xff00ff));
		setTurretColor(new Color(0x79ff4d));
	}

	@Override
	protected void actions() {
		counter++;
		scan();

		shoot();

		drive();
	}

	private void shoot() {
		if (counter > 150) {
			help_angle = 80;
		}
		shoot_circle();
	}

	private void drive() {
		setAutopilot(latest_angle, speed);
		if (counter % 80 == 0) {

			if (help) {
				latest_angle = latest_angle.add(new Angle(help_angle, "d"));
			} else {
				latest_angle = latest_angle.sub(new Angle(help_angle, "d"));
			}
			help = !help;
		}

	}

	private void shoot_circle() {
		if (counter % 80 == 0) {
			if (newScan != null && oldScan != null) {
				Vector v = newScan.location.sub(getPosition());

				Angle a1 = oldScan.location.sub(getPosition()).getAngle();
				Angle a2 = newScan.location.sub(getPosition()).getAngle();

				Angle dif = a1.sub(a2);

				if (dif.isPositive()) {
					dif.add(new Angle(90, "d"));
				} else {
					dif.sub(new Angle(90, "d"));
				}

				setLaunchProjectileCommand(v.getAngle().sub(dif));

			}
		}
	}

	private void scan() {

		if (counter % 20 == 0) {

			if (getEnergy() > 0.70) {
				setSonarEnergy(0.05);

				oldScan = newScan;
				newScan = getLastSonarTrace();
			}
		}
	}
}
