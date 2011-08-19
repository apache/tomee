/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *	Log the output to Slf4j 
 *
 */

public class Slf4jLogStream implements LogStream {
	private Logger log;
	
	public Slf4jLogStream(LogCategory logCategory) {
		log = LoggerFactory.getLogger(logCategory.getName());
	}

	@Override
	public boolean isFatalEnabled() {
		return log.isErrorEnabled();
	}

	@Override
	public void fatal(String message) {
		log.error(message);
	}

	@Override
	public void fatal(String message, Throwable t) {
		log.error(message,t);
	}

	@Override
	public boolean isErrorEnabled() {
		return log.isErrorEnabled();
	}

	@Override
	public void error(String message) {
		log.error(message);
	}

	@Override
	public void error(String message, Throwable t) {
		log.error(message,t);
	}

	@Override
	public boolean isWarnEnabled() {
		return log.isWarnEnabled();
	}

	@Override
	public void warn(String message) {
		log.warn(message);
	}

	@Override
	public void warn(String message, Throwable t) {
		log.warn(message,t);	
	}

	@Override
	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}

	@Override
	public void info(String message) {
		log.info(message);
	}

	@Override
	public void info(String message, Throwable t) {
		log.info(message,t);
	}

	@Override
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	@Override
	public void debug(String message) {
		log.debug(message);
	}

	@Override
	public void debug(String message, Throwable t) {
		log.debug(message,t);
	}

}
