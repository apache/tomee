take the openejb-provisionning.zip (or the tar.gz), extract it in the tomee webapp (or add it in the openejb classloader)

then use the Deployer (DeployerEjb) to deploy path like:
    mvn:groupId:artifactId:version:packaging
or
    mvn:groupId/artifactId/version/packaging
