package gloomcore.paper.contract;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

public interface PlayerContext extends Context<Player> {

    TagResolver[] tagResolvers();
}
