package pgnetwork.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pgnetwork.PGSettings;
import pgnetwork.agent.GeneralAgent;

/**
 * implementation of the course of a game with rational agents
 * 
 * @author Johannes Zschache
 *
 */
public class RationalPGGame extends PGGame{
	
	private boolean stChanged = false;
	private PGSettings settings;
	
	public RationalPGGame(PGSettings settings){
		super(settings);
		initAgents(settings.getNetworkSize(),	settings.getInitCoopRate(), settings.isRational(), settings.isPrintOut());
		initNetwork(settings.getInitDensity(), settings.getInitSegregation());
		
		this.settings = settings;
	}
	
	public void step(){
		stChanged = false;
		List<GeneralAgent> randomList = new ArrayList<GeneralAgent>();
		for (GeneralAgent agent : getAllAgents())
			randomList.add(agent);
		if (settings.getUpdateView().equals(PGSettings.UPDATE_VIEW_AGENT))
			settings.setMovingRatio(settings.getMovingRatio() * 1.35);
		for (int i = 0; i < getAllAgents().length; i++){
			GeneralAgent agent = randomList.remove((int)Math.floor(Math.random() * randomList.size())); 
			agent.step();
			if (settings.getUpdateView().equals(PGSettings.UPDATE_VIEW_AGENT)){
				agent.moveFromHighestDegreeStranger(settings);
				agent.moveToHighestDegreeNeigh(settings);
			}
		}
	}
	
	public boolean terminate(){
		return !stChanged;
	}
	
	public int maxGames(){
		return 100;
	}
	
	/**
	 * returns the agents of sameAction like <i>one</i> who are willing to connect with <i>one</i> given that <i>one</i> is going to change is action or not 
	 * @param one - the agent who wants to build up new ties
	 * @param sameAction - filter agents who are considered for bonding
	 * @param change - does <i>one</i> wants to change is action?
	 * @return
	 */
	public GeneralAgent[] getWillingAgents(GeneralAgent one, boolean sameAction, boolean change){
		Set<GeneralAgent> result = new HashSet<GeneralAgent>();
		
		GeneralAgent[] agentsArray = ((sameAction) ? ((one.isCooperating()) ? getCoopAgentsAsArray() : getDefAgentsAsArray()) : ((one.isCooperating()) ? getDefAgentsAsArray() : getCoopAgentsAsArray()));
		Set<GeneralAgent> agents = new HashSet<GeneralAgent>();
		for (GeneralAgent a : agentsArray)
			if (a!=one && !one.personalNetworkContains(a))
				agents.add(a);
		
		for (GeneralAgent two : agents){
			int coopAgentsSize = getCoopAgentsSize() + ((change) ? ((one.isCooperating()) ? -1 : 1) : 0);
			int sameDegree = two.getSameDegree() + ((sameAction) ? ((change) ? 0 : 1) : ((change) ? 1 : 0));
			if (two.ownPayoff() <= (payoff(coopAgentsSize, two.isCooperating(), two.getDegree() + 1, sameDegree) - cost2 + RationalPGGame.epsilon))
				result.add(two);
		}
		return result.toArray(new GeneralAgent[0]);
	}

	/**
	 * @param stChanged the stChanged to set
	 */
	public void setStChanged(boolean stChanged) {
		this.stChanged = stChanged;
	}
}
