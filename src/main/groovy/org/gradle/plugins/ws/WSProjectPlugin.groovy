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
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

class WSProjectPlugin implements Plugin<Project> {
	private Logger log = Logging.getLogger(WSProjectPlugin.class)
	
	def void apply(Project project) {
		log.info "Applying plugin: wsp"
		
		WSUtils.addWSProject project
		
		if( project.convention.plugins.wsp == null ){
			project.convention.plugins.wsp = new GroovyWSProjectPluginConvention(project)
		}
		WSUtils.setupProjectProperties( project.name )
			
		project.task('wsp_info') {
		} << { task ->
			wsp = task.project.convention.plugins.wsp
			println "Workspace Project Name: "+project.name
			project.artifacts.configurationContainer.getByName('default').each { artifact ->
				println "Artifact: "+artifact.dump()
			}
			println "Project is Active: "+wsp.wsp_active
			// get WS convention to access ws_archives
			def rootPrj = WSUtils.getRootProject()
			if( rootPrj != null ){
				ws = rootPrj.convention.plugins.ws
				def ar = ws.ws_archives.get( task.project.name)
				if( ar != null ){
					println task.project.name+" -> "+ar
				}
			}
			WSUtils.dumpProjectProperties (project)
		}
	}
}
