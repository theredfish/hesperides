package org.hesperides.batch.redis;

import lombok.Value;

@Value
public class LegacyModuleDeletedEvent implements LegacyInterface{
    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleDeletedEvent";

    String moduleName;
    String moduleVersion;
    boolean workingCopy;
}
