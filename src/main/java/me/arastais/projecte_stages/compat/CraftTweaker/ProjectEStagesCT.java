package me.arastais.projecte_stages.compat.CraftTweaker;

import crafttweaker.annotations.ZenRegister;
import me.arastais.projecte_stages.ProjectEStages;
import me.arastais.projecte_stages.Stage;
import net.darkhax.bookshelf.util.ModUtils;
import net.darkhax.bookshelf.util.RegistryUtils;
import net.darkhax.bookshelf.util.StackUtils;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.ProjectEStages")
public class ProjectEStagesCT {
    @ZenMethod
    public static void stageItem(String stage, String item) {
    	stageItem(stage, item, OreDictionary.WILDCARD_VALUE);
    }
    
    @ZenMethod
    public static void stageItem(String stage, String item, int metadata) {
    	boolean stageReplace = false;
    	Item regItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(item));
    	if(regItem == null) ProjectEStages.log.warn(ProjectEStages.getText("log", "failed_stage", item));
    	else {
    		String itemId = StackUtils.getStackIdentifier(new ItemStack(regItem, 1, metadata));
    		if(metadata == OreDictionary.WILDCARD_VALUE) { //staging a wildcard value, so get rid of non-wildcard value rules
    			for(ItemStack iStack : StackUtils.findVariations(regItem))
    				if(ProjectEStages.STAGES.remove(new Stage(itemId, iStack.getMetadata(), stage))) { //remove all non-wildcard value rules
    					ProjectEStages.log.debug(ProjectEStages.getText("log", "unstage", item, itemId, iStack.getMetadata(), stage)); //log it
    	    			stageReplace = true;
    				}
    		}
    		if(!ProjectEStages.STAGES.contains(new Stage(itemId, OreDictionary.WILDCARD_VALUE, stage))) { //if there isnt a wildcard rule for the item,
    			ProjectEStages.STAGES.add(new Stage(itemId, metadata, stage)); //add it,
        		ProjectEStages.log.info(ProjectEStages.getText("log", "stage", item, itemId, (metadata == OreDictionary.WILDCARD_VALUE ? "*" : metadata), stage) +
        				(stageReplace ? " " + ProjectEStages.getText("log", "stage_replace") : "")); //and log it
    		}
    		else ProjectEStages.log.warn(ProjectEStages.getText("log", "wildcard_exists", item, itemId, (metadata == OreDictionary.WILDCARD_VALUE ? "*" : metadata))); //wildcard rule already exists
    	}
    }
    @ZenMethod
    public static void stageODItem(String stage, String item) {
    	for (final ItemStack iStack : OreDictionary.getOres(item))
            ProjectEStages.STAGES.add(new Stage(StackUtils.getStackIdentifier(iStack), stage));
    }
    
    @ZenMethod
    public static void stageAllModItems(String stage, String modid) {
    	for (final Item item : ModUtils.getSortedEntries(ForgeRegistries.ITEMS).get(modid)) {
            if (item != null && item != Items.AIR) {
                ProjectEStages.STAGES.add(new Stage(StackUtils.getStackIdentifier(new ItemStack(item)), stage));
            }
        }
    }
    @ZenMethod
    public static void unstageItem(String stage, String item) {
    	unstageItem(stage, item, OreDictionary.WILDCARD_VALUE);
    }
    
    @ZenMethod
    public static void unstageItem(String stage, String item, int metadata) {
    	Item regItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(item));
    	if(regItem == null) ProjectEStages.log.warn(ProjectEStages.getText("log", "failed_unstage", item));
    	else { 
    		String itemId = RegistryUtils.getRegistryId(regItem);
    		NonNullList<ItemStack> vars = StackUtils.findVariations(regItem);
    		ProjectEStages.log.debug(ProjectEStages.getText("log", "list_vars", vars.toString(), itemId));
    		//TODO simplify this logic
    		if(metadata == OreDictionary.WILDCARD_VALUE) { //remove ALL variants of the item
    			for(ItemStack iStack : vars)
    				if(ProjectEStages.STAGES.remove(new Stage(StackUtils.getStackIdentifier(iStack), iStack.getMetadata(), stage))) //remove each variant of the item
    					ProjectEStages.log.debug(ProjectEStages.getText("log", "unstage", item, StackUtils.getStackIdentifier(iStack), iStack.getMetadata(), stage)); //log it
    			if(ProjectEStages.STAGES.remove(new Stage(itemId, metadata, stage))) //remove the wildcard rule
    				ProjectEStages.log.debug(ProjectEStages.getText("log", "unstage", item, itemId, "*", stage)); //log it
    			ProjectEStages.log.info(ProjectEStages.getText("log", "unstage_all", item, itemId, stage));
    			return;
    		}
    		if (ProjectEStages.STAGES.contains(new Stage(itemId, OreDictionary.WILDCARD_VALUE, stage))) { //wildcard metadata is staged
    			ProjectEStages.STAGES.remove(new Stage(itemId, OreDictionary.WILDCARD_VALUE, stage)); //remove wildcard rule
    			ProjectEStages.log.debug(ProjectEStages.getText("log", "unstage", item,itemId, "*", stage)); //log removing wildcard rule
    			for(ItemStack iStack : vars) 
    				if(iStack.getMetadata() != metadata)
    					if(ProjectEStages.STAGES.add(new Stage(StackUtils.getStackIdentifier(iStack), iStack.getMetadata(), stage))) //add back rule for each variant that doesn't have the chosen metadata
    						ProjectEStages.log.debug(ProjectEStages.getText("log", "stage", item, StackUtils.getStackIdentifier(iStack), iStack.getMetadata(), stage)); //log adding back
        		ProjectEStages.log.info(ProjectEStages.getText("log", "unstage", item, itemId, metadata, stage) + " " + ProjectEStages.getText("log", "unstage_replace"));
        		return;
    		}
    		ProjectEStages.STAGES.remove(new Stage(itemId, metadata, stage)); //regular remove
    		ProjectEStages.log.info(ProjectEStages.getText("log", "unstage", item,itemId, metadata, stage));
    	}
    }
}
