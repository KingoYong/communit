package com.example.demo.util.request;

//import cn.emay.eucp.inter.framework.dto.PersonalityParams;


import com.example.demo.util.dto.PersonalityParams;

/**
 * 批量短信发送参数
 * @author Frank
 *
 */
public class SmsPersonalityAllRequest extends BaseRequest {

	private static final long serialVersionUID = 1L;

	private PersonalityParams[] smses;

	public PersonalityParams[] getSmses() {
		return smses;
	}

	public void setSmses(PersonalityParams[] smses) {
		this.smses = smses;
	}
	

}
