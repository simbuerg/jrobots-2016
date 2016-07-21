import java.awt.Color;
import jrobots.simulation.simulationObjects.JRobot2015_2;
import jrobots.utils.*;
import java.math.*;

public class Test2 extends JRobot2015_2{
	private SonarTrace oldScan = null;
	private int frame;
	private int ausgewichen;
	private Vector aktVorhersage;
	private Vector aktPostition;
	private Angle aktRichtung;
	private Angle ausgRichtung;
	private double energie;
	private Vector oldLocation;
	private int RAPIDFIRE = 50;
	private int FRAMESPERSHOT = 100;
	
	public Test2(){
		frame = 0;
		this.setNameColor(Color.YELLOW);
		this.setBodyColor(Color.CYAN);
		this.setTurretColor(Color.MAGENTA);
	}
	
	protected void actions() {
		energie = this.getEnergy();
		frame ++;
		if (frame%FRAMESPERSHOT== 0 && energie > 0.3){
		
			schiessen();
			
		}
		if(energie>1.2&&(frame%RAPIDFIRE==RAPIDFIRE-5 ||frame% RAPIDFIRE== RAPIDFIRE -10)){
			schiessen();
		}
		
		if (frame%FRAMESPERSHOT== FRAMESPERSHOT -20 || frame % FRAMESPERSHOT == FRAMESPERSHOT -40 && energie > 0.2){
			
			scan();
			oldScan = this.getLastSonarTrace();
			
		}
		
		oldLocation = this.getPosition();
		fahren();
		
		drawDebug();
	}
	
	private void fahren(){
		if (!gefahr()){
			
			if(ausgewichen!=0&&frame-ausgewichen<50){
				this.setAutopilot(ausgRichtung,2); 
			
			}else if(aktVorhersage != null){
				Angle richtung = (this.getPosition().sub(oldScan.location)).getAngle();
				double speed = 0.8;
				this.setAutopilot(richtung, speed);
				aktRichtung = richtung;
			
			}else{
				this.setAutopilot(UP, 0.8);
				aktRichtung= UP;
			}
			
		}else{
			ausgewichen=frame;
			Vector projektil = this.getProjectileRadar().speed;
			Angle links = projektil.rotate(new Angle(270.0,"d")).getAngle();
			Angle rechts =projektil.rotate(new Angle(90.0,"d")).getAngle();
			
			if(Math.abs((links.sub(aktRichtung)).getValueAsDegrees())<= Math.abs(rechts.sub(aktRichtung).getValueAsDegrees())){
				this.setAutopilot(links,1 );
				ausgRichtung=links;
				if(energie>=0.6){
					this.setBoost();
				}
			
			}else{
				this.setAutopilot(rechts, 1);
				ausgRichtung=rechts;
				
				if(energie>=0.6){
					this.setBoost();	
				}
				
			} 
			
		}   
	}
	
	private boolean gefahr(){
		if (this.getProjectileRadar()==null){
			return false;
		}
	
		if ((this.getPosition().sub(this.getProjectileRadar().pos)).getLength()<4*this.getJRobotLength()){
			
			return true;
		}else{
			return false;
		}
	}
	
	private void schiessen(){
		
		if (this.getLastSonarTrace() != null && this.oldScan != null){
			Vector vorhersage = predictTarget();
			aktVorhersage = vorhersage;
			aktPostition = this.getPosition();
			
			if (oldLocation != null && oldLocation.sub(this.getPosition()).getLength()<0.01){
				//this.setLaunchProjectileCommand(vorhersage.getAngle(),this.getMaxArenaDiameter());
				this.setLaunchProjectileCommand(vorhersage.getAngle(),vorhersage.getLength()); //* 0.85);
			}else{
				this.setLaunchProjectileCommand(vorhersage.getAngle(),vorhersage.getLength()); //* 0.85);
			}

		}
	}
	
	public void drawDebug() {
		if (aktVorhersage != null) {
			this.addDebugArrow(aktPostition, aktVorhersage.add(aktPostition));
			this.addDebugCrosshair(aktVorhersage.add(aktPostition));
		}
		if (this.getLastSonarTrace() != null) {
			this.addDebugCrosshair(this.getLastSonarTrace().location);
		}
		if (this.oldScan != null) {
			this.addDebugCrosshair(this.oldScan.location);
		}

	}
	
	public void scan(){
		this.setSonarEnergy(0.1);
	}
	
	private Vector predictTarget() {
		//double futureTime = this.getTime() + (this.getLastSonarTrace().location.getLength() / getGrenadeSpeed());
		double futureTime = this.getTime() + (this.getPosition().distanceTo(this.getLastSonarTrace().location) / getGrenadeSpeed());
		
		System.out.println("TIME: " + futureTime);
		Vector prediction = LinearPredictor.predict(this.getLastSonarTrace(), this.oldScan, futureTime).sub(this.getPosition());
		
		return prediction;
	}
	
}



