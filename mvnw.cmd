@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one or more
@REM contributor license agreements. See the NOTICE file distributed with
@REM this work for additional information regarding copyright ownership.
@REM The ASF licenses this file to you under the Apache License, Version 2.0.
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET ___MVNW_UGLY_CYGWIN_PATH_HACK=
@SET __MVNW_CURRENT_DIR=%~dp0
@IF "%__MVNW_CURRENT_DIR:~-1%"=="\" SET __MVNW_CURRENT_DIR=%__MVNW_CURRENT_DIR:~0,-1%
@SET __MVNW_LAUNCHER_CONF=%__MVNW_CURRENT_DIR%\.mvn\wrapper\maven-wrapper.properties

@FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%__MVNW_LAUNCHER_CONF%") DO (
    @IF "%%A"=="distributionUrl" (SET __MVNW_DISTRIBUTION_URL=%%B)
    @IF "%%A"=="wrapperUrl" (SET __MVNW_WRAPPER_URL=%%B)
)

@SET __MVNW_USER_HOME=%USERPROFILE%
@IF "%__MVNW_USER_HOME%"=="" (SET __MVNW_USER_HOME=%HOME%)

@SET __MVNW_APACHE_MAVEN_DIR=%__MVNW_USER_HOME%\.m2\wrapper\dists
@SET __MVNW_JAR=%__MVNW_USER_HOME%\.m2\wrapper\maven-wrapper.jar

@IF NOT EXIST "%__MVNW_JAR%" (
    @IF NOT EXIST "%__MVNW_USER_HOME%\.m2\wrapper" (
        @MD "%__MVNW_USER_HOME%\.m2\wrapper"
    )
    @powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%__MVNW_WRAPPER_URL%' -OutFile '%__MVNW_JAR%'}"
)

@java -classpath "%__MVNW_JAR%" "-Dmaven.multiModuleProjectDirectory=%__MVNW_CURRENT_DIR%" org.apache.maven.wrapper.MavenWrapperMain %*
