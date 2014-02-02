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

import org.gradle.api.Project;
import org.gradle.api.tasks.GradleBuild;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;


// TODO: convert to a sigleton
class WSUtils {
	private static Logger log = Logging.getLogger(WSUtils.class)

	// must exist just one root project
	private static Project _rootProject = null
	
	
	private static WSData _data = null;
	
	// add list of ws project
	private static ArrayList<Project> _wsProjects = new ArrayList() 
	
	private WSUtils() {}
	
	
	static void setRootProject( final Project prj){
		_rootProject = prj
	}
	
	static void setWSData( final WSData data ) {
		_data = data
	}
	
	static WSData getWSData() {
		return _data
	}
	
	static Project getRootProject( ){
		return _rootProject
	}
	
	static boolean isRootProject( final String name) {
		if( _rootProject == null ){
			return false
		}
		return _rootProject.name.equals (name)
	}
	static void addWSProject( final Project prj){
		_wsProjects.add(prj)
	}
	static boolean isWSProject( final Project prj ){
		return _wsProjects.contains (prj)
	}
	static List getWSProjects() {
		return _wsProjects.asList()
	}

	static void dumpProjectProperties( final Project prj ) {
		if( isWSProject(prj) == false && isRootProject (prj.name) == false ){
			return
		}
		if( _data != null ){
			def vars = _data.getProjectValues ( "\"project\"")
			def props = prj.getProperties()
			vars.each { var -> 
				def v = var.replaceAll("\"","")
				v = v.trim()
				println "Prop: "+v+ " = "+ props.get( v )
			}

		}
	}
	static void setupProjectProperties( final String name ){
		//println "Setting up workspace project: "+prj.name
		//if( data.isWorkspaceProject("\""+prj.name+"\"") == true ){
		def prj = getProject( name )
		if( isWSProject(prj) == false && isRootProject (name) == false ){
			return
		}
		if( _data != null ){
			def vars = _data.getProjectValues ( "\"project\"")
	
			
			// TODO: remove the following duplicated code
			// find the values of the current project
			def values = _data.getProjectValues("\""+prj.name+"\"")
			if( values == null ) {
				// use the defaults
				values = _data.getProjectValues("\"default\"")
			}

			if( vars != null && values != null ){
				if( vars.size() == values.size() ){
					def index = 0
					vars.each {
						def var = vars[index]
						var = var.replaceAll("\"","")
						var = var.trim()
						def value = values[index]
						value = value.replaceAll("\"","")
						value = value.trim()
						//println "Setting "+prj.name+"."+var+" = "+value
						prj.setProperty( var, value )
						if( var == "wsp_active" ){
							if( prj.convention.plugins.wsp == null ){
								prj.convention.plugins.wsp =  new GroovyWSProjectPluginConvention(prj)
							}
							log.info "Using var = "+var+" value = "+ value
							prj.convention.plugins.wsp.wsp_active = Boolean.valueOf (value)
							log.info "Set "+prj.name+".wsp.wsp_active = "+prj.convention.plugins.wsp.wsp_active
						}
						index++
					}
				}
			}
		}
	}
	static boolean initProject( final String name ) {
		if( isProject( name ) == false ) {
		  return false
		}
		  def prj_path = new File( "../$name" ) // TODO: replace rootDir
		  if( prj_path.exists() == true ) {
			  // TODO: should we clean it?
		  } else {
		  	prj_path.mkdirs()
		  }
		  return true
	  }

	static boolean isProjectPresent( final String name ) {
		if( isProject( name ) == false ) {
		  return false
		}
		  def isPresent = true
		  def prj_path = new File( "../$name" ) // TODO: replace rootDir
		  if( prj_path.exists() == true ) {
			  isPresent = true
		  } else {
		  //println "Project $name is not present!!!"
			  isPresent = false
		  }
		  isPresent
	  }
	  
	  static boolean isProjectSrcPresent( final String name ) {
		if( isProject( name ) == false ) {
		  return false
		}
		  def isPresent = true
		  def prj_path = new File( "../$name/src" ) // TODO: replace rootDir
		  if( prj_path.exists() == true ) {
			  isPresent = true
		  } else {
		  //println "Project source code of $name is not present!!!"
			  isPresent = false
		  }
		  isPresent
	  }
	  
	  static boolean isProject( final String prjName ) {
		if( _rootProject == null ){
			return false
		}  
		def found = false
		for( prj in _rootProject.subprojects ) { //  scan the projects
	  
		  if( prj.name.equals(prjName) == true ) {
			found = true
			break
		  }
		}
		if(found){
			//println prjName+' is active'
		}else{
			//println prjName+' is NOT active'
		}
		found
	  }
	  
	  
	  static Project getProject( final String prjName ){
		  if( _rootProject == null ){
			  return null
		  }
		  if( isRootProject(prjName) == true ) {
			  return getRootProject()
		  }
		  for( prj in _rootProject.subprojects ) { //  scan the projects
			  
				  if( prj.name.equals(prjName) == true ) {
					  return prj
				  }
		  }
		
	  }

}
