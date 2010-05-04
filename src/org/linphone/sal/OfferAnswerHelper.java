package org.linphone.sal;

import java.util.Vector;

import org.linphone.jortp.PayloadType;


public class OfferAnswerHelper {
	public static class AnswerResult{
		private SalMediaDescription mResult,mAnswer;
		public SalMediaDescription getResult(){
			return mResult;
		}
		public SalMediaDescription getAnswer(){
			return mAnswer;
		}
	}
	public static SalMediaDescription computeOutgoing(SalMediaDescription local, SalMediaDescription remote){
		SalMediaDescription md=SalFactory.instance().createSalMediaDescription();
		int i;
		md.setAddress(remote.getAddress());
		for (i=0;i<remote.getNumStreams();++i){
			SalStreamDescription rsd=remote.getStream(i);
			SalStreamDescription sd=intersect(local.getStream(i),rsd);
			sd.setAddress(rsd.getAddress());
			sd.setPort(rsd.getPort());
			sd.setPtime(rsd.getPtime());
			md.addStreamDescription(sd);
		}
		return md;
	}
	public static AnswerResult computeIncoming(SalMediaDescription local, SalMediaDescription remote){
		AnswerResult ar=new AnswerResult();
		SalMediaDescription answer=SalFactory.instance().createSalMediaDescription();
		SalMediaDescription result=SalFactory.instance().createSalMediaDescription();
		int i;
		
		ar.mAnswer=answer;
		ar.mResult=result;
		answer.setAddress(local.getAddress());
		result.setAddress(remote.getAddress());
		
		for(i=0;i<remote.getNumStreams();++i){
			SalStreamDescription lsd=local.getStream(i);
			SalStreamDescription rsd=remote.getStream(i);
			
			SalStreamDescription sd=intersect(local.getStream(i),rsd);
			sd.setPort(rsd.getPort());
			sd.setPtime(rsd.getPtime());
			
			SalStreamDescription asd=new SalStreamDescription();
			asd.setPayloadTypes(sd.getPayloadTypes());
			asd.setProto(sd.getProto());
			asd.setType(sd.getType());
			asd.setPtime(lsd.getPtime());
			asd.setPort(lsd.getPort());
			
			result.addStreamDescription(sd);
			answer.addStreamDescription(asd);
		}
		return ar;
	}
	private static SalStreamDescription intersect(SalStreamDescription local, SalStreamDescription remote){
		SalStreamDescription sd=new SalStreamDescription();
		PayloadType lpts[]=local.getPayloadTypes();
		PayloadType rpts[]=remote.getPayloadTypes();
		Vector result=new Vector();
		int i,j;
		
		sd.setProto(local.getProto());
		sd.setType(local.getType());
		
		for(i=0;i<lpts.length;++i){
			PayloadType lpt=lpts[i];
			for (j=0;j<rpts.length;++j){
				PayloadType rpt=rpts[j];
				if (lpt.getMimeType().equalsIgnoreCase(rpt.getMimeType())
					&& lpt.getClockRate()==rpt.getClockRate()	){
					
					PayloadType matched=(PayloadType)lpt.clone();
					matched.setNumber(rpt.getNumber());
					matched.setSendFmtp(rpt.getRecvFmtp());
					result.addElement(matched);
					
				}
			}
		}
		PayloadType ptr[]=new PayloadType[result.size()];
		result.copyInto(ptr);
		sd.setPayloadTypes(ptr);
		return sd;
	}
}
