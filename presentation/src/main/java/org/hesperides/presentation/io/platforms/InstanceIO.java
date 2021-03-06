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
package org.hesperides.presentation.io.platforms;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.domain.platforms.entities.Instance;
import org.hesperides.domain.platforms.queries.views.InstanceView;
import org.hesperides.presentation.io.platforms.properties.ValuedPropertyIO;

import java.util.List;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class InstanceIO {

    String name;
    @SerializedName("key_values")
    List<ValuedPropertyIO> valuedProperties;

    public InstanceIO(InstanceView instanceView) {
        this.name = instanceView.getName();
        this.valuedProperties = ValuedPropertyIO.fromPropertyViews(instanceView.getValuedProperties());
    }

    public Instance toDomainInstance() {
        return new Instance(name, ValuedPropertyIO.toDomainInstances(valuedProperties));
    }

    public static List<Instance> toDomainInstances(List<InstanceIO> instanceIOS) {
        List<Instance> instances = null;
        if (instanceIOS != null) {
            instances = instanceIOS.stream().map(InstanceIO::toDomainInstance).collect(Collectors.toList());
        }
        return instances;
    }

    public static List<InstanceIO> fromInstanceViews(List<InstanceView> instanceViews) {
        List<InstanceIO> instanceIOS = null;
        if (instanceViews != null) {
            instanceIOS = instanceViews.stream().map(InstanceIO::new).collect(Collectors.toList());
        }
        return instanceIOS;
    }
}
