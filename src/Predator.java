import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import jrobots.utils.*;
import jrobots.simulation.simulationObjects.JRobot2015_3;

/**
 * A minimal functional Bot, ready for being programmed.
 * <p>
 * Please enter the documentation via the method <tt>actions()</tt>.
 * <p>
 * Please rename uniquely. The individual name will be displayed in the GUI.
 * 
 * @see JRobot2015_2#actions()
 */
public class Predator extends JRobot2015_3 {
	private static final long serialVersionUID = 1L;
	SonarTrace newPos;
	SonarTrace oldPos;
	SonarTrace oldoOldPos;
	int frame = 0;
	int FRAMESPERSCAN = 12;
	int fps = JRobot2015_3.getFramesPerSecond();
	boolean enemyPredictsLiniar = false;
	boolean enemyPredictsPara = false;
	int i1 = 0;
	double i2 = 0;
	double speed = 0;
	double rot = 0;
	Angle a3;
	ProximityScan projectile1=null, projectile2=null;
	
	int r=0;

	private Vector refLoc;

	private Vector center;
	private static final int MAX_TRACES = 3;
	private LinkedList<SonarTrace> traces = new LinkedList<SonarTrace>();

	public Predator() {
		center = new Vector(5, 5);
	}

	@Override
	protected void actions() {
		int r = (int) (Math.random() * 5);
		
		if (r == 0) {
			this.setNameColor(Color.BLACK);
			this.setBodyColor(Color.BLACK);
			this.setTurretColor(Color.BLACK);

		} else if (r == 1) {
			this.setNameColor(Color.BLUE);
			this.setBodyColor(Color.BLUE);
			this.setTurretColor(Color.BLUE);

		}

		else if (r == 2) {
			this.setNameColor(Color.YELLOW);
			this.setBodyColor(Color.YELLOW);
			this.setTurretColor(Color.YELLOW);

		} else if (r == 3) {
			this.setNameColor(Color.RED);
			this.setBodyColor(Color.RED);
			this.setTurretColor(Color.RED);

		} else if (r == 4) {
			this.setNameColor(Color.GREEN);
			this.setBodyColor(Color.GREEN);
			this.setTurretColor(Color.GREEN);

		} else if (r == 5) {
			this.setNameColor(Color.PINK);
			this.setBodyColor(Color.PINK);
			this.setTurretColor(Color.PINK);

		}

		if (getHealth() != 1.0 && enemyPredictsLiniar == false) {
			enemyPredictsLiniar = true;
		}

		// if (frame == 0) {
		// newPos = getLastSonarTrace();
		// oldPos = newPos;
		// }
		//

		frame++;
		
			
		if (frame % 10 == 0 && this.getEnergy() > this.getEnergyConsumptionProjectile() * 3) {
			this.setSonarEnergy(0.2);	
		}
		
	
		speed = 0.5;
		fahrAlgorithmik();
		//projectileScan();

		SonarTrace t = getLastSonarTrace();
		if (t == null)
			return;

		if ((traces.size() > 0) && traces.getLast().timestamp == t.timestamp) {
			return;
		}
			
		traces.addLast(t);
		while (traces.size() > MAX_TRACES)
			traces.removeFirst();
	
		
		if (traces.getLast().timestamp == t.timestamp && this.getEnergy() > this.getEnergyConsumptionProjectile()*2) {
			fire();
		}
		
		
	}
	
	public void projectileScan(){
		projectile2 = projectile1;
		projectile1 = this.getProjectileRadar();
		
		double dxt=Math.abs(getLastSonarTrace().location.getX()-getPosition().getX());
		double dyt=Math.abs(getLastSonarTrace().location.getY()-getPosition().getY());
		double dt=Math.pow(dyt*dyt+dxt*dxt, (1/2));
		
		double dxp=Math.abs(projectile1.pos.getX()-getPosition().getX());
		double dyp=Math.abs(projectile1.pos.getY()-getPosition().getY());
		double dp=Math.pow(dyp*dyp+dxp*dxp, (1/2));

		Angle alphaT = new Angle(Math.atan(dxt/dyt)*180/Math.PI+90,"d"); 
//		Angle alphaP = new Angle(Math.atan(dxp/dyp)*180/Math.PI,"d"); 
//		Angle alphaPmax = new Angle(Math.atan(dxt/dyt)*180/Math.PI*1.1,"d"); 
//		Angle alphaPmin = new Angle(Math.atan(dxt/dyt)*180/Math.PI*0.9,"d");

		double alphaTD = Math.atan(dxt/dyt)*180/Math.PI; 
		double alphaPD = Math.atan(dxp/dyp)*180/Math.PI; 
		
		if (projectile2!=null){
			if(projectile2==projectile1){
				int rand=(int)Math.random();
				if (rand==1){
					r=90;
				}
				else {
					r=-90;
				}
				Angle alphaP = new Angle(Math.atan(dxp/dyp)*180/Math.PI+r,"d"); 
				this.setAutopilot(alphaP, speed);
			}
		
			else{	
				if(alphaTD<=alphaPD*0.9 || alphaTD>=alphaPD*1.1){
					this.setAutopilot(alphaT, 0);
					this.setBoost();
				}
				else{
					speed=0;
					
					}
		}
		}
	}
	
	
	
	
	

	public Vector predict(Vector currentScan) {
		/**
		 * oldPos = getLastSonarTrace(); if((frame+a) % a==0) {if
		 * (enemyPredictsLiniar==true) { oldPos = getLastSonarTrace(); }
		 **/

		newPos = getLastSonarTrace();

		double futureTime = getTime()
				+ (currentScan.getLength() / JRobot2015_3.getProjectileSpeed());
		if (newPos != null && oldPos != null) {
			return LinearPredictor.predict(oldPos, newPos, futureTime);
		}
		oldPos = newPos;
		return null;
	}

	

	public void fire() {
		int sz = traces.size();
		if (sz == 3) {
			predictorIndicators(traces.get(sz-3),traces.get(sz-2),traces.get(sz-1));
		} else {
			return;
		}
		 double distance = getLastSonarTrace().location.getLength();
		 double projectileSpeed = getProjectileSpeed();
		 double time = distance/projectileSpeed;
		 Vector predictedPos = predictPosition(getTime() + time);
				 
		 if (distance != 0) {
			 setLaunchProjectileCommand(predictedPos.sub(getPosition()).getAngle());
		 }		 

	}

	public void schneckenKreis() {

		Vector toCenter = center.sub(getPosition());
		Vector direction = toCenter.rotate(new Angle(90.005, "d"));

		setAutopilot(direction.getAngle(), speed);

	}

	public void geradeFahren() {
		
		
		i2++;
		if (i2 == 180)
			i2 = 0;
		// i2 = Math.sin(frame/30);
		Angle a1 = new Angle(i2, "degree");
		if (frame % 100 == 1) {
			a1 = getLastSonarTrace().location.getAngle();
		}
		setAutopilot(a1, speed);

	}

	public void stoppen() {
		setAutopilot(getOrientation(), 0.0);
	}


	public void minenRadar() {

	}

	public void fahrAlgorithmik() {
		double dxt=Math.abs(getLastSonarTrace().location.getX()-getPosition().getX());
		double dyt=Math.abs(getLastSonarTrace().location.getY()-getPosition().getY());
		double dt=Math.pow(dyt*dyt+dxt*dxt, (1/2));
		
		
		if (enemyPredictsLiniar == false && enemyPredictsPara == false) {
			geradeFahren();
		}
		if (enemyPredictsLiniar) {
			a3 = getOrientation();
			schneckenKreis();
		}
	}

	public static Vector swap(Vector v1) {
		return new Vector(v1.getY(), v1.getX());
	}

	double x0 = 0;
	double x1 = -2;
	double x2 = 1;
	double y0 = 0;
	double y1 = 1;
	double y2 = 2;
	
	Vector v1 = new Vector(-2, 1);
	Vector v2 = new Vector(0, 2);
	Vector v3 = new Vector(0, 3);
	
	
	public Vector predictCoordinates(double x, Vector v1, Vector v2, Vector v3) {
		x0 = v1.getX();
		x1 = v2.getX();
		x2 = v3.getX();
		y0 = v1.getY();
		y1 = v2.getY();
		y2 = v3.getY();		
		
		double y = y0 * ((x - x1) / (x0 - x1) * (x - x2) / (x0 - x2)) + y1
				* ((x - x0) / (x1 - x0) * (x - x2) / (x1 - x2)) + y2
				* ((x - x0) / (x2 - x0) * (x - x1) / (x2 - x1));
		//System.out.print("y=" + y);

//		double a = ((x0 * y0 - x1 * y1 - x2 * (y0 - y1)) / (x0 - x1) + (y2 * (x1 - x2))
//				/ (x0 - x2));
//		double b = (-(x0 * (x1 * (y0 - y1) + x2 * (y0 + y1)) - x2
//				* (x1 * (y0 + y1) + x2 * (y0 - y1)))
//				/ (x0 - x1) - (x1 * y2 * (x1 - x2)) / (x0 - x2));
//		double c = (x2 * (x0 * (x1 * (y0 - y1) + x2 * y1) - x1 * x2 * y0))
//				/ (x0 - x1);

		return new Vector(x, y);
	}

	public Vector predictPosition(double futureTime) {
		SonarTrace s1, s2, s3;
		
		int sz = traces.size();
		if (sz >= 3) {
			s1 = traces.get(sz-3);
			s2 = traces.get(sz-2);
			s3 = traces.get(sz-1);
		} else
			return null;
		
		
		double t1, t2, t3;

		v1 = s1.location;
		t1 = s1.timestamp;
		
		v2 = s2.location;
		t2 = s2.timestamp;
		
		v3 = s3.location;
		t3 = s3.timestamp;
		
		double x = getTime()-50;
	
		for (int i = 400; i >=0; i--)
		{
			x = x + 0.25;
			Vector tx1 = new Vector(t1, v1.getX());
			Vector tx2 = new Vector(t2, v2.getX());
			Vector tx3 = new Vector(t3, v3.getX());
			Vector vx = predictCoordinates(x, tx1, tx2, tx3);
			
			Vector ty1 = new Vector(t1, v1.getY());
			Vector ty2 = new Vector(t2, v2.getY());
			Vector ty3 = new Vector(t3, v3.getY());
			Vector vy = predictCoordinates(x, ty1, ty2, ty3);
			
			addDebugCrosshair(new Vector(vx.getY(), vy.getY()));
			
			
		}		
		
		Vector tx1 = new Vector(t1, v1.getX());
		Vector tx2 = new Vector(t2, v2.getX());
		Vector tx3 = new Vector(t3, v3.getX());
		Vector vx = predictCoordinates(futureTime, tx1, tx2, tx3);
		
		Vector ty1 = new Vector(t1, v1.getY());
		Vector ty2 = new Vector(t2, v2.getY());
		Vector ty3 = new Vector(t3, v3.getY());
		Vector vy = predictCoordinates(futureTime, ty1, ty2, ty3);
		
		addDebugArrow(getPosition(), v1);
		addDebugArrow(getPosition(), v2);
		addDebugArrow(getPosition(), v3);
		addDebugArrow(getPosition(), new Vector(vx.getY(), vy.getY()));
			
		return new Vector(vx.getY(), vy.getY());
	}

	public void predictorIndicators(SonarTrace s1, SonarTrace s2, SonarTrace s3) {
		v1 = s1.location;
		v2 = s2.location;
		v3 = s3.location;
			
		x0 = v1.getX();
		x1 = v2.getX();
		x2 = v3.getX();
		y0 = v1.getY();
		y1 = v2.getY();
		y2 = v3.getY();
		
		System.out.print("x0=" + x0);
		System.out.print(" x1=" + x1);
		System.out.print(" x2=" + x2);
		System.out.print(" y0=" + y0);
		System.out.print("y 1=" + y1);
		System.out.println(" y2=" + y2);
	}
}
