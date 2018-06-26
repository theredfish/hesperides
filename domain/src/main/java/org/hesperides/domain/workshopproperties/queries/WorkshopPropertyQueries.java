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
package org.hesperides.domain.workshopproperties.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.domain.framework.Queries;
import org.hesperides.domain.workshopproperties.GetWorkshopPropertyByKeyQuery;
import org.hesperides.domain.workshopproperties.queries.views.WorkshopPropertyView;

import java.util.Optional;

public class WorkshopPropertyQueries extends Queries {

    protected WorkshopPropertyQueries(QueryGateway queryGateway) {
        super(queryGateway);
    }

    /**
     * Check if a workshop property exists in the database for the given key
     * @param workshopPropertyKey
     * @return true if the workshop property view exists for the given key, else false
     */
    public boolean workshopPropertyExists(String workshopPropertyKey) {
        Optional<WorkshopPropertyView> workshopPropertyView = querySyncOptional(new GetWorkshopPropertyByKeyQuery(workshopPropertyKey), WorkshopPropertyView.class);
        return workshopPropertyView.isPresent();
    }

    /**
     * Get the view for a given workshop property key
     * @param workshopPropertyKey
     * @return a workshop property view
     */
    public Optional<WorkshopPropertyView> getOptionalWorkshopProperty(String workshopPropertyKey) {
        return querySyncOptional(new GetWorkshopPropertyByKeyQuery(workshopPropertyKey), WorkshopPropertyView.class);
    }
}
