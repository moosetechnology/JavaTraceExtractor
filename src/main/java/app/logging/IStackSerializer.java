package app.logging;

import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StringReference;

public interface IStackSerializer {

	String framesStart();

	String framesEnd();

	String frameLineStart(int i);

	String frameLineEnd();

	String methodSignature(Method method);

	String methodArgumentsStart();

	String methodArgumentsEnd();

	String methodArgumentsValuesStart();

	String methodArgumentsValuesEnd();

	String inaccessibleArgument();

	String fieldsStart();

	String fieldsEnd();
	
	String fieldStart(String name);
	
	String fieldValueStart();
	
	String fieldValueEnd();
	
	String fieldEnd();
	
	String inaccessibleField();
	
	String methodReceiverStart();

	String methodReceiverEnd();

	String nullValue();

	String maxDepth();

	String primitiveValue(PrimitiveValue value);

	String stringReference(StringReference value);

	String objectReferenceStart();
	
	String objectReferenceEnd();

	String objectReferenceInfoStart(ObjectReference value);

	String objectReferenceInfoEnd();

	String objectReferenceAlreadyFound(ObjectReference value);

	String emptyArray();

	String arrayValueStart(int number);

	String arrayValueEnd();

	String classNotPrepared();

	String joinElementListing();

	String arrayReferenceStart();

	String arrayReferenceEnd();


}
