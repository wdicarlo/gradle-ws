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

import java.util.Hashtable;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import org.gradle.api.GradleScriptException;
import org.gradle.api.Project 
import org.gradle.api.artifacts.SelfResolvingDependency;

class GroovyWSPluginConvention {
	private Logger log = Logging.getLogger(GroovyWSProjectPluginConvention.class)
	final String ws_info = "Gradle Workspace Plugin"
	String ws_name
	ArrayList<String> [] ws_projects = new ArrayList<String>()
	private Project _project
	Hashtable<String,String[]> ws_archives = new Hashtable<String,String[]>();
	
	
	public GroovyWSPluginConvention(final Project project) {
		_project = project
		// TODO: assert that the ws project must be the root project
	}
	def void helloworld(){
		println "Hello World project: "+_project.name
	}
	
	/*
	 * Manage project dependencies for project not in the workspace
	 */
	def ws_project( String path ){
		log.info "=================================================="
		//println "WS: "+ _project.name+".ws_project "+path
		
		// TODO: throw an exception
		throw new GradleScriptException()
		// return project(path) or project_name.jar
//		String prjName = path
//		if( path.startsWith(":") == true )
//			prjName = path.substring(1)
//		def Project prj = WSUtils.getProject(prjName)
//		if( WSUtils.isProjectSrcPresent(prjName) == true ){
//			log.info "Setting up dpendency with project: "+prjName
//			return _project.project(path)
//		}else{
//			log.info "Setting up dependency with jar: lib/"+prjName+".jar"
//			// TODO: use the current active project not the root _project
//			//return _project.files( [_project.getProjectDir().getAbsolutePath()+'/lib/'+prjName+".jar"])
//			assert "Cannot use ws_project in projects not in the workspace", true == false
//		}
	}
	
	def ws(Closure closure) {
		closure.delegate = this
		println "ws closure"
		
		closure()
	}
}
