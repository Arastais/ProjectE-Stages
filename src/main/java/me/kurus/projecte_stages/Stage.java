package me.kurus.projecte_stages;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.oredict.OreDictionary;
import scala.actors.threadpool.Arrays;

public class Stage {
	public String itemCodeName;
	public Set<String> stageNames = new HashSet<String>();
	public int metadata;
	public Stage() {
		this.itemCodeName = "N/A";
		this.metadata = OreDictionary.WILDCARD_VALUE;
	}
	public Stage(String itemName, String stage) {
		this(itemName, OreDictionary.WILDCARD_VALUE, stage);
	}
	@SuppressWarnings("unchecked")
	public Stage(String itemName, int metadata, String... stages) {
		this.itemCodeName = itemName;
		this.metadata = metadata;
		this.stageNames.addAll(Arrays.asList(stages));
	}
	
	@Override
	public boolean equals(Object other) {
	    if (this == other) return true;
	    if (other == null || getClass() != other.getClass()) return false;
	    Stage otherStage = (Stage)other;
	    return itemCodeName.equals(otherStage.itemCodeName) && metadata == otherStage.metadata;
	}
	@Override
	public int hashCode() {
		final int prime = 5;
		int result = 3;
		result = prime * result + (itemCodeName == null ? 0 : itemCodeName.hashCode());
		result = prime * result + metadata;
		return result;
	}
	@Override
	public String toString() {
		return "{" + stageNames + ", " + itemCodeName + "$" + (metadata == OreDictionary.WILDCARD_VALUE ? "*" : String.valueOf(metadata)) + "}";
	}
}
