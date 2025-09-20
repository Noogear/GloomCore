package gloomcore.contract;

import java.util.UUID;

public interface Context<U> {

    UUID id();

    U user();

}
