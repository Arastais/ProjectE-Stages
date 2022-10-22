package me.arastais.projecte_stages.commands;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.arastais.projecte_stages.ProjectEStages;
import net.darkhax.bookshelf.registry.RegistryHelper;
import net.darkhax.gamestages.command.StageArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.command.arguments.ItemInput;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

///projecte_stages [<stage|item> <stage name|item name>]
public final class CommandProjectE {
	private static final TextFormatting WARN_COLOR = TextFormatting.YELLOW, LIST_COLOR = TextFormatting.GOLD;
	private CommandProjectE() {
		throw new IllegalStateException("Class cannot be instantiated: use CommandProjectE#register(RegistryHelper) instead");
	}
	
	public static void register(RegistryHelper registry) {
		//registry.commands.registerCommandArgument("stage", StageArgumentType.class, StageArgumentType.SERIALIZERS);
		LiteralArgumentBuilder<CommandSource> cmd = Commands.literal(ProjectEStages.MODID);
    	cmd.executes(ctx -> getStageData(ctx, ""));
    	cmd.then(Commands.argument("stage", new StageArgumentType()).executes(ctx -> getStageData(ctx, StageArgumentType.getStage(ctx, "stage"))));
    	cmd.then(Commands.argument("item", ItemArgument.item()).executes(ctx -> getItemData(ctx, ItemArgument.getItem(ctx, "item"))));
    	registry.commands.registerCommand(cmd);
	}
	
	private static int getStageData(CommandContext<CommandSource> ctx, String stageFilter) throws CommandSyntaxException {
		Supplier<Stream<Entry<String, Collection<String>>>> stream = () -> ProjectEStages.STAGES.asMap().entrySet().stream();
		IFormattableTextComponent header = new StringTextComponent("");
		if(!stageFilter.isEmpty()) { 
			stream = () -> ProjectEStages.STAGES.asMap().entrySet().stream().filter(entry -> entry.getValue().contains(stageFilter));
			if(!stream.get().findAny().isPresent()) throw new DynamicCommandExceptionType(filter -> {
				return ProjectEStages.getIText("command", "invalid_stage", WARN_COLOR, filter);
			}).create(stageFilter);
			header = ProjectEStages.getIText("command", "stage_header", stageFilter, LIST_COLOR, TextFormatting.UNDERLINE).append(new StringTextComponent("\n"));
		}
		ctx.getSource().sendSuccess(header.append(
				new StringTextComponent(stream.get().sorted(Map.Entry.comparingByKey()).map(CommandProjectE::createEntryString).collect(Collectors.joining("\n")))
				.setStyle(Style.EMPTY.withColor(LIST_COLOR).setUnderlined(false))), false); //false argument of sendSuccess means don't log the command
		return Command.SINGLE_SUCCESS;
	}
	private static int getItemData(CommandContext<CommandSource> ctx, ItemInput item) throws CommandSyntaxException {
		final String itemId = item.getItem().getRegistryName().toString();
		Supplier<Stream<Entry<String, Collection<String>>>> stream = () -> ProjectEStages.STAGES.asMap().entrySet().stream().filter(entry -> entry.getKey().equals(itemId));
		if(!stream.get().findAny().isPresent()) throw new DynamicCommandExceptionType(filter -> {
			return ProjectEStages.getIText("command", "invalid_item", WARN_COLOR, filter);
		}).create(itemId);
		ctx.getSource().sendSuccess(ProjectEStages.getIText("command", "item_header", itemId, LIST_COLOR, TextFormatting.UNDERLINE)
				.append(new StringTextComponent("\n" + stream.get().sorted(Map.Entry.comparingByKey()).map(CommandProjectE::createEntryString).collect(Collectors.joining("\n")))
				.setStyle(Style.EMPTY.withColor(LIST_COLOR).setUnderlined(false))), false);
		return Command.SINGLE_SUCCESS;
	}
	private static String createEntryString(Entry<String, Collection<String>> entry) {
		return "{" + entry.getKey() + ", " + entry.getValue() + "}";
	}
}
