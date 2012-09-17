package veny.smevente.server;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.orientechnologies.orient.core.id.ORID;

/**
 * Special class to serialize the OrientDB's <code>ORecordId</code> into JSON.
 * <p/>
 * It solves the problem with cyclic associations that results into
 * <pre>
 * Direct self-reference leading to cycle (through reference chain: java.util.HashMap["units"]->
 * java.util.ArrayList[0]->veny.smevente.model.Unit["id"]->com.orientechnologies.orient.core.id.ORecordId["identity"])
 * </pre>
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 17.9.2012
 */
public class OrientdbRid2JsonSerializer extends JsonSerializer<ORID> {

    /**
     * Method that can be called to ask implementation to serialize values of type this serializer handles.
     *
     * @param rid rid to serialize; can not be null.
     * @param jgen generator used to output resulting JSON content
     * @param provider provider that can be used to get serializers for serializing Objects value contains, if any
     *
     * @throws IOException if something goes wrong
     */
    @Override
    public void serialize(final ORID rid, final JsonGenerator jgen, final SerializerProvider provider)
        throws IOException {

        jgen.writeString(rid.toString());
    }

}
