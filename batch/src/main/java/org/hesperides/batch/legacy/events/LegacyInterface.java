package org.hesperides.batch.legacy.events;

import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

public interface LegacyInterface {
    TemplateContainer.Key getKey();
    Object toDomainEvent(User user);
}
