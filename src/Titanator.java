import java.awt.Color;
import java.util.Random;

import jrobots.utils.Angle;
import jrobots.utils.LinearPredictor;
import jrobots.utils.ProximityScan;
import jrobots.utils.SonarTrace;
import jrobots.utils.Vector;
import jrobots.simulation.simulationObjects.JRobot2015_3;


public class Titanator extends JRobot2015_3 {
	private static final long serialVersionUID = 1L;
	private Angle a = new Angle(0,"d");
	  private double distance = Double.MAX_VALUE;
	private int t = 0;
	  private SonarTrace oldScan = null;
	  private SonarTrace newSonar = null;
	
	
	
	public Titanator() {
		super();
	    this.setNameColor(Color.WHITE);
	    this.setBodyColor(Color.yellow);
	    this.setTurretColor(Color.RED);
	    this.drive();
	    
	    
	    
	}
	
	@Override
	protected void actions() {
		t++;
		
			
			this.farbe();
				this.drive();
				if(t % 20 == 0 ){
				Scan();
				if(this.getLastSonarTrace() != null){
				this.fire();
				
				}	
				
			}
			
				this.proScan();
				
			
			}
				
			
			
				
	public void drive(){
		
		if(t % 48 == 0){	
			a = a.add(new Angle(45, "d"));
			this.setAutopilot(a, 10);	
			}
	}
	
	
	

	public void farbe(){
		Random r = new Random();
		if(t % 48 == 0){
			if(r.nextInt(2)== 0){
				this.setTurretColor(Color.green);
			}
			else{
				this.setTurretColor(Color.RED);
			}
		}
		
	}
	
	public void proScan(){
		
			
		ProximityScan p = this.getProjectileRadar();
		if(p != null && p.speed != null){
			System.out.println("S"+t);
			Vector v = new Vector();
			for(int i = 1; i<4; i++){
			 v = p.predict(this.getTime()+0.03*i);
			 System.out.println(v.distanceTo(getPosition()));
			 if(v.distanceTo(getPosition())<30){
					 this.setAutopilot(a.add(new Angle(90,"d")), 1);
				 //}
				 
				 
			 }
			 break;
			}
		
		}
	}


public final double Raketenzeit(){
	
	return this.getPosition().distanceTo(GegnerScan()) / getProjectileSpeed();
	
}

public Vector GegnerScan(){
	
	return this.getLastSonarTrace().location;
	
}

public void Scan(){
	this.oldScan = this.newSonar;
	  //System.out.println(this.oldScan);
	  	this.setSonarEnergy(0.06);
    	this.newSonar = this.getLastSonarTrace();
    //Vector scanPos = this.getLastSonarTrace().location.sub(this.getPosition());
    	if(newSonar != null){
    			this.distance = this.newSonar.location.distanceTo(this.getPosition());
    	}
	
}

private void fire() {
	  if (this.newSonar != null) {
	      Vector scanPos = this.newSonar.location.sub(this.getPosition());
	      if(this.oldScan != null) {
	      scanPos = predictTarget(scanPos);
	      }
	      if (this.getEnergy() > 0.6 ){
	    	  this.setLaunchProjectileCommand(scanPos.getAngle());  
	      this.setLaunchProjectileCommand(scanPos.getAngle());
	  } else {
	    	  this.setLaunchProjectileCommand(scanPos.getAngle());
	      }
	  }
	  
}


private Vector predictTarget(Vector currentScan) {
	  double max = 1.2;
	  double min = .65;
	  double con = GetIdeal(min, max);
  double futureTime = this.getTime() + (currentScan.getLength() / super.getProjectileSpeed()) * con;
  Vector prediction = LinearPredictor.predict(newSonar, this.oldScan, futureTime).sub(this.getPosition());
  
  return prediction;
}

private double GetIdeal(double min, double max) {
	  double ideal;
	  
	  double v = this.distance / (max-min);
	  
	   ideal = (min +(v/100)*(max) -0.015) ;
	  	  
	  System.out.println(ideal);
	  if(v > 85 / (max-min)) {
		  ideal = max;
		  
	  } else if(v < 15 / (max-min)) {
		  ideal = min;
	  }	 
	  
	  if (ideal > max)
		  ideal = max;
	  
	  return ideal;
}


}
	
	
	


