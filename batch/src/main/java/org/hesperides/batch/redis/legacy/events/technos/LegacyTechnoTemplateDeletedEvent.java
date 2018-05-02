package org.hesperides.batch.redis.legacy.events.technos;

import org.hesperides.batch.redis.legacy.events.LegacyInterface;
import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

public class LegacyTechnoTemplateDeletedEvent implements LegacyInterface {
    @Override
    public TemplateContainer.Key getKey() {
        return null;
    }

    @Override
    public Object toDomainEvent(User user) {
        return null;
    }
}
