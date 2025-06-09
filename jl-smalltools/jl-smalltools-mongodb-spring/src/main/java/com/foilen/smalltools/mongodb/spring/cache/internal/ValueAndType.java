package com.foilen.smalltools.mongodb.spring.cache.internal;

import com.foilen.smalltools.reflection.ReflectionTools;
import com.foilen.smalltools.restapi.model.AbstractApiBase;
import com.foilen.smalltools.tools.JsonTools;

public class ValueAndType extends AbstractApiBase {

    private String jsonValue;
    private String type;

    public Object toValue() {
        if (this.type == null) {
            return null;
        }

        Class<?> type = ReflectionTools.safelyGetClass(this.type);
        if (type == null) {
            throw new IllegalArgumentException("The type is unknown " + this.type);
        }
        return JsonTools.readFromString(jsonValue, type);
    }

    public String getJsonValue() {
        return jsonValue;
    }

    public ValueAndType setJsonValue(String jsonValue) {
        this.jsonValue = jsonValue;
        return this;
    }

    public String getType() {
        return type;
    }

    public ValueAndType setType(String type) {
        this.type = type;
        return this;
    }

}
