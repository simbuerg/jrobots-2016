import java.awt.Color;
import java.util.Random;

import jrobots.utils.Angle;
import jrobots.utils.ProximityScan;
import jrobots.utils.SonarTrace;
import jrobots.utils.Vector;
import jrobots.simulation.simulationObjects.JRobot2015_3;
import jrobots.simulation.simulationObjects.Pilot;



public class Black_Bierbauch_Uno extends JRobot2015_3 {
	private static final long serialVersionUID = 1L;
	private Angle a = new Angle(0,"d");
	private int t = 0;
	private int value =0;
	private int time = 36;
	private double energy = 0.27;
	
	public Black_Bierbauch_Uno(){
		super();
	    this.setNameColor(Color.WHITE);
	    this.setBodyColor(Color.yellow);
	    this.setTurretColor(Color.RED);
	    this.setAutopilot(LEFT, 1);
	    System.out.println(this.getEnergy());
	    
	    
	    
	}
	
	@Override
	protected void actions() {
		t++;
		value = value + 1;
		Random r = new Random();
		int d = r.nextInt(480);
		if(d< value){
			time = 72;
			energy = 0.54;
		}
		else{
			time = 36;
			energy = 0.27;
			
		}
		
		this.farbe();
		this.vectorScan();
			if(t % time == 0 ){
				Scan();
				
			}
				if(this.getLastSonarTrace() != null && t % time == 1){
					this.fire();	
				}
		
			
				
			
			if(t % 1200 == 0){
				
				this.end();
			}
			
			//System.out.println("Boost");
		}
			
			
			
			
			
			
	
		
	
	public void drive(){
		
		if(t % 48 == 0){	
			a = a.add(new Angle(45, "d"));
			this.setAutopilot(a, 1);	
			
			}
	}
	
	public void end(){
		
		this.setAutopilot(getOrientation().getNextQuadrant(), 1);
		this.setBoost();
	}
	
	
	

	public void farbe(){
		Random r = new Random();
		if(t % 5 == 0){
			int i  = r.nextInt(8);
			if(i== 0){
				this.setTurretColor(Color.BLACK);
			}
			else if(i== 1){
				this.setTurretColor(Color.BLUE);
			}
			else if(i== 2){
				this.setTurretColor(Color.YELLOW);
			}
			else if(i== 3){
				this.setTurretColor(Color.GREEN);
			}
			else if(i== 4){
				this.setTurretColor(Color.MAGENTA);
			}
			else if(i== 5){
				this.setTurretColor(Color.WHITE);
			}
			else if(i== 6){
				this.setTurretColor(Color.CYAN);
			}
			else if(i== 7){
				this.setTurretColor(Color.PINK);
			}
		
		i  = r.nextInt(8);
		if(i== 0){
			this.setBodyColor(Color.BLACK);
			
		}
		else if(i== 1){
			this.setBodyColor(Color.BLUE);
		}
		else if(i== 2){
			this.setBodyColor(Color.YELLOW);
		}
		else if(i== 3){
			this.setBodyColor(Color.GREEN);
		}
		else if(i== 4){
			this.setBodyColor(Color.MAGENTA);
		}
		else if(i== 5){
			this.setBodyColor(Color.WHITE);
		}
		else if(i== 6){
			this.setBodyColor(Color.CYAN);
		}
		else if(i== 7){
			this.setBodyColor(Color.PINK);
		}
		
		i  = r.nextInt(8);
		if(i== 0){
			this.setNameColor(Color.BLACK);
		}
		else if(i== 1){
			this.setNameColor(Color.BLUE);
		}
		else if(i== 2){
			this.setNameColor(Color.YELLOW);
		}
		else if(i== 3){
			this.setNameColor(Color.GREEN);
		}
		else if(i== 4){
			this.setNameColor(Color.MAGENTA);
		}
		else if(i== 5){
			this.setNameColor(Color.WHITE);
		}
		else if(i== 6){
			this.setNameColor(Color.CYAN);
		}
		else if(i== 7){
			this.setNameColor(Color.PINK);
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
					
					 System.out.println("boo"+i);
				 
				 
				 
			 }
			 break;
			}
		
		}
	}
	
	
	public void vectorScan(){
		
		
		ProximityScan p = this.getProjectileRadar();
		if(p != null && t % 10 == 0){
		double my_X = this.getPosition().getX();
		double my_Y = this.getPosition().getY();
		
		double pro_X = p.pos.getX();
		double pro_Y = p.pos.getY();
		
		Vector v = p.predict(1/24);
		double pro_X2 = v.getX();
		double pro_Y2 = v.getY();
		
		double pro_steigung = (pro_Y - pro_Y2)/(pro_X - pro_X2);
		double pro_t = pro_Y - pro_steigung*pro_X;
		double pro_y_schnitt = pro_steigung * my_X +pro_t;
		this.addDebugArrow(p.pos, new Vector(my_X,pro_y_schnitt));
		if(this.getPosition().distanceTo(new Vector(my_X,pro_y_schnitt)) < 30){
			Vector vec = new Vector(pro_X-pro_X2, pro_Y-pro_Y2);
			System.out.println("DING" + t + " "+ vec.getAngle()); 
			
			if(my_Y < pro_y_schnitt){
				
				this.setAutopilot(vec.getAngle().add(new Angle(75,"d")), 1);
			}
			else{
				
				this.setAutopilot(vec.getAngle().add(new Angle(-75,"d")), 1);
			}
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
	
	this.setSonarEnergy(this.getEnergy()-this.getEnergyConsumptionEngine()-energy);
	getLastSonarTrace();
}


public void fire(){
	Vector v = this.GegnerScan();


	this.setLaunchProjectileCommand(v.sub(this.getPosition()).getAngle());
	this.addDebugCrosshair(v);
}




}
	
	
	


