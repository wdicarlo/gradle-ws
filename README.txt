This is the README.txt file for the Workspace Gradle plugin.

1. DESCRIPTION
The Workspace (WS) and Workspace Project (WSP) plugins allows to manage a partial set of projects.

2. FEATURES
- workspace project folder can or cannot be present
- workspace project can be or not active for project dependencies
- workspace project properties can be loaded from a CSV file
- smart project dependency definition
- project build script generated from workspace build script
- project and workspace build script sychronization

3. TODOs
- store project archives name in the CSV file (column archives=[String, ...])
- define enabled project relationships for dependencies setup (column relationshipd=[String, ...])
- accept workspace name from environment variable (-Dws_name=<name>)
- improve documentation
- improve performances
- improve code quality
- add unit tests

4. LICENSING

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 

4. KNOW LIMITATIONS
 
