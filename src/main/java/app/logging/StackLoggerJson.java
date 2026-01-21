package app.logging;

import java.util.Iterator;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StringReference;

public class StackLoggerJson extends AbstractStackLoggerFormat {

	public StackLoggerJson(String outputName, String Extension) {
		super(outputName, Extension);
	}

	@Override
	public void framesStart() {
		this.objectStart();

		write(quotes("Lines") + ":");
		this.arrayStart();
	}

	@Override
	public void framesEnd() {
		this.arrayEnd();

		this.objectEnd();
	}

	@Override
	public void frameLineStart(int i) {
		this.objectStart();

	}

	@Override
	public void frameLineEnd() {
		this.objectEnd();

	}

	@Override
	public void methodSignature(Method method) {
		// Retrieving the type of the parameters is important because it provides the
		// most general type that can be used

		write(quotes("method") + ":");
		// open object
		this.objectStart();
		// writing the name
		write(quotes("name") + ":" + quotes(method.name()));
		this.joinElementListing();
		// writing signature
		write(quotes("signature") + ":" + signatureParameter(method));
		this.joinElementListing();
		// writing class side information
		write(quotes("isClassSide") + ":" + method.isStatic());
		this.joinElementListing();
		// writing the class declaring this method
		write(quotes("parentType") + ":" + quotes(this.parentType(method)));
		this.joinElementListing();
		// writing all arguments types
		write(quotes("parameters") + ":");
		// open array
		this.arrayStart();

		// fill the array with the parameters names and type
		parameters(method);

		// close array
		this.arrayEnd();

		// close object
		this.objectEnd();
	}
	
	/**
	 * Adapt the parent type of the method to make sure it matches moose parent types
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
		if (var.genericSignature()==null) return parser.parseTypeSig(var.signature());
		else return parser.parseTypeSig(var.genericSignature()); 
	}
	
	@Override
	public void methodArgumentsStart() {
		write(quotes("arguments") + ":");

		// open object
		this.objectStart();

	}

	@Override
	public void methodArgumentsEnd() {
		// close object
		this.objectEnd();
	}

	@Override
	public void methodArgumentsValuesStart() {
		write(quotes("argsValues") + ":");
		this.arrayStart();

	}

	@Override
	public void methodArgumentsValuesEnd() {
		this.arrayEnd();
	}

	@Override
	public void inaccessibleArgument() {
		write(quotes("accessible") + ":" + "false");
	}

	@Override
	public void fieldsStart() {
		// open object for fields
		this.objectStart();

		write(quotes("fields") + ":");
		// open array
		this.arrayStart();
	}

	@Override
	public void fieldsEnd() {
		// open array
		this.arrayEnd();

		// close fields
		this.objectEnd();
	}

	@Override
	public void fieldStart(String name) {
		// open object for field
		this.objectStart();
		write(quotes("field") + ":");
		// open object for field description
		this.objectStart();
		write(quotes("name") + ":" + quotes(name));
	}

	@Override
	public void fieldValueStart() {
		this.joinElementListing();
		write(quotes("value") + ":");
	}

	@Override
	public void fieldValueEnd() {
		// Nothing needed to be logged

	}

	@Override
	public void fieldEnd() {
		// close object for field description field
		this.objectEnd();
		// close object field
		this.objectEnd();
	}

	@Override
	public void inaccessibleField() {
		this.joinElementListing();
		write(quotes("accessible") + ":" + "false");
	}

	@Override
	public void methodReceiverStart() {
		write(quotes("receiver") + ":");
		// open object
		// Resolve the case of {
		// this.objectStart();
	}

	@Override
	public void methodReceiverEnd() {
		// close object
		// this.objectEnd();
	}

	@Override
	public void nullValue() {
		write("null");
	}

	@Override
	public void maxDepth() {
		write(quotes("<<MAX_DEPTH_REACHED>>"));
	}

	@Override
	public void primitiveValue(PrimitiveValue value) {
		// open object for the primitive type
		this.objectStart();

		write(quotes("primitiveValue") + ":");

		// open object for the description of the type
		this.objectStart();

		write(quotes("type") + ":" + quotes(value.type().name()));

		this.joinElementListing();

		write(quotes("value") + ":" + quotes(value));

		// close object for the description of the type
		this.objectEnd();

		// close object for the primitive type
		this.objectEnd();
	}

	@Override
	public void stringReference(StringReference value) {
		write(quotes(value.value()));
	}

	@Override
	public void objectReferenceStart() {
		// open object for the reference
		this.objectStart();
		write(quotes("reference") + ":");
		// open object for the description
		this.objectStart();
	}

	public void objectReferenceEnd() {
		// close object for the description
		this.objectEnd();
		// close object for the reference
		this.objectEnd();
	}

	public void objectReferenceInfoStart(ObjectReference value) {
		write(quotes("type") + ":" + quotes(value.referenceType().name()));
		this.joinElementListing();
		write(quotes("uniqueId") + ":" + value.uniqueID());
		this.joinElementListing();
		write(quotes("refered") + ":");
	}

	public void objectReferenceInfoEnd() {
	}

	public void objectReferenceAlreadyFound(ObjectReference value) {

		write(quotes("alreadyFound") + ":" + "true");
		this.joinElementListing();
		write(quotes("uniqueId") + ":" + value.uniqueID());
	}

	@Override
	public void emptyArray() {
		// Nothing
	}

	@Override
	public void arrayValueStart(int number) {
		// Nothing
	}

	@Override
	public void arrayValueEnd() {
		// Nothing

	}

	@Override
	public void classNotPrepared() {
		write(quotes("<<CLASS_NOT_PREPARED>>"));

	}

	@Override
	public void joinElementListing() {
		write(",");
	}

	@Override
	public void arrayReferenceStart() {
		this.objectStart();
		write(quotes("elements") + ":");
		this.arrayStart();

	}

	@Override
	public void arrayReferenceEnd() {
		this.arrayEnd();
		this.objectEnd();
	}

	private void arrayStart() {
		write("[");
	}

	private void arrayEnd() {
		write("]");
	}

	private void parameters(Method method) {
		try {
			// trying to obtain the arguments information
			Iterator<LocalVariable> ite = method.arguments().iterator();

			if (ite.hasNext()) {
				parameter(ite.next());
			}
			while (ite.hasNext()) {
				this.joinElementListing();
				parameter(ite.next());
			}

		} catch (AbsentInformationException e) {
			// arguments name could not be obtained
			// Since the name are not obtainable just log the parameters types
			Iterator<String> ite = method.argumentTypeNames().iterator();

			if (ite.hasNext()) {
				parameter(ite.next());
			}
			while (ite.hasNext()) {
				this.joinElementListing();
				parameter(ite.next());
			}
		}
	}

	/*
	 * log the name and type of the parameter if the LocalVariable could be obtained
	 */
	private void parameter(LocalVariable var) {
		this.objectStart();
		write(quotes("name") + ":");
		write(quotes(var.name()));
		this.joinElementListing();
		write(quotes("type") + ":");
		write(quotes(var.typeName()));
		this.objectEnd();
	}

	/*
	 * log only the type of the parameter if the LocalVariable could not be obtained
	 */
	private void parameter(String typeName) {
		this.objectStart();
		write(quotes("name") + ":");
		this.nullValue();
		this.joinElementListing();
		write(quotes("type") + ":");
		write(quotes(typeName));
		this.objectEnd();
	}

	private String quotes(Object obj) {
		return "\"" + obj.toString() + "\"";
	}

	private void objectStart() {
		write("{");
	}

	private void objectEnd() {
		write("}");
	}

}
