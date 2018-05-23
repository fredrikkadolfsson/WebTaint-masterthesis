# WebTaint - Dynamic Taint Tracker
WebTaint is a Dynamic Taint Tracker developed during the conduction of a masters thesis.

## Installation (Generate Agent and RT JAR)
* cd  *WebTaint root directory*
* ./gradlew clean assemble

## Installation (Add Agent and RT JAR to Web Application)
Add following two Java options to application startup execution.
* -Xbootclasspath/p:*WebTaint root directory*/*build/libs*/webtaint-rt-1.0-SNAPSHOT.jar 
* -javaagent:*WebTaint root directory*/*build/libs*/webtaint-agent-1.0-SNAPSHOT.jar