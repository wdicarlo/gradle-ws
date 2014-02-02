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
 * 
 */

package org.gradle.plugins.ws

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.logging.Logging;
import org.gradle.api.logging.Logger;
import java.util.Date


class WSPlugin implements Plugin<Project> {

	
	private Logger log = Logging.getLogger(WSPlugin.class)
	
	private CSVParser csvParser = new CSVParser()
	// TODO: use a Map of WSData in order to read only once the same CSV file
	private Project _project = null
	
	
	def void apply(Project project) {
		log.info("Applying Plugin: ws")
		_project = project
		
		// only one workspace may be defined
		assert WSUtils.getRootProject() == null
		WSUtils.setRootProject(project.getRootProject())
		
		project.convention.plugins.ws = new GroovyWSPluginConvention(project)
		if( project.ws_name == null ) {
			project.ws_name = "workspace"
		}
		project.convention.plugins.ws.ws_name = project.ws_name
		
				
		log.info ( "Configuring workspace: "+project.ws_name )
		project.configure(project) {

			afterEvaluate {
				
				// manage workspace build script
				// cannot put this in a task because it must be done before
				// project scripts are evaluated
				def bFile = new File( project.ws_name+'.gradle' )
				def refresh = false
				if( bFile.exists() == true ){
					log.info ( "Found workspace config file: "+bFile )
					
					// TODO: check status o project scripts
					// - no project scripts then create them
					// - workspace script newer then project scrips then update them
					// - workspace script older then project scripts then update it
					// - mixed status then throw an exception
					def status = getWorkspaceScriptStatus()
					log.info ( "Workspace script status: "+status )
					if( status == -1 ){
						throw new RuntimeException("Updated both workspace and project scripts")
					}
					def updated = false
					def text = bFile.getText()
					def configScript = new WSConfigScript( text )
					
					// TODO: evaluate only the workspaces projects
					project.subprojects.each { prj ->
						if( WSUtils.isProjectPresent (prj.name ) == false ) {
							WSUtils.initProject (prj.name)
						}
						if( WSUtils.isProjectPresent (prj.name ) == true ) {
							def buildFile = new File( prj.projectDir.path+"/build.gradle")
							def isExisting = buildFile.exists()
							def sez = configScript.findProjectScript( prj.name )
							if( isExisting == false ) {
								if( sez != null ){
									log.info ("Creating build file: "+prj.name+'/build.gradle')
									buildFile.setText (sez)
									refresh = true
									// apply it 
									prj.apply from: buildFile
								}
							} else {
								if( status == 2 ){
									if( sez != null ){
										log.info ("Updating build file: "+prj.name+'/build.gradle')
										buildFile.setText (sez)
										refresh = true
									}
								} else if( status == 1 ){
									if( isSameScript( prj )  == false ){
										log.info ("Updating workspace build script: "+project.name+"/"+project.ws_name+".gradle")
										configScript.updateProjectScript( prj.name, buildFile.getText() )
										updated = true
									}
								}
							}
						} else {
							log.info ("Missing project: "+prj.name)
						}
						
					}
					if( updated == true ){
						log.info ("Updating workspace build file: "+project.name+"/"+project.ws_name+".gradle")
						// update workspace script file
						bFile.setText ( configScript.getScript() )
						refresh = true
					}
				} else {
					// workspace build script does not exist
					// build it
					// TODO: evaluate only the workspaces projects
					def text = "// Build script for workspace: "+project.ws_name+"\n\n"
					def create = false
					project.subprojects.each { prj ->
						if( WSUtils.isProjectPresent(prj.name) == true ){
							def buildFile = new File( prj.projectDir.path+"/build.gradle")
							def isExisting = buildFile.exists()
							if( isExisting == true ) {
								text += "/*********************************************\n"
								text += "*                "+prj.name+"\n"
								text += "*********************************************/\n\n"
								text += "project(':"+prj.name+"'){\n"
								text += buildFile.getText()
								text += "\n}\n"
								create = true
							}
						}
					}
					if( create == true ){
						// create the workspace script if at least one project is present
						bFile.setText ( text )
						refresh = true
					}
				}
				if( refresh == true ){
					// update the projects scripts to avoid synch issues
					// TODO: evaluate only the workspaces projects
					def current = new Date();
					project.subprojects.each { prj ->
						if( WSUtils.isProjectPresent (prj.name ) == true ) {
							def buildFile = new File( prj.projectDir.path+"/build.gradle")
							def isExisting = buildFile.exists()
							if( isExisting == true ) {
								// touch the file avoiding to read/write it?
//								text = buildFile.getText()
//								buildFile.setText(text)
								buildFile.setLastModified(current.getTime());
							}
						}
					}

				}

			}
			afterEvaluate {
				project.subprojects.each { prj ->
					if( WSUtils.isWSProject (prj) == false ){
						prj.apply plugin: 'wsg'
					}
				}
			}
		}				
		
		
		def wsfile = new File(project.ws_name+".csv")
		
		def WSData data = null
		if( wsfile.exists() == true ){
			log.info("Found workspace CSV file: "+wsfile.getName())
			
			csvParser.parseCSV wsfile
			//			csvParser.dump()
			data = new WSData( csvParser.getMymap() )
			
			WSUtils.setWSData data
			WSUtils.setupProjectProperties( project.name )
		}
		
		project.task('ws_dump') {
		} << { task -> 
			data = WSUtils.getWSData()
			if( data == null ){
				log.info("No workspace data has been loaded")
				
				return
			}
			def vars = data.getProjectValues ( "\"project\"")
			if( vars == null ){
				return
			}
			WSUtils.getWSProjects().each { prj ->
				vars.each { var -> 
					def v = var.replaceAll("\"","")
					v = v.trim()
					def value = prj.getProperty( v )
					if( value != null ){
						println prj.name+"."+v+" = "+value
					}
				}
			}
		}
		
		project.task('ws_info') {
		} << { task ->
			ws = task.project.convention.plugins.ws
			println "Info: "+ws.ws_info
			println "Project Name: "+project.name
			println "Workspace Name: "+ws.ws_name
			if( data != null ){
				println "Workspace Properties:"
				def props = data.getProjectValues ( "\"project\"")
				props.each { prop ->
					prop = prop.replaceAll("\"","")
					prop = prop.trim()
					println project.name+"."+prop+" = "+task.project.getProperty(prop)
				}
			}
			
			if( ws.ws_archives != null ){
				println "Workspace ws_archives:"
				ws.ws_archives.each { item -> println "> "+item }
			}
			// list sub projects
			WSUtils.getRootProject().subprojects.each { prj ->
				print "Subproject: "+prj.name
				if( WSUtils.isWSProject(prj) == true ){
					print " Workspace: "+ws.ws_name
					if( prj.wsp_active == true )
						println " ACTIVE"
					else
						println " DISABLED"
				} else
					println " "
			}
		}
		project.task ('ws_init') << {
			ws = project.convention.plugins.ws
			WSUtils.getWSProjects().each { prj ->
				if( WSUtils.isProjectPresent( prj.name ) == false ) {
					log.info("Initializing project $prj.name ...")
					prj_path = new File( "../$prj.name" ) // TODO: replace rootDir
					prj_path.mkdir()
				}
			}
		}
		
		project.task ('ws_deleteall') << {
			ws = project.convention.plugins.ws
			println "If you are sure to DELETE ALL projects then input YES_DELETE_ALL_PROJECTS ? "
			input = new Scanner(System.in)
			answer = input.nextLine()
			if( "YES_DELETE_ALL_PROJECTS".equals(answer) == false ) {
				return
				//System.exit(0)
			}
			println "Deleting all projects ..."
			WSUtils.getWSProjects().each { prj ->
				if( WSUtils.isProjectSrcPresent( prj.name ) ) {
					prj_path = new File( "../$prj.name" ) // TODO: replace rootDir
					log.info "Deleting folder "+prj_path.path
					prj_path.deleteDir()
				}
			}
		}
		
		project.task ('ws_status') << {
			ws = project.convention.plugins.ws
			println "Projects status:"
			WSUtils.getWSProjects().each { prj ->
				print ":"+prj.name+" "
				if( WSUtils.isProjectPresent( prj.name ) ) {
					if( WSUtils.isProjectSrcPresent( prj.name ) ) {
						println "PRESENT WITH SOURCES"
					} else {
						println "PRESENT BUT WITH NO SOURCES"
					}
				} else {
					println "NOT PRESENT"
				}
				if( data != null ){
					println "Worspace project properties:"
					def props = data.getProjectValues ( "\"project\"")
					props.each { prop ->
						prop = prop.replaceAll("\"","")
						prop = prop.trim()
						println prj.name+"."+prop+" = "+prj.getProperty(prop)
					}
				}
			}
		}
	}
	/*
	 * 
	 * 
	 * return:
	 * - -1 -> invalid state
	 * - 0 -> not applicable
	 * - 1 -> older
	 * - 2 -> newer
	 * 
	 * Notes:
	 * - An older workspace script will be updated only if the content is different
	 * 
	 * TODO: use an Enum
	 */
	private int getWorkspaceScriptStatus() {
		def bFile = new File( _project.ws_name+'.gradle' )
		def int status = 0
		if( bFile.exists() == true ){
			def wsDate = bFile.lastModified()
			status = 2
			// TODO: evaluate only the workspace projects
			_project.subprojects.each { prj ->
				if( status != -1){
					def buildFile = new File( prj.projectDir.path+"/build.gradle")
					if( buildFile.exists() == true ) {
						def pDate = buildFile.lastModified()
						if( wsDate < pDate ){
							status = 1	
						} else if( wsDate > pDate ){
							if( status == 1 ){
								status = -1
							}
						}
					}
				}
			}
		}
		return status
	}
	
	private boolean isSameScript( final Project prj ){
		if( prj == null ){
			return false
		}
		def pBuildFile = new File( prj.projectDir.path+"/build.gradle")
		if( pBuildFile.exists() == true ) {
			def wsBuildFile = new File( _project.ws_name+'.gradle' )
			if( wsBuildFile.exists() == true ){
				def text = wsBuildFile.getText()
				def configScript = new WSConfigScript( text )
				text = pBuildFile.getText()
				return configScript.isProjectScriptEquals( prj.name, text)
			}
		}
		return false
	}
}
