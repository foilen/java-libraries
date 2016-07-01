java-libraries
==============

Many common tools.

License: The MIT License (MIT)


Projects
--------

- jl-smalltools: A couple of small tools that are mostly one class each.
- jl-incubator: Everything that is new and that might be removed or that might be changed (like a different API)

Usage
-----

Include this library to your project.
When you use any classes, check the Javadoc to see which dependencies you should add.

Process
-------

Versioning:
- The version number is the date of the release (e.g 2015.01.20).
- Any stable API will be there for the next year so it is always safe to update 6 months in the future. (see "Upgrading the library in your project")

For new things:
- New ideas and API should start in "jl-incubator" unless it is very small and that it is sure that nothing will be changed in it.
- When an API is stable, it can go in one of the other stable projects.

For changes/removal in the stable API:
- When something is in the stable API, it must be present for a year. Like that, upgrading is made easy by 6 months interval.
- The first step is to add a @deprecated annotation on anything that will be removed. Specify the first release date and the path to upgrade (like using a different method/class/library).
- Only one year later, the @deprecated API can be removed.

Upgrading the library in your project:
- It is normal to not always upgrade all your libraries every day.
- Since an API will stick for a year, it is safe to take any release that is up to one year later in the time. This is clearly visible by the release name since it is the date.
- You should get a new version that is about 6 months later than yours, fix any deprecation warnings with the suggestion and repeat until you are at the latest version.


Design choices
--------------

- Java 8: To support Android, the libraries needs to be compiled for Java 8 (starting at Android N)
- No transitive libraries: Since the goal is to provide a lot of diversity while reusing other libraries, it is better for you to include only the libraries you need.


Deployment instructions
-----------------------

See *DEPLOYMENT.txt* for the instructions.
