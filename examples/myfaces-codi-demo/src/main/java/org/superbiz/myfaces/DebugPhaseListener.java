/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.superbiz.myfaces;

import org.apache.myfaces.extensions.cdi.core.api.Advanced;
import org.apache.myfaces.extensions.cdi.core.api.activation.ProjectStageActivated;
import org.apache.myfaces.extensions.cdi.core.api.logging.Logger;
import org.apache.myfaces.extensions.cdi.core.api.projectstage.ProjectStage;
import org.apache.myfaces.extensions.cdi.jsf.api.listener.phase.JsfPhaseListener;

import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.inject.Inject;

@ProjectStageActivated({ProjectStage.Development.class, CustomProjectStage.Debugging.class})

@Advanced
@JsfPhaseListener
public class DebugPhaseListener implements PhaseListener {

    private static final long serialVersionUID = 5899542118538949019L;

    @Inject
    private Logger logger;

    public void beforePhase(PhaseEvent phaseEvent) {
        this.logger.info("before " + phaseEvent.getPhaseId());
    }

    public void afterPhase(PhaseEvent phaseEvent) {
        this.logger.info("after " + phaseEvent.getPhaseId());
    }

    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }
}
