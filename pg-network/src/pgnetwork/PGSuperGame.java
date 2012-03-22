package pgnetwork;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;

import pgnetwork.agent.GeneralAgent;
import pgnetwork.game.LearningPGGame;
import pgnetwork.game.PGGame;
import pgnetwork.game.RationalPGGame;
import pgnetwork.jdbc.DataManager;

import repast.simphony.annotate.AgentAnnot;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduledMethod;

/**
 * 
 * The main class controlling the simulation cycle and the data collection
 * 
 * @author Johannes Zschache
 *
 */
@AgentAnnot (displayName="Public Good Super Game")
public class PGSuperGame {
	
	private PGSettings settings;
	private PGGame game;
	
	//independent Variables
	private double initDensity;
	private double initSegregation;
	private double initCoopRate;
	private double cost2;
	private double learning1;
	private double learning2;
	
	// corrected independent Variables
	private double startCoopRate;
	private double startDensity;
	private double startSegregation;
	
	//dependent variables
	private double coopRate;
	private double density;
	private double segregation;
	
	
	private DataManager dataManager;
	private ISchedule schedule;
	private int currentGame;
	
	public PGSuperGame(PGSettings settings){
		this.settings = settings;
		
		this.cost2 = settings.getCost2();
		this.initDensity = settings.getInitDensity();
		this.initSegregation = settings.getInitSegregation();
		this.initCoopRate = settings.getInitCoopRate();
		
		this.learning1 = settings.getLearning1();
		this.learning2 = settings.getLearning2();
		
		currentGame = 1;
		
		initNewGame();
		
		if (RunEnvironment.getInstance().isBatch()){
			dataManager = DataManager.getInstance();
			dataManager.openConnection();
			dataManager.newSweep(settings.isRational(), cost2, initDensity, initSegregation, initCoopRate, learning1, learning2);
			schedule = RunEnvironment.getInstance().getCurrentSchedule();
		}
	}
	
	private void initNewGame(){
		
		settings.setMovingRatio(2.0);
		
		if (game != null)
			game.deleteAllAgents();
		if (settings.isRational()){
			game = new RationalPGGame(settings);
		} else {
			game = new LearningPGGame(settings);
		}
		
		startCoopRate = game.getStartCoopRate();
		startDensity = game.getStartDensity();
		startSegregation = game.getStartSegregation();
		
		if (!settings.getUpdateView().equals(PGSettings.UPDATE_VIEW_NULL))
			updateConspace();
		
		if (settings.isPrintOut()){
			System.out.println("new game");
			System.out.println("cost2:" + cost2);
			System.out.println("density: " + initDensity + "/" + startDensity);
			System.out.println("segregation: " + initSegregation + "/" + startSegregation);
			System.out.println("coopRate: " + initCoopRate + "/" + startCoopRate);
		}
	}
	
	@ScheduledMethod(start=1, interval=1)
	public void step(){

		if (settings.isPrintOut()){
			System.out.println("step");
		}
		
		game.step();
			
		updateDepVars();
		printResults();
		if (settings.getUpdateView().equals(PGSettings.UPDATE_VIEW_STEP)){
			updateConspace();
		}
		
		if (game.terminate()){
			
			if (RunEnvironment.getInstance().isBatch()){
				dataManager.save(startDensity, startSegregation, startCoopRate, coopRate, density, segregation);
			}
			
			currentGame++;
			
			if (currentGame > game.maxGames()){
				schedule.setFinishing(true);
				dataManager.closeConnection();
			}
			else { // new game with same parameters
				initNewGame();
			}
		}
	}
	

	private void updateDepVars(){
		if (settings.isRational())
			coopRate = (double)game.getCoopAgentsSize() / (double)settings.getNetworkSize();
		else
			coopRate = ((LearningPGGame)game).getAvgCoopRate();
		density = game.getDensity();
		segregation = game.getSegregation();
	}
	
	private void printResults(){
		if (settings.isPrintOut()){
			System.out.println("coopRate: " + coopRate);
			System.out.println("density: " + density);
			System.out.println("segregation: " + segregation);
		}
	}
	
	private void updateConspace(){
		
		if (settings.getUpdateView().equals(PGSettings.UPDATE_VIEW_STEP)){
			
			settings.setMovingRatio(settings.getMovingRatio() * 1.5);
			
			GeneralAgent[] allAgents = game.getAllAgents();
			
			//sort descending by degree
			Arrays.sort(allAgents, new AgentComparator(false));
			for (GeneralAgent agent : allAgents)
				agent.moveFromHighestDegreeStranger(settings);
			
			//sort ascending by degree
			Arrays.sort(allAgents, new AgentComparator(true));
			for (GeneralAgent agent : allAgents){
				agent.moveToHighestDegreeNeigh(settings);
			}
		}
	}
	
	

	// -------------- independent Variables: getters used by repast view --------- //
	
	DecimalFormat df = new DecimalFormat("###.###");
	public double getInitDensity(){
		return Double.parseDouble(df.format(initDensity).replace(",", "."));
	}
	public double getInitSegregation(){
		return Double.parseDouble(df.format(initSegregation).replace(",", "."));
	}
	public double getInitCoopRate(){
		return Double.parseDouble(df.format(initCoopRate).replace(",", "."));
	}
	public double getStartDensity(){
		return startDensity;
	}
	public double getStartSegregation(){
		return startSegregation;
	}
	public double getStartCoopRate(){
		return startCoopRate;
	}
	public double getCost2(){
		return cost2;
	}
	public double getLearning1(){
		return learning1;
	}
	public double getLearning2(){
		return learning2;
	}
	
	public double getDensity() {
		return density;
	}
	public double getSegregation() {
		return segregation;
	}
	public double getCoopRate() {
		return coopRate;
	}
	
}

class AgentComparator implements Comparator<GeneralAgent> {

	private boolean ascending = true;
	
	public AgentComparator(boolean ascending){
		this.ascending = ascending;
	}
	
	@Override
	public int compare(GeneralAgent o1, GeneralAgent o2) {
		int degree1 = o1.getDegree();
		int degree2 = o2.getDegree();
		if (degree1 == degree2)
			return 0;
		else
			return (((o1.getDegree()>o2.getDegree())? 1 : -1) * ((ascending) ? 1 : -1));
	}
}

