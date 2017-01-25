package com.rizsi.rcom.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import hu.qgears.rtemplate.runtime.TemplateState;
import joptsimple.annot.AnnotatedClass;

public class UtilCli {
	public static void parse(Object o, String[] args, boolean showHelp) throws Exception
	{
		AnnotatedClass ac=new AnnotatedClass();
		ac.parseAnnotations(o);
		if(showHelp)
		{
			ac.printHelpOn(System.out);
		}
		ac.parseArgs(args);
		if(showHelp)
		{
			ac.print();
		}
	}

	public static void printHelp(TemplateState templateState, Object args) throws Exception {
		AnnotatedClass ac=new AnnotatedClass();
		ac.parseAnnotations(args);
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		PrintStream ps=new PrintStream(bos, false, "UTF-8");
		ac.printHelpOn(ps);
		ps.close();
		templateState.append(new String(bos.toByteArray(), StandardCharsets.UTF_8));
	}
}
