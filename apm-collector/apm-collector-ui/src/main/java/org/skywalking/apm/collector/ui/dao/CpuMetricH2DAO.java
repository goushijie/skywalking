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

package org.skywalking.apm.collector.ui.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.skywalking.apm.collector.client.h2.H2Client;
import org.skywalking.apm.collector.client.h2.H2ClientException;
import org.skywalking.apm.collector.core.util.Const;
import org.skywalking.apm.collector.core.util.TimeBucketUtils;
import org.skywalking.apm.collector.storage.define.jvm.CpuMetricTable;
import org.skywalking.apm.collector.storage.h2.SqlBuilder;
import org.skywalking.apm.collector.storage.h2.dao.H2DAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;

/**
 * @author pengys5, clevertension
 */
public class CpuMetricH2DAO extends H2DAO implements ICpuMetricDAO {
    private final Logger logger = LoggerFactory.getLogger(InstanceH2DAO.class);
    private static final String GET_CPU_METRIC_SQL = "select * from {0} where {1} = ?";
    private static final String GET_CPU_METRICS_SQL = "select * from {0} where {1} in (";
    @Override public int getMetric(int instanceId, long timeBucket) {
        String id = timeBucket + Const.ID_SPLIT + instanceId;
        H2Client client = getClient();
        String sql = SqlBuilder.buildSql(GET_CPU_METRIC_SQL, CpuMetricTable.TABLE, "id");
        Object[] params = new Object[]{id};
        try (ResultSet rs = client.executeQuery(sql, params)) {
            if (rs.next()) {
                return rs.getInt(CpuMetricTable.COLUMN_USAGE_PERCENT);
            }
        } catch (SQLException | H2ClientException e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }

    @Override public JsonArray getMetric(int instanceId, long startTimeBucket, long endTimeBucket) {
        H2Client client = getClient();
        String sql = SqlBuilder.buildSql(GET_CPU_METRICS_SQL, CpuMetricTable.TABLE, "id");

        long timeBucket = startTimeBucket;
        List<String> idList = new ArrayList<>();
        do {
            timeBucket = TimeBucketUtils.INSTANCE.addSecondForSecondTimeBucket(TimeBucketUtils.TimeBucketType.SECOND.name(), timeBucket, 1);
            String id = timeBucket + Const.ID_SPLIT + instanceId;
            idList.add(id);
        }
        while (timeBucket <= endTimeBucket);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < idList.size(); i++) {
            builder.append("?,");
        }
        builder.delete(builder.length() - 1, builder.length());
        builder.append(")");
        sql = sql + builder;
        Object[] params = idList.toArray(new String[0]);

        JsonArray metrics = new JsonArray();
        try (ResultSet rs = client.executeQuery(sql, params)) {
            while (rs.next()) {
                double cpuUsed = rs.getDouble(CpuMetricTable.COLUMN_USAGE_PERCENT);
                metrics.add((int)(cpuUsed * 100));
            }
        } catch (SQLException | H2ClientException e) {
            logger.error(e.getMessage(), e);
        }
        return metrics;
    }
}
