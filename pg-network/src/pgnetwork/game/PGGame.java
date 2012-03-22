package pgnetwork.game;

import java.util.HashSet;
import java.util.Set;

import pgnetwork.PGSettings;
import pgnetwork.agent.GeneralAgent;
import pgnetwork.agent.LearningAgent;
import pgnetwork.agent.RationalAgent;

import cern.jet.random.Normal;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

/**
 * responsible for initialisation of agents and network
 * calculates game properties
 * 
 * @author Johannes Zschache
 *
 */
public abstract class PGGame {
	
	private GeneralAgent[] allAgents;
	private Set<GeneralAgent> coopAgents;
	private Set<GeneralAgent> defAgents;
	
	private Context<Object> context;
	private Network<Object> network;
	
	//game properties
	private final double pgValue;
	private final double cost1;
	private final double bcValue;
	public final double cost2;
	
	//epsilon
	public static final double epsilon = 0.000001;
	
	private double startDensity = 0.0;
	private double startSegregation = 0.0;
	private double startCoopRate = 0.0;
	
	protected PGGame(PGSettings settings){
		this.context = settings.getContext();
		this.network = settings.getNetwork();
		this.pgValue = settings.getPgValue();
		this.cost1 = settings.getCost1();
		this.bcValue = settings.getBcValue();
		this.cost2 = settings.getCost2();
	}
	
	protected void initAgents(int total, double coop, boolean rational, boolean print){
		allAgents = new GeneralAgent[total];
		coopAgents = new HashSet<GeneralAgent>();
		defAgents = new HashSet<GeneralAgent>();
		Set<GeneralAgent> allAgentsSet = new HashSet<GeneralAgent>();
		double averageCoop = (double)total * coop;
		Normal normal = RandomHelper.createNormal(averageCoop, 1.0);
		int coopSize = (int)Math.round(normal.nextDouble());
		int defSize = total - coopSize;
		for (int i = 0;i<coopSize;i++){
			GeneralAgent agent = null;
			if (rational)
				agent = new RationalAgent(true, network, (RationalPGGame)this, print);
			else
				agent = new LearningAgent(true, network, (LearningPGGame)this, print);
			coopAgents.add(agent);
			allAgentsSet.add(agent);
			context.add(agent);
		}
		for (int i = 0;i<defSize;i++){
			GeneralAgent agent = null;
			if (rational)
				agent = new RationalAgent(false, network, (RationalPGGame)this, print);
			else
				agent = new LearningAgent(false, network, (LearningPGGame)this, print);
			defAgents.add(agent);
			allAgentsSet.add(agent);
			context.add(agent);
		}
		this.allAgents = allAgentsSet.toArray(new GeneralAgent[0]);
		startCoopRate = (double)coopSize / (double)total; 
	}
	
	
	protected void initNetwork(double density, double segregation){
		int size = allAgents.length;
		double avgNeighSize = size * density;
		Normal normal = RandomHelper.createNormal(avgNeighSize, 1.0);
		
		Set<GeneralAgent> coopAgents = new HashSet<GeneralAgent>();
		Set<GeneralAgent> defAgents = new HashSet<GeneralAgent>();
		for (GeneralAgent agent : this.coopAgents)
			coopAgents.add(agent);
		for (GeneralAgent agent : this.defAgents)
			defAgents.add(agent);
		
		Set<GeneralAgent> first = this.defAgents;
		Set<GeneralAgent> second = this.coopAgents;
		if (this.getCoopAgentsSize()<this.getDefAgentsSize()){
			first = this.coopAgents;
			second = this.defAgents;
		}
		for (GeneralAgent agent : first){
			agent.setAvailAgents(coopAgents, defAgents);
			int neighSize = (int)Math.round(normal.nextDouble());
			agent.initPersonalNetwork(neighSize, segregation);
			coopAgents.remove(agent);
			defAgents.remove(agent);
		}
		for (GeneralAgent agent : second){
			agent.setAvailAgents(coopAgents, defAgents);
			int neighSize = (int)Math.round(normal.nextDouble());
			agent.initPersonalNetwork(neighSize, segregation);
			coopAgents.remove(agent);
			defAgents.remove(agent);
		}
		startDensity = getDensity();
		startSegregation = getSegregation();

	}
	
	public void deleteAllAgents() {
		Iterable<RepastEdge<Object>> iter =  network.getEdges();
		Set<RepastEdge<Object>> remove = new HashSet<RepastEdge<Object>>();
		for (RepastEdge<Object> edge : iter)
			remove.add(edge);
		for (RepastEdge<Object> edge : remove)
			network.removeEdge(edge);
		for (GeneralAgent agent : allAgents){
			context.remove(agent);
		}
	}
	
	public abstract void step();
	
	public abstract boolean terminate();
	
	public abstract int maxGames(); 
	
	public double getDensity(){
		double count = 0.0;
		for (GeneralAgent agent : allAgents){
			count += (double)agent.getDegree() / (double)allAgents.length;
		}
		return (double)count / (double)allAgents.length;
	}
	public double getSegregation(){
		int count = 0;
		int sameCount = 0;
		for (GeneralAgent agent : allAgents){
			count += agent.getDegree();
			sameCount += agent.getSameDegree();
		}
		return (count != 0) ? ((double)sameCount / (double)count) : 0.0;
	}
	
	public double payoff(int coopSize, boolean cooperating, double degree, double samedegree){
		double publicGood = pgValue * coopSize;
		double contrCost = ((cooperating) ? 1.0 : 0.0) * cost1;
		double behCon = bcValue * (samedegree - (degree - samedegree));
		return publicGood - contrCost + behCon;
	}
	
	public void updateAgentSets(GeneralAgent agent){
		if (agent.isCooperating()){
			defAgents.remove(agent);
			coopAgents.add(agent);
		} else {
			coopAgents.remove(agent);
			defAgents.add(agent);
		}
	}
	/**
	 * @return the allAgents
	 */
	public GeneralAgent[] getAllAgents() {
		return allAgents;
	}

	/**
	 * @return the coopAgents
	 */
	public GeneralAgent[] getCoopAgentsAsArray() {
		return coopAgents.toArray(new GeneralAgent[0]);
	}
	
	public int getCoopAgentsSize(){
		return coopAgents.size();
	}

	/**
	 * @return the defAgents
	 */
	public GeneralAgent[] getDefAgentsAsArray() {
		return defAgents.toArray(new GeneralAgent[0]);
	}
	
	public int getDefAgentsSize(){
		return defAgents.size();
	}

	/**
	 * @return the startDensity
	 */
	public double getStartDensity() {
		return startDensity;
	}

	/**
	 * @return the startSegregation
	 */
	public double getStartSegregation() {
		return startSegregation;
	}

	/**
	 * @return the startCoopRate
	 */
	public double getStartCoopRate() {
		return startCoopRate;
	}
	
	
}
