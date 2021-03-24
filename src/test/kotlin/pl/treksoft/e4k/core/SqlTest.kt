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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.annotation.Id
import org.springframework.r2dbc.core.await
import java.time.OffsetDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

data class User(
    @Id
    val userId: Int? = null,
    val username: String,
    val password: String,
    val name: String,
    val description: String? = null,
    val createdAt: OffsetDateTime? = null,
    val active: Boolean? = null
)

open class SqlTest {
    @Autowired
    lateinit var dbClient: DbClient

    @BeforeTest
    fun createDbTable() {
        runBlocking {
            dbClient.execute(
                """
                    CREATE TABLE IF NOT EXISTS users (
                      user_id serial NOT NULL,
                      username varchar(255) NOT NULL,
                      password varchar(255) NOT NULL,
                      name varchar(255) NOT NULL,
                      description varchar(255),                    
                      created_at TIMESTAMP WITH TIME ZONE,
                      active BOOLEAN,
                      PRIMARY KEY (user_id),
                      UNIQUE(username)
                    );
                    INSERT INTO users (username, password, name, description, created_at, active) 
                    VALUES ('jsmith', 'pass', 'John Smith', 'A test user', CURRENT_TIMESTAMP, true);
                """.trimIndent()
            ).await()
        }
    }

    @AfterTest
    fun dropDbTable() {
        runBlocking {
            dbClient.execute(
                """
                    DROP TABLE users;
                """.trimIndent()
            ).await()
        }
    }

}
