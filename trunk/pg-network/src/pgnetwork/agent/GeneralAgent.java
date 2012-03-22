package pgnetwork.agent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pgnetwork.PGSettings;
import pgnetwork.game.PGGame;

import repast.simphony.annotate.AgentAnnot;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;

/**
 * contains methods for initialising the network
 * controls update of personal Network and strategy
 * 
 * @author Johannes Zschache
 */
@AgentAnnot (displayName="Agent")
public class GeneralAgent {
	
	private boolean cooperating;
	
	private double lastPayoff;
	
	private Set<GeneralAgent> personalNetwork = new HashSet<GeneralAgent>();
	private int degree = 0;
	private int sameDegree = 0;
	private int otherDegree = 0;
	
	private Network<Object> network;
	private PGGame game;
	
	/**
	 * Map of probabilities specifying the likelihood of an agent to be new contact of this
	 */
	//private Map<GeneralAgent, Double> probDist;
	
	public GeneralAgent(Network<Object> network, PGGame game){
		this.network = network;
		this.game = game;
	}
	
	
	
	//@ScheduledMethod(start=1, interval=1)
	public void step(){
		//Override by subclasses
	}
	
	
	private Set<GeneralAgent> availSame;
	private Set<GeneralAgent> availOther;
	
	public void setAvailAgents(Set<GeneralAgent> allCoops, Set<GeneralAgent> allDefs){
		availSame = new HashSet<GeneralAgent>();
		availOther = new HashSet<GeneralAgent>();
		for (GeneralAgent a : allCoops)
			if (!personalNetwork.contains(a) && !a.equals(this))
				if (cooperating)
					availSame.add(a);
				else
					availOther.add(a);
		for (GeneralAgent a : allDefs)
			if (!personalNetwork.contains(a) && !a.equals(this))
				if (cooperating)
					availOther.add(a);
				else
					availSame.add(a);
	}
	
	public void initPersonalNetwork(int neighSize, double segregation){
		//System.out.println("neighSize: " + neighSize);
		int maxSameSize = availSame.size() + getSameDegree();
		int maxOtherSize = availOther.size() + getOtherDegree();
		int sameSize = (int)Math.round((double)(neighSize) * segregation);
		int otherSize = neighSize - sameSize;
		if (sameSize > maxSameSize){
			sameSize = maxSameSize;
			otherSize = (int)Math.round((double)sameSize * (1-segregation) / segregation);
		}
		if (otherSize > maxOtherSize){
			otherSize = maxOtherSize;
			sameSize = (int)Math.round((double)otherSize * (segregation) / (1-segregation));
		}
		//System.out.println("same Size: " + sameSize + "; other Size: " + otherSize);
		//System.out.println("same Degree: " + sameDegree + "; other Degree: " + otherDegree);
		int sameAdd = sameSize - sameDegree;
		int otherAdd = otherSize - otherDegree;
		for (int i = 0; i < sameAdd; i++){
			addAgent(true);
		}
		for (int i = 0; i < otherAdd; i++){
			addAgent(false);
		}
		//System.out.println("same Degree: " + sameDegree + "; other Degree: " + otherDegree);
		
	}
	
	private void addAgent(boolean same){
		GeneralAgent agent = null;
		GeneralAgent[] candidates = null; 
		if (same)
			candidates = availSame.toArray(new GeneralAgent[0]);
		else 
			candidates = availOther.toArray(new GeneralAgent[0]);
		
		if (candidates != null && candidates.length>0){
			//choose candidate with smallest sameDegree, or otherDegree respectively
			int smallestDegree = game.getAllAgents().length;
			for (GeneralAgent a : candidates){
				int testDegree = (same) ? a.getSameDegree() : a.getOtherDegree();
				if (testDegree < smallestDegree){
					agent = a;
					smallestDegree = testDegree;
				}
			}
		}
		if (agent != null){
			network.addEdge(this, agent);
			personalNetwork.add(agent);
			agent.personalNetwork.add(this);
			updateDegree();
			agent.updateDegree();
			if (same)
				availSame.remove(agent);
			else
				availOther.remove(agent);
		}
			
	}
	
	/**
	 * adds agent to this' personalNetwork
	 * adds this to agents's personalNetwork
	 * adds an edge to the network
	 * @param agent to be added
	 * @return true if agent was not null and agent was not already member of this' personalNetwork
	 */
	public void addContact(GeneralAgent agent){
		if (agent != null)
			if (!personalNetwork.contains(agent)){
				network.addEdge(this, agent);
				personalNetwork.add(agent);
				agent.personalNetwork.add(this);
				updateDegree();
				agent.updateDegree();
			}
	}
	
	public void removeContact(GeneralAgent agent){
		if (agent != null)
			if (personalNetwork.contains(agent)){
				network.removeEdge(network.getEdge(this, agent));
				personalNetwork.remove(agent);
				agent.personalNetwork.remove(this);
				updateDegree();
				agent.updateDegree();
			}
	}
	
	/**
	 * removes randomAgent from this' personalNetwork
	 * removes this from randomAgent's personalNetwork
	 * removes edge from network 
	 * @param same: true if agent to be removed should be of same action; false if of different action
	 * @return true if able to remove an agent (personalNetwork.size()>0)
	 */
	public boolean removeRandomContact(boolean same){
		if (personalNetwork.size() > 0){
			List<GeneralAgent> list = new ArrayList<GeneralAgent>();
			for (GeneralAgent agent : personalNetwork){
				if ((agent.cooperating == cooperating) && same)
					list.add(agent);
				if ((agent.cooperating != cooperating) && !same)
					list.add(agent);
			}
			// choose random neighbour
			GeneralAgent randomNeighbour = list.get((int)Math.floor(Math.random() * list.size()));
			// remove tie
			network.removeEdge(network.getEdge(this, randomNeighbour));
			personalNetwork.remove(randomNeighbour);
			randomNeighbour.personalNetwork.remove(this);
			updateDegree();
			randomNeighbour.updateDegree();
			return true;
		}
		return false;
	}
	

	public double ownPayoff(){
		return game.payoff(game.getCoopAgentsSize(), cooperating, degree, sameDegree);
	}
	
	/**
	 * @return the cooperating
	 */
	public boolean isCooperating() {
		return cooperating;
	}
	
	public void setAction(boolean cooperating){
		this.cooperating = cooperating;
		updateDegree();
		for (GeneralAgent agent : this.personalNetwork){
			agent.updateDegree();
		}
		game.updateAgentSets(this);
	}

	private void updateDegree(){
		degree = personalNetwork.size();
		sameDegree = 0;
		otherDegree = 0;
		for (GeneralAgent agent : this.personalNetwork){
			if (agent.cooperating == cooperating)
				sameDegree++;
			else
				otherDegree++;
		}
	}

	/**
	 * @return the degree
	 */
	public int getDegree() {
		return degree;
	}

	/**
	 * @return the personalNetwork
	 */
	public GeneralAgent[] getPersonalNetwork() {
		return personalNetwork.toArray(new GeneralAgent[]{});
	}
	
	public boolean personalNetworkContains(GeneralAgent agent){
		return personalNetwork.contains(agent);
	}

	/**
	 * @return the currentPayoff
	 */
	public double getCurrentPayoff() {
		return ownPayoff();
	}

	/**
	 * @return the sameDegree
	 */
	public int getSameDegree() {
		return sameDegree;
	}

	/**
	 * @return the otherDegree
	 */
	public int getOtherDegree() {
		return otherDegree;
	}



	/**
	 * @return the lastPayoff
	 */
	public double getLastPayoff() {
		return lastPayoff;
	}



	/**
	 * @param lastPayoff the lastPayoff to set
	 */
	public void setLastPayoff(double lastPayoff) {
		this.lastPayoff = lastPayoff;
	}
	
	public void moveFromHighestDegreeStranger(PGSettings settings){
		
		ContinuousSpace<Object> conspace = settings.getConspace();
		GeneralAgent[] allAgents = game.getAllAgents();
		
		NdPoint point = conspace.getLocation(this);
		
		// fetching stranger with highest degree
		int maxStrangerDegree = 0;
		GeneralAgent maxStranger = null;
		for (GeneralAgent stranger : allAgents)
			if (!this.personalNetworkContains(stranger))
				if (stranger.getDegree()>maxStrangerDegree){
					maxStrangerDegree = stranger.getDegree();
					maxStranger = stranger;
				}
		double moveX = 0.0;
		double moveY = 0.0;
		double moveZ = 0.0;
		
		double movingRatio = settings.getMovingRatio();
		
		// getting a little closer towards the chosen neighbour
		if (maxStranger != null){
			NdPoint nPoint = conspace.getLocation(maxStranger);
			double distX = (nPoint.getX() - point.getX());
			moveX = (-1) * distX / movingRatio;
			double distY = (nPoint.getY() - point.getY());
			moveY =  (-1) * distY / movingRatio;
			double distZ = (nPoint.getZ() - point.getZ());
			moveZ =  (-1) * distZ / movingRatio;
		}
		
		conspace.moveByDisplacement(this, moveX, moveY, moveZ);
	}
	
	public void moveToHighestDegreeNeigh(PGSettings settings){
		
		ContinuousSpace<Object> conspace = settings.getConspace();
		
		NdPoint point = conspace.getLocation(this);
		GeneralAgent[] neighbours = this.getPersonalNetwork();
		
		// fetching neighbour with highest degree
		int maxNeighDegree = 0;
		GeneralAgent maxNeighbour = null;
		for (GeneralAgent neighbour : neighbours)
			if (neighbour.getDegree()>maxNeighDegree){
				maxNeighDegree = neighbour.getDegree();
				maxNeighbour = neighbour;
			}
		
		double movingRatio = settings.getMovingRatio();
		
		double moveX = 0.0;
		double moveY = 0.0;
		double moveZ = 0.0;
		// getting a little closer towards the chosen neighbour
		if (maxNeighbour != null){
			NdPoint nPoint = conspace.getLocation(maxNeighbour);
			moveX = (nPoint.getX() - point.getX()) / movingRatio;
			moveY = (nPoint.getY() - point.getY()) / movingRatio;
			moveZ = (nPoint.getZ() - point.getZ()) / movingRatio;
		}
		
		conspace.moveByDisplacement(this, moveX, moveY, moveZ);
	}
}



	
// -------------------------------- Methods for small world distribution - no longer valid / working -----------------------
//	
//	/**
//	 * is called by PGGame after all agents have been created
//	 * @param agents: all agents, including itself
//	 */
//	public void initProbDist(GeneralAgent[] agents){
//		int totalSize = agents.length;
//		probDist = new HashMap<GeneralAgent, Double>();
//		for (GeneralAgent agent : agents)
//			probDist.put(agent, new Double(1/totalSize));
//	}
//	
//	
//	/**
//	 * used for initialisation of the network only
//	 * @param same: specifies randomAgent's action
//	 * @param smallWorld: specifies the degree of small-world-structure that is ought to be build 
//	 * @return random agent from network that is not connected to this yet 
//	 */
//	public GeneralAgent randomAgent(boolean same, double smallWorld){
//		updateProbDist(smallWorld);
//		GeneralAgent agent = null;
//		GeneralAgent[] coopAgents = game.getCoopAgentsAsArray();
//		GeneralAgent[] defAgents = game.getDefAgentsAsArray();
//		Candidate[] candidates = null;
//		if ((same && isCooperating() && coopAgents.length > 1) || (!same && !isCooperating() && coopAgents.length > 0))
//			candidates = candidates(coopAgents);
//		if ((same && !isCooperating() && defAgents.length > 1) || (!same && isCooperating() && defAgents.length > 0))
//			candidates = candidates(defAgents);
//		if (candidates != null && candidates.length>0){
//			double r = Math.random();
//			int index = 0;
//			double sum = candidates[index].getProb();
//			int loopstopper = 0;
//			while (r>=sum && loopstopper++<candidates.length){
//				index++;
//				sum += candidates[index].getProb();
//			}
//			agent = candidates[index].getAgent();
//		}
//		return agent;
//	}
//	
//	/**
//	 * used for initialisation of the network only
//	 * returns an array of Candidate referring to an agent and its probability to be chosen as new contact
//	 * probabilities sum up to 1.0
//	 * @param agents from whom candidates are taken
//	 * @return
//	 */
//	private Candidate[] candidates(GeneralAgent[] agents){
//		Map<GeneralAgent, Double> map = new HashMap<GeneralAgent, Double>();
//		double sum = 0.0;
//		for (int i = 0; i<agents.length; i++){
//			if (!this.personalNetwork.contains(agents[i]) && this != agents[i]){
//				double prob = probDist.get(agents[i]);
//				map.put(agents[i], prob);
//				sum += prob;
//			}
//		}
//		Candidate[] result = new Candidate[0];
//		if (sum > 0.0){
//			result = new Candidate[map.size()];
//			int index = 0;
//			for (Entry<GeneralAgent, Double> entry : map.entrySet()){
//				result[index++]= new Candidate(entry.getKey(), (entry.getValue() / sum));
//			}
//		}
//		return result;
//	}
//	
//	/**
//	 * used for initialisation of the network only
//	 * updates the private Map of probabilities specifying the likelihood of an agent to be new contact of this
//	 * @param smallWorld
//	 */
//	private void updateProbDist(double smallWorld){
//		Set<GeneralAgent> extPersonalNetwork = new HashSet<GeneralAgent>();
//		for (GeneralAgent agent : personalNetwork)
//			extPersonalNetwork.addAll(agent.getPersonalNetwork());
//		for (GeneralAgent agent : personalNetwork)
//			extPersonalNetwork.remove(agent);
//		extPersonalNetwork.remove(this);
//		
//		GeneralAgent[] allAgents = game.getAllAgents();
//		double extPNSize = (double) extPersonalNetwork.size();
//		double totalNetSize = (double) allAgents.length;
//		
//		if (personalNetwork.size() == 0)
//			// initially same probability to connect to any other agent
//			smallWorld = 1.0;
//		else 
//			if (extPNSize == 0) // no contacts with contacts; connect preferably to contacts with no other contacts
//				for (int i = 0; i<allAgents.length; i++){
//					if (allAgents[i].getDegree()==0)
//						extPersonalNetwork.add(allAgents[i]);
//				}
//		extPNSize = extPersonalNetwork.size();
//		for (int i = 0; i<probDist.size(); i++){
//			if (extPersonalNetwork.contains(allAgents[i]))
//				probDist.put(allAgents[i], new Double((1/extPNSize)*(1-smallWorld) + (1/totalNetSize)*smallWorld));
//			else
//				probDist.put(allAgents[i], new Double(smallWorld * (1 / totalNetSize)));
//		}
//	}

///**
// * secondary class used to build the network
// * added for realisation of small world structure
// * 
// * @author Johannes Zschache <johannes.zschache@gmail.com>
// *
// */
//class Candidate{
//	private GeneralAgent agent;
//	private double prob;
//	
//	
//	public Candidate(GeneralAgent agent, double prob) {
//		this.agent = agent;
//		this.prob = prob;
//	}
//	/**
//	 * @return the agent
//	 */
//	public GeneralAgent getAgent() {
//		return agent;
//	}
//	/**
//	 * @param agent the agent to set
//	 */
//	public void setAgent(GeneralAgent agent) {
//		this.agent = agent;
//	}
//	/**
//	 * @return the prob
//	 */
//	public double getProb() {
//		return prob;
//	}
//	/**
//	 * @param prob the prob to set
//	 */
//	public void setProb(double prob) {
//		this.prob = prob;
//	}
//}