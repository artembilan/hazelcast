/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.map;

import com.hazelcast.spi.Invocation;
import com.hazelcast.spi.NodeEngine;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.hazelcast.map.MapService.MAP_SERVICE_NAME;

public class MapRecordTask implements Runnable {

    NodeEngine nodeEngine;
    MapRecordStateOperation operation;
    int partitionId;


    public MapRecordTask(NodeEngine nodeEngine, MapRecordStateOperation operation, int partitionId) {
        this.nodeEngine = nodeEngine;
        this.operation = operation;
        this.partitionId = partitionId;
    }

    public void run() {
        try {
            Invocation invocation = nodeEngine.getOperationService().createInvocationBuilder(MAP_SERVICE_NAME, operation, partitionId)
                    .build();
            Future invoke = invocation.invoke();
            invoke.get();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}