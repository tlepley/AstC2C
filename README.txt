/*
  This file is part of AstC2C.

  Copyright (c) STMicroelectronics, 2013.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, 
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of STMicroelectronics nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

  Authors: Thierry Lepley
*/


1. Functionality
================

ASTC2C is a prototype C and OpenCL C source-to-source compiler infrastructure,
which allows by extending it, performing transformations on pure or extended C
code. AstC2C comes with a driver which manages preprocessing (with
'gcc -E' by default), source-to-source compilation and 'backend' compilation
and link (with gcc by default). Communication between each stage of can be
done either with files, either with pipes.

In the standard version, ASTC2C parses a set of C source file and raises
possible syntax errors, then build symbol tables with types and complex literal
tables, computes constant expressions, performs type checking and other
contextual checking such as lvalues, and finally regenerates the C code.

AstC2C can be considered as non intrusive since by default, no code
transformation is performed and the regenerated code is exactly the original
code with some differences:
  - spacing of lexems
  - the 'int' type is added to declaration without type specifier
  - unamed tags (struct, union, enum) are automaticall named
  - comments are not regenerated

Two code manipulation features are implemented in standard in the framework:
  - splitting of declarators at parse time which ensures that one symbol
    declaration per statement. This can ease manipulation of declaration,
    for example their displacement, thanks to a more canonical representation
    of declarations. In some particular cases when splitting, some
    symbols have to change scope and are then automatically mangled.
  - extraction of instance variables of a program (global and static variables)
    and automatic generation of reentrant code (rc2c), where all instance
    variables are collected into a single structure

As input language, AST supports ANSI-C 90, K&R C. It also supports a subset of
ANSI-C 99 and GNU C, in particular the declaration of variables at the middle of
compound blocks, compound literals, compound expressions and array or field
indexing in initializers.



2. Structure of the package and build
======================================

a) Package
----------
The structure of the package is the following:

  AstC2C
  |-- bin
      |-- astc2c
      `-- rc2c
  |-- src
      |-- common
      |-- engine
      |-- driver
      |-- ir
      |-- parser
      |-- target
      `-- utility
  |-- builtins
  |-- script
  |-- jar
      `-- antlr.jar
  |-- devdoc
  |-- devtest
  `-- examples
      |-- h264
      `-- VC1

+ The 'bin' directory contains drivers for the source-to-source compiler
  - astc2c is the generic sdource to source compiler
  - rc2c is the reentrant code generator
+ The 'src' directory contains sources of compiler
+ The 'builtins' directory is intended to contain builtin declaration files
  for the compiler. For exmaple, OpenCL C declares a set of builtin types and
  functions
+ 'script' contains some useful scripts for the AstC2C, and in particular all
  related to installation
+ The 'jar' directory contains external java libraries included in the package
  (ANTLR in particular)
+ The 'devdoc' directory contains some documentation for the compiler
  developper
+ The 'devtest' directory contains a set of development tests
+ The 'examples' contains a set of media applications to which the AstC2C
  (standard and reentrant) can be applied
  - 'h264' is a video h264 reference code which contains an encoder and a
    decoder
  - VC1 is the Microsoft video decoder
  Examples directories contain a README file. Please read this file for more
  information.


b) AstC2C build process
-----------------------

Environment dependencies:
  - The java compiler must be installed and accessible through the standard
    execution path
  - The build process make use of the 'ant' tool and of makefile.
    + 'ant' and 'make' must be installed
    + 'make' must be accessible through the standard execution path

Compilation directives are given by the 'build.xml' file in the AstC2C root
directory.
  - For compiling the tool, simple run 'ant' in the AstC2C root directory.
  - For cleaning the build, use the 'ant clean' command.

When extending AstC2C:
  - Java files placed in the 'src' directory or in any of its sub-directory
    are automatically taken into account by the build process
  - New ANTLR grammars must be placed in the 'src/parser' directory and the
    makefile located in this directory must be modify to take into account the
    new grammar and its potential dependencies with other grammars


c) Targeting for a given processor
----------------------------------

Source-to-source compilation is mainly target independent. Nevertheless, some
target specific information are necessary, for example to compute the value of
'sizeof()' and '__alignof()' builtin functions. The 'TargetSpecific' class
containing size and alignment contraints for scalar types must then be adapted
for each the processor target. This file located in the 'src/target' directory.


3. Compiling with AstC2C
========================

a) Environment dependencies
---------------------------

For executing AstC2C, the requirements are the following
  - The JVM (Java Virtual Machine) must be installed. JAVA_HOME must be
    optionally set to the java installation directory
  - gcc must be installed and accessible in the PATH environment variable 


b) AstC2C execution
-------------------

The compiler infrastructure has a driver which handles the preprocessing, the
source-to-source compilation and the backend C compilation/link process,
located: bin/astc2c. Option for all theses stages can be provided as option of
the driver, the driver transmitting the information to the right stage. AstC2C
can handle different backend C compilers so that it does not interpret all C
compilation option. In order to distinguish backend compiler options from source
files, source files must be provided at the end of the command liner after '--'.

  bin/astc2c [option]* -- <source file>+ 
  bin/rc2c [option]* -- <source file>+ 


c) Compilation stage options
----------------------------
Compilation stage are handled by the driver and a new option -C has been added
to usual one for the source-to-souce compilation stage. The -o option work like
with gcc, for all compilation stage stop options. 
  -E : stop after preprocessing
  -C : stop after the source-to-source stage, before the backend C compilation
  -S : stops in the backend compilation stage before the assembler
  -c : stops before link stops after the backend c compilation stage

d) General AstC2C options
-------------------------
  -v               : print tool version
  --help            : print the command line help
  --verbose <level> : display information for the application developper
              1: additionally display warnings related to a particular version
	         of the C language
  --debug   <level> : display information for the compiler developper
              1: additionally display steps of the compilation process
              2: additionally dumps symbol and complex literal
                 tables
              3: Additionally dumps internal ASTs

e) Specific source-to-source compilation option
-----------------------------------------------
  -C		    : Stop the compilation process after the source-to-source
  --keep     	    : keep intermediate files of the source-to-source process
  --forcelink        : continue on link variable redefinition
  --outdir <name>    : specifies the output directory
  --nopreproc        : does not generate preprocessing directives
  --split            : split C declarators. Only one symbol is declared per
                      statement in the resulting IR

e) Driver compilation option
----------------------------
  --pipe             : pipes instead of files between the C2C and the backend
                      compiler
		      (with rc2c, pipe are used by default, so this option is
		      not available)

f) Backend compiler option
--------------------------
  --backendcc <path>  : sets the backend C compiler (default is 'gcc')


5. Implementation notes
=======================

The intermediate representation of the tool is intentionally very high level.
It more or less has the shape of the abstract syntax tree of the parser. This
choice has been driven by the target to regenerate exactly the same C syntax as
the input source syntax. This allows the C2C not to as less intrusive as
possible in the tool flow in order not to impact performance of the compiled
code and also keep the possibility to easily debug the source code lately in
the tool flow. Compilers which have the C-generation capability usually
regenerate C code from their intermediate representation which has been lowered.
It often ends up to regenerated code very different code from the original.

AstC2C take support on ANTLR 2.7. ANTLR is a modern parser generator which
allows describing conveniently contextual sensitive grammars, has automatic
facilities to build an intermediate representation. It also provides a grammar
inheritance mechanism which is convenient for extend existing parsers. Finally,
it allows going through an abstract syntax tree structure through a grammar and
offers facilities to manipulate the compiler AST.

AstC2C is implemented in Java. This language has first of all be chosen because
it is a well engineered language and because it is easily portable, not for its
execution speed which may be lower than C or C++. Nevertheless, execution time
is acceptable.H264 application, which is a substantial media application,
compiles with acceptable speed on standard workstation.

This tool is a prototype in the sense that it has not been extensively tested yet
through classical C language conformance test suites. Nevertheless, it has been
tested on a relevant set of applications under linux with standard gcc include files.



6. Some Limitations
===================

+ AstC2C detects many semantical errors but does not necessarily dectect all errors
Nevertheless, the tool ensures that wrong C code accepted by the tool is
regenerated in the same way to the output. This error will then be detected at
C compilation time.
+ Syntax errors are reported in a non explicit way, on the contrary to errors
raised from type checking.
+ Applications given as examples have been tested in the linux environment with
different versions of gcc includes: 3.2.3, 3.4.6, 4.1.1, 4.1.2, 4.4.1. Other versions
of gcc includes may bring new GNU-C or ISO-C99 features which have not been
yet implemented
