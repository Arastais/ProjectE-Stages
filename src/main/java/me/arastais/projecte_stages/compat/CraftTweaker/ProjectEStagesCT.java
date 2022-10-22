package me.arastais.projecte_stages.compat.CraftTweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;

import me.arastais.projecte_stages.ProjectEStages;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.projecte_stages.Items")
public class ProjectEStagesCT {
	@ZenCodeType.Method
    public static void stageItem(String item, String... stages) {
    	final Item regItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(item));
    	if(regItem == null) ProjectEStages.log.warn(ProjectEStages.getText("log", "failed_stage", item, new ResourceLocation(item).toString()));
    	final List<String> stageList = Arrays.asList(stages);
    	final String itemId = regItem.getRegistryName().toString();
    	if(ProjectEStages.STAGES.putAll(itemId, stageList))
    		ProjectEStages.log.info(ProjectEStages.getText("log", "stage", item, itemId, stageList));
    	else ProjectEStages.log.warn(ProjectEStages.getText("log", "stage_exists", item, itemId, stageList));
    }
	@ZenCodeType.Method
    public static void stageTagItem(String item, String... stages) {
    	final List<String> stageList = Arrays.asList(stages);
    	for (final Item tagItem : ItemTags.getAllTags().getTag(new ResourceLocation(item)).getValues())
            if(ProjectEStages.STAGES.putAll(tagItem.getRegistryName().toString(), stageList))
				ProjectEStages.log.info(ProjectEStages.getText("log", "stage", item, tagItem.getRegistryName().toString(), stageList));
    }
    
	//BUG: reports that it stages successfully, which it does, then wrongly reports that it couldn't stage it.
	@ZenCodeType.Method
    public static void stageAllModItems(String modid, String... stages) {
    	final List<String> stageList = Arrays.asList(stages);
    	for (final Item item : ForgeRegistries.ITEMS.getValues().stream().filter(i -> i.getRegistryName().getNamespace().equals(modid)).collect(Collectors.toList())) {
            if (item != Items.AIR && ProjectEStages.STAGES.putAll(item.getRegistryName().toString(), stageList))
            	ProjectEStages.log.info(ProjectEStages.getText("log", "stage", item.getRegistryName().getPath(), item.getRegistryName().toString(), stageList));
            else ProjectEStages.log.warn(ProjectEStages.getText("log", "failed_stage", item, "mod '" + modid + "'"));
        }
    }
	@ZenCodeType.Method
	public static void unstageItem(String item) {
    	final Item regItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(item));
    	if(regItem == null) ProjectEStages.log.warn(ProjectEStages.getText("log", "failed_unstage", item));
    	final String itemId = regItem.getRegistryName().toString();
    	if(!ProjectEStages.STAGES.removeAll(itemId).isEmpty())
    		ProjectEStages.log.info(ProjectEStages.getText("log", "unstage_all", item, itemId));
    	else ProjectEStages.log.warn(ProjectEStages.getText("log", "unstage_none", item, itemId));
	}
	@ZenCodeType.Method
    public static void unstageItem(String item, String... stages) {
    	final List<String> stageList = Arrays.asList(stages);
    	final Item regItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(item));
    	if(regItem == null) ProjectEStages.log.warn(ProjectEStages.getText("log", "failed_unstage", item));
    	final String itemId = regItem.getRegistryName().toString();
    	if(ProjectEStages.STAGES.containsKey(itemId)) {
    		for(String stage : stageList) {
    			if(ProjectEStages.STAGES.remove(itemId, stage))
    				ProjectEStages.log.info(ProjectEStages.getText("log", "unstage", item, itemId, stage));
    			else ProjectEStages.log.warn(ProjectEStages.getText("log", "unstage_missing", item, itemId, stage));
    		}
    	}
    	else ProjectEStages.log.warn(ProjectEStages.getText("log", "unstage_none", item, itemId));
    }
}
