/*
 * Copyright (c) 2008-2019 Haulmont.
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
 */

package com.haulmont.cuba.core.sys.environmentcheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * System-level class for environment sanity checks.
 */
public class EnvironmentChecks {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentChecks.class);
    protected List<EnvironmentCheck> checks;

    public EnvironmentChecks() {
        addDefaultChecks();
    }

    public EnvironmentChecks(List<EnvironmentCheck> checks) {
        this.checks = checks;
    }

    public List<EnvironmentCheck> getChecks() {
        return checks;
    }

    public void setChecks(List<EnvironmentCheck> checks) {
        this.checks = checks;
    }

    public void addCheck(EnvironmentCheck check) {
        if (checks == null)
            checks = new ArrayList<>();
        checks.add(check);
    }

    /**
     * Run all environment sanity checks.
     *
     * @return list of failed checks results, empty list if all checks completed successfully
     */
    public List<CheckFailedResult> runChecks() {
        if (checks == null)
            return Collections.emptyList();
        List<CheckFailedResult> results = new ArrayList<>();
        for (EnvironmentCheck check : checks) {
            results.addAll(check.doCheck());
        }
        if (!results.isEmpty()) {
            StringBuilder resultMessage = new StringBuilder();
            resultMessage.append("\n=================================================================" +
                    "\nSome of environment sanity checks failed:");
            for (CheckFailedResult result : results) {
                resultMessage.append("\n");
                resultMessage.append(result.getMessage());
            }
            resultMessage.append("\n=================================================================");
            log.warn(resultMessage.toString());
        } else {
            log.info("Environment checks completed successfully");
        }
        return results;
    }

    protected void addDefaultChecks() {
        addCheck(new JvmCheck());
        addCheck(new DirectoriesCheck());
        addCheck(new DataStoresCheck());
    }
}
