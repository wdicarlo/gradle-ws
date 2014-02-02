/*
 * Copyright 2010 Walter Di Carlo.
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

package org.gradle.plugins.ws

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logging;
import org.gradle.api.logging.Logger;

class WSGhostPlugin implements Plugin<Project> {
	private Logger log = Logging.getLogger(WSGhostPlugin.class)
	
	def void apply(Project project) {
		log.info("Applying Plugin: wsg")
		project.convention.plugins.wsg = new GroovyWSGhostPluginConvention(project)
		
	}
}
