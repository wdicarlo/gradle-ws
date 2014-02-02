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

import org.gradle.api.logging.Logging;
import org.gradle.api.logging.Logger;


class WSConfigScript {
	private Logger log = Logging.getLogger(WSConfigScript.class)
	def String _script = null
	
	public WSConfigScript( final String script) {
		_script = script
	}
	
	public String getScript() {
		return _script
	}
	
	public boolean isProjectScriptEquals( final String prjName, final String text ){
		if( text == null ){
			return false
		}
		def currScript = findProjectScript( prjName )
		if( currScript == null ){
			return false
		}
		return text.trim().equals( currScript.trim() )
		
	}
	
	public boolean updateProjectScript( final String prjName, final String text ){
		// validate text { ... }
		if( text == null ){
			return false
		}
		// find location range
		def start = new String( "project(\':"+prjName+"\'){");
		def sIndex = _script.indexOf(start);
		if( sIndex == -1 ) {
			// TODO: add new script
			return false
		}
		def begin = _script.indexOf( '{', sIndex)
		if( begin == -1 )
			return false
		def end = findScopeEnd( begin )

		// replace text
		def script = _script.substring(0,begin+1)
		script += text
		script += _script.substring( end )
		
		_script = script
		
		return true
	}
	
	public String findProjectScript( final String prjName ){
		// TODO: use regular expressions
		def start = new String( "project(\':"+prjName+"\'){");
		def sIndex = _script.indexOf(start);
		if( sIndex == -1 )
			return null
		log.info ("Extracting build script for project: "+prjName)
		return findScope( sIndex )	  
	}
	
	private String findScope( final int sIdx ){
		def begin = _script.indexOf( '{', sIdx)
		if( begin == -1 )
			return null
		def end = findScopeEnd( begin )
		
		return _script.substring (begin+1, end-1)	
	}
	
	private int findScopeEnd( final int sIdx ){
		def b = sIdx
		if( _script[b] != '{' ){
			// throw exception
			throw RuntimeException("Cannot find project script start position")
		}
		b = b + 1
		def begin = b
		def level = 0;
		def found = false
		while( b < _script.length()  ){
			if( _script[b] == '{' ){
				level++
			}
			if( _script[b] == '}' ) {
				if( level > 0 )
					level--
				else {
					found = true
					break
				}
			}
			b++
		}
		if( found == false ){
			// throw exception
			throw RuntimeException("Cannot find project script end position")
		}
		return b
	}
}
