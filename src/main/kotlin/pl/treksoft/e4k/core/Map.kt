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

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.util.*

fun Map<String, Any?>.bool(name: String): Boolean = this.boolOrNull(name)!!

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
fun Map<String, Any?>.boolOrNull(name: String): Boolean? = (this[name] as? java.lang.Boolean)?.booleanValue()

fun Map<String, Any?>.int(name: String): Int = this.intOrNull(name)!!

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
fun Map<String, Any?>.intOrNull(name: String): Int? {
    val v = this[name]
    return when (v) {
        is Integer -> v.toInt()
        is java.lang.Short -> v.toInt()
        is java.lang.Long -> v.toInt()
        else -> null
    }
}

fun Map<String, Any?>.long(name: String): Long = this.longOrNull(name)!!

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
fun Map<String, Any?>.longOrNull(name: String): Long? = (this[name] as? java.lang.Long)?.toLong()

fun Map<String, Any?>.string(name: String): String = this.stringOrNull(name)!!

fun Map<String, Any?>.stringOrNull(name: String): String? = (this[name] as? String)

fun Map<String, Any?>.date(name: String): LocalDate = this.dateOrNull(name)!!

fun Map<String, Any?>.dateOrNull(name: String): LocalDate? =
    (this[name] as? LocalDate)

fun Map<String, Any?>.time(name: String): LocalTime = this.timeOrNull(name)!!

fun Map<String, Any?>.timeOrNull(name: String): LocalTime? =
    (this[name] as? LocalTime)

fun Map<String, Any?>.datetime(name: String): LocalDateTime = this.datetimeOrNull(name)!!

fun Map<String, Any?>.datetimeOrNull(name: String): LocalDateTime? =
    (this[name] as? LocalDateTime)

fun Map<String, Any?>.offsetTime(name: String): OffsetTime = this.offsetTimeOrNull(name)!!

fun Map<String, Any?>.offsetTimeOrNull(name: String): OffsetTime? =
    (this[name] as? OffsetTime)

fun Map<String, Any?>.offsetDatetime(name: String): OffsetDateTime = this.offsetDatetimeOrNull(name)!!

fun Map<String, Any?>.offsetDatetimeOrNull(name: String): OffsetDateTime? =
    (this[name] as? OffsetDateTime)

fun Map<String, Any?>.bigDecimal(name: String): BigDecimal = this.bigDecimalOrNull(name)!!

fun Map<String, Any?>.bigDecimalOrNull(name: String): BigDecimal? =
    (this[name] as? BigDecimal)

fun Map<String, Any?>.uuid(name: String): UUID = this.uuidOrNull(name)!!

fun Map<String, Any?>.uuidOrNull(name: String): UUID? =
    (this[name] as? UUID)
