
/*!
 ************************************************************************
 *  \file
 *     sei.c
 *  \brief
 *     implementation of SEI related functions
 *  \author(s)
 *      - Dong Tian                             <tian@cs.tut.fi>
 *
 ************************************************************************
 */

#include <stdlib.h>
#include <assert.h>
#include <memory.h>
#include <string.h>
#include "global.h"

#include "memalloc.h"
#include "rtp.h"
#include "mbuffer.h"
#include "sei.h"
#include "vlc.h"

Boolean seiHasTemporal_reference=FALSE;
Boolean seiHasClock_timestamp=FALSE;
Boolean seiHasPanscan_rect=FALSE;
Boolean seiHasHrd_picture=FALSE;
Boolean seiHasFiller_payload=FALSE;
Boolean seiHasUser_data_registered_itu_t_t35=FALSE;
Boolean seiHasUser_data_unregistered=FALSE;
Boolean seiHasRecoveryPoint_info=FALSE;
Boolean seiHasRef_pic_buffer_management_repetition=FALSE;
Boolean seiHasSpare_picture=FALSE;

Boolean seiHasBuffering_period=FALSE;
Boolean seiHasPicTiming_info=FALSE;

Boolean seiHasSceneInformation=FALSE;

Boolean seiHasSubseq_information=FALSE;
Boolean seiHasSubseq_layer_characteristics=FALSE;
Boolean seiHasSubseq_characteristics=FALSE;
Boolean seiHasTone_mapping=FALSE;
Boolean seiHasPostFilterHints_info=FALSE;
//#define PRINT_TONE_MAPPING

/*
 ************************************************************************
 *  \basic functions on supplemental enhancement information
 *  \brief
 *     The implementations are based on FCD
 ************************************************************************
 */

//! sei_message[0]: this struct is to store the sei message packetized independently
//! sei_message[1]: this struct is to store the sei message packetized together with slice data
sei_struct sei_message[2];

void InitSEIMessages()
{
  int i;
  for (i=0; i<2; i++)
  {
    sei_message[i].data = malloc(MAXRTPPAYLOADLEN);
    if( sei_message[i].data == NULL ) no_mem_exit("InitSEIMessages: sei_message[i].data");
    sei_message[i].subPacketType = SEI_PACKET_TYPE;
    clear_sei_message(i);
  }

  // init sei messages
  seiSparePicturePayload.data = NULL;
  InitSparePicture();
  InitSubseqChar();
  if (input->NumFramesInELSubSeq != 0)
    InitSubseqLayerInfo();
  InitSceneInformation();
  // init panscanrect sei message
  InitPanScanRectInfo();
  // init user_data_unregistered
  InitUser_data_unregistered();
  // init user_data_unregistered
  InitUser_data_registered_itu_t_t35();
  // init user_RandomAccess
  InitRandomAccess();
  // Init tone_mapping
  InitToneMapping();
  // init post_filter_hints
  InitPostFilterHints();
  // init BufferingPeriod
  InitBufferingPeriod();
  // init PicTiming
  InitPicTiming();
}

void CloseSEIMessages()
{
  int i;

  if (input->NumFramesInELSubSeq != 0)
    CloseSubseqLayerInfo();

  CloseSubseqChar();
  CloseSparePicture();
  CloseSceneInformation();
  ClosePanScanRectInfo();
  CloseUser_data_unregistered();
  CloseUser_data_registered_itu_t_t35();
  CloseRandomAccess();
  CloseToneMapping();
  ClosePostFilterHints();
  CloseBufferingPeriod();
  ClosePicTiming();

  for (i=0; i<MAX_LAYER_NUMBER; i++)
  {
    if ( sei_message[i].data ) free( sei_message[i].data );
    sei_message[i].data = NULL;
  }
}

Boolean HaveAggregationSEI()
{
  if (sei_message[AGGREGATION_SEI].available && img->type != B_SLICE)
    return TRUE;
  if (seiHasSubseqInfo)
    return TRUE;
  if (seiHasSubseqLayerInfo && img->number == 0)
    return TRUE;
  if (seiHasSubseqChar)
    return TRUE;
  if (seiHasSceneInformation)
    return TRUE;
  if (seiHasPanScanRectInfo)
    return TRUE;
  if (seiHasUser_data_unregistered_info)
    return TRUE;
  if (seiHasUser_data_registered_itu_t_t35_info)
    return TRUE;
  if (seiHasRecoveryPoint_info)
    return TRUE;
  if (seiHasTone_mapping)
    return TRUE;
  if (seiHasPostFilterHints_info)
    return TRUE;
  if (seiHasBuffering_period)
    return TRUE;
  if (seiHasPicTiming_info)
    return TRUE;

  return FALSE;
//  return input->SparePictureOption && ( seiHasSpare_picture || seiHasSubseq_information ||
//    seiHasSubseq_layer_characteristics || seiHasSubseq_characteristics );
}

/*!
 ************************************************************************
 *  \brief
 *     write one sei payload to the sei message
 *  \param id
 *    0, if this is the normal packet\n
 *    1, if this is a aggregation packet
 *  \param payload
 *    a pointer that point to the sei payload. Note that the bitstream
 *    should have be byte aligned already.
 *  \param payload_size
 *    the size of the sei payload
 *  \param payload_type
 *    the type of the sei payload
 *  \par Output
 *    the content of the sei message (sei_message[id]) is updated.
 ************************************************************************
 */
void write_sei_message(int id, byte* payload, int payload_size, int payload_type)
{
  int offset, type, size;
  assert(payload_type >= 0 && payload_type < SEI_MAX_ELEMENTS);

  type = payload_type;
  size = payload_size;
  offset = sei_message[id].payloadSize;

  while ( type > 254 )
  {
    sei_message[id].data[offset++] = 0xFF;
    type = type - 255;
  }
  sei_message[id].data[offset++] = (byte) type;

  while ( size > 254 )
  {
    sei_message[id].data[offset++] = 0xFF;
    size = size - 255;
  }
  sei_message[id].data[offset++] = (byte) size;

  memcpy(sei_message[id].data + offset, payload, payload_size);
  offset += payload_size;

  sei_message[id].payloadSize = offset;
}

/*!
 ************************************************************************
 *  \brief
 *     write rbsp_trailing_bits to the sei message
 *  \param id
 *    0, if this is the normal packet \n
 *    1, if this is a aggregation packet
 *  \par Output
 *    the content of the sei message is updated and ready for packetisation
 ************************************************************************
 */
void finalize_sei_message(int id)
{
  int offset = sei_message[id].payloadSize;

  sei_message[id].data[offset] = 0x80;
  sei_message[id].payloadSize++;

  sei_message[id].available = TRUE;
}

/*!
 ************************************************************************
 *  \brief
 *     empty the sei message buffer
 *  \param id
 *    0, if this is the normal packet \n
 *    1, if this is a aggregation packet
 *  \par Output
 *    the content of the sei message is cleared and ready for storing new
 *      messages
 ************************************************************************
 */
void clear_sei_message(int id)
{
  memset( sei_message[id].data, 0, MAXRTPPAYLOADLEN);
  sei_message[id].payloadSize       = 0;
  sei_message[id].available         = FALSE;
}

/*!
 ************************************************************************
 *  \brief
 *     copy the bits from one bitstream buffer to another one
 *  \param dest
 *    pointer to the dest bitstream buffer
 *  \param source
 *    pointer to the source bitstream buffer
 *  \par Output
 *    the content of the dest bitstream is changed.
 ************************************************************************
 */
void AppendTmpbits2Buf( Bitstream* dest, Bitstream* source )
{
  int i, j;
  byte mask;
  int bits_in_last_byte;

  // copy the first bytes in source buffer
  for (i=0; i<source->byte_pos; i++)
  {
    mask = 0x80;
    for (j=0; j<8; j++)
    {
      dest->byte_buf <<= 1;
      if (source->streamBuffer[i] & mask)
        dest->byte_buf |= 1;
      dest->bits_to_go--;
      mask >>= 1;
      if (dest->bits_to_go==0)
      {
        dest->bits_to_go = 8;
        dest->streamBuffer[dest->byte_pos++]=dest->byte_buf;
        dest->byte_buf = 0;
      }
    }
  }
  // copy the last byte, there are still (8-source->bits_to_go) bits in the source buffer
  bits_in_last_byte = 8-source->bits_to_go;
  if ( bits_in_last_byte > 0 )
  {
    mask = (byte) (1 << (bits_in_last_byte-1));
    for (j=0; j<bits_in_last_byte; j++)
    {
      dest->byte_buf <<= 1;
      if (source->byte_buf & mask)
        dest->byte_buf |= 1;
      dest->bits_to_go--;
      mask >>= 1;
      if (dest->bits_to_go==0)
      {
        dest->bits_to_go = 8;
        dest->streamBuffer[dest->byte_pos++]=dest->byte_buf;
        dest->byte_buf = 0;
      }
    }
  }
}

/*
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  \functions on spare pictures
 *  \brief
 *     implementation of Spare Pictures related functions based on
 *      JVT-D100
 *  \author
 *      Dong Tian                 <tian@cs.tut.fi>
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

// global variables for spare pictures

// In current implementation, Sept 2002, the spare picture info is
// paketized together with the immediately following frame. Thus we
// define one set of global variables to save the info.
Boolean seiHasSparePicture = FALSE;
spare_picture_struct seiSparePicturePayload;

/*!
 ************************************************************************
 *  \brief
 *      Init the global variables for spare picture information
 ************************************************************************
 */
void InitSparePicture()
{
  if ( seiSparePicturePayload.data != NULL ) CloseSparePicture();

  seiSparePicturePayload.data = malloc( sizeof(Bitstream) );
  if ( seiSparePicturePayload.data == NULL ) no_mem_exit("InitSparePicture: seiSparePicturePayload.data");
  seiSparePicturePayload.data->streamBuffer = malloc(MAXRTPPAYLOADLEN);
  if ( seiSparePicturePayload.data->streamBuffer == NULL ) no_mem_exit("InitSparePicture: seiSparePicturePayload.data->streamBuffer");
  memset( seiSparePicturePayload.data->streamBuffer, 0, MAXRTPPAYLOADLEN);
  seiSparePicturePayload.num_spare_pics = 0;
  seiSparePicturePayload.target_frame_num = 0;

  seiSparePicturePayload.data->bits_to_go  = 8;
  seiSparePicturePayload.data->byte_pos    = 0;
  seiSparePicturePayload.data->byte_buf    = 0;
}

/*!
 ************************************************************************
 *  \brief
 *      Close the global variables for spare picture information
 ************************************************************************
 */
void CloseSparePicture()
{
  if (seiSparePicturePayload.data->streamBuffer)
    free(seiSparePicturePayload.data->streamBuffer);
  seiSparePicturePayload.data->streamBuffer = NULL;
  if (seiSparePicturePayload.data)
    free(seiSparePicturePayload.data);
  seiSparePicturePayload.data = NULL;
  seiSparePicturePayload.num_spare_pics = 0;
  seiSparePicturePayload.target_frame_num = 0;
}

/*!
 ************************************************************************
 *  \brief
 *     Calculate the spare picture info, save the result in map_sp
 *      then compose the spare picture information.
 *  \par Output
 *      the spare picture payload is available in *seiSparePicturePayload*
 *      the syntax elements in the loop (see FCD), excluding the two elements
 *      at the beginning.
 ************************************************************************
 */
void CalculateSparePicture()
{
  /*
  int i, j, tmp, i0, j0, m;
  byte **map_sp;
  int delta_spare_frame_num;
  Bitstream *tmpBitstream;

  int num_of_mb=(img->height/16) * (img->width/16);
  int threshold1 = 16*16*input->SPDetectionThreshold;
  int threshold2 = num_of_mb * input->SPPercentageThreshold / 100;
  int ref_area_indicator;
  int CandidateSpareFrameNum, SpareFrameNum;
  int possible_spare_pic_num;

  // define it for debug purpose
  #define WRITE_MAP_IMAGE

#ifdef WRITE_MAP_IMAGE
  byte **y;
  int k;
  FILE* fp;
  static int first = 1;
  char map_file_name[255]="map.yuv";
#endif

  // basic check
  if (fb->picbuf_short[0]->used==0 || fb->picbuf_short[1]->used==0)
  {
#ifdef WRITE_MAP_IMAGE
    fp = fopen( map_file_name, "wb" );
    assert( fp != NULL );
    // write the map image
    for (i=0; i < img->height; i++)
      for (j=0; j < img->width; j++)
        fputc(0, fp);

    for (k=0; k < 2; k++)
      for (i=0; i < img->height/2; i++)
        for (j=0; j < img->width/2; j++)
          fputc(128, fp);
    fclose( fp );
#endif
    seiHasSparePicture = FALSE;
    return;
  }
  seiHasSparePicture = TRUE;

  // set the global bitstream memory.
  InitSparePicture();
  seiSparePicturePayload.target_frame_num = img->number % MAX_FN;
  // init the local bitstream memory.
  tmpBitstream = malloc(sizeof(Bitstream));
  if ( tmpBitstream == NULL ) no_mem_exit("CalculateSparePicture: tmpBitstream");
  tmpBitstream->streamBuffer = malloc(MAXRTPPAYLOADLEN);
  if ( tmpBitstream->streamBuffer == NULL ) no_mem_exit("CalculateSparePicture: tmpBitstream->streamBuffer");
  memset( tmpBitstream->streamBuffer, 0, MAXRTPPAYLOADLEN);

#ifdef WRITE_MAP_IMAGE
  if ( first )
  {
    fp = fopen( map_file_name, "wb" );
    first = 0;
  }
  else
    fp = fopen( map_file_name, "ab" );
  get_mem2D(&y, img->height, img->width);
#endif
  get_mem2D(&map_sp, img->height/16, img->width/16);

  if (fb->picbuf_short[2]->used!=0) possible_spare_pic_num = 2;
  else possible_spare_pic_num = 1;
  // loop over the spare pictures
  for (m=0; m<possible_spare_pic_num; m++)
  {
    // clear the temporal bitstream buffer
    tmpBitstream->bits_to_go  = 8;
    tmpBitstream->byte_pos    = 0;
    tmpBitstream->byte_buf    = 0;
    memset( tmpBitstream->streamBuffer, 0, MAXRTPPAYLOADLEN);

    // set delta_spare_frame_num
    // the order of the following lines cannot be changed.
    if (m==0)
      CandidateSpareFrameNum = seiSparePicturePayload.target_frame_num - 1; // TargetFrameNum - 1;
    else
      CandidateSpareFrameNum = SpareFrameNum - 1;
    if ( CandidateSpareFrameNum < 0 ) CandidateSpareFrameNum = MAX_FN - 1;
    SpareFrameNum = fb->picbuf_short[m+1]->frame_num_256;
    delta_spare_frame_num = CandidateSpareFrameNum - SpareFrameNum;
    assert( delta_spare_frame_num == 0 );

    // calculate the spare macroblock map of one spare picture
    // the results are stored into map_sp[][]
    for (i=0; i < img->height/16; i++)
      for (j=0; j < img->width/16; j++)
      {
        tmp = 0;
        for (i0=0; i0<16; i0++)
          for (j0=0; j0<16; j0++)
            tmp+=iabs(fb->picbuf_short[m+1]->Refbuf11[(i*16+i0)*img->width+j*16+j0]-
                       fb->picbuf_short[0]->Refbuf11[(i*16+i0)*img->width+j*16+j0]);
        tmp = (tmp<=threshold1? 255 : 0);
        map_sp[i][j] = (tmp==0? 1 : 0);
#ifdef WRITE_MAP_IMAGE
//        if (m==0)
        {
        for (i0=0; i0<16; i0++)
          for (j0=0; j0<16; j0++)
            y[i*16+i0][j*16+j0]=tmp;
        }
#endif
      }

    // based on map_sp[][], compose the spare picture information
    // and write the spare picture information to a temp bitstream
    tmp = 0;
    for (i=0; i < img->height/16; i++)
      for (j=0; j < img->width/16; j++)
        if (map_sp[i][j]==0) tmp++;
    if ( tmp > threshold2 )
      ref_area_indicator = 0;
    else if ( !CompressSpareMBMap(map_sp, tmpBitstream) )
      ref_area_indicator = 1;
    else
      ref_area_indicator = 2;

//    printf( "ref_area_indicator = %d\n", ref_area_indicator );

#ifdef WRITE_MAP_IMAGE
    // write the map to a file
//    if (m==0)
    {
      // write the map image
      for (i=0; i < img->height; i++)
        for (j=0; j < img->width; j++)
        {
          if ( ref_area_indicator == 0 ) fputc(255, fp);
          else fputc(y[i][j], fp);
        }

      for (k=0; k < 2; k++)
        for (i=0; i < img->height/2; i++)
          for (j=0; j < img->width/2; j++)
            fputc(128, fp);
    }
#endif

    // Finnally, write the current spare picture information to
    // the global variable: seiSparePicturePayload
    ComposeSparePictureMessage(delta_spare_frame_num, ref_area_indicator, tmpBitstream);
    seiSparePicturePayload.num_spare_pics++;
  }  // END for (m=0; m<2; m++)

  free_mem2D( map_sp );
  free( tmpBitstream->streamBuffer );
  free( tmpBitstream );

#ifdef WRITE_MAP_IMAGE
  free_mem2D( y );
  fclose( fp );
#undef WRITE_MAP_IMAGE
#endif
  */
}

/*!
 ************************************************************************
 *  \brief
 *      compose the spare picture information.
 *  \param delta_spare_frame_num
 *      see FCD
 *  \param ref_area_indicator
 *      Indicate how to represent the spare mb map
 *  \param tmpBitstream
 *      pointer to a buffer to save the payload
 *  \par Output
 *      bitstream: the composed spare picture payload are
 *        ready to put into the sei_message.
 ************************************************************************
 */
void ComposeSparePictureMessage(int delta_spare_frame_num, int ref_area_indicator, Bitstream *tmpBitstream)
{
  Bitstream *bitstream = seiSparePicturePayload.data;
  SyntaxElement sym;

  sym.type = SE_HEADER;
  sym.mapping = ue_linfo;

  sym.value1 = delta_spare_frame_num;
  writeSyntaxElement2Buf_UVLC(&sym, bitstream);
  sym.value1 = ref_area_indicator;
  writeSyntaxElement2Buf_UVLC(&sym, bitstream);

  AppendTmpbits2Buf( bitstream, tmpBitstream );
}

/*!
 ************************************************************************
 *  \brief
 *      test if the compressed spare mb map will occupy less mem and
 *      fill the payload buffer.
 *  \param map_sp
 *      in which the spare picture information are stored.
 *  \param bitstream
 *      pointer to a buffer to save the payload
 *  \return
 *      TRUE: If it is compressed version, \n
 *             FALSE: If it is not compressed.
 ************************************************************************
 */
Boolean CompressSpareMBMap(unsigned char **map_sp, Bitstream *bitstream)
{
  int j, k;
  int noc, bit0, bit1, bitc;
  SyntaxElement sym;
  int x, y, left, right, bottom, top, directx, directy;

  // this is the size of the uncompressed mb map:
  int size_uncompressed = (img->height/16) * (img->width/16);
  int size_compressed   = 0;
  Boolean ret;

  // initialization
  sym.type = SE_HEADER;
  sym.mapping = ue_linfo;
  noc = 0;
  bit0 = 0;
  bit1 = 1;
  bitc = bit0;

  // compress the map, the result goes to the temporal bitstream buffer
  x = ( img->width/16 - 1 ) / 2;
  y = ( img->height/16 - 1 ) / 2;
  left = right = x;
  top = bottom = y;
  directx = 0;
  directy = 1;
  for (j=0; j<img->height/16; j++)
    for (k=0; k<img->width/16; k++)
    {
      // check current mb
      if ( map_sp[y][x] == bitc ) noc++;
      else
      {
        sym.value1 = noc;
        size_compressed += writeSyntaxElement2Buf_UVLC(&sym, bitstream);    // the return value indicate the num of bits written
        noc=0;
      }
      // go to the next mb:
      if ( directx == -1 && directy == 0 )
      {
        if (x > left) x--;
        else if (x == 0)
        {
          y = bottom + 1;
          bottom++;
          directx = 1;
          directy = 0;
        }
        else if (x == left)
        {
          x--;
          left--;
          directx = 0;
          directy = 1;
        }
      }
      else if ( directx == 1 && directy == 0 )
      {
        if (x < right) x++;
        else if (x == img->width/16 - 1)
        {
          y = top - 1;
          top--;
          directx = -1;
          directy = 0;
        }
        else if (x == right)
        {
          x++;
          right++;
          directx = 0;
          directy = -1;
        }
      }
      else if ( directx == 0 && directy == -1 )
      {
        if ( y > top) y--;
        else if (y == 0)
        {
          x = left - 1;
          left--;
          directx = 0;
          directy = 1;
        }
        else if (y == top)
        {
          y--;
          top--;
          directx = -1;
          directy = 0;
        }
      }
      else if ( directx == 0 && directy == 1 )
      {
        if (y < bottom) y++;
        else if (y == img->height/16 - 1)
        {
          x = right+1;
          right++;
          directx = 0;
          directy = -1;
        }
        else if (y == bottom)
        {
          y++;
          bottom++;
          directx = 1;
          directy = 0;
        }
      }
    }
  if (noc!=0)
  {
    sym.value1 = noc;
    size_compressed += writeSyntaxElement2Buf_UVLC(&sym, bitstream);
  }

  ret = (size_compressed<size_uncompressed? TRUE : FALSE);
  if ( !ret ) // overwrite the streambuffer with the original mb map
  {
    // write the mb map to payload bit by bit
    bitstream->byte_buf = 0;
    bitstream->bits_to_go = 8;
    bitstream->byte_pos = 0;
    for (j=0; j<img->height/16; j++)
    {
      for (k=0; k<img->width/16; k++)
      {
        bitstream->byte_buf <<= 1;
        if (map_sp[j][k]) bitstream->byte_buf |= 1;
        bitstream->bits_to_go--;
        if (bitstream->bits_to_go==0)
        {
          bitstream->bits_to_go = 8;
          bitstream->streamBuffer[bitstream->byte_pos++]=bitstream->byte_buf;
          bitstream->byte_buf = 0;
        }
      }
    }
  }

  return ret;
}

/*!
 ************************************************************************
 *  \brief
 *      Finalize the spare picture SEI payload.
 *        The spare picture paylaod will be ready for encapsulation, and it
 *        should be called before current picture packetized.
 *  \par Input
 *      seiSparePicturePayload.data: points to the payload starting from
 *        delta_spare_frame_num. (See FCD)
 *  \par Output
 *      seiSparePicturePayload.data is updated, pointing to the whole spare
 *        picture information: spare_picture( PayloadSize ) (See FCD)
 *        Make sure it is byte aligned.
 ************************************************************************
 */
void FinalizeSpareMBMap()
{
  int CurrFrameNum = img->number % MAX_FN;
  int delta_frame_num;
  SyntaxElement sym;
  Bitstream *dest, *source;

  sym.type = SE_HEADER;
  sym.mapping = ue_linfo;

  source = seiSparePicturePayload.data;
  dest = malloc(sizeof(Bitstream));
  if ( dest == NULL ) no_mem_exit("FinalizeSpareMBMap: dest");
  dest->streamBuffer = malloc(MAXRTPPAYLOADLEN);
  if ( dest->streamBuffer == NULL ) no_mem_exit("FinalizeSpareMBMap: dest->streamBuffer");
  dest->bits_to_go  = 8;
  dest->byte_pos    = 0;
  dest->byte_buf    = 0;
  memset( dest->streamBuffer, 0, MAXRTPPAYLOADLEN);

  //    delta_frame_num
  delta_frame_num = CurrFrameNum - seiSparePicturePayload.target_frame_num;
  if ( delta_frame_num < 0 ) delta_frame_num += MAX_FN;
  sym.value1 = delta_frame_num;
  writeSyntaxElement2Buf_UVLC(&sym, dest);

  // num_spare_pics_minus1
  sym.value1 = seiSparePicturePayload.num_spare_pics - 1;
  writeSyntaxElement2Buf_UVLC(&sym, dest);

  // copy the other bits
  AppendTmpbits2Buf( dest, source);

  // make sure the payload is byte aligned, stuff bits are 10..0
  if ( dest->bits_to_go != 8 )
  {
    (dest->byte_buf) <<= 1;
    dest->byte_buf |= 1;
    dest->bits_to_go--;
    if ( dest->bits_to_go != 0 ) (dest->byte_buf) <<= (dest->bits_to_go);
    dest->bits_to_go = 8;
    dest->streamBuffer[dest->byte_pos++]=dest->byte_buf;
    dest->byte_buf = 0;
  }
  seiSparePicturePayload.payloadSize = dest->byte_pos;

  // the payload is ready now
  seiSparePicturePayload.data = dest;
  free( source->streamBuffer );
  free( source );
}

/*
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  \functions on subseq information sei messages
 *  \brief
 *      JVT-D098
 *  \author
 *      Dong Tian                 <tian@cs.tut.fi>
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

Boolean seiHasSubseqInfo = FALSE;
subseq_information_struct seiSubseqInfo[MAX_LAYER_NUMBER];

/*!
 ************************************************************************
 *  \brief
 *      init subseqence info
 ************************************************************************
 */
void InitSubseqInfo(int currLayer)
{
  static unsigned short id = 0;

  seiHasSubseqInfo = TRUE;
  seiSubseqInfo[currLayer].subseq_layer_num = currLayer;
  seiSubseqInfo[currLayer].subseq_id = id++;
  seiSubseqInfo[currLayer].last_picture_flag = 0;
  seiSubseqInfo[currLayer].stored_frame_cnt = -1;
  seiSubseqInfo[currLayer].payloadSize = 0;

  seiSubseqInfo[currLayer].data = malloc( sizeof(Bitstream) );
  if ( seiSubseqInfo[currLayer].data == NULL ) no_mem_exit("InitSubseqInfo: seiSubseqInfo[currLayer].data");
  seiSubseqInfo[currLayer].data->streamBuffer = malloc( MAXRTPPAYLOADLEN );
  if ( seiSubseqInfo[currLayer].data->streamBuffer == NULL ) no_mem_exit("InitSubseqInfo: seiSubseqInfo[currLayer].data->streamBuffer");
  seiSubseqInfo[currLayer].data->bits_to_go  = 8;
  seiSubseqInfo[currLayer].data->byte_pos    = 0;
  seiSubseqInfo[currLayer].data->byte_buf    = 0;
  memset( seiSubseqInfo[currLayer].data->streamBuffer, 0, MAXRTPPAYLOADLEN );
}

/*!
 ************************************************************************
 *  \brief
 *      update subseqence info
 ************************************************************************
 */
void UpdateSubseqInfo(int currLayer)
{
  if (img->type != B_SLICE)
  {
    seiSubseqInfo[currLayer].stored_frame_cnt ++;
    seiSubseqInfo[currLayer].stored_frame_cnt = seiSubseqInfo[currLayer].stored_frame_cnt % MAX_FN;
  }

  if ( currLayer == 0 )
  {
    if ( img->number == input->no_frames-1 )
      seiSubseqInfo[currLayer].last_picture_flag = 1;
    else
      seiSubseqInfo[currLayer].last_picture_flag = 0;
  }
  if ( currLayer == 1 )
  {
    if ( ((IMG_NUMBER%(input->NumFramesInELSubSeq+1)==0) && (input->successive_Bframe != 0) && (IMG_NUMBER>0)) || // there are B frames
      ((IMG_NUMBER%(input->NumFramesInELSubSeq+1)==input->NumFramesInELSubSeq) && (input->successive_Bframe==0))  // there are no B frames
      )
      seiSubseqInfo[currLayer].last_picture_flag = 1;
    else
      seiSubseqInfo[currLayer].last_picture_flag = 0;
  }
}

/*!
 ************************************************************************
 *  \brief
 *      Finalize subseqence info
 ************************************************************************
 */
void FinalizeSubseqInfo(int currLayer)
{
  SyntaxElement sym;
  Bitstream *dest = seiSubseqInfo[currLayer].data;

  sym.type = SE_HEADER;
  sym.mapping = ue_linfo;

  sym.value1 = seiSubseqInfo[currLayer].subseq_layer_num;
  writeSyntaxElement2Buf_UVLC(&sym, dest);
  sym.value1 = seiSubseqInfo[currLayer].subseq_id;
  writeSyntaxElement2Buf_UVLC(&sym, dest);
  sym.bitpattern = seiSubseqInfo[currLayer].last_picture_flag;
  sym.len = 1;
  writeSyntaxElement2Buf_Fixed(&sym, dest);
  sym.value1 = seiSubseqInfo[currLayer].stored_frame_cnt;
  writeSyntaxElement2Buf_UVLC(&sym, dest);

  // make sure the payload is byte aligned, stuff bits are 10..0
  if ( dest->bits_to_go != 8 )
  {
    (dest->byte_buf) <<= 1;
    dest->byte_buf |= 1;
    dest->bits_to_go--;
    if ( dest->bits_to_go != 0 ) (dest->byte_buf) <<= (dest->bits_to_go);
    dest->bits_to_go = 8;
    dest->streamBuffer[dest->byte_pos++]=dest->byte_buf;
    dest->byte_buf = 0;
  }
  seiSubseqInfo[currLayer].payloadSize = dest->byte_pos;

//  printf("layer %d, last picture %d, stored_cnt %d\n", currLayer, seiSubseqInfo[currLayer].last_picture_flag, seiSubseqInfo[currLayer].stored_frame_cnt );
}

/*!
 ************************************************************************
 *  \brief
 *      Clear the payload buffer
 ************************************************************************
 */
void ClearSubseqInfoPayload(int currLayer)
{
  seiSubseqInfo[currLayer].data->bits_to_go  = 8;
  seiSubseqInfo[currLayer].data->byte_pos    = 0;
  seiSubseqInfo[currLayer].data->byte_buf    = 0;
  memset( seiSubseqInfo[currLayer].data->streamBuffer, 0, MAXRTPPAYLOADLEN );
  seiSubseqInfo[currLayer].payloadSize = 0;
}

/*!
 ************************************************************************
 *  \brief
 *      Close the global variables for spare picture information
 ************************************************************************
 */
void CloseSubseqInfo(int currLayer)
{
  seiSubseqInfo[currLayer].stored_frame_cnt = -1;
  seiSubseqInfo[currLayer].payloadSize = 0;

  free( seiSubseqInfo[currLayer].data->streamBuffer );
  free( seiSubseqInfo[currLayer].data );
}

/*
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  \functions on subseq layer characteristic sei messages
 *  \brief
 *      JVT-D098
 *  \author
 *      Dong Tian                 <tian@cs.tut.fi>
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

Boolean seiHasSubseqLayerInfo = FALSE;
subseq_layer_information_struct seiSubseqLayerInfo;

/*!
 ************************************************************************
 *  \brief
 *      Init the global variables for spare picture information
 ************************************************************************
 */
void InitSubseqLayerInfo()
{
  int i;
  seiHasSubseqLayerInfo = TRUE;
  seiSubseqLayerInfo.layer_number = 0;
  for (i=0; i<MAX_LAYER_NUMBER; i++)
  {
    seiSubseqLayerInfo.bit_rate[i] = 0;
    seiSubseqLayerInfo.frame_rate[i] = 0;
    seiSubseqLayerInfo.layer_number++;
  }
}

/*!
 ************************************************************************
 *  \brief
 *
 ************************************************************************
 */
void CloseSubseqLayerInfo()
{
}

/*!
 ************************************************************************
 *  \brief
 *      Write the data to buffer, which is byte aligned
 ************************************************************************
 */
void FinalizeSubseqLayerInfo()
{
  int i, pos;
  pos = 0;
  seiSubseqLayerInfo.payloadSize = 0;
  for (i=0; i<seiSubseqLayerInfo.layer_number; i++)
  {
    *((unsigned short*)&(seiSubseqLayerInfo.data[pos])) = seiSubseqLayerInfo.bit_rate[i];
    pos += 2;
    *((unsigned short*)&(seiSubseqLayerInfo.data[pos])) = seiSubseqLayerInfo.frame_rate[i];
    pos += 2;
    seiSubseqLayerInfo.payloadSize += 4;
  }
}

/*
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  \functions on subseq characteristic sei messages
 *  \brief
 *      JVT-D098
 *  \author
 *      Dong Tian                 <tian@cs.tut.fi>
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

Boolean seiHasSubseqChar = FALSE;
subseq_char_information_struct seiSubseqChar;

void InitSubseqChar()
{
  seiSubseqChar.data = malloc( sizeof(Bitstream) );
  if( seiSubseqChar.data == NULL ) no_mem_exit("InitSubseqChar: seiSubseqChar.data");
  seiSubseqChar.data->streamBuffer = malloc(MAXRTPPAYLOADLEN);
  if( seiSubseqChar.data->streamBuffer == NULL ) no_mem_exit("InitSubseqChar: seiSubseqChar.data->streamBuffer");
  ClearSubseqCharPayload();

  seiSubseqChar.subseq_layer_num = img->layer;
  seiSubseqChar.subseq_id = seiSubseqInfo[img->layer].subseq_id;
  seiSubseqChar.duration_flag = 0;
  seiSubseqChar.average_rate_flag = 0;
  seiSubseqChar.num_referenced_subseqs = 0;
}

void ClearSubseqCharPayload()
{
  memset( seiSubseqChar.data->streamBuffer, 0, MAXRTPPAYLOADLEN);
  seiSubseqChar.data->bits_to_go  = 8;
  seiSubseqChar.data->byte_pos    = 0;
  seiSubseqChar.data->byte_buf    = 0;
  seiSubseqChar.payloadSize       = 0;

  seiHasSubseqChar = FALSE;
}

void UpdateSubseqChar()
{
  seiSubseqChar.subseq_layer_num = img->layer;
  seiSubseqChar.subseq_id = seiSubseqInfo[img->layer].subseq_id;
  seiSubseqChar.duration_flag = 0;
  seiSubseqChar.average_rate_flag = 0;
  seiSubseqChar.average_bit_rate = 100;
  seiSubseqChar.average_frame_rate = 30;
  seiSubseqChar.num_referenced_subseqs = 0;
  seiSubseqChar.ref_subseq_layer_num[0] = 1;
  seiSubseqChar.ref_subseq_id[0] = 2;
  seiSubseqChar.ref_subseq_layer_num[1] = 3;
  seiSubseqChar.ref_subseq_id[1] = 4;

  seiHasSubseqChar = TRUE;
}

void FinalizeSubseqChar()
{
  int i;
  SyntaxElement sym;
  Bitstream *dest = seiSubseqChar.data;

  sym.type = SE_HEADER;
  sym.mapping = ue_linfo;

  sym.value1 = seiSubseqChar.subseq_layer_num;
  writeSyntaxElement2Buf_UVLC(&sym, dest);
  sym.value1 = seiSubseqChar.subseq_id;
  writeSyntaxElement2Buf_UVLC(&sym, dest);
  sym.bitpattern = seiSubseqChar.duration_flag;
  sym.len = 1;
  writeSyntaxElement2Buf_Fixed(&sym, dest);
  if ( seiSubseqChar.duration_flag )
  {
    sym.bitpattern = seiSubseqChar.subseq_duration;
    sym.len = 32;
    writeSyntaxElement2Buf_Fixed(&sym, dest);
  }
  sym.bitpattern = seiSubseqChar.average_rate_flag;
  sym.len = 1;
  writeSyntaxElement2Buf_Fixed(&sym, dest);
  if ( seiSubseqChar.average_rate_flag )
  {
    sym.bitpattern = seiSubseqChar.average_bit_rate;
    sym.len = 16;
    writeSyntaxElement2Buf_Fixed(&sym, dest);
    sym.bitpattern = seiSubseqChar.average_frame_rate;
    sym.len = 16;
    writeSyntaxElement2Buf_Fixed(&sym, dest);
  }
  sym.value1 = seiSubseqChar.num_referenced_subseqs;
  writeSyntaxElement2Buf_UVLC(&sym, dest);
  for (i=0; i<seiSubseqChar.num_referenced_subseqs; i++)
  {
    sym.value1 = seiSubseqChar.ref_subseq_layer_num[i];
    writeSyntaxElement2Buf_UVLC(&sym, dest);
    sym.value1 = seiSubseqChar.ref_subseq_id[i];
    writeSyntaxElement2Buf_UVLC(&sym, dest);
  }

  // make sure the payload is byte aligned, stuff bits are 10..0
  if ( dest->bits_to_go != 8 )
  {
    (dest->byte_buf) <<= 1;
    dest->byte_buf |= 1;
    dest->bits_to_go--;
    if ( dest->bits_to_go != 0 ) (dest->byte_buf) <<= (dest->bits_to_go);
    dest->bits_to_go = 8;
    dest->streamBuffer[dest->byte_pos++]=dest->byte_buf;
    dest->byte_buf = 0;
  }
  seiSubseqChar.payloadSize = dest->byte_pos;
}

void CloseSubseqChar()
{
  if (seiSubseqChar.data)
  {
    free(seiSubseqChar.data->streamBuffer);
    free(seiSubseqChar.data);
  }
  seiSubseqChar.data = NULL;
}


// JVT-D099
/*
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  \functions on scene information SEI message
 *  \brief
 *      JVT-D099
 *  \author
 *      Ye-Kui Wang                 <wyk@ieee.org>
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

scene_information_struct seiSceneInformation;

void InitSceneInformation()
{
  seiHasSceneInformation = TRUE;

  seiSceneInformation.scene_id = 0;
  seiSceneInformation.scene_transition_type = 0;
  seiSceneInformation.second_scene_id = -1;

  seiSceneInformation.data = malloc( sizeof(Bitstream) );
  if( seiSceneInformation.data == NULL ) no_mem_exit("InitSceneInformation: seiSceneInformation.data");
  seiSceneInformation.data->streamBuffer = malloc( MAXRTPPAYLOADLEN );
  if( seiSceneInformation.data->streamBuffer == NULL ) no_mem_exit("InitSceneInformation: seiSceneInformation.data->streamBuffer");
  seiSceneInformation.data->bits_to_go  = 8;
  seiSceneInformation.data->byte_pos    = 0;
  seiSceneInformation.data->byte_buf    = 0;
  memset( seiSceneInformation.data->streamBuffer, 0, MAXRTPPAYLOADLEN );
}

void CloseSceneInformation()
{
  if (seiSceneInformation.data)
  {
    free(seiSceneInformation.data->streamBuffer);
    free(seiSceneInformation.data);
  }
  seiSceneInformation.data = NULL;
}

void FinalizeSceneInformation()
{
  SyntaxElement sym;
  Bitstream *dest = seiSceneInformation.data;

  sym.type = SE_HEADER;
  sym.mapping = ue_linfo;

  sym.bitpattern = seiSceneInformation.scene_id;
  sym.len = 8;
  writeSyntaxElement2Buf_Fixed(&sym, dest);

  sym.value1 = seiSceneInformation.scene_transition_type;
  writeSyntaxElement2Buf_UVLC(&sym, dest);

  if(seiSceneInformation.scene_transition_type > 3)
  {
    sym.bitpattern = seiSceneInformation.second_scene_id;
    sym.len = 8;
    writeSyntaxElement2Buf_Fixed(&sym, dest);
  }

  // make sure the payload is byte aligned, stuff bits are 10..0
  if ( dest->bits_to_go != 8 )
  {
    (dest->byte_buf) <<= 1;
    dest->byte_buf |= 1;
    dest->bits_to_go--;
    if ( dest->bits_to_go != 0 ) (dest->byte_buf) <<= (dest->bits_to_go);
    dest->bits_to_go = 8;
    dest->streamBuffer[dest->byte_pos++]=dest->byte_buf;
    dest->byte_buf = 0;
  }
  seiSceneInformation.payloadSize = dest->byte_pos;
}

// HasSceneInformation: To include a scene information SEI into the next slice/DP,
//      set HasSceneInformation to be TRUE when calling this function. Otherwise,
//      set HasSceneInformation to be FALSE.
void UpdateSceneInformation(Boolean HasSceneInformation, int sceneID, int sceneTransType, int secondSceneID)
{
  seiHasSceneInformation = HasSceneInformation;

  assert (sceneID < 256);
  seiSceneInformation.scene_id = sceneID;

  assert (sceneTransType <= 6 );
  seiSceneInformation.scene_transition_type = sceneTransType;

  if(sceneTransType > 3)
  {
    assert (secondSceneID < 256);
    seiSceneInformation.second_scene_id = secondSceneID;
  }
}
// End JVT-D099


/*
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  \functions on Pan Scan messages
 *  \brief
 *      Based on FCD
 *  \author
 *      Shankar Regunathan                 <tian@cs.tut.fi>
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

Boolean seiHasPanScanRectInfo = FALSE;
panscanrect_information_struct seiPanScanRectInfo;

void InitPanScanRectInfo()
{

  seiPanScanRectInfo.data = malloc( sizeof(Bitstream) );
  if( seiPanScanRectInfo.data == NULL ) no_mem_exit("InitPanScanRectInfo: seiPanScanRectInfo.data");
  seiPanScanRectInfo.data->streamBuffer = malloc(MAXRTPPAYLOADLEN);
  if( seiPanScanRectInfo.data->streamBuffer == NULL ) no_mem_exit("InitPanScanRectInfo: seiPanScanRectInfo.data->streamBuffer");
  ClearPanScanRectInfoPayload();

  seiPanScanRectInfo.pan_scan_rect_left_offset = 0;
  seiPanScanRectInfo.pan_scan_rect_right_offset = 0;
  seiPanScanRectInfo.pan_scan_rect_top_offset = 0;
  seiPanScanRectInfo.pan_scan_rect_bottom_offset = 0;

}


void ClearPanScanRectInfoPayload()
{
  memset( seiPanScanRectInfo.data->streamBuffer, 0, MAXRTPPAYLOADLEN);
  seiPanScanRectInfo.data->bits_to_go  = 8;
  seiPanScanRectInfo.data->byte_pos    = 0;
  seiPanScanRectInfo.data->byte_buf    = 0;
  seiPanScanRectInfo.payloadSize       = 0;

  seiHasPanScanRectInfo = FALSE;
}

void UpdatePanScanRectInfo()
{
  seiPanScanRectInfo.pan_scan_rect_id = 3;
  seiPanScanRectInfo.pan_scan_rect_left_offset = 10;
  seiPanScanRectInfo.pan_scan_rect_right_offset = 40;
  seiPanScanRectInfo.pan_scan_rect_top_offset = 20;
  seiPanScanRectInfo.pan_scan_rect_bottom_offset =32;
  seiHasPanScanRectInfo = TRUE;
}

void FinalizePanScanRectInfo()
{
  SyntaxElement sym;
  Bitstream *dest = seiPanScanRectInfo.data;


  sym.type = SE_HEADER;
  sym.mapping = ue_linfo;

  sym.value1 = seiPanScanRectInfo.pan_scan_rect_id;
  writeSyntaxElement2Buf_UVLC(&sym, dest);
  sym.value1 = seiPanScanRectInfo.pan_scan_rect_left_offset;
  writeSyntaxElement2Buf_UVLC(&sym, dest);
  sym.value1 = seiPanScanRectInfo.pan_scan_rect_right_offset;
  writeSyntaxElement2Buf_UVLC(&sym, dest);
  sym.value1 = seiPanScanRectInfo.pan_scan_rect_top_offset;
  writeSyntaxElement2Buf_UVLC(&sym, dest);
  sym.value1 = seiPanScanRectInfo.pan_scan_rect_bottom_offset;
  writeSyntaxElement2Buf_UVLC(&sym, dest);

// #define PRINT_PAN_SCAN_RECT
#ifdef PRINT_PAN_SCAN_RECT
  printf("Pan Scan Id %d Left %d Right %d Top %d Bottom %d \n", seiPanScanRectInfo.pan_scan_rect_id, seiPanScanRectInfo.pan_scan_rect_left_offset, seiPanScanRectInfo.pan_scan_rect_right_offset, seiPanScanRectInfo.pan_scan_rect_top_offset, seiPanScanRectInfo.pan_scan_rect_bottom_offset);
#endif
#ifdef PRINT_PAN_SCAN_RECT
#undef PRINT_PAN_SCAN_RECT
#endif
  // make sure the payload is byte aligned, stuff bits are 10..0
  if ( dest->bits_to_go != 8 )
  {
    (dest->byte_buf) <<= 1;
    dest->byte_buf |= 1;
    dest->bits_to_go--;
    if ( dest->bits_to_go != 0 ) (dest->byte_buf) <<= (dest->bits_to_go);
    dest->bits_to_go = 8;
    dest->streamBuffer[dest->byte_pos++]=dest->byte_buf;
    dest->byte_buf = 0;
  }
  seiPanScanRectInfo.payloadSize = dest->byte_pos;
}



void ClosePanScanRectInfo()
{
  if (seiPanScanRectInfo.data)
  {
    free(seiPanScanRectInfo.data->streamBuffer);
    free(seiPanScanRectInfo.data);
  }
  seiPanScanRectInfo.data = NULL;
}

/*
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  \functions on arbitrary (unregistered) data
 *  \brief
 *      Based on FCD
 *  \author
 *      Shankar Regunathan                 <tian@cs.tut.fi>
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */
Boolean seiHasUser_data_unregistered_info;
user_data_unregistered_information_struct seiUser_data_unregistered;
void InitUser_data_unregistered()
{

  seiUser_data_unregistered.data = malloc( sizeof(Bitstream) );
  if( seiUser_data_unregistered.data == NULL ) no_mem_exit("InitUser_data_unregistered: seiUser_data_unregistered.data");
  seiUser_data_unregistered.data->streamBuffer = malloc(MAXRTPPAYLOADLEN);
  if( seiUser_data_unregistered.data->streamBuffer == NULL ) no_mem_exit("InitUser_data_unregistered: seiUser_data_unregistered.data->streamBuffer");
  seiUser_data_unregistered.byte = malloc(MAXRTPPAYLOADLEN);
  if( seiUser_data_unregistered.byte == NULL ) no_mem_exit("InitUser_data_unregistered: seiUser_data_unregistered.byte");
  ClearUser_data_unregistered();

}


void ClearUser_data_unregistered()
{
  memset( seiUser_data_unregistered.data->streamBuffer, 0, MAXRTPPAYLOADLEN);
  seiUser_data_unregistered.data->bits_to_go  = 8;
  seiUser_data_unregistered.data->byte_pos    = 0;
  seiUser_data_unregistered.data->byte_buf    = 0;
  seiUser_data_unregistered.payloadSize       = 0;

  memset( seiUser_data_unregistered.byte, 0, MAXRTPPAYLOADLEN);
  seiUser_data_unregistered.total_byte = 0;

  seiHasUser_data_unregistered_info = FALSE;
}

void UpdateUser_data_unregistered()
{
  int i, temp_data;
  int total_byte;


  total_byte = 7;
  for(i = 0; i < total_byte; i++)
  {
    temp_data = i * 4;
    seiUser_data_unregistered.byte[i] = (char) iClip3(0, 255, temp_data);
  }
  seiUser_data_unregistered.total_byte = total_byte;
}

void FinalizeUser_data_unregistered()
{
  int i;
  SyntaxElement sym;
  Bitstream *dest = seiUser_data_unregistered.data;

  sym.type = SE_HEADER;
  sym.mapping = ue_linfo;

// #define PRINT_USER_DATA_UNREGISTERED_INFO
  for( i = 0; i < seiUser_data_unregistered.total_byte; i++)
  {
    sym.bitpattern = seiUser_data_unregistered.byte[i];
    sym.len = 8; // b (8)
    writeSyntaxElement2Buf_Fixed(&sym, dest);
#ifdef PRINT_USER_DATA_UNREGISTERED_INFO
    printf("Unreg data payload_byte = %d\n", seiUser_data_unregistered.byte[i]);
#endif
  }
#ifdef PRINT_USER_DATA_UNREGISTERED_INFO
#undef PRINT_USER_DATA_UNREGISTERED_INFO
#endif
  // make sure the payload is byte aligned, stuff bits are 10..0
  if ( dest->bits_to_go != 8 )
  {
    (dest->byte_buf) <<= 1;
    dest->byte_buf |= 1;
    dest->bits_to_go--;
    if ( dest->bits_to_go != 0 ) (dest->byte_buf) <<= (dest->bits_to_go);
    dest->bits_to_go = 8;
    dest->streamBuffer[dest->byte_pos++]=dest->byte_buf;
    dest->byte_buf = 0;
  }
  seiUser_data_unregistered.payloadSize = dest->byte_pos;
}

void CloseUser_data_unregistered()
{
  if (seiUser_data_unregistered.data)
  {
    free(seiUser_data_unregistered.data->streamBuffer);
    free(seiUser_data_unregistered.data);
  }
  seiUser_data_unregistered.data = NULL;
  if(seiUser_data_unregistered.byte)
  {
    free(seiUser_data_unregistered.byte);
  }
}


/*
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  \functions on registered ITU_T_T35 user data
 *  \brief
 *      Based on FCD
 *  \author
 *      Shankar Regunathan                 <tian@cs.tut.fi>
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */
Boolean seiHasUser_data_registered_itu_t_t35_info;
user_data_registered_itu_t_t35_information_struct seiUser_data_registered_itu_t_t35;
void InitUser_data_registered_itu_t_t35()
{

  seiUser_data_registered_itu_t_t35.data = malloc( sizeof(Bitstream) );
  if( seiUser_data_registered_itu_t_t35.data == NULL ) no_mem_exit("InitUser_data_unregistered: seiUser_data_registered_itu_t_t35.data");
  seiUser_data_registered_itu_t_t35.data->streamBuffer = malloc(MAXRTPPAYLOADLEN);
  if( seiUser_data_registered_itu_t_t35.data->streamBuffer == NULL ) no_mem_exit("InitUser_data_unregistered: seiUser_data_registered_itu_t_t35.data->streamBuffer");
  seiUser_data_registered_itu_t_t35.byte = malloc(MAXRTPPAYLOADLEN);
  if( seiUser_data_registered_itu_t_t35.data == NULL ) no_mem_exit("InitUser_data_unregistered: seiUser_data_registered_itu_t_t35.byte");
  ClearUser_data_registered_itu_t_t35();

}


void ClearUser_data_registered_itu_t_t35()
{
  memset( seiUser_data_registered_itu_t_t35.data->streamBuffer, 0, MAXRTPPAYLOADLEN);
  seiUser_data_registered_itu_t_t35.data->bits_to_go  = 8;
  seiUser_data_registered_itu_t_t35.data->byte_pos    = 0;
  seiUser_data_registered_itu_t_t35.data->byte_buf    = 0;
  seiUser_data_registered_itu_t_t35.payloadSize       = 0;

  memset( seiUser_data_registered_itu_t_t35.byte, 0, MAXRTPPAYLOADLEN);
  seiUser_data_registered_itu_t_t35.total_byte = 0;
  seiUser_data_registered_itu_t_t35.itu_t_t35_country_code = 0;
  seiUser_data_registered_itu_t_t35.itu_t_t35_country_code_extension_byte = 0;

  seiHasUser_data_registered_itu_t_t35_info = FALSE;
}

void UpdateUser_data_registered_itu_t_t35()
{
  int i, temp_data;
  int total_byte;
  int country_code;

  country_code = 82; // Country_code for India

  if(country_code < 0xFF)
  {
    seiUser_data_registered_itu_t_t35.itu_t_t35_country_code = country_code;
  }
  else
  {
    seiUser_data_registered_itu_t_t35.itu_t_t35_country_code = 0xFF;
    seiUser_data_registered_itu_t_t35.itu_t_t35_country_code_extension_byte = country_code - 0xFF;
  }

  total_byte = 7;
  for(i = 0; i < total_byte; i++)
  {
    temp_data = i * 3;
    seiUser_data_registered_itu_t_t35.byte[i] = (char) iClip3(0, 255, temp_data);
  }
  seiUser_data_registered_itu_t_t35.total_byte = total_byte;
}

void FinalizeUser_data_registered_itu_t_t35()
{
  int i;
  SyntaxElement sym;
  Bitstream *dest = seiUser_data_registered_itu_t_t35.data;

  sym.type = SE_HEADER;
  sym.mapping = ue_linfo;

  sym.bitpattern = seiUser_data_registered_itu_t_t35.itu_t_t35_country_code;
  sym.len = 8;
  writeSyntaxElement2Buf_Fixed(&sym, dest);

// #define PRINT_USER_DATA_REGISTERED_ITU_T_T35_INFO
#ifdef PRINT_USER_DATA_REGISTERED_ITU_T_T35_INFO
  printf(" ITU_T_T35_COUNTRTY_CODE %d \n", seiUser_data_registered_itu_t_t35.itu_t_t35_country_code);
#endif

  if(seiUser_data_registered_itu_t_t35.itu_t_t35_country_code == 0xFF)
  {
    sym.bitpattern = seiUser_data_registered_itu_t_t35.itu_t_t35_country_code_extension_byte;
    sym.len = 8;
    writeSyntaxElement2Buf_Fixed(&sym, dest);
#ifdef PRINT_USER_DATA_REGISTERED_ITU_T_T35_INFO
    printf(" ITU_T_T35_COUNTRTY_CODE_EXTENSION_BYTE %d \n", seiUser_data_registered_itu_t_t35.itu_t_t35_country_code_extension_byte);
#endif
  }

  for( i = 0; i < seiUser_data_registered_itu_t_t35.total_byte; i++)
  {
    sym.bitpattern = seiUser_data_registered_itu_t_t35.byte[i];
    sym.len = 8; // b (8)
    writeSyntaxElement2Buf_Fixed(&sym, dest);
#ifdef PRINT_USER_DATA_REGISTERED_ITU_T_T35_INFO
    printf("itu_t_t35 payload_byte = %d\n", seiUser_data_registered_itu_t_t35.byte[i]);
#endif
  }
#ifdef PRINT_USER_DATA_REGISTERED_ITU_T_T35_INFO
#undef PRINT_USER_DATA_REGISTERED_ITU_T_T35_INFO
#endif
  // make sure the payload is byte aligned, stuff bits are 10..0
  if ( dest->bits_to_go != 8 )
  {
    (dest->byte_buf) <<= 1;
    dest->byte_buf |= 1;
    dest->bits_to_go--;
    if ( dest->bits_to_go != 0 ) (dest->byte_buf) <<= (dest->bits_to_go);
    dest->bits_to_go = 8;
    dest->streamBuffer[dest->byte_pos++]=dest->byte_buf;
    dest->byte_buf = 0;
  }
  seiUser_data_registered_itu_t_t35.payloadSize = dest->byte_pos;
}

void CloseUser_data_registered_itu_t_t35()
{
  if (seiUser_data_registered_itu_t_t35.data)
  {
    free(seiUser_data_registered_itu_t_t35.data->streamBuffer);
    free(seiUser_data_registered_itu_t_t35.data);
  }
  seiUser_data_registered_itu_t_t35.data = NULL;
  if(seiUser_data_registered_itu_t_t35.byte)
  {
    free(seiUser_data_registered_itu_t_t35.byte);
  }
}

/*
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  \functions on random access message
 *  \brief
 *      Based on FCD
 *  \author
 *      Shankar Regunathan                 <tian@cs.tut.fi>
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */
recovery_point_information_struct seiRecoveryPoint;
void InitRandomAccess()
{

  seiRecoveryPoint.data = malloc( sizeof(Bitstream) );
  if( seiRecoveryPoint.data == NULL ) no_mem_exit("InitRandomAccess: seiRandomAccess.data");
  seiRecoveryPoint.data->streamBuffer = malloc(MAXRTPPAYLOADLEN);
  if( seiRecoveryPoint.data->streamBuffer == NULL ) no_mem_exit("InitRandomAccess: seiRandomAccess.data->streamBuffer");
  ClearRandomAccess();

}


void ClearRandomAccess()
{
  memset( seiRecoveryPoint.data->streamBuffer, 0, MAXRTPPAYLOADLEN);
  seiRecoveryPoint.data->bits_to_go  = 8;
  seiRecoveryPoint.data->byte_pos    = 0;
  seiRecoveryPoint.data->byte_buf    = 0;
  seiRecoveryPoint.payloadSize       = 0;

  seiRecoveryPoint.recovery_frame_cnt = 0;
  seiRecoveryPoint.broken_link_flag = 0;
  seiRecoveryPoint.exact_match_flag = 0;

  seiHasRecoveryPoint_info = FALSE;
}

void UpdateRandomAccess()
{

  if(img->type == I_SLICE)
  {
    seiRecoveryPoint.recovery_frame_cnt = 0;
    seiRecoveryPoint.exact_match_flag = 1;
    seiRecoveryPoint.broken_link_flag = 0;
    seiHasRecoveryPoint_info = TRUE;
  }
  else
  {
    seiHasRecoveryPoint_info = FALSE;
  }
}

void FinalizeRandomAccess()
{
  Bitstream *bitstream = seiRecoveryPoint.data;

  ue_v(   "SEI: recovery_frame_cnt",       seiRecoveryPoint.recovery_frame_cnt,       bitstream);
  u_1 (   "SEI: exact_match_flag",         seiRecoveryPoint.exact_match_flag,         bitstream);
  u_1 (   "SEI: broken_link_flag",         seiRecoveryPoint.broken_link_flag,         bitstream);
  u_v (2, "SEI: changing_slice_group_idc", seiRecoveryPoint.changing_slice_group_idc, bitstream);


// #define PRINT_RECOVERY_POINT
#ifdef PRINT_RECOVERY_POINT
  printf(" recovery_frame_cnt %d \n",       seiRecoveryPoint.recovery_frame_cnt);
  printf(" exact_match_flag %d \n",         seiRecoveryPoint.exact_match_flag);
  printf(" broken_link_flag %d \n",         seiRecoveryPoint.broken_link_flag);
  printf(" changing_slice_group_idc %d \n", seiRecoveryPoint.changing_slice_group_idc);
  printf(" %d %d \n", bitstream->byte_pos, bitstream->bits_to_go);

#undef PRINT_RECOVERY_POINT
#endif
  // make sure the payload is byte aligned, stuff bits are 10..0
  if ( bitstream->bits_to_go != 8 )
  {
    (bitstream->byte_buf) <<= 1;
    bitstream->byte_buf |= 1;
    bitstream->bits_to_go--;
    if ( bitstream->bits_to_go != 0 )
      (bitstream->byte_buf) <<= (bitstream->bits_to_go);
    bitstream->bits_to_go = 8;
    bitstream->streamBuffer[bitstream->byte_pos++]=bitstream->byte_buf;
    bitstream->byte_buf = 0;
  }
  seiRecoveryPoint.payloadSize = bitstream->byte_pos;
}

void CloseRandomAccess()
{
  if (seiRecoveryPoint.data)
  {
    free(seiRecoveryPoint.data->streamBuffer);
    free(seiRecoveryPoint.data);
  }
  seiRecoveryPoint.data = NULL;
}

/*
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  \functions on HDR tone-mapping messages
 *  \brief
 *      Based on JVT-T060
 *  \author
 *      Jane Zhao, sharp labs of america
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */
tone_mapping_struct seiToneMapping;

int ParseToneMappingConfigFile(tone_mapping_struct* pSeiToneMapping)
{
  int i;
  FILE* fp;
  char buf[1024];
  unsigned int tmp;

  printf ("Parsing Tone mapping cfg file %s ..........\n\n", input->ToneMappingFile);
  if ((fp = fopen(input->ToneMappingFile, "r")) == NULL) {
    fprintf(stderr, "Tone mapping config file %s is not found, disable tone mapping SEI\n", input->ToneMappingFile);
    seiHasTone_mapping=FALSE;

    return 1;
  }

  //read the tone mapping config file	
  while (fscanf(fp, "%s", buf)!=EOF) 
  {
    if (strcmp(buf, "tone_map_id")==0) 
    {
      fscanf(fp, " = %ud\n", &(pSeiToneMapping->tone_map_id));							
    }
    else if (strcmp(buf, "tone_map_cancel_flag")==0) 
    {
      fscanf(fp, " = %ud\n", &tmp);
      pSeiToneMapping->tone_map_cancel_flag = tmp ? 1 : 0;
    }
    else if (strcmp(buf, "tone_map_repetition_period")==0) 
    {
      fscanf(fp, " = %ud\n", &(pSeiToneMapping->tone_map_repetition_period));
    }				
    else if (strcmp(buf, "coded_data_bit_depth")==0) 
    {
      fscanf(fp, " = %ud\n", &tmp);
      pSeiToneMapping->coded_data_bit_depth = (unsigned char) tmp;
    }
    else if (strcmp(buf, "sei_bit_depth")==0) 
    {
      fscanf(fp, " = %ud\n", &tmp);
      pSeiToneMapping->sei_bit_depth =  (unsigned char) tmp;
    }
    else if (strcmp(buf, "model_id")==0) 
    {
      fscanf(fp, " = %ud\n", &(pSeiToneMapping->model_id));
    }
    //else if (model_id ==0) 
    else if (strcmp(buf, "min_value")==0) 
    {
      fscanf(fp, " = %d\n", &(pSeiToneMapping->min_value));
    }
    else if (strcmp(buf, "max_value")==0) 
    {
      fscanf(fp, " = %d\n", &(pSeiToneMapping->max_value));
    }		
    //(model_id == 1)
    else if (strcmp(buf, "sigmoid_midpoint")==0) 
    {
      fscanf(fp, " = %d\n", &(pSeiToneMapping->sigmoid_midpoint));
    }
    else if (strcmp(buf, "sigmoid_width")==0) 
    {
      fscanf(fp, " = %d\n", &(pSeiToneMapping->sigmoid_width));
    }		 
    // (model_id == 2) 
    else if (strcmp(buf, "start_of_coded_interval")==0) 
    {
      int max_output_num = 1<<(pSeiToneMapping->sei_bit_depth);
      fscanf(fp, " = ");
      for (i=0; i < max_output_num; i++)
        fscanf(fp, "%d\n", &(pSeiToneMapping->start_of_coded_interval[i]));
    }		 
    //(model_id == 3)
    else if (strcmp(buf, "num_pivots")==0) 
    {
      fscanf(fp, " = %d\n", &(pSeiToneMapping->num_pivots));
    }

    else if (strcmp(buf, "coded_pivot_value")==0) 
    {
      fscanf(fp, " = ");
      for (i=0; i < pSeiToneMapping->num_pivots; i++)
        fscanf(fp, "%d\n", &(pSeiToneMapping->coded_pivot_value[i]));
    }
    else if (strcmp(buf, "sei_pivot_value")==0) 
    {
      fscanf(fp, " = ");
      for (i=0; i < pSeiToneMapping->num_pivots; i++)
        fscanf(fp, "%d\n", &(pSeiToneMapping->sei_pivot_value[i]));
    }		
    else
    {
      // read till the line end 
      fgets(buf, sizeof(buf), fp);
    }
  }

  fclose(fp);

  return 0;
}

void InitToneMapping() 
{
  if (input->ToneMappingSEIPresentFlag == 0)
  {
    seiHasTone_mapping = FALSE;
    return;
  }
  else
    seiHasTone_mapping = TRUE;

  seiToneMapping.data = malloc( sizeof(Bitstream) );
  if( seiToneMapping.data == NULL ) no_mem_exit("InitToneMapping: seiToneMapping.data");
  seiToneMapping.data->streamBuffer = malloc(MAXRTPPAYLOADLEN);
  if( seiToneMapping.data->streamBuffer == NULL ) no_mem_exit("InitToneMapping: seiToneMapping.data->streamBuffer");
  memset( seiToneMapping.data->streamBuffer, 0, MAXRTPPAYLOADLEN);
  seiToneMapping.data->bits_to_go  = 8;
  seiToneMapping.data->byte_pos    = 0;
  seiToneMapping.data->byte_buf    = 0;
  seiToneMapping.payloadSize       = 0;

  // read tone mapping config from file
  ParseToneMappingConfigFile(&seiToneMapping);
}

void FinalizeToneMapping()
{
  Bitstream *bitstream = seiToneMapping.data;  
  int i;

  ue_v("SEI: tone_map_id"						, seiToneMapping.tone_map_id,				bitstream);
  u_1("SEI: tone_map_cancel_flag"				, seiToneMapping.tone_map_cancel_flag,		bitstream);

#ifdef PRINT_TONE_MAPPING
  printf("frame %d: Tone-mapping SEI message\n", img->frame_num);
  printf("tone_map_id = %d\n", seiToneMapping.tone_map_id);
  printf("tone_map_cancel_flag = %d\n", seiToneMapping.tone_map_cancel_flag);
#endif
  if (!seiToneMapping.tone_map_cancel_flag) 
  {
    ue_v(  "SEI: tone_map_repetition_period", seiToneMapping.tone_map_repetition_period, bitstream);
    u_v (8,"SEI: coded_data_bit_depth"      , seiToneMapping.coded_data_bit_depth,       bitstream);
    u_v (8,"SEI: sei_bit_depth"             , seiToneMapping.sei_bit_depth,              bitstream);
    ue_v(  "SEI: model_id"                  , seiToneMapping.model_id,                   bitstream);

#ifdef PRINT_TONE_MAPPING
    printf("tone_map_repetition_period = %d\n", seiToneMapping.tone_map_repetition_period);
    printf("coded_data_bit_depth = %d\n", seiToneMapping.coded_data_bit_depth);
    printf("sei_bit_depth = %d\n", seiToneMapping.sei_bit_depth);
    printf("model_id = %d\n", seiToneMapping.model_id);
#endif
    if (seiToneMapping.model_id == 0) 
    { // linear mapping
      u_v (32,"SEI: min_value", seiToneMapping.min_value,	bitstream);
      u_v (32,"SEI: min_value", seiToneMapping.max_value,	bitstream);
#ifdef PRINT_TONE_MAPPING
      printf("min_value = %d, max_value = %d\n", seiToneMapping.min_value, seiToneMapping.max_value);
#endif
    }
    else if (seiToneMapping.model_id == 1) 
    { // sigmoidal mapping
      u_v (32,"SEI: sigmoid_midpoint", seiToneMapping.sigmoid_midpoint,			bitstream);
      u_v (32,"SEI: sigmoid_width", seiToneMapping.sigmoid_width,					bitstream);
#ifdef PRINT_TONE_MAPPING
      printf("sigmoid_midpoint = %d, sigmoid_width = %d\n", seiToneMapping.sigmoid_midpoint, seiToneMapping.sigmoid_width);
#endif
    }
    else if (seiToneMapping.model_id == 2) 
    { // user defined table mapping
      int bit_depth_val = 1<<seiToneMapping.sei_bit_depth;
      for (i=0; i<bit_depth_val; i++) 
      {
        u_v((((seiToneMapping.coded_data_bit_depth+7)>>3)<<3), "SEI: start_of_coded_interval", seiToneMapping.start_of_coded_interval[i],	bitstream);
#ifdef PRINT_TONE_MAPPING
        //printf("start_of_coded_interval[%d] = %d\n", i, seiToneMapping.start_of_coded_interval[i]);
#endif			
      }
    }
    else if (seiToneMapping.model_id == 3) 
    {  // piece-wise linear mapping
      u_v (16,"SEI: num_pivots", seiToneMapping.num_pivots, bitstream);
#ifdef PRINT_TONE_MAPPING
      printf("num_pivots = %d\n", seiToneMapping.num_pivots);
#endif
      for (i=0; i < seiToneMapping.num_pivots; i++) 
      {
        u_v( (((seiToneMapping.coded_data_bit_depth+7)>>3)<<3), "SEI: coded_pivot_value",	seiToneMapping.coded_pivot_value[i],	bitstream);
        u_v( (((seiToneMapping.sei_bit_depth+7)>>3)<<3), "SEI: sei_pivot_value",			seiToneMapping.sei_pivot_value[i],		bitstream);
#ifdef PRINT_TONE_MAPPING
        printf("coded_pivot_value[%d] = %d, sei_pivot_value[%d] = %d\n", i, seiToneMapping.coded_pivot_value[i], i, seiToneMapping.sei_pivot_value[i]);
#endif
      }
    }
  } // end !tone_map_cancel_flag

  // make sure the payload is byte aligned, stuff bits are 10..0
  if ( bitstream->bits_to_go != 8 )
  {
    (bitstream->byte_buf) <<= 1;
    bitstream->byte_buf |= 1;
    bitstream->bits_to_go--;
    if ( bitstream->bits_to_go != 0 ) 
      (bitstream->byte_buf) <<= (bitstream->bits_to_go);
    bitstream->bits_to_go = 8;
    bitstream->streamBuffer[bitstream->byte_pos++]=bitstream->byte_buf;
    bitstream->byte_buf = 0;
  }
  seiToneMapping.payloadSize = bitstream->byte_pos;
}


void UpdateToneMapping() 
{
  // return;

  // you may manually generate some test case here
}

void ClearToneMapping() 
{
  memset( seiToneMapping.data->streamBuffer, 0, MAXRTPPAYLOADLEN);
  seiToneMapping.data->bits_to_go  = 8;
  seiToneMapping.data->byte_pos    = 0;
  seiToneMapping.data->byte_buf    = 0;
  seiToneMapping.payloadSize       = 0;

  seiHasTone_mapping=FALSE;
}

void CloseToneMapping() 
{

  if (seiToneMapping.data)
  {
    free(seiToneMapping.data->streamBuffer);
    free(seiToneMapping.data);
  }
  seiToneMapping.data = NULL;	
  seiHasTone_mapping = FALSE;
}

/*
 ************************************************************************
 *  \functions on post-filter message
 *  \brief
 *      Based on JVT-U035
 *  \author
 *      Steffen Wittmann <steffen.wittmann@eu.panasonic.com>
 ************************************************************************
 */
post_filter_information_struct seiPostFilterHints;

void InitPostFilterHints()
{
  seiPostFilterHints.data = malloc( sizeof(Bitstream) );
  if( seiPostFilterHints.data == NULL ) no_mem_exit("InitPostFilterHints: seiPostFilterHints.data");
  seiPostFilterHints.data->streamBuffer = malloc(MAXRTPPAYLOADLEN);
  if( seiPostFilterHints.data->streamBuffer == NULL ) no_mem_exit("InitPostFilterHints: seiPostFilterHints.data->streamBuffer");
  ClearPostFilterHints();
}

void ClearPostFilterHints()
{
  memset( seiPostFilterHints.data->streamBuffer, 0, MAXRTPPAYLOADLEN);
  seiPostFilterHints.data->bits_to_go  = 8;
  seiPostFilterHints.data->byte_pos    = 0;
  seiPostFilterHints.data->byte_buf    = 0;
  seiPostFilterHints.payloadSize       = 0;

  seiPostFilterHints.filter_hint_size_y        = 0;
  seiPostFilterHints.filter_hint_size_x        = 0;
  seiPostFilterHints.filter_hint_type          = 0;
  seiPostFilterHints.additional_extension_flag = 0;
}

void UpdatePostFilterHints()
{
  unsigned int color_component, cx, cy;
  seiPostFilterHints.filter_hint_type = 0; //define filter_hint_type here
  seiPostFilterHints.filter_hint_size_y = seiPostFilterHints.filter_hint_size_x = 5; //define filter_hint_size here
  get_mem3Dint(&seiPostFilterHints.filter_hint, 3, seiPostFilterHints.filter_hint_size_y, seiPostFilterHints.filter_hint_size_x);

  for (color_component = 0; color_component < 3; color_component ++)
    for (cy = 0; cy < seiPostFilterHints.filter_hint_size_y; cy ++)
      for (cx = 0; cx < seiPostFilterHints.filter_hint_size_x; cx ++)
		seiPostFilterHints.filter_hint[color_component][cy][cx] = 1; //define filter_hint here

  seiPostFilterHints.additional_extension_flag = 0;
}

void FinalizePostFilterHints()
{
  Bitstream *bitstream = seiPostFilterHints.data;
  unsigned int color_component, cx, cy;

  ue_v(  "SEI: post_filter_hint_size_y", seiPostFilterHints.filter_hint_size_y, bitstream);
  ue_v(  "SEI: post_filter_hint_size_x", seiPostFilterHints.filter_hint_size_x, bitstream);
  u_v (2,"SEI: post_filter_hint_type",   seiPostFilterHints.filter_hint_type,   bitstream);

  for (color_component = 0; color_component < 3; color_component ++)
    for (cy = 0; cy < seiPostFilterHints.filter_hint_size_y; cy ++)
      for (cx = 0; cx < seiPostFilterHints.filter_hint_size_x; cx ++)
		se_v("SEI: post_filter_hints", seiPostFilterHints.filter_hint[color_component][cy][cx], bitstream);

  u_1 ("SEI: post_filter_additional_extension_flag", seiPostFilterHints.additional_extension_flag, bitstream);

// #define PRINT_POST_FILTER_HINTS
#ifdef PRINT_POST_FILTER_HINTS
  printf(" post_filter_hint_size_y %d \n", seiPostFilterHints.filter_hint_size_y);
  printf(" post_filter_hint_size_x %d \n", seiPostFilterHints.filter_hint_size_x);
  printf(" post_filter_hint_type %d \n",   seiPostFilterHints.filter_hint_type);
  for (color_component = 0; color_component < 3; color_component ++)
    for (cy = 0; cy < seiPostFilterHints.filter_hint_size_y; cy ++)
      for (cx = 0; cx < seiPostFilterHints.filter_hint_size_x; cx ++)
		printf(" post_filter_hint[%d][%d][%d] %d \n", color_component, cy, cx, filter_hint[color_component][cy][cx]);

  printf(" additional_extension_flag %d \n", seiPostFilterHints.additional_extension_flag);

#undef PRINT_POST_FILTER_HINTS
#endif
  // make sure the payload is byte aligned, stuff bits are 10..0
  if ( bitstream->bits_to_go != 8 )
  {
    (bitstream->byte_buf) <<= 1;
    bitstream->byte_buf |= 1;
    bitstream->bits_to_go--;
    if ( bitstream->bits_to_go != 0 ) 
      (bitstream->byte_buf) <<= (bitstream->bits_to_go);
    bitstream->bits_to_go = 8;
    bitstream->streamBuffer[bitstream->byte_pos++]=bitstream->byte_buf;
    bitstream->byte_buf = 0;
  }
  seiPostFilterHints.payloadSize = bitstream->byte_pos;
}

void ClosePostFilterHints()
{
  if (seiPostFilterHints.data)
  {
    free(seiPostFilterHints.data->streamBuffer);
    free(seiPostFilterHints.data);  
    if (seiPostFilterHints.filter_hint)
      free_mem3Dint(seiPostFilterHints.filter_hint, 3);
  }
  seiPostFilterHints.data = NULL;
}

/*
**++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
*  \functions to write SEI message into NAL
*  \brief     
**++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
*/
int Write_SEI_NALU(int len)
{  
  NALU_t *nalu = NULL;
  int RBSPlen = 0;
  int NALUlen;
  byte *rbsp;

  if (HaveAggregationSEI())
  {
    nalu = AllocNALU(64000);
    rbsp = sei_message[AGGREGATION_SEI].data;
    RBSPlen = sei_message[AGGREGATION_SEI].payloadSize;
    NALUlen = RBSPtoNALU (rbsp, nalu, RBSPlen, NALU_TYPE_SEI, NALU_PRIORITY_LOW, 0, 1);
    nalu->startcodeprefix_len = 4;

    len += WriteNALU (nalu);
    FreeNALU (nalu);
  }  

  return len;
}

/*
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  \functions on buffering period SEI message
 *  \brief
 *      Based on final Recommendation
 *  \author
 *      Athanasios Leontaris                 <aleon@dolby.com>
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */
void InitBufferingPeriod()
{
  seiBufferingPeriod.data = malloc( sizeof(Bitstream) );
  if( seiBufferingPeriod.data == NULL ) 
    no_mem_exit("InitBufferingPeriod: seiBufferingPeriod.data");

  seiBufferingPeriod.data->streamBuffer = malloc(MAXRTPPAYLOADLEN);
  if( seiBufferingPeriod.data->streamBuffer == NULL ) 
    no_mem_exit("InitBufferingPeriod: seiBufferingPeriod.data->streamBuffer");

  ClearBufferingPeriod();
}

void ClearBufferingPeriod()
{
  unsigned int SchedSelIdx;
  memset( seiBufferingPeriod.data->streamBuffer, 0, MAXRTPPAYLOADLEN);

  seiBufferingPeriod.data->bits_to_go  = 8;
  seiBufferingPeriod.data->byte_pos    = 0;
  seiBufferingPeriod.data->byte_buf    = 0;
  seiBufferingPeriod.payloadSize       = 0;

  seiBufferingPeriod.seq_parameter_set_id = active_sps->seq_parameter_set_id;
  if ( active_sps->vui_seq_parameters.nal_hrd_parameters_present_flag )
  {
    for ( SchedSelIdx = 0; SchedSelIdx <= active_sps->vui_seq_parameters.nal_hrd_parameters.cpb_cnt_minus1; SchedSelIdx++ )
    {
      seiBufferingPeriod.nal_initial_cpb_removal_delay[SchedSelIdx] = 0;
      seiBufferingPeriod.nal_initial_cpb_removal_delay_offset[SchedSelIdx] = 0;
    }
  }
  if ( active_sps->vui_seq_parameters.vcl_hrd_parameters_present_flag )
  {
    for ( SchedSelIdx = 0; SchedSelIdx <= active_sps->vui_seq_parameters.vcl_hrd_parameters.cpb_cnt_minus1; SchedSelIdx++ )
    {
      seiBufferingPeriod.vcl_initial_cpb_removal_delay[SchedSelIdx] = 0;
      seiBufferingPeriod.vcl_initial_cpb_removal_delay_offset[SchedSelIdx] = 0;
    }
  }

  seiHasBuffering_period = FALSE;
}

void UpdateBufferingPeriod()
{
  seiHasBuffering_period = FALSE;
}

void FinalizeBufferingPeriod()
{
  unsigned int SchedSelIdx;
  Bitstream *bitstream = seiBufferingPeriod.data;

  ue_v(   "SEI: seq_parameter_set_id",     seiBufferingPeriod.seq_parameter_set_id,   bitstream);
  if ( active_sps->vui_seq_parameters.nal_hrd_parameters_present_flag )
  {
    for ( SchedSelIdx = 0; SchedSelIdx <= active_sps->vui_seq_parameters.nal_hrd_parameters.cpb_cnt_minus1; SchedSelIdx++ )
    {
      u_v( active_sps->vui_seq_parameters.nal_hrd_parameters.initial_cpb_removal_delay_length_minus1 + 1,
        "SEI: initial_cpb_removal_delay",     seiBufferingPeriod.nal_initial_cpb_removal_delay[SchedSelIdx],   bitstream);
      u_v( active_sps->vui_seq_parameters.nal_hrd_parameters.initial_cpb_removal_delay_length_minus1 + 1,
        "SEI: initial_cpb_removal_delay_offset",     seiBufferingPeriod.nal_initial_cpb_removal_delay_offset[SchedSelIdx],   bitstream);
    }
  }
  if ( active_sps->vui_seq_parameters.vcl_hrd_parameters_present_flag )
  {
    for ( SchedSelIdx = 0; SchedSelIdx <= active_sps->vui_seq_parameters.vcl_hrd_parameters.cpb_cnt_minus1; SchedSelIdx++ )
    {
      u_v( active_sps->vui_seq_parameters.vcl_hrd_parameters.initial_cpb_removal_delay_length_minus1 + 1,
        "SEI: initial_cpb_removal_delay",     seiBufferingPeriod.vcl_initial_cpb_removal_delay[SchedSelIdx],   bitstream);
      u_v( active_sps->vui_seq_parameters.vcl_hrd_parameters.initial_cpb_removal_delay_length_minus1 + 1,
        "SEI: initial_cpb_removal_delay_offset",     seiBufferingPeriod.vcl_initial_cpb_removal_delay_offset[SchedSelIdx],   bitstream);
    }
  }

  // make sure the payload is byte aligned, stuff bits are 10..0
  if ( bitstream->bits_to_go != 8 )
  {
    (bitstream->byte_buf) <<= 1;
    bitstream->byte_buf |= 1;
    bitstream->bits_to_go--;
    if ( bitstream->bits_to_go != 0 )
      (bitstream->byte_buf) <<= (bitstream->bits_to_go);
    bitstream->bits_to_go = 8;
    bitstream->streamBuffer[bitstream->byte_pos++]=bitstream->byte_buf;
    bitstream->byte_buf = 0;
  }
  seiBufferingPeriod.payloadSize = bitstream->byte_pos;
}

void CloseBufferingPeriod()
{
  if (seiBufferingPeriod.data)
  {
    free(seiBufferingPeriod.data->streamBuffer);
    free(seiBufferingPeriod.data);
  }
  seiBufferingPeriod.data = NULL;
}

/*
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  \functions on picture timing SEI message
 *  \brief
 *      Based on final Recommendation
 *  \author
 *      Athanasios Leontaris                 <aleon@dolby.com>
 **++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */
void InitPicTiming()
{
  seiPicTiming.data = malloc( sizeof(Bitstream) );
  if( seiPicTiming.data == NULL ) 
    no_mem_exit("InitPicTiming: seiPicTiming.data");

  seiPicTiming.data->streamBuffer = malloc(MAXRTPPAYLOADLEN);
  if( seiPicTiming.data->streamBuffer == NULL ) 
    no_mem_exit("InitPicTiming: seiPicTiming.data->streamBuffer");

  ClearPicTiming();
}

void ClearPicTiming()
{
  memset( seiPicTiming.data->streamBuffer, 0, MAXRTPPAYLOADLEN);

  seiPicTiming.data->bits_to_go  = 8;
  seiPicTiming.data->byte_pos    = 0;
  seiPicTiming.data->byte_buf    = 0;
  seiPicTiming.payloadSize       = 0;

  // initialization
  seiPicTiming.cpb_removal_delay = 0;
  seiPicTiming.dpb_output_delay = 0;
  seiPicTiming.pic_struct = 0;
  memset(seiPicTiming.clock_timestamp_flag, 0, MAX_PIC_STRUCT_VALUE * sizeof(Boolean) ); // 0 == FALSE
  seiPicTiming.ct_type = 0;
  seiPicTiming.nuit_field_based_flag = FALSE;
  seiPicTiming.counting_type = 0;
  seiPicTiming.full_timestamp_flag = FALSE;
  seiPicTiming.discontinuity_flag = FALSE;
  seiPicTiming.cnt_dropped_flag = FALSE;
  seiPicTiming.n_frames = 0;
  seiPicTiming.seconds_value = 0;
  seiPicTiming.minutes_value = 0;
  seiPicTiming.hours_value = 0;
  seiPicTiming.seconds_flag = FALSE;
  seiPicTiming.minutes_flag = FALSE;
  seiPicTiming.hours_flag = FALSE;
  seiPicTiming.time_offset = 0;  

  seiHasPicTiming_info = FALSE;
}

void UpdatePicTiming()
{
  seiHasPicTiming_info = FALSE;
}

void FinalizePicTiming()
{
  Bitstream *bitstream = seiPicTiming.data;
  // CpbDpbDelaysPresentFlag can also be set "by some means not specified in this Recommendation | International Standard"
  Boolean CpbDpbDelaysPresentFlag =  (Boolean) (active_sps->vui_parameters_present_flag
                              && (   (active_sps->vui_seq_parameters.nal_hrd_parameters_present_flag != 0)
                                   ||(active_sps->vui_seq_parameters.vcl_hrd_parameters_present_flag != 0)));
  hrd_parameters_t *hrd = NULL;

  assert( active_sps->vui_seq_parameters.vcl_hrd_parameters_present_flag || active_sps->vui_seq_parameters.nal_hrd_parameters_present_flag );
  if (active_sps->vui_seq_parameters.vcl_hrd_parameters_present_flag)
    hrd = &(active_sps->vui_seq_parameters.vcl_hrd_parameters);
  else if (active_sps->vui_seq_parameters.nal_hrd_parameters_present_flag)
    hrd = &(active_sps->vui_seq_parameters.nal_hrd_parameters);
  else // this should never happen
    error ("HRD structures not properly created.",-1);

  if ( CpbDpbDelaysPresentFlag )
  {
    u_v( hrd->cpb_removal_delay_length_minus1 + 1, "SEI: cpb_removal_delay", seiPicTiming.cpb_removal_delay, bitstream);
    u_v( hrd->dpb_output_delay_length_minus1  + 1, "SEI: dpb_output_delay",  seiPicTiming.dpb_output_delay,  bitstream);
  }
  if ( active_sps->vui_seq_parameters.pic_struct_present_flag )
  {
    int NumClockTS = 0, i;
    int bottom_field_flag = (img->structure == BOTTOM_FIELD) ? 1 : 0;

    u_v( 4, "SEI: pic_struct", seiPicTiming.pic_struct, bitstream);
    // interpret pic_struct
    switch( seiPicTiming.pic_struct )
    {
    case 0:
    default:
      // frame
      assert( img->fld_flag == 0 );
      NumClockTS = 1;
      break;
    case 1:
      // top field
      assert( img->fld_flag == 1 && bottom_field_flag == 0 );
      NumClockTS = 1;
      break;
    case 2:
      // bottom field
      assert( img->fld_flag == 1 && bottom_field_flag == 1 );
      NumClockTS = 1;
      break;
    case 3:
      // top field, bottom field, in that order
    case 4:
      // bottom field, top field, in that order
      assert( img->fld_flag == 0 );
      NumClockTS = 2;
      break;
    case 5:
      // top field, bottom field, top field repeated, in that order
    case 6:
      // bottom field, top field, bottom field repeated, in that order
      assert( img->fld_flag == 0 );
      NumClockTS = 3;
    case 7:
      // frame doubling
      assert( img->fld_flag == 0 && active_sps->vui_seq_parameters.fixed_frame_rate_flag == 1 );
      NumClockTS = 2;
      break;
    case 8:
      // frame tripling
      assert( img->fld_flag == 0 && active_sps->vui_seq_parameters.fixed_frame_rate_flag == 1 );
      NumClockTS = 3;
      break;
    }
    for ( i = 0; i < NumClockTS; i++ )
    {
      u_1( "SEI: clock_timestamp_flag", seiPicTiming.clock_timestamp_flag[i], bitstream);
      if ( seiPicTiming.clock_timestamp_flag[i] )
      {
        u_v( 2, "SEI: ct_type", seiPicTiming.ct_type, bitstream);
        u_1( "SEI: nuit_field_based_flag", seiPicTiming.nuit_field_based_flag, bitstream);
        u_v( 5, "SEI: counting_type", seiPicTiming.counting_type, bitstream);
        u_1( "SEI: full_timestamp_flag", seiPicTiming.full_timestamp_flag, bitstream);
        u_1( "SEI: discontinuity_flag", seiPicTiming.discontinuity_flag, bitstream);
        u_1( "SEI: cnt_dropped_flag", seiPicTiming.cnt_dropped_flag, bitstream);
        u_v( 8, "SEI: n_frames", seiPicTiming.n_frames, bitstream);

        if ( seiPicTiming.full_timestamp_flag )
        {      
          u_v( 6, "SEI: seconds_value", seiPicTiming.seconds_value, bitstream);
          u_v( 6, "SEI: minutes_value", seiPicTiming.minutes_value, bitstream);
          u_v( 5, "SEI: hours_value",   seiPicTiming.hours_value, bitstream);
        }
        else
        {            
          u_1( "SEI: seconds_flag", seiPicTiming.seconds_flag, bitstream);
          if (seiPicTiming.seconds_flag)
          {
            u_v( 6, "SEI: seconds_value", seiPicTiming.seconds_value, bitstream);
            u_1( "SEI: minutes_flag", seiPicTiming.minutes_flag, bitstream);
            if (seiPicTiming.minutes_flag)
            {
              u_v( 6, "SEI: minutes_value", seiPicTiming.minutes_value, bitstream);
              u_1( "SEI: hours_flag", seiPicTiming.hours_flag, bitstream);
              if (seiPicTiming.hours_flag)
              {
                u_v( 5, "SEI: hours_value",   seiPicTiming.hours_value, bitstream);
              }
            }
          }
        }
        if ( hrd->time_offset_length )
        {
          u_v( hrd->time_offset_length, "SEI: time_offset", seiPicTiming.time_offset, bitstream);
        }
      }
    }
  }

  // make sure the payload is byte aligned, stuff bits are 10..0
  if ( bitstream->bits_to_go != 8 )
  {
    (bitstream->byte_buf) <<= 1;
    bitstream->byte_buf |= 1;
    bitstream->bits_to_go--;
    if ( bitstream->bits_to_go != 0 )
      (bitstream->byte_buf) <<= (bitstream->bits_to_go);
    bitstream->bits_to_go = 8;
    bitstream->streamBuffer[bitstream->byte_pos++]=bitstream->byte_buf;
    bitstream->byte_buf = 0;
  }
  seiPicTiming.payloadSize = bitstream->byte_pos;
}

void ClosePicTiming()
{
  if (seiPicTiming.data)
  {
    free(seiPicTiming.data->streamBuffer);
    free(seiPicTiming.data);
  }
  seiPicTiming.data = NULL;
}

