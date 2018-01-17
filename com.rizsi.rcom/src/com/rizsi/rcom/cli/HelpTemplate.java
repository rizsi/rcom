package com.rizsi.rcom.cli;

import com.rizsi.rcom.VideoConnection;
import com.rizsi.rcom.gui.GuiCliArgs;

import hu.qgears.rtemplate.runtime.DummyCodeGeneratorContext;
import hu.qgears.rtemplate.runtime.RAbstractTemplatePart;

public class HelpTemplate extends RAbstractTemplatePart {

	public HelpTemplate() {
		super(new DummyCodeGeneratorContext());
	}
	public String generate() throws Exception
	{
		write("= RCOM communication software Usage Help\n\nSimple video communication software that builds on exisiting command line tools to generate and decode video.\n\n*This project is a proof of concept, not a real product yet.*\n\nSee: https://github.com/rizsi/rcom\n\n== Usage:\n\nVersion: ");
		writeObject(VideoConnection.serviceID);
		write("\n\n=== GUI\n\nWith PulseAudio echo cancellation:\n\n $ PULSE_PROP=filter.want=echo-cancel java -jar rcom.jar gui [...arguments...]\n\nWith libspeex echo cancellation:\n\n $ java -jar rcom.jar gui --echoCanceller [...other arguments...]\n\n----\n");
		UtilCli.printHelp(getTemplateState(), new GuiCliArgs());
		write("----\n\n=== Server\n\n $ java -jar rcom.jar server [...arguments...]\n\n----\n");
		UtilCli.printHelp(getTemplateState(), new ServerCliArgs());
		write("----\n\n=== Command line client\n\nFor testing purpose only\n\n $ java -jar rcom.jar client [...arguments...]\n\n----\n");
		UtilCli.printHelp(getTemplateState(), new ClientCliArgs());
		write("----\n\n");
		finishDeferredParts();
		return getTemplateState().getOut().toString();
	}
}
