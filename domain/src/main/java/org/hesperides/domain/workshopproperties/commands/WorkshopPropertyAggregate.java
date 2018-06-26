/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.domain.workshopproperties.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.domain.workshopproperties.CreateWorkshopPropertyCommand;
import org.hesperides.domain.workshopproperties.UpdateWorkshopPropertyCommand;
import org.hesperides.domain.workshopproperties.WorkshopPropertyCreatedEvent;
import org.hesperides.domain.workshopproperties.WorkshopPropertyUpdatedEvent;
import org.hesperides.domain.workshopproperties.entities.WorkshopProperty;

import java.io.Serializable;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Slf4j
@Aggregate
@NoArgsConstructor
public class WorkshopPropertyAggregate implements Serializable {

    @AggregateIdentifier
    private String key;

    @CommandHandler
    public WorkshopPropertyAggregate(CreateWorkshopPropertyCommand command) {
        WorkshopProperty workshopProperty = command.getWorkshopProperty().concatKeyValue();

        apply(new WorkshopPropertyCreatedEvent(workshopProperty, command.getUser()));

        log.debug("Workshop property create command dispatched");
    }

    @CommandHandler
    public void updateWorkshopProperty(UpdateWorkshopPropertyCommand command) {
        WorkshopProperty workshopProperty = command.getWorkshopProperty().concatKeyValue();

        apply(new WorkshopPropertyUpdatedEvent(workshopProperty, command.getUser()));

        log.debug("Workshop property update command dispatched");
    }

    @EventSourcingHandler
    public void onCreatedEvent(WorkshopPropertyCreatedEvent event) {
        this.key = event.getWorkshopProperty().getKey();

        log.debug("workshop property created");
    }

    @EventSourcingHandler
    public void onUpdatedEvent(WorkshopPropertyUpdatedEvent event) {
         log.debug("Workshop property updated");
    }
}
