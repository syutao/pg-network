package pgnetwork.agent;

import pgnetwork.game.LearningPGGame;
import pgnetwork.game.PGGame;
import repast.simphony.space.graph.Network;

/**
 * implementation of a learning agent
 * 
 * @author Johannes Zschache
 *
 */
public class LearningAgent extends GeneralAgent {

	private boolean print = false;
	
	private double aspLevel = 0.0;
	private double satisfaction = 0.0;
	
	private LearningPGGame game;
	
	public LearningAgent(boolean cooperating, Network<Object> network, LearningPGGame game, boolean print) {
		super(network, game);
		this.game = game;
		this.setAction(cooperating);
		this.print = print;
		this.aspLevel = game.getuMin() - game.getuMax();
		//this.aspLevel = 0;
	}
	
	@Override
	public void step(){
		
		if (print){
			System.out.println("agent steps .. ");
			System.out.println("strategy: " + ((this.isCooperating()) ? "cooperating" : "defecting"));
			System.out.println("same friends: " + this.getSameDegree());
			System.out.println("other friends: " + this.getOtherDegree());
		}
		
		GeneralAgent[] pn = this.getPersonalNetwork();
		GeneralAgent[] refGroup = new GeneralAgent[pn.length];
		for (int i = 0; i < pn.length; i++)
			refGroup[i] = pn[i];
		
		double payoff = this.getLastPayoff();
		if (print)
			System.out.println("PayOff: " + payoff);
		
		double minPay = payoff;
		double maxPay = payoff;
		double averagePay = payoff;
		for (GeneralAgent agent : refGroup){
			double p = agent.getLastPayoff();
			if (p<minPay)
				minPay = p;
			if (p>maxPay)
				maxPay = p;
			averagePay += p;
		}
		if (refGroup.length>0)
			averagePay = averagePay / (refGroup.length + 1);
		if (print){
			System.out.println("aspLevel: " + aspLevel);
			System.out.print("averagePay: " + averagePay);
			System.out.print("; minPay: " + minPay);
			System.out.println("; maxPay: " + maxPay);
		}
		if (aspLevel < game.getuMin())
			// first time setting of aspLevel
			aspLevel = averagePay;
		else
			aspLevel = 0.5 * aspLevel + 0.5 * averagePay;
		
		if (print)
			System.out.println("aspLevel: " + aspLevel);
		if (payoff > (aspLevel - PGGame.epsilon) && payoff < (aspLevel + PGGame.epsilon))
			satisfaction = 0.0;
		else
			satisfaction = (payoff - aspLevel) / (game.getuMax() - game.getuMin());
		if (satisfaction < -1 || satisfaction > 1)
			System.out.println("!!!!!!!!!!!!!!!!!!satisfaction: " + satisfaction);
		
		if (print)
			System.out.println("satisfaction: " + satisfaction);
	}

	/**
	 * @return the satisfaction
	 */
	public double getSatisfaction() {
		return satisfaction;
	}

	/**
	 * @param satisfaction the satisfaction to set
	 */
	public void setSatisfaction(double satisfaction) {
		this.satisfaction = satisfaction;
	}

}
