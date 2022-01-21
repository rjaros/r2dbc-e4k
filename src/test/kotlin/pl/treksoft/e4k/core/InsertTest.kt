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

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.r2dbc.core.awaitOne
import org.springframework.test.context.junit4.SpringRunner
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@DataR2dbcTest
open class InsertTest : SqlTest() {

    @Test
    fun `should insert records without entity class`() {
        runBlocking {
            val count1 = dbClient.execute<Int>("SELECT COUNT(*) FROM users").fetch().awaitOne()
            val id = dbClient.insert().into("users", "user_id")
                .value("username", "nick")
                .value("password", "pass")
                .value("name", "John Smith")
                .awaitOne()
            assertTrue(id > 0, "should return correct id value")
            val rowsUpdated = dbClient.insert().into("users")
                .value("username", "nick2")
                .value("password", "pass2")
                .value("name", "John2 Smith2")
                .value("created_at", OffsetDateTime.now())
                .nullValue("active")
                .fetch().rowsUpdated().awaitSingle()
            assertEquals(1, rowsUpdated, "should update one row")
            val count2 = dbClient.execute<Int>("SELECT COUNT(*) FROM users").fetch().awaitOne()
            assertEquals(2, count2 - count1, "should find two new records")
        }
    }

    @Test
    fun `should insert records with entity class`() {
        runBlocking {
            val count1 = dbClient.execute<Int>("SELECT COUNT(*) FROM users").fetch().awaitOne()
            val user = dbClient.insert().into<User>().into("users")
                .using(
                    User(
                        username = "rjaros",
                        password = "pass",
                        name = "Robert Jaros",
                        createdAt = OffsetDateTime.now()
                    )
                ).awaitSingle()
            assertTrue(user.userId!! > 0, "should return user with correct id value")
            val user2 = dbClient.insert().into<User>().into("users")
                .using(
                    User(
                        username = "jbond",
                        password = "pass",
                        name = "James Bond",
                        createdAt = OffsetDateTime.now(),
                        active = false
                    )
                ).awaitSingle()
            assertTrue(user2.userId!! > 0, "should return second user with correct id value")
            val count2 = dbClient.execute<Int>("SELECT COUNT(*) FROM users").fetch().awaitOne()
            assertEquals(2, count2 - count1, "should find two new records")
        }
    }

    @Test
    fun `should insert records with bigserial keys`() {
        runBlocking {
            val count1 = dbClient.execute<Int>("SELECT COUNT(*) FROM logs").fetch().awaitOne()
            val id = dbClient.insert().into("logs", "logs_id")
                .value("description", "Test entry")
                .awaitOneLong()
            assertTrue(id > 0, "should return correct id value")
            val count2 = dbClient.execute<Int>("SELECT COUNT(*) FROM logs").fetch().awaitOne()
            assertEquals(1, count2 - count1, "should find one new records")
        }
    }
}
