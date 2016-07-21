import java.awt.Color;
import java.util.LinkedList;

import jrobots.simulation.simulationObjects.JRobot2015_2;
import jrobots.utils.Angle;
import jrobots.utils.LinearPredictor;
import jrobots.utils.ProximityScan;
import jrobots.utils.SonarTrace;
import jrobots.utils.Vector;


public class Klasse extends JRobot2015_2 {
	
	int i = 0;
	Vector enemPos1;
	Vector enemPos2;
	double time;
	int fps = getFramesPerSecond();
	static final int FRAMES_PER_SCAN = 18;
	Vector loc;
	SonarTrace oldTrace;
	SonarTrace newTrace;
	ProximityScan proj;
	
	public Klasse() {
		time = getTime();
		oldTrace = new SonarTrace(0, new Vector(0, 0), 0);
	    newTrace = new SonarTrace(0, new Vector(0, 0), 0);
	    this.setNameColor(Color.BLACK);
	    this.setBodyColor(Color.BLUE);
	    this.setTurretColor(Color.BLUE);
	}
	
	@Override
	protected void actions() {
		time = getTime();
		this.scan();
		evalScan();
	   	this.fire();
		this.drive();
		i++;
	}
	
	public void evalScan() {
		SonarTrace trace = getLastSonarTrace();
		if (trace == null)
			return;
		
		if (newTrace == null) {
			newTrace = trace;
			return;
		}

		if (newTrace.timestamp == trace.timestamp)
			return;
		
		oldTrace = newTrace;
		newTrace = trace;
	}
	
	public void scan(){
		if (i % FRAMES_PER_SCAN == 0) {
			double regeneration = Klasse.getEnergyProductionPerFrame();
			double energy = regeneration * 12;
			setSonarEnergy(energy);	
		}
	}
	
	public void ausweichen(){
		Angle projdir = proj.pos.getAngle();
	    this.addDebugArrow(proj.pos, proj.predict(2));
	    Vector Proj = new Vector(projdir, 2);
	}
	
	public void fire(){
		if (i % FRAMES_PER_SCAN == 0 && i > 18) {
			double projSpeed = getGrenadeSpeed();
			double ds = newTrace.location.distanceTo(getPosition());
			double timeA = (ds/projSpeed);
			
			Vector Predict = LinearPredictor.predict(oldTrace, newTrace, time+timeA);
			loc = Predict.sub(getPosition());
			
			if(newTrace.location.distanceTo(oldTrace.location) < 25){
				setLaunchProjectileCommand(loc.getAngle(), loc.getLength()+8);
			}
		}
	}
	
	public void drive(){
	    double speed1 = 1;
	    double speed2 = 2;
	    double speed3 = -1;
	    double speed4 = -2;
	    
	    if(i > 25){
	    	Angle enemdir = (loc != null) ? loc.getAngle() : new Angle(Math.random() * 360, "d");
		    Vector mine = this.getMineDetectorScan();
		    boolean mineDetected = mine != null;
		    
			if (mineDetected) {
				Angle minedir = mine.getAngle().sub(getOrientation()).normalize();
				this.addDebugArrow(getPosition(), mine);
				System.out.println("Mine angle: " + mine.getAngle().sub(getOrientation()).getValueAsDegrees() + " length" + mine.getLength());
				
	  			if (minedir.getValueAsDegrees() >= 0
	  					&& minedir.getValueAsDegrees() < 20 
	  					&& mine.getLength() < 40) {
					this.setAutopilot(mine.getAngle().add(new Angle(27, "d")), 1);
					System.out.println("links");
				} else if (minedir.getValueAsDegrees() < 360
						&& minedir.getValueAsDegrees() > 340 
						&& mine.getLength() < 40) {
					this.setAutopilot(mine.getAngle().sub(new Angle(27, "d")), 1);
					System.out.println("rechts");
				} else {
					System.out.println("no match");
				}
			}
		    else{
			    double entf = (loc != null) ?  loc.getLength() : 50; 
			    if (entf >= 25){
			    	if(i % 150 < 75){
			    		this.setAutopilot(enemdir.add(new Angle(38, "d")), 1.5);
			    		this.addDebugArrow(getPosition(), new Vector(getOrientation(), 2));
			    	}
			    	else {
			    		this.setAutopilot(enemdir.sub(new Angle(38, "d")), 1.5);
			    		this.addDebugArrow(getPosition(), new Vector(getOrientation(), 2));
			    	}
			    }
			    else if(entf < 25){
			    	double rand = Math.random()*4;
			    	int Rand = (int)rand;
			    	if (i % 100 == 50){
			    		switch (Rand){
			    			case 1:
			    				this.setAutopilot(enemdir.add(new Angle(90.0, "d")), speed1);
			    				this.addDebugArrow(getPosition(), new Vector(getOrientation(), 3));
			    				break;
				    		
			    			case 2:
			    				this.setAutopilot(enemdir.add(new Angle(90.0, "d")), speed2);
			    				this.addDebugArrow(getPosition(), new Vector(getOrientation(), 3));
			    				break;
				   		
			    			case 3:
			    				this.setAutopilot(enemdir.add(new Angle(90.0, "d")), speed3);
			    				this.addDebugArrow(getPosition(), new Vector(getOrientation(), 3));
			    				break;
				    			
			    			case 4:
			    				this.setAutopilot(enemdir.add(new Angle(90.0, "d")), speed4);
			    				this.addDebugArrow(getPosition(), new Vector(getOrientation(), 3));
			    				break;
			       		}
			    	}
			    }
		    }
	    }	    
	}
}
