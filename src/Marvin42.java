import jrobots.simulation.simulationObjects.JRobot2015_2;
import jrobots.utils.Angle;
import jrobots.utils.LinearPredictor;
import jrobots.utils.SonarTrace;
import jrobots.utils.Vector;

import java.awt.*;
import java.io.Serializable;
import java.util.Random;

/**
 *  * A minimal functional Bot, ready for being programmed.
 *   * <p>Please enter the documentation via the method <tt>actions()</tt>.
 *    * <p>Please rename uniquely. The individual name will be displayed in the GUI.
 *     *
 *      * @see JRobot2015_2#actions()
 *       */
public class Marvin42 extends JRobot2015_2 {
  private static final long serialVersionUID = 1L;
  private static Random rand = new Random();

  private class Tuple<T extends Serializable, S extends Serializable> implements Serializable {
    T first;
    S second;

    Tuple(T first, S second) {
      this.first = first;
      this.second = second;
    }
  }

  private SonarTrace oldScan = null;
  private Vector oldSVector = null;
  private int inter = 0;
  private int FRAMESPERSCAN = 32;
  private Vector lastPrediction = null;
  private double multiply = 1;
  private Tuple<Vector, Vector>[] postitions = new Tuple[2];
  private boolean flag = true;

  public Marvin42() {
    this.setNameColor(Color.CYAN);
    this.setBodyColor(Color.YELLOW);
    this.setTurretColor(Color.GREEN);
  }

  @Override
  protected void actions() {
    this.blink();
    this.fire();
    this.drive();
  }

  private void blink() {
    double frame = (getTime() / getFramesPerSecond()) * 25;
    if (flag) {
      this.setNameColor(Color.YELLOW);
      this.setBodyColor(Color.RED);
      this.setTurretColor(Color.WHITE);
    } else {
      this.setNameColor(Color.MAGENTA);
      this.setBodyColor(Color.WHITE);
      this.setTurretColor(Color.RED);
    }
    flag = !flag;
  }

  private void fire() {
    if (this.getLastSonarTrace() != null && this.getLastSonarTrace().timestamp == this.getTime()) {
      Vector scanPos = this.getLastSonarTrace().location.sub(this.getPosition());
      drawDebugTargets();
      if (this.oldScan != null && this.getLastSonarTrace().timestamp - this.oldScan.timestamp < 5.0D) {
        if (postitions[0] != null && postitions[1] != null) {
          if (postitions[0].first.distanceTo(postitions[0].second) <
              postitions[1].first.distanceTo(postitions[1].second)) {
            multiply = 1.1;
          } else {
            multiply = 0.9;
          }
        } else {
          multiply = 1;
        }

        Vector pred = predictTarget();
        postitions[0] = postitions[1];
        postitions[1] = new Tuple<>(this.getPosition(), scanPos);

        this.setLaunchProjectileCommand(pred.getAngle(), pred.getLength() * 0.95);
      } else {
        //this.setLaunchProjectileCommand(scanPos.getAngle(), scanPos.getLength() * 0.95);
      }

      this.oldScan = this.getLastSonarTrace();
    }

    inter++;
    if (inter % FRAMESPERSCAN == 0 && this.getEnergy() >= 1.2D * getEnergyConsumptionGrenade()) {
      this.setSonarEnergy(0.7);
    }
  }

  private Vector predictTarget() {
    //double futureTime = this.getTime() + (currentScanPos.getLength() / getGrenadeSpeed()) * multiply;
    double futureTime = this.getTime() + (this.getPosition().distanceTo(this.getLastSonarTrace().location) / getGrenadeSpeed()) * multiply;
    Vector prediction = LinearPredictor.predict(this.getLastSonarTrace(), this.oldScan, futureTime).sub(this.getPosition());

    return prediction;
  }

  private void drawDebugTargets() {
    Vector scanPos = this.getLastSonarTrace().location.sub(this.getPosition());
    if (scanPos != null && oldScan != null) {
      this.addDebugArrow(new Vector(), predictTarget());
      this.addDebugArrow(new Vector(), this.getPosition());
      this.addDebugLine(this.getPosition(), predictTarget().sub(this.getPosition()));
    }
  }

  private void drive() {
    Vector scanProjectile = getProjectileRadar() == null ? null : getProjectileRadar().pos;
    Vector scanMine = getMineDetectorScan();

    double frame = (getTime() / getFramesPerSecond()) * 25;
    double dir = Math.sin(frame) + (rand.nextDouble() * rand.nextInt(2) == 0 ? -1 : 1);

    setAutopilot(new Angle(dir, "r"), 1);

    Angle evadeAngle = null;
    int evadeInt = 0;

    if (scanProjectile != null && getProjectileRadar().timeOfScan >= getTime() - getFramesPerSecond()) {
      Vector ownPos = getPosition();
      Vector vecToProj = scanProjectile.sub(ownPos);
      addDebugArrow(ownPos, scanProjectile);

      if (Math.abs(vecToProj.getAngle().angularDistance(getOrientation()).getValueAsDegrees()) <= 180) {
        setAutopilot(vecToProj.getNegative().getAngle(), 1);
        evadeAngle = vecToProj.getNegative().getAngle();
        evadeInt = -1;
      } else {
        setAutopilot(vecToProj.getAngle(), -1);
        evadeAngle = vecToProj.getAngle();
        evadeInt = 1;
      }
      addDebugLine(new Vector(getOrientation(), 10).add(ownPos), ownPos);
      if (this.getEnergy() > 1.5D)
        setBoost();

      //if (scanMine != null) {
      //    Vector vecToProj2 = new Vector(ownPos.getX() - scanMine.getX(), ownPos.getY() - scanMine.getY());
      //    if (Math.abs(vecToProj2.getAngle().angularDistance(getOrientation()).getValueAsDegrees()) < 25) {
      //        if (evadeAngle != null)
      //            setAutopilot(evadeAngle.getNegative(), evadeInt);
      //    }
      //}
    }

    if (scanMine != null) {
      Vector ownPos = getPosition();
      Vector vecToProj = new Vector(ownPos.getX() - scanMine.getX(), ownPos.getY() - scanMine.getY());
      addDebugArrow(ownPos, scanMine);
      if (Math.abs(vecToProj.getAngle().angularDistance(getOrientation()).getValueAsDegrees()) <= 180) {
        setAutopilot(vecToProj.getAngle(), 1);
      } else {
        setAutopilot(vecToProj.getNegative().getAngle(), -1);
      }
    }
  }
}
