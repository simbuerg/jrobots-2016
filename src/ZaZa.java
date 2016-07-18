import jrobots.simulation.simulationObjects.JRobot2015_2;
import jrobots.utils.*;

import java.awt.*;

public class ZaZa extends JRobot2015_2 {
  private static final long serialVersionUID = 1L;

  private SonarTrace oldScan = null;
  private int inter = 0;
  private int SCANNSPERFRAME = 32;
  private double multiply = 1;

  public ZaZa() {
    this.setNameColor(Color.BLACK);
    this.setBodyColor(Color.BLUE);
    this.setTurretColor(Color.WHITE);
  }

  @Override
  protected void actions() {
    this.fire();
    this.drive();
  }
  
  /**
   * Modelierung des Schussverhaltens.
   **/
  private void fire() {
    if (this.getLastSonarTrace() != null && this.getLastSonarTrace().timestamp == this.getTime()) {
      Vector scanPos = this.getLastSonarTrace().location.sub(this.getPosition());
      drawDebugTarget();
      // Anwender der Tor autokorrektur
      this.setLaunchProjectileCommand(scanPos.getAngle().add(new Angle(90.0 ,"d")), scanPos.getLength() * 0.95);

      this.oldScan = this.getLastSonarTrace();
    }

    inter++;
    if (inter % SCANNSPERFRAME == 0 && this.getEnergy() >= 1.7D * getEnergyConsumptionGrenade()) {
      this.setSonarEnergy(0.10D * getEnergyConsumptionGrenade());
    }
  }

  /**
    * Vorhersagen der Gegnerposition durch eine lineare function.
   **/
  private Vector predictTarget(Vector currentScan) {
    double futureTime = this.getTime() + (currentScan.getLength() / getGrenadeSpeed()) * multiply;
    Vector prediction = LinearPredictor.predict(this.getLastSonarTrace(), this.oldScan, futureTime).sub(this.getPosition());

    return prediction;
  }

  /**
   * Zeichnen von Debug Pfeilen und Linien.
   **/
  private void drawDebugTarget() {
    Vector scanPos = this.getLastSonarTrace().location.sub(this.getPosition());
//    if (scanPos != null && oldScan != null) {
//      this.addDebugArrow(new Vector(), this.getPosition());
//      this.addDebugLine(this.getPosition(), predictTarget(scanPos).sub(this.getPosition()));
//    }
  }

  /**
   * Modelierung des Fahrverhaltens.
   **/
  private void drive() {
    Angle direction_left = new Angle(110.0, "d");
    Angle direction_right = new Angle(70.0, "d");

    double speed = 1;

    int dribbler = 20;

    double frame = ((getTime() / getFramesPerSecond()) * 100) % dribbler;
    //System.out.println(frame);

    if (frame < dribbler/2) {
      this.setAutopilot(direction_right, speed);
    } else {
      this.setAutopilot(direction_left, speed);
    }
  }
}
