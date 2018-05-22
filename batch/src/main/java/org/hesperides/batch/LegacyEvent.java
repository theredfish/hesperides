package org.hesperides.batch;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LegacyEvent {
    String eventType;
    String data;
    Long timestamp;
    String user;

}
