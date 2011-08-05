# Dynamic DAO Implementation

*Note:* this feature is not standard

OpenEBJ support EJB annotations on interfaces (in particular @Stateless and @Singleton).

The interface has to be annotated with @PersistenceContext to define which EntityManager to use.

Methods should respect these conventions:
* void save(Foo foo): persist foo
* Foo update(Foo foo): merge foo
* void delete(Foo foo): remove foo, if foo is detached it tries to attach it
* Collection<Foo>|Foo namedQuery(String name[, Map<String, ?> params, int first, int max]): run the named query called name, params contains bindings, first and max are used for magination. Last three parameters are optionnals
* Collection<Foo>|Foo nativeQuery(String name[, Map<String, ?> params, int first, int max]): run the native query called name, params contains bindings, first and max are used for magination. Last three parameters are optionnals
* Collection<Foo>|Foo query(String value [, Map<String, ?> params, int first, int max]): run the query put as first parameter, params contains bindings, first and max are used for magination. Last three parameters are optionnals
* Collection<Foo> findAll([int first, int max]): find all Foo, parameters are used for pagination
* Collection<Foo> findByBar1AndBar2AndBar3(<bar 1 type> bar1, <bar 2 type> bar2, <bar3 type> bar3 [, int first, int max]): find all Foo with specified field values for bar1, bar2, bar3.

Dynamic finder can have as much as you want field constraints. For String like is used and for other type equals is used. See UserDao for an example and DynamicUserDaoTest for an usage example. 

# Download

[Download as zip](${zip})

# Files

## APIs

${apis}

## Java files

${javas}

## Resource files

${resources}
