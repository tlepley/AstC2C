###
###     Makefile for H.26L encoder
###
###             generated for UNIX/LINUX environments
###             by H. Schwarz
###


NAME=   lencod

### include debug information: 1=yes, 0=no
#DBG= 0

DEPEND= dependencies

BINDIR= ../bin
INCDIR= inc
SRCDIR= src
OBJDIR= obj
OBJDIRSPLIT= objSplit

ADDSRCDIR= ../lcommon/src
ADDINCDIR= ../lcommon/inc

CC=     $(shell which gcc)

LIBS=   -lm
CFLAGS=  -std=gnu99 -ffloat-store -fno-strict-aliasing
FLAGS=  $(CFLAGS) -Wall -I$(INCDIR) -I$(ADDINCDIR) -D __USE_LARGEFILE64 -D _FILE_OFFSET_BITS=64

ifdef DBG
SUFFIX= .dbg
FLAGS+= -g
else
SUFFIX=
FLAGS+= -O3
endif

OBJSUF= .o$(SUFFIX)

SRC=    $(wildcard $(SRCDIR)/*.c) 
ADDSRC= $(wildcard $(ADDSRCDIR)/*.c)

OBJ=    $(SRC:$(SRCDIR)/%.c=$(OBJDIR)/%.o$(SUFFIX)) $(ADDSRC:$(ADDSRCDIR)/%.c=$(OBJDIR)/%.o$(SUFFIX)) 

BIN=    $(BINDIR)/$(NAME)$(SUFFIX).exe
C2C_BIN=    $(BINDIR)/c2c_$(NAME)$(SUFFIX).exe
C2CS_BIN=   $(BINDIR)/c2cs_$(NAME)$(SUFFIX).exe

C2C_SRC = $(OBJ:$(OBJDIR)/%.o$(SUFFIX)=$(OBJDIR)/%.c2c.c)
C2C_I   = $(OBJ:$(OBJDIR)/%.o$(SUFFIX)=$(OBJDIR)/%.i)
C2C_OBJ = $(C2C_SRC:$(OBJDIR)/%.c=$(OBJDIR)/%.o)
C2CS_SRC= $(OBJ:$(OBJDIR)/%.o$(SUFFIX)=$(OBJDIRSPLIT)/%.c2c.c) $(OBJDIRSPLIT)/DATA.h.c
C2CS_I  = $(OBJ:$(OBJDIR)/%.o$(SUFFIX)=$(OBJDIRSPLIT)/%.i)
C2CS_OBJ= $(C2CS_SRC:$(OBJDIRSPLIT)/%.c=$(OBJDIRSPLIT)/%.o)


default: depend $(BIN) $(C2C_BIN) $(C2CS_BIN) tags 

dependencies:
	@echo "" >dependencies

clean:
	@echo remove all objects
	@rm -f $(OBJDIR)/*
	@rm -f $(OBJDIRSPLIT)/*

cleanc2c:
	@echo remove all C2C objects
	@rm -f $(C2C_I) $(C2CS_I) $(C2CS_OBJ) $(C2CS_SRC) $(C2C_OBJ) $(C2C_SRC) $(OBJDIRSPLIT)/DATA.h

tags:
	@echo update tag table
	@ctags inc/*.h src/*.c



$(BIN): $(OBJ)
	@echo
	@echo 'creating binary "$(BIN)"'
	@$(CC) -o $(BIN) $(OBJ) $(LIBS)
	@echo '... done'
	@echo

ASTCC = ../../../bin/astc2c --keep -C
RCC   = ../../../bin/rc2c --keep -C

$(C2C_SRC) : $(SRC) $(ADDSRC)
	@mkdir -p $(OBJDIR)
	$(ASTCC) $(FLAGS) --forcelink --debug 1 --outdir $(OBJDIR) -- $(SRC) $(ADDSRC)

$(C2C_BIN): $(C2C_OBJ)
	@echo
	@echo 'creating C2C binary "$(C2C_BIN)"'
	@$(CC) -o $(C2C_BIN) $(C2C_OBJ) $(LIBS)
	@echo '... done'
	@echo

$(C2CS_SRC) : $(SRC) $(ADDSRC)
	@mkdir -p $(OBJDIRSPLIT)
	$(RCC) $(FLAGS) --forcelink --debug 1 --outdir $(OBJDIRSPLIT) -- $(SRC) $(ADDSRC)

$(C2CS_BIN): $(C2CS_OBJ)
	@echo
	@echo 'creating C2CS binary "$(C2CS_OBJ)"'
	@$(CC) -o $(C2CS_BIN) $(C2CS_OBJ) $(LIBS)
	@echo '... done'
	@echo

depend:
	@echo
	@echo 'checking dependencies'
	@$(SHELL) -ec '$(CC) -MM $(CFLAGS) -I$(INCDIR) -I$(ADDINCDIR) $(SRC) $(ADDSRC)                  \
         | sed '\''s@\(.*\)\.o[ :]@$(OBJDIR)/\1.o$(SUFFIX):@g'\''               \
         >$(DEPEND)'
	@echo


$(OBJDIR)/%.o$(SUFFIX): $(SRCDIR)/%.c
	@echo 'compiling object file "$@" ...'
	@mkdir -p $(OBJDIR)
	@$(CC) -c -o $@ $(FLAGS) $<

$(OBJDIR)/%.o$(SUFFIX): $(ADDSRCDIR)/%.c
	@echo 'compiling object file "$@" ...'
	@mkdir -p $(OBJDIR)
	@$(CC) -c -o $@ $(FLAGS) $<

$(OBJDIR)/%.o: $(OBJDIR)/%.c
	@echo 'compiling object file "$@" (c2c/c2cs) ...'
	$(CC) -c -o $@ $(FLAGS) $<

$(OBJDIRSPLIT)/%.o: $(OBJDIRSPLIT)/%.c
	@echo 'compiling object file "$@" (c2cs) ...'
	$(CC) -c -o $@ $(FLAGS) $<

include $(DEPEND)

