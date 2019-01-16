package utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public class JsonUtils {
    private static Object readData(Object data)
    {
        Object result = null;
        try
        {
            if(data instanceof List<?>) {
                JSONArray array = new JSONArray();
                for(Object item : (List<?>) data)
                { array.put(JsonUtils.readData(item)); }
                result = array;
            }
            else {
                JSONObject object = new JSONObject();
                Field[] fields = data.getClass().getFields();
                for(Field field : fields)
                {
                    Object fieldData = field.get(data);
                    if(fieldData != null)
                    {
                        Class<?> cls = field.getType();
                        if(cls == String.class)
                        { object.put(field.getName(), fieldData); }
                        else if(cls == Integer.class || cls == int.class)
                        {
                            try { object.put(field.getName(), ((Integer) fieldData).intValue()); }
                            catch(Exception ex) { }
                        }
                        else if(cls == Double.class || cls == double.class)
                        {
                            try { object.put(field.getName(), ((Double) fieldData).doubleValue()); }
                            catch(Exception ex) { }
                        }
                        else if(cls == Long.class || cls == long.class)
                        {
                            try { object.put(field.getName(), ((Long) fieldData).longValue()); }
                            catch(Exception ex) { }
                        }
                        else if(cls == Boolean.class || cls == boolean.class)
                        {
                            try { object.put(field.getName(), ((Boolean) fieldData).booleanValue()); }
                            catch(Exception ex) { }
                        }
                        else
                        {
                            Object info = JsonUtils.readData(fieldData);
                            if(info != null) { object.put(field.getName(), info); }
                        }
                    }
                }
                result = object;
            }
        }
        catch(Exception ex) { }
        return result;
    }
    private static void writeData(Object data, JSONObject json)
    {
        if(json == null) return;
        try
        {
            JSONArray names = json.names();
            if(names == null) return;
            for(int i = 0; i < names.length(); i++)
            {
                String name = names.getString(i);
                Field field = data.getClass().getField(name);
                Class<?> cls = field.getType();
                if(cls == String.class) { field.set(data, json.getString(name)); }
                else if(cls == Integer.class || cls == int.class) { field.set(data, json.getInt(name)); }
                else if(cls == Double.class || cls == double.class) { field.set(data, json.getDouble(name)); }
                else if(cls == Long.class || cls == long.class) { field.set(data, json.getLong(name)); }
                else if(cls == Boolean.class || cls == boolean.class) { field.set(data, json.getBoolean(name)); }
                else
                {
                    Object nested = createInstance(cls);
                    if(nested instanceof List<?>)
                    {
                        Method add = List.class.getDeclaredMethod("add", Object.class);
                        ParameterizedType genericType = ((ParameterizedType)field.getGenericType());
                        Class<?> type = (Class<?>)genericType.getActualTypeArguments()[0];
                        if(type != null)
                        {
                            JSONArray array = json.getJSONArray(name);
                            if(array != null)
                            {
                                for(int j = 0; j < array.length(); j++)
                                {
                                    Object item = createInstance(type);
                                    cls = item.getClass();
                                    if(cls == String.class) { field.set(item, array.getString(j)); }
                                    else if(cls == Integer.class || cls == int.class) { field.set(item, array.getInt(j)); }
                                    else if(cls == Double.class || cls == double.class) { field.set(item, array.getDouble(j)); }
                                    else if(cls == Long.class || cls == long.class) { field.set(item, array.getLong(j)); }
                                    else if(cls == Boolean.class || cls == boolean.class) { field.set(item, array.getBoolean(j)); }
                                    else { writeData(item, JsonUtils.getJSONObject(array, j)); }
                                    add.invoke(nested, item);
                                }
                            }
                        }
                    }
                    else { writeData(nested, JsonUtils.getJSONObject(json, name)); }
                    field.set(data, nested);
                }
                json.get(name);
            }
        }
        catch(Exception ex) { }
    }
    private static <T> T createInstance(final Class<T> clazz)
            throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        T instanceToReturn = null;
        try {
            Class<?> enclosingClass = clazz.getEnclosingClass();
            if (enclosingClass != null) {
                Object instanceOfEnclosingClass = createInstance(enclosingClass);
                Constructor<T> ctor = clazz.getConstructor(enclosingClass);
                if (ctor != null) {
                    instanceToReturn = ctor.newInstance(instanceOfEnclosingClass);
                }
            } else {
                instanceToReturn = clazz.newInstance();
            }
        }
        catch (Exception ex) { }
        return instanceToReturn;
    }
    private static JSONObject getJSONObject(JSONObject parent, String name)
    {
        JSONObject object = null;
        try { object = parent.getJSONObject(name); }
        catch(Exception ex) { }
        if(object == null) object = new JSONObject();
        return object;
    }
    private static JSONObject getJSONObject(JSONArray parent, int index)
    {
        JSONObject object = null;
        try { object = parent.getJSONObject(index); }
        catch(Exception ex) { }
        if(object == null) object = new JSONObject();
        return object;
    }
    public static String getJson(Object data) {
        try {
            Object object = JsonUtils.readData(data);
            if(object instanceof JSONArray)
            { return ((JSONArray) object).toString(); }
            else if(object instanceof JSONObject)
            { return ((JSONObject) object).toString(); }
        } catch (Exception e) { }
        return "";
    }
    public static void getObject(Object data, String json) {
        try {
            Object value = new JSONTokener(json).nextValue();
            if(value instanceof JSONArray)
            { /*----- NOT IMPLEMENTED -----*/ }
            else
            { JsonUtils.writeData(data, new JSONObject(json)); }
        } catch (Exception e) { }
    }
}
