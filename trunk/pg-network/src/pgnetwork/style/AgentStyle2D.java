package pgnetwork.style;

import java.awt.Color;

import pgnetwork.agent.GeneralAgent;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;

public class AgentStyle2D extends DefaultStyleOGL2D {
	
	@Override
	public java.awt.Color getColor(java.lang.Object obj){
	
		if (obj instanceof GeneralAgent){
			if (((GeneralAgent) obj).isCooperating())
				return Color.BLUE;
			else
				return Color.RED;
		}
		else
			return null;
	}

}
