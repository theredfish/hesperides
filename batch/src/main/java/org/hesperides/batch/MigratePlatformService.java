package org.hesperides.batch;

import java.util.HashMap;

public class MigratePlatformService extends MigrateAbstractService {
    static {
        //TODO : être sur de cet aggregat
        AGGREGATE_TYPE = "PlatformAggregate";
        PATTERN = "platform";
        LEGACY_EVENTS_DICTIONARY = new HashMap<>();


    }
}
