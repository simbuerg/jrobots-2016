import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import jrobots.simulation.simulationObjects.JRobot2015_3;
import jrobots.utils.Angle;
import jrobots.utils.ProximityScan;
import jrobots.utils.SonarTrace;
import jrobots.utils.Vector;

public class Error404 extends JRobot2015_3 {
  private static final long serialVersionUID = 1L;
  
  private SonarTrace[] st = new SonarTrace[3];
  
  private int inter = 0;
  private int scanAbstand = 20;
  
  private ProximityScan oldPS = null;
  private ProximityScan oldPS2 = oldPS;
  
  private int schiessenAbAbstand = 55;
  private double ImmerSchiessenAbEnergie = 1.2;
  
  private double abstand = 70;
  private double minimalabstand = 15;
  //private int newDrive = 32;
  
  private int haufigkeitRakete = 70; //100 ca. Energie 1;
  private boolean fire = true;
  //private int RaketenAusweichenWinkel = 45;
  private int BoosterAbstandRakete = 30;
  private double BoosterAbEnergieBenutzen = 0.40;
  
  private double lastHealth = 100;
  
  private double time = 0;
  
  private Vector lastTarget = null;
  
  //DO NOT CHANGE
  private boolean areWeFire = false;
  
  public Error404() {    
    c.add(Color.black);
    c.add(Color.BLUE);
    c.add(Color.cyan);
    c.add(Color.darkGray);
    c.add(Color.green);
    c.add(Color.orange);
    c.add(Color.magenta);
    c.add(Color.gray);
    c.add(Color.orange);
    c.add(Color.pink);
    c.add(Color.red);
    c.add(Color.white);
    c.add(Color.lightGray);
    c.add(Color.yellow);
  }
  public ArrayList<Color> c = new ArrayList<Color>();

  @Override
  protected void actions() {
	this.drive();
	  
	this.mustfire();
	
	this.scanning();
    this.fire();    
    
    LebenRetten();
    
    drawDebug();
    
    areWeFire = false;
  }
  
  private void LebenRetten(){
	  if(this.getHealth() <= lastHealth - 10){
		  BoosterAbstandRakete += 1;
		  BoosterAbEnergieBenutzen -= 0.02;
		  
		  lastHealth = this.getHealth();
	  }
  }
  
  private void scanning(){
	  inter++;
	  if (inter % scanAbstand == 0 && this.getEnergy() >= 0.3D * getEnergyConsumptionProjectile()) {
	      this.setSonarEnergy(0.3D * getEnergyConsumptionProjectile());
	  }
	  
	  if(oldPS != null){
		  oldPS2 = oldPS;
	  }
	   
	  this.oldPS = this.getProjectileRadar();
	  
	  if (st[0] != this.getLastSonarTrace()) {
		  st[2] = st[1];
		  st[1] = st[0];
		  st[0] = this.getLastSonarTrace();
	  }

  }
  
  private int fireaktuell = 0;
  @SuppressWarnings("static-access")
  private void fire() {
	  if (st[0] != null && st[1] != null && this.getLastSonarTrace() != null) {
		  double distance = st[0].location.distanceTo(this.getPosition());
		  
		  if((haufigkeitRakete <= fireaktuell || distance <= minimalabstand) && fire == true){
		      if(st[0] != null && st[1] != null && st[2] != null && st[0].location.distanceTo(this.getPosition()) <= schiessenAbAbstand || this.getEnergy() >= ImmerSchiessenAbEnergie){
		    	  double x1 = st[0].location.getX();
		    	  double x2 = st[1].location.getX();
		    	  double x3 = st[2].location.getX();
		    	  //System.out.println("x1= " + x1 + " x2= " + x2 +" x3= " + x3);
		    	  
		    	  double y1 = st[0].location.getY();
		    	  double y2 = st[1].location.getY();
		    	  double y3 = st[2].location.getY();
		    	  //System.out.println("y1= " + y1 + " y2= " + y2 +" y3= " + y3);
		    	  
		    	  double a = (double) (x1 * (y2 - y3) + x2 * (y3-y1) + x3 * (y1-y2))
		    			  				/ 
		    			  				((x1-x2) * (x1-x3) * (x3-x2)); 	
		    	  double b = (double) ((x1 * x1) * (y2-y3) + (x2 * x2) * (y3-y1) + (x3 * x3) *(y1-y2))/((x1-x2) * (x1-x3) * (x2-x3));
		    	  double c = (double) ((x1 * x1) * (x2 * y3-x3 * y2) + x1 * ((x3 * x3) * y2- (x2 * x2) * y3)+x2 * x3 * y1 * (x2-x3))/((x1-x2) * (x1-x3) *(x2-x3));
		    	  		    	  
		    	  double d = Math.abs(st[0].location.getX() - st[1].location.getX());
		    	  double dTime = Math.abs(st[0].timestamp - st[1].timestamp);
		    	   
		    	  boolean rechts = true;
		    	  if(st[1].location.getX() > st[0].location.getX()){
		    		  rechts = false;
		    	  }else{
		    		  rechts = true;
		    	  }
		    	  
		    	  double time = st[0].location.distanceTo(this.getPosition()) / this.getProjectileSpeed();
		    	  
		    	  double x = 0;
		    	  if(rechts == true){
		    		  x = (double) st[0].location.getX() + dTime * (time / dTime) * d;
		    	  }else{
		    		  x = (double) st[0].location.getX() - dTime * (time / dTime) * d;
		    	  }
		    	  
		    	  double y = (double) a * (x * x) + b * x + c;
		    	  
		    	  //System.out.println("y " + y + " x= " + x +" d= " + d);
		    	  
		    	  Vector v = new Vector(x, y);
		    	  
		    	  Vector scanPos = v.sub(this.getPosition());
		    	  
		    	  this.lastTarget = v;
		    	  
		    	  this.setLaunchProjectileCommand(scanPos.getAngle());
		    	  
		    	  areWeFire = true;
		      }else if(st[0].location.distanceTo(st[1].location) <= 10){
		    	  
			      Vector scanPos = st[0].location.sub(this.getPosition());
		    	
		    	  this.setLaunchProjectileCommand(scanPos.getAngle());
		    	  
		    	  this.lastTarget = null;
		    	  
		    	  areWeFire = true;
		      }
		      
		      fireaktuell = 0;
		  }else{
			  fireaktuell++;
		  }
	  }else{
		  fireaktuell++;
	  }
  }
  private void mustfire(){
	  if((oldPS == null || oldPS.timeOfScan <= this.getTime() - 20) && this.getEnergy() <= 1.5){
		  fire = false;
	  }else{
		  fire = true;
	  }
  }
  
  private void drive() {
	  this.Ausweichen();
  }
  
  private void DriveElse(){
	  Random r = new Random();
	  double d = r.nextInt(360);
	  
	  drive(new Angle(d, "d"));
  }
  
  private void drawDebug() {
	  if (st[0] != null && st[1] != null &&st[2] != null) {
		  this.addDebugArrow(new Vector(st[2].location.getX(),  st[2].location.getY()),
		  			 new Vector(st[1].location.getX(), st[1].location.getY()));
		  this.addDebugArrow(new Vector(st[1].location.getX(),  st[1].location.getY()),
		 			 new Vector(st[0].location.getX(), st[0].location.getY()));
	  }
	  
	  if (st[0] != null && this.lastTarget != null) {
		  this.addDebugArrow(new Vector(st[0].location.getX(),  st[0].location.getY()),
		 			 		 this.lastTarget);
	  }
  }
  
  private void Ausweichen(){
	  if(st[0] != null || (oldPS != null && oldPS2 != null)){		  
		if(aufDerLinie()){
			driveCircle(oldPS.pos, 1);
		}else if(mine()){
			driveCircle(oldPS.pos);
		}else if(st[0] != null){
				driveCircle(st[0].location);
		}else{
			DriveElse();
		}
	  }else{
		  DriveElse();
	  }
	  
	  allok();
  }
  
  private int nah = 100;
  private int weg = 80;
  private int ok = 90;
  private void driveCircle(Vector v){
	  driveCircle(v, 1);
  }
  private void driveCircle(Vector v, int speed){	  
	  Angle a = v.sub(this.getPosition()).getAngle();
	  
	  this.addDebugArrow(this.getPosition(), v);
	  
	  Angle c = null;
	  if(zuNah()){ 
		  c = new Angle(nah, "d");
		  speed = 1;
	  }else if(zuWeitWeg()){
		  c = new Angle(weg, "d");
		  speed = 1; 
	  }else{
		  c = a.add(new Angle(ok, "d"));
	  }
	  
	  if(HabLinie() && aufDerLinie() == false){
			if(this.getPosition().getY() + 5 < FlugraketeGetY(this.getPosition().getX())){
				drive(oldPS.pos.getAngle().sub(new Angle(180 - ok, "d")), DriveSpeed(1));
			}else if(this.getPosition().getY() + 5	> FlugraketeGetY(this.getPosition().getX())){
				drive(oldPS.pos.getAngle().add(new Angle(180 - ok, "d")), DriveSpeed(1));
			}else{
				driveCircle(st[0].location);
			}
	  }else{
		drive(a.add(c), DriveSpeed(speed));
	  }
  }
  private void drive(Angle a){
	  this.setAutopilot(a, DriveSpeed());
  }
  private void drive(Angle a, double speed){
	  this.setAutopilot(a, speed);
  }

  private double DriveSpeed(){
	  return DriveSpeed(-1);
  }
  private double DriveSpeed(double speed){
	//int i = (int)((Math.random()) * (maxspeed - minspeed + 1) + minspeed);
	  //double d = (double) i / 10;
	  
	  if(st[0] != null && oldPS != null){
		  if((oldPS.pos.distanceTo(this.getPosition()) <= BoosterAbstandRakete || this.getEnergy() >= BoosterAbEnergieBenutzen) && areWeFire == false){			 
			  if(aufDerLinie()){
				  this.setBoost();
				  
				  return 1;
			  }
		  }
	  }
	  
	  if(aufDerLinie()  || zuNah()  || zuWeitWeg()){
		  return 1;
	  }else{
		  if(time < 3){
			  time = this.getTime();
			  return 1;
		  }else{
			  if(speed != -1){
				  return speed;
			  }
			  
			  return 0.5;
		  }
	  }
  }
  
  private double FlugraketeGetY(double x){
	  if(oldPS != null && oldPS2 != null){		 
			double m = (double) (oldPS2.pos.getY() - oldPS.pos.getY()) / (oldPS2.pos.getX() - oldPS.pos.getX());
				 
			double t = oldPS.pos.getY() - m * oldPS.pos.getX();
				 
			if(m < Integer.MAX_VALUE && t < Integer.MAX_VALUE && m > Integer.MIN_VALUE && t > Integer.MIN_VALUE){
				this.addDebugLine(new Vector(-1000, m*-1000 + t), new Vector(1000, m*1000 + t));
			
				return m*x + t;
			}
	  }
	  
	  return 0;
  }
  private boolean HabLinie(){
	  if(oldPS != null && oldPS2 != null){
		return true;
	  }
	  return false;
  }
  private boolean aufDerLinie(){
	  if(oldPS != null && oldPS2 != null){		 
		double m = (double) (oldPS2.pos.getY() - oldPS.pos.getY()) / (oldPS2.pos.getX() - oldPS.pos.getX());
			 
		double t = oldPS.pos.getY() - m * oldPS.pos.getX();
			 
		double y = m * this.getPosition().getX() + t;
		
		if(m < Integer.MAX_VALUE && t < Integer.MAX_VALUE && m > Integer.MIN_VALUE && t > Integer.MIN_VALUE){		
			this.addDebugLine(new Vector(-1000, m*-1000 + t), new Vector(1000, m*1000 + t));
				 
			if(st[0] != null && oldPS.pos.getX() > st[0].location.getX() && oldPS.pos.getX() > this.getPosition().getX() && oldPS.pos.getY() > st[0].location.getY() && oldPS.pos.getY() > this.getPosition().getY()){
				return false;
			}else if(st[0] != null && oldPS.pos.getX() < st[0].location.getX() && oldPS.pos.getX() < this.getPosition().getX() && oldPS.pos.getY() < st[0].location.getY() && oldPS.pos.getY() < this.getPosition().getY()){
				return false;
			}else if(y >= this.getPosition().getY() - 9 && y <= this.getPosition().getY() + 9){
				ChangeColor(Color.red);
				 return true;
			}
		}
	  }
	  
	  return false;
  }
  private boolean mine(){
	  if(oldPS != null && oldPS2 != null){
			if(oldPS2.pos.getY() == oldPS.pos.getY() && oldPS2.pos.getX() == oldPS.pos.getX()){					 
				if(oldPS2.pos.getY() >= this.getPosition().getY() - 9 && oldPS2.pos.getY() <= this.getPosition().getY() + 9){
					ChangeColor(Color.orange);
					return true;
				}
			}
		  }
		  
		  return false;
  }
  private boolean zuNah(){
	  if(st[0] != null){
		  double a = st[0].location.distanceTo(this.getPosition());
		  
		  if(a < abstand){
			  ChangeColor(Color.yellow);
			  return true;
		  }
	  }
	  return false;
  }
  private boolean zuWeitWeg(){
	  if(st[0] != null){
		  double a = st[0].location.distanceTo(this.getPosition());
		  
		  if(a > abstand + 10){
			  ChangeColor(Color.blue);
			  return true;
		  }
	  }
	  return false;
  }
  private void allok(){
	  if(mine() == false && aufDerLinie() == false && zuNah() == false && zuWeitWeg() == false){
		  ChangeColor(Color.green);
	  }
  }
  private void ChangeColor(Color c){
	  this.setBodyColor(c);
	  this.setTurretColor(c);
	  this.setNameColor(c);
  }
  @SuppressWarnings("unused")
  private void ChangeColor(Color c1, Color c2, Color c3){
	  this.setBodyColor(c1);
	  this.setTurretColor(c2);
	  this.setNameColor(c3);
  }
}
