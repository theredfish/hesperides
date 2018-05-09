package org.hesperides.batch.redis.legacy.entities;

import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

public class LegacyTechno {

    String name;
    String version;
    Boolean working_copy;

    public Techno toDomainInstance() {
        return new Techno(new TemplateContainer.Key(name, version, working_copy ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release), null);
    }
}
