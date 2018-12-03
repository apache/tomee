index-group=Unrevised
type=page
status=published
title=DataSourceRealm and TomEE DataSource
~~~~~~

## Quick start

To test it:

    mvn clean package tomee:run

## How does it work?

A datasource is defined in tomee.xml:

    <Resource id="myDataSource" type="DataSource" /> <!-- standard properties -->

Then this datasource is referenced in server.xml:

    <Realm
        className="org.apache.catalina.realm.DataSourceRealm"
        dataSourceName="myDataSource"
        userTable="users"
        userNameCol="user_name"
        userCredCol="user_pass"
        userRoleTable="user_roles"
        roleNameCol="role_name"/>

To initialize the datasource (for the test) we used the TomEE hook which consists in providing
a file import-<datasource name>.sql. It should be in the classpath of the datasource so here it is
the TomEE classpath so we added it to lib (by default in the classloader). It simply contains the
table creations and the insertion of the "admin" "tomee" with the password "tomee".

## Test it

Go to http://localhost:8080/realm-in-tomee-1.1.0-SNAPSHOT/, then connect using
the login/password tomee/tomee. You should see "Home".
