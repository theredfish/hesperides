package org.hesperides.batch.redis.legacy.entities;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.hesperides.domain.templatecontainer.entities.Template;

@Value
public class LegacyTemplate {
    String name;
    String filename;
    String location;
    String content;
    Template.Rights rights;

    @SerializedName("version_id")
    Long versionId;
}