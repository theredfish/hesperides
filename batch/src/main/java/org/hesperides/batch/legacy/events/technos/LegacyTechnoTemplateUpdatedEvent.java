package org.hesperides.batch.legacy.events.technos;

import com.google.gson.annotations.SerializedName;
import org.hesperides.batch.legacy.entities.LegacyTemplate;
import org.hesperides.batch.legacy.events.LegacyInterface;
import org.hesperides.domain.security.User;
import org.hesperides.domain.technos.TechnoTemplateUpdatedEvent;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

public class LegacyTechnoTemplateUpdatedEvent implements LegacyInterface {
    @SerializedName("updated")
    LegacyTemplate legacyTemplate;

    @Override
    public TemplateContainer.Key getKey() {
        return legacyTemplate.getKeyFromNamespace();
    }

    @Override
    public Object toDomainEvent(User user) {
        TemplateContainer.Key key = getKey();
        return new TechnoTemplateUpdatedEvent(key, legacyTemplate.toDomainTemplate(key), user);
    }
}
