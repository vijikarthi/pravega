/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.emc.pravega.common.util;

import com.emc.pravega.common.Exceptions;
import com.google.common.base.Preconditions;

public final class Retry {

    private Retry() {}
    
    public static RetryWithBackoff withExpBackoff(long initialMillies, int multiplier, int attempts) {
        return new RetryWithBackoff(initialMillies, multiplier, attempts);
    }
    
    public static final class RetryWithBackoff {
        private final long initialMillies;
        private final int multiplier;
        private final int attempts;

        private RetryWithBackoff(long initialMillies, int multiplier, int attempts) {
            Preconditions.checkArgument(initialMillies >= 1);
            Preconditions.checkArgument(multiplier >= 1);
            Preconditions.checkArgument(attempts >= 1);
            this.initialMillies = initialMillies;
            this.multiplier = multiplier;
            this.attempts = attempts;
        }
        
        public <RetryT extends Exception> RetringOnException<RetryT> retryingOn(Class<RetryT> retryType) {
            return new RetringOnException<>(retryType, this);
        }
        
    }
    
    public static final class RetringOnException<RetryT extends Exception> {
        private final Class<RetryT> retryType;
        private final RetryWithBackoff params;

        RetringOnException(Class<RetryT> retryType, RetryWithBackoff params) {
            this.retryType = retryType;
            this.params = params;
        }
        
        public <ThrowsT extends Exception> ThrowingOnException<RetryT, ThrowsT> throwingOn(Class<ThrowsT> throwType) {
            return new ThrowingOnException<>(retryType, throwType, params);
        }
    }
    
    @FunctionalInterface
    public interface Retryable<ReturnT, RetryableET extends Exception, NonRetryableET extends Exception> {
        ReturnT attempt() throws RetryableET, NonRetryableET;
    }
    
    public static final class ThrowingOnException<RetryT extends Exception, ThrowsT extends Exception> {
        private final Class<RetryT> retryType;
        private final Class<ThrowsT> throwType;
        private final RetryWithBackoff params;
        
        ThrowingOnException(Class<RetryT> retryType, Class<ThrowsT> throwType, RetryWithBackoff params) {
            this.retryType = retryType;
            this.throwType = throwType;
            this.params = params;
        }
        
        @SuppressWarnings("unchecked")
        public <ReturnT> ReturnT run(Retryable<ReturnT, RetryT, ThrowsT> r) throws ThrowsT {
            long delay = params.initialMillies;
            Exception last = null;
            for (int attemptNumber = 1; attemptNumber <= params.attempts; attemptNumber++) {
                try {
                    return r.attempt();
                } catch (Exception e) {
                    Class<? extends Exception> type = e.getClass();
                    if (retryType.isAssignableFrom(type)) {
                        last = e;
                    } else if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else {
                        throw (ThrowsT) e;
                    }
                }
                
                final long sleepFor = delay;
                Exceptions.handleInterupted(() -> Thread.sleep(sleepFor));
 
                delay *= params.multiplier;
            }
            throw new RetriesExaustedException(last);
        }
    }
    
    
    
    




}
