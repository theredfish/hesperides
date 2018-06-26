package org.hesperides.presentation.io;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.workshopproperties.queries.views.WorkshopPropertyView;

@Value
public class WorkshopPropertyOutput {
    String key;
    String value;
    String keyValue;

    public static WorkshopPropertyOutput fromWorkshopPropertyView(WorkshopPropertyView workshopPropertyView) {
        return new WorkshopPropertyOutput(
                workshopPropertyView.getKey(),
                workshopPropertyView.getValue(),
                workshopPropertyView.getKeyValue()
        );
    }
}
