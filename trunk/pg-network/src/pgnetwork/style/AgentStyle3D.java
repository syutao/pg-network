package pgnetwork.style;

import java.awt.Color;
import java.awt.Font;

import javax.media.j3d.Shape3D;

import pgnetwork.agent.GeneralAgent;

import repast.simphony.visualization.visualization3D.AppearanceFactory;
import repast.simphony.visualization.visualization3D.ShapeFactory;
import repast.simphony.visualization.visualization3D.style.Style3D;
import repast.simphony.visualization.visualization3D.style.TaggedAppearance;
import repast.simphony.visualization.visualization3D.style.TaggedBranchGroup;

public class AgentStyle3D implements Style3D<GeneralAgent> {

	@Override
	public TaggedAppearance getAppearance(GeneralAgent obj, TaggedAppearance appearance, Object shapeID) {
		if (appearance == null) {
			appearance = new TaggedAppearance();
		}
		if (obj.isCooperating())
			AppearanceFactory.setMaterialAppearance(appearance.getAppearance(), Color.BLUE);
		else
			AppearanceFactory.setMaterialAppearance(appearance.getAppearance(), Color.RED);
		
		return appearance;
	}

	@Override
	public TaggedBranchGroup getBranchGroup(GeneralAgent obj, TaggedBranchGroup group) {
		if (group == null || group.getTag() == null){
			group = new TaggedBranchGroup("DEFAULT");
			Shape3D sphere = ShapeFactory.createSphere(0.07f, "DEFAULT");
			sphere.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
			group.getBranchGroup().addChild(sphere);
			return group;
		}
		return null;
	}

	@Override
	public String getLabel(GeneralAgent obj, String currentLabel) {
		return null;
	}

	@Override
	public Color getLabelColor(GeneralAgent obj, Color currentColor) {
		return Color.YELLOW;
	}

	@Override
	public Font getLabelFont(GeneralAgent obj, Font currentFont) {
		return null;
	}

	@Override
	public float getLabelOffset(GeneralAgent obj) {
		return 0.35f;
	}

	@Override
	public repast.simphony.visualization.visualization3D.style.Style3D.LabelPosition getLabelPosition(GeneralAgent obj,repast.simphony.visualization.visualization3D.style.Style3D.LabelPosition curentPosition) {
		return LabelPosition.NORTH;
	}

	@Override
	public float[] getRotation(GeneralAgent obj) {
		return null;
	}

	@Override
	public float[] getScale(GeneralAgent obj) {
		return null;
	}
	
	

}
