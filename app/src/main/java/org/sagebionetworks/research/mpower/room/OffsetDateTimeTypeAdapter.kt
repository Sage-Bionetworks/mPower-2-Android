package org.sagebionetworks.research.mpower.room

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.threeten.bp.OffsetDateTime
import java.io.IOException

class OffsetDateTimeTypeAdapter : TypeAdapter<OffsetDateTime>() {
    @Throws(IOException::class)
    override fun read(reader: JsonReader): OffsetDateTime {
        val src = reader.nextString()
        return OffsetDateTime.parse(src)
    }

    @Throws(IOException::class)
    override fun write(writer: JsonWriter, offsetDate: OffsetDateTime?) {
        if (offsetDate != null) {
            writer.value(offsetDate.toString())
        } else {
            writer.nullValue()
        }
    }
}