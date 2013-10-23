##################################################################
#   \par  Description
#   Compilation of ANTLR grammar files
#
#   \par
#   This file is part of the AstC2C source-to-source compiler technology.
#   Redistribution of this file to outside parties is strictly prohibited
#   without the written consent of the module owner indicated below.\n
#
#   \par  Module owner:
#   Thierry Lepley, STMicroelectronics (thierry.lepley@st.com)
#
#   \par  STMicroelectronics Copyright (C) 2010-2013
#
#   \par  Authors:
#   Thierry Lepley, STMicroelectronics (thierry.lepley@st.com)
#
##################################################################

# Sanity check
ifneq ($(MAKECMDGOALS),clean)
  ifeq ($(ROOT_DIR),)
    $(error ROOT_DIR variable must be defined)
  endif
endif

GENDIR = $(ROOT_DIR)/parser-build/parser
ANTLR_PATH = $(ROOT_DIR)/jar/antlr.jar
SRCDIR = .

# Useful defines
ANTLR = java -classpath $(ANTLR_PATH) antlr.Tool


# Project definition
GENSRC = $(GENDIR)/GnuCParser.java \
	 $(GENDIR)/GnuCLexer.java $(GENDIR)/GNUCTokenTypes.java \
	 $(GENDIR)/GnuCTreeParser.java $(GENDIR)/GnuCTreeParserTokenTypes.java \
	 $(GENDIR)/GnuCEmitter.java $(GENDIR)/GnuCEmitterTokenTypes.java \
	 $(GENDIR)/GnuCEmitterVectorEmulation.java $(GENDIR)/GnuCEmitterVectorEmulationTokenTypes.java \
	 $(GENDIR)/SymbolTableBuilder.java $(GENDIR)/SymbolTableBuilderTokenTypes.java \
	 $(GENDIR)/ThisRewriter.java $(GENDIR)/ThisRewriterTokenTypes.java \
	 $(GENDIR)/KernelSignature.java $(GENDIR)/KernelSignature.java \
	 $(GENDIR)/OclRewriter.java $(GENDIR)/OclRewriterTokenTypes.java \
	 $(GENDIR)/ReentrantRewriter.java $(GENDIR)/ReentrantRewriterTokenTypes.java


###################################################################
#     General rules
###################################################################

.PHONY : all clean

all : $(GENSRC)

clean : 
	rm -f *.txt *.smap $(GENSRC) *.class \
	$(GENDIR)/GnuCEmitterTokenTypes* \
	$(GENDIR)/GNUCTokenTypes* $(GENDIR)/GnuCTreeParserTokenTypes* \
	$(GENDIR)/expandedGnuCParser.g $(GENDIR)/expandedGnuCEmitter.g \
	$(GENDIR)/expandedSymbolTableBuilder.g $(GENDIR)/expandedThisRewriter.g \
	$(GENDIR)/expandedKernelSignature.g \
	$(GENDIR)/expandedOclRewriter.g \
	$(GENDIR)/expandedReentrantRewriter.g $(GENDIR)/expandedGnuCEmitterVectorEmulation.g 



###################################################################
# Parser generation rules
###################################################################
#
# Note 1:
# ------
# We have to put a link to 'StdCParser.g' and 'GnuCTreeParser.g' in
# the build directory because ANTLR, when -glib option is set, looks
# for '<name>TokenTypes.txt' in the same directory as the .g file
#
# Note 2:
# ------
# The ANTLR execution does not necessarily end up to the time stamp
# modification of files. To inform the makefile that they are 
# up-to-date,we force a new time stamp after each call to ANTLR
# (by touching files)


$(GENDIR)/GnuCParser.smap GnuCLexer.smap $(GENDIR)/GNUCTokenTypes.txt \
$(GENDIR)/GnuCParser.java GnuCLexer.java $(GENDIR)/GNUCTokenTypes.java \
: $(SRCDIR)/GnuCParser.g
	mkdir -p $(GENDIR)
	$(ANTLR) -o $(GENDIR) $<
	touch $(GENDIR)/GnuCParser.smap GnuCLexer.smap $(GENDIR)/GNUCTokenTypes.txt \
	      $(GENDIR)/GnuCParser.java GnuCLexer.java $(GENDIR)/GNUCTokenTypes.java

$(GENDIR)/GnuCTreeParser.smap $(GENDIR)/GnuCTreeParserTokenTypes.txt $(GENDIR)/GnuCTreeParser.java \
$(GENDIR)/GnuCTreeParserTokenTypes.java : $(SRCDIR)/GnuCTreeParser.g $(GENDIR)/GNUCTokenTypes.txt
	mkdir -p $(GENDIR)
	$(ANTLR) -o $(GENDIR) $<
	touch $(GENDIR)/GnuCTreeParser.smap $(GENDIR)/GnuCTreeParserTokenTypes.txt \
	      $(GENDIR)/GnuCTreeParser.java $(GENDIR)/GnuCTreeParserTokenTypes.java

$(GENDIR)/GnuCEmitter.smap $(GENDIR)/GnuCEmitterTokenTypes.txt \
$(GENDIR)/GnuCEmitter.java \
$(GENDIR)/GnuCEmitterTokenTypes.java : $(SRCDIR)/GnuCEmitter.g $(GENDIR)/GNUCTokenTypes.txt
	mkdir -p $(GENDIR)
	$(ANTLR) -o $(GENDIR) $<
	touch $(GENDIR)/GnuCEmitter.smap $(GENDIR)/GnuCEmitterTokenTypes.txt \
	      $(GENDIR)/GnuCEmitter.java $(GENDIR)/GnuCEmitterTokenTypes.java 

$(GENDIR)/GnuCEmitterVectorEmulation.java : $(SRCDIR)/GnuCEmitterVectorEmulation.g $(SRCDIR)/GnuCEmitter.g $(GENDIR)/GNUCTokenTypes.txt
	mkdir -p $(GENDIR)
	$(ANTLR) -o $(GENDIR) -glib $(SRCDIR)/GnuCEmitter.g $<
	touch $(GENDIR)/GnuCEmitterVectorEmulation.java


$(GENDIR)/SymbolTableBuilder.java : $(SRCDIR)/SymbolTableBuilder.g $(GENDIR)/GNUCTokenTypes.txt
	mkdir -p $(GENDIR)
	$(ANTLR) -o $(GENDIR) $<
	touch $(GENDIR)/SymbolTableBuilder.java

$(GENDIR)/ThisRewriter.java : $(SRCDIR)/ThisRewriter.g $(SRCDIR)/GnuCTreeParser.g $(GENDIR)/GNUCTokenTypes.txt
	mkdir -p $(GENDIR)
	$(ANTLR) -o $(GENDIR) -glib $(SRCDIR)/GnuCTreeParser.g $<
	touch $(GENDIR)/ThisRewriter.java

$(GENDIR)/KernelSignature.java : $(SRCDIR)/KernelSignature.g $(SRCDIR)/GnuCTreeParser.g $(GENDIR)/GNUCTokenTypes.txt
	mkdir -p $(GENDIR)
	$(ANTLR) -o $(GENDIR) -glib $(SRCDIR)/GnuCTreeParser.g $<
	touch $(GENDIR)/KernelSignature.java

$(GENDIR)/OclRewriter.java : OclRewriter.g GnuCTreeParser.g $(GENDIR)/GNUCTokenTypes.txt
	mkdir -p $(GENDIR)
	$(ANTLR) -o $(GENDIR) -glib $(SRCDIR)/GnuCTreeParser.g $<
	touch $(GENDIR)/OclRewriter.java

$(GENDIR)/ReentrantRewriter.java : $(SRCDIR)/ReentrantRewriter.g $(SRCDIR)/GnuCTreeParser.g $(GENDIR)/GNUCTokenTypes.txt
	mkdir -p $(GENDIR)
	$(ANTLR) -o $(GENDIR) -glib $(SRCDIR)/GnuCTreeParser.g $<
	touch $(GENDIR)/ReentrantRewriter.java



