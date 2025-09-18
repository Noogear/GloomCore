package gloomcore.paper.adventure;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public interface MiniTag<C> {

    TagResolver get(final C c);

}
