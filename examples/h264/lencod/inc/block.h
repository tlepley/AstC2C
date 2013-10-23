
/*!
 ************************************************************************
 * \file block.h
 *
 * \brief
 *    constant arrays for single block processing
 *
 * \author
 *    Inge Lille-Langoy               <inge.lille-langoy@telenor.com>    \n
 *    Telenor Satellite Services                                         \n
 *    P.O.Box 6914 St.Olavs plass                                        \n
 *    N-0130 Oslo, Norway
 *
 ************************************************************************
 */

#ifndef _BLOCK_H_
#define _BLOCK_H_

//! make chroma QP from quant
extern const byte QP_SCALE_CR[52] ;


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


//! array used to find expencive coefficients
const byte COEFF_COST[2][16] =
{
  {3,2,2,1,1,1,0,0,0,0,0,0,0,0,0,0},
  {9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9}
};



//! bit cost for coefficients
const byte COEFF_BIT_COST[3][16][16]=
{
  { // 2x2 scan (corrested per Gisle's Email 11/23/2000 by StW
    { 3, 5, 7, 9, 9,11,11,11,11,13,13,13,13,13,13,13},
    { 5, 7, 9, 9,11,11,11,11,13,13,13,13,13,13,13,13},
    { 7, 9, 9,11,11,11,11,13,13,13,13,13,13,13,13,15},
    { 7, 9, 9,11,11,11,11,13,13,13,13,13,13,13,13,15},
    { 7, 7, 9, 9, 9, 9,11,11,11,11,11,11,11,11,13,13},
    { 7, 7, 9, 9, 9, 9,11,11,11,11,11,11,11,11,13,13},
    { 7, 7, 9, 9, 9, 9,11,11,11,11,11,11,11,11,13,13},
    { 7, 7, 9, 9, 9, 9,11,11,11,11,11,11,11,11,13,13},
    { 7, 7, 9, 9, 9, 9,11,11,11,11,11,11,11,11,13,13},
    { 7, 7, 9, 9, 9, 9,11,11,11,11,11,11,11,11,13,13},
    { 7, 7, 9, 9, 9, 9,11,11,11,11,11,11,11,11,13,13},
    { 7, 7, 9, 9, 9, 9,11,11,11,11,11,11,11,11,13,13},
    { 7, 7, 9, 9, 9, 9,11,11,11,11,11,11,11,11,13,13},
    { 7, 7, 9, 9, 9, 9,11,11,11,11,11,11,11,11,13,13},
    { 7, 7, 9, 9, 9, 9,11,11,11,11,11,11,11,11,13,13},
    { 7, 7, 9, 9, 9, 9,11,11,11,11,11,11,11,11,13,13},
  },
  {  // double scan
    { 3, 5, 7, 7, 7, 9, 9, 9, 9,11,11,13,13,13,13,15},
    { 5, 9, 9,11,11,13,13,13,13,15,15,15,15,15,15,15},
    { 7,11,11,13,13,13,13,15,15,15,15,15,15,15,15,17},
    { 9,11,11,13,13,13,13,15,15,15,15,15,15,15,15,17},
    { 9,11,11,13,13,13,13,15,15,15,15,15,15,15,15,17},
    {11,11,13,13,13,13,15,15,15,15,15,15,15,15,17,17},
    {11,11,13,13,13,13,15,15,15,15,15,15,15,15,17,17},
    {11,11,13,13,13,13,15,15,15,15,15,15,15,15,17,17},
    {11,11,13,13,13,13,15,15,15,15,15,15,15,15,17,17},
    {11,11,13,13,13,13,15,15,15,15,15,15,15,15,17,17},
    {11,11,13,13,13,13,15,15,15,15,15,15,15,15,17,17},
    {11,11,13,13,13,13,15,15,15,15,15,15,15,15,17,17},
    {11,11,13,13,13,13,15,15,15,15,15,15,15,15,17,17},
    {11,11,13,13,13,13,15,15,15,15,15,15,15,15,17,17},
  },
  {    // single scan
    { 3, 7, 9, 9,11,13,13,15,15,15,15,17,17,17,17,17},
    { 5, 9,11,13,13,15,15,15,15,17,17,17,17,17,17,17},
    { 5, 9,11,13,13,15,15,15,15,17,17,17,17,17,17,17},
    { 7,11,13,13,15,15,15,15,17,17,17,17,17,17,17,17},
    { 7,11,13,13,15,15,15,15,17,17,17,17,17,17,17,17},
    { 7,11,13,13,15,15,15,15,17,17,17,17,17,17,17,17},
    { 9,11,13,13,15,15,15,15,17,17,17,17,17,17,17,17},
    { 9,11,13,13,15,15,15,15,17,17,17,17,17,17,17,17},
    { 9,11,13,13,15,15,15,15,17,17,17,17,17,17,17,17},
    { 9,11,13,13,15,15,15,15,17,17,17,17,17,17,17,17},
    {11,13,13,15,15,15,15,17,17,17,17,17,17,17,17,19},
    {11,13,13,15,15,15,15,17,17,17,17,17,17,17,17,19},
    {11,13,13,15,15,15,15,17,17,17,17,17,17,17,17,19},
    {11,13,13,15,15,15,15,17,17,17,17,17,17,17,17,19},
    {11,13,13,15,15,15,15,17,17,17,17,17,17,17,17,19},
    {11,13,13,15,15,15,15,17,17,17,17,17,17,17,17,19},
  },
};

//! single scan pattern
const byte SCAN_YUV422  [8][2] =
{
  {0,0},{0,1},
  {1,0},{0,2},
  {0,3},{1,1},
  {1,2},{1,3}
};

//! look up tables for FRExt-chroma support
const unsigned char hor_offset[4][4][4] =
  {{{0, 0, 0, 0},
  {0, 0, 0, 0},
  {0, 0, 0, 0},
  {0, 0, 0, 0}},

  {{0, 4, 0, 4},
  {0, 0, 0, 0},
  {0, 0, 0, 0},
  {0, 0, 0, 0}},

  {{0, 4, 0, 4},
  {0, 4, 0, 4},
  {0, 0, 0, 0},
  {0, 0, 0, 0}},

  {{0, 4, 0, 4},
  {8,12, 8,12},
  {0, 4, 0, 4},
  {8,12, 8,12}}};

const unsigned char ver_offset[4][4][4] =
  { {{0, 0, 0, 0},
  {0, 0, 0, 0},
  {0, 0, 0, 0},
  {0, 0, 0, 0}},

  {{0, 0, 4, 4},
  {0, 0, 0, 0},
  {0, 0, 0, 0},
  {0, 0, 0, 0}},

  {{0, 0, 4, 4},
  {8, 8,12,12},
  {0, 0, 0, 0},
  {0, 0, 0, 0}},

  {{0, 0, 4, 4},
  {0, 0, 4, 4},
  {8, 8,12,12},
  {8, 8,12,12}}};

static unsigned char cbp_blk_chroma[8][4] =
  { {16, 17, 18, 19},
    {20, 21, 22, 23},
    {24, 25, 26, 27},
    {28, 29, 30, 31},
    {32, 33, 34, 35},
    {36, 37, 38, 39},
    {40, 41, 42, 43},
    {44, 45, 46, 47} };

#endif

