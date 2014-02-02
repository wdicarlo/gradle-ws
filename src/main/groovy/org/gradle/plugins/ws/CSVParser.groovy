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
*
* TODO: 
* - remove dependency from ""
* 
*/

package org.gradle.plugins.ws

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CSVParser {
	private static final Logger LOGGER = LoggerFactory.getLogger( CSVParser.class );
	
	File csvFile
	Map mymap = [:]
	List defaults = null 
	List columns = null

	def getMap() {
		return mymap
	}
	def dump() {
		println "Dump:"
		mymap.each { key,value ->
			println "("+key+","+value+")"
		}
	}
	
	def parseCSV( File file) {
		if( file.exists() == false ){
			LOGGER.error "The workspace file "+file.getName()+" does not exist"
			throw new IllegalArgumentException("The workspace file "+file.getName()+" does not exist");
		}
		
		csvFile = file
		def lineCount = 0
		def numColumns = -1
		csvFile.eachLine() { line ->
			line = line.trim()
			def llen = line.length()
			line = line.replaceAll( ",,", ",\"\",")
			// TODO: replace this workaround for replaceAll bug
			while(llen!= line.length()){
				llen = line.length()
				line = line.replaceAll( ",,", ",\"\",")
			}	
			if( line.startsWith(',') == true ) {
				line = "\"\""+line
			}
			if( line.endsWith(',') == true ) {
				line = line + "\"\""
			}
			def field = line.tokenize(",")
			def len = field.size()-1
			
			if( lineCount == 0 ){
				if( field[0] == '"project"' ) {
					columns = field[1..len]
				} else {
					LOGGER.error "The workspace file "+file.getName()+" does not have the row project as first row"
					throw new IllegalFormatException("The workspace file "+file.getName()+" does not have the row project as first row");
				}
				numColumns = columns.size()
			}
			
			if( lineCount > 0 ){
				if( len != numColumns ){
					LOGGER.error "The workspace file "+file.getName()+" has the row "+(lineCount+1)+" with a wrong number of columns ("+len+"/"+numColumns+")"
					throw new IllegalFormatException("The workspace file "+file.getName()+" has the row "+(lineCount+1)+" with a wrong number of columns ("+len+"/"+numColumns+")");
				}
			}
			
			if( lineCount == 1 ){
				if( field[0] == '"default"' ) {
					defaults = field[1..len]
				} else {
					LOGGER.error "The workspace file "+file.getName()+" does not have the row default as second row"
					throw new IllegalFormatException("The workspace file "+file.getName()+" does not have the row default as second row");
				}
			}			
			def i = 0
			def row = []	
			field.each { val ->
				if( i  > 0 ) {
					def v = val
					if( val == null || val == '""') {
						if( defaults != null ) {
							v = defaults[i-1]
						}
					}
					row.add( v )
				}
				i++
			}

			mymap.put( field[0], row )

			lineCount++
		}
	}
}