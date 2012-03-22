package pgnetwork;


import pgnetwork.agent.GeneralAgent;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.Dimensions;
import repast.simphony.space.continuous.ContinuousAdder;
import repast.simphony.space.continuous.ContinuousSpace;
import simphony.util.messages.MessageCenter;

/**
 * (mainly copied from repast.simphony.space.continuous.RandomCartesianAdder)
 * 
 * makes sure that only objects of the class pgnetwork.agent.GeneralAgent are added to the space
 * 
 * @author Johannes Zschache
 * @param <T>
 */
public class PGAgentRandomAdder<T> implements ContinuousAdder<T> {
	
	private static final int TRY_WARN_LIMIT = 10000;
	
	/**
	 * Adds the specified object to the space at a random location.
	 * 
	 * @param space the space to add the object to.
	 * @param obj the object to add.
	 */
	public void add(ContinuousSpace<T> space, T obj) {
		if (GeneralAgent.class.isAssignableFrom(obj.getClass())){
			Dimensions dims = space.getDimensions();
			double[] location = new double[dims.size()];
			int tries = 0;
			findLocation(location, dims);
			while (!space.moveTo(obj, location)) {
				findLocation(location, dims);
				tries++;
				if (tries == TRY_WARN_LIMIT) {
					MessageCenter.getMessageCenter(this.getClass()).warn("Possible hang in filling space '" +
							space.getName() + "': space may be full. Please reduce number of agents added or use a larger grid");
				}
			}
		}
	}

	private void findLocation(double[] location, Dimensions dims) {
		double[] origin = dims.originToDoubleArray(null);
		for (int i = 0; i < location.length; i++) {
			try{
				location[i] = RandomHelper.getUniform().nextDoubleFromTo(0, dims.getDimension(i)) - origin[i];
			}
			catch(Exception e){
			}
		}
	}

}
