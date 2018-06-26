package org.hesperides.domain.workshopproperties.entities;

import lombok.Value;

@Value
public class WorkshopProperty {
    String key;
    String value;
    String keyValue;

    public WorkshopProperty concatKeyValue() {
        return new WorkshopProperty(
                this.key,
                this.value,
                this.key+this.value
        );
    }
}
