/**
 * Copyright (c) 2017 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package io.pravega.common.health.processor;

import io.pravega.common.health.HealthReporter;
import io.pravega.common.health.NoSuchHealthCommand;
import io.pravega.common.health.NoSuchHealthProcessor;
import java.io.IOException;
import java.io.OutputStream;

public class NullRequestProcessor extends HealthRequestProcessor {

    @Override
    public void registerHealthProcessor(String id, HealthReporter reporter) {

    }

    @Override
    public void processHealthRequest(OutputStream writer, String target, String cmd) throws IOException, NoSuchHealthProcessor, NoSuchHealthCommand {

    }
}