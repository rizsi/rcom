package com.rizsi.rcom.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.view.View;
import org.flexdock.view.Viewport;

import com.rizsi.rcom.cli.UtilCli;

/**
 * Improved GUI based on Flexdock
 */
public class Gui2 extends JFrame implements DockingConstants
{
	private static final long serialVersionUID = 1L;
	private GuiMainView gmv;
	public static void main(String[] args) throws Exception {
		commandline(args);
	}
	public static void commandline(String[] args) throws Exception {
		final GuiCliArgs a=new GuiCliArgs();
		UtilCli.parse(a, args, true);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				startup(a);
			}
		});
	}

	private static void startup(GuiCliArgs aa) {
		// turn on floating support
		DockingManager.setFloatingEnabled(true);

		Gui2 f = new Gui2(aa);
		f.setSize(800, 600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocationByPlatform(true);
		f.setVisible(true);
		if(aa.connectionString!=null)
		{
			f.gmv.launchClient();
		}
	}
	private GuiCliArgs arguments;
	public Gui2(GuiCliArgs aa) {
		super("RCOM");
		arguments=aa;
		setContentPane(createContentPane(aa));
	}
	Viewport viewport;
	private JPanel createContentPane(GuiCliArgs aa) {
		createMenu(this);
		JPanel p = new JPanel(new BorderLayout(0, 0));
		p.setBorder(new EmptyBorder(5, 5, 5, 5));

		viewport = new Viewport();
		p.add(viewport, BorderLayout.CENTER);

		createMainView(viewport);
/*		for (File f : aa.file) {
			try {
				CircuitEditorWindow w = ws.openFile(f);
				View v=w.createView();
				viewport.dock((Dockable)v, DockingManager.CENTER_REGION);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
//		imagesArea=createStartPage();
//		viewport.dock((Dockable)imagesArea, DockingManager.EAST_REGION);
		return p;
	}
	private void createMainView(Viewport viewport) {
		gmv=new GuiMainView(arguments, this);
		viewport.dock((Dockable)gmv.getView(), DockingManager.CENTER_REGION);
	}
	public static void createMenu(JFrame f) {
		JMenu menu;
//		JMenuItem i1;
		JMenuBar mb = new JMenuBar();
		menu = new JMenu("File");
//		i1 = new JMenuItem("Save All");
//		i1.addActionListener(e->{ws.saveAll();});
//		menu.add(i1);
//		i1 = new JMenuItem("Export Gerber");
//		i1.addActionListener(e->{ws.exportGerber();});
//		menu.add(i1);
		mb.add(menu);
		f.setJMenuBar(mb);
	}
	public void showView(View v) {
		viewport.dock((Dockable)v, DockingManager.EAST_REGION);
	}
}
