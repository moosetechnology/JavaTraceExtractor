package app.logging;

import java.util.Iterator;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StringReference;

public class StackSerializerJson implements IStackSerializer {

	public StackSerializerJson() {

	}

	@Override
	public String framesStart() {
		return this.objectStart() + quotes("Lines") + ":" + this.arrayStart();

	}

	@Override
	public String framesEnd() {
		return this.arrayEnd() + this.objectEnd();
	}

	@Override
	public String frameLineStart(int i) {
		return this.objectStart();
	}

	@Override
	public String frameLineEnd() {
		return this.objectEnd();

	}

	@Override
	public String methodSignature(Method method) {
		String res = "";
		// Retrieving the type of the parameters is important because it provides the
		// most general type that can be used
		res += quotes("method") + ":";

		// open object
		res += this.objectStart();

		// writing the name
		res += quotes("name") + ":" + quotes(method.name());
		res += this.joinElementListing();

		// writing signature
		res += quotes("signature") + ":" + signatureParameter(method);
		res += this.joinElementListing();

		// writing class side information
		res += quotes("isClassSide") + ":" + method.isStatic();
		res += this.joinElementListing();

		// writing the class declaring this method
		res += quotes("parentType") + ":" + quotes(this.parentType(method));
		res += this.joinElementListing();

		// writing all arguments types
		res += quotes("parameters") + ":";

		// open array
		res += this.arrayStart();

		// fill the array with the parameters names and type
		res += parameters(method);

		// close array
		res += this.arrayEnd();

		// close object
		res += this.objectEnd();

		return res;
	}

	/**
	 * Adapt the parent type of the method to make sure it matches moose parent
	 * types
	 * 
	 * @param method
	 * @return the parent type of the method
	 */
	public String parentType(Method method) {
		String parentType = method.location().declaringType().name();
		if (parentType.contains(".")) {
			return parentType;
		}

		return "<Default Package>.".concat(parentType);
	}

	public String signatureParameter(Method method) {
		String paramString = "";

		try {
			Iterator<LocalVariable> ite = method.arguments().iterator();
			if (ite.hasNext()) {
				paramString = parseTypeName(ite.next());
			}
			while (ite.hasNext()) {
				paramString = String.format("%s,%s", paramString, parseTypeName(ite.next()));
			}
		} catch (AbsentInformationException e) {
			// nothing to do
		}
		return quotes(String.format("%s(%s)", method.name(), paramString));
	}

	public String parseTypeName(LocalVariable var) {
		JVMSignatureToMooseSignatureConverter parser = JVMSignatureToMooseSignatureConverter.make();
		if (var.genericSignature() == null)
			return parser.parseTypeSig(var.signature());
		else
			return parser.parseTypeSig(var.genericSignature());
	}

	@Override
	public String methodArgumentsStart() {
		String res = "";

		res += quotes("arguments") + ":";

		// open object
		res += this.objectStart();

		return res;

	}

	@Override
	public String methodArgumentsEnd() {
		// close object
		return this.objectEnd();
	}

	@Override
	public String methodArgumentsValuesStart() {
		String res;

		res = quotes("argsValues") + ":";
		res += this.arrayStart();

		return res;

	}

	@Override
	public String methodArgumentsValuesEnd() {
		return this.arrayEnd();
	}

	@Override
	public String inaccessibleArgument() {
		return quotes("accessible") + ":" + "false";
	}

	@Override
	public String fieldsStart() {
		String res;
		// open object for fields
		res = this.objectStart();

		res += quotes("fields") + ":";
		// open array
		res += this.arrayStart();

		return res;
	}

	@Override
	public String fieldsEnd() {
		String res;

		// open array
		res = this.arrayEnd();

		// close fields
		res += this.objectEnd();

		return res;
	}

	@Override
	public String fieldStart(String name) {
		String res;
		// open object for field
		res = this.objectStart();
		res += quotes("field") + ":";
		// open object for field description
		res += this.objectStart();
		res += quotes("name") + ":" + quotes(name);

		return res;
	}

	@Override
	public String fieldValueStart() {
		String res = this.joinElementListing();

		res += quotes("value") + ":";

		return res;
	}

	@Override
	public String fieldValueEnd() {
		// Nothing needed to be logged
		return "";
	}

	@Override
	public String fieldEnd() {
		String res;
		// close object for field description field
		res = this.objectEnd();
		// close object field
		res += this.objectEnd();

		return res;
	}

	@Override
	public String inaccessibleField() {
		String res;
		res = this.joinElementListing();
		res += quotes("accessible") + ":" + "false";

		return res;
	}

	@Override
	public String methodReceiverStart() {
		return quotes("receiver") + ":";
		// open object
		// Resolve the case of {
		// this.objectStart();
	}

	@Override
	public String methodReceiverEnd() {
		// close object
		// this.objectEnd();
		return "";
	}

	@Override
	public String nullValue() {
		return "null";
	}

	@Override
	public String maxDepth() {
		return quotes("<<MAX_DEPTH_REACHED>>");
	}

	@Override
	public String primitiveValue(PrimitiveValue value) {

		String res;
		// open object for the primitive type
		res = this.objectStart();

		res += quotes("primitiveValue") + ":";

		// open object for the description of the type
		res += this.objectStart();

		res += quotes("type") + ":" + quotes(value.type().name());

		res += this.joinElementListing();

		res += quotes("value") + ":" + quotes(value);

		// close object for the description of the type
		res += this.objectEnd();

		// close object for the primitive type
		res += this.objectEnd();

		return res;
	}

	@Override
	public String stringReference(StringReference value) {
		return quotes(value.value());
	}

	@Override
	public String objectReferenceStart() {
		String res;

		// open object for the reference
		res = this.objectStart();
		res += quotes("reference") + ":";
		// open object for the description
		res += this.objectStart();

		return res;
	}

	public String objectReferenceEnd() {
		String res;
		// close object for the description
		res = this.objectEnd();
		// close object for the reference
		res += this.objectEnd();
		return res;
	}

	public String objectReferenceInfoStart(ObjectReference value) {
		String res;
		res = quotes("type") + ":" + quotes(value.referenceType().name());
		res += this.joinElementListing();
		res += quotes("uniqueId") + ":" + value.uniqueID();
		res += this.joinElementListing();
		res += quotes("refered") + ":";

		return res;
	}

	public String objectReferenceInfoEnd() {
		return "";
	}

	public String objectReferenceAlreadyFound(ObjectReference value) {
		String res;
		res = quotes("alreadyFound") + ":" + "true";
		res += this.joinElementListing();
		res += quotes("uniqueId") + ":" + value.uniqueID();

		return res;
	}

	@Override
	public String emptyArray() {
		return "";
	}

	@Override
	public String arrayValueStart(int number) {
		return "";
	}

	@Override
	public String arrayValueEnd() {
		return "";
	}

	@Override
	public String classNotPrepared() {
		return quotes("<<CLASS_NOT_PREPARED>>");

	}

	@Override
	public String joinElementListing() {
		return ",";
	}

	@Override
	public String arrayReferenceStart() {
		String res;
		res = this.objectStart();
		res += quotes("elements") + ":";
		res += this.arrayStart();

		return res;

	}

	@Override
	public String arrayReferenceEnd() {
		String res;
		res = this.arrayEnd();
		res += this.objectEnd();

		return res;
	}

	private String arrayStart() {
		return "[";
	}

	private String arrayEnd() {
		return "]";
	}

	private String parameters(Method method) {
		String res = "";
		try {
			// trying to obtain the arguments information
			Iterator<LocalVariable> ite = method.arguments().iterator();

			if (ite.hasNext()) {
				res += parameter(ite.next());
			}
			while (ite.hasNext()) {
				res += this.joinElementListing();
				res += parameter(ite.next());
			}

		} catch (AbsentInformationException e) {
			// arguments name could not be obtained
			// Since the name are not obtainable just log the parameters types
			Iterator<String> ite = method.argumentTypeNames().iterator();

			if (ite.hasNext()) {
				res += parameter(ite.next());
			}
			while (ite.hasNext()) {
				res += this.joinElementListing();
				res += parameter(ite.next());
			}
		}

		return res;
	}

	/*
	 * log the name and type of the parameter if the LocalVariable could be obtained
	 */
	private String parameter(LocalVariable var) {
		String res;

		res = this.objectStart();
		res += quotes("name") + ":";
		res += quotes(var.name());
		res += this.joinElementListing();
		res += quotes("type") + ":";
		res += quotes(var.typeName());
		res += this.objectEnd();

		return res;
	}

	/*
	 * log only the type of the parameter if the LocalVariable could not be obtained
	 */
	private String parameter(String typeName) {
		String res;

		res = this.objectStart();
		res += quotes("name") + ":";
		res += this.nullValue();
		res += this.joinElementListing();
		res += quotes("type") + ":";
		res += quotes(typeName);
		res += this.objectEnd();

		return res;
	}

	private String quotes(Object obj) {
		return "\"" + obj.toString() + "\"";
	}

	private String objectStart() {
		return "{";
	}

	private String objectEnd() {
		return "}";
	}

}
