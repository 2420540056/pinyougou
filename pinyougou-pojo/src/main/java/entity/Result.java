package entity;

import java.io.Serializable;

public class Result implements Serializable {
	private Boolean success;
	private String mssage;
	
	
	public Result() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Result(Boolean success, String mssage) {
		super();
		this.success = success;
		this.mssage = mssage;
	}
	@Override
	public String toString() {
		return "Result [success=" + success + ", mssage=" + mssage + "]";
	}
	public Boolean getSuccess() {
		return success;
	}
	public void setSuccess(Boolean success) {
		this.success = success;
	}
	public String getMssage() {
		return mssage;
	}
	public void setMssage(String mssage) {
		this.mssage = mssage;
	}
	
}
