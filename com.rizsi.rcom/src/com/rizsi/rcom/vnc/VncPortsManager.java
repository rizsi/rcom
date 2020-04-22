package com.rizsi.rcom.vnc;

import com.rizsi.rcom.cli.ServerCliArgs;

public class VncPortsManager {
	ServerCliArgs args;
	public VncPortsManager(ServerCliArgs args) {
		this.args=args;
	}

	synchronized public VncForwardingPorts allocateVNCPorts() {
		return new VncForwardingPorts();
	}
}
