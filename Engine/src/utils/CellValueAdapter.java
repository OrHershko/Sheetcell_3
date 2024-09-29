package utils;

import api.CellValue;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import impl.cell.value.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CellValueAdapter extends TypeAdapter<CellValue> {

    @Override
    public void write(JsonWriter out, CellValue value) throws IOException {
        out.beginObject();
        out.name("type").value(value.getClass().getSimpleName()); // שומר את סוג האובייקט
        out.name("value");
        if (value instanceof StringValue) {
            out.value(((StringValue) value).getValue());
        } else if (value instanceof NumericValue) {
            out.value(((NumericValue) value).eval());
        } else if (value instanceof BooleanValue) {
            out.value(((BooleanValue) value).eval());
        } else if (value instanceof FunctionValue) {
            FunctionValue functionValue = (FunctionValue) value;
            out.beginObject();
            out.name("functionType").value(functionValue.getFunctionType().name());
            out.name("arguments");
            out.beginArray();
            for (CellValue arg : functionValue.getArguments()) {
                write(out, arg); // ריקורסיה על הארגומנטים
            }
            out.endArray();

            // כתיבה של effectiveValue
            out.name("effectiveValue");
            if (functionValue.getValue() instanceof String) {
                out.value((String) functionValue.getValue());
            }
            else if (functionValue.getValue() instanceof FunctionValue) {
                write(out, (FunctionValue) functionValue.getValue());
            }
            else {
                System.out.println("effectiveValue is null");
                out.nullValue();
            }

            out.endObject();
        } else {
            throw new IllegalArgumentException("Unknown CellValue type");
        }

        out.endObject();
    }


    @Override
    public CellValue read(JsonReader in) throws IOException {
        in.beginObject();
        String type = null;
        String value = null;
        double number = 0;
        FunctionValue functionValue = null;

        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("type")) {
                type = in.nextString();
            }
            else if (name.equals("value")) {
                if ("StringValue".equals(type)) {
                    value = in.nextString();
                } else if ("NumericValue".equals(type)) {
                    number = in.nextDouble();
                } else if ("BooleanValue".equals(type)) {
                    value = in.nextString();
                } else if ("FunctionValue".equals(type)) {
                    functionValue = parseFunctionValue(in);
                }
            }
        }
        in.endObject();

        return switch (type) {
            case "StringValue" -> new StringValue(value);
            case "NumericValue" -> new NumericValue(number);
            case "BooleanValue" -> new BooleanValue(Boolean.parseBoolean(value));
            case "FunctionValue" -> functionValue;
            default -> throw new IllegalArgumentException("Unknown CellValue type: " + type);
        };
    }

    private FunctionValue parseFunctionValue(JsonReader in) throws IOException {
        in.beginObject();
        String functionType = null;
        List<CellValue> arguments = new ArrayList<>();
        Object effectiveValue = null;

        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("functionType")) {
                functionType = in.nextString(); // קריאת סוג הפונקציה
            } else if (name.equals("arguments")) {
                in.beginArray();
                while (in.hasNext()) {
                    arguments.add(read(in)); // ריקורסיה על הארגומנטים
                }
                in.endArray();
            } else if (name.equals("effectiveValue")) {
                // קריאת ה-effectiveValue
                if (in.peek() == JsonToken.STRING) {
                    effectiveValue = in.nextString();
                }
                else {
                    effectiveValue = parseFunctionValue(in);
                }
            }
        }
        in.endObject();

        FunctionValue functionValue = new FunctionValue(functionType, arguments);
        functionValue.setEffectiveValue(effectiveValue);
        return functionValue;
    }
}
