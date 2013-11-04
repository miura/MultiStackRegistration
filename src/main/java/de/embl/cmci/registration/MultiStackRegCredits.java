package de.embl.cmci.registration;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*====================================================================
|	stackRegCredits
\===================================================================*/
public class MultiStackRegCredits extends Dialog { /* begin class multiStackRegCredits */

	/*....................................................................
		Public methods
	....................................................................*/

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/********************************************************************/
	public Insets getInsets (
	) {
		return(new Insets(0, 20, 20, 20));
	} /* end getInsets */

	/********************************************************************/
	public MultiStackRegCredits ( final Frame parentWindow) {
		super(parentWindow, "StackReg", true);
		setLayout(new BorderLayout(0, 20));
		final Label separation = new Label("");
		final Panel buttonPanel = new Panel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		final Button doneButton = new Button("Done");
		doneButton.addActionListener(
			new ActionListener (
			) {
				public void actionPerformed (
					final ActionEvent ae
				) {
					if (ae.getActionCommand().equals("Done")) {
						dispose();
					}
				}
			}
		);
		buttonPanel.add(doneButton);
		final TextArea text = new TextArea(25, 56);
		text.setEditable(false);
		text.append("\n");
		text.append("Welcome to MultiStackReg v1.4!\n");
		text.append("\n");
		text.append("This plugin has three modes of use:\n");
		text.append("1) Align a single stack of images\n");
		text.append("2) Align a stack to another stack\n");
		text.append("3) Load a previously created transformation file\n");
			
		text.append("\n");
		text.append("\n");
		text.append("To align a single stack:\n");
		text.append("\n");
		text.append("Choose the image to be aligned in the Stack 1 dropdown box.\n");
		text.append("Leave 'Align' in the Action 1 box.\n");
		text.append("\n");
		text.append("\n");
		text.append("To align two stacks:\n");
		text.append("\n");
		text.append("Place the reference stack in Stack 1's box, and the stack to be\n");
		text.append("aligned in Stack 2's box.  Select 'Use as Reference' as Action 1\n");
		text.append("and 'Align to First Stack' as Action 2.\n");
		text.append("\n");
		text.append("\n");
		text.append("To load a transformation file:\n");
		text.append("\n");
		text.append("Place the stack to be aligned in either stack box, choose 'Load \n");
		text.append("Transformation File' in its Action dropdown box. The File field\n");
	    text.append("can be used as a shortcut to avoid having to select the matrix\n");
	    text.append("manually.\n");
		text.append("\n");
		text.append("\n");
		text.append("\n");
		text.append("Credits:\n");
		text.append("\n");
		text.append("\n");
		text.append(" This work is based on the following paper:\n");
		text.append("\n");
		text.append(" P. Th" + (char)233 + "venaz, U.E. Ruttimann, M. Unser\n");
		text.append(" A Pyramid Approach to Subpixel Registration Based on Intensity\n");
		text.append(" IEEE Transactions on Image Processing\n");
		text.append(" vol. 7, no. 1, pp. 27-41, January 1998.\n");
		text.append("\n");
		text.append(" This paper is available on-line at\n");
		text.append(" http://bigwww.epfl.ch/publications/thevenaz9801.html\n");
		text.append("\n");
		text.append(" Other relevant on-line publications are available at\n");
		text.append(" http://bigwww.epfl.ch/publications/\n");
		text.append("\n");
		text.append(" Additional help available at\n");
		text.append(" http://bigwww.epfl.ch/thevenaz/stackreg/\n");
		text.append("\n");
		text.append(" Ancillary TurboReg_ plugin available at\n");
		text.append(" http://bigwww.epfl.ch/thevenaz/turboreg/\n");
		text.append("\n");
		text.append(" You'll be free to use this software for research purposes, but\n");
		text.append(" you should not redistribute it without our consent. In addition,\n");
		text.append(" we expect you to include a citation or acknowledgment whenever\n");
		text.append(" you present or publish results that are based on it.\n");
		text.append("\n\n");
		text.append("A few changes (loadTransform, appendTransform, multi stack support)\n");
	  text.append("to support load/save functionality and multiple stacks were\n");
	  text.append("added by Brad Busse ( bbusse@stanford.edu ) and released into\n ");
		text.append("the public domain, so go by their ^^ guidelines for distribution, etc.\n");
		add("North", separation);
		add("Center", text);
		add("South", buttonPanel);
		pack();
	} /* end stackRegCredits */

} /* end class stackRegCredits */

