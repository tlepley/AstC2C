
/*!
 ************************************************************************
 * \file macroblock.h
 *
 * \brief
 *    Arrays for macroblock encoding
 *
 * \author
 *    Inge Lille-Langoy               <inge.lille-langoy@telenor.com>
 *    Copyright (C) 1999 Telenor Satellite Services, Norway
 ************************************************************************
 */

#ifndef _MACROBLOCK_H_
#define _MACROBLOCK_H_


//! single scan pattern
const byte SNGL_SCAN[16][2] =
{
  {0,0},{1,0},{0,1},{0,2},
  {1,1},{2,0},{3,0},{2,1},
  {1,2},{0,3},{1,3},{2,2},
  {3,1},{3,2},{2,3},{3,3}
};

//! field scan pattern
const byte FIELD_SCAN[16][2] =
{
  {0,0},{0,1},{1,0},{0,2},
  {0,3},{1,1},{1,2},{1,3},
  {2,0},{2,1},{2,2},{2,3},
  {3,0},{3,1},{3,2},{3,3}
};


//! gives CBP value from codeword number, both for intra and inter
const unsigned char NCBP[2][48][2]=
{
  {  // 0      1        2       3       4       5       6       7       8       9      10      11
    {15, 0},{ 0, 1},{ 7, 2},{11, 4},{13, 8},{14, 3},{ 3, 5},{ 5,10},{10,12},{12,15},{ 1, 7},{ 2,11},
    { 4,13},{ 8,14},{ 6, 6},{ 9, 9},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},
    { 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},
    { 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0},{ 0, 0}
  },
  {
    {47, 0},{31,16},{15, 1},{ 0, 2},{23, 4},{27, 8},{29,32},{30, 3},{ 7, 5},{11,10},{13,12},{14,15},
    {39,47},{43, 7},{45,11},{46,13},{16,14},{ 3, 6},{ 5, 9},{10,31},{12,35},{19,37},{21,42},{26,44},
    {28,33},{35,34},{37,36},{42,40},{44,39},{ 1,43},{ 2,45},{ 4,46},{ 8,17},{17,18},{18,20},{20,24},
    {24,19},{ 6,21},{ 9,26},{22,28},{25,23},{32,27},{33,29},{34,30},{36,22},{40,25},{38,38},{41,41}
  }
};


//! used to control block sizes : Not used/16x16/16x8/8x16/8x8/8x4/4x8/4x4
const int BLOCK_STEP[8][2]=
{
  {0,0},{4,4},{4,2},{2,4},{2,2},{2,1},{1,2},{1,1}
};

//! Dequantization coefficients
const int dequant_coef[6][4][4] = {
  {{10, 13, 10, 13},{ 13, 16, 13, 16},{10, 13, 10, 13},{ 13, 16, 13, 16}},
  {{11, 14, 11, 14},{ 14, 18, 14, 18},{11, 14, 11, 14},{ 14, 18, 14, 18}},
  {{13, 16, 13, 16},{ 16, 20, 16, 20},{13, 16, 13, 16},{ 16, 20, 16, 20}},
  {{14, 18, 14, 18},{ 18, 23, 18, 23},{14, 18, 14, 18},{ 18, 23, 18, 23}},
  {{16, 20, 16, 20},{ 20, 25, 20, 25},{16, 20, 16, 20},{ 20, 25, 20, 25}},
  {{18, 23, 18, 23},{ 23, 29, 23, 29},{18, 23, 18, 23},{ 23, 29, 23, 29}}
};

const byte QP_SCALE_CR[52]=
{
    0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,
   12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,
   28,29,29,30,31,32,32,33,34,34,35,35,36,36,37,37,
   37,38,38,38,39,39,39,39

};

//! single scan pattern
const byte SNGL_SCAN8x8[64][2] = {
  {0,0}, {1,0}, {0,1}, {0,2}, {1,1}, {2,0}, {3,0}, {2,1}, {1,2}, {0,3}, {0,4}, {1,3}, {2,2}, {3,1}, {4,0}, {5,0},
  {4,1}, {3,2}, {2,3}, {1,4}, {0,5}, {0,6}, {1,5}, {2,4}, {3,3}, {4,2}, {5,1}, {6,0}, {7,0}, {6,1}, {5,2}, {4,3},
  {3,4}, {2,5}, {1,6}, {0,7}, {1,7}, {2,6}, {3,5}, {4,4}, {5,3}, {6,2}, {7,1}, {7,2}, {6,3}, {5,4}, {4,5}, {3,6},
  {2,7}, {3,7}, {4,6}, {5,5}, {6,4}, {7,3}, {7,4}, {6,5}, {5,6}, {4,7}, {5,7}, {6,6}, {7,5}, {7,6}, {6,7}, {7,7}
};


//! field scan pattern
const byte FIELD_SCAN8x8[64][2] = {   // 8x8
  {0,0}, {0,1}, {0,2}, {1,0}, {1,1}, {0,3}, {0,4}, {1,2}, {2,0}, {1,3}, {0,5}, {0,6}, {0,7}, {1,4}, {2,1}, {3,0},
  {2,2}, {1,5}, {1,6}, {1,7}, {2,3}, {3,1}, {4,0}, {3,2}, {2,4}, {2,5}, {2,6}, {2,7}, {3,3}, {4,1}, {5,0}, {4,2},
  {3,4}, {3,5}, {3,6}, {3,7}, {4,3}, {5,1}, {6,0}, {5,2}, {4,4}, {4,5}, {4,6}, {4,7}, {5,3}, {6,1}, {6,2}, {5,4},
  {5,5}, {5,6}, {5,7}, {6,3}, {7,0}, {7,1}, {6,4}, {6,5}, {6,6}, {6,7}, {7,2}, {7,3}, {7,4}, {7,5}, {7,6}, {7,7}
};


static const int dequant_coef8[6][8][8] =
{
  {
    {20,  19, 25, 19, 20, 19, 25, 19},
    {19,  18, 24, 18, 19, 18, 24, 18},
    {25,  24, 32, 24, 25, 24, 32, 24},
    {19,  18, 24, 18, 19, 18, 24, 18},
    {20,  19, 25, 19, 20, 19, 25, 19},
    {19,  18, 24, 18, 19, 18, 24, 18},
    {25,  24, 32, 24, 25, 24, 32, 24},
    {19,  18, 24, 18, 19, 18, 24, 18}
  },
  {
    {22,  21, 28, 21, 22, 21, 28, 21},
    {21,  19, 26, 19, 21, 19, 26, 19},
    {28,  26, 35, 26, 28, 26, 35, 26},
    {21,  19, 26, 19, 21, 19, 26, 19},
    {22,  21, 28, 21, 22, 21, 28, 21},
    {21,  19, 26, 19, 21, 19, 26, 19},
    {28,  26, 35, 26, 28, 26, 35, 26},
    {21,  19, 26, 19, 21, 19, 26, 19}
  },
  {
    {26,  24, 33, 24, 26, 24, 33, 24},
    {24,  23, 31, 23, 24, 23, 31, 23},
    {33,  31, 42, 31, 33, 31, 42, 31},
    {24,  23, 31, 23, 24, 23, 31, 23},
    {26,  24, 33, 24, 26, 24, 33, 24},
    {24,  23, 31, 23, 24, 23, 31, 23},
    {33,  31, 42, 31, 33, 31, 42, 31},
    {24,  23, 31, 23, 24, 23, 31, 23}
  },
  {
    {28,  26, 35, 26, 28, 26, 35, 26},
    {26,  25, 33, 25, 26, 25, 33, 25},
    {35,  33, 45, 33, 35, 33, 45, 33},
    {26,  25, 33, 25, 26, 25, 33, 25},
    {28,  26, 35, 26, 28, 26, 35, 26},
    {26,  25, 33, 25, 26, 25, 33, 25},
    {35,  33, 45, 33, 35, 33, 45, 33},
    {26,  25, 33, 25, 26, 25, 33, 25}
  },
  {
    {32,  30, 40, 30, 32, 30, 40, 30},
    {30,  28, 38, 28, 30, 28, 38, 28},
    {40,  38, 51, 38, 40, 38, 51, 38},
    {30,  28, 38, 28, 30, 28, 38, 28},
    {32,  30, 40, 30, 32, 30, 40, 30},
    {30,  28, 38, 28, 30, 28, 38, 28},
    {40,  38, 51, 38, 40, 38, 51, 38},
    {30,  28, 38, 28, 30, 28, 38, 28}
  },
  {
    {36,  34, 46, 34, 36, 34, 46, 34},
    {34,  32, 43, 32, 34, 32, 43, 32},
    {46,  43, 58, 43, 46, 43, 58, 43},
    {34,  32, 43, 32, 34, 32, 43, 32},
    {36,  34, 46, 34, 36, 34, 46, 34},
    {34,  32, 43, 32, 34, 32, 43, 32},
    {46,  43, 58, 43, 46, 43, 58, 43},
    {34,  32, 43, 32, 34, 32, 43, 32}
  }

};

//! single scan pattern
const byte SCAN_YUV422[8][2] =
{
  {0,0},{0,1},
  {1,0},{0,2},
  {0,3},{1,1},
  {1,2},{1,3}
};

//! look up tables for FRExt_chroma support
const unsigned char subblk_offset_x[3][8][4] =
{
  { {0, 4, 0, 4},
    {0, 4, 0, 4},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0}, },

  { {0, 4, 0, 4},
    {0, 4, 0, 4},
    {0, 4, 0, 4},
    {0, 4, 0, 4},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0}, },

  { {0, 4, 0, 4},
    {8,12, 8,12},
    {0, 4, 0, 4},
    {8,12, 8,12},
    {0, 4, 0, 4},
    {8,12, 8,12},
    {0, 4, 0, 4},
    {8,12, 8,12}  }
};

const unsigned char subblk_offset_y[3][8][4] =
{ { {0, 0, 4, 4},
    {0, 0, 4, 4},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0}, },

  { {0, 0, 4, 4},
    {8, 8,12,12},
    {0, 0, 4, 4},
    {8, 8,12,12},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0}  },

  { {0, 0, 4, 4},
    {0, 0, 4, 4},
    {8, 8,12,12},
    {8, 8,12,12},
    {0, 0, 4, 4},
    {0, 0, 4, 4},
    {8, 8,12,12},
    {8, 8,12,12} }
};


const unsigned char cofuv_blk_x[3][8][4] =
{ { {0, 1, 0, 1},
    {2, 3, 2, 3},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0} },

  { {0, 1, 0, 1},
    {0, 1, 0, 1},
    {2, 3, 2, 3},
    {2, 3, 2, 3},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0} },

  { {0, 1, 0, 1},
    {2, 3, 2, 3},
    {0, 1, 0, 1},
    {2, 3, 2, 3},
    {0, 1, 0, 1},
    {2, 3, 2, 3},
    {0, 1, 0, 1},
    {2, 3, 2, 3} }
};
  
const unsigned char cofuv_blk_y[3][8][4] =
{
  { { 4, 4, 5, 5},
    { 4, 4, 5, 5},
    { 0, 0, 0, 0},
    { 0, 0, 0, 0},
    { 0, 0, 0, 0},
    { 0, 0, 0, 0},
    { 0, 0, 0, 0},
    { 0, 0, 0, 0} },

  { { 4, 4, 5, 5},
    { 6, 6, 7, 7},
    { 4, 4, 5, 5},
    { 6, 6, 7, 7},
    { 0, 0, 0, 0},
    { 0, 0, 0, 0},
    { 0, 0, 0, 0},
    { 0, 0, 0, 0} },

  { { 4, 4, 5, 5},
    { 4, 4, 5, 5},
    { 6, 6, 7, 7},
    { 6, 6, 7, 7},
    { 8, 8, 9, 9},
    { 8, 8, 9, 9},
    {10,10,11,11},
    {10,10,11,11} }
};

static unsigned char cbp_blk_chroma[8][4] =
{ {16, 17, 18, 19},
  {20, 21, 22, 23},
  {24, 25, 26, 27},
  {28, 29, 30, 31},
  {32, 33, 34, 35},
  {36, 37, 38, 39},
  {40, 41, 42, 43},
  {44, 45, 46, 47} };


int   block8x8_idx[3][4][4] =
{ { {0, 1, 2, 3},
    {0, 0, 0, 0},
    {0, 0, 0, 0},
    {0, 0, 0, 0}, },

  { {0, 1, 0, 1},
    {2, 3, 2, 3},
    {0, 0, 0, 0},
    {0, 0, 0, 0}  },

  { {0, 0, 0, 0},
    {1, 1, 1, 1},
    {2, 2, 2, 2},
    {3, 3, 3, 3}  }
};

#endif

