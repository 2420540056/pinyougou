package cn.itcast.sms;

import java.util.Map;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;

@Component
public class SmsListener {
	
	@Autowired
	private SmsUtil smsUtil;

	@JmsListener(destination="sms")
	
	public void onMessage(Map<String,String> map) {
		try {
			System.out.println("接收数据"+map.get("mobile")+map.get("template_code")+map.get("sign_name")+map.get("param")+"测试");
			SendSmsResponse response=smsUtil.sendSms(
					map.get("mobile"),
					map.get("template_code"),
					map.get("sign_name"),
					map.get("param"));
			
			System.out.println("Code=" + response.getCode());
			System.out.println("Message=" + response.getMessage());
			System.out.println("RequestId=" + response.getRequestId());
			System.out.println("BizId=" + response.getBizId());
		} catch (ClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
