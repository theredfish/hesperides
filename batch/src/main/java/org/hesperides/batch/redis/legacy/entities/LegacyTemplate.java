package org.hesperides.batch.redis.legacy.entities;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

@Value
public class LegacyTemplate {
    String name;
    String filename;
    String location;
    String content;
    String namespace;
    Template.Rights rights;

    @SerializedName("version_id")
    Long versionId;

    public Template toDomainTemplate(TemplateContainer.Key key) {
        if (rights == null) {
            Template.Rights rights = new Template.Rights(new Template.FileRights(null, null, null), new Template.FileRights(null, null, null), new Template.FileRights(null, null, null));
            return new Template(name, filename, location, content, rights, versionId, key);

        }
        return new Template(name, filename, location, content, rights, versionId, key);
    }

    public TemplateContainer.Key getKeyFromNamespace() {
        String[] temp = namespace.split("#");
        TemplateContainer.Key key = new TemplateContainer.Key(temp[1], temp[2],
                "WORKINGCOPY".equals(temp[3]) ? TemplateContainer.VersionType.workingcopy : TemplateContainer.VersionType.release);
        return key;
    }
}