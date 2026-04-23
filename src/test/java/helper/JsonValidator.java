package helper;

import java.util.regex.Pattern;

public class JsonValidator {
	
    private static final Pattern JSON_PATTERN_WITH_TYPES_AND_OTHERS = Pattern.compile(
            "\\{\\s*(\"[^\"]+\"\\s*:\\s*(\"[^\"]+\"|true|false|null|\\d+(\\.\\d+)?|\\{[^{}]*}|\\[[^\\[\\]]*]))"
                    + "(\\s*,\\s*\"[^\"]+\"\\s*:\\s*(\"[^\"]+\"|true|false|null|\\d+(\\.\\d+)?|\\{[^{}]*}|\\[[^\\[\\]]*]))*\\s*}"
    );
    
    public static boolean validate(String json){
        return JSON_PATTERN_WITH_TYPES_AND_OTHERS.matcher(json).matches();
    }
}
