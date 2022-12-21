//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.geotools.geojson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.time.FastDateFormat;
import org.geotools.util.Converters;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.locationtech.jts.geom.Coordinate;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 * @exclude
 *
 * */
public class GeoJSONUtil {
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");
    public static final FastDateFormat dateFormatter;

    public GeoJSONUtil() {
    }

    public static Reader toReader(Object input) throws IOException {
        if (input instanceof BufferedReader) {
            return (BufferedReader) input;
        } else if (input instanceof Reader) {
            return new BufferedReader((Reader) input);
        } else if (input instanceof InputStream) {
            return new BufferedReader(new InputStreamReader((InputStream) input));
        } else if (input instanceof File) {
            return new BufferedReader(new FileReader((File) input));
        } else if (input instanceof String) {
            return new StringReader((String) input);
        } else {
            throw new IllegalArgumentException("Unable to turn " + input + " into a reader");
        }
    }

    public static Writer toWriter(final Object output) throws IOException {
        if (output instanceof OutputStreamWriter) {
            return new Writer() {
                Writer writer = new BufferedWriter((Writer) output);

                public void write(char[] cbuf, int off, int len) throws IOException {
                    this.writer.write(cbuf, off, len);
                }

                public void flush() throws IOException {
                    this.writer.flush();
                }

                public void close() throws IOException {
                }
            };
        } else if (output instanceof BufferedWriter) {
            return (BufferedWriter) output;
        } else if (output instanceof Writer) {
            return new BufferedWriter((Writer) output);
        } else if (output instanceof OutputStream) {
            return new BufferedWriter(new OutputStreamWriter((OutputStream) output));
        } else if (output instanceof File) {
            return new BufferedWriter(new FileWriter((File) output));
        } else if (output instanceof String) {
            return new BufferedWriter(new FileWriter((String) output));
        } else {
            throw new IllegalArgumentException("Unable to turn " + output + " into a writer");
        }
    }

    public static StringBuilder string(String string, StringBuilder sb) {
        sb.append("\"").append(JSONObject.escape(string)).append("\"");
        return sb;
    }

    public static StringBuilder entry(String key, Object value, StringBuilder sb) {
        string(key, sb).append(":");
        value(value, sb);
        return sb;
    }

    private static void value(Object value, StringBuilder sb) {
        if (value == null) {
            nul(sb);
        } else if (value.getClass().isArray()) {
            array(value, sb);
        } else if (!(value instanceof Number) && !(value instanceof Boolean) && !(value instanceof Date)) {
            String str = (String) Converters.convert(value, String.class);
            if (str == null) {
                str = value.toString();
            }

            string(str, sb);
        } else {
            literal(value, sb);
        }

    }

    private static void array(Object array, StringBuilder sb) {
        sb.append("[");
        int length = Array.getLength(array);

        for (int i = 0; i < length; ++i) {
            Object value = Array.get(array, i);
            value(value, sb);
            if (i < length - 1) {
                sb.append(", ");
            }
        }

        sb.append("]");
    }

    static StringBuilder literal(Object value, StringBuilder sb) {
        return value instanceof Date ? string(dateFormatter.format((Date) value), sb) : sb.append(value);
    }

    public static StringBuilder array(String key, Object value, StringBuilder sb) {

        ObjectMapper mapper = new ObjectMapper();

        try {
            String json = mapper.writeValueAsString(value);

            return string(key, sb).append(":").append(json);
        } catch (JsonProcessingException e) {
            return string(key, sb).append(":").append(value);
        }


    }

    public static StringBuilder nul(StringBuilder sb) {
        sb.append("null");
        return sb;
    }

    public static <T> T trace(T handler, Class<T> clazz) {
        return (T) Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class[]{clazz}, new TracingHandler(handler));
    }

    public static boolean addOrdinate(List<Object> ordinates, Object value) {
        if (ordinates != null) {
            ordinates.add(value);
        }

        return true;
    }

    public static Coordinate createCoordinate(List ordinates) throws ParseException {
        Coordinate c = new Coordinate();
        if (ordinates.size() <= 1) {
            throw new ParseException(2, "Too few ordinates to create coordinate");
        } else {
            if (ordinates.size() > 1) {
                c.x = ((Number) ordinates.get(0)).doubleValue();
                c.y = ((Number) ordinates.get(1)).doubleValue();
            }

            if (ordinates.size() > 2) {
                c.setZ(((Number) ordinates.get(2)).doubleValue());
            }

            return c;
        }
    }

    public static Coordinate[] createCoordinates(List<Coordinate> coordinates) {
        return (Coordinate[]) coordinates.toArray(new Coordinate[coordinates.size()]);
    }

    public static <T> T parse(IContentHandler<T> handler, Object input, boolean trace) throws IOException {
        Reader reader = toReader(input);
        Throwable var4 = null;

        Object var6;
        try {
            if (trace) {
                handler = (IContentHandler) Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class[]{IContentHandler.class}, new TracingHandler(handler));
            }

            JSONParser parser = new JSONParser();

            try {
                parser.parse(reader, handler);
                var6 = handler.getValue();
            } catch (ParseException var16) {
                throw (IOException) (new IOException()).initCause(var16);
            }
        } catch (Throwable var17) {
            var4 = var17;
            throw var17;
        } finally {
            if (reader != null) {
                if (var4 != null) {
                    try {
                        reader.close();
                    } catch (Throwable var15) {
                        var4.addSuppressed(var15);
                    }
                } else {
                    reader.close();
                }
            }

        }

        return (T) var6;
    }

    public static void encode(String json, Object output) throws IOException {
        Writer w = toWriter(output);
        Throwable var3 = null;

        try {
            w.write(json);
            w.flush();
        } catch (Throwable var12) {
            var3 = var12;
            throw var12;
        } finally {
            if (w != null) {
                if (var3 != null) {
                    try {
                        w.close();
                    } catch (Throwable var11) {
                        var3.addSuppressed(var11);
                    }
                } else {
                    w.close();
                }
            }

        }

    }

    public static void encode(Map<String, Object> obj, Object output) throws IOException {
        Writer w = toWriter(output);
        Throwable var3 = null;

        try {
            JSONObject.writeJSONString(obj, w);
            w.flush();
        } catch (Throwable var12) {
            var3 = var12;
            throw var12;
        } finally {
            if (w != null) {
                if (var3 != null) {
                    try {
                        w.close();
                    } catch (Throwable var11) {
                        var3.addSuppressed(var11);
                    }
                } else {
                    w.close();
                }
            }

        }

    }

    static {
        dateFormatter = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZ", TIME_ZONE);
    }
}
