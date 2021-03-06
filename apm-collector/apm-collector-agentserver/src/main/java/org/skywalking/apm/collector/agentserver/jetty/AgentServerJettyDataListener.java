/*
 * Copyright 2017, OpenSkywalking Organization All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project repository: https://github.com/OpenSkywalking/skywalking
 */

package org.skywalking.apm.collector.agentserver.jetty;

import org.skywalking.apm.collector.agentserver.AgentServerModuleGroupDefine;
import org.skywalking.apm.collector.cluster.ClusterModuleDefine;
import org.skywalking.apm.collector.core.cluster.ClusterDataListener;

/**
 * @author pengys5
 */
public class AgentServerJettyDataListener extends ClusterDataListener {

    @Override public String path() {
        return ClusterModuleDefine.BASE_CATALOG + "." + AgentServerModuleGroupDefine.GROUP_NAME + "." + AgentServerJettyModuleDefine.MODULE_NAME;
    }

    @Override public void serverJoinNotify(String serverAddress) {

    }

    @Override public void serverQuitNotify(String serverAddress) {

    }
}
