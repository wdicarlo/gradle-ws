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

import groovy.lang.Closure;

import org.gradle.api.Project
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;


class GroovyWSProjectPluginConvention {
	private Logger log = Logging.getLogger(GroovyWSProjectPluginConvention.class)
	
	Project _project
	boolean wsp_active = true // true means the project partecipate in dependencies
	
	public GroovyWSProjectPluginConvention(Project project) {
		_project = project
	}
	
	def ws_project( String path ){
		
		log.info _project.name+".ws_project "+path
		//println "WSProject: "+ _project.name+".ws_project "+path
		
		// return project(path) or project_name.jar
		String prjName = path
		if( path.startsWith(":") == true )
			prjName = path.substring(1)
		def Project prj = WSUtils.getProject(prjName)
		def isActive = true
		if( WSUtils.isWSProject (prj) == true ){
			if( prj.convention.plugins.wsp != null )
				isActive = prj.convention.plugins.wsp.wsp_active
		}	
		if( isActive == true && WSUtils.isProjectSrcPresent(prjName) == true ){
			log.info "Setting up dpendency with project: "+prjName
			return _project.project(path)
		}else{
			def rootPrj = WSUtils.getRootProject()
			if( rootPrj != null ){
				def ws = rootPrj.convention.plugins.ws
				def archives = ws.ws_archives
				def p_archives = archives.get( prjName )
				if( p_archives != null ){
					def paths = [ ]
					p_archives.each { a ->
						paths.add (_project.getProjectDir().getAbsolutePath()+'/lib/'+a+".jar")
					}
					log.info "Setting up dependency with jars: "+p_archives
					return _project.files ( paths )
				}
			} 
			log.info "Setting up dependency with jar: lib/"+prjName+".jar"
			return _project.files( [_project.getProjectDir().getAbsolutePath()+'/lib/'+prjName+".jar"])
			
		}
	}
	
		
	
	  def wsp(Closure closure) {
		  closure.delegate = this
		  println "wsp closure"
		  
		  closure()
	  }

}
