/*
 * Copyright (c) 2020 Robert Jaros
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package pl.treksoft.e4k.core

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.r2dbc.core.awaitOne
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.r2dbc.core.flow
import org.springframework.test.context.junit4.SpringRunner
import pl.treksoft.e4k.query.query
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@DataR2dbcTest
open class ExecuteTest : SqlTest() {

    @Test
    fun `should select values without entity class`() {
        runBlocking {
            val count = dbClient.execute<Int>("SELECT COUNT(*) FROM users").fetch().awaitOne()
            assertEquals(1, count, "should return number of records")
            val id = dbClient.execute<Int>("SELECT user_id FROM users WHERE username = :username").bind("username", "jsmith")
                .fetch().awaitOne()
            assertTrue(id > 0, "should return correct id of the user")
            val map = dbClient.execute("SELECT name, description FROM users WHERE username = :username")
                .bind("username", "jsmith").fetch().awaitOne()
            assertEquals(
                mapOf("name" to "John Smith", "description" to "A test user"),
                map,
                "should select correct values from the database"
            )
            assertEquals("John Smith", map.string("name"), "should extract value from a resulting map")
            assertEquals("A test user", map.stringOrNull("description"), "should extract value from a resulting map")
        }
    }

    @Test
    fun `should select values with entity class`() {
        runBlocking {
            val users = dbClient.execute<User>("SELECT * FROM users").fetch().flow().toList()
            assertEquals(1, users.size, "should return correct number of rows")
            val nousers = dbClient.execute<User>("SELECT * FROM users WHERE false").fetch().flow().toList()
            assertTrue(nousers.isEmpty(), "should return zero rows")
            val smiths = dbClient.execute<User>("SELECT * FROM users WHERE username = :username")
                .bind("username", "jsmith").fetch().flow().toList()
            assertEquals(1, smiths.size, "should return correct number of rows selected with a condition")
            val firstSmith = dbClient.execute<User>("SELECT * FROM users WHERE username = :username LIMIT 1")
                .bind("username", "jsmith").fetch().awaitOneOrNull()
            assertNotNull(firstSmith, "should select single row")
            val computedUser =
                dbClient.execute<User>("SELECT 5 as user_id, 'jbond' as username, 'pass' as password, 'James Bond' as name")
                    .fetch().awaitOne()
            assertEquals(5, computedUser.userId, "should return correct values of the computed record")
            assertEquals("jbond", computedUser.username, "should return correct values of the computed record")
            assertEquals("pass", computedUser.password, "should return correct values of the computed record")
            assertEquals("James Bond", computedUser.name, "should return correct values of the computed record")
            val smiths2 = dbClient.execute<User>("SELECT * FROM users WHERE username = ?")
                .bind(0, "jsmith").fetch().flow().toList()
            assertEquals(1, smiths2.size, "should return correct number of rows selected with an indexed condition")
        }
    }

    @Test
    fun `should select values using query builder`() {
        runBlocking {
            val query = query {
                select("SELECT * FROM users")
                whereGroup {
                    where("username = :username")
                    parameter("username", "jsmith")
                    where("password = :password")
                    parameter("password", "pass")
                    where("name = :name")
                    parameter("name", "John Smith")
                }
            }
            val users = dbClient.execute<User>(query).flow().toList()
            assertEquals(1, users.size, "should return one record")
            val emptyQuery = query {
                select("SELECT * FROM users")
                whereGroup {
                    where("username = :username")
                    parameter("username", "jsmith")
                    where("password = :password")
                    parameter("password", "pass")
                    where("name = :name")
                    parameter("name", "James Bond")
                }
            }
            val emptyUsers = dbClient.execute<User>(emptyQuery).flow().toList()
            assertEquals(0, emptyUsers.size, "should return no records")
        }
    }

}
