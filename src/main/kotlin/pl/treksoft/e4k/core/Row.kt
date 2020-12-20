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

import io.r2dbc.spi.Row
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun Row.bool(name: String): Boolean = this.boolOrNull(name)!!

fun Row.boolOrNull(name: String): Boolean? = this.get(name, java.lang.Boolean::class.java)?.booleanValue()

fun Row.int(name: String): Int = this.intOrNull(name)!!

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
fun Row.intOrNull(name: String): Int? {
    val v = this[name]
    return when (v) {
        is Integer -> v.toInt()
        is java.lang.Short -> v.toInt()
        else -> null
    }
}

fun Row.string(name: String): String = this.stringOrNull(name)!!

fun Row.stringOrNull(name: String): String? = this.get(name, String::class.java)

fun Row.date(name: String): LocalDate = this.dateOrNull(name)!!

fun Row.dateOrNull(name: String): LocalDate? =
    this.get(name, LocalDate::class.java)

fun Row.datetime(name: String): LocalDateTime = this.datetimeOrNull(name)!!

fun Row.datetimeOrNull(name: String): LocalDateTime? =
    this.get(name, LocalDateTime::class.java)

fun Row.time(name: String): LocalTime = this.timeOrNull(name)!!

fun Row.timeOrNull(name: String): LocalTime? =
    this.get(name, LocalTime::class.java)

fun Row.bigDecimal(name: String): BigDecimal = this.bigDecimalOrNull(name)!!

fun Row.bigDecimalOrNull(name: String): BigDecimal? =
    this.get(name, BigDecimal::class.java)
