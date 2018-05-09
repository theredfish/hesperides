package org.hesperides.batch.redis.legacy.events;

import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

public interface LegacyInterface {

//    String ToDomainEvent(User user);
    TemplateContainer.Key getKey();
    Object toDomainEvent(User user);
    String getKeyString();
}
