package me.arastais.projecte_stages;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import me.arastais.projecte_stages.commands.CommandProjectE;
import moze_intel.projecte.api.event.PlayerAttemptCondenserSetEvent;
import moze_intel.projecte.api.event.PlayerAttemptLearnEvent;
import net.darkhax.bookshelf.registry.RegistryHelper;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ProjectEStages.MODID)
public class ProjectEStages
{
    public static final String MODID = "projecte_stages", NAME = "ProjectE Stages";
    //Logger for the mod (from Bookshelf)
    public static final Logger log = LogManager.getLogger(NAME);
    //public static final Set<Stage> STAGES = new HashSet<Stage>();
    public static final Multimap<String,String> STAGES = HashMultimap.create(); //item (key) is mapped to stages (val)
    public static final TextFormatting NEG_COLOR = TextFormatting.RED, POS_COLOR = TextFormatting.GREEN;
    private static final RegistryHelper REGISTRY = new RegistryHelper(MODID, log);

    public ProjectEStages() {
        // Register ourselves for server and other game events we are interested in
        CommandProjectE.register(REGISTRY);
        REGISTRY.initialize(FMLJavaModLoadingContext.get().getModEventBus());
    	log.info(getText("log", "register_cmd"));
        MinecraftForge.EVENT_BUS.register(this);
    	log.info(getText("log", "register_event"));
    }

    //These ProjectE events are only fired on the server, so a physical side check must be made instead and the server thread has to send the message
    @SubscribeEvent
    public void onPlayerCondenserSetEvent(PlayerAttemptCondenserSetEvent event) {
    	final String itemId = event.getSourceInfo().getItem().getRegistryName().toString();
    	if(STAGES.containsKey(itemId) && !GameStageHelper.hasAnyOf(event.getPlayer(), STAGES.get(itemId))) {
    		event.setCanceled(true);
    		if(FMLEnvironment.dist.equals(Dist.CLIENT)) 
    			event.getPlayer().sendMessage(getIText("message", "deny_condense", NEG_COLOR, ((IFormattableTextComponent)event.getSourceInfo().createStack().getDisplayName()).setStyle(Style.EMPTY.withColor(NEG_COLOR))), Util.NIL_UUID);
    		log.debug(getText("log","canceled_condense", event.getClass().toString(), event.getPlayer().getName(), event.getSourceInfo().createStack().getDisplayName()));
    	}
    }
    
    @SubscribeEvent
    public void onPlayerLearnEvent(PlayerAttemptLearnEvent event) {
    	final String itemId =  event.getSourceInfo().getItem().getRegistryName().toString();
    	if(STAGES.containsKey(itemId) && !GameStageHelper.hasAnyOf(event.getPlayer(), STAGES.get(itemId))) {
    		event.setCanceled(true);
    		if(FMLEnvironment.dist.equals(Dist.CLIENT)) 
    			event.getPlayer().sendMessage(getIText("message","deny_learn", NEG_COLOR, ((IFormattableTextComponent)event.getSourceInfo().createStack().getDisplayName()).setStyle(Style.EMPTY.withColor(NEG_COLOR))).append(getIText("message", "deny_learn_extra", TextFormatting.YELLOW)), Util.NIL_UUID);
    		log.debug(getText("log","canceled_learn", event.getClass().toString(), event.getPlayer().getName(), event.getSourceInfo().createStack().getDisplayName()));
    		//No event for transmutation (for some reason), so emc refund can not be done; stackSize is also not accurate
    		//final long emc = EMCHelper.getEmcValue(event.getStack());
    	}
    }
    //TODO patchouli integration?
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onTooltipEvent(ItemTooltipEvent event) {
    	if(event.getPlayer() == null) return;
    	final String itemId = event.getItemStack().getItem().getRegistryName().toString();
    	boolean locked = STAGES.containsKey(itemId) && !GameStageHelper.hasAnyOf(event.getPlayer(), STAGES.get(itemId));
    	TextFormatting color = locked ? NEG_COLOR : POS_COLOR;
    	if ((event.getFlags().isAdvanced() || Screen.hasShiftDown()) && !event.getItemStack().isEmpty()) {
    		if(STAGES.containsKey(itemId)) {
        		event.getToolTip().add(getIText("tooltip", locked ? "locked" : "unlocked", color).append(locked ? getIText("tooltip", "locked_extra", color, TextFormatting.BOLD) : new StringTextComponent("")));
    			event.getToolTip().add(Screen.hasControlDown() 
    					? getIText("tooltip", locked ? "info_locked" : "info_unlocked", color, STAGES.get(itemId))
    					: getIText("tooltip", "ctrl", TextFormatting.DARK_GRAY, TextFormatting.ITALIC));
    		} 
    		else event.getToolTip().add(getIText("tooltip", "unstaged", color));
    	}
    }

	public static IFormattableTextComponent getIText(String prefix, String suffix, Object... args) {
		Style s = Style.EMPTY;
		List<Object> others = new ArrayList<>();
		for(Object o : args) {
			if(o instanceof TextFormatting) { 
				TextFormatting t = (TextFormatting)o;
				switch(t) {
				case UNDERLINE:
					s = s.setUnderlined(true);
					break;
				case BOLD:
					s = s.withBold(true);
					break;
				case STRIKETHROUGH:
					s = s.setStrikethrough(true);
					break;
				case ITALIC:
					s = s.withItalic(true);
					break;
				case RESET:
					s = Style.EMPTY;
					break;
				default:
					s = s.withColor(t);
					break;
				}
			}
			else others.add(o);
		}
		return new TranslationTextComponent(prefix + "." + MODID + "." + suffix, others.toArray()).setStyle(s);
	}
	//Note: this is unformatted, so it's best for logging
	public static String getText(String prefix, String suffix, Object... args) {
		return getIText(prefix,suffix,args).getString();
	}
}
