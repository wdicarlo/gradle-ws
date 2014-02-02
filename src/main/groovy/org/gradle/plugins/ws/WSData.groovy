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

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

class WSData {
	private Logger log = Logging.getLogger(WSData.class)
	
	private Map _data = null
	
	WSData( Map data ){
		log.info "Setting up data for ws plugin"
		_data = data
	}
	
	def dump() {
		println "Data Dump:"
		_data.each { key,value ->
			println "("+key+","+value+")"
		}
	}
	
	def int getPropertyIndex( final String propName ){
		def props = _data.get('\"project\"')
		def i = 0
		def index = -1
		props.each { elem -> 
			if( elem == propName ){
				index = i
				return
			}
			i++
		}
		return index
	}
	def List getProjectValues( final String project ){
		def values = _data.get (project)
		return values
	}
	
	def String getPropertyValue( final String project, final String propName ){
		def index = getPropertyIndex( propName )
		if( index == -1 )
			return null
		def values = _data.get (project)
		return values[index]
	}
	
	def boolean isWorkspaceProject( final String name ){
		return _data.containsKey( name )
	}

}
