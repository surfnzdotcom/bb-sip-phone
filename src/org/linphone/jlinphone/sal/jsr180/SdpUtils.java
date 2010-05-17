package org.linphone.jlinphone.sal.jsr180;

import java.util.Vector;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.jortp.PayloadType;
import org.linphone.sal.SalException;
import org.linphone.sal.SalFactory;
import org.linphone.sal.SalMediaDescription;
import org.linphone.sal.SalMediaDescriptionBase;
import org.linphone.sal.SalStreamDescription;

import sip4me.gov.nist.core.ParseException;
import sip4me.gov.nist.javax.sdp.MediaDescription;
import sip4me.gov.nist.javax.sdp.SdpException;
import sip4me.gov.nist.javax.sdp.SdpFactory;
import sip4me.gov.nist.javax.sdp.SdpParseException;
import sip4me.gov.nist.javax.sdp.SessionDescription;
import sip4me.gov.nist.javax.sdp.fields.AttributeField;
import sip4me.gov.nist.javax.sdp.fields.ConnectionField;
import sip4me.gov.nist.javax.sdp.fields.MediaField;
import sip4me.gov.nist.javax.sdp.fields.OriginField;
import sip4me.gov.nist.javax.sdp.fields.SessionNameField;
import sip4me.gov.nist.javax.sdp.fields.TimeField;

public class SdpUtils {
	static Logger sLogger=null;
	static private Logger getLogger(){
		if (sLogger==null)
			sLogger=Logger.getLogger("Sal");
		return sLogger;
	}
	public static SalMediaDescription toSalMediaDescription(SessionDescription sd) throws SalException{
		SalMediaDescription md= SalFactory.instance().createSalMediaDescription();
		ConnectionField c=sd.getConnection();
		Vector mlines;
		try {
			mlines = sd.getMediaDescriptions(false);
			md.setAddress(c.getAddress());

			for(int i=0;i<mlines.size();++i){
				MediaDescription mline=(MediaDescription)mlines.elementAt(i);
				
				SalStreamDescription ssd=toStreamDescription(mline);
				if (ssd.getAddress() == null) {
					ssd.setAddress(c.getAddress());
				}
				if (ssd!=null) md.addStreamDescription(ssd);
			}
		} catch (Exception e) {
			throw new SalException("Could parse sdp", e);
		}

		return md;
	}
	public static SessionDescription toSessionDescription(SalMediaDescription md){
		SessionDescription sd=null;
		try {
			sd=SdpFactory.getInstance().createSessionDescription();
			SessionNameField sessionName=SdpFactory.getInstance().createSessionName("Phone call");
			OriginField origin=SdpFactory.getInstance().createOrigin("blackberry", md.getAddress());
			ConnectionField c=SdpFactory.getInstance().createConnection(md.getAddress());
			TimeField t=SdpFactory.getInstance().createTime();
			sd.setSessionName(sessionName);
			sd.setOrigin(origin);
			sd.setConnection(c);
			sd.addField(t);
			Vector mlines=new Vector();
			for (int i=0;i<md.getNumStreams();++i){
				MediaDescription mline=toMLine(md.getStream(i));
				mlines.addElement(mline);
			}
			sd.setMediaDescriptions(mlines);
			
		} catch (SdpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sd;
	}
	private static MediaDescription toMLine(SalStreamDescription sd){
		Vector attrs=new Vector();
		PayloadType pts[]=sd.getPayloadTypes();
		int payloads[]=new int[pts.length];
		int i;
		for (i=0;i<pts.length;++i){
			PayloadType pt=pts[i];
			StringBuffer rtpmap=new StringBuffer();
			rtpmap.append(pt.getNumber());
			rtpmap.append(' ');
			rtpmap.append(pt.getMimeType());
			rtpmap.append('/');
			rtpmap.append(pt.getClockRate());
			if (pt.getNumChannels()>0){
				rtpmap.append('/');
				rtpmap.append(pt.getNumChannels());
			}
			AttributeField attr=null;
			try {
				attr = SdpFactory.getInstance().createAttribute("rtpmap", rtpmap.toString());
			} catch (SdpException e) {
				//ignore
			}
			payloads[i]=pt.getNumber();
			if (attr!=null) attrs.addElement(attr);
		}
		MediaDescription mline=null;
		try {
			mline = SdpFactory.getInstance().createMediaDescription(
					sd.getType()==SalStreamDescription.Type.Audio ? "Audio" : "Video",
					sd.getPort(),0,
					sd.getProto()==SalStreamDescription.Proto.RtpAvp ? "RTP/AVP" : "RTP/SAVP", payloads);
			mline.setAttributes(attrs);
		} catch (IllegalArgumentException e) {
			getLogger().error("Could not create mline", e);
		} catch (SdpException e) {
			getLogger().error("Could not create mline", e);
		}
		return mline;
	}
	
	private static SalStreamDescription toStreamDescription(
			MediaDescription mline) {
		SalStreamDescription ssd=new SalStreamDescription();
		MediaField mf=mline.getMedia();
		SalStreamDescription.Type mt;
		try {
			if (mf.getMediaType().equals("audio"))
				mt=SalStreamDescription.Type.Audio;
			else if (mf.getMediaType().equals("video"))
				mt=SalStreamDescription.Type.Video;
			else mt=SalStreamDescription.Type.Other;
		} catch (SdpParseException e) {
			getLogger().error("Could not get mediatype", e);
			return null;
		}
		ssd.setType(mt);
		if (mf.getProto().equals("RTP/AVP"))
			ssd.setProto(SalStreamDescription.Proto.RtpAvp);
		else if (mf.getProto().equals("RTP/SAVP"))
			ssd.setProto(SalStreamDescription.Proto.RtpSavp);
		else{
			getLogger().error("Unsupported proto" + mf.getProto(), null);
			return null;
		}
		ssd.setPort(mf.getPort());
		try {
			String ptime=mline.getAttribute("ptime");
			if (ptime!=null) ssd.setPtime(Integer.parseInt(ptime));
		} catch (SdpParseException e) {
			//ignore
		}
		
		fillCodecs(ssd,mline);
		return ssd;
	}
	private static void extractRtpmap(PayloadType pt, String value){
		int space=value.indexOf(' ');
		String number=value.substring(0, space);
		int n=Integer.parseInt(number);
		if (n==pt.getNumber()){
			int i=space;
			//jump to amr/8000/1
			while(value.charAt(i)==' ') i++;
			String rtpmap=value.substring(i);
			String rate;
			int slash=rtpmap.indexOf('/');
			pt.setMimeType(rtpmap.substring(0,slash));
			int slash2=rtpmap.indexOf('/',slash+1);
			if (slash2>0){
				rate=rtpmap.substring(slash+1,slash2);
			}else rate=rtpmap.substring(slash+1);
			pt.setClockRate(Integer.parseInt(rate));
			if (slash2>0){
				String chans=rtpmap.substring(slash2+1);
				pt.setNumChannels(Integer.parseInt(chans));
			}
		}
	}
	private static void fillPayloadType(PayloadType pt, MediaDescription mline){
		Vector attrs=mline.getAttributeFields();
		int i;
		for(i=0;i<attrs.size();++i){
			AttributeField attr=(AttributeField)attrs.elementAt(i);
			try {
				if (attr.getName().equals("rtpmap")){
					extractRtpmap(pt,attr.getValue());
				}else if (attr.getName().equals("fmtp")){
					pt.appendSendFmtp(attr.getValue());
				}
			} catch (SdpParseException e) {
				getLogger().error("Cannot get rtpmap value for " + attr,null);
			}
		}
	}
	private static void fillCodecs(SalStreamDescription ssd, MediaDescription mline) {
		MediaField mf=mline.getMedia();
		Vector payloadNumbers=mf.getFormats();
		Vector payLoadTypes = new Vector();
		int i;
		for(i=0;i<payloadNumbers.size();++i){
			String numstr=(String)payloadNumbers.elementAt(i);
			PayloadType pt=JOrtpFactory.instance().createPayloadType();
			pt.setNumber(Integer.parseInt(numstr));
			fillPayloadType(pt,mline);
			payLoadTypes.addElement(pt);
		}
		PayloadType[] payloadTypeArray = new PayloadType[payLoadTypes.size()];
		payLoadTypes.copyInto(payloadTypeArray);
		ssd.setPayloadTypes(payloadTypeArray);
	}
	
}
