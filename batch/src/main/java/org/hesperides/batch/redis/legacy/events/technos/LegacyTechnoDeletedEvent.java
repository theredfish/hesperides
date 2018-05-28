package org.hesperides.batch.redis.legacy.events.technos;

import org.hesperides.batch.redis.legacy.events.LegacyInterface;
import org.hesperides.domain.security.User;
import org.hesperides.domain.technos.TechnoDeletedEvent;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

public class LegacyTechnoDeletedEvent implements LegacyInterface {

    String packageName;
    String packageVersion;
    Boolean workingCopy;

    @Override
    public TemplateContainer.Key getKey() {

        return new TemplateContainer.Key(
                packageName,
                packageVersion,
                workingCopy ? TemplateContainer.VersionType.workingcopy : TemplateContainer.VersionType.release);

    }

    @Override
    public Object toDomainEvent(User user) {
        return new TechnoDeletedEvent(getKey(),user);
    }

    @Override
    public String getKeyString() {
        return getKey().toString("techno");
    }
}
