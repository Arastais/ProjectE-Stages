package me.arastais.projecte_stages.commands;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.arastais.projecte_stages.ProjectEStages;
import me.arastais.projecte_stages.Stage;
import net.darkhax.bookshelf.command.Command;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandProjectE extends Command {
	private static final TextFormatting CMD_COLOR = TextFormatting.YELLOW, WARN_COLOR = TextFormatting.YELLOW, LIST_COLOR = TextFormatting.GOLD;
	
	@Override
	public String getName() {
		return "projecte";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/gamestage projecte [<stage|item> <stage name|item name>]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		ITextComponent msg = null;
		switch(args.length) {
		case 0:
			msg = new TextComponentString(ProjectEStages.STAGES.stream().map(Stage::toString).sorted().collect(Collectors.joining("\n"))).setStyle(new Style().setColor(LIST_COLOR));
			break;
		case 2:
			switch(args[0]) {
			case "stage":
				Supplier<Stream<Stage>> sStages = () -> ProjectEStages.STAGES.stream().filter(st -> st.stageNames.contains(args[1]));
				if(sStages.get().findAny().isPresent()) msg = ProjectEStages.getIText("command", "stage_header", args[1], LIST_COLOR, TextFormatting.UNDERLINE).appendSibling(
						new TextComponentString("\n" + sStages.get().map(Stage::toString).sorted().collect(Collectors.joining("\n"))).setStyle(new Style().setUnderlined(false)));
				else msg = ProjectEStages.getIText("command", "invalid_stage", WARN_COLOR, args[1]);
				break;
			case "item":
				Supplier<Stream<Stage>> iStages = () -> ProjectEStages.STAGES.stream().filter(st -> st.itemCodeName.equals(new ResourceLocation(args[1]).toString()));
				if(iStages.get().findAny().isPresent()) msg = ProjectEStages.getIText("command", "item_header", args[1], LIST_COLOR, TextFormatting.UNDERLINE).appendSibling(
						new TextComponentString("\n" + iStages.get().map(Stage::toString).sorted().collect(Collectors.joining("\n"))).setStyle(new Style().setUnderlined(false)));
				else msg = ProjectEStages.getIText("command", "invalid_item", WARN_COLOR, args[1]);
				break;
			default:
				msg = ProjectEStages.getIText("command", "invalid_arg", ProjectEStages.NEG_COLOR).appendSibling(ProjectEStages.getIText("command", "usage", getUsage(sender),CMD_COLOR));
				break;
			}
			break;
		default:
			msg = ProjectEStages.getIText("command", "num_args", ProjectEStages.NEG_COLOR).appendSibling(ProjectEStages.getIText("command", "usage", getUsage(sender),CMD_COLOR));
			break;
		}
		sender.sendMessage(msg);
	}
    @Override
    public int getRequiredPermissionLevel () {
        return 0;
    }
}
