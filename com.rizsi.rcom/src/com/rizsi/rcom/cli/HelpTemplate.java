package com.rizsi.rcom.cli;

import com.rizsi.rcom.gui.GuiCliArgs;
import com.rizsi.rcom.ssh.ConnectArgs;

import hu.qgears.rtemplate.runtime.DummyCodeGeneratorContext;
import hu.qgears.rtemplate.runtime.RAbstractTemplatePart;

public class HelpTemplate extends RAbstractTemplatePart {

	public HelpTemplate() {
		super(new DummyCodeGeneratorContext());
	}
	public String generate() throws Exception
	{
		write("= RCOM communication software.\n\nSimple video communication software that builds on exisiting command line tools to generate and decode video.\n\n*This project is a proof of concept, not a real product yet.*\n\nSee: https://github.com/rizsi/rcom\n\n== Usage:\n\n=== GUI\n\n $ java -jar rcom.jar gui [...arguments...]\n\n----\n");
		UtilCli.printHelp(getTemplateState(), new GuiCliArgs());
		write("----\n\n=== Server\n\n $ java -jar rcom.jar server [...arguments...]\n\n----\n");
		UtilCli.printHelp(getTemplateState(), new ServerCliArgs());
		write("----\n\n=== Connect\n\nConnect mode is used by ssh server only to connect stdin and stdout to the server TCP port. This mode also sends the authorized (ssh) user name to the server.\n \n $ java -jar rcom.jar connect [...arguments...]\");\n\n----\n");
		UtilCli.printHelp(getTemplateState(), new ConnectArgs());
		write("----\n\n=== Command line client\n\nFor testing purpose only\n\n $ java -jar rcom.jar client [...arguments...]\n\n----\n");
		UtilCli.printHelp(getTemplateState(), new ClientCliArgs());
		write("----\n\n");
		finishDeferredParts();
		return getTemplateState().getOut().toString();
	}
}