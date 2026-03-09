package org.jdiextractor.service.serializer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;

import org.jdiextractor.tracemodel.entities.Trace;
import org.jdiextractor.tracemodel.entities.TraceArgument;
import org.jdiextractor.tracemodel.entities.TraceAssignation;
import org.jdiextractor.tracemodel.entities.TraceAssignationLeft;
import org.jdiextractor.tracemodel.entities.TraceAssignationRight;
import org.jdiextractor.tracemodel.entities.TraceElement;
import org.jdiextractor.tracemodel.entities.TraceInvocation;
import org.jdiextractor.tracemodel.entities.TraceMethod;
import org.jdiextractor.tracemodel.entities.TraceParameter;
import org.jdiextractor.tracemodel.entities.TraceReceiver;
import org.jdiextractor.tracemodel.entities.TraceValue;
import org.jdiextractor.tracemodel.entities.traceValues.TraceArrayReference;
import org.jdiextractor.tracemodel.entities.traceValues.TraceArrayValue;
import org.jdiextractor.tracemodel.entities.traceValues.TraceClassNotPrepared;
import org.jdiextractor.tracemodel.entities.traceValues.TraceClassReference;
import org.jdiextractor.tracemodel.entities.traceValues.TraceField;
import org.jdiextractor.tracemodel.entities.traceValues.TraceObjectReference;
import org.jdiextractor.tracemodel.entities.traceValues.TracePrimitiveValue;
import org.jdiextractor.tracemodel.entities.traceValues.TraceStringReference;
import org.jdiextractor.tracemodel.entities.traceValues.TraceValueAlreadyFound;

public class TraceSerializerJson extends TraceSerializer {

	private BufferedWriter writer;
	private boolean valueIndependents;
	private int nbElementLogged;

	public TraceSerializerJson(BufferedWriter writer, boolean valueIndependents) {
		this.writer = writer;
		this.valueIndependents = valueIndependents;
		this.nbElementLogged = 0;
	}

	@Override
	public void startSerialize() {
		try {
			writer.write(this.objectStart());

			writer.write(quotes("valueIndependents") + ":" + valueIndependents);
			writer.write(this.joinElementListing());

			writer.write(quotes("Lines") + ":");
			writer.write(this.arrayStart());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(TraceElement element) {
		if (nbElementLogged != 0) {
			try {
				writer.write(this.joinElementListing());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		element.acceptSerializer(this);
		this.nbElementLogged++;
	}

	@Override
	public void endSerialize() {
		try {
			writer.write(this.arrayEnd());
			writer.write(this.objectEnd());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(Trace trace) {
		this.startSerialize();
		Iterator<TraceElement> ite = trace.getElements().iterator();
		while (ite.hasNext()) {
			ite.next().acceptSerializer(this);
		}
		this.endSerialize();
	}

	@Override
	public void serialize(TraceArgument traceArgument) {
		TraceValue value = traceArgument.getValue();
		if (value == null) {
			try {
				writer.write("null");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			value.acceptSerializer(this);
		}
	}

	@Override
	public void serialize(TraceAssignation traceAssignation) {
		// TODO Auto-generated method stub
	}

	@Override
	public void serialize(TraceAssignationLeft traceAssignationLeft) {
		// TODO Auto-generated method stub
	}

	@Override
	public void serialize(TraceAssignationRight traceAssignationRight) {
		// TODO Auto-generated method stub
	}

	@Override
	public void serialize(TraceInvocation traceInvocation) {
		// TODO Auto-generated method stub
	}

	@Override
	public void serialize(TraceMethod traceMethod) {
		// NOTE : IT IS IMPORTANT TO FOLLOW THE STEPS
		// 1. Method
		// 2. Arguments
		// 3. Receiver
		// BECAUSE THE MOOSE IMPORTER NEED THIS EXACT ORDER TO IMPORT ALREADY FOUND
		// REFERENCES
		try {
			// Starting Element
			writer.write(this.objectStart());

			writer.write(quotes("method") + ":");
			// Starting Method
			writer.write(objectStart());
			// writing the name
			writer.write(quotes("name") + ":" + quotes(traceMethod.getName()));
			writer.write(this.joinElementListing());

			// writing signature
			writer.write(quotes("signature") + ":" + quotes(traceMethod.getSignature()));
			writer.write(this.joinElementListing());

			// writing class side information
			writer.write(quotes("isClassSide") + ":" + traceMethod.isClassSide());
			writer.write(this.joinElementListing());

			// writing the class declaring this method
			writer.write(quotes("parentType") + ":" + quotes(traceMethod.getParentType()));
			writer.write(this.joinElementListing());

			// writing all arguments types
			writer.write(quotes("parameters") + ":");

			// open parameter array
			writer.write(this.arrayStart());
			Iterator<TraceParameter> paramIte = traceMethod.getParameters().iterator();
			if (paramIte.hasNext()) {
				paramIte.next().acceptSerializer(this);
			}
			while (paramIte.hasNext()) {
				writer.write(this.joinElementListing());
				paramIte.next().acceptSerializer(this);
			}

			// close parameter array
			writer.write(this.arrayEnd());

			// Closing Method
			writer.write(objectEnd());

			writer.write(this.joinElementListing());

			writer.write(quotes("arguments") + ":");
			// Open Arguments Object
			writer.write(this.objectStart());

			if (traceMethod.isArgsAccessible()) {
				writer.write(quotes("argsValues") + ":");
				// Open argument values
				writer.write(this.arrayStart());

				Iterator<TraceArgument> argIte = traceMethod.getArguments().iterator();
				if (argIte.hasNext()) {
					argIte.next().acceptSerializer(this);
				}
				while (argIte.hasNext()) {
					writer.write(this.joinElementListing());
					argIte.next().acceptSerializer(this);
				}
				// Close argument values
				writer.write(this.arrayEnd());
			} else {
				writer.write(quotes("accessible") + ":" + "false");
			}
			// Close Arguments Object
			writer.write(this.objectEnd());

			// Receiver
			writer.write(this.joinElementListing());
			writer.write(quotes("receiver") + ":");
			if (traceMethod.getReceiver() == null) {
				writer.write("null");
			} else {
				traceMethod.getReceiver().acceptSerializer(this);
			}
			// Closing Element
			writer.write(this.objectEnd());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(TraceReceiver traceReceiver) {
		traceReceiver.getValue().acceptSerializer(this);
	}

	@Override
	public void serialize(TraceParameter traceParameter) {
		try {
			writer.write(this.objectStart());
			writer.write(quotes("name") + ":");
			String name = traceParameter.getName();
			if (name == null) {
				writer.write("null");
			} else {

				writer.write(quotes(traceParameter.getName()));
			}
			writer.write(this.joinElementListing());
			writer.write(quotes("type") + ":");
			writer.write(quotes(traceParameter.getTypeName()));
			writer.write(this.objectEnd());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void serialize(TracePrimitiveValue tracePrimitiveValue) {

		try {
			// open object for the primitive type
			writer.write(this.objectStart());

			writer.write(quotes("primitiveValue") + ":");

			// open object for the description of the type
			writer.write(this.objectStart());

			writer.write(quotes("type") + ":" + quotes(tracePrimitiveValue.getType()));

			writer.write(this.joinElementListing());

			writer.write(quotes("value") + ":" + quotes(tracePrimitiveValue.getValue()));

			// close object for the description of the type
			writer.write(this.objectEnd());

			// close object for the primitive type
			writer.write(this.objectEnd());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void serialize(TraceValueAlreadyFound traceValueAlreadyFound) {
		try {
			// open object for the reference
			writer.write(this.objectStart());
			writer.write(quotes("reference") + ":");
			writer.write(this.objectStart());
			writer.write(quotes("alreadyFound") + ":" + "true");
			writer.write(this.joinElementListing());
			writer.write(quotes("uniqueId") + ":" + traceValueAlreadyFound.getUniqueID());
			writer.write(this.objectEnd());
			// close object for the reference
			writer.write(this.objectEnd());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(TraceArrayReference traceArrayReference) {
		try {
			this.ObjectReferenceStart(traceArrayReference);

			// ArrayReference Start
			writer.write(this.objectStart());

			writer.write(quotes("elements") + ":");
			writer.write(this.arrayStart());
			if (traceArrayReference.isAtMaxDepth()) {
				// close array
				writer.write(this.arrayEnd());
				writer.write(this.joinElementListing());
				writer.write(quotes("atMaxDepth") + ":");
				writer.write("true");
			} else {
				Iterator<TraceArrayValue> ite = traceArrayReference.getArrayValues().iterator();
				if (ite.hasNext()) {
					ite.next().acceptSerializer(this);
				}
				while (ite.hasNext()) {
					writer.write(this.joinElementListing());
					ite.next().acceptSerializer(this);
				}
				// close array
				writer.write(this.arrayEnd());
			}

			// close fields
			writer.write(this.objectEnd());

			this.ObjectReferenceEnd(traceArrayReference);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(TraceArrayValue traceArrayValue) {
		TraceValue value = traceArrayValue.getValue();
		if (value == null) {
			try {
				writer.write("null");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			value.acceptSerializer(this);
		}
	}

	@Override
	public void serialize(TraceClassNotPrepared traceClassNotPrepared) {
		try {
			this.ObjectReferenceStart(traceClassNotPrepared);
			writer.write(quotes("<<CLASS_NOT_PREPARED>>"));
			this.ObjectReferenceEnd(traceClassNotPrepared);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(TraceClassReference traceClassReference) {
		try {
			this.ObjectReferenceStart(traceClassReference);

			// open object for fields
			writer.write(this.objectStart());

			writer.write(quotes("fields") + ":");
			// open array
			writer.write(this.arrayStart());
			Iterator<TraceField> ite = traceClassReference.getFields().iterator();
			if (ite.hasNext()) {
				ite.next().acceptSerializer(this);
			}
			while (ite.hasNext()) {
				writer.write(this.joinElementListing());
				ite.next().acceptSerializer(this);
			}
			// close array
			writer.write(this.arrayEnd());

			// close fields
			writer.write(this.objectEnd());

			this.ObjectReferenceEnd(traceClassReference);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void serialize(TraceField traceField) {
		try {
			// open object for field
			writer.write(this.objectStart());

			writer.write(quotes("field") + ":");
			// open object for field description
			writer.write(this.objectStart());
			writer.write(quotes("name") + ":" + quotes(traceField.getName()));

			writer.write(this.joinElementListing());
			if (traceField.isAccessible()) {
				writer.write(quotes("value") + ":");
				if (traceField.getValue() == null) {
					writer.write("null");
				} else {
					traceField.getValue().acceptSerializer(this);
				}
			} else {
				writer.write(quotes("accessible") + ":" + "false");
			}
			
			if(traceField.isAtMaxDepth() ) {
				writer.write(this.joinElementListing());
				writer.write(quotes("atMaxDepth") + ":");
				writer.write("true");
			}

			// close object for field description field
			writer.write(this.objectEnd());
			// close object field
			writer.write(this.objectEnd());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void serialize(TraceStringReference traceStringReference) {
		try {
			this.ObjectReferenceStart(traceStringReference);
			writer.write(quotes(traceStringReference.getValue()));
			this.ObjectReferenceEnd(traceStringReference);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void ObjectReferenceStart(TraceObjectReference traceObjectReference) throws IOException {

		writer.write(this.objectStart());
		writer.write(quotes("reference") + ":");
		// open object for the description
		writer.write(this.objectStart());
		writer.write(quotes("type") + ":" + quotes(traceObjectReference.getType()));
		writer.write(this.joinElementListing());
		writer.write(quotes("uniqueId") + ":" + traceObjectReference.getUniqueID());
		writer.write(this.joinElementListing());
		writer.write(quotes("refered") + ":");

	}

	private void ObjectReferenceEnd(TraceObjectReference traceObjectReference) throws IOException {
		// close object for the description
		writer.write(this.objectEnd());
		// close object for the reference
		writer.write(this.objectEnd());

	}

	private String joinElementListing() {
		return ",";
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

	private String arrayStart() {
		return "[";
	}

	private String arrayEnd() {
		return "]";
	}

}
