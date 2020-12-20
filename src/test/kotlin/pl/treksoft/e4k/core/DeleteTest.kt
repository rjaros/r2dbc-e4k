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

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.core.allAndAwait
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.r2dbc.core.awaitOne
import org.springframework.r2dbc.core.awaitRowsUpdated
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@DataR2dbcTest
open class DeleteTest : SqlTest() {

    @Test
    fun `should delete record without entity class`() {
        runBlocking {
            dbClient.insert().into("users")
                .value("username", "nick")
                .value("password", "pass")
                .value("name", "John Smith")
                .await()
            val count1 = dbClient.execute<Int>("SELECT COUNT(*) FROM users").fetch().awaitOne()
            val rowsUpdated = dbClient.delete().from("users")
                .matching("username = :username", mapOf("username" to "nick"))
                .fetch().awaitRowsUpdated()
            assertEquals(1, rowsUpdated, "should delete one row")
            val count2 = dbClient.execute<Int>("SELECT COUNT(*) FROM users").fetch().awaitOne()
            assertEquals(1, count1 - count2, "should find one record less")
        }
    }

    @Test
    fun `should delete record with entity class`() {
        runBlocking {
            dbClient.insert().into("users")
                .value("username", "nick")
                .value("password", "pass")
                .value("name", "John Smith")
                .await()
            val count1 = dbClient.execute<Int>("SELECT COUNT(*) FROM users").fetch().awaitOne()
            val rowsUpdated = dbClient.delete().from<User>().from("users")
                .matching(Query.query(where("username").`is`("nick")))
                .allAndAwait()
            assertEquals(1, rowsUpdated, "should delete one row")
            val count2 = dbClient.execute<Int>("SELECT COUNT(*) FROM users").fetch().awaitOne()
            assertEquals(1, count1 - count2, "should find one record less")
        }
    }
}
