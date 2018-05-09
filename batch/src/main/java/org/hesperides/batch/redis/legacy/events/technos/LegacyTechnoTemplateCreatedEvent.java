package org.hesperides.batch.redis.legacy.events.technos;

import com.google.gson.annotations.SerializedName;
import org.hesperides.batch.redis.legacy.entities.LegacyTemplate;
import org.hesperides.batch.redis.legacy.events.LegacyInterface;
import org.hesperides.domain.security.User;
import org.hesperides.domain.technos.TemplateAddedToTechnoEvent;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

public class LegacyTechnoTemplateCreatedEvent implements LegacyInterface {

    @SerializedName("created")
    LegacyTemplate legacyTemplate;

    @Override
    public TemplateContainer.Key getKey() {
        return legacyTemplate.getKeyFromNamespace();
    }

    @Override
    public Object toDomainEvent(User user) {
        TemplateContainer.Key key = getKey();
        return new TemplateAddedToTechnoEvent(key,legacyTemplate.toDomainTemplate(key),user);

    }

    @Override
    public String getKeyString() {
        return getKey().toString("techno");
    }
}
