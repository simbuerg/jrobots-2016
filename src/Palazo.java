import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import jrobots.simulation.simulationObjects.JRobot2015_2;
import jrobots.utils.Angle;
import jrobots.utils.LinearPredictor;
import jrobots.utils.SonarTrace;
import jrobots.utils.Vector;

public class Palazo extends JRobot2015_2 {
	private static final long serialVersionUID = 1L;
	private final double MAX_DISTANCE = 25;
	
	
	private SonarTrace latestTrace = new SonarTrace(0, new Vector(0, 0), 0);

	private LinkedList<SonarTrace> traceHistory = new LinkedList<SonarTrace>();
	private static final int HISTORY_LENGTH = 2;

	private double sonarEnergy = 8 * Palazo.getEnergyProductionPerFrame();
	private double sonarFrequency = 1;
	
	private double motionRefresh = 2;
	private double lastRefresh = 0;
	private double fuzz = 0;
	
	private double fireFrequency = 3;
	private double lastFire;

	enum Category {
		SURVIVAL,
		FIRE,
		MOTION,
		MISC
	}
	
	abstract class Action implements Serializable {
		protected static final long serialVersionUID = 1L;
		protected Category cat;
		protected double energyCost;

		public Action(double cost, Category cat) {
			this.cat = cat;
			this.energyCost = cost;
		}

		public double getEnergyCost() {
			return energyCost;
		}
		public abstract void exec();
	}
	
	class Scheduler implements Serializable {
		private static final long serialVersionUID = 1L;
		private LinkedList<Action> actions = new LinkedList<Action>();
		
		private LinkedList<Action> filterByCat(Category cat, LinkedList<Action> in) {
			LinkedList<Action> out = new LinkedList<Action>();

 			for (Action a : in)
 				if (a.cat == cat)
 					out.add(a);
 			
 			Collections.sort(out, new Comparator<Action>() {
				@Override
				public int compare(Action o1, Action o2) {
					return o1.cat.compareTo(o2.cat);
				}
			});
 			
 			return out;
		}
		
		public void add(Action a) { actions.add(a); }

		
		protected double runActions(double availableEnergy, LinkedList<Action> actions) {
			double remainingEnergy = availableEnergy;
			
			for (Action a : actions) {
				if (remainingEnergy >= a.getEnergyCost()) {
					a.exec();
					remainingEnergy -= a.getEnergyCost();
				}
			}
			return remainingEnergy;
		}
		
		public void run(double availableEnergy) {
			LinkedList<Action> l;
			
			for (Category c : Category.values()) {
				l = filterByCat(c, actions);
				availableEnergy = runActions(availableEnergy, l);
			}
			
			actions.clear();
		}
	}
	private Scheduler sched = new Scheduler();
	
	private void printStats() {
		System.out.println("====");
		System.out.println("Energy: " + this.getEnergy());
		System.out.println("SonarEnergy: " + this.sonarEnergy);
		if (latestTrace != null)
			System.out.println("Precision: " + this.latestTrace.standardDeviation);
			
		System.out.println("====");
	}

	private boolean reachedTimeThreshold(double timeCompare, double timeDelta) {
		double timediff = getTime() - timeCompare;
		return timediff >= timeDelta;
	}

	private void scheduleScan(double energy) {
		double timeOfLastSonar = latestTrace.timestamp;

		if (this.getEnergy() >= energy &&
			reachedTimeThreshold(timeOfLastSonar, sonarFrequency)) {
			sched.add(new Action(energy, Category.MISC) {
				private static final long serialVersionUID = 1L;

				@Override
				public void exec() {
					setSonarEnergy(energyCost);
				}
			});
		}
	}

	private void evalScan() {
		SonarTrace trace = this.getLastSonarTrace();
		if (trace == null && latestTrace.timestamp == 0)
			scheduleScan(sonarEnergy);

		if (trace == null)
			return;
		
		if (trace.timestamp >= latestTrace.timestamp) {
			traceHistory.add(latestTrace);
			while (traceHistory.size() > HISTORY_LENGTH)
				traceHistory.remove();

			latestTrace = trace;
			scheduleScan(sonarEnergy);
		}
	}
	
	private void updateMotionParamters() {
		if (!reachedTimeThreshold(lastRefresh, motionRefresh))
			return;
	
		lastRefresh = getTime();
		fuzz = Math.random();	
	}
	
    private void drive() {
    	Angle avoidance = new Angle(20, "d");
		avoidance = avoidance.mult((fuzz == 0) ? -1 : 1);
		Vector loc = latestTrace.location;

		boolean invert = getPosition().distanceTo(latestTrace.location) < MAX_DISTANCE;

		final Angle direction;
		if (!invert)
			direction = loc.sub(getPosition()).getAngle().add(avoidance);
		else
			direction = loc.sub(getPosition()).getAngle().add(avoidance).getOpposite();
		
		final double speed = Math.random();
		sched.add(new Action(speed * getEnergyConsumptionEngine(), Category.MOTION) {
			private static final long serialVersionUID = 1L;

			@Override
			public void exec() {
				setAutopilot(direction, speed);
			}	
		});
	}
    
    private Vector predictLinear(SonarTrace st_old, SonarTrace st_new, double time) {
    	Vector s_old = st_old.location;
    	Vector s_new = st_new.location;
    	
    	double t_old = st_old.timestamp;
    	double t_new = st_new.timestamp;
    	
    	Vector d_s = s_new.sub(s_old);
    	double d_t = t_new - t_old;

    	if (d_t == 0.0)
    		return s_new;
    	double scale = (time - t_new) / d_t;
    	return s_new.add(d_s.mult(scale));
    }
    
    private Vector predictEnemy(LinkedList<SonarTrace> scans, SonarTrace latest) {
    	boolean hasHistory = traceHistory.size() >= 1;
    	if (!hasHistory)
    		return latest.location.sub(getPosition());

    	int idx_new = traceHistory.size() - 1;
    	SonarTrace st_new = traceHistory.get(idx_new);
		double g_spd = Palazo.getGrenadeSpeed();
		
		
		double dt = getPosition().distanceTo(latest.location) / g_spd;
		// That's the location when the grenade lands at the latest location.
		// Use this location to predict the real dt needed.
		Vector loc = predictLinear(st_new, latest, getTime() + dt);
		addDebugCrosshair(loc);
		dt = getPosition().distanceTo(loc) / g_spd;
		
		return loc;
    }
    
    private void fire() {
    	if (!reachedTimeThreshold(lastFire, fireFrequency))
    		return;

    	boolean hasCurrentTrace = latestTrace.timestamp != 0;
    	
    	double reqEnergy = getEnergyConsumptionGrenade();
    	
    	if (hasCurrentTrace) {
    		final Vector loc = predictEnemy(traceHistory, latestTrace);
    		final double distance = loc.getLength();

    		sched.add(new Action(reqEnergy, Category.FIRE) {
    			private static final long serialVersionUID = 1L;

				@Override
				public void exec() {
					Vector target = loc.sub(getPosition());
					System.out.println("distance: " + distance);
					setLaunchProjectileCommand(target.getAngle(), target.getLength());
					lastFire = getTime();
				}
    		});
    	}
    	
    }
	
	@Override
	protected void actions() {
		evalScan();
		drive();
		updateMotionParamters();
		fire();
		
		sched.run(getEnergy());
	}
}
