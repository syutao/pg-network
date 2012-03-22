package pgnetwork.game;

import pgnetwork.PGSettings;
import pgnetwork.agent.GeneralAgent;
import pgnetwork.agent.LearningAgent;

/**
 * implementation of the course of a game with learning agents
 * 
 * @author Johannes Zschache
 *
 */
public class LearningPGGame extends PGGame{
	
	PGSettings settings;
	
	GeneralAgent[] agents;
	private double[] propCoop;
	private double[][] propNet;
	
	private double uMax;
	private double uMin;
	
	private double learning1;
	private double learning2;
	
	private boolean print = false;
	
	private int stepCount = 0;
	
	public LearningPGGame(PGSettings settings){
		super(settings);
		this.settings = settings;
		this.print = settings.isPrintOut();
		this.learning1 = settings.getLearning1();
		this.learning2 = settings.getLearning2();
		
		uMax = (double)settings.getNetworkSize() * settings.getPgValue() - settings.getCost1() +
				settings.getBcValue() * ((double)settings.getNetworkSize() - 1.0);
		uMin = settings.getPgValue() - settings.getCost1() - settings.getBcValue() * ((double)settings.getNetworkSize() - 1.0);
		if (print){
			System.out.println("uMax: " + uMax);
			System.out.println("uMin: " + uMin);
		}
		//init agents
		initAgents(settings.getNetworkSize(), settings.getInitCoopRate(), settings.isRational(), settings.isPrintOut());
		
		GeneralAgent[] allAgents = this.getAllAgents();
		// make a copy of all Agents, because the array of PGGame is subject of alterations
		agents = new GeneralAgent[allAgents.length];
		for (int i = 0; i<allAgents.length;i++)
			agents[i] = allAgents[i];
		
		propCoop = new double[agents.length];
		
		for (int i = 0; i<agents.length; i++){
			if (agents[i].isCooperating())
//				propCoop[i] = this.getStartCoopRate();
				propCoop[i] = 1.0;
		}
		if (print){
			System.out.println("start coop:");
			System.out.print("[");
			for (double d : propCoop)
				System.out.print(d + ", ");
			System.out.println("]");
		}
		// init network
		propNet = new double[agents.length][agents.length];
		
		initNetwork(settings.getInitDensity(), settings.getInitSegregation());
//		double pSame = 2 * this.getStartDensity() * this.getStartSegregation();
//		double pOther = (1.0 - this.getStartSegregation()) / this.getStartSegregation() * pSame;
		
		for (int i = 0; i<agents.length; i++){
			for (int j = 0; j<agents.length; j++){
				if (i != j && agents[i].personalNetworkContains(agents[j]))
					propNet[i][j] = 1.0;
//				if (i != j)
//					propNet[i][j] = ((agents[i].isCooperating() == agents[j].isCooperating()) ? pSame : pOther);
			}
		}
		if (print){
			System.out.println("start network:");
			for (int j= 0; j<propNet[0].length; j++){
				System.out.print("[");
				for (int i= 0; i<propNet.length; i++)
					System.out.print(propNet[i][j] + ", ");
				System.out.println("]");
			}
		}
	}
	
	public void step(){
		stepCount++;
		if (print)
			System.out.println("new round .. ");
		// 1. play n-Person public goods game and hand over corresponding payoff to each agent
		for (GeneralAgent agent : agents){
			agent.setLastPayoff(agent.getCurrentPayoff());
		}
		
		// 2. each agent gets the chance to be satisfied/dissatisfied with current payoff
		for (GeneralAgent agent : agents){
			agent.step();
		}
		// 3. after each agent has 'calculated'/'felt' its satisfaction value, propensities are going to be updated
		if (print)
			System.out.println("updating propensities .. ");
		for (int i = 0; i < agents.length; i++){
			double sati = ((LearningAgent)agents[i]).getSatisfaction();
			if (sati >= 0)
				propCoop[i] = propCoop[i] + learning1 * sati * (((agents[i].isCooperating()) ? 1 : 0) - propCoop[i]);
			else 
				propCoop[i] = propCoop[i] + learning1 * sati * (((agents[i].isCooperating()) ? 1 : 0) + propCoop[i] - 1);
			if (propCoop[i] < 0 || propCoop[i] > 1)
				System.out.println("warning!!: propCoop=" + propCoop[i] + "; sati=" + sati + "; coop=" + ((agents[i].isCooperating()) ? 1 : 0));
		}
		for (int i = 0; i < agents.length; i++){
			for (int j = 0; j < i; j++){
				boolean connected = agents[i].personalNetworkContains(agents[j]);
				double sati = ((LearningAgent)agents[i]).getSatisfaction();
				double satj = ((LearningAgent)agents[j]).getSatisfaction();
				//double sat = (sati + satj) / 2;
				if (connected)
					if (sati < 0 && satj < 0){ // both are dissatisfied
						propNet[i][j] = propNet[i][j] + learning2 * sati * propNet[i][j];
						propNet[i][j] = propNet[i][j] + learning2 * satj * propNet[i][j];
					} else if (sati < 0 || satj < 0){
						propNet[i][j] = propNet[i][j] + learning2 * Math.min(sati,satj) * propNet[i][j];
						propNet[i][j] = propNet[i][j] + learning2 * Math.max(sati,satj) * (1 - propNet[i][j]);
					} else { // both are satisfied
						propNet[i][j] = propNet[i][j] + learning2 * sati * (1 - propNet[i][j]);
						propNet[i][j] = propNet[i][j] + learning2 * satj * (1 - propNet[i][j]);
					}
				else // not connected
					if (sati<0 && satj<0){ // both dissatisfied
						propNet[i][j] = propNet[i][j] - learning2 * sati * (1 - propNet[i][j]);
						propNet[i][j] = propNet[i][j] - learning2 * satj * (1 - propNet[i][j]);
					} else if (sati >= 0 && satj >= 0){ //both are satisfied
						propNet[i][j] = propNet[i][j] - learning2 * sati * propNet[i][j];
						propNet[i][j] = propNet[i][j] - learning2 * satj * propNet[i][j];
					} else {// one of parties is satisfied
						propNet[i][j] = propNet[i][j] - learning2 * Math.min(sati,satj) * (1 - propNet[i][j]);
						propNet[i][j] = propNet[i][j] - learning2 * Math.max(sati,satj) * propNet[i][j];
					}
				propNet[j][i] = propNet[i][j];

				// old update
//				if (sat >= 0)
//					propNet[i][j] = propNet[i][j] + lambda2 * sat * ((connected? 1 : 0) - propNet[i][j]);
//				else
//					propNet[i][j] = propNet[i][j] + lambda2 * sat * ((connected? 1 : 0) + propNet[i][j] - 1);
				
			}
		}
		if (print){
			System.out.println("coop:");
			System.out.print("[");
			for (double d : propCoop)
				System.out.print(d + ", ");
			System.out.println("]");
		}
		if (print){
			System.out.println("network:");
			for (int j= 0; j<propNet[0].length; j++){
				System.out.print("[");
				for (int i= 0; i<propNet.length; i++)
					System.out.print(propNet[i][j] + ", ");
				System.out.println("]");
			}
		}
		// 4. update the strategies and network corresponding to the propensities
		for (int i = 0; i < agents.length; i++){
			agents[i].setAction(Math.random() < propCoop[i]);
			for (int j = 0; j < i; j++){
				if (Math.random() < propNet[i][j])
					agents[i].addContact(agents[j]);
				else
					agents[i].removeContact(agents[j]);
			}
		}
		// 5. update view
		if (settings.getUpdateView().equals(PGSettings.UPDATE_VIEW_AGENT)){
			for (GeneralAgent agent : agents){
				agent.moveFromHighestDegreeStranger(settings);
				agent.moveToHighestDegreeNeigh(settings);
			}
		}
	}

	@Override
	public boolean terminate() {
		
		return stepCount >= 4000;
	}
	
	public int maxGames(){
		return 100;
	}
	
	public double getAvgCoopRate(){
		double sum = 0.0;
		for (double d : propCoop)
			sum += d;
		return sum / (double)propCoop.length;
	}

	/**
	 * @return the uMax
	 */
	public double getuMax() {
		return uMax;
	}

	/**
	 * @return the uMin
	 */
	public double getuMin() {
		return uMin;
	}
	

}
