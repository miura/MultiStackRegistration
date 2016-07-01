MultiStackRegistration
======================

Modified version of MultiStackRegistration, for use from scripts. 

##Authors

* Brad Busse (Designer, Developer, - v 1.45)
  * bbusse@stanford.edu 
* Kota Miura (Minor upgrades, v1.46)
  * Mavenized, Scripting enabled, bug fixing. 
  * miura@cmci.info	

##Dependencies

* ImageJ
  * <http://imagej.org> 
* TurboReg (Bundled in Fiji)
  * <http://bigwww.epfl.ch/thevenaz/turboreg/>

##Usage

Welcome to MultiStackReg v1.4!

This plugin has three modes of use:

1. Align a single stack of images
2. Align a stack to another stack
3. Load a previously created transformation file


###To align a single stack:

Choose the image to be aligned in the Stack 1 dropdown box.
Leave 'Align' in the Action 1 box.


###To align two stacks:

Place the reference stack in Stack 1's box, and the stack to be aligned in Stack 2's box.  Select 'Use as Reference' as Action 1 and 'Align to First Stack' as Action 2.


###To load a transformation file:

Place the stack to be aligned in either stack box, choose 'Load Transformation File' in its Action dropdown box. The File field can be used as a shortcut to avoid having to select the matrix manually.



##Credits:


This work is based on the following paper:

    P. Thévenaz, U.E. Ruttimann, M. Unser
    A Pyramid Approach to Subpixel Registration Based on Intensity
    IEEE Transactions on Image Processing
    vol. 7, no. 1, pp. 27-41, January 1998.

This paper is available on-line at

* <http://bigwww.epfl.ch/publications/thevenaz9801.html>

Other relevant on-line publications are available at

* <http://bigwww.epfl.ch/publications/>

Additional help available at

* <http://bigwww.epfl.ch/thevenaz/stackreg/>

Ancillary TurboReg_ plugin available at

* <http://bigwww.epfl.ch/thevenaz/turboreg/>

You'll be free to use this software for research purposes, but you should not redistribute it without our consent. In addition, we expect you to include a citation or acknowledgment whenever you present or publish results that are based on it.

A few changes (loadTransform, appendTransform, multi stack support) to support load/save functionality and multiple stacks were added by Brad Busse ( bbusse@stanford.edu ) and released into the public domain, so go by their ^^ guidelines for distribution, etc.
