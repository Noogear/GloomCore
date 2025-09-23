package gloomcore.paper.contract;

import net.kyori.adventure.audience.Audience;

public interface Context<U extends Audience> {

    U audience();

}
