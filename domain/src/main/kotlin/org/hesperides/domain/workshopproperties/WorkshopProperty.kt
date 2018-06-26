package org.hesperides.domain.workshopproperties

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.domain.security.User
import org.hesperides.domain.security.UserEvent
import org.hesperides.domain.workshopproperties.entities.WorkshopProperty

// Commands
data class CreateWorkshopPropertyCommand(val workshopProperty: WorkshopProperty, val user: User)

data class UpdateWorkshopPropertyCommand(@TargetAggregateIdentifier val key: String,
                                         val workshopProperty: WorkshopProperty,
                                         val user:User)

// Events
data class WorkshopPropertyCreatedEvent(val workshopProperty: WorkshopProperty,
                                        override val user: User) : UserEvent(user)

data class WorkshopPropertyUpdatedEvent(val workshopProperty: WorkshopProperty,
                                        override val user: User) : UserEvent(user)

// Queries
data class GetWorkshopPropertyByKeyQuery(val platformKey: String)