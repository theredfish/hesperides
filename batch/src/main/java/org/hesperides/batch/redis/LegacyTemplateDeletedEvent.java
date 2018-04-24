package org.hesperides.batch.redis;

import lombok.Value;

@Value
public class LegacyTemplateDeletedEvent implements LegacyInterface{
    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleTemplateDeletedEvent";

    String moduleName;
    String moduleVersion;
    String templateName;
}
