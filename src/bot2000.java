import java.awt.Color;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Locale.Category;

import jrobots.simulation.simulationObjects.JRobot2015_3;
import jrobots.utils.Angle;
import jrobots.utils.LinearPredictor;
import jrobots.utils.SonarTrace;
import jrobots.utils.Vector;

public class bot2000 extends JRobot2015_3 {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private double SCANENERGYLIMIT = 0.612;
	
	private double speed = 0.5;//1.0
	private int status = 0;
	private SonarTrace oldScan = null;
	private int frame = 0;
	private int SCANSPERFRAME = 37;
	private double multiply = 1;

	private double angle = 0;
	
	private Vector lastTarget = null;

	private Vector lastPos = null;

	public bot2000() {
		this.setNameColor(Color.red);
		this.setBodyColor(Color.red);
		this.setTurretColor(Color.red);
	}
	
	enum Priority {
		SURVIVAL,
		FIRE,
		OTHER
	}
	
	abstract class Action  implements Serializable {
		private static final long serialVersionUID = 1L;
		private double cost;
		private Priority prio;
		public Action(double cost, Priority prio) {
			this.cost = cost;
			this.prio = prio;
		}
		
		public abstract void run();
	}
	
	class Scheduler implements Serializable {
		private static final long serialVersionUID = 1L;
		private LinkedList<Action> actions = new LinkedList<Action>();
	
		public Scheduler() {}
		
		private LinkedList<Action> filterByPrio(Priority p) {
			LinkedList<Action> out = new LinkedList<Action>();
			for(Action a: actions)
				if (a.prio == p)
					out.add(a);
			
			return out;
		}
		
		private LinkedList<Action> sort(LinkedList<Action> in) {
			Collections.sort(in, new Comparator<Action>() {
				@Override
				public int compare(Action o1, Action o2) {
					return Double.compare(o1.cost, o2.cost);
				}
			});
			return in;
		}
		
		public void run() {
			double availableEnergy = getEnergy();
			LinkedList<Action> executed = new LinkedList<Action>();
			for (Priority p : Priority.values()) {
				LinkedList<Action> filtered = filterByPrio(p);
				filtered = sort(filtered);
				for (Action a : filtered)
					if (a.cost < availableEnergy) {
						a.run();
						executed.add(a);
						availableEnergy -= a.cost;
					}
			}
			

			if (executed.size() != actions.size()) {
				System.out.println("Not all actions could be executed.");
			}
			actions.clear();
		}
		
		public void add(Action a) {
			actions.add(a);
		}
	}
	
	private Scheduler sched = new Scheduler();
	
	@Override
	protected void actions() {

		frame++;
		drawDebugTarget();
		this.updateStatus();
		if(status == 0 || status == 1){
			if (this.getEnergy() < SCANENERGYLIMIT - 0.1) {
				this.drive();
			} else {
				sched.add(new Action(0, Priority.OTHER) {
					@Override
					public void run() {
						setAutopilot(new Angle(Math.random() * 360, "d"), 0.5);
					}
				});
				this.fire();
			}
		} else {
			this.drive();
			this.fire();
		}
		
		sched.run();
	}

	protected void drive() {
		/*if(lastTarget != null){
			double length = lastTarget.getLength();
			System.out.println(length);
			if(length <= 182.0){*/
				double angle = Math.sin(this.getTime() * 0.7) * 100;
				sched.add(new Action(this.getEnergyConsumptionEngine() * speed, Priority.OTHER) {
					@Override
					public void run() {
						setAutopilot(new Angle(angle, "d"), speed);
					}
				});
			/*} else {
				if(status <= 2){
					this.setAutopilot(new Angle(angle, "d"), 0);
					//SCANENERGY = 70.0;//0.18;
				}*/
			//}
		//}

		int boost_threshold = (int) (JRobot2015_3.getEnergyConsumptionBooster()/getEnergyProductionPerFrame() * 25); 
		if (frame % boost_threshold == 0) {
			sched.add(new Action(this.getEnergyConsumptionBooster(), Priority.SURVIVAL) {
				@Override
				public void run() {
					setBoost();
				}	
			});
			System.out.println("boost"+ boost_threshold);
		}
	}

	private void fire() {
		if (this.getLastSonarTrace() != null
				&& this.getLastSonarTrace().timestamp == this.getTime()
				&& oldScan != null) {
			// Vector scanPos = this.getLastSonarTrace().location.sub(this
			// .getPosition());
			Vector scanPos = this.predictTarget(this.getLastSonarTrace().location);
			
			this.lastTarget = scanPos;
			this.lastPos = this.getPosition();
			
			sched.add(new Action(this.getEnergyConsumptionProjectile(),
					Priority.FIRE) {
				@Override
				public void run() {
					setLaunchProjectileCommand(scanPos.getAngle());
				}
			});
		}
		this.oldScan = this.getLastSonarTrace();

		if (frame % SCANSPERFRAME == 0) {
			double energy = SCANSPERFRAME * getEnergyProductionPerFrame() * 0.30;
			sched.add(new Action(energy, Priority.OTHER) {

				@Override
				public void run() {
					setSonarEnergy(energy); 
				}
			});
		}
		
	}

	private void drawDebugTarget() {
		if (lastTarget != null && lastPos != null) {
			this.addDebugLine(lastPos, lastTarget);

			// this.addDebugLine(this.getPosition(),
			// predictTarget(scanPos).sub(this.getPosition()));
		}
	}

	private Vector predictTarget(Vector currentScan) {
		double futureTime = this.getTime()
				+ (currentScan.getLength() / JRobot2015_3.getProjectileSpeed()) * multiply;
		Vector prediction = LinearPredictor.predict(this.getLastSonarTrace(),
				this.oldScan, futureTime).sub(this.getPosition());

		return prediction;
	}

	private void updateStatus() {
		double health = this.getHealth();
		if (health <= 0.70) {
			speed = 1.0;
			this.setBodyColor(Color.red);
			this.setTurretColor(Color.red);
			status = 1;

			if (health <= 0.40) {
				speed = 1.5;
				this.setBodyColor(Color.red);
				this.setTurretColor(new Color(160, 40, 235));
				status = 2;


				if (health <= 0.20) {
					speed = 1.5;
					this.setBodyColor(Color.red);
					status = 3;

					if (this.getProjectileRadar() != null
							&& this.getEnergy() >= 0.25) {
						this.setBoost();
					}
				}
				
			}

		} else {
			speed = 0;
			this.setBodyColor(Color.red);
			this.setTurretColor(Color.red);
			status = 0;
		}
	}
}
