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
import org.jdiextractor.tracemodel.entities.TraceReceiver;

public class TraceSerializerJson extends TraceSerializer {

	BufferedWriter writer;

	public TraceSerializerJson(BufferedWriter writer) {
		this.writer = writer;
	}

	@Override
	public void serialize(Trace trace) {
		try {
			writer.write(arrayStart());
			Iterator<TraceElement> ite = trace.getElements().iterator();
			while (ite.hasNext()) {
				ite.next().acceptSerializer(this);
			}

			writer.write(arrayEnd());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(TraceArgument traceArgument) {
		// TODO Auto-generated method stub
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
	public void serialize(TraceInvocation traceInvocation) {
		// TODO Auto-generated method stub
	}

	@Override
	public void serialize(TraceMethod traceMethod) {
		try {
			writer.write(quotes("method") + ":");

			writer.write(objectStart());
			// writing the name
			writer.write(quotes("name") + ":" + quotes(traceMethod.getName()));
			writer.write(this.joinElementListing());

			// writing signature
			writer.write(quotes("signature") + ":" + quotes(traceMethod.getSignature()));

			writer.write(objectEnd());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(TraceReceiver traceReceiver) {
		// TODO Auto-generated method stub
	}

	@Override
	public void serialize(TraceAssignationRight traceAssignationRight) {
		// TODO Auto-generated method stub
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
