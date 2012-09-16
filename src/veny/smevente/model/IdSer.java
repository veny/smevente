package veny.smevente.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.orientechnologies.orient.core.id.ORID;

public class IdSer extends JsonSerializer<ORID> {

    @Override
    public void serialize(ORID arg0, JsonGenerator jgen, SerializerProvider arg2)
            throws IOException, JsonProcessingException {
        System.out.println("customer JsonDeserializer");
        jgen.writeString(arg0.toString());

    }

}
