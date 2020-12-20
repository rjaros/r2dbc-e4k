# r2dbc-e4k

Kotlin extensions for Spring Data R2DBC

[![Download](https://api.bintray.com/packages/rjaros/kotlin/r2dbc-e4k/images/download.svg) ](https://bintray.com/rjaros/kotlin/kvision/_latestVersion)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Introduction

This library is designed as a workaround for the changes and deprecations introduced in Spring Data R2DBC 1.2.0.
Although the reason for these changes is explained in detail in [the migration guide](https://docs.spring.io/spring-data/r2dbc/docs/1.2.2/reference/html/#upgrading.1.1-1.2), 
there is no clear migration path for some use cases. Especially those concerning mixing entity classes and raw SQL statements and using fluent API without entity classes.

Some issues have been opened on GitHub ([#491](https://github.com/spring-projects/spring-data-r2dbc/issues/491), [#500](https://github.com/spring-projects/spring-data-r2dbc/issues/500)), unfortunately it doesn't seem they will be addressed.

This library takes advantage of the Kotlin language extension functions and tries to deliver API,
which is as similar to the 1.1.x `DatabaseClient` API as possible, without any deprecations being used. 
It should allow fairly easy migration of medium or even large projects created with old, now deprecated API.

## Limitations

* The API of this library is not one to one compatible with Spring Data R2DBC 1.1.x. In particular, it does not use `Criteria` API for `UPDATE` and `DELETE` operations. Instead plain SQL code is used.
* The API is designed for Kotlin. It probably can't be used from Java.
* The API is designed and tested with Kotlin coroutines. It wasn't tested with plain Spring Reactor, although it may work fine.

## Samples

```kotlin
// Spring Data R2DBC 1.1.x

val count = databaseClient.execute("SELECT COUNT(*) FROM users")
    .`as`(Integer::class.java).fetch().awaitOne().toInt()

// r2dbc-e4k

val count = dbClient.execute<Int>("SELECT COUNT(*) FROM users").fetch().awaitOne()

// Spring Data R2DBC 1.1.x

val user = databaseClient.execute("SELECT * FROM users WHERE username = :username LIMIT 1")
    .`as`(User::class.java).bind("username", "jsmith").fetch().awaitOneOrNull()

// r2dbc-e4k

val user = dbClient.execute<User>("SELECT * FROM users WHERE username = :username LIMIT 1")
    .bind("username", "jsmith").fetch().awaitOneOrNull()

// Spring Data R2DBC 1.1.x

val rowsUpdated = databaseClient.insert().into("users")
    .value("username", "nick")
    .value("password", "pass")
    .value("name", "John Smith")
    .value("created_at", OffsetDateTime.now())
    .nullValue("active", java.lang.Boolean::class.java)
    .fetch().rowsUpdated().awaitSingle()

// r2dbc-e4k

val rowsUpdated = dbClient.insert().into("users")
    .value("username", "nick")
    .value("password", "pass")
    .value("name", "John Smith")
    .value("created_at", OffsetDateTime.now())
    .nullValue("active")
    .fetch().rowsUpdated().awaitSingle()

// Spring Data R2DBC 1.1.x

val newId = databaseClient.insert().into("users")
    .value("username", "nick")
    .value("password", "pass")
    .value("name", "John Smith")
    .map { row -> row.get("id") }
    .awaitOne() as Int

// r2dbc-e4k

val newId = dbClient.insert().into("users", "id")
    .value("username", "nick")
    .value("password", "pass")
    .value("name", "John Smith")
    .awaitOne()

// Spring Data R2DBC 1.1.x

val rowsUpdated = databaseClient.update().table("users").using(
    Update.update("description", null)
        .set("created_at", OffsetDateTime.now())
).matching(where("username").`is`("nick"))
    .fetch().awaitRowsUpdated()

// r2dbc-e4k

val rowsUpdated = dbClient.update().table("users").using {
    Update.update("description", null)
        .set("created_at", OffsetDateTime.now())
}.matching("username = :username", mapOf("username" to "nick"))
    .fetch().awaitRowsUpdated()

// Spring Data R2DBC 1.1.x

val rowsUpdated = databaseClient.update().table(User::class.java).table("users")
    .using(updatedUser).fetch().awaitRowsUpdated()

// r2dbc-e4k

val rowsUpdated = dbClient.update().table<User>().inTable("users")
    .using(updatedUser, dbClient).awaitSingle()

// Spring Data R2DBC 1.1.x

val rowsDeleted = databaseClient.delete().from("users")
    .matching(where("username").`is`("nick"))
    .fetch().awaitRowsUpdated()

// r2dbc-e4k

val rowsDeleted = dbClient.delete().from("users")
    .matching("username = :username", mapOf("username" to "nick"))
    .fetch().awaitRowsUpdated()
```

## Using

The artifacts for this project are available on Bintray.
To use it in your project add the following code in your `build.gradle.kts` file.

    repositories {
        ...
        maven { url = uri("https://dl.bintray.com/rjaros/kotlin") }
    }

    dependencies {
        ...
        implementation("pl.treksoft:r2dbc-e4k:0.0.1")
    }

The `DbClient` bean is automatically available to your Spring application. Inject it inside your components instead of `DatabaseClient` from Spring Data R2DBC.

## Query builder

This project incorporates and enhances `QueryBuilder` class from an unmaintained but awesome project [Kwery](https://github.com/andrewoma/kwery) by Andrew O'Malley. This class allows you to build SQL queries with a simple DSL and is integrated with a number of R2DBC methods in this project.

## Leave us a star

If you like this project, please give it a star on GitHub. Thank you!
