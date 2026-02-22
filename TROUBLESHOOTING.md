# Troubleshooting Guide -- Java & VS Code Setup

This guide helps resolve common environment issues when running the
Spring Boot microservices and Angular frontend from Chapter 02.

------------------------------------------------------------------------

## Quick Global Verification Checklist

Run:

    java -version
    mvn -version

Ensure: - Java 21 or higher - Maven uses the same Java version -
JAVA_HOME is correctly set - VS Code restarted after changes

------------------------------------------------------------------------

## VS Code Core Java Settings

Open Settings (CTRL + , or Cmd + ,)

Search for: - java.jdt.ls.java.home - java.configuration.runtimes

Example:

    "java.jdt.ls.java.home": "/path/to/jdk"

Optional multiple runtimes:

    "java.configuration.runtimes": [
      { "name": "JavaSE-21", "path": "/path/to/jdk-21" },
      { "name": "JavaSE-24", "path": "/path/to/jdk-24", "default": true }
    ]

Restart VS Code after updating settings.

------------------------------------------------------------------------

# macOS

List JDKs:

    /usr/libexec/java_home -V

Set JAVA_HOME in \~/.zshrc:

    export JAVA_HOME=$(/usr/libexec/java_home -v 24)
    export PATH=$JAVA_HOME/bin:$PATH

Reload:

    source ~/.zshrc

------------------------------------------------------------------------

# Linux

List JDKs:

    update-alternatives --config java

Set JAVA_HOME in \~/.bashrc:

    export JAVA_HOME=/usr/lib/jvm/jdk-24
    export PATH=$JAVA_HOME/bin:$PATH

Reload:

    source ~/.bashrc

------------------------------------------------------------------------

# Windows

Check version:

    where java
    java -version

Set JAVA_HOME in System Environment Variables to:

    C:\Program Files\Java\jdk-24

Ensure PATH includes:

    %JAVA_HOME%\bin

Restart terminal after changes.

------------------------------------------------------------------------

## Common Problems

Unsupported class file major version: Align Java versions between Maven,
terminal, and VS Code.

Port already in use: Change server.port in application.yml or stop
running process.

Eureka shows no instances: Ensure discovery server runs first and
defaultZone is correct.
