package pgnetwork;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;

/**
 * The interface between the simulation model and the repast interface
 * 
 * @author Johannes Zschache
 *
 */
public class PGContextBuilder implements ContextBuilder<Object> {
	
	@Override
	public Context<Object> build(Context<Object> context){
		
		context.setId("pgnetwork");
		
		RandomHelper.setSeed((int)System.currentTimeMillis());
		
		Network<Object> network = new NetworkBuilder<Object>("network", context, false).buildNetwork();
		double[] size = { 100, 100, 100 };
		ContinuousSpace<Object> conspace = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
			.createContinuousSpace("conspace", context, new PGAgentRandomAdder<Object>(),
						new repast.simphony.space.continuous.StickyBorders(), size);
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		PGSettings settings = new PGSettings();
		settings.setContext(context);
		settings.setNetwork(network);
		settings.setConspace(conspace);
		
		settings.setRational(((String)params.getValue("agentType")).equals("forward"));
		settings.setPrintOut((Boolean) params.getValue("print"));
		settings.setUpdateView((String)params.getValue("updateView"));
		
		settings.setPgValue((Double) params.getValue("pgValue"));
		settings.setBcValue((Double) params.getValue("bcValue"));
		settings.setCost1((Double) params.getValue("cost1"));
		settings.setCost2((Double) params.getValue("cost2"));
		
		settings.setNetworkSize((Integer) params.getValue("networksize"));
		settings.setInitCoopRate((Double) params.getValue("initialCooperation"));
		settings.setInitDensity((Double) params.getValue("initialDensity"));
		settings.setInitSegregation((Double) params.getValue("initialSegregation"));
		
		settings.setLearning1((Double) params.getValue("learning1"));
		settings.setLearning2((Double) params.getValue("learning2"));
		
		context.add(new PGSuperGame(settings));
		
		return context;
	}
}
