package jdiextractor.service.serializer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import jdiextractor.config.components.LoggingConfig;
import jdiextractor.tracemodel.entities.Trace;
import jdiextractor.tracemodel.entities.TraceArgument;
import jdiextractor.tracemodel.entities.TraceAssignation;
import jdiextractor.tracemodel.entities.TraceAssignationLeft;
import jdiextractor.tracemodel.entities.TraceAssignationRight;
import jdiextractor.tracemodel.entities.TraceElement;
import jdiextractor.tracemodel.entities.TraceInvocation;
import jdiextractor.tracemodel.entities.TraceMethod;
import jdiextractor.tracemodel.entities.TraceParameter;
import jdiextractor.tracemodel.entities.TraceReceiver;
import jdiextractor.tracemodel.entities.TraceValue;
import jdiextractor.tracemodel.entities.javaType.TraceJavaClass;
import jdiextractor.tracemodel.entities.javaType.TraceJavaPrimitiveType;
import jdiextractor.tracemodel.entities.traceValues.TraceArrayReference;
import jdiextractor.tracemodel.entities.traceValues.TraceArrayValue;
import jdiextractor.tracemodel.entities.traceValues.TraceClassReference;
import jdiextractor.tracemodel.entities.traceValues.TraceField;
import jdiextractor.tracemodel.entities.traceValues.TraceObjectReference;
import jdiextractor.tracemodel.entities.traceValues.TracePrimitiveValue;
import jdiextractor.tracemodel.entities.traceValues.TraceStringReference;
import jdiextractor.tracemodel.entities.traceValues.TraceValueAlreadyFound;

public class TraceSerializerJson extends TraceSerializer {

	private LoggingConfig loggingConfig;

	private BufferedWriter writer;
	private boolean valueIndependents;
	private int nbElementLogged;

	public TraceSerializerJson(LoggingConfig loggingConfig, boolean valueIndependents) {
		this.valueIndependents = valueIndependents;
		this.loggingConfig = loggingConfig;
		this.nbElementLogged = 0;
	}

	@Override
	public void startSerialize() {
		String fileName = this.loggingConfig.getOutputName() + "." + this.loggingConfig.getExtension();
		try {
			this.writer = new BufferedWriter(new FileWriter(fileName));
		} catch (IOException e) {
			throw new RuntimeException("Cannot open a file writer on the file: " + fileName);
		}
		
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
		try {
			if (nbElementLogged != 0) {
				try {
					writer.write(this.joinElementListing());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// Starting Element
			writer.write(this.objectStart());
			writer.write(quotes("id") + ":" + element.getId());
			writer.write(this.joinElementListing());
			writer.write(quotes("parentId") + ":" + element.getParentId());
			writer.write(this.joinElementListing());
			element.acceptSerializer(this);
			// Closing Element
			writer.write(this.objectEnd());
		} catch (IOException e) {
			e.printStackTrace();
		}
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

			// In case the method is parametric precise it
			if (traceMethod.isParametric()) {
				writer.write(quotes("isParametric") + ":" + "true");
				writer.write(this.joinElementListing());
			}

			// writing the class declaring this method
			writer.write(quotes("parentType") + ":");
			traceMethod.getParentType().acceptSerializer(this);
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
			traceParameter.getType().acceptSerializer(this);
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
	public void serialize(TraceClassReference traceClassReference) {
		try {
			this.ObjectReferenceStart(traceClassReference);
			// open object
			writer.write(this.objectStart());
			if (traceClassReference.isPrepared()) {

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

			} else {
				writer.write(quotes("isPrepared") + ":" + "false");
			}

			// close object
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

			if (traceField.isAtMaxDepth()) {
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

	@Override
	public void serialize(TraceJavaPrimitiveType traceJavaPrimitiveType) {
		try {
			writer.write(quotes(traceJavaPrimitiveType.getName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(TraceJavaClass traceJavaClass) {
		try {
			writer.write(this.objectStart());
			writer.write(quotes("name") + ":");
			if (traceJavaClass.getName().contains(".")) {
				writer.write(quotes(traceJavaClass.getName()));
			} else {
				writer.write(quotes("<Default Package>.".concat(traceJavaClass.getName())));
			}

			if (traceJavaClass.isParametric()) {
				writer.write(this.joinElementListing());
				writer.write(quotes("isParametric") + ":" + "true");
			}

			writer.write(this.objectEnd());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void ObjectReferenceStart(TraceObjectReference traceObjectReference) throws IOException {

		writer.write(this.objectStart());
		writer.write(quotes("reference") + ":");
		// open object for the description
		writer.write(this.objectStart());
		writer.write(quotes("type") + ":");
		traceObjectReference.getType().acceptSerializer(this);
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
