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
import org.springframework.r2dbc.core.awaitRowsUpdated
import org.springframework.test.context.junit4.SpringRunner
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(SpringRunner::class)
@DataR2dbcTest
open class UpdateTest : SqlTest() {

    @Test
    fun `should update record without entity class`() {
        runBlocking {
            dbClient.insert().into("users")
                .value("username", "nick")
                .value("password", "pass")
                .value("name", "John Smith")
                .await()
            val rowsUpdated = dbClient.update().table("users")
                .setNullable<String>("description")
                .set("created_at", OffsetDateTime.now())
                .fetch().awaitRowsUpdated()
            assertEquals(2, rowsUpdated, "should update two rows")
            val rowsUpdated2 = dbClient.update().table("users")
                .set("description", "updated")
                .setNullable<OffsetDateTime>("created_at")
                .set("active", false)
                .matching("username = :username", mapOf("username" to "nick"))
                .fetch().awaitRowsUpdated()
            assertEquals(1, rowsUpdated2, "should update one row")
            val rowsUpdated3 = dbClient.update().table("users")
                .using {
                    Update.update("description", "updated")
                        .setNullable<OffsetDateTime>("created_at")
                        .set("active", false)
                }.matching("username = :username", mapOf("username" to "nick"))
                .fetch().awaitRowsUpdated()
            assertEquals(1, rowsUpdated3, "should update one row")
        }
    }

    @Test
    fun `should update record with entity class`() {
        runBlocking {
            dbClient.insert().into("users")
                .value("username", "nick")
                .value("password", "pass")
                .value("name", "John Smith")
                .await()
            val user = dbClient.execute<User>("SELECT * FROM users WHERE username = :username").bind("username", "nick")
                .fetch().awaitOne()
            assertNotNull(user.id)
            assertEquals("nick", user.username)
            assertEquals("pass", user.password)
            assertEquals("John Smith", user.name)
            assertNull(user.description)
            assertNull(user.createdAt)
            assertNull(user.active)
            val now = OffsetDateTime.now()
            val oldId = user.id
            val newUser = user.copy(description = "description", createdAt = now, active = true)
            val rowsUpdated =
                dbClient.update().table<User>().inTable("users").using(newUser, dbClient).awaitSingle()
            assertEquals(1, rowsUpdated, "should update one row")
            val changedUser =
                dbClient.execute<User>("SELECT * FROM users WHERE username = :username").bind("username", "nick")
                    .fetch().awaitOne()
            assertNotNull(changedUser.id)
            assertEquals(oldId, changedUser.id)
            assertEquals("nick", changedUser.username)
            assertEquals("pass", changedUser.password)
            assertEquals("John Smith", changedUser.name)
            assertEquals("description", changedUser.description)
            assertEquals(now, changedUser.createdAt)
            assertEquals(true, changedUser.active)
        }
    }
}
