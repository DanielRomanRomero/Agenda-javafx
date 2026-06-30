@echo off
set JAVA_HOME=C:\Dev\JDKs\jdk-25
"C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" clean javafx:run -f "%~dp0pom.xml"
