#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

import sys
import os
global AdminConfig
global AdminTask

#-----------------------------------------------------------------
# getName - Returns the base name of the config object.
#-----------------------------------------------------------------
def getName (objectId):
	endIndex = (objectId.find("(c") - 1)
	stIndex = 0
	if (objectId.find("\"") == 0):
		stIndex = 1
	#endIf
	return objectId[stIndex:endIndex+1]
#endDef

#-----------------------------------------------------------------
# getNodeId - Return the node id of the existing node.  If in an
#             ND environment, returns the first node in the list.
#-----------------------------------------------------------------
def getNodeId ():
	nodeList = AdminConfig.list("Node").split("\n")

	node=""
	if (len(nodeList) == 1):
		node = nodeList[0]
	#endIf
   
	return node
#endDef

#-----------------------------------------------------------------
# getServerId - Return the server id of the existing server. If
#           more than one server exists, returns the first server
#           in the list.
#-----------------------------------------------------------------
def getServerId ():
	serverList = AdminConfig.list("Server").split("\n")
	
	server = serverList[0]
	return server
#endDef

def addDatasourceProperty (datasourceId, name, value):
    parms = ["-propertyName", name, "-propertyValue", value]
    AdminTask.setResourceProperty(datasourceId, parms)
#endDef 

# Set the default database provider to Derby
DefaultProviderType="Derby"
DefaultProviderName="Derby JDBC Provider"
DefaultPathName="${DERBY_JDBC_DRIVER_PATH}/derby.jar"
DefaultNativePathName= ""

# If in an ND environment with multiple nodes or servers
# specify the target node and server name
TargetNodeName = ""
TargetServerName = ""

if (TargetNodeName == ""):
	TargetNodeName = getName(getNodeId())
#endIf

if (TargetServerName == ""):
	TargetServerName = getName(getServerId())
#endIf

print "TargetNodeName: " + TargetNodeName
print "TargetServerName: " + TargetServerName

# Build the scope for the Derby provider
scope = AdminConfig.getid("/Node:"+TargetNodeName+"/Server:"+TargetServerName+"/")

scopeName = getName(scope)
print "Scope: " + scopeName

providerId = AdminConfig.getid("/Server:"+scopeName+"/JDBCProvider:\""+DefaultProviderName+"\"/" )

if (providerId == ""):
	print "Creating new JDBC provider"
	providerId = AdminTask.createJDBCProvider('[-scope Node='+TargetNodeName+',Server='+TargetServerName+' -databaseType Derby -providerType "Derby JDBC Provider" -implementationType "Connection pool data source" -name "Derby JDBC Provider" -description "Derby embedded non-XA JDBC Provider." -classpath [${DERBY_JDBC_DRIVER_PATH}/derby.jar ] -nativePath "" ]')
#endIf

print "Creating new JDBC data sources"
dataSourceId = AdminTask.createDatasource(providerId, '[-name "OpenBooks Data Source" -jndiName jdbc/OpenBooks -dataStoreHelperClassName com.ibm.websphere.rsadapter.DerbyDataStoreHelper -containerManagedPersistence false -componentManagedAuthenticationAlias -configureResourceProperties [[databaseName java.lang.String OpenBooks]]')
addDatasourceProperty(dataSourceId, "createDatabase", "create")
nonTxDataSourceId = AdminTask.createDatasource(providerId, '[-name "OpenBooks Non-transactional Data Source" -jndiName jdbc/NonTxOpenBooks -dataStoreHelperClassName com.ibm.websphere.rsadapter.DerbyDataStoreHelper -containerManagedPersistence false -componentManagedAuthenticationAlias -configureResourceProperties [[databaseName java.lang.String OpenBooks]]')
addDatasourceProperty(nonTxDataSourceId, "nonTransactionalDataSource", "true")

print "Saving configuration"
AdminConfig.save( )
print "Done"