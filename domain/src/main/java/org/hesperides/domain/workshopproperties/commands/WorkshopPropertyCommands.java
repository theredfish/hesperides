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

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hesperides.domain.security.User;
import org.hesperides.domain.workshopproperties.CreateWorkshopPropertyCommand;
import org.hesperides.domain.workshopproperties.UpdateWorkshopPropertyCommand;
import org.hesperides.domain.workshopproperties.entities.WorkshopProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkshopPropertyCommands {

    private final CommandGateway commandGateway;

    @Autowired
    public WorkshopPropertyCommands(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public String createWorkshopProperty(WorkshopProperty workshopProperty, User user) {
        return commandGateway.sendAndWait(new CreateWorkshopPropertyCommand(workshopProperty, user));
    }

    public void updateWorkshopProperty(WorkshopProperty workshopProperty, User user) {
        commandGateway.sendAndWait(new UpdateWorkshopPropertyCommand(workshopProperty.getKey(), workshopProperty,
                user));
    }
}
