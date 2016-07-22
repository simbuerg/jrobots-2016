import java.awt.Color;

import jrobots.simulation.simulationObjects.JRobot2015_3;
import jrobots.utils.Angle;
import jrobots.utils.LinearPredictor;
import jrobots.utils.ProximityScan;
import jrobots.utils.SonarTrace;
import jrobots.utils.Vector;


public class Terminator extends JRobot2015_3 {

	@Override
	protected void actions() {
		
		this.shoot();
		this.Scan();
		this.drive();
		
		// TODO Auto-generated method stub
	}
	
	private double SCANENERGY = 0.1;
	public double Zahl = 0;
	private SonarTrace lastscan = null;
	private double speed = 1;
	
	public Terminator() {
	this.setTurretColor(Color.magenta);
	this.setBodyColor(Color.magenta);
	this.setNameColor(Color.magenta);
	
	}
	
	protected void drive () {	
		
		ProximityScan projectile = this.getProjectileRadar();
	    if (projectile != null) {
	    this.addDebugArrow(this.getPosition(), projectile.pos);

		if(this.getLastSonarTrace()!= null ){
		this.setAutopilot(this.getLastSonarTrace().location.sub(this.getPosition()).getAngle(), speed);
		}
		

	    //if (projectile){
	    	
	    //}
	    	
	    }
		
		}
		
	public void Scan() {
		Zahl++;
		if (Zahl % 20 == 0) {
			this.setSonarEnergy(SCANENERGY);
		}
		lastscan=this.getLastSonarTrace();
	}
	
	private Vector predictTarget() {
		double futureTime = this.getTime()
				+ (this.getLastSonarTrace().location.getLength() / getProjectileSpeed());
		Vector prediction = LinearPredictor.predict(this.getLastSonarTrace(),
				this.lastscan, futureTime).sub(this.getPosition());

		return prediction;
	}
	
	protected void shoot() {	
		if (this.lastscan != null && this.lastscan.timestamp != this.getLastSonarTrace().timestamp && this.getEnergy() >=0.4 ) {
			Vector scanPos = this.getLastSonarTrace().location.sub(this.getPosition());
			
			Vector targetPos = predictTarget();
			
			//Vector Prediction = this.getLastSonarTrace().location.sub(lastscan.location);
			//Vector Future_Position = Prediction.add(Prediction);		
			
			
			
			//this.setLaunchProjectileCommand(this.getLastSonarTrace().location.sub(this.getPosition()).getAngle() );
			
			//System.out.println(targetPos);
			
			this.setLaunchProjectileCommand(targetPos.getAngle());
			
			
			//this.setLaunchProjectileCommand(scanPos.getAngle() );
		}
	}
}	