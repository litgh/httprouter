package com.litgh.httprouter;

import java.util.List;

public class Action {
	private List<Param> p;
	private Boolean tsr;
	private Handler handler;
	
	public Action(Handler handler, List<Param> p, Boolean tsr) {
		this.handler = handler;
		this.p = p;
		this.tsr = tsr;
	}
	
	public List<Param> getP() {
		return p;
	}
	public void setP(List<Param> p) {
		this.p = p;
	}
	public Boolean getTsr() {
		return tsr;
	}
	public void setTsr(Boolean tsr) {
		this.tsr = tsr;
	}
	public Handler getHandler() {
		return handler;
	}
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
}
