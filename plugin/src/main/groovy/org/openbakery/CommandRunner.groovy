/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openbakery

import org.openbakery.output.OutputAppender
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CommandRunner {

	private static Logger logger = LoggerFactory.getLogger(CommandRunner.class)

	private StringBuilder resultStringBuilder;

	private def commandListToString(List<String> commandList) {
		def result = ""
		commandList.each {
			item -> result += item + " "
		}
		return "'" + result.trim() + "'"
	}

	def run(String directory, List<String> commandList, Map<String, String> environment, OutputAppender outputAppender) {
		resultStringBuilder = new StringBuilder();

		logger.debug("Run command: {}", commandListToString(commandList))
		if (environment != null) {
			logger.debug("with additional environment variables: {}", environment)
		}
		def processBuilder = new ProcessBuilder(commandList)
		processBuilder.redirectErrorStream(true)
		processBuilder.directory(new File(directory))
		if (environment != null) {
			Map<String, String> env = processBuilder.environment()
			env.putAll(environment)
		}
		def process = processBuilder.start()
		process.inputStream.eachLine {
			if (outputAppender) {
				outputAppender.append(it)
			}
			logger.debug("{}", it)
			if (resultStringBuilder != null) {
				if (resultStringBuilder.length() > 0) {
					resultStringBuilder.append("\n");
				}
				resultStringBuilder.append(it);
			}
		}
		process.waitFor()
		if (process.exitValue() > 0) {
			throw new CommandRunnerException("Command failed to run: " + commandListToString(commandList))
		}
	}

	def run(String directory, List<String> commandList, OutputAppender outputAppender) {
		run(directory, commandList, null, outputAppender)
	}

	def run(String directory, List<String> commandList) {
		run(directory, commandList, null, null)
	}

	def run(List<String> commandList) {
		run(".", commandList)
	}

	def runWithResult(List<String> commandList) {
		return runWithResult(".", commandList)
	}

	def runWithResult(String directory, List<String> commandList) {
		return runWithResult(directory, commandList, null, null)
	}

	def runWithResult(String directory, List<String> commandList, Map<String, String> environment, OutputAppender outputAppender) {
		run(directory, commandList, environment, outputAppender);
		return resultStringBuilder.toString();
	}


	def getResult() {
		return resultStringBuilder.toString();
	}
}