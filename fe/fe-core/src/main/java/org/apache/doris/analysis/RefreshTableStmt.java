// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.analysis;

import org.apache.doris.catalog.Catalog;
import org.apache.doris.common.AnalysisException;
import org.apache.doris.common.ErrorCode;
import org.apache.doris.common.ErrorReport;
import org.apache.doris.common.UserException;
import org.apache.doris.common.util.Util;
import org.apache.doris.mysql.privilege.PrivPredicate;
import org.apache.doris.qe.ConnectContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RefreshTableStmt extends DdlStmt {
    private static final Logger LOG = LogManager.getLogger(RefreshTableStmt.class);

    private TableName tableName;

    public RefreshTableStmt(TableName tableName) {
        this.tableName = tableName;
    }

    public String getDbName() {
        return tableName.getDb();
    }

    public String getTblName() {
        return tableName.getTbl();
    }

    public TableName getTableName() {
        return tableName;
    }

    @Override
    public void analyze(Analyzer analyzer) throws AnalysisException, UserException {
        super.analyze(analyzer);
        tableName.analyze(analyzer);
        // disallow external catalog
        Util.prohibitExternalCatalog(tableName.getCtl(), this.getClass().getSimpleName());

        // check access
        if (!Catalog.getCurrentCatalog().getAuth().checkTblPriv(ConnectContext.get(), tableName.getDb(),
                tableName.getTbl(), PrivPredicate.DROP)) {
            ErrorReport.reportAnalysisException(ErrorCode.ERR_SPECIFIC_ACCESS_DENIED_ERROR, "DROP");
        }

        if (!Catalog.getCurrentCatalog().getAuth().checkTblPriv(ConnectContext.get(), tableName.getDb(),
                tableName.getTbl(), PrivPredicate.CREATE)) {
            ErrorReport.reportAnalysisException(ErrorCode.ERR_SPECIFIC_ACCESS_DENIED_ERROR, "CREATE");
        }
    }

    @Override
    public String toSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("REFRESH TABLE ").append(tableName.toSql());
        return sb.toString();
    }

    @Override
    public String toString() {
        return toSql();
    }
}
