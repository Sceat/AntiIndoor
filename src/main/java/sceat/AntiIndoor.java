package sceat;

import fr.aresrpg.commons.domain.condition.Option;
import fr.aresrpg.commons.domain.event.Listener;
import fr.aresrpg.commons.domain.event.Subscribe;
import fr.aresrpg.commons.domain.log.Logger;
import fr.aresrpg.commons.domain.log.LoggerBuilder;
import fr.aresrpg.commons.domain.log.handler.formatters.ErrorFormatter;
import fr.aresrpg.commons.domain.log.handler.formatters.Formatter;
import fr.aresrpg.tofumanchou.domain.Manchou;
import fr.aresrpg.tofumanchou.domain.command.Command;
import fr.aresrpg.tofumanchou.domain.event.player.MapJoinEvent;
import fr.aresrpg.tofumanchou.domain.plugin.ManchouPlugin;
import fr.aresrpg.tofumanchou.domain.util.concurrent.Executors;
import fr.aresrpg.tofumanchou.infra.data.ManchouCell;
import fr.aresrpg.tofumanchou.infra.data.ManchouPerso;

import java.util.concurrent.TimeUnit;

/**
 * 
 * @since
 */
public class AntiIndoor implements ManchouPlugin, Listener {

	public static final Logger LOGGER = new LoggerBuilder("antiblock").setUseConsoleHandler(true, true, Option.<Formatter> none(), Option.<ErrorFormatter> none()).build();
	private static final int merge = -2;
	private final int balking = -merge;
	private int canGoIndoor = balking;

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public int getSubVersion() {
		return 0;
	}

	@Override
	public String getAuthor() {
		return "Sceat";
	}

	@Override
	public String getName() {
		return "AntiIndoor";
	}

	@Override
	public void onEnable() {
		Manchou.registerEvent(this);
		Manchou.registerCommand(new Command() {

			@Override
			public void trigger(final String[] args) {
				canGoIndoor = ((canGoIndoor ^ balking) & 1) == 1 ? canGoIndoor << 1 : canGoIndoor >> 1;
				if ((canGoIndoor ^ (merge ^ (merge >> 31)) - (merge >> 31)) < 1) LOGGER.success("Vous sortirez automatiquement des cavernes et maisons !");
				else LOGGER.success("Vous pouvez à nouveau rentrer dans les cavernes et maisons !");
			}

			@Override
			public String getCmd() {
				return "antiblock";
			}
		});
	}

	@Override
	public void onDisable() {
	}

	@Subscribe
	public void goback(final MapJoinEvent e) {
		if (!((canGoIndoor ^ 2) > ~-1)) return;
		Executors.SCHEDULED.schedule(() -> {
			final ManchouPerso perso = (ManchouPerso) e.getClient().getPerso();
			final ManchouCell cell = perso.getNearestTeleporters()[0];
			perso.moveToCell(cell.getId(), true, true, false);
		} , 1, TimeUnit.SECONDS);
	}

}
