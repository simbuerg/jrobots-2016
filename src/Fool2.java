import jrobots.utils.*;

import java.lang.Math;
import java.util.Random;

import jrobots.simulation.simulationObjects.JRobot2015_3;






import java.awt.*;

public class Fool2 extends JRobot2015_3 {
  private static final long serialVersionUID = 1L;

  private SonarTrace oldScan = null;
  private SonarTrace newSonar = null;
  private double multiply = 1;
  private double lastAngle = 0.0;
  private double distance = Double.MAX_VALUE;
  private double distanceY = 0;
  private double distanceX = 0;
  private int counter = 0;
  private double frame = 0;
  private ProximityScan lastScan = null;
  private ProximityScan newScan = null;
  
  private int counterps = 0; 
  private float r; 
  private float g; 
  private float b ; 
  Random rand = new Random();
  private int boostcounter; 

  public Fool2() {
    this.setNameColor(Color.CYAN);
    this.setBodyColor(Color.MAGENTA);
    this.setTurretColor(Color.PINK);
    newScan = this.getProjectileRadar();
    this.boostcounter = 15; 
    counter = 12; 
    this.setAutopilot(new Angle ( 45 , "d"), 3);
  }

  @Override
  protected void actions() {
	  if(this.counter == 12 && this.getEnergy() < 0.3)
	  {
		  this.Scan(); 
		  counter = 0;
	  }
	  
	  
	  this.drive () ; 
	  
	  if (counterps == 1){
		  this.dodeg4(newScan);
		  this.counterps = 0; 
	  }
	  if (this.getEnergy () > 0.3){
	  this.fire();
	  }
	  
	  MisileScan();
    counter++;
    boostcounter++; 
    float r = rand.nextFloat();
    float g = rand.nextFloat();
    float b = rand.nextFloat();
    Color randomColor = new Color(r, g, b);
    Color randomColor2 = new Color(b, g, r);
    Color randomColor3 = new Color(b, r, g);
    this.setTurretColor(randomColor);
    this.setBodyColor(randomColor2);
    this.setNameColor(randomColor3);
  }
  
  /**
   * Modelierung des Schussverhaltens.
   **/
  private void fire() {
	  if (this.newSonar != null) {
	      Vector scanPos = this.Scan().location.sub(this.getPosition());
	      if (this.getEnergy() > 0.6  && this.getHealth() > 0.75){
	    	  this.setLaunchProjectileCommand(scanPos.getAngle());  
	    	  this.setLaunchProjectileCommand(scanPos.getAngle());
	      }
	    else {
	    		  this.setLaunchProjectileCommand(scanPos.getAngle());
	    	  }
	  }
	  }
  

  private Vector predictTarget(Vector currentScan) {
	  double max = 1.5;
	  double min = .25;
	  double con = GetIdeal(min, max);
    double futureTime = this.getTime() + (currentScan.getLength() / super.getProjectileSpeed()) * con;
    Vector prediction = LinearPredictor.predict(newSonar, this.oldScan, futureTime).sub(this.getPosition());
    
    return prediction;
  }

  private double GetIdeal(double min, double max) {
	  double ideal;
	  double v = this.distance / (max-min);
	  ideal = (min +(v/100)*max) -0.01;
	  //System.out.println(distance);
	  if(v > 85 / (max-min)) {
		  ideal = max;
	  } else if(v < 20 / (max-min)) {
		  ideal = min;
	  }	  
	  if(v < 10 / (max-min))
		  ideal = .2;
	  if(v < 5 / (max-min)) {
		  ideal = .175;
	  }
	  return ideal;
  }

  /**
   * Zeichnen von Debug Pfeilen und Linien.
   **/
  //private void drawDebugTarget() {
	  //Vector scanPos = this.getLastSonarTrace().location.sub(this.getPosition());
    
   // if (scanPos != null && oldScan != null) {
   //  this.addDebugArrow(new Vector(this.getPosition().getX() + 50, this.getPosition().getY()), this.getPosition());
    	//this.addDebugArrow(this.getPosition(), new Vector(this.getPosition().getX() + 50, this.getPosition().getY()));
     //this.addDebugLine(this.getPosition(), scanPos);
  // }
  //}
  
  private void dodgeMisiles(ProximityScan ps) {
	  if(this.getTime() != frame && ps != null) {
		  this.newScan = this.getProjectileRadar(); 
		  if(newScan != null) {
	      double tan = (newScan.pos.getY()-lastScan.pos.getY()) /(newScan.pos.getX()-lastScan.pos.getX()); 
	      tan = tan*(-1); 
	      tan = Math.atan(tan);
	      
	      if(this.distanceY < 0) {
	      		this.lastAngle = tan -90; 
				  } else {
				  this.lastAngle = tan +90;   
				  }
				  
	      tan = Math.toDegrees(tan);
	    	  this.setAutopilot(new Angle(tan, "d"), 10.0);
		  }	
		  if (this.newScan.pos.distanceTo(this.getPosition())< 17) {
				this.setBoost();
			}	
	      }
 
  }
  
private void dodeg4 (ProximityScan ps) { 
	if(this.getTime() != frame ) {
		 this.newScan = this.getProjectileRadar(); 
		  if(newScan != null) {
			  double tan = 0; 
			  double wink = 0; 
			  if (this.lastScan.pos.getY()< this.newScan.pos.getY()){
				  tan = ((this.newScan.pos.getX() - this.lastScan.pos.getX()))  / ((this.newScan.pos.getY() - this.lastScan.pos.getY())); 
			  }
			  else if (this.lastScan.pos.getY()> this.newScan.pos.getY()){
				   tan = ((this.newScan.pos.getY() - this.lastScan.pos.getY()) ) / ((this.newScan.pos.getX() - this.lastScan.pos.getX())); 
			  }
			  if (this.getPosition().getY()> this.newScan.pos.getY()){
				  wink = ((this.newScan.pos.getX() - this.getPosition().getX()))  / ((this.newScan.pos.getY() - this.getPosition().getY())); 
			  }
			  else if (this.getPosition().getY()< this.newScan.pos.getY()){
				   wink = ((this.newScan.pos.getY() - this.getPosition().getY()) ) / ((this.newScan.pos.getX() - this.getPosition().getX())); 
			  }
			  Angle d = new Angle (0, "d"); 
		  if (tan != 0 && wink != 0 ){
			  Angle tan1 = new Angle (Math.toDegrees(tan), "d"); 
			  Angle wink1 = new Angle (Math.toDegrees(wink), "d"); 
			  if (tan1.angle > wink1.angle ){
				  d = new Angle (tan1.angle -90, "d" ); 
			  }
			  else if (tan1.angle < wink1.angle ){
				  d = new Angle (tan1.angle +90, "d" ); 
			  }
			  else if (tan1.angle ==  wink1.angle ){
				  d = new Angle (tan1.angle -75, "d" ); 
				  //this.setBoost() ;
				  //this.boostcounter = 0; 
					System.out.println ("direct hit");
			  }
		  }
		  
		  
	
		  
		  //System.out.println (d.toString()); 
			  this.setAutopilot(d, 5.00);
				if (this.newScan.pos.distanceTo(this.getPosition())< 15 && this.boostcounter > 20) {
					this.setBoost();
					this.boostcounter = 0;  
				}	
			  }				  
			    
		  }
}


	

  
private SonarTrace  Scan() {
	  if(this.getEnergy()> 0.13) {
		  this.oldScan = this.newSonar;
		  //System.out.println(this.oldScan);
		  	this.setSonarEnergy(0.07);
	      	this.newSonar = this.getLastSonarTrace();
	      //Vector scanPos = this.getLastSonarTrace().location.sub(this.getPosition());
	      	if(newSonar != null){
	      			this.distance = this.newSonar.location.distanceTo(this.getPosition());
	      			
	      			this.distanceY = this.newSonar.location.getY() - this.getPosition().getY();
	      			//System.out.println(this.distanceY);
	      			this.distanceX = this.newSonar.location.getX() - this.getPosition().getX();
	      			//if(this.getEnergy()> 0.2)
	      			//System.out.println(distance);
	      }
	      			//drawDebugTarget();
	      counter = 0;
	  }
	  return this.newSonar ; 
}
  private void MisileScan() {
	  lastScan = this.getProjectileRadar();
	  frame = this.getTime();
	  if(lastScan != null) {	  
	  counterps = 1;  
	  }
  }
  /**
   * Modelierung des Fahrverhaltens.
   **/

	  public void drive () {
		if (this.newSonar != null) {
			
			if (this.newSonar.location.distanceTo(this.getPosition()) < 40){
				this.setAutopilot(this.newSonar.location.sub(this.getPosition()).getAngle().add(new Angle (180, "d")) , 4);
			}
			else if (this.newSonar.location.distanceTo(this.getPosition())>  80){
				this.setAutopilot(this.newSonar.location.sub(this.getPosition()).getAngle(), 4);
			}
		  }
	  }
}
