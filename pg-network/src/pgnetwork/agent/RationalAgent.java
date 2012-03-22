package pgnetwork.agent;


import java.util.ArrayList;
import java.util.List;

import pgnetwork.game.PGGame;
import pgnetwork.game.RationalPGGame;

import repast.simphony.space.graph.Network;

/**
 * implementation of a rational agents
 * 
 * @author Johannes Zschache
 *
 */
public class RationalAgent extends GeneralAgent {
	
	private RationalPGGame game;
	
	public RationalAgent(boolean isCooperating, Network<Object> network, RationalPGGame game, boolean print){
		super(network, game);
		this.game = game;
		this.setAction(isCooperating);
		this.print = print;
	}
	
	private boolean print = false;
	
	@Override
	public void step() {
		if (print){
			System.out.println("step:");
			System.out.println("action: " + ((this.isCooperating())? "cooperating":"defecting") + "; degree: " + this.getDegree() + "; same: " + this.getSameDegree());
			System.out.println("payoff: " + this.getCurrentPayoff());
		}
		
		//looking for agents willing to form relation with me
		GeneralAgent[] willSameAgentsStay = game.getWillingAgents(this, true, false);
		GeneralAgent[] willSameAgentsChange = game.getWillingAgents(this, true, true);
		GeneralAgent[] willOtherAgentsStay = game.getWillingAgents(this, false, false);
		GeneralAgent[] willOtherAgentsChange = game.getWillingAgents(this, false, true);
		
		if (print){
			System.out.println("willSameAgentsStay: " + willSameAgentsStay.length);
			System.out.println("willSameAgentsCange: " + willSameAgentsChange.length);
			System.out.println("willOtherAgentsStay: " + willOtherAgentsStay.length);
			System.out.println("willOtherAgentsChange: " + willOtherAgentsChange.length);
		}
		
		int maxSameSize = Math.max(willSameAgentsStay.length, willSameAgentsChange.length);
		int maxOtherSize = Math.max(willOtherAgentsStay.length, willOtherAgentsChange.length);
		// shorter names .. 
		int sameDegree = this.getSameDegree();
		int otherDegree = this.getOtherDegree();
		int degree = this.getDegree();
		boolean cooperating = this.isCooperating();
		// calculate payoffs for each possible combination of action choice and network choice
		double[][][] possPayoffs = new double[maxSameSize + sameDegree + 1][maxOtherSize + otherDegree + 1][2];
		// 1. no action change
		//System.out.println("calculating possible payoffs same action .. ");
		for (int i = 0; i < willSameAgentsStay.length + sameDegree + 1; i++)
			for (int j = 0; j< willOtherAgentsStay.length + otherDegree + 1; j++){
				double newContacts = (double)Math.max(0, i - sameDegree) + Math.max(0, j - otherDegree);
				double oldContacts = (double)Math.max(0, sameDegree - i) + Math.max(0, otherDegree - j);
				double p = game.payoff(game.getCoopAgentsSize(), cooperating, degree + (j - otherDegree) + (i - sameDegree), sameDegree + (i - sameDegree));
				possPayoffs[i][j][0] = p - newContacts * game.cost2 - oldContacts * game.cost2;
			}
		// 2. change actions
		//System.out.println("calculating possible payoffs other action .. ");
		for (int i = 0; i < willSameAgentsChange.length + sameDegree + 1; i++)
			for (int j = 0; j< willOtherAgentsChange.length + otherDegree + 1; j++){
				double newContacts = (double)Math.max(0, i - sameDegree) + Math.max(0, j - otherDegree);
				double oldContacts = (double)Math.max(0, sameDegree - i) + Math.max(0, otherDegree - j);
				double p = game.payoff((game.getCoopAgentsSize() + (cooperating ? -1 : 1)),!cooperating, degree + (j - otherDegree) + (i - sameDegree), otherDegree + (j - otherDegree));
				possPayoffs[i][j][1] = p - newContacts * game.cost2 - oldContacts * game.cost2;
			}
		if (print){
			for (int a= 0; a<possPayoffs[0][0].length; a++) {
				System.out.println("a = " + a);
				for (int j= 0; j<possPayoffs[0].length; j++){
					System.out.print("[");
					for (int i= 0; i<possPayoffs.length; i++)
						System.out.print(possPayoffs[i][j][a] + ", ");
					System.out.println("]");
				}
			}
		}
		
		// find best possible action-network-combination
		double bestPayoff = this.getCurrentPayoff();
		boolean change = false;
		boolean changeAction = false;
		int bestSame = sameDegree;
		int bestOther = otherDegree;
		List<UpdatePoss> alternatives = new ArrayList<UpdatePoss>();
		alternatives.add(new UpdatePoss(bestPayoff, changeAction, bestSame, bestOther));
		for (int i= 0; i<possPayoffs.length; i++)
			for (int j= 0; j<possPayoffs[i].length; j++)
				for (int a= 0; a<possPayoffs[i][j].length; a++) {
					if (possPayoffs[i][j][a] > (bestPayoff + PGGame.epsilon)){
						change = true;
						alternatives = new ArrayList<UpdatePoss>();
						bestPayoff = possPayoffs[i][j][a];
						changeAction = ((a==1) ? true : false);
						bestSame = i;
						bestOther = j;
						alternatives.add(new UpdatePoss(bestPayoff, changeAction, bestSame, bestOther));
					} else if (possPayoffs[i][j][a] > (bestPayoff - PGGame.epsilon) && possPayoffs[i][j][a] < (bestPayoff + PGGame.epsilon)){
						alternatives.add(new UpdatePoss(possPayoffs[i][j][a], ((a==1) ? true : false), i, j));
					}
				}
		//random choosing of best update, which is one of the updates with minimal network change
		if (change && alternatives.size()>1){
			List<UpdatePoss> minNetAlt = new ArrayList<UpdatePoss>();
			for (UpdatePoss alt : alternatives){
				if ((Math.abs(bestSame - sameDegree) + Math.abs(bestOther - otherDegree)) > (Math.abs(alt.getSame() - sameDegree) + Math.abs(alt.getOther() - otherDegree))){
					minNetAlt = new ArrayList<UpdatePoss>();
					changeAction = alt.isChangeAction();
					bestSame = alt.getSame();
					bestOther = alt.getOther();
					minNetAlt.add(alt);
				} else if ((Math.abs(bestSame - sameDegree) + Math.abs(bestOther - otherDegree)) == (Math.abs(alt.getSame() - sameDegree) + Math.abs(alt.getOther() - otherDegree))){
					minNetAlt.add(alt);
				}	
			}
			// now: randomise over alternatives with minimal network update
			if (minNetAlt.size()>1){
				UpdatePoss rand = minNetAlt.get((int)Math.floor(Math.random() * minNetAlt.size()));
				bestSame = rand.getSame();
				bestOther = rand.getOther();
				changeAction = rand.isChangeAction();
			}
		}
		if (print){
			System.out.println("change: " + change);
			System.out.println("bestPayoff: " + bestPayoff);
			System.out.println("changeAction: " + changeAction);
			System.out.println("bestSame: " + bestSame);
			System.out.println("bestOther: " + bestOther);
		}
		// change towards best possible action-network-combination
		if (change){
			if (bestSame > sameDegree){
				// add same contacts
				GeneralAgent[] arr = null;
				if (changeAction)
					arr = willSameAgentsChange;
				else
					arr = willSameAgentsStay;
				for (int i = 0; i < bestSame - sameDegree; i++)
					this.addContact(arr[i]);
			} else if (bestSame < sameDegree){
				//remove same contacts
				for (int i = 0; i < sameDegree - bestSame; i++)
					this.removeRandomContact(true);
			}
			if (bestOther > otherDegree){
				// add other contacts
				GeneralAgent[] arr = null;
				if (changeAction)
					arr = willOtherAgentsChange;
				else
					arr = willOtherAgentsStay;
				for (int i = 0; i < bestOther - otherDegree; i++)
					this.addContact(arr[i]);
			} else if (bestOther < otherDegree){
				//remove other contacts
				for (int i = 0; i < otherDegree - bestOther; i++)
					this.removeRandomContact(false);
			}
			// change action - has to be the last one!!
			if (changeAction)
				this.setAction(!cooperating);
			game.setStChanged(true);
			if (print){
				System.out.println("step finished:");
				System.out.println("action: " + ((this.isCooperating())? "cooperating":"defecting") + "; degree: " + this.getDegree() + "; same: " + this.getSameDegree());
				System.out.println("payoff: " + this.getCurrentPayoff());
			}
		}
	}

}

class UpdatePoss{
	private double payoff;
	private boolean changeAction;
	private int same;
	private int other;
	
	public UpdatePoss(double payoff, boolean changeAction, int same, int other) {
		this.payoff = payoff;
		this.changeAction = changeAction;
		this.same = same;
		this.other = other;
	}
	/**
	 * @return the payoff
	 */
	public double getPayoff() {
		return payoff;
	}
	/**
	 * @param payoff the payoff to set
	 */
	public void setPayoff(double payoff) {
		this.payoff = payoff;
	}
	/**
	 * @return the changeAction
	 */
	public boolean isChangeAction() {
		return changeAction;
	}
	/**
	 * @param changeAction the changeAction to set
	 */
	public void setChangeAction(boolean changeAction) {
		this.changeAction = changeAction;
	}
	/**
	 * @return the same
	 */
	public int getSame() {
		return same;
	}
	/**
	 * @param same the same to set
	 */
	public void setSame(int same) {
		this.same = same;
	}
	/**
	 * @return the other
	 */
	public int getOther() {
		return other;
	}
	/**
	 * @param other the other to set
	 */
	public void setOther(int other) {
		this.other = other;
	}
}
