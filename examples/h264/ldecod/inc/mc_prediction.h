
/*!
 *************************************************************************************
 * \file mc_prediction.h
 *
 * \brief
 *    definitions for motion compensated prediction
 *
 * \author
 *      Main contributors (see contributors.h for copyright, 
 *                         address and affiliation details)
 *      - Alexis Michael Tourapis  <alexismt@ieee.org>
 *
 *************************************************************************************
 */

#ifndef _MC_PREDICTION_H_
#define _MC_PREDICTION_H_

#include "global.h"
#include "mbuffer.h"

extern StorablePicture *dec_picture;

extern void get_block_luma(int ref_frame, StorablePicture **list, int x_pos, int y_pos, int ver_block_size, int hor_block_size, struct img_par *img, imgpel block[MB_BLOCK_SIZE][MB_BLOCK_SIZE]);
extern void get_block_chroma(int uv, int ref_frame, StorablePicture **list, int x_pos, int y_pos, int hor_block_size, int ver_block_size, struct img_par *img, imgpel block[MB_BLOCK_SIZE][MB_BLOCK_SIZE]);

extern void intra_cr_decoding(int yuv, struct img_par *img, int smb, Macroblock *currMB);
extern void prepare_direct_params(StorablePicture *dec_picture, struct img_par *img, int mb_nr, short pmvl0[2], short pmvl1[2],char *l0_rFrame, char *l1_rFrame);
extern void perform_mc(StorablePicture *dec_picture, struct img_par *img, Macroblock *currMB, int pred_dir, int i, int j, int list_offset,   int block_size_x, int block_size_y, int curr_mb_field);
#endif

