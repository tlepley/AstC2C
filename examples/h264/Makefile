##########################################################
# Home ###################################################
##########################################################


BINDIR= ./bin

# Encoder
ENC_NAME=     lencod
ENC_BIN=      $(ENC_NAME).exe
C2C_ENC_BIN=  c2c_$(ENC_NAME).exe
C2CS_ENC_BIN= c2cs_$(ENC_NAME).exe
ENC_CONF=     encoder.cfg
C2C_ENC_CONF= encoder_c2c.cfg
C2CS_ENC_CONF=encoder_c2cs.cfg

# Decoder
DEC_NAME=     ldecod
DEC_BIN=      $(DEC_NAME).exe
C2C_DEC_BIN=  c2c_$(DEC_NAME).exe
C2CS_DEC_BIN= c2cs_$(DEC_NAME).exe
DEC_CONF=     decoder.cfg
C2C_DEC_CONF= decoder_c2c.cfg
C2CS_DEC_CONF=decoder_c2cs.cfg


bins:
	cd lencod;make
	cd ldecod;make

test : bins
	cd $(BINDIR);./$(ENC_BIN)     -f $(ENC_CONF)
	cd $(BINDIR);./$(C2C_ENC_BIN) -f $(C2C_ENC_CONF)
	cd $(BINDIR);./$(C2CS_ENC_BIN) -f $(C2CS_ENC_CONF)
	@echo "########################################################################"
	@echo "# Comparing encoding video files                                       #"
	@echo "########################################################################"
	@cd $(BINDIR);diff test.264 test_c2c.264; \
	exit_status=$$?; \
	if [ $$exit_status -ne 0 ]; then \
	  echo "ERROR, both videos (STD/C2C) are different"; \
	else \
	  echo "OK, both videos (STD/C2C) are equivalent"; \
	fi;
	@cd $(BINDIR);diff test.264 test_c2cs.264; \
	exit_status=$$?; \
	if [ $$exit_status -ne 0 ]; then \
	  echo "ERROR, both videos (STD/C2CS) are different"; \
	else \
	  echo "OK, both videos (STD/C2CS) are equivalent"; \
	fi;
	@cd $(BINDIR);diff test_rec.yuv test_rec_c2c.yuv; \
	exit_status=$$?; \
	if [ $$exit_status -ne 0 ]; then \
	  echo "ERROR, both yuv (STD/C2C) are different"; \
	else \
	  echo "OK, both yuv (STD/C2C) are equivalent"; \
	fi;
	@cd $(BINDIR);diff test_rec.yuv test_rec_c2cs.yuv; \
	exit_status=$$?; \
	if [ $$exit_status -ne 0 ]; then \
	  echo "ERROR, both yuv (STD/C2CS) are different"; \
	else \
	  echo "OK, both yuv (STD/C2CS) are equivalent"; \
	fi; \
	echo
	echo
	echo
	cd $(BINDIR);./$(DEC_BIN)     $(DEC_CONF)
	cd $(BINDIR);./$(C2C_DEC_BIN) $(C2C_DEC_CONF)
	cd $(BINDIR);./$(C2CS_DEC_BIN) $(C2CS_DEC_CONF)
	@echo "########################################################################"
	@echo "# Comparing decoding video files                                       #"
	@echo "########################################################################"
	@cd $(BINDIR);diff test_dec.yuv test_dec_c2c.yuv; \
	exit_status=$$?; \
	if [ $$exit_status -ne 0 ]; then \
	  echo "ERROR, both yuv (STD/C2C) are different"; \
	else \
	  echo "OK, both yuv (STD/C2C) are equivalent"; \
	fi;
	@cd $(BINDIR);diff test_dec.yuv test_dec_c2cs.yuv; \
	exit_status=$$?; \
	if [ $$exit_status -ne 0 ]; then \
	  echo "ERROR, both yuv (STD/C2CS) are different"; \
	else \
	  echo "OK, both yuv (STD/C2CS) are equivalent"; \
	fi; \
	echo



clean:
	cd $(BINDIR);rm -f dataDec.txt test_dec_c2c.yuv test_dec_c2cs.yuv log.dec test_dec.yuv test_c2c.264 test_rec_c2c.yuv testc2c.264 test_c2cs.264 test_rec_c2cs.yuv testc2cs.264 data.txt log.dat test.264 test_rec.yuv stats.dat leakybucketparam.cfg

cleanall: clean
	cd lencod;make clean;
	cd ldecod;make clean;
	cd $(BINDIR);rm -f lencod.exe ldecod.exe c2c_lencod.exe c2c_ldecod.exe c2cs_lencod.exe c2cs_ldecod.exe

cleanc2c:
	cd lencod;make cleanc2c;
	cd ldecod;make cleanc2c;
	cd $(BINDIR);rm -f $(C2C_ENC_BIN) $(C2CS_ENC_BIN) $(C2C_DEC_BIN) $(C2CS_DEC_BIN)
