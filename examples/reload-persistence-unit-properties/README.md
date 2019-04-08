index-group=Unrevised
type=page
status=published
title=Reload Persistence Unit Properties
~~~~~~

This example aims to simulate a benchmark campaign on JPA.

First you'll run your application then you'll realize you could need L2 cache to respect your SLA.

So you change your persistence.xml configuration, then restart your application,
you wait a bit because you are using OpenEJB ;)...but you wait...

So to try to go faster on long campaign simply change your configuration at runtime to test it then when it works change
your configuration file to keep the modification.

To do it we can simply use JMX.

OpenEJB automatically register one MBeans by entitymanager (persistence unit).

It allows you mainly to change your persistence unit properties even if more is possible.

## The test itself

The test is simple: we persist an entity, we query it three times without cache then we activate cache and realize
running again our queries that one is enough to do the same.

# The output

In the ouput we find the 3 parts described just before.

    INFO - TEST, data initialization
    DEBUG - <t 1523828380, conn 93608538> executing stmnt 1615782385 CREATE TABLE Person (id BIGINT NOT NULL, name VARCHAR(255), PRIMARY KEY (id))
    DEBUG - <t 1523828380, conn 93608538> [1 ms] spent
    DEBUG - <t 1523828380, conn 1506565411> executing prepstmnt 668144844 INSERT INTO Person (id, name) VALUES (?, ?) [params=?, ?]
    DEBUG - <t 1523828380, conn 1506565411> [0 ms] spent
    INFO - TEST, end of data initialization


    INFO - TEST, doing some queries without cache
    DEBUG - <t 1523828380, conn 1506565411> executing prepstmnt 1093240870 SELECT t0.name FROM Person t0 WHERE t0.id = ? [params=?]
    DEBUG - <t 1523828380, conn 1506565411> [0 ms] spent
    DEBUG - <t 1523828380, conn 1506565411> executing prepstmnt 1983702821 SELECT t0.name FROM Person t0 WHERE t0.id = ? [params=?]
    DEBUG - <t 1523828380, conn 1506565411> [0 ms] spent
    DEBUG - <t 1523828380, conn 1506565411> executing prepstmnt 1178041898 SELECT t0.name FROM Person t0 WHERE t0.id = ? [params=?]
    DEBUG - <t 1523828380, conn 1506565411> [1 ms] spent
    INFO - TEST, queries without cache done


    INFO - TEST, doing some queries with cache
    DEBUG - <t 1523828380, conn 1506565411> executing prepstmnt 1532943889 SELECT t0.name FROM Person t0 WHERE t0.id = ? [params=?]
    DEBUG - <t 1523828380, conn 1506565411> [0 ms] spent
    INFO - TEST, queries with cache done
