package org.hesperides.batch.redis.legacy.events.technos;

import org.hesperides.batch.redis.legacy.events.LegacyInterface;
import org.hesperides.domain.security.User;
import org.hesperides.domain.technos.TechnoTemplateDeletedEvent;
import org.hesperides.domain.technos.TemplateDeletedEvent;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

public class LegacyTechnoTemplateDeletedEvent implements LegacyInterface {

    String namespace;
    String name;
    Long versionID;

    @Override
    public TemplateContainer.Key getKey() {

        String[] temp = namespace.split("#");
        return new TemplateContainer.Key(temp[1], temp[2],
                "WORKINGCOPY".equals(temp[3]) ? TemplateContainer.VersionType.workingcopy : TemplateContainer.VersionType.release);
    }

    @Override
    public Object toDomainEvent(User user) {

        return new TechnoTemplateDeletedEvent(getKey(), name, user);
    }
}
