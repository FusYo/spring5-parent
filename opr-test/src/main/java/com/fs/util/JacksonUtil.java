package com.fs.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonUtil {

	    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	    /**
	     * 将POJO转为JSON
	     */
	    public static <T> String toJson(T obj) {
	        String json;
	        try {
	            json = OBJECT_MAPPER.writeValueAsString(obj);
	        } catch (Exception e) {
//	            LOGGER.error("convert POJO to JSON failure", e);
	            throw new RuntimeException(e);
	        }
	        return json;
	    }

	    /**
	     * 将JSON转为POJO
	     */
	    public static <T> T fromJson(String json, Class<T> type) {
	        T pojo;
	        try {
	            pojo = OBJECT_MAPPER.readValue(json, type);
	        } catch (Exception e) {
//	            LOGGER.error("convert JSON to POJO failure", e);
	            throw new RuntimeException(e);
	        }
	        return pojo;
	    }
}
