package pgnetwork;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;

/**
 * basically a java bean containing all variables and constants
 * as well as the repast context, network, and the continuousSpace
 * 
 * @author Johannes Zschache
 *
 */
public class PGSettings {
	
	public static final String UPDATE_VIEW_NULL = "null";
	public static final String UPDATE_VIEW_AGENT = "agent";
	public static final String UPDATE_VIEW_STEP = "step";
	
	private Context<Object> context;
	private Network<Object> network;
	private ContinuousSpace<Object> conspace;
	
	private boolean rational; 
	private boolean printOut;
	private String updateView;
	private double movingRatio;
	
	private double pgValue;
	private double bcValue;
	private double cost1;
	
	private int networkSize;
	
	//independent Variables
	private double cost2;
	private double initDensity;
	private double initSegregation;
	private double initCoopRate;
	
	//learning rates
	private double learning1;
	private double learning2;
	
	
	public Context<Object> getContext() {
		return context;
	}
	public Network<Object> getNetwork() {
		return network;
	}
	public ContinuousSpace<Object> getConspace() {
		return conspace;
	}
	public boolean isRational() {
		return rational;
	}
	public boolean isPrintOut() {
		return printOut;
	}
	public double getPgValue() {
		return pgValue;
	}
	public double getBcValue() {
		return bcValue;
	}
	public double getCost1() {
		return cost1;
	}
	public int getNetworkSize() {
		return networkSize;
	}
	public double getCost2() {
		return cost2;
	}
	public double getInitDensity() {
		return initDensity;
	}
	public double getInitSegregation() {
		return initSegregation;
	}
	public double getInitCoopRate() {
		return initCoopRate;
	}
	public double getLearning1() {
		return learning1;
	}
	public double getLearning2() {
		return learning2;
	}
	public String getUpdateView() {
		return updateView;
	}
	public double getMovingRatio() {
		return movingRatio;
	}
	public void setMovingRatio(double movingRatio) {
		this.movingRatio = movingRatio;
	}
	public void setUpdateView(String updateView) {
		this.updateView = updateView;
	}
	public void setContext(Context<Object> context) {
		this.context = context;
	}
	public void setNetwork(Network<Object> network) {
		this.network = network;
	}
	public void setConspace(ContinuousSpace<Object> conspace) {
		this.conspace = conspace;
	}
	public void setRational(boolean rational) {
		this.rational = rational;
	}
	public void setPrintOut(boolean printOut) {
		this.printOut = printOut;
	}
	public void setPgValue(double pgValue) {
		this.pgValue = pgValue;
	}
	public void setBcValue(double bcValue) {
		this.bcValue = bcValue;
	}
	public void setCost1(double cost1) {
		this.cost1 = cost1;
	}
	public void setNetworkSize(int networkSize) {
		this.networkSize = networkSize;
	}
	public void setCost2(double cost2) {
		this.cost2 = cost2;
	}
	public void setInitDensity(double initDensity) {
		this.initDensity = initDensity;
	}
	public void setInitSegregation(double initSegregation) {
		this.initSegregation = initSegregation;
	}
	public void setInitCoopRate(double initCoopRate) {
		this.initCoopRate = initCoopRate;
	}
	public void setLearning1(double learning1) {
		this.learning1 = learning1;
	}
	public void setLearning2(double learning2) {
		this.learning2 = learning2;
	}

}