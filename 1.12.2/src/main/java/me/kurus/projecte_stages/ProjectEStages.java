package me.kurus.projecte_stages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import me.kurus.projecte_stages.commands.CommandProjectE;
import moze_intel.projecte.api.event.PlayerAttemptCondenserSetEvent;
import moze_intel.projecte.api.event.PlayerAttemptLearnEvent;
import net.darkhax.bookshelf.lib.LoggingHelper;
import net.darkhax.bookshelf.util.StackUtils;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.GameStages;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

@Mod(modid = ProjectEStages.MODID, name = ProjectEStages.NAME, version = ProjectEStages.VERSION, 
dependencies = "required-after:projecte;required-after:bookshelf;required-after:gamestages@[2.0.89,);required-after:crafttweaker")
public class ProjectEStages
{
    public static final String MODID = "projecte_stages";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VERSION@";
    
    public static final Set<Stage> STAGES = new HashSet<Stage>();//, EXCLUSIONS = new HashSet<Stage>();
    public static final TextFormatting NEG_COLOR = TextFormatting.RED, POS_COLOR = TextFormatting.GREEN;
    
    //The logger for the mod
    public static final LoggingHelper log = new LoggingHelper("ProjectE Stages");

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
    	log.info(getText("log", "register_event"));
    	GameStages.COMMAND.addSubcommand(new CommandProjectE());
    	log.info(getText("log", "register_cmd"));
    }
    
    @SubscribeEvent
    public void onPlayerCondenserSetEvent(PlayerAttemptCondenserSetEvent event) {
    	final String itemId = StackUtils.getStackIdentifier(event.getStack());
    	final int meta = event.getStack().getMetadata();
    	if(getMatchingStage(itemId, meta).isPresent() && !GameStageHelper.hasAnyOf(event.getPlayer(), getMatchingStage(itemId, meta).orElse(new Stage()).stageNames)) {
    		event.setCanceled(true);
    		if(!event.getPlayer().getEntityWorld().isRemote) 
    			event.getPlayer().sendMessage(getIText("message", "deny_condense", NEG_COLOR, event.getStack().getDisplayName()));
    		log.debug(getText("log","canceled_condense", event.getClass().toString(), event.getPlayer().getName(), event.getStack().getDisplayName()));
    		log.info(StackUtils.findVariations(event.getStack().getItem()).toString());
    	}
    }
    
    @SubscribeEvent
    public void onPlayerLearnEvent(PlayerAttemptLearnEvent event) {
    	final String itemId = StackUtils.getStackIdentifier(event.getStack());
    	final int meta = event.getStack().getMetadata();
    	if(getMatchingStage(itemId, meta).isPresent() && !GameStageHelper.hasAnyOf(event.getPlayer(), getMatchingStage(itemId, meta).orElse(new Stage()).stageNames)) {
    		event.setCanceled(true);
    		if(!event.getPlayer().getEntityWorld().isRemote) 
    			event.getPlayer().sendMessage(getIText("message","deny_learn", NEG_COLOR, event.getStack().getDisplayName()).appendSibling(getIText("message", "deny_learn_extra", TextFormatting.YELLOW)));
    		log.debug(getText("log","canceled_learn", event.getClass().toString(), event.getPlayer().getName(), event.getStack().getDisplayName()));
    		//No event for transmutation (for some reason), so emc refund can not be done; stackSize is also not accurate
    		//final long emc = EMCHelper.getEmcValue(event.getStack());
    	}
    }
    //TODO patchouli integration?
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onTooltipEvent(ItemTooltipEvent event) {
    	if(event.getEntityPlayer() == null) return;
    	final String itemId = StackUtils.getStackIdentifier(event.getItemStack());
    	final int meta = event.getItemStack().getMetadata();
    	boolean locked = getMatchingStage(itemId, meta).isPresent() && !GameStageHelper.hasAnyOf(event.getEntityPlayer(), getMatchingStage(itemId, meta).orElse(new Stage()).stageNames);
    	TextFormatting color = locked ? NEG_COLOR : POS_COLOR;
    	if ((event.getFlags().isAdvanced() || GuiScreen.isShiftKeyDown()) && !event.getItemStack().isEmpty()) {
    		if(getMatchingStage(itemId, meta).isPresent()) {
        		event.getToolTip().add(getText("tooltip", locked ? "locked" : "unlocked", color) + (locked ? getText("tooltip", "locked_extra", color, TextFormatting.BOLD) : ""));
    			event.getToolTip().add(GuiScreen.isCtrlKeyDown() 
    					? getText("tooltip", locked ? "info_locked" : "info_unlocked", color, getMatchingStage(itemId, meta).orElse(new Stage()).stageNames)
    					: getText("tooltip", "ctrl", TextFormatting.DARK_GRAY, TextFormatting.ITALIC));
    		} 
    		else event.getToolTip().add(getText("tooltip", "unstaged", color));
    	}
    }

	public static final ITextComponent getIText(String prefix, String suffix, Object... args) {
		Style s = new Style();
		List<Object> others = new ArrayList<>();
		for(Object o : args) {
			if(o instanceof TextFormatting) { 
				TextFormatting t = (TextFormatting)o;
				switch(t) {
				case UNDERLINE:
					s.setUnderlined(true);
					break;
				case BOLD:
					s.setBold(true);
					break;
				case STRIKETHROUGH:
					s.setStrikethrough(true);
					break;
				case ITALIC:
					s.setItalic(true);
					break;
				case RESET:
					s = new Style();
					break;
				default:
					s.setColor(t);
					break;
				}
			}
			else others.add(o);
		}
		return new TextComponentTranslation(prefix + "." + MODID + "." + suffix, others.toArray()).setStyle(s);
	}
	public static final String getText(String prefix, String suffix, Object... args) {
		return getIText(prefix,suffix,args).getFormattedText();
	}

	public static Optional<Stage> getMatchingStage(String itemName, int metadata) {
    	return STAGES.stream().filter(o -> o.itemCodeName.equals(itemName) && (o.metadata == OreDictionary.WILDCARD_VALUE || o.metadata == metadata)).findFirst();
    }
	/***@deprecated Unreliable as metadata isn't explicit. Use {@link ProjectEStages#getMatchingStage(String, int)} instead*/
	@Deprecated
	public static Optional<Stage> getMatchingStage(String itemName) {
		return getMatchingStage(itemName, OreDictionary.WILDCARD_VALUE);
	}
}
