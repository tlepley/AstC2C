# Microsoft Developer Studio Project File - Name="lencod" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Console Application" 0x0103

CFG=lencod - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "lencod.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "lencod.mak" CFG="lencod - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "lencod - Win32 Release" (based on "Win32 (x86) Console Application")
!MESSAGE "lencod - Win32 Debug" (based on "Win32 (x86) Console Application")
!MESSAGE "lencod - Win32 VTune" (based on "Win32 (x86) Console Application")
!MESSAGE "lencod - Win32 Intel" (based on "Win32 (x86) Console Application")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=xicl6.exe
RSC=rc.exe

!IF  "$(CFG)" == "lencod - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "lencod\Release"
# PROP Intermediate_Dir "lencod\Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /YX /FD /c
# ADD CPP /nologo /W3 /GX /Zi /O2 /Ob2 /I "lencod\inc" /D "WIN32" /D "DEBUG" /D "_CONSOLE" /D "_MBCS" /FR /YX /FD /c
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=xilink6.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /machine:I386
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib Ws2_32.lib /nologo /subsystem:console /debug /machine:I386 /out:"./bin/lencod.exe" /fixed:no
# SUBTRACT LINK32 /pdb:none

!ELSEIF  "$(CFG)" == "lencod - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "lencod/Debug"
# PROP Intermediate_Dir "lencod/Debug"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /YX /FD /GZ /c
# ADD CPP /nologo /W3 /GX /ZI /Od /I "lencod/inc" /I "lcommon/inc" /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /FR /YX /FD /GZ /c
# ADD BASE RSC /l 0x409 /d "_DEBUG"
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=xilink6.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /debug /machine:I386 /pdbtype:sept
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib Ws2_32.lib /nologo /subsystem:console /profile /debug /machine:I386 /out:"./bin/lencod.exe"

!ELSEIF  "$(CFG)" == "lencod - Win32 VTune"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "lencod\Win32_VTune"
# PROP BASE Intermediate_Dir "lencod\Win32_VTune"
# PROP BASE Ignore_Export_Lib 0
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "lencod\Win32_VTune"
# PROP Intermediate_Dir "lencod\Win32_VTune"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /GX /Zi /O2 /Ob2 /I "lencod\inc" /I "lcommon\inc" /D "WIN32" /D "DEBUG" /D "_CONSOLE" /D "_MBCS" /Fr /YX /FD /c
# ADD CPP /nologo /W3 /GX /Zi /Op /Ob2 /I "lencod\inc" /I "lcommon\inc" /D "WIN32" /D "DEBUG" /D "_CONSOLE" /D "_MBCS" /FR /YX /FD /c
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=xilink6.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /debug /machine:I386 /out:"./bin/lencod.exe" /fixed:no
# SUBTRACT BASE LINK32 /pdb:none
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /debug /machine:I386 /out:"./bin/lencod_vtune.exe" /fixed:no
# SUBTRACT LINK32 /pdb:none

!ELSEIF  "$(CFG)" == "lencod - Win32 Intel"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "lencod\Win32_Intel"
# PROP BASE Intermediate_Dir "lencod\Win32_Intel"
# PROP BASE Ignore_Export_Lib 0
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "lencod\Win32_Intel"
# PROP Intermediate_Dir "lencod\Win32_Intel"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /GX /Zi /O2 /Ob2 /I "lencod\inc" /I "lcommon\inc" /D "WIN32" /D "DEBUG" /D "_CONSOLE" /D "_MBCS" /Fr /YX /FD /c
# ADD CPP /nologo /W4 /GX /Zi /O2 /Ob2 /I "lencod\inc" /I "lcommon\inc" /D "WIN32" /D "DEBUG" /D "_CONSOLE" /D "_MBCS" /FR /YX /FD /QaxKWNP /fast /c
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=xilink6.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /debug /machine:I386 /out:"./bin/lencod.exe"
# SUBTRACT BASE LINK32 /pdb:none
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib ws2_32.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /debug /machine:I386 /out:"./bin/lencod.exe" /fixed:no
# SUBTRACT LINK32 /pdb:none

!ENDIF 

# Begin Target

# Name "lencod - Win32 Release"
# Name "lencod - Win32 Debug"
# Name "lencod - Win32 VTune"
# Name "lencod - Win32 Intel"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=.\lencod\src\annexb.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\biariencode.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\block.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\cabac.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\configfile.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\context_ini.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\decoder.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\explicit_gop.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\filehandle.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\fmo.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\header.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\image.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\img_chroma.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\img_luma.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\intrarefresh.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\leaky_bucket.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\lencod.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\loopFilter.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\macroblock.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\mb_access.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\mbuffer.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\md_high.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\md_highfast.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\md_highloss.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\md_low.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\me_distortion.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\me_epzs.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\me_fullfast.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\me_fullsearch.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\me_umhex.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\me_umhexsmp.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\memalloc.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\mode_decision.c
# End Source File
# Begin Source File

SOURCE=".\lencod\src\mv-search.c"
# End Source File
# Begin Source File

SOURCE=.\lencod\src\nal.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\nalu.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\nalucommon.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\output.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\parset.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\parsetcommon.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\q_matrix.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\q_offsets.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\ratectl.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\rc_quadratic.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\rdopt.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\rdopt_coding_state.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\rdpicdecision.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\refbuf.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\rtp.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\sei.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\slice.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\symbol.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\transform8x8.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\vlc.c
# End Source File
# Begin Source File

SOURCE=.\lencod\src\weighted_prediction.c
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=.\lencod\inc\annexb.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\biariencode.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\block.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\cabac.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\configfile.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\context_ini.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\contributors.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\ctx_tables.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\defines.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\elements.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\explicit_gop.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\fmo.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\global.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\header.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\ifunctions.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\image.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\img_chroma.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\img_luma.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\intrarefresh.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\leaky_bucket.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\macroblock.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\mb_access.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\mbuffer.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\me_distortion.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\me_epzs.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\me_fullfast.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\me_fullsearch.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\me_umhex.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\me_umhexsmp.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\memalloc.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\minmax.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\mode_decision.h
# End Source File
# Begin Source File

SOURCE=".\lencod\inc\mv-search.h"
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\nalu.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\nalucommon.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\output.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\parset.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\parsetcommon.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\q_matrix.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\q_offsets.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\ratectl.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\rc_quadratic.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\rdopt_coding_state.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\refbuf.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\rtp.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\sei.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\symbol.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\transform8x8.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\vlc.h
# End Source File
# Begin Source File

SOURCE=.\lencod\inc\win32.h
# End Source File
# End Group
# Begin Source File

SOURCE=.\bin\encoder.cfg
# End Source File
# Begin Source File

SOURCE=.\bin\encoder_yuv422.cfg
# End Source File
# End Target
# End Project
