package com.amazonaws.lambda.demo;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
public class PostLambda implements RequestHandler<Map<String,String>, Map<String,String>> {

	@Override
    public Map<String,String> handleRequest(Map<String,String> input, Context context) {
		String received = (String) input.get("input");  
		context.getLogger().log("Input: " + received);
    	  Map<String, String> responseMap = new HashMap<>();
    	  responseMap.put("response", received.length()+"");
    	  return responseMap;
    }

}
