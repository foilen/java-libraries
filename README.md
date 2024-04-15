java-libraries
==============

Many common tools.

License: The MIT License (MIT)


Projects
--------

- jl-smalltools: A couple of small tools that are mostly one class each.

Sub-projects
------------

- jl-smalltools-main: The core with a lot of tools.
    - Converters: frequency, time, space.
    - Smooth triggers.
    - Standard hashers: MD5, SHA-1, SHA-256, SHA-512
    - Files and directories helpers.
    - and more.
- jl-smalltools-bouncycastle: Some more tools to work with encryption by using Bouncy Castle.
- jl-smalltools-database: Some more tools to work with JDBC databases (for now, mostly just the Upgrader tracker and
  abstract task to manage JDBC databases).
- jl-smalltools-hibernate61: Some more tools to work with Hibernate 6.1 (for now, mostly just a tool to generate an SQL
  file for a specific dialect using the Entity classes).
- jl-smalltools-hibernate63: Some more tools to work with Hibernate 6.3 (for now, mostly just a tool to generate an SQL
  file for a specific dialect using the Entity classes).
- jl-smalltools-mongodb: Some more tools to work with MongoDB.
    - the Upgrader tracker.
    - Some distributed basic data structures that are backed by MongoDB. (Map, Queue/Deque, ReentrantLock, Spring Cache)
    - Some helpers to manage collections and wait on Change Streams.
- jl-smalltools-mongodb-spring:
    - the Upgrader abstract task to manage MongoDB databases.
    - Spring Cache implementation that uses MongoDB.
- jl-smalltools-spring: Some more tools to work with Spring.
    - Some basic POJOs to create a REST API.
    - Some tools to copy POJOs values.
    - Helpers to send emails in text or HTML. Can also send emails with attachments and use Freemarker template.
- jl-smalltools-ssh:
    - Some helpers to use jsch to connect to a SSH server.
    - Can execute commands and redirect the output to a file or a stream.
    - Can create an SFTP channel and let you upload/download files.

Usage
-----

Include this library to your project.

Process
-------

Versioning:

- The version number is in the format MAJOR.MINOR.BUGFIX (e.g 0.1.0).
- The API in a MAJOR release is stable. Everything that will be removed in the next MAJOR release are marked as
  deprecated.

For changes/removal in the stable API:

- When something is in the stable API, it will be there for all the releases in the same MAJOR version.
- Everything that will be removed in the next MAJOR version is marked as @deprecated and the Javadoc will explain what
  to use instead if there is a workaround.

Deployment instructions
-----------------------

Execute:

```
# For locally testing and skip checking the tests:
./create-local-release-no-tests.sh   # Will use master-SNAPSHOT as the version
./create-local-release-no-tests.sh THE_VERSION

# For locally testing:
./create-local-release.sh   # Will use master-SNAPSHOT as the version
./create-local-release.sh THE_VERSION

# For creating a public release and publishing it
./create-public-release.sh THE_VERSION
```

You can see releases available:

- https://repo1.maven.org/maven2/com/foilen/jl-smalltools-main/
