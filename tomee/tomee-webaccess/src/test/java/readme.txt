If there is at least one file (Java or not) in src/main/java,
then all files in src/main/groovy will be found.
If, however, src/main/java is empty, then src/main/groovy will be ignored.
You can get around this by placing an empty file in src/main/java just so that src/main/groovy
will be recognized. The same is true for src/test/java and src/test/groovy.
This is actually a workaround for GRECLIPSE-1221.