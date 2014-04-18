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
package org.superbiz.deltaspike;

import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.jsf.api.listener.phase.JsfPhaseListener;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import java.util.logging.Logger;

@Exclude(exceptIfProjectStage = {ProjectStage.Development.class, CustomProjectStage.Debugging.class})

@JsfPhaseListener
public class DebugPhaseListener implements PhaseListener
{
    private static final long serialVersionUID = 5899542118538949019L;

    private Logger logger = Logger.getLogger(Logger.class.getName());

    public void beforePhase(PhaseEvent phaseEvent)
    {
        this.logger.info("before " + phaseEvent.getPhaseId());
    }

    public void afterPhase(PhaseEvent phaseEvent)
    {
        this.logger.info("after " + phaseEvent.getPhaseId());
    }

    public PhaseId getPhaseId()
    {
        return PhaseId.ANY_PHASE;
    }
}
