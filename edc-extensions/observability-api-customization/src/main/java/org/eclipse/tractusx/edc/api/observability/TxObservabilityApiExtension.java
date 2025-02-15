/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.tractusx.edc.api.observability;

import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.health.HealthCheckResult;
import org.eclipse.edc.spi.system.health.HealthCheckService;
import org.eclipse.edc.web.spi.WebService;

@Extension(TxObservabilityApiExtension.NAME)
public class TxObservabilityApiExtension implements ServiceExtension {
    public static final String NAME = "Tractus-X Observability API";

    public static final String DEFAULT_ALLOW_INSECURE = "false";
    public static final String OBSERVABILITY_CONTEXT_NAME = "observability";

    @Setting(value = "Allow the Observability API to be accessible without authentication", type = "boolean", defaultValue = DEFAULT_ALLOW_INSECURE)
    public static final String ALLOW_INSECURE_API_SETTING = "tractusx.api.observability.allow-insecure";


    @Inject
    private WebService webService;

    @Inject
    private HealthCheckService healthCheckService;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var controller = new TxObservabilityApiController(healthCheckService);

        // contribute to the liveness probe
        healthCheckService.addReadinessProvider(() -> HealthCheckResult.Builder.newInstance().component(NAME).build());
        healthCheckService.addLivenessProvider(() -> HealthCheckResult.Builder.newInstance().component(NAME).build());

        var contextName = "management";
        if (allowInsecure(context)) {
            contextName = OBSERVABILITY_CONTEXT_NAME;
        }
        webService.registerResource(contextName, controller);
    }


    private boolean allowInsecure(ServiceExtensionContext context) {
        return Boolean.parseBoolean(context.getSetting(ALLOW_INSECURE_API_SETTING, DEFAULT_ALLOW_INSECURE));
    }

}
