package org.hesperides.batch.redis.legacy.events.technos;

import org.hesperides.batch.redis.legacy.events.LegacyInterface;
import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

public class LegacyTechnoDeletedEvent implements LegacyInterface {

    @Override
    public TemplateContainer.Key getKey() {
        return null;
    }

    @Override
    public Object toDomainEvent(User user) {
        return null;
    }

    @Override
    public String getKeyString() {
        return getKey().toString("techno");
    }
}
